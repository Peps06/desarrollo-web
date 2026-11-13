package com.bitacora.viajes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Recibe JSON que manda el cliente cuando quiere crear un viaje (POST /api/viajes).
public class ViajeRequest {

    @NotBlank(message = "El destino es obligatorio")
    @Size(max = 150, message = "El destino no puede pasar de 150 caracteres")
    private String destino;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El presupuesto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El presupuesto debe ser mayor a cero")
    private BigDecimal presupuestoTotal;

    // Si el cliente no manda moneda, en el servicio le asignamos MXN por defecto
    private String monedaBase;

    public ViajeRequest() {
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public void setPresupuestoTotal(BigDecimal presupuestoTotal) {
        this.presupuestoTotal = presupuestoTotal;
    }

    public String getMonedaBase() {
        return monedaBase;
    }

    public void setMonedaBase(String monedaBase) {
        this.monedaBase = monedaBase;
    }
}
