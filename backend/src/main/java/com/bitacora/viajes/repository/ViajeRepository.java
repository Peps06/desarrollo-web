package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Viaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViajeRepository extends JpaRepository<Viaje, Integer> {
}
