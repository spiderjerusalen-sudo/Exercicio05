# **Proyecto 5: Procesador de Ficheros Concurrente Multi-SO**

## **Introducción y Requisitos Esenciales**

Aplicación desarrollada en Kotlin que gestiona el procesamiento de múltiples archivos de texto de forma **concurrente** mediante el uso de **procesos separados** (ProcessBuilder).

**Requisitos Clave:**

* Selección de múltiples ficheros.  
* Procesamiento individual de cada fichero en un **proceso externo**.  
* Operaciones: Contar Líneas, Palabras o Caracteres (a elección del usuario).  
* **Compatibilidad Multi-SO:** La lógica de ejecución debe adaptarse a Windows, Linux y macOS.  
* Muestra de progreso individual y manejo de errores.

## **Arquitectura y Concurrencia**

| Tecnología | Propósito |
| :---- | :---- |
| **Kotlin** | Lenguaje principal de desarrollo. |
| **ProcessBuilder** | **Concurrencia real** mediante el lanzamiento de procesos hijo. |
| **JavaFX / Compose** | Interfaz Gráfica (UI) responsiva. |

### **Diseño (Multi-SO)**

La arquitectura se basa en dos capas clave:

1. **Detección de SO:** Se identifica el sistema operativo (System.getProperty("os.name")) para seleccionar la estrategia de comando.  
2. **Gestión de Comandos:** Para cada operación, ProcessBuilder construye el comando específico:  
   * **Linux/macOS:** Utiliza los comandos nativos (wc \-l, wc \-w, wc \-c).  
   * **Windows:** Emplea una alternativa nativa (por ejemplo, PowerShell) o una función interna de Kotlin/Java ejecutada en el proceso hijo, ya que el comando wc no está disponible por defecto.

## **Funcionalidades e Implementación**

El usuario selecciona archivos y elige las operaciones. Al iniciar, la aplicación:

1. Lanza un proceso independiente por cada archivo/operación.  
2. Lee la salida del proceso (stdout) para obtener el resultado.  
3. Actualiza la interfaz de forma asíncrona para mostrar el progreso y el resultado final.  
4. Captura errores de proceso (ej. archivo no encontrado) mediante el código de salida (exitCode \!= 0).

## **Pruebas (Resumen)**

Se verificó el **procesamiento correcto** de los conteos (Líneas, Palabras, Caracteres), la **detección efectiva del SO** para usar la sintaxis de comandos correcta, y el **manejo de errores** al intentar procesar ficheros inexistentes.

## **Conclusiones y Dificultades**

El principal reto fue lograr la **compatibilidad total entre sistemas operativos**. Implementar la lógica para que ProcessBuilder funcionara de forma eficiente tanto con wc en entornos UNIX como con la solución alternativa en Windows requirió una cuidadosa capa de abstracción. Se logró mantener la UI fluida al delegar el trabajo pesado a los procesos del sistema.

## **Enlace al Repositorio del Proyecto**

[https://github.com/spiderjerusalen-sudo/Exercicio05.git](https://github.com/spiderjerusalen-sudo/Exercicio05.git)

