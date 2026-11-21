package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.BalanceResponse;
import com.bitacora.viajes.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-21 | Responsable: Alfonso
 * Endpoint REST para consultar el balance de deudas del grupo.
 * El controlador solo recibe la petición; el cálculo lo hace BalanceService.
 */
@RestController
@RequestMapping("api")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    // Devuelve la cuota por persona, el saldo de cada participante y las transferencias sugeridas.
    // El usuario se toma de la sesión (autenticacion.getName() es su correo).
    @GetMapping("/viajes/{viajeId}/balance")
    public ResponseEntity<BalanceResponse> consultarBalance(@PathVariable Integer viajeId,
                                                            Authentication autenticacion) {
        BalanceResponse respuesta = balanceService.obtenerBalance(viajeId, autenticacion.getName());
        return ResponseEntity.ok(respuesta);
    }
}
