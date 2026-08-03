package com.vanlutec.swingbuilder.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.LinkedHashSet;
import java.util.Set;

/** Editor de la propiedad {@code font}, parecido al de WindowBuilder. */
public final class FontChooserDialog extends DialogWrapper {

    private final JComboBox<String> family;
    private final JSpinner size;
    private final JBCheckBox bold = new JBCheckBox("Bold");
    private final JBCheckBox italic = new JBCheckBox("Italic");
    private final JBLabel preview = new JBLabel("AaBbCc 123");

    public FontChooserDialog(@Nullable JComponent parent, Font current) {
        super(parent, false);
        setTitle("Fuente");
        family = new JComboBox<>(familyModel(current.getFamily()));
        family.setSelectedItem(current.getFamily());
        size = new JSpinner(new SpinnerNumberModel(current.getSize(), 6, 96, 1));
        bold.setSelected((current.getStyle() & Font.BOLD) != 0);
        italic.setSelected((current.getStyle() & Font.ITALIC) != 0);
        family.addActionListener(e -> updatePreview());
        size.addChangeListener(e -> updatePreview());
        bold.addActionListener(e -> updatePreview());
        italic.addActionListener(e -> updatePreview());
        init();
        updatePreview();
    }

    private static ComboBoxModel<String> familyModel(String current) {
        Set<String> families = new LinkedHashSet<>();
        families.add("Dialog");
        families.add("SansSerif");
        families.add("Serif");
        families.add("Monospaced");
        families.add(current);
        families.addAll(java.util.Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        return new DefaultComboBoxModel<>(families.toArray(new String[0]));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel styles = new JPanel(new BorderLayout());
        JPanel flags = new JPanel();
        flags.add(bold);
        flags.add(italic);
        styles.add(flags, BorderLayout.WEST);

        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Familia:", family)
                .addLabeledComponent("Tamano:", size)
                .addComponent(styles)
                .addSeparator()
                .addComponent(preview)
                .getPanel();
        panel.setPreferredSize(JBUI.size(360, 170));
        return panel;
    }

    private void updatePreview() {
        preview.setFont(getSelectedFont());
    }

    public Font getSelectedFont() {
        int style = (bold.isSelected() ? Font.BOLD : 0) | (italic.isSelected() ? Font.ITALIC : 0);
        Object selected = family.getSelectedItem();
        String name = selected == null ? "Dialog" : selected.toString();
        return new Font(name, style, ((Number) size.getValue()).intValue());
    }
}
