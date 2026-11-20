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
    private Integer viajeId;
    private String concepto;
    private CategoriaGasto categoria;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private Integer usuarioId;
    private String usuarioNombre;
    private String monedaOriginal;
    private BigDecimal montoOriginal;
    private BigDecimal tipoCambio;
    private String comprobanteUrl;

    public GastoResponse(Gasto gasto) {
        this.id = gasto.getId();
        this.viajeId = gasto.getViaje().getId();
        this.concepto = gasto.getConcepto();
        this.categoria = gasto.getCategoria();
        this.monto = gasto.getMonto();
        this.fecha = gasto.getFecha();
        this.usuarioId = gasto.getUsuario().getId();
        this.usuarioNombre = gasto.getUsuario().getNombre();
        this.monedaOriginal = gasto.getMonedaOriginal();
        this.montoOriginal = gasto.getMontoOriginal();
        this.tipoCambio = gasto.getTipoCambio();
        // Ruta para descargar el comprobante; null si el gasto no tiene uno
        this.comprobanteUrl = gasto.getImagenUrl() != null
                ? "/api/gastos/" + gasto.getId() + "/comprobante"
                : null;
    }

    public Integer getId() {
        return id;
    }

    public Integer getViajeId() {
        return viajeId;
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

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public String getMonedaOriginal() {
        return monedaOriginal;
    }

    public BigDecimal getMontoOriginal() {
        return montoOriginal;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }
}
