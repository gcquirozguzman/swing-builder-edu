package com.vanlutec.swingbuilder.model;

import java.util.ArrayList;
import java.util.List;

/** El formulario completo: el JFrame y los componentes que contiene. */
public final class FormModel {

    public static final int MIN_FRAME_WIDTH = 160;
    public static final int MIN_FRAME_HEIGHT = 120;

    private String className = "MiFormulario";
    private String title = "MiFormulario";
    private int frameWidth = 560;
    private int frameHeight = 380;
    private FormTheme theme = FormTheme.SISTEMA;
    private final List<WidgetModel> widgets = new ArrayList<>();

    public FormTheme getTheme() {
        return theme;
    }

    public void setTheme(FormTheme theme) {
        this.theme = theme == null ? FormTheme.SISTEMA : theme;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        if (className != null && !className.isBlank()) {
            this.className = className.trim();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(int frameWidth) {
        this.frameWidth = Math.max(MIN_FRAME_WIDTH, frameWidth);
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(int frameHeight) {
        this.frameHeight = Math.max(MIN_FRAME_HEIGHT, frameHeight);
    }

    /** Lista viva de componentes, en orden de creacion (= orden de pintado). */
    public List<WidgetModel> getWidgets() {
        return widgets;
    }

    public void addWidget(WidgetModel widget) {
        widgets.add(widget);
    }

    public void removeWidget(WidgetModel widget) {
        widgets.remove(widget);
    }

    /**
     * Devuelve un nombre de variable libre a partir del nombre por defecto del tipo,
     * igual que WindowBuilder: {@code btnNewButton}, {@code btnNewButton_1}, ...
     */
    public String nextName(WidgetType type) {
        String base = type.getDefaultName();
        if (!isNameTaken(base)) {
            return base;
        }
        for (int i = 1; i < 1000; i++) {
            String candidate = base + "_" + i;
            if (!isNameTaken(candidate)) {
                return candidate;
            }
        }
        return base + "_x";
    }

    public boolean isNameTaken(String name) {
        for (WidgetModel widget : widgets) {
            if (widget.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** Nombre unico respetando al componente que se esta renombrando. */
    public String uniqueName(String requested, WidgetModel exclude) {
        String sanitized = sanitizeIdentifier(requested);
        boolean free = true;
        for (WidgetModel widget : widgets) {
            if (widget != exclude && widget.getName().equals(sanitized)) {
                free = false;
                break;
            }
        }
        if (free) {
            return sanitized;
        }
        for (int i = 1; i < 1000; i++) {
            String candidate = sanitized + "_" + i;
            boolean taken = false;
            for (WidgetModel widget : widgets) {
                if (widget != exclude && widget.getName().equals(candidate)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                return candidate;
            }
        }
        return sanitized;
    }

    /** Convierte cualquier texto en un identificador Java valido. */
    public static String sanitizeIdentifier(String raw) {
        if (raw == null || raw.isBlank()) {
            return "componente";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : raw.trim().toCharArray()) {
            if (sb.isEmpty() ? Character.isJavaIdentifierStart(c) : Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            }
        }
        return sb.isEmpty() ? "componente" : sb.toString();
    }
}
