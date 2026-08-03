package com.vanlutec.swingbuilder.ui;

import com.vanlutec.swingbuilder.model.FormTheme;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * El canvas quiere componentes inertes (los eventos son de la capa de seleccion) y la
 * vista previa los quiere vivos. Confundir los dos deja los JTextField sin poder escribir.
 */
class WidgetRendererTest {

    /** Los colores no influyen en el foco: cualquier tema vale para estas pruebas. */
    private static final FormTheme TEMA = FormTheme.SISTEMA;

    private static WidgetModel widget(WidgetType type) {
        return new WidgetModel(type, "x", 0, 0);
    }

    @Test
    void enLaVistaPreviaSePuedeEscribirEnElTextField() {
        WidgetRenderer viva = WidgetRenderer.createInteractive(widget(WidgetType.TEXT_FIELD), TEMA);

        assertTrue(viva.inner().isFocusable());
        assertTrue(viva.inner().isRequestFocusEnabled(),
                "sin requestFocusEnabled el clic del raton no da el foco: no se puede escribir");
    }

    @Test
    void enLaVistaPreviaTambienSeEscribeEnElTextAreaDentroDelScroll() {
        WidgetRenderer viva = WidgetRenderer.createInteractive(widget(WidgetType.TEXT_AREA), TEMA);

        // El fallo original: se reactivaba el JScrollPane (outer) y no el JTextArea (inner).
        assertTrue(viva.inner().isFocusable());
        assertTrue(viva.inner().isRequestFocusEnabled());
    }

    @Test
    void enElCanvasLosComponentesSonInertes() {
        for (WidgetType type : WidgetType.values()) {
            WidgetRenderer inerte = WidgetRenderer.create(widget(type), TEMA);
            assertFalse(inerte.outer().isFocusable(), type + " no deberia coger el foco en el canvas");
            assertFalse(inerte.inner().isFocusable(), type + " (interior) tampoco");
        }
    }

    @Test
    void laInerciaDelCanvasBajaHastaLosHijos() {
        WidgetRenderer inerte = WidgetRenderer.create(widget(WidgetType.TEXT_AREA), TEMA);

        assertFalse(inerte.outer().isFocusable());
        assertFalse(inerte.inner().isRequestFocusEnabled());
    }
}
