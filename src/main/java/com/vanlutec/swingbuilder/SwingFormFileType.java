package com.vanlutec.swingbuilder;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.nio.charset.StandardCharsets;

/**
 * El fichero de diseno: {@code MiFormulario.sbe}. Contiene un XML muy simple
 * (ver la pestana "Text" del editor) y se abre por defecto con el disenador.
 */
public final class SwingFormFileType implements FileType {

    public static final SwingFormFileType INSTANCE = new SwingFormFileType();
    public static final String EXTENSION = "sbe";

    private SwingFormFileType() {
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Swing Form";
    }

    @Override
    public @NotNull String getDescription() {
        return "Diseno de formulario Swing (Swing Builder Edu)";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return EXTENSION;
    }

    @Override
    public @Nullable Icon getIcon() {
        return SbeIcons.SWING_FORM;
    }

    @Override
    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public @Nullable String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
        return StandardCharsets.UTF_8.name();
    }
}
