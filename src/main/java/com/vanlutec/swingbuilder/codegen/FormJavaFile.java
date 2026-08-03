package com.vanlutec.swingbuilder.codegen;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.SlowOperations;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * El fichero {@code .java} que acompana a un diseno {@code .sbe}: vive al lado, con
 * el nombre de la clase del formulario.
 * <p>
 * Se encarga de crearlo, de mantener al dia sus zonas generadas y de anadir los
 * {@code ActionListener} cuando el alumno hace doble clic en un boton.
 */
public final class FormJavaFile {

    private static final Logger LOG = Logger.getInstance(FormJavaFile.class);

    private final Project project;
    private final VirtualFile sbeFile;
    private String cachedPackageName;

    public FormJavaFile(@NotNull Project project, @NotNull VirtualFile sbeFile) {
        this.project = project;
        this.sbeFile = sbeFile;
    }

    /** El {@code .java} hermano, si ya existe. */
    public @Nullable VirtualFile find(FormModel model) {
        VirtualFile parent = sbeFile.getParent();
        return parent == null ? null : parent.findChild(model.getClassName() + ".java");
    }

    /**
     * Devuelve el {@code .java} del formulario: lo crea si no existe y, si ya
     * estaba, reescribe sus zonas generadas.
     */
    public @Nullable VirtualFile ensure(FormModel model) {
        VirtualFile existing = find(model);
        if (existing != null) {
            refresh(model, existing);
            return existing;
        }
        return create(model);
    }

    private @Nullable VirtualFile create(FormModel model) {
        VirtualFile parent = sbeFile.getParent();
        if (parent == null) {
            return null;
        }
        String source = JavaFormGenerator.newFile(model, packageName(parent));
        VirtualFile[] created = new VirtualFile[1];
        IOException[] failure = new IOException[1];
        WriteCommandAction.runWriteCommandAction(project, "Generar clase Java", "SwingBuilderEdu", () -> {
            try {
                VirtualFile file = parent.createChildData(this, model.getClassName() + ".java");
                VfsUtil.saveText(file, source);
                created[0] = file;
            } catch (IOException e) {
                failure[0] = e;
            }
        });
        if (failure[0] != null) {
            LOG.warn("No se pudo crear la clase Java del formulario", failure[0]);
            Messages.showErrorDialog(project,
                    "No se pudo crear " + model.getClassName() + ".java: " + failure[0].getMessage(),
                    "Swing Builder Edu");
        }
        return created[0];
    }

    /** Reescribe solo las zonas generadas; el codigo del alumno se queda como esta. */
    public void refresh(FormModel model, @NotNull VirtualFile javaFile) {
        Document document = FileDocumentManager.getInstance().getDocument(javaFile);
        if (document == null) {
            return;
        }
        String current = document.getText();
        List<JavaFormGenerator.Region> regions = JavaFormGenerator.regions(current, model);
        if (regions == null) {
            // Sin marcas: el fichero es del alumno de cabo a rabo, no lo tocamos.
            return;
        }
        List<JavaFormGenerator.Region> stale = regions.stream()
                .filter(region -> !region.isUpToDate(current))
                .toList();
        if (stale.isEmpty()) {
            return;
        }
        // Solo las zonas generadas: asi el cursor y el codigo del alumno se quedan donde estan.
        WriteCommandAction.runWriteCommandAction(project, "Actualizar el codigo del formulario",
                "SwingBuilderEdu", () -> {
                    for (JavaFormGenerator.Region region : stale) {
                        document.replaceString(region.start(), region.end(), region.body());
                    }
                });
    }

    public void open(FormModel model) {
        VirtualFile javaFile = ensure(model);
        if (javaFile != null) {
            FileEditorManager.getInstance(project).openFile(javaFile, true);
        }
    }

    // ------------------------------------------------------- doble clic = evento

    /**
     * Crea (o localiza) el {@code ActionListener} de un boton y deja el cursor
     * dentro, listo para escribir. Es lo que ocurre al hacer doble clic en el canvas.
     */
    public void openActionListener(FormModel model, WidgetModel button) {
        if (button.getType() != WidgetType.BUTTON) {
            return;
        }
        // Todo esto va en el hilo de UI porque nace de un doble clic y el alumno espera
        // ver el codigo al instante. Leer el PSI de la clase cuenta como operacion lenta;
        // se marca como excepcion conocida en vez de fingir que no lo es.
        try (AccessToken ignored = SlowOperations.knownIssue("SBE: doble clic sobre un boton, en el EDT")) {
            VirtualFile javaFile = ensure(model);
            if (javaFile == null) {
                return;
            }
            Document document = FileDocumentManager.getInstance().getDocument(javaFile);
            if (document == null) {
                return;
            }

            PsiDocumentManager.getInstance(project).commitDocument(document);

            String marker = button.getName() + ".addActionListener";
            if (!document.getText().contains(marker)) {
                insertListener(model, javaFile, document, button.getName());
            }
            navigateInsideListener(javaFile, document, marker);
        }
    }

    private void insertListener(FormModel model, VirtualFile javaFile, Document document, String buttonName) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(javaFile);
        if (!(psiFile instanceof PsiJavaFile javaPsi)) {
            return;
        }
        PsiClass formClass = findFormClass(javaPsi, model.getClassName());
        if (formClass == null) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(project, "Crear el ActionListener", "SwingBuilderEdu", () -> {
            PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
            PsiMethod events = eventsMethod(formClass, factory);
            PsiCodeBlock body = events.getBody();
            if (body == null) {
                return;
            }
            PsiStatement statement =
                    factory.createStatementFromText(JavaFormGenerator.actionListenerStatement(buttonName), body);
            PsiElement rBrace = body.getRBrace();
            PsiElement added = rBrace != null ? body.addBefore(statement, rBrace) : body.add(statement);
            CodeStyleManager.getInstance(project).reformat(added);
        });

        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
    }

    /** El metodo {@code initEventos()}; si el alumno lo borro, se vuelve a crear. */
    private PsiMethod eventsMethod(PsiClass formClass, PsiElementFactory factory) {
        PsiMethod[] found = formClass.findMethodsByName(JavaFormGenerator.EVENTS_METHOD, false);
        if (found.length > 0) {
            return found[0];
        }
        PsiMethod created = factory.createMethodFromText(
                "private void " + JavaFormGenerator.EVENTS_METHOD + "() {\n}", formClass);
        return (PsiMethod) formClass.add(created);
    }

    private static @Nullable PsiClass findFormClass(PsiJavaFile file, String className) {
        PsiClass[] classes = file.getClasses();
        for (PsiClass candidate : classes) {
            if (className.equals(candidate.getName())) {
                return candidate;
            }
        }
        return classes.length > 0 ? classes[0] : null;
    }

    /**
     * Deja el cursor en la linea de dentro del listener (el {@code // TODO}, o la
     * primera linea del codigo que el alumno ya haya escrito ahi).
     */
    private void navigateInsideListener(VirtualFile javaFile, Document document, String marker) {
        String text = document.getText();
        int at = text.indexOf(marker);
        if (at < 0) {
            FileEditorManager.getInstance(project).openFile(javaFile, true);
            return;
        }
        int openingLineEnd = text.indexOf('\n', at);
        int caret;
        if (openingLineEnd < 0) {
            caret = text.length();
        } else {
            int nextLineEnd = text.indexOf('\n', openingLineEnd + 1);
            caret = nextLineEnd < 0 ? text.length() : nextLineEnd;
        }
        new OpenFileDescriptor(project, javaFile, caret).navigate(true);
    }

    // ------------------------------------------------------------------ paquete

    /**
     * El paquete de la carpeta donde vive el {@code .sbe}.
     * <p>
     * Se calcula una sola vez: esto acaba llamandose desde el hilo de UI (al soltar un
     * componente en el canvas) y {@code getPackage} consulta indices, que ahi es una
     * operacion lenta. Como el fichero no se mueve de carpeta mientras el editor esta
     * abierto, con calcularlo la primera vez sobra.
     */
    private String packageName(VirtualFile directory) {
        if (cachedPackageName == null) {
            cachedPackageName = computePackageName(directory);
        }
        return cachedPackageName;
    }

    private String computePackageName(VirtualFile directory) {
        PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(directory);
        if (psiDirectory == null) {
            return "";
        }
        try (AccessToken ignored = SlowOperations.knownIssue("SBE: paquete calculado en el EDT, una vez por editor")) {
            PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDirectory);
            return psiPackage == null ? "" : psiPackage.getQualifiedName();
        }
    }
}
