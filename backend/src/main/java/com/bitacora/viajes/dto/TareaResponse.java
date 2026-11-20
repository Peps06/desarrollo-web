package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.Tarea;

public class TareaResponse {

    private Integer id;
    private Integer viajeId;
    private String descripcion;
    private boolean completada;
    private String creadorNombre;

    public TareaResponse() {}

    public static TareaResponse desde(Tarea tarea) {
        TareaResponse dto = new TareaResponse();
        dto.id = tarea.getId();
        dto.viajeId = tarea.getViaje().getId();
        dto.descripcion = tarea.getDescripcion();
        dto.completada = tarea.isCompletada();
        dto.creadorNombre = tarea.getCreador() != null ? tarea.getCreador().getNombre() : "";
        return dto;
    }

    public Integer getId() { return id; }
    public Integer getViajeId() { return viajeId; }
    public String getDescripcion() { return descripcion; }
    public boolean isCompletada() { return completada; }
    public String getCreadorNombre() { return creadorNombre; }
}