// HU-07 | Responsable: Dana | Repositorio Spring Data JPA para Usuario. Hereda save, findAll, findById, count y delete.
package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
}
