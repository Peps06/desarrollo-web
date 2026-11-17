package com.bitacora.viajes.dto;

import java.math.BigDecimal;

// HU-21 | Responsable: Alfonso | DTO de salida con quién debe pagar a quién y cuánto.
public record TransferenciaResponse(
        Integer deudorId,
        String deudorNombre,
        Integer acreedorId,
        String acreedorNombre,
        BigDecimal monto) {
}
