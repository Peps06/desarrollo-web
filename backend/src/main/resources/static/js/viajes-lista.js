document.addEventListener("DOMContentLoaded", () => {
    const rejillaViajes = document.getElementById("rejillaViajes");
    const estadoVacio = document.getElementById("estadoVacio");
    const tituloEstadoVacio = document.getElementById("tituloEstadoVacio");
    const textoEstadoVacio = document.getElementById("textoEstadoVacio");
    const btnCrearVacio = document.getElementById("btnCrearVacio");
    const estadoCarga = document.getElementById("estadoCarga");
    const contenedorMensajes = document.getElementById("contenedorMensajes");

    const inputDestino = document.getElementById("filtroDestino");
    const selectRol = document.getElementById("filtroRol");

    let temporizadorBusqueda = null;

    function mostrarMensajeError(texto) {
        if (!contenedorMensajes) return;
        contenedorMensajes.innerHTML = "";
        const alerta = document.createElement("div");
        alerta.className = "mensaje mensaje-error";
        alerta.setAttribute("role", "alert");
        alerta.textContent = texto;
        contenedorMensajes.appendChild(alerta);

        setTimeout(() => {
            alerta.remove();
        }, 4000);
    }

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

    function crearTarjetaViaje(viaje) {
        const enlace = document.createElement("a");
        enlace.href = `/viajes/${viaje.id}`;
        enlace.className = "tarjeta tarjeta--enlace pila";

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

        return enlace;
    }

    function renderizarViajes(viajes, hayFiltrosActivos) {
        estadoCarga.classList.add("oculto");
        rejillaViajes.innerHTML = "";

        if (!Array.isArray(viajes) || viajes.length === 0) {
            rejillaViajes.classList.add("oculto");
            estadoVacio.classList.remove("oculto");

            if (hayFiltrosActivos) {
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
        const hayFiltrosActivos = Boolean(destino || rol);

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

            if (respuesta.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (!respuesta.ok) {
                const errorData = await respuesta.json().catch(() => null);
                mostrarMensajeError(errorData?.mensaje || "No se pudieron cargar tus viajes.");
                estadoCarga.classList.add("oculto");
                return;
            }

            const viajes = await respuesta.json();
            renderizarViajes(viajes, hayFiltrosActivos);
        } catch (error) {
            estadoCarga.classList.add("oculto");
            mostrarMensajeError("Error de conexión al consultar tus viajes.");
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