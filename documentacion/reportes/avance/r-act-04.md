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
- **Dana:** Balance de deudas del grupo en el frontend. Se hizo la sección «Balance del grupo» en el detalle del viaje, con una tarjeta por participante y la lista de transferencias, que se actualiza sin recargar al registrar un gasto.
- **Dana:** Conversor automático de divisas en el frontend. Se agregó el selector de moneda (MXN, USD, EUR) al formulario de gastos, que consulta el tipo de cambio a Frankfurter, muestra el equivalente en la moneda del viaje antes de enviar y guarda el gasto ya convertido, con aviso si no hay internet.
- **Dana:** Conexión del backend con el frontend. Se configuró Maven para copiar `frontend/` a `static/` al compilar, y se borraron las copias de CSS y JS que vivían dentro del backend, para que los archivos vivan solo en `frontend/`. También se agregaron los archivos de cookies de sesión al `.gitignore`.
- **Alfonso:** Votación en vivo en el frontend (`static/js/votos.js`). Cada botón de voto usa `fetch()` para votar (`POST /api/actividades/{id}/votos`) o quitar el voto (`DELETE` a la misma ruta) y actualiza el contador y el estado del botón con la respuesta `{actividadId, totalVotos, votadaPorMi}`, sin recargar la página. Antes de programar se revisó el estado real del backend de votos, las tarjetas de actividad y los mensajes compartidos.
- **Citlaly:** Listado de gastos por viaje en el backend (`GET /api/viajes/{id}/gastos`), solo para participantes, y ampliación de la respuesta de gastos con nombre de quien pagó, moneda original, tipo de cambio y ruta del comprobante. Se renombró `totalViaje` a `totalGastado` para cumplir `api.md`. Formulario, lista y edición/eliminación de gastos en `gastos.js` con `fetch()`, sin recargar la página (HU-31). Barra de presupuesto en `presupuesto.js` con colores por porcentaje, aviso de presupuesto excedido y actualización del encabezado al cambiar un gasto (HU-32). Vista previa del comprobante antes de guardar y ventana modal para verlo, con concepto, monto, quién pagó y mensaje de permiso (HU-39).
- **Diego:** Detalle del viaje con línea de tiempo por días (`/viajes/{id}`) y Exportar itinerario a PDF (`GET /api/viajes/{id}/exportar-pdf`). Se construyó la plantilla `viajes/detalle.html` con encabezado, filtros y modal de confirmación, el script `viaje-detalle.js` (carga con `fetch()`, proponer, editar y eliminar actividades sin recargar, filtros por día y costo, evento `viaje:actualizado`), el servicio `PdfService` con OpenPDF y el controlador `ExportarPdfController`. También se ajustó `buscarParticipacion` para que un viaje cancelado responda 404.

---

# 2. Evidencias de trabajo

| Actividad                                                                               | Integrante responsable              | Enlace a commit                                                                                                                                                                      |
| :-------------------------------------------------------------------------------------- | :---------------------------------- | :----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Login y registro en el frontend                                                         | Dana Carmona Santamaría             | [https://github.com/Peps06/desarrollo-web/commit/702fff77765d6726d7748f55771c5997bd00d4b4](https://github.com/Peps06/desarrollo-web/commit/702fff77765d6726d7748f55771c5997bd00d4b4) |
| Formulario de crear viaje en frontend (`/viajes/nuevo`)                                 | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/d41feee](https://github.com/Peps06/desarrollo-web/commit/d41feee)                                                                   |
| Lista de viajes en frontend con filtros (`/viajes`)                                     | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/9a9bfe0](https://github.com/Peps06/desarrollo-web/commit/9a9bfe0)                                                                   |
| Mensajes compartidos de confirmación y error en UI                                      | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/a3faeed](https://github.com/Peps06/desarrollo-web/commit/a3faeed)                                                                   |
| Endpoints y checklist colaborativa de tareas                                            | Stephanie Elizdeth Hernández Prieto | [https://github.com/Peps06/desarrollo-web/commit/14edfac](https://github.com/Peps06/desarrollo-web/commit/14edfac)                                                                   |
| Balance de deudas del grupo en el frontend                                              | Dana Carmona Santamaría             | [https://github.com/Peps06/desarrollo-web/commit/5f1c4393f450922f9619696ecadcca6c0ace6968](https://github.com/Peps06/desarrollo-web/commit/5f1c4393f450922f9619696ecadcca6c0ace6968) |
| Conversor automático de divisas en el frontend                                          | Dana Carmona Santamaría             | [https://github.com/Peps06/desarrollo-web/commit/f4c4197dfdac9078351e8c3636b06a2df8ad9e0d](https://github.com/Peps06/desarrollo-web/commit/f4c4197dfdac9078351e8c3636b06a2df8ad9e0d) |
| Votación en vivo con `fetch()` (`votos.js`)                                             | Alfonso Mendoza Vásquez             | [https://github.com/Peps06/desarrollo-web/commit/152ae73e5d7f95c57b02944bb0d500a3dbd950de](https://github.com/Peps06/desarrollo-web/commit/152ae73e5d7f95c57b02944bb0d500a3dbd950de) |
| HU-31: registrar, editar y eliminar gastos (`gastos.js`) y listado por viaje en backend | Citlaly Morales Viveros             | [https://github.com/Peps06/desarrollo-web/commit/ae5826e](https://github.com/Peps06/desarrollo-web/commit/ae5826e)                                                                   |
| HU-32: barra de progreso del presupuesto (`presupuesto.js`)                             | Citlaly Morales Viveros             | [https://github.com/Peps06/desarrollo-web/commit/44c2fb5](https://github.com/Peps06/desarrollo-web/commit/44c2fb5)                                                                   |
| HU-39: vista previa y modal de comprobantes                                             | Citlaly Morales Viveros             | [https://github.com/Peps06/desarrollo-web/commit/3c01b024cd2c919440e6943a5c6dc1d97ac80222](https://github.com/Peps06/desarrollo-web/commit/3c01b024cd2c919440e6943a5c6dc1d97ac80222) |
| Merge de las historias de gastos, presupuesto y comprobantes                            | Citlaly Morales Viveros             | [https://github.com/Peps06/desarrollo-web/commit/b4e39a915d9197afa57ec38ce5cfa4e27a03d198](https://github.com/Peps06/desarrollo-web/commit/b4e39a915d9197afa57ec38ce5cfa4e27a03d198) |
| Detalle del viaje con línea de tiempo y exportación del itinerario a PDF | Diego Rafael Jiménez Trujano | [https://github.com/Peps06/desarrollo-web/commit/cdb786eecd431fce11bb09a0501f3e2d6b5e1348](https://github.com/Peps06/desarrollo-web/commit/cdb786eecd431fce11bb09a0501f3e2d6b5e1348) |

---

# 3. Problemas encontrados

- **Página de login de prueba (Dana):** La versión que había en el repo traía credenciales escritas y clases que no existen en el CSS. Se reescribió por completo.
- **Ruta `/viajes` no mapeada en `PaginaController` (Stephanie):** Al navegar a `http://localhost:8085/viajes`, el servidor devolvía `{"codigo":404,"mensaje":"El recurso no existe"}` porque `PaginaController` tenía registradas `/viajes/nuevo` y `/viajes/{id}` pero faltaba `@GetMapping("/viajes")`. Se agregó el mapeo hacia `viajes/lista`.
- **Sincronización de archivos estáticos y protocolo HTTPS en navegador (Stephanie):** Los scripts y hojas de estilo editados en `frontend/` no se reflejaban hasta copiarlos a `backend/src/main/resources/static/`, y el navegador intentó abrir inicialmente `https://localhost:8085` provocando un error de método inválido en Tomcat (`0x160x030x01`). Se sincronizaron los recursos estáticos y se verificó el acceso explícito mediante `http://`.
- **Sesión caducada (`401`) en pruebas de permisos de tareas con `cURL` (Stephanie):** Al probar el acceso de un usuario ajeno a `/api/viajes/1/tareas` sin generar previamente una cookie de sesión vigente, Spring Security respondía `401` («Tu sesión expiró») antes de llegar al servicio. Se autenticó un usuario externo mediante `POST /api/auth/login` guardando su cookie y se verificó la respuesta `403` («No participas en este viaje»).
- **Botón de voto ausente en las tarjetas (Alfonso):** Al revisar `viaje-detalle.js` se vio que las tarjetas de actividad solo muestran el texto «N votos» y no tienen botón de voto, y que `detalle.html` no carga `votos.js`. Como esos archivos son de Diego, no se modificaron; se le entregó el cambio exacto que necesita (un botón `.btn-voto` con su contador, la etiqueta `<script>` de `votos.js` y un listener del evento `viaje:recargar`). Hasta entonces `votos.js` no tiene un botón al cual conectarse.
- **Error `Access denied` al arrancar el servidor (Alfonso):** `./mvnw spring-boot:run` desde PowerShell falló porque se conectó al MySQL de Windows, donde no existe el usuario `bitacora_app`, y no al MySQL de WSL donde estaban el usuario y los datos. Se resolvió arrancando la aplicación desde WSL.
- **Error `bad interpreter` al ejecutar `./mvnw` (Citlaly):** El archivo `mvnw` tenía saltos de línea de Windows (CRLF) y Linux no los reconoce. Se resolvió convirtiéndolo a LF con `sed -i 's/\r$//' mvnw` y dándole permiso de ejecución con `chmod +x mvnw`. Después la aplicación compiló y arrancó.
- **Error `Access denied` de MySQL para `bitacora_app` (Citlaly):** Spring no podía conectarse a la base con ese usuario, aunque el login directo desde WSL sí respondía. Se investigó el puerto (`3306` lo usaba solo la instancia de WSL) y la clave configurada. La causa fue que habia un caracter no detectado en la contraseña, se resolvió con una clave distinta en `application.properties`.
- **Archivo `gastos.js` modificado por el equipo (Citlaly):** Al agregar la vista previa y la modal de comprobantes se partió de la versión del equipo, que ya tenía la conversión de divisas de Dana, y no se quitó esa parte.
- **Viaje cancelado respondía 403 y no 404 (Diego):** Quien no participaba en un viaje cancelado recibía 403 porque `buscarParticipacion` solo comprobaba que el viaje existiera. Se cambió `existsById` por una consulta que también revisa el estado, para respetar el orden 404, 403 y 400.
- **Script del detalle no servido por Spring (Diego):** El script estaba solo en `frontend/js/` y Spring sirve los archivos desde `static/`, por lo que la página no cargaba la lógica. Se agregó la copia en `backend/src/main/resources/static/js/`; después, el commit de conexión entre backend y frontend dejó `frontend/` como única fuente.
- **Botones de editar y eliminar desalineados (Diego):** Quedaban mal alineados en la fila de la actividad; se movieron a una fila propia dentro de la información de la actividad.
- **Ejecución en WSL (Diego):** `.\mvnw.cmd` no existe en WSL y devolvía «command not found». Se usó `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`, con MySQL de WSL iniciado, porque el puerto 8080 estaba ocupado.

---

# 4. Avance funcional

**Operativas / completadas (módulos de Dana):**

- Página de inicio de sesión con validación y mensajes de error (`/login`).
- Página de registro con validación y mensajes de error (`/registro`).
- Botón «Cerrar sesión» en la barra de navegación.
- Sección «Balance del grupo» en el detalle del viaje (`#zona-balance`) con tarjetas por participante y lista de transferencias, actualizada sin recargar al registrar un gasto. Pendiente: re-agregar la línea `<script>` de `balance.js` en `detalle.html` y probarla en vivo.
- Conversor de divisas en el formulario de gastos: selector de moneda, equivalente en la moneda del viaje antes de enviar y gasto guardado ya convertido, con aviso si falla el servicio externo.

**Operativas / completadas (módulos de Stephanie):**

- Formulario de creación de viaje (`/viajes/nuevo`) con validación de destino, fechas coherentes (`fechaFin >= fechaInicio`), presupuesto mayor a cero y moneda (`MXN`, `USD`, `EUR`), envío asíncrono con `fetch()` y redirección al detalle del viaje creado.
- Vista de lista de viajes (`/viajes`) con cuadrícula responsiva de tarjetas, insignia diferenciada para Organizador y Participante, enlace directo al detalle `/viajes/{id}`, filtrado dinámico en vivo por destino (con _debounce_) y por rol sin recargar la página, y vista de estado vacío («Aún no tienes viajes»).
- Módulo compartido de mensajes flotantes (`mensajes.js` en `fragments/base.html`) con auto-cierre en 4 segundos, cierre manual y traducción de errores HTTP (`400`, `401`, `403`, `404` y error de red).
- Endpoints REST de tareas y checklist colaborativa en `#zona-checklist` (`viajes/detalle.html`) con contador «X de Y completadas», alta, eliminación y marcado tachado en tiempo real persistido en MySQL.

**Operativas / completadas (módulos de Diego):**

- Detalle del viaje (`/viajes/{id}`) con encabezado (destino, fechas, participantes, presupuesto, gastado y restante) y línea de tiempo por días ordenada por hora, con marca de la actividad más votada y estado vacío por día.
- Proponer, editar (incluso cambiando de día) y eliminar actividades sin recargar la página, con validación en cliente, errores del servidor con `mensajes.js`, modal de confirmación y botones visibles solo para el autor o el organizador.
- Filtros de actividades por día y costo máximo sin recargar.
- Exportar itinerario a PDF (`GET /api/viajes/{id}/exportar-pdf`) con OpenPDF: descarga `itinerario-<destino>.pdf` con respuestas 404, 403 y 401 según el caso.
- Contenedores `zona-*` y evento `viaje:actualizado` para conectar los módulos de los demás integrantes.

**Operativas / completadas (módulos de Alfonso):**

- `votos.js` terminado: alterna entre votar (`POST`) y quitar el voto (`DELETE`) según el estado del botón, actualiza contador y clase `votado` con la respuesta de la API, deshabilita el botón mientras la petición está en curso y lo rehabilita al terminar, y muestra los errores `400`, `403`, `404` y de red con las funciones de `mensajes.js` sin cambiar el contador. Usa un solo listener en `document` para que funcione aunque las tarjetas se vuelvan a crear.
- **Pendiente de integración:** el botón de voto en las tarjetas, la carga de `votos.js` en `detalle.html` y el refresco de la insignia «Más votada» dependen de cambios en el código de Diego.

**Operativas / completadas (módulos de Citlaly):**

- Listado de gastos por viaje en el backend (`GET /api/viajes/{id}/gastos`), solo para participantes, con nombre de quien pagó, moneda original, tipo de cambio y ruta del comprobante en la respuesta.
- Formulario de gastos (`gastos.js`): registro con validación, edición y eliminación con confirmación. Editar y eliminar aparecen solo para el autor del gasto o el organizador.
- Barra de presupuesto (`presupuesto.js`): estados de color por porcentaje (menor a 75%, de 75% a 100% y arriba de 100%), aviso de presupuesto excedido y actualización del encabezado del viaje al cambiar un gasto.
- Comprobantes: subida con vista previa antes de guardar, y ventana modal para ver la imagen con concepto, monto y quién pagó, o un mensaje si no hay permiso.
- **Pendiente de prueba:** flujo completo con dos usuarios (participante y organizador), confirmación de que la imagen se muestra en la modal, y verificación de la clase `mensaje-error` en `mensajes.js`.

---

# 5. Uso de IA generativa

- **Dana**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Diseño de las páginas de login y registro, la validación en JavaScript, el envío con `fetch()` y el botón de cerrar sesión. También el balance de deudas del grupo y conversor automático de divisas en el frontend.
- **Código o contenido generado:** `login.html`, `registro.html`, `frontend/js/validacion.js`, `frontend/js/auth.js` y el botón de cerrar sesión en `fragments/base.html`, `frontend/js/balance.js`, `frontend/js/divisas.js`, cambios en `frontend/js/gastos.js` y en `detalle.html`, y la configuración de Maven en `backend/pom.xml` para copiar `frontend/` a `static/` al compilar.
- **Modificaciones realizadas por el equipo:** Se usaron los fragmentos y las clases CSS que ya existían (`head`, `navbar`, `.formulario`, `.campo`, `.campo__error`, `.mensaje-error`). Se usaron las clases CSS que ya existían (`.saldo-favor`, `.saldo-deuda`, `.saldo-cero`, `.transferencia`, `.selector-moneda`, `.equivalente-cambio`) y el patrón de `presupuesto.js`; no se tocó el backend.
- **Forma en que se verificó el resultado:** Pruebas con `curl` y pruebas manuales en el navegador. Revisión de sintaxis con `node --check` y pruebas en la consola del navegador (endpoint del balance, conversión y tipo de cambio). El detalle está en `documentacion/reportes/ia-desarrollo/dana/r-ia-dev-s07.md`.

---

- **Stephanie**

- **Herramienta utilizada:** Gemini (Google)
- **Actividad para la que se utilizó:** Construcción de las vistas Thymeleaf y la lógica JavaScript (`fetch()`) para el Formulario de crear viaje (`/viajes/nuevo`) y la Lista de viajes (`/viajes`) con filtros dinámicos, además del diagnóstico de rutas MVC en `PaginaController` y errores HTTP/protocolo en entorno local.
- **Código o contenido generado:** `viajes/nuevo.html`, `viajes/lista.html`, `static/js/viaje-nuevo.js`, `static/js/viajes-lista.js`, actualización de `fragments/base.html` y mapeo `@GetMapping("/viajes")` en `PaginaController.java`.
- **Modificaciones realizadas por el equipo:** Se adaptaron las plantillas y tarjetas a las clases definidas en `guia-css.md` y `componentes.css` (`.pagina`, `.rejilla`, `.tarjeta--enlace`, `.insignia-organizador`, `.vacio`) y se conectaron con los parámetros exactos de `ViajeController` (`destino` y `rol`).
- **Forma en que se verificó el resultado:** Pruebas funcionales en navegador sobre `http://localhost:8085/viajes/nuevo` y `http://localhost:8085/viajes` comprobando validaciones, creación en MySQL, filtros combinables sin recarga y estado vacío. El detalle está en `documentacion/reportes/ia-desarrollo/stephanie/r-ia-dev-s04.md` y `documentacion/reportes/ia-documental/stephanie/r-ia-doc-s04.md`.

---

- **Diego**

- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Construcción de la página de detalle del viaje con línea de tiempo y gestión de actividades en JavaScript (`fetch()`), y del backend de exportación del itinerario a PDF con OpenPDF; además, consultas sobre decisiones técnicas del desarrollo y credenciales de prueba.
- **Código o contenido generado:** `viajes/detalle.html`, `viaje-detalle.js` (en `frontend/js/`), `PdfService.java`, `ExportarPdfController.java` y un ajuste en `ViajeDetalleService.java`.
- **Modificaciones realizadas por el equipo:** Ninguna antes del commit: el código subido es idéntico al generado. Se usaron las clases de `componentes.css`, el fragmento `base.html` y `mensajes.js` ya existentes.
- **Forma en que se verificó el resultado:** Pruebas en el navegador y con `Invoke-WebRequest`. El detalle está en `documentacion/reportes/ia-desarrollo/diego/r-ia-dev-s03.md` y `documentacion/reportes/ia-documental/diego/r-ia-doc-s03.md`.

---

- **Alfonso**

- **Herramienta utilizada:** Claude Code (Anthropic), extensión de Visual Studio Code, modelos Claude Sonnet 5.5 y Claude Opus 5.5
- **Actividad para la que se utilizó:** Diagnóstico del estado del proyecto y escritura de `votos.js` (votación en vivo con `fetch()`), además de consultas para levantar el servidor local y entrar con los usuarios de prueba.
- **Código o contenido generado:** `frontend/js/votos.js` (funciones `actualizarBotonVoto` y `cambiarVoto` y un listener de clic en `document`).
- **Modificaciones realizadas por el equipo:** Se reutilizaron `manejarErrorApi` y `mostrarErrorRed` de `mensajes.js` y las clases `.btn-voto`, `.votado` y `.btn-voto__contador` que ya existían en `componentes.css`. No se tocó código de otros integrantes.
- **Forma en que se verificó el resultado:** Revisión de sintaxis con `node --check` y pruebas manuales en el navegador con los usuarios de prueba. Las pruebas completas dependen del botón de voto pendiente en las tarjetas. El detalle está en `documentacion/reportes/ia-desarrollo/alfonso/r-ia-dev-s05.md` y `documentacion/reportes/ia-documental/alfonso/r-ia-doc-s02.md`.

---

- **Citlaly**

- **Herramienta utilizada:** Claude (Anthropic), en claude.ai modelo Calude Haiku 5.5 medio.
- **Actividad para la que se utilizó:** Generación de código para HU-31 (backend y frontend), HU-32 y HU-39. Modificación de `gastos.js` para agregar la vista previa y la ventana modal de comprobantes. Explicaciones para ejecutar el proyecto desde WSL y para resolver errores de arranque (CRLF en `mvnw` y conexión a MySQL).
- **Código o contenido generado:** `GastoRepository.java` (consulta por viaje), `GastoService.java` (método `listarPorViaje`), `GastoResponse.java`, `GastoRegistradoResponse.java`, `ViajeDetalleController.java` (endpoint de gastos), `frontend/js/gastos.js` (versión inicial y vista previa/modal de comprobantes), `frontend/js/presupuesto.js` y el `<script>` de `presupuesto.js` en `detalle.html`.
- **Modificaciones realizadas por el equipo:** Dana agregó la conversión de divisas a `gastos.js`, el `<script>` de `balance.js` y la configuración de Maven. Yo agregé la vista previa y la modal de comprobantes sobre la versión del equipo.
- **Forma en que se verificó el resultado:** La revisión de sintaxis con `node --check` y con pruebas manuales en la interfaz desde el navegador con los datos de prueba.
- Los detalles están en `documentacion/reportes/ia-desarrollo/citlaly/` (`r-ia-dev-s02` a `s05`) y en `documentacion/reportes/ia-documental/citlaly/r-ia-doc-s01.md`.

---

# 6. Participación individual

- **Dana Carmona Santamaría:** Hizo las páginas de inicio de sesión y de registro, la validación de los datos, los mensajes de error y el botón de cerrar sesión. Revisó la respuesta del backend al registrar un correo repetido. También hizo el balance de deudas del grupo y el conversor de divisas en el frontend, y configuró Maven para que los archivos JS y CSS vivan solo en `frontend/`.
- **Stephanie Elizdeth Hernández Prieto:** Desarrolló el formulario de creación de viajes (`/viajes/nuevo`) y la vista de listado de viajes (`/viajes`) con tarjetas diferenciadas por rol y filtros en tiempo real mediante `fetch()`, integrando la plantilla base compartida y corrigiendo el mapeo de rutas en `PaginaController`.
- **Alfonso Mendoza Vásquez:** Revisó el backend de votos, las tarjetas de actividad y los mensajes compartidos antes de programar, y escribió `votos.js` para votar y quitar el voto con `fetch()` sin recargar la página. Documentó los cambios que necesita el código de Diego para conectar el botón y resolvió el error de conexión a MySQL al arrancar el servidor.
- **Citlaly Morales Viveros:** Implementó el listado de gastos por viaje en el backend y la respuesta ampliada de gastos. Desarrolló la interfaz de gastos (formulario, lista, edición y eliminación), la barra de presupuesto con actualización al cambiar gastos, y la vista previa y ventana modal de comprobantes. Resolvió el error de ejecución de `mvnw` en WSL, investigó el error de conexión a MySQL y documentó el uso de IA en sus reportes.
- **Diego Rafael Jiménez Trujano:** Desarrolló la página de detalle del viaje con la línea de tiempo por días y la gestión de actividades sin recargar, y la exportación del itinerario a PDF con OpenPDF. Corrigió el orden de errores para viajes cancelados y dejó los contenedores y el evento que usan los demás módulos del detalle.

_(Pendiente: participación de Diego.)_

---

# 7. Próximas actividades

- Integración del módulo compartido de notificaciones y traducción de errores HTTP en la interfaz (`mensajes.js` en `fragments/base.html`).
- Implementación de los endpoints REST y la interfaz interactiva de checklist colaborativa dentro del detalle del viaje (`#zona-checklist`).
- Integración del botón de voto en las tarjetas de actividad con Diego (botón `.btn-voto`, carga de `votos.js` en `detalle.html` y evento `viaje:recargar`) y pruebas de la votación en vivo con dos usuarios.
- Pruebas integrales de flujo completo (registro, creación de viaje, actividades, votación, gastos, balance, checklist y exportación a PDF) y preparación de evidencias finales.
- Re-agregar la línea `<script>` de `balance.js` en `detalle.html` y probar el balance en vivo en el navegador.

---

## Observaciones adicionales

- Los archivos JS y CSS viven solo en `frontend/`; Maven los copia a `static/` al compilar, así que ya no se copian a mano dentro del backend.
