package com.bitacora.viajes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bitacora.viajes.dto.PresupuestoResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * HU-16: pruebas unitarias del cálculo del presupuesto.
 * Solo prueban el método calcular(), que no usa la base de datos,
 * por eso el servicio se crea con null en lugar de los repositorios.
 */
class PresupuestoServiceTest {

    private final PresupuestoService servicio = new PresupuestoService(null, null, null);

    @Test
    void sinGastosTodoEnCero() {
        // Viaje de 10,000 con 2 personas y ningún gasto (la suma llega como null).
        PresupuestoResponse r = servicio.calcular(new BigDecimal("10000"), null, 2, "MXN");

        assertEquals(new BigDecimal("0.00"), r.totalGastado());
        assertEquals(new BigDecimal("10000.00"), r.restante());
        assertEquals(new BigDecimal("0.00"), r.porcentajeUsado());
        assertEquals(new BigDecimal("0.00"), r.gastoPorPersona());
    }

    @Test
    void gastoDentroDelPresupuesto() {
        // Se gastaron 3,800.50 de 20,000 entre 2 personas (ejemplo del catálogo api.md).
        PresupuestoResponse r = servicio.calcular(new BigDecimal("20000"), new BigDecimal("3800.50"), 2, "MXN");

        assertEquals(new BigDecimal("16199.50"), r.restante());
        assertEquals(new BigDecimal("19.00"), r.porcentajeUsado());
        assertEquals(new BigDecimal("1900.25"), r.gastoPorPersona());
        assertEquals("MXN", r.monedaBase());
    }

    @Test
    void presupuestoExcedido() {
        // Se gastaron 1,200 de 1,000 entre 3 personas: el restante es negativo y pasa del 100%.
        PresupuestoResponse r = servicio.calcular(new BigDecimal("1000"), new BigDecimal("1200"), 3, "MXN");

        assertEquals(new BigDecimal("-200.00"), r.restante());
        assertEquals(new BigDecimal("120.00"), r.porcentajeUsado());
        assertEquals(new BigDecimal("400.00"), r.gastoPorPersona());
    }
}
