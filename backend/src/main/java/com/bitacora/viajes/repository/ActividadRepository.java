package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
}
