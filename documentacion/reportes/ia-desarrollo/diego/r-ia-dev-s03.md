# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 19-11-2026
**Número de reporte:** 03 (Diego Rafael Jiménez Trujano - Detalle del viaje con línea de tiempo por días y exportación del itinerario a PDF)

---

# 1. Breve descripción de uso

Apoyo en la codificación del frontend del **Detalle del viaje (`/viajes/{id}`)**, con encabezado, línea de tiempo por días y gestión de actividades (proponer, editar, eliminar y filtrar sin recargar la página), y del backend de **Exportar itinerario a PDF (`GET /api/viajes/{id}/exportar-pdf`)** con OpenPDF. Se reutilizaron `ViajeDetalleService.obtenerDetalle`, `buscarParticipacion`, `ErrorNegocioException`, el formato `ErrorResponse`, `GlobalExceptionHandler`, el fragmento `fragments/base.html`, `mensajes.js` y las clases de `componentes.css`.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic), con acceso al repositorio desde la aplicación de escritorio |
| **Modelo/temperatura** | Claude Sonnet 5.5 |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `702fff7` (Login y Logout) |
| **Código posterior** | Commit `cdb786e` (Linea de tiempo y exportación de itinerario) |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat 702fff7 cdb786e` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver pruebas](#pruebas): comprobaciones hechas por la herramienta en el navegador y pruebas con PowerShell (`Invoke-WebRequest`) y sesión por cookie, ejecutadas por mí |
| **Código de uso** | Generación / Depuración / Explicación |
| **Reflexión del estudiante** | Entendí que un viaje cancelado debe tratarse como inexistente para cualquiera que consulte su detalle o su PDF, y que para respetar el orden 404, 403 y 400 la comprobación de existencia tiene que ocurrir antes que la de participación; por eso en `buscarParticipacion` se cambió `existsById` por una consulta que también revisa el estado. También entendí que el PDF no debe volver a consultar la base de datos por su cuenta: reutiliza `obtenerDetalle`, así el documento muestra exactamente lo mismo que la pantalla y se mantienen las mismas reglas de permiso. En el frontend entendí que Spring sirve los archivos desde `static/`, por lo que el script tenía que estar ahí y no solo en `frontend/`, y que después de editar o eliminar una actividad conviene volver a pedir el detalle y avisar con el evento `viaje:actualizado` para que los demás módulos de la página (votos, presupuesto, gastos, balance) se actualicen. |

---

### Prompt original
> "Actúa como desarrollador senior full-stack (Spring Boot 3.2.5, Java 17, Thymeleaf, JavaScript con Fetch, MySQL). Estoy trabajando en el repositorio de la 'Bitácora de viajes colaborativa' y soy Diego, responsable del entregable 4 (frontend). Necesito dos historias:
>
> 1. **Detalle del viaje con línea de tiempo por días (HU-29)** en `/viajes/{id}`.
> 2. **Exportar itinerario a PDF (HU-34)** con OpenPDF 2.0.3.
>
> Reglas: el usuario siempre se toma de la sesión. El orden de las respuestas de error es 404, 403 y 400. Reutiliza `buscarParticipacion`, `ErrorNegocioException`, `ErrorResponse` y `GlobalExceptionHandler`. Usa DTOs y evita el problema N+1. Usa `mensajes.js` o `componentes.css` para los mensajes. Los comentarios deben ser breves, en español y sin mencionar historias de usuario ni mi nombre.
>
> Regla visual importante: basa el diseño en `login.html`, `lista.html`, `nuevo.html`, `base.html`, los CSS existentes, `guia-css.md` y el wireframe 04.
>
> Restricciones: no modifiques archivos de otros integrantes salvo que sea indispensable y avisa; no crees CSS nuevos; crea archivos solo si es indispensable; respeta CRLF; no inventes resultados de pruebas; si tienes dudas, pregúntalas todas de una sola vez.
>
> Entregables: el código completo de cada archivo con su ruta; comandos PowerShell con `Invoke-WebRequest` y `WebSession` contra `http://localhost:8081` con `ana@example.com`, `luis@example.com` y `Password123` sobre el viaje 1 de Oaxaca para éxito, 404, 403 y viaje sin actividades; pasos de prueba en el navegador y un resumen corto."

### Respuesta de IA
La herramienta modificó o creó seis archivos:

- **`ExportarPdfController`** (nuevo): `GET /api/viajes/{id}/exportar-pdf`, con el usuario tomado de la sesión. Responde `application/pdf` con `Content-Disposition: attachment` y el nombre `itinerario-<destino>.pdf`.
- **`PdfService`**: agregó `generarItinerario(Integer, String)`, que reutiliza `viajeDetalleService.obtenerDetalle` (por lo tanto hereda el 404 y el 403) y devuelve el record `ItinerarioPdf(nombreArchivo, contenido)`. El nombre del archivo se normaliza (sin acentos, caracteres no válidos reemplazados por `-`, máximo 60 caracteres). El PDF incluye destino, fechas, participantes, presupuesto, gastado y restante, un bloque por día con una tabla de hora, actividad y costo, el texto «Sin actividades registradas» para los días vacíos y la marca «MÁS VOTADA» en la actividad con más votos del día.
- **`ViajeDetalleService`**: en `buscarParticipacion` se reemplazó `existsById` por una consulta que considera inexistente un viaje cancelado, para que quien no participa reciba 404 y no 403. Es el único archivo de otro integrante que se modificó y fue indispensable para el orden de errores.
- **`viajes/detalle.html`**: encabezado del viaje, barra de filtros, formulario para proponer actividad, modal de confirmación para eliminar, botón «Exportar a PDF» y los contenedores `zona-votos`, `zona-presupuesto`, `zona-gastos`, `zona-balance`, `zona-checklist` y `zona-exportar` para los demás módulos.
- **`static/js/viaje-detalle.js`**: carga el detalle con `fetch` (`credentials: "same-origin"`), dibuja la línea de tiempo ordenada por hora, marca la actividad más votada, muestra los botones de editar y eliminar solo al autor o al organizador, valida en cliente y muestra los errores del servidor con `mensajes.js`, permite proponer, editar (incluso cambiando de día) y eliminar sin recargar, aplica los filtros por día y costo máximo y emite el evento `viaje:actualizado`. Se guardó una copia idéntica en `frontend/js/viaje-detalle.js`.

La herramienta no pudo ejecutar Maven ni la aplicación real (el entorno donde trabajó no tenía red). Compiló `PdfService` y el controlador con Java 17 contra clases de apoyo que simulaban OpenPDF y Spring, y revisó la sintaxis del script con `node --check`. La compilación con las dependencias reales y las pruebas contra la aplicación y MySQL las hice yo.

### Diferencias entre versiones
```text
37	1	backend/src/main/java/com/bitacora/viajes/controller/ExportarPdfController.java
259	1	backend/src/main/java/com/bitacora/viajes/service/PdfService.java
5	1	backend/src/main/java/com/bitacora/viajes/service/ViajeDetalleService.java
942	0	backend/src/main/resources/static/js/viaje-detalle.js
107	1	backend/src/main/resources/templates/viajes/detalle.html
942	1	frontend/js/viaje-detalle.js
```

El commit `cdb786e` contiene exactamente el código generado: el contenido de los archivos es idéntico al que entregó la herramienta.

---

### Pruebas

**Comprobaciones hechas por la herramienta** (no sustituyen las mías): en Chrome, con una API simulada y sin base de datos, la página del detalle pasó 33 comprobaciones: carga y encabezado, orden por hora, actividad más votada, día vacío, permisos de Luis y de Ana sobre los botones de editar y eliminar, presencia de los seis contenedores `zona-*`, validación en cliente y del servidor, proponer sin recargar, editar moviendo una actividad de día, eliminar con confirmación, filtros por día y costo, descarga del PDF con el nombre `itinerario-Oaxaca-Oaxaca.pdf`, mensajes visibles para 403 y 404, y ausencia de desplazamiento horizontal a 390 px y 900 px de ancho. No se ejecutó Maven ni la aplicación real.

**Pruebas hechas por mí** con la aplicación corriendo desde WSL en el puerto 8081 y los datos de `DataLoader` (viaje 1 de Oaxaca del 2026-12-20 al 2026-12-27; Ana es organizadora y Luis participante).


Inicio de sesión:

```
PS> $B = "http://localhost:8081"
PS> $null = Invoke-RestMethod -Method Post -Uri "$B/api/auth/login" -ContentType "application/json" -Body '{"correo":"ana@example.com","contrasena":"Password123"}' -SessionVariable ana
PS> $null = Invoke-RestMethod -Method Post -Uri "$B/api/auth/login" -ContentType "application/json" -Body '{"correo":"luis@example.com","contrasena":"Password123"}' -SessionVariable luis
```

Descarga del PDF (éxito) como organizadora y como participante:

```
PS> Invoke-WebRequest -UseBasicParsing -Uri "$B/api/viajes/1/exportar-pdf" -WebSession $ana -OutFile itinerario-ana.pdf

PS> Invoke-WebRequest -UseBasicParsing -Uri "$B/api/viajes/1/exportar-pdf" -WebSession $luis -OutFile itinerario-luis.pdf

```

Viaje sin actividades (viaje de Mérida creado por Ana) y usuario que no participa:

```
PS> (crear el viaje de Mérida con Ana y descargar su PDF)

PS> Invoke-WebRequest -UseBasicParsing -Uri "$B/api/viajes/<id de Mérida>/exportar-pdf" -WebSession $luis

```
Viaje inexistente, viaje cancelado y sin sesión:

```
PS> Invoke-WebRequest -UseBasicParsing -Uri "$B/api/viajes/999999/exportar-pdf" -WebSession $ana


PS> (cancelar un viaje y pedir su PDF con un usuario que no participa)


PS> Invoke-WebRequest -UseBasicParsing -Uri "$B/api/viajes/1/exportar-pdf"


Pruebas en el navegador (`http://localhost:8081/viajes/1`):


---

## Observaciones adicionales

El script se subió en `backend/src/main/resources/static/js/` porque Spring solo sirve los archivos de `static/`, y además se guardó la misma copia en `frontend/js/`. Después, el commit `dfe85d5` (conexión backend y frontend) eliminó la carpeta `static` del backend y configuró `pom.xml` para copiar `frontend/` a `static` al compilar, así que la versión vigente del script es la de `frontend/js/viaje-detalle.js`.

Los demás módulos de la página (votos, presupuesto, gastos, balance, checklist y divisas) los desarrollan otros integrantes y se conectan a los contenedores `zona-*` y al evento `viaje:actualizado`; no se modificaron. Pendientes conocidos que no se resolvieron en este entregable: el menú de navegación queda pegado en pantallas muy angostas (`responsive.css`) y `application.properties` sigue con el puerto 8080.

Problemas durante el desarrollo: al principio un viaje cancelado devolvía 403 y no 404 a quien no participaba, y se corrigió en `buscarParticipacion`; los botones de editar y eliminar quedaban mal alineados en la fila de la actividad y se movieron a una fila propia dentro de la información de la actividad. Para ejecutar la aplicación se usó `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` desde WSL, ya que `.\mvnw.cmd` no existe en ese entorno.
