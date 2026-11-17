package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.BalanceResponse;
import com.bitacora.viajes.dto.SaldoResponse;
import com.bitacora.viajes.dto.TransferenciaResponse;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.repository.GastoRepository;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-21 | Responsable: Alfonso
 * Calcula cuánto debe o le deben a cada participante y qué transferencias
 * hacen falta para que todos queden a mano.
 */
@Service
public class BalanceService {

    private final ViajeDetalleService viajeDetalleService;
    private final ParticipanteViajeRepository participanteViajeRepository;
    private final GastoRepository gastoRepository;

    public BalanceService(ViajeDetalleService viajeDetalleService,
                          ParticipanteViajeRepository participanteViajeRepository,
                          GastoRepository gastoRepository) {
        this.viajeDetalleService = viajeDetalleService;
        this.participanteViajeRepository = participanteViajeRepository;
        this.gastoRepository = gastoRepository;
    }

    // Junta los datos del viaje desde la base de datos y hace el cálculo.
    @Transactional(readOnly = true)
    public BalanceResponse obtenerBalance(Integer viajeId, String correoUsuario) {
        // 404 si el viaje no existe o está cancelado, 403 si el usuario no participa (igual que HU-16).
        viajeDetalleService.buscarParticipacion(viajeId, correoUsuario);

        // Participantes en orden de alta: usuarioId -> nombre.
        // LinkedHashMap conserva el orden en que se agregan.
        Map<Integer, String> participantes = new LinkedHashMap<>();
        for (ParticipanteViaje p : participanteViajeRepository.buscarPorViaje(viajeId)) {
            participantes.put(p.getUsuario().getId(), p.getUsuario().getNombre());
        }

        // Lo que pagó cada usuario: usuarioId -> total pagado.
        Map<Integer, BigDecimal> pagos = new HashMap<>();
        for (Object[] fila : gastoRepository.sumarMontoPorUsuario(viajeId)) {
            pagos.put((Integer) fila[0], (BigDecimal) fila[1]);
        }

        return calcular(participantes, pagos);
    }

    // Hace las cuentas con los datos ya obtenidos. Es público y no usa la base de datos,
    // así las pruebas JUnit lo pueden llamar directamente con números de ejemplo.
    // participantes: usuarioId -> nombre (en el orden en que se muestran).
    // pagos: usuarioId -> total que pagó (quien no pagó nada puede no venir).
    public BalanceResponse calcular(Map<Integer, String> participantes, Map<Integer, BigDecimal> pagos) {
        // 1. Total gastado: suma de lo que pagó cada participante.
        BigDecimal totalGastado = BigDecimal.ZERO;
        for (Integer usuarioId : participantes.keySet()) {
            totalGastado = totalGastado.add(pagadoPor(usuarioId, pagos));
        }
        totalGastado = dosDecimales(totalGastado);

        // 2. Cuota por persona = total / número de participantes.
        BigDecimal cuota = new BigDecimal("0.00");
        if (!participantes.isEmpty()) {
            cuota = totalGastado.divide(new BigDecimal(participantes.size()), 2, RoundingMode.HALF_UP);
        }

        // 3. Saldo de cada uno = lo que pagó - cuota.
        List<Integer> ids = new ArrayList<>();
        List<BigDecimal> pagados = new ArrayList<>();
        List<BigDecimal> saldos = new ArrayList<>();
        for (Integer usuarioId : participantes.keySet()) {
            BigDecimal pagado = pagadoPor(usuarioId, pagos);
            ids.add(usuarioId);
            pagados.add(pagado);
            saldos.add(pagado.subtract(cuota));
        }

        // 4. Por el redondeo de la cuota, la suma de saldos puede no dar 0 (ej. 100 / 3).
        ajustarRedondeo(saldos);

        // 5. Arma la lista de saldos para la respuesta.
        List<SaldoResponse> listaSaldos = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Integer usuarioId = ids.get(i);
            listaSaldos.add(new SaldoResponse(usuarioId, participantes.get(usuarioId),
                    pagados.get(i), saldos.get(i)));
        }

        // 6. Calcula quién le paga a quién.
        List<TransferenciaResponse> transferencias = generarTransferencias(listaSaldos);

        return new BalanceResponse(cuota, totalGastado, listaSaldos, transferencias);
    }

    // Si la suma de los saldos no es 0, la diferencia (unos centavos) se le quita
    // al participante con el saldo más alto, para que todo cuadre exacto.
    private void ajustarRedondeo(List<BigDecimal> saldos) {
        BigDecimal suma = BigDecimal.ZERO;
        for (BigDecimal saldo : saldos) {
            suma = suma.add(saldo);
        }
        if (suma.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        int mayor = posicionDelMayor(saldos);
        saldos.set(mayor, saldos.get(mayor).subtract(suma));
    }

    // Empareja al que más debe con al que más le deben, una y otra vez,
    // hasta que todos los saldos queden en 0.
    private List<TransferenciaResponse> generarTransferencias(List<SaldoResponse> saldos) {
        // Copia de los saldos que se va "gastando" con cada transferencia.
        List<BigDecimal> pendientes = new ArrayList<>();
        for (SaldoResponse s : saldos) {
            pendientes.add(s.saldo());
        }

        List<TransferenciaResponse> transferencias = new ArrayList<>();
        // Con menos de 2 personas no hay a quién pagarle.
        if (pendientes.size() < 2) {
            return transferencias;
        }

        while (true) {
            int deudor = posicionDelMenor(pendientes);    // el saldo más negativo
            int acreedor = posicionDelMayor(pendientes);  // el saldo más positivo

            BigDecimal debe = pendientes.get(deudor).negate();   // lo que debe, en positivo
            BigDecimal leDeben = pendientes.get(acreedor);

            // Si ya nadie debe (o a nadie le deben), todos están en 0 y se termina.
            if (debe.compareTo(BigDecimal.ZERO) <= 0 || leDeben.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Se paga lo menor de las dos cantidades; así al menos uno de los dos queda en 0.
            BigDecimal monto = debe.min(leDeben);

            SaldoResponse d = saldos.get(deudor);
            SaldoResponse a = saldos.get(acreedor);
            transferencias.add(new TransferenciaResponse(d.usuarioId(), d.nombre(),
                    a.usuarioId(), a.nombre(), monto));

            pendientes.set(deudor, pendientes.get(deudor).add(monto));
            pendientes.set(acreedor, pendientes.get(acreedor).subtract(monto));
        }
        return transferencias;
    }

    // Lo que pagó un usuario con 2 decimales; 0.00 si no pagó nada.
    private BigDecimal pagadoPor(Integer usuarioId, Map<Integer, BigDecimal> pagos) {
        BigDecimal pagado = pagos.get(usuarioId);
        if (pagado == null) {
            pagado = BigDecimal.ZERO;
        }
        return dosDecimales(pagado);
    }

    // Posición del valor más grande. Si hay empate, gana el primero de la lista.
    private int posicionDelMayor(List<BigDecimal> valores) {
        int posicion = 0;
        for (int i = 1; i < valores.size(); i++) {
            if (valores.get(i).compareTo(valores.get(posicion)) > 0) {
                posicion = i;
            }
        }
        return posicion;
    }

    // Posición del valor más pequeño. Si hay empate, gana el primero de la lista.
    private int posicionDelMenor(List<BigDecimal> valores) {
        int posicion = 0;
        for (int i = 1; i < valores.size(); i++) {
            if (valores.get(i).compareTo(valores.get(posicion)) < 0) {
                posicion = i;
            }
        }
        return posicion;
    }

    // Deja un monto con exactamente 2 decimales (por ejemplo 0 -> 0.00).
    private BigDecimal dosDecimales(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }
}
