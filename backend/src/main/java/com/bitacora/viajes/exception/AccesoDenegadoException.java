// HU-17 | Responsable: Dana | Excepción cuando el usuario no tiene permiso (403).
package com.bitacora.viajes.exception;

/**
 * Se lanza cuando el usuario no tiene permiso para la acción.
 *
 * @author Citlaly Morales Viveros
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
