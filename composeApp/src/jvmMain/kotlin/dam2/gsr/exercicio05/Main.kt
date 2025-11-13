package dam2.gsr.exercicio05

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState


/**
 * Función principal (punto de entrada) de la aplicación de escritorio Compose.
 *
 * Configura la ventana principal de la aplicación, estableciendo su tamaño inicial y su título,
 * y lanza el componente principal [App].
 */
fun main() = application {

    // 1. Definimos el estado de la ventana, incluyendo el tamaño inicial
    val windowState = rememberWindowState(
        // Tamaño fijo inicial: Ancho 750dp, Altura 900dp (para que se vea todo el contenido)
        size = DpSize(width = 750.dp, height = 900.dp)
    )

    Window(
        onCloseRequest = ::exitApplication, // Permite cerrar la aplicación
        title = "Proyecto Procesador de ficheros por Gonzalo",
        state = windowState, // 2. Aplicamos el estado con el tamaño fijo
    ) {
        App() // Llama a tu Composable principal
    }
}