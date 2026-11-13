package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.EstadoViaje;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ParticipanteViajeRepository extends JpaRepository<ParticipanteViaje, Integer> {

//Busca las participaciones de un usuario filtrando solo los viajes activos (no cancelados)
// JOIN FETCH carga los datos del viaje jusnto con la participación en una sola consulta.

    @Query("SELECT p FROM ParticipanteViaje p JOIN FETCH p.viaje v "+
        "WHERE p.usuario.id = :usuarioId AND v.estado = :estado " +
        "ORDER BY v.fechaInicio ASC")
    List<ParticipanteViaje> buscarPorUsuarioYEstadoViaje(
        @Param("usuarioId") Integer usuarioId,
        @Param("estado") EstadoViaje estado);
}

// Este archivo es la interfaz que se conecta con la tabla participante_viaje en MySQL.
// Como un viaje tiene participantes con distintos roles, necesitamos una consulta que
// traiga todas las filas donde aparece el usuario que inició sesión, pero ignorando
// los viajes cancelados.