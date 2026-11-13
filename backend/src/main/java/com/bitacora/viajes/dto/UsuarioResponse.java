package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.Usuario;

/** Datos de un usuario que se devuelven al cliente. Nunca incluye la contraseña ni su hash. */
public class UsuarioResponse {

    private final Integer id;
    private final String nombre;
    private final String correo;

    public UsuarioResponse(Integer id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getCorreo());
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
}
