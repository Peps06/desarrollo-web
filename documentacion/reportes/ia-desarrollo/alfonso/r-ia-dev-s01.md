# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 13-11-2026
**Número de reporte:** 01 (Alfonso Mendoza Vásquez - HU-14 Votar actividad y quitar voto)

---

# 1. Breve descripción de uso

Implementación del servicio y los endpoints de votos: `POST` y `DELETE /api/actividades/{id}/votos`. Ambos responden `{actividadId, totalVotos, votadaPorMi}` y validan 404 (actividad inexistente o quitar un voto que no existe), 403 (no participa en el viaje) y 400 (voto duplicado).

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic) |
| **Modelo/temperatura** | Claude Sonnet 5.5(Anthropic), chat web |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `9ae41c2dea4170a517172da76949c8db4d5caeb3` (Reporte act) |
| **Código posterior** | Commit `7c0b87df5a550fd1cfc636e950d6909c94e423fa` (HU-14: expone POST y DELETE /api/actividades/{id}/votos) |
| **Diferencias entre versiones** | [Ver diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Entendí que las reglas de negocio se validan en el servicio y no en el controlador y que la restricción única en mySQL es un respaldo por si la validación falla. 
También que el usuario se toma de la sesión para que nadie vote a nombre de otro, y que regresar el conteo actualizado evita otra consulta desde el frontend. |

---

### Prompt original
> Te pido que comencemos a revisar lo faltante. HU-14 Votar actividad y quitar voto. Como participante de un viaje, quiero votar a favor de una actividad y poder quitar mi voto, para influir en el plan del grupo.
> Alcance: Aplica cambios para que yo pueda implementar el servicio de votos, los endpoints devuelven el conteo actualizado que usa el JavaScript de HU-30.
>
> POST /api/actividades/{id}/votos registra el voto del usuario de la sesión en la tabla voto; DELETE /api/actividades/{id}/votos quita su propio voto.
> Respuesta de ambos: {actividadId, totalVotos, votadaPorMi}, para que el frontend actualice el contador sin otra consulta.
> Validaciones en el servicio: actividad inexistente → 404; usuario que no participa en el viaje de la actividad → 403; voto duplicado → 400; DELETE sin voto previo → 404.
> La restricción única (actividad, usuario) de MySQL respalda la regla aunque falle la validación.


>
Instrucción general de estilo dada antes en la misma conversación: 
>"todo el codigo que consideres añadir o modificar debe ser: Limpio, sencllo, y entendible. Para que yo y cualquiera lo entienda completamente

### Respuesta de IA
Se generaron tres cambios: consultas nuevas en `VotoRepository` (buscar el voto de un usuario y contar votos), `VotoService` con las validaciones 404/403/400 y `VotoResponse`, y `VotoController` con los endpoints POST y DELETE. El usuario se toma de la sesión, nunca del cuerpo de la petición.

### Diferencias entre versiones
```text
8	0	backend/src/main/java/com/bitacora/viajes/repository/VotoRepository.java
6	1	backend/src/main/java/com/bitacora/viajes/dto/VotoResponse.java
100	1	backend/src/main/java/com/bitacora/viajes/service/VotoService.java
53	1	backend/src/main/java/com/bitacora/viajes/controller/VotoController.java
```

---

### Pruebas
Pruebas ejecutadas en WSL contra el servidor local (`./mvnw spring-boot:run`) con los datos de prueba del `DataLoader`. Se usa la actividad 2 porque en los datos de prueba los dos participantes ya votaron por la actividad 1, y el primer caso respondería 400 en lugar de 201.

```text
$ curl -s -c /tmp/cookies.txt -H "Content-Type: application/json" -d '{"correo":"luis@example.com","contrasena":"Password123"}' http://localhost:8080/api/auth/login
{"id":2,"nombre":"Luis Pérez","correo":"luis@example.com"}

# 1. Voto válido → 201
$ curl -i -b /tmp/cookies.txt -X POST http://localhost:8080/api/actividades/2/votos
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
Date: Tue, 17 Nov 2026 04:36:35 GMT

{"actividadId":2,"totalVotos":2,"votadaPorMi":true}

# 2. Voto duplicado → 400
$ curl -i -b /tmp/cookies.txt -X POST http://localhost:8080/api/actividades/2/votos
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
Date: Tue, 17 Nov 2026 04:36:35 GMT
Connection: close

{"campo":"voto","mensaje":"Ya votaste por esta actividad"}

# 3. Quitar voto → 200
$ curl -i -b /tmp/cookies.txt -X DELETE http://localhost:8080/api/actividades/2/votos
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
Date: Tue, 17 Nov 2026 04:36:35 GMT

{"actividadId":2,"totalVotos":1,"votadaPorMi":false}

# 4. Quitarlo otra vez → 404
$ curl -i -b /tmp/cookies.txt -X DELETE http://localhost:8080/api/actividades/2/votos
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
Date: Tue, 17 Nov 2026 04:36:35 GMT

{"campo":"voto","mensaje":"No has votado por esta actividad"}

# 5. Usuario que no participa → 403 (login con un tercer usuario, registrado previamente)
$ curl -s -c /tmp/cookies-pepe.txt -H "Content-Type: application/json" -d '{"correo":"pepe@example.com","contrasena":"Password123"}' http://localhost:8080/api/auth/login
{"id":3,"nombre":"Pepe Prueba","correo":"pepe@example.com"}

$ curl -i -b /tmp/cookies-pepe.txt -X POST http://localhost:8080/api/actividades/2/votos
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
Date: Tue, 17 Nov 2026 04:36:35 GMT

{"campo":"viaje","mensaje":"No participas en este viaje"}
```