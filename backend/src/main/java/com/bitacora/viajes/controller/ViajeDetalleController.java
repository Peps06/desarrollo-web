package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.dto.ViajeDetalleResponse;
import com.bitacora.viajes.service.ViajeDetalleService;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/viajes")
public class ViajeDetalleController {

    private final ViajeDetalleService viajeDetalleService;

    public ViajeDetalleController(ViajeDetalleService viajeDetalleService) {
        this.viajeDetalleService = viajeDetalleService;
    }

    // Detalle del viaje: datos generales, participantes, total gastado y actividades por día.
    // El usuario sale siempre de la sesión.
    @GetMapping("/{id}")
    public ViajeDetalleResponse obtenerDetalle(@PathVariable Integer id, Authentication autenticacion) {
        return viajeDetalleService.obtenerDetalle(id, autenticacion.getName());
    }

    @ExceptionHandler(ErrorNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarError(ErrorNegocioException e) {
        return ResponseEntity.status(e.getEstado()).body(new ErrorResponse(e.getCampo(), e.getMessage()));
    }
}
