package com.vanlutec.swingbuilder.ui;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.FormTheme;
import com.vanlutec.swingbuilder.model.WidgetModel;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel derecho: la tabla de propiedades del elemento seleccionado
 * (el JFrame o un componente), editable en vivo.
 */
public final class PropertiesPanel extends JPanel {

    public interface Listener {
        /** Se ha cambiado una propiedad; hay que refrescar el canvas y guardar. */
        void propertyChanged(@Nullable WidgetModel widget, String commandName);
    }

    private static final Color NAME_BG = new JBColor(new Color(0xF2F3F5), new Color(0x3C3F41));
    private static final Color CATEGORY_BG = new JBColor(new Color(0xE4E6EA), new Color(0x45484A));
    private static final Color GRID = new JBColor(new Color(0xD3D6DB), new Color(0x2B2D30));
    private static final Color TITLE_FG = new JBColor(Gray._70, Gray._160);

    private final List<Listener> listeners = new ArrayList<>();
    private final PropertiesTableModel tableModel = new PropertiesTableModel();
    private final JBTable table = new JBTable(tableModel);
    private final JBLabel header = new JBLabel();

    private FormModel form = new FormModel();
    private @Nullable WidgetModel widget;

    public PropertiesPanel() {
        super(new BorderLayout());
        setBackground(UIUtil.getPanelBackground());

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(new ViewTitle("Properties"), BorderLayout.NORTH);
        header.setBorder(JBUI.Borders.empty(4, 8));
        header.setIconTextGap(JBUI.scale(6));
        north.add(header, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        table.setTableHeader(null);
        table.setShowGrid(true);
        table.setGridColor(GRID);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(JBUI.scale(22));
        table.setStriped(false);
        table.setCellSelectionEnabled(true);
        table.setSurrendersFocusOnKeystroke(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(JBUI.scale(90));
        table.getColumnModel().getColumn(1).setPreferredWidth(JBUI.scale(150));
        table.getColumnModel().getColumn(0).setCellRenderer(new NameRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new ValueRenderer());

        JBTextField textEditor = new JBTextField();
        textEditor.setBorder(JBUI.Borders.empty(0, 4));
        table.getColumnModel().getColumn(1).setCellEditor(new ValueEditor(textEditor));

        JBScrollPane scroll = new JBScrollPane(table);
        scroll.setBorder(JBUI.Borders.empty());
        add(scroll, BorderLayout.CENTER);

        showFrame(form);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /** Muestra las propiedades del JFrame. */
    public void showFrame(FormModel form) {
        this.form = form;
        this.widget = null;
        header.setIcon(com.vanlutec.swingbuilder.SbeIcons.SWING_FORM);
        header.setText(form.getClassName() + "  -  JFrame");
        tableModel.reload();
    }

    /** Muestra las propiedades de un componente. */
    public void showWidget(FormModel form, WidgetModel widget) {
        this.form = form;
        this.widget = widget;
        header.setIcon(widget.getType().getIcon());
        header.setText(widget.getName() + "  -  " + widget.getType().getSimpleName());
        tableModel.reload();
    }

    /** Refresca los valores sin reconstruir filas (tras mover con el raton). */
    public void refreshValues() {
        if (widget != null) {
            header.setText(widget.getName() + "  -  " + widget.getType().getSimpleName());
        } else {
            header.setText(form.getClassName() + "  -  JFrame");
        }
        if (table.isEditing()) {
            return;
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Empieza a editar la propiedad {@code text} del componente seleccionado.
     * <p>
     * Es lo que hace WindowBuilder al dar doble clic sobre un componente que no
     * tiene evento asociado (una etiqueta, por ejemplo).
     */
    public void startEditingText() {
        if (widget == null) {
            return;
        }
        int row = tableModel.indexOf("text");
        if (row < 0 || table.getColumnCount() < 2) {
            return;
        }
        table.setRowSelectionInterval(row, row);
        table.scrollRectToVisible(table.getCellRect(row, 1, true));
        if (!table.editCellAt(row, 1)) {
            return;
        }
        Component editor = table.getEditorComponent();
        if (editor != null) {
            editor.requestFocusInWindow();
            if (editor instanceof javax.swing.text.JTextComponent field) {
                field.selectAll();
            }
        }
    }

    private void fireChanged(String commandName) {
        for (Listener listener : listeners) {
            listener.propertyChanged(widget, commandName);
        }
    }

    // ------------------------------------------------------------------ filas

    private enum Kind {CATEGORY, STRING, INT, FONT, BOOLEAN, THEME, READONLY}

    private record Row(String name, Kind kind, boolean child) {
    }

    private static final List<Row> FRAME_ROWS = List.of(
            new Row("class", Kind.STRING, false),
            new Row("title", Kind.STRING, false),
            new Row("theme", Kind.THEME, false),
            new Row("layout", Kind.READONLY, false),
            new Row("size", Kind.CATEGORY, false),
            new Row("width", Kind.INT, true),
            new Row("height", Kind.INT, true));

    private static final List<Row> BOUNDS_ROWS = List.of(
            new Row("bounds", Kind.CATEGORY, false),
            new Row("x", Kind.INT, true),
            new Row("y", Kind.INT, true),
            new Row("width", Kind.INT, true),
            new Row("height", Kind.INT, true));

    private static final List<Row> WIDGET_ROWS = concat(List.of(
            new Row("name", Kind.STRING, false),
            new Row("text", Kind.STRING, false),
            new Row("font", Kind.FONT, false)), BOUNDS_ROWS);

    /** El JTextArea ademas decide si parte las lineas o saca scroll horizontal. */
    private static final List<Row> TEXT_AREA_ROWS = concat(List.of(
            new Row("name", Kind.STRING, false),
            new Row("text", Kind.STRING, false),
            new Row("font", Kind.FONT, false),
            new Row("lineWrap", Kind.BOOLEAN, false)), BOUNDS_ROWS);

    /** El JComboBox no tiene "text": tiene la lista de opciones. */
    private static final List<Row> COMBO_BOX_ROWS = concat(List.of(
            new Row("name", Kind.STRING, false),
            new Row("items", Kind.STRING, false),
            new Row("font", Kind.FONT, false)), BOUNDS_ROWS);

    private static List<Row> concat(List<Row> first, List<Row> second) {
        List<Row> all = new ArrayList<>(first);
        all.addAll(second);
        return List.copyOf(all);
    }

    private final class PropertiesTableModel extends AbstractTableModel {

        private List<Row> rows = FRAME_ROWS;

        /** Fila que lleva ese nombre de propiedad, o -1. */
        private int indexOf(String propertyName) {
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).name().equals(propertyName)) {
                    return i;
                }
            }
            return -1;
        }

        private void reload() {
            if (widget == null) {
                rows = FRAME_ROWS;
            } else {
                rows = switch (widget.getType()) {
                    case TEXT_AREA -> TEXT_AREA_ROWS;
                    case COMBO_BOX -> COMBO_BOX_ROWS;
                    default -> WIDGET_ROWS;
                };
            }
            fireTableStructureChanged();
            if (table.getColumnCount() == 2) {
                table.getColumnModel().getColumn(0).setPreferredWidth(JBUI.scale(90));
                table.getColumnModel().getColumn(0).setCellRenderer(new NameRenderer());
                table.getColumnModel().getColumn(1).setCellRenderer(new ValueRenderer());
                table.getColumnModel().getColumn(1).setCellEditor(new ValueEditor(new JBTextField()));
            }
        }

        private Row row(int index) {
            return rows.get(index);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return false;
            }
            Kind kind = row(rowIndex).kind();
            return kind != Kind.CATEGORY && kind != Kind.READONLY;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row row = row(rowIndex);
            if (columnIndex == 0) {
                return row.name();
            }
            if (row.kind() == Kind.CATEGORY) {
                return "";
            }
            if (widget == null) {
                return switch (row.name()) {
                    case "class" -> form.getClassName();
                    case "title" -> form.getTitle();
                    case "theme" -> form.getTheme().getTitulo();
                    case "layout" -> "null (posiciones absolutas)";
                    case "width" -> String.valueOf(form.getFrameWidth());
                    case "height" -> String.valueOf(form.getFrameHeight());
                    default -> "";
                };
            }
            return switch (row.name()) {
                case "name" -> widget.getName();
                case "text" -> widget.getText();
                case "font" -> widget.getFontDescription();
                case "lineWrap" -> String.valueOf(widget.isLineWrap());
                case "items" -> widget.getItems();
                case "x" -> String.valueOf(widget.getX());
                case "y" -> String.valueOf(widget.getY());
                case "width" -> String.valueOf(widget.getWidth());
                case "height" -> String.valueOf(widget.getHeight());
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            Row row = row(rowIndex);
            String text = value == null ? "" : value.toString();
            if (widget == null) {
                switch (row.name()) {
                    case "class" -> form.setClassName(FormModel.sanitizeIdentifier(text));
                    case "title" -> form.setTitle(text);
                    case "theme" -> form.setTheme(themeByTitle(text));
                    case "width" -> form.setFrameWidth(parseInt(text, form.getFrameWidth()));
                    case "height" -> form.setFrameHeight(parseInt(text, form.getFrameHeight()));
                    default -> {
                        return;
                    }
                }
                fireChanged("Cambiar " + row.name());
                refreshValues();
                return;
            }
            switch (row.name()) {
                case "name" -> widget.setName(form.uniqueName(text, widget));
                case "text" -> widget.setText(text);
                case "lineWrap" -> widget.setLineWrap(Boolean.parseBoolean(text));
                case "items" -> widget.setItems(text);
                case "x" -> widget.setX(Math.max(0, parseInt(text, widget.getX())));
                case "y" -> widget.setY(Math.max(0, parseInt(text, widget.getY())));
                case "width" -> widget.setWidth(parseInt(text, widget.getWidth()));
                case "height" -> widget.setHeight(parseInt(text, widget.getHeight()));
                case "font" -> {
                    // el editor de fuente ya ha aplicado el valor
                }
                default -> {
                    return;
                }
            }
            fireChanged("Cambiar " + row.name());
            refreshValues();
        }

        private FormTheme themeByTitle(String title) {
            for (FormTheme theme : FormTheme.values()) {
                if (theme.getTitulo().equals(title)) {
                    return theme;
                }
            }
            return form.getTheme();
        }

        private int parseInt(String text, int fallback) {
            try {
                return Integer.parseInt(text.trim());
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
    }

    // ------------------------------------------------------------- renderizado

    private final class NameRenderer extends JComponent implements TableCellRenderer {

        private String text = "";
        private boolean category;
        private boolean child;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Row model = tableModel.row(row);
            this.text = model.name();
            this.category = model.kind() == Kind.CATEGORY;
            this.child = model.child();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                UIUtil.applyRenderingHints(g2);
                g2.setColor(category ? CATEGORY_BG : NAME_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(category ? JBUI.Fonts.label().deriveFont(Font.BOLD) : JBUI.Fonts.label());
                g2.setColor(UIUtil.getLabelForeground());
                FontMetrics fm = g2.getFontMetrics();
                int x = JBUI.scale(child ? 18 : 6);
                if (category) {
                    int cx = JBUI.scale(8);
                    int cy = getHeight() / 2;
                    int s = JBUI.scale(3);
                    g2.fillPolygon(new int[]{cx - s, cx + s, cx}, new int[]{cy - s + 1, cy - s + 1, cy + s}, 3);
                    x = JBUI.scale(16);
                }
                g2.drawString(text, x, (getHeight() + fm.getAscent()) / 2 - JBUI.scale(1));
            } finally {
                g2.dispose();
            }
        }
    }

    private final class ValueRenderer extends JComponent implements TableCellRenderer {

        private String text = "";
        private boolean category;
        private boolean readOnly;
        private boolean selected;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Row model = tableModel.row(row);
            this.text = value == null ? "" : value.toString();
            this.category = model.kind() == Kind.CATEGORY;
            this.readOnly = model.kind() == Kind.READONLY;
            this.selected = isSelected;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                UIUtil.applyRenderingHints(g2);
                if (category) {
                    g2.setColor(CATEGORY_BG);
                } else if (selected) {
                    g2.setColor(UIUtil.getTableSelectionBackground(true));
                } else {
                    g2.setColor(UIUtil.getTableBackground());
                }
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(JBUI.Fonts.label());
                g2.setColor(readOnly
                        ? UIUtil.getInactiveTextColor()
                        : selected ? UIUtil.getTableSelectionForeground(true) : UIUtil.getLabelForeground());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, JBUI.scale(5), (getHeight() + fm.getAscent()) / 2 - JBUI.scale(1));
            } finally {
                g2.dispose();
            }
        }
    }

    /** Editor de la columna de valores: texto normal, o dialogo para {@code font}. */
    private final class ValueEditor extends AbstractCellEditor implements TableCellEditor {

        private final DefaultCellEditor delegate;
        private final JPanel fontEditor = new JPanel(new BorderLayout());
        private final JBLabel fontLabel = new JBLabel();
        private final DefaultCellEditor booleanEditor =
                new DefaultCellEditor(new javax.swing.JComboBox<>(new String[]{"false", "true"}));
        private final DefaultCellEditor themeEditor = new DefaultCellEditor(new javax.swing.JComboBox<>(themeTitles()));
        private boolean editingFont;
        private boolean editingBoolean;
        private boolean editingTheme;

        private static String[] themeTitles() {
            FormTheme[] all = FormTheme.values();
            String[] titles = new String[all.length];
            for (int i = 0; i < all.length; i++) {
                titles[i] = all[i].getTitulo();
            }
            return titles;
        }

        private ValueEditor(JBTextField field) {
            this.delegate = new DefaultCellEditor(field);
            this.delegate.setClickCountToStart(2);
            this.booleanEditor.setClickCountToStart(2);
            fontEditor.setOpaque(true);
            fontEditor.setBackground(UIUtil.getTableBackground());
            fontLabel.setBorder(JBUI.Borders.empty(0, 5));
            JButton browse = new JButton("...");
            browse.setMargin(JBUI.insets(0, 2));
            browse.setPreferredSize(new Dimension(JBUI.scale(22), JBUI.scale(18)));
            browse.setFocusable(false);
            browse.addActionListener(e -> {
                if (widget == null) {
                    return;
                }
                FontChooserDialog dialog = new FontChooserDialog(PropertiesPanel.this, widget.getFont());
                if (dialog.showAndGet()) {
                    widget.setFont(dialog.getSelectedFont());
                    fireChanged("Cambiar font");
                    refreshValues();
                }
                cancelCellEditing();
            });
            fontEditor.add(fontLabel, BorderLayout.CENTER);
            fontEditor.add(browse, BorderLayout.EAST);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            Kind kind = tableModel.row(row).kind();
            editingFont = kind == Kind.FONT;
            if (editingFont) {
                fontLabel.setText(value == null ? "" : value.toString());
                return fontEditor;
            }
            editingBoolean = kind == Kind.BOOLEAN;
            if (editingBoolean) {
                return booleanEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
            editingTheme = kind == Kind.THEME;
            if (editingTheme) {
                return themeEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
            return delegate.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            if (editingFont) {
                return fontLabel.getText();
            }
            if (editingBoolean) {
                return booleanEditor.getCellEditorValue();
            }
            return editingTheme ? themeEditor.getCellEditorValue() : delegate.getCellEditorValue();
        }

        @Override
        public boolean shouldSelectCell(java.util.EventObject anEvent) {
            return true;
        }
    }

    /** Cabecera gris con el nombre de la "vista", como en Eclipse. */
    private static final class ViewTitle extends JComponent {

        private final String text;

        private ViewTitle(String text) {
            this.text = text;
            setPreferredSize(new Dimension(0, JBUI.scale(22)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                UIUtil.applyRenderingHints(g2);
                g2.setColor(NAME_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(TITLE_FG);
                g2.setFont(JBUI.Fonts.label(11f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, JBUI.scale(8), (getHeight() + fm.getAscent()) / 2 - JBUI.scale(1));
                g2.setColor(GRID);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            } finally {
                g2.dispose();
            }
        }
    }
}
