// HU-17 | Responsable: Dana | Excepción para datos inválidos o reglas de negocio (400).
package com.bitacora.viajes.exception;

/**
 * Se lanza cuando los datos enviados no cumplen las reglas.
 *
 * @author Citlaly Morales Viveros
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
