package dam2.gsr.exercicio05

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dam2.gsr.exercicio05.core.Operacion
import dam2.gsr.exercicio05.core.ProceConcu
import dam2.gsr.exercicio05.core.ProcessResult
import dam2.gsr.exercicio05.core.SOEnum
import dam2.gsr.exercicio05.core.detectarSO
import dam2.gsr.exercicio05.core.from
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job       // Importación necesaria para el nuevo patrón
import kotlinx.coroutines.joinAll   // Importación necesaria para esperar a todos los Jobs
import kotlin.system.exitProcess
import androidx.compose.foundation.background

/**
 * Componente Composable principal de la aplicación.
 *
 * Configura el estado, detecta el SO, maneja la selección de archivos,
 * inicia el procesamiento concurrente y muestra los resultados en la interfaz de usuario.
 */
@Composable
@Preview
fun App() {
    // 1. DEFINICIÓN DEL ESTADO OBSERVABLE
    // Usamos mutableStateListOf para que Compose detecte cambios en la lista
    val ficherosSeleccionados = remember {
        mutableStateListOf<File>()
    }

    // Resultados del procesamiento (fichero + operación + resultado/error)
    val resultados = remember {
        mutableStateListOf<ProcessResult>()
    }

    // Estado para saber si se está procesando actualmente
    val isProcessing = remember {
        mutableStateOf(false)
    }

    // Lista de Jobs activos para saber cuándo el procesamiento ha terminado completamente
    val activeJobs = remember {
        mutableStateListOf<Job>()
    }

    // Detección del SO
    val osDetected = remember {
        SOEnum.from(detectarSO())
    }

    // Inicialización del procesador
    val procesador = remember {
        ProceConcu(osDetected)
    }

    // Coroutine Scope para manejar las operaciones asíncronas de la UI
    val scope = rememberCoroutineScope()


    // Operaciones que el usuario puede seleccionar
    val operacionesDisponibles = remember {
        Operacion.values().toList()
    }
    // Operaciones seleccionadas (mutableStateOf para que Compose se redibuje al cambiar)
    val selectedOperations = remember {
        mutableStateListOf<Operacion>(Operacion.ContarLineas) // Líneas por defecto
    }

    // Función que se pasa al PanelDeSeleccion para actualizar la lista de ficheros
    val onFilesSelected: (List<File>) -> Unit = { files ->
        // Limpiar la lista anterior y añadir los nuevos, forzando un redibujo.
        ficherosSeleccionados.clear()
        ficherosSeleccionados.addAll(files)

        // Limpiar resultados anteriores
        resultados.clear()
    }

    /**
     * Inicia la ejecucion de todos los procesos (ficheros x operaciones).
     */
    val startProcessing: () -> Unit = {
        // 1. Limpiar resultados y marcar inicio de procesamiento
        resultados.clear()
        isProcessing.value = true
        activeJobs.clear()

        // 2. Iniciar todas las tareas concurrentes y capturar los Jobs
        val newJobs = procesador.iniciarProcesamiento(
            scope = scope,
            ficheros = ficherosSeleccionados.toList(),
            operaciones = selectedOperations.toList(),
            onFinalizado = { resultadoParcial ->
                // Este callback se ejecuta en el hilo principal
                resultados.add(resultadoParcial)
            }
        )
        activeJobs.addAll(newJobs)

        // 3. Lanzar una corrutina de supervisión para esperar a que todos terminen
        scope.launch {
            try {
                activeJobs.joinAll() // Esperar a que todos los jobs terminen
            } catch (e: Exception) {
                // Manejo de cancelación o errores inesperados
                println("Error durante joinAll: ${e.message}")
            } finally {
                // Esto se ejecuta SÍ o SÍ cuando todos los jobs hayan terminado (o hayan sido cancelados)
                isProcessing.value = false
                activeJobs.clear() // Limpiar la lista de Jobs
            }
        }
    }


    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TÍTULO y DETECCIÓN de SO
            Text(
                "Procesador de Ficheros Concurrente",
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "SO Detectado: ${osDetected.name}",
                style = MaterialTheme.typography.subtitle1,
                color = if (osDetected == SOEnum.OTRO) Color.Red else MaterialTheme.colors.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // SECCIÓN DE SELECCIÓN DE ARCHIVOS
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón para seleccionar archivos
                PanelDeSeleccion(
                    onFicherosSeleccionados = onFilesSelected
                )

                // Botón para iniciar el procesamiento
                Button(
                    onClick = startProcessing,
                    enabled = !isProcessing.value && ficherosSeleccionados.isNotEmpty() && selectedOperations.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        disabledBackgroundColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(if (isProcessing.value) "Procesando..." else "INICIAR PROCESAMIENTO")
                    if (isProcessing.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp).padding(start = 8.dp),
                            color = MaterialTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // SECCIÓN DE FICHEROS Y OPERACIONES SELECCIONADAS
            Card(
                modifier = Modifier.fillMaxWidth().height(200.dp).padding(vertical = 8.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Lista de ficheros seleccionados
                    Text("Ficheros a procesar (${ficherosSeleccionados.size}):",
                        style = MaterialTheme.typography.h6)

                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (ficherosSeleccionados.isEmpty()) {
                            item {
                                Text("No hay ficheros seleccionados.", style = MaterialTheme.typography.body2)
                            }
                        }
                        items(ficherosSeleccionados) { fichero ->
                            Text(
                                text = fichero.name,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Opciones de operación (Checkboxes)
                    Text("Operaciones:", style = MaterialTheme.typography.h6)
                    Row(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        operacionesDisponibles.forEach { operacion ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val isChecked = selectedOperations.contains(operacion)
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { nuevoEstado ->
                                        if (nuevoEstado) {
                                            selectedOperations.add(operacion)
                                        } else {
                                            selectedOperations.remove(operacion)
                                        }
                                    }
                                )
                                Text(operacion.nombreContar, style = MaterialTheme.typography.body2)
                            }
                        }
                    }
                }
            }

            // SECCIÓN DE RESULTADOS
            Text(
                "Resultados (Tareas Completadas: ${resultados.size} / ${ficherosSeleccionados.size * selectedOperations.size})",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            // Lista de resultados
            Card(
                modifier = Modifier.fillMaxSize().weight(1f),
                elevation = 4.dp
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (resultados.isEmpty() && !isProcessing.value) {
                        item {
                            Text("Los resultados aparecerán aquí.",
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray)
                        }
                    }
                    items(resultados) { resultado ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            elevation = 2.dp,
                            backgroundColor = when (resultado) {
                                is ProcessResult.Success -> MaterialTheme.colors.surface
                                is ProcessResult.Failure -> MaterialTheme.colors.error.copy(alpha = 0.1f) // Fondo sutilmente rojo
                            }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Fichero y Operación
                                Text("Fichero: ${resultado.nombreFichero}",
                                    style = MaterialTheme.typography.caption)

                                when (resultado) {
                                    is ProcessResult.Success -> {
                                        Text("${resultado.operacion.aliasnombre} ${resultado.valor}",
                                            color = MaterialTheme.colors.primary,
                                            style = MaterialTheme.typography.subtitle1)
                                    }
                                    is ProcessResult.Failure -> {
                                        Text("${resultado.operacion.aliasnombre} ${resultado.mensajeError}",
                                            color = MaterialTheme.colors.error,
                                            style = MaterialTheme.typography.subtitle1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}