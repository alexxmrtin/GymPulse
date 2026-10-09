package com.gympulse.backend.models;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "fotos_perfil")
public class FotoPerfil {

    public static final int TAMANO_MAXIMO = 5 * 1024 * 1024;

    @Id
    private Long usuarioId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] datos;

    @Column(nullable = false)
    private String tipo;

    //-------------------------------------------

    public Long getUsuarioId() { return usuarioId; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public byte[] getDatos() { return datos; }
    public void setDatos(byte[] datos) { this.datos = datos; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
