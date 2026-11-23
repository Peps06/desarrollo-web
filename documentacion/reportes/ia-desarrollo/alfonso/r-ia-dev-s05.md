# Reporte de IA Desarrollo en el Proyecto

**Fecha:** 20-11-2026
**Número de reporte:** 05 (Alfonso Mendoza Vásquez - Votación en vivo con Fetch)

---

# 1. Breve descripción de uso

Implementación de `frontend/js/votos.js`, que permite votar y quitar el voto de una actividad con `fetch()` sin recargar la página: `POST /api/actividades/{id}/votos` para votar y `DELETE` a la misma ruta para quitarlo (según el estado `votadaPorMi` del botón). Con la respuesta `{actividadId, totalVotos, votadaPorMi}` se actualizan el contador y la clase `votado` del botón. Mientras la petición está en curso el botón se deshabilita y se vuelve a habilitar al terminar, tanto en éxito como en error. Los errores 400, 403, 404 y de red se muestran con las funciones de `mensajes.js` (HU-36) y el contador no cambia. Tras un voto exitoso se lanza el evento `viaje:recargar` para que se vuelva a pedir el detalle del viaje y se refresque la insignia de actividad más votada.

Antes de escribir código se hizo un diagnóstico del estado actual del proyecto (backend de votos, tarjetas de actividad, mensajes, CSRF). Se encontró que las tarjetas de HU-29 aún no tienen botón de voto y que `detalle.html` no carga `votos.js`, por lo que esos cambios quedaron pendientes para el dueño de esos archivos (Diego) y no se modificaron.

---

# 2. Evidencias de uso

| Evidencia | Formato |
| :--- | :--- |
| **Herramienta** | Claude Code (Anthropic), extensión de VS Code |
| **Modelo/temperatura** | Claude Sonnet 5.5 y Claude Opus 5.5 (Anthropic) |
| **Prompt original** | [Ver prompt original](#prompt-original) |
| **Respuesta de IA** | [Ver resumen de respuesta](#respuesta-de-ia) |
| **Código anterior** | Commit `f4c4197dfdac9078351e8c3636b06a2df8ad9e0d` (Conversor automático de divisas para gastos extranjeros) |
| **Código posterior** | Commit `152ae73e5d7f95c57b02944bb0d500a3dbd950de` (Votación en vivo con fetch) |
| **Diferencias entre versiones** | [Ver diferencias](#diferencias-entre-versiones) |
| **Prueba de funcionamiento** | [Ver salida de pruebas](#pruebas) |
| **Código de uso** | Generación |
| **Reflexión del estudiante** | Aprendí que las tarjetas se generan con JavaScript y no con Thymeleaf, y todavía no tenían botón de voto, así que mi archivo solo funciona completamente cuando Diego lo agregue. Entendí la delegación de eventos: como las tarjetas se borran y se vuelven a crear en cada recarga, un solo `addeventlistener` en `document` con `closest(".btn-voto")` atiende también los botones nuevos, sin volver a conectar cada uno. Reforcé el uso de `try/catch/finally` para que el botón se rehabilite siempre, aunque falle la red, y a reutilizar `manejarErrorApi` y `mostrarErrorRed` de mis compañeras en lugar de escribir mis propios mensajes. También que no hace falta token CSRF en la api porque `SecurityConfig` lo desactiva para esas rutas y la sesión viaja en la cookie. Para terminar vi que si solo cambiara el DOM, la variable `viaje` de `viaje-detalle.js` quedaría con votos viejos, por eso conviene pedir un evento de recarga en vez de modificar su estado desde mi archivo. |

---

### Prompt original
> Soy Alfonso. Ahora HU-30: Votación en vivo (Fetch). El archivo static/js/votos.js existe pero solo tiene una línea de cabecera. Reemplaza esa cabecera por un comentario breve que describa el archivo, según las reglas de arriba.
>
> Contexto: el backend de votos (HU-14) ya está hecho y las tarjetas de actividad son de HU-29 (código de Diego). El equipo refactorizó cosas, así que NO asumas nada.
>
> PASO 0 - Diagnóstico (antes de escribir código): Revisa el estado actual de VotoController, templates/viajes/detalle.html, static/js/viaje-detalle.js, static/js/mensajes.js, templates/fragments/base.html y static/css. Dime: a) rutas, métodos y JSON real de los endpoints de voto; b) cómo es hoy el botón de voto en las tarjetas y si las tarjetas vienen de Thymeleaf o de JavaScript; c) nombre y firma de la función de mensajes de HU-36; d) cómo se vuelve a pedir el detalle del viaje y cómo se pinta la insignia de actividadMasVotada; e) si hace falta token CSRF. Si falta algo de HU-14, HU-29 o HU-36, dímelo en lugar de inventarlo o parcharlo.
>
> Requisitos en votos.js:
> - Cada actividad tiene un botón de voto que usa fetch(): POST /api/actividades/{id}/votos para votar y DELETE a la misma ruta para quitar el voto (toggle según votadaPorMi).
> - El contador y el estado visual del botón (clase CSS votado / no votado) se actualizan con la respuesta {actividadId, totalVotos, votadaPorMi}, sin recargar la página.
> - Mientras la petición está en curso, el botón se deshabilita y se vuelve a habilitar al terminar, tanto en éxito como en error.
> - Si la API responde 400, 403 o 404, se muestra el mensaje con la función de HU-36 y el contador NO cambia. Lo mismo con error de red.
> - Después de un voto exitoso, se vuelve a pedir el detalle del viaje para refrescar la insignia de actividad más votada del día.
> - Conecta los botones de la forma más simple que funcione con cómo Diego genera las tarjetas; si se crean con JavaScript, explícame qué opción elegiste y por qué.
> - JavaScript plano, sin librerías.
>
> Fuera de alcance: WebSockets, SSE, polling o actualización automática entre pestañas; cambios de estilos (HU-35), de mensajes (HU-36), del backend (HU-14) o de las tarjetas (HU-29).
>
> Límites: modifica SOLO votos.js. Si necesitas cambiar HTML, CSS o backend de otra persona, dime exactamente qué cambio y a quién pedírselo. No hagas commits, ramas ni push.
>
> Pruebas manuales: 1. votar y ver el contador subir sin recargar; 2. quitar el voto y ver el contador bajar; 3. votar con un usuario no participante; 4. dos pestañas con usuarios distintos votando la misma actividad.

Instrucciones generales dadas antes en la misma conversación:
> Sin líneas que indiquen la HU, sin encabezados ni banners decorativos, sin comentarios que suenen a explicación de una IA; comentarios solo en formato Javadoc/JSDoc para funciones con cierta complejidad o una frase corta para lo sencillo.
> No solo generes codigo para concluir la asignación, dentro de las explicaciones de lo que me asistas, incluye pistas , pasos, ayuda y consejos para que pueda llegar por mi cuenta a la solución y codificacion de las HU.


### Respuesta de IA
Primero se hizo el diagnóstico. Resultados principales:
- Endpoints: `POST` responde 201 y `DELETE` responde 200, ambos con `{actividadId, totalVotos, votadaPorMi}`. Los errores traen `{codigo, mensaje}` con 400 (voto duplicado), 403 (no participa) y 404 (actividad inexistente o quitar un voto que no existe).
- Las tarjetas se generan con JavaScript en `crearFilaActividad` de `viaje-detalle.js`; hoy solo muestran un `<span>` con el texto "N votos", sin botón. El CSS (`.btn-voto`, `.btn-voto.votado`, `.btn-voto__contador`) ya existía. `detalle.html` no carga `votos.js`.
- `mensajes.js` ofrece `mostrarMensaje(tipo, texto)`, `manejarErrorApi(respuesta)` y `mostrarErrorRed()`.
- `recargarDetalle()` está dentro del `DOMContentLoaded` de `viaje-detalle.js`, así que no se puede llamar desde otro archivo. La insignia "Más votada" se pinta comparando con `dia.actividadMasVotada.id`.
- No se necesita CSRF: `SecurityConfig` lo desactiva para `/api/**`.

Después se escribió `votos.js` con tres piezas: `actualizarBotonVoto` (pinta contador, clase `votado` y `aria-pressed`), `cambiarVoto` (elige POST o DELETE según la clase `votado`, deshabilita el botón, hace el `fetch`, y en el `finally` lo rehabilita) y un único listener de clic en `document` (delegación de eventos). Se eligió la delegación porque las tarjetas se recrean en cada recarga. Tras un voto exitoso se lanza `viaje:recargar`.

Se entregó además la lista de cambios que debe hacer Diego en archivos que no son míos: agregar `<script th:src="@{/js/votos.js}">` en `detalle.html`, reemplazar el `<span>` de votos por un botón `.btn.btn-voto` con su `.btn-voto__contador` en `crearFilaActividad`, y escuchar el evento con `document.addEventListener("viaje:recargar", recargarDetalle)`. Se sugirió a Stephanie (opcional) que `manejarErrorApi` muestre el mensaje del servidor en 403 y 404.

### Diferencias entre versiones
```text
65	1	frontend/js/votos.js
```

---

### Pruebas
Pruebas manuales en el navegador contra el servidor local (`./mvnw spring-boot:run`, `http://localhost:8080`) con los datos de prueba (`ana@example.com` y `luis@example.com`, contraseña `Password123`), usando una ventana normal y una de incógnito para tener dos sesiones.

Estado al momento de este reporte: el botón de voto todavía no existe en las tarjetas (pendiente de Diego), por lo que solo se pudo comprobar lo que no depende de ese botón. Completar la tabla con lo observado:

| # | Prueba | Resultado esperado | Resultado obtenido |
| :--- | :--- | :--- | :--- |
| 1 | Votar y ver subir el contador sin recargar | POST 201, contador +1, botón con clase `votado`, sin recarga | PENDIENTE |
| 2 | Quitar el voto | DELETE 200, contador -1, se quita `votado` | PENDIENTE |
| 3 | Votar con un usuario que ya no participa | Mensaje "No tienes permiso", contador sin cambio, botón habilitado | PENDIENTE |
| 4 | Dos pestañas con usuarios distintos votando la misma actividad | Cada voto suma en el servidor; la otra pestaña lo ve al recargar o al votar | PENDIENTE |

Comprobación del código: `node --check frontend/js/votos.js` sin errores de sintaxis.

```text
$ node --check frontend/js/votos.js
OK
```

Salida esperada en la pestaña Network de DevTools (Fetch/XHR) para el paso 1 y 2, a pegar aquí cuando se pruebe con el botón de Diego:

```text
POST   /api/actividades/{id}/votos   201   {"actividadId":..,"totalVotos":..,"votadaPorMi":true}
GET    /api/viajes/{id}              200
DELETE /api/actividades/{id}/votos   200   {"actividadId":..,"totalVotos":..,"votadaPorMi":false}
```
