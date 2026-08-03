package com.vanlutec.swingbuilder.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormTemplateTest {

    @Test
    void todasLasPlantillasSonUsables() {
        for (FormTemplate template : FormTemplate.values()) {
            FormModel model = template.createModel("MiFormulario");

            assertEquals("MiFormulario", model.getClassName());
            assertFalse(template.getTitulo().isBlank());
            assertFalse(template.getDescripcion().isBlank());

            Set<String> nombres = new HashSet<>();
            for (WidgetModel widget : model.getWidgets()) {
                assertTrue(nombres.add(widget.getName()),
                        template + " repite el nombre " + widget.getName());
                assertEquals(widget.getName(), FormModel.sanitizeIdentifier(widget.getName()),
                        template + ": " + widget.getName() + " no es un identificador Java");
            }
        }
    }

    @Test
    void cadaModoOfreceSusPlantillas() {
        for (FormCategory categoria : FormCategory.values()) {
            FormTemplate[] suyas = categoria.getTemplates();
            assertTrue(suyas.length > 0, categoria + " se quedaria sin plantillas que ofrecer");
            for (FormTemplate template : suyas) {
                assertEquals(categoria, template.getCategoria());
            }
        }
        // Entre los dos modos estan todas, sin repetir.
        assertEquals(FormTemplate.values().length,
                FormCategory.DESARROLLO.getTemplates().length + FormCategory.APRENDIZAJE.getTemplates().length);
    }

    @Test
    void losNombresDeLasPlantillasNoSeRepiten() {
        Set<String> titulos = new HashSet<>();
        for (FormTemplate template : FormTemplate.values()) {
            assertTrue(titulos.add(template.getTitulo()), "titulo repetido: " + template.getTitulo());
        }
    }

    @Test
    void todasLasPlantillasMidenLoMismo() {
        for (FormTemplate template : FormTemplate.values()) {
            FormModel model = template.createModel("X");
            assertEquals(FormTemplate.ANCHO, model.getFrameWidth(), template.toString());
            assertEquals(FormTemplate.ALTO, model.getFrameHeight(), template.toString());
        }
    }

    @Test
    void losComponentesCabenDentroDelFormulario() {
        for (FormTemplate template : FormTemplate.values()) {
            FormModel model = template.createModel("X");
            int alto = model.getFrameHeight() - 26;  // el disenador reserva la barra de titulo
            for (WidgetModel widget : model.getWidgets()) {
                assertTrue(widget.getX() >= 0 && widget.getY() >= 0, template + ": fuera por arriba/izquierda");
                assertTrue(widget.getX() + widget.getWidth() <= model.getFrameWidth(),
                        template + ": " + widget.getName() + " se sale por la derecha");
                assertTrue(widget.getY() + widget.getHeight() <= alto,
                        template + ": " + widget.getName() + " se sale por abajo");
            }
        }
    }

    @Test
    void elVacioEstaVacioYLosDemasNo() {
        assertTrue(FormTemplate.VACIO.createModel("X").getWidgets().isEmpty());
        for (FormTemplate template : FormTemplate.values()) {
            if (template != FormTemplate.VACIO) {
                assertFalse(template.createModel("X").getWidgets().isEmpty(), template.toString());
            }
        }
    }

    @Test
    void laPlantillaSobreviveAlIrYVolverDelXml() {
        for (FormTemplate template : FormTemplate.values()) {
            FormModel original = template.createModel("MiFormulario");
            FormModel leido = FormModelIO.fromXml(FormModelIO.toXml(original), "MiFormulario");

            assertEquals(original.getWidgets().size(), leido.getWidgets().size(), template.toString());
            for (int i = 0; i < original.getWidgets().size(); i++) {
                WidgetModel esperado = original.getWidgets().get(i);
                WidgetModel actual = leido.getWidgets().get(i);
                assertEquals(esperado.getName(), actual.getName());
                assertEquals(esperado.getType(), actual.getType());
                assertEquals(esperado.getText(), actual.getText());
                assertEquals(esperado.getBounds(), actual.getBounds());
                assertEquals(esperado.isLineWrap(), actual.isLineWrap());
            }
        }
    }
}
