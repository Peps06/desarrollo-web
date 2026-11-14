# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 13-11-2026
**Número de reporte:** 01 (Diego Rafael Jiménez Trujano - Detalle del viaje por días y Proponer actividad)

---

# 1. Breve descripción de uso

Apoyo en la codificación del backend para **Detalle del viaje por días (`GET /api/viajes/{id}`)** y **Proponer actividad (`POST /api/viajes/{id}/actividades`)**, siguiendo la arquitectura de 3 capas del sistema (`Controller`, `Service`, `Repository` y `DTO`), reutilizando `ViajeResponse`, `ErrorResponse` y las consultas que ya existían en `ParticipanteViajeRepository`.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Sonnet 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `6015521` (Reportes de IA Dana) |
| **Código posterior** | Commit `77663df` (Detalle del viaje por dias y proponer actividad backend) |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat 6015521 77663df` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver pruebas](#pruebas) con `cURL` y cookie `JSESSIONID` |
| **Código de uso** | Generación / Depuración / Explicación |
| **Reflexión del estudiante** | Entendí que para el detalle del viaje no conviene consultar actividad por actividad, porque cada día o actividad generaría consultas extra (problema N+1). Por eso se hace una consulta por tipo de dato (participación, participantes, actividades, votos y gastos) y el armado por días se hace en el servicio. También aprendí que el orden de las respuestas de error importa: primero 404 si el viaje no existe, luego 403 si el usuario no participa y al final 400 por datos inválidos. |

---

### Prompt original
> "Actúa como desarrollador backend con experiencia en Spring Boot 3, Spring Data JPA y Spring Security. Trabajas en el repositorio de la 'Bitácora de viajes colaborativa' (Java 17, MySQL, arquitectura de 3 capas: Controller, Service, Repository y DTO). Revisa el código existente y completa los archivos que están vacíos para implementar dos funcionalidades:
>
> 1. Detalle del viaje por días: devuelve los datos generales del viaje, los participantes con su rol, el total gastado y una entrada por cada fecha entre `fechaInicio` y `fechaFin`, aunque el día no tenga actividades. Las actividades de cada día van ordenadas por hora e incluyen autor, total de votos y si el usuario en sesión ya votó.
> 2. Proponer actividad: recibe título, descripción, día, hora y costo estimado. El título y la hora son obligatorios, el día debe estar dentro de las fechas del viaje y el costo no puede ser negativo. Responde `201` con la actividad creada.
>
> Reglas: el usuario siempre se toma de la sesión, nunca del cliente; responde `404` si el viaje no existe, `403` si el usuario no participa y `400` en validaciones, en ese orden; usa DTOs en lugar de entidades; evita el problema N+1 con `JOIN FETCH` o consultas agrupadas; los montos con `BigDecimal` a 2 decimales.
>   
> Restricciones: reutiliza lo que ya existe (`ViajeResponse`, `ErrorResponse` y las consultas de los repositorios), no modifiques archivos de otros integrantes, no agregues archivos CSS y crea archivos nuevos solo si es indispensable. Los comentarios deben ser solo documentación breve de la funcionalidad, en español y sin mencionar historias de usuario. Entrega el código completo de cada archivo y comandos `cURL` para probarlo con la cookie de sesión."

### Respuesta de IA
La herramienta completó los archivos que estaban vacíos en la estructura del proyecto: los DTOs `ActividadRequest`, `ActividadResponse`, `DiaResponse` y `ViajeDetalleResponse` (este último extiende `ViajeResponse` para reutilizar los datos generales del viaje), los servicios `ViajeDetalleService` y `ActividadService`, y los controladores `ViajeDetalleController` y `ActividadController`. Agregó consultas JPQL con `JOIN FETCH` y agrupadas en `ParticipanteViajeRepository`, `ActividadRepository`, `VotoRepository` y `GastoRepository`. La validación de participación (404 si el viaje no existe o está cancelado, 403 si no participa) quedó en un solo método de `ViajeDetalleService` que también usa `ActividadService`.

### Diferencias entre versiones
```text
51	1	backend/src/main/java/com/bitacora/viajes/controller/ActividadController.java
36	1	backend/src/main/java/com/bitacora/viajes/controller/ViajeDetalleController.java
34	1	backend/src/main/java/com/bitacora/viajes/dto/ActividadRequest.java
38	1	backend/src/main/java/com/bitacora/viajes/dto/ActividadResponse.java
8	1	backend/src/main/java/com/bitacora/viajes/dto/DiaResponse.java
38	1	backend/src/main/java/com/bitacora/viajes/dto/ViajeDetalleResponse.java
8	0	backend/src/main/java/com/bitacora/viajes/repository/ActividadRepository.java
7	0	backend/src/main/java/com/bitacora/viajes/repository/GastoRepository.java
13	0	backend/src/main/java/com/bitacora/viajes/repository/ParticipanteViajeRepository.java
18	0	backend/src/main/java/com/bitacora/viajes/repository/VotoRepository.java
52	1	backend/src/main/java/com/bitacora/viajes/service/ActividadService.java
126	1	backend/src/main/java/com/bitacora/viajes/service/ViajeDetalleService.java
```

---

### Pruebas
Con la aplicación iniciada y los datos de `DataLoader` (viaje 1 de Oaxaca del 2026-12-20 al 2026-12-27; Ana es organizadora y Luis participante):

```
# Sin sesión: 401
curl -i http://localhost:8080/api/viajes
HTTP/1.1 401
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Set-Cookie: JSESSIONID=A1E88FEA05DF0A7E9782F53B467829AA; Path=/; HttpOnly
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Length: 0
Date: Sat, 14 Nov 2026 03:25:15 GMT

# Login correcto: 200 y se guarda la cookie en /tmp/cookies.txt
curl -i -c /tmp/cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"ana@example.com","contrasena":"Password123"}'
HTTP/1.1 200
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 03:25:34 GMT

{"id":1,"nombre":"Ana López","correo":"ana@example.com"}

# Detalle del viaje: 200 con participantes, totalGastado y un elemento en "dias" por cada fecha
curl -i -b /tmp/cookies.txt http://localhost:8080/api/viajes/1
HTTP/1.1 200
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 03:25:44 GMT

{"id":1,"destino":"Oaxaca, Oaxaca","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":15000.00,"monedaBase":"MXN","rol":"ORGANIZADOR","totalGastado":5025.00,"participantes":[{"usuarioId":1,"nombre":"Ana López","rol":"ORGANIZADOR"},{"usuarioId":2,"nombre":"Luis Pérez","rol":"PARTICIPANTE"}],"dias":[{"fecha":"2026-12-20","actividades":[]},{"fecha":"2026-12-21","actividades":[{"id":1,"viajeId":1,"titulo":"Visita a Hierve el Agua","descripcion":"Cascadas petrificadas y albercas naturales.","dia":"2026-12-21","hora":"09:00","costoEstimado":350.00,"autor":{"id":1,"nombre":"Ana López"},"totalVotos":2,"votadaPorMi":true}]},{"fecha":"2026-12-22","actividades":[{"id":2,"viajeId":1,"titulo":"Cena en el Mercado 20 de Noviembre","descripcion":"Probar tlayudas y mole.","dia":"2026-12-22","hora":"20:00","costoEstimado":400.00,"autor":{"id":2,"nombre":"Luis Pérez"},"totalVotos":1,"votadaPorMi":true}]},{"fecha":"2026-12-23","actividades":[]},{"fecha":"2026-12-24","actividades":[]},{"fecha":"2026-12-25","actividades":[]},{"fecha":"2026-12-26","actividades":[]}, ... (salida recortada: el día 2026-12-27 también aparece con "actividades":[])]}

# Proponer actividad: 201 con el autor tomado de la sesión
curl -i -b /tmp/cookies.txt -X POST http://localhost:8080/api/viajes/1/actividades \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Visita a Monte Albán","descripcion":"Recorrido de 3 h","dia":"2026-12-23","hora":"09:00","costoEstimado":150.00}'
HTTP/1.1 201
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 03:26:00 GMT

{"id":3,"viajeId":1,"titulo":"Visita a Monte Albán","descripcion":"Recorrido de 3 h","dia":"2026-12-23","hora":"09:00","costoEstimado":150.00,"autor":{"id":1,"nombre":"Ana López"},"totalVotos":0,"votadaPorMi":false}
```

Casos de error:

```
# Viaje inexistente: 404
curl -i -b /tmp/cookies.txt http://localhost:8080/api/viajes/999
HTTP/1.1 404
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 04:14:34 GMT

{"campo":"viaje","mensaje":"El viaje no existe"}

# Día fuera del rango del viaje: 400 con campo "dia"
curl -i -b /tmp/cookies.txt -X POST http://localhost:8080/api/viajes/1/actividades \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Fuera de fechas","dia":"2027-01-10","hora":"09:00","costoEstimado":0}'
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 04:14:41 GMT
Connection: close

{"campo":"dia","mensaje":"El día debe estar dentro de las fechas del viaje"}

# Título vacío: 400 con campo "titulo"
curl -i -b /tmp/cookies.txt -X POST http://localhost:8080/api/viajes/1/actividades \
  -H "Content-Type: application/json" \
  -d '{"titulo":"","dia":"2026-12-23","hora":"09:00"}'
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 04:14:47 GMT
Connection: close

{"campo":"titulo","mensaje":"El título es obligatorio"}

# Costo negativo: 400 con campo "costoEstimado"
curl -i -b /tmp/cookies.txt -X POST http://localhost:8080/api/viajes/1/actividades \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Taxi","dia":"2026-12-23","hora":"09:00","costoEstimado":-5}'
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 04:14:56 GMT
Connection: close

{"campo":"costoEstimado","mensaje":"El costo estimado no puede ser negativo"}

# Registro de un usuario que no participa en el viaje: 201
curl -i -X POST http://localhost:8080/api/auth/registro -H "Content-Type: application/json" \
  -d '{"nombre":"Prueba","correo":"prueba@example.com","contrasena":"Password123"}'
HTTP/1.1 201
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 04:15:04 GMT

{"id":3,"nombre":"Prueba","correo":"prueba@example.com"}

# Login del usuario sin participación: 200 y se guarda la cookie en /tmp/cookies2.txt
curl -s -c /tmp/cookies2.txt -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"correo":"prueba@example.com","contrasena":"Password123"}'
{"id":3,"nombre":"Prueba","correo":"prueba@example.com"}

# Detalle de un viaje en el que no participa: 403
curl -i -b /tmp/cookies2.txt http://localhost:8080/api/viajes/1
HTTP/1.1 403
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sat, 14 Nov 2026 04:15:17 GMT

{"campo":"viaje","mensaje":"No participas en este viaje"}
```

---

## Observaciones adicionales

La actividad más votada de cada día (`actividadMasVotada`) no se incluyó en `DiaResponse` porque su cálculo corresponde a la historia de Alfonso (`ActividadGanadoraService`); el detalle ya devuelve `totalVotos` y `votadaPorMi` por actividad. Al arrancar la aplicación con una base de datos nueva, Hibernate no pudo crear las tablas `actividad` y `gasto` (`Invalid default value for 'fecha_creacion'`) porque `@ColumnDefault("CURRENT_TIMESTAMP")` genera un `datetime(6)` que MySQL rechaza. Se resolvió creando las tablas con `database/esquema.sql` y se avisó a la responsable de los modelos. Las excepciones del detalle y de las actividades quedaron como una clase interna de `ViajeDetalleService` (igual que `ValidacionViajeException` en `ViajeService`) para no modificar `GlobalExceptionHandler`, que está asignado a Dana.
