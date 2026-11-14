package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.VotoResponse;
import com.bitacora.viajes.service.VotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para votar una actividad y quitar el voto propio.
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
}
