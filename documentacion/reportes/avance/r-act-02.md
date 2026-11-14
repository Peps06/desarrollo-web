# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)  
**Fecha:** 13/11/2026  
**Número de reporte:** 2  
**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

Durante esta fase avanzamos en el desarrollo de **Backend básico (Entregable 2: API funcional inicial)**

- Se creó la base del proyecto con Spring Boot. Incluye la configuración, las 7 tablas como entidades, sus repositorios, la conexión a MySQL y datos de prueba para empezar a trabajar desde el inicio.
- Se hizo el registro de usuarios (`POST /api/auth/registro`). Revisa que el nombre, el correo y la contraseña sean válidos, guarda la contraseña cifrada y responde 201 o 400 según el caso.
- Se configuró el inicio y cierre de sesión (`POST /api/auth/login` y `POST /api/auth/logout`) con Spring Security. Las rutas de la API sin sesión responden 401.
- Se implementó el endpoint `POST /api/viajes`para crear nuevos viajes con validaciones de campos (destino obligatorio, fechas coherentes, presupuesto mayor a cero y moneda base de 3 letras con `MXN` por defecto), asociando automáticamente al usuario autenticado como `ORGANIZADOR` en la tabla `participante_viaje` dentro de una transacción (`@Transactional`).
- Se implementó el endpoint `GET /api/viajes` para consultar únicamente los viajes activos (`ACTIVO`, excluyendo `CANCELADO`) en los que participa el usuario en sesión, devolviendo los datos generales del viaje junto con su rol (`ORGANIZADOR` o `PARTICIPANTE`).
- Se ejecutaron pruebas funcionales mediante `cURL` con manejo de cookies de sesión (`JSESSIONID`) para verificar casos de éxito (`201 Created`, `200 OK`) y validación de errores (`400 Bad Request`), comprobando además la persistencia en MySQL.
- Se implementó el endpoint `GET /api/viajes/{id}` que devuelve el detalle del viaje: datos generales, participantes con rol, total gastado y una entrada por cada fecha entre `fechaInicio` y `fechaFin` (aunque no tenga actividades), con las actividades de cada día ordenadas por hora, su autor, total de votos y si el usuario en sesión ya votó. Se usan DTOs y consultas con `JOIN FETCH` y agrupadas para evitar el problema N+1.
- Se implementó el endpoint `POST /api/viajes/{id}/actividades` para proponer actividades, con validación de título, hora, día dentro del rango del viaje y costo estimado no negativo, tomando el autor de la sesión y respondiendo `201 Created`, `403` si el usuario no participa y `404` si el viaje no existe.

- *(Espacio para que los demás integrantes agreguen sus actividades)*

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :--- | :--- | :--- |
| Implementación de endpoint `POST /api/viajes` para crear viaje y registrar organizador | Stephanie Hernández | [https://github.com/Peps06/desarrollo-web/commit/15cc7a1](https://github.com/Peps06/desarrollo-web/commit/15cc7a1) |
| Implementación de endpoint `GET /api/viajes` para listar viajes activos del usuario autenticado | Stephanie Hernández | [https://github.com/Peps06/desarrollo-web/commit/bf861e5](https://github.com/Peps06/desarrollo-web/commit/bf861e5) |
| Implementación de endpoint `GET /api/viajes/{id}` para consultar el detalle del viaje por días | Diego Jiménez | [https://github.com/Peps06/desarrollo-web/commit/77663df](https://github.com/Peps06/desarrollo-web/commit/77663df) |
| Implementación de endpoint `POST /api/viajes/{id}/actividades` para proponer una actividad | Diego Jiménez | [https://github.com/Peps06/desarrollo-web/commit/77663df](https://github.com/Peps06/desarrollo-web/commit/77663df) |
| Base del proyecto Spring Boot con entidades, repositorios, conexión a MySQL y datos de prueba | Dana Carmona Santamaría | [https://github.com/Peps06/desarrollo-web/commit/aa9594a](https://github.com/Peps06/desarrollo-web/commit/aa9594a) |
| Registro de usuarios con validaciones y hash de contraseña | Dana Carmona Santamaría | [https://github.com/Peps06/desarrollo-web/commit/b42ac86](https://github.com/Peps06/desarrollo-web/commit/b42ac86) |
| Inicio y cierre de sesión con Spring Security | Dana Carmona Santamaría | [https://github.com/Peps06/desarrollo-web/commit/4f66eed](https://github.com/Peps06/desarrollo-web/commit/4f66eed) |
| *(Espacio para actividades de Alfonso y Citlaly)* | | |

---

# 3. Problemas encontrados

- **Error de versiones Spring Boot:** Se presentó un error entre vesiones que trajo el Spring Initializr que era 4.x y del proyecto general que es 3.2.5. La solución fue reemplazar el pom.xml completo con las dependencias de Spring Boot 3.2.5
- **Error de sintaxis JPQL al iniciar Spring Boot:** Se presentó un `QueryCreationException` en `ParticipanteViajeRepository` por falta de un espacio entre el alias de la entidad y la cláusula `WHERE` en la consulta concatenada (`vWHERE`). Se corrigió agregando el espaciado adecuado en la cadena `@Query`.
- **Redirección `302` en peticiones sin sesión a `/api/viajes`:** Al probar el acceso no autenticado con `cURL`, Spring Security devolvió `302` hacia `/login` en lugar de `401 Unauthorized` debido al orden de los `AuthenticationEntryPoint` en `SecurityConfig.java`. Se documentó el hallazgo para su ajuste en la configuración de seguridad.

---

# 4. Avance funcional

**Operativas / completadas (módulos de Stephanie):**

- Creación de viajes (`POST /api/viajes`) con validación de negocio, persistencia transaccional en `viaje` y `participante_viaje`, y respuesta `201 Created`.
- Consulta de viajes activos por usuario autenticado (`GET /api/viajes`) devolviendo listado con rol asignado o lista vacía (`200 OK`).

**Operativas / completadas (módulos de Diego):**

- Consulta del detalle del viaje (`GET /api/viajes/{id}`) con participantes, total gastado y días con sus actividades ordenadas por hora, validando que el usuario participe (`403`) y que el viaje exista (`404`).
- Creación de actividades (`POST /api/viajes/{id}/actividades`) con validaciones de negocio y respuesta `201 Created` con el autor tomado de la sesión.

**Operativas / completadas (módulos de Dana):**

- Base del proyecto Spring Boot con entidades, repositorios, conexión a MySQL y datos de prueba.
- Registro de usuarios (`POST /api/auth/registro`) con validaciones y hash de contraseña.
- Inicio y cierre de sesión (`POST /api/auth/login`, `POST /api/auth/logout`) y consulta de sesión (`GET /api/auth/sesion`) con Spring Security.

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

-**Diego**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Apoyo en la codificación de `ViajeDetalleController`, `ViajeDetalleService`, `ActividadController` y `ActividadService`, los DTOs `ViajeDetalleResponse`, `DiaResponse`, `ActividadRequest` y `ActividadResponse`, y las consultas JPQL de `ParticipanteViajeRepository`, `ActividadRepository`, `VotoRepository` y `GastoRepository`.
- **Código o contenido generado:** Borradores de los DTOs, de la lógica que agrupa las actividades por día y de las consultas con `JOIN FETCH` y agrupadas.
- **Modificaciones realizadas por el equipo:** Se reutilizó `ViajeResponse` (el detalle lo extiende) y el formato `ErrorResponse(campo, mensaje)` del equipo, y se dejó la excepción del servicio como clase interna para no modificar `GlobalExceptionHandler`.
- **Forma en que se verificó el resultado:** Pruebas manuales con `cURL` y cookie de sesión (`200`, `201`, `400`, `401`, `403` y `404`) y revisión de los datos en MySQL. El detalle completo se encuentra en `documentacion/reportes/ia-desarrollo/diego/`.

---

-**Dana**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Apoyo en la creación de la base del proyecto (entidades, repositorios, configuración de seguridad y datos de prueba), el registro de usuarios y el inicio y cierre de sesión.
- **Código o contenido generado:** Entidades JPA y enums, repositorios, `SecurityConfig`, `DataLoader`, DTOs de registro y login, `UsuarioService`, `UsuarioDetailsService` y `AuthController`, además de los comandos de prueba con `cURL`.
- **Modificaciones realizadas por el equipo:** Se corrigieron errores de compilación y de rutas de carpetas, se cambió Spring Boot a la versión 3.2.5 para coincidir con lo visto en clase y se adaptaron las respuestas de error al formato `ErrorResponse(campo, mensaje)` del equipo.
- **Forma en que se verificó el resultado:** Compilación con `./mvnw compile`, pruebas con `cURL` (códigos `201`, `400`, `200` y `401`) y revisión de las tablas, el conteo de usuarios y el hash de las contraseñas en MySQL.

---

# 6. Participación individual

- **Stephanie Elizdeth Hernández Prieto:** Implementación de **Crear viaje** (endpoint `POST /api/viajes`, DTOs `ViajeRequest`/`ViajeResponse`, validaciones y servicio transaccional para registrar al creador como `ORGANIZADOR`), implementación de **Ver mis viajes** (endpoint `GET /api/viajes` y consulta JPQL filtrando viajes activos del usuario en sesión), pruebas con `cURL` y verificación en MySQL.
- **Dana Carmona Santamaría:** Creó la base del proyecto con sus entidades, repositorios, configuración de seguridad y datos de prueba. Implementó el registro de usuarios con validaciones y hash de contraseña, y configuró el inicio y cierre de sesión con Spring Security. Hizo pruebas con `cURL` y revisó los datos en MySQL.
- **Diego Rafael Jiménez Trujano:** Implementación de **Detalle del viaje por días** (endpoint `GET /api/viajes/{id}`, DTOs `ViajeDetalleResponse`/`DiaResponse`/`ActividadResponse`, servicio con consultas sin N+1 y validación de participación) e implementación de **Proponer actividad** (endpoint `POST /api/viajes/{id}/actividades`, DTO `ActividadRequest` con validaciones y servicio que registra al autor desde la sesión), pruebas con `cURL` y verificación en MySQL.
- **Alfonso Mendoza Vásquez:** *(Completar)*
- **Citlaly Morales Viveros:** *(Completar)*

---

# 7. Próximas actividades

- Actividad 1: Implementar gestión de participantes por viaje (`POST` y `DELETE /api/viajes/{id}/participantes`) — **Gestionar participantes**.
- Actividad 2: Implementar edición y cancelación lógica de viajes (`PUT` y `DELETE /api/viajes/{id}`) — **Editar y eliminar actividades**.
- Actividad 3: Implementar edición y eliminación de actividades (`PUT /api/actividades/{id}` y `DELETE /api/actividades/{id}`) y filtros de actividades por día y costo máximo.
- Implementar el manejo uniforme de errores HTTP y revisar que los endpoints de la fase 2 cumplan el formato {codigo, mensaje} **Manejo uniforme de errores HTTP**.
- *(Espacio para próximas actividades del resto del equipo)*

---

## Observaciones adicionales
