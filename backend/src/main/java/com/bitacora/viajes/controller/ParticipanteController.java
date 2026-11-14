package com.bitacora.viajes.controller;

import com.bitacora.viajes.dto.ParticipanteRequest;
import com.bitacora.viajes.dto.ParticipanteResponse;
import com.bitacora.viajes.service.ParticipanteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/viajes/{id}/participantes")
public class ParticipanteController {

    private final ParticipanteService participanteService;

    public ParticipanteController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @GetMapping
    public ResponseEntity<List<ParticipanteResponse>> listarParticipantes(
            @PathVariable("id") Integer viajeId,
            Authentication authentication) {
        List<ParticipanteResponse> participantes =
                participanteService.listarParticipantes(viajeId, authentication.getName());
        return ResponseEntity.ok(participantes);
    }

    @PostMapping
    public ResponseEntity<ParticipanteResponse> agregarParticipante(
            @PathVariable("id") Integer viajeId,
            @RequestBody ParticipanteRequest request,
            Authentication authentication) {
        ParticipanteResponse creado =
                participanteService.agregarParticipante(viajeId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<Void> quitarParticipante(
            @PathVariable("id") Integer viajeId,
            @PathVariable("usuarioId") Integer usuarioId,
            Authentication authentication) {
        participanteService.quitarParticipante(viajeId, usuarioId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}