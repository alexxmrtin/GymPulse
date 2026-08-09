package com.gympulse.android

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gympulse.android.ui.cuentaajustes.PantallaCuentaAjustes
import com.gympulse.android.ui.idiomaajustes.PantallaIdiomaAjustes
import com.gympulse.android.ui.temaajustes.PantallaTemaAjustes
import com.gympulse.android.ui.unidadesajustes.PantallaUnidadesAjustes
import com.gympulse.android.ui.ajustes.PantallaAjustes
import com.gympulse.android.ui.notificacionesajustes.PantallaNotificacionesAjustes
import com.gympulse.android.ui.perfilajustes.PantallaPerfilAjustes
import com.gympulse.android.ui.componentes.BarraInferior
import com.gympulse.android.ui.ejercicios.PantallaEjercicios
import com.gympulse.android.ui.entrenamiento.PantallaEntrenamiento
import com.gympulse.android.ui.entrenamientovacio.PantallaEntrenamientoVacio
import com.gympulse.android.ui.estadisticas.PantallaEstadisticas
import com.gympulse.android.ui.historial.PantallaHistorial
import com.gympulse.android.ui.login.PantallaLogin
import com.gympulse.android.ui.perfil.PantallaPerfil
import com.gympulse.android.ui.registro.PantallaRegistro
import com.gympulse.android.ui.theme.GymPulseTheme

/*
 ARRANQUE DE LA APP Y NAVEGACION ENTRE PANTALLAS

 MainActivity
 Una Activity es una pantalla de Android. Esta es la que se abre al pulsar el icono, y quien lo decide es el AndroidManifest con su bloque intent-filter LAUNCHER. Solo arranca la app y elige que pantalla pintar: el contenido vive en ui/.
 KOTLIN: los dos puntos son el "extends" de Java, y los parentesis de ComponentActivity() indican que ademas se llama a su constructor ahi mismo, en la declaracion de la clase.

 onCreate
 Android no llama a un main(). Llama a metodos del ciclo de vida en momentos concretos, y onCreate() es el primero: se ejecuta al crearse la pantalla. Los demas (onStart, onPause, onDestroy...) entran cuando el usuario minimiza la app o gira el movil.
 KOTLIN: "fun" declara una funcion y el tipo va DESPUES del nombre, al reves que en Java. "override" es obligatorio al redefinir un metodo del padre, no opcional como el @Override de Java. El ? de Bundle? significa que ese valor puede ser null; sin el ?, el compilador no dejaria asignarle null nunca.

 enableEdgeToEdge
 Hace que la app dibuje por debajo de la barra de estado y la de navegacion, en vez de quedarse en el hueco del medio. SystemBarStyle.dark() significa "barra de fondo oscuro", asi que el sistema pinta sus iconos en claro; sin esto los coge del modo del movil, y en modo claro los pinta oscuros y desaparecen sobre nuestro fondo negro.

 setContent
 Es la frontera: aqui termina el Android tradicional y empieza Compose. Todo lo que va dentro de las llaves describe la interfaz.
 KOTLIN: setContent { ... } es una llamada a funcion aunque no lo parezca. Cuando el ultimo parametro de una funcion es a su vez otra funcion, Kotlin permite sacarla fuera de los parentesis, y por eso Compose se ve como llaves anidadas. En realidad es setContent(content = { GymPulseTheme(content = { ... }) }): no se pasa el RESULTADO de la llamada de dentro, sino una funcion que el padre ejecutara cuando le toque.

 GymPulseTheme
 Aplica los colores y tipografias de ui/theme/ a todo lo que envuelve. Equivale a un proveedor de tema en React.

 pantallaActual
 El estado que decide que se ve. Vive en memoria, asi que al cerrar la app se pierde y vuelve al login; mas adelante lo sustituiremos por DataStore para que la sesion persista.

 Scaffold
 El esqueleto de la pantalla: reserva los huecos estandar y calcula cuanto ocupan. innerPadding es el margen que entrega para que el contenido no lo tapen las barras del sistema ni la barra inferior. bottomBar es el hueco de abajo, y Scaffold suma su altura a ese innerPadding automaticamente.
 KOTLIN: "modifier = ..." es un argumento con nombre, se indica a que parametro corresponde en vez de fiarse del orden. Lo que va entre llaves es una funcion sin nombre que recibe de Scaffold un parametro, innerPadding.

 when y la navegacion
 En Compose no se navega dando ordenes: se describe que mostrar segun el estado, y cuando pantallaActual cambia, Compose repinta solo. Cada pantalla es un componente que recibe QUE HACER (onLoginCorrecto, onIrARegistro...) y solo avisa de que se ha pulsado algo; ninguna sabe que existen las demas ni toca pantallaActual directamente. Eso funciona porque esos bloques se escriben aqui, y una funcion se lleva consigo las variables del sitio donde fue escrita.
 KOTLIN: "when" es el switch de Java pero mejor: al usarlo con un enum, el compilador obliga a cubrir todos los valores. Si anades una pantalla y olvidas tratarla, no compila.

 enum Pantalla
 La lista cerrada de pantallas que existen, y cada una lleva asociado si muestra la barra inferior. Se hace asi en vez de poner la condicion suelta en el Scaffold para que, al anadir una pantalla nueva, el compilador obligue a decidirlo.
*/
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        setContent {
            GymPulseTheme {

                //Por defecto enseña la pagina PantallaLogin
                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    
                    //Muestra la barra inferior si "muestraBarraInferior" es true
                    bottomBar = {
                        if (pantallaActual.muestraBarraInferior) {
                            BarraInferior(
                                pantallaActual = pantallaActual,
                                onPantallaSeleccionada = { pantallaActual = it }
                            )
                        }
                    }

                ) { innerPadding ->
                    when (pantallaActual) {

                        Pantalla.LOGIN -> PantallaLogin(
                            onLoginCorrecto = { pantallaActual = Pantalla.PERFIL },
                            onIrARegistro = { pantallaActual = Pantalla.REGISTRO },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.REGISTRO -> PantallaRegistro(
                            onRegistroCorrecto = { pantallaActual = Pantalla.PERFIL },
                            onVolverALogin = { pantallaActual = Pantalla.LOGIN },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.ENTRENAMIENTO -> PantallaEntrenamiento(
                            onEjercicios = { pantallaActual = Pantalla.EJERCICIOS },
                            onEntrenamientoVacio = { pantallaActual = Pantalla.ENTRENAMIENTO_VACIO },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.EJERCICIOS -> PantallaEjercicios(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.ENTRENAMIENTO_VACIO -> PantallaEntrenamientoVacio(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.PERFIL -> PantallaPerfil(
                            onEstadisticas = { pantallaActual = Pantalla.ESTADISTICAS },
                            onHistorial = { pantallaActual = Pantalla.HISTORIAL },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.ESTADISTICAS -> PantallaEstadisticas(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.HISTORIAL -> PantallaHistorial(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.AJUSTES -> PantallaAjustes(
                            onPerfil = { pantallaActual = Pantalla.PERFIL_AJUSTES },
                            onCuenta = { pantallaActual = Pantalla.CUENTA_AJUSTES },
                            onNotificaciones = { pantallaActual = Pantalla.NOTIFICACIONES_AJUSTES },
                            onIdioma = { pantallaActual = Pantalla.IDIOMA_AJUSTES },
                            onTema = { pantallaActual = Pantalla.TEMA_AJUSTES },
                            onUnidades = { pantallaActual = Pantalla.UNIDADES_AJUSTES },
                            onCerrarSesion = { pantallaActual = Pantalla.LOGIN },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.PERFIL_AJUSTES -> PantallaPerfilAjustes(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.CUENTA_AJUSTES -> PantallaCuentaAjustes(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.NOTIFICACIONES_AJUSTES -> PantallaNotificacionesAjustes(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.IDIOMA_AJUSTES -> PantallaIdiomaAjustes(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.TEMA_AJUSTES -> PantallaTemaAjustes(
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.UNIDADES_AJUSTES -> PantallaUnidadesAjustes(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

enum class Pantalla(val muestraBarraInferior: Boolean) {
    LOGIN(muestraBarraInferior = false),
    REGISTRO(muestraBarraInferior = false),
    ENTRENAMIENTO(muestraBarraInferior = true),
    PERFIL(muestraBarraInferior = true),
    AJUSTES(muestraBarraInferior = true),
    ESTADISTICAS(muestraBarraInferior = true),
    HISTORIAL(muestraBarraInferior = true),
    EJERCICIOS(muestraBarraInferior = true),
    ENTRENAMIENTO_VACIO(muestraBarraInferior = true),
    PERFIL_AJUSTES(muestraBarraInferior = true),
    CUENTA_AJUSTES(muestraBarraInferior = true),
    NOTIFICACIONES_AJUSTES(muestraBarraInferior = true),
    IDIOMA_AJUSTES(muestraBarraInferior = true),
    TEMA_AJUSTES(muestraBarraInferior = true),
    UNIDADES_AJUSTES(muestraBarraInferior = true)
}
