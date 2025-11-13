package dam2.gsr.exercicio05.core

/**
 * Enum para representar los sistemas operativos soportados.
 * Esto se utiliza internamente para elegir los comandos de ProcessBuilder.
 */
enum class SOEnum {
    WINDOWS,
    LINUX,
    MACOS,
    OTRO; // Para cualquier SO no soportado o no detectado

    /**
     * El 'companion object' es necesario para poder definir la funcion 'from'
     * como una funcion de extension en el mismo archivo, pero actuando como
     * un constructor de fábrica estático para el enum.
     */
    companion object
}

/**
 * Mapea la cadena de texto detectada por detectarSO() a un enum seguro.
 * Utiliza una funcion de extension sobre el 'companion object' para simular
 * una funcion de conversion estatica (e.g., SOEnum.from("WINDOWS")).
 *
 * @param soString La cadena devuelta por detectarSO() (ej: "WINDOWS", "LINUX").
 * @return El [SOEnum] correspondiente.
 */
fun SOEnum.Companion.from(soString: String): SOEnum {
    // Usamos uppercase() para que la comparación sea insensible a mayúsculas/minúsculas
    return when (soString.uppercase()) {
        "WINDOWS" -> SOEnum.WINDOWS
        "LINUX" -> SOEnum.LINUX
        "MACOS" -> SOEnum.MACOS
        else -> SOEnum.OTRO
    }
}