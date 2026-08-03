package com.vanlutec.swingbuilder.model;

import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/** Un componente colocado en el formulario. */
public final class WidgetModel {

    private final WidgetType type;
    private String name;
    private String text;
    private int x;
    private int y;
    private int width;
    private int height;
    private String fontFamily = "Dialog";
    private int fontStyle = Font.PLAIN;
    private int fontSize = 12;
    /** Solo para {@code JTextArea}. Apagado = hay barra de scroll horizontal. */
    private boolean lineWrap;
    /** Solo para {@code JComboBox}: las opciones, separadas por comas. */
    private String items = "Opcion 1, Opcion 2, Opcion 3";

    public WidgetModel(WidgetType type, String name, int x, int y) {
        this(type, name, x, y, type.getDefaultWidth(), type.getDefaultHeight(), type.getDefaultText());
    }

    public WidgetModel(WidgetType type, String name, int x, int y, int width, int height, String text) {
        this.type = type;
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text == null ? "" : text;
    }

    public WidgetType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(4, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(4, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void setBounds(Rectangle r) {
        this.x = r.x;
        this.y = r.y;
        setWidth(r.width);
        setHeight(r.height);
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public int getFontStyle() {
        return fontStyle;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Font getFont() {
        return new Font(fontFamily, fontStyle, fontSize);
    }

    public void setFont(Font font) {
        this.fontFamily = font.getFamily();
        this.fontStyle = font.getStyle();
        this.fontSize = font.getSize();
    }

    public void setFont(String family, int style, int size) {
        this.fontFamily = family == null || family.isBlank() ? "Dialog" : family;
        this.fontStyle = style & (Font.BOLD | Font.ITALIC);
        this.fontSize = Math.max(1, size);
    }

    /** Representacion corta para la tabla de propiedades: {@code Dialog 12 Bold}. */
    public String getFontDescription() {
        StringBuilder sb = new StringBuilder(fontFamily).append(' ').append(fontSize);
        if ((fontStyle & Font.BOLD) != 0) {
            sb.append(" Bold");
        }
        if ((fontStyle & Font.ITALIC) != 0) {
            sb.append(" Italic");
        }
        return sb.toString();
    }

    /**
     * Ajuste de linea del {@code JTextArea}. Apagado (por defecto, como un
     * {@code JTextArea} recien creado) las lineas largas no se parten y aparece la
     * barra de scroll horizontal.
     */
    public boolean isLineWrap() {
        return lineWrap;
    }

    public void setLineWrap(boolean lineWrap) {
        this.lineWrap = lineWrap;
    }

    /** Las opciones del {@code JComboBox}, tal cual se escriben: {@code "Uno, Dos, Tres"}. */
    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items == null ? "" : items;
    }

    /** Las mismas opciones ya separadas y sin espacios sobrantes. */
    public List<String> getItemList() {
        List<String> list = new ArrayList<>();
        for (String raw : items.split(",")) {
            String item = raw.trim();
            if (!item.isEmpty()) {
                list.add(item);
            }
        }
        return list;
    }

    public WidgetModel copy() {
        WidgetModel copy = new WidgetModel(type, name, x, y, width, height, text);
        copy.setFont(fontFamily, fontStyle, fontSize);
        copy.setLineWrap(lineWrap);
        copy.setItems(items);
        return copy;
    }
}
