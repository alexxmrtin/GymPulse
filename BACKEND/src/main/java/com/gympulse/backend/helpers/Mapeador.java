package com.gympulse.backend.helpers;

import java.util.List;

import com.gympulse.backend.dtos.EjercicioDTO;
import com.gympulse.backend.dtos.EntrenamientoDTO;
import com.gympulse.backend.dtos.SerieDTO;
import com.gympulse.backend.dtos.UsuarioDTO;
import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.models.Entrenamiento;
import com.gympulse.backend.models.Serie;
import com.gympulse.backend.models.Usuario;

/*
 Que hace
 Convierte las entidades (los objetos que representan las tablas: Usuario, Ejercicio, Entrenamiento, Serie) en los DTO que salen por la API. Nada mas. Es el unico sitio del proyecto donde se decide que campos de la BD acaban en el JSON y con que forma.

 Por que existe
 Sin el, cada servicio tendria que construir el DTO a mano cada vez que devuelve algo, y el mismo mapeo estaria repetido en cuatro sitios. En cuanto uno se olvidara de actualizarlo, dos endpoints devolverian el mismo objeto con distinta forma.

 Solo traduce en un sentido: entidad → DTO
 El camino de vuelta (DTO → entidad) no esta aqui, esta en los servicios. El motivo es que un DTO de entrada trae ids sueltos (usuarioId, ejercicioId) y para construir la entidad hay que ir a la BD a buscar esos objetos y comprobar que existen. Eso necesita los repositorios, que es justo lo que esta clase no tiene ni debe tener.
*/

public final class Mapeador {

    private Mapeador() {
    }

    public static UsuarioDTO aDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getPeso(),
                usuario.getAltura(),
                usuario.getFechaNacimiento(),
                usuario.getFotoActualizadaEn());
    }

    public static EjercicioDTO aDTO(Ejercicio ejercicio) {
        return new EjercicioDTO(
                ejercicio.getId(),
                ejercicio.getNombre(),
                ejercicio.getDescripcion(),
                ejercicio.getGrupoMuscular(),
                ejercicio.esPredefinido());
    }

    public static SerieDTO aDTO(Serie serie) {
        return new SerieDTO(
                serie.getId(),
                serie.getEntrenamiento().getId(),
                serie.getEjercicio().getId(),
                serie.getEjercicio().getNombre(),
                serie.getRepeticiones(),
                serie.getPeso());
    }

    public static EntrenamientoDTO aDTO(Entrenamiento entrenamiento) {
        List<SerieDTO> series = entrenamiento.getSeries() == null
                ? List.of()
                : entrenamiento.getSeries().stream().map(Mapeador::aDTO).toList();

        return new EntrenamientoDTO(
                entrenamiento.getId(),
                entrenamiento.getUsuario().getId(),
                entrenamiento.getNombre(),
                entrenamiento.getFecha(),
                series);
    }
}
