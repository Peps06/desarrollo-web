package com.bitacora.viajes.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bitacora.viajes.dto.BalanceResponse;
import com.bitacora.viajes.dto.SaldoResponse;
import com.bitacora.viajes.dto.TransferenciaResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * HU-21 | Responsable: Alfonso
 * Pruebas unitarias de BalanceService.
 * Solo prueban el método calcular(), que no usa la base de datos,
 * por eso el servicio se crea con null en lugar de los repositorios.
 */
class BalanceServiceTest {

    private final BalanceService servicio = new BalanceService(null, null, null);

    // Participantes de ejemplo: 1 = Ana, 2 = Beto, 3 = Cris (en ese orden).
    private Map<Integer, String> tresParticipantes() {
        Map<Integer, String> participantes = new LinkedHashMap<>();
        participantes.put(1, "Ana");
        participantes.put(2, "Beto");
        participantes.put(3, "Cris");
        return participantes;
    }

    // Suma los saldos de la respuesta; siempre debe dar 0.00.
    private BigDecimal sumaDeSaldos(BalanceResponse r) {
        BigDecimal suma = new BigDecimal("0.00");
        for (SaldoResponse s : r.saldos()) {
            suma = suma.add(s.saldo());
        }
        return suma;
    }

    @Test
    void unoPagoTodoYLosOtrosDosLePagan() {
        // Ana pagó 600, Beto y Cris no pagaron nada (ejemplo del catálogo api.md).
        Map<Integer, BigDecimal> pagos = new HashMap<>();
        pagos.put(1, new BigDecimal("600"));

        BalanceResponse r = servicio.calcular(tresParticipantes(), pagos);

        assertEquals(new BigDecimal("600.00"), r.totalGastado());
        assertEquals(new BigDecimal("200.00"), r.cuotaPorPersona());
        assertEquals(new BigDecimal("400.00"), r.saldos().get(0).saldo());
        assertEquals(new BigDecimal("-200.00"), r.saldos().get(1).saldo());
        assertEquals(new BigDecimal("-200.00"), r.saldos().get(2).saldo());
        assertEquals(new BigDecimal("0.00"), sumaDeSaldos(r));

        assertEquals(2, r.transferencias().size());

        TransferenciaResponse primera = r.transferencias().get(0);
        assertEquals("Beto", primera.deudorNombre());
        assertEquals("Ana", primera.acreedorNombre());
        assertEquals(new BigDecimal("200.00"), primera.monto());

        TransferenciaResponse segunda = r.transferencias().get(1);
        assertEquals("Cris", segunda.deudorNombre());
        assertEquals("Ana", segunda.acreedorNombre());
        assertEquals(new BigDecimal("200.00"), segunda.monto());
    }

    @Test
    void sinGastosTodoEnCero() {
        // Nadie ha pagado nada: el mapa de pagos llega vacío.
        BalanceResponse r = servicio.calcular(tresParticipantes(), new HashMap<>());

        assertEquals(new BigDecimal("0.00"), r.totalGastado());
        assertEquals(new BigDecimal("0.00"), r.cuotaPorPersona());
        for (SaldoResponse s : r.saldos()) {
            assertEquals(new BigDecimal("0.00"), s.pagado());
            assertEquals(new BigDecimal("0.00"), s.saldo());
        }
        assertTrue(r.transferencias().isEmpty());
    }

    @Test
    void unSoloParticipanteNoTieneTransferencias() {
        // Ana viaja sola y pagó 1,500: la cuota es todo lo que pagó y queda en 0.
        Map<Integer, String> participantes = new LinkedHashMap<>();
        participantes.put(1, "Ana");
        Map<Integer, BigDecimal> pagos = new HashMap<>();
        pagos.put(1, new BigDecimal("1500"));

        BalanceResponse r = servicio.calcular(participantes, pagos);

        assertEquals(new BigDecimal("1500.00"), r.cuotaPorPersona());
        assertEquals(new BigDecimal("0.00"), r.saldos().get(0).saldo());
        assertTrue(r.transferencias().isEmpty());
    }

    @Test
    void conDecimalesLaSumaDeSaldosDaCero() {
        // Ana pagó 100 entre 3: la cuota es 33.33 y sobra 1 centavo (33.33 x 3 = 99.99).
        Map<Integer, BigDecimal> pagos = new HashMap<>();
        pagos.put(1, new BigDecimal("100"));

        BalanceResponse r = servicio.calcular(tresParticipantes(), pagos);

        assertEquals(new BigDecimal("33.33"), r.cuotaPorPersona());
        // Sin ajuste Ana tendría 66.67; se le quita el centavo sobrante para que cuadre.
        assertEquals(new BigDecimal("66.66"), r.saldos().get(0).saldo());
        assertEquals(new BigDecimal("-33.33"), r.saldos().get(1).saldo());
        assertEquals(new BigDecimal("-33.33"), r.saldos().get(2).saldo());
        assertEquals(new BigDecimal("0.00"), sumaDeSaldos(r));

        // Beto y Cris le pagan 33.33 cada uno a Ana: 66.66 en total.
        assertEquals(2, r.transferencias().size());
        assertEquals(new BigDecimal("33.33"), r.transferencias().get(0).monto());
        assertEquals(new BigDecimal("33.33"), r.transferencias().get(1).monto());
    }
}
