package dam2.gsr.exercicio05

import androidx.compose.material.*
import androidx.compose.runtime.*
import core.selecMultiFiche
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.Modifier


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