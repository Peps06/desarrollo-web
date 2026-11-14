# Reporte de Uso Documental en el Proyecto

**Fecha:** 13-11-2026
**Número de reporte:** 01 (Diego Rafael Jiménez Trujano)

---

# 1. Breve descripción de uso

Consultas para decidir cómo armar el detalle del viaje por días y la creación de actividades: cómo evitar consultas repetidas a la base de datos, en qué orden responder los errores 404, 403 y 400, y cómo se muestran las fechas, horas y montos en el JSON.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 13-11-2026 | Claude | [Consulta 1: Problema N+1 en el detalle del viaje](#consulta-1-problema-n1-en-el-detalle-del-viaje) | [Respuesta 1](#respuesta-1) |
| 13-11-2026 | Claude | [Consulta 2: Orden de los errores 404, 403 y 400](#consulta-2-orden-de-los-errores-404-403-y-400) | [Respuesta 2](#respuesta-2) |
| 13-11-2026 | Claude | [Consulta 3: Formato de fechas, horas y montos](#consulta-3-formato-de-fechas-horas-y-montos) | [Respuesta 3](#respuesta-3) |

---

### Consulta 1: Problema N+1 en el detalle del viaje
> "¿Cómo armo el detalle del viaje con sus días y actividades sin hacer una consulta por cada día o actividad?"

#### Respuesta 1
Se hace una consulta por tipo de dato y el armado se hace en memoria. La participación del usuario y el viaje se cargan juntos con `JOIN FETCH`. Las actividades se traen todas con su autor, ordenadas por día y hora. Los votos se cuentan con una consulta agrupada por actividad. El total gastado sale de una suma en la base de datos. Después el servicio agrupa las actividades por fecha y crea una entrada por cada día entre el inicio y el fin del viaje, aunque ese día no tenga actividades. Así el número de consultas es el mismo sin importar cuántos días o actividades haya.

---

### Consulta 2: Orden de los errores 404, 403 y 400
> "¿En qué orden se validan el viaje, la participación y los datos para responder el código correcto?"

#### Respuesta 2
Primero se revisa la sesión (401). Después se revisa que el viaje exista (404), luego que el usuario participe en él (403) y al final los datos enviados (400), como se indica en `api.md`. Un viaje cancelado se trata como inexistente. Con esto un usuario que no participa no obtiene datos del viaje.

---

### Consulta 3: Formato de fechas, horas y montos
> "¿Cómo hago que la hora salga como `HH:mm` y el costo como número con 2 decimales en el JSON?"

#### Respuesta 3
Las fechas se envían y reciben como `yyyy-MM-dd`. Para la hora se usa `@JsonFormat(pattern = "HH:mm")` en la respuesta, porque por defecto Jackson la devuelve con segundos. Los montos se guardan con `BigDecimal` y `setScale(2, RoundingMode.HALF_UP)` para que siempre tengan 2 decimales, y `@Digits` en la petición evita valores que no caben en `DECIMAL(10,2)`.

---

## Observaciones adicionales

Estas consultas sirvieron para decidir la estructura del servicio del detalle y el manejo de errores antes de escribir el código.
