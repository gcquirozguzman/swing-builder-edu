package com.vanlutec.swingbuilder.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void elVacioEstaVacioYLosDemasFormulariosNo() {
        assertTrue(FormTemplate.VACIO.createModel("X").getWidgets().isEmpty());
        for (FormTemplate template : FormTemplate.values()) {
            if (template != FormTemplate.VACIO && !template.isSoloCodigo()) {
                assertFalse(template.createModel("X").getWidgets().isEmpty(), template.toString());
            }
        }
    }

    @Test
    void lasPlantillasDeAprendizajeSonSoloCodigoYNoCreanFormulario() {
        for (FormTemplate template : FormCategory.APRENDIZAJE.getTemplates()) {
            assertTrue(template.isSoloCodigo(), template + " deberia ser solo codigo");
            assertTrue(template.createModel("X").getWidgets().isEmpty(),
                    template + " no deberia crear ningun componente");

            String fuente = template.javaSource("MiClase", "com.ejemplo");
            assertTrue(fuente.startsWith("package com.ejemplo;"), fuente);
            assertTrue(fuente.contains("public class MiClase {"), fuente);
            assertTrue(fuente.contains("public static void main(String[] args)"), fuente);
        }
    }

    @Test
    void lasPlantillasDeDesarrolloNoOfrecenCodigoSuelto() {
        for (FormTemplate template : FormCategory.DESARROLLO.getTemplates()) {
            assertFalse(template.isSoloCodigo(), template.toString());
            assertThrows(IllegalStateException.class, () -> template.javaSource("X", ""));
        }
    }

    @Test
    void laLeccionDeVariablesSoloUsaLosCuatroTiposDelCurso() {
        String fuente = FormTemplate.VARIABLES.javaSource("MiClase", "");

        for (String tipo : new String[]{"int ", "double ", "String ", "boolean "}) {
            assertTrue(fuente.contains(tipo), "falta " + tipo.trim());
        }
        // Los demas primitivos se dejan fuera a proposito: en la primera clase despistan.
        for (String fuera : new String[]{"byte ", "short ", "long ", "float ", "char "}) {
            assertFalse(fuente.contains(fuera), "no deberia aparecer " + fuera.trim());
        }
    }

    @Test
    void laLeccionDeVariablesNoEntraEnOperaciones() {
        String fuente = FormTemplate.VARIABLES.javaSource("MiClase", "");

        // La aritmetica es otra clase: aqui solo se declara, se muestra y se convierte.
        // (Nada de buscar " * " a secas: casaria con los asteriscos del javadoc.)
        for (String operacion : new String[]{"7 / 2", "7 % 2", "subtotal", "printf", "IGV"}) {
            assertFalse(fuente.contains(operacion), "sobra la operacion: " + operacion);
        }
    }

    @Test
    void laLeccionDeOperacionesSiOperaYSigueConLosCuatroTipos() {
        String fuente = FormTemplate.OPERACIONES.javaSource("MiClase", "");

        // Lo que sacamos de "Variables y tipos" tiene que estar aqui.
        for (String debe : new String[]{"a / 2.0", "a % b", "contador +=", "contador++",
                "nota >= 12", "&&", "||"}) {
            assertTrue(fuente.contains(debe), "falta: " + debe);
        }
        // Y se mantiene el mismo alcance de tipos que la leccion anterior.
        for (String fuera : new String[]{"byte ", "short ", "long ", "float ", "char "}) {
            assertFalse(fuente.contains(fuera), "no deberia aparecer " + fuera.trim());
        }
    }

    @Test
    void laLeccionDeCondicionalesCubreLasTresFormas() {
        String fuente = FormTemplate.CONDICIONALES.javaSource("MiClase", "");

        assertTrue(fuente.contains("if (nota >= 12)"), "falta el if");
        assertTrue(fuente.contains("} else if ("), "falta el else if");
        assertTrue(fuente.contains("} else {"), "falta el else");
        // Las dos sintaxis: la clasica para que la reconozcan en codigo ajeno,
        // la moderna como la que deben escribir ellos.
        assertTrue(fuente.contains("switch (dia)"), "falta el switch sobre un int");
        assertTrue(fuente.contains("case 1:"), "falta la forma clasica con dos puntos");
        assertTrue(fuente.contains("break;"), "la forma clasica necesita break");
        assertTrue(fuente.contains("switch (categoria)"), "falta el switch sobre un String");
        assertTrue(fuente.contains("case \"A\" ->"), "falta la forma moderna con flecha");
        assertTrue(fuente.contains("default ->"), "falta el default");
        // Se apoya en lo visto antes: comparaciones y operadores logicos.
        assertTrue(fuente.contains("&&"), "deberia enlazar con los logicos de la clase anterior");
    }

    @Test
    void laLeccionDeMetodosCubreLasCuatroCombinaciones() {
        String fuente = FormTemplate.METODOS.javaSource("MiClase", "");

        assertTrue(fuente.contains("static void saludar()"), "falta sin parametros y sin retorno");
        assertTrue(fuente.contains("static void saludarA(String nombre)"), "falta con parametros, sin retorno");
        assertTrue(fuente.contains("static int obtenerAnio()"), "falta sin parametros, con retorno");
        assertTrue(fuente.contains("static int sumar(int a, int b)"), "falta con parametros y con retorno");
        // Y que se vean llamados, no solo declarados.
        assertTrue(fuente.contains("saludarA(\"Ana\");"), "hay que verlo llamado");
        assertTrue(fuente.contains("sumar(3, 4)"), "hay que verlo llamado");
    }

    @Test
    void lasLeccionesNoLlevanBloqueDeEjercicios() {
        for (FormTemplate template : FormCategory.APRENDIZAJE.getTemplates()) {
            assertFalse(template.javaSource("MiClase", "").contains("TU TURNO"), template.toString());
        }
    }

    @Test
    void laLeccionSinPaqueteNoEmitePackage() {
        for (FormTemplate template : FormCategory.APRENDIZAJE.getTemplates()) {
            assertFalse(template.javaSource("MiClase", "").startsWith("package"), template.toString());
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
