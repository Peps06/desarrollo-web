// HU-17 | Responsable: Dana | Excepción cuando un recurso no existe (404).
package com.bitacora.viajes.exception;

/**
 * Se lanza cuando el recurso solicitado no existe.
 *
 * @author Citlaly Morales Viveros
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}