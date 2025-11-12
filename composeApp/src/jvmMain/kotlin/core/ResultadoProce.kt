package dam2.gsr.exercicio05.core

import dam2.gsr.exercicio05.core.Operacion

/**
 * Clase sellada para representar el resultado del procesamiento de un solo archivo.
 * Esto permite manejar el exito o el fracaso de forma limpia (manejo de errores).
 */
sealed class ProcessResult {
    // Definimos las propiedades comunes con 'abstract'
    abstract val nombreFichero: String
    abstract val operacion: Operacion

    // Clase para resultados exitosos
    data class Success(
        override val nombreFichero: String,
        override val operacion: Operacion,
        val valor: Long
    ) : ProcessResult()

    // Clase para errores
    data class Failure(
        override val nombreFichero: String,
        override val operacion: Operacion,
        val mensajeError: String // Descripcion del error (ej: fichero no encontrado, fallo de comando)
    ) : ProcessResult()
}