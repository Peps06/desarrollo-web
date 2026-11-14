package com.bitacora.viajes.dto;

import java.math.BigDecimal;

// HU-16 | Estado del presupuesto de un viaje. Alimenta la barra de progreso de HU-32.
// Todos los montos van en la moneda base del viaje, con 2 decimales.
public record PresupuestoResponse(
        BigDecimal presupuestoTotal,
        BigDecimal totalGastado,
        BigDecimal restante,
        BigDecimal porcentajeUsado,
        int numParticipantes,
        BigDecimal gastoPorPersona,
        String monedaBase) {
}
