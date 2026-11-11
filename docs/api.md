# API REST – Bitácora de viajes colaborativa

Base: `http://localhost:8080`

---

## 1. Convenciones

| Tema | Regla |
|---|---|
| Prefijo | Todas las rutas cuelgan de `/api`. |
| Formato | JSON (`Content-Type: application/json`). Excepciones: `POST /api/gastos` con comprobante (`multipart/form-data`), el comprobante (imagen) y el PDF. |
| Campos | `camelCase` (`fechaInicio`, `montoOriginal`). |
| Fechas y horas | Fecha `yyyy-MM-dd`, hora `HH:mm`, fecha y hora de registro `yyyy-MM-ddTHH:mm:ss`. |
| Montos | Número con 2 decimales (`1250.00`). `tipoCambio` admite hasta 6 decimales. |
| Moneda | Código de 3 letras (`MXN`, `USD`, `EUR`). `MXN` por defecto. |
| Identificadores | Enteros. |
| Enumeraciones | Minúsculas: `rol` = `organizador` \| `participante`; `categoria` = `comida` \| `transporte` \| `hospedaje` \| `actividades` \| `otros`; `estado` = `activo` \| `cancelado`. |
| Identidad | El usuario sale siempre de la sesión (cookie `JSESSIONID`). El cliente no envía autor, quién pagó ni su propio id. |
| Autenticación | Públicas: `/login`, `/registro`, `/api/auth/**` y archivos estáticos. El resto requiere sesión. |
| Respuestas | Se devuelven DTOs, no entidades. Una lista sin resultados responde `200` con `[]`. |

### Códigos HTTP

| Código | Cuándo |
|---|---|
| `200` | Consulta o actualización correcta. |
| `201` | Recurso creado. |
| `204` | Eliminación o cancelación correcta, sin cuerpo. |
| `400` | Validación fallida o datos inválidos (JSON mal formado, id no numérico, campo nulo, regla de negocio). |
| `401` | Sin sesión o credenciales incorrectas. |
| `403` | Con sesión, pero no participa en el viaje o no tiene permiso. |
| `404` | El recurso no existe. |
| `500` | Error inesperado, con mensaje genérico y sin stack trace. |

### Formato de error

`codigo` es el código HTTP numérico. En validaciones se agrega `errores` con el detalle por campo.

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

### Columna «Acceso»

| Etiqueta | Significado |
|---|---|
| Público | No requiere sesión. |
| Autenticado | Cualquier usuario con sesión. |
| Participante | Aparece en `participante_viaje` del viaje, con cualquier rol. |
| Organizador | Participante con rol `organizador` de ese viaje. |
| Autor u organizador | Quien creó la actividad o el gasto, o el organizador del viaje. |

---

## 2. Endpoints

Los objetos en negritas se definen en la [sección 4](#4-esquemas-de-objetos). Todo endpoint protegido puede responder además `401`.

| # | Verbo | Ruta | Acceso | Cuerpo de petición | Respuesta exitosa | Códigos posibles |
|---|---|---|---|---|---|---|
| **Autenticación** |
| 1 | POST | `/api/auth/registro` | Público | `{nombre, correo, contrasena}` | `201` **UsuarioResponse** | 201, 400 |
| 2 | POST | `/api/auth/login` | Público | `{correo, contrasena}` | `200` **UsuarioResponse** + cookie `JSESSIONID` | 200, 400, 401 |
| 3 | POST | `/api/auth/logout` | Público | — | `204` (cierra la sesión; sin sesión también responde `204`) | 204 |
| **Viajes** |
| 4 | POST | `/api/viajes` | Autenticado | `{destino, fechaInicio, fechaFin, presupuestoTotal, monedaBase}` | `201` **ViajeResponse** (el creador queda como `organizador`) | 201, 400, 401 |
| 5 | GET | `/api/viajes` | Autenticado | Query opcional: `destino`, `rol` | `200` lista de **ViajeResponse** (sin cancelados) | 200, 400, 401 |
| 6 | GET | `/api/viajes/{id}` | Participante | — | `200` **ViajeDetalleResponse** | 200, 401, 403, 404 |
| 7 | PUT | `/api/viajes/{id}` | Organizador | `{destino, fechaInicio, fechaFin, presupuestoTotal, monedaBase}` | `200` **ViajeResponse** | 200, 400, 401, 403, 404 |
| 8 | DELETE | `/api/viajes/{id}` | Organizador | — | `204` (estado `cancelado`, sin borrado físico) | 204, 401, 403, 404 |
| **Participantes** |
| 9 | GET | `/api/viajes/{id}/participantes` | Participante | — | `200` lista de **ParticipanteResponse** | 200, 401, 403, 404 |
| 10 | POST | `/api/viajes/{id}/participantes` | Organizador | `{correo}` | `201` **ParticipanteResponse** (rol `participante`) | 201, 400, 401, 403, 404 |
| 11 | DELETE | `/api/viajes/{id}/participantes/{usuarioId}` | Organizador | — | `204` (conserva sus gastos anteriores) | 204, 400, 401, 403, 404 |
| **Actividades** |
| 12 | POST | `/api/viajes/{id}/actividades` | Participante | `{titulo, descripcion, dia, hora, costoEstimado}` | `201` **ActividadResponse** | 201, 400, 401, 403, 404 |
| 13 | GET | `/api/viajes/{id}/actividades` | Participante | Query opcional: `dia` (`yyyy-MM-dd`), `costoMax` | `200` lista de **ActividadResponse** | 200, 400, 401, 403, 404 |
| 14 | PUT | `/api/actividades/{id}` | Autor u organizador | `{titulo, descripcion, dia, hora, costoEstimado}` | `200` **ActividadResponse** | 200, 400, 401, 403, 404 |
| 15 | DELETE | `/api/actividades/{id}` | Autor u organizador | — | `204` (borra sus votos en cascada) | 204, 401, 403, 404 |
| **Votos** |
| 16 | POST | `/api/actividades/{id}/votos` | Participante | — | `201` **VotoResponse** | 201, 400 (voto duplicado), 401, 403, 404 |
| 17 | DELETE | `/api/actividades/{id}/votos` | Participante | — | `200` **VotoResponse** | 200, 401, 403, 404 (actividad inexistente o sin voto previo) |
| **Gastos** |
| 18 | POST | `/api/gastos` | Participante | JSON: `{viajeId, concepto, categoria, monto, monedaOriginal, montoOriginal, tipoCambio}`, o `multipart/form-data` con esos campos y un archivo opcional `comprobante` | `201` `{gasto: GastoResponse, totalGastado}` | 201, 400, 401, 403, 404 |
| 19 | GET | `/api/viajes/{id}/gastos` | Participante | — | `200` lista de **GastoResponse** (más reciente primero) | 200, 401, 403, 404 |
| 20 | PUT | `/api/gastos/{id}` | Autor u organizador | `{concepto, categoria, monto, monedaOriginal, montoOriginal, tipoCambio}` | `200` `{gasto: GastoResponse, totalGastado}` | 200, 400, 401, 403, 404 |
| 21 | DELETE | `/api/gastos/{id}` | Autor u organizador | — | `204` (borra también el comprobante) | 204, 401, 403, 404 |
| 22 | GET | `/api/gastos/{id}/comprobante` | Participante | — | `200` imagen (`image/jpeg`, `image/png` o `image/webp`) | 200, 401, 403, 404 (gasto inexistente o sin comprobante) |
| **Presupuesto y balance** |
| 23 | GET | `/api/viajes/{id}/presupuesto` | Participante | — | `200` **PresupuestoResponse** | 200, 401, 403, 404 |
| 24 | GET | `/api/viajes/{id}/balance` | Participante | — | `200` **BalanceResponse** | 200, 401, 403, 404 |
| **Tareas** |
| 25 | GET | `/api/viajes/{id}/tareas` | Participante | — | `200` lista de **TareaResponse** | 200, 401, 403, 404 |
| 26 | POST | `/api/viajes/{id}/tareas` | Participante | `{descripcion}` | `201` **TareaResponse** | 201, 400, 401, 403, 404 |
| 27 | PATCH | `/api/tareas/{id}/toggle` | Participante | — (invierte `completada`) | `200` **TareaResponse** | 200, 401, 403, 404 |
| 28 | DELETE | `/api/tareas/{id}` | Participante | — | `204` | 204, 401, 403, 404 |
| **Exportar** |
| 29 | GET | `/api/viajes/{id}/exportar-pdf` | Participante | — | `200` `application/pdf` con `Content-Disposition: attachment; filename="itinerario-{destino}.pdf"` | 200, 401, 403, 404 |

---

## 3. Detalle por grupo

### 3.1 Autenticación

**1. `POST /api/auth/registro`**

Validaciones: `nombre` no vacío, `correo` con formato válido y no repetido, `contrasena` de mínimo 8 caracteres. La contraseña se guarda con hash BCrypt y no se devuelve.

```json
// Petición
{ "nombre": "Diego Trujano", "correo": "diego@correo.com", "contrasena": "clave1234" }

// 201
{ "id": 1, "nombre": "Diego Trujano", "correo": "diego@correo.com" }
```

**2. `POST /api/auth/login`**

Abre la sesión (cookie `JSESSIONID`). Un login incorrecto responde `401` con un mensaje genérico, sin indicar si falló el correo o la contraseña.

```json
// Petición
{ "correo": "diego@correo.com", "contrasena": "clave1234" }

// 200
{ "id": 1, "nombre": "Diego Trujano", "correo": "diego@correo.com" }

// 401
{ "codigo": 401, "mensaje": "Correo o contraseña incorrectos" }
```

**3. `POST /api/auth/logout`** — cierra la sesión.

### 3.2 Viajes

**4. `POST /api/viajes`**

Validaciones: `destino` no vacío, `fechaFin ≥ fechaInicio`, `presupuestoTotal > 0`, `monedaBase` de 3 letras (`MXN` por defecto). El viaje y la fila de `participante_viaje` con rol `organizador` se crean en una sola transacción.

```json
// Petición
{ "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-23",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN" }

// 201
{ "id": 7, "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-23",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN", "estado": "activo", "rol": "organizador" }
```

**5. `GET /api/viajes`**

Devuelve los viajes donde el usuario de la sesión participa, sin los cancelados. Filtros opcionales y combinables: `destino` (coincidencia parcial, sin distinguir mayúsculas) y `rol` (`organizador` o `participante`). Un `rol` inválido responde `400`. Ejemplo: `GET /api/viajes?destino=oax&rol=organizador`.

**6. `GET /api/viajes/{id}`**

Incluye una entrada en `dias` por cada fecha entre `fechaInicio` y `fechaFin`, aunque no tenga actividades. Las actividades de cada día van ordenadas por hora. `actividadMasVotada` es `null` si el día no tiene una ganadora.

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

La actividad más votada de cada día es la de más votos. En caso de empate gana la de menor costo estimado y luego la creada primero. Sin votos, o con empate total, es `null`.

**7. `PUT /api/viajes/{id}`** — mismo cuerpo y validaciones que el `POST`. Si se acortan las fechas y quedan actividades fuera del nuevo rango, responde `400` indicando el conflicto.

**8. `DELETE /api/viajes/{id}`** — cambia el estado a `cancelado`. El viaje deja de aparecer en la lista de todos los participantes.

### 3.3 Participantes

Solo el organizador agrega o quita participantes.

**10. `POST /api/viajes/{id}/participantes`**

```json
// Petición
{ "correo": "dana@correo.com" }

// 201
{ "usuarioId": 2, "nombre": "Dana", "correo": "dana@correo.com", "rol": "participante" }
```

Errores: `404` si el correo no tiene cuenta, `400` si ya participa, `403` si quien llama no es organizador.

**11. `DELETE /api/viajes/{id}/participantes/{usuarioId}`** — `400` si el organizador intenta quitarse a sí mismo. Quien es quitado deja de ver el viaje, votar y registrar gastos; sus gastos anteriores se conservan.

### 3.4 Actividades

**12. `POST /api/viajes/{id}/actividades`**

Validaciones: `titulo` (máx. 150) y `hora` obligatorios, `dia` dentro del rango del viaje, `costoEstimado ≥ 0`, `descripcion` opcional (máx. 500). El autor sale de la sesión.

```json
// Petición
{ "titulo": "Cena en el Mercado 20 de Noviembre", "descripcion": "Probar tlayudas",
  "dia": "2026-12-21", "hora": "20:00", "costoEstimado": 300.00 }

// 201
{ "id": 15, "viajeId": 7, "titulo": "Cena en el Mercado 20 de Noviembre",
  "descripcion": "Probar tlayudas", "dia": "2026-12-21", "hora": "20:00",
  "costoEstimado": 300.00, "autor": { "id": 1, "nombre": "Diego Trujano" },
  "totalVotos": 0, "votadaPorMi": false }
```

**13. `GET /api/viajes/{id}/actividades`** — lista las actividades del viaje. Filtros opcionales y combinables: `dia=yyyy-MM-dd` y `costoMax=500`. Una fecha mal formada responde `400`.

**14. `PUT /api/actividades/{id}`** — mismo cuerpo y validaciones que el `POST`.

**15. `DELETE /api/actividades/{id}`** — elimina la actividad y sus votos en la misma transacción.

### 3.5 Votos

Ambos endpoints devuelven el conteo actualizado:

```json
// 201 (POST) / 200 (DELETE)
{ "actividadId": 12, "totalVotos": 4, "votadaPorMi": true }
```

- `POST`: `400` si ya votó, `403` si no participa en el viaje de la actividad, `404` si la actividad no existe.
- `DELETE`: quita el voto del usuario de la sesión. `404` si la actividad no existe o no tenía voto.
- Un usuario solo puede votar una vez por actividad (restricción única `actividad_id, usuario_id`).

### 3.6 Gastos

Campos de la petición (`POST` y `PUT`):

| Campo | Tipo | Obligatorio | Regla |
|---|---|---|---|
| `viajeId` | entero | Solo en `POST` | Viaje existente (`404`) del que el usuario es participante (`403`). No se puede cambiar con `PUT`. |
| `concepto` | texto (máx. 150) | Sí | No vacío. |
| `categoria` | enum | Sí | `comida`, `transporte`, `hospedaje`, `actividades`, `otros`. |
| `monto` | decimal | Sí | Mayor que 0. Siempre en la moneda base del viaje. |
| `monedaOriginal` | texto (3) | No | Si falta, se usa la moneda base. |
| `montoOriginal` | decimal | No | Monto en `monedaOriginal`. Si falta, igual a `monto`. |
| `tipoCambio` | decimal (6) | No | Si falta, `1`. |

El gasto queda asociado al usuario de la sesión.

**18. `POST /api/gastos`** — acepta dos formas:

1. JSON, sin comprobante (ejemplo en la [sección 5.2](#52-post-apigastos)).
2. `multipart/form-data`, con los mismos campos como partes del formulario y un archivo opcional `comprobante`. Solo `jpg`, `png` o `webp`, máximo 2 MB; otro tipo o tamaño responde `400`. El archivo se guarda en `/uploads` con nombre UUID y en la base de datos solo se guarda la ruta relativa.

**19. `GET /api/viajes/{id}/gastos`** — lista de `GastoResponse`, el más reciente primero.

**20. `PUT /api/gastos/{id}`** — mismas validaciones que el alta. Devuelve el total actualizado del viaje.

**21. `DELETE /api/gastos/{id}`** — elimina el gasto y, si tenía comprobante, también el archivo.

**22. `GET /api/gastos/{id}/comprobante`** — devuelve la imagen del comprobante.

### 3.7 Presupuesto y balance

**23. `GET /api/viajes/{id}/presupuesto`** — ejemplo en la [sección 5.3](#53-get-apiviajesidpresupuesto).

- `totalGastado`: suma de los gastos del viaje.
- `restante`: `presupuestoTotal − totalGastado` (puede ser negativo).
- `porcentajeUsado`: `totalGastado / presupuestoTotal × 100`.
- `gastoPorPersona`: `totalGastado / numParticipantes`.
- Sin gastos: `totalGastado` y `porcentajeUsado` son `0`.

**24. `GET /api/viajes/{id}/balance`**

```json
// 200 — 3 personas; Ana pagó 600, Beto y Cris 0
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

- `cuotaPorPersona` = `totalGastado / numParticipantes`; `saldo` = `pagado − cuota`.
- Saldo positivo: le deben. Saldo negativo: debe. La suma de los saldos es `0`.
- Las transferencias empatan al que más debe con al que más le deben hasta dejar todos los saldos en cero.
- Sin gastos: saldos en `0` y `transferencias: []`. Con un solo participante: sin transferencias.

### 3.8 Tareas

**26. `POST /api/viajes/{id}/tareas`** — `{ "descripcion": "Comprar boletos de autobús" }`. `descripcion` es obligatoria (máx. 255).

**27. `PATCH /api/tareas/{id}/toggle`** — invierte `completada` y devuelve la tarea actualizada.

### 3.9 Exportar PDF

**29. `GET /api/viajes/{id}/exportar-pdf`** — el PDF se genera al vuelo, sin guardarse en disco. Incluye nombre y fechas del viaje, participantes, itinerario por día (hora, título, costo y marca de actividad más votada) y presupuesto (total, gastado y restante). Un viaje sin actividades genera el PDF con el texto «Sin actividades registradas».

---

## 4. Esquemas de objetos

**UsuarioResponse**
```json
{ "id": 1, "nombre": "Diego Trujano", "correo": "diego@correo.com" }
```

**ViajeResponse** — `rol` es el rol del usuario de la sesión en ese viaje.
```json
{ "id": 7, "destino": "Oaxaca", "fechaInicio": "2026-12-20", "fechaFin": "2026-12-23",
  "presupuestoTotal": 20000.00, "monedaBase": "MXN", "estado": "activo", "rol": "organizador" }
```

**ViajeDetalleResponse** — `ViajeResponse` más `totalGastado`, `participantes[]` y `dias[]` (ejemplo en el endpoint 6).

**ParticipanteResponse**
```json
{ "usuarioId": 2, "nombre": "Dana", "correo": "dana@correo.com", "rol": "participante" }
```

**ActividadResponse**
```json
{ "id": 15, "viajeId": 7, "titulo": "Cena en el mercado", "descripcion": "Probar tlayudas",
  "dia": "2026-12-21", "hora": "20:00", "costoEstimado": 300.00,
  "autor": { "id": 1, "nombre": "Diego Trujano" }, "totalVotos": 2, "votadaPorMi": false }
```

**VotoResponse**
```json
{ "actividadId": 15, "totalVotos": 2, "votadaPorMi": true }
```

**GastoResponse** — `comprobanteUrl` es `null` si el gasto no tiene comprobante.
```json
{ "id": 31, "viajeId": 7, "usuarioId": 1, "usuarioNombre": "Diego Trujano",
  "concepto": "Taxi al aeropuerto", "categoria": "transporte",
  "monto": 350.00, "monedaOriginal": "MXN", "montoOriginal": 350.00, "tipoCambio": 1.000000,
  "comprobanteUrl": "/api/gastos/31/comprobante", "fecha": "2026-12-20T08:15:00" }
```

**Respuesta de `POST` y `PUT /api/gastos`**
```json
{ "gasto": { "...": "GastoResponse" }, "totalGastado": 3800.50 }
```

**PresupuestoResponse**
```json
{ "presupuestoTotal": 20000.00, "totalGastado": 3800.50, "restante": 16199.50,
  "porcentajeUsado": 19.00, "numParticipantes": 2, "gastoPorPersona": 1900.25, "monedaBase": "MXN" }
```

**BalanceResponse** — ver endpoint 24 (`saldos[]` y `transferencias[]`).

**TareaResponse**
```json
{ "id": 4, "descripcion": "Comprar boletos de autobús", "completada": false,
  "creadorId": 1, "creadorNombre": "Diego Trujano" }
```

**Error** — ver [formato de error](#formato-de-error).

---

## 5. Ejemplos con Fetch

Todas las peticiones usan `credentials: "same-origin"` para enviar la cookie de sesión.

### 5.1 `POST /api/actividades/{id}/votos`

No lleva cuerpo.

```js
const resp = await fetch(`/api/actividades/${id}/votos`, {
  method: "POST",
  credentials: "same-origin"
});
const datos = await resp.json();
```

```http
POST /api/actividades/12/votos HTTP/1.1
Cookie: JSESSIONID=...
```

**`201 Created`**
```json
{ "actividadId": 12, "totalVotos": 4, "votadaPorMi": true }
```

**`400`** — voto duplicado
```json
{ "codigo": 400, "mensaje": "Ya votaste por esta actividad" }
```

**`403`** — no participa en el viaje de la actividad
```json
{ "codigo": 403, "mensaje": "No tienes permiso para votar en este viaje" }
```

**`404`** — la actividad no existe
```json
{ "codigo": 404, "mensaje": "La actividad no existe" }
```

**`401`** — sin sesión
```json
{ "codigo": 401, "mensaje": "Tu sesión expiró" }
```

Para quitar el voto: `DELETE /api/actividades/12/votos` responde `200` con el mismo formato (`totalVotos: 3`, `votadaPorMi: false`), o `404` si no había voto.

### 5.2 `POST /api/gastos`

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

**`201 Created`** — se guarda en la moneda base con `tipoCambio = 1`
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

**b) Gasto en moneda extranjera** — `monto` ya viene convertido a la moneda base
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
form.append("comprobante", archivo);   // jpg, png o webp, máx. 2 MB
await fetch("/api/gastos", { method: "POST", credentials: "same-origin", body: form });
// No fijar Content-Type: el navegador agrega el boundary
```

**`400`** — validación
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

**`400`** — comprobante inválido
```json
{ "codigo": 400, "mensaje": "El comprobante debe ser una imagen jpg, png o webp de máximo 2 MB" }
```

**`403`**
```json
{ "codigo": 403, "mensaje": "No participas en este viaje" }
```

**`404`**
```json
{ "codigo": 404, "mensaje": "El viaje no existe" }
```

### 5.3 `GET /api/viajes/{id}/presupuesto`

```js
const resp = await fetch(`/api/viajes/${viajeId}/presupuesto`, { credentials: "same-origin" });
const p = await resp.json();
```

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

**`200 OK`** — sin gastos
```json
{ "presupuestoTotal": 20000.00, "totalGastado": 0.00, "restante": 20000.00,
  "porcentajeUsado": 0.00, "numParticipantes": 2, "gastoPorPersona": 0.00, "monedaBase": "MXN" }
```

**`200 OK`** — presupuesto excedido (`restante` negativo)
```json
{ "presupuestoTotal": 5000.00, "totalGastado": 6200.00, "restante": -1200.00,
  "porcentajeUsado": 124.00, "numParticipantes": 2, "gastoPorPersona": 3100.00, "monedaBase": "MXN" }
```

**`403`** `{ "codigo": 403, "mensaje": "No participas en este viaje" }`
**`404`** `{ "codigo": 404, "mensaje": "El viaje no existe" }`
**`401`** `{ "codigo": 401, "mensaje": "Tu sesión expiró" }`

---

## 6. Reglas de acceso

| Situación | Respuesta |
|---|---|
| Sin sesión en una ruta protegida | `401` |
| Recurso que no existe | `404` |
| Usuario con sesión que no participa en el viaje | `403` (no revela datos del viaje) |
| Participante que intenta una acción de organizador (editar o cancelar el viaje, agregar o quitar participantes) | `403` |
| Participante que edita o elimina una actividad o gasto ajeno sin ser organizador | `403` |
| Participante quitado del viaje | `403`; sus gastos anteriores se conservan |

Orden de validación: `401`, `404`, `403` y por último `400`.

---

## 7. Correspondencia con la base de datos

| Campo de la API | Columna de MySQL |
|---|---|
| `contrasena` (solo en la petición) | `usuario.contrasena_hash` (BCrypt) |
| `fechaInicio`, `fechaFin`, `presupuestoTotal`, `monedaBase`, `estado` | `viaje.fecha_inicio`, `fecha_fin`, `presupuesto_total`, `moneda_base`, `estado` |
| `rol` | `participante_viaje.rol` (`ORGANIZADOR` / `PARTICIPANTE`) |
| `costoEstimado`, `autor` | `actividad.costo_estimado`, `actividad.autor_id` |
| `usuarioId` y `usuarioNombre` del gasto | `gasto.usuario_id` (quien pagó) |
| `monedaOriginal`, `montoOriginal`, `tipoCambio` | `gasto.moneda_original`, `monto_original`, `tipo_cambio` |
| `comprobanteUrl` | Derivada de `gasto.imagen_url` (`null` si no hay comprobante) |
| `creadorId` | `tarea.creador_id` |

Las enumeraciones se guardan en mayúsculas en MySQL y se convierten a minúsculas en la API.

Restricciones únicas: `(viaje_id, usuario_id)` en `participante_viaje`, `(actividad_id, usuario_id)` en `voto` y `correo` en `usuario`. Al borrar una actividad se borran sus votos (`ON DELETE CASCADE`).

---

## 8. Pruebas con cURL

```bash
# Login: guarda la cookie de sesión
curl -i -c cookies.txt -H "Content-Type: application/json" \
  -d '{"correo":"diego@correo.com","contrasena":"clave1234"}' \
  http://localhost:8080/api/auth/login

# Listar viajes
curl -b cookies.txt http://localhost:8080/api/viajes

# Votar
curl -b cookies.txt -X POST http://localhost:8080/api/actividades/12/votos

# Registrar gasto
curl -b cookies.txt -H "Content-Type: application/json" \
  -d '{"viajeId":7,"concepto":"Taxi","categoria":"transporte","monto":350.00}' \
  http://localhost:8080/api/gastos

# Presupuesto
curl -b cookies.txt http://localhost:8080/api/viajes/7/presupuesto

# Sin sesión: responde 401
curl -i http://localhost:8080/api/viajes
```
