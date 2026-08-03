package com.vanlutec.swingbuilder.model;

import com.vanlutec.swingbuilder.codegen.LessonGenerator;

/**
 * Los puntos de partida que ofrece {@code New | Swing Form (Designer)}.
 * <p>
 * La idea es que nadie empiece delante de una ventana vacia: se elige algo
 * parecido a lo que se quiere y se retoca en el disenador.
 * <p>
 * <b>Todas miden lo mismo</b> ({@link #ANCHO} x {@link #ALTO}): asi la vista previa del
 * dialogo no da saltos al cambiar de plantilla, y lo que se compara de una a otra es la
 * disposicion, no el tamano.
 */
public enum FormTemplate {

    VACIO(FormCategory.DESARROLLO, "Formulario vacio",
            "Solo la ventana. Empieza arrastrando componentes desde la paleta.") {
        @Override
        void fill(FormModel model) {
            // Sin componentes: la ventana tal cual.
        }
    },

    BOTON_Y_TEXTO(FormCategory.DESARROLLO, "Boton y area de texto",
            "Un boton y un JTextArea con scroll. El clasico \"pulso el boton y aparece texto\".") {
        @Override
        void fill(FormModel model) {
            model.addWidget(button("btnProcesar", "Procesar", 20, 20, 120, 28));
            model.addWidget(textArea("textArea", 20, 62, 520, 270));
        }
    },

    ENTRADA_DE_DATOS(FormCategory.DESARROLLO, "Entrada de datos",
            "Etiqueta, campo de texto y boton, con un area para la respuesta.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblNombre", "Introduce tu nombre:", 20, 22, 180, 16));
            model.addWidget(textField("txtNombre", 20, 46, 300, 26));
            model.addWidget(button("btnProcesar", "Procesar", 340, 45, 120, 28));
            model.addWidget(textArea("txtResultado", 20, 88, 520, 244));
        }
    },

    DOS_ENTRADAS(FormCategory.DESARROLLO, "Dos entradas, dos botones y salida",
            "Dos campos con sus etiquetas, dos botones y un area grande de salida. "
                    + "El formulario tipico de practica.") {
        @Override
        void fill(FormModel model) {
            // Misma rejilla que SELECCION_Y_DATO: etiqueta en 20, campo en 120 (200x26),
            // boton en 420. Asi las dos plantillas se ven como hermanas.
            model.addWidget(label("lblDato1", "Dato 1:", 20, 22, 90, 16));
            model.addWidget(textField("txtDato1", 120, 19, 200, 26));
            model.addWidget(label("lblDato2", "Dato 2:", 20, 58, 90, 16));
            model.addWidget(textField("txtDato2", 120, 55, 200, 26));
            model.addWidget(button("btnProcesar", "Procesar", 420, 19, 120, 26));
            model.addWidget(button("btnLimpiar", "Limpiar", 420, 55, 120, 26));
            model.addWidget(textArea("txtSalida", 20, 96, 520, 236));
        }
    },

    SELECCION_Y_DATO(FormCategory.DESARROLLO, "Seleccion y dato",
            "Un JComboBox para elegir y un campo de texto debajo, con dos botones y salida.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblTipo", "Tipo:", 20, 22, 90, 16));
            model.addWidget(comboBox("cmbTipo", "Suma, Resta, Multiplicacion, Division",
                    120, 19, 200, 26));
            model.addWidget(label("lblValor", "Valor:", 20, 58, 90, 16));
            model.addWidget(textField("txtValor", 120, 55, 200, 26));
            model.addWidget(button("btnProcesar", "Procesar", 420, 19, 120, 26));
            model.addWidget(button("btnLimpiar", "Limpiar", 420, 55, 120, 26));
            model.addWidget(textArea("txtSalida", 20, 96, 520, 236));
        }
    },

    // ---------------------------------------------------------------- aprendizaje
    // Una plantilla por tema del curso. Anadir mas es solo otra constante aqui.
    // Este modo va sin vista previa: en clase lo que se explica es el codigo.

    VARIABLES(FormCategory.APRENDIZAJE, "Variables y tipos",
            "Una clase con main y comentarios: enteros, decimales, char, boolean, String, "
                    + "conversiones y constantes. Sin formulario.") {
        @Override
        void fill(FormModel model) {
            // Sin componentes: esta plantilla no crea formulario.
        }

        @Override
        public String javaSource(String className, String packageName) {
            return LessonGenerator.variablesYTipos(className, packageName);
        }
    },

    OPERACIONES(FormCategory.APRENDIZAJE, "Operaciones con variables",
            "Continua la anterior: aritmetica, la division entera, prioridad de "
                    + "parentesis, comparaciones y operadores logicos. Sin formulario.") {
        @Override
        void fill(FormModel model) {
            // Sin componentes: esta plantilla no crea formulario.
        }

        @Override
        public String javaSource(String className, String packageName) {
            return LessonGenerator.operaciones(className, packageName);
        }
    },

    CONDICIONALES(FormCategory.APRENDIZAJE, "Condicionales (if, else if, switch)",
            "Usar los booleanos de la clase anterior para decidir: if, else if, else, "
                    + "condiciones combinadas y switch. Sin formulario.") {
        @Override
        void fill(FormModel model) {
            // Sin componentes: esta plantilla no crea formulario.
        }

        @Override
        public String javaSource(String className, String packageName) {
            return LessonGenerator.condicionales(className, packageName);
        }
    },

    METODOS(FormCategory.APRENDIZAJE, "Metodos",
            "Las cuatro combinaciones, una por una: con y sin parametros, con y sin "
                    + "retorno. Sin formulario.") {
        @Override
        void fill(FormModel model) {
            // Sin componentes: esta plantilla no crea formulario.
        }

        @Override
        public String javaSource(String className, String packageName) {
            return LessonGenerator.metodos(className, packageName);
        }
    };

    /** El tamano de ventana de todas las plantillas. */
    public static final int ANCHO = 560;
    public static final int ALTO = 380;

    private final FormCategory categoria;
    private final String titulo;
    private final String descripcion;

    FormTemplate(FormCategory categoria, String titulo, String descripcion) {
        this.categoria = categoria;
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public FormCategory getCategoria() {
        return categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getAncho() {
        return ANCHO;
    }

    public int getAlto() {
        return ALTO;
    }

    /**
     * {@code true} si la plantilla no crea formulario, solo una clase Java con la
     * leccion. En ese caso no hay {@code .sbe} ni disenador: se abre el codigo.
     */
    public boolean isSoloCodigo() {
        return categoria == FormCategory.APRENDIZAJE;
    }

    /**
     * El fuente de la leccion. Solo tiene sentido si {@link #isSoloCodigo()}.
     *
     * @param packageName paquete de destino, o cadena vacia si no hay
     */
    public String javaSource(String className, String packageName) {
        throw new IllegalStateException(name() + " genera un formulario, no codigo suelto");
    }

    /** Anade al modelo los componentes de la plantilla. */
    abstract void fill(FormModel model);

    /** Crea el formulario inicial: la ventana con el nombre pedido y sus componentes. */
    public FormModel createModel(String className) {
        FormModel model = new FormModel();
        model.setClassName(className);
        model.setTitle(className);
        model.setFrameWidth(ANCHO);
        model.setFrameHeight(ALTO);
        fill(model);
        return model;
    }

    @Override
    public String toString() {
        return titulo;
    }

    // ------------------------------------------------------------------ ayudas

    private static WidgetModel label(String name, String text, int x, int y, int w, int h) {
        return new WidgetModel(WidgetType.LABEL, name, x, y, w, h, text);
    }

    private static WidgetModel textField(String name, int x, int y, int w, int h) {
        return new WidgetModel(WidgetType.TEXT_FIELD, name, x, y, w, h, "");
    }

    private static WidgetModel button(String name, String text, int x, int y, int w, int h) {
        return new WidgetModel(WidgetType.BUTTON, name, x, y, w, h, text);
    }

    private static WidgetModel textArea(String name, int x, int y, int w, int h) {
        return new WidgetModel(WidgetType.TEXT_AREA, name, x, y, w, h, "");
    }

    private static WidgetModel comboBox(String name, String items, int x, int y, int w, int h) {
        WidgetModel widget = new WidgetModel(WidgetType.COMBO_BOX, name, x, y, w, h, "");
        widget.setItems(items);
        return widget;
    }
}
