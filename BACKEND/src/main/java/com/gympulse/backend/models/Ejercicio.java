package com.gympulse.backend.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ejercicios")
public class Ejercicio {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;
    private String grupoMuscular;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    /*
     Un ejercicio puede aparecer en muchas series (de muchos entrenamientos distintos).
     No tiene cascade: borrar un ejercicio no debe borrar el historial de quien lo ha hecho.
    */
    @OneToMany(mappedBy = "ejercicio")
    private List<Serie> series;


   //-------------------------------------------------------------------------
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public boolean esPredefinido() { return usuario == null; }

    public List<Serie> getSeries() { return series; }
    public void setSeries(List<Serie> series) { this.series = series; }
}
