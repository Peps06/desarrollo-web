# Reporte de Avance del Proyecto

**Equipo:** Bitácora de viajes colaborativa (Dana Carmona Santamaría, Stephanie Elizdeth Hernández Prieto, Diego Rafael Jiménez Trujano, Alfonso Mendoza Vásquez, Citlaly Morales Viveros)

**Fecha:** 20/11/2026

**Número de reporte:** 4

**Repositorio GitHub:** [https://github.com/Peps06/desarrollo-web](https://github.com/Peps06/desarrollo-web)

---

# 1. Actividades realizadas

- **Dana:** Login y registro en el frontend. Se hicieron las páginas de inicio de sesión y de registro con Thymeleaf, la validación de los datos antes de enviarlos, los errores debajo de cada campo y el botón «Cerrar sesión» en la barra de navegación.
- **Stephanie:** Formulario de crear viaje (`/viajes/nuevo`) y Lista de viajes (`/viajes`) en el frontend. Se construyeron las plantillas Thymeleaf conectadas a `fragments/base.html`, la validación previa al envío y creación de viajes mediante `fetch()` hacia `POST /api/viajes`, y el listado en cuadrícula con tarjetas clicables hacia `/viajes/{id}`, distinción visual de rol (Organizador / Participante), filtros en vivo sin recargar la página por destino y rol (`GET /api/viajes?destino=...&rol=...`) y estado vacío («Aún no tienes viajes»).
- **Stephanie:** Formulario de crear viaje (`/viajes/nuevo`), Lista de viajes (`/viajes`), Mensajes de confirmación y error en la UI (`mensajes.js` y `fragments/base.html`) y Checklist colaborativa de preparación y equipaje (`GET/POST /api/viajes/{id}/tareas`, `PATCH /api/tareas/{id}/toggle`, `DELETE /api/tareas/{id}` e interfaz en `#zona-checklist`).

*(Pendiente: actividades del resto del equipo.)*

---

# 2. Evidencias de trabajo

| Actividad | Integrante responsable | Enlace a commit |
| :--- | :--- | :--- |
| Login y registro en el frontend | Dana Carmona Santamaría | [https://github.com/Peps06/desarrollo-web/commit/702fff77765d6726d7748f55771c5997bd00d4b4](https://github.com/Peps06/desarrollo-web/commit/702fff77765d6726d7748f55771c5997bd00d4b4) |
| Formulario de crear viaje en frontend (`/viajes/nuevo`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/d41feee](https://github.com/Peps06/desarrollo-web/commit/d41feee)  |
| Lista de viajes en frontend con filtros (`/viajes`) | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/9a9bfe0](https://github.com/Peps06/desarrollo-web/commit/9a9bfe0) |
| Mensajes compartidos de confirmación y error en UI | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/a3faeed](https://github.com/Peps06/desarrollo-web/commit/a3faeed) |
| Endpoints y checklist colaborativa de tareas | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/14edfac](https://github.com/Peps06/desarrollo-web/commit/14edfac) |

*(Pendiente: filas de los demás integrantes.)*

---

# 3. Problemas encontrados

- **Página de login de prueba (Dana):** La versión que había en el repo traía credenciales escritas y clases que no existen en el CSS. Se reescribió por completo.
- **Ruta `/viajes` no mapeada en `PaginaController` (Stephanie):** Al navegar a `http://localhost:8085/viajes`, el servidor devolvía `{"codigo":404,"mensaje":"El recurso no existe"}` porque `PaginaController` tenía registradas `/viajes/nuevo` y `/viajes/{id}` pero faltaba `@GetMapping("/viajes")`. Se agregó el mapeo hacia `viajes/lista`.
- **Sincronización de archivos estáticos y protocolo HTTPS en navegador (Stephanie):** Los scripts y hojas de estilo editados en `frontend/` no se reflejaban hasta copiarlos a `backend/src/main/resources/static/`, y el navegador intentó abrir inicialmente `https://localhost:8085` provocando un error de método inválido en Tomcat (`0x160x030x01`). Se sincronizaron los recursos estáticos y se verificó el acceso explícito mediante `http://`.
- **Sesión caducada (`401`) en pruebas de permisos de tareas con `cURL` (Stephanie):** Al probar el acceso de un usuario ajeno a `/api/viajes/1/tareas` sin generar previamente una cookie de sesión vigente, Spring Security respondía `401` («Tu sesión expiró») antes de llegar al servicio. Se autenticó un usuario externo mediante `POST /api/auth/login` guardando su cookie y se verificó la respuesta `403` («No participas en este viaje»).

---

# 4. Avance funcional

**Operativas / completadas (módulos de Dana):**

- Página de inicio de sesión con validación y mensajes de error (`/login`).
- Página de registro con validación y mensajes de error (`/registro`).
- Botón «Cerrar sesión» en la barra de navegación.


**Operativas / completadas (módulos de Stephanie):**

- Formulario de creación de viaje (`/viajes/nuevo`) con validación de destino, fechas coherentes (`fechaFin >= fechaInicio`), presupuesto mayor a cero y moneda (`MXN`, `USD`, `EUR`), envío asíncrono con `fetch()` y redirección al detalle del viaje creado.
- Vista de lista de viajes (`/viajes`) con cuadrícula responsiva de tarjetas, insignia diferenciada para Organizador y Participante, enlace directo al detalle `/viajes/{id}`, filtrado dinámico en vivo por destino (con *debounce*) y por rol sin recargar la página, y vista de estado vacío («Aún no tienes viajes»).
- Formulario de creación de viaje (`/viajes/nuevo`) con validación en cliente y envío `fetch()`.
- Vista de lista de viajes (`/viajes`) con distinción visual de rol, filtros dinámicos sin recarga por destino y rol y estado vacío.
- Módulo compartido de mensajes flotantes (`mensajes.js` en `fragments/base.html`) con auto-cierre en 4 segundos, cierre manual y traducción de errores HTTP (`400`, `401`, `403`, `404` y error de red).
- Endpoints REST de tareas y checklist colaborativa en `#zona-checklist` (`viajes/detalle.html`) con contador «X de Y completadas», alta, eliminación y marcado tachado en tiempo real persistido en MySQL.

**Operativas / completadas (módulos de Diego):**

*(Pendiente)*

**Operativas / completadas (módulos de Alfonso):**

*(Pendiente)*

**Operativas / completadas (módulos de Citlaly):**

*(Pendiente)*

---

# 5. Uso de IA generativa

- **Dana**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Diseño de las páginas de login y registro, la validación en JavaScript, el envío con `fetch()` y el botón de cerrar sesión.
- **Código o contenido generado:** `login.html`, `registro.html`, `static/js/validacion.js`, `static/js/auth.js` y el botón de cerrar sesión en `fragments/base.html`.
- **Modificaciones realizadas por el equipo:** Se usaron los fragmentos y las clases CSS que ya existían (`head`, `navbar`, `.formulario`, `.campo`, `.campo__error`, `.mensaje-error`). Se quitaron las credenciales de la versión de prueba.
- **Forma en que se verificó el resultado:** Pruebas con `curl` y pruebas manuales en el navegador. El detalle está en `documentacion/reportes/ia-desarrollo/dana/r-ia-dev-s07.md`.

---

- **Stephanie**

- **Herramienta utilizada:** Gemini (Google)
- **Actividad para la que se utilizó:** Construcción de las vistas Thymeleaf y la lógica JavaScript (`fetch()`) para el Formulario de crear viaje (`/viajes/nuevo`) y la Lista de viajes (`/viajes`) con filtros dinámicos, además del diagnóstico de rutas MVC en `PaginaController` y errores HTTP/protocolo en entorno local.
- **Código o contenido generado:** `viajes/nuevo.html`, `viajes/lista.html`, `static/js/viaje-nuevo.js`, `static/js/viajes-lista.js`, actualización de `fragments/base.html` y mapeo `@GetMapping("/viajes")` en `PaginaController.java`.
- **Modificaciones realizadas por el equipo:** Se adaptaron las plantillas y tarjetas a las clases definidas en `guia-css.md` y `componentes.css` (`.pagina`, `.rejilla`, `.tarjeta--enlace`, `.insignia-organizador`, `.vacio`) y se conectaron con los parámetros exactos de `ViajeController` (`destino` y `rol`).
- **Forma en que se verificó el resultado:** Pruebas funcionales en navegador sobre `http://localhost:8085/viajes/nuevo` y `http://localhost:8085/viajes` comprobando validaciones, creación en MySQL, filtros combinables sin recarga y estado vacío. El detalle está en `documentacion/reportes/ia-desarrollo/stephanie/r-ia-dev-s04.md` y `documentacion/reportes/ia-documental/stephanie/r-ia-doc-s04.md`.

---

- **Diego**

*(Pendiente)*

---

- **Alfonso**

*(Pendiente)*

---

- *(Pendiente: uso de IA generativa de Citlaly Morales Viveros)*

---

# 6. Participación individual

- **Dana Carmona Santamaría:** Hizo las páginas de inicio de sesión y de registro, la validación de los datos, los mensajes de error y el botón de cerrar sesión. Revisó la respuesta del backend al registrar un correo repetido.
- **Stephanie Elizdeth Hernández Prieto:** Desarrolló el formulario de creación de viajes (`/viajes/nuevo`) y la vista de listado de viajes (`/viajes`) con tarjetas diferenciadas por rol y filtros en tiempo real mediante `fetch()`, integrando la plantilla base compartida y corrigiendo el mapeo de rutas en `PaginaController`.

*(Pendiente: participación del resto del equipo.)*

---

# 7. Próximas actividades

- Integración del módulo compartido de notificaciones y traducción de errores HTTP en la interfaz (`mensajes.js` en `fragments/base.html`).
- Implementación de los endpoints REST y la interfaz interactiva de checklist colaborativa dentro del detalle del viaje (`#zona-checklist`).
- Pruebas integrales de flujo completo (registro, creación de viaje, actividades, votación, gastos, balance, checklist y exportación a PDF) y preparación de evidencias finales.

---

## Observaciones adicionales

- Se recomienda mantener sincronizados los archivos de `frontend/js/` con `backend/src/main/resources/static/js/` antes de cada commit para que Spring Boot sirva siempre la versión más reciente de los scripts.