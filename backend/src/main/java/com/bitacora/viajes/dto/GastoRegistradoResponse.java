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
    private BigDecimal totalGastado;

    public GastoRegistradoResponse(GastoResponse gasto, BigDecimal totalGastado) {
        this.gasto = gasto;
        this.totalGastado = totalGastado;
    }

    public GastoResponse getGasto() {
        return gasto;
    }

    public BigDecimal getTotalGastado() {
        return totalGastado;
    }
}
