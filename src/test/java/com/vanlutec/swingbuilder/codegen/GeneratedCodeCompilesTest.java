package com.vanlutec.swingbuilder.codegen;

import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Font;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * La red de seguridad de verdad: se pasa por {@code javac} lo que el plugin va a
 * escribir en el proyecto del alumno. Si un dia el generador emite algo que no
 * compila, se entera aqui y no el alumno en clase.
 */
class GeneratedCodeCompilesTest {

    /** Un formulario con los cuatro componentes de la paleta. */
    private static FormModel completo() {
        FormModel model = new FormModel();
        model.setClassName("Calculadora");
        model.setTitle("Calculadora \"simple\"");
        model.setFrameWidth(420);
        model.setFrameHeight(300);

        WidgetModel titulo = new WidgetModel(WidgetType.LABEL, "lblTitulo", 20, 20);
        titulo.setText("Introduce un numero:");
        titulo.setFont("Segoe UI", Font.BOLD | Font.ITALIC, 14);
        model.addWidget(titulo);

        model.addWidget(new WidgetModel(WidgetType.TEXT_FIELD, "txtNumero", 20, 50));

        WidgetModel boton = new WidgetModel(WidgetType.BUTTON, "btnProcesar", 20, 90);
        boton.setText("Procesar");
        model.addWidget(boton);

        model.addWidget(new WidgetModel(WidgetType.TEXT_AREA, "txtResultado", 20, 130));
        return model;
    }

    @Test
    void elFormularioRecienCreadoCompila(@TempDir Path dir) throws Exception {
        compila(JavaFormGenerator.newFile(completo(), "com.ejemplo.formularios"), dir);
    }

    @Test
    void tambienCompilaEnElPaquetePorDefecto(@TempDir Path dir) throws Exception {
        compila(JavaFormGenerator.newFile(completo(), ""), dir);
    }

    @Test
    void elFormularioVacioCompila(@TempDir Path dir) throws Exception {
        FormModel vacio = new FormModel();
        vacio.setClassName("Calculadora");
        compila(JavaFormGenerator.newFile(vacio, "com.ejemplo"), dir);
    }

    @Test
    void conElActionListenerDelDobleClicTambienCompila(@TempDir Path dir) throws Exception {
        String source = JavaFormGenerator.newFile(completo(), "com.ejemplo");
        String vacio = "    private void " + JavaFormGenerator.EVENTS_METHOD + "() {\n    }";
        assertTrue(source.contains(vacio), "cambio la forma de initEventos()");

        String statement = JavaFormGenerator.actionListenerStatement("btnProcesar");
        String conListener = source.replace(vacio,
                "    private void " + JavaFormGenerator.EVENTS_METHOD + "() {\n"
                        + "        " + statement.replace("\n", "\n        ") + "\n"
                        + "    }");

        compila(conListener, dir);
    }

    @Test
    void todasLasPlantillasGeneranCodigoQueCompila(@TempDir Path dir) throws Exception {
        for (com.vanlutec.swingbuilder.model.FormTemplate template
                : com.vanlutec.swingbuilder.model.FormTemplate.values()) {
            if (template.isSoloCodigo()) {
                continue;  // se comprueba en la prueba de las lecciones
            }
            compila(JavaFormGenerator.newFile(template.createModel("Calculadora"), "com.ejemplo"),
                    Files.createDirectories(dir.resolve(template.name())));
        }
    }

    /**
     * Las lecciones acaban delante de 60 alumnos: no basta con que compilen, tienen que
     * ejecutarse enteras sin reventar.
     */
    @Test
    void lasLeccionesDeAprendizajeCompilanYSeEjecutan(@TempDir Path dir) throws Exception {
        for (com.vanlutec.swingbuilder.model.FormTemplate template
                : com.vanlutec.swingbuilder.model.FormCategory.APRENDIZAJE.getTemplates()) {
            Path conPaquete = Files.createDirectories(dir.resolve(template.name()));
            compila(template.javaSource("Calculadora", "com.ejemplo"), conPaquete);

            // Sin paquete, para poder ejecutarla por su nombre a secas.
            Path suelta = Files.createDirectories(dir.resolve(template.name() + "_sin_paquete"));
            compila(template.javaSource("Calculadora", ""), suelta);
            ejecuta(suelta.resolve("out"), template.toString());
        }
    }

    /** Lanza la clase compilada y exige que termine bien y escriba algo. */
    private static void ejecuta(Path clases, String leccion) throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().startsWith("win") ? "java.exe" : "java");
        Process process = new ProcessBuilder(java.toString(), "-cp", clases.toString(), "Calculadora")
                .redirectErrorStream(true)
                .start();
        String salida = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int status = process.waitFor();

        assertEquals(0, status, () -> "la leccion \"" + leccion + "\" revienta al ejecutarse:\n" + salida);
        assertTrue(salida.length() > 100, () -> "la leccion \"" + leccion + "\" apenas imprime nada:\n" + salida);
    }

    @Test
    void todosLosTemasGeneranCodigoQueCompila(@TempDir Path dir) throws Exception {
        for (com.vanlutec.swingbuilder.model.FormTheme theme
                : com.vanlutec.swingbuilder.model.FormTheme.values()) {
            FormModel model = completo();
            model.addWidget(new WidgetModel(WidgetType.COMBO_BOX, "cmbTipo", 20, 250));
            model.setTheme(theme);
            compila(JavaFormGenerator.newFile(model, "com.ejemplo"),
                    Files.createDirectories(dir.resolve(theme.name())));
        }
    }

    @Test
    void trasRegenerarSigueCompilando(@TempDir Path dir) throws Exception {
        FormModel model = completo();
        String source = JavaFormGenerator.newFile(model, "com.ejemplo");

        // El alumno mueve cosas y anade un componente: se reescriben las zonas generadas.
        model.getWidgets().get(0).setBounds(new java.awt.Rectangle(5, 5, 200, 20));
        model.addWidget(new WidgetModel(WidgetType.BUTTON, "btnLimpiar", 200, 90));
        String regenerado = JavaFormGenerator.refresh(source, model);

        compila(regenerado, dir);
    }

    // ------------------------------------------------------------------ javac

    /**
     * Se lanza el {@code javac} del JDK como proceso aparte: dentro del runner de
     * tests de la plataforma, la API {@code ToolProvider} no ve al compilador.
     */
    private static void compila(String source, Path dir) throws Exception {
        Path javac = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().startsWith("win") ? "javac.exe" : "javac");
        assumeTrue(Files.isExecutable(javac), "hace falta un JDK (no un JRE) para esta prueba: " + javac);

        Path file = dir.resolve("Calculadora.java");
        Files.writeString(file, source, StandardCharsets.UTF_8);
        Path out = Files.createDirectories(dir.resolve("out"));

        Process process = new ProcessBuilder(javac.toString(), "-nowarn",
                "-d", out.toString(), file.toString())
                .redirectErrorStream(true)
                .start();
        String salida = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int status = process.waitFor();

        assertEquals(0, status, () -> "javac rechazo el codigo generado:\n" + salida + "\n---\n" + source);
    }
}
