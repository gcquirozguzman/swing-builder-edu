package com.vanlutec.swingbuilder.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Element;

import java.awt.Font;
import java.util.List;

/**
 * Serializa el modelo al fichero {@code .sbe} (un XML sencillo y legible: los
 * alumnos pueden abrir la pestana "Text" del editor y ver lo que hace el diseno).
 */
public final class FormModelIO {

    private static final Logger LOG = Logger.getInstance(FormModelIO.class);

    private FormModelIO() {
    }

    public static String toXml(FormModel model) {
        Element root = new Element("form");
        root.setAttribute("class", model.getClassName());
        root.setAttribute("title", model.getTitle());
        root.setAttribute("width", String.valueOf(model.getFrameWidth()));
        root.setAttribute("height", String.valueOf(model.getFrameHeight()));
        root.setAttribute("theme", model.getTheme().name());
        for (WidgetModel widget : model.getWidgets()) {
            Element element = new Element("widget");
            element.setAttribute("type", widget.getType().name());
            element.setAttribute("name", widget.getName());
            element.setAttribute("text", widget.getText());
            element.setAttribute("x", String.valueOf(widget.getX()));
            element.setAttribute("y", String.valueOf(widget.getY()));
            element.setAttribute("width", String.valueOf(widget.getWidth()));
            element.setAttribute("height", String.valueOf(widget.getHeight()));
            element.setAttribute("fontFamily", widget.getFontFamily());
            element.setAttribute("fontStyle", String.valueOf(widget.getFontStyle()));
            element.setAttribute("fontSize", String.valueOf(widget.getFontSize()));
            if (widget.getType().needsScrollPane()) {
                element.setAttribute("lineWrap", String.valueOf(widget.isLineWrap()));
            }
            if (widget.getType() == WidgetType.COMBO_BOX) {
                element.setAttribute("items", widget.getItems());
            }
            root.addContent(element);
        }
        return JDOMUtil.write(root);
    }

    /** Nunca falla: si el XML esta roto devuelve un formulario vacio. */
    public static FormModel fromXml(String text, String fallbackClassName) {
        FormModel model = new FormModel();
        if (fallbackClassName != null && !fallbackClassName.isBlank()) {
            model.setClassName(fallbackClassName);
            model.setTitle(fallbackClassName);
        }
        if (text == null || text.isBlank()) {
            return model;
        }
        try {
            Element root = JDOMUtil.load(text);
            model.setClassName(root.getAttributeValue("class", model.getClassName()));
            model.setTitle(root.getAttributeValue("title", model.getTitle()));
            model.setFrameWidth(intValue(root.getAttributeValue("width"), model.getFrameWidth()));
            model.setFrameHeight(intValue(root.getAttributeValue("height"), model.getFrameHeight()));
            model.setTheme(FormTheme.fromId(root.getAttributeValue("theme"), FormTheme.SISTEMA));
            List<Element> children = root.getChildren("widget");
            for (Element element : children) {
                WidgetType type = WidgetType.fromId(element.getAttributeValue("type"), WidgetType.LABEL);
                String name = element.getAttributeValue("name", model.nextName(type));
                WidgetModel widget = new WidgetModel(
                        type,
                        name,
                        intValue(element.getAttributeValue("x"), 10),
                        intValue(element.getAttributeValue("y"), 10),
                        intValue(element.getAttributeValue("width"), type.getDefaultWidth()),
                        intValue(element.getAttributeValue("height"), type.getDefaultHeight()),
                        element.getAttributeValue("text", ""));
                widget.setFont(
                        element.getAttributeValue("fontFamily", "Dialog"),
                        intValue(element.getAttributeValue("fontStyle"), Font.PLAIN),
                        intValue(element.getAttributeValue("fontSize"), 12));
                widget.setLineWrap(Boolean.parseBoolean(element.getAttributeValue("lineWrap", "false")));
                widget.setItems(element.getAttributeValue("items", widget.getItems()));
                model.addWidget(widget);
            }
        } catch (Exception e) {
            LOG.warn("No se pudo leer el diseno (.sbe): " + e.getMessage());
        }
        return model;
    }

    private static int intValue(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
