// HU-15 | Responsable: Citlaly | DTO de entrada para registrar un gasto.
package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.CategoriaGasto;
import java.math.BigDecimal;

/**
 * Datos que el usuario envía para registrar o editar un gasto.
 *
 * @author Citlaly Morales Viveros
 */
public class GastoRequest {

    private String concepto;
    private CategoriaGasto categoria;
    private BigDecimal monto;

    public GastoRequest() {}

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public CategoriaGasto getCategoria() { return categoria; }
    public void setCategoria(CategoriaGasto categoria) { this.categoria = categoria; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}