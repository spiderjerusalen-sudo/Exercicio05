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
 * a traves de ProcessBuilder para realizar las operaciones de conteo.
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
     *
     * La implementacion utiliza 'wc' para sistemas Unix (Linux/macOS) y una version
     * adaptada con 'Get-Content' y 'Measure-Object' para Windows.
     *
     * @param ficheroPath La ruta absoluta del fichero a procesar.
     * @param operacion La operacion de conteo a realizar (Lineas, Palabras, Caracteres, Bytes).
     * @return Una lista de cadenas que representan el comando y sus argumentos.
     */
    private fun generarComandoArray(ficheroPath: String, operacion: Operacion): List<String> {
        // Escapamos la ruta del fichero para evitar problemas con espacios en los nombres de archivo
        val rutaEscapada = when (so) {
            SO.WINDOWS -> "\"$ficheroPath\"" // PowerShell necesita comillas dobles
            else -> ficheroPath // Unix puede usar rutas directas
        }

        return when (so) {
            SO.WINDOWS -> {
                // Comando PowerShell: Get-Content "ruta" | Measure-Object -<parametro> | Select-Object -ExpandProperty <propiedad>
                // Se usa 'Measure-Object' para contar y 'Select-Object' para extraer solo el valor.
                val propiedadWindows = when (operacion) {
                    Operacion.ContarLineas -> "Lines"
                    Operacion.ContarPalabras -> "Words"
                    Operacion.ContarCaracteres, Operacion.ContarBytes -> "Characters" // Ambos usan Characters en esta implementacion simple de Measure-Object
                }
                listOf(
                    "powershell.exe",
                    "-Command",
                    "Get-Content -Path $rutaEscapada | Measure-Object ${operacion.parametroComando} | Select-Object -ExpandProperty $propiedadWindows"
                )
            }
            // Unix: Linux y macOS
            SO.LINUX, SO.MACOS -> {
                // Comando Unix: wc -<parametro> "ruta"
                listOf(
                    "wc",
                    operacion.parametroComando,
                    ficheroPath
                )
            }
            SO.OTRO -> throw UnsupportedOperationException("SO no soportado.")
        }
    }

    /**
     * Ejecuta el proceso externo de forma síncrona para un fichero y operacion dados.
     *
     * Este metodo utiliza [ProcessBuilder] para ejecutar el comando nativo.
     * El tiempo de espera maximo para el proceso es de 10 segundos.
     *
     * @param fichero El objeto [File] a procesar.
     * @param operacion La [Operacion] a realizar.
     * @return Un objeto [ProcessResult] que sera [ProcessResult.Success] si tiene exito, o [ProcessResult.Failure] en caso contrario.
     */
    private fun ejecutarProceso(fichero: File, operacion: Operacion): ProcessResult {
        // Comprobacion rapida de existencia
        if (!fichero.exists()) {
            return ProcessResult.Failure(fichero.name, operacion, "Fichero no encontrado.")
        }

        try {
            val comando = generarComandoArray(fichero.absolutePath, operacion)
            val process = ProcessBuilder(comando)
                .redirectErrorStream(true) // Combina stdout y stderr
                .start()

            // Esperar a que el proceso termine con un timeout
            val finished = process.waitFor(10, TimeUnit.SECONDS)

            if (!finished) {
                process.destroyForcibly()
                return ProcessResult.Failure(fichero.name, operacion, "Timeout (mas de 10 segundos).")
            }

            // Si el comando no tuvo exito (ej: error en la ruta, permisos)
            if (process.exitValue() != 0) {
                val errorOutput = process.inputStream.bufferedReader().use { it.readText() }
                // Devolver error del comando.
                return ProcessResult.Failure(fichero.name, operacion, "Fallo del comando: $errorOutput")
            }

            // Leer la salida y extraer el valor (el numero)
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()

            // Intentar extraer el valor numerico
            val valor = when (so) {
                SO.WINDOWS -> output.toLongOrNull() // PowerShell devuelve el numero directamente
                SO.LINUX, SO.MACOS -> output.trim().split(Regex("\\s+")).firstOrNull()?.toLongOrNull() // wc devuelve "  123 ruta"
                SO.OTRO -> null // No deberia ocurrir
            }

            if (valor != null) {
                return ProcessResult.Success(fichero.name, operacion, valor)
            } else {
                return ProcessResult.Failure(fichero.name, operacion, "No se pudo extraer el valor del resultado: '$output'")
            }

        } catch (e: IOException) {
            return ProcessResult.Failure(fichero.name, operacion, "Error de I/O: ${e.message}")
        } catch (e: SecurityException) {
            return ProcessResult.Failure(fichero.name, operacion, "Error de seguridad (permisos): ${e.message}")
        } catch (e: Exception) {
            return ProcessResult.Failure(fichero.name, operacion, "Error inesperado: ${e.message}")
        }
    }


    /**
     * Inicia el procesamiento concurrente de multiples ficheros y operaciones.
     *
     * Lanza una corrutina por cada combinacion (fichero + operacion) utilizando [Dispatchers.IO]
     * para no bloquear la UI. Cada vez que una tarea finaliza, se llama al callback
     * [onFinalizado] para actualizar la interfaz.
     *
     * @param scope El [CoroutineScope] en el que se lanzaran las corrutinas (normalmente el scope de la UI).
     * @param ficheros Lista de [File] a procesar.
     * @param operaciones Lista de [Operacion] a aplicar a cada fichero.
     * @param onFinalizado Callback que se invoca cuando cada tarea (fichero + operacion) ha terminado.
     * @return Una lista de [Job] para poder esperar a la finalizacion de *todo* el proceso (ej: usando `joinAll`).
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