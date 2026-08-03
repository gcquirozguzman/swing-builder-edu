package com.vanlutec.swingbuilder;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.IconUtil;

import javax.swing.Icon;

/** Iconos de la paleta, al estilo de los de WindowBuilder. */
public final class SbeIcons {

    public static final Icon JLABEL = load("/icons/jlabel.svg");
    public static final Icon JTEXT_FIELD = load("/icons/jtextfield.svg");
    public static final Icon JBUTTON = load("/icons/jbutton.svg");
    public static final Icon JCOMBO_BOX = load("/icons/jcombobox.svg");
    public static final Icon JTEXT_AREA = load("/icons/jtextarea.svg");
    public static final Icon SWING_FORM = load("/icons/swingForm.svg");

    /** El logo de Cibertec, tal cual esta en recursos (50x50). */
    private static final Icon CIBERTEC = load("/icons/cibertec/cibertec.png");

    private SbeIcons() {
    }

    /**
     * El logo a la altura que se pida, en pixeles logicos.
     * <p>
     * Se calcula al llamar, no al cargar la clase: pedir el tamano fuerza a leer la
     * imagen, y {@link com.vanlutec.swingbuilder.model.WidgetType} inicializa esta
     * clase tambien fuera del IDE (por ejemplo en las pruebas).
     */
    public static Icon cibertec(int altura) {
        int actual = CIBERTEC.getIconHeight();
        if (actual <= 0) {
            return CIBERTEC;
        }
        return IconUtil.scale(CIBERTEC, null, JBUIScale.scale((float) altura) / actual);
    }

    private static Icon load(String path) {
        return IconLoader.getIcon(path, SbeIcons.class);
    }
}
