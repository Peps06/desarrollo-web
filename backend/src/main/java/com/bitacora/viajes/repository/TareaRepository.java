package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Tarea;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TareaRepository extends JpaRepository<Tarea, Integer> {

    List<Tarea> findByViajeIdOrderByIdAsc(Integer viajeId);
}