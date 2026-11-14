package com.bitacora.viajes.dto;

public class ParticipanteRequest {

    private String correo;

    public ParticipanteRequest(){

    }

    public ParticipanteRequest(String correo){
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}