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
- *(Espacio para que los demás integrantes agreguen sus actividades)*

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :--- | :--- | :--- |
| Manejo uniforme de errores HTTP y formato único de respuesta | Dana Carmona Santamaria | [https://github.com/Peps06/desarrollo-web/commit/e5f4e](https://github.com/Peps06/desarrollo-web/commit/e5f4e8e965130cc7c9cd91d068bfefdc7105c63e) |
| Gestionar participantes del viaje (`GET`, `POST` y `DELETE /api/viajes/{id}/participantes`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/f5fe7b9](https://github.com/Peps06/desarrollo-web/commit/f5fe7b9) |
| Editar y cancelar viaje (`PUT` y `DELETE /api/viajes/{id}`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/e5ecb26](https://github.com/Peps06/desarrollo-web/commit/e5ecb26) |
| *(Espacio para actividades de los demás integrantes)* | | |

---

# 3. Problemas encontrados

- **Respuestas 401 en todas las pruebas:** Las pruebas con cURL respondían 401 aunque la petición fuera válida. La causa fue que el archivo de cookies no tenía una sesión activa[cite: 46]. Se resolvió iniciando sesión otra vez antes de repetir las pruebas.
- **Error de sintaxis HQL al iniciar Spring Boot en consultas de participantes:** Al arrancar la aplicación, Hibernate lanzó una excepción de sintaxis porque faltaba un espacio en blanco al concatenar las cadenas JPQL entre `JOIN FETCH p.usuario` y `WHERE`. Se solucionó agregando el espacio correspondiente en el repositorio y recompilando.
- **Invalidación de sesiones al comprobar persistencia tras reiniciar el servidor:** Al detener y reiniciar Spring Boot para verificar la persistencia en MySQL, las peticiones `cURL` devolvían `401` porque Spring Security almacena los `JSESSIONID` en memoria. Se solucionó renovando la cookie mediante `POST /api/auth/login` antes de consultar `GET /api/viajes`.
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

**Operativas / completadas (módulos de los demás integrantes):**

- *(Espacio para completar)*

**Pendientes:**

- *(Espacio para completar)*

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

---

- *(Espacio para el uso de IA de los demás integrantes)*

---

# 6. Participación individual

- **Stephanie Elizdeth Hernández Prieto:** Implementó los endpoints para gestionar participantes del viaje (`GET`, `POST` y `DELETE /api/viajes/{id}/participantes`) y para editar y cancelar viajes (`PUT` y `DELETE /api/viajes/{id}`), incorporando validaciones de permisos de organizador, conflictos de fechas con actividades, borrado lógico y pruebas de persistencia con `cURL` en WSL.
- **Dana Carmona Santamaría:** Creó el manejo común de errores HTTP y el formato de respuesta de toda la API. Configuró las respuestas 401 y 403 en JSON y revisó las respuestas de error con pruebas de cURL.
- **Diego Rafael Jiménez Trujano:** *(Completar)*
- **Alfonso Mendoza Vásquez:** *(Completar)*
- **Citlaly Morales Viveros:** *(Completar)*

---

# 7. Próximas actividades

- Actividad 1: Revisar que los endpoints de los demás integrantes respondan con el formato de error común.
- Actividad 2: Iniciar el desarrollo de las vistas frontend (formularios, tablas y consumo de la API con `fetch`) para la gestión de viajes y participantes.
- *(Espacio para próximas actividades del resto del equipo)*

---

## Observaciones adicionales

- *(Espacio para observaciones del equipo)*