package com.bitacora.viajes.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "participante_viaje",
       uniqueConstraints = @UniqueConstraint(name = "uq_viaje_usuario", columnNames = {"viaje_id", "usuario_id"}))
public class ParticipanteViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 15)
    private RolParticipante rol;

    public ParticipanteViaje() {}

    public ParticipanteViaje(Viaje viaje, Usuario usuario, RolParticipante rol) {
        this.viaje = viaje;
        this.usuario = usuario;
        this.rol = rol;
    }

    public Integer getId() { return id; }
    public Viaje getViaje() { return viaje; }
    public void setViaje(Viaje viaje) { this.viaje = viaje; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public RolParticipante getRol() { return rol; }
    public void setRol(RolParticipante rol) { this.rol = rol; }
}
