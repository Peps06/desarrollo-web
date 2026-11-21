# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 03 (Citlaly Morales Viveros - HU-31 frontend: formulario, lista y fetch de gastos)

---

# 1. Breve descripción de uso

Crear la interfaz de gastos del detalle del viaje: formulario para registrar un gasto, lista de gastos con edición y eliminación, y comunicación con la API mediante `fetch()` sin recargar la página. Cada cambio emite el evento `gasto-registrado` para que la barra de presupuesto se actualice.

---

# 2. Evidencias de uso

| Concepto                    | Descripción                                                                 |
| :-------------------------- | :-------------------------------------------------------------------------- |
| Herramienta                 | Claude (Anthropic), claude.ai                                               |
| Modelo/temperatura          | Haiku 5.5 medio / "no configurable"                                         |
| Prompt original             | [Ver prompt original](#prompt-original)                                     |
| Respuesta de IA             | [Ver resumen de respuesta](#respuesta-de-ia)                                |
| Código anterior             | Commit `14edfac` (Implementar endpoints y checklist colaborativa de tareas) |
| Código posterior            | Commit `ae5826e` (Registrar gasto en vivo (Fetch))                          |
| Diferencias entre versiones | [Ver diferencias](#diferencias-entre-versiones)                             |
| Prueba de funcionamiento    | [Ver prueba](#prueba)                                                       |
| Código de uso               | Generación                                                                  |
| Reflexión del estudiante    | [Ver reflexión](#reflexion-del-estudiante)                                  |

---

### Prompt original

> "Hola necesito que me ayudes a implementar esto que se vea básico, hecho por estudiantes Recuerda no mencionar que historia de usuario es, en este caso los archivos deben de llevar estrictamente lo de "Autora: Citlaly Morales Viveros" Para este caso en específico no quiero que sobrepienses tanto no hagas pruebas, entrega el primer resultado y explica su funcionamiento uno por uno, cosas que modificaste, con que se ccomunica, etc, el arbol compartido es uno que esta como ejemplo, no teneos todos esos archivos, te paso un comando de git donde sí muestra los archivos existentes, tu dime cual necesitas, antes mira el zip para que comprendas que nos vemos limitados a implementar solo funciones que se utilizaron en ese proyecto Por el momento busco hacer la hu 31 primero, que archivos necesitas?"

Mensaje de seguimiento, con los archivos y la decisión de carpetas:

> "despues de terminar el hu-31, lee solo los archivos necesarios por el momento, te paso los archivos y la decisión: solo trabajamos en backend/.../static/, no actualices la copia espejo."

Se adjuntaron el plan `plan-hu31-hu32-hu39.md` y el zip de ejemplo `ejemplo_de_manera_de_trabajo.zip`, y después los archivos del módulo de gastos (`archivos-gastos.zip`).

### Respuesta de IA

Claude entregó la implementación de HU-31 sin ejecutar pruebas, como se le pidió. El archivo nuevo lleva el encabezado "Autora: Citlaly Morales Viveros" y se entregó para `static/js/gastos.js`; la versión que quedó en el repositorio es `frontend/js/gastos.js`, la carpeta fuente que el `pom.xml` copia a `static/` al compilar.

El archivo tiene estas partes:

- Plantilla que pinta en `zona-gastos` el formulario (concepto, categoría, monto, comprobante opcional), la lista y el mensaje de lista vacía.
- Validación antes de enviar: concepto obligatorio y máximo 150 caracteres, categoría obligatoria y monto mayor a cero. Los errores se muestran junto al campo.
- Alta con `POST /api/gastos` en JSON (o con `FormData` si hay comprobante, sin fijar `Content-Type` para que el navegador agregue el boundary). Al responder 201 agrega el gasto al inicio de la lista, limpia el formulario y emite `gasto-registrado`.
- Edición con `PUT /api/gastos/{id}`, que llena el formulario y oculta el campo de comprobante (el backend no lo acepta al editar).
- Eliminación con `DELETE /api/gastos/{id}`, con confirmación previa (`window.confirm()`).
- Permisos en pantalla: Editar y Eliminar aparecen solo para el autor del gasto o el organizador, según `viaje:actualizado` y `/api/auth/sesion`.
- Todo el texto de la lista se escribe con `textContent`, no con HTML.
- El evento de eliminar no lleva total: el DELETE responde 204 sin cuerpo, así que se emite `{gastoEliminadoId}` y HU-32 debe volver a pedir el presupuesto en vez de usar el total del evento.
- Solo MXN: no se agregó selector de moneda extranjera; el plan lo deja para una segunda vuelta.

Cómo se comunica:

- Con el backend por `fetch()` a `/api/viajes/{id}/gastos`, `/api/gastos`, `/api/gastos/{id}` y `/api/auth/sesion`.
- Con la página del viaje escucha `viaje:actualizado`, que `viaje-detalle.js` emite al pintar. De ahí toma el rol del usuario y la moneda base.
- Con otros scripts usa las funciones globales de `mensajes.js` (`mostrarMensaje`, `manejarErrorApi`, `mostrarErrorRed`) y emite `gasto-registrado` para HU-32.

### Diferencias entre versiones

`git diff --numstat 14edfac ae5826e` (solo los archivos del frontend de HU-31; el resto del commit se reporta en `r-ia-dev-s02.md`):

```text
13	12	backend/src/main/resources/templates/viajes/detalle.html
484	1	frontend/js/gastos.js
```

### Prueba

La prueba en el navegador está pendiente en WSL. Lo verificado hasta ahora:

- `node --check frontend/js/gastos.js` pasa (sintaxis OK).
- Revisión contra los contratos reales: el POST, PUT y DELETE usan las rutas y respuestas correctas (`{gasto, totalGastado}`), el evento `gasto-registrado` se emite al crear, editar y eliminar, y los botones respetan autor u organizador.
- `./mvnw compile` en `backend/` pasa con `EXIT=0` (probado el 20-11-2026 sobre el commit `5f1c439`, que incluye este código).
- Claude entregó además una lista de verificación para probar HU-31, HU-32 y HU-39 en el navegador (carga inicial, crear, validaciones, editar, eliminar y permisos), que es la que se usa para la prueba.

Sugerencias de prueba en el navegador: registrar un gasto y ver que aparece sin recargar; validar el concepto vacío; editar y eliminar un gasto propio; entrar como Luis y comprobar que no ve Editar ni Eliminar en un gasto de Ana.

### Reflexión del estudiante

Aprendí a armar el formulario y la lista con `fetch()` sin recargar la página, y que el evento `gasto-registrado` es lo que conecta los gastos con la barra del presupuesto. Lo que más me costó fue que los botones de editar y eliminar solo se ven si eres la autora o la organizadora, porque eso depende de la sesión y del rol que trae el detalle del viaje.

---

## Observaciones adicionales

- El archivo que cuenta es el de `frontend/js/`, porque el `pom.xml` copia esa carpeta a `static/` al compilar.
- El código generado lo copió la integrante al repositorio y lo subió en el commit `ae5826e`; Claude no ejecutó pruebas, como se le pidió en el prompt.
- Después de este reporte, Dana modificó este mismo archivo para agregar la conversión automática de divisas (commit `f4c4197`, 20-11-2026: 103 líneas agregadas y 6 eliminadas en `gastos.js`, más el archivo nuevo `frontend/js/divisas.js`). El texto "Equivale a ..." de un gasto en otra moneda viene de ese cambio, no de HU-31.
