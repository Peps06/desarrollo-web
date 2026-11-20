/**
 * Módulo compartido de notificaciones visuales y traducción de errores HTTP.
 * Disponible de forma global para todos los scripts del frontend mediante fragments/base.html.
 */

function obtenerContenedorMensajes() {
    let contenedor = document.getElementById("contenedorMensajes");
    if (!contenedor) {
        contenedor = document.createElement("div");
        contenedor.id = "contenedorMensajes";
        contenedor.className = "contenedor-mensajes";
        contenedor.setAttribute("aria-live", "polite");
        document.body.appendChild(contenedor);
    }
    return contenedor;
}

/**
 * Muestra una notificación flotante no bloqueante que desaparece a los 4 segundos
 * y puede cerrarse manualmente.
 *
 * @param {"exito"|"error"|"aviso"} tipo
 * @param {string} texto
 */
function mostrarMensaje(tipo, texto) {
    if (!texto) return;

    const contenedor = obtenerContenedorMensajes();
    const claseTipo = {
        exito: "mensaje-exito",
        error: "mensaje-error",
        aviso: "mensaje-aviso"
    }[tipo] || "mensaje-error";

    const alerta = document.createElement("div");
    alerta.className = `mensaje ${claseTipo}`;
    alerta.setAttribute("role", tipo === "error" ? "alert" : "status");
    alerta.style.display = "flex";
    alerta.style.alignItems = "flex-start";
    alerta.style.justifyContent = "space-between";
    alerta.style.gap = "0.75rem";

    const cuerpoTexto = document.createElement("span");
    cuerpoTexto.textContent = texto;

    const btnCerrar = document.createElement("button");
    btnCerrar.type = "button";
    btnCerrar.setAttribute("aria-label", "Cerrar mensaje");
    btnCerrar.textContent = "×";
    btnCerrar.style.background = "transparent";
    btnCerrar.style.border = "0";
    btnCerrar.style.color = "inherit";
    btnCerrar.style.fontSize = "1.2rem";
    btnCerrar.style.lineHeight = "1";
    btnCerrar.style.cursor = "pointer";
    btnCerrar.style.padding = "0";

    let temporizador = null;
    const cerrarAlerta = () => {
        if (temporizador) clearTimeout(temporizador);
        alerta.remove();
    };

    btnCerrar.addEventListener("click", cerrarAlerta);

    alerta.appendChild(cuerpoTexto);
    alerta.appendChild(btnCerrar);
    contenedor.appendChild(alerta);

    temporizador = setTimeout(cerrarAlerta, 4000);
}

/**
 * Traduce un objeto Response de fetch() con formato ErrorResponse ({codigo, mensaje, errores})
 * a mensajes claros en español y muestra la alerta correspondiente.
 *
 * @param {Response} respuesta
 * @returns {Promise<object|null>} El cuerpo JSON parseado si existía
 */
async function manejarErrorApi(respuesta) {
    const errorData = await respuesta.json().catch(() => null);
    const detalle = errorData && errorData.mensaje ? errorData.mensaje : "";

    switch (respuesta.status) {
        case 400:
            mostrarMensaje(
                "error",
                detalle && detalle !== "Datos inválidos"
                    ? `Revisa los datos: ${detalle}`
                    : "Revisa los datos"
            );
            break;

        case 401:
            mostrarMensaje("error", "Tu sesión expiró");
            setTimeout(() => {
                window.location.href = "/login";
            }, 900);
            break;

        case 403:
            mostrarMensaje("error", "No tienes permiso");
            break;

        case 404:
            mostrarMensaje("error", "No se encontró");
            break;

        default:
            mostrarMensaje("error", detalle || "Ocurrió un error inesperado en el servidor");
            break;
    }

    return errorData;
}

/**
 * Muestra el mensaje estándar cuando fetch() falla por red caída o servidor inaccesible.
 */
function mostrarErrorRed() {
    mostrarMensaje("error", "Sin conexión con el servidor");
}