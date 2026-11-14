package com.bitacora.viajes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Forma única de los errores de la API: un código HTTP y un mensaje.
 * Cuando hay errores de validación, también incluye la lista de campos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final int codigo;
    private final String mensaje;
    private final List<ErrorCampo> errores;

    public ErrorResponse(int codigo, String mensaje) {
        this(codigo, mensaje, null);
    }

    public ErrorResponse(int codigo, String mensaje, List<ErrorCampo> errores) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    /** Crea un error de validación con un solo campo afectado. */
    public static ErrorResponse validacion(String campo, String mensaje) {
        return new ErrorResponse(400, "Datos inválidos", List.of(new ErrorCampo(campo, mensaje)));
    }

    public int getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public List<ErrorCampo> getErrores() { return errores; }
}
