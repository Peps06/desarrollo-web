package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ParticipanteRequest;
import com.bitacora.viajes.dto.ParticipanteResponse;
import com.bitacora.viajes.exception.AccesoDenegadoException;
import com.bitacora.viajes.exception.DatosInvalidosException;
import com.bitacora.viajes.exception.RecursoNoEncontradoException;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import com.bitacora.viajes.repository.UsuarioRepository;
import com.bitacora.viajes.repository.ViajeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParticipanteService {

    private final ViajeRepository viajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final ParticipanteViajeRepository participanteViajeRepository;

    public ParticipanteService(
            ViajeRepository viajeRepository,
            UsuarioRepository usuarioRepository,
            ParticipanteViajeRepository participanteViajeRepository) {
        this.viajeRepository = viajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.participanteViajeRepository = participanteViajeRepository;
    }

    @Transactional(readOnly = true)
    public List<ParticipanteResponse> listarParticipantes(Integer viajeId, String correoAutenticado) {
        Viaje viaje = obtenerViajeError(viajeId);

        participanteViajeRepository.buscarParticipacion(viaje.getId(), correoAutenticado)
                .orElseThrow(() -> new AccesoDenegadoException("No participas en este viaje"));

        return participanteViajeRepository.buscarPorViaje(viaje.getId())
                .stream()
                .map(ParticipanteResponse::desdeEntidad)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipanteResponse agregarParticipante(
            Integer viajeId, ParticipanteRequest request, String correoAutenticado) {
        Viaje viaje = obtenerViajeError(viajeId);
        verificarOrganizador(viaje.getId(), correoAutenticado);

        if (request == null || request.getCorreo() == null || request.getCorreo().trim().isEmpty()) {
            throw new DatosInvalidosException("El correo del participante es obligatorio");
        }

        String correoNormalizado = request.getCorreo().trim().toLowerCase();

        Usuario usuarioAgregar = usuarioRepository.findByCorreo(correoNormalizado)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta registrada con ese correo"));

        if (participanteViajeRepository.buscarPorViajeYUsuarioId(viaje.getId(), usuarioAgregar.getId()).isPresent()) {
            throw new DatosInvalidosException("El usuario ya es participante de este viaje");
        }

        ParticipanteViaje nuevoParticipante = new ParticipanteViaje(
                viaje,
                usuarioAgregar,
                RolParticipante.PARTICIPANTE
        );

        ParticipanteViaje guardado = participanteViajeRepository.save(nuevoParticipante);
        return ParticipanteResponse.desdeEntidad(guardado);
    }

    @Transactional
    public void quitarParticipante(Integer viajeId, Integer usuarioIdEliminar, String correoAutenticado) {
        Viaje viaje = obtenerViajeError(viajeId);
        ParticipanteViaje organizador = verificarOrganizador(viaje.getId(), correoAutenticado);

        if (organizador.getUsuario().getId().equals(usuarioIdEliminar)) {
            throw new DatosInvalidosException("El organizador no puede eliminarse a sí mismo del viaje");
        }

        ParticipanteViaje participacionEliminar = participanteViajeRepository
                .buscarPorViajeYUsuarioId(viaje.getId(), usuarioIdEliminar)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no participa en este viaje"));

        participanteViajeRepository.delete(participacionEliminar);
    }

    private Viaje obtenerViajeError(Integer viajeId) {
        return viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
    }

    private ParticipanteViaje verificarOrganizador(Integer viajeId, String correoAutenticado) {
        ParticipanteViaje participacion = participanteViajeRepository
                .buscarParticipacion(viajeId, correoAutenticado)
                .orElseThrow(() -> new AccesoDenegadoException("No participas en este viaje"));

        if (participacion.getRol() != RolParticipante.ORGANIZADOR) {
            throw new AccesoDenegadoException("Solo el organizador puede gestionar participantes");
        }
        return participacion;
    }
}