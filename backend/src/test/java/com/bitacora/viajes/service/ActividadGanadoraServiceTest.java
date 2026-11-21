package com.bitacora.viajes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.bitacora.viajes.dto.ActividadMasVotadaResponse;
import com.bitacora.viajes.model.Actividad;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * HU-22 | Responsable: Alfonso
 * Pruebas unitarias de ActividadGanadoraService.
 * Solo prueban elegirGanadora(), que no usa la base de datos.
 */
class ActividadGanadoraServiceTest {

    private final ActividadGanadoraService servicio = new ActividadGanadoraService();

    // Fecha de referencia para fechaCreacion.
    private static final LocalDateTime LUNES_9AM = LocalDateTime.of(2026, 11, 16, 9, 0);

    // Crea una actividad de prueba sin base de datos.
    // Actividad no tiene setId (en la app lo genera MySQL), así que el id se pone
    // con ReflectionTestUtils, una utilidad de Spring pensada para pruebas.
    private Actividad actividad(int id, String titulo, String costo, LocalDateTime creada) {
        Actividad a = new Actividad();
        ReflectionTestUtils.setField(a, "id", id);
        a.setTitulo(titulo);
        a.setCostoEstimado(new BigDecimal(costo));
        a.setFechaCreacion(creada);
        return a;
    }

    @Test
    void sinVotosNoHayGanadora() {
        List<Actividad> dia = List.of(
                actividad(1, "Museo", "200", LUNES_9AM),
                actividad(2, "Playa", "0", LUNES_9AM.plusHours(1)));

        // Nadie ha votado: el mapa de votos llega vacío.
        assertNull(servicio.elegirGanadora(dia, new HashMap<>()));
    }

    @Test
    void ganaLaDeMasVotos() {
        List<Actividad> dia = List.of(
                actividad(1, "Museo", "200", LUNES_9AM),
                actividad(2, "Playa", "0", LUNES_9AM.plusHours(1)),
                actividad(3, "Tour", "500", LUNES_9AM.plusHours(2)));
        Map<Integer, Long> votos = new HashMap<>();
        votos.put(1, 1L);
        votos.put(2, 2L);
        votos.put(3, 4L);

        ActividadMasVotadaResponse ganadora = servicio.elegirGanadora(dia, votos);

        assertEquals(3, ganadora.id());
        assertEquals("Tour", ganadora.titulo());
        assertEquals(4, ganadora.totalVotos());
    }

    @Test
    void empateEnVotosGanaLaMasBarata() {
        // Museo y Playa tienen 3 votos; Playa cuesta menos.
        List<Actividad> dia = List.of(
                actividad(1, "Museo", "200", LUNES_9AM),
                actividad(2, "Playa", "50", LUNES_9AM.plusHours(1)));
        Map<Integer, Long> votos = new HashMap<>();
        votos.put(1, 3L);
        votos.put(2, 3L);

        ActividadMasVotadaResponse ganadora = servicio.elegirGanadora(dia, votos);

        assertEquals(2, ganadora.id());
        assertEquals("Playa", ganadora.titulo());
        assertEquals(3, ganadora.totalVotos());
    }

    @Test
    void empateEnVotosYCostoGanaLaCreadaPrimero() {
        // Mismos votos y mismo costo; Mercado se creó una hora antes que Museo.
        // Se pone primero en la lista la creada después, para comprobar que no gana solo por el orden.
        List<Actividad> dia = List.of(
                actividad(1, "Museo", "100", LUNES_9AM.plusHours(1)),
                actividad(2, "Mercado", "100.00", LUNES_9AM));
        Map<Integer, Long> votos = new HashMap<>();
        votos.put(1, 2L);
        votos.put(2, 2L);

        ActividadMasVotadaResponse ganadora = servicio.elegirGanadora(dia, votos);

        assertEquals(2, ganadora.id());
        assertEquals("Mercado", ganadora.titulo());
    }

    @Test
    void empateTotalNoHayGanadora() {
        // Mismos votos, mismo costo y misma fecha de creación: no se puede decidir.
        List<Actividad> dia = List.of(
                actividad(1, "Museo", "100", LUNES_9AM),
                actividad(2, "Mercado", "100", LUNES_9AM),
                actividad(3, "Playa", "0", LUNES_9AM));
        Map<Integer, Long> votos = new HashMap<>();
        votos.put(1, 2L);
        votos.put(2, 2L);
        votos.put(3, 1L);

        assertNull(servicio.elegirGanadora(dia, votos));
    }
}
