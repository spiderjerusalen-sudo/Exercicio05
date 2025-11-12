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

@Composable
@Preview
fun App() {
    // 1. DEFINICIÓN DEL ESTADO OBSERVABLE
    // Usamos mutableStateListOf para que Compose detecte cambios en la lista
    val ficherosSeleccionados = remember {
        mutableStateListOf<File>()
    }

    // Detección del SO
    // Se ejecuta una vez al inicio. El resultado es el enum (SOEnum)
    val sistemaOperativo = remember {
        // Llama a la función que devuelve el String (ej. "WINDOWS")
        val soString = detectarSO()
        // Usa la función de extensión para convertirlo al enum (ej. SOEnum.WINDOWS)
        SOEnum.from(soString)
    }

    //Operaciones Seleccionadas (MutableStateSet)
    val operaSelec = remember { mutableStateSetOf<Operacion>()} // Almacena múltiples operaciones sin duplicados
    val resultados = remember { mutableStateListOf<ProcessResult>() }
    val procesando = remember { mutableStateOf(false) } // Variable para el indicador de progreso
    val scope = rememberCoroutineScope() // Ambito para lanzar la corrutina de procesamiento
    val procesador = ProceConcu(sistemaOperativo) // Instancia de la clase de lógica


    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFE5F1FF)).padding(24.dp)) {

            //  Mostrar el SO detectado
            Text(
                "SO Detectado: ${sistemaOperativo.name}",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.secondary
            )

            Spacer(Modifier.height(2.dp))

            // Título
            Text("Procesador de Ficheros Concurrente", style = MaterialTheme.typography.h4, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))


            // Selector de Operaciones (Checkboxes)
            Text("Seleccione las Operaciones:", style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Iterar sobre todas las operaciones disponibles
                Operacion.entries.forEach { operacion ->

                    // Fila que contiene la casilla y el texto
                    Row(
                        modifier = Modifier.clickable {
                            // Lógica de añadir/quitar del Set
                            if (operaSelec.contains(operacion)) {
                                operaSelec.remove(operacion)
                            } else {
                                operaSelec.add(operacion)
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = operaSelec.contains(operacion), // TRUE si está en el Set
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    operaSelec.add(operacion)
                                } else {
                                    operaSelec.remove(operacion)
                                }
                            }
                        )
                        Text(operacion.nombreContar)
                    }
                    Spacer(Modifier.width(12.dp)) // Espacio entre las casillas
                }
            }

            Spacer(Modifier.height(12.dp))

            // AQUÍ VA EL BOTÓN DE INICIO
            Button(
                onClick = {
                    // La lógica para iniciar el procesamiento concurrente va aquí
                    scope.launch {
                        // 1. Iniciar progreso e inicializar
                        procesando.value = true
                        resultados.clear() // ️ LIMPIAR RESULTADOS ANTERIORES

                        // 2. Iniciar el procesamiento con el nuevo método y el callback
                        val tareasJobs: List<Job> = procesador.iniciarProcesamiento( //  <-- CAMBIO IMPORTANTE
                            scope = this,
                            ficheros = ficherosSeleccionados.toList(),
                            operaciones = operaSelec.toList(),
                            onFinalizado = { nuevoResultado -> // <-- EL CALLBACK
                                // Este bloque se ejecuta cada vez que una tarea (fichero+operacion) termina
                                resultados.add(nuevoResultado) // El resultado se añade directamente
                            }
                        )

                        // 3. Esperar a que todas las tareas (Jobs) finalicen
                        tareasJobs.joinAll() // <-- CAMBIO IMPORTANTE: Esperamos a que todos los Jobs terminen

                        // 4. Finalizar progreso
                        procesando.value = false
                    }
                },
                // Deshabilitar el botón si no hay ficheros o no hay operaciones seleccionadas
                enabled = !procesando.value && ficherosSeleccionados.isNotEmpty() && operaSelec.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("INICIAR PROCESAMIENTO CONCURRENTE")
            }

            Spacer(Modifier.height(12.dp))

            // INDICADOR DE PROGRESO (opcional, pero recomendado)
            if (procesando.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // NUEVA ROW PARA ALINEAR EL PANEL DE SELECCIÓN Y EL BOTÓN DE CIERRE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // Esto empuja los extremos a los lados
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 2. INTEGRACIÓN DEL PANEL DE SELECCIÓN (Botton Seleccionar Archivo)
                PanelDeSeleccion(
                    // Implementación del Callback:
                    onFicherosSeleccionados = { nuevaLista ->
                        // 2a. Borrar la lista anterior
                        ficherosSeleccionados.clear()
                        // 2b. Añadir los nuevos ficheros seleccionados
                        ficherosSeleccionados.addAll(nuevaLista)
                    },
                    // Ajustamos el modificador del PanelDeSeleccion para que tome el espacio
                    modifier = Modifier.weight(1f).height(48.dp).padding(end = 16.dp)
                )

                // BOTÓN DE CIERRE (a la derecha)
                Button(
                    onClick = { exitProcess(0) }, // Llama a la función para salir de la JVM
                    modifier = Modifier.height(48.dp),
                    // Color Rojo para indicar la acción final
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                ) {
                    Text("SALIR", color = Color.White, style = MaterialTheme.typography.button)
                }

            }

            Spacer(Modifier.height(10.dp))

            // LISTADO DE FICHEROS A PROCESAR
            Text(
                text = "Archivos para procesar (${ficherosSeleccionados.size}):",
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(Modifier.height(8.dp))

            // 3. MOSTRAR LA LISTA DE FICHEROS
            if (ficherosSeleccionados.isNotEmpty()) {
                // Usamos un LazyColumn para que se pueda desplazar si hay muchos archivos.
                // El peso (1f) asegura que ocupe el espacio disponible
                LazyColumn(modifier = Modifier.weight(if (resultados.isEmpty()) 1f else 0.5f)) {
                    items(ficherosSeleccionados) { file ->
                        Text(
                            text = "» ${file.name}",
                            modifier = Modifier.padding(vertical = 4.dp), // Aumentamos el padding para separarlos
                            style = MaterialTheme.typography.body1 // Usamos body1 para mejor lectura
                        )
                        Divider()
                    }
                }
            } else {
                Text("Aún no se ha seleccionado ningún fichero.")
            }

            Spacer(Modifier.height(24.dp)) // Separador visual entre las dos secciones

            // Solo se muestra una vez que la lista 'resultados' se llena (al terminar la concurrencia).
            if (resultados.isNotEmpty()) {
                Text(
                    "Resultados del Procesamiento (${resultados.size} tareas completadas):",
                    style = MaterialTheme.typography.h6
                )
                Spacer(Modifier.height(8.dp))

                //LazyColumn de Resultados
                LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                    items(resultados) { resultado ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            elevation = 4.dp, // Usamos una elevación mayor para destacar
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