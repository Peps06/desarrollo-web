package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.dto.ViajeRequest;
import com.bitacora.viajes.dto.ViajeResponse;
import com.bitacora.viajes.service.ViajeService;
import com.bitacora.viajes.service.ViajeService.ValidacionViajeException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/viajes")
public class ViajeController {

    @Autowired
    private ViajeService viajeService;

    // Crear un nuevo viaje y asignar al usuario actual como ORGANIZADOR
    @PostMapping
    public ResponseEntity<?> crearViaje(@Valid @RequestBody ViajeRequest solicitud,
            BindingResult resultado,
            Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("sesion", "Debes iniciar sesión para crear un viaje"));
        }

        if (resultado.hasErrors()) {
            FieldError error = resultado.getFieldError();
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(error.getField(), error.getDefaultMessage()));
        }

        try {
            String correoUsuario = autenticacion.getName();
            ViajeResponse viajeCreado = viajeService.crearViaje(solicitud, correoUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(viajeCreado);
        } catch (ValidacionViajeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getCampo(), e.getMessage()));
        }
    }

    // Listar los viajes activos donde participa el usuario en sesión
    @GetMapping
    public ResponseEntity<?> listarMisViajes(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("sesion", "Debes iniciar sesión para ver tus viajes"));
        }

        String correoUsuario = autenticacion.getName();
        List<ViajeResponse> viajes = viajeService.listarMisViajes(correoUsuario);
        return ResponseEntity.ok(viajes);
    }

    // HU-23: Editar datos de un viaje (solo ORGANIZADOR)
    @PutMapping("/{id}")
    public ResponseEntity<?> editarViaje(@PathVariable("id") Integer viajeId,
            @Valid @RequestBody ViajeRequest solicitud,
            BindingResult resultado,
            Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("sesion", "Debes iniciar sesión para editar un viaje"));
        }

        if (resultado.hasErrors()) {
            FieldError error = resultado.getFieldError();
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(error.getField(), error.getDefaultMessage()));
        }

        ViajeResponse actualizado = viajeService.editarViaje(viajeId, solicitud, autenticacion.getName());
        return ResponseEntity.ok(actualizado);
    }

    // HU-23: Cancelar un viaje sin borrado físico (solo ORGANIZADOR)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarViaje(@PathVariable("id") Integer viajeId,
            Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("sesion", "Debes iniciar sesión para cancelar un viaje"));
        }

        viajeService.cancelarViaje(viajeId, autenticacion.getName());
        return ResponseEntity.noContent().build();
    }
}