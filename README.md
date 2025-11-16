# ** Procesador de Ficheros Concurrente Multi-Sistema Operativo**

## **Introducción y Requisitos Generales**

Este proyecto, **Proyecto 5: Procesador de ficheros concurrente**, consiste en una aplicación desarrollada en Kotlin que gestiona el procesamiento de múltiples archivos de texto de manera simultánea.

La funcionalidad principal es:

* Permitir la selección de múltiples ficheros.  
* Procesar cada fichero en un **proceso separado** utilizando ProcessBuilder.  
* Ofrecer operaciones de procesamiento a elección del usuario (contar líneas, palabras o caracteres).  
* Mostrar el progreso individual y el resultado final.  
* Implementar manejo de errores.  
* **Requisito clave:** La lógica de procesamiento debe ser compatible con distintos sistemas operativos (Windows, Linux, macOS).

## **Descripción de Tecnologías y Arquitectura**

### **Tecnologías Usadas**

| Tecnología | Propósito |
| :---- | :---- |
| **Kotlin** | Lenguaje principal de desarrollo. |
| **JavaFX / Compose** | Framework para la interfaz gráfica (UI). |
| **ProcessBuilder** | Gestión de procesos externos para concurrencia. |
| **Git/GitHub** | Control de versiones y repositorio público. |

### **Diseño de la Arquitectura**

*(Describe aquí brevemente cómo has estructurado el código: Patrones de concurrencia utilizados (Threads, Coroutines, etc.), la función de detección de SO, y cómo el código de la UI se separa de la lógica del procesador.)*

## **Funcionalidades e Implementación**

*(Detalla los pasos que sigue la aplicación: 1\. Selección de archivos. 2\. Elección de operación. 3\. Lanzamiento concurrente de procesos. 4\. Detección de SO para ejecutar el comando correcto (wc \-l en Linux/macOS vs. otro método en Windows, o tu propia solución nativa). Incluye capturas de pantalla de la interfaz en esta sección.)*

## **Breve Manual de Usuario**

1. **Ejecución:** Inicia la aplicación desde el fichero .jar o desde tu IDE.  
2. **Selección de Archivos:** Haz clic en "Seleccionar Archivos" y elige uno o más ficheros de texto.  
3. **Selección de Operación:** Elige la operación que deseas realizar (Líneas, Palabras, Caracteres).  
4. **Procesar:** Pulsa el botón "Iniciar Procesamiento".  
5. **Visualización:** Observa el progreso individual de cada archivo y el resultado final consolidado.

## **Pruebas**

*(Describe las pruebas que has realizado, incluyendo los casos de prueba obligatorios para el proyecto: prueba de procesamiento correcto, prueba de manejo de errores (fichero no encontrado), y la prueba de la detección del sistema operativo.)*

## **Conclusiones y Dificultades Encontradas**

*(Redacta una breve conclusión sobre el proyecto. Menciona las dificultades más importantes. Por ejemplo: "La principal dificultad fue adaptar los comandos de ProcessBuilder para que fueran funcionales tanto en entornos Linux/macOS como en Windows, y manejar la concurrencia para que la interfaz no se bloqueara.")*

## **Enlace al Repositorio del Proyecto**

\[ENLACE A TU REPOSITORIO PÚBLICO EN GITHUB\]

*Este README resume los contenidos de la memoria del proyecto para su visualización rápida en el repositorio.*