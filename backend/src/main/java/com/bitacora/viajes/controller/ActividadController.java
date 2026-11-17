package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ActividadRequest;
import com.bitacora.viajes.dto.ActividadResponse;
import com.bitacora.viajes.service.ActividadService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(ActividadService actividadService) {
        this.actividadService = actividadService;
    }

    // Propone una actividad en el viaje. El autor se toma de la sesión, nunca del cliente.
    @PostMapping("/viajes/{viajeId}/actividades")
    public ResponseEntity<ActividadResponse> crearActividad(@PathVariable Integer viajeId,
                                                            @Valid @RequestBody ActividadRequest solicitud,
                                                            Authentication autenticacion) {
        ActividadResponse creada = actividadService.crear(viajeId, solicitud, autenticacion.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    // Lista las actividades del viaje. Filtros opcionales y combinables: dia (yyyy-MM-dd) y costoMax.
    @GetMapping("/viajes/{viajeId}/actividades")
    public ResponseEntity<List<ActividadResponse>> listarActividades(@PathVariable Integer viajeId,
                                                                     @RequestParam(required = false) String dia,
                                                                     @RequestParam(required = false) String costoMax,
                                                                     Authentication autenticacion) {
        return ResponseEntity.ok(actividadService.listar(viajeId, dia, costoMax, autenticacion.getName()));
    }

    // Edita una actividad. Solo su autor o el organizador del viaje. Los datos se validan en el servicio.
    @PutMapping("/actividades/{actividadId}")
    public ResponseEntity<ActividadResponse> editarActividad(@PathVariable Integer actividadId,
                                                             @RequestBody ActividadRequest solicitud,
                                                             Authentication autenticacion) {
        return ResponseEntity.ok(actividadService.editar(actividadId, solicitud, autenticacion.getName()));
    }

    // Elimina una actividad junto con sus votos. Solo su autor o el organizador del viaje.
    @DeleteMapping("/actividades/{actividadId}")
    public ResponseEntity<Void> eliminarActividad(@PathVariable Integer actividadId,
                                                  Authentication autenticacion) {
        actividadService.eliminar(actividadId, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}
