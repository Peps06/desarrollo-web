# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 22-11-2026
**Número de reporte:** 04 (Diego Rafael Jiménez Trujano - Votación en vivo conectada al detalle del viaje)

---

# 1. Breve descripción de uso

Apoyo en la integración frontend de la **votación en vivo (HU-30)** en la pantalla del detalle del viaje (`/viajes/{id}`). El archivo `votos.js` ya existía y estaba completo, pero no estaba conectado a la página. Se hicieron tres cambios en archivos propios: cargar `votos.js` en `detalle.html`, reemplazar el texto «N votos» por un botón con contador en `viaje-detalle.js` y escuchar el evento `viaje:recargar` para que la insignia «Más votada» se actualice. En una segunda iteración se agregó un ícono fijo al botón y se confirmó el criterio de accesibilidad de su etiqueta. Se reutilizaron `votos.js`, `VotoResponse` y `ActividadResponse` (que ya devuelve `totalVotos` y `votadaPorMi`), `recargarDetalle`, las clases `.btn-voto` de `componentes.css` y las funciones de `mensajes.js`. No se modificó el backend.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude (Anthropic), con acceso al repositorio desde la aplicación de escritorio, y un segundo chat del proyecto que aplicó los cambios |
| **Modelo/temperatura** | Claude Sonnet 5.5 |
| **Prompt original** | [Ver prompts originales](#prompt-original) (dos iteraciones) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `3a3593e` (participantes) |
| **Código posterior** | Commit `69f4e4d` (Votación en vivo conectada al detalle del viaje) |
| **Diferencias entre versiones** | Ver salida de `git diff --numstat 3a3593e 69f4e4d` en [Diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver pruebas](#pruebas): revisión del código hecha por la herramienta y pruebas en el navegador con la pestaña Network y consultas a MySQL, ejecutadas por mí |
| **Código de uso** | Generación / Depuración / Explicación |
| **Reflexión del estudiante** | Entendí que `votos.js` ya hacía el trabajo del `fetch`, pero nunca estaba conectado: emitía el evento `viaje:recargar` y en el detalle no había ningún listener que lo escuchara, así que la insignia «Más votada» no podía actualizarse. También entendí que `votos.js` atiende los clics con un solo listener en `document`, por lo que el botón se puede volver a crear cada vez que se repinta el detalle sin perder funcionamiento, siempre que conserve las clases `.btn-voto` y `.btn-voto__contador` y esté dentro de `.actividad[data-actividad-id]`. El ícono se dejó fijo porque `votos.js` solo actualiza el contador y el estado, y el `aria-label` se dejó fijo («Votar por …») porque `aria-pressed` ya comunica si el usuario votó. Además, comprobé que cada voto repinta el detalle completo y emite `viaje:actualizado`, por lo que presupuesto, balance y gastos vuelven a pedir sus datos; lo dejé así por ser lo más simple, aunque genera más peticiones. Por último, aprendí a respetar los archivos de otros integrantes: la línea de `participantes.js` en `detalle.html` se conservó y solo se agregó una línea para `votos.js`. |

---

### Prompt original

**Iteración 1: conectar la votación**
> "Actúa como desarrollador senior full-stack (Spring Boot 3.2.5, Java 17, Thymeleaf, JavaScript con Fetch, MySQL). Estoy trabajando en el repositorio de la 'Bitácora de viajes colaborativa' y soy Diego, responsable del detalle del viaje (frontend). Necesito dejar conectada la **votación en vivo (HU-30)** en la pantalla `/viajes/{id}`. El archivo `frontend/js/votos.js` ya existe y está completo (hace `fetch` POST/DELETE a `/api/actividades/{id}/votos`, actualiza el botón y emite el evento `viaje:recargar`), pero nunca se conectó a la página. Faltan tres cosas, todas en mis archivos:
>
> 1. En `backend/src/main/resources/templates/viajes/detalle.html`, agregar `<script th:src="@{/js/votos.js}"></script>` junto a los demás scripts. NO sobrescribas el archivo: edita solo esa línea. La línea de `participantes.js` (línea 110) debe quedar como está.
> 2. En `frontend/js/viaje-detalle.js`, dentro de `crearFilaActividad` (líneas ~415-418), reemplazar el `span.insignia` que muestra "N votos" por un `<button type="button" class="btn btn-voto">` con un hijo `<span class="btn-voto__contador">` con `actividad.totalVotos`. `votos.js` espera que el botón esté dentro de `.actividad[data-actividad-id]` (ya existe) y usa `.votado` y `aria-pressed` según `actividad.votadaPorMi`. Agrega un `aria-label` claro (por ejemplo, "Votar por <título>") y conserva la función `textoVotos` si todavía se usa en otro lado.
> 3. En `frontend/js/viaje-detalle.js`, dentro del `DOMContentLoaded`, agregar un listener de `viaje:recargar` que llame a `recargarDetalle()` (línea ~859), para que la insignia "Más votada" se actualice después de votar. Revisa si al volver a pintar el detalle se pierde el foco del botón o se hacen peticiones de más (cada repintado emite `viaje:actualizado` y presupuesto, balance y gastos vuelven a pedir sus datos) y dime si conviene hacer algo; no cambies nada fuera de lo pedido sin avisar.
>
> Contexto que debes respetar: Spring sirve `frontend/` como `static` gracias a `pom.xml`, así que la versión vigente de los scripts es la de `frontend/js/`; no crees una copia en `backend/src/main/resources/static`. `mensajes.js` y `auth.js` ya se cargan desde `fragments/base.html`; `votos.js` usa `manejarErrorApi` y `mostrarErrorRed` de esos archivos. Los estilos `.btn-voto`, `.btn-voto.votado` y `.btn-voto__contador` ya existen en `componentes.css`. El backend (`VotoController`, `VotoService`, `ActividadResponse` con `totalVotos` y `votadaPorMi`) no debe modificarse.
>
> Hay además un detalle que quiero que revises y me reportes sin resolverlo por tu cuenta: `detalle.html` carga `/js/participantes.js`, pero ese archivo no existe en `frontend/js` ni en git, así que da 404 en consola. No lo crees ni lo quites; dime cuál es el efecto real en la página y qué opciones hay.
>
> Reglas: el usuario siempre se toma de la sesión. No cambies el diseño: usa las clases de `componentes.css`, `guia-css.md` y el wireframe 05 (votación). Los comentarios deben ser breves, en español y sin mencionar historias de usuario ni mi nombre.
>
> Restricciones: no modifiques archivos de otros integrantes (`votos.js` es de Alfonso; solo lo modificas si hay un error real y avisas antes); no crees CSS nuevos; no crees archivos nuevos; respeta CRLF; no inventes resultados de pruebas; si tienes dudas, pregúntalas todas de una sola vez antes de empezar.
>
> Entregables: los cambios exactos de cada archivo (ruta y fragmento antes/después, no el archivo completo); comandos PowerShell con `Invoke-WebRequest` y `WebSession` contra `http://localhost:8081` con `ana@example.com` y `luis@example.com` (contraseña `Password123`) sobre el viaje 1 de Oaxaca, para verificar votar, votar dos veces (400), quitar voto, quitar sin voto previo (404) y votar sin sesión (401); pasos de prueba en el navegador con dos usuarios votando la misma actividad desde dos ventanas (una normal y una de incógnito): el contador sube y baja, el botón cambia de estado y la insignia "Más votada" cambia al recargar; y un resumen corto. Indica claramente qué probaste tú y qué queda para que yo pruebe en mi equipo."

**Iteración 2: afinar el botón de voto**
> "Actúa como desarrollador senior full-stack (Spring Boot 3.2.5, Java 17, Thymeleaf, JavaScript con Fetch, MySQL). Estoy trabajando en el repositorio de la 'Bitácora de viajes colaborativa' y soy Diego, responsable del detalle del viaje (frontend). Ya conecté la votación en vivo (HU-30) en `/viajes/{id}`: en `frontend/js/viaje-detalle.js`, la función `crearFilaActividad` crea un botón `<button class="btn btn-voto">` con un `<span class="btn-voto__contador">` que solo muestra el número de votos, con `aria-pressed` y `aria-label` = "Votar por <título>". `frontend/js/votos.js` (de Alfonso) atiende el clic y actualiza únicamente el contador, la clase `votado` y `aria-pressed`. Necesito afinar dos detalles de ese botón:
>
> 1. **Texto visible.** Hoy el botón muestra solo un número y no se entiende qué es. Agrega un ícono o texto fijo antes del contador (por ejemplo un "👍" dentro de un `<span aria-hidden="true">`) para que se lea como un botón de voto. El texto o ícono NO debe depender del estado (votado o no), porque `votos.js` no lo actualiza y quedaría desfasado hasta que se recargue el detalle. Revisa en `componentes.css` y en el wireframe 05 (votación) cómo debe verse y dime si el diseño actual aguanta el cambio sin tocar CSS.
> 2. **Etiqueta accesible.** Confirma si el `aria-label` debe quedarse fijo ("Votar por <título>"), dado que `aria-pressed` ya comunica si el usuario votó, y no lo cambies a menos que encuentres una razón real. Explícame brevemente tu criterio.
>
> Reglas: no cambies el comportamiento de `votos.js` ni del listener de `viaje:recargar`. El botón debe seguir siendo hijo de `.actividad[data-actividad-id]` y conservar `.btn-voto__contador` con solo el número, porque `votos.js` busca ese elemento. No uses `innerHTML` con datos del servidor: construye los nodos con `createElement` y `textContent`, como el resto del archivo. Los comentarios deben ser breves, en español y sin mencionar historias de usuario ni mi nombre.
>
> Restricciones: no modifiques archivos de otros integrantes (`votos.js` es de Alfonso; si crees que hay que tocarlo, avísame antes y explica por qué); no crees CSS nuevos ni archivos nuevos; no copies el script a `backend/.../static` (Spring sirve `frontend/` por `pom.xml`); respeta CRLF; no inventes resultados de pruebas; si tienes dudas, pregúntalas todas de una sola vez antes de empezar.
>
> Entregables: el cambio exacto en `viaje-detalle.js` (fragmento antes y después); pasos de prueba en el navegador con dos usuarios (`ana@example.com` y `luis@example.com`, contraseña `Password123`, viaje 1 de Oaxaca, `http://localhost:8081/viajes/1`) para verificar que el ícono se ve, que el contador sube y baja al votar y quitar el voto, que el botón sigue viéndose bien a 390 px de ancho y que el lector de pantalla anuncia el estado; y un resumen corto. Indica claramente qué probaste tú y qué queda para que yo pruebe en mi equipo."

### Respuesta de IA
La herramienta modificó dos archivos propios y no tocó ningún archivo de otro integrante:

- **`viajes/detalle.html`**: agregó `<script th:src="@{/js/votos.js}"></script>` después de la línea de `participantes.js`, que quedó intacta.
- **`static/js/viaje-detalle.js`** (versión vigente en `frontend/js/viaje-detalle.js`):
  - En `crearFilaActividad`, el `span.insignia` con el texto «N votos» se reemplazó por un `<button type="button" class="btn btn-voto">`. El botón recibe la clase `votado` y `aria-pressed` según `actividad.votadaPorMi`, tiene `aria-label` «Votar por <título>» y contiene un ícono 👍 con `aria-hidden="true"` y un `span.btn-voto__contador` con `actividad.totalVotos`. Los nodos se crean con `createElement` y `textContent`.
  - Se eliminó la función `textoVotos`, que dejó de usarse.
  - Al final del `DOMContentLoaded` se agregó un listener de `viaje:recargar` que llama a `recargarDetalle()`, por lo que el detalle se vuelve a pedir y se repinta, y la insignia «Más votada» se actualiza.
- **Criterio de accesibilidad:** el `aria-label` se mantuvo fijo porque `aria-pressed` ya comunica el estado; cambiar el texto además del estado sería redundante.
- **Hallazgo sobre `participantes.js`:** la línea 110 de `detalle.html` apuntaba a un archivo que no existía y daba 404 en consola. Después de esta revisión, el archivo `frontend/js/participantes.js` se incorporó al repositorio en el commit `3a3593e`.
- **Efecto secundario identificado:** cada voto repinta el detalle completo y emite `viaje:actualizado`, por lo que presupuesto, balance y gastos vuelven a pedir sus datos.

Una revisión posterior del diff, hecha con Claude, confirmó que solo cambiaron esos dos archivos, que los saltos de línea CRLF se conservaron y que `textoVotos` no se usaba en ningún otro archivo.

### Diferencias entre versiones
```text
1	0	backend/src/main/resources/templates/viajes/detalle.html
22	9	frontend/js/viaje-detalle.js
```

El commit `69f4e4d` contiene exactamente los cambios revisados; ambas iteraciones (botón con contador e ícono fijo) quedaron en un solo commit.

---

### Pruebas

**Comprobaciones hechas por la herramienta** (no sustituyen las mías): revisión estática del diff con `git diff`; `node --check frontend/js/viaje-detalle.js` sin errores de sintaxis; saltos de línea CRLF conservados en `viaje-detalle.js` (955 de 955 líneas) y en `detalle.html` (114 de 114); `textoVotos` sin otros usos en `frontend/js`; `votos.js` sin modificaciones. No se ejecutó la aplicación.

**Pruebas hechas por mí** con la aplicación corriendo desde WSL en el puerto 8081 y los datos de `DataLoader` (viaje 1 de Oaxaca), con la pestaña Network del navegador filtrada por Fetch/XHR y con Preserve log activado. Las capturas están en `documentacion/evidencias/05-en-vivo/` y `documentacion/evidencias/04-vistas/`:

- **Botón de voto visible:** cada actividad muestra el ícono 👍 con su contador y la actividad más votada conserva la insignia «Más votada» ([detalle-itinerario-votacion-mas-votada-balance](../../../evidencias/04-vistas/detalle-itinerario-votacion-mas-votada-balance.png)).
- **Votar sin recargar:** al votar sale la petición `votos` con estado 201, iniciada por `votos.js:39`, y a continuación se vuelven a pedir el detalle (`viaje-detalle.js:105`), el presupuesto (`presupuesto.js:120`) y el balance (`balance.js:195`), lo que confirma que el evento `viaje:recargar` llega al listener nuevo ([votacion-network-post-201-y-recarga-detalle](../../../evidencias/05-en-vivo/votacion-network-post-201-y-recarga-detalle.png)).
- **La insignia «Más votada» cambia sin recargar:** con varios votos seguidos (estados 201, 200 y 201; el 200 corresponde a quitar un voto) la insignia pasó de «visitar a pepis» a «Cena» sin recargar la página ([votacion-network-cambio-mas-votada-201-200](../../../evidencias/05-en-vivo/votacion-network-cambio-mas-votada-201-200.png)).
- **Respuesta de la API:** la respuesta de la petición `votos` trae `actividadId`, `totalVotos` y `votadaPorMi` ([votacion-network-respuesta-json](../../../evidencias/05-en-vivo/votacion-network-respuesta-json.png) y [votacion-network-status-200](../../../evidencias/05-en-vivo/votacion-network-status-200.png)).
- **Persistencia:** después de las pruebas, la tabla `voto` de MySQL muestra los votos nuevos ([select-usuario-viaje-voto-gasto](../../../evidencias/03-mysql/select-usuario-viaje-voto-gasto.png)).

---

## Observaciones adicionales

La línea de `participantes.js` de `detalle.html` se respetó y `votos.js` (de Alfonso) no se modificó. Cada voto vuelve a pedir el detalle y, con él, el presupuesto, el balance y los gastos; es un costo conocido que se aceptó a cambio de mantener «Más votada» siempre actualizada sin tocar el backend.

Pendiente conocido que no se resolvió en este entregable: `participantes.js` no emite `viaje:recargar` al agregar o quitar un participante, por lo que el balance y los datos del detalle pueden quedar desfasados hasta que se recargue la página. Es un cambio de una línea que corresponde al autor de ese archivo.

El commit sugerido para este entregable fue `HU-30: Votación en vivo conectada al detalle del viaje`.
