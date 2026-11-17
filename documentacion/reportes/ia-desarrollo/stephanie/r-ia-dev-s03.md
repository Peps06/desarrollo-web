# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 16-11-2026  
**Número de reporte:** 03 (Stephanie Elizdeth Hernández Prieto - Editar y cancelar viaje)  

---

# 1. Breve descripción de uso

Apoyo en la implementación, depuración y validación de persistencia del backend para la historia de usuario **HU-23: Editar y cancelar viaje** (`PUT /api/viajes/{id}` y `DELETE /api/viajes/{id}`) sobre la arquitectura de 3 capas (`Controller`, `Service` y `Repository`), incorporando validación de integridad temporal contra actividades existentes cuando se acortan las fechas del viaje y borrado lógico mediante cambio de estado a `CANCELADO`.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Gemini |
| **Modelo/temperatura** | No aplica |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `f5fe7b9` |
| **Código posterior** | Commit `e5ecb26` |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat f5fe7b9 e5ecb26` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | Pruebas ejecutadas con `cURL` en WSL (`200 OK`, `204 No Content`, `400 Bad Request` por conflicto de fechas, `403 Forbidden` a participante y verificación post-reinicio de Spring Boot) |
| **Código de uso** | Generación / Modificación / Pruebas |
| **Reflexión del estudiante** | Aprendí a implementar borrado lógico mediante estados (`EstadoViaje.CANCELADO`) para no romper llaves foráneas de actividades, votos y gastos en MySQL, y a proteger la consistencia del itinerario consultando actividades fuera del nuevo rango de fechas antes de permitir acortar la duración de un viaje. |

---

### Prompt original
> "Ahora me falta implementar HU-23: Editar y cancelar viaje (`PUT /api/viajes/{id}` y `DELETE /api/viajes/{id}`). Te paso `ViajeController.java`, `ViajeService.java`, `Viaje.java`, `Actividad.java` y `ActividadRepository.java`. Necesito validar que solo el organizador pueda editar o cancelar (`403`), que si acorta las fechas y quedan actividades fuera del nuevo rango responda `400` explicando el conflicto, y que al cancelar pase a estado `CANCELADO` sin borrado físico dejando de aparecer en la lista de todos los participantes."

### Respuesta de IA
La herramienta propuso extender los componentes existentes de viajes y actividades sin alterar el comportamiento previo de HU-10 y HU-11:
1. **`ActividadRepository.java`:** Se agregó la consulta JPQL `contarFueraDeRango(Integer viajeId, LocalDate fechaInicio, LocalDate fechaFin)` para contar actividades cuya fecha (`a.dia`) quede antes de `fechaInicio` o después de `fechaFin`.
2. **`ViajeService.java`:** Se agregaron los métodos transaccionales `editarViaje` y `cancelarViaje`, validando existencia del viaje (`404`), permiso exclusivo de `ORGANIZADOR` (`403`), coherencia de destino/fechas/presupuesto/moneda (`400`), ausencia de actividades fuera del nuevo rango (`400`) y actualización de estado a `EstadoViaje.CANCELADO` sin `delete` físico.
3. **`ViajeController.java`:** Se agregaron los endpoints `@PutMapping("/{id}")` (`200 OK` con `ViajeResponse`) y `@DeleteMapping("/{id}")` (`204 No Content`).

### Diferencias entre versiones
```text
44	0	backend/src/main/java/com/bitacora/viajes/controller/ViajeController.java
10	0	backend/src/main/java/com/bitacora/viajes/repository/ActividadRepository.java
95	2	backend/src/main/java/com/bitacora/viajes/service/ViajeService.java

# 1. Participante (no organizador) intenta editar viaje -> 403 Forbidden
PUT /api/viajes/1 (luis@example.com) -> HTTP/1.1 403
{"campo":"acceso","mensaje":"Solo el organizador puede editar o cancelar el viaje"}

# 2. Organizador intenta acortar fechas dejando actividades fuera -> 400 Bad Request
PUT /api/viajes/1 (2026-12-10 a 2026-12-11) -> HTTP/1.1 400
{"campo":"datos","mensaje":"No se pueden acortar las fechas porque hay 2 actividad(es) programada(s) fuera del nuevo rango (2026-12-10 a 2026-12-11)"}

# 3. Organizador edita viaje con fechas válidas (2026-12-20 a 2026-12-28) -> 200 OK
PUT /api/viajes/1 -> HTTP/1.1 200

# 4. Organizador cancela viaje de prueba -> 204 No Content y desaparece del listado
DELETE /api/viajes/2 -> HTTP/1.1 204
GET /api/viajes -> HTTP/1.1 200 (viaje 2 excluido de la lista activa)

# 5. Comprobación de persistencia tras reiniciar Spring Boot y renovar sesión
POST /api/auth/login -> HTTP/1.1 200
GET /api/viajes -> HTTP/1.1 200 (cambios editados persisten en MySQL y viajes cancelados no aparecen)