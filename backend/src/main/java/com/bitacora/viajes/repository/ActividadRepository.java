package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Actividad;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    // Actividades de un viaje con su autor cargado, ordenadas por día y hora.
    @Query("SELECT a FROM Actividad a JOIN FETCH a.autor " +
        "WHERE a.viaje.id = :viajeId ORDER BY a.dia, a.hora, a.id")
    List<Actividad> buscarPorViaje(@Param("viajeId") Integer viajeId);

    //Cuenta las actividades de un viaje cuya fecha qieda fuera del nuevo rango
    @Query("SELECT COUNT(a) FROM Actividad a "
            + "WHERE a.viaje.id = :viajeId AND (a.dia < :fechaInicio OR a.dia > :fechaFin)")
    long contarFueraDeRango(
        @Param("viajeId") Integer viajeId,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin);
}
