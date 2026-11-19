/*
 * validacion.js
 * Revisa los datos del login y del registro antes de enviarlos al servidor.
 */

/**
 * Revisa que el correo tenga forma de correo (algo@algo.algo).
 * @param {string} correo 
 * @returns {boolean}
 */
function correoValido(correo) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo);
}

/**
 * Revisa los datos del login.
 * @param {string} correo 
 * @param {string} contrasena 
 * @returns {Array<{campo: string, mensaje: string}>} 
 */
function validarLogin(correo, contrasena) {
    const errores = [];
    // Formato válido
    if (!correoValido(correo)) {
        errores.push({ campo: "correo", mensaje: "Escribe un correo válido" });
    }
    // No puede estar vacía
    if (contrasena === "") {
        errores.push({ campo: "contrasena", mensaje: "Escribe tu contraseña" });
    }
    return errores;
}

/**
 * Revisa los datos del registro.
 * @param {string} nombre
 * @param {string} correo
 * @param {string} contrasena
 * @returns {Array<{campo: string, mensaje: string}>}
 */
function validarRegistro(nombre, correo, contrasena) {
    const errores = [];
    if (nombre.trim() === "") {
        errores.push({ campo: "nombre", mensaje: "El nombre es obligatorio" });
    }
    if (!correoValido(correo)) {
        errores.push({ campo: "correo", mensaje: "Escribe un correo válido" });
    }
    if (contrasena.length < 8) {
        errores.push({ campo: "contrasena", mensaje: "La contraseña debe tener al menos 8 caracteres" });
    }
    return errores;
}

/**
 * Pone un mensaje de error debajo de un campo y lo marca en rojo.
 *
 * @param {string} campo 
 * @param {string} mensaje
 */
function mostrarErrorCampo(campo, mensaje) {
    const span = document.getElementById("error-" + campo);
    const grupo = document.getElementById("grupo-" + campo);
    if (span) span.textContent = mensaje;
    if (grupo) grupo.classList.add("campo--invalido");
}

/**
 * Borra los errores de todos los campos y el mensaje general.
 * No borra lo que el usuario escribió.
 */
function limpiarErrores() {
    ["nombre", "correo", "contrasena"].forEach(function (campo) {
        const span = document.getElementById("error-" + campo);
        const grupo = document.getElementById("grupo-" + campo);

        if (span) span.textContent = "";
        if (grupo) grupo.classList.remove("campo--invalido");
    });

    // Borra el mensaje general de arriba de la página
    const contenedor = document.getElementById("contenedorMensajes");
    if (contenedor) contenedor.innerHTML = "";
}