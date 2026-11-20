document.addEventListener("DOMContentLoaded", () => {
    const zonaChecklist = document.getElementById("zona-checklist");
    const paginaDetalle = document.getElementById("paginaDetalle");

    if (!zonaChecklist || !paginaDetalle) return;

    const viajeId = paginaDetalle.dataset.viajeId;
    if (!viajeId) return;

    let tareas = [];

    zonaChecklist.innerHTML = `
        <div class="tarjeta pila">
            <div class="fila fila--separada">
                <h2 class="tarjeta__titulo" style="margin: 0;">Pendientes del viaje</h2>
                <span id="contadorTareas" class="contador-tareas" aria-live="polite">0 de 0 completadas</span>
            </div>

            <form id="formNuevaTarea" class="fila" novalidate>
                <label for="inputDescripcionTarea" class="oculto">Nueva tarea</label>
                <input
                    type="text"
                    id="inputDescripcionTarea"
                    maxlength="255"
                    placeholder="Agregar tarea (ej. Llevar repelente)"
                    style="flex: 1; min-height: 40px; padding: 0.5rem 0.75rem; border: 1px solid var(--azul-grisaceo); border-radius: var(--radio-md);"
                    required
                >
                <button type="submit" id="btnAgregarTarea" class="btn btn-primario btn-pequeno">Agregar</button>
            </form>

            <p id="estadoVacioTareas" class="tarjeta-dia__vacio oculto">
                Aún no hay pendientes registrados para este viaje.
            </p>
            <ul id="listaChecklist" class="checklist"></ul>
        </div>
    `;

    const contadorTareas = document.getElementById("contadorTareas");
    const formNuevaTarea = document.getElementById("formNuevaTarea");
    const inputDescripcion = document.getElementById("inputDescripcionTarea");
    const btnAgregarTarea = document.getElementById("btnAgregarTarea");
    const estadoVacioTareas = document.getElementById("estadoVacioTareas");
    const listaChecklist = document.getElementById("listaChecklist");

    function actualizarContador() {
        const total = tareas.length;
        const completadas = tareas.filter((t) => t.completada).length;
        contadorTareas.textContent = `${completadas} de ${total} completadas`;
    }

    function crearElementoTarea(tarea) {
        const item = document.createElement("li");
        item.className = tarea.completada ? "tarea completada" : "tarea";
        item.dataset.tareaId = tarea.id;

        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = Boolean(tarea.completada);
        checkbox.setAttribute("aria-label", `Marcar "${tarea.descripcion}" como completada`);

        const texto = document.createElement("span");
        texto.className = "tarea__texto";
        texto.textContent = tarea.descripcion;

        const btnEliminar = document.createElement("button");
        btnEliminar.type = "button";
        btnEliminar.className = "btn btn-texto btn-pequeno";
        btnEliminar.textContent = "Eliminar";
        btnEliminar.setAttribute("aria-label", `Eliminar "${tarea.descripcion}"`);

        checkbox.addEventListener("change", async () => {
            const estadoAnterior = tarea.completada;
            const nuevoEstado = checkbox.checked;

            // Actualización optimista inmediata
            tarea.completada = nuevoEstado;
            item.classList.toggle("completada", nuevoEstado);
            actualizarContador();
            checkbox.disabled = true;

            try {
                const respuesta = await fetch(`/api/tareas/${tarea.id}/toggle`, {
                    method: "PATCH",
                    headers: { "Accept": "application/json" }
                });

                if (!respuesta.ok) {
                    // Revertir si el servidor rechaza la operación
                    tarea.completada = estadoAnterior;
                    checkbox.checked = estadoAnterior;
                    item.classList.toggle("completada", estadoAnterior);
                    actualizarContador();
                    await manejarErrorApi(respuesta);
                    return;
                }

                const actualizada = await respuesta.json();
                tarea.completada = Boolean(actualizada.completada);
                checkbox.checked = tarea.completada;
                item.classList.toggle("completada", tarea.completada);
                actualizarContador();
                mostrarMensaje("exito", tarea.completada ? "Tarea completada" : "Tarea marcada como pendiente");
            } catch (error) {
                tarea.completada = estadoAnterior;
                checkbox.checked = estadoAnterior;
                item.classList.toggle("completada", estadoAnterior);
                actualizarContador();
                mostrarErrorRed();
            } finally {
                checkbox.disabled = false;
            }
        });

        btnEliminar.addEventListener("click", async () => {
            btnEliminar.disabled = true;
            try {
                const respuesta = await fetch(`/api/tareas/${tarea.id}`, {
                    method: "DELETE"
                });

                if (!respuesta.ok) {
                    await manejarErrorApi(respuesta);
                    btnEliminar.disabled = false;
                    return;
                }

                tareas = tareas.filter((t) => t.id !== tarea.id);
                renderizarLista();
                mostrarMensaje("exito", "Tarea eliminada");
            } catch (error) {
                mostrarErrorRed();
                btnEliminar.disabled = false;
            }
        });

        item.appendChild(checkbox);
        item.appendChild(texto);
        item.appendChild(btnEliminar);
        return item;
    }

    function renderizarLista() {
        listaChecklist.innerHTML = "";
        actualizarContador();

        if (tareas.length === 0) {
            estadoVacioTareas.classList.remove("oculto");
            return;
        }

        estadoVacioTareas.classList.add("oculto");
        tareas.forEach((tarea) => {
            listaChecklist.appendChild(crearElementoTarea(tarea));
        });
    }

    async function cargarTareas() {
        try {
            const respuesta = await fetch(`/api/viajes/${viajeId}/tareas`, {
                method: "GET",
                headers: { "Accept": "application/json" }
            });

            if (!respuesta.ok) {
                await manejarErrorApi(respuesta);
                return;
            }

            tareas = await respuesta.json();
            renderizarLista();
        } catch (error) {
            mostrarErrorRed();
        }
    }

    formNuevaTarea.addEventListener("submit", async (evento) => {
        evento.preventDefault();

        const descripcion = inputDescripcion.value.trim();
        if (!descripcion) {
            mostrarMensaje("error", "Escribe la descripción de la tarea");
            inputDescripcion.focus();
            return;
        }

        btnAgregarTarea.disabled = true;
        try {
            const respuesta = await fetch(`/api/viajes/${viajeId}/tareas`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify({ descripcion })
            });

            if (respuesta.status === 201) {
                const creada = await respuesta.json();
                tareas.push(creada);
                inputDescripcion.value = "";
                renderizarLista();
                mostrarMensaje("exito", "Tarea agregada");
                return;
            }

            await manejarErrorApi(respuesta);
        } catch (error) {
            mostrarErrorRed();
        } finally {
            btnAgregarTarea.disabled = false;
        }
    });

    cargarTareas();
});