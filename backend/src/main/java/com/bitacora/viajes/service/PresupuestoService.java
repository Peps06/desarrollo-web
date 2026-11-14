package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.PresupuestoResponse;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.GastoRepository;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-16: calcula el estado del presupuesto de un viaje.
 * Toda la cuenta vive aquí y no en el controlador.
 */
@Service
public class PresupuestoService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ViajeDetalleService viajeDetalleService;
    private final ParticipanteViajeRepository participanteViajeRepository;
    private final GastoRepository gastoRepository;

    public PresupuestoService(ViajeDetalleService viajeDetalleService,
                              ParticipanteViajeRepository participanteViajeRepository,
                              GastoRepository gastoRepository) {
        this.viajeDetalleService = viajeDetalleService;
        this.participanteViajeRepository = participanteViajeRepository;
        this.gastoRepository = gastoRepository;
    }

    // Junta los datos del viaje desde la base de datos y hace el cálculo.
    @Transactional(readOnly = true)
    public PresupuestoResponse obtenerPresupuesto(Integer viajeId, String correoUsuario) {
        // Reutiliza la validación del detalle del viaje (HU-12):
        // 404 si el viaje no existe o está cancelado, 403 si el usuario no participa.
        ParticipanteViaje participacion = viajeDetalleService.buscarParticipacion(viajeId, correoUsuario);
        Viaje viaje = participacion.getViaje();

        BigDecimal totalGastado = gastoRepository.sumarMontoPorViaje(viajeId);
        int numParticipantes = participanteViajeRepository.buscarPorViaje(viajeId).size();

        return calcular(viaje.getPresupuestoTotal(), totalGastado, numParticipantes, viaje.getMonedaBase());
    }

    // Hace las cuentas con los datos ya obtenidos. Es público y no usa la base de datos,
    // así las pruebas JUnit lo pueden llamar directamente con números de ejemplo.
    public PresupuestoResponse calcular(BigDecimal presupuestoTotal, BigDecimal totalGastado,
                                        int numParticipantes, String monedaBase) {
        // Si el viaje no tiene gastos, el total es 0.
        if (totalGastado == null) {
            totalGastado = BigDecimal.ZERO;
        }

        // Puede quedar negativo si se excedió el presupuesto.
        BigDecimal restante = presupuestoTotal.subtract(totalGastado);

        // porcentajeUsado = totalGastado / presupuestoTotal * 100.
        // Se multiplica antes de dividir para no perder decimales.
        // El presupuesto siempre es mayor a 0 (lo valida HU-10 y la BD), así que nunca se divide entre cero.
        BigDecimal porcentajeUsado = totalGastado.multiply(CIEN)
                .divide(presupuestoTotal, 2, RoundingMode.HALF_UP);

        // gastoPorPersona = totalGastado / participantes (siempre hay al menos el organizador).
        BigDecimal gastoPorPersona = BigDecimal.ZERO;
        if (numParticipantes > 0) {
            gastoPorPersona = totalGastado.divide(new BigDecimal(numParticipantes), 2, RoundingMode.HALF_UP);
        }

        return new PresupuestoResponse(
                dosDecimales(presupuestoTotal),
                dosDecimales(totalGastado),
                dosDecimales(restante),
                porcentajeUsado,
                numParticipantes,
                gastoPorPersona,
                monedaBase);
    }

    // Deja un monto con exactamente 2 decimales (por ejemplo 0 -> 0.00).
    private BigDecimal dosDecimales(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP);
    }
}
