# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)  
**Fecha:** 16/11/2026  
**Número de reporte:** 3  
**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

Durante esta fase avanzamos en el desarrollo de **Backend completo (Entregable 3: API REST completa)**.

- Se creó un manejo de errores común para toda la API. Todas las respuestas de error tienen el mismo formato, con un código y un mensaje. Cuando hay errores en los datos, también se muestra la lista de campos con problemas.
- Los errores de inicio de sesión y de permisos (401 y 403) también responden en el mismo formato, en lugar de responder sin contenido.
- Los errores inesperados responden con un mensaje general, sin mostrar detalles internos.
- Se implementaron los endpoints para gestionar los participantes de un viaje (`GET`, `POST` y `DELETE /api/viajes/{id}/participantes`), permitiendo listar integrantes, agregar nuevos participantes registrados por correo electrónico y remover participantes con control de permisos exclusivo para el organizador.
- Se implementaron los endpoints para editar y cancelar viajes (`PUT` y `DELETE /api/viajes/{id}`), validando la integridad de las fechas frente a actividades ya programadas en el itinerario y aplicando borrado lógico mediante cambio de estado a `CANCELADO`.
<<<<<<< HEAD
- Se agregaron los endpoints para editar y eliminar actividades (`PUT` y `DELETE /api/actividades/{id}`) y para listar las actividades de un viaje con filtros opcionales por día y por costo máximo (`GET /api/viajes/{id}/actividades?dia=&costoMax=`).
- Se agregó el listado de los viajes activos del usuario en sesión (`GET /api/viajes`) con filtros opcionales y combinables por destino y por rol (organizador o participante).
- Se agregó el comprobante opcional al registrar un gasto (`POST /api/gastos` con `multipart/form-data`) y su consulta (`GET /api/gastos/{id}/comprobante`), visible solo para los participantes del viaje.
=======
- Se implementó la edición de actividades (`PUT /api/actividades/{id}`) con las mismas validaciones que al proponer una actividad. Solo el autor de la actividad o el organizador del viaje pueden editarla; los demás reciben 403. Responde 200 con la actividad actualizada, su total de votos y si el usuario en sesión ya votó.
- Se implementó la eliminación de actividades (`DELETE /api/actividades/{id}`), que responde 204 y elimina sus votos en la misma transacción. La actividad deja de aparecer en el detalle del viaje.
- Se agregó el filtro de actividades `GET /api/viajes/{id}/actividades?dia=yyyy-MM-dd&costoMax=500` y los filtros `destino` y `rol` en `GET /api/viajes`. Son opcionales y combinables, y se aplican en la consulta a la base de datos. Un rol inválido o una fecha mal formada responden 400, y un filtro sin resultados responde 200 con lista vacía.
- El orden de las respuestas de error es 404 (no existe), 403 (sin permiso) y 400 (datos inválidos).
- *(Espacio para que los demás integrantes agreguen sus actividades)*
>>>>>>> a7dca92827789c4f9dd5ee0b66963f019b1ab46b

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :--- | :--- | :--- |
| Manejo uniforme de errores HTTP y formato único de respuesta | Dana Carmona Santamaria | [https://github.com/Peps06/desarrollo-web/commit/e5f4e](https://github.com/Peps06/desarrollo-web/commit/e5f4e8e965130cc7c9cd91d068bfefdc7105c63e) |
| Gestionar participantes del viaje (`GET`, `POST` y `DELETE /api/viajes/{id}/participantes`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/f5fe7b9](https://github.com/Peps06/desarrollo-web/commit/f5fe7b9) |
| Editar y cancelar viaje (`PUT` y `DELETE /api/viajes/{id}`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/e5ecb26](https://github.com/Peps06/desarrollo-web/commit/e5ecb26) |
<<<<<<< HEAD
| Editar, eliminar y filtrar actividades y viajes (`PUT` y `DELETE /api/actividades/{id}`, `GET /api/viajes/{id}/actividades?dia=&costoMax=`, `GET /api/viajes?destino=&rol=`) | Diego Rafael Jiménez Trujano | [https://github.com/Peps06/desarrollo-web/commit/d58e19e](https://github.com/Peps06/desarrollo-web/commit/d58e19effc920030ff78e5a0038e48aa4727d0ed) |
| Comprobante opcional en gastos y su consulta (`POST /api/gastos` multipart, `GET /api/gastos/{id}/comprobante`) | Citlaly Morales Viveros | [https://github.com/Peps06/desarrollo-web/commit/ec2137d](https://github.com/Peps06/desarrollo-web/commit/ec2137d4f0164c50794aa52e31bb472dc8d4e8d3) |
=======
| Edición y eliminación de actividades (`PUT` y `DELETE /api/actividades/{id}`) y filtros de búsqueda de actividades y viajes | Diego Jiménez | [https://github.com/Peps06/desarrollo-web/commit/d58e19e](https://github.com/Peps06/desarrollo-web/commit/d58e19e) |
| *(Espacio para actividades de los demás integrantes)* | | |
>>>>>>> a7dca92827789c4f9dd5ee0b66963f019b1ab46b

---

# 3. Problemas encontrados

- **Respuestas 401 en todas las pruebas:** Las pruebas con cURL respondían 401 aunque la petición fuera válida. La causa fue que el archivo de cookies no tenía una sesión activa[cite: 46]. Se resolvió iniciando sesión otra vez antes de repetir las pruebas.
- **Error de sintaxis HQL al iniciar Spring Boot en consultas de participantes:** Al arrancar la aplicación, Hibernate lanzó una excepción de sintaxis porque faltaba un espacio en blanco al concatenar las cadenas JPQL entre `JOIN FETCH p.usuario` y `WHERE`. Se solucionó agregando el espacio correspondiente en el repositorio y recompilando.
- **Invalidación de sesiones al comprobar persistencia tras reiniciar el servidor:** Al detener y reiniciar Spring Boot para verificar la persistencia en MySQL, las peticiones `cURL` devolvían `401` porque Spring Security almacena los `JSESSIONID` en memoria. Se solucionó renovando la cookie mediante `POST /api/auth/login` antes de consultar `GET /api/viajes`.
- **Error de conexión a MySQL al arrancar:** La aplicación no arrancaba con `Access denied for user 'bitacora_app'`. Había dos servidores MySQL en el equipo: el servicio `MySQL80` de Windows y el de WSL, ambos en el puerto 3306, y la aplicación en Windows se conectaba al que no tenía el usuario. Se resolvió corriendo la aplicación desde WSL.
- **Puerto 8080 ocupado:** Las peticiones respondían 404 con una página de Jetty porque otro programa usaba el puerto 8080. La aplicación se arrancó en el puerto 8081 para las pruebas.
- *(Espacio para los problemas de los demás integrantes)*

---

# 4. Avance funcional

**Operativas / completadas (módulos de Dana):**

- Formato único de error en toda la API, con código, mensaje y lista de campos cuando aplica.
- Respuestas 401 y 403 en formato JSON desde la configuración de seguridad.
- Respuesta 405 para métodos no permitidos y 404 para rutas que no existen.

**Operativas / completadas (módulos de Stephanie):**

- Consulta, incorporación por correo y eliminación de participantes de un viaje con validación de rol `ORGANIZADOR`, bloqueo de auto-eliminación y revocación inmediata de acceso en votación y gastos.
- Edición de datos generales de un viaje (`PUT /api/viajes/{id}`) con validación de conflictos cuando se acortan fechas y existen actividades fuera del nuevo rango.
- Cancelación lógica de viajes (`DELETE /api/viajes/{id}`) cambiando su estado a `CANCELADO` para ocultarlos del listado activo de todos los participantes sin perder integridad referencial en MySQL.

**Operativas / completadas (módulos de Diego):**

<<<<<<< HEAD
- Edición y eliminación de actividades (`PUT` y `DELETE /api/actividades/{id}`).
- Listado de actividades de un viaje con filtros por día y costo máximo.
- Listado de los viajes activos del usuario con filtros por destino y rol.
=======
- Edición de actividades (`PUT /api/actividades/{id}`) con permiso solo para el autor o el organizador, y respuesta con total de votos y voto del usuario.
- Eliminación de actividades (`DELETE /api/actividades/{id}`) con borrado de sus votos en la misma transacción.
- Filtros de actividades por día y costo máximo (`GET /api/viajes/{id}/actividades`) y de viajes por destino y rol (`GET /api/viajes`), opcionales y combinables.
>>>>>>> a7dca92827789c4f9dd5ee0b66963f019b1ab46b

**Operativas / completadas (módulos de Citlaly):**

- Registro de gastos con comprobante opcional (`POST /api/gastos` con `multipart/form-data`).
- Consulta del comprobante de un gasto (`GET /api/gastos/{id}/comprobante`), visible solo para los participantes del viaje.

---

# 5. Uso de IA generativa

- **Dana**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Diseño del manejo común de errores, configuración de respuestas 401 y 403 en la seguridad, y revisión del formato de respuesta.
- **Código o contenido generado:** Formato de error, manejador global de errores, cambios en la configuración de seguridad y cambios en las llamadas de error de los controladores.
- **Modificaciones realizadas por el equipo:** Se realizó en manejo completo y unificado para todos los erres existentes, ya no están separados localmente.
- **Forma en que se verificó el resultado:** Pruebas con cURL y revisión del formato de cada respuesta[cite: 46]. El detalle está en `documentacion/reportes/ia-desarrollo/` y en `documentacion/reportes/ia-documental/`.

---

- **Stephanie**

- **Herramienta utilizada:** Gemini
- **Actividad para la que se utilizó:** Diseño y codificación de endpoints para gestionar participantes, editar y cancelar viajes, depuración de consultas JPQL y validación de pruebas de integración y persistencia.
- **Código o contenido generado:** DTOs (`ParticipanteRequest`, `ParticipanteResponse`), servicios transaccionales (`ParticipanteService`, ampliación de `ViajeService`), controladores REST (`ParticipanteController`, ampliación de `ViajeController`) y consultas JPQL en `ParticipanteViajeRepository` y `ActividadRepository`.
- **Modificaciones realizadas por el equipo:** Ajuste de referencias entre entidades (`getUsuario()`), integración con las excepciones globales de la API y adaptación de escenarios de prueba según los datos precargados en MySQL.
- **Forma en que se verificó el resultado:** Pruebas con `cURL` en WSL verificando códigos HTTP (`200`, `201`, `204`, `400`, `403`, `404`), validación cruzada con votación y reinicio del servidor para comprobar persistencia en MySQL. El detalle está en `documentacion/reportes/ia-desarrollo/stephanie/` (`r-ia-dev-s02.md`, `r-ia-dev-s03.md`) y en `documentacion/reportes/ia-documental/stephanie/` (`r-ia-doc-s02.md`, `r-ia-doc-s03.md`).

<<<<<<< HEAD
=======
---

-**Diego**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Apoyo en la codificación de la edición y eliminación de actividades en `ActividadController` y `ActividadService`, los filtros de actividades y de viajes, y las consultas JPQL de `ActividadRepository` y `ParticipanteViajeRepository`.
- **Código o contenido generado:** Los métodos de editar, eliminar y listar actividades, la verificación de permisos, las consultas con parámetros opcionales y el borrado de votos, además de los comandos de prueba.
- **Modificaciones realizadas por el equipo:** No se modificó el código generado antes del commit. Se reutilizaron `ActividadRequest`, `ActividadResponse`, `buscarParticipacion` y `ErrorNegocioException`, y los cambios en `ViajeController`, `ViajeService` y `ParticipanteViajeRepository` se limitaron a agregar los filtros opcionales.
- **Forma en que se verificó el resultado:** Compilación y pruebas manuales hechas por mí con PowerShell y sesión por cookie: filtros solos y combinados, editar una actividad propia (200) y ajena (403), eliminar una actividad con votos (204) y comprobación en MySQL de que sus votos desaparecen, y los casos 404 y 400. El detalle completo se encuentra en `documentacion/reportes/ia-desarrollo/diego/r-ia-dev-s02.md` y en `documentacion/reportes/ia-documental/diego/r-ia-doc-s02.md`.

- *(Espacio para el uso de IA de los demás integrantes)*

---

>>>>>>> a7dca92827789c4f9dd5ee0b66963f019b1ab46b
# 6. Participación individual

- **Stephanie Elizdeth Hernández Prieto:** Implementó los endpoints para gestionar participantes del viaje (`GET`, `POST` y `DELETE /api/viajes/{id}/participantes`) y para editar y cancelar viajes (`PUT` y `DELETE /api/viajes/{id}`), incorporando validaciones de permisos de organizador, conflictos de fechas con actividades, borrado lógico y pruebas de persistencia con `cURL` en WSL.
- **Dana Carmona Santamaría:** Creó el manejo común de errores HTTP y el formato de respuesta de toda la API. Configuró las respuestas 401 y 403 en JSON y revisó las respuestas de error con pruebas de cURL.
<<<<<<< HEAD
- **Diego Rafael Jiménez Trujano:** Implementó la edición y eliminación de actividades (`PUT` y `DELETE /api/actividades/{id}`), el listado de actividades de un viaje con filtros por día y costo máximo, y el listado de los viajes activos del usuario con filtros por destino y rol.
=======
- **Diego Rafael Jiménez Trujano:** Implementación de **Editar y eliminar actividades** (endpoints `PUT` y `DELETE /api/actividades/{id}`, permiso solo para el autor o el organizador, borrado de los votos de la actividad en la misma transacción) e implementación de **Filtros de búsqueda** (`GET /api/viajes/{id}/actividades` por día y costo máximo, y `destino` y `rol` en `GET /api/viajes`), pruebas con PowerShell y verificación en MySQL.
>>>>>>> a7dca92827789c4f9dd5ee0b66963f019b1ab46b
- **Alfonso Mendoza Vásquez:** *(Completar)*
- **Citlaly Morales Viveros:** Agregó el comprobante opcional al registrar un gasto (`POST /api/gastos` con `multipart/form-data`) y su consulta (`GET /api/gastos/{id}/comprobante`), visible solo para los participantes del viaje.

---

# 7. Próximas actividades

- Actividad 1: Revisar que los endpoints de los demás integrantes respondan con el formato de error común.
- Actividad 2: Iniciar el desarrollo de las vistas frontend (formularios, tablas y consumo de la API con `fetch`) para la gestión de viajes y participantes.
---

## Observaciones adicionales
En esta entrega el API REST quedó completo: todos los módulos de la API están implementados y comparten el mismo formato de errores, por lo que el frontend podrá consumirlos de forma uniforme. Antes de iniciar las vistas conviene verificar que cada endpoint responda con ese formato, como se plantea en las próximas actividades.