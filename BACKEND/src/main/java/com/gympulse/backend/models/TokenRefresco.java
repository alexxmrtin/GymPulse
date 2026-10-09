package com.gympulse.backend.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tokens_refresco")
public class TokenRefresco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 64)
    private String hash;

    @Column(nullable = false)
    private Instant creadoEn;

    @Column(nullable = false)
    private Instant expiraEn;

    private Instant revocadoEn;

    //-------------------------------------------

    public Long getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }

    public Instant getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Instant expiraEn) { this.expiraEn = expiraEn; }

    public Instant getRevocadoEn() { return revocadoEn; }
    public void setRevocadoEn(Instant revocadoEn) { this.revocadoEn = revocadoEn; }
}
