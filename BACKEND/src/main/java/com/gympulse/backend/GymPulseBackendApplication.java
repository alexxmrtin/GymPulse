package com.gympulse.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
PUNTO DE ARRANQUE DE LA APLICACION
 
Esta clase enciende Springboot. No hace nada por si misma: su unico trabajo es
poner el framework en marcha. Por eso practicamente nunca hay que tocarla.

Las dos cosas que importamos arriba son las piezas que encienden Spring Boot:
SpringApplication es una clase con el metodo run(), el que arranca el framework
y levanta el servidor web; @SpringBootApplication es una anotacion que se coloca
sobre esta clase y le dice a Spring que empiece por aqui, que configure sola la
aplicacion segun las librerias del pom.xml, y que busque en este paquete y en los
de debajo las clases de las que debe hacerse cargo (como los controladores).

En este programa lo unico que hacemos es configurar la aplicacion usando pom.xml y llamamos a el metodo run() de la clase SpringApplication el cual levanta el servidor Tomcat en el puerto 8080 y lo deja escuchando peticiones. A partir de ahi todo lo gestiona Spring: crear los objetos y decidir que clase responde a cada URL.
**/


@SpringBootApplication
public class GymPulseBackendApplication {


	public static void main(String[] args) {
		SpringApplication.run(GymPulseBackendApplication.class, args);
	}

}

