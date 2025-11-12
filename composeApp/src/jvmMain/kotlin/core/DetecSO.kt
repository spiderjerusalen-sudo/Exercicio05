package dam2.gsr.exercicio05.core // <- El nuevo paquete

/**
 * Detecta y devuelve el tipo de sistema operativo en el que se está ejecutando la aplicación.
 * @return Una cadena que indica el sistema operativo: "WINDOWS", "MACOS", "LINUX", o "OTROS".
 */
fun detectarSO(): String {
    // 1. Obtener la propiedad del sistema.
    val nombreOSBruto = System.getProperty("os.name").lowercase()

    // 2. Comprobar las palabras clave conocidas.
    return when {
        nombreOSBruto.contains("win") -> "WINDOWS"
        nombreOSBruto.contains("mac") -> "MACOS"
        nombreOSBruto.contains("nux") || nombreOSBruto.contains("lin") -> "LINUX"
        else -> "OTROS"
    }
}

// Ejemplo de uso:
fun main() {
    val os = detectarSO()
    println("Sistema Operativo detectado: $os")
}