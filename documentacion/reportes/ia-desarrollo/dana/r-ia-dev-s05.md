# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 11-11-2026
**Número de reporte:** 05 (Dana Carmona Santamaria - Login, logout y sesión)

---

# 1. Breve descripción de uso

Configuración de Spring Security con sesión por cookie `JSESSIONID`, y creación de los endpoints de login, logout y consulta de sesión. La API sin sesión debe responder 401 y las páginas deben redirigir a `/login`.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `b42ac860033358b2de02e7d50948fb949a4cf835` (Validación de usuarios y hash) |
| **Código posterior** | Commit `4f66eedba6dfae1cb07068f3d4d3dd5b6066a212` (Login, logout y corrección de version) |
| **Diferencias entre versiones** | [ Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | En esta sesión se revisan las credenciales para crear una sesión que se guarda en la cookie, para que se pueda acceder a esta sesión en diferentes maneras y se mantenga activa, solo desaparece la sesión de las cookies si se cierra la sesión. |

---

### Prompt original
> "Configura Spring Security con sesión (JSESSIONID). POST /api/auth/login con correo y contraseña, y POST /api/auth/logout. Las rutas /api/** sin sesión responden 401 y las páginas redirigen a /login. Login incorrecto responde 401 con mensaje genérico. CSRF desactivado solo en /api."

### Respuesta de IA
Se devolvieron cuatro archivos: `LoginRequest`, `UsuarioDetailsService`, `SecurityConfig` y `AuthController`. El login valida la contraseña con `AuthenticationManager` y guarda la sesión. El logout invalida la sesión.

### Diferencias entre versiones
```text
66	92	backend/pom.xml
28	4	backend/src/main/java/com/bitacora/viajes/config/SecurityConfig.java
74	2	backend/src/main/java/com/bitacora/viajes/controller/AuthController.java
19	1	backend/src/main/java/com/bitacora/viajes/dto/LoginRequest.java
35	1	backend/src/main/java/com/bitacora/viajes/service/UsuarioDetailsService.java
1	0	cookies.txt
```

---

### Pruebas
```
# Login correcto: 200 y se guarda la cookie en /tmp/cookies.txt
curl -i -c /tmp/cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"ana@example.com","contrasena":"Password123"}'
HTTP/1.1 200
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Set-Cookie: JSESSIONID=17B1DE6186773E05BEFBBFD73AA5B36C; Path=/; HttpOnly
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 13 Nov 2026 05:36:44 GMT

{"id":1,"nombre":"Ana López","correo":"ana@example.com"}

# Login incorrecto: 401 con mensaje genérico
curl -i -X POST http://localhost:8080/api/auth/login \$ curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"ana@example.com","contrasena":"claveerronea"}'
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
Date: Fri, 13 Nov 2026 05:30:51 GMT

{"campo":"credenciales","mensaje":"Correo o contraseña incorrectos"}

# Sesión activa: 200 con los datos del usuario
curl -i -b /tmp/cookies.txt http://localhost:8080/api/auth/sesion
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
Date: Fri, 13 Nov 2026 05:31:02 GMT

{"id":1,"nombre":"Ana López","correo":"ana@example.com"}

# Logout: 204
curl -i -b /tmp/cookies.txt -c /tmp/cookies.txt -X POST http://localhost:8080/api/auth/logout
HTTP/1.1 204
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Date: Fri, 13 Nov 2026 05:31:09 GMT

# Después del logout: 401 sin acceso
curl -i -b /tmp/cookies.txt http://localhost:8080/api/auth/sesion
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
Date: Fri, 13 Nov 2026 05:31:18 GMT
```

## Observaciones adicionales

En este commit también se cambió `pom.xml` a Spring Boot 3.2.5, porque la versión que generó Spring Initializr no coincidía con las dependencias. Al probar `/api/viajes` sin sesión se obtuvo 302 en lugar de 401, porque `SecurityConfig` usa un punto de entrada explícito. Esa corrección todavía no tiene commit. El archivo `cookies.txt` volvió a aparecer en este commit.
