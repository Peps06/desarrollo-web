package com.bitacora.viajes.dto;

// HU-22 | Responsable: Alfonso | DTO de salida con la actividad más votada de un día.
public record ActividadMasVotadaResponse(
        Integer id,
        String titulo,
        long totalVotos) {
}
