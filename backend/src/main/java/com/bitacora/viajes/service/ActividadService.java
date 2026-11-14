package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ActividadRequest;
import com.bitacora.viajes.dto.ActividadResponse;
import com.bitacora.viajes.model.Actividad;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.ActividadRepository;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActividadService {

    private final ViajeDetalleService viajeDetalleService;
    private final ActividadRepository actividadRepository;

    public ActividadService(ViajeDetalleService viajeDetalleService, ActividadRepository actividadRepository) {
        this.viajeDetalleService = viajeDetalleService;
        this.actividadRepository = actividadRepository;
    }

    // Registra una actividad en el viaje. El autor es el usuario de la sesión y el día
    // debe estar entre las fechas de inicio y fin del viaje.
    @Transactional
    public ActividadResponse crear(Integer viajeId, ActividadRequest datos, String correoUsuario) {
        ParticipanteViaje participacion = viajeDetalleService.buscarParticipacion(viajeId, correoUsuario);
        Viaje viaje = participacion.getViaje();

        if (datos.dia().isBefore(viaje.getFechaInicio()) || datos.dia().isAfter(viaje.getFechaFin())) {
            throw new ErrorNegocioException(HttpStatus.BAD_REQUEST, "dia",
                    "El día debe estar dentro de las fechas del viaje");
        }

        Actividad actividad = new Actividad();
        actividad.setViaje(viaje);
        actividad.setAutor(participacion.getUsuario());
        actividad.setTitulo(datos.titulo().trim());
        actividad.setDescripcion(datos.descripcion() == null || datos.descripcion().isBlank()
                ? null : datos.descripcion().trim());
        actividad.setDia(datos.dia());
        actividad.setHora(datos.hora());
        actividad.setCostoEstimado((datos.costoEstimado() == null ? BigDecimal.ZERO : datos.costoEstimado())
                .setScale(2, RoundingMode.HALF_UP));

        return ActividadResponse.desde(actividadRepository.save(actividad), 0, false);
    }
}
