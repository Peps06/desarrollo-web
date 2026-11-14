package com.bitacora.viajes.dto;

/** Describe un campo que no pasó la validación y el motivo. */
public class ErrorCampo {

    private final String campo;
    private final String mensaje;

    public ErrorCampo(String campo, String mensaje) {
        this.campo = campo;
        this.mensaje = mensaje;
    }

    public String getCampo() { return campo; }
    public String getMensaje() { return mensaje; }
}
