// HU-15 | Responsable: Citlaly | DTO de entrada para registrar un gasto.
package com.bitacora.viajes.dto;

import com.bitacora.viajes.model.CategoriaGasto;
import java.math.BigDecimal;

/**
 * Datos que el usuario envía para registrar o editar un gasto.
 * Los campos de moneda son opcionales: si no se mandan, se usa MXN y tipo de cambio 1.
 *
 * @author Citlaly Morales Viveros
 */
public class GastoRequest {

    private Integer viajeId;
    private String concepto;
    private CategoriaGasto categoria;
    private BigDecimal monto;
    private String monedaOriginal;
    private BigDecimal montoOriginal;
    private BigDecimal tipoCambio;

    public GastoRequest() {}

    public Integer getViajeId() { return viajeId; }
    public void setViajeId(Integer viajeId) { this.viajeId = viajeId; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public CategoriaGasto getCategoria() { return categoria; }
    public void setCategoria(CategoriaGasto categoria) { this.categoria = categoria; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMonedaOriginal() { return monedaOriginal; }
    public void setMonedaOriginal(String monedaOriginal) { this.monedaOriginal = monedaOriginal; }
    public BigDecimal getMontoOriginal() { return montoOriginal; }
    public void setMontoOriginal(BigDecimal montoOriginal) { this.montoOriginal = montoOriginal; }
    public BigDecimal getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }
}