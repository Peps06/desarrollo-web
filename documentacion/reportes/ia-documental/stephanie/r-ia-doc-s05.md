
# Reporte de Uso Documental en el Proyecto

**Fecha:** 20-11-2026   
**Número de reporte:** 05 (Stephanie Elizdeth Hernández Prieto)  

---

# 1. Breve descripción de uso

Consultas de verificación funcional en navegador y consola de desarrollador, diagnóstico de autenticación HTTP `401` vs `403` en pruebas con `cURL` y organización del flujo de commits por historia de usuario en Git.

---

# 2. Evidencias de uso

| Fecha | Herramienta | Enlace a prompt | Enlace a respuesta |
| :--- | :--- | :--- | :--- |
| 20-11-2026 | Gemini | [Consulta 1: Cómo verificar la traducción de códigos HTTP y el auto-cierre de mensajes](#consulta-1-cómo-verificar-la-traducción-de-códigos-http-y-el-auto-cierre-de-mensajes) | [Respuesta 1](#respuesta-1) |
| 20-11-2026 | Gemini | [Consulta 2: Respuesta `401` en lugar de `403` al probar permisos de tareas con `cURL`](#consulta-2-respuesta-401-en-lugar-de-403-al-probar-permisos-de-tareas-con-curl) | [Respuesta 2](#respuesta-2) |
| 20-11-2026 | Gemini | [Consulta 3: Separación de archivos en commits individuales por historia de usuario](#consulta-3-separación-de-archivos-en-commits-individuales-por-historia-de-usuario) | [Respuesta 3](#respuesta-3) |

---

### Consulta 1: Cómo verificar la traducción de códigos HTTP y el auto-cierre de mensajes
> "¿Cómo verifico en el navegador el contenedor fijo `#contenedorMensajes`, la desaparición automática a los 4 segundos, el cierre manual con `×` y la traducción de los errores `400`, `401`, `403`, `404` y error de red?"

#### Respuesta 1
Se detalló una guía de pruebas desde la consola de desarrollador (`F12`) invocando `mostrarMensaje("exito"|"error"|"aviso", ...)` para comprobar las clases CSS y el temporizador de 4 segundos, simulando respuestas `Response` con estados `400`, `401`, `403` y `404` en `manejarErrorApi(...)`, y deteniendo temporalmente el servidor Spring Boot para comprobar la alerta «Sin conexión con el servidor».

---

### Consulta 2: Respuesta `401` en lugar de `403` al probar permisos de tareas con `cURL`
> "Al ejecutar `curl -i -b cookies_ajeno.txt http://localhost:8085/api/viajes/1/tareas` me respondió `HTTP/1.1 401` (`Tu sesión expiró`) en lugar de `403`."

#### Respuesta 2
La respuesta `401` ocurrió porque `cookies_ajeno.txt` no existía aún o contenía una cookie caducada tras reiniciar el servidor. Para que Spring Security identifique al usuario y evalúe la regla de participación del viaje (`403`), primero se debe registrar o autenticar a un usuario ajeno con `POST /api/auth/registro` y `POST /api/auth/login -c cookies_ajeno.txt`, y después consultar `GET /api/viajes/1/tareas -b cookies_ajeno.txt`.

---

### Consulta 3: Separación de archivos en commits individuales por historia de usuario
> "Ya terminé las funcionalidades, ahora quiero subir primero una historia en un commit y luego otra, ¿cómo agrupo los archivos correspondientes?"

#### Respuesta 3
Se indicó preparar el área de *staging* mediante `git add` especificando únicamente los archivos relacionados con cada historia (primero plantilla base, `mensajes.js` y ejemplos en `viaje-nuevo.js`; después repositorio, DTOs, servicio, controlador de tareas, `checklist.js` y `detalle.html`), ejecutando un `git commit` independiente por cada entrega antes de realizar `git push origin main`.

---

## Observaciones adicionales

Estas consultas permitieron comprobar cada criterio de aceptación de ambas funcionalidades y mantener un historial de commits limpio, trazable y separado por entregable en el repositorio del equipo.