package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.ViajeRequest;
import com.bitacora.viajes.dto.ViajeResponse;
import com.bitacora.viajes.model.EstadoViaje;
import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.model.Viaje;
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

    //Crea el viaje y registra al usuario actual como ORGANIZADOR en una sola transacción
    @Transactional
    public ViajeResponse crearViaje(ViajeRequest datos, String correoUsuario){
        //1. Validar que las fechas sean coherentes (fechaFin >= fechInicio)
        if (datos.getFechaFin().isBefore(datos.getFechaInicio())) {
            throw new ValidacionViajeException("fechaFin", "La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        //2. Valida presupuesto mayor a 0
        if (datos.getPresupuestoTotal() == null || datos.getPresupuestoTotal().compareTo(BigDecimal.ZERO) <= 0){
            throw new ValidacionViajeException("presupuestoTotal", "El presupuesto total debe ser mayor a 0");
        }

        //3. Validar monedaBase (3 letras, MXN por defecto si no se envía)
        String moneda = "MXN";
        if (datos.getMonedaBase() != null && !datos.getMonedaBase().trim().isEmpty()) {
            datos.getMonedaBase().trim().toUpperCase();
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

        //6. Registrar al creador en participante_viaje con rol ORGANIZADOR
        ParticipanteViaje participacion = new ParticipanteViaje(
                viajeGuardado,
                organizador,
                RolParticipante.ORGANIZADOR
        );
        participanteViajeRepository.save(participacion);

        return ViajeResponse.desde(viajeGuardado, RolParticipante.ORGANIZADOR);
    }

    @Transactional(readOnly = true)
    public List<ViajeResponse> listarMisViajes(String correoUsuario){
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new ValidacionViajeException("sesion", "Usuario autenticado no encontrado"));
        
        //Solo traemos los viajes con estado ACTIVO (excluye CANCELADO)
        List<ParticipanteViaje> participaciones = participanteViajeRepository
                .buscarPorUsuarioYEstadoViaje(usuario.getId(), EstadoViaje.ACTIVO);
                
        List<ViajeResponse>listaViajes = new ArrayList<>();
        for (ParticipanteViaje p : participaciones) {
            listaViajes.add(ViajeResponse.desdeParticipacion(p));
        }
        
        return listaViajes;
    }

    // Excepción sencilla para indicar qué campo falló al validar en el servicio
    public static class ValidacionViajeException extends RuntimeException{
        private final String campo;

        public ValidacionViajeException(String campo, String mensaje){
            super(mensaje);
            this.campo = campo;
        }

        public String getCampo() {
            return campo;
        }
    }
}