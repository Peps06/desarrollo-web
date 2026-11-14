package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ViajeRequest;
import com.bitacora.viajes.dto.ViajeResponse;
import com.bitacora.viajes.exception.AccesoDenegadoException;
import com.bitacora.viajes.exception.DatosInvalidosException;
import com.bitacora.viajes.exception.RecursoNoEncontradoException;
import com.bitacora.viajes.model.EstadoViaje;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.ActividadRepository;
import com.bitacora.viajes.repository.ParticipanteViajeRepository;
import com.bitacora.viajes.repository.UsuarioRepository;
import com.bitacora.viajes.repository.ViajeRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViajeService {

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private ParticipanteViajeRepository participanteViajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    // Crea el viaje y registra al usuario actual como ORGANIZADOR en una sola transacción
    @Transactional
    public ViajeResponse crearViaje(ViajeRequest datos, String correoUsuario) {
        if (datos.getFechaFin().isBefore(datos.getFechaInicio())) {
            throw new ValidacionViajeException("fechaFin", "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (datos.getPresupuestoTotal() == null || datos.getPresupuestoTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidacionViajeException("presupuestoTotal", "El presupuesto total debe ser mayor a 0");
        }

        String moneda = "MXN";
        if (datos.getMonedaBase() != null && !datos.getMonedaBase().trim().isEmpty()) {
            moneda = datos.getMonedaBase().trim().toUpperCase();
            if (!moneda.matches("^[A-Z]{3}$")) {
                throw new ValidacionViajeException("monedaBase", "La moneda base debe ser un código de 3 letras (ej. MXN, USD, EUR)");
            }
        }

        Usuario organizador = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ValidacionViajeException("sesion", "Usuario autenticado no encontrado"));

        Viaje nuevoViaje = new Viaje();
        nuevoViaje.setDestino(datos.getDestino().trim());
        nuevoViaje.setFechaInicio(datos.getFechaInicio());
        nuevoViaje.setFechaFin(datos.getFechaFin());
        nuevoViaje.setPresupuestoTotal(datos.getPresupuestoTotal());
        nuevoViaje.setMonedaBase(moneda);
        nuevoViaje.setEstado(EstadoViaje.ACTIVO);

        Viaje viajeGuardado = viajeRepository.save(nuevoViaje);

        ParticipanteViaje participacion = new ParticipanteViaje(
                viajeGuardado,
                organizador,
                RolParticipante.ORGANIZADOR
        );
        participanteViajeRepository.save(participacion);

        return ViajeResponse.desde(viajeGuardado, RolParticipante.ORGANIZADOR);
    }

    @Transactional(readOnly = true)
    public List<ViajeResponse> listarMisViajes(String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ValidacionViajeException("sesion", "Usuario autenticado no encontrado"));

        List<ParticipanteViaje> participaciones = participanteViajeRepository
                .buscarPorUsuarioYEstadoViaje(usuario.getId(), EstadoViaje.ACTIVO);

        List<ViajeResponse> listaViajes = new ArrayList<>();
        for (ParticipanteViaje p : participaciones) {
            listaViajes.add(ViajeResponse.desdeParticipacion(p));
        }

        return listaViajes;
    }

    // Edita los datos de un viaje activo (solo ORGANIZADOR)
    @Transactional
    public ViajeResponse editarViaje(Integer viajeId, ViajeRequest datos, String correoUsuario) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));

        verificarOrganizador(viajeId, correoUsuario);

        if (viaje.getEstado() == EstadoViaje.CANCELADO) {
            throw new DatosInvalidosException("No se puede editar un viaje cancelado");
        }

        if (datos.getDestino() == null || datos.getDestino().trim().isEmpty()) {
            throw new DatosInvalidosException("El destino es obligatorio");
        }
        if (datos.getFechaInicio() == null || datos.getFechaFin() == null) {
            throw new DatosInvalidosException("Las fechas de inicio y fin son obligatorias");
        }
        if (datos.getFechaFin().isBefore(datos.getFechaInicio())) {
            throw new DatosInvalidosException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (datos.getPresupuestoTotal() == null || datos.getPresupuestoTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInvalidosException("El presupuesto total debe ser mayor a 0");
        }

        String moneda = viaje.getMonedaBase() != null ? viaje.getMonedaBase() : "MXN";
        if (datos.getMonedaBase() != null && !datos.getMonedaBase().trim().isEmpty()) {
            moneda = datos.getMonedaBase().trim().toUpperCase();
            if (!moneda.matches("^[A-Z]{3}$")) {
                throw new DatosInvalidosException("La moneda base debe ser un código de 3 letras (ej. MXN, USD, EUR)");
            }
        }

        long actividadesFuera = actividadRepository.contarFueraDeRango(
                viaje.getId(), datos.getFechaInicio(), datos.getFechaFin());
        if (actividadesFuera > 0) {
            throw new DatosInvalidosException(
                    "No se pueden acortar las fechas porque hay " + actividadesFuera
                    + " actividad(es) programada(s) fuera del nuevo rango ("
                    + datos.getFechaInicio() + " a " + datos.getFechaFin() + ")");
        }

        viaje.setDestino(datos.getDestino().trim());
        viaje.setFechaInicio(datos.getFechaInicio());
        viaje.setFechaFin(datos.getFechaFin());
        viaje.setPresupuestoTotal(datos.getPresupuestoTotal());
        viaje.setMonedaBase(moneda);

        Viaje actualizado = viajeRepository.save(viaje);
        return ViajeResponse.desde(actualizado, RolParticipante.ORGANIZADOR);
    }

    //Cancela un viaje (borrado lógico -> estado CANCELADO, solo ORGANIZADOR)
    @Transactional
    public void cancelarViaje(Integer viajeId, String correoUsuario) {
        Viaje viaje = viajeRepository.findById(viajeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));

        verificarOrganizador(viajeId, correoUsuario);

        if (viaje.getEstado() == EstadoViaje.CANCELADO) {
            throw new DatosInvalidosException("El viaje ya se encuentra cancelado");
        }

        viaje.setEstado(EstadoViaje.CANCELADO);
        viajeRepository.save(viaje);
    }

    private ParticipanteViaje verificarOrganizador(Integer viajeId, String correoUsuario) {
        ParticipanteViaje participacion = participanteViajeRepository
                .buscarParticipacion(viajeId, correoUsuario)
                .orElseThrow(() -> new AccesoDenegadoException("No participas en este viaje"));

        if (participacion.getRol() != RolParticipante.ORGANIZADOR) {
            throw new AccesoDenegadoException("Solo el organizador puede editar o cancelar el viaje");
        }
        return participacion;
    }

    public static class ValidacionViajeException extends RuntimeException {
        private final String campo;

        public ValidacionViajeException(String campo, String mensaje) {
            super(mensaje);
            this.campo = campo;
        }

        public String getCampo() {
            return campo;
        }
    }
}