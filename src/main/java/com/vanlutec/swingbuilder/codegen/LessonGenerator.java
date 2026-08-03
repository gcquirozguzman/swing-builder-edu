package com.vanlutec.swingbuilder.codegen;

/**
 * El codigo de las lecciones del modo Aprendizaje.
 * <p>
 * No generan formulario: son clases con {@code main} y comentarios, para explicar un
 * tema en clase y que el alumno ejecute y toque. Todo el fichero es suyo; aqui no hay
 * zonas generadas ni nada que se reescriba despues.
 */
public final class LessonGenerator {

    private LessonGenerator() {
    }

    /**
     * Leccion "Variables y tipos".
     * <p>
     * Alcance deliberadamente estrecho: solo {@code int}, {@code double}, {@code String}
     * y {@code boolean}, y solo declarar, mostrar y convertir. <b>Nada de operaciones
     * aritmeticas</b>: eso es otra clase, y mezclarlo aqui desvia la atencion del tema.
     */
    public static String variablesYTipos(String className, String packageName) {
        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }
        sb.append("""
                /**
                 * Variables y tipos en Java.
                 * <p>
                 * Una variable es una caja con nombre donde guardas un dato. El tipo dice
                 * que clase de dato cabe dentro. Vamos a ver cuatro:
                 * <ul>
                 *   <li>int     - numeros enteros</li>
                 *   <li>double  - numeros con decimales</li>
                 *   <li>String  - texto</li>
                 *   <li>boolean - verdadero o falso</li>
                 * </ul>
                 * Ejecuta esta clase (flecha verde junto a main) y ve leyendo la salida
                 * mientras sigues los comentarios. Cambia valores y vuelve a ejecutar:
                 * equivocarse aqui no rompe nada.
                 *
                 * Cibertec - Swing Builder Edu
                 */
                public class %CLASE% {

                    // Una constante: lleva "final" y por convencion se escribe en MAYUSCULAS.
                    // Es una variable cuyo valor ya no se puede cambiar. Si lo intentas mas
                    // abajo, el compilador no te deja.
                    static final String INSTITUTO = "Cibertec";

                    public static void main(String[] args) {

                        // ---------------------------------------------------------------
                        // 1. int  -  numeros enteros
                        // ---------------------------------------------------------------
                        // Para edades, cantidades, contadores... nada de decimales.
                        //
                        // Se declara asi:   tipo  nombre = valor;
                        int edad = 20;
                        int cantidad = 3;

                        System.out.println("== int ==");
                        System.out.println("edad     = " + edad);
                        System.out.println("cantidad = " + cantidad);

                        // Una variable se puede cambiar despues (por eso se llama variable).
                        edad = 21;
                        System.out.println("edad, despues de cambiarla = " + edad);

                        // ---------------------------------------------------------------
                        // 2. double  -  numeros con decimales
                        // ---------------------------------------------------------------
                        // Para precios, notas, medidas... El separador es el PUNTO, no la coma.
                        double precio = 149.90;
                        double nota = 15.5;

                        System.out.println();
                        System.out.println("== double ==");
                        System.out.println("precio = " + precio);
                        System.out.println("nota   = " + nota);

                        // Un int cabe en un double, asi que esto vale:
                        double copia = edad;
                        System.out.println("un int guardado en un double = " + copia);

                        // Al reves NO: descomenta la linea siguiente y lee el error.
                        // int mal = precio;

                        // ---------------------------------------------------------------
                        // 3. String  -  texto
                        // ---------------------------------------------------------------
                        // Va entre comillas DOBLES. No es un tipo primitivo: es una clase,
                        // por eso empieza en mayuscula y tiene metodos.
                        String nombre = "Gian Carlo";
                        String curso = "Programacion en Java";

                        System.out.println();
                        System.out.println("== String ==");
                        System.out.println("nombre        = " + nombre);
                        System.out.println("curso         = " + curso);
                        System.out.println("longitud      = " + nombre.length());
                        System.out.println("en mayusculas = " + nombre.toUpperCase());

                        // El + entre textos los une. A esto se le llama concatenar.
                        String saludo = "Hola, " + nombre + "! Bienvenido a " + INSTITUTO + ".";
                        System.out.println(saludo);

                        // ---------------------------------------------------------------
                        // 4. boolean  -  verdadero o falso
                        // ---------------------------------------------------------------
                        // Solo admite dos valores: true o false. Sin comillas.
                        boolean matriculado = true;
                        boolean beca = false;

                        System.out.println();
                        System.out.println("== boolean ==");
                        System.out.println("matriculado = " + matriculado);
                        System.out.println("beca        = " + beca);

                        // ---------------------------------------------------------------
                        // 5. CONVERTIR DE UN TIPO A OTRO
                        // ---------------------------------------------------------------
                        // Esto es lo que hara falta en los formularios: lo que el usuario
                        // escribe en un JTextField SIEMPRE llega como String, aunque haya
                        // tecleado un numero.
                        System.out.println();
                        System.out.println("== Conversiones ==");

                        String textoEdad = "42";
                        int edadConvertida = Integer.parseInt(textoEdad);
                        System.out.println("el texto \\"42\\" convertido a int    = " + edadConvertida);

                        String textoPrecio = "3.75";
                        double precioConvertido = Double.parseDouble(textoPrecio);
                        System.out.println("el texto \\"3.75\\" convertido a double = " + precioConvertido);

                        // Al reves: de numero a texto.
                        String edadComoTexto = String.valueOf(edadConvertida);
                        System.out.println("el int 42 como texto ocupa " + edadComoTexto.length() + " caracteres");

                        // De double a int hay que pedirlo a mano, y se PIERDEN los decimales
                        // (no redondea: los corta).
                        int precioSinDecimales = (int) precio;
                        System.out.println(precio + " convertido a int = " + precioSinDecimales);
                    }
                }
                """.replace("%CLASE%", className));
        return sb.toString();
    }

    /**
     * Leccion "Operaciones con variables".
     * <p>
     * Continua donde termina {@link #variablesYTipos}: los mismos cuatro tipos, pero
     * ahora operando con ellos. Aritmetica, comparaciones y logica.
     */
    public static String operaciones(String className, String packageName) {
        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }
        sb.append("""
                /**
                 * Operaciones con variables.
                 * <p>
                 * Ya sabes declarar int, double, String y boolean. Ahora toca operar:
                 * <ul>
                 *   <li>aritmeticas  - sumar, restar, multiplicar, dividir</li>
                 *   <li>comparaciones - dan como resultado un boolean</li>
                 *   <li>logicas      - combinan booleanos</li>
                 * </ul>
                 * Ejecuta la clase y ve comparando la salida con los comentarios. Hay dos
                 * trampas clasicas marcadas con OJO: leelas despacio.
                 *
                 * Cibertec - Swing Builder Edu
                 */
                public class %CLASE% {

                    public static void main(String[] args) {

                        // ---------------------------------------------------------------
                        // 1. LAS CUATRO DE SIEMPRE
                        // ---------------------------------------------------------------
                        int a = 7;
                        int b = 2;

                        System.out.println("== Aritmetica con int ==");
                        System.out.println("a = " + a + ", b = " + b);
                        System.out.println("a + b = " + (a + b));
                        System.out.println("a - b = " + (a - b));
                        System.out.println("a * b = " + (a * b));
                        System.out.println("a / b = " + (a / b));

                        // OJO (1): al dividir dos int el resultado tambien es int, asi que
                        // 7 / 2 da 3 y no 3.5. Los decimales no se redondean: se tiran.
                        // Para que salga 3.5, al menos uno tiene que ser double:
                        System.out.println("a / 2.0 = " + (a / 2.0));

                        // El resto de la division. Sirve, por ejemplo, para saber si un
                        // numero es par: si el resto de dividir entre 2 es 0, lo es.
                        System.out.println("a % b = " + (a % b));

                        // ---------------------------------------------------------------
                        // 2. CON DECIMALES
                        // ---------------------------------------------------------------
                        double precio = 149.90;
                        int cantidad = 3;

                        double subtotal = precio * cantidad;
                        double igv = subtotal * 0.18;
                        double total = subtotal + igv;

                        System.out.println();
                        System.out.println("== Aritmetica con double ==");
                        System.out.println("subtotal = " + subtotal);
                        System.out.println("igv      = " + igv);
                        System.out.println("total    = " + total);

                        // ---------------------------------------------------------------
                        // 3. LOS PARENTESIS MANDAN
                        // ---------------------------------------------------------------
                        // Sin parentesis, primero se multiplica y divide, y despues se
                        // suma y resta. Las dos lineas siguientes NO dan lo mismo.
                        System.out.println();
                        System.out.println("== Prioridad ==");
                        System.out.println("2 + 3 * 4   = " + (2 + 3 * 4));     // 14
                        System.out.println("(2 + 3) * 4 = " + ((2 + 3) * 4));   // 20

                        // ---------------------------------------------------------------
                        // 4. ATAJOS PARA CAMBIAR UNA VARIABLE
                        // ---------------------------------------------------------------
                        int contador = 10;
                        System.out.println();
                        System.out.println("== Atajos ==");
                        System.out.println("contador empieza en " + contador);

                        contador = contador + 5;   // la forma larga
                        System.out.println("despues de contador = contador + 5 -> " + contador);

                        contador += 5;             // exactamente lo mismo, mas corto
                        System.out.println("despues de contador += 5           -> " + contador);

                        contador++;                // sumar 1
                        System.out.println("despues de contador++              -> " + contador);

                        contador--;                // restar 1
                        System.out.println("despues de contador--              -> " + contador);

                        // ---------------------------------------------------------------
                        // 5. COMPARACIONES  ->  dan un boolean
                        // ---------------------------------------------------------------
                        int nota = 15;

                        System.out.println();
                        System.out.println("== Comparaciones ==");
                        System.out.println("nota = " + nota);
                        System.out.println("nota > 12   = " + (nota > 12));
                        System.out.println("nota < 12   = " + (nota < 12));
                        System.out.println("nota >= 15  = " + (nota >= 15));
                        System.out.println("nota == 15  = " + (nota == 15));   // == es comparar
                        System.out.println("nota != 15  = " + (nota != 15));

                        // OJO (2): un solo = ASIGNA, dos == COMPARAN. Es el error mas
                        // repetido de todo el curso.
                        boolean aprobado = nota >= 12;
                        System.out.println("aprobado = " + aprobado);

                        // ---------------------------------------------------------------
                        // 6. OPERADORES LOGICOS  ->  combinan booleanos
                        // ---------------------------------------------------------------
                        boolean tieneBeca = false;

                        System.out.println();
                        System.out.println("== Logicos ==");
                        // &&  (Y):  cierto solo si LOS DOS son ciertos
                        System.out.println("aprobado && tieneBeca = " + (aprobado && tieneBeca));
                        // ||  (O):  cierto si AL MENOS UNO es cierto
                        System.out.println("aprobado || tieneBeca = " + (aprobado || tieneBeca));
                        // !   (NO): le da la vuelta
                        System.out.println("!aprobado             = " + (!aprobado));

                        // ---------------------------------------------------------------
                        // 7. EL + CON TEXTO NO SUMA: UNE
                        // ---------------------------------------------------------------
                        System.out.println();
                        System.out.println("== El + con String ==");
                        System.out.println("1 + 2         = " + (1 + 2));        // 3, suma
                        System.out.println("\\"1\\" + 2       = " + ("1" + 2));      // 12, une

                        // En cuanto aparece un String, todo lo que sigue se une como texto.
                        // Por eso conviene poner parentesis a las cuentas dentro de un
                        // println, como se ha hecho en toda esta clase.
                        System.out.println("sin parentesis: " + 1 + 2);
                        System.out.println("con parentesis: " + (1 + 2));
                    }
                }
                """.replace("%CLASE%", className));
        return sb.toString();
    }

    /**
     * Leccion "Condicionales".
     * <p>
     * Va detras de {@link #operaciones}: alli se ve que una comparacion da un
     * {@code boolean}; aqui se usa ese boolean para decidir. {@code if}, {@code else if},
     * {@code else} y {@code switch}.
     */
    public static String condicionales(String className, String packageName) {
        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }
        sb.append("""
                /**
                 * Condicionales: if, else if y switch.
                 * <p>
                 * Hasta ahora el programa hacia siempre lo mismo, de arriba abajo. Con un
                 * condicional el programa <b>decide</b>: ejecuta unas lineas u otras segun
                 * se cumpla algo.
                 * <p>
                 * Lo que va dentro del parentesis de un if tiene que ser un boolean, que es
                 * justo lo que devuelven las comparaciones de la clase anterior.
                 *
                 * Cibertec - Swing Builder Edu
                 */
                public class %CLASE% {

                    public static void main(String[] args) {

                        // ---------------------------------------------------------------
                        // 1. if  -  hazlo solo si se cumple
                        // ---------------------------------------------------------------
                        int nota = 15;

                        System.out.println("== if ==");
                        System.out.println("nota = " + nota);

                        if (nota >= 12) {
                            // Estas lineas solo se ejecutan si la condicion es true.
                            System.out.println("Aprobaste");
                        }

                        // Fijate: dentro del if() va una comparacion, que da un boolean.
                        // Tambien vale un boolean guardado en una variable:
                        boolean aprobado = nota >= 12;
                        if (aprobado) {
                            System.out.println("(lo mismo, usando la variable aprobado)");
                        }

                        // ---------------------------------------------------------------
                        // 2. if / else  -  o una cosa, o la otra
                        // ---------------------------------------------------------------
                        System.out.println();
                        System.out.println("== if / else ==");

                        if (nota >= 12) {
                            System.out.println("APROBADO");
                        } else {
                            // Se ejecuta cuando la condicion NO se cumple.
                            System.out.println("DESAPROBADO");
                        }

                        // ---------------------------------------------------------------
                        // 3. else if  -  varios casos en cadena
                        // ---------------------------------------------------------------
                        // Se comprueban de arriba abajo y SE PARA en el primero que se
                        // cumple. Por eso el orden importa: si pusieras nota >= 11 arriba
                        // del todo, nunca se llegaria a las notas altas.
                        System.out.println();
                        System.out.println("== else if ==");

                        if (nota >= 18) {
                            System.out.println("Excelente");
                        } else if (nota >= 15) {
                            System.out.println("Notable");
                        } else if (nota >= 12) {
                            System.out.println("Aprobado justo");
                        } else {
                            System.out.println("Desaprobado");
                        }

                        // ---------------------------------------------------------------
                        // 4. CONDICIONES COMBINADAS
                        // ---------------------------------------------------------------
                        // Con && y || de la clase anterior se piden dos cosas a la vez.
                        int asistencia = 80;

                        System.out.println();
                        System.out.println("== Condiciones combinadas ==");
                        System.out.println("asistencia = " + asistencia);

                        if (nota >= 12 && asistencia >= 70) {
                            System.out.println("Aprobado: nota suficiente Y asistencia suficiente");
                        } else {
                            System.out.println("No aprobado: falla la nota o la asistencia");
                        }

                        // ---------------------------------------------------------------
                        // 5. switch  -  cuando comparas UNA variable con valores exactos
                        // ---------------------------------------------------------------
                        // Es mas legible que una cadena larga de else if cuando siempre
                        // comparas lo mismo contra valores concretos.
                        //
                        // Hay DOS formas de escribirlo. Vamos a ver la clasica primero,
                        // porque es la que te vas a encontrar en libros y en codigo antiguo.

                        // -- FORMA CLASICA:  case valor:  ...  break;  ------------------
                        int dia = 3;

                        System.out.println();
                        System.out.println("== switch, forma clasica (con : y break) ==");
                        System.out.println("dia = " + dia);

                        switch (dia) {
                            case 1:
                                System.out.println("Lunes");
                                break;          // el break corta aqui
                            case 2:
                                System.out.println("Martes");
                                break;
                            case 3:
                                System.out.println("Miercoles");
                                break;
                            // default es el "y si no es ninguno de los anteriores".
                            default:
                                System.out.println("Otro dia");
                        }

                        // OJO (3): si te olvidas del break, el programa NO se para ahi:
                        // sigue ejecutando los casos siguientes. Se llama "caida en
                        // cascada" y no da ningun error, simplemente hace algo raro.
                        // Ejecuta esto y miralo: opcion vale 1, pero imprime tres lineas.
                        int opcion = 1;

                        System.out.println();
                        System.out.println("== Que pasa si falta el break ==");
                        System.out.println("opcion = " + opcion);

                        switch (opcion) {
                            case 1:
                                System.out.println("Caso 1");
                                // aqui falta el break, a proposito
                            case 2:
                                System.out.println("Caso 2  <- no deberia salir");
                                // y aqui tambien
                            case 3:
                                System.out.println("Caso 3  <- tampoco");
                                break;
                        }

                        // -- FORMA MODERNA:  case valor -> ...  --------------------------
                        // Con flecha no hay caida en cascada, asi que no hace falta break.
                        // Es la forma recomendada desde Java 14. Usala tu siempre.
                        String categoria = "B";

                        System.out.println();
                        System.out.println("== switch, forma moderna (con ->) ==");
                        System.out.println("categoria = " + categoria);

                        switch (categoria) {
                            case "A" -> System.out.println("Descuento del 20%");
                            case "B" -> System.out.println("Descuento del 10%");
                            case "C" -> System.out.println("Sin descuento");
                            default -> System.out.println("Categoria desconocida");
                        }

                        // Varios valores pueden compartir la misma respuesta.
                        int mes = 12;

                        System.out.println();
                        System.out.println("mes = " + mes);
                        switch (mes) {
                            case 12, 1, 2 -> System.out.println("Verano");
                            case 3, 4, 5 -> System.out.println("Otono");
                            case 6, 7, 8 -> System.out.println("Invierno");
                            default -> System.out.println("Primavera");
                        }

                        // Un switch tambien puede devolver un valor directamente.
                        String estacion = switch (mes) {
                            case 12, 1, 2 -> "Verano";
                            case 3, 4, 5 -> "Otono";
                            case 6, 7, 8 -> "Invierno";
                            default -> "Primavera";
                        };
                        System.out.println("guardado en una variable: " + estacion);

                        // ---------------------------------------------------------------
                        // 6. CUANDO USAR CADA UNO
                        // ---------------------------------------------------------------
                        // if / else if  -> cuando las condiciones son RANGOS o mezclan
                        //                  variables distintas  (nota >= 12 && asistencia >= 70)
                        // switch        -> cuando comparas UNA variable con valores exactos
                        //                  (dia == 1, dia == 2, dia == 3...)
                    }
                }
                """.replace("%CLASE%", className));
        return sb.toString();
    }

    /**
     * Leccion "Metodos".
     * <p>
     * Las cuatro combinaciones posibles, una por una y en orden de dificultad: sin
     * parametros y sin retorno, con parametros, con retorno, y las dos cosas. Nada de
     * sobrecarga ni recursividad: eso es otra clase.
     */
    public static String metodos(String className, String packageName) {
        StringBuilder sb = new StringBuilder();
        if (packageName != null && !packageName.isBlank()) {
            sb.append("package ").append(packageName).append(";\n\n");
        }
        sb.append("""
                /**
                 * Metodos.
                 * <p>
                 * Un metodo es un trozo de codigo con nombre. Lo escribes una vez y lo
                 * usas (lo "llamas") todas las veces que quieras.
                 * <p>
                 * Un metodo puede recibir datos y puede devolver un resultado. De ahi
                 * salen cuatro combinaciones, y son las cuatro que vas a ver aqui:
                 * <ol>
                 *   <li>sin datos y sin resultado</li>
                 *   <li>con datos y sin resultado</li>
                 *   <li>sin datos y con resultado</li>
                 *   <li>con datos y con resultado</li>
                 * </ol>
                 *
                 * Cibertec - Swing Builder Edu
                 */
                public class %CLASE% {

                    // main es el metodo por el que empieza el programa. Desde aqui vamos
                    // llamando a los demas, que estan escritos mas abajo.
                    public static void main(String[] args) {

                        // ---------------------------------------------------------------
                        // 1. SIN DATOS Y SIN RESULTADO
                        // ---------------------------------------------------------------
                        // Se llama escribiendo su nombre y (). Hace lo suyo y ya esta.
                        System.out.println("== 1. Sin datos y sin resultado ==");
                        saludar();
                        saludar();   // se puede llamar las veces que quieras

                        // ---------------------------------------------------------------
                        // 2. CON DATOS Y SIN RESULTADO
                        // ---------------------------------------------------------------
                        // Entre los parentesis le pasamos un dato. El metodo lo usa.
                        System.out.println();
                        System.out.println("== 2. Con datos y sin resultado ==");
                        saludarA("Ana");
                        saludarA("Luis");

                        // El dato puede venir de una variable.
                        String nombre = "Gian Carlo";
                        saludarA(nombre);

                        // ---------------------------------------------------------------
                        // 3. SIN DATOS Y CON RESULTADO
                        // ---------------------------------------------------------------
                        // Devuelve un valor, asi que lo podemos guardar en una variable.
                        System.out.println();
                        System.out.println("== 3. Sin datos y con resultado ==");

                        int anio = obtenerAnio();
                        System.out.println("el metodo devolvio: " + anio);

                        // O usarlo directamente, sin guardarlo.
                        System.out.println("y otra vez, sin guardarlo: " + obtenerAnio());

                        // ---------------------------------------------------------------
                        // 4. CON DATOS Y CON RESULTADO
                        // ---------------------------------------------------------------
                        // Le pasamos datos y nos devuelve algo calculado a partir de ellos.
                        System.out.println();
                        System.out.println("== 4. Con datos y con resultado ==");

                        int suma = sumar(3, 4);
                        System.out.println("sumar(3, 4) devolvio " + suma);
                        System.out.println("sumar(10, 25) devolvio " + sumar(10, 25));

                        // Los datos que le pasas pueden ser variables.
                        int a = 8;
                        int b = 5;
                        System.out.println("sumar(a, b) devolvio " + sumar(a, b));

                        // Y el resultado se puede usar dentro de otra llamada.
                        System.out.println("sumar(sumar(1, 2), 3) devolvio " + sumar(sumar(1, 2), 3));

                        // Un metodo que devuelve boolean se puede usar en un if.
                        System.out.println();
                        System.out.println("== Un metodo dentro de un if ==");
                        int nota = 15;
                        if (estaAprobado(nota)) {
                            System.out.println("con " + nota + " estas aprobado");
                        } else {
                            System.out.println("con " + nota + " estas desaprobado");
                        }
                    }

                    // ===============================================================
                    // LOS METODOS
                    // ===============================================================
                    // Todos llevan "static" para poder llamarlos desde main tal cual.
                    // Ya veras mas adelante que significa; por ahora, ponlo siempre.

                    /**
                     * 1. SIN DATOS Y SIN RESULTADO.
                     *
                     * void = "no devuelve nada".
                     * ()   = "no recibe nada".
                     */
                    static void saludar() {
                        System.out.println("Hola!");
                    }

                    /**
                     * 2. CON DATOS Y SIN RESULTADO.
                     *
                     * Entre los parentesis va lo que recibe: su tipo y su nombre.
                     * A eso se le llama parametro. Dentro del metodo, "nombre" se usa
                     * como cualquier otra variable.
                     */
                    static void saludarA(String nombre) {
                        System.out.println("Hola, " + nombre + "!");
                    }

                    /**
                     * 3. SIN DATOS Y CON RESULTADO.
                     *
                     * Donde antes ponia void, ahora pone int: eso es lo que devuelve.
                     * Y dentro tiene que haber un "return" con un int.
                     */
                    static int obtenerAnio() {
                        return 2026;
                    }

                    /**
                     * 4. CON DATOS Y CON RESULTADO.
                     *
                     * Recibe dos int y devuelve un int. Fijate en el orden al llamarlo:
                     * sumar(3, 4) hace que a valga 3 y b valga 4.
                     */
                    static int sumar(int a, int b) {
                        int resultado = a + b;
                        return resultado;

                        // Lo de arriba se puede escribir en una sola linea:
                        //     return a + b;
                        // Nada de lo que pongas DESPUES de un return se ejecuta: el
                        // metodo termina justo ahi.
                    }

                    /**
                     * Otro con resultado, pero devolviendo boolean.
                     * Los metodos que devuelven boolean suelen llamarse "es..." o "tiene..."
                     * y encajan de maravilla dentro de un if.
                     */
                    static boolean estaAprobado(int nota) {
                        return nota >= 12;
                    }
                }
                """.replace("%CLASE%", className));
        return sb.toString();
    }
}
