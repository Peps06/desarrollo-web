package com.bitacora.viajes.dto;

import java.math.BigDecimal;
import java.util.List;

// HU-21 | Responsable: Alfonso | DTO de salida con el balance general del grupo.
// Todos los montos van en la moneda base del viaje, con 2 decimales.
public record BalanceResponse(
        BigDecimal cuotaPorPersona,
        BigDecimal totalGastado,
        List<SaldoResponse> saldos,
        List<TransferenciaResponse> transferencias) {
}
