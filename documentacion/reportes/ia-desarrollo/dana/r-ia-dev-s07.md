# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 19-11-2026
**Número de reporte:** 07 (Dana Carmona Santamaría - Login y registro en el frontend)

---

# 1. Breve descripción de uso

Uso de Claude para hacer las páginas de inicio de sesión y de registro con Thymeleaf y el JavaScript que revisa los datos antes de enviarlos . Los errores se muestran debajo de cada campo o como mensaje general, sin recargar la página.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `9a9bfe0c4d2d818fe03b96dc9ef564452623fb3b` |
| **Código posterior** | Commit `702fff77765d6726d7748f55771c5997bd00d4b4` |
| **Diferencias entre versiones** | [Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de prueba](#pruebas) |
| **Código de uso** | Generación y modificación |
| **Reflexión del estudiante** | Durante esta sesión realice el html correspondiente de login.html y resgistro, también ocupe la ia para que me ayudara a realizar las validaciones nesearias para completar los campos, por lo que el enfoque principal fueron los errores que se muestran. |

---

### Prompt original
> "Quiero las páginas de login y registro. Login y registro con validación al instante: correo con formato válido, contraseña de mínimo 8 caracteres y nombre no vacío. El error aparece junto al campo. Enviar con fetch(); los errores del servidor (correo ocupado, credenciales incorrectas) se muestran sin recargar y sin borrar lo escrito. Login correcto lleva a la lista de viajes.

### Respuesta de IA
Claude revisó el código que ya existía en el repo. Escribió de nuevo `login.html`, porque la versión de prueba tenía credenciales escritas y clases que no existen en el CSS. También hizo `registro.html`, `validacion.js` (las reglas de cada campo) y `auth.js` (el envío con `fetch()` y el manejo de la respuesta). Como el equipo ya usa Thymeleaf, se descartó el primer enfoque, que era usar HTML aparte con CORS.

### Diferencias entre versiones
```
git diff --numstat 9a9bfe0c4d2d818fe03b96dc9ef564452623fb3b 702fff77765d6726d7748f55771c5997bd00d4b4
0       0       backend/src/main/resources/static/js/.gitkeep
135     0       backend/src/main/resources/static/js/auth.js
84      0       backend/src/main/resources/static/js/validacion.js
2       0       backend/src/main/resources/templates/fragments/base.html
23      38      backend/src/main/resources/templates/login.html
40      1       backend/src/main/resources/templates/registro.html
135     1       frontend/js/auth.js
84      1       frontend/js/validacion.js
```
---

### Pruebas

```
# Registro nuevo: 201
dana@Dana:~/desarrollo-web$ curl -i -X POST http://localhost:8081/api/auth/r
egistro -H "Content-Type: application/json" -d '{"nombre":"Prueba","correo":
"prueba2@ejemplo.com","contrasena":"Password123"}'
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
Date: Thu, 19 Nov 2026 22:40:24 GMT

{"id":7,"nombre":"Prueba","correo":"prueba2@ejemplo.com"}

# Mismo correo: 400 con "El correo ya está registrado"
curl -i -X POST htt^C
dana@Dana:~/desarrollo-web$ curl -i -X POST http://localhost:8081/api/auth/r
egistro -H "Content-Type: application/json" -d '{"nombre":"Prueba","correo":
"prueba2@ejemplo.com","contrasena":"Password123"}'
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
Date: Thu, 19 Nov 2026 22:41:19 GMT
Connection: close

# Login incorrecto:  401
curl -i -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"correo":"prueba2@ejemplo.com"
,"contrasena":"otra"}'
curl: (7) Failed to connect to localhost port 8080 after 0 ms: Could not connect to server
dana@Dana:~/desarrollo-web$ curl -i -X POST http://localhost:8081/api/auth/l
ogin -H "Content-Type: application/json" -d '{"correo":"prueba2@ejemplo.com"
,"contrasena":"otra"}'
HTTP/1.1 401
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
Date: Thu, 19 Nov 2026 22:42:28 GMT

{"codigo":401,"mensaje":"Correo o contraseña incorrectos"}
```
---

## Observaciones adicionales
No fue algo dificil de hacer ya que utilice elementos básicos y que ya existian del proyecto. La razón principal por la que use ia fue para comprender las validaciones necesarias y la forma en que se muestran los erroes, pero también tome ayudame para la vista de los html.