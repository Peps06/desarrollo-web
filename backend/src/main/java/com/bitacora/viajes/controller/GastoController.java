// HU-15, HU-20, HU-25 | Responsable: Citlaly | Endpoints REST para registrar y consultar gastos.
package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.GastoRegistradoResponse;
import com.bitacora.viajes.dto.GastoRequest;
import com.bitacora.viajes.service.GastoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para editar y eliminar gastos.
 *
 * @author Citlaly Morales Viveros
 */
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    // Regresa 200 con el gasto actualizado y el total del viaje
    @PutMapping("/{id}")
    public ResponseEntity<GastoRegistradoResponse> actualizar(@PathVariable Integer id, @RequestBody GastoRequest request, Authentication auth) {
        return ResponseEntity.ok(gastoService.actualizar(id, request, auth.getName()));
    }

    // Regresa 204 cuando el gasto se borra
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication auth) {
        gastoService.eliminar(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}