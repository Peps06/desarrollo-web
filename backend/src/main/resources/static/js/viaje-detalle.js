/**
 * Detalle del viaje: encabezado, línea de tiempo por días, propuesta, edición y
 * eliminación de actividades, filtros y descarga del itinerario en PDF.
 * Todo se carga con fetch() sin recargar la página.
 * Al terminar de pintar emite el evento "viaje:actualizado" con el detalle del viaje,
 * para que las secciones de las zonas (votos, presupuesto, gastos...) puedan refrescarse.
 */
document.addEventListener("DOMContentLoaded", () => {
    const pagina = document.getElementById("paginaDetalle");
    const viajeId = pagina.dataset.viajeId;

    const contenedorMensajes = document.getElementById("contenedorMensajes");
    const estadoCarga = document.getElementById("estadoCarga");
    const estadoError = document.getElementById("estadoError");
    const tituloEstadoError = document.getElementById("tituloEstadoError");
    const textoEstadoError = document.getElementById("textoEstadoError");
    const contenidoViaje = document.getElementById("contenidoViaje");

    const tituloDestino = document.getElementById("tituloDestino");
    const textoFechas = document.getElementById("textoFechas");
    const textoParticipantes = document.getElementById("textoParticipantes");
    const listaGrupo = document.getElementById("listaGrupo");
    const datoPresupuesto = document.getElementById("datoPresupuesto");
    const datoGastado = document.getElementById("datoGastado");
    const etiquetaRestante = document.getElementById("etiquetaRestante");
    const datoRestante = document.getElementById("datoRestante");

    const listaDias = document.getElementById("listaDias");
    const filtroDia = document.getElementById("filtroDia");
    const filtroCostoMax = document.getElementById("filtroCostoMax");
    const grupoCostoMax = document.getElementById("grupo-filtroCostoMax");
    const errorCostoMax = document.getElementById("error-filtroCostoMax");
    const textoFiltros = document.getElementById("textoFiltros");
    const btnLimpiarFiltros = document.getElementById("btnLimpiarFiltros");
    const btnProponer = document.getElementById("btnProponer");
    const btnExportarPdf = document.getElementById("btnExportarPdf");

    const MESES = [
        "ene.", "feb.", "mar.", "abr.", "may.", "jun.",
        "jul.", "ago.", "sep.", "oct.", "nov.", "dic."
    ];
    const MESES_LARGOS = [
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    ];
    const DIAS_SEMANA = ["Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"];
    const DIAS_SEMANA_LARGOS = ["domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"];

    let viaje = null;                 // Último detalle recibido de GET /api/viajes/{id}
    let usuarioActualId = null;       // Id del usuario de la sesión (GET /api/auth/sesion)
    let actividadesFiltradas = null;  // Resultado del filtro; null cuando no hay filtros
    let diaFiltrado = "";             // Día usado en el último filtro aplicado
    let numeroPeticionFiltro = 0;     // Descarta respuestas de filtros que ya no son las más recientes
    let temporizadorFiltro = null;

    /* ---------- Mensajes ---------- */

    // Muestra un mensaje de éxito, error o aviso que desaparece a los 4 segundos.
    // Si existe una función global mostrarMensaje (mensajes.js), se usa esa.
    function mostrarMensaje(tipo, texto) {
        if (typeof window.mostrarMensaje === "function") {
            window.mostrarMensaje(tipo, texto);
            return;
        }
        if (!contenedorMensajes) return;
        const alerta = document.createElement("div");
        alerta.className = `mensaje mensaje-${tipo}`;
        alerta.setAttribute("role", tipo === "error" ? "alert" : "status");
        alerta.textContent = texto;
        contenedorMensajes.appendChild(alerta);

        setTimeout(() => {
            alerta.remove();
        }, 4000);
    }

    // Reemplaza la vista por un mensaje claro cuando el viaje no se puede mostrar.
    function mostrarEstadoError(codigo, mensaje) {
        estadoCarga.classList.add("oculto");
        contenidoViaje.classList.add("oculto");
        estadoError.classList.remove("oculto");

        if (codigo === 404) {
            tituloEstadoError.textContent = "Viaje no encontrado";
            textoEstadoError.textContent = "El viaje no existe o fue cancelado.";
        } else if (codigo === 403) {
            tituloEstadoError.textContent = "No tienes acceso a este viaje";
            textoEstadoError.textContent = mensaje || "Solo los participantes del viaje pueden ver su itinerario.";
        } else if (codigo === 400) {
            tituloEstadoError.textContent = "Viaje no válido";
            textoEstadoError.textContent = "La dirección del viaje no es correcta.";
        } else if (codigo === 0) {
            tituloEstadoError.textContent = "Sin conexión";
            textoEstadoError.textContent = "No se pudo conectar con el servidor. Verifica tu conexión.";
        } else {
            tituloEstadoError.textContent = "No se pudo cargar el viaje";
            textoEstadoError.textContent = mensaje || "Ocurrió un error inesperado. Intenta más tarde.";
        }
    }

    /* ---------- Peticiones ---------- */

    // Petición con la cookie de sesión. Si la sesión expiró regresa al login y devuelve null.
    async function pedir(url, opciones = {}) {
        const respuesta = await fetch(url, {
            ...opciones,
            credentials: "same-origin",
            headers: { "Accept": "application/json", ...(opciones.headers || {}) }
        });
        if (respuesta.status === 401) {
            window.location.href = "/login";
            return null;
        }
        return respuesta;
    }

    async function leerError(respuesta) {
        return respuesta.json().catch(() => null);
    }

    /* ---------- Formatos ---------- */

    function partesFecha(isoFecha) {
        const [anio, mes, dia] = isoFecha.split("-").map((parte) => parseInt(parte, 10));
        // Se usa UTC para que la zona horaria del navegador no cambie el día.
        const diaSemana = new Date(Date.UTC(anio, mes - 1, dia)).getUTCDay();
        return { anio, mes, dia, diaSemana };
    }

    function formatearFecha(isoFecha) {
        if (!isoFecha) return "";
        const { anio, mes, dia } = partesFecha(isoFecha);
        return `${dia} ${MESES[mes - 1]} ${anio}`;
    }

    function formatearFechaLarga(isoFecha) {
        const { anio, mes, dia, diaSemana } = partesFecha(isoFecha);
        return `${DIAS_SEMANA_LARGOS[diaSemana]} ${dia} de ${MESES_LARGOS[mes - 1]} de ${anio}`;
    }

    function formatearMoneda(monto, moneda) {
        const valor = Number(monto ?? 0);
        const formateado = Math.abs(valor).toLocaleString("es-MX", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
        return `${valor < 0 ? "-" : ""}$${formateado} ${moneda || "MXN"}`;
    }

    function textoVotos(total) {
        return total === 1 ? "1 voto" : `${total} votos`;
    }

    function esOrganizador() {
        return String(viaje?.rol).toUpperCase() === "ORGANIZADOR";
    }

    function puedeModificar(actividad) {
        return esOrganizador() || (usuarioActualId !== null && actividad.autor?.id === usuarioActualId);
    }

    /* ---------- Encabezado ---------- */

    function renderizarEncabezado() {
        tituloDestino.textContent = viaje.destino;
        document.title = `${viaje.destino} | Bitácora de Viajes`;

        const totalDias = viaje.dias.length;
        textoFechas.textContent = `${formatearFecha(viaje.fechaInicio)} – ${formatearFecha(viaje.fechaFin)}`
            + ` · ${totalDias} ${totalDias === 1 ? "día" : "días"}`;

        const totalParticipantes = viaje.participantes.length;
        textoParticipantes.textContent = `${totalParticipantes} ${totalParticipantes === 1 ? "participante" : "participantes"}`;

        listaGrupo.innerHTML = "";
        viaje.participantes.forEach((participante) => {
            const elemento = document.createElement("span");
            elemento.setAttribute("role", "listitem");
            const organizador = String(participante.rol).toUpperCase() === "ORGANIZADOR";
            elemento.className = organizador ? "insignia insignia-organizador" : "insignia";
            elemento.textContent = organizador ? `${participante.nombre} · Organizador` : participante.nombre;
            listaGrupo.appendChild(elemento);
        });

        const presupuesto = Number(viaje.presupuestoTotal ?? 0);
        const gastado = Number(viaje.totalGastado ?? 0);
        const restante = presupuesto - gastado;
        datoPresupuesto.textContent = formatearMoneda(presupuesto, viaje.monedaBase);
        datoGastado.textContent = formatearMoneda(gastado, viaje.monedaBase);
        etiquetaRestante.textContent = restante < 0 ? "Excedido por" : "Disponible";
        datoRestante.textContent = formatearMoneda(Math.abs(restante), viaje.monedaBase);
    }

    /* ---------- Filtros ---------- */

    // Llena el selector de días una sola vez, conservando la opción elegida.
    function prepararFiltroDias() {
        const seleccionado = filtroDia.value;
        filtroDia.querySelectorAll("option:not([value=''])").forEach((opcion) => opcion.remove());
        viaje.dias.forEach((dia) => {
            const { mes, dia: numero, diaSemana } = partesFecha(dia.fecha);
            const opcion = document.createElement("option");
            opcion.value = dia.fecha;
            opcion.textContent = `${DIAS_SEMANA[diaSemana]} ${numero} ${MESES[mes - 1]}`;
            filtroDia.appendChild(opcion);
        });
        filtroDia.value = seleccionado;
    }

    function limpiarErrorCosto() {
        errorCostoMax.textContent = "";
        grupoCostoMax.classList.remove("campo--invalido");
    }

    function actualizarTextoFiltros() {
        if (actividadesFiltradas === null) {
            textoFiltros.textContent = "";
            btnLimpiarFiltros.classList.add("oculto");
            return;
        }
        const total = actividadesFiltradas.length;
        textoFiltros.textContent = total === 1
            ? "1 actividad cumple los filtros."
            : `${total} actividades cumplen los filtros.`;
        btnLimpiarFiltros.classList.remove("oculto");
    }

    // Consulta GET /api/viajes/{id}/actividades?dia&costoMax y vuelve a pintar los días.
    async function aplicarFiltros() {
        const dia = filtroDia.value;
        const costoTexto = filtroCostoMax.value.trim();
        limpiarErrorCosto();

        if (filtroCostoMax.validity.badInput || (costoTexto && (Number.isNaN(Number(costoTexto)) || Number(costoTexto) < 0))) {
            errorCostoMax.textContent = "Escribe un número mayor o igual a 0.";
            grupoCostoMax.classList.add("campo--invalido");
            return;
        }

        const peticion = ++numeroPeticionFiltro;

        if (!dia && !costoTexto) {
            actividadesFiltradas = null;
            diaFiltrado = "";
            renderizarDias();
            actualizarTextoFiltros();
            return;
        }

        const parametros = new URLSearchParams();
        if (dia) parametros.set("dia", dia);
        if (costoTexto) parametros.set("costoMax", costoTexto);

        try {
            const respuesta = await pedir(`/api/viajes/${viajeId}/actividades?${parametros.toString()}`);
            if (!respuesta || peticion !== numeroPeticionFiltro) return;

            if (respuesta.status === 403 || respuesta.status === 404) {
                const errorData = await leerError(respuesta);
                mostrarEstadoError(respuesta.status, errorData?.mensaje);
                return;
            }
            if (!respuesta.ok) {
                const errorData = await leerError(respuesta);
                mostrarMensaje("error", errorData?.mensaje || "No se pudieron aplicar los filtros.");
                return;
            }

            actividadesFiltradas = await respuesta.json();
            diaFiltrado = dia;
            renderizarDias();
            actualizarTextoFiltros();
        } catch (error) {
            mostrarMensaje("error", "Error de conexión al filtrar las actividades.");
        }
    }

    /* ---------- Días y actividades ---------- */

    function renderizarDias() {
        listaDias.innerHTML = "";
        const hayFiltros = actividadesFiltradas !== null;
        const dias = hayFiltros && diaFiltrado
            ? viaje.dias.filter((dia) => dia.fecha === diaFiltrado)
            : viaje.dias;

        dias.forEach((dia) => {
            const actividades = (hayFiltros
                ? actividadesFiltradas.filter((actividad) => actividad.dia === dia.fecha)
                : dia.actividades || []
            ).slice().sort((a, b) => a.hora.localeCompare(b.hora) || a.id - b.id);

            listaDias.appendChild(crearTarjetaDia(dia, actividades, hayFiltros));
        });

        document.dispatchEvent(new CustomEvent("viaje:actualizado", { detail: viaje }));
    }

    function crearTarjetaDia(dia, actividades, hayFiltros) {
        const { mes, dia: numero, diaSemana } = partesFecha(dia.fecha);

        const tarjeta = document.createElement("article");
        tarjeta.className = "tarjeta-dia";
        tarjeta.id = `dia-${dia.fecha}`;
        tarjeta.dataset.fecha = dia.fecha;
        tarjeta.setAttribute("aria-label", formatearFechaLarga(dia.fecha));

        const fecha = document.createElement("div");
        fecha.className = "tarjeta-dia__fecha";
        fecha.setAttribute("aria-hidden", "true");
        const spanNumero = document.createElement("span");
        spanNumero.className = "tarjeta-dia__numero";
        spanNumero.textContent = numero;
        const spanMes = document.createElement("span");
        spanMes.className = "tarjeta-dia__mes";
        spanMes.textContent = MESES[mes - 1];
        const spanSemana = document.createElement("span");
        spanSemana.className = "tarjeta-dia__mes";
        spanSemana.textContent = DIAS_SEMANA[diaSemana];
        fecha.append(spanNumero, spanMes, spanSemana);

        const contenido = document.createElement("div");
        contenido.className = "tarjeta-dia__contenido pila";

        if (actividades.length === 0) {
            const vacio = document.createElement("p");
            vacio.className = "tarjeta-dia__vacio";
            vacio.textContent = hayFiltros
                ? "Ninguna actividad de este día cumple los filtros."
                : "Sin actividades registradas para este día.";
            contenido.appendChild(vacio);
        } else {
            const lista = document.createElement("div");
            const idGanadora = dia.actividadMasVotada ? dia.actividadMasVotada.id : null;
            actividades.forEach((actividad) => {
                lista.appendChild(crearFilaActividad(actividad, actividad.id === idGanadora));
            });
            contenido.appendChild(lista);
        }

        // Botón que despliega el formulario para proponer una actividad en este día.
        const formulario = crearFormularioActividad({ fecha: dia.fecha });
        formulario.classList.add("oculto");

        const contenedorBoton = document.createElement("div");
        const btnMostrar = document.createElement("button");
        btnMostrar.type = "button";
        btnMostrar.className = "btn btn-secundario btn-pequeno";
        btnMostrar.textContent = "+ Proponer actividad";
        btnMostrar.dataset.accion = "proponer";
        btnMostrar.setAttribute("aria-expanded", "false");
        btnMostrar.addEventListener("click", () => {
            contenedorBoton.classList.add("oculto");
            btnMostrar.setAttribute("aria-expanded", "true");
            formulario.classList.remove("oculto");
            formulario.elements.titulo.focus();
        });
        contenedorBoton.appendChild(btnMostrar);

        formulario.addEventListener("cancelar", () => {
            formulario.reset();
            limpiarErroresFormulario(formulario);
            formulario.classList.add("oculto");
            contenedorBoton.classList.remove("oculto");
            btnMostrar.setAttribute("aria-expanded", "false");
            btnMostrar.focus();
        });

        contenido.append(contenedorBoton, formulario);
        tarjeta.append(fecha, contenido);
        return tarjeta;
    }

    function crearFilaActividad(actividad, esGanadora) {
        const fila = document.createElement("div");
        fila.className = esGanadora ? "actividad actividad--ganadora" : "actividad";
        fila.dataset.actividadId = actividad.id;

        const hora = document.createElement("span");
        hora.className = "actividad__hora";
        hora.textContent = actividad.hora;

        const info = document.createElement("div");
        info.className = "actividad__info";

        const titulo = document.createElement("p");
        titulo.className = "actividad__titulo";
        titulo.textContent = actividad.titulo;
        if (esGanadora) {
            const insignia = document.createElement("span");
            insignia.className = "insignia insignia-ganadora";
            insignia.textContent = "Más votada";
            titulo.append(" ", insignia);
        }
        info.appendChild(titulo);

        if (actividad.descripcion) {
            const descripcion = document.createElement("p");
            descripcion.className = "actividad__detalle";
            descripcion.textContent = actividad.descripcion;
            info.appendChild(descripcion);
        }

        const autor = document.createElement("p");
        autor.className = "actividad__detalle";
        autor.textContent = `Propuesta por ${actividad.autor?.nombre || "un participante"}`;
        info.appendChild(autor);

        const datos = document.createElement("div");
        datos.className = "fila";

        const costo = document.createElement("strong");
        costo.textContent = formatearMoneda(actividad.costoEstimado, viaje.monedaBase);

        const votos = document.createElement("span");
        votos.className = "insignia";
        votos.dataset.votos = actividad.totalVotos;
        votos.textContent = textoVotos(actividad.totalVotos);

        datos.append(costo, votos);

        // Editar y eliminar solo para el autor de la actividad o el organizador del viaje.
        if (puedeModificar(actividad)) {
            const acciones = document.createElement("div");
            acciones.className = "fila";

            const btnEditar = document.createElement("button");
            btnEditar.type = "button";
            btnEditar.className = "btn btn-texto btn-pequeno";
            btnEditar.textContent = "Editar";
            btnEditar.setAttribute("aria-label", `Editar ${actividad.titulo}`);
            btnEditar.addEventListener("click", () => abrirEdicion(fila, actividad, esGanadora));

            const btnEliminar = document.createElement("button");
            btnEliminar.type = "button";
            btnEliminar.className = "btn btn-texto btn-pequeno";
            btnEliminar.textContent = "Eliminar";
            btnEliminar.setAttribute("aria-label", `Eliminar ${actividad.titulo}`);
            btnEliminar.addEventListener("click", () => eliminarActividad(actividad, btnEliminar));

            acciones.append(btnEditar, btnEliminar);
            info.appendChild(acciones);
        }

        fila.append(hora, info, datos);
        return fila;
    }

    // Cambia la fila de la actividad por su formulario de edición.
    function abrirEdicion(fila, actividad, esGanadora) {
        const formulario = crearFormularioActividad({ actividad });
        formulario.addEventListener("cancelar", () => {
            const filaNueva = crearFilaActividad(actividad, esGanadora);
            formulario.replaceWith(filaNueva);
            filaNueva.querySelector("button")?.focus();
        });
        fila.replaceWith(formulario);
        formulario.elements.titulo.focus();
    }

    /* ---------- Formulario de actividad (proponer y editar) ---------- */

    function crearCampo(sufijo, nombre, etiqueta, entrada, ayuda) {
        const campo = document.createElement("div");
        campo.className = "campo";
        campo.dataset.campo = nombre;

        const label = document.createElement("label");
        label.className = "campo__etiqueta";
        label.htmlFor = `act-${sufijo}-${nombre}`;
        label.textContent = etiqueta;

        entrada.id = `act-${sufijo}-${nombre}`;
        entrada.name = nombre;

        const error = document.createElement("span");
        error.className = "campo__error";
        error.id = `act-${sufijo}-${nombre}-error`;
        entrada.setAttribute("aria-describedby", error.id);

        campo.append(label, entrada);
        if (ayuda) {
            const textoAyuda = document.createElement("span");
            textoAyuda.className = "campo__ayuda";
            textoAyuda.textContent = ayuda;
            campo.appendChild(textoAyuda);
        }
        campo.appendChild(error);
        return campo;
    }

    function crearEntrada(tipo, atributos) {
        const entrada = document.createElement(tipo === "textarea" ? "textarea" : "input");
        if (tipo !== "textarea") entrada.type = tipo;
        Object.entries(atributos).forEach(([clave, valor]) => entrada.setAttribute(clave, valor));
        return entrada;
    }

    // Crea el formulario. Con "fecha" propone en ese día; con "actividad" la edita y permite cambiar el día.
    function crearFormularioActividad({ fecha, actividad }) {
        const editando = Boolean(actividad);
        const sufijo = editando ? `editar-${actividad.id}` : fecha;

        const formulario = document.createElement("form");
        formulario.className = "formulario formulario--ancho";
        formulario.noValidate = true;
        formulario.setAttribute("aria-label", editando ? `Editar ${actividad.titulo}` : `Proponer actividad para el ${formatearFechaLarga(fecha)}`);

        const titulo = crearEntrada("text", { maxlength: "150", placeholder: "Ej. Visita a Monte Albán", required: "" });
        formulario.appendChild(crearCampo(sufijo, "titulo", "Título *", titulo));

        const fila = document.createElement("div");
        fila.className = "formulario__fila";

        if (editando) {
            const dia = crearEntrada("date", { min: viaje.fechaInicio, max: viaje.fechaFin, required: "" });
            fila.appendChild(crearCampo(sufijo, "dia", "Día *", dia));
        }
        const hora = crearEntrada("time", { required: "" });
        fila.appendChild(crearCampo(sufijo, "hora", "Hora *", hora));

        const costo = crearEntrada("number", { min: "0", step: "0.01", inputmode: "decimal", placeholder: "0.00" });
        fila.appendChild(crearCampo(sufijo, "costoEstimado", `Costo estimado (${viaje.monedaBase})`, costo));
        formulario.appendChild(fila);

        const descripcion = crearEntrada("textarea", { maxlength: "500", rows: "2", placeholder: "Detalles opcionales" });
        formulario.appendChild(crearCampo(sufijo, "descripcion", "Descripción", descripcion, "Opcional, máximo 500 caracteres."));

        const acciones = document.createElement("div");
        acciones.className = "fila";
        const btnCancelar = document.createElement("button");
        btnCancelar.type = "button";
        btnCancelar.className = "btn btn-secundario";
        btnCancelar.textContent = "Cancelar";
        const btnGuardar = document.createElement("button");
        btnGuardar.type = "submit";
        btnGuardar.className = "btn btn-primario";
        btnGuardar.textContent = editando ? "Guardar cambios" : "Proponer actividad";
        acciones.append(btnCancelar, btnGuardar);
        formulario.appendChild(acciones);

        if (editando) {
            titulo.value = actividad.titulo;
            formulario.elements.dia.value = actividad.dia;
            hora.value = actividad.hora;
            costo.value = Number(actividad.costoEstimado ?? 0).toFixed(2);
            descripcion.value = actividad.descripcion || "";
        }

        btnCancelar.addEventListener("click", () => formulario.dispatchEvent(new Event("cancelar")));
        formulario.addEventListener("keydown", (evento) => {
            if (evento.key === "Escape") formulario.dispatchEvent(new Event("cancelar"));
        });

        // Al escribir se quita el error del campo.
        formulario.addEventListener("input", (evento) => {
            const campo = evento.target.closest(".campo");
            if (campo) {
                campo.classList.remove("campo--invalido");
                campo.querySelector(".campo__error").textContent = "";
            }
        });

        formulario.addEventListener("submit", (evento) => {
            evento.preventDefault();
            const datos = validarFormulario(formulario, editando ? null : fecha);
            if (!datos) return;
            if (editando) {
                enviarActividad(formulario, btnGuardar, "PUT", `/api/actividades/${actividad.id}`, datos);
            } else {
                enviarActividad(formulario, btnGuardar, "POST", `/api/viajes/${viajeId}/actividades`, datos);
            }
        });

        return formulario;
    }

    function limpiarErroresFormulario(formulario) {
        formulario.querySelectorAll(".campo").forEach((campo) => {
            campo.classList.remove("campo--invalido");
            campo.querySelector(".campo__error").textContent = "";
        });
    }

    // Devuelve true si el error se pudo mostrar junto a su campo.
    function mostrarErrorCampo(formulario, nombre, mensaje) {
        const campo = formulario.querySelector(`.campo[data-campo="${nombre}"]`);
        if (!campo) return false;
        campo.classList.add("campo--invalido");
        campo.querySelector(".campo__error").textContent = mensaje;
        return true;
    }

    // Aplica las mismas reglas que el servidor. Regresa el cuerpo de la petición o null si hay errores.
    function validarFormulario(formulario, fechaFija) {
        limpiarErroresFormulario(formulario);
        const elementos = formulario.elements;
        let esValido = true;

        const titulo = elementos.titulo.value.trim();
        const descripcion = elementos.descripcion.value.trim();
        const dia = fechaFija || elementos.dia.value;
        const hora = elementos.hora.value;
        const costoTexto = elementos.costoEstimado.value.trim();
        const costo = costoTexto === "" ? 0 : Number(costoTexto);

        if (!titulo) {
            mostrarErrorCampo(formulario, "titulo", "El título es obligatorio.");
            esValido = false;
        } else if (titulo.length > 150) {
            mostrarErrorCampo(formulario, "titulo", "El título no puede pasar de 150 caracteres.");
            esValido = false;
        }

        if (!fechaFija) {
            if (!dia) {
                mostrarErrorCampo(formulario, "dia", "Selecciona un día.");
                esValido = false;
            } else if (dia < viaje.fechaInicio || dia > viaje.fechaFin) {
                mostrarErrorCampo(formulario, "dia", "El día debe estar dentro de las fechas del viaje.");
                esValido = false;
            }
        }

        if (!hora) {
            mostrarErrorCampo(formulario, "hora", "La hora es obligatoria.");
            esValido = false;
        }

        if (elementos.costoEstimado.validity.badInput || Number.isNaN(costo) || costo < 0) {
            mostrarErrorCampo(formulario, "costoEstimado", "El costo debe ser un número mayor o igual a 0.");
            esValido = false;
        }

        if (descripcion.length > 500) {
            mostrarErrorCampo(formulario, "descripcion", "La descripción no puede pasar de 500 caracteres.");
            esValido = false;
        }

        if (!esValido) {
            formulario.querySelector(".campo--invalido input, .campo--invalido textarea")?.focus();
            return null;
        }

        return {
            titulo,
            descripcion: descripcion || null,
            dia,
            hora: hora.slice(0, 5),
            costoEstimado: costo
        };
    }

    // Envía el alta (POST) o la edición (PUT) y, si sale bien, recarga el itinerario.
    async function enviarActividad(formulario, boton, metodo, url, datos) {
        const textoOriginal = boton.textContent;
        boton.disabled = true;
        boton.textContent = "Guardando...";

        try {
            const respuesta = await pedir(url, {
                method: metodo,
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(datos)
            });
            if (!respuesta) return;

            if (respuesta.ok) {
                mostrarMensaje("exito", metodo === "POST" ? "Actividad propuesta." : "Actividad actualizada.");
                await recargarDetalle();
                return;
            }

            const errorData = await leerError(respuesta);
            if (respuesta.status === 400) {
                const errores = Array.isArray(errorData?.errores) ? errorData.errores : [];
                const sinCampo = errores.filter((error) => !mostrarErrorCampo(formulario, error.campo, error.mensaje));
                if (errores.length === 0 || sinCampo.length > 0) {
                    mostrarMensaje("error", sinCampo[0]?.mensaje || errorData?.mensaje || "Revisa los datos de la actividad.");
                }
            } else if (respuesta.status === 403) {
                mostrarMensaje("error", errorData?.mensaje || "No tienes permiso para modificar esta actividad.");
            } else if (respuesta.status === 404) {
                mostrarMensaje("error", errorData?.mensaje || "La actividad o el viaje ya no existen.");
                await recargarDetalle();
            } else {
                mostrarMensaje("error", "Ocurrió un error inesperado al guardar la actividad. Intenta más tarde.");
            }
        } catch (error) {
            mostrarMensaje("error", "No se pudo conectar con el servidor. Verifica tu conexión.");
        } finally {
            if (boton.isConnected) {
                boton.disabled = false;
                boton.textContent = textoOriginal;
            }
        }
    }

    /* ---------- Eliminar ---------- */

    // Ventana de confirmación con el componente .modal. Resuelve true si la persona confirma.
    function confirmar(titulo, texto, textoAceptar) {
        return new Promise((resolver) => {
            const elementoPrevio = document.activeElement;

            const fondo = document.createElement("div");
            fondo.className = "modal-fondo";

            const modal = document.createElement("div");
            modal.className = "modal";
            modal.setAttribute("role", "alertdialog");
            modal.setAttribute("aria-modal", "true");
            modal.setAttribute("aria-labelledby", "tituloConfirmacion");
            modal.setAttribute("aria-describedby", "textoConfirmacion");

            const cabecera = document.createElement("div");
            cabecera.className = "modal__cabecera";
            const encabezado = document.createElement("h2");
            encabezado.id = "tituloConfirmacion";
            encabezado.textContent = titulo;
            cabecera.appendChild(encabezado);

            const parrafo = document.createElement("p");
            parrafo.id = "textoConfirmacion";
            parrafo.textContent = texto;

            const acciones = document.createElement("div");
            acciones.className = "fila";
            const btnCancelar = document.createElement("button");
            btnCancelar.type = "button";
            btnCancelar.className = "btn btn-secundario";
            btnCancelar.textContent = "Cancelar";
            const btnAceptar = document.createElement("button");
            btnAceptar.type = "button";
            btnAceptar.className = "btn btn-peligro";
            btnAceptar.textContent = textoAceptar;
            acciones.append(btnCancelar, btnAceptar);

            modal.append(cabecera, parrafo, acciones);
            fondo.appendChild(modal);
            document.body.appendChild(fondo);
            btnCancelar.focus();

            function cerrar(resultado) {
                document.removeEventListener("keydown", alPresionarTecla);
                fondo.remove();
                if (elementoPrevio && elementoPrevio.isConnected) elementoPrevio.focus();
                resolver(resultado);
            }

            // Escape cierra y Tab no sale de la ventana.
            function alPresionarTecla(evento) {
                if (evento.key === "Escape") {
                    cerrar(false);
                } else if (evento.key === "Tab") {
                    evento.preventDefault();
                    (document.activeElement === btnCancelar ? btnAceptar : btnCancelar).focus();
                }
            }

            document.addEventListener("keydown", alPresionarTecla);
            btnCancelar.addEventListener("click", () => cerrar(false));
            btnAceptar.addEventListener("click", () => cerrar(true));
            fondo.addEventListener("click", (evento) => {
                if (evento.target === fondo) cerrar(false);
            });
        });
    }

    async function eliminarActividad(actividad, boton) {
        const confirmado = await confirmar(
            "Eliminar actividad",
            `¿Seguro que quieres eliminar "${actividad.titulo}"? También se borrarán sus votos.`,
            "Eliminar"
        );
        if (!confirmado) return;

        boton.disabled = true;
        try {
            const respuesta = await pedir(`/api/actividades/${actividad.id}`, { method: "DELETE" });
            if (!respuesta) return;

            if (respuesta.status === 204) {
                mostrarMensaje("exito", "Actividad eliminada.");
                await recargarDetalle();
                return;
            }

            const errorData = await leerError(respuesta);
            if (respuesta.status === 403) {
                mostrarMensaje("error", errorData?.mensaje || "No tienes permiso para eliminar esta actividad.");
            } else if (respuesta.status === 404) {
                mostrarMensaje("aviso", "La actividad ya había sido eliminada.");
                await recargarDetalle();
            } else {
                mostrarMensaje("error", "Ocurrió un error inesperado al eliminar la actividad. Intenta más tarde.");
            }
        } catch (error) {
            mostrarMensaje("error", "No se pudo conectar con el servidor. Verifica tu conexión.");
        } finally {
            if (boton.isConnected) boton.disabled = false;
        }
    }

    /* ---------- Exportar PDF ---------- */

    function nombreDesdeEncabezado(encabezado) {
        if (!encabezado) return null;
        const coincidencia = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(encabezado);
        return coincidencia ? decodeURIComponent(coincidencia[1]) : null;
    }

    // Descarga GET /api/viajes/{id}/exportar-pdf como archivo, mostrando los errores si los hay.
    async function exportarPdf() {
        const textoOriginal = btnExportarPdf.textContent;
        btnExportarPdf.disabled = true;
        btnExportarPdf.textContent = "Generando PDF...";

        try {
            const respuesta = await pedir(`/api/viajes/${viajeId}/exportar-pdf`, {
                headers: { "Accept": "application/pdf, application/json" }
            });
            if (!respuesta) return;

            if (!respuesta.ok) {
                const errorData = await leerError(respuesta);
                if (respuesta.status === 404) {
                    mostrarMensaje("error", errorData?.mensaje || "El viaje no existe o fue cancelado.");
                } else if (respuesta.status === 403) {
                    mostrarMensaje("error", errorData?.mensaje || "No participas en este viaje.");
                } else {
                    mostrarMensaje("error", "No se pudo generar el PDF. Intenta más tarde.");
                }
                return;
            }

            const archivo = await respuesta.blob();
            const nombre = nombreDesdeEncabezado(respuesta.headers.get("Content-Disposition")) || `itinerario-${viajeId}.pdf`;
            const url = URL.createObjectURL(archivo);
            const enlace = document.createElement("a");
            enlace.href = url;
            enlace.download = nombre;
            document.body.appendChild(enlace);
            enlace.click();
            enlace.remove();
            setTimeout(() => URL.revokeObjectURL(url), 1000);
            mostrarMensaje("exito", "Se descargó el itinerario en PDF.");
        } catch (error) {
            mostrarMensaje("error", "No se pudo conectar con el servidor. Verifica tu conexión.");
        } finally {
            btnExportarPdf.disabled = false;
            btnExportarPdf.textContent = textoOriginal;
        }
    }

    /* ---------- Carga ---------- */

    // Vuelve a pedir el detalle (para refrescar la más votada y los totales) y reaplica los filtros.
    async function recargarDetalle() {
        try {
            const respuesta = await pedir(`/api/viajes/${viajeId}`);
            if (!respuesta) return;
            if (!respuesta.ok) {
                const errorData = await leerError(respuesta);
                mostrarEstadoError(respuesta.status, errorData?.mensaje);
                return;
            }
            viaje = await respuesta.json();
            renderizarEncabezado();
            renderizarDias();
            if (actividadesFiltradas !== null) {
                await aplicarFiltros();
            }
        } catch (error) {
            mostrarMensaje("error", "No se pudo actualizar el itinerario. Verifica tu conexión.");
        }
    }

    async function cargarPagina() {
        try {
            const [respuestaViaje, respuestaSesion] = await Promise.all([
                pedir(`/api/viajes/${viajeId}`),
                pedir("/api/auth/sesion")
            ]);
            if (!respuestaViaje || !respuestaSesion) return;

            if (!respuestaViaje.ok) {
                const errorData = await leerError(respuestaViaje);
                mostrarEstadoError(respuestaViaje.status, errorData?.mensaje);
                return;
            }

            viaje = await respuestaViaje.json();
            if (respuestaSesion.ok) {
                const sesion = await respuestaSesion.json();
                usuarioActualId = sesion.id;
            }

            renderizarEncabezado();
            prepararFiltroDias();
            renderizarDias();

            estadoCarga.classList.add("oculto");
            contenidoViaje.classList.remove("oculto");
        } catch (error) {
            mostrarEstadoError(0);
        }
    }

    /* ---------- Eventos ---------- */

    filtroDia.addEventListener("change", () => {
        clearTimeout(temporizadorFiltro);
        aplicarFiltros();
    });

    filtroCostoMax.addEventListener("input", () => {
        clearTimeout(temporizadorFiltro);
        temporizadorFiltro = setTimeout(() => {
            aplicarFiltros();
        }, 300);
    });

    btnLimpiarFiltros.addEventListener("click", () => {
        clearTimeout(temporizadorFiltro);
        filtroDia.value = "";
        filtroCostoMax.value = "";
        aplicarFiltros();
    });

    // Abre el formulario del primer día visible.
    btnProponer.addEventListener("click", () => {
        const boton = listaDias.querySelector("[data-accion='proponer']");
        if (!boton) return;
        boton.closest(".tarjeta-dia").scrollIntoView({ behavior: "smooth", block: "start" });
        boton.click();
    });

    btnExportarPdf.addEventListener("click", exportarPdf);

    cargarPagina();
});
