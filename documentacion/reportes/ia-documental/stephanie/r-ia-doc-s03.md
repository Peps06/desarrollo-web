# Reporte de Uso Documental en el Proyecto

**Fecha:** 14-11-2026   
**Número de reporte:** 03 (Stephanie Elizdeth Hernández Prieto)  

---

# 1. Breve descripción de uso

Consultas de análisis de requerimientos y diagnóstico de pruebas HTTP sobre validación de rangos de fechas en viajes con actividades ya programadas e invalidación de sesiones `JSESSIONID` en memoria al reiniciar el servidor Spring Boot.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 14-11-2026 | Gemini | [Consulta 1: Revisión de archivos necesarios para HU-23](#consulta-1-revisión-de-archivos-necesarios-para-hu-23) | [Respuesta 1](#respuesta-1) |
| 14-11-2026 | Gemini | [Consulta 2: Respuestas HTTP 401 y conflicto de fechas en pruebas PUT](#consulta-2-respuestas-http-401-y-conflicto-de-fechas-en-pruebas-put) | [Respuesta 2](#respuesta-2) |
| 14-11-2026 | Gemini | [Consulta 3: Error 401 al comprobar persistencia tras reiniciar Spring Boot](#consulta-3-error-401-al-comprobar-persistencia-tras-reiniciar-spring-boot) | [Respuesta 3](#respuesta-3) |

---

### Consulta 1: Revisión de archivos necesarios para HU-23
> "te paso lo de mi commit para que lo tengas guardado y ahora me falta esto: Editar y cancelar viaje (PUT/DELETE). me dices que necesitas del arbol que te pasé para hacer esto"

#### Respuesta 1
Se revisaron `ViajeController.java`, `ViajeService.java`, `Viaje.java`, `Actividad.java` y `ActividadRepository.java`[cite: 11, 12], confirmando que `Actividad` almacena la fecha en `dia` (`LocalDate`)[cite: 11] para validar mediante JPQL si existen actividades fuera del nuevo rango al acortar fechas[cite: 10], y que `Viaje` permite cambiar `estado` a `EstadoViaje.CANCELADO` sin borrado físico[cite: 10, 11].

---

### Consulta 2: Respuestas HTTP 401 y conflicto de fechas en pruebas PUT
> "haciendo las prubas que pides no me salen los errores que deberian salir, me salen otros (`HTTP/1.1 401` y `HTTP/1.1 400` con `2026-12-01` a `2026-12-06`)"

#### Respuesta 2
Se explicó que el `401 Unauthorized` inicial ocurrió porque no se había ejecutado `POST /api/auth/login` para generar cookies de sesión válidas[cite: 13, 14, 15, 16, 17], y que el `400 Bad Request` al enviar `2026-12-01` a `2026-12-06` confirmaba el funcionamiento correcto de la regla de negocio, ya que el viaje `1` en MySQL abarca del `2026-12-20` al `2026-12-27` y ese rango dejaba fuera sus dos actividades precargadas.

---

### Consulta 3: Error 401 al comprobar persistencia tras reiniciar Spring Boot
> "lo de comprobar resistenncia me sale asi (`curl -i -b cookies_org.txt http://localhost:8080/api/viajes -> HTTP/1.1 401`)"

#### Respuesta 3
Al detener y reiniciar `./mvnw spring-boot:run`, Spring Security limpia las sesiones `JSESSIONID` almacenadas en memoria RAM, por lo que las cookies previas caducan[cite: 18]. Se indicó ejecutar primero `POST /api/auth/login` para renovar `cookies_org.txt` y posteriormente consultar `GET /api/viajes` para verificar que los cambios editados persistían en MySQL.

---

## Observaciones adicionales

Estas consultas permitieron validar tanto la integridad temporal entre viajes y actividades como el ciclo de vida de las sesiones HTTP con cookies al reiniciar el servidor durante las pruebas de persistencia[cite: 10, 18].