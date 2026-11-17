package com.bitacora.viajes.dto;

import java.math.BigDecimal;

// HU-21 | Responsable: Alfonso | DTO de salida con el saldo de cada participante.
// saldo = pagado - cuota. Positivo: le deben. Negativo: debe.
public record SaldoResponse(
        Integer usuarioId,
        String nombre,
        BigDecimal pagado,
        BigDecimal saldo) {
}
