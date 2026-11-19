document.addEventListener("DOMContentLoaded", () => {
    const formulario = document.getElementById("formNuevoViaje");
    const btnGuardar = document.getElementById("btnGuardarViaje");
    const contenedorMensajes = document.getElementById("contenedorMensajes");

    const inputDestino = document.getElementById("destino");
    const inputFechaInicio = document.getElementById("fechaInicio");
    const inputFechaFin = document.getElementById("fechaFin");
    const inputPresupuesto = document.getElementById("presupuestoTotal");
    const selectMoneda = document.getElementById("monedaBase");

    const campos = ["destino", "fechaInicio", "fechaFin", "presupuestoTotal", "monedaBase"];

    function limpiarErrores() {
        if (contenedorMensajes) {
            contenedorMensajes.innerHTML = "";
        }

        campos.forEach((campo) => {
            const spanError = document.getElementById(`error-${campo}`);
            const grupo = document.getElementById(`grupo-${campo}`);
            if (spanError) {
                spanError.textContent = "";
            }
            if (grupo) {
                grupo.classList.remove("campo--invalido");
            }
        });
    }

    function mostrarErrorCampo(campo, mensaje) {
        const spanError = document.getElementById(`error-${campo}`);
        const grupo = document.getElementById(`grupo-${campo}`);
        if (spanError) {
            spanError.textContent = mensaje;
        }
        if (grupo) {
            grupo.classList.add("campo--invalido");
        }
        if (!spanError) {
            mostrarMensajeGeneral(mensaje);
        }
    }

    function mostrarMensajeGeneral(mensaje) {
        if (!contenedorMensajes) return;
        const alerta = document.createElement("div");
        alerta.className = "mensaje mensaje-error";
        alerta.setAttribute("role", "alert");
        alerta.textContent = mensaje;
        contenedorMensajes.appendChild(alerta);
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
        btnGuardar.textContent = "Creando viaje...";

        try {
            const respuesta = await fetch("/api/viajes", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (respuesta.status === 401) {
                window.location.href = "/login";
                return;
            }

            if (respuesta.status === 201) {
                const viajeCreado = await respuesta.json();
                window.location.href = `/viajes/${viajeCreado.id}`;
                return;
            }

            if (respuesta.status === 400) {
                const errorData = await respuesta.json().catch(() => null);
                if (errorData) {
                    if (Array.isArray(errorData.errores) && errorData.errores.length > 0) {
                        errorData.errores.forEach((err) => {
                            mostrarErrorCampo(err.campo, err.mensaje);
                        });
                    } else if (errorData.campo && errorData.mensaje) {
                        mostrarErrorCampo(errorData.campo, errorData.mensaje);
                    } else if (errorData.mensaje) {
                        mostrarMensajeGeneral(errorData.mensaje);
                    } else {
                        mostrarMensajeGeneral("Revisa los datos capturados e inténtalo de nuevo.");
                    }
                } else {
                    mostrarMensajeGeneral("Datos inválidos. Verifica los campos del formulario.");
                }
                return;
            }

            mostrarMensajeGeneral("Ocurrió un error inesperado al crear el viaje. Intenta más tarde.");
        } catch (error) {
            mostrarMensajeGeneral("No se pudo conectar con el servidor. Verifica tu conexión.");
        } finally {
            btnGuardar.disabled = false;
            btnGuardar.textContent = "Crear viaje";
        }
    });
});