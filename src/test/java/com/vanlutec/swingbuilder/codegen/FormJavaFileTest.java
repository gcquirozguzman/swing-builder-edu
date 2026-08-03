package com.vanlutec.swingbuilder.codegen;

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;

import java.awt.Rectangle;

/**
 * El camino del doble clic, con un proyecto de verdad: crear la clase, insertar el
 * {@code ActionListener} via PSI y volver a generar sin llevarse por delante nada.
 */
public class FormJavaFileTest extends LightJavaCodeInsightFixtureTestCase {

    private FormModel model;
    private WidgetModel boton;
    private VirtualFile sbe;
    private FormJavaFile javaFile;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        model = new FormModel();
        model.setClassName("MiFormulario");
        model.setTitle("Mi formulario");
        boton = new WidgetModel(WidgetType.BUTTON, "btnProcesar", 10, 10);
        boton.setText("Procesar");
        model.addWidget(boton);

        sbe = myFixture.getTempDirFixture().createFile("MiFormulario.sbe", "<form/>");
        javaFile = new FormJavaFile(getProject(), sbe);
    }

    private static String textoDe(VirtualFile file) {
        return FileDocumentManager.getInstance().getDocument(file).getText();
    }

    private static int veces(String texto, String trozo) {
        int total = 0;
        for (int at = texto.indexOf(trozo); at >= 0; at = texto.indexOf(trozo, at + 1)) {
            total++;
        }
        return total;
    }

    public void testCreaLaClaseJavaAlLadoDelDiseno() {
        VirtualFile java = javaFile.ensure(model);

        assertNotNull(java);
        assertEquals("MiFormulario.java", java.getName());
        assertEquals(sbe.getParent(), java.getParent());

        String source = textoDe(java);
        assertTrue(source.contains("public class MiFormulario extends JFrame {"));
        assertTrue(source.contains("private JButton btnProcesar;"));
        assertTrue(source.contains("btnProcesar.setText(\"Procesar\");"));
    }

    public void testElDobleClicCreaElActionListener() {
        javaFile.openActionListener(model, boton);

        VirtualFile java = javaFile.find(model);
        assertNotNull("el doble clic deberia crear la clase si no existia", java);

        String source = textoDe(java);
        assertTrue(source, source.contains("btnProcesar.addActionListener(e -> {"));
        assertTrue(source, source.contains(JavaFormGenerator.TODO_COMMENT));
        // Y esta dentro de initEventos(), que es zona del alumno.
        int eventos = source.indexOf("private void " + JavaFormGenerator.EVENTS_METHOD + "()");
        int listener = source.indexOf("btnProcesar.addActionListener");
        assertTrue("el listener deberia caer dentro de initEventos()", eventos < listener);
        assertTrue("el listener no puede acabar en la zona generada",
                listener > source.indexOf(JavaFormGenerator.DESIGN_END_KEY));
    }

    public void testElSegundoDobleClicNoDuplicaElListener() {
        javaFile.openActionListener(model, boton);
        javaFile.openActionListener(model, boton);
        javaFile.openActionListener(model, boton);

        String source = textoDe(javaFile.find(model));
        assertEquals(source, 1, veces(source, "btnProcesar.addActionListener"));
    }

    public void testRegenerarNoSeLlevaPorDelanteElListener() {
        javaFile.openActionListener(model, boton);
        VirtualFile java = javaFile.find(model);

        // El alumno escribe dentro del listener...
        String conCodigo = textoDe(java).replace(JavaFormGenerator.TODO_COMMENT,
                "System.out.println(\"pulsado\");");
        com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction(() ->
                FileDocumentManager.getInstance().getDocument(java).setText(conCodigo));

        // ...y luego mueve el boton y anade una etiqueta en el disenador.
        boton.setBounds(new Rectangle(120, 200, 140, 32));
        model.addWidget(new WidgetModel(WidgetType.LABEL, "lblAviso", 5, 5));
        javaFile.refresh(model, java);

        String source = textoDe(java);
        // La zona generada, al dia.
        assertTrue(source, source.contains("btnProcesar.setBounds(120, 200, 140, 32);"));
        assertTrue(source, source.contains("private JLabel lblAviso;"));
        assertTrue(source, source.contains("import javax.swing.JLabel;"));
        // El codigo del alumno, intacto.
        assertTrue(source, source.contains("System.out.println(\"pulsado\");"));
        assertEquals(source, 1, veces(source, "btnProcesar.addActionListener"));
    }

    public void testUnFicheroSinMarcasNoSeToca() throws Exception {
        String aMano = "public class MiFormulario {\n    // todo mio\n}\n";
        VirtualFile java = myFixture.getTempDirFixture().createFile("MiFormulario.java", aMano);

        javaFile.refresh(model, java);

        assertEquals(aMano, textoDe(java));
    }

    public void testSoloLosBotonesLlevanActionListener() {
        WidgetModel etiqueta = new WidgetModel(WidgetType.LABEL, "lblTitulo", 0, 0);
        model.addWidget(etiqueta);

        javaFile.openActionListener(model, etiqueta);

        assertNull("una etiqueta no deberia generar nada", javaFile.find(model));
    }

    public void testSiElAlumnoBorraInitEventosSeVuelveACrear() {
        VirtualFile java = javaFile.ensure(model);
        String sinMetodo = textoDe(java).replace(
                "    private void " + JavaFormGenerator.EVENTS_METHOD + "() {\n    }", "");
        com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction(() ->
                FileDocumentManager.getInstance().getDocument(java).setText(sinMetodo));

        javaFile.openActionListener(model, boton);

        String source = textoDe(java);
        assertTrue(source, source.contains("private void " + JavaFormGenerator.EVENTS_METHOD + "()"));
        assertTrue(source, source.contains("btnProcesar.addActionListener"));
    }
}
