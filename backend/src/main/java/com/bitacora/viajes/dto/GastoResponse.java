// HU-15 | Responsable: Citlaly | DTO de salida con los datos de un gasto.
package com.bitacora.viajes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bitacora.viajes.model.CategoriaGasto;
import com.bitacora.viajes.model.Gasto;

/**
 * Representación de un gasto que se regresa al cliente.
 *
 * @author Citlaly Morales Viveros
 */
public class GastoResponse {

    private Integer id;
    private String concepto;
    private CategoriaGasto categoria;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private Integer usuarioId;

    public GastoResponse(Gasto gasto) {
        this.id = gasto.getId();
        this.concepto = gasto.getConcepto();
        this.categoria = gasto.getCategoria();
        this.monto = gasto.getMonto();
        this.fecha = gasto.getFecha();
        this.usuarioId = gasto.getUsuario().getId();
    }

    public Integer getId() {
        return id;
    }

    public String getConcepto() {
        return concepto;
    }

    public CategoriaGasto getCategoria() {
        return categoria;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }
}
