package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.RolParticipante;
import java.math.BigDecimal;
import java.util.List;

// Detalle de un viaje: los datos generales de ViajeResponse más el total gastado,
// los participantes y una entrada por cada día entre la fecha de inicio y la de fin.
public class ViajeDetalleResponse extends ViajeResponse {

    private final BigDecimal totalGastado;
    private final List<Participante> participantes;
    private final List<DiaResponse> dias;

    public ViajeDetalleResponse(ViajeResponse viaje, BigDecimal totalGastado,
                                List<Participante> participantes, List<DiaResponse> dias) {
        super(viaje.getId(), viaje.getDestino(), viaje.getFechaInicio(), viaje.getFechaFin(),
                viaje.getPresupuestoTotal(), viaje.getMonedaBase(), viaje.getRol());
        this.totalGastado = totalGastado;
        this.participantes = participantes;
        this.dias = dias;
    }

    public record Participante(Integer usuarioId, String nombre, RolParticipante rol) {
    }

    public BigDecimal getTotalGastado() {
        return totalGastado;
    }

    public List<Participante> getParticipantes() {
        return participantes;
    }

    public List<DiaResponse> getDias() {
        return dias;
    }
}
