// HU-15 | Responsable: Citlaly | DTO de salida tras registrar un gasto (incluye presupuesto actualizado).
package com.bitacora.viajes.dto;

import java.math.BigDecimal;

/**
 * Respuesta que incluye el gasto y el total gastado del viaje ya recalculado.
 *
 * @author Citlaly Morales Viveros
 */
public class GastoRegistradoResponse {

    private GastoResponse gasto;
    private BigDecimal totalViaje;

    public GastoRegistradoResponse(GastoResponse gasto, BigDecimal totalViaje) {
        this.gasto = gasto;
        this.totalViaje = totalViaje;
    }

    public GastoResponse getGasto() {
        return gasto;
    }

    public BigDecimal getTotalViaje() {
        return totalViaje;
    }
}
