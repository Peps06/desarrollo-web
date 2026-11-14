package com.bitacora.viajes.config;

import com.bitacora.viajes.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    /**
     * Codificador BCrypt: guarda las contraseñas como hash y las compara al
     * iniciar sesión.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager para usarlo en el endpoint de login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Respuesta de error en formato JSON para la API sin sesión (401).
        AuthenticationEntryPoint apiSinSesion = (peticion, respuesta, excepcion) ->
                escribirError(respuesta, 401, "Tu sesión expiró");

        // Respuesta de error en formato JSON para la API sin permiso (403).
        AccessDeniedHandler sinPermiso = (peticion, respuesta, excepcion) ->
                escribirError(respuesta, 403, "No tienes permiso para esta acción");

        http
                // CSRF se desactiva solo para /api, porque el cliente envía JSON con fetch.
                // Los formularios de las páginas siguen protegidos contra CSRF.
                // CORS no se configura: la página y la API se sirven desde el mismo origen.
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/api/**")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/registro", "/api/auth/**",
                                "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // Peticiones a /api sin sesión: 401
                        .defaultAuthenticationEntryPointFor(
                                apiSinSesion,
                                new AntPathRequestMatcher("/api/**"))
                        // Páginas sin sesión: redirección a /login.
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new AntPathRequestMatcher("/**"))
                        // Peticiones a /api sin permiso: 403 ¿
                        .accessDeniedHandler(sinPermiso));
        return http.build();
    }

    // Escribe un error en formato JSON con el mismo formato que el resto de la API.
    private static void escribirError(HttpServletResponse respuesta, int codigo, String mensaje)
            throws IOException {
        respuesta.setStatus(codigo);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write(new ObjectMapper()
                .writeValueAsString(new ErrorResponse(codigo, mensaje)));
    }
}
