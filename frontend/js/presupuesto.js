/**
 * Autora: Citlaly Morales Viveros
 *
 * Presupuesto del viaje: barra de progreso con el porcentaje gastado, aviso cuando
 * el presupuesto se excede y actualización del resumen del encabezado.
 * Se vuelve a consultar al cargar la página, al registrar, editar o eliminar un gasto
 * y cuando la página del viaje se vuelve a pintar.
 */
document.addEventListener("DOMContentLoaded", () => {
    const zonaPresupuesto = document.getElementById("zona-presupuesto");
    const paginaDetalle = document.getElementById("paginaDetalle");

    if (!zonaPresupuesto || !paginaDetalle) return;

    const viajeId = paginaDetalle.dataset.viajeId;
    if (!viajeId) return;

    // Elementos del resumen del encabezado (los pinta viaje-detalle.js al cargar)
    const datoGastado = document.getElementById("datoGastado");
    const datoRestante = document.getElementById("datoRestante");
    const etiquetaRestante = document.getElementById("etiquetaRestante");

    let numeroPeticion = 0;  // Descarta respuestas viejas si llegan dos consultas seguidas

    zonaPresupuesto.innerHTML = `
        <div class="tarjeta pila">
            <h2 class="tarjeta__titulo" style="margin: 0;">Presupuesto</h2>
            <p id="estadoPresupuesto" class="texto-suave" role="status">Cargando presupuesto...</p>
            <div id="contenidoPresupuesto" class="oculto"></div>
        </div>
    `;

    const estadoPresupuesto = document.getElementById("estadoPresupuesto");
    const contenidoPresupuesto = document.getElementById("contenidoPresupuesto");

    /* ---------- Formatos ---------- */

    function formatearMoneda(monto, moneda) {
        const valor = Number(monto ?? 0);
        const texto = Math.abs(valor).toLocaleString("es-MX", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
        return `${valor < 0 ? "-" : ""}$${texto} ${moneda || "MXN"}`;
    }

    function textoParticipantes(total) {
        return total === 1 ? "1 participante" : `${total} participantes`;
    }

    // Estado de color según el porcentaje usado (RF-24)
    function estadoDe(porcentaje) {
        if (porcentaje > 100) return "estado-excedido";
        if (porcentaje >= 75) return "estado-alerta";
        return "estado-ok";
    }

    /* ---------- Pintado ---------- */

    // Dibuja la tarjeta completa con los datos del presupuesto
    function renderizarPresupuesto(datos) {
        const porcentaje = Number(datos.porcentajeUsado);
        const restante = Number(datos.restante);
        const excedido = porcentaje > 100;
        const usoVisible = Math.min(porcentaje, 100);

        contenidoPresupuesto.innerHTML = `
            <div class="barra-presupuesto" role="progressbar" aria-valuemin="0" aria-valuemax="100"
                 aria-valuenow="${Math.round(usoVisible)}" aria-label="Porcentaje del presupuesto usado">
                <span class="barra-presupuesto__relleno"></span>
            </div>
            <div class="barra-presupuesto__etiqueta">
                <span id="textoGastado"></span>
                <span id="textoPorcentaje"></span>
            </div>
            <p class="texto-suave" id="textoPorPersona" style="margin-top: var(--espacio-2);"></p>
            <div id="avisoExcedido" class="aviso-excedido oculto" role="status"></div>
        `;

        const barra = contenidoPresupuesto.querySelector(".barra-presupuesto");
        barra.classList.add(estadoDe(porcentaje));
        barra.style.setProperty("--uso", `${usoVisible}%`);

        contenidoPresupuesto.querySelector("#textoGastado").textContent =
            `Gastado: ${formatearMoneda(datos.totalGastado, datos.monedaBase)} de ${formatearMoneda(datos.presupuestoTotal, datos.monedaBase)}`;
        contenidoPresupuesto.querySelector("#textoPorcentaje").textContent = `${Math.round(porcentaje)}%`;
        contenidoPresupuesto.querySelector("#textoPorPersona").textContent =
            `${textoParticipantes(datos.numParticipantes)} · ${formatearMoneda(datos.gastoPorPersona, datos.monedaBase)} por persona`;

        if (excedido) {
            const aviso = contenidoPresupuesto.querySelector("#avisoExcedido");
            aviso.textContent = `Presupuesto excedido por ${formatearMoneda(Math.abs(restante), datos.monedaBase)}`;
            aviso.classList.remove("oculto");
        }

        contenidoPresupuesto.classList.remove("oculto");
        estadoPresupuesto.classList.add("oculto");
    }

    // Actualiza gastado y disponible en el encabezado del viaje
    function actualizarEncabezado(datos) {
        const restante = Number(datos.restante);
        datoGastado.textContent = formatearMoneda(datos.totalGastado, datos.monedaBase);
        etiquetaRestante.textContent = restante < 0 ? "Excedido por" : "Disponible";
        datoRestante.textContent = formatearMoneda(Math.abs(restante), datos.monedaBase);
    }

    function mostrarErrorPresupuesto(texto) {
        contenidoPresupuesto.classList.add("oculto");
        estadoPresupuesto.classList.remove("oculto");
        estadoPresupuesto.textContent = texto;
    }

    /* ---------- Carga ---------- */

    async function cargarPresupuesto() {
        const peticion = ++numeroPeticion;

        try {
            const respuesta = await fetch(`/api/viajes/${viajeId}/presupuesto`, {
                method: "GET",
                credentials: "same-origin",
                headers: { "Accept": "application/json" }
            });

            // Si ya salió otra consulta más reciente, esta respuesta sobra
            if (peticion !== numeroPeticion) return;

            if (!respuesta.ok) {
                // 401 redirige al login; otros errores se muestran dentro de la tarjeta
                if (respuesta.status === 401) {
                    await manejarErrorApi(respuesta);
                    return;
                }
                const errorData = await respuesta.json().catch(() => null);
                mostrarErrorPresupuesto(errorData?.mensaje || "No se pudo cargar el presupuesto.");
                return;
            }

            const datos = await respuesta.json();
            renderizarPresupuesto(datos);
            actualizarEncabezado(datos);
        } catch (error) {
            if (peticion === numeroPeticion) {
                mostrarErrorPresupuesto("Sin conexión con el servidor. No se pudo cargar el presupuesto.");
            }
        }
    }

    // Se actualiza al cargar, al cambiar un gasto y cuando la página del viaje se vuelve a pintar
    document.addEventListener("gasto-registrado", cargarPresupuesto);
    document.addEventListener("viaje:actualizado", cargarPresupuesto);

    cargarPresupuesto();
});