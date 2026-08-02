package com.gympulse.backend.controllers;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gympulse.backend.services.EntrenamientoService;

/*
POST /entrenamiento               ->   crear un entrenamiento
GET /entrenamiento                ->   obtener un entrenamiento
GET /entrenamiento?usuarioId=1    -> obtener todos los entrenamientos a partir de un usuario
DELETE /entrenamiento/{id}        -> eliminar un entrenamiento
*/

@RestController
@RequestMapping("/entrenamiento")
public class EntrenamientoController {
    
    private final EntrenamientoService entrenamientoService;
    public EntrenamientoController(EntrenamientoService entrenamientoService){
        this.entrenamientoService = entrenamientoService;
    }



}
