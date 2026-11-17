package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Actividad;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // Actividad con su viaje y su autor ya cargados, para validar permisos sin consultas extra.
    @Query("SELECT a FROM Actividad a JOIN FETCH a.viaje JOIN FETCH a.autor WHERE a.id = :id")
    Optional<Actividad> buscarConViajeYAutor(@Param("id") Integer id);

    // Actividades de un viaje filtradas por día y/o costo máximo. Un filtro nulo no se aplica.
    @Query("SELECT a FROM Actividad a JOIN FETCH a.autor "
            + "WHERE a.viaje.id = :viajeId "
            + "AND (:dia IS NULL OR a.dia = :dia) "
            + "AND (:costoMax IS NULL OR a.costoEstimado <= :costoMax) "
            + "ORDER BY a.dia, a.hora, a.id")
    List<Actividad> filtrarPorViaje(
        @Param("viajeId") Integer viajeId,
        @Param("dia") LocalDate dia,
        @Param("costoMax") BigDecimal costoMax);

    // Elimina todos los votos de una actividad. Se usa antes de borrarla, en la misma transacción.
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Voto v WHERE v.actividad.id = :actividadId")
    int eliminarVotos(@Param("actividadId") Integer actividadId);
}
