package com.bitacora.viajes.model;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "tarea")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "completada", nullable = false, columnDefinition = "TINYINT(1)")
    @ColumnDefault("0")
    private boolean completada = false;

    public Tarea() {}

    public Integer getId() { return id; }
    public Viaje getViaje() { return viaje; }
    public void setViaje(Viaje viaje) { this.viaje = viaje; }
    public Usuario getCreador() { return creador; }
    public void setCreador(Usuario creador) { this.creador = creador; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}
