# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 05 (Citlaly Morales Viveros - HU-39: vista previa y modal de comprobantes)

---

# 1. Breve descripción de uso

Modificar `gastos.js` para agregar vista previa de la imagen antes de guardar el gasto, y reemplazar el enlace "Ver comprobante" (que abría una pestaña nueva) por una ventana modal como la del wireframe 10. La modal muestra la imagen, el concepto, el monto y quién pagó, y si el servidor responde 403 muestra el mensaje de permiso.

---

# 2. Evidencias de uso

| Concepto                    | Descripción                                                                         |
| :-------------------------- | :---------------------------------------------------------------------------------- |
| Herramienta                 | Claude (Anthropic), claude.ai                                                       |
| Modelo/temperatura          | Haiku 5.5 medio / "no configurable"                                                 |
| Prompt original             | [Ver prompt original](#prompt-original)                                             |
| Respuesta de IA             | [Ver resumen de respuesta](#respuesta-de-ia)                                        |
| Código anterior             | Commit `f4c4197` (Conversor automático de divisas para gastos extranjeros, de Dana) |
| Código posterior            | Commit `3c01b02` (Comprobantes de gasto: adjuntar y ver imagen (frontend))          |
| Diferencias entre versiones | [Ver diferencias](#diferencias-entre-versiones)                                     |
| Prueba de funcionamiento    | [Ver prueba](#prueba)                                                               |
| Código de uso               | Modificación                                                                        |
| Reflexión del estudiante    | [Ver reflexión](#reflexion-del-estudiante)                                          |

---

### Prompt original

> "Continuemos con HU-39"

### Respuesta de IA

Claude entregó la modificación de `gastos.js` sin ejecutar pruebas, como se le pidió en la sesión. El archivo ya llevaba el encabezado "Autora: Citlaly Morales Viveros" de HU-31.

Cambios, uno por uno:

- Vista previa: una imagen debajo del campo de archivo (`#vistaPreviaComprobante`, máximo 160 px) que muestra el comprobante elegido, con `URL.createObjectURL`. Al cambiar el archivo o quitarlo, libera la URL anterior con `URL.revokeObjectURL`.
- Validación del archivo: solo jpg, png o webp y máximo 2 MB. Si pesa más, muestra "El comprobante no puede pesar más de 2 MB."; si el tipo no es imagen, "Elige una imagen jpg, png o webp de máximo 2 MB.". El botón "Quitar" desaparece el archivo y la vista previa.
- Modal: un elemento `<dialog id="dialogoComprobante">` con título ("Comprobante: {concepto}"), botón de cerrar (X y "Cerrar"; la tecla Esc cierra sola porque es un `<dialog>`), la imagen y las filas Concepto, Monto y Quién pagó.
- Carga del comprobante con `fetch` y la cookie de sesión (no con `<img src>` directo), con `Accept: image/*`. Así un 403 se detecta y se muestra "No tienes permiso para ver este comprobante.". Si el servidor responde 401, se cierra la modal y se usa `manejarErrorApi`. Si no se puede cargar por otro motivo, se muestra "No se pudo cargar el comprobante."; sin conexión, "Sin conexión con el servidor. No se pudo cargar el comprobante.".
- Al cerrar la modal, se libera la URL de la imagen para no dejarla en memoria.
- La lista ya no tiene un enlace a pestaña nueva; el botón "Ver comprobante" abre la modal.

### Diferencias entre versiones

`git diff --numstat f4c4197 3c01b02` (solo `gastos.js`, que es el archivo de HU-39; el commit completo incluye además archivos de balance, pruebas y la limpieza de imports sin uso en `Usuario.java` y `Voto.java`, que no son de esta historia):

```text
120	9	frontend/js/gastos.js
```

### Prueba

La integrante probó HU-39 en el navegador con la lista de verificación que Claude entregó en la sesión, y la historia quedó implementada correctamente el 20-11-2026. Los puntos de la lista:

- Al elegir un .png o .jpg aparece el nombre del archivo y la vista previa de la imagen; con Quitar desaparecen el archivo y la vista previa.
- Al elegir un .pdf o un archivo de más de 2 MB aparece el error y el archivo no se queda seleccionado.
- Al guardar un gasto con comprobante aparece en la lista con el botón "Ver comprobante".
- Al hacer clic se abre la ventana con el título, la imagen, y abajo Concepto, Monto y Quién pagó, como en el wireframe 10.
- Se cierra con la X, con Cerrar y con la tecla Esc.
- En la pestaña Red, `GET /api/gastos/{id}/comprobante` responde 200 y el tipo de contenido es de imagen.

Verificado en el código el 20-11-2026:

- `GastoController.verComprobante` responde el comprobante con un tipo de imagen (`contentType` según el archivo guardado), así que la modal muestra la imagen.
- La clase `mensaje-error` existe en `frontend/js/mensajes.js` y en `frontend/css/componentes.css`, y la modal la usa para el mensaje de permiso, así que el texto aparece con estilo.

Pendiente de confirmar: el caso de permiso denegado (un usuario que no participa en el viaje debe ver "No tienes permiso para ver este comprobante." y no la imagen). Si no se probó, hay que probarlo con un segundo usuario antes de entregar.

### Reflexión del estudiante

Aprendí que la vista previa se hace con `URL.createObjectURL` y que hay que liberar la URL cuando ya no se usa, para no dejarla en memoria. También entendí por qué la modal carga el comprobante con `fetch` y la cookie en vez de ponerlo directo en un `<img>`: así se puede detectar el 403 y mostrar el mensaje de permiso dentro de la ventana. Esta sesión modificó código de una sesión anterior (el `gastos.js` de HU-31), así que tuve que cuidar no romper lo que ya funcionaba.

---

## Observaciones adicionales

- Esta sesión modificó código generado en una sesión anterior (`r-ia-dev-s03.md`), y el archivo que se modificó ya incluía el cambio de divisas de Dana (`f4c4197`).
- La tecla Esc cierra la modal sola porque es un elemento `<dialog>` del navegador, no hace falta programarla.
- El código modificado lo copió la integrante al repositorio y lo subió en el commit `3c01b02`; Claude no ejecutó pruebas, como se le pidió en el prompt.
