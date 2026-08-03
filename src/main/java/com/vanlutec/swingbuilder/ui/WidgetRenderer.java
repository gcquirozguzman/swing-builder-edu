package com.vanlutec.swingbuilder.ui;

import com.vanlutec.swingbuilder.model.FormTheme;
import com.vanlutec.swingbuilder.model.WidgetModel;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.JTextComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

/**
 * Crea los componentes Swing <b>reales</b> que se ven en el canvas, para que el
 * diseno sea de verdad WYSIWYG.
 *
 * @param outer componente que se anade al contenedor (para un JTextArea, su JScrollPane)
 * @param inner componente al que se aplican las propiedades (text, font, colores)
 */
public record WidgetRenderer(JComponent outer, JComponent inner) {

    /** Para el canvas: componentes que se ven pero no reaccionan (los eventos son del overlay). */
    public static WidgetRenderer create(WidgetModel widget, FormTheme theme) {
        WidgetRenderer rendered = build(widget, theme);
        makeInert(rendered.outer);
        return rendered;
    }

    /**
     * Para la vista previa: componentes vivos, que se pueden pulsar y escribir.
     * <p>
     * Ojo: no vale con crear uno del canvas y volver a ponerle {@code focusable};
     * {@link #makeInert} tambien apaga {@code requestFocusEnabled} y baja por los
     * hijos (el {@code JTextArea} de dentro de su {@code JScrollPane}).
     */
    public static WidgetRenderer createInteractive(WidgetModel widget, FormTheme theme) {
        return build(widget, theme);
    }

    private static WidgetRenderer build(WidgetModel widget, FormTheme theme) {
        WidgetRenderer rendered = switch (widget.getType()) {
            case LABEL -> single(new JLabel());
            case TEXT_FIELD -> single(new JTextField());
            case BUTTON -> single(new JButton());
            case COMBO_BOX -> single(new JComboBox<String>());
            case TEXT_AREA -> {
                JTextArea area = new JTextArea();
                JScrollPane scroll = new JScrollPane(area);
                scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                yield new WidgetRenderer(scroll, area);
            }
        };
        rendered.apply(widget, theme);
        return rendered;
    }

    private static WidgetRenderer single(JComponent component) {
        return new WidgetRenderer(component, component);
    }

    /** Refleja en el componente real los valores actuales del modelo. */
    @SuppressWarnings("unchecked")
    public void apply(WidgetModel widget, FormTheme theme) {
        switch (widget.getType()) {
            case LABEL -> ((JLabel) inner).setText(widget.getText());
            case TEXT_FIELD -> ((JTextField) inner).setText(widget.getText());
            case BUTTON -> ((JButton) inner).setText(widget.getText());
            case COMBO_BOX -> {
                JComboBox<String> combo = (JComboBox<String>) inner;
                combo.removeAllItems();
                for (String item : widget.getItemList()) {
                    combo.addItem(item);
                }
                if (theme == null || theme.isSistema()) {
                    combo.updateUI();  // devuelve el aspecto propio del LAF
                }
            }
            case TEXT_AREA -> {
                JTextArea area = (JTextArea) inner;
                area.setText(widget.getText());
                area.setLineWrap(widget.isLineWrap());
                area.setWrapStyleWord(widget.isLineWrap());
                // Sin ajuste de linea, las lineas largas piden scroll horizontal.
                ((JScrollPane) outer).setHorizontalScrollBarPolicy(widget.isLineWrap()
                        ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                        : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            }
        }
        inner.setFont(widget.getFont());
        applyTheme(widget, theme);
        outer.setBounds(widget.getBounds());
    }

    /** Los mismos colores que emitira el generador, para que el diseno no mienta. */
    private void applyTheme(WidgetModel widget, FormTheme theme) {
        if (theme == null || theme.isSistema()) {
            return;
        }
        switch (widget.getType()) {
            case LABEL -> inner.setForeground(theme.getTextForeground());
            case BUTTON -> {
                JButton boton = (JButton) inner;
                // Plano: si se deja al LAF pintar el fondo, ignora setBackground y el
                // texto del tema acaba sobre un fondo que no le corresponde.
                boton.setContentAreaFilled(false);
                boton.setOpaque(true);
                boton.setBackground(theme.getButtonBackground());
                boton.setForeground(theme.getButtonForeground());
            }
            case TEXT_FIELD, TEXT_AREA -> {
                Color bg = theme.getFieldBackground();
                Color fg = theme.getFieldForeground();
                inner.setBackground(bg);
                inner.setForeground(fg);
                if (inner instanceof JTextComponent text) {
                    text.setCaretColor(fg);
                }
                if (outer instanceof JScrollPane scroll) {
                    scroll.getViewport().setBackground(bg);
                }
            }
            case COMBO_BOX -> {
                // Mismo motivo que en el boton: con la UI del LAF, setBackground se ignora.
                // La UI basica ademas tine la lista desplegable con estos mismos colores.
                ((JComboBox<?>) inner).setUI(new BasicComboBoxUI());
                inner.setBackground(theme.getFieldBackground());
                inner.setForeground(theme.getFieldForeground());
            }
        }
    }

    /**
     * El canvas dibuja componentes, no una aplicacion en marcha: nadie debe recibir
     * el foco ni reaccionar al raton (de eso se encarga la capa de seleccion).
     */
    private static void makeInert(Component component) {
        component.setFocusable(false);
        if (component instanceof JComponent jc) {
            jc.setRequestFocusEnabled(false);
            jc.setToolTipText(null);
        }
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                makeInert(child);
            }
        }
    }
}
