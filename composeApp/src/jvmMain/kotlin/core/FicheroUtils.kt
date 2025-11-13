package core


import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Muestra el diálogo de selección de múltiples ficheros nativo del sistema operativo (AWT).
 *
 * Esta función es bloqueante y debe ser llamada desde un hilo de I/O (ej: [Dispatchers.IO])
 * para no bloquear el hilo principal de la UI de Compose.
 *
 * @return Una lista de los ficheros seleccionados como objetos [File], o una lista vacía si se cancela.
 */
fun selecMultiFiche(): List<File> {
    val framePadre = Frame()
    val fileDialog = FileDialog(framePadre, "Seleccionar Ficheros a Procesar", FileDialog.LOAD)

    // Habilitar la selección múltiple
    fileDialog.isMultipleMode = true

    fileDialog.isVisible = true

    val archivosSeleccionados: Array<File>? = fileDialog.files

    framePadre.dispose()

    return archivosSeleccionados?.toList() ?: emptyList()
}