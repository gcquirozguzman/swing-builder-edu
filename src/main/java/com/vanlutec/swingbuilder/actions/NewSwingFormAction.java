package com.vanlutec.swingbuilder.actions;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.AccessToken;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.util.SlowOperations;
import com.vanlutec.swingbuilder.SbeIcons;
import com.vanlutec.swingbuilder.SwingFormFileType;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.FormModelIO;
import com.vanlutec.swingbuilder.model.FormNames;
import com.vanlutec.swingbuilder.model.FormTemplate;
import com.vanlutec.swingbuilder.ui.NewFormDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Predicate;

/** "New | Swing Form (Designer)": crea el par {@code Nombre.sbe} + {@code Nombre.java}. */
public final class NewSwingFormAction extends AnAction implements DumbAware {

    public NewSwingFormAction() {
        super("Swing Form (Designer)", "Crea un formulario Swing y lo abre en el disenador", SbeIcons.SWING_FORM);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null && targetDirectory(e) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiDirectory directory = targetDirectory(e);
        if (project == null || directory == null) {
            return;
        }
        VirtualFile dir = directory.getVirtualFile();
        // Un nombre que no choque con lo que ya hay: MiFormulario, MiFormulario01, ...
        Predicate<String> ocupado = nombre -> dir.findChild(nombre + "." + SwingFormFileType.EXTENSION) != null
                || dir.findChild(nombre + ".java") != null;

        NewFormDialog dialog = new NewFormDialog(project, FormNames.suggest(FormNames.BASE, ocupado), ocupado);
        if (!dialog.showAndGet()) {
            return;
        }
        String name = dialog.getFormName();
        if (dir.findChild(name + "." + SwingFormFileType.EXTENSION) != null) {
            Messages.showErrorDialog(project, "Ya existe " + name + "." + SwingFormFileType.EXTENSION,
                    "Nuevo formulario Swing");
            return;
        }

        FormTemplate template = dialog.getTemplate();
        if (template.isSoloCodigo()) {
            // Modo Aprendizaje: no hay formulario ni .sbe, solo la clase con la leccion.
            crearFichero(project, dir, name + ".java",
                    template.javaSource(name, packageName(project, directory)));
            return;
        }

        FormModel model = template.createModel(name);
        model.setTheme(dialog.getTheme());
        crearFichero(project, dir, name + "." + SwingFormFileType.EXTENSION, FormModelIO.toXml(model));
    }

    /** Crea el fichero con ese contenido y lo abre. */
    private void crearFichero(Project project, VirtualFile dir, String nombre, String contenido) {
        WriteCommandAction.runWriteCommandAction(project, "Nuevo formulario Swing", "SwingBuilderEdu", () -> {
            try {
                VirtualFile creado = dir.createChildData(this, nombre);
                VfsUtil.saveText(creado, contenido);
                FileEditorManager.getInstance(project).openFile(creado, true);
            } catch (IOException ex) {
                Messages.showErrorDialog(project, "No se pudo crear " + nombre + ": " + ex.getMessage(),
                        "Nuevo formulario Swing");
            }
        });
    }

    /** El paquete de la carpeta destino, para la sentencia {@code package} de la leccion. */
    private static String packageName(Project project, PsiDirectory directory) {
        try (AccessToken ignored = SlowOperations.knownIssue("SBE: paquete al crear, en el EDT")) {
            PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(directory);
            return psiPackage == null ? "" : psiPackage.getQualifiedName();
        }
    }

    private static @Nullable PsiDirectory targetDirectory(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        if (view != null) {
            PsiDirectory[] directories = view.getDirectories();
            if (directories.length > 0) {
                return directories[0];
            }
        }
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file != null) {
            VirtualFile dir = file.isDirectory() ? file : file.getParent();
            if (dir != null) {
                return PsiManager.getInstance(project).findDirectory(dir);
            }
        }
        return null;
    }
}
