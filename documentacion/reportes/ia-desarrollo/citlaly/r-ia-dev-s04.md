# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 04 (Citlaly Morales Viveros - HU-32: barra de progreso del presupuesto)

---

# 1. Breve descripción de uso

Crear la barra de progreso del presupuesto en `zona-presupuesto`, con colores según el porcentaje usado (RF-24), aviso cuando el presupuesto se excede y actualización del resumen del encabezado. Consume `GET /api/viajes/{id}/presupuesto`.

---

# 2. Evidencias de uso

| Concepto                    | Descripción                                                        |
| :-------------------------- | :----------------------------------------------------------------- |
| Herramienta                 | Claude (Anthropic), claude.ai                                      |
| Modelo/temperatura          | Haiku 5.5 medio / "no configurable"                                |
| Prompt original             | [Ver prompt original](#prompt-original)                            |
| Respuesta de IA             | [Ver resumen de respuesta](#respuesta-de-ia)                       |
| Código anterior             | Commit `ae5826e` (Registrar gasto en vivo (Fetch))                 |
| Código posterior            | Commit `44c2fb5` (Barra de progreso del presupuesto (Fetch + CSS)) |
| Diferencias entre versiones | [Ver diferencias](#diferencias-entre-versiones)                    |
| Prueba de funcionamiento    | [Ver prueba](#prueba)                                              |
| Código de uso               | Generación                                                         |
| Reflexión del estudiante    | [Ver reflexión](#reflexion-del-estudiante)                         |

---

### Prompt original

> "Hola necesito que me ayudes a implementar esto que se vea básico, hecho por estudiantes Recuerda no mencionar que historia de usuario es, en este caso los archivos deben de llevar estrictamente lo de "Autora: Citlaly Morales Viveros" Para este caso en específico no quiero que sobrepienses tanto no hagas pruebas, entrega el primer resultado y explica su funcionamiento uno por uno, cosas que modificaste, con que se ccomunica, etc, el arbol compartido es uno que esta como ejemplo, no teneos todos esos archivos, te paso un comando de git donde sí muestra los archivos existentes, tu dime cual necesitas, antes mira el zip para que comprendas que nos vemos limitados a implementar solo funciones que se utilizaron en ese proyecto Por el momento busco hacer la hu 31 primero, que archivos necesitas?"

Es el mensaje con el que empezó la sesión (primero HU-31 y después HU-32 y HU-39). El mensaje específico con el que se pidió HU-32 no está en la conversación pegada; si lo tienes, pégalo aquí.

Se adjuntaron el plan `plan-hu31-hu32-hu39.md` y el zip de ejemplo `ejemplo_de_manera_de_trabajo.zip`.

### Respuesta de IA

Claude entregó la implementación de HU-32 sin ejecutar pruebas, como se le pidió en la sesión. El archivo nuevo lleva el encabezado "Autora: Citlaly Morales Viveros" y quedó en `frontend/js/presupuesto.js`.

El archivo tiene estas partes:

- Consulta `GET /api/viajes/{id}/presupuesto` al cargar la página, al emitirse `gasto-registrado` y al emitirse `viaje:actualizado`.
- Pinta la barra `.barra-presupuesto` con la variable `--uso` (el porcentaje, nunca mayor a 100%) y el estado de color: `estado-ok` por debajo de 75%, `estado-alerta` de 75% a 100%, y `estado-excedido` arriba de 100%.
- Muestra "Gastado: X de Y", el porcentaje, los participantes y el gasto por persona.
- Si el presupuesto se excede, muestra "Presupuesto excedido por $Z".
- Actualiza `datoGastado`, `datoRestante` y `etiquetaRestante` del encabezado. Muestra "Excedido por" cuando el restante es negativo.
- Descarta respuestas viejas si llegan dos consultas seguidas.
- Los errores 401 usan `manejarErrorApi`; los demás se muestran dentro de la tarjeta.
- Se agregó el `<script>` de `presupuesto.js` en `detalle.html`.

El CSS (`presupuesto.css`) ya existía en el proyecto y no se modificó.

### Diferencias entre versiones

`git diff --numstat ae5826e 44c2fb5`:

```text
1	0	backend/src/main/resources/templates/viajes/detalle.html
155	1	frontend/js/presupuesto.js
```

### Prueba

La prueba en el navegador está pendiente en WSL. Lo verificado hasta ahora:

- `node --check frontend/js/presupuesto.js` pasa (sintaxis OK).
- Revisión contra los contratos reales: los campos usados (`presupuestoTotal`, `totalGastado`, `restante`, `porcentajeUsado`, `numParticipantes`, `gastoPorPersona`, `monedaBase`) coinciden uno a uno con `PresupuestoResponse`, y los límites de color se verificaron contra RF-24 y el plan.
- `./mvnw compile` en `backend/` pasa con `EXIT=0` (probado el 20-11-2026 sobre el commit `5f1c439`, que incluye este código).
- Claude entregó además una lista de verificación para probar HU-32 en el navegador (barra en 0% verde sin gastos, cambio al registrar, amarilla entre 75% y 100%, roja con aviso al pasar 100%, recálculo al eliminar), que es la que se usa para la prueba.

Sugerencias de prueba en el navegador: sin gastos la barra está en 0% y verde; al pasar de 100% queda roja y aparece el aviso; el encabezado cambia al registrar un gasto.

### Reflexión del estudiante

Aprendí cómo se pinta la barra con la variable `--uso` y que el porcentaje nunca pasa de 100% aunque el gasto se pase del presupuesto, y que los colores cambian en 75% y 100%. También vi que hay que descartar respuestas viejas cuando llegan dos consultas seguidas, porque si no la barra puede mostrar un dato viejo.

---

## Observaciones adicionales

- Los límites de color 75 y 100 se verificaron contra RF-24 y el plan: verde por debajo de 75%, amarilla de 75% a 100%, roja por encima de 100% (barra llena y aviso).
- El código generado lo copió la integrante al repositorio y lo subió en el commit `44c2fb5`; Claude no ejecutó pruebas, como se le pidió en el prompt.
