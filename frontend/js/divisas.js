/**
 * Conversión de divisas para los gastos del viaje.
 * Pide el tipo de cambio a Frankfurter, convierte montos a la moneda del viaje,
 * da formato a los equivalentes y llena el selector de moneda del formulario de gastos.
 */

// Monedas que se pueden elegir al registrar un gasto
const MONEDAS_GASTO = ["MXN", "USD", "EUR"];
const tiposCambioGuardados = {};

/**
 * Llena el selector de moneda. La moneda del viaje va primero y queda seleccionada.
 * @param {HTMLSelectElement} selector
 * @param {string} monedaBase 
 */
function llenarSelectorMoneda(selector, monedaBase) {
    const monedas = [monedaBase, ...MONEDAS_GASTO.filter((m) => m !== monedaBase)];
    selector.innerHTML = monedas.map((m) => `<option value="${m}">${m}</option>`).join("");
    selector.value = monedaBase;
}

/**
 * Pide la tasa a Frankfurter. Primero usa la versión 2 y si falla, la versión 1.
 * @param {string} desde - Moneda del gasto
 * @param {string} hacia - Moneda del viaje
 * @returns {Promise<number>}
 */
async function pedirTasaFrankfurter(desde, hacia) {
    try {
        const respuesta = await fetch(`https://api.frankfurter.dev/v2/rate/${desde}/${hacia}`);
        if (respuesta.ok) {
            const datos = await respuesta.json();
            if (typeof datos.rate === "number") return datos.rate;
        }
    } catch (error) {
    }

    const respuesta = await fetch(`https://api.frankfurter.app/latest?from=${desde}&to=${hacia}`);
    if (!respuesta.ok) throw new Error("Tipo de cambio no disponible");

    const datos = await respuesta.json();
    const tasa = datos.rates?.[hacia];
    if (typeof tasa !== "number") throw new Error("Tipo de cambio no disponible");
    return tasa;
}

/**
 * Obtiene cuántas unidades de la moneda del viaje vale una unidad de otra moneda.
 * El resultado se guarda para no volver a pedirlo.
 * @param {string} desde
 * @param {string} hacia
 * @returns {Promise<number>}
 */
async function obtenerTipoCambio(desde, hacia) {
    const clave = `${desde}-${hacia}`;
    if (tiposCambioGuardados[clave]) return tiposCambioGuardados[clave];

    const tasa = Number((await pedirTasaFrankfurter(desde, hacia)).toFixed(6));
    tiposCambioGuardados[clave] = tasa;
    return tasa;
}

/**
 * Convierte un monto a la moneda del viaje, redondeado a 2 decimales.
 * @param {number} monto
 * @param {number} tipoCambio
 * @returns {number}
 */
function convertirAMonedaBase(monto, tipoCambio) {
    return Math.round(monto * tipoCambio * 100) / 100;
}

/**
 * Da formato a un monto
 * @param {number} monto
 * @param {string} moneda
 * @returns {string}
 */
function formatearMontoDivisa(monto, moneda) {
    const valor = Number(monto ?? 0);
    const texto = Math.abs(valor).toLocaleString("es-MX", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
    return `${valor < 0 ? "-" : ""}$${texto} ${moneda || "MXN"}`;
}