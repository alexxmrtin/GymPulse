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
import com.gympulse.android.ui.inicio.PantallaInicio
import com.gympulse.android.ui.login.PantallaLogin
import com.gympulse.android.ui.registro.PantallaRegistro
import com.gympulse.android.ui.theme.GymPulseTheme

// Una Activity es una pantalla. Esta es la de inicio: lo marca el AndroidManifest.
// KOTLIN: los dos puntos son el "extends" de Java, y los parentesis de ComponentActivity() indican que ademas se llama a su constructor ahi mismo, en la declaracion de la clase.
class MainActivity : ComponentActivity() {

    // Android no llama a main(). onCreate() es el primer metodo del ciclo de vida.
    // KOTLIN: "fun" declara una funcion y el tipo va DESPUES del nombre, al reves que en Java. "override" es obligatorio al redefinir un metodo del padre, no opcional como el @Override de Java. El ? de Bundle? significa que ese valor puede ser null; sin el ?, el compilador no dejaria asignarle null nunca.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dibuja bajo la barra de estado y la de navegacion.
        // SystemBarStyle.dark() significa "barra de fondo oscuro", asi que el sistema pinta sus iconos en claro. Sin esto los coge del modo del movil: si esta en modo claro los pinta oscuros y desaparecen sobre nuestro fondo negro.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        // Aqui empieza Compose: todo lo de dentro describe la interfaz.
        // KOTLIN: setContent { ... } es una llamada a funcion aunque no lo parezca. Cuando el ultimo parametro de una funcion es a su vez otra funcion, Kotlin permite sacarla fuera de los parentesis. Por eso Compose se ve como llaves anidadas. En realidad estamos haciendo setContent(content = { GymPulseTheme(content = { Scaffold(content = { PantallaInicio() }) }) }): no se pasa el RESULTADO de la llamada de dentro, sino una funcion que el padre ejecutara cuando le toque.
        setContent {
            // Aplica los colores y tipografias de ui/theme/ a todo lo de dentro.
            GymPulseTheme {

                // Estado temporal en memoria: al cerrar la app se pierde y vuelve al login. Mas adelante lo sustituiremos por DataStore para que la sesion persista.
                var pantallaActual by remember { mutableStateOf(Pantalla.LOGIN) }

                // Esqueleto de pantalla. innerPadding es el margen que Scaffold reserva para que el contenido no lo tapen las barras del sistema.
                // KOTLIN: "modifier = ..." es un argumento con nombre, se indica a que parametro corresponde en vez de fiarse del orden. Lo que va entre llaves es una funcion sin nombre que recibe de Scaffold un parametro, innerPadding, y ese valor se le pasa a PantallaInicio para decirle cuanto tiene que apartarse.
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Aqui se decide la pantalla. En Compose no se navega dando ordenes: se describe que mostrar segun el estado, y cuando pantallaActual cambia, Compose repinta solo.
                    // KOTLIN: "when" es el switch de Java pero mejor: al usarlo con un enum, el compilador obliga a cubrir todos los valores. Si anades una pantalla y olvidas tratarla, no compila.
                    when (pantallaActual) {

                        //Cada componente es una pantalla, esa pantalla puede hacer llamadas a esta pantalla principal la cual devuelve un wraper, dependiendo de cual sea ese wraper, la app sabe que pantalla tiene que renderizar
                        Pantalla.LOGIN -> PantallaLogin(
                            onLoginCorrecto = { pantallaActual = Pantalla.INICIO },
                            onIrARegistro = { pantallaActual = Pantalla.REGISTRO },

                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.REGISTRO -> PantallaRegistro(
                            onRegistroCorrecto = { pantallaActual = Pantalla.INICIO },
                            onVolverALogin = { pantallaActual = Pantalla.LOGIN },
                            
                            modifier = Modifier.padding(innerPadding)
                        )

                        Pantalla.INICIO -> PantallaInicio(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

// Las pantallas que existen. Un enum limita los valores posibles a esta lista, asi que no se puede pedir una pantalla que no exista.
enum class Pantalla { LOGIN, REGISTRO, INICIO }
