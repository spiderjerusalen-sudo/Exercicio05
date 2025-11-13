package dam2.gsr.exercicio05.core

import java.awt.FileDialog
import java.awt.Frame
import java.io.File

/**
 * Muestra el diálogo de selección de múltiples ficheros nativo del sistema operativo.
 * @return Una lista de los ficheros seleccionados (objetos File).
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