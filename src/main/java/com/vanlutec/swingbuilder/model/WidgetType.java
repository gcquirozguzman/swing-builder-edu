package com.vanlutec.swingbuilder.model;

import com.vanlutec.swingbuilder.SbeIcons;

import javax.swing.Icon;

/**
 * Los cuatro componentes que ofrece la paleta.
 * <p>
 * El curso solo necesita estos; anadir mas seria tan simple como agregar
 * una constante aqui (la paleta, el canvas y el generador de codigo los
 * recorren automaticamente).
 */
public enum WidgetType {

    LABEL("JLabel", "javax.swing.JLabel", "lblNewLabel", "Nueva etiqueta", 90, 16, SbeIcons.JLABEL),
    TEXT_FIELD("JTextField", "javax.swing.JTextField", "textField", "", 130, 24, SbeIcons.JTEXT_FIELD),
    BUTTON("JButton", "javax.swing.JButton", "btnNewButton", "Nuevo boton", 120, 26, SbeIcons.JBUTTON),
    COMBO_BOX("JComboBox", "javax.swing.JComboBox", "comboBox", "", 130, 26, SbeIcons.JCOMBO_BOX),
    TEXT_AREA("JTextArea", "javax.swing.JTextArea", "textArea", "", 180, 90, SbeIcons.JTEXT_AREA);

    private final String simpleName;
    private final String qualifiedName;
    private final String defaultName;
    private final String defaultText;
    private final int defaultWidth;
    private final int defaultHeight;
    private final Icon icon;

    WidgetType(String simpleName, String qualifiedName, String defaultName, String defaultText,
               int defaultWidth, int defaultHeight, Icon icon) {
        this.simpleName = simpleName;
        this.qualifiedName = qualifiedName;
        this.defaultName = defaultName;
        this.defaultText = defaultText;
        this.defaultWidth = defaultWidth;
        this.defaultHeight = defaultHeight;
        this.icon = icon;
    }

    /** Nombre que se muestra en la paleta: {@code JButton}, {@code JLabel}, ... */
    public String getSimpleName() {
        return simpleName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public int getDefaultHeight() {
        return defaultHeight;
    }

    public Icon getIcon() {
        return icon;
    }

    /** Los JTextArea se generan dentro de un JScrollPane, igual que en WindowBuilder. */
    public boolean needsScrollPane() {
        return this == TEXT_AREA;
    }

    public static WidgetType fromId(String id, WidgetType fallback) {
        for (WidgetType type : values()) {
            if (type.name().equalsIgnoreCase(id) || type.simpleName.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return fallback;
    }
}
