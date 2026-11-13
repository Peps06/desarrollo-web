# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)  
**Fecha:** 13/11/2026  
**Número de reporte:** 2  
**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

Durante esta fase avanzamos en el desarrollo de **Backend básico (Entregable 2: API funcional inicial)**

- Se implementó el endpoint `POST /api/viajes`para crear nuevos viajes con validaciones de campos (destino obligatorio, fechas coherentes, presupuesto mayor a cero y moneda base de 3 letras con `MXN` por defecto), asociando automáticamente al usuario autenticado como `ORGANIZADOR` en la tabla `participante_viaje` dentro de una transacción (`@Transactional`).
- Se implementó el endpoint `GET /api/viajes` para consultar únicamente los viajes activos (`ACTIVO`, excluyendo `CANCELADO`) en los que participa el usuario en sesión, devolviendo los datos generales del viaje junto con su rol (`ORGANIZADOR` o `PARTICIPANTE`).
- Se ejecutaron pruebas funcionales mediante `cURL` con manejo de cookies de sesión (`JSESSIONID`) para verificar casos de éxito (`201 Created`, `200 OK`) y validación de errores (`400 Bad Request`), comprobando además la persistencia en MySQL.
- *(Espacio para que los demás integrantes agreguen sus actividades)*

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :--- | :--- | :--- |
| Implementación de endpoint `POST /api/viajes` para crear viaje y registrar organizador | Stephanie Hernández | [https://github.com/Peps06/desarrollo-web/commit/15cc7a1](https://github.com/Peps06/desarrollo-web/commit/15cc7a1) |
| Implementación de endpoint `GET /api/viajes` para listar viajes activos del usuario autenticado | Stephanie Hernández | [https://github.com/Peps06/desarrollo-web/commit/bf861e5](https://github.com/Peps06/desarrollo-web/commit/bf861e5) |
| *(Espacio para actividades de Dana, Diego, Alfonso y Citlaly)* | | |

---

# 3. Problemas encontrados


- **Error de sintaxis JPQL al iniciar Spring Boot:** Se presentó un `QueryCreationException` en `ParticipanteViajeRepository` por falta de un espacio entre el alias de la entidad y la cláusula `WHERE` en la consulta concatenada (`vWHERE`). Se corrigió agregando el espaciado adecuado en la cadena `@Query`.
- **Redirección `302` en peticiones sin sesión a `/api/viajes`:** Al probar el acceso no autenticado con `cURL`, Spring Security devolvió `302` hacia `/login` en lugar de `401 Unauthorized` debido al orden de los `AuthenticationEntryPoint` en `SecurityConfig.java`. Se documentó el hallazgo para su ajuste en la configuración de seguridad.

---

# 4. Avance funcional

**Operativas / completadas (módulos de Stephanie):**

- Creación de viajes (`POST /api/viajes`) con validación de negocio, persistencia transaccional en `viaje` y `participante_viaje`, y respuesta `201 Created`.
- Consulta de viajes activos por usuario autenticado (`GET /api/viajes`) devolviendo listado con rol asignado o lista vacía (`200 OK`).

**Pendientes (próxima fase - Backend completo):**

- Gestión de participantes del viaje por correo (`POST` y `DELETE /api/viajes/{id}/participantes`) correspondiente a **Gestionar participantes**.
- Edición y cancelación lógica de viajes (`PUT` y `DELETE /api/viajes/{id}`) correspondiente a **Editar y eliminar actividades**.

---

# 5. Uso de IA generativa

-**Stephanie**

- **Herramienta utilizada:** Gemini
- **Actividad para la que se utilizó:** Apoyo en la estructuración de las clases de la arquitectura de 3 capas (`ViajeRequest`, `ViajeResponse`, `ViajeService`, `ViajeController`), consulta JPQL con `JOIN FETCH` en `ParticipanteViajeRepository` y depuración de errores de arranque en WSL/Spring Boot.
- **Código o contenido generado:** Borradores iniciales de los DTOs de viaje, lógica transaccional de creación y consulta de viajes activos, y comandos `cURL` de prueba.
- **Modificaciones realizadas por el equipo:** Se adaptó el manejo de errores al formato `ErrorResponse(campo, mensaje)` del equipo, se omitió `@CrossOrigin` para mantener coherencia con `SecurityConfig.java` (mismo origen con sesión por cookies) y se corrigió la sintaxis de la consulta JPQL.
- **Forma en que se verificó el resultado:** Pruebas de integración manuales con `cURL` (casos de validación `400`, creación `201` y consulta `200`) y verificación directa de registros en las tablas `viaje` y `participante_viaje` de MySQL. El detalle completo se encuentra en `documentacion/reportes/ia-desarrollo/`.

---

# 6. Participación individual

- **Stephanie Elizdeth Hernández Prieto:** Implementación de **Crear viaje** (endpoint `POST /api/viajes`, DTOs `ViajeRequest`/`ViajeResponse`, validaciones y servicio transaccional para registrar al creador como `ORGANIZADOR`), implementación de **Ver mis viajes** (endpoint `GET /api/viajes` y consulta JPQL filtrando viajes activos del usuario en sesión), pruebas con `cURL` y verificación en MySQL.
- **Dana Carmona Santamaría:** *(Completar)*
- **Diego Rafael Jiménez Trujano:** *(Completar)*
- **Alfonso Mendoza Vásquez:** *(Completar)*
- **Citlaly Morales Viveros:** *(Completar)*

---

# 7. Próximas actividades

- Actividad 1: Implementar gestión de participantes por viaje (`POST` y `DELETE /api/viajes/{id}/participantes`) — **Gestionar participantes**.
- Actividad 2: Implementar edición y cancelación lógica de viajes (`PUT` y `DELETE /api/viajes/{id}`) — **Editar y eliminar actividades**.
- *(Espacio para próximas actividades del resto del equipo)*

---

## Observaciones adicionales
