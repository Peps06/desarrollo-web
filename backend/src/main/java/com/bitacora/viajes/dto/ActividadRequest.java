package com.bitacora.viajes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Datos que envía el cliente para proponer una actividad dentro de un viaje.
// El costo estimado es opcional y vale 0 si no se envía.
public record ActividadRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede pasar de 150 caracteres")
        String titulo,

        @Size(max = 500, message = "La descripción no puede pasar de 500 caracteres")
        String descripcion,

        @NotNull(message = "El día es obligatorio")
        LocalDate dia,

        @NotNull(message = "La hora es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime hora,

        @DecimalMin(value = "0.00", message = "El costo estimado no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El costo estimado admite hasta 8 enteros y 2 decimales")
        BigDecimal costoEstimado) {
}
