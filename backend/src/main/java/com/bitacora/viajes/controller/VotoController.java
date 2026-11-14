package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.dto.VotoResponse;
import com.bitacora.viajes.service.VotoService;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-14: endpoints para votar una actividad y quitar el voto propio.
 * Ambos devuelven {actividadId, totalVotos, votadaPorMi}.
 */
@RestController
@RequestMapping("api")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    // Vota por la actividad. El usuario sale de la sesión. 201 porque se crea un voto.
    @PostMapping("/actividades/{actividadId}/votos")
    public ResponseEntity<VotoResponse> votar(@PathVariable Integer actividadId,
                                              Authentication autenticacion) {
        VotoResponse respuesta = votoService.votar(actividadId, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Quita el voto propio de la actividad. 200 con el contador actualizado.
    @DeleteMapping("/actividades/{actividadId}/votos")
    public ResponseEntity<VotoResponse> quitarVoto(@PathVariable Integer actividadId,
                                                   Authentication autenticacion) {
        VotoResponse respuesta = votoService.quitarVoto(actividadId, autenticacion.getName());
        return ResponseEntity.ok(respuesta);
    }

    // Convierte los errores de negocio (404, 403, 400) al formato {campo, mensaje},
    // igual que los demás controladores, mientras Dana termina el manejador global (HU-17).
    @ExceptionHandler(ErrorNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarError(ErrorNegocioException e) {
        return ResponseEntity.status(e.getEstado()).body(new ErrorResponse(e.getCampo(), e.getMessage()));
    }
}
