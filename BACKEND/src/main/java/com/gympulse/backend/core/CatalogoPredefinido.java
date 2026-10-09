package com.gympulse.backend.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gympulse.backend.models.Ejercicio;
import com.gympulse.backend.repositories.EjercicioRepository;

@Component
public class CatalogoPredefinido implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogoPredefinido.class);

    private record Entrada(String grupo, String nombre, String descripcion) {
    }

    private static final List<Entrada> CATALOGO = List.of(
            new Entrada("Pecho", "Press de banca", "Barra en banco plano, bajando hasta el pecho"),
            new Entrada("Pecho", "Press inclinado con mancuernas", "Banco a 30-45 grados, parte alta del pecho"),
            new Entrada("Pecho", "Aperturas con mancuernas", "Brazos semiflexionados, abriendo en arco"),
            new Entrada("Pecho", "Cruce de poleas", "Poleas altas cruzando las manos delante del pecho"),
            new Entrada("Pecho", "Flexiones", "Peso corporal, cuerpo recto de cabeza a talones"),
            new Entrada("Espalda", "Dominadas", "Agarre prono, subir hasta pasar la barbilla de la barra"),
            new Entrada("Espalda", "Jalón al pecho", "Polea alta llevando la barra a la parte alta del pecho"),
            new Entrada("Espalda", "Remo con barra", "Torso inclinado, tirando de la barra hacia el ombligo"),
            new Entrada("Espalda", "Remo en polea baja", "Sentado, espalda recta, tirando hacia el abdomen"),
            new Entrada("Espalda", "Peso muerto", "Levantar la barra del suelo con la espalda neutra"),
            new Entrada("Hombro", "Press militar", "De pie, barra desde los hombros hasta por encima de la cabeza"),
            new Entrada("Hombro", "Elevaciones laterales", "Mancuernas hasta la altura de los hombros"),
            new Entrada("Hombro", "Pájaros", "Torso inclinado, abriendo los brazos para el deltoides posterior"),
            new Entrada("Hombro", "Face pull", "Polea a la altura de la cara, separando las manos al tirar"),
            new Entrada("Bíceps", "Curl con barra", "De pie, codos pegados al cuerpo"),
            new Entrada("Bíceps", "Curl martillo", "Mancuernas con agarre neutro"),
            new Entrada("Bíceps", "Curl en banco Scott", "Brazos apoyados en el banco predicador"),
            new Entrada("Tríceps", "Fondos en paralelas", "Bajar hasta 90 grados de codo y empujar"),
            new Entrada("Tríceps", "Extensión en polea alta", "Codos fijos, extendiendo hacia abajo"),
            new Entrada("Tríceps", "Press francés", "Tumbado, bajando la barra hacia la frente"),
            new Entrada("Pierna", "Sentadilla", "Barra en la espalda, bajando al menos hasta paralelo"),
            new Entrada("Pierna", "Prensa de piernas", "Empujar la plataforma sin bloquear las rodillas"),
            new Entrada("Pierna", "Zancadas", "Paso largo bajando la rodilla trasera hacia el suelo"),
            new Entrada("Pierna", "Extensión de cuádriceps", "Máquina, extendiendo las rodillas"),
            new Entrada("Pierna", "Curl femoral", "Máquina, flexionando las rodillas"),
            new Entrada("Pierna", "Peso muerto rumano", "Piernas casi rectas, cadera atrás, para isquios"),
            new Entrada("Pierna", "Elevación de gemelos", "De pie, subiendo sobre las puntas de los pies"),
            new Entrada("Glúteo", "Hip thrust", "Espalda apoyada en un banco, empujando la cadera con la barra"),
            new Entrada("Glúteo", "Patada de glúteo en polea", "Extensión de cadera hacia atrás con tobillera"),
            new Entrada("Core", "Plancha", "Antebrazos apoyados, cuerpo recto. Anota los segundos en repeticiones"),
            new Entrada("Core", "Crunch abdominal", "Tumbado, despegando los hombros del suelo"),
            new Entrada("Core", "Elevación de piernas colgado", "Colgado de la barra, subiendo las piernas"),
            new Entrada("Core", "Rueda abdominal", "De rodillas, rodando hacia delante y volviendo"),
            new Entrada("Cardio", "Cinta de correr", "Anota los minutos en repeticiones"),
            new Entrada("Cardio", "Bicicleta estática", "Anota los minutos en repeticiones"),
            new Entrada("Cardio", "Remo ergómetro", "Anota los minutos en repeticiones"));

    private final EjercicioRepository ejercicioRepository;

    public CatalogoPredefinido(EjercicioRepository ejercicioRepository) {
        this.ejercicioRepository = ejercicioRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        int creados = 0;
        for (Entrada entrada : CATALOGO) {
            if (ejercicioRepository.existsByNombreIgnoreCaseAndUsuarioIsNull(entrada.nombre())) {
                continue;
            }
            Ejercicio ejercicio = new Ejercicio();
            ejercicio.setNombre(entrada.nombre());
            ejercicio.setDescripcion(entrada.descripcion());
            ejercicio.setGrupoMuscular(entrada.grupo());
            ejercicioRepository.save(ejercicio);
            creados++;
        }
        if (creados > 0) {
            log.info("Catalogo predefinido: {} ejercicios nuevos", creados);
        }
    }
}
