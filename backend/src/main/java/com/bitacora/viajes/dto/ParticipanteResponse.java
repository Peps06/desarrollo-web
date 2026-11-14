package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.ParticipanteViaje;

public class ParticipanteResponse {

    private Integer usuarioId;
    private String nombre;
    private String correo;
    private String rol;

    public ParticipanteResponse(){

    }

    public ParticipanteResponse(Integer usuarioId, String nombre, String correo, String  rol){
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
    }

    public static ParticipanteResponse desdeEntidad(ParticipanteViaje participacion){
        return new ParticipanteResponse(
            participacion.getUsuario().getId(),
            participacion.getUsuario().getNombre(),
            participacion.getUsuario().getCorreo(),
            participacion.getRol().name().toLowerCase()
        );
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}