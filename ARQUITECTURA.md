# Swing Builder Edu

Plugin de IntelliJ IDEA: un **diseñador visual de GUIs Swing** con la apariencia y el
flujo de trabajo de **WindowBuilder**, pensado para clases de programación en Java.

## Objetivo

Que un alumno pueda dibujar un formulario Swing y pasar de la interfaz al código sin
salir del IDE. El código Java generado no busca ser elegante: busca ser **legible para
alguien que está aprendiendo** y no romperse cuando el alumno escribe encima.

## Alcance

### Prioridad 1 — Apariencia (lo que se ve como WindowBuilder)

- **Panel izquierdo — Paleta.** Categoría plegable `Components` con exactamente cuatro
  elementos, cada uno con su icono: `JLabel`, `JTextField`, `JButton`, `JTextArea`.
  Lista vertical de icono + nombre.
- **Panel central — Canvas WYSIWYG.** Se dibuja el `JFrame` con sus componentes Swing
  *reales*. Arrastrar y soltar desde la paleta, selección con tiradores de
  redimensionado, guías de alineación, mover con las flechas, `Supr` para borrar.
- **Panel derecho — Propiedades.** Tabla del elemento seleccionado (`name`, `text`,
  `font`, `bounds`; el `JTextArea` añade `lineWrap`; para el `JFrame`: `class`, `title`,
  `size`), editable en vivo.

### Prioridad 2 — Doble clic para eventos

**Doble clic sobre un `JButton`** en el canvas crea su `ActionListener` en la clase Java
y lleva el cursor dentro:

```java
btnProcesar.addActionListener(e -> {
    // TODO: accion aqui
});
```

Si el listener ya existe, no se duplica: se salta a él. Doble clic sobre cualquier otro
componente edita su propiedad `text` en la tabla (igual que WindowBuilder).

### Fuera de alcance

Layouts que no sean `null` (absoluto), más componentes de los cuatro citados, menús,
paneles anidados, agrupar/alinear varios componentes a la vez y deshacer propio del
diseñador (se apoya en el del IDE).

## Cómo se usa

1. `New | Swing Form (Designer)` → diálogo con el nombre de la clase y la **plantilla**
   de partida (`FormTemplate`: vacío, botón + área de texto, entrada de datos).
2. Se crea `MiFormulario.sbe` y se abre en el diseñador.
3. Al primer cambio (o con el botón *Generar código Java*) aparece al lado
   `MiFormulario.java`, ejecutable con su propio `main`.

### Vista previa ≠ ejecutar

El botón *Vista previa* dibuja la ventana **a partir del diseño**, no de la clase
compilada: los botones no hacen nada porque el código del alumno no interviene. Para
probar el código hay que ejecutar el `main` de la clase generada. El diálogo lo dice
explícitamente, porque el icono de "play" invita a confundirse.

## Arquitectura

```
model/      FormModel, WidgetModel, WidgetType   el diseño, sin nada de UI
            FormModelIO                          .sbe <-> modelo (XML legible)
            FormTemplate                         puntos de partida del diálogo "New"
ui/         PalettePanel, DesignCanvas,          los tres paneles
            PropertiesPanel, WidgetRenderer
            NewFormDialog                        nombre de clase + plantilla
editor/     SwingDesignerEditor(+Provider)       pestaña "Design" del .sbe
codegen/    JavaFormGenerator                    modelo -> texto Java (lógica pura)
            FormJavaFile                         crea/actualiza el .java, PSI, navegación
actions/    NewSwingFormAction                   New | Swing Form (Designer)
```

### Dos ficheros por formulario

`MiFormulario.sbe` (XML del diseño, es lo que edita el diseñador) y
`MiFormulario.java` (la clase, es lo que edita el alumno). El `.sbe` es la fuente de
verdad del diseño; el `.java` se deriva de él.

El modelo vive dentro del `Document` del `.sbe`, así que **deshacer/rehacer del IDE y la
pestaña "Text" funcionan gratis**.

### Zonas generadas (la decisión importante)

El `.java` tiene dos zonas vigiladas por marcas de comentario:

```java
// >>> SBE-IMPORTS ... >>>   ...   // <<< SBE-IMPORTS <<<
    // >>> SBE-DISENO ... >>>  ...  // <<< SBE-DISENO <<<
```

Al regenerar **solo se reescribe lo que hay dentro de esas marcas** (y solo si cambió,
para no mover el cursor del alumno). Todo lo demás es zona del alumno y no se toca:
`initEventos()`, `main`, métodos propios, imports añadidos a mano.

Los `ActionListener` van en `initEventos()`, que está **fuera** de la zona generada y se
llama al final del constructor. Ése es el motivo de que exista ese método: sin él, la
siguiente regeneración se llevaría por delante el código que el alumno escribió dentro
del listener. Es el mismo reparto que usa NetBeans.

Si el `.java` no tiene las marcas (fichero escrito a mano), **no se toca nada**.

### El tamaño se le da al panel, no a la ventana

El generado hace `contentPane.setPreferredSize(...)` + `pack()`, nunca
`setBounds` sobre el `JFrame`. Con `setBounds`, el marco y la barra de título del sistema
se comen el área útil y todo aparece desplazado —  pegado a la derecha y abajo— respecto
a lo que se ve en el diseñador. El alto que se reserva para la barra de título (26 px) es
el mismo que dibuja el canvas, así que diseño y ejecución coinciden píxel a píxel.

## Comandos

| Comando | Para qué |
|---|---|
| `./gradlew runIde` | IDE en sandbox con el plugin instalado |
| `./gradlew buildPlugin` | `build/distributions/swing-builder-edu-<version>.zip` (Install Plugin from Disk) |
| `./gradlew test` | Pruebas del generador |
| `./gradlew verifyPluginStructure` | Comprueba `plugin.xml` y el empaquetado |

### JDK

Hace falta un **`JAVA_HOME` válido con JDK 21 o superior**; `gradlew` lo comprueba antes
que nada, así que un `JAVA_HOME` roto falla aunque `gradle.properties` diga otra cosa.
El toolchain de `build.gradle.kts` usa el JDK con el que corre Gradle (sea cual sea) y
genera bytecode 21 vía `options.release`, para no exigir una versión concreta instalada.

Si no hay JDK aparte, sirve el JBR que trae IntelliJ:
`C:/Program Files/JetBrains/IntelliJ IDEA <version>/jbr`.

## Pruebas

| Clase | Qué cubre |
|---|---|
| `JavaFormGeneratorTest` | La forma del código y, sobre todo, que **regenerar respeta el código del alumno** |
| `GeneratedCodeCompilesTest` | Pasa por **`javac`** lo que el plugin escribiría (formulario nuevo, con el listener del doble clic, regenerado, paquete por defecto) |
| `FormJavaFileTest` | El camino del **doble clic** con un proyecto de verdad: crear el `.java`, insertar el listener vía PSI, no duplicarlo, y sobrevivir a la regeneración |

Si el generador emite algo que no compila, se entera la suite y no el alumno en clase.

Dos rarezas del arranque de las pruebas, ya resueltas en `build.gradle.kts`: el runner de
la plataforma exige **JUnit 4** en el classpath aunque las pruebas sean JUnit 5 (de ahí el
motor *vintage*, que además ejecuta las de `BasePlatformTestCase`), y bajo ese runner
`ToolProvider.getSystemJavaCompiler()` devuelve `null`, así que las pruebas lanzan el
`javac` del JDK como proceso aparte.

## Limitaciones conocidas

- **Renombrar la clase** en las propiedades genera un `.java` nuevo y deja huérfano el
  anterior; hay que borrar el viejo a mano.
- **Renombrar un componente** que ya tenía listener deja el `addActionListener` con el
  nombre viejo en `initEventos()` (deja de compilar hasta corregirlo). Igual que pasa en
  WindowBuilder al renombrar sin refactorizar.
- Si el alumno borra `initEventos()`, el doble clic lo vuelve a crear, pero la llamada
  desde el constructor está en zona generada y se restaura sola.
