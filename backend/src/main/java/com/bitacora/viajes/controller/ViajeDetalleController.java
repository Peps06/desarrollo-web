package com.bitacora.viajes.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bitacora.viajes.dto.GastoResponse;
import com.bitacora.viajes.dto.ViajeDetalleResponse;
import com.bitacora.viajes.service.GastoService;
import com.bitacora.viajes.service.ViajeDetalleService;

@RestController
@RequestMapping("api/viajes")
public class ViajeDetalleController {

    private final ViajeDetalleService viajeDetalleService;
    private final GastoService gastoService;

    public ViajeDetalleController(ViajeDetalleService viajeDetalleService, GastoService gastoService) {
        this.viajeDetalleService = viajeDetalleService;
        this.gastoService = gastoService;
    }

    // Detalle del viaje: datos generales, participantes, total gastado y actividades por día.
    // El usuario sale siempre de la sesión.
    @GetMapping("/{id}")
    public ViajeDetalleResponse obtenerDetalle(@PathVariable Integer id, Authentication autenticacion) {
        return viajeDetalleService.obtenerDetalle(id, autenticacion.getName());
    }

    // Gastos del viaje, del más reciente al más antiguo. Solo para participantes.
    @GetMapping("/{id}/gastos")
    public List<GastoResponse> listarGastos(@PathVariable Integer id, Authentication autenticacion) {
        return gastoService.listarPorViaje(id, autenticacion.getName());
    }
}
