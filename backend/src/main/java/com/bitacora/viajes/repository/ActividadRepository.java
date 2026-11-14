package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Actividad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    // Actividades de un viaje con su autor cargado, ordenadas por día y hora.
    @Query("SELECT a FROM Actividad a JOIN FETCH a.autor " +
        "WHERE a.viaje.id = :viajeId ORDER BY a.dia, a.hora, a.id")
    List<Actividad> buscarPorViaje(@Param("viajeId") Integer viajeId);
}
