import org.jetbrains.compose.desktop.application.dsl.TargetFormat


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("org.jetbrains.dokka") version "1.9.10"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

        }
    }
}


compose.desktop {
    application {
        mainClass = "dam2.gsr.exercicio05.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dam2.gsr.exercicio05"
            packageVersion = "1.0.0"
        }
    }
}

tasks.dokkaHtml {
    // Título que aparecerá en la parte superior de la documentación HTML
    moduleName.set("Proyecto Procesador de Ficheros Concurrente (DAM2)")

    // Directorio de salida: se generará en la carpeta build/dokka
    outputDirectory.set(file("${rootProject.buildDir}/dokka/html"))

    // Configuramos Dokka por conjunto de fuentes (Source Set)
    dokkaSourceSets {

        // 1. Configuramos el sourceSet común
        named("commonMain") {
            sourceRoots.setFrom(
                project.projectDir.resolve("src/commonMain/kotlin")
            )
            // Forma segura de vaciar el set
            dependentSourceSets.set(emptySet())
        }

        // 2. Configuramos el sourceSet específico de JVM (donde está AWT)
        named("jvmMain") {
            sourceRoots.setFrom(
                project.projectDir.resolve("src/jvmMain/kotlin")
            )

            dependsOn(getByName("commonMain"))

            // ⭐ SOLUCIÓN AL ERROR DE AWT/JAVA
            // Añadimos las dependencias de classpath de JVM (necesario para el diálogo de archivos AWT)
            classpath.from(project.configurations.named("jvmCompileClasspath"))
        }

        // 3. Suprimimos otros sourceSets que no tienen KDoc relevante
        all {
            if (name.endsWith("Test") || name.startsWith("js") || name.startsWith("android")) {
                suppress.set(true)
            }
        }
    }
}