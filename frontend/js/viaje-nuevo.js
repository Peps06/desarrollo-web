// Formulario de crear y editar viaje (HU-27 + HU-23, frontend).
// Basado en frontend/js/viaje-nuevo.js del ZIP desarrollo-web_0_yj7j.zip
// (cabeza 96f7c78, tag V4, 20 nov 2026). Cambios respecto a esa versión:
// - Modo edición: si la página es /viajes/{id}/editar, el formulario carga
//   los datos del viaje con GET /api/viajes/{id}, cambia el título a
//   "Editar viaje" y el botón a "Guardar cambios", y al enviar usa
//   PUT /api/viajes/{id} en lugar de POST /api/viajes.
// - La ruta /viajes/{id}/editar vive en PaginaController.java (incluido
//   en esta misma carpeta) y devuelve la misma plantilla viajes/nuevo.
// - El botón Editar solo lo ve la organizadora (en viajes-lista.js): el
//   backend responde 403 a cualquier otro participante.
// - Si se acortan las fechas y hay actividades fuera del nuevo rango, el
//   backend responde 400 y el mensaje se muestra sin perder lo capturado.
document.addEventListener("DOMContentLoaded", () => {
    const formulario = document.getElementById("formNuevoViaje");
    const btnGuardar = document.getElementById("btnGuardarViaje");
    const titulo = document.querySelector("h1.tarjeta__titulo");

    const inputDestino = document.getElementById("destino");
    const inputFechaInicio = document.getElementById("fechaInicio");
    const inputFechaFin = document.getElementById("fechaFin");
    const inputPresupuesto = document.getElementById("presupuestoTotal");
    const selectMoneda = document.getElementById("monedaBase");

    const campos = ["destino", "fechaInicio", "fechaFin", "presupuestoTotal", "monedaBase"];

    // Modo edición si la ruta es /viajes/{id}/editar; si no, es creación.
    // La ruta se parte por "/" y queda ["", "viajes", "{id}", "editar"].
    const partesRuta = window.location.pathname.split("/");
    const esEdicion = partesRuta.length === 4 && partesRuta[1] === "viajes" && partesRuta[3] === "editar";
    let viajeId = null;
    if (esEdicion) {
        viajeId = partesRuta[2];
    }
    const textoBoton = esEdicion ? "Guardar cambios" : "Crear viaje";

    function limpiarErrores() {
        campos.forEach((campo) => {
            const spanError = document.getElementById(`error-${campo}`);
            const grupo = document.getElementById(`grupo-${campo}`);
            if (spanError) spanError.textContent = "";
            if (grupo) grupo.classList.remove("campo--invalido");
        });
    }

    function mostrarErrorCampo(campo, mensaje) {
        const spanError = document.getElementById(`error-${campo}`);
        const grupo = document.getElementById(`grupo-${campo}`);
        if (spanError) spanError.textContent = mensaje;
        if (grupo) grupo.classList.add("campo--invalido");
    }

    function validarFormulario() {
        limpiarErrores();
        let esValido = true;

        const destino = inputDestino.value.trim();
        const fechaInicio = inputFechaInicio.value;
        const fechaFin = inputFechaFin.value;
        const presupuesto = parseFloat(inputPresupuesto.value);
        const moneda = selectMoneda.value;

        if (!destino) {
            mostrarErrorCampo("destino", "El destino es obligatorio.");
            esValido = false;
        } else if (destino.length > 150) {
            mostrarErrorCampo("destino", "El destino no puede exceder los 150 caracteres.");
            esValido = false;
        }

        if (!fechaInicio) {
            mostrarErrorCampo("fechaInicio", "Selecciona una fecha de inicio.");
            esValido = false;
        }

        if (!fechaFin) {
            mostrarErrorCampo("fechaFin", "Selecciona una fecha de fin.");
            esValido = false;
        }

        if (fechaInicio && fechaFin && fechaFin < fechaInicio) {
            mostrarErrorCampo("fechaFin", "La fecha de fin debe ser igual o posterior a la fecha de inicio.");
            esValido = false;
        }

        if (Number.isNaN(presupuesto) || presupuesto <= 0) {
            mostrarErrorCampo("presupuestoTotal", "El presupuesto total debe ser mayor a 0.");
            esValido = false;
        }

        if (!["MXN", "USD", "EUR"].includes(moneda)) {
            mostrarErrorCampo("monedaBase", "Selecciona una moneda válida (MXN, USD o EUR).");
            esValido = false;
        }

        if (!esValido) {
            mostrarMensaje("error", "Revisa los datos");
        }

        return esValido;
    }

    campos.forEach((campo) => {
        const elemento = document.getElementById(campo);
        if (elemento) {
            elemento.addEventListener("input", () => {
                const spanError = document.getElementById(`error-${campo}`);
                const grupo = document.getElementById(`grupo-${campo}`);
                if (spanError) spanError.textContent = "";
                if (grupo) grupo.classList.remove("campo--invalido");
            });
        }
    });

    // En modo edición, precarga los datos del viaje. Si no se puede
    // cargar (404 o 403), manejarErrorApi ya muestra el mensaje.
    async function cargarViajeParaEditar() {
        if (!esEdicion) return;
        if (titulo) titulo.textContent = "Editar viaje";
        btnGuardar.textContent = textoBoton;
        try {
            const respuesta = await fetch(`/api/viajes/${viajeId}`, {
                method: "GET",
                headers: { "Accept": "application/json" }
            });
            if (!respuesta.ok) {
                await manejarErrorApi(respuesta);
                return;
            }
            const viaje = await respuesta.json();
            inputDestino.value = viaje.destino || "";
            inputFechaInicio.value = viaje.fechaInicio || "";
            inputFechaFin.value = viaje.fechaFin || "";
            if (viaje.presupuestoTotal === null || viaje.presupuestoTotal === undefined) {
                inputPresupuesto.value = "";
            } else {
                inputPresupuesto.value = viaje.presupuestoTotal;
            }
            if (viaje.monedaBase) selectMoneda.value = viaje.monedaBase;
        } catch (error) {
            mostrarErrorRed();
        }
    }

    formulario.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        if (!validarFormulario()) {
            return;
        }

        const payload = {
            destino: inputDestino.value.trim(),
            fechaInicio: inputFechaInicio.value,
            fechaFin: inputFechaFin.value,
            presupuestoTotal: parseFloat(inputPresupuesto.value),
            monedaBase: selectMoneda.value
        };

        btnGuardar.disabled = true;
        btnGuardar.textContent = esEdicion ? "Guardando cambios..." : "Creando viaje...";

        try {
            const respuesta = await fetch(esEdicion ? `/api/viajes/${viajeId}` : "/api/viajes", {
                method: esEdicion ? "PUT" : "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (respuesta.ok) {
                const viajeGuardado = await respuesta.json();
                mostrarMensaje("exito", esEdicion ? "Viaje actualizado correctamente" : "Viaje creado correctamente");
                setTimeout(() => {
                    window.location.href = `/viajes/${viajeGuardado.id}`;
                }, 700);
                return;
            }

            const errorData = await manejarErrorApi(respuesta);
            if (respuesta.status === 400 && errorData && Array.isArray(errorData.errores)) {
                errorData.errores.forEach((err) => {
                    mostrarErrorCampo(err.campo, err.mensaje);
                });
            }
        } catch (error) {
            mostrarErrorRed();
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.textContent = textoBoton;
        }
    });

    cargarViajeParaEditar();
});
