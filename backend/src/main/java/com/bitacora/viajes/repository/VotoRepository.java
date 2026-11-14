package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Voto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VotoRepository extends JpaRepository<Voto, Integer> {

    // Total de votos por actividad en un viaje y cuántos son del usuario indicado.
    // Solo devuelve las actividades que tienen al menos un voto.
    @Query("SELECT v.actividad.id AS actividadId, COUNT(v) AS total, " +
        "SUM(CASE WHEN v.usuario.id = :usuarioId THEN 1 ELSE 0 END) AS propios " +
        "FROM Voto v WHERE v.actividad.viaje.id = :viajeId GROUP BY v.actividad.id")
    List<ConteoVotos> contarPorViaje(
        @Param("viajeId") Integer viajeId,
        @Param("usuarioId") Integer usuarioId);

    interface ConteoVotos {
        Integer getActividadId();
        Long getTotal();
        Number getPropios();
    }
}
