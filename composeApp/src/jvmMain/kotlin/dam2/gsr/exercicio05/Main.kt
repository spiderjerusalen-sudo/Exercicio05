package dam2.gsr.exercicio05

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState


// Esto es un ejemplo de cómo se ve el punto de entrada
    fun main() = application {

        // 1. Definimos el estado de la ventana, incluyendo el tamaño inicial
        val windowState = rememberWindowState(
            // Tamaño fijo inicial: Ancho 700dp, Altura 950dp (para que se vea todo el contenido)
            size = DpSize(width = 750.dp, height = 900.dp)
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "Proyecto Procesador de ficheros por Gonzalo",
            state = windowState, // 2. Aplicamos el estado con el tamaño fijo
        ) {
            App() // Llama a tu Composable principal
        }
    }
