import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // useInstaller = false baja el IDE como artefacto de Maven en vez de como
        // instalador. El instalador se lee con el "product-info layout", que en Linux
        // revienta al resolver los plugins agrupados (ClosedFileSystemException leyendo
        // java-impl.jar) y deja com.intellij.java sin encontrar.
        create {
            type = providers.gradleProperty("platformType").map { IntelliJPlatformType.fromCode(it) }
            version = providers.gradleProperty("platformVersion")
            useInstaller = false
        }
        // Sin instalador no viene el JBR incluido: hay que pedirlo aparte para runIde.
        jetbrainsRuntime()

        // El generador de codigo usa PSI de Java (bundled en IDEA Community).
        bundledPlugin("com.intellij.java")
        // Para las pruebas que necesitan un proyecto e indices de verdad
        // (LightJavaCodeInsightFixtureTestCase vive en el framework de Java).
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Las pruebas de plataforma (BasePlatformTestCase) son JUnit 3/4, asi que hacen
    // falta las dos generaciones a la vez: vintage ejecuta las viejas sobre JUnit 5.
    testImplementation("junit:junit:4.13.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

java {
    // Compilamos con el JDK que ejecuta Gradle (cualquiera >= 21) en lugar de exigir
    // uno concreto: asi basta con tener instalado el JBR del IDE o un JDK moderno.
    // El bytecode sigue siendo 21 gracias a `options.release` (mas abajo).
    toolchain {
        languageVersion = JavaLanguageVersion.of(JavaVersion.current().majorVersion)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            // Sin limite superior: el plugin se puede instalar en IDEs mas nuevos.
            untilBuild = provider { null }
        }
    }
    buildSearchableOptions = false
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

/**
 * Genera el `updatePlugins.xml` del repositorio propio de plugins.
 *
 * Es el fichero que consulta IntelliJ para saber si hay version nueva: los alumnos
 * anaden su URL una sola vez y a partir de ahi las actualizaciones les llegan solas.
 */
val updatePluginsXml by tasks.registering {
    val version = providers.gradleProperty("pluginVersion").get()
    val repositoryUrl = providers.gradleProperty("pluginRepositoryUrl").get().trimEnd('/')
    val sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
    val destino = layout.buildDirectory.file("updatePlugins/updatePlugins.xml")

    outputs.file(destino)
    doLast {
        val zip = "${rootProject.name}-$version.zip"
        val descarga = "$repositoryUrl/releases/download/v$version/$zip"
        val fichero = destino.get().asFile
        fichero.parentFile.mkdirs()
        fichero.writeText(
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <plugins>
              <plugin id="com.vanlutec.swing-builder-edu" url="$descarga" version="$version">
                <name>Swing Builder Edu</name>
                <description><![CDATA[Disenador visual de GUIs Swing con el flujo de WindowBuilder, para clases de Java.]]></description>
                <idea-version since-build="$sinceBuild"/>
              </plugin>
            </plugins>
            """.trimIndent() + "\n"
        )
        logger.lifecycle("updatePlugins.xml generado para la version $version -> $descarga")
    }
}
