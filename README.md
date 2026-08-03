# Swing Builder Edu

Plugin de IntelliJ IDEA: un **diseñador visual de GUIs Swing** con la apariencia y el
flujo de trabajo de WindowBuilder, pensado para clases de programación en Java.

![Java](https://img.shields.io/badge/Java-21-blue)
![IntelliJ](https://img.shields.io/badge/IntelliJ%20IDEA-2024.3%2B-orange)

## Qué hace

- **Paleta** a la izquierda con `JLabel`, `JTextField`, `JButton`, `JComboBox` y `JTextArea`.
- **Canvas WYSIWYG** en el centro: arrastrar y soltar, selección con tiradores de
  redimensionado y guías de alineación.
- **Tabla de propiedades** a la derecha (`name`, `text`, `font`, `bounds`…), editable en vivo.
- **Doble clic sobre un JButton** → genera su `ActionListener` en la clase Java y te
  lleva al código:

  ```java
  btnProcesar.addActionListener(e -> {
      // TODO: accion aqui
  });
  ```

- **Plantillas** al crear el formulario, en dos modos: *Desarrollo* y *Aprendizaje*
  (una plantilla por tema del curso: variables, condicionales, bucles, arreglos, objetos).
- **Temas** de color: Sistema, Claro, Oscuro, Alto contraste y Terminal.

## Instalación

Requiere IntelliJ IDEA 2024.3 o superior (Community o Ultimate).

### Recomendada: añadir el repositorio (se actualiza solo)

Se hace **una sola vez** y a partir de ahí las nuevas versiones llegan como las de
cualquier otro plugin.

1. **Settings → Plugins → ⚙ → Manage Plugin Repositories…**
2. Pulsa **+** y pega:

   ```
   https://gcquirozguzman.github.io/swing-builder-edu/updatePlugins.xml
   ```

3. Busca **Swing Builder Edu** en la pestaña **Marketplace** e instálalo.

### Alternativa: instalar el ZIP a mano

Descarga el `.zip` de [la última versión](https://github.com/gcquirozguzman/swing-builder-edu/releases/latest)
y usa **Settings → Plugins → ⚙ → Install Plugin from Disk…**

No recibirás avisos de actualización: habrá que repetirlo con cada versión.

## Publicar una versión nueva

La versión vive en `gradle.properties`. Para entregar una actualización:

```bash
# 1. sube la version
#    gradle.properties -> pluginVersion=0.2.0

# 2. commit y tag (el tag debe ser v + la misma version)
git commit -am "chore: bump version to 0.2.0"
git tag v0.2.0
git push && git push --tags
```

El workflow `.github/workflows/publicar.yml` se encarga del resto: compila, pasa las
pruebas, crea la Release con el ZIP y actualiza el `updatePlugins.xml` en GitHub Pages.
Si el tag no coincide con `pluginVersion`, la publicación se detiene antes de subir nada.

## Uso

1. Clic derecho en una carpeta de código → **New → Swing Form (Designer)**.
2. Elige nombre, modo y plantilla.
3. Diseña arrastrando desde la paleta.
4. Doble clic en un botón para escribir su código.
5. Ejecuta el `main` de la clase generada con la flecha verde ▶.

> La **Vista previa** muestra cómo quedará la ventana, pero no ejecuta tu código.
> Para probarlo, ejecuta el `main`.

## Compilar desde el código fuente

```bash
./gradlew runIde        # IDE de pruebas con el plugin instalado
./gradlew buildPlugin   # genera build/distributions/swing-builder-edu-<version>.zip
./gradlew test          # pruebas
```

### Qué JDK hace falta

Un **JDK 21 o superior**, pero dónde tiene que estar depende de cómo compiles:

- **Desde IntelliJ** (panel Gradle o el botón de las tareas): no necesitas configurar
  nada del sistema. El IDE usa su *Gradle JVM*
  (Settings → Build Tools → Gradle → Gradle JVM), y ahí vale el JBR que el propio
  IntelliJ trae incluido.
- **Desde la terminal** con `./gradlew`: el script lanzador busca un JDK antes de
  arrancar Gradle. Necesita `JAVA_HOME` apuntando a uno, o `java` en el `PATH`.

> Cuidado con un `JAVA_HOME` que apunte a una carpeta que ya no existe: en ese caso
> `gradlew` falla directamente, sin llegar a probar el `PATH`. Si te pasa, corrige la
> variable o bórrala.

El proyecto compila con el JDK que ejecute Gradle, sea cual sea, y genera bytecode 21
(`options.release`), así que no te obliga a instalar una versión concreta.

Los detalles de arquitectura están en [ARQUITECTURA.md](ARQUITECTURA.md).

## Autor

**Gian Carlo Quiroz** — [github.com/gcquirozguzman](https://github.com/gcquirozguzman)

Creado para las clases de Java de **Cibertec**.

## Licencia

Ver [LICENSE](LICENSE).
