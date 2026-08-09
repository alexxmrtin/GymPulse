package com.gympulse.android

import android.app.Application
import com.gympulse.android.data.api.ClienteApi
import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.repositorios.EjercicioRepositorio
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.data.repositorios.SerieRepositorio
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.data.sesion.AlmacenSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/*
 Una clase Application se crea una sola vez, antes que cualquier Activity, y vive mientras viva el
 proceso. Es el sitio donde montar lo que debe existir una unica vez en toda la app.

 El Contenedor es inyeccion de dependencias hecha a mano: en vez de que cada pantalla se fabrique
 su propio cliente HTTP, aqui se crea uno y se reparte. Es lo mismo que hace Spring en el backend
 con los @Service, solo que alli el framework lo hace solo y aqui lo escribimos nosotros.
*/
class Contenedor(aplicacion: Application) {

    val almacenSesion: AlmacenSesion = AlmacenSesion(aplicacion)

    private val api: GymPulseApi = ClienteApi.crear(almacenSesion)

    val usuarios = UsuarioRepositorio(api, almacenSesion)
    val ejercicios = EjercicioRepositorio(api)
    val entrenamientos = EntrenamientoRepositorio(api)
    val series = SerieRepositorio(api)
}

class AppGymPulse : Application() {

    lateinit var contenedor: Contenedor
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = Contenedor(this)

        //Recupera el token guardado en disco para que al abrir la app no haya que volver a entrar
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            contenedor.almacenSesion.cargar()
        }
    }
}
