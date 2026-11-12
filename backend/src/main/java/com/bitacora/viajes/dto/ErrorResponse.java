package com.bitacora.viajes.dto;

/** Cuerpo de las respuestas de error: indica el campo afectado y el motivo. */
public class ErrorResponse {

    private final String campo;
    private final String mensaje;

    public ErrorResponse(String campo, String mensaje) {
        this.campo = campo;
        this.mensaje = mensaje;
    }

    public String getCampo() { return campo; }
    public String getMensaje() { return mensaje; }
}
