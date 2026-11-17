# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16-11-2026  
**Número de reporte:** 02 (Stephanie Elizdeth Hernández Prieto - Gestionar participantes)  

---

# 1. Breve descripción de uso

Apoyo en el diseño, codificación, depuración y pruebas de integración del backend para la historia de usuario **Gestionar participantes** (`GET /api/viajes/{id}/participantes`, `POST /api/viajes/{id}/participantes` y `DELETE /api/viajes/{id}/participantes/{usuarioId}`) siguiendo la arquitectura de 3 capas del sistema (`Controller`, `Service`, `Repository` y `DTO`), validando permisos exclusivos del organizador, evitando la auto-eliminación y comprobando el bloqueo de acciones posteriores en módulos dependientes (`VotoService` y `GastoService`).

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Gemini |
| **Modelo/temperatura** | No aplica |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `d7d0b08` |
| **Código posterior** | Commit `f5fe7b9` |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat d7d0b08 f5fe7b9` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | Pruebas ejecutadas con `cURL` en WSL (`201 Created`, `200 OK`, `204 No Content`, `400 Bad Request`, `403 Forbidden`, `404 Not Found`) y prueba cruzada de votación |
| **Código de uso** | Generación / Depuración / Pruebas |
| **Reflexión del estudiante** | Comprendí cómo navegar relaciones `@ManyToOne` en JPA para mapear DTOs (`participacion.getUsuario().getId()`), la importancia de los espacios en consultas JPQL concatenadas (`JOIN FETCH`) y cómo eliminar una fila en `participante_viaje` revoca automáticamente permisos de votación y gastos en otros servicios sin borrar el historial financiero registrado en MySQL. |

---

### Prompt original
> "Te pasaré los archivos que tengo (`ParticipanteViajeRepository.java`, `GlobalExceptionHandler.java`, `api.md`, `ParticipanteViaje.java`, `UsuarioRepository.java`, `VotoService.java` y `GastoService.java`), lo demás está vacío. Necesito implementar HU-18 (Gestionar participantes: listar, agregar por correo y quitar participantes) siguiendo el contrato de la API, respetando el orden de validación 404 -> 403 -> 400 y verificando que al quitar a alguien ya no pueda votar ni registrar gastos."

### Respuesta de IA
La herramienta propuso completar los 5 componentes de la funcionalidad respetando las convenciones del equipo:
1. **`ParticipanteViajeRepository.java`:** Se conservaron las 3 consultas existentes y se agregó `buscarPorViajeYUsuarioId(Integer viajeId, Integer usuarioId)` con `JOIN FETCH p.usuario`.
2. **`ParticipanteRequest.java` y `ParticipanteResponse.java`:** DTOs para recibir `{"correo": "..."}` y responder `{usuarioId, nombre, correo, rol}` con el rol en minúsculas (`"organizador"` | `"participante"`).
3. **`ParticipanteService.java`:** Implementación transaccional de `listarParticipantes`, `agregarParticipante` y `quitarParticipante` lanzando `RecursoNoEncontradoException` (`404`), `AccesoDenegadoException` (`403`) y `DatosInvalidosException` (`400`) cuando el correo está vacío, el usuario ya participa o el organizador intenta quitarse a sí mismo.
4. **`ParticipanteController.java`:** Exposición de `GET`, `POST` (`201`) y `DELETE /{usuarioId}` (`204`) bajo `/api/viajes/{id}/participantes`, obteniendo el usuario en sesión mediante `Authentication`.

### Diferencias entre versiones
```text
52	1	backend/src/main/java/com/bitacora/viajes/controller/ParticipanteController.java
22	1	backend/src/main/java/com/bitacora/viajes/dto/ParticipanteRequest.java
57	1	backend/src/main/java/com/bitacora/viajes/dto/ParticipanteResponse.java
12	5	backend/src/main/java/com/bitacora/viajes/repository/ParticipanteViajeRepository.java
104	1	backend/src/main/java/com/bitacora/viajes/service/ParticipanteService.java

# 1. Login de usuario nuevo (Carlos) para obtener cookie de sesión
POST /api/auth/login -> HTTP/1.1 200
{"id":3,"nombre":"Carlos Prueba","correo":"carlos@example.com"}

# 2. Organizador agrega participante por correo
POST /api/viajes/1/participantes -> HTTP/1.1 201 Created

# 3. Validaciones de negocio (correo inexistente / duplicado / auto-eliminación)
POST /api/viajes/1/participantes (correo inexistente) -> HTTP/1.1 404 Not Found
POST /api/viajes/1/participantes (usuario ya participa) -> HTTP/1.1 400 Bad Request
DELETE /api/viajes/1/participantes/1 (organizador intenta quitarse) -> HTTP/1.1 400 Bad Request

# 4. Organizador quita al participante con ID 3
DELETE /api/viajes/1/participantes/3 -> HTTP/1.1 204 No Content

# 5. Prueba cruzada con HU-14: Carlos intenta votar tras ser removido
POST /api/actividades/2/votos -> HTTP/1.1 403 Forbidden