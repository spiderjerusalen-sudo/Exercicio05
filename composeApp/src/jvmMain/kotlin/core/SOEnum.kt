package dam2.gsr.exercicio05.core

/**
 * Enum para representar los sistemas operativos soportados.
 * Esto se utiliza internamente para elegir los comandos de ProcessBuilder.
 */
enum class SOEnum {
    WINDOWS,
    LINUX,
    MACOS,
    OTRO;

    companion object
}
/**
 * Mapea la cadena de texto detectada por detectarSO() a un enum seguro.
 * @param soString La cadena devuelta por detectarSO().
 * @return El [SOEnum] correspondiente.
 */
fun SOEnum.Companion.from(soString: String): SOEnum {
    return when (soString.uppercase()) {
        "WINDOWS" -> SOEnum.WINDOWS
        "LINUX" -> SOEnum.LINUX
        "MACOS" -> SOEnum.MACOS
        else -> SOEnum.OTRO
    }
}
