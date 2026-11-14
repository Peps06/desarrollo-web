package com.bitacora.viajes.repository;

import com.bitacora.viajes.model.Gasto;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GastoRepository extends JpaRepository<Gasto, Integer> {

    // Suma de los montos (en moneda base) de un viaje. Es null si el viaje no tiene gastos.
    @Query("SELECT SUM(g.monto) FROM Gasto g WHERE g.viaje.id = :viajeId")
    BigDecimal sumarMontoPorViaje(@Param("viajeId") Integer viajeId);
}
