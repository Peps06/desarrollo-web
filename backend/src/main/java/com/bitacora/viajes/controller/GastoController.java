// HU-15, HU-20, HU-25 | Responsable: Citlaly | Endpoints REST para registrar y consultar gastos.
package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.GastoRegistradoResponse;
import com.bitacora.viajes.dto.GastoRequest;
import com.bitacora.viajes.service.AlmacenamientoService;
import com.bitacora.viajes.service.GastoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoints para registrar, editar y eliminar gastos.
 *
 * Autora: Citlaly Morales Viveros
 */
@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    @Autowired
    private GastoService gastoService;

    @Autowired
    private AlmacenamientoService almacenamientoService;

    // Regresa 201 con el gasto creado y el total del viaje
    @PostMapping
    public ResponseEntity<GastoRegistradoResponse> registrar(@RequestBody GastoRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gastoService.registrar(request, auth.getName()));
    }

    // Regresa 201 igual que el de JSON, pero aquí llega el comprobante opcional
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GastoRegistradoResponse> registrarConComprobante(
            @ModelAttribute GastoRequest request,
            @RequestParam(value = "comprobante", required = false) MultipartFile comprobante,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gastoService.registrar(request, auth.getName(), comprobante));
    }

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

    // Regresa la imagen del comprobante solo a los participantes del viaje
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<Resource> verComprobante(@PathVariable Integer id, Authentication auth) {
        Resource recurso = gastoService.obtenerComprobante(id, auth.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(almacenamientoService.tipoContenido(recurso.getFilename())))
                .body(recurso);
    }
}
