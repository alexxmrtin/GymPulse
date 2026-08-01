package com.gympulse.backend.models;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/*
 @Entity le dice a Hibernate que esta clase es una tabla de la BD.
 @Table(name = "...") elige el nombre de la tabla; sin ella Hibernate usaria el nombre de la clase.
*/
@Entity
@Table(name = "usuarios")
public class Usuario {

    /*
     @Id marca la clave primaria.
     @GeneratedValue con IDENTITY delega en la BD la generacion del id (autoincremento).
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     @Column permite configurar la columna: nullable = false equivale a NOT NULL en SQL,
     unique = true añade una restriccion de unicidad.
    */
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private Double peso;
    private Double altura;
    private LocalDate fechaNacimiento;

    /*
     @OneToMany: un usuario tiene muchos entrenamientos.
     mappedBy = "usuario" le dice a Hibernate que la columna de union esta en Entrenamiento,
     no aqui. cascade = ALL propaga las operaciones: si borras un usuario se borran sus entrenos.
    */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Entrenamiento> entrenamientos;

    //-------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public List<Entrenamiento> getEntrenamientos() { return entrenamientos; }
    public void setEntrenamientos(List<Entrenamiento> entrenamientos) { this.entrenamientos = entrenamientos; }
}
