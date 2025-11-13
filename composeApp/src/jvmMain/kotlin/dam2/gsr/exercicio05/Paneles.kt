package dam2.gsr.exercicio05

import androidx.compose.material.*
import androidx.compose.runtime.*
import dam2.gsr.exercicio05.core.selecMultiFiche
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.Modifier


/**
 * Componente Composable que muestra un botón para la selección de archivos mediante un diálogo nativo.
 *
 * El diálogo de selección ([selecMultiFiche]) se ejecuta en un hilo de I/O ([Dispatchers.IO])
 * dentro de una corrutina para evitar el bloqueo del hilo principal de la Interfaz de Usuario (UI).
 *
 * @param modifier El [Modifier] de Compose para aplicar estilo y layout.
 * @param onFicherosSeleccionados Callback que se ejecuta con la lista de [File] seleccionados
 * cuando el usuario ha finalizado la selección. La lista está vacía si el usuario cancela.
 */
@Composable
fun PanelDeSeleccion(
    modifier: Modifier = Modifier,
    onFicherosSeleccionados: (List<File>) -> Unit // Callback para pasar los ficheros al procesador
) {
    // Necesitas un CoroutineScope para llamar a la función de diálogo
    // sin bloquear la UI, aunque el diálogo ya se encarga de pausar el hilo.
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch(Dispatchers.IO) {
                // La operación de diálogo debe ejecutarse fuera del hilo de la UI (Dispatchers.IO)
                val listaDeFicheros = selecMultiFiche()

                // Una vez que tenemos la lista, la pasamos a la lógica principal de la app
                if (listaDeFicheros.isNotEmpty()) {
                    onFicherosSeleccionados(listaDeFicheros)
                }
            }
        },
        // Opcional: Personalizar la apariencia del botón
    ) {
        Text("Selecciona Archivo/s")
    }
}