package com.vanlutec.swingbuilder.model;

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

    VARIABLES(FormCategory.APRENDIZAJE, "Variables y tipos",
            "Pide un texto y un numero para practicar String, int y la conversion con "
                    + "Integer.parseInt.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblNombre", "Nombre (String):", 20, 22, 130, 16));
            model.addWidget(textField("txtNombre", 160, 19, 200, 26));
            model.addWidget(label("lblEdad", "Edad (int):", 20, 58, 130, 16));
            model.addWidget(textField("txtEdad", 160, 55, 200, 26));
            model.addWidget(button("btnMostrar", "Mostrar", 420, 19, 120, 26));
            model.addWidget(button("btnLimpiar", "Limpiar", 420, 55, 120, 26));
            model.addWidget(textArea("txtSalida", 20, 96, 520, 236));
        }
    },

    CONDICIONALES(FormCategory.APRENDIZAJE, "Condicionales (if)",
            "Una nota y un boton que decide si aprueba. Para practicar if / else if / else.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblNota", "Nota (0-20):", 20, 22, 130, 16));
            model.addWidget(textField("txtNota", 160, 19, 200, 26));
            model.addWidget(button("btnEvaluar", "Evaluar", 420, 19, 120, 26));
            model.addWidget(button("btnLimpiar", "Limpiar", 420, 55, 120, 26));
            model.addWidget(label("lblResultado", "Resultado:", 20, 60, 130, 16));
            model.addWidget(textArea("txtSalida", 20, 96, 520, 236));
        }
    },

    BUCLES(FormCategory.APRENDIZAJE, "Bucles (for / while)",
            "Un numero y un boton que escribe su tabla de multiplicar linea a linea.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblNumero", "Numero:", 20, 22, 130, 16));
            model.addWidget(textField("txtNumero", 160, 19, 200, 26));
            model.addWidget(label("lblRepeticiones", "Repeticiones:", 20, 58, 130, 16));
            model.addWidget(textField("txtRepeticiones", 160, 55, 200, 26));
            model.addWidget(button("btnGenerar", "Generar", 420, 19, 120, 26));
            model.addWidget(button("btnLimpiar", "Limpiar", 420, 55, 120, 26));
            model.addWidget(textArea("txtSalida", 20, 96, 520, 236));
        }
    },

    ARREGLOS(FormCategory.APRENDIZAJE, "Arreglos y listas",
            "Anadir elementos y listarlos. Para practicar arreglos, ArrayList y recorridos.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblElemento", "Elemento:", 20, 22, 130, 16));
            model.addWidget(textField("txtElemento", 160, 19, 200, 26));
            model.addWidget(label("lblOrden", "Orden:", 20, 58, 130, 16));
            model.addWidget(comboBox("cmbOrden", "Sin ordenar, Ascendente, Descendente",
                    160, 55, 200, 26));
            model.addWidget(button("btnAgregar", "Agregar", 420, 19, 120, 26));
            model.addWidget(button("btnListar", "Listar", 420, 55, 120, 26));
            model.addWidget(textArea("txtSalida", 20, 96, 520, 236));
        }
    },

    OBJETOS(FormCategory.APRENDIZAJE, "Clases y objetos",
            "Los datos de un objeto y un boton que lo crea y lo muestra. Para practicar "
                    + "clases, constructores y toString.") {
        @Override
        void fill(FormModel model) {
            model.addWidget(label("lblCodigo", "Codigo:", 20, 22, 130, 16));
            model.addWidget(textField("txtCodigo", 160, 19, 200, 26));
            model.addWidget(label("lblDescripcion", "Descripcion:", 20, 58, 130, 16));
            model.addWidget(textField("txtDescripcion", 160, 55, 200, 26));
            model.addWidget(label("lblCategoria", "Categoria:", 20, 94, 130, 16));
            model.addWidget(comboBox("cmbCategoria", "Basico, Intermedio, Avanzado",
                    160, 91, 200, 26));
            model.addWidget(button("btnCrear", "Crear", 420, 19, 120, 26));
            model.addWidget(button("btnLimpiar", "Limpiar", 420, 55, 120, 26));
            model.addWidget(textArea("txtSalida", 20, 132, 520, 200));
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
