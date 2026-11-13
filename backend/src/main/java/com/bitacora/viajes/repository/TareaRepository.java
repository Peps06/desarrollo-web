package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepository extends JpaRepository<Tarea, Integer> {
}
