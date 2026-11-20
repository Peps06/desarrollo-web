package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.TareaRequest;
import com.bitacora.viajes.dto.TareaResponse;
import com.bitacora.viajes.exception.AccesoDenegadoException;
import com.bitacora.viajes.exception.DatosInvalidosException;
import com.bitacora.viajes.exception.RecursoNoEncontradoException;
import com.bitacora.viajes.model.Tarea;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import com.bitacora.viajes.repository.TareaRepository;
import com.bitacora.viajes.repository.UsuarioRepository;
import com.bitacora.viajes.repository.ViajeRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private ParticipanteViajeRepository participanteViajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<TareaResponse> listarPorViaje(Integer viajeId, String correoUsuario) {
        verificarViajeYParticipante(viajeId, correoUsuario);
        return tareaRepository.findByViajeIdOrderByIdAsc(viajeId)
                .stream()
                .map(TareaResponse::desde)
                .toList();
    }

    @Transactional
    public TareaResponse crearTarea(Integer viajeId, TareaRequest datos, String correoUsuario) {
        Viaje viaje = verificarViajeYParticipante(viajeId, correoUsuario);

        if (datos == null || datos.getDescripcion() == null || datos.getDescripcion().trim().isEmpty()) {
            throw new DatosInvalidosException("La descripción de la tarea es obligatoria");
        }

        String descripcionLimpia = datos.getDescripcion().trim();
        if (descripcionLimpia.length() > 255) {
            throw new DatosInvalidosException("La descripción no puede exceder los 255 caracteres");
        }

        Usuario creador = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario autenticado no encontrado"));

        Tarea nueva = new Tarea();
        nueva.setViaje(viaje);
        nueva.setCreador(creador);
        nueva.setDescripcion(descripcionLimpia);
        nueva.setCompletada(false);

        return TareaResponse.desde(tareaRepository.save(nueva));
    }

    @Transactional
    public TareaResponse toggleCompletada(Integer tareaId, String correoUsuario) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La tarea no existe"));

        verificarParticipante(tarea.getViaje().getId(), correoUsuario);

        tarea.setCompletada(!tarea.isCompletada());
        return TareaResponse.desde(tareaRepository.save(tarea));
    }

    @Transactional
    public void eliminarTarea(Integer tareaId, String correoUsuario) {
        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La tarea no existe"));

        verificarParticipante(tarea.getViaje().getId(), correoUsuario);

        tareaRepository.delete(tarea);
    }

    private Viaje verificarViajeYParticipante(Integer viajeId, String correoUsuario) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
        verificarParticipante(viajeId, correoUsuario);
        return viaje;
    }

    private void verificarParticipante(Integer viajeId, String correoUsuario) {
        participanteViajeRepository.buscarParticipacion(viajeId, correoUsuario)
                .orElseThrow(() -> new AccesoDenegadoException("No participas en este viaje"));
    }
}