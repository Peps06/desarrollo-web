// Votación de actividades con fetch(), sin recargar la página.

/**
 * Pinta en el botón el contador y el estado que devolvió la API.
 *
 * @param {HTMLButtonElement} boton botón de voto de la actividad
 * @param {object} datos respuesta de la API: {actividadId, totalVotos, votadaPorMi}
 */
function actualizarBotonVoto(boton, datos) {
    const contador = boton.querySelector(".btn-voto__contador");
    contador.textContent = datos.totalVotos;

    if (datos.votadaPorMi) {
        boton.classList.add("votado");
    } else {
        boton.classList.remove("votado");
    }
    boton.setAttribute("aria-pressed", String(datos.votadaPorMi));
}

/**
 * Vota la actividad o quita el voto, según si el botón ya tiene la clase "votado".
 * Si sale bien, actualiza el botón y pide a viaje-detalle.js que vuelva a cargar
 * el detalle para refrescar la insignia de la más votada. Si falla, el contador no cambia.
 *
 * @param {HTMLButtonElement} boton botón de voto que se presionó
 */
async function cambiarVoto(boton) {
    const fila = boton.closest(".actividad");
    const actividadId = fila.dataset.actividadId;

    let metodo = "POST";
    if (boton.classList.contains("votado")) {
        metodo = "DELETE";
    }

    boton.disabled = true;
    try {
        const respuesta = await fetch(`/api/actividades/${actividadId}/votos`, {
            method: metodo,
            credentials: "same-origin",
            headers: { "Accept": "application/json" }
        });

        if (respuesta.ok) {
            const datos = await respuesta.json();
            actualizarBotonVoto(boton, datos);
            document.dispatchEvent(new Event("viaje:recargar"));
        } else {
            await manejarErrorApi(respuesta);
        }
    } catch (error) {
        mostrarErrorRed();
    } finally {
        boton.disabled = false;
    }
}

// Un solo listener en el documento atiende también los botones que se crean después.
document.addEventListener("click", function (evento) {
    const boton = evento.target.closest(".btn-voto");
    if (boton && !boton.disabled) {
        cambiarVoto(boton);
    }
});
