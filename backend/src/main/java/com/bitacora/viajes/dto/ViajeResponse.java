package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.ParticipanteViaje;
import com.bitacora.viajes.model.RolParticipante;
import com.bitacora.viajes.model.Viaje;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ViajeResponse {
    private Integer id;
    private String destino;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal presupuestoTotal;
    private String monedaBase;
    private RolParticipante rol;

    public ViajeResponse(){

    }

    public ViajeResponse(Integer id, String destino, LocalDate fechaInicio, LocalDate fechaFin,
                        BigDecimal presupuestoTotal, String monedaBase, RolParticipante rol){
        this.id = id;
        this.destino = destino;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.presupuestoTotal = presupuestoTotal;
        this.monedaBase = monedaBase;
        this.rol = rol;
 
    }

    //Método para armar la respuesta a partir de un Viaje y el rol del usuario
    public static ViajeResponse desde(Viaje viaje, RolParticipante rol){
        return new ViajeResponse(
            viaje.getId(),
            viaje.getDestino(),
            viaje.getFechaInicio(),
            viaje.getFechaFin(),
            viaje.getPresupuestoTotal(),
            viaje.getMonedaBase(),
            rol
        );
    }

    public static ViajeResponse desdeParticipacion(ParticipanteViaje participacion){
        return desde(participacion.getViaje(), participacion.getRol());
    }

    public Integer getId() {
        return id;
    }

    public String getDestino() {
        return destino;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public BigDecimal getPresupuestoTotal() {
        return presupuestoTotal;
    }

    public String getMonedaBase() {
        return monedaBase;
    }

    public RolParticipante getRol(){
        return rol;
    }
}