package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Integer> {
}
