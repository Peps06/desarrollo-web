package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ActividadMasVotadaResponse;
import com.bitacora.viajes.model.Actividad;
import com.bitacora.viajes.repository.VotoRepository.ConteoVotos;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * HU-22 | Responsable: Alfonso
 * Determina la actividad más votada de cada día del viaje.
 * Desempate: más votos -> menor costo estimado -> creada primero.
 * Si el día no tiene votos o hay empate total, no hay ganadora (null).
 * No consulta la base de datos: recibe las actividades y los votos que ya cargó el detalle del viaje.
 */
@Service
public class ActividadGanadoraService {

    // Calcula la ganadora de cada día del viaje.
    // actividades: todas las actividades del viaje (de cualquier día).
    // conteos: actividadId -> conteo de votos (VotoRepository.contarPorViaje, una sola consulta).
    // Los días sin actividades no aparecen en el resultado.
    public Map<LocalDate, ActividadMasVotadaResponse> calcularPorDia(List<Actividad> actividades,
                                                                     Map<Integer, ConteoVotos> conteos) {
        // 1. Deja solo el total de votos de cada actividad: actividadId -> total.
        Map<Integer, Long> votos = new HashMap<>();
        for (Integer actividadId : conteos.keySet()) {
            votos.put(actividadId, conteos.get(actividadId).getTotal());
        }

        // 2. Separa las actividades por día.
        Map<LocalDate, List<Actividad>> actividadesPorDia = new HashMap<>();
        for (Actividad actividad : actividades) {
            List<Actividad> delDia = actividadesPorDia.get(actividad.getDia());
            if (delDia == null) {
                delDia = new ArrayList<>();
                actividadesPorDia.put(actividad.getDia(), delDia);
            }
            delDia.add(actividad);
        }

        // 3. Elige la ganadora de cada día.
        Map<LocalDate, ActividadMasVotadaResponse> ganadoras = new HashMap<>();
        for (LocalDate dia : actividadesPorDia.keySet()) {
            ganadoras.put(dia, elegirGanadora(actividadesPorDia.get(dia), votos));
        }
        return ganadoras;
    }

    // Elige la actividad más votada de UN día. Es público y no usa la base de datos,
    // así las pruebas JUnit lo pueden llamar directamente.
    // votos: actividadId -> total de votos (las actividades sin votos pueden no venir).
    // Devuelve null si nadie votó o si hay empate total.
    public ActividadMasVotadaResponse elegirGanadora(List<Actividad> actividadesDelDia, Map<Integer, Long> votos) {
        Actividad ganadora = null;
        long votosGanadora = 0;
        boolean empateTotal = false;

        for (Actividad actividad : actividadesDelDia) {
            long votosActividad = votosDe(actividad, votos);

            // Una actividad sin votos no compite.
            if (votosActividad == 0) {
                continue;
            }

            // La primera con votos es la ganadora provisional.
            if (ganadora == null) {
                ganadora = actividad;
                votosGanadora = votosActividad;
                continue;
            }

            int resultado = comparar(actividad, votosActividad, ganadora, votosGanadora);
            if (resultado > 0) {
                // Le gana a la provisional: toma su lugar y ya no hay empate.
                ganadora = actividad;
                votosGanadora = votosActividad;
                empateTotal = false;
            } else if (resultado == 0) {
                // Empata en todo con la provisional.
                empateTotal = true;
            }
            // Si resultado < 0, pierde y no pasa nada.
        }

        if (ganadora == null || empateTotal) {
            return null;
        }
        return new ActividadMasVotadaResponse(ganadora.getId(), ganadora.getTitulo(), votosGanadora);
    }

    // Compara dos actividades. Devuelve un número positivo si "a" es mejor que "b",
    // negativo si "b" es mejor, y 0 si empatan en todo.
    private int comparar(Actividad a, long votosA, Actividad b, long votosB) {
        // 1. Más votos.
        if (votosA != votosB) {
            return Long.compare(votosA, votosB);
        }

        // 2. Menor costo estimado (por eso se compara b contra a: el más barato sale positivo).
        int porCosto = costoDe(b).compareTo(costoDe(a));
        if (porCosto != 0) {
            return porCosto;
        }

        // 3. Creada primero (también al revés: la fecha más antigua sale positiva).
        return b.getFechaCreacion().compareTo(a.getFechaCreacion());
    }

    // Votos de una actividad; 0 si no aparece en el mapa.
    private long votosDe(Actividad actividad, Map<Integer, Long> votos) {
        Long total = votos.get(actividad.getId());
        if (total == null) {
            return 0;
        }
        return total;
    }

    // Costo estimado de una actividad; 0 si viene vacío.
    private BigDecimal costoDe(Actividad actividad) {
        if (actividad.getCostoEstimado() == null) {
            return BigDecimal.ZERO;
        }
        return actividad.getCostoEstimado();
    }
}
