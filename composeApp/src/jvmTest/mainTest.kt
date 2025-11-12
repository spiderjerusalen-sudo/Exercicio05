package dam2.gsr.exercicio05

import dam2.gsr.exercicio05.core.detectarSO
import dam2.gsr.exercicio05.core.SOEnum
import dam2.gsr.exercicio05.core.ProceConcu
import dam2.gsr.exercicio05.core.Operacion
import dam2.gsr.exercicio05.core.ProcessResult
import dam2.gsr.exercicio05.core.from
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File

// NOTA: Esta funcion reemplaza temporalmente la llamada a 'App()' para la prueba.
fun mainTest
            () = runBlocking {
    println("--- INICIO PRUEBA NUCLEO CONCURRENTE ---")

    // 1. Deteccion de SO e inicializacion
    val soDetectado = SOEnum.from(detectarSO())
    println("SO Detectado: ${soDetectado.name}")

    val procesador = ProceConcu(soDetectado)

    // 2. Definicion de la prueba
    val fichero = File("pruebaTexto.txt") // <--- Archivo de prueba actualizado
    val operaciones = Operacion.entries.toList() // Contar Lineas, Palabras, Caracteres, Bytes

    if (!fichero.exists()) {
        println("ERROR: El fichero de prueba '${fichero.name}' no existe. Crealo para continuar.")
        return@runBlocking
    }

    // 3. Iniciar el procesamiento concurrente (se lanza una corrutina por fichero)
    val tareas = procesador.iniciarProceConcu(
        scope = this, // Usamos el scope de runBlocking
        ficheros = listOf(fichero),
        operaciones = operaciones
    )

    // 4. Esperar y recolectar resultados
    val resultadosListas = tareas.awaitAll()
    val resultadosFinales = resultadosListas.flatten()

    // 5. Mostrar resultados
    println("\n--- RESULTADOS ---")
    resultadosFinales.forEach { resultado ->
        when (resultado) {
            is ProcessResult.Success -> {
                println("${resultado.operacion}: ${resultado.resultado}")
            }
            is ProcessResult.Failure -> {
                println("${resultado.operacion} FALLO: ${resultado.mensajeError}")
            }
        }
    }
    println("--- PRUEBA FINALIZADA ---")
}