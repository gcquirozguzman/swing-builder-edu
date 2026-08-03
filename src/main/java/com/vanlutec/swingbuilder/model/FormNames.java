package com.vanlutec.swingbuilder.model;

import java.util.function.Predicate;

/** Propone nombres de clase libres: {@code MiFormulario}, {@code MiFormulario01}, ... */
public final class FormNames {

    public static final String BASE = "MiFormulario";

    private FormNames() {
    }

    /**
     * El primer nombre libre a partir de {@code base}.
     * <p>
     * Si {@code base} esta libre se devuelve tal cual; si no, se le va pegando un numero
     * de dos cifras ({@code 01}, {@code 02}, ...) hasta encontrar hueco.
     *
     * @param ocupado dice si un nombre ya esta cogido (por un .sbe o un .java)
     */
    public static String suggest(String base, Predicate<String> ocupado) {
        String limpio = FormModel.sanitizeIdentifier(base);
        if (!ocupado.test(limpio)) {
            return limpio;
        }
        for (int i = 1; i < 1000; i++) {
            String candidato = limpio + String.format("%02d", i);
            if (!ocupado.test(candidato)) {
                return candidato;
            }
        }
        return limpio;
    }
}
