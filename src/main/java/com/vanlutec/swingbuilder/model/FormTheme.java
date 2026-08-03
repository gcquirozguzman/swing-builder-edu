package com.vanlutec.swingbuilder.model;

import java.awt.Color;

/**
 * Combinacion de colores del formulario.
 * <p>
 * Se aplica a lo que <b>todos</b> los Look and Feel respetan: el fondo del panel, el
 * color de texto de etiquetas y botones, y el fondo/texto de los campos editables. El
 * fondo de un {@code JButton} se deja en paz a proposito: la mitad de los LAF lo
 * ignoran y el resultado seria distinto en el disenador y al ejecutar.
 */
public enum FormTheme {

    SISTEMA("Sistema",
            "Los colores propios del sistema. El codigo generado no toca ningun color."),

    CLARO("Claro",
            "Fondo claro y texto oscuro.",
            0xF7F8FA, 0x1F2328, 0xFFFFFF, 0x1F2328, 0xE3E6EB, 0x1F2328),

    OSCURO("Oscuro",
            "Fondo gris oscuro y texto claro.",
            0x2B2D30, 0xE6E6E6, 0x3C3F41, 0xE6E6E6, 0x4A4E52, 0xE6E6E6),

    ALTO_CONTRASTE("Alto contraste",
            "Negro y amarillo. Util en proyectores y para baja vision.",
            0x000000, 0xFFE000, 0x000000, 0xFFE000, 0xFFE000, 0x000000),

    TERMINAL("Terminal",
            "Verde sobre negro, estilo consola.",
            0x0A0F0A, 0x33FF66, 0x0A0F0A, 0x33FF66, 0x123018, 0x33FF66);

    private final String titulo;
    private final String descripcion;
    private final Integer panelBg;
    private final Integer textoFg;
    private final Integer campoBg;
    private final Integer campoFg;
    private final Integer botonBg;
    private final Integer botonFg;

    FormTheme(String titulo, String descripcion) {
        this(titulo, descripcion, null, null, null, null, null, null);
    }

    FormTheme(String titulo, String descripcion,
              Integer panelBg, Integer textoFg, Integer campoBg, Integer campoFg,
              Integer botonBg, Integer botonFg) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.panelBg = panelBg;
        this.textoFg = textoFg;
        this.campoBg = campoBg;
        this.campoFg = campoFg;
        this.botonBg = botonBg;
        this.botonFg = botonFg;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** {@code true} si no hay que tocar ningun color (tema "Sistema"). */
    public boolean isSistema() {
        return panelBg == null;
    }

    /** Fondo del {@code contentPane}, o {@code null} con el tema del sistema. */
    public Color getPanelBackground() {
        return color(panelBg);
    }

    /** Color del texto de etiquetas y botones. */
    public Color getTextForeground() {
        return color(textoFg);
    }

    /** Fondo de {@code JTextField} y {@code JTextArea}. */
    public Color getFieldBackground() {
        return color(campoBg);
    }

    /** Texto (y cursor) de {@code JTextField} y {@code JTextArea}. */
    public Color getFieldForeground() {
        return color(campoFg);
    }

    /** Fondo del {@code JButton}. Se pinta plano, ver {@link #buttonBackgroundLiteral()}. */
    public Color getButtonBackground() {
        return color(botonBg);
    }

    public Color getButtonForeground() {
        return color(botonFg);
    }

    /** El literal Java del color, para el generador: {@code new Color(0x2B2D30)}. */
    public String panelBackgroundLiteral() {
        return literal(panelBg);
    }

    public String textForegroundLiteral() {
        return literal(textoFg);
    }

    public String fieldBackgroundLiteral() {
        return literal(campoBg);
    }

    public String fieldForegroundLiteral() {
        return literal(campoFg);
    }

    /**
     * Fondo del boton. Va siempre acompanado de {@code setContentAreaFilled(false)} y
     * {@code setOpaque(true)}: sin eso, la mitad de los Look and Feel (Darcula, Windows)
     * pintan su propio fondo encima y el {@code setBackground} no se ve. El boton queda
     * plano, pero igual en el disenador y al ejecutar.
     */
    public String buttonBackgroundLiteral() {
        return literal(botonBg);
    }

    public String buttonForegroundLiteral() {
        return literal(botonFg);
    }

    private static Color color(Integer rgb) {
        return rgb == null ? null : new Color(rgb);
    }

    private static String literal(Integer rgb) {
        return rgb == null ? null : String.format("new Color(0x%06X)", rgb);
    }

    public static FormTheme fromId(String id, FormTheme fallback) {
        if (id != null) {
            for (FormTheme theme : values()) {
                if (theme.name().equalsIgnoreCase(id)) {
                    return theme;
                }
            }
        }
        return fallback;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
