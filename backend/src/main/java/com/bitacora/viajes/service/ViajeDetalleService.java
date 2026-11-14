package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ActividadResponse;
import com.bitacora.viajes.dto.DiaResponse;
import com.bitacora.viajes.dto.ViajeDetalleResponse;
import com.bitacora.viajes.dto.ViajeDetalleResponse.Participante;
import com.bitacora.viajes.dto.ViajeResponse;
import com.bitacora.viajes.model.Actividad;
import com.bitacora.viajes.model.EstadoViaje;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.ActividadRepository;
import com.bitacora.viajes.repository.GastoRepository;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import com.bitacora.viajes.repository.ViajeRepository;
import com.bitacora.viajes.repository.VotoRepository;
import com.bitacora.viajes.repository.VotoRepository.ConteoVotos;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViajeDetalleService {

    private final ViajeRepository viajeRepository;
    private final ParticipanteViajeRepository participanteViajeRepository;
    private final ActividadRepository actividadRepository;
    private final VotoRepository votoRepository;
    private final GastoRepository gastoRepository;

    public ViajeDetalleService(ViajeRepository viajeRepository,
            ParticipanteViajeRepository participanteViajeRepository,
            ActividadRepository actividadRepository,
            VotoRepository votoRepository,
            GastoRepository gastoRepository) {
        this.viajeRepository = viajeRepository;
        this.participanteViajeRepository = participanteViajeRepository;
        this.actividadRepository = actividadRepository;
        this.votoRepository = votoRepository;
        this.gastoRepository = gastoRepository;
    }

    // Arma el detalle del viaje con una consulta por cada tipo de dato (participación, participantes,
    // actividades, votos y gastos), sin importar cuántos días o actividades tenga.
    @Transactional(readOnly = true)
    public ViajeDetalleResponse obtenerDetalle(Integer viajeId, String correoUsuario) {
        ParticipanteViaje participacion = buscarParticipacion(viajeId, correoUsuario);
        Viaje viaje = participacion.getViaje();

        List<Participante> participantes = participanteViajeRepository.buscarPorViaje(viajeId).stream()
                .map(p -> new Participante(p.getUsuario().getId(), p.getUsuario().getNombre(), p.getRol()))
                .toList();

        Map<Integer, ConteoVotos> votos = votoRepository
                .contarPorViaje(viajeId, participacion.getUsuario().getId()).stream()
                .collect(Collectors.toMap(ConteoVotos::getActividadId, Function.identity()));

        // Las actividades llegan ordenadas por día y hora, y agrupar conserva ese orden dentro de cada día.
        Map<LocalDate, List<ActividadResponse>> actividadesPorDia = actividadRepository.buscarPorViaje(viajeId)
                .stream()
                .collect(Collectors.groupingBy(Actividad::getDia,
                        Collectors.mapping(a -> aRespuesta(a, votos.get(a.getId())), Collectors.toList())));

        // Un día por cada fecha del viaje, aunque no tenga actividades.
        List<DiaResponse> dias = viaje.getFechaInicio().datesUntil(viaje.getFechaFin().plusDays(1))
                .map(fecha -> new DiaResponse(fecha, actividadesPorDia.getOrDefault(fecha, List.of())))
                .toList();

        BigDecimal totalGastado = gastoRepository.sumarMontoPorViaje(viajeId);
        totalGastado = (totalGastado == null ? BigDecimal.ZERO : totalGastado).setScale(2, RoundingMode.HALF_UP);

        return new ViajeDetalleResponse(ViajeResponse.desde(viaje, participacion.getRol()),
                totalGastado, participantes, dias);
    }

    // Devuelve la participación del usuario en el viaje, con el viaje y el usuario ya cargados.
    // 404 si el viaje no existe o está cancelado, 403 si el usuario no participa en él.
    public ParticipanteViaje buscarParticipacion(Integer viajeId, String correoUsuario) {
        ParticipanteViaje participacion = participanteViajeRepository
                .buscarParticipacion(viajeId, correoUsuario).orElse(null);

        if (participacion == null) {
            if (!viajeRepository.existsById(viajeId)) {
                throw new ErrorNegocioException(HttpStatus.NOT_FOUND, "viaje", "El viaje no existe");
            }
            throw new ErrorNegocioException(HttpStatus.FORBIDDEN, "viaje", "No participas en este viaje");
        }
        if (participacion.getViaje().getEstado() == EstadoViaje.CANCELADO) {
            throw new ErrorNegocioException(HttpStatus.NOT_FOUND, "viaje", "El viaje no existe");
        }
        return participacion;
    }

    private static ActividadResponse aRespuesta(Actividad actividad, ConteoVotos conteo) {
        long total = conteo == null ? 0 : conteo.getTotal();
        boolean votadaPorMi = conteo != null && conteo.getPropios().longValue() > 0;
        return ActividadResponse.desde(actividad, total, votadaPorMi);
    }

    // Error de negocio con el código HTTP que le corresponde y el campo afectado.
    public static class ErrorNegocioException extends RuntimeException {

        private final HttpStatus estado;
        private final String campo;

        public ErrorNegocioException(HttpStatus estado, String campo, String mensaje) {
            super(mensaje);
            this.estado = estado;
            this.campo = campo;
        }

        public HttpStatus getEstado() {
            return estado;
        }

        public String getCampo() {
            return campo;
        }
    }
}
