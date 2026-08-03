package com.vanlutec.swingbuilder.codegen;

import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.FormTheme;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;

import org.jetbrains.annotations.Nullable;

import java.awt.Font;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Convierte el modelo del disenador en codigo Java.
 * <p>
 * El fichero generado tiene <b>dos zonas vigiladas</b> (imports y diseno) que se
 * reescriben enteras cada vez que cambia el diseno. Todo lo que hay fuera de esas
 * zonas es del alumno y no se toca nunca: ahi viven {@code initEventos()},
 * {@code main} y cualquier metodo que anada.
 * <p>
 * Es el mismo reparto que usa NetBeans: zona generada + zona del usuario. Asi el
 * doble clic puede anadir un {@code ActionListener} sin miedo a que la siguiente
 * regeneracion se lleve por delante el codigo escrito dentro.
 */
public final class JavaFormGenerator {

    /** Marcas de las zonas generadas (se buscan por prefijo, no por linea exacta). */
    public static final String IMPORTS_BEGIN_KEY = "// >>> SBE-IMPORTS";
    public static final String IMPORTS_END_KEY = "// <<< SBE-IMPORTS";
    public static final String DESIGN_BEGIN_KEY = "// >>> SBE-DISENO";
    public static final String DESIGN_END_KEY = "// <<< SBE-DISENO";

    /** Metodo (fuera de las zonas generadas) donde aterrizan los ActionListener. */
    public static final String EVENTS_METHOD = "initEventos";

    private static final String NO_TOCAR = ": zona generada por Swing Builder Edu (no editar a mano) >>>";
    private static final String IMPORTS_BEGIN = IMPORTS_BEGIN_KEY + NO_TOCAR;
    private static final String IMPORTS_END = IMPORTS_END_KEY + " <<<";
    private static final String DESIGN_BEGIN = "    " + DESIGN_BEGIN_KEY + NO_TOCAR;
    private static final String DESIGN_END = "    " + DESIGN_END_KEY + " <<<";

    private static final String CONTENT_PANE = "contentPane";

    /** Alto de la barra de titulo que dibuja el disenador dentro del alto del formulario. */
    private static final int TITLE_BAR = 26;

    private JavaFormGenerator() {
    }

    // ------------------------------------------------------------ fichero nuevo

    /** El fichero completo, tal y como se crea la primera vez. */
    public static String newFile(FormModel model, String packageName) {
        String cls = model.getClassName();
        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }
        sb.append(IMPORTS_BEGIN).append('\n');
        sb.append(imports(model));
        sb.append(IMPORTS_END).append("\n\n");

        sb.append("/**\n");
        sb.append(" * Formulario generado por Swing Builder Edu.\n");
        sb.append(" * <p>\n");
        sb.append(" * El diseno se edita en ").append(cls).append(".sbe (pestana Design). Las zonas marcadas\n");
        sb.append(" * como generadas se reescriben solas; escribe tu codigo en ").append(EVENTS_METHOD).append("()\n");
        sb.append(" * o en metodos nuevos, que eso no se toca.\n");
        sb.append(" *\n");
        sb.append(" * Cibertec - Swing Builder Edu\n");
        sb.append(" */\n");
        sb.append("public class ").append(cls).append(" extends JFrame {\n\n");

        sb.append(DESIGN_BEGIN).append('\n');
        sb.append(design(model));
        sb.append(DESIGN_END).append("\n\n");

        sb.append("    /**\n");
        sb.append("     * Aqui van tus eventos.\n");
        sb.append("     * <p>\n");
        sb.append("     * Haz <b>doble clic</b> sobre un JButton en el disenador y Swing Builder Edu\n");
        sb.append("     * anadira aqui su ActionListener, listo para que escribas dentro.\n");
        sb.append("     */\n");
        sb.append("    private void ").append(EVENTS_METHOD).append("() {\n");
        sb.append("    }\n\n");

        sb.append("    public static void main(String[] args) {\n");
        sb.append("        EventQueue.invokeLater(() -> new ").append(cls).append("().setVisible(true));\n");
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }

    // ------------------------------------------------------------ regeneracion

    /** Un trozo generado del fichero, con el sitio exacto que ocupa. */
    public record Region(int start, int end, String body) {

        public boolean isUpToDate(String source) {
            return source.substring(start, end).equals(body);
        }
    }

    /**
     * Localiza las dos zonas generadas y calcula su contenido nuevo.
     * <p>
     * Vienen <b>de atras hacia delante</b>, para poder aplicarlas una tras otra sin
     * que se muevan los offsets. Solo se reemplaza lo de dentro de las marcas: el
     * resto del fichero (el codigo del alumno) ni se lee ni se toca.
     *
     * @return las zonas, o {@code null} si faltan las marcas (fichero escrito a
     *         mano: en ese caso no se toca nada)
     */
    public static @Nullable List<Region> regions(String source, FormModel model) {
        Region imports = locate(source, IMPORTS_BEGIN_KEY, IMPORTS_END_KEY, imports(model));
        Region design = locate(source, DESIGN_BEGIN_KEY, DESIGN_END_KEY, design(model));
        if (imports == null || design == null || design.start() < imports.end()) {
            return null;
        }
        return List.of(design, imports);
    }

    /**
     * Reescribe las dos zonas generadas de un fuente completo.
     *
     * @return el fuente actualizado, o {@code null} si faltan las marcas
     */
    public static @Nullable String refresh(String source, FormModel model) {
        List<Region> regions = regions(source, model);
        if (regions == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(source);
        for (Region region : regions) {
            sb.replace(region.start(), region.end(), region.body());
        }
        return sb.toString();
    }

    /** El hueco entre la linea de la marca de inicio y la linea de la marca de cierre. */
    private static @Nullable Region locate(String source, String beginKey, String endKey, String body) {
        int begin = source.indexOf(beginKey);
        if (begin < 0) {
            return null;
        }
        int afterBeginLine = source.indexOf('\n', begin);
        int end = source.indexOf(endKey, begin);
        if (afterBeginLine < 0 || end < 0) {
            return null;
        }
        // Retrocede hasta el principio de la linea de cierre, para conservar su sangria.
        int endLineStart = source.lastIndexOf('\n', end) + 1;
        return new Region(afterBeginLine + 1, endLineStart, body);
    }

    // ------------------------------------------------------------ zona: imports

    static String imports(FormModel model) {
        Set<String> needed = new TreeSet<>();
        needed.add("java.awt.Dimension");
        needed.add("java.awt.EventQueue");
        needed.add("javax.swing.JFrame");
        needed.add("javax.swing.JPanel");
        if (!model.getTheme().isSistema()) {
            needed.add("java.awt.Color");
        }
        for (WidgetModel widget : model.getWidgets()) {
            needed.add(widget.getType().getQualifiedName());
            if (widget.getType().needsScrollPane()) {
                needed.add("javax.swing.JScrollPane");
            }
            if (widget.getType() == WidgetType.COMBO_BOX && !model.getTheme().isSistema()) {
                needed.add("javax.swing.plaf.basic.BasicComboBoxUI");
            }
            if (hasCustomFont(widget)) {
                needed.add("java.awt.Font");
            }
        }
        // java.* antes que javax.*, como hace el propio IDE.
        Set<String> ordered = new LinkedHashSet<>();
        needed.stream().filter(i -> i.startsWith("java.")).forEach(ordered::add);
        needed.stream().filter(i -> !i.startsWith("java.")).forEach(ordered::add);

        StringBuilder sb = new StringBuilder();
        String previousRoot = null;
        for (String imported : ordered) {
            String root = imported.substring(0, imported.indexOf('.'));
            if (previousRoot != null && !previousRoot.equals(root)) {
                sb.append('\n');
            }
            sb.append("import ").append(imported).append(";\n");
            previousRoot = root;
        }
        return sb.toString();
    }

    // ------------------------------------------------------------- zona: diseno

    static String design(FormModel model) {
        StringBuilder sb = new StringBuilder();

        sb.append("    private JPanel ").append(CONTENT_PANE).append(";\n");
        for (WidgetModel widget : model.getWidgets()) {
            sb.append("    private ").append(fieldType(widget))
                    .append(' ').append(widget.getName()).append(";\n");
            if (widget.getType().needsScrollPane()) {
                sb.append("    private JScrollPane ").append(scrollName(widget)).append(";\n");
            }
        }
        sb.append('\n');

        sb.append("    public ").append(model.getClassName()).append("() {\n");
        sb.append("        setTitle(").append(quote(model.getTitle())).append(");\n");
        sb.append("        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n");
        sb.append('\n');
        sb.append("        ").append(CONTENT_PANE).append(" = new JPanel();\n");
        sb.append("        ").append(CONTENT_PANE).append(".setLayout(null);\n");
        if (!model.getTheme().isSistema()) {
            sb.append("        ").append(CONTENT_PANE).append(".setBackground(")
                    .append(model.getTheme().panelBackgroundLiteral()).append(");\n");
        }
        // El tamano se le da al panel, no a la ventana: si usaramos setBounds sobre el
        // JFrame, el marco y la barra de titulo se comerian el area util y todo
        // quedaria pegado a la derecha y abajo respecto a lo que se ve en el disenador.
        sb.append("        ").append(CONTENT_PANE).append(".setPreferredSize(new Dimension(")
                .append(model.getFrameWidth()).append(", ")
                .append(Math.max(1, model.getFrameHeight() - TITLE_BAR)).append("));\n");
        sb.append("        setContentPane(").append(CONTENT_PANE).append(");\n");

        for (WidgetModel widget : model.getWidgets()) {
            sb.append('\n');
            appendWidget(sb, widget, model.getTheme());
        }

        sb.append('\n');
        sb.append("        pack();\n");
        sb.append("        setLocationRelativeTo(null);\n");
        sb.append("        ").append(EVENTS_METHOD).append("();\n");
        sb.append("    }\n");
        return sb.toString();
    }

    private static void appendWidget(StringBuilder sb, WidgetModel widget, FormTheme theme) {
        String name = widget.getName();

        sb.append("        ").append(name).append(" = new ").append(constructor(widget)).append(";\n");
        if (widget.getType() == WidgetType.COMBO_BOX) {
            for (String item : widget.getItemList()) {
                sb.append("        ").append(name).append(".addItem(").append(quote(item)).append(");\n");
            }
        } else if (!widget.getText().isEmpty()) {
            sb.append("        ").append(name).append(".setText(").append(quote(widget.getText())).append(");\n");
        }
        appendTheme(sb, widget, theme);
        if (hasCustomFont(widget)) {
            sb.append("        ").append(name).append(".setFont(new Font(")
                    .append(quote(widget.getFontFamily())).append(", ")
                    .append(fontStyle(widget.getFontStyle())).append(", ")
                    .append(widget.getFontSize()).append("));\n");
        }
        if (widget.getType() == WidgetType.TEXT_AREA) {
            // Sin ajuste de linea, el JScrollPane saca por su cuenta la barra horizontal.
            sb.append("        ").append(name).append(".setLineWrap(")
                    .append(widget.isLineWrap()).append(");\n");
            sb.append("        ").append(name).append(".setWrapStyleWord(")
                    .append(widget.isLineWrap()).append(");\n");
        }

        String added = name;
        if (widget.getType().needsScrollPane()) {
            added = scrollName(widget);
            sb.append("        ").append(added).append(" = new JScrollPane(").append(name).append(");\n");
        }
        sb.append("        ").append(added).append(".setBounds(")
                .append(widget.getX()).append(", ").append(widget.getY()).append(", ")
                .append(widget.getWidth()).append(", ").append(widget.getHeight()).append(");\n");
        sb.append("        ").append(CONTENT_PANE).append(".add(").append(added).append(");\n");
    }

    // ------------------------------------------------------------------ helpers

    /** El tipo del campo: {@code JComboBox<String>} lleva su parametro de tipo. */
    private static String fieldType(WidgetModel widget) {
        return widget.getType() == WidgetType.COMBO_BOX
                ? "JComboBox<String>"
                : widget.getType().getSimpleName();
    }

    private static String constructor(WidgetModel widget) {
        return widget.getType() == WidgetType.COMBO_BOX
                ? "JComboBox<>()"
                : widget.getType().getSimpleName() + "()";
    }

    /** Los colores del tema, si no es el del sistema. */
    private static void appendTheme(StringBuilder sb, WidgetModel widget, FormTheme theme) {
        if (theme == null || theme.isSistema()) {
            return;
        }
        String name = widget.getName();
        switch (widget.getType()) {
            case LABEL ->
                    sb.append("        ").append(name).append(".setForeground(")
                            .append(theme.textForegroundLiteral()).append(");\n");
            case BUTTON -> {
                // Sin estas dos lineas, el LAF pinta su propio fondo y setBackground no se ve.
                sb.append("        ").append(name).append(".setContentAreaFilled(false);\n");
                sb.append("        ").append(name).append(".setOpaque(true);\n");
                sb.append("        ").append(name).append(".setBackground(")
                        .append(theme.buttonBackgroundLiteral()).append(");\n");
                sb.append("        ").append(name).append(".setForeground(")
                        .append(theme.buttonForegroundLiteral()).append(");\n");
            }
            case COMBO_BOX -> {
                // Igual que el boton: con la UI del LAF, setBackground se ignora.
                sb.append("        ").append(name).append(".setUI(new BasicComboBoxUI());\n");
                sb.append("        ").append(name).append(".setBackground(")
                        .append(theme.fieldBackgroundLiteral()).append(");\n");
                sb.append("        ").append(name).append(".setForeground(")
                        .append(theme.fieldForegroundLiteral()).append(");\n");
            }
            case TEXT_FIELD, TEXT_AREA -> {
                sb.append("        ").append(name).append(".setBackground(")
                        .append(theme.fieldBackgroundLiteral()).append(");\n");
                sb.append("        ").append(name).append(".setForeground(")
                        .append(theme.fieldForegroundLiteral()).append(");\n");
                // Sin esto, sobre fondo oscuro el cursor de escritura no se ve.
                sb.append("        ").append(name).append(".setCaretColor(")
                        .append(theme.fieldForegroundLiteral()).append(");\n");
            }
        }
    }

    /** El {@code JScrollPane} que envuelve a un {@code JTextArea}: {@code scrollTextArea}. */
    public static String scrollName(WidgetModel widget) {
        String name = widget.getName();
        return "scroll" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /** La sentencia que crea el listener de un boton, tal cual se inserta en el codigo. */
    public static String actionListenerStatement(String buttonName) {
        return buttonName + ".addActionListener(e -> {\n"
                + "    // TODO: accion aqui\n"
                + "});";
    }

    /** El comentario que deja el cursor del alumno en su sitio tras el doble clic. */
    public static final String TODO_COMMENT = "// TODO: accion aqui";

    private static boolean hasCustomFont(WidgetModel widget) {
        return !"Dialog".equals(widget.getFontFamily())
                || widget.getFontStyle() != Font.PLAIN
                || widget.getFontSize() != 12;
    }

    private static String fontStyle(int style) {
        boolean bold = (style & Font.BOLD) != 0;
        boolean italic = (style & Font.ITALIC) != 0;
        if (bold && italic) {
            return "Font.BOLD | Font.ITALIC";
        }
        if (bold) {
            return "Font.BOLD";
        }
        if (italic) {
            return "Font.ITALIC";
        }
        return "Font.PLAIN";
    }

    private static String quote(String raw) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : raw.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
