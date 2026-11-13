# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 11-11-2026
**Número de reporte:** 04 (Dana Carmona Santamaria - Registro de usuario)

---

# 1. Breve descripción de uso

Creación del endpoint `POST /api/auth/registro` con validaciones de nombre, correo y contraseña. La contraseña se guarda con hash BCrypt. Las respuestas son 201 con los datos del usuario, o 400 con el campo inválido.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Haiku 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `aa9594aaed7a1f90fc2d3f96c92d9446962f7166` (Base de datos lista) |
| **Código posterior** | Commit `b42ac860033358b2de02e7d50948fb949a4cf835` (Validación de usuarios y hash) |
| **Diferencias entre versiones** |[Ver Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Realice el endpoint POST, con el cual se escriben datos como nombre, correo y contraseña y se validan los datos para saber si puede acceder o no. Muestra diferentes mensajes de error dependiendo del falló que hubo.|

---

### Prompt original
> "Implementa POST /api/auth/registro con JSON {nombre, correo, contrasena}. Validaciones: nombre no vacío, correo con formato válido y no repetido, contraseña de mínimo 8 caracteres. Contraseña guardada con hash BCrypt. Respuestas: 201 con {id, nombre, correo}; 400 con el detalle del campo inválido o del correo repetido. Usa el paquete com.bitacora.viajes."

### Respuesta de IA
Se devolvieron los DTOs de solicitud, respuesta y error, el repositorio con `existsByCorreo` y `findByCorreo`, el servicio que normaliza el correo y codifica la contraseña, y el controlador con `@Valid` y `BindingResult`.

### Diferencias entre versiones
```text
48	1	backend/src/main/java/com/bitacora/viajes/controller/AuthController.java
16	1	backend/src/main/java/com/bitacora/viajes/dto/ErrorResponse.java
31	1	backend/src/main/java/com/bitacora/viajes/dto/RegistroRequest.java
25	1	backend/src/main/java/com/bitacora/viajes/dto/UsuarioResponse.java
7	1	backend/src/main/java/com/bitacora/viajes/repository/UsuarioRepository.java
46	1	backend/src/main/java/com/bitacora/viajes/service/UsuarioService.java
4	0	cookies.txt
```

---
### Pruebas
```
# Registro correcto
curl -i -X POST http://localhost:8080/api/auth/registro -H "Content-Type: application/json" -d '{"nombre":"Prueba","correo":"prueba1@example.com","contrasena":"Viajes2026"}'
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
Date: Fri, 13 Nov 2026 05:24:06 GMT

# Correo repetido
curl -i -X POST http://localhost:8080/api/auth/registro \curl -i -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","correo":"ana@example.com","contrasena":"Viajes2026"}'
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
Date: Fri, 13 Nov 2026 05:24:15 GMT
Connection: close

{"campo":"correo","mensaje":"El correo ya está registrado"}

# Contraseña corta
curl -i -X POST http://localhost:8080/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Pedro","correo":"pedro@example.com","contrasena":"abc123"}'
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
Date: Fri, 13 Nov 2026 05:24:25 GMT
Connection: close

{"campo":"contrasena","mensaje":"La contraseña debe tener al menos 8 caracteres"}
```

## Observaciones adicionales

El registro necesita la dependencia `spring-boot-starter-validation`. El archivo `cookies.txt` aparece en este commit, pero contiene una sesión y no debe estar en el repositorio.
