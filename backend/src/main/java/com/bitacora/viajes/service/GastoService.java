// HU-15, HU-20, HU-25 | Responsable: Citlaly | Lógica de negocio de gastos.
package com.bitacora.viajes.service;

import com.bitacora.viajes.dto.GastoRegistradoResponse;
import com.bitacora.viajes.dto.GastoRequest;
import com.bitacora.viajes.dto.GastoResponse;
import com.bitacora.viajes.exception.AccesoDenegadoException;
import com.bitacora.viajes.exception.DatosInvalidosException;
import com.bitacora.viajes.exception.RecursoNoEncontradoException;
import com.bitacora.viajes.model.Gasto;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.repository.GastoRepository;
import com.bitacora.viajes.repository.UsuarioRepository;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Edición y eliminación de gastos con control de permisos.
 * Solo puede modificar o borrar el gasto quien lo registró o el organizador del viaje.
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
    private AlmacenamientoService almacenamientoService;

    /**
     * Actualiza un gasto y regresa el gasto con el total del viaje recalculado.
     */
    @Transactional
    public GastoRegistradoResponse actualizar(Integer id, GastoRequest request, String email) {
        Gasto gasto = buscarGasto(id);
        validarPermiso(gasto, buscarUsuario(email));
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

        String comprobante = gasto.getImagenUrl();
        gastoRepository.delete(gasto);

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

    // Solo el autor del gasto o el organizador del viaje pueden cambiarlo
    private void validarPermiso(Gasto gasto, Usuario usuario) {
        boolean esAutor = gasto.getUsuario().getId().equals(usuario.getId());
        boolean esOrganizador = gastoRepository.esRolEnViaje(
                gasto.getViaje().getId(), usuario.getId(), RolParticipante.ORGANIZADOR);

        if (!esAutor && !esOrganizador) {
            throw new AccesoDenegadoException("No tienes permiso para modificar este gasto");
        }
    }

    // Mismas reglas que al registrar: monto > 0, concepto obligatorio, categoría válida
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
    }
}
