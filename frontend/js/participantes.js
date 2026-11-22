// Gestión de participantes del viaje (HU-18, frontend).
// Archivo nuevo: no modifica viaje-detalle.js (de Diego) ni ningún
// otro módulo. Se carga con una línea <script> en viajes/detalle.html.
// Usa los endpoints que ya existen: GET, POST y DELETE
// /api/viajes/{id}/participantes. Solo la organizadora ve el formulario
// para agregar y los botones para quitar: el backend responde 403 a
// cualquier otro participante, y 400 si la organizadora intenta
// quitarse a sí misma. El correo sin cuenta responde 404 y si la
// persona ya participa responde 400; ambos casos los muestra
// mensajes.js sin perder lo capturado.
document.addEventListener("DOMContentLoaded", () => {
    const pagina = document.getElementById("paginaDetalle");
    if (!pagina) return; // Solo corre en la página de detalle del viaje.
    const viajeId = pagina.dataset.viajeId;
    if (!viajeId) return;

    const listaGrupo = document.getElementById("listaGrupo");
    const textoParticipantes = document.getElementById("textoParticipantes");

    let participantes = [];
    let miUsuarioId = null;
    let soyOrganizadora = false;

    async function pedir(url, opciones = {}) {
        return fetch(url, {
            ...opciones,
            credentials: "same-origin",
            headers: { "Accept": "application/json", ...(opciones.headers || {}) }
        });
    }

    async function cargarParticipantes() {
        try {
            const respuestaSesion = await pedir("/api/auth/sesion");
            if (!respuestaSesion.ok) {
                await manejarErrorApi(respuestaSesion);
                return;
            }
            const sesion = await respuestaSesion.json();
            miUsuarioId = sesion.id;

            const respuestaLista = await pedir(`/api/viajes/${viajeId}/participantes`);
            if (!respuestaLista.ok) {
                await manejarErrorApi(respuestaLista);
                return;
            }
            participantes = await respuestaLista.json();

            soyOrganizadora = false;
            for (let i = 0; i < participantes.length; i++) {
                if (participantes[i].usuarioId === miUsuarioId) {
                    if (String(participantes[i].rol).toUpperCase() === "ORGANIZADOR") {
                        soyOrganizadora = true;
                    }
                }
            }
            renderizarParticipantes();
        } catch (error) {
            mostrarErrorRed();
        }
    }

    // Mantiene el encabezado (chips y contador) igual que lo pinta
    // viaje-detalle.js, para que no quede desactualizado al agregar
    // o quitar a alguien sin recargar.
    function sincronizarEncabezado() {
        if (!listaGrupo || !textoParticipantes) return;
        const total = participantes.length;
        textoParticipantes.textContent = `${total} ${total === 1 ? "participante" : "participantes"}`;
        listaGrupo.innerHTML = "";
        participantes.forEach((participante) => {
            const elemento = document.createElement("span");
            elemento.setAttribute("role", "listitem");
            const organizador = String(participante.rol).toUpperCase() === "ORGANIZADOR";
            elemento.className = organizador ? "insignia insignia-organizador" : "insignia";
            elemento.textContent = organizador ? `${participante.nombre} · Organizador` : participante.nombre;
            listaGrupo.appendChild(elemento);
        });
    }

    function renderizarParticipantes() {
        let seccion = document.getElementById("zona-participantes");
        if (!seccion) {
            seccion = document.createElement("section");
            seccion.id = "zona-participantes";
            seccion.className = "tarjeta pila";
            seccion.setAttribute("aria-label", "Participantes del viaje");
            // Va justo debajo del encabezado del viaje, donde está la
            // lista de participantes que ya pinta viaje-detalle.js.
            const encabezado = listaGrupo ? listaGrupo.closest("section") : null;
            if (encabezado && encabezado.parentNode) {
                encabezado.parentNode.insertBefore(seccion, encabezado.nextSibling);
            } else {
                pagina.appendChild(seccion);
            }
        }
        seccion.innerHTML = "";

        const titulo = document.createElement("h2");
        titulo.className = "tarjeta__titulo";
        titulo.style.margin = "0";
        titulo.textContent = "Participantes";
        seccion.appendChild(titulo);

        const lista = document.createElement("div");
        lista.className = "pila";
        participantes.forEach((participante) => {
            const fila = document.createElement("div");
            fila.className = "fila fila--separada";

            const nombre = document.createElement("span");
            const organizador = String(participante.rol).toUpperCase() === "ORGANIZADOR";
            nombre.textContent = organizador
                ? `${participante.nombre} (${participante.correo}) · Organizador`
                : `${participante.nombre} (${participante.correo})`;
            fila.appendChild(nombre);

            // La organizadora puede quitar a cualquiera menos a sí misma.
            if (soyOrganizadora && participante.usuarioId !== miUsuarioId) {
                const btnQuitar = document.createElement("button");
                btnQuitar.type = "button";
                btnQuitar.className = "btn btn-texto btn-pequeno";
                btnQuitar.textContent = "Quitar";
                btnQuitar.setAttribute("aria-label", `Quitar a ${participante.nombre} del viaje`);
                btnQuitar.addEventListener("click", () => quitarParticipante(participante, btnQuitar));
                fila.appendChild(btnQuitar);
            }
            lista.appendChild(fila);
        });
        seccion.appendChild(lista);

        if (!soyOrganizadora) return;

        const formulario = document.createElement("form");
        formulario.className = "formulario__fila";
        formulario.setAttribute("novalidate", "");

        const inputCorreo = document.createElement("input");
        inputCorreo.type = "email";
        inputCorreo.placeholder = "Correo de la persona a agregar";
        inputCorreo.setAttribute("aria-label", "Correo de la persona a agregar");
        inputCorreo.required = true;

        const btnAgregar = document.createElement("button");
        btnAgregar.type = "submit";
        btnAgregar.className = "btn btn-primario";
        btnAgregar.textContent = "Agregar participante";

        formulario.appendChild(inputCorreo);
        formulario.appendChild(btnAgregar);
        formulario.addEventListener("submit", async (evento) => {
            evento.preventDefault();
            const correo = inputCorreo.value.trim();
            if (!correo || !correo.includes("@")) {
                mostrarMensaje("error", "Escribe un correo válido.");
                return;
            }
            btnAgregar.disabled = true;
            try {
                const respuesta = await pedir(`/api/viajes/${viajeId}/participantes`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ correo })
                });
                if (!respuesta.ok) {
                    await manejarErrorApi(respuesta);
                    return;
                }
                const agregado = await respuesta.json();
                inputCorreo.value = "";
                mostrarMensaje("exito", `${agregado.nombre} se agregó al viaje.`);
                await cargarParticipantes();
                sincronizarEncabezado();
            } catch (error) {
                mostrarErrorRed();
            } finally {
                btnAgregar.disabled = false;
            }
        });
        seccion.appendChild(formulario);
    }

    async function quitarParticipante(participante, boton) {
        if (!window.confirm(`¿Quitar a ${participante.nombre} del viaje? Dejará de poder votar, registrar gastos y ver el viaje.`)) {
            return;
        }
        boton.disabled = true;
        try {
            const respuesta = await pedir(`/api/viajes/${viajeId}/participantes/${participante.usuarioId}`, {
                method: "DELETE"
            });
            if (!respuesta.ok) {
                await manejarErrorApi(respuesta);
                return;
            }
            mostrarMensaje("exito", `${participante.nombre} salió del viaje.`);
            await cargarParticipantes();
            sincronizarEncabezado();
        } catch (error) {
            mostrarErrorRed();
        } finally {
            boton.disabled = false;
        }
    }

    cargarParticipantes();
});
