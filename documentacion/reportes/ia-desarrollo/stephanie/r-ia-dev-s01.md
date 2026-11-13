# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 12-11-2026
**Número de reporte:** 01 (Stephanie Elizdeth Hernández Prieto - Crear viaje y Ver mis viajes)  

---

# 1. Breve descripción de uso

Apoyo en la estructuración, codificación y depuración del backend para las historias de usuario **(Crear viaje: `POST /api/viajes`)** y **(Ver mis viajes: `GET /api/viajes`)** siguiendo la arquitectura de 3 capas del sistema (`Controller`, `Service`, `Repository` y `DTO`), respetando la entidad `ParticipanteViaje` y el manejo de sesiones con Spring Security.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Gemini |
| **Modelo/temperatura** | No aplica |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `51e1a05` |
| **Código posterior** | Commit `bf861e5`|
| **Diferencias entre versiones** | Ver salida de `git diff --numstat 51e1a05 bf861e5` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | Pruebas ejecutadas con `cURL` (códigos `400`, `201` y `200` con cookie `JSESSIONID`) y verificación `SELECT` en MySQL |
| **Código de uso** | Generación / Depuración / Explicación |
| **Reflexión del estudiante** | Comprendí cómo garantizar atomicidad con `@Transactional` al guardar simultáneamente en `viaje` y `participante_viaje`, cómo extraer el usuario autenticado de `Authentication` sin confiar en parámetros del cliente y cómo evitar consultas N+1 usando `JOIN FETCH` en JPQL. |

---

### Prompt original
> "Ayúdame paso a paso a implementar las historias HU-10 (POST /api/viajes) y HU-11 (GET /api/viajes) en Spring Boot siguiendo la arquitectura de 3 capas de nuestro proyecto (DTO, Service con @Transactional, Repository y RestController), respetando las entidades Viaje y ParticipanteViaje y el formato de ErrorResponse que ya usa nuestro equipo."

### Respuesta de IA
La herramienta propuso la creación de los DTOs `ViajeRequest` y `ViajeResponse`, el servicio transaccional `ViajeService`, el controlador REST `ViajeController` y una consulta JPQL con `JOIN FETCH` en `ParticipanteViajeRepository` para filtrar los viajes con estado `ACTIVO` asociados al usuario en sesión.

### Diferencias entre versiones
```text
74	1	backend/src/main/java/com/bitacora/viajes/controller/ViajeController.java
72	1	backend/src/main/java/com/bitacora/viajes/dto/ViajeRequest.java
78	1	backend/src/main/java/com/bitacora/viajes/dto/ViajeResponse.java
27	7	backend/src/main/java/com/bitacora/viajes/repository/ParticipanteViajeRepository.java
107	1	backend/src/main/java/com/bitacora/viajes/service/ViajeService.java