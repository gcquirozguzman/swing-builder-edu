package com.vanlutec.swingbuilder.model;

/**
 * Los dos modos del dialogo de formulario nuevo.
 * <p>
 * No cambian lo que se genera: solo que plantillas se ofrecen.
 */
public enum FormCategory {

    DESARROLLO("Desarrollo",
            "Formularios de proposito general para empezar a construir."),

    APRENDIZAJE("Aprendizaje",
            "Formularios con un tema de Java cada uno, para practicar en clase.");

    private final String titulo;
    private final String descripcion;

    FormCategory(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /** Las plantillas de este modo, en orden. */
    public FormTemplate[] getTemplates() {
        return java.util.Arrays.stream(FormTemplate.values())
                .filter(template -> template.getCategoria() == this)
                .toArray(FormTemplate[]::new);
    }

    @Override
    public String toString() {
        return titulo;
    }
}
