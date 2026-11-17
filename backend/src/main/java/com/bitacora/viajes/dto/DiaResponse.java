package com.bitacora.viajes.dto;

import java.time.LocalDate;
import java.util.List;

// Un día del itinerario con sus actividades ordenadas por hora. La lista queda vacía si el día no tiene actividades.
// HU-22: actividadMasVotada es null si el día no tiene votos o si hay empate total.
public record DiaResponse(LocalDate fecha, List<ActividadResponse> actividades,
                          ActividadMasVotadaResponse actividadMasVotada) {
}
