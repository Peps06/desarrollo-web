package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ErrorResponse;
import com.bitacora.viajes.dto.LoginRequest;
import com.bitacora.viajes.dto.RegistroRequest;
import com.bitacora.viajes.dto.UsuarioResponse;
import com.bitacora.viajes.model.Usuario;
import com.bitacora.viajes.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String MENSAJE_CORREO_REPETIDO = "El correo ya está registrado";
    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos";

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository repositorioContexto = new HttpSessionSecurityContextRepository();

    public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
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
            return ResponseEntity.badRequest().body(new ErrorResponse("correo", MENSAJE_CORREO_REPETIDO));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest solicitud, BindingResult resultado,
                                   HttpServletRequest peticion, HttpServletResponse respuesta) {
        if (resultado.hasErrors()) {
            FieldError error = resultado.getFieldError();
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(error.getField(), error.getDefaultMessage()));
        }
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            UsuarioService.normalizarCorreo(solicitud.getCorreo()),
                            solicitud.getContrasena()));

            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);

            // Se descarta la sesión anterior y se crea una nueva. Así una cookie vieja no sirve después del login.
            HttpSession anterior = peticion.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }
            repositorioContexto.saveContext(contexto, peticion, respuesta);

            Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());
            return ResponseEntity.ok(UsuarioResponse.desde(usuario));
        } catch (AuthenticationException e) {
            // El mensaje no dice si falló el correo o la contraseña.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("credenciales", MENSAJE_CREDENCIALES));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest peticion) {
        SecurityContextHolder.clearContext();
        HttpSession sesion = peticion.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sesion")
    public ResponseEntity<?> sesionActual(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("sesion", "No hay una sesión iniciada"));
        }
        Usuario usuario = usuarioService.buscarPorCorreo(autenticacion.getName());
        return ResponseEntity.ok(UsuarioResponse.desde(usuario));
    }
}
