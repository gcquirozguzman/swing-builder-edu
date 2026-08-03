package com.vanlutec.swingbuilder.codegen;

import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.junit.jupiter.api.Test;

import java.awt.Font;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lo importante que se comprueba aqui: al regenerar, el codigo que ha escrito el
 * alumno sobrevive intacto.
 */
class JavaFormGeneratorTest {

    private static FormModel form() {
        FormModel model = new FormModel();
        model.setClassName("MiFormulario");
        model.setTitle("Mi formulario");
        return model;
    }

    private static WidgetModel widget(WidgetType type, String name) {
        return new WidgetModel(type, name, 10, 20);
    }

    @Test
    void newFileCompilaConLasPiezasEsperadas() {
        FormModel model = form();
        model.addWidget(widget(WidgetType.BUTTON, "btnProcesar"));

        String source = JavaFormGenerator.newFile(model, "com.ejemplo");

        assertTrue(source.startsWith("package com.ejemplo;"), source);
        assertTrue(source.contains("public class MiFormulario extends JFrame {"));
        assertTrue(source.contains("import javax.swing.JButton;"));
        assertTrue(source.contains("private JButton btnProcesar;"));
        assertTrue(source.contains("btnProcesar = new JButton();"));
        assertTrue(source.contains("btnProcesar.setBounds(10, 20, 120, 26);"));
        assertTrue(source.contains("contentPane.add(btnProcesar);"));
        assertTrue(source.contains("private void " + JavaFormGenerator.EVENTS_METHOD + "() {"));
        assertTrue(source.contains("public static void main(String[] args) {"));
        // Las cuatro marcas de zona generada.
        assertTrue(source.contains(JavaFormGenerator.IMPORTS_BEGIN_KEY));
        assertTrue(source.contains(JavaFormGenerator.IMPORTS_END_KEY));
        assertTrue(source.contains(JavaFormGenerator.DESIGN_BEGIN_KEY));
        assertTrue(source.contains(JavaFormGenerator.DESIGN_END_KEY));
    }

    @Test
    void sinPaqueteNoSeEmiteLaSentenciaPackage() {
        assertTrue(JavaFormGenerator.newFile(form(), "").startsWith("// >>> SBE-IMPORTS"));
    }

    @Test
    void elTextAreaVaDentroDeUnJScrollPane() {
        FormModel model = form();
        model.addWidget(widget(WidgetType.TEXT_AREA, "textArea"));

        String source = JavaFormGenerator.newFile(model, "p");

        assertTrue(source.contains("import javax.swing.JScrollPane;"));
        assertTrue(source.contains("private JScrollPane scrollTextArea;"));
        assertTrue(source.contains("scrollTextArea = new JScrollPane(textArea);"));
        assertTrue(source.contains("scrollTextArea.setBounds(10, 20, 180, 90);"));
        assertTrue(source.contains("contentPane.add(scrollTextArea);"));
        // El JTextArea se anade a traves del scroll, nunca directamente.
        assertFalse(source.contains("contentPane.add(textArea);"));
    }

    @Test
    void elAjusteDeLineaSaleTalYComoEsteEnElDisenador() {
        FormModel model = form();
        WidgetModel area = widget(WidgetType.TEXT_AREA, "textArea");
        model.addWidget(area);

        // Por defecto apagado: es lo que hace que haya scroll horizontal.
        assertFalse(area.isLineWrap());
        String sinAjuste = JavaFormGenerator.newFile(model, "p");
        assertTrue(sinAjuste.contains("textArea.setLineWrap(false);"));
        assertTrue(sinAjuste.contains("textArea.setWrapStyleWord(false);"));

        area.setLineWrap(true);
        String conAjuste = JavaFormGenerator.newFile(model, "p");
        assertTrue(conAjuste.contains("textArea.setLineWrap(true);"));
        assertTrue(conAjuste.contains("textArea.setWrapStyleWord(true);"));
    }

    @Test
    void elTamanoSeLeDaAlPanelYNoALaVentana() {
        FormModel model = form();
        model.setFrameWidth(420);
        model.setFrameHeight(300);

        String source = JavaFormGenerator.newFile(model, "p");

        // Con setBounds sobre el JFrame, el marco se comeria el area util y todo
        // quedaria pegado a la derecha respecto a lo que se ve en el disenador.
        assertFalse(source.contains("setBounds(100, 100,"));
        assertTrue(source.contains("contentPane.setPreferredSize(new Dimension(420, 274));"));
        assertTrue(source.contains("import java.awt.Dimension;"));
        assertTrue(source.contains("pack();"));
    }

    @Test
    void soloSeEmiteSetFontCuandoLaFuenteNoEsLaPorDefecto() {
        FormModel porDefecto = form();
        porDefecto.addWidget(widget(WidgetType.LABEL, "lbl"));
        String sinFuente = JavaFormGenerator.newFile(porDefecto, "p");
        assertFalse(sinFuente.contains("setFont"));
        assertFalse(sinFuente.contains("import java.awt.Font;"));

        FormModel personalizada = form();
        WidgetModel etiqueta = widget(WidgetType.LABEL, "lbl");
        etiqueta.setFont("Arial", Font.BOLD | Font.ITALIC, 18);
        personalizada.addWidget(etiqueta);
        String conFuente = JavaFormGenerator.newFile(personalizada, "p");
        assertTrue(conFuente.contains("import java.awt.Font;"));
        assertTrue(conFuente.contains("lbl.setFont(new Font(\"Arial\", Font.BOLD | Font.ITALIC, 18));"));
    }

    @Test
    void seEscapanLasComillasDelTexto() {
        FormModel model = form();
        WidgetModel etiqueta = widget(WidgetType.LABEL, "lbl");
        etiqueta.setText("Di \"hola\"\\adios");
        model.addWidget(etiqueta);

        assertTrue(JavaFormGenerator.newFile(model, "p")
                .contains("lbl.setText(\"Di \\\"hola\\\"\\\\adios\");"));
    }

    @Test
    void regenerarRespetaElCodigoDelAlumno() {
        FormModel model = form();
        model.addWidget(widget(WidgetType.BUTTON, "btnProcesar"));
        String original = JavaFormGenerator.newFile(model, "com.ejemplo");

        // El alumno escribe su listener y un metodo propio.
        String conCodigo = original.replace(
                "    private void " + JavaFormGenerator.EVENTS_METHOD + "() {\n    }",
                "    private void " + JavaFormGenerator.EVENTS_METHOD + "() {\n"
                        + "        btnProcesar.addActionListener(e -> {\n"
                        + "            saludar();\n"
                        + "        });\n"
                        + "    }\n\n"
                        + "    private void saludar() {\n"
                        + "        System.out.println(\"hola\");\n"
                        + "    }");

        // Y ahora mueve el boton y anade una etiqueta en el disenador.
        model.getWidgets().get(0).setBounds(new java.awt.Rectangle(200, 150, 130, 30));
        model.addWidget(widget(WidgetType.LABEL, "lblNuevo"));

        String regenerado = JavaFormGenerator.refresh(conCodigo, model);
        assertNotNull(regenerado);

        // La zona generada esta al dia...
        assertTrue(regenerado.contains("btnProcesar.setBounds(200, 150, 130, 30);"));
        assertTrue(regenerado.contains("private JLabel lblNuevo;"));
        assertTrue(regenerado.contains("import javax.swing.JLabel;"));
        // ...y el codigo del alumno, intacto.
        assertTrue(regenerado.contains("btnProcesar.addActionListener(e -> {"));
        assertTrue(regenerado.contains("            saludar();"));
        assertTrue(regenerado.contains("    private void saludar() {"));
        assertTrue(regenerado.contains("System.out.println(\"hola\");"));
    }

    @Test
    void regenerarDosVecesSeguidasDaLoMismo() {
        FormModel model = form();
        model.addWidget(widget(WidgetType.BUTTON, "btn"));
        model.addWidget(widget(WidgetType.TEXT_AREA, "area"));
        String original = JavaFormGenerator.newFile(model, "com.ejemplo");

        String unaVez = JavaFormGenerator.refresh(original, model);
        assertEquals(original, unaVez, "regenerar sin cambios no deberia tocar el fichero");
        assertEquals(unaVez, JavaFormGenerator.refresh(unaVez, model));
    }

    @Test
    void unFicheroSinMarcasNoSeToca() {
        assertNull(JavaFormGenerator.refresh("public class Hecho a mano {}", form()));
        assertNull(JavaFormGenerator.regions("class X {}", form()));
    }

    @Test
    void lasZonasVienenDeAtrasHaciaDelante() {
        FormModel model = form();
        String source = JavaFormGenerator.newFile(model, "p");

        var regions = JavaFormGenerator.regions(source, model);
        assertNotNull(regions);
        assertEquals(2, regions.size());
        assertTrue(regions.get(0).start() > regions.get(1).end(),
                "aplicar de atras hacia delante mantiene validos los offsets");
    }

    @Test
    void elStubDelListenerEsElQuePideElCurso() {
        assertEquals("btnProcesar.addActionListener(e -> {\n"
                + "    // TODO: accion aqui\n"
                + "});", JavaFormGenerator.actionListenerStatement("btnProcesar"));
    }
}
