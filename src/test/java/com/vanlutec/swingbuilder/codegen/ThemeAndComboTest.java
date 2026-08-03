package com.vanlutec.swingbuilder.codegen;

import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.FormModelIO;
import com.vanlutec.swingbuilder.model.FormTheme;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThemeAndComboTest {

    private static FormModel form() {
        FormModel model = new FormModel();
        model.setClassName("MiFormulario");
        return model;
    }

    // ------------------------------------------------------------------ combo

    @Test
    void elComboBoxSaleTipadoYConSusOpciones() {
        FormModel model = form();
        WidgetModel combo = new WidgetModel(WidgetType.COMBO_BOX, "cmbTipo", 10, 10);
        combo.setItems("Suma, Resta , Multiplicacion");
        model.addWidget(combo);

        String source = JavaFormGenerator.newFile(model, "p");

        assertTrue(source.contains("import javax.swing.JComboBox;"));
        assertTrue(source.contains("private JComboBox<String> cmbTipo;"));
        assertTrue(source.contains("cmbTipo = new JComboBox<>();"));
        assertTrue(source.contains("cmbTipo.addItem(\"Suma\");"));
        assertTrue(source.contains("cmbTipo.addItem(\"Resta\");"), "hay que quitar los espacios sobrantes");
        assertTrue(source.contains("cmbTipo.addItem(\"Multiplicacion\");"));
        // Un JComboBox no tiene setText.
        assertFalse(source.contains("cmbTipo.setText("));
    }

    @Test
    void lasOpcionesSeParten() {
        WidgetModel combo = new WidgetModel(WidgetType.COMBO_BOX, "c", 0, 0);
        combo.setItems(" Uno ,, Dos ,  ");
        assertEquals(List.of("Uno", "Dos"), combo.getItemList());
    }

    @Test
    void lasOpcionesSobrevivenAlXml() {
        FormModel model = form();
        WidgetModel combo = new WidgetModel(WidgetType.COMBO_BOX, "cmbTipo", 10, 10);
        combo.setItems("Rojo, Verde, Azul");
        model.addWidget(combo);

        FormModel leido = FormModelIO.fromXml(FormModelIO.toXml(model), "MiFormulario");

        assertEquals("Rojo, Verde, Azul", leido.getWidgets().get(0).getItems());
    }

    // ------------------------------------------------------------------ temas

    @Test
    void elTemaSistemaNoTocaNingunColor() {
        FormModel model = form();
        model.addWidget(new WidgetModel(WidgetType.LABEL, "lbl", 0, 0));
        model.setTheme(FormTheme.SISTEMA);

        String source = JavaFormGenerator.newFile(model, "p");

        assertFalse(source.contains("setForeground"));
        assertFalse(source.contains("setBackground"));
        assertFalse(source.contains("import java.awt.Color;"));
    }

    @Test
    void unTemaDeColorPintaPanelEtiquetasYCampos() {
        FormModel model = form();
        model.addWidget(new WidgetModel(WidgetType.LABEL, "lbl", 0, 0));
        model.addWidget(new WidgetModel(WidgetType.TEXT_FIELD, "txt", 0, 30));
        model.setTheme(FormTheme.OSCURO);

        String source = JavaFormGenerator.newFile(model, "p");

        assertTrue(source.contains("import java.awt.Color;"));
        assertTrue(source.contains("contentPane.setBackground(new Color(0x2B2D30));"));
        assertTrue(source.contains("lbl.setForeground(new Color(0xE6E6E6));"));
        assertTrue(source.contains("txt.setBackground(new Color(0x3C3F41));"));
        assertTrue(source.contains("txt.setForeground(new Color(0xE6E6E6));"));
        // Sin esto no se ve donde se escribe sobre fondo oscuro.
        assertTrue(source.contains("txt.setCaretColor(new Color(0xE6E6E6));"));
    }

    @Test
    void elBotonSePintaPlanoParaQueElColorSeRespete() {
        FormModel model = form();
        model.addWidget(new WidgetModel(WidgetType.BUTTON, "btn", 0, 0));
        model.setTheme(FormTheme.TERMINAL);

        String source = JavaFormGenerator.newFile(model, "p");

        // Sin contentAreaFilled(false) + opaque(true), Darcula y el LAF de Windows pintan
        // su propio fondo encima: el texto del tema acabaria sobre un fondo ajeno (que es
        // como el tema Claro dejaba el boton negro sobre negro).
        assertTrue(source.contains("btn.setContentAreaFilled(false);"));
        assertTrue(source.contains("btn.setOpaque(true);"));
        assertTrue(source.contains("btn.setBackground(new Color(0x123018));"));
        assertTrue(source.contains("btn.setForeground(new Color(0x33FF66));"));
    }

    @Test
    void elComboBoxTambienSePintaConLaUiBasica() {
        FormModel model = form();
        model.addWidget(new WidgetModel(WidgetType.COMBO_BOX, "cmb", 0, 0));
        model.setTheme(FormTheme.CLARO);

        String source = JavaFormGenerator.newFile(model, "p");

        assertTrue(source.contains("import javax.swing.plaf.basic.BasicComboBoxUI;"));
        assertTrue(source.contains("cmb.setUI(new BasicComboBoxUI());"));
        assertTrue(source.contains("cmb.setBackground(new Color(0xFFFFFF));"));
        assertTrue(source.contains("cmb.setForeground(new Color(0x1F2328));"));
    }

    @Test
    void conElTemaDelSistemaElComboNoLlevaUiPropia() {
        FormModel model = form();
        model.addWidget(new WidgetModel(WidgetType.COMBO_BOX, "cmb", 0, 0));

        String source = JavaFormGenerator.newFile(model, "p");

        assertFalse(source.contains("BasicComboBoxUI"));
    }

    @Test
    void enTodosLosTemasElBotonTieneFondoYTextoDistintos() {
        for (FormTheme theme : FormTheme.values()) {
            if (theme.isSistema()) {
                continue;
            }
            assertFalse(theme.getButtonBackground().equals(theme.getButtonForeground()),
                    theme + ": el boton quedaria ilegible, mismo color de fondo y de texto");
        }
    }

    @Test
    void elTemaSobreviveAlXml() {
        for (FormTheme theme : FormTheme.values()) {
            FormModel model = form();
            model.setTheme(theme);
            assertEquals(theme, FormModelIO.fromXml(FormModelIO.toXml(model), "X").getTheme());
        }
    }

    @Test
    void unTemaDesconocidoNoRompeNada() {
        assertEquals(FormTheme.SISTEMA,
                FormModelIO.fromXml("<form class=\"X\" theme=\"NEON_2050\"/>", "X").getTheme());
    }
}
