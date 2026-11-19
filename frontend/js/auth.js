/*
 * auth.js
 * Maneja el login, el registro y el logout.
 * Habla con la API del mismo servidor usando fetch().
 */

document.addEventListener("DOMContentLoaded", function () {
    const formLogin = document.getElementById("formLogin");
    const formRegistro = document.getElementById("formRegistro");

    if (formLogin) formLogin.addEventListener("submit", iniciarSesion);
    if (formRegistro) formRegistro.addEventListener("submit", registrar);
});

/**
 * Muestra un mensaje general arriba de la página.
 *
 * @param {string} texto 
 */
function mostrarMensajeGeneral(texto) {
    const contenedor = document.getElementById("contenedorMensajes");
    if (!contenedor) return;
    contenedor.innerHTML = "";
    const aviso = document.createElement("div");
    aviso.className = "mensaje mensaje-error";
    aviso.setAttribute("role", "alert");
    aviso.textContent = texto;
    contenedor.appendChild(aviso);
}

/**
 * Lee el error que mandó el servidor y lo muestra.
 * @param {Response} respuesta 
 * @param {string} mensajePorDefecto 
 * @returns {Promise} 
 */
function mostrarErrorServidor(respuesta, mensajePorDefecto) {
    return respuesta.json().catch(function () { return null; }).then(function (datos) {

        // El servidor manda una lista de errores por campo
        if (datos && Array.isArray(datos.errores) && datos.errores.length > 0) {
            datos.errores.forEach(function (e) {
                mostrarErrorCampo(e.campo, e.mensaje);
            });
        } else {
            // El servidor manda un mensaje general
            mostrarMensajeGeneral((datos && datos.mensaje) || mensajePorDefecto);
        }
    });
}

/**
 * Revisa los datos, los manda al servidor y, si todo sale bien,
 * lleva al usuario a su lista de viajes.
 * @param {Event} evento 
 */
function iniciarSesion(evento) {
    // Evita que la página se recargue sola
    evento.preventDefault();
    limpiarErrores();
    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;
    const errores = validarLogin(correo, contrasena);
    if (errores.length > 0) {
        errores.forEach(function (e) { mostrarErrorCampo(e.campo, e.mensaje); });
        return;
    }

    // Envía los datos al servidor
    fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ correo: correo, contrasena: contrasena })
    })
        .then(function (respuesta) {
            // Si entró bien lleva a la lista de viajes
            if (respuesta.ok) {
                window.location.href = "/viajes";
                return;
            }
            // Si no entró, muestra el error del servidor
            return mostrarErrorServidor(respuesta, "Correo o contraseña incorrectos");
        })
        .catch(function () {
            // Si el servidor no responde 
            mostrarMensajeGeneral("No se pudo conectar con el servidor");
        });
}

/**
 * Se ejecuta al enviar el formulario de registro.
 * Revisa los datos, los manda al servidor y si la cuenta se creó,
 * lleva al usuario al login.
 *
 * @param {Event} evento
 */
function registrar(evento) {
    evento.preventDefault();
    limpiarErrores();
    const nombre = document.getElementById("nombre").value;
    const correo = document.getElementById("correo").value.trim();
    const contrasena = document.getElementById("contrasena").value;
    const errores = validarRegistro(nombre, correo, contrasena);
    if (errores.length > 0) {
        errores.forEach(function (e) { mostrarErrorCampo(e.campo, e.mensaje); });
        return;
    }
    fetch("/api/auth/registro", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nombre: nombre.trim(), correo: correo, contrasena: contrasena })
    })
        .then(function (respuesta) {
            // Si la cuenta se creó, lleva al login
            if (respuesta.status === 201) {
                window.location.href = "/login";
                return;
            }
            // Si no se creó, muestra el error del servidor
            return mostrarErrorServidor(respuesta, "No se pudo crear la cuenta");
        })
        .catch(function () {
            // Si el servidor no responde
            mostrarMensajeGeneral("No se pudo conectar con el servidor");
        });
}

/**
 * Cierra la sesión y regresa a la página de login.
 */
function cerrarSesion() {
    fetch("/api/auth/logout", { method: "POST" })
        .then(function () { window.location.href = "/login"; })
        .catch(function () { window.location.href = "/login"; });
}