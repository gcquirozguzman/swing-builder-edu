package com.vanlutec.swingbuilder.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.vanlutec.swingbuilder.SbeIcons;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.FormCategory;
import com.vanlutec.swingbuilder.model.FormTemplate;
import com.vanlutec.swingbuilder.model.FormTheme;
import com.vanlutec.swingbuilder.model.WidgetModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Predicate;

/** La "pantallita" de {@code New | Swing Form (Designer)}: nombre de la clase y plantilla. */
public final class NewFormDialog extends DialogWrapper {

    /** La maqueta se reserva el hueco de la plantilla mas grande, para que no baile. */
    private static final int FRAME_W = maxOf(true);
    private static final int FRAME_H = maxOf(false);

    private static int maxOf(boolean ancho) {
        int max = 0;
        for (FormTemplate template : FormTemplate.values()) {
            max = Math.max(max, ancho ? template.getAncho() : template.getAlto());
        }
        return max;
    }

    private final JBTextField nameField;
    /** Dice si un nombre de clase ya esta cogido en la carpeta destino. */
    private final Predicate<String> nombreOcupado;
    private final JBRadioButton modoDesarrollo = new JBRadioButton(FormCategory.DESARROLLO.getTitulo(), true);
    private final JBRadioButton modoAprendizaje = new JBRadioButton(FormCategory.APRENDIZAJE.getTitulo());
    private final JBLabel modoDescription = new JBLabel();
    private final JBList<FormTemplate> templates = new JBList<>(FormCategory.DESARROLLO.getTemplates());
    private final JBLabel description = new JBLabel();
    private final ComboBox<FormTheme> themes = new ComboBox<>(FormTheme.values());
    private final JBLabel themeDescription = new JBLabel();
    private final JPanel previewContent = new JPanel(null);
    private final JPanel previewFrame = new PreviewFrame();

    public NewFormDialog(@Nullable Project project, String nombreSugerido, Predicate<String> nombreOcupado) {
        super(project, false);
        this.nombreOcupado = nombreOcupado;
        this.nameField = new JBTextField(nombreSugerido, 18);
        setTitle("Nuevo formulario Swing");
        templates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templates.setSelectedIndex(0);
        templates.setCellRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends FormTemplate> list, FormTemplate value,
                                                 int index, boolean selected, boolean focused) {
                if (value != null) {
                    setIcon(SbeIcons.SWING_FORM);
                    append(value.getTitulo());
                }
            }
        });
        templates.addListSelectionListener(e -> {
            updateDescription();
            updatePreview();
        });
        description.setForeground(UIUtil.getContextHelpForeground());
        modoDescription.setForeground(UIUtil.getContextHelpForeground());
        themeDescription.setForeground(UIUtil.getContextHelpForeground());

        ButtonGroup modos = new ButtonGroup();
        modos.add(modoDesarrollo);
        modos.add(modoAprendizaje);
        modoDesarrollo.addActionListener(e -> aplicarModo());
        modoAprendizaje.addActionListener(e -> aplicarModo());
        modoDescription.setText(wrap(FormCategory.DESARROLLO.getDescripcion()));
        themes.addActionListener(e -> {
            themeDescription.setText(wrap(getTheme().getDescripcion()));
            updatePreview();
        });
        themeDescription.setText(wrap(getTheme().getDescripcion()));
        // El titulo de la maqueta sigue al nombre que se va escribiendo.
        nameField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                previewFrame.repaint();
            }
        });
        updateDescription();
        updatePreview();
        init();
    }

    /** Cambia la lista de plantillas al modo elegido. */
    private void aplicarModo() {
        FormCategory categoria = getCategoria();
        modoDescription.setText(wrap(categoria.getDescripcion()));
        templates.setListData(categoria.getTemplates());
        templates.setSelectedIndex(0);
        updateDescription();
        updatePreview();
    }

    public FormCategory getCategoria() {
        return modoAprendizaje.isSelected() ? FormCategory.APRENDIZAJE : FormCategory.DESARROLLO;
    }

    private void updateDescription() {
        FormTemplate selected = templates.getSelectedValue();
        description.setText(wrap(selected == null ? " " : selected.getDescripcion()));
    }

    /**
     * Las descripciones se envuelven a lo ancho de la columna: si se dejan en una
     * sola linea, estiran el dialogo y la vista previa acaba recortada por la derecha.
     */
    private static String wrap(String texto) {
        return "<html><div width='" + JBUI.scale(250) + "'>" + texto + "</div></html>";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JBScrollPane scroll = new JBScrollPane(templates);
        scroll.setPreferredSize(new Dimension(JBUI.scale(260), JBUI.scale(96)));

        JPanel modos = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, JBUI.scale(12), 0));
        modos.add(modoDesarrollo);
        modos.add(modoAprendizaje);

        JComponent left = FormBuilder.createFormBuilder()
                .addLabeledComponent("Nombre de la clase:", nameField)
                .addLabeledComponent("Modo:", modos)
                .addComponentToRightColumn(modoDescription)
                .addLabeledComponent("Plantilla:", scroll, true)
                .addComponentToRightColumn(description)
                .addLabeledComponent("Tema:", themes)
                .addComponentToRightColumn(themeDescription)
                .getPanel();

        JPanel right = new JPanel(new BorderLayout(0, JBUI.scale(4)));
        JBLabel caption = new JBLabel("Vista previa");
        caption.setForeground(UIUtil.getContextHelpForeground());
        right.add(caption, BorderLayout.NORTH);
        right.add(previewFrame, BorderLayout.CENTER);

        JPanel root = new JPanel(new BorderLayout(JBUI.scale(16), 0));
        root.add(marca(), BorderLayout.NORTH);
        root.add(left, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);
        return root;
    }

    /** Cabecera con el logo: es la primera pantalla que ve el alumno en cada clase. */
    private static JComponent marca() {
        JBLabel logo = new JBLabel(SbeIcons.cibertec(22));
        logo.setBorder(JBUI.Borders.emptyBottom(10));
        JPanel fila = new JPanel(new BorderLayout());
        fila.add(logo, BorderLayout.WEST);
        return fila;
    }

    /**
     * La maqueta de la derecha: el JFrame con los componentes <b>reales</b> de la
     * plantilla, inertes (es una foto, no una aplicacion).
     */
    private final class PreviewFrame extends JPanel {

        private static final int TITLE_H = 26;

        private PreviewFrame() {
            super(null);
            setOpaque(true);
            add(previewContent);
            previewContent.setOpaque(true);
            Dimension size = new Dimension(FRAME_W + 2, FRAME_H + 1);
            setPreferredSize(size);
            // Minimo tambien: si el dialogo se estrecha, lo que no debe encogerse es esto
            // (si no, los componentes de la derecha del formulario desaparecen de la vista).
            setMinimumSize(size);
        }

        private int frameWidth() {
            FormTemplate selected = templates.getSelectedValue();
            return selected == null ? FRAME_W : selected.getAncho();
        }

        private int frameHeight() {
            FormTemplate selected = templates.getSelectedValue();
            return selected == null ? FRAME_H : selected.getAlto();
        }

        @Override
        public void doLayout() {
            previewContent.setBounds(1, TITLE_H, frameWidth(), frameHeight() - TITLE_H - 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                int w = frameWidth() + 1;
                int h = frameHeight();
                g2.setColor(UIUtil.getPanelBackground());
                g2.fillRect(0, 0, w + 1, h);
                // barra de titulo, como la que dibuja el canvas del disenador
                g2.setColor(new JBColor(new Color(0xE4E7EC), new Color(0x4E5157)));
                g2.fillRect(0, 0, w + 1, TITLE_H);
                g2.setColor(new JBColor(new Color(0x2B2D30), new Color(0xDFE1E5)));
                g2.setFont(JBUI.Fonts.label().deriveFont(Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(titleText(), 8, (TITLE_H + fm.getAscent()) / 2 - 1);
                g2.setColor(new JBColor(new Color(0x8A8F98), new Color(0x1E1F22)));
                g2.drawRect(0, 0, w, h - 1);
                g2.drawLine(0, TITLE_H, w, TITLE_H);
            } finally {
                g2.dispose();
            }
        }

        private String titleText() {
            String name = getFormName();
            return name.isEmpty() ? "MiFormulario" : name;
        }
    }

    /** Rehace la maqueta con la plantilla elegida. */
    private void updatePreview() {
        FormTemplate selected = templates.getSelectedValue();
        FormTheme theme = getTheme();
        previewContent.removeAll();
        previewContent.setBackground(theme.isSistema()
                ? UIUtil.getPanelBackground()
                : theme.getPanelBackground());
        if (selected != null) {
            String name = getFormName();
            FormModel model = selected.createModel(name.isEmpty() ? "MiFormulario" : name);
            for (WidgetModel widget : model.getWidgets()) {
                // create() los deja inertes: aqui no se pulsa ni se escribe nada.
                JComponent component = WidgetRenderer.create(widget, theme).outer();
                component.setBounds(widget.getBounds());
                previewContent.add(component);
            }
        }
        previewFrame.revalidate();
        previewFrame.repaint();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return nameField;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String name = getFormName();
        if (name.isEmpty()) {
            return new ValidationInfo("Escribe un nombre para la clase.", nameField);
        }
        if (!FormModel.sanitizeIdentifier(name).equals(name)) {
            return new ValidationInfo("No es un nombre de clase Java valido.", nameField);
        }
        if (nombreOcupado.test(name)) {
            return new ValidationInfo("Ya existe un " + name + " en esa carpeta.", nameField);
        }
        return null;
    }

    public String getFormName() {
        return nameField.getText().trim();
    }

    public FormTemplate getTemplate() {
        FormTemplate selected = templates.getSelectedValue();
        return selected == null ? FormTemplate.VACIO : selected;
    }

    public FormTheme getTheme() {
        FormTheme selected = (FormTheme) themes.getSelectedItem();
        return selected == null ? FormTheme.SISTEMA : selected;
    }
}
