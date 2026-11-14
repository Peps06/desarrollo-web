package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.dto.PresupuestoResponse;
import com.bitacora.viajes.service.PresupuestoService;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-16: endpoint para consultar el estado del presupuesto de un viaje.
 * El controlador solo recibe la petición; el cálculo lo hace PresupuestoService.
 */
@RestController
@RequestMapping("api")
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    public PresupuestoController(PresupuestoService presupuestoService) {
        this.presupuestoService = presupuestoService;
    }

    // Devuelve presupuesto total, gastado, restante, porcentaje usado y gasto por persona.
    @GetMapping("/viajes/{viajeId}/presupuesto")
    public ResponseEntity<PresupuestoResponse> consultarPresupuesto(@PathVariable Integer viajeId,
                                                                    Authentication autenticacion) {
        PresupuestoResponse respuesta = presupuestoService.obtenerPresupuesto(viajeId, autenticacion.getName());
        return ResponseEntity.ok(respuesta);
    }

    // Convierte los errores de negocio (404, 403) al formato {campo, mensaje}, igual que los demás controladores.
    @ExceptionHandler(ErrorNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarError(ErrorNegocioException e) {
        return ResponseEntity.status(e.getEstado()).body(new ErrorResponse(e.getCampo(), e.getMessage()));
    }
}
