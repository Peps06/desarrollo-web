package com.bitacora.viajes.dto;

// HU-14 | Respuesta al votar o quitar un voto: lleva el contador ya actualizado
// para que el JavaScript (HU-30) refresque la pantalla sin pedir el detalle otra vez.
public record VotoResponse(Integer actividadId, long totalVotos, boolean votadaPorMi) {
}
