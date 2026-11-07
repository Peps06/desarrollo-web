# API REST – Bitácora de viajes colaborativa

> **HU-05 · Catálogo de endpoints REST** · Responsable: Diego
> Contrato entre backend y frontend. Todos programamos contra este documento.
> Si algo cambia después del día 2: se avisa en el grupo y se actualiza este archivo **en la misma rama** del cambio (ver [Registro de cambios](#12-registro-de-cambios)).

**Base:** `http://localhost:8080` · **Paquete Java:** `com.bitacora.viajes` · **Base de datos:** MySQL (ver `docs/diagrama-er.png`, HU-04)

---

## 1. Convenciones generales

| Tema | Regla |
|---|---|
| Prefijo | Todas las rutas de la API cuelgan de `/api`. |
| Formato | JSON (`Content-Type: application/json`), salvo `POST /api/gastos` con comprobante (`multipart/form-data`), el comprobante (imagen) y el PDF. |
| Nombres de campos | `camelCase` (`fechaInicio`, `montoOriginal`). Las columnas de MySQL usan `snake_case`; el DTO hace la traducción. |
| Fechas | ISO `yyyy-MM-dd` (`2026-12-20`). Horas `HH:mm` (`09:30`). Fecha y hora de registro: `yyyy-MM-ddTHH:mm:ss`. |
| Montos | Número con **2 decimales** (`1250.00`). Se calculan con `BigDecimal`. `tipoCambio` usa hasta 6 decimales. |
| Moneda | Código de 3 letras (`MXN`, `USD`, `EUR`). `MXN` por defecto. |
| Identificadores | Enteros (`id`, `viajeId`, `usuarioId`, …). |
| Enumeraciones | En minúsculas en la API: `rol` = `organizador` \| `participante`; `categoria` = `comida` \| `transporte` \| `hospedaje` \| `actividades` \| `otros`; `estado` = `activo` \| `cancelado`. (En MySQL se guardan en mayúsculas, como en el ER; el DTO convierte.) |
| Identidad | El usuario **siempre** sale de la sesión (cookie `JSESSIONID`). El cliente **nunca** manda quién es el autor, quién pagó ni su propio `usuarioId`. |
| Autenticación | Públicas: `/login`, `/registro`, `/api/auth/**` y archivos estáticos. **Todo lo demás requiere sesión.** |
| Entidades | La API devuelve **DTOs**, nunca entidades JPA. |
| Filtros vacíos | Una lista sin resultados responde `200` con `[]` (no es error). |

### Códigos HTTP

| Código | Cuándo |
|---|---|
| `200` | Consulta o actualización correcta. |
| `201` | Recurso creado. |
| `204` | Eliminación o cancelación correcta (sin cuerpo). |
| `400` | Validación fallida o datos inválidos (JSON mal formado, id no numérico, campo nulo, regla de negocio). |
| `401` | Sin sesión o credenciales incorrectas. La página redirige a `/login`. |
| `403` | Con sesión, pero no participa en el viaje o no tiene permiso para la acción. |
| `404` | El recurso (viaje, actividad, gasto, tarea, usuario) no existe. |
| `500` | Error inesperado. Mensaje genérico, **sin stack trace**. Ningún dato inválido del usuario debe causar 500. |

### Formato de error (todas las respuestas `4xx` y `5xx`)

`codigo` es el **código HTTP numérico**. En validaciones se añade `errores` con el detalle por campo.

```json
{ "codigo": 404, "mensaje": "El viaje no existe" }
```

```json
{
  "codigo": 400,
  "mensaje": "Datos inválidos",
  "errores": [
    { "campo": "titulo", "mensaje": "El título es obligatorio" },
    { "campo": "dia", "mensaje": "El día debe estar dentro de las fechas del viaje" }
  ]
}
```

### Quién puede llamarlo (columna «Acceso»)

| Etiqueta | Significado |
|---|---|
| **Público** | No requiere sesión. |
| **Autenticado** | Cualquier usuario con sesión. |
| **Participante** | Aparece en `participante_viaje` del viaje (cualquier rol). Si no: `403`. |
| **Organizador** | Participante con rol `organizador` de ese viaje. |
| **Autor u organizador** | Quien creó la actividad/gasto, o el organizador del viaje. |

---

## 2. Resumen de endpoints (una fila por endpoint)

Los objetos en **negrita** (`ViajeResponse`, `ActividadResponse`, …) se definen en la [sección 4](#4-esquemas-de-objetos). Todos los endpoints protegidos pueden devolver además `401`.

| # | Verbo | Ruta | Acceso | Cuerpo de petición | Respuesta exitosa | Códigos posibles |
|---|---|---|---|---|---|---|
| **Autenticación** (HU-08, HU-09) |
| 1 | POST | `/api/auth/registro` | Público | `{nombre, correo, contrasena}` | `201` **UsuarioResponse** | 201, 400 |
| 2 | POST | `/api/auth/login` | Público | `{correo, contrasena}` | `200` **UsuarioResponse** + cookie `JSESSIONID` | 200, 400, 401 |
| 3 | POST | `/api/auth/logout` | Público (`/api/auth/**`) | — | `204` (cierra la sesión; sin sesión también responde `204`) | 204 |
| **Viajes** (HU-10, 11, 12, 23, 24) |
| 4 | POST | `/api/viajes` | Autenticado | `{destino, fechaInicio, fechaFin, presupuestoTotal, monedaBase}` | `201` **ViajeResponse** (el creador queda como `organizador`) | 201, 400, 401 |
| 5 | GET | `/api/viajes` | Autenticado | Query opcional: `destino`, `rol` | `200` lista de **ViajeResponse** (sin cancelados) | 200, 400, 401 |
| 6 | GET | `/api/viajes/{id}` | Participante | — | `200` **ViajeDetalleResponse** | 200, 401, 403, 404 |
| 7 | PUT | `/api/viajes/{id}` | Organizador | `{destino, fechaInicio, fechaFin, presupuestoTotal, monedaBase}` | `200` **ViajeResponse** | 200, 400, 401, 403, 404 |
| 8 | DELETE | `/api/viajes/{id}` | Organizador | — | `204` (estado `cancelado`, sin borrado físico) | 204, 401, 403, 404 |
| **Participantes** (HU-18) |
| 9 | GET | `/api/viajes/{id}/participantes` | Participante | — | `200` lista de **ParticipanteResponse** | 200, 401, 403, 404 |
| 10 | POST | `/api/viajes/{id}/participantes` | Organizador | `{correo}` | `201` **ParticipanteResponse** (rol `participante`) | 201, 400, 401, 403, 404 |
| 11 | DELETE | `/api/viajes/{id}/participantes/{usuarioId}` | Organizador | — | `204` (conserva sus gastos anteriores) | 204, 400, 401, 403, 404 |
| **Actividades** (HU-13, 19, 24) |
| 12 | POST | `/api/viajes/{id}/actividades` | Participante | `{titulo, descripcion, dia, hora, costoEstimado}` | `201` **ActividadResponse** | 201, 400, 401, 403, 404 |
| 13 | GET | `/api/viajes/{id}/actividades` | Participante | Query opcional: `dia` (`yyyy-MM-dd`), `costoMax` | `200` lista de **ActividadResponse** | 200, 400, 401, 403, 404 |
| 14 | PUT | `/api/actividades/{id}` | Autor u organizador | `{titulo, descripcion, dia, hora, costoEstimado}` | `200` **ActividadResponse** | 200, 400, 401, 403, 404 |
| 15 | DELETE | `/api/actividades/{id}` | Autor u organizador | — | `204` (borra sus votos en cascada) | 204, 401, 403, 404 |
| **Votos** (HU-14) |
| 16 | POST | `/api/actividades/{id}/votos` | Participante | — (sin cuerpo) | `201` **VotoResponse** | 201, 400 (voto duplicado), 401, 403, 404 |
| 17 | DELETE | `/api/actividades/{id}/votos` | Participante | — (sin cuerpo) | `200` **VotoResponse** | 200, 401, 403, 404 (actividad inexistente o sin voto previo) |
| **Gastos** (HU-15, 20, 25) |
| 18 | POST | `/api/gastos` | Participante | JSON: `{viajeId, concepto, categoria, monto, monedaOriginal, montoOriginal, tipoCambio}` · o `multipart/form-data` con esos campos + archivo opcional `comprobante` | `201` `{gasto: GastoResponse, totalGastado}` | 201, 400, 401, 403, 404 |
| 19 | GET | `/api/viajes/{id}/gastos` | Participante | — | `200` lista de **GastoResponse** (más reciente primero) | 200, 401, 403, 404 |
| 20 | PUT | `/api/gastos/{id}` | Autor u organizador | `{concepto, categoria, monto, monedaOriginal, montoOriginal, tipoCambio}` | `200` `{gasto: GastoResponse, totalGastado}` | 200, 400, 401, 403, 404 |
| 21 | DELETE | `/api/gastos/{id}` | Autor u organizador | — | `204` (borra también el comprobante de `/uploads`) | 204, 401, 403, 404 |
| 22 | GET | `/api/gastos/{id}/comprobante` | Participante | — | `200` imagen (`image/jpeg`, `image/png` o `image/webp`) | 200, 401, 403, 404 (gasto inexistente o sin comprobante) |
| **Presupuesto y balance** (HU-16, 21) |
| 23 | GET | `/api/viajes/{id}/presupuesto` | Participante | — | `200` **PresupuestoResponse** | 200, 401, 403, 404 |
| 24 | GET | `/api/viajes/{id}/balance` | Participante | — | `200` **BalanceResponse** | 200, 401, 403, 404 |
| **Tareas** (HU-37) |
| 25 | GET | `/api/viajes/{id}/tareas` | Participante | — | `200` lista de **TareaResponse** | 200, 401, 403, 404 |
| 26 | POST | `/api/viajes/{id}/tareas` | Participante | `{descripcion}` | `201` **TareaResponse** | 201, 400, 401, 403, 404 |
| 27 | PATCH | `/api/tareas/{id}/toggle` | Participante | — (invierte `completada`) | `200` **TareaResponse** | 200, 401, 403, 404 |
| 28 | DELETE | `/api/tareas/{id}` | Participante | — | `204` | 204, 401, 403, 404 |
| **Exportar** (HU-34) |
| 29 | GET | `/api/viajes/{id}/exportar-pdf` | Participante | — | `200` `application/pdf` con `Content-Disposition: attachment; filename="itinerario-{destino}.pdf"` | 200, 401, 403, 404 |

**Total: 29 endpoints.**

---

## 3. Detalle por grupo

### 3.1 Autenticación (HU-08, HU-09 · Dana)

**1. `POST /api/auth/registro`**

Validaciones: `nombre` no vacío · `correo` con formato válido y no repetido · `contrasena` de mínimo 8 caracteres. La contraseña se guarda con hash BCrypt y **nunca** se devuelve.

```json
// Petición
{ "nombre": "Diego Trujano", "correo": "diego@correo.com", "contrasena": "clave1234" }

// 201
{ "id": 1, "nombre": "Diego Trujano", "correo": "diego@correo.com" }
```

**2. `POST /api/auth/login`** — abre sesión (cookie `JSESSIONID`). Un login incorrecto responde `401` con **mensaje genérico** (no revela si falló el correo o la contraseña).

```json
// Petición
{ "correo": "diego@correo.com", "contrasena": "clave1234" }

// 200
{ "id": 1, "nombre": "Diego Trujano", "correo": "diego@correo.com" }

// 401
{ "codigo": 401, "mensaje": "Correo o contraseña incorrectos" }
```

**3. `POST /api/auth/logout`** — cierra la sesión. `204` sin cuerpo.

### 3.2 Viajes (HU-10, 11, 12, 23, 24 · Stephy y Diego)

**4. `POST /api/viajes`** (Stephy)
Validaciones: `destino` no vacío · `fechaFin ≥ fechaInicio` · `presupuestoTotal > 0` · `monedaBase` de 3 letras (`MXN` por defecto). El viaje y la fila de `participante_viaje` (rol `organizador`) se crean en una sola transacción.

```json
// Petición
{ "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-23",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN" }

// 201
{ "id": 7, "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-23",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN", "estado": "activo", "rol": "organizador" }
```

**5. `GET /api/viajes`** (Stephy / Diego)
Devuelve solo los viajes donde el usuario de la sesión participa, **sin los cancelados**. Filtros opcionales y combinables: `destino` (coincidencia parcial, sin distinguir mayúsculas) y `rol` (`organizador` o `participante`). Rol inválido → `400`. Ejemplo: `GET /api/viajes?destino=oax&rol=organizador`.

**6. `GET /api/viajes/{id}`** (Diego, HU-12) — contrato que usa la línea de tiempo del detalle (HU-29).

Devuelve una entrada en `dias` por **cada fecha** entre `fechaInicio` y `fechaFin`, aunque no tenga actividades. Las actividades de cada día van ordenadas por hora. `actividadMasVotada` es `null` si no hay ganadora (HU-22).

```json
// 200
{
  "id": 7, "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-22",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN", "estado": "activo", "rol": "organizador",
  "totalGastado": 3450.50,
  "participantes": [
    { "usuarioId": 1, "nombre": "Diego Trujano", "rol": "organizador" },
    { "usuarioId": 2, "nombre": "Dana", "rol": "participante" }
  ],
  "dias": [
    {
      "fecha": "2026-12-20",
      "actividadMasVotada": { "id": 12, "titulo": "Visita a Monte Albán", "totalVotos": 3 },
      "actividades": [
        { "id": 12, "titulo": "Visita a Monte Albán", "descripcion": "Recorrido de 3 h",
          "hora": "09:00", "costoEstimado": 150.00,
          "autor": { "id": 1, "nombre": "Diego Trujano" }, "totalVotos": 3, "votadaPorMi": true }
      ]
    },
    { "fecha": "2026-12-21", "actividadMasVotada": null, "actividades": [] },
    { "fecha": "2026-12-22", "actividadMasVotada": null, "actividades": [] }
  ]
}
```

**7. `PUT /api/viajes/{id}`** (Stephy) — mismo cuerpo y validaciones que el `POST`. Si se acortan las fechas y quedan actividades fuera del nuevo rango → `400` explicando el conflicto.

**8. `DELETE /api/viajes/{id}`** (Stephy) — **cancela** el viaje (`estado = cancelado`), no lo borra. Deja de aparecer en la lista de todos los participantes.

### 3.3 Participantes (HU-18 · Stephy)

Solo el organizador agrega o quita participantes.

**10. `POST /api/viajes/{id}/participantes`**

```json
// Petición
{ "correo": "dana@correo.com" }

// 201
{ "usuarioId": 2, "nombre": "Dana", "correo": "dana@correo.com", "rol": "participante" }
```

Errores: `404` correo sin cuenta · `400` ya participa · `403` quien llama no es organizador.

**11. `DELETE /api/viajes/{id}/participantes/{usuarioId}`** — `400` si el organizador intenta quitarse a sí mismo. Al quitar a alguien deja de poder ver el viaje, votar y registrar gastos; **sus gastos anteriores se conservan**.

### 3.4 Actividades (HU-13, 19, 24 · Diego)

**12. `POST /api/viajes/{id}/actividades`**
Validaciones: `titulo` y `hora` obligatorios · `dia` dentro del rango `fechaInicio`–`fechaFin` del viaje · `costoEstimado ≥ 0` · `descripcion` opcional (máx. 500) · `titulo` máx. 150. El autor sale de la sesión.

```json
// Petición
{ "titulo": "Cena en el Mercado 20 de Noviembre", "descripcion": "Probar tlayudas",
  "dia": "2026-12-21", "hora": "20:00", "costoEstimado": 300.00 }

// 201 (ActividadResponse)
{ "id": 15, "viajeId": 7, "titulo": "Cena en el Mercado 20 de Noviembre",
  "descripcion": "Probar tlayudas", "dia": "2026-12-21", "hora": "20:00",
  "costoEstimado": 300.00, "autor": { "id": 1, "nombre": "Diego Trujano" },
  "totalVotos": 0, "votadaPorMi": false }
```

**13. `GET /api/viajes/{id}/actividades`** — lista plana de actividades del viaje. Filtros opcionales y combinables: `dia=yyyy-MM-dd` y `costoMax=500`. Fecha mal formada → `400`.

**14. `PUT /api/actividades/{id}`** — mismo cuerpo y validaciones que el `POST`. `403` si no es el autor ni el organizador.

**15. `DELETE /api/actividades/{id}`** — elimina la actividad y, en cascada, sus votos (misma transacción).

### 3.5 Votos (HU-14 · Alfonso)

Ambos endpoints devuelven el conteo actualizado, para que el JavaScript (HU-30) refresque el contador sin otra consulta.

```json
// 201 (POST) / 200 (DELETE)
{ "actividadId": 12, "totalVotos": 4, "votadaPorMi": true }
```

- `POST`: `400` si ya votó (voto duplicado) · `403` si no participa en el viaje de la actividad · `404` si la actividad no existe.
- `DELETE`: quita **su propio** voto. `404` si la actividad no existe o si no tenía voto.
- Respaldo en BD: restricción única `(actividad_id, usuario_id)`.

### 3.6 Gastos (HU-15, 20, 25 · Citlaly; conversor de divisas en HU-38 · Dana)

**Campos de la petición (`POST` y `PUT`):**

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `viajeId` | entero | Solo en `POST` | Viaje existente (`404`) del que el usuario es participante (`403`). |
| `concepto` | texto (máx. 150) | Sí | No vacío. |
| `categoria` | enum | Sí | `comida`, `transporte`, `hospedaje`, `actividades`, `otros`. |
| `monto` | decimal | Sí | `> 0`. **Siempre en la moneda base del viaje** (ya convertido). |
| `monedaOriginal` | texto (3) | No | Si falta, se usa la moneda base. |
| `montoOriginal` | decimal | No | Monto en `monedaOriginal`. Si falta, igual a `monto`. |
| `tipoCambio` | decimal (6) | No | Si falta, `1`. Se guarda para que el gasto no cambie después. |

El gasto se asocia al usuario de la sesión: el cliente no manda quién pagó.

**18. `POST /api/gastos`** — dos formas, el comportamiento es el mismo:

1. **JSON** (sin comprobante): ver ejemplo en la [sección 5.2](#52-post-apigastos).
2. **`multipart/form-data`** (con comprobante): los mismos campos como partes de formulario más el archivo opcional `comprobante`. Solo `jpg`, `png` o `webp`, **máximo 2 MB**; otro tipo o tamaño → `400`. El archivo se guarda en `/uploads` con nombre UUID; en la BD solo se guarda la ruta relativa. En el frontend usar `FormData` y **no** fijar el `Content-Type` a mano.

**19. `GET /api/viajes/{id}/gastos`** — lista de `GastoResponse`, el más reciente primero.

**20. `PUT /api/gastos/{id}`** — mismas validaciones que el alta. Respuesta con el total actualizado del viaje.

**21. `DELETE /api/gastos/{id}`** — elimina el gasto y, si tenía comprobante, también el archivo de `/uploads`. El presupuesto se recalcula.

**22. `GET /api/gastos/{id}/comprobante`** — devuelve la imagen (no expone la carpeta `/uploads`). Solo participantes del viaje.

### 3.7 Presupuesto y balance (HU-16, 21 · Alfonso)

**23. `GET /api/viajes/{id}/presupuesto`** — ejemplo completo en la [sección 5.3](#53-get-apiviajesidpresupuesto). Cálculo en `PresupuestoService`:

- `totalGastado` = suma de los gastos del viaje
- `restante` = `presupuestoTotal − totalGastado` (**puede ser negativo**)
- `porcentajeUsado` = `totalGastado / presupuestoTotal × 100`
- `gastoPorPersona` = `totalGastado / numParticipantes`
- Sin gastos: `totalGastado = 0` y `porcentajeUsado = 0`. Nunca división entre cero.

**24. `GET /api/viajes/{id}/balance`**

```json
// 200 — ejemplo: 3 personas; A pagó 600, B y C 0
{
  "cuotaPorPersona": 200.00,
  "totalGastado": 600.00,
  "saldos": [
    { "usuarioId": 1, "nombre": "Ana",  "pagado": 600.00, "saldo": 400.00 },
    { "usuarioId": 2, "nombre": "Beto", "pagado": 0.00,   "saldo": -200.00 },
    { "usuarioId": 3, "nombre": "Cris", "pagado": 0.00,   "saldo": -200.00 }
  ],
  "transferencias": [
    { "deudorId": 2, "deudorNombre": "Beto", "acreedorId": 1, "acreedorNombre": "Ana", "monto": 200.00 },
    { "deudorId": 3, "deudorNombre": "Cris", "acreedorId": 1, "acreedorNombre": "Ana", "monto": 200.00 }
  ]
}
```

`saldo` positivo = le deben; negativo = debe. La suma de los saldos es `0`. Sin gastos: saldos en `0` y `transferencias: []`. Con un solo participante: sin transferencias.

### 3.8 Tareas (HU-37 · Stephy)

**26. `POST /api/viajes/{id}/tareas`** — `{ "descripcion": "Comprar boletos de autobús" }` → `201` `TareaResponse`. `descripcion` obligatoria (máx. 255).

**27. `PATCH /api/tareas/{id}/toggle`** — invierte `completada` y devuelve la tarea actualizada. El contador «X de Y completadas» lo calcula el frontend sobre la lista del `GET`.

### 3.9 Exportar PDF (HU-34 · Diego)

**29. `GET /api/viajes/{id}/exportar-pdf`** — responde `application/pdf` con descarga. Se genera al vuelo con OpenPDF (`PdfService`), sin guardarse en disco. Incluye nombre y fechas del viaje, participantes, itinerario por día (hora, título, costo y marca de actividad más votada) y presupuesto (total, gastado, restante). Un viaje sin actividades genera el PDF con el texto «Sin actividades registradas».

---

## 4. Esquemas de objetos

Los nombres coinciden con los DTO del árbol del proyecto.

**UsuarioResponse** · `UsuarioResponse.java`
```json
{ "id": 1, "nombre": "Diego Trujano", "correo": "diego@correo.com" }
```

**ViajeResponse** · `ViajeResponse.java` — `rol` es el rol del usuario de la sesión en ese viaje.
```json
{ "id": 7, "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-23",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN", "estado": "activo", "rol": "organizador" }
```

**ViajeDetalleResponse** · `ViajeDetalleResponse.java` / **DiaResponse** · `DiaResponse.java` — ver [ejemplo del endpoint 6](#32-viajes-hu-10-11-12-23-24--stephy-y-diego). Es `ViajeResponse` + `totalGastado`, `participantes[]` y `dias[]`.

**ParticipanteResponse** · `ParticipanteResponse.java`
```json
{ "usuarioId": 2, "nombre": "Dana", "correo": "dana@correo.com", "rol": "participante" }
```

**ActividadResponse** · `ActividadResponse.java`
```json
{ "id": 15, "viajeId": 7, "titulo": "Cena en el mercado", "descripcion": "Probar tlayudas",
  "dia": "2026-12-21", "hora": "20:00", "costoEstimado": 300.00,
  "autor": { "id": 1, "nombre": "Diego Trujano" }, "totalVotos": 2, "votadaPorMi": false }
```

**VotoResponse** · `VotoResponse.java`
```json
{ "actividadId": 15, "totalVotos": 2, "votadaPorMi": true }
```

**GastoResponse** · `GastoResponse.java` — `comprobanteUrl` es `null` si el gasto no tiene comprobante; nunca se expone la ruta de `/uploads`.
```json
{ "id": 31, "viajeId": 7, "usuarioId": 1, "usuarioNombre": "Diego Trujano",
  "concepto": "Taxi al aeropuerto", "categoria": "transporte",
  "monto": 350.00, "monedaOriginal": "MXN", "montoOriginal": 350.00, "tipoCambio": 1.000000,
  "comprobanteUrl": "/api/gastos/31/comprobante", "fecha": "2026-12-20T08:15:00" }
```

**GastoRegistradoResponse** · `GastoRegistradoResponse.java` — respuesta de `POST` y `PUT /api/gastos`
```json
{ "gasto": { "...": "GastoResponse" }, "totalGastado": 3800.50 }
```

**PresupuestoResponse** · `PresupuestoResponse.java`
```json
{ "presupuestoTotal": 20000.00, "totalGastado": 3800.50, "restante": 16199.50,
  "porcentajeUsado": 19.00, "numParticipantes": 2, "gastoPorPersona": 1900.25, "monedaBase": "MXN" }
```

**BalanceResponse** · `BalanceResponse.java` / **SaldoResponse** · `SaldoResponse.java` / **TransferenciaResponse** · `TransferenciaResponse.java` — ver [endpoint 24](#37-presupuesto-y-balance-hu-16-21--alfonso).

**TareaResponse** · `TareaResponse.java`
```json
{ "id": 4, "descripcion": "Comprar boletos de autobús", "completada": false,
  "creadorId": 1, "creadorNombre": "Diego Trujano" }
```

**ErrorResponse** · `ErrorResponse.java` / **ErrorCampo** · `ErrorCampo.java` — ver [formato de error](#formato-de-error-todas-las-respuestas-4xx-y-5xx).

---

## 5. Ejemplos completos de los 3 endpoints con Fetch

Los tres los llama el JavaScript del frontend. En `fetch` usar siempre `credentials: "same-origin"` (misma origin; envía la cookie de sesión).

### 5.1 `POST /api/actividades/{id}/votos`

**Uso:** `votos.js` (HU-30). No lleva cuerpo.

```js
const resp = await fetch(`/api/actividades/${id}/votos`, {
  method: "POST",
  credentials: "same-origin"
});
const datos = await resp.json();
```

**Petición**
```http
POST /api/actividades/12/votos HTTP/1.1
Cookie: JSESSIONID=...
```

**`201 Created`**
```json
{ "actividadId": 12, "totalVotos": 4, "votadaPorMi": true }
```

**`400 Bad Request`** — voto duplicado
```json
{ "codigo": 400, "mensaje": "Ya votaste por esta actividad" }
```

**`403 Forbidden`** — no participa en el viaje de la actividad
```json
{ "codigo": 403, "mensaje": "No tienes permiso para votar en este viaje" }
```

**`404 Not Found`** — la actividad no existe
```json
{ "codigo": 404, "mensaje": "La actividad no existe" }
```

**`401 Unauthorized`** — sin sesión
```json
{ "codigo": 401, "mensaje": "Tu sesión expiró" }
```

Para quitar el voto: `DELETE /api/actividades/12/votos` → `200` con el mismo formato (`totalVotos: 3`, `votadaPorMi: false`); `404` si no había voto.

### 5.2 `POST /api/gastos`

**Uso:** `gastos.js` (HU-31), conversor de divisas `divisas.js` (HU-38) y comprobantes `comprobantes.js` (HU-39).

**a) Gasto en la moneda base (JSON)**

```js
const resp = await fetch("/api/gastos", {
  method: "POST",
  credentials: "same-origin",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    viajeId: 7, concepto: "Taxi al aeropuerto", categoria: "transporte", monto: 350.00
  })
});
```

```http
POST /api/gastos HTTP/1.1
Content-Type: application/json

{ "viajeId": 7, "concepto": "Taxi al aeropuerto", "categoria": "transporte", "monto": 350.00 }
```

**`201 Created`** (se guarda en la moneda base con `tipoCambio = 1`)
```json
{
  "gasto": {
    "id": 31, "viajeId": 7, "usuarioId": 1, "usuarioNombre": "Diego Trujano",
    "concepto": "Taxi al aeropuerto", "categoria": "transporte",
    "monto": 350.00, "monedaOriginal": "MXN", "montoOriginal": 350.00, "tipoCambio": 1.000000,
    "comprobanteUrl": null, "fecha": "2026-12-20T08:15:00"
  },
  "totalGastado": 3800.50
}
```

**b) Gasto en moneda extranjera** (el frontend envía `monto` ya convertido)

```json
{ "viajeId": 7, "concepto": "Museo", "categoria": "actividades",
  "monto": 360.00, "monedaOriginal": "USD", "montoOriginal": 20.00, "tipoCambio": 18.000000 }
```

**c) Con comprobante (`multipart/form-data`)**

```js
const form = new FormData();
form.append("viajeId", 7);
form.append("concepto", "Cena");
form.append("categoria", "comida");
form.append("monto", "480.00");
form.append("comprobante", archivo);          // File: jpg, png o webp, máx. 2 MB
await fetch("/api/gastos", { method: "POST", credentials: "same-origin", body: form });
// NO fijar Content-Type: el navegador agrega el boundary
```

**`400 Bad Request`** — validación (monto 0 y concepto vacío)
```json
{
  "codigo": 400,
  "mensaje": "Datos inválidos",
  "errores": [
    { "campo": "monto", "mensaje": "El monto debe ser mayor a 0" },
    { "campo": "concepto", "mensaje": "El concepto es obligatorio" }
  ]
}
```

**`400 Bad Request`** — comprobante inválido
```json
{ "codigo": 400, "mensaje": "El comprobante debe ser una imagen jpg, png o webp de máximo 2 MB" }
```

**`403 Forbidden`**
```json
{ "codigo": 403, "mensaje": "No participas en este viaje" }
```

**`404 Not Found`**
```json
{ "codigo": 404, "mensaje": "El viaje no existe" }
```

### 5.3 `GET /api/viajes/{id}/presupuesto`

**Uso:** `presupuesto.js` (HU-32): alimenta la barra de progreso. Se vuelve a llamar tras el evento `gasto-registrado`.

```js
const resp = await fetch(`/api/viajes/${viajeId}/presupuesto`, { credentials: "same-origin" });
const p = await resp.json();
```

**Petición**
```http
GET /api/viajes/7/presupuesto HTTP/1.1
Cookie: JSESSIONID=...
```

**`200 OK`**
```json
{
  "presupuestoTotal": 20000.00,
  "totalGastado": 3800.50,
  "restante": 16199.50,
  "porcentajeUsado": 19.00,
  "numParticipantes": 2,
  "gastoPorPersona": 1900.25,
  "monedaBase": "MXN"
}
```

**`200 OK`** — viaje sin gastos (no hay error ni división entre cero)
```json
{ "presupuestoTotal": 20000.00, "totalGastado": 0.00, "restante": 20000.00,
  "porcentajeUsado": 0.00, "numParticipantes": 2, "gastoPorPersona": 0.00, "monedaBase": "MXN" }
```

**`200 OK`** — presupuesto excedido (`restante` negativo; la barra se pone roja)
```json
{ "presupuestoTotal": 5000.00, "totalGastado": 6200.00, "restante": -1200.00,
  "porcentajeUsado": 124.00, "numParticipantes": 2, "gastoPorPersona": 3100.00, "monedaBase": "MXN" }
```

| Estado de la barra | `porcentajeUsado` |
|---|---|
| Verde | `< 75` |
| Amarilla | `75 – 100` |
| Roja | `> 100` |

**`403`** `{ "codigo": 403, "mensaje": "No participas en este viaje" }` · **`404`** `{ "codigo": 404, "mensaje": "El viaje no existe" }` · **`401`** `{ "codigo": 401, "mensaje": "Tu sesión expiró" }`

---

## 6. Mensajes de error sugeridos para la interfaz

`mostrarMensaje(tipo, texto)` (HU-36) traduce el código HTTP a un texto entendible:

| Código | Mensaje en pantalla |
|---|---|
| 401 | «Tu sesión expiró» y redirige a `/login` |
| 403 | «No tienes permiso» |
| 404 | «No se encontró» |
| 400 | Se muestra el `mensaje` (o cada `errores[].mensaje` junto a su campo) |
| Red caída | «Sin conexión con el servidor» |

---

## 7. Mapa de endpoints por historia de usuario

| HU | Responsable | Endpoints (#) |
|---|---|---|
| HU-08 | Dana | 1 |
| HU-09 | Dana | 2, 3 |
| HU-10 | Stephy | 4 |
| HU-11 | Stephy | 5 |
| HU-12 | Diego | 6 |
| HU-13 | Diego | 12 |
| HU-14 | Alfonso | 16, 17 |
| HU-15 | Citlaly | 18, 19 |
| HU-16 | Alfonso | 23 |
| HU-18 | Stephy | 9, 10, 11 |
| HU-19 | Diego | 14, 15 |
| HU-20 | Citlaly | 20, 21 |
| HU-21 | Alfonso | 24 |
| HU-22 | Alfonso | (campo `actividadMasVotada` del #6) |
| HU-23 | Stephy | 7, 8 |
| HU-24 | Diego | filtros de #5 y #13 (`GET /api/viajes/{id}/actividades` se crea aquí si aún no existe) |
| HU-25 | Citlaly | 18 (multipart), 22 |
| HU-34 | Diego | 29 |
| HU-37 | Stephy | 25, 26, 27, 28 |
| HU-38 | Dana | 18 (campos de moneda) |
| HU-17 | Dana | Formato de error de todos los endpoints |

---

## 8. Reglas de acceso (resumen)

| Situación | Respuesta |
|---|---|
| Sin sesión en cualquier ruta protegida | `401` |
| Viaje o recurso que no existe | `404` |
| Usuario con sesión que **no participa** en el viaje | `403` (nunca revela datos del viaje) |
| Participante que intenta una acción de organizador (editar/cancelar viaje, agregar/quitar participantes) | `403` |
| Participante que edita o elimina una actividad/gasto ajeno (y no es organizador) | `403` |
| Participante quitado del viaje | Pierde acceso (`403`); sus gastos anteriores se conservan |

> **Orden de validación recomendado:** `401` (sin sesión) → `404` (recurso inexistente) → `403` (sin permiso) → `400` (datos inválidos). Así el frontend siempre recibe el error más específico.

---

## 9. Relación con el modelo de datos (HU-04)

| Campo de la API | Columna de MySQL |
|---|---|
| `usuario.contrasena` (solo petición) | `usuario.contrasena_hash` (BCrypt) |
| `fechaInicio`, `fechaFin`, `presupuestoTotal`, `monedaBase`, `estado` | `viaje.fecha_inicio`, `fecha_fin`, `presupuesto_total`, `moneda_base`, `estado` |
| `rol` | `participante_viaje.rol` (`ORGANIZADOR` / `PARTICIPANTE`) |
| `costoEstimado`, `autor` | `actividad.costo_estimado`, `actividad.autor_id` |
| `usuarioId` / `usuarioNombre` del gasto | `gasto.usuario_id` (quien pagó) |
| `monedaOriginal`, `montoOriginal`, `tipoCambio` | `gasto.moneda_original`, `monto_original`, `tipo_cambio` |
| `comprobanteUrl` | Derivada de `gasto.imagen_url` (nula si no hay comprobante) |
| `creadorId` | `tarea.creador_id` |

Restricciones que respaldan las reglas ante peticiones simultáneas: `UNIQUE (viaje_id, usuario_id)` en `participante_viaje`, `UNIQUE (actividad_id, usuario_id)` en `voto`, `UNIQUE (correo)` en `usuario`, y `ON DELETE CASCADE` de `voto` hacia `actividad`.

---

## 10. Cómo probar con cURL (cookie jar)

```bash
# 1) Login: guarda la cookie de sesión
curl -i -c cookies.txt -H "Content-Type: application/json" \
  -d '{"correo":"diego@correo.com","contrasena":"clave1234"}' \
  http://localhost:8080/api/auth/login

# 2) Usar la sesión
curl -b cookies.txt http://localhost:8080/api/viajes

# 3) Votar
curl -b cookies.txt -X POST http://localhost:8080/api/actividades/12/votos

# 4) Registrar gasto
curl -b cookies.txt -H "Content-Type: application/json" \
  -d '{"viajeId":7,"concepto":"Taxi","categoria":"transporte","monto":350.00}' \
  http://localhost:8080/api/gastos

# 5) Presupuesto
curl -b cookies.txt http://localhost:8080/api/viajes/7/presupuesto

# 6) Sin sesión debe dar 401
curl -i http://localhost:8080/api/viajes
```

---

## 11. Decisiones tomadas en este catálogo (para revisión del equipo)

Estos puntos no estaban definidos en las HU; se fijaron aquí para que todos programemos igual. Si alguien no está de acuerdo, se resuelve el día 2 en la revisión.

1. `codigo` en el formato de error es el **código HTTP numérico**.
2. Los valores de enumeraciones (`rol`, `categoria`, `estado`) viajan en **minúsculas** en la API y en mayúsculas en MySQL.
3. `POST` de votos responde `201`; el `DELETE` de votos responde `200` (ambos con `VotoResponse`).
4. `DELETE` de viaje, participante, actividad, gasto y tarea responde `204` sin cuerpo; `POST /api/auth/logout` también.
5. `PUT /api/gastos/{id}` **no** permite cambiar el `viajeId`.
6. El gasto expone `comprobanteUrl` (`/api/gastos/{id}/comprobante` o `null`) en lugar de la ruta de `imagen_url`.
7. `monto` siempre va en la moneda base del viaje; el frontend (HU-38) hace la conversión antes de enviar.
8. `GET /api/viajes/{id}/tareas` devuelve la lista simple; el contador «X de Y» se calcula en el frontend.
9. Orden de validación: 401 → 404 → 403 → 400.

---

## 12. Registro de cambios

| Fecha | Autor | Cambio |
|---|---|---|
| 2026-11-06 | Diego | Versión inicial del catálogo (HU-05): 29 endpoints, esquemas y 3 ejemplos Fetch. |
