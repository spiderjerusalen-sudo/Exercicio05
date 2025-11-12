package dam2.gsr.exercicio05.core

/**
 * Enum para representar las operaciones de procesamiento de ficheros.
 *
 * Cada operacion lleva asociado el parsmetro que utiliza el comando 'wc'
 * en sistemas Unix (Linux/macOS) para esa funcion.
 */
enum class Operacion(val nombreContar: String, val aliasnombre:String, val parametroComando: String) {
    ContarLineas("Contar Lineas", "Lineas: ","-l"),       // Requisito 1
    ContarPalabras("Contar Palabras", "Palabras: ", "-w"),     // Requisito 2
    ContarCaracteres("Contar Caracteres", "Caracteres: ","-m"),   // Requisito 3 (Multibyte character count)
    ContarBytes("Contar Bytes", "Bytes: ", "-c")         // Operacion extra: Contar el tamano del fichero en bytes
}