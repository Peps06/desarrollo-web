package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.TareaRequest;
import com.bitacora.viajes.dto.TareaResponse;
import com.bitacora.viajes.service.TareaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TareaController {

    @Autowired
    private TareaService tareaService;

    @GetMapping("/viajes/{viajeId}/tareas")
    public ResponseEntity<List<TareaResponse>> listarTareas(
            @PathVariable Integer viajeId,
            Authentication auth) {
        return ResponseEntity.ok(tareaService.listarPorViaje(viajeId, auth.getName()));
    }

    @PostMapping("/viajes/{viajeId}/tareas")
    public ResponseEntity<TareaResponse> crearTarea(
            @PathVariable Integer viajeId,
            @Valid @RequestBody TareaRequest request,
            Authentication auth) {
        TareaResponse creada = tareaService.crearTarea(viajeId, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PatchMapping("/tareas/{tareaId}/toggle")
    public ResponseEntity<TareaResponse> toggleTarea(
            @PathVariable Integer tareaId,
            Authentication auth) {
        return ResponseEntity.ok(tareaService.toggleCompletada(tareaId, auth.getName()));
    }

    @DeleteMapping("/tareas/{tareaId}")
    public ResponseEntity<Void> eliminarTarea(
            @PathVariable Integer tareaId,
            Authentication auth) {
        tareaService.eliminarTarea(tareaId, auth.getName());
        return ResponseEntity.noContent().build();
    }
}