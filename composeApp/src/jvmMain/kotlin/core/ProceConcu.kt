package dam2.gsr.exercicio05.core

import dam2.gsr.exercicio05.core.Operacion
import dam2.gsr.exercicio05.core.SOEnum
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

typealias SO = SOEnum

/**
 * Clase que gestiona la ejecucion concurrente de procesos externos por fichero.
 *
 * Utiliza comandos nativos del sistema operativo (wc en Unix, PowerShell en Windows)
 * a traves de ProcessBuilder para realizar las operaciones.
 *
 * @param so El SO detectado para construir los comandos adecuados.
 */
class ProceConcu(private val so: SO) {

    init {
        if (so == SO.OTRO) {
            throw UnsupportedOperationException("SO no soportado: ${so.name}")
        }
    }

    /**
     * Genera el array de comandos especifico para el SO y la operacion.
     */
    private fun generarComandoArray(ficheroPath: String, operacion: Operacion): List<String> {
        // Escapamos la ruta del fichero para evitar problemas con espacios en los nombres de archivo
        val rutaEscapada = when (so) {
            SO.WINDOWS -> "\"$ficheroPath\"" // PowerShell necesita comillas dobles
            else -> ficheroPath // Unix puede usar rutas directas
        }

        return when (so) {
            // PRIORIDAD WINDOWS (PowerShell)
            SO.WINDOWS -> {
                when (operacion) {
                    Operacion.ContarLineas -> listOf(
                        "powershell",
                        "-Command",
                        "(Get-Content -Path $rutaEscapada | Measure-Object -Line).Lines"
                    )

                    Operacion.ContarPalabras -> listOf(
                        "powershell",
                        "-Command",
                        "(Get-Content -Path $rutaEscapada | Measure-Object -Word).Words"
                    )

                    Operacion.ContarBytes -> listOf("powershell", "-Command", "(Get-Item -Path $rutaEscapada).Length")
                    Operacion.ContarCaracteres -> listOf(
                        "powershell",
                        "-Command",
                        "(Get-Content -Path $rutaEscapada | Measure-Object -Character).Characters"
                    )
                }
            }

            // AGRUPACION UNIX (LINUX y MACOS)
            SO.LINUX, SO.MACOS -> {
                // wc usa -l (lineas), -w (palabras), -m (caracteres), -c (bytes)
                // Se usa la ruta directa ya que la gestionamos al inicio con rutaEscapada
                val comandoBase = if (so == SO.MACOS) "/usr/bin/wc" else "wc" // Mac a veces requiere ruta completa
                listOf(comandoBase, operacion.parametroComando, rutaEscapada)
            }

            SO.OTRO -> throw IllegalStateException("Error de logica interna. El SO 'OTRO' debería haber sido filtrado.")
        }
    }

    /**
     * Ejecuta ProcessBuilder para una única tarea (fichero + operación).
     * Esta ejecución es síncrona dentro de la corrutina de Dispatchers.IO.
     *
     * @return El resultado del proceso, éxito o fallo.
     */
    private fun ejecutarProceso(fichero: File, operacion: Operacion): ProcessResult {
        if (!fichero.exists() || fichero.isDirectory) {
            return ProcessResult.Failure(fichero.name, operacion, "Error: El fichero no existe o es un directorio.")
        }

        val comando = generarComandoArray(fichero.absolutePath, operacion)

        try {
            val processBuilder = ProcessBuilder(comando)
            processBuilder.redirectErrorStream(true) // Combina stdout y stderr

            val process = processBuilder.start()

            // Esperar con un timeout (15 segundos)
            val finished = process.waitFor(15, TimeUnit.SECONDS)

            if (!finished) {
                process.destroyForcibly()
                return ProcessResult.Failure(
                    fichero.name,
                    operacion,
                    "Error: El proceso excedio el tiempo limite (15s)."
                )
            }

            val resultadoLinea = process.inputStream.bufferedReader().use { it.readLine()?.trim() ?: "" }
            val exitCode = process.exitValue()

            if (exitCode != 0 || resultadoLinea.isEmpty()) {
                val errorMsg = if (exitCode != 0) "Comando nativo fallo (código $exitCode)." else "La salida fue vacía."
                return ProcessResult.Failure(fichero.name, operacion, errorMsg)
            }

            // Parsear el resultado (obtener solo el valor numérico)
            val valorStr = resultadoLinea.trim().split(Regex("\\s+")).firstOrNull()

            val valor = valorStr?.toLongOrNull()

            return if (valor != null) {
                ProcessResult.Success(fichero.name, operacion, valor)
            } else {
                ProcessResult.Failure(fichero.name, operacion, "Error de parseo del resultado: '$resultadoLinea'")
            }

        } catch (e: IOException) {
            return ProcessResult.Failure(
                fichero.name,
                operacion,
                "Error de E/S. El comando no se encontro o no se pudo ejecutar: ${e.message}"
            )
        } catch (e: Exception) {
            return ProcessResult.Failure(fichero.name, operacion, "Error desconocido al ejecutar proceso: ${e.message}")
        }
    }


    /**
     * Inicia el procesamiento concurrente, lanzando una corrutina (Job) por cada tarea individual.
     *
     * @param scope Ambito de Corrutinas (ej: de la UI) donde se lanzaran las tareas.
     * @param ficheros La lista de archivos a procesar.
     * @param operaciones El conjunto de operaciones a realizar por fichero.
     * @param onFinalizado Callback que se llama cuando una *única* tarea (fichero + operación) ha terminado.
     * @return Una lista de Jobs para poder esperar a la finalización de *todo* el proceso.
     */
    // ⭐ CAMBIO CLAVE EN LA FIRMA
    fun iniciarProcesamiento(
        scope: CoroutineScope,
        ficheros: List<File>,
        operaciones: List<Operacion>, // Lo dejamos como List<Operacion> para ser consistente con el procesarFichero
        onFinalizado: (ProcessResult) -> Unit // ⭐ EL CALLBACK
    ): List<Job> { // ⭐ RETORNA UNA LISTA DE JOBS

        if (operaciones.isEmpty() || ficheros.isEmpty()) {
            return emptyList()
        }

        val todosLosJobs = mutableListOf<Job>()

        // Iterar sobre cada fichero y operación
        ficheros.forEach { fichero ->
            operaciones.forEach { operacion ->

                // 3. Lanzar una corrutina (Job) por cada TAREA (fichero + operación)
                val job = scope.launch(Dispatchers.IO) {
                    // Ejecutar el proceso síncrono en el hilo IO
                    val resultado = ejecutarProceso(fichero, operacion)

                    // Notificar el progreso a la UI mediante el callback.
                    // Aseguramos que el callback se ejecuta en el hilo principal (Main)
                    withContext(Dispatchers.Main) {
                        onFinalizado(resultado)
                    }
                }
                todosLosJobs.add(job)
            }
        }

        return todosLosJobs
    }
}