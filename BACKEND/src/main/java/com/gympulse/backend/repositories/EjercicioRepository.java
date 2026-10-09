package com.gympulse.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gympulse.backend.models.Ejercicio;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    @Query("""
            select e from Ejercicio e
            where (e.usuario is null or e.usuario.id = :usuarioId)
            order by lower(e.nombre)""")
    List<Ejercicio> visiblesPara(@Param("usuarioId") Long usuarioId);

    @Query("""
            select e from Ejercicio e
            where (e.usuario is null or e.usuario.id = :usuarioId)
              and e.grupoMuscular = :grupoMuscular
            order by lower(e.nombre)""")
    List<Ejercicio> visiblesParaPorGrupo(@Param("usuarioId") Long usuarioId,
            @Param("grupoMuscular") String grupoMuscular);

    @Query("""
            select e from Ejercicio e
            where (e.usuario is null or e.usuario.id = :usuarioId)
              and lower(e.nombre) like lower(concat('%', :nombre, '%'))
            order by lower(e.nombre)""")
    List<Ejercicio> visiblesParaPorNombre(@Param("usuarioId") Long usuarioId, @Param("nombre") String nombre);

    @Query("""
            select count(e) > 0 from Ejercicio e
            where (e.usuario is null or e.usuario.id = :usuarioId)
              and lower(e.nombre) = lower(:nombre)""")
    boolean existeVisible(@Param("usuarioId") Long usuarioId, @Param("nombre") String nombre);

    boolean existsByNombreIgnoreCaseAndUsuarioIsNull(String nombre);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Ejercicio e where e.usuario.id = :usuarioId")
    int borrarTodosDe(@Param("usuarioId") Long usuarioId);
}
