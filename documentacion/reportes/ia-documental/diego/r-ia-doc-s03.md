# Reporte de Uso Documental en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 03 (Diego Rafael Jiménez Trujano)

---

# 1. Breve descripción de uso

Consultas sobre las decisiones técnicas del detalle del viaje y la exportación a PDF: orden de los códigos de error con viajes cancelados, reutilización del servicio del detalle en el PDF, nombre del archivo descargado, comunicación con los módulos de los demás integrantes, ubicación de los archivos estáticos y credenciales de prueba.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 20-11-2026 | Claude | [Consulta 1: Orden de respuestas de error ante un viaje cancelado](#consulta-1-orden-de-respuestas-de-error-ante-un-viaje-cancelado) | [Respuesta 1](#respuesta-1) |
| 20-11-2026 | Claude | [Consulta 2: Reutilización de la lógica del detalle en la exportación a PDF](#consulta-2-reutilización-de-la-lógica-del-detalle-en-la-exportación-a-pdf) | [Respuesta 2](#respuesta-2) |
| 20-11-2026 | Claude | [Consulta 3: Normalización del nombre del archivo PDF](#consulta-3-normalización-del-nombre-del-archivo-pdf) | [Respuesta 3](#respuesta-3) |
| 20-11-2026 | Claude | [Consulta 4: Comunicación entre el detalle del viaje y los módulos de los demás integrantes](#consulta-4-comunicación-entre-el-detalle-del-viaje-y-los-módulos-de-los-demás-integrantes) | [Respuesta 4](#respuesta-4) |
| 20-11-2026 | Claude | [Consulta 5: Ubicación de los archivos estáticos tras la conexión de backend y frontend](#consulta-5-ubicación-de-los-archivos-estáticos-tras-la-conexión-de-backend-y-frontend) | [Respuesta 5](#respuesta-5) |
| 20-11-2026 | Claude | [Consulta 6: Credenciales de prueba](#consulta-6-credenciales-de-prueba) | [Respuesta 6](#respuesta-6) |

---



---

### Consulta 1: Orden de respuestas de error ante un viaje cancelado
> "En el detalle del viaje y en la exportación a PDF, ¿qué código HTTP debe recibir un usuario que no participa en un viaje cancelado, y cómo se mantiene el orden 404, 403 y 400?"

#### Respuesta 1
Debe recibir 404: un viaje cancelado se trata como inexistente para cualquier usuario, de modo que la comprobación de existencia va antes que la de participación. En `ViajeDetalleService.buscarParticipacion`, cuando el usuario no participa, antes solo se verificaba con `existsById` si el viaje existía y se respondía 403 aunque estuviera cancelado. Se reemplazó por una consulta que también revisa el estado (`EstadoViaje.CANCELADO`), y así el 404 se evalúa antes que el 403. Como el detalle y el PDF comparten este método, ambos quedan con el mismo comportamiento.

---

### Consulta 2: Reutilización de la lógica del detalle en la exportación a PDF
> "¿Conviene que el servicio del PDF consulte por su cuenta las actividades del viaje o que reutilice el servicio que ya arma el detalle?"

#### Respuesta 2
Conviene reutilizar `ViajeDetalleService.obtenerDetalle`, que es lo que hace `PdfService.generarItinerario`. De esa forma el documento muestra exactamente la misma información que la pantalla (días, actividades, votos y presupuesto) y hereda las mismas reglas de permiso y los mismos errores (404 y 403), sin duplicar consultas ni validaciones. El servicio solo se encarga de dar formato al contenido con OpenPDF y de devolver el nombre del archivo junto con los bytes.

---

### Consulta 3: Normalización del nombre del archivo PDF
> "¿Cómo debe construirse el nombre del archivo descargado para que no cause problemas con destinos que tienen acentos, espacios o comas, como «Oaxaca, Oaxaca»?"

#### Respuesta 3
Se construye el nombre `itinerario-<destino>.pdf` normalizando el destino con `Normalizer` en forma NFD para quitar los acentos, sustituyendo los caracteres que no son letras, números, guion o guion bajo por un guion y limitando el largo del destino a 60 caracteres. Con ello «Oaxaca, Oaxaca» produce `itinerario-Oaxaca-Oaxaca.pdf`. La cabecera de respuesta se arma con `ContentDisposition.attachment().filename(...)` de Spring para que el navegador descargue el archivo en lugar de abrirlo.

---

### Consulta 4: Comunicación entre el detalle del viaje y los módulos de los demás integrantes
> "La página del detalle debe alojar los módulos de votos, presupuesto, gastos, balance y checklist que desarrollan otros integrantes. ¿Cómo evitar modificar su código y aun así mantenerlos actualizados cuando cambia una actividad?"

#### Respuesta 4
Se dejaron en `detalle.html` contenedores con identificadores fijos (`zona-votos`, `zona-presupuesto`, `zona-gastos`, `zona-balance`, `zona-checklist` y `zona-exportar`) y el atributo `data-viaje-id` en el contenedor principal, de donde cada módulo toma el identificador del viaje. Cuando el script del detalle recarga la información después de proponer, editar o eliminar una actividad, emite el evento `viaje:actualizado` en `document`, al que los demás módulos pueden suscribirse para refrescarse sin que el detalle dependa de su código.

---

### Consulta 5: Ubicación de los archivos estáticos tras la conexión de backend y frontend
> "Mi script `viaje-detalle.js` está en `frontend/js/`, pero Spring Boot no lo sirve. ¿Dónde debe estar para que la página lo cargue?"

#### Respuesta 5
Spring Boot solo sirve los recursos que están en `static/` dentro del classpath, por lo que en el commit del detalle el script se guardó en `backend/src/main/resources/static/js/` además de `frontend/js/`. Después, el commit `dfe85d5` (conexión backend y frontend) eliminó esa carpeta y agregó en `pom.xml` un recurso que copia `frontend/` hacia `static` al compilar, por lo que ahora la única copia que se debe editar es la de `frontend/js/`.

---

### Consulta 6: Credenciales de prueba
> "cuales son las credenciales que tenemos?"

#### Respuesta 6
Se revisaron los datos que carga `DataLoader` al iniciar la aplicación. Existen dos usuarios de prueba, ambos con la contraseña `Password123`: `ana@example.com`, que es organizadora del viaje 1 (Oaxaca, del 2026-12-20 al 2026-12-27), y `luis@example.com`, que es participante de ese viaje. Con esos dos usuarios se pueden probar los permisos: solo el autor de una actividad o la organizadora pueden editarla o eliminarla.

---

## Observaciones adicionales

Estas consultas sirvieron para fundamentar las decisiones técnicas del entregable y dejar claro el entorno de pruebas. La implementación completa quedó documentada en el reporte de IA de desarrollo 03.
