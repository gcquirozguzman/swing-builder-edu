package com.vanlutec.swingbuilder.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.vanlutec.swingbuilder.SwingFormFileType;
import org.jetbrains.annotations.NotNull;

/** Registra la pestana "Design" para los ficheros {@code .sbe}. */
public final class SwingDesignerEditorProvider implements FileEditorProvider, DumbAware {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return SwingFormFileType.EXTENSION.equalsIgnoreCase(file.getExtension());
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new SwingDesignerEditor(project, file);
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return "swing-builder-edu.designer";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        // "Design" primero, "Text" despues: igual que WindowBuilder (Design / Source).
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
    }
}
