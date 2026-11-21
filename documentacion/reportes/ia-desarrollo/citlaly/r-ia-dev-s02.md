# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 02 (Citlaly Morales Viveros - HU-31 backend: listar gastos por viaje)

---

# 1. Breve descripción de uso

Agregar al backend el endpoint `GET /api/viajes/{id}/gastos`, que documenta `api.md` pero no existía en el código. Incluye la consulta de gastos ordenados por fecha, el método de servicio con control de acceso para participantes, la ampliación de `GastoResponse` con los campos que pide el contrato y el renombre de `totalViaje` a `totalGastado` en la respuesta de alta y edición.

---

# 2. Evidencias de uso

| Concepto                    | Descripción                                                                 |
| :-------------------------- | :-------------------------------------------------------------------------- |
| Herramienta                 | Claude (Anthropic), claude.ai                                               |
| Modelo/temperatura          | Haiku 5.5 medio / "no configurable"                                         |
| Prompt original             | [Ver prompt original](#prompt-original)                                     |
| Respuesta de IA             | [Ver resumen de respuesta](#respuesta-de-ia)                                |
| Código anterior             | Commit `14edfac` (Implementar endpoints y checklist colaborativa de tareas) |
| Código posterior            | Commit `ae5826e` (Registrar gasto en vivo (Fetch))                          |
| Diferencias entre versiones | [Ver diferencias](#diferencias-entre-versiones)                             |
| Prueba de funcionamiento    | [Ver prueba](#prueba)                                                       |
| Código de uso               | Generación                                                                  |
| Reflexión del estudiante    | [Ver reflexión](#reflexion-del-estudiante)                                  |

---

### Prompt original

> "Hola necesito que me ayudes a implementar esto que se vea básico, hecho por estudiantes Recuerda no mencionar que historia de usuario es, en este caso los archivos deben de llevar estrictamente lo de "Autora: Citlaly Morales Viveros" Para este caso en específico no quiero que sobrepienses tanto no hagas pruebas, entrega el primer resultado y explica su funcionamiento uno por uno, cosas que modificaste, con que se ccomunica, etc, el arbol compartido es uno que esta como ejemplo, no teneos todos esos archivos, te paso un comando de git donde sí muestra los archivos existentes, tu dime cual necesitas, antes mira el zip para que comprendas que nos vemos limitados a implementar solo funciones que se utilizaron en ese proyecto Por el momento busco hacer la hu 31 primero, que archivos necesitas?"

Mensaje de seguimiento, con los archivos y la decisión de carpetas:

> "despues de terminar el hu-31, lee solo los archivos necesarios por el momento, te paso los archivos y la decisión: solo trabajamos en backend/.../static/, no actualices la copia espejo."

Se adjuntaron el plan `plan-hu31-hu32-hu39.md` y el zip de ejemplo `ejemplo_de_manera_de_trabajo.zip`, y después los archivos del módulo de gastos (`archivos-gastos.zip`).

### Respuesta de IA

Claude revisó el plan y el zip de ejemplo, pidió los archivos necesarios y entregó la implementación de HU-31 en la carpeta de salida, sin ejecutar pruebas como se le pidió. Los archivos nuevos llevan el encabezado "Autora: Citlaly Morales Viveros".

Cambios en el backend, uno por uno:

1. `GastoRepository.java`: agregó `findByViajeIdOrderByFechaDesc(viajeId)`. Spring Data genera la consulta a partir del nombre, así que no hace falta escribir SQL. Devuelve los gastos del viaje, del más reciente al más antiguo.
2. `GastoService.java`: agregó `listarPorViaje(viajeId, email)`. Revisa que el viaje exista, que el usuario participe (si no, responde 403) y devuelve la lista como `GastoResponse`. Usa `@Transactional(readOnly = true)` porque el nombre del usuario se carga de forma perezosa.
3. `GastoResponse.java`: agregó los campos que documenta `api.md` y que faltaban: `usuarioNombre`, `monedaOriginal`, `montoOriginal`, `tipoCambio` y `comprobanteUrl`. Este último es `null` si el gasto no tiene comprobante.
4. `GastoRegistradoResponse.java`: renombró `totalViaje` a `totalGastado`, porque así lo dice `api.md`.
5. `ViajeDetalleController.java`: agregó `GET /api/viajes/{id}/gastos`. Lo puso aquí y no en `GastoController`, porque ese controlador tiene la ruta base `/api/gastos`. Recibe `GastoService` por el constructor, igual que el resto de dependencias de la clase.

Decisiones y riesgos que señaló la IA:

- Categorías en mayúsculas (COMIDA, TRANSPORTE, etc.), como dice el plan. `api.md` las documenta en minúsculas. Verificado después: el enum `CategoriaGasto.java` sí tiene las categorías en mayúsculas, así que el alta no responde 400 por esto.
- El renombre de `totalViaje`: si algún otro archivo llamaba `getTotalViaje()`, dejaría de compilar. Verificado el 20-11-2026: ningún archivo lo usa.
- `Usuario.getNombre()`: la IA lo asumió para `usuarioNombre`. Verificado: el método existe en `Usuario.java`.
- `.toList()` en el servicio requiere Java 16 o superior. Spring Boot 3 ya exige Java 17, así que está bien.

### Diferencias entre versiones

`git diff --numstat 14edfac ae5826e` (solo los cinco archivos del backend; el commit completo incluye además `frontend/js/gastos.js` y `detalle.html`, que se reportan en `r-ia-dev-s03.md`):

```text
16	3	backend/src/main/java/com/bitacora/viajes/controller/ViajeDetalleController.java
5	5	backend/src/main/java/com/bitacora/viajes/dto/GastoRegistradoResponse.java
36	2	backend/src/main/java/com/bitacora/viajes/dto/GastoResponse.java
3	0	backend/src/main/java/com/bitacora/viajes/repository/GastoRepository.java
21	1	backend/src/main/java/com/bitacora/viajes/service/GastoService.java
```

### Prueba

La prueba en vivo con `curl` está pendiente en WSL. Lo verificado hasta ahora:

- `./mvnw compile` en `backend/` pasa con `EXIT=0` (probado el 20-11-2026 sobre el commit `5f1c439`, que incluye este código).
- Revisión contra los contratos reales: el endpoint existe en el controlador que toca, el servicio revisa participación, y los campos de `GastoResponse` coinciden con `api.md`.

Comandos para la prueba pendiente en WSL (reemplazar `{id}` por el id del viaje de Oaxaca, que se obtiene con `GET /api/viajes`; el puerto es el de tu `application.properties` local, 8081):

```bash
curl -c cookies.txt -H "Content-Type: application/json" -d '{"correo":"ana@example.com","contrasena":"Password123"}' http://localhost:8081/api/auth/login
curl -b cookies.txt http://localhost:8081/api/viajes/{id}/gastos
```

Esperado: `200` con una lista JSON de gastos, el más reciente primero. Con un id de viaje inexistente debe responder `404`; con un viaje en el que la usuaria no participe, `403` (ese caso necesita un segundo viaje, porque en los datos de prueba Ana y Luis participan en el único viaje).

### Reflexión del estudiante

Aprendí que el endpoint para listar los gastos de un viaje va en `ViajeDetalleController` y no en `GastoController`, porque la ruta base de este último es `/api/gastos` y la ruta que pide `api.md` es `/api/viajes/{id}/gastos`. También entendí por qué se renombró `totalViaje` a `totalGastado`: el código debe decir lo mismo que el contrato de `api.md`. Lo que más tuve que revisar fue el control de acceso: solo un participante del viaje puede ver sus gastos.

---

## Observaciones adicionales

- El plan indicaba que el `GET` debía ir en `ViajeDetalleController` y no en `GastoController`, porque este último tiene la ruta base `/api/gastos`.
- El renombre de `totalViaje` se hizo para que el código coincidiera con `api.md`. Verificado el 20-11-2026: ningún archivo usa `getTotalViaje()`.
- El plan que se le pasó a Claude lo había entregado antes Pecas (Muse) en el chat; la implementación de este reporte es de Claude, por eso la herramienta es Claude y no Pecas.
- El código generado lo copió la integrante al repositorio y lo subió en el commit `ae5826e`; Claude no ejecutó pruebas, como se le pidió en el prompt.
