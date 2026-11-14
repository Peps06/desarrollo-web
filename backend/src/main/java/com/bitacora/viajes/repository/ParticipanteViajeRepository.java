package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.EstadoViaje;
import com.bitacora.viajes.model.ParticipanteViaje;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipanteViajeRepository extends JpaRepository<ParticipanteViaje, Integer> {

    @Query("SELECT p FROM ParticipanteViaje p JOIN FETCH p.viaje v "
            + "WHERE p.usuario.id = :usuarioId AND v.estado = :estado "
            + "ORDER BY v.fechaInicio ASC")
    List<ParticipanteViaje> buscarPorUsuarioYEstadoViaje(
            @Param("usuarioId") Integer usuarioId,
            @Param("estado") EstadoViaje estado);

    @Query("SELECT p FROM ParticipanteViaje p JOIN FETCH p.viaje JOIN FETCH p.usuario "
            + "WHERE p.viaje.id = :viajeId AND p.usuario.correo = :correo")
    Optional<ParticipanteViaje> buscarParticipacion(
            @Param("viajeId") Integer viajeId,
            @Param("correo") String correo);

    @Query("SELECT p FROM ParticipanteViaje p JOIN FETCH p.usuario "
            + "WHERE p.viaje.id = :viajeId ORDER BY p.id")
    List<ParticipanteViaje> buscarPorViaje(@Param("viajeId") Integer viajeId);

    @Query("SELECT p FROM ParticipanteViaje p JOIN FETCH p.usuario "
            + "WHERE p.viaje.id = :viajeId AND p.usuario.id = :usuarioId")
    Optional<ParticipanteViaje> buscarPorViajeYUsuarioId(
            @Param("viajeId") Integer viajeId,
            @Param("usuarioId") Integer usuarioId);
}

// Este archivo es la interfaz que se conecta con la tabla participante_viaje en MySQL.
// Como un viaje tiene participantes con distintos roles, necesitamos una consulta que
// traiga todas las filas donde aparece el usuario que inició sesión, pero ignorando
// los viajes cancelados.