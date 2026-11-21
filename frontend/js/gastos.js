/**
 * Autora: Citlaly Morales Viveros
 *
 * Gastos del viaje: formulario para registrar un gasto (con comprobante opcional)
 * y lista con edición y eliminación. Todo se hace con fetch() sin recargar la página.
 * Cada alta, cambio o baja emite el evento "gasto-registrado" para que la barra
 * del presupuesto se actualice.
 */
document.addEventListener("DOMContentLoaded", () => {
    const zonaGastos = document.getElementById("zona-gastos");
    const paginaDetalle = document.getElementById("paginaDetalle");

    if (!zonaGastos || !paginaDetalle) return;

    const viajeId = paginaDetalle.dataset.viajeId;
    if (!viajeId) return;

    // Valores que espera el backend, con el texto que ve la persona
    const CATEGORIAS = [
        { valor: "COMIDA", texto: "Comida" },
        { valor: "TRANSPORTE", texto: "Transporte" },
        { valor: "HOSPEDAJE", texto: "Hospedaje" },
        { valor: "ACTIVIDADES", texto: "Actividades" },
        { valor: "OTROS", texto: "Otros" }
    ];
    const TIPOS_COMPROBANTE = ["image/jpeg", "image/png", "image/webp"];
    const TAMANO_MAXIMO_COMPROBANTE = 2 * 1024 * 1024;

    let gastos = [];              // Gastos del viaje, el más reciente primero
    let usuarioActualId = null;   // Id del usuario de la sesión
    let esOrganizador = false;    // Si el usuario es organizador del viaje
    let monedaBase = "MXN";
    let gastoEditandoId = null;   // Id del gasto en edición; null cuando se registra uno nuevo
    let tipoCambioActual = 1;     // Tipo de cambio de la moneda elegida; null si no se pudo consultar

    zonaGastos.innerHTML = `
        <div class="tarjeta pila">
            <h2 class="tarjeta__titulo" style="margin: 0;">Gastos del viaje</h2>

            <form id="formGasto" class="pila" novalidate>
                <div class="campo">
                    <label for="inputConcepto" class="campo__etiqueta">Concepto</label>
                    <input type="text" id="inputConcepto" maxlength="150" placeholder="Ej. Taxi al aeropuerto">
                    <span class="campo__error"></span>
                </div>

                <div class="formulario__fila">
                    <div class="campo">
                        <label for="selectCategoria" class="campo__etiqueta">Categoría</label>
                        <select id="selectCategoria">
                            <option value="">Elige una</option>
                            ${CATEGORIAS.map((c) => `<option value="${c.valor}">${c.texto}</option>`).join("")}
                        </select>
                        <span class="campo__error"></span>
                    </div>

                    <div class="selector-moneda">
                        <div class="campo">
                            <label for="inputMonto" class="campo__etiqueta">Monto (<span id="etiquetaMonto">MXN</span>)</label>
                            <input type="number" id="inputMonto" min="0.01" step="0.01" inputmode="decimal" placeholder="0.00">
                            <span class="campo__error"></span>
                        </div>
                        <div class="campo">
                            <label for="selectMoneda" class="campo__etiqueta">Moneda</label>
                            <select id="selectMoneda"></select>
                        </div>
                    </div>
                </div>
                <p id="equivalenteMoneda" class="equivalente-cambio oculto" role="status"></p>

                <div class="campo" id="campoComprobante">
                    <label for="inputComprobante" class="campo__etiqueta">Comprobante (opcional)</label>
                    <input type="file" id="inputComprobante" accept="image/jpeg,image/png,image/webp">
                    <span class="campo__ayuda">Jpg, png o webp, máximo 2 MB.</span>
                    <div class="fila">
                        <span id="nombreComprobante" class="texto-suave"></span>
                        <button type="button" id="btnQuitarComprobante" class="btn btn-texto btn-pequeno oculto">Quitar</button>
                    </div>
                    <span class="campo__error"></span>
                </div>

                <div class="fila">
                    <button type="submit" id="btnGuardarGasto" class="btn btn-primario btn-pequeno">Registrar gasto</button>
                    <button type="button" id="btnCancelarEdicion" class="btn btn-secundario btn-pequeno oculto">Cancelar</button>
                </div>
            </form>

            <p id="estadoVacioGastos" class="tarjeta-dia__vacio oculto">Aún no hay gastos registrados para este viaje.</p>
            <ul id="listaGastos" style="list-style: none; padding: 0; margin: 0;"></ul>
        </div>
    `;

    const formGasto = document.getElementById("formGasto");
    const inputConcepto = document.getElementById("inputConcepto");
    const selectCategoria = document.getElementById("selectCategoria");
    const inputMonto = document.getElementById("inputMonto");
    const etiquetaMonto = document.getElementById("etiquetaMonto");
    const selectMoneda = document.getElementById("selectMoneda");
    const equivalenteMoneda = document.getElementById("equivalenteMoneda");
    const campoComprobante = document.getElementById("campoComprobante");
    const inputComprobante = document.getElementById("inputComprobante");
    const nombreComprobante = document.getElementById("nombreComprobante");
    const btnQuitarComprobante = document.getElementById("btnQuitarComprobante");
    const btnGuardarGasto = document.getElementById("btnGuardarGasto");
    const btnCancelarEdicion = document.getElementById("btnCancelarEdicion");
    const estadoVacioGastos = document.getElementById("estadoVacioGastos");
    const listaGastos = document.getElementById("listaGastos");

    /* ---------- Formatos y permisos ---------- */

    function formatearMoneda(monto, moneda) {
        const valor = Number(monto ?? 0);
        const texto = Math.abs(valor).toLocaleString("es-MX", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
        return `${valor < 0 ? "-" : ""}$${texto} ${moneda || "MXN"}`;
    }

    function formatearFecha(fechaIso) {
        if (!fechaIso) return "";
        return new Date(fechaIso).toLocaleDateString("es-MX", {
            day: "numeric",
            month: "short",
            year: "numeric"
        });
    }

    function textoCategoria(valor) {
        const categoria = CATEGORIAS.find((c) => c.valor === valor);
        return categoria ? categoria.texto : valor;
    }

    // Solo el autor del gasto o el organizador del viaje pueden editarlo o eliminarlo
    function puedeModificar(gasto) {
        return esOrganizador || (usuarioActualId !== null && gasto.usuarioId === usuarioActualId);
    }

    /* ---------- Moneda y conversión ---------- */

    // Muestra u oculta la línea de equivalente
    function mostrarTextoEquivalente(texto) {
        equivalenteMoneda.textContent = texto;
        equivalenteMoneda.classList.toggle("oculto", !texto);
    }

    // Muestra cuánto equivale el monto en la moneda del viaje
    function mostrarEquivalente() {
        if (gastoEditandoId !== null) return;

        const moneda = selectMoneda.value;
        const monto = Number(inputMonto.value);

        if (moneda === monedaBase || !monto || monto <= 0) {
            mostrarTextoEquivalente("");
            return;
        }
        if (tipoCambioActual === null) {
            mostrarTextoEquivalente(`No pudimos consultar el tipo de cambio. Puedes capturar el monto en ${monedaBase}.`);
            return;
        }
        const equivale = convertirAMonedaBase(monto, tipoCambioActual);
        mostrarTextoEquivalente(`Equivale a ${formatearMontoDivisa(equivale, monedaBase)} al tipo de cambio de hoy`);
    }

    // Al cambiar la moneda se pide el tipo de cambio; si falla, se avisa y se puede usar la moneda del viaje
    async function cambiarMoneda() {
        const moneda = selectMoneda.value;
        etiquetaMonto.textContent = moneda;

        if (moneda === monedaBase) {
            tipoCambioActual = 1;
            mostrarEquivalente();
            return;
        }

        tipoCambioActual = null;
        mostrarTextoEquivalente("Consultando tipo de cambio...");
        try {
            const tasa = await obtenerTipoCambio(moneda, monedaBase);
            if (selectMoneda.value !== moneda) return;  // Ya eligieron otra moneda
            tipoCambioActual = tasa;
        } catch (error) {
            if (selectMoneda.value !== moneda) return;
            mostrarMensaje("aviso", "No pudimos consultar el tipo de cambio. Puedes capturar el monto en la moneda del viaje.");
        }
        mostrarEquivalente();
    }

    /* ---------- Errores del formulario ---------- */

    function mostrarErrorCampo(idEntrada, texto) {
        const campo = document.getElementById(idEntrada).closest(".campo");
        campo.classList.add("campo--invalido");
        campo.querySelector(".campo__error").textContent = texto;
    }

    function limpiarErrores() {
        formGasto.querySelectorAll(".campo").forEach((campo) => {
            campo.classList.remove("campo--invalido");
            const error = campo.querySelector(".campo__error");
            if (error) error.textContent = "";
        });
    }

    // Al escribir o cambiar un campo se quita su error
    formGasto.addEventListener("input", (evento) => {
        const campo = evento.target.closest(".campo");
        if (!campo) return;
        campo.classList.remove("campo--invalido");
        campo.querySelector(".campo__error").textContent = "";
    });

    /* ---------- Formulario ---------- */

    // Revisa el formulario con las mismas reglas del servidor. Regresa los datos o null si hay errores.
    function leerFormulario() {
        limpiarErrores();
        let valido = true;

        const concepto = inputConcepto.value.trim();
        const categoria = selectCategoria.value;
        const montoTexto = inputMonto.value.trim();
        const monto = Number(montoTexto);
        const comprobante = gastoEditandoId === null ? (inputComprobante.files[0] || null) : null;

        if (!concepto) {
            mostrarErrorCampo("inputConcepto", "Escribe el concepto del gasto.");
            valido = false;
        } else if (concepto.length > 150) {
            mostrarErrorCampo("inputConcepto", "El concepto no puede pasar de 150 caracteres.");
            valido = false;
        }

        if (!categoria) {
            mostrarErrorCampo("selectCategoria", "Elige una categoría.");
            valido = false;
        }

        if (!montoTexto || Number.isNaN(monto) || monto <= 0) {
            mostrarErrorCampo("inputMonto", "El monto debe ser mayor a cero.");
            valido = false;
        }

        // Al editar la moneda no cambia: el monto se guarda tal como se escribió
        const moneda = gastoEditandoId === null ? selectMoneda.value : monedaBase;
        if (moneda !== monedaBase && tipoCambioActual === null) {
            mostrarErrorCampo("selectMoneda", "No hay tipo de cambio. Espera un momento o usa la moneda del viaje.");
            valido = false;
        }

        if (comprobante && !TIPOS_COMPROBANTE.includes(comprobante.type)) {
            mostrarErrorCampo("inputComprobante", "El comprobante debe ser jpg, png o webp.");
            valido = false;
        } else if (comprobante && comprobante.size > TAMANO_MAXIMO_COMPROBANTE) {
            mostrarErrorCampo("inputComprobante", "El comprobante no puede pesar más de 2 MB.");
            valido = false;
        }

        if (!valido) {
            formGasto.querySelector(".campo--invalido input, .campo--invalido select")?.focus();
            return null;
        }
        // El monto se guarda en la moneda del viaje; el original y el tipo de cambio quedan de respaldo
        const tipo = moneda === monedaBase ? 1 : tipoCambioActual;
        return {
            concepto,
            categoria,
            comprobante,
            monto: moneda === monedaBase ? monto : convertirAMonedaBase(monto, tipo),
            monedaOriginal: moneda,
            montoOriginal: monto,
            tipoCambio: tipo
        };
    }

    function actualizarNombreComprobante() {
        const archivo = inputComprobante.files[0];
        nombreComprobante.textContent = archivo ? archivo.name : "";
        btnQuitarComprobante.classList.toggle("oculto", !archivo);
    }

    function limpiarFormulario() {
        formGasto.reset();
        limpiarErrores();
        actualizarNombreComprobante();
        selectMoneda.value = monedaBase;
        selectMoneda.disabled = false;
        etiquetaMonto.textContent = monedaBase;
        tipoCambioActual = 1;
        mostrarTextoEquivalente("");
    }

    // Si el archivo elegido no sirve, se avisa de inmediato y se quita
    inputComprobante.addEventListener("change", () => {
        const archivo = inputComprobante.files[0];
        if (archivo && (!TIPOS_COMPROBANTE.includes(archivo.type) || archivo.size > TAMANO_MAXIMO_COMPROBANTE)) {
            inputComprobante.value = "";
            mostrarErrorCampo("inputComprobante", "Elige una imagen jpg, png o webp de máximo 2 MB.");
        }
        actualizarNombreComprobante();
    });

    btnQuitarComprobante.addEventListener("click", () => {
        inputComprobante.value = "";
        actualizarNombreComprobante();
    });

    function empezarEdicion(gasto) {
        limpiarFormulario();
        gastoEditandoId = gasto.id;
        inputConcepto.value = gasto.concepto;
        selectCategoria.value = gasto.categoria;
        inputMonto.value = Number(gasto.monto).toFixed(2);
        // La moneda original no se puede cambiar al editar; solo se muestra como información
        selectMoneda.disabled = true;
        if (gasto.monedaOriginal && gasto.monedaOriginal !== monedaBase) {
            mostrarTextoEquivalente(`Registrado como ${Number(gasto.montoOriginal).toFixed(2)} ${gasto.monedaOriginal} (tipo de cambio ${Number(gasto.tipoCambio)})`);
        }
        // El backend no permite cambiar el comprobante al editar, por eso se oculta el campo
        campoComprobante.classList.add("oculto");
        btnGuardarGasto.textContent = "Guardar cambios";
        btnCancelarEdicion.classList.remove("oculto");
        inputConcepto.focus();
    }

    function terminarEdicion() {
        gastoEditandoId = null;
        limpiarFormulario();
        campoComprobante.classList.remove("oculto");
        btnGuardarGasto.textContent = "Registrar gasto";
        btnCancelarEdicion.classList.add("oculto");
    }

    selectMoneda.addEventListener("change", cambiarMoneda);
    inputMonto.addEventListener("input", mostrarEquivalente);
    btnCancelarEdicion.addEventListener("click", terminarEdicion);

    /* ---------- Peticiones ---------- */

    // Petición con la cookie de sesión. Los errores se muestran con manejarErrorApi (mensajes.js).
    function pedir(url, opciones = {}) {
        return fetch(url, {
            ...opciones,
            credentials: "same-origin",
            headers: { "Accept": "application/json", ...(opciones.headers || {}) }
        });
    }

    // Avisa a la barra de presupuesto que el total del viaje pudo cambiar
    function avisarCambio(detalle) {
        document.dispatchEvent(new CustomEvent("gasto-registrado", { detail: detalle }));
    }

    async function crearGasto(datos) {
        let respuesta;

        if (datos.comprobante) {
            // Con comprobante va como multipart; no se fija Content-Type para que el navegador ponga el boundary
            const formulario = new FormData();
            formulario.append("viajeId", viajeId);
            formulario.append("concepto", datos.concepto);
            formulario.append("categoria", datos.categoria);
            formulario.append("monto", String(datos.monto));
            formulario.append("monedaOriginal", datos.monedaOriginal);
            formulario.append("montoOriginal", String(datos.montoOriginal));
            formulario.append("tipoCambio", String(datos.tipoCambio));
            formulario.append("comprobante", datos.comprobante);
            respuesta = await pedir("/api/gastos", { method: "POST", body: formulario });
        } else {
            respuesta = await pedir("/api/gastos", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    viajeId: Number(viajeId),
                    concepto: datos.concepto,
                    categoria: datos.categoria,
                    monto: datos.monto,
                    monedaOriginal: datos.monedaOriginal,
                    montoOriginal: datos.montoOriginal,
                    tipoCambio: datos.tipoCambio
                })
            });
        }

        if (respuesta.status === 201) {
            const cuerpo = await respuesta.json();
            gastos = [cuerpo.gasto, ...gastos];
            limpiarFormulario();
            renderizarLista();
            mostrarMensaje("exito", "Gasto registrado");
            avisarCambio(cuerpo);
            return;
        }

        // Si hubo error el formulario conserva lo escrito
        await manejarErrorApi(respuesta);
    }

    async function guardarEdicion(datos) {
        const respuesta = await pedir(`/api/gastos/${gastoEditandoId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                concepto: datos.concepto,
                categoria: datos.categoria,
                monto: datos.monto
            })
        });

        if (respuesta.ok) {
            const cuerpo = await respuesta.json();
            gastos = gastos.map((g) => (g.id === cuerpo.gasto.id ? cuerpo.gasto : g));
            terminarEdicion();
            renderizarLista();
            mostrarMensaje("exito", "Gasto actualizado");
            avisarCambio(cuerpo);
            return;
        }

        await manejarErrorApi(respuesta);
    }

    formGasto.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const datos = leerFormulario();
        if (!datos) return;

        btnGuardarGasto.disabled = true;
        try {
            if (gastoEditandoId !== null) {
                await guardarEdicion(datos);
            } else {
                await crearGasto(datos);
            }
        } catch (error) {
            mostrarErrorRed();
        } finally {
            btnGuardarGasto.disabled = false;
        }
    });

    async function eliminarGasto(gasto, boton) {
        if (!window.confirm(`¿Seguro que quieres eliminar "${gasto.concepto}"?`)) return;

        boton.disabled = true;
        try {
            const respuesta = await pedir(`/api/gastos/${gasto.id}`, { method: "DELETE" });

            if (respuesta.status === 204) {
                gastos = gastos.filter((g) => g.id !== gasto.id);
                if (gastoEditandoId === gasto.id) terminarEdicion();
                renderizarLista();
                mostrarMensaje("exito", "Gasto eliminado");
                // El DELETE no regresa total, así que la barra vuelve a pedir el presupuesto
                avisarCambio({ gastoEliminadoId: gasto.id });
                return;
            }

            await manejarErrorApi(respuesta);
            boton.disabled = false;
        } catch (error) {
            mostrarErrorRed();
            boton.disabled = false;
        }
    }

    /* ---------- Lista ---------- */

    function crearFilaGasto(gasto) {
        const fila = document.createElement("li");
        fila.className = "fila fila--separada";
        fila.style.padding = "0.5rem 0";
        fila.dataset.gastoId = gasto.id;

        const info = document.createElement("div");

        const concepto = document.createElement("strong");
        concepto.textContent = gasto.concepto;

        const detalle = document.createElement("p");
        detalle.className = "texto-suave";
        detalle.textContent = `${textoCategoria(gasto.categoria)} · ${formatearFecha(gasto.fecha)} · Pagó ${gasto.usuarioNombre || "un participante"}`;

        info.append(concepto, detalle);

        if (gasto.comprobanteUrl) {
            const enlace = document.createElement("a");
            enlace.href = gasto.comprobanteUrl;
            enlace.target = "_blank";
            enlace.rel = "noopener";
            enlace.textContent = "Ver comprobante";
            info.appendChild(enlace);
        }

        const acciones = document.createElement("div");
        acciones.className = "fila";

        const monto = document.createElement("strong");
        monto.textContent = formatearMoneda(gasto.monto, monedaBase);
        acciones.appendChild(monto);

        if (puedeModificar(gasto)) {
            const btnEditar = document.createElement("button");
            btnEditar.type = "button";
            btnEditar.className = "btn btn-texto btn-pequeno";
            btnEditar.textContent = "Editar";
            btnEditar.setAttribute("aria-label", `Editar ${gasto.concepto}`);
            btnEditar.addEventListener("click", () => empezarEdicion(gasto));

            const btnEliminar = document.createElement("button");
            btnEliminar.type = "button";
            btnEliminar.className = "btn btn-texto btn-pequeno";
            btnEliminar.textContent = "Eliminar";
            btnEliminar.setAttribute("aria-label", `Eliminar ${gasto.concepto}`);
            btnEliminar.addEventListener("click", () => eliminarGasto(gasto, btnEliminar));

            acciones.append(btnEditar, btnEliminar);
        }

        fila.append(info, acciones);
        return fila;
    }

    function renderizarLista() {
        listaGastos.innerHTML = "";

        if (gastos.length === 0) {
            estadoVacioGastos.classList.remove("oculto");
            return;
        }

        estadoVacioGastos.classList.add("oculto");
        gastos.forEach((gasto) => {
            listaGastos.appendChild(crearFilaGasto(gasto));
        });
    }

    async function cargarGastos() {
        try {
            const respuesta = await pedir(`/api/viajes/${viajeId}/gastos`);
            if (!respuesta.ok) {
                await manejarErrorApi(respuesta);
                return;
            }
            gastos = await respuesta.json();
            renderizarLista();
        } catch (error) {
            mostrarErrorRed();
        }
    }

    async function cargarSesion() {
        try {
            const respuesta = await pedir("/api/auth/sesion");
            if (respuesta.ok) {
                const sesion = await respuesta.json();
                usuarioActualId = sesion.id;
                renderizarLista();
            }
        } catch (error) {
            // Sin sesión legible no se muestran los botones de editar; lo demás sigue funcionando
        }
    }

    // La página del viaje emite este evento al pintarse: aquí se toma el rol y la moneda base
    document.addEventListener("viaje:actualizado", (evento) => {
        const viaje = evento.detail;
        if (!viaje) return;

        esOrganizador = String(viaje.rol).toUpperCase() === "ORGANIZADOR";
        monedaBase = viaje.monedaBase || "MXN";
        etiquetaMonto.textContent = monedaBase;
        llenarSelectorMoneda(selectMoneda, monedaBase);
        renderizarLista();
    });

    cargarSesion();
    cargarGastos();
});