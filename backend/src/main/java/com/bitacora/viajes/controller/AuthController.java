package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.dto.RegistroRequest;
import com.bitacora.viajes.dto.UsuarioResponse;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String MENSAJE_CORREO_REPETIDO = "El correo ya está registrado";

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest solicitud, BindingResult resultado) {
        if (resultado.hasErrors()) {
            FieldError error = resultado.getFieldError();
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(error.getField(), error.getDefaultMessage()));
        }
        try {
            Usuario usuario = usuarioService.registrar(solicitud);
            return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.desde(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("correo", MENSAJE_CORREO_REPETIDO));
        } catch (DataIntegrityViolationException e) {
            // Dos registros con el mismo correo al mismo tiempo: la restricción UNIQUE de la base lo rechaza.
            return ResponseEntity.badRequest().body(new ErrorResponse("correo", MENSAJE_CORREO_REPETIDO));
        }
    }
}
