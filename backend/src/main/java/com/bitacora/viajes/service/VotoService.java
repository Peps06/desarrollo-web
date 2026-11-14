package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.VotoResponse;
import com.bitacora.viajes.model.Actividad;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.Voto;
import com.bitacora.viajes.repository.ActividadRepository;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import com.bitacora.viajes.repository.VotoRepository;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-14: lógica de votar una actividad y quitar el voto propio.
 * El usuario siempre se toma de la sesión, nunca lo manda el cliente.
 */
@Service
public class VotoService {

    private final ActividadRepository actividadRepository;
    private final ParticipanteViajeRepository participanteViajeRepository;
    private final VotoRepository votoRepository;

    public VotoService(ActividadRepository actividadRepository,
                       ParticipanteViajeRepository participanteViajeRepository,
                       VotoRepository votoRepository) {
        this.actividadRepository = actividadRepository;
        this.participanteViajeRepository = participanteViajeRepository;
        this.votoRepository = votoRepository;
    }

    // Registra el voto del usuario sobre la actividad y devuelve el contador actualizado.
    @Transactional
    public VotoResponse votar(Integer actividadId, String correoUsuario) {
        Actividad actividad = buscarActividad(actividadId);
        ParticipanteViaje participacion = buscarParticipacion(actividad, correoUsuario);

        // Un usuario solo puede votar una vez por actividad.
        // Si dos peticiones llegaran al mismo tiempo, la restricción UNIQUE
        // (actividad_id, usuario_id) de MySQL rechaza la segunda de todos modos.
        Voto votoExistente = votoRepository
                .findByActividadIdAndUsuarioId(actividadId, participacion.getUsuario().getId())
                .orElse(null);
        if (votoExistente != null) {
            throw new ErrorNegocioException(HttpStatus.BAD_REQUEST, "voto",
                    "Ya votaste por esta actividad");
        }

        votoRepository.save(new Voto(actividad, participacion.getUsuario()));
        return armarRespuesta(actividadId, true);
    }

    // Quita el voto del usuario sobre la actividad y devuelve el contador actualizado.
    @Transactional
    public VotoResponse quitarVoto(Integer actividadId, String correoUsuario) {
        Actividad actividad = buscarActividad(actividadId);
        ParticipanteViaje participacion = buscarParticipacion(actividad, correoUsuario);

        Voto voto = votoRepository
                .findByActividadIdAndUsuarioId(actividadId, participacion.getUsuario().getId())
                .orElse(null);
        if (voto == null) {
            throw new ErrorNegocioException(HttpStatus.NOT_FOUND, "voto",
                    "No has votado por esta actividad");
        }

        votoRepository.delete(voto);
        return armarRespuesta(actividadId, false);
    }

    // 404 si la actividad no existe.
    private Actividad buscarActividad(Integer actividadId) {
        Actividad actividad = actividadRepository.findById(actividadId).orElse(null);
        if (actividad == null) {
            throw new ErrorNegocioException(HttpStatus.NOT_FOUND, "actividad",
                    "La actividad no existe");
        }
        return actividad;
    }

    // 403 si el usuario no participa en el viaje al que pertenece la actividad.
    private ParticipanteViaje buscarParticipacion(Actividad actividad, String correoUsuario) {
        ParticipanteViaje participacion = participanteViajeRepository
                .buscarParticipacion(actividad.getViaje().getId(), correoUsuario)
                .orElse(null);
        if (participacion == null) {
            throw new ErrorNegocioException(HttpStatus.FORBIDDEN, "viaje",
                    "No participas en este viaje");
        }
        return participacion;
    }

    // Respuesta común de ambos endpoints: el total ya actualizado.
    private VotoResponse armarRespuesta(Integer actividadId, boolean votadaPorMi) {
        long totalVotos = votoRepository.countByActividadId(actividadId);
        return new VotoResponse(actividadId, totalVotos, votadaPorMi);
    }
}
