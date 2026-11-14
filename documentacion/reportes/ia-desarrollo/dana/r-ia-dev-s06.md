# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 14-11-2026
**Número de reporte:** 06 (Dana Carmona Santamaria - Manejo uniforme de errores HTTP)

---

# 1. Breve descripción de uso

Creación de un manejo de errores común para toda la API. Todas las respuestas de error tienen el mismo formato: un código HTTP y un mensaje. En las validaciones también se incluye la lista de campos con error. Los errores inesperados responden 500 con un mensaje genérico.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `e5ecb26636c0dd07576ace2c94545f598b16704e` |
| **Código posterior** | Commit `e5f4e8e965130cc7c9cd91d068bfefdc7105c63e` |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#prueba) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Gracias a los cambios realizado, logré hacer que nos mensajes de error estuvieran unificados y siguiendo a un solo formato, por lo que modifique el archvivo principal de GlobalExceptionHandler.java y los controladores que ocupadan manejos de erroes locales|

---

### Prompt original
> "Quiero manejo uniforme de errores HTTP con @RestControllerAdvice y excepciones propias. El formato debe ser {codigo, mensaje} y, en validaciones, una lista de errores con {campo, mensaje}. Los códigos son 400, 401, 403, 404 y 500. Un error inesperado debe responder 500 con mensaje genérico y sin stack trace. Ningún endpoint debe responder 500 por datos inválidos del usuario."

### Respuesta de IA
Se devolvió un formato de error con `codigo`, `mensaje` y `errores`, y un manejador global con un método por tipo de error. Los errores de seguridad (401 y 403) se configuraron en `SecurityConfig`, porque los filtros de seguridad se ejecutan antes de los controladores. Se quitaron los manejadores locales de los controladores, para que todos usen el manejador global.

### Diferencias entre versiones
```
git diff --numstat e5ecb26636c0dd07576ace2c94545f598b16704e e5f4e8e965130cc7c9cd91d068bfefdc7105c63e

29      4       backend/src/main/java/com/bitacora/viajes/config/SecurityConfig.java
0       17      backend/src/main/java/com/bitacora/viajes/controller/ActividadController.java
6       6       backend/src/main/java/com/bitacora/viajes/controller/AuthController.java
1       10      backend/src/main/java/com/bitacora/viajes/controller/PresupuestoController.java
7       7       backend/src/main/java/com/bitacora/viajes/controller/ViajeController.java
0       9       backend/src/main/java/com/bitacora/viajes/controller/ViajeDetalleController.java
1       11      backend/src/main/java/com/bitacora/viajes/controller/VotoController.java
16      1       backend/src/main/java/com/bitacora/viajes/dto/ErrorCampo.java
24      5       backend/src/main/java/com/bitacora/viajes/dto/ErrorResponse.java
85      7       backend/src/main/java/com/bitacora/viajes/exception/GlobalExceptionHandler.java
```

---

### Pruebas

```
# Error 401: sin sesión
dana@Dana:~/desarrollo-web$ curl -i http://localhost:8081/api/viajes/1
HTTP/1.1 401
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Set-Cookie: JSESSIONID=0FB0C18B176668E2807E33D86921CA34; Path=/; HttpOnly
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json;charset=UTF-8
Content-Length: 45
Date: Sat, 14 Nov 2026 17:03:31 GMT

{"codigo":401,"mensaje":"Tu sesión expiró"}

# Error 400: id que no es número
dana@Dana:~/desarrollo-web$ curl -i -b /tmp/cookies.txt http://localhost:8081/api/viajes/abc
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
Date: Sat, 14 Nov 2026 17:04:06 GMT
Connection: close

{"codigo":400,"mensaje":"Datos inválidos","errores":[{"campo":"id","mensaje":"El valor no es válido"}]}

#Error 404: el viaje no existe
dana@Dana:~/desarrollo-web$ curl -i -b /tmp/cookies.txt http://localhost:8081/api/viajes/9999
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
Date: Sat, 14 Nov 2026 17:04:18 GMT

{"codigo":404,"mensaje":"El viaje no existe"}

#Error 400: sin destino
dana@Dana:~/desarrollo-web$ curl -i -b /tmp/cookies.txt -X POST http://localhost:8081/api/viajes -H "Content-Type: application/json" -d '{"destino":"","fechaInicio":"2026-12-20","fechaFin":"2026-12-27","presupuestoTotal":1000,"monedaBase":"MXN"}'
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
Date: Sat, 14 Nov 2026 17:04:28 GMT
Connection: close

{"codigo":400,"mensaje":"Datos inválidos","errores":[{"campo":"destino","mensaje":"El destino es obligatorio"}]}

#Error 400: mal formato
dana@Dana:~/desarrollo-web$ curl -i -b /tmp/cookies.txt -X POST http://localhost:8081/api/viajes -H "Content-Type: application/json" -d '{"destino":'
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
Date: Sat, 14 Nov 2026 17:04:37 GMT
Connection: close

{"codigo":400,"mensaje":"El cuerpo de la petición no es un JSON válido"}}
```
---

## Observaciones adicionales

Se agregaron dos códigos de error adicionales, para que respondan a otros errores especificos. 405 que responde que el método no está permitido y 415 que responde que el tipo no es compatible

