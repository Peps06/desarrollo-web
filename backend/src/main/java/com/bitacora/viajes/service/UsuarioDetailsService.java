package com.bitacora.viajes.service;

import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Le indica a Spring Security cómo encontrar un usuario por su correo.
 * La contraseña que entrega es el hash guardado en la base; Spring la compara
 * con la contraseña enviada usando el PasswordEncoder.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(UsuarioService.normalizarCorreo(correo))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));

        return User.withUsername(usuario.getCorreo())
                .password(usuario.getContrasenaHash())
                .roles("USUARIO")
                .build();
    }
}
