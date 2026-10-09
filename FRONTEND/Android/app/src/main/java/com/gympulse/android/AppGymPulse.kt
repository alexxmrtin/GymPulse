package com.gympulse.android

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.gympulse.android.data.api.ApiSesion
import com.gympulse.android.data.api.ClienteApi
import com.gympulse.android.data.api.GymPulseApi
import com.gympulse.android.data.perfil.FotoPerfilRepositorio
import com.gympulse.android.data.preferencias.AlmacenPreferencias
import com.gympulse.android.data.repositorios.EjercicioRepositorio
import com.gympulse.android.data.repositorios.EntrenamientoRepositorio
import com.gympulse.android.data.repositorios.SerieRepositorio
import com.gympulse.android.data.repositorios.UsuarioRepositorio
import com.gympulse.android.data.sesion.AlmacenSesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

/*
 Una clase Application se crea una sola vez, antes que cualquier Activity, y vive mientras viva el
 proceso. Es el sitio donde montar lo que debe existir una unica vez en toda la app.

 El Contenedor es inyeccion de dependencias hecha a mano: en vez de que cada pantalla se fabrique
 su propio cliente HTTP, aqui se crea uno y se reparte. Es lo mismo que hace Spring en el backend
 con los @Service, solo que alli el framework lo hace solo y aqui lo escribimos nosotros.
*/
class Contenedor(aplicacion: Application) {

    val almacenSesion: AlmacenSesion = AlmacenSesion(aplicacion)
    val preferencias: AlmacenPreferencias = AlmacenPreferencias(aplicacion)

    private val clienteBase: OkHttpClient = ClienteApi.crearClienteBase()
    private val apiSesion: ApiSesion = ClienteApi.crearApiSesion(clienteBase)
    val clienteHttp: OkHttpClient = ClienteApi.crearClienteHttp(clienteBase, almacenSesion, apiSesion)
    private val api: GymPulseApi = ClienteApi.crear(clienteHttp)

    val usuarios = UsuarioRepositorio(api, apiSesion, almacenSesion, CredentialManager.create(aplicacion))
    val fotoPerfil = FotoPerfilRepositorio(aplicacion, api, almacenSesion)
    val ejercicios = EjercicioRepositorio(api)
    val entrenamientos = EntrenamientoRepositorio(api)
    val series = SerieRepositorio(api)
}

class AppGymPulse : Application(), SingletonImageLoader.Factory {

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

    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { contenedor.clienteHttp }))
            }
            .build()
}
