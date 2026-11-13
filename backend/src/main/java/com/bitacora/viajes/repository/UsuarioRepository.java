package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /** Indica si ya existe una cuenta registrada con ese correo. */
    boolean existsByCorreo(String correo);

    /** Busca la cuenta asociada a un correo. Se usa al iniciar sesión. */
    Optional<Usuario> findByCorreo(String correo);
}
