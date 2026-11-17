package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ActividadRequest;
import com.bitacora.viajes.dto.ActividadResponse;
import com.bitacora.viajes.model.Actividad;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.ActividadRepository;
import com.bitacora.viajes.repository.VotoRepository;
import com.bitacora.viajes.repository.VotoRepository.ConteoVotos;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActividadService {

    private final ViajeDetalleService viajeDetalleService;
    private final ActividadRepository actividadRepository;
    private final VotoRepository votoRepository;
    private final Validator validador;

    public ActividadService(ViajeDetalleService viajeDetalleService,
                            ActividadRepository actividadRepository,
                            VotoRepository votoRepository,
                            Validator validador) {
        this.viajeDetalleService = viajeDetalleService;
        this.actividadRepository = actividadRepository;
        this.votoRepository = votoRepository;
        this.validador = validador;
    }

    // Registra una actividad en el viaje. El autor es el usuario de la sesión y el día
    // debe estar entre las fechas de inicio y fin del viaje.
    @Transactional
    public ActividadResponse crear(Integer viajeId, ActividadRequest datos, String correoUsuario) {
        ParticipanteViaje participacion = viajeDetalleService.buscarParticipacion(viajeId, correoUsuario);
        Viaje viaje = participacion.getViaje();

        validarDia(viaje, datos.dia());

        Actividad actividad = new Actividad();
        actividad.setViaje(viaje);
        actividad.setAutor(participacion.getUsuario());
        aplicarDatos(actividad, datos);

        return ActividadResponse.desde(actividadRepository.save(actividad), 0, false);
    }

    // Lista las actividades del viaje, ordenadas por día y hora. Los filtros "dia" (yyyy-MM-dd)
    // y "costoMax" son opcionales y combinables; se aplican en la consulta a la base de datos.
    @Transactional(readOnly = true)
    public List<ActividadResponse> listar(Integer viajeId, String dia, String costoMax, String correoUsuario) {
        ParticipanteViaje participacion = viajeDetalleService.buscarParticipacion(viajeId, correoUsuario);
        LocalDate diaFiltro = leerDia(dia);
        BigDecimal costoFiltro = leerCostoMaximo(costoMax);

        // Una consulta para las actividades y otra para todos los conteos de votos del viaje.
        Map<Integer, ConteoVotos> votos = votoRepository
                .contarPorViaje(viajeId, participacion.getUsuario().getId()).stream()
                .collect(Collectors.toMap(ConteoVotos::getActividadId, Function.identity()));

        return actividadRepository.filtrarPorViaje(viajeId, diaFiltro, costoFiltro).stream()
                .map(a -> {
                    ConteoVotos conteo = votos.get(a.getId());
                    long total = conteo == null ? 0 : conteo.getTotal();
                    boolean votadaPorMi = conteo != null && conteo.getPropios().longValue() > 0;
                    return ActividadResponse.desde(a, total, votadaPorMi);
                })
                .toList();
    }

    // Edita una actividad. Solo su autor o el organizador del viaje pueden hacerlo.
    // Orden de errores: 404 (no existe), 403 (sin permiso) y 400 (datos inválidos).
    @Transactional
    public ActividadResponse editar(Integer actividadId, ActividadRequest datos, String correoUsuario) {
        Actividad actividad = buscarActividad(actividadId);
        ParticipanteViaje participacion = verificarPermiso(actividad, correoUsuario);

        validarDatos(datos);
        validarDia(actividad.getViaje(), datos.dia());
        aplicarDatos(actividad, datos);
        Actividad guardada = actividadRepository.save(actividad);

        long totalVotos = votoRepository.countByActividadId(actividadId);
        boolean votadaPorMi = votoRepository
                .findByActividadIdAndUsuarioId(actividadId, participacion.getUsuario().getId())
                .isPresent();
        return ActividadResponse.desde(guardada, totalVotos, votadaPorMi);
    }

    // Elimina una actividad y sus votos en la misma transacción. Mismos permisos que al editar.
    @Transactional
    public void eliminar(Integer actividadId, String correoUsuario) {
        Actividad actividad = buscarActividad(actividadId);
        verificarPermiso(actividad, correoUsuario);

        actividadRepository.eliminarVotos(actividadId);
        actividadRepository.delete(actividad);
    }

    // 404 si la actividad no existe.
    private Actividad buscarActividad(Integer actividadId) {
        return actividadRepository.buscarConViajeYAutor(actividadId)
                .orElseThrow(() -> new ErrorNegocioException(HttpStatus.NOT_FOUND, "actividad",
                        "La actividad no existe"));
    }

    // 403 si el usuario no participa en el viaje o no es el autor ni el organizador.
    private ParticipanteViaje verificarPermiso(Actividad actividad, String correoUsuario) {
        ParticipanteViaje participacion = viajeDetalleService
                .buscarParticipacion(actividad.getViaje().getId(), correoUsuario);

        boolean esOrganizador = participacion.getRol() == RolParticipante.ORGANIZADOR;
        boolean esAutor = actividad.getAutor().getId().equals(participacion.getUsuario().getId());
        if (!esOrganizador && !esAutor) {
            throw new ErrorNegocioException(HttpStatus.FORBIDDEN, "actividad",
                    "Solo el autor de la actividad o el organizador del viaje pueden modificarla");
        }
        return participacion;
    }

    // Aplica las mismas reglas de campos que al proponer una actividad (título, hora, costo, etc.).
    // Se valida aquí y no en el controlador para que los errores 404 y 403 se detecten antes.
    private void validarDatos(ActividadRequest datos) {
        validador.validate(datos).stream()
                .min(Comparator.comparing((ConstraintViolation<ActividadRequest> v) ->
                        v.getPropertyPath().toString()))
                .ifPresent(v -> {
                    throw new ErrorNegocioException(HttpStatus.BAD_REQUEST,
                            v.getPropertyPath().toString(), v.getMessage());
                });
    }

    // El día de la actividad debe estar entre las fechas de inicio y fin del viaje.
    private void validarDia(Viaje viaje, LocalDate dia) {
        if (dia.isBefore(viaje.getFechaInicio()) || dia.isAfter(viaje.getFechaFin())) {
            throw new ErrorNegocioException(HttpStatus.BAD_REQUEST, "dia",
                    "El día debe estar dentro de las fechas del viaje");
        }
    }

    // Copia los datos del cliente a la actividad, normalizando textos y costo.
    private void aplicarDatos(Actividad actividad, ActividadRequest datos) {
        actividad.setTitulo(datos.titulo().trim());
        actividad.setDescripcion(datos.descripcion() == null || datos.descripcion().isBlank()
                ? null : datos.descripcion().trim());
        actividad.setDia(datos.dia());
        actividad.setHora(datos.hora());
        actividad.setCostoEstimado((datos.costoEstimado() == null ? BigDecimal.ZERO : datos.costoEstimado())
                .setScale(2, RoundingMode.HALF_UP));
    }

    // Convierte el filtro "dia" a fecha. Vacío significa sin filtro; formato incorrecto responde 400.
    private LocalDate leerDia(String dia) {
        if (dia == null || dia.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dia.trim());
        } catch (DateTimeParseException e) {
            throw new ErrorNegocioException(HttpStatus.BAD_REQUEST, "dia",
                    "La fecha debe tener el formato yyyy-MM-dd");
        }
    }

    // Convierte el filtro "costoMax" a número. Vacío significa sin filtro; inválido o negativo responde 400.
    private BigDecimal leerCostoMaximo(String costoMax) {
        if (costoMax == null || costoMax.isBlank()) {
            return null;
        }
        BigDecimal costo;
        try {
            costo = new BigDecimal(costoMax.trim());
        } catch (NumberFormatException e) {
            throw new ErrorNegocioException(HttpStatus.BAD_REQUEST, "costoMax",
                    "El costo máximo debe ser un número");
        }
        if (costo.signum() < 0) {
            throw new ErrorNegocioException(HttpStatus.BAD_REQUEST, "costoMax",
                    "El costo máximo no puede ser negativo");
        }
        return costo;
    }
}
