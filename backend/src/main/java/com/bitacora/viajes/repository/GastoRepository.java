package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GastoRepository extends JpaRepository<Gasto, Integer> {
}
