package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.Actividad;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Actividad del itinerario con su autor, el total de votos y si el usuario en sesión ya votó por ella.
public record ActividadResponse(
        Integer id,
        Integer viajeId,
        String titulo,
        String descripcion,
        LocalDate dia,
        @JsonFormat(pattern = "HH:mm") LocalTime hora,
        BigDecimal costoEstimado,
        Autor autor,
        long totalVotos,
        boolean votadaPorMi) {

    public record Autor(Integer id, String nombre) {
    }

    public static ActividadResponse desde(Actividad actividad, long totalVotos, boolean votadaPorMi) {
        return new ActividadResponse(
                actividad.getId(),
                actividad.getViaje().getId(),
                actividad.getTitulo(),
                actividad.getDescripcion(),
                actividad.getDia(),
                actividad.getHora(),
                actividad.getCostoEstimado(),
                new Autor(actividad.getAutor().getId(), actividad.getAutor().getNombre()),
                totalVotos,
                votadaPorMi);
    }
}
