package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ActividadRequest;
import com.bitacora.viajes.dto.ActividadResponse;
import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.service.ActividadService;
import com.bitacora.viajes.service.ViajeDetalleService.ErrorNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @ExceptionHandler(ErrorNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarError(ErrorNegocioException e) {
        return ResponseEntity.status(e.getEstado()).body(new ErrorResponse(e.getCampo(), e.getMessage()));
    }

    // Validaciones de campos (@NotBlank, @NotNull, @DecimalMin...): responde con el primer error.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        return ResponseEntity.badRequest().body(new ErrorResponse(error.getField(), error.getDefaultMessage()));
    }
}
