package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.RegistroRequest;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea una cuenta nueva. El correo se normaliza a minúsculas para que no existan
     * dos cuentas con el mismo correo escrito con distintas mayúsculas.
     */
    public Usuario registrar(RegistroRequest solicitud) {
        String correo = normalizarCorreo(solicitud.getCorreo());

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setContrasenaHash(passwordEncoder.encode(solicitud.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(normalizarCorreo(correo))
                .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta con ese correo"));
    }

    public static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }
}
