/**
 * Balance de deudas del grupo.
 *
 * Muestra cuánto pagó, cuánto debe cada participante del viaje y la lista
 * de transferencias sugeridas para dejar todas las cuentas en cero.
 */
document.addEventListener("DOMContentLoaded", () => {
    const zonaBalance = document.getElementById("zona-balance");
    const paginaDetalle = document.getElementById("paginaDetalle");

    if (!zonaBalance || !paginaDetalle) return;

    const viajeId = paginaDetalle.dataset.viajeId;
    if (!viajeId) return;

    let monedaBase = "MXN";
    let usuarioActualId = null;
    let numeroPeticion = 0;

    zonaBalance.innerHTML = `
        <div class="tarjeta pila">
            <h2 class="tarjeta__titulo" style="margin: 0;">Balance del grupo</h2>
            <p id="estadoBalance" class="texto-suave" role="status">Cargando balance...</p>
            <div id="contenidoBalance" class="oculto">
                <div id="listaSaldos" class="fila"></div>
                <p class="campo__etiqueta">Transferencias sugeridas</p>
                <div id="listaTransferencias"></div>
            </div>
        </div>
    `;

    const estadoBalance = document.getElementById("estadoBalance");
    const contenidoBalance = document.getElementById("contenidoBalance");
    const listaSaldos = document.getElementById("listaSaldos");
    const listaTransferencias = document.getElementById("listaTransferencias");

    /**
     * Da formato a un monto de dinero
     * @param {number|string} monto
     * @param {string} moneda 
     * @returns {string}
     */
    function formatearMoneda(monto, moneda) {
        const valor = Number(monto ?? 0);
        const texto = Math.abs(valor).toLocaleString("es-MX", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
        return `${valor < 0 ? "-" : ""}$${texto} ${moneda || "MXN"}`;
    }

    /**
     * Crea el nombre de un participante.
     * @param {number} usuarioId
     * @param {string} nombre
     * @returns {HTMLElement}
     */
    function nombreVisible(usuarioId, nombre) {
        const span = document.createElement("span");
        if (usuarioId === usuarioActualId) {
            const negrita = document.createElement("strong");
            negrita.textContent = `${nombre} (tú)`;
            span.appendChild(negrita);
        } else {
            span.textContent = nombre;
        }
        return span;
    }

    /**
     * Dibuja una tarjeta por participante con lo que pagó, su cuota y su saldo.
     * @param {object} datos 
     */
    function renderizarSaldos(datos) {
        listaSaldos.innerHTML = "";

        datos.saldos.forEach((persona) => {
            const saldo = Number(persona.saldo);
            let clase = "saldo-cero";
            let texto = "Está al día";

            if (saldo > 0) {
                clase = "saldo-favor";
                texto = `Le deben ${formatearMoneda(saldo, monedaBase)}`;
            } else if (saldo < 0) {
                clase = "saldo-deuda";
                texto = `Debe ${formatearMoneda(Math.abs(saldo), monedaBase)}`;
            }

            const tarjeta = document.createElement("div");
            tarjeta.className = "tarjeta";

            const titulo = document.createElement("p");
            titulo.className = "tarjeta__titulo";
            titulo.appendChild(nombreVisible(persona.usuarioId, persona.nombre));

            const detalle = document.createElement("p");
            detalle.className = "texto-suave";
            detalle.textContent = `Pagó ${formatearMoneda(persona.pagado, monedaBase)} · cuota ${formatearMoneda(datos.cuotaPorPersona, monedaBase)}`;

            const estado = document.createElement("p");
            estado.className = clase;
            estado.textContent = texto;

            tarjeta.appendChild(titulo);
            tarjeta.appendChild(detalle);
            tarjeta.appendChild(estado);
            listaSaldos.appendChild(tarjeta);
        });
    }

    /**
     * Dibuja una frase por transferencia, si no hay transferencias
     * muestra que todo está saldado.
     * @param {Array<object>} transferencias
     */
    function renderizarTransferencias(transferencias) {
        listaTransferencias.innerHTML = "";

        if (transferencias.length === 0) {
            const saldado = document.createElement("p");
            saldado.className = "texto-suave";
            saldado.textContent = "Todo está saldado. No hay deudas pendientes entre los participantes.";
            listaTransferencias.appendChild(saldado);
            return;
        }

        transferencias.forEach((transferencia) => {
            const frase = document.createElement("p");
            frase.className = "transferencia";
            frase.appendChild(nombreVisible(transferencia.deudorId, transferencia.deudorNombre));
            frase.appendChild(document.createTextNode(` le debe ${formatearMoneda(transferencia.monto, monedaBase)} a `));
            frase.appendChild(nombreVisible(transferencia.acreedorId, transferencia.acreedorNombre));
            listaTransferencias.appendChild(frase);
        });
    }

    /**
     * Oculta el contenido del balance y muestra un mensaje en su lugar.
     * @param {string} texto
     */
    function mostrarEstado(texto) {
        contenidoBalance.classList.add("oculto");
        estadoBalance.classList.remove("oculto");
        estadoBalance.textContent = texto;
    }

    /**
     * Decide qué mostrar según los datos: el balance o el mensaje de sin gastos.
     * @param {object} datos
     */
    function renderizarBalance(datos) {
        // Sin gastos, el backend manda saldos en 0 y transferencias vacías
        if (Number(datos.totalGastado) === 0) {
            mostrarEstado("Todavía no hay gastos que repartir");
            return;
        }

        renderizarSaldos(datos);
        renderizarTransferencias(datos.transferencias);
        contenidoBalance.classList.remove("oculto");
        estadoBalance.classList.add("oculto");
    }

    /**
     * Pide el usuario en sesión para poder resaltarlo.
     * Si falla, el balance se muestra igual, sin resaltar a nadie.
     */
    async function cargarSesion() {
        try {
            const respuesta = await fetch("/api/auth/sesion", {
                method: "GET",
                credentials: "same-origin",
                headers: { "Accept": "application/json" }
            });
            if (respuesta.ok) {
                const sesion = await respuesta.json();
                usuarioActualId = sesion.id;
            }
        } catch (error) {
        }
    }

    const sesionLista = cargarSesion();

    /**
     * Consulta el balance del viaje y lo dibuja.
     * Si llega una respuesta vieja después de una más nueva, la ignora.
     */
    async function cargarBalance() {
        const peticion = ++numeroPeticion;
        await sesionLista;

        try {
            const respuesta = await fetch(`/api/viajes/${viajeId}/balance`, {
                method: "GET",
                credentials: "same-origin",
                headers: { "Accept": "application/json" }
            });

            // Si ya salió otra consulta más reciente, esta respuesta sobra
            if (peticion !== numeroPeticion) return;

            if (!respuesta.ok) {
                // Los errores de la API se muestran con los avisos de mensajes.js
                await manejarErrorApi(respuesta);
                mostrarEstado("No se pudo cargar el balance.");
                return;
            }

            const datos = await respuesta.json();
            renderizarBalance(datos);
        } catch (error) {
            if (peticion === numeroPeticion) {
                mostrarErrorRed();
                mostrarEstado("Sin conexión con el servidor. No se pudo cargar el balance.");
            }
        }
    }

    document.addEventListener("gasto-registrado", cargarBalance);
    document.addEventListener("viaje:actualizado", (evento) => {
        monedaBase = evento.detail?.monedaBase || monedaBase;
        cargarBalance();
    });

    cargarBalance();
});