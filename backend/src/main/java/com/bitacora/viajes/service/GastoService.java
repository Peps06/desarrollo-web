// HU-15, HU-20, HU-25 | Responsable: Citlaly | Lógica de negocio de gastos.
package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.GastoRegistradoResponse;
import com.bitacora.viajes.dto.GastoRequest;
import com.bitacora.viajes.dto.GastoResponse;
import com.bitacora.viajes.exception.AccesoDenegadoException;
import com.bitacora.viajes.exception.DatosInvalidosException;
import com.bitacora.viajes.exception.RecursoNoEncontradoException;
import com.bitacora.viajes.model.EstadoViaje;
import com.bitacora.viajes.model.Gasto;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.model.Viaje;
import com.bitacora.viajes.repository.GastoRepository;
import com.bitacora.viajes.repository.UsuarioRepository;
import com.bitacora.viajes.repository.ViajeRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro, edición y eliminación de gastos con control de permisos.
 *
 * @author Citlaly Morales Viveros
 */
@Service
public class GastoService {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ViajeRepository viajeRepository;

    @Autowired
    private AlmacenamientoService almacenamientoService;

    /**
     * Registra un gasto nuevo en un viaje en el que el usuario participa.
     */
    @Transactional
    public GastoRegistradoResponse registrar(GastoRequest request, String email) {
        validarDatos(request);
        if (request.getViajeId() == null) {
            throw new DatosInvalidosException("El viaje es obligatorio");
        }

        Usuario usuario = buscarUsuario(email);
        Viaje viaje = buscarViaje(request.getViajeId());
        validarViajeActivo(viaje);

        if (!gastoRepository.existeParticipacion(viaje.getId(), usuario.getId())) {
            throw new AccesoDenegadoException("No participas en este viaje");
        }

        Gasto gasto = new Gasto();
        gasto.setViaje(viaje);
        gasto.setUsuario(usuario);
        gasto.setConcepto(request.getConcepto().trim());
        gasto.setCategoria(request.getCategoria());
        gasto.setMonto(request.getMonto());

        // Si no mandan moneda, se asume MXN y tipo de cambio 1
        String moneda = request.getMonedaOriginal() != null ? request.getMonedaOriginal() : "MXN";
        BigDecimal montoOriginal = request.getMontoOriginal() != null
                ? request.getMontoOriginal() : request.getMonto();
        BigDecimal tipoCambio = request.getTipoCambio() != null ? request.getTipoCambio() : BigDecimal.ONE;

        gasto.setMonedaOriginal(moneda);
        gasto.setMontoOriginal(montoOriginal);
        gasto.setTipoCambio(tipoCambio);

        Gasto guardado = gastoRepository.save(gasto);
        BigDecimal total = gastoRepository.sumarMontoPorViaje(viaje.getId());
        return new GastoRegistradoResponse(new GastoResponse(guardado), total);
    }

    /**
     * Actualiza un gasto y regresa el gasto con el total del viaje recalculado.
     */
    @Transactional
    public GastoRegistradoResponse actualizar(Integer id, GastoRequest request, String email) {
        Gasto gasto = buscarGasto(id);
        validarPermiso(gasto, buscarUsuario(email));
        validarViajeActivo(gasto.getViaje());
        validarDatos(request);

        gasto.setConcepto(request.getConcepto().trim());
        gasto.setCategoria(request.getCategoria());
        gasto.setMonto(request.getMonto());

        Gasto guardado = gastoRepository.save(gasto);
        BigDecimal total = gastoRepository.sumarMontoPorViaje(guardado.getViaje().getId());
        return new GastoRegistradoResponse(new GastoResponse(guardado), total);
    }

    /**
     * Elimina un gasto. Si tenía comprobante, también borra el archivo.
     */
    @Transactional
    public void eliminar(Integer id, String email) {
        Gasto gasto = buscarGasto(id);
        validarPermiso(gasto, buscarUsuario(email));
        validarViajeActivo(gasto.getViaje());

        String comprobante = gasto.getImagenUrl();
        gastoRepository.delete(gasto);

        // Si borrar el archivo falla, la excepción revierte el delete de la base
        if (comprobante != null && !comprobante.isBlank()) {
            almacenamientoService.eliminar(comprobante);
        }
    }

    private Gasto buscarGasto(Integer id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Gasto no encontrado"));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Viaje buscarViaje(Integer id) {
        return viajeRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));
    }

    // Solo el autor del gasto o el organizador del viaje pueden cambiarlo
    private void validarPermiso(Gasto gasto, Usuario usuario) {
        boolean esAutor = gasto.getUsuario().getId().equals(usuario.getId());
        boolean esOrganizador = gastoRepository.esRolEnViaje(
                gasto.getViaje().getId(), usuario.getId(), RolParticipante.ORGANIZADOR);

        if (!esAutor && !esOrganizador) {
            throw new AccesoDenegadoException("No tienes permiso para modificar este gasto");
        }
    }

    // Los gastos de un viaje cancelado ya no se pueden tocar
    private void validarViajeActivo(Viaje viaje) {
        if (viaje.getEstado() == EstadoViaje.CANCELADO) {
            throw new DatosInvalidosException("No se pueden modificar gastos de un viaje cancelado");
        }
    }

    private void validarDatos(GastoRequest request) {
        if (request.getMonto() == null || request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInvalidosException("El monto debe ser mayor a cero");
        }
        if (request.getConcepto() == null || request.getConcepto().isBlank()) {
            throw new DatosInvalidosException("El concepto es obligatorio");
        }
        if (request.getConcepto().trim().length() > 150) {
            throw new DatosInvalidosException("El concepto no puede pasar de 150 caracteres");
        }
        if (request.getCategoria() == null) {
            throw new DatosInvalidosException("La categoría no es válida");
        }
        if (request.getMonedaOriginal() != null && !request.getMonedaOriginal().matches("^[A-Z]{3}$")) {
            throw new DatosInvalidosException("La moneda debe ser un código de 3 letras (ej. MXN, USD)");
        }
        if (request.getMontoOriginal() != null && request.getMontoOriginal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInvalidosException("El monto original debe ser mayor a cero");
        }
        if (request.getTipoCambio() != null && request.getTipoCambio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInvalidosException("El tipo de cambio debe ser mayor a cero");
        }
    }
}