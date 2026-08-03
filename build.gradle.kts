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
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )
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
