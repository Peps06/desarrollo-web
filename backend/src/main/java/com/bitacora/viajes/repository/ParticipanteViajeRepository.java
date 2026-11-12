package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.ParticipanteViaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipanteViajeRepository extends JpaRepository<ParticipanteViaje, Integer> {
}
