// Lista de viajes: tarjetas con filtros y eliminación (HU-23, frontend).
// Basado en frontend/js/viajes-lista.js del ZIP desarrollo-web_0_yj7j.zip
// (cabeza 96f7c78, tag V4, 20 nov 2026). Cambios respecto a esa versión:
// - La tarjeta ya no es solo un enlace: ahora es un <article> que contiene
//   el enlace al detalle y, solo si eres la organizadora, un botón Eliminar.
//   Un botón no puede vivir dentro de un <a>, por eso cambió la estructura.
// - El botón Eliminar llama a DELETE /api/viajes/{id} (HU-23, backend ya
//   hecho: cancela el viaje, estado CANCELADO, 204) y quita la tarjeta de
//   la lista sin recargar. Si era el último viaje, aparece el estado vacío.
// - Solo la organizadora ve el botón: el backend responde 403 a cualquier
//   otro participante, así que no se muestra lo que no se puede usar.
document.addEventListener("DOMContentLoaded", () => {
    const rejillaViajes = document.getElementById("rejillaViajes");
    const estadoVacio = document.getElementById("estadoVacio");
    const tituloEstadoVacio = document.getElementById("tituloEstadoVacio");
    const textoEstadoVacio = document.getElementById("textoEstadoVacio");
    const btnCrearVacio = document.getElementById("btnCrearVacio");
    const estadoCarga = document.getElementById("estadoCarga");

    const inputDestino = document.getElementById("filtroDestino");
    const selectRol = document.getElementById("filtroRol");

    let temporizadorBusqueda = null;
    let viajesActuales = [];

    function formatearFecha(isoFecha) {
        if (!isoFecha) return "";
        const [anio, mes, dia] = isoFecha.split("-");
        const meses = [
            "ene.", "feb.", "mar.", "abr.", "may.", "jun.",
            "jul.", "ago.", "sep.", "oct.", "nov.", "dic."
        ];
        const indiceMes = parseInt(mes, 10) - 1;
        return `${parseInt(dia, 10)} ${meses[indiceMes] || mes} ${anio}`;
    }

    function formatearMoneda(monto, moneda) {
        const valor = Number(monto ?? 0);
        const formateado = valor.toLocaleString("es-MX", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
        return `$${formateado} ${moneda || "MXN"}`;
    }

    function hayFiltrosActivos() {
        return Boolean(inputDestino.value.trim() || selectRol.value);
    }

    async function eliminarViaje(viaje, tarjeta, boton) {
        if (!window.confirm(`¿Eliminar el viaje a ${viaje.destino}? Se cancelará y dejará de aparecer en la lista de todos los participantes.`)) {
            return;
        }

        boton.disabled = true;
        try {
            const respuesta = await fetch(`/api/viajes/${viaje.id}`, {
                method: "DELETE",
                credentials: "same-origin"
            });

            if (!respuesta.ok) {
                await manejarErrorApi(respuesta);
                return;
            }

            // 204 sin cuerpo: el viaje ya está CANCELADO y el GET de la
            // lista no lo devuelve, así que lo quitamos de la vista.
            viajesActuales = viajesActuales.filter((v) => v.id !== viaje.id);
            mostrarMensaje("exito", `Viaje a ${viaje.destino} eliminado.`);
            renderizarViajes(viajesActuales, hayFiltrosActivos());
        } catch (error) {
            mostrarErrorRed();
        } finally {
            boton.disabled = false;
        }
    }

    function crearTarjetaViaje(viaje) {
        const tarjeta = document.createElement("article");
        tarjeta.className = "tarjeta pila";

        const enlace = document.createElement("a");
        enlace.href = `/viajes/${viaje.id}`;
        enlace.className = "tarjeta--enlace pila";

        const esOrganizador = String(viaje.rol).toUpperCase() === "ORGANIZADOR";

        const cabecera = document.createElement("div");
        cabecera.className = "fila fila--separada";

        const titulo = document.createElement("h2");
        titulo.className = "tarjeta__titulo";
        titulo.style.margin = "0";
        titulo.textContent = viaje.destino;

        const insignia = document.createElement("span");
        insignia.className = esOrganizador ? "insignia insignia-organizador" : "insignia";
        insignia.textContent = esOrganizador ? "Organizador" : "Participante";

        cabecera.appendChild(titulo);
        cabecera.appendChild(insignia);

        const fechas = document.createElement("p");
        fechas.className = "tarjeta__meta";
        fechas.style.margin = "0";
        fechas.textContent = `${formatearFecha(viaje.fechaInicio)} – ${formatearFecha(viaje.fechaFin)}`;

        const presupuesto = document.createElement("p");
        presupuesto.style.margin = "0";
        presupuesto.style.fontWeight = "600";
        presupuesto.textContent = `Presupuesto: ${formatearMoneda(viaje.presupuestoTotal, viaje.monedaBase)}`;

        enlace.appendChild(cabecera);
        enlace.appendChild(fechas);
        enlace.appendChild(presupuesto);
        tarjeta.appendChild(enlace);

        // Solo la organizadora puede editar o cancelar el viaje (el
        // backend responde 403 a los demás), así que solo ella ve
        // estos botones.
        if (esOrganizador) {
            const acciones = document.createElement("div");
            acciones.className = "fila";

            const enlaceEditar = document.createElement("a");
            enlaceEditar.href = `/viajes/${viaje.id}/editar`;
            enlaceEditar.className = "btn btn-texto btn-pequeno";
            enlaceEditar.textContent = "Editar";
            enlaceEditar.setAttribute("aria-label", `Editar viaje a ${viaje.destino}`);
            acciones.appendChild(enlaceEditar);

            const btnEliminar = document.createElement("button");
            btnEliminar.type = "button";
            btnEliminar.className = "btn btn-texto btn-pequeno";
            btnEliminar.textContent = "Eliminar viaje";
            btnEliminar.setAttribute("aria-label", `Eliminar viaje a ${viaje.destino}`);
            btnEliminar.addEventListener("click", () => eliminarViaje(viaje, tarjeta, btnEliminar));

            acciones.appendChild(btnEliminar);
            tarjeta.appendChild(acciones);
        }

        return tarjeta;
    }

    function renderizarViajes(viajes, filtrosActivos) {
        estadoCarga.classList.add("oculto");
        rejillaViajes.innerHTML = "";

        if (!Array.isArray(viajes) || viajes.length === 0) {
            rejillaViajes.classList.add("oculto");
            estadoVacio.classList.remove("oculto");

            if (filtrosActivos) {
                tituloEstadoVacio.textContent = "Sin coincidencias";
                textoEstadoVacio.textContent = "No encontramos viajes con los filtros seleccionados.";
                btnCrearVacio.classList.add("oculto");
            } else {
                tituloEstadoVacio.textContent = "Aún no tienes viajes";
                textoEstadoVacio.textContent = "¡Comienza a planear tu próxima aventura con tu grupo!";
                btnCrearVacio.classList.remove("oculto");
            }
            return;
        }

        estadoVacio.classList.add("oculto");
        rejillaViajes.classList.remove("oculto");

        viajes.forEach((viaje) => {
            rejillaViajes.appendChild(crearTarjetaViaje(viaje));
        });
    }

    async function cargarViajes() {
        const destino = inputDestino.value.trim();
        const rol = selectRol.value;
        const filtrosActivos = Boolean(destino || rol);

        const params = new URLSearchParams();
        if (destino) params.set("destino", destino);
        if (rol) params.set("rol", rol);

        const query = params.toString();
        const url = query ? `/api/viajes?${query}` : "/api/viajes";

        try {
            const respuesta = await fetch(url, {
                method: "GET",
                headers: { "Accept": "application/json" }
            });

            if (!respuesta.ok) {
                await manejarErrorApi(respuesta);
                estadoCarga.classList.add("oculto");
                return;
            }

            viajesActuales = await respuesta.json();
            renderizarViajes(viajesActuales, filtrosActivos);
        } catch (error) {
            estadoCarga.classList.add("oculto");
            mostrarErrorRed();
        }
    }

    inputDestino.addEventListener("input", () => {
        clearTimeout(temporizadorBusqueda);
        temporizadorBusqueda = setTimeout(() => {
            cargarViajes();
        }, 250);
    });

    selectRol.addEventListener("change", () => {
        cargarViajes();
    });

    cargarViajes();
});
