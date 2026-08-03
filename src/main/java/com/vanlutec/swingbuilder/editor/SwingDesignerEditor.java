package com.vanlutec.swingbuilder.editor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.vanlutec.swingbuilder.codegen.FormJavaFile;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.FormModelIO;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import com.vanlutec.swingbuilder.ui.DesignCanvas;
import com.vanlutec.swingbuilder.ui.PalettePanel;
import com.vanlutec.swingbuilder.ui.PropertiesPanel;
import com.vanlutec.swingbuilder.ui.WidgetRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * La pestana "Design": paleta a la izquierda, canvas en el centro y tabla de
 * propiedades a la derecha, como en WindowBuilder.
 * <p>
 * El modelo se guarda en el {@link Document} del fichero {@code .sbe}, asi que
 * el deshacer/rehacer del IDE y la pestana "Text" funcionan sin mas.
 */
public final class SwingDesignerEditor extends UserDataHolderBase implements FileEditor {

    private final Project project;
    private final VirtualFile file;
    private final @Nullable Document document;
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    private final PalettePanel palette = new PalettePanel();
    private final DesignCanvas canvas = new DesignCanvas();
    private final PropertiesPanel properties = new PropertiesPanel();
    private final JPanel root = new JPanel(new BorderLayout());
    private final FormJavaFile javaFile;

    private FormModel model;
    private boolean pushingToDocument;

    public SwingDesignerEditor(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;
        this.document = FileDocumentManager.getInstance().getDocument(file);
        this.model = FormModelIO.fromXml(document != null ? document.getText() : "", file.getNameWithoutExtension());
        this.javaFile = new FormJavaFile(project, file);

        buildUi();
        wireListeners();

        canvas.setPalette(palette);
        canvas.setModel(model);
        properties.showFrame(model);

        if (document != null && document.getText().isBlank()) {
            ApplicationManager.getApplication().invokeLater(() -> pushModelToDocument("Crear formulario"));
        }
    }

    private void buildUi() {
        root.add(createToolbar(), BorderLayout.NORTH);

        JBScrollPane paletteScroll = new JBScrollPane(palette,
                JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        paletteScroll.setBorder(JBUI.Borders.empty());

        JBScrollPane canvasScroll = new JBScrollPane(canvas);
        canvasScroll.setBorder(JBUI.Borders.empty());
        canvasScroll.getVerticalScrollBar().setUnitIncrement(16);

        OnePixelSplitter inner = new OnePixelSplitter(false, 0.74f);
        inner.setFirstComponent(canvasScroll);
        inner.setSecondComponent(properties);

        OnePixelSplitter outer = new OnePixelSplitter(false, 0.17f);
        outer.setFirstComponent(paletteScroll);
        outer.setSecondComponent(inner);

        root.add(outer, BorderLayout.CENTER);
        root.setPreferredSize(new Dimension(JBUI.scale(900), JBUI.scale(600)));
    }

    private JComponent createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();

        group.add(new DumbAwareAction("Generar codigo Java",
                "Escribe (o actualiza) la clase Java del formulario", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                generateJava(true);
            }
        });
        group.add(new DumbAwareAction("Abrir la clase Java", "Abre el .java generado", AllIcons.FileTypes.Java) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                openJava();
            }
        });
        group.add(new ToggleAction("Auto-generar Java", "Regenera el .java cada vez que cambia el diseno",
                AllIcons.General.AutoscrollToSource) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return autoGenerate;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean state) {
                autoGenerate = state;
                if (state) {
                    generateJava(false);
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });
        group.addSeparator();
        group.add(new DumbAwareAction("Vista previa",
                "Muestra como quedara la ventana. NO ejecuta tu codigo: para eso, ejecuta el main "
                        + "de la clase Java (flecha verde).",
                AllIcons.Actions.Execute) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                showPreview();
            }
        });
        group.add(new DumbAwareAction("Borrar el componente seleccionado", "Supr", AllIcons.General.Remove) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                canvas.deleteSelected();
            }

            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setEnabled(canvas.getSelectedWidget() != null);
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        });

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("SwingBuilderEdu.Designer", group, true);
        toolbar.setTargetComponent(root);

        JPanel north = new JPanel(new BorderLayout());
        north.add(toolbar.getComponent(), BorderLayout.WEST);
        JBLabel hint = new JBLabel("Layout: null (absoluto)  ·  doble clic en un JButton = crear su ActionListener");
        hint.setBorder(JBUI.Borders.empty(0, 10));
        hint.setForeground(com.intellij.util.ui.UIUtil.getContextHelpForeground());
        north.add(hint, BorderLayout.CENTER);
        return north;
    }

    private boolean autoGenerate = true;

    private void wireListeners() {
        canvas.addListener(new DesignCanvas.Listener() {
            @Override
            public void selectionChanged(@Nullable WidgetModel widget) {
                if (widget == null) {
                    properties.showFrame(model);
                } else {
                    properties.showWidget(model, widget);
                }
            }

            @Override
            public void modelCommitted(String commandName) {
                properties.refreshValues();
                pushModelToDocument(commandName);
            }

            @Override
            public void widgetDoubleClicked(WidgetModel widget) {
                onWidgetDoubleClicked(widget);
            }
        });

        properties.addListener((widget, commandName) -> {
            if (widget == null) {
                canvas.refreshFrame();
            } else {
                canvas.refreshWidget(widget);
            }
            pushModelToDocument(commandName);
        });

        if (document != null) {
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void documentChanged(@NotNull DocumentEvent event) {
                    if (pushingToDocument) {
                        return;
                    }
                    ApplicationManager.getApplication().invokeLater(SwingDesignerEditor.this::reloadFromDocument);
                }
            }, this);
        }
    }

    private void reloadFromDocument() {
        if (document == null || !file.isValid()) {
            return;
        }
        model = FormModelIO.fromXml(document.getText(), file.getNameWithoutExtension());
        canvas.setModel(model);
        WidgetModel selected = canvas.getSelectedWidget();
        if (selected == null) {
            properties.showFrame(model);
        } else {
            properties.showWidget(model, selected);
        }
    }

    /** Guarda el modelo en el fichero .sbe (una sola entrada de deshacer por gesto). */
    private void pushModelToDocument(String commandName) {
        if (document == null) {
            return;
        }
        String xml = FormModelIO.toXml(model);
        if (!xml.equals(document.getText())) {
            pushingToDocument = true;
            try {
                WriteCommandAction.runWriteCommandAction(project, commandName, "SwingBuilderEdu",
                        () -> document.setText(xml));
            } finally {
                pushingToDocument = false;
            }
        }
        if (autoGenerate) {
            generateJava(false);
        }
    }

    // ------------------------------------------------------- generacion de Java

    /** Crea o pone al dia la clase Java del formulario. */
    private void generateJava(boolean openAfterwards) {
        if (project.isDisposed() || !file.isValid()) {
            return;
        }
        if (openAfterwards) {
            javaFile.open(model);
        } else {
            javaFile.ensure(model);
        }
    }

    private void openJava() {
        if (!project.isDisposed() && file.isValid()) {
            javaFile.open(model);
        }
    }

    /**
     * Doble clic en el canvas. Sobre un JButton crea su {@code ActionListener} y
     * salta al codigo; sobre cualquier otro componente, edita su texto en la tabla
     * de propiedades (que es lo que hace WindowBuilder).
     */
    private void onWidgetDoubleClicked(WidgetModel widget) {
        if (widget.getType() == WidgetType.BUTTON) {
            javaFile.openActionListener(model, widget);
        } else {
            properties.startEditingText();
        }
    }

    // ---------------------------------------------------------------- preview

    private void showPreview() {
        FormModel snapshot = model;
        DialogWrapper dialog = new DialogWrapper(project, false) {
            {
                setTitle(snapshot.getTitle() + "  -  vista previa del diseno");
                init();
            }

            @Override
            protected @Nullable JComponent createCenterPanel() {
                JPanel panel = new JPanel(null);
                panel.setPreferredSize(new Dimension(snapshot.getFrameWidth(), snapshot.getFrameHeight() - 26));
                if (!snapshot.getTheme().isSistema()) {
                    panel.setBackground(snapshot.getTheme().getPanelBackground());
                }
                for (WidgetModel widget : snapshot.getWidgets()) {
                    JComponent component = WidgetRenderer.createInteractive(widget, snapshot.getTheme()).outer();
                    component.setBounds(widget.getBounds());
                    panel.add(component);
                }
                return panel;
            }

            /** Que nadie espere que los botones hagan algo: esto es solo el aspecto. */
            @Override
            protected @Nullable JComponent createSouthPanel() {
                JPanel south = new JPanel(new BorderLayout());
                JBLabel nota = new JBLabel("Solo es el aspecto. Para probar tu codigo, ejecuta el main de "
                        + snapshot.getClassName() + ".java");
                nota.setForeground(com.intellij.util.ui.UIUtil.getContextHelpForeground());
                nota.setBorder(JBUI.Borders.empty(4, 10));
                south.add(nota, BorderLayout.WEST);
                JComponent buttons = super.createSouthPanel();
                if (buttons != null) {
                    south.add(buttons, BorderLayout.EAST);
                }
                return south;
            }

            @Override
            protected Action @NotNull [] createActions() {
                return new Action[]{getOKAction()};
            }
        };
        dialog.show();
    }

    // ---------------------------------------------------------------- FileEditor

    @Override
    public @NotNull JComponent getComponent() {
        return root;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return canvas;
    }

    @Override
    public @NotNull String getName() {
        return "Design";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public @NotNull FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return FileEditorState.INSTANCE;
    }

    @Override
    public boolean isModified() {
        return document != null && FileDocumentManager.getInstance().isDocumentUnsaved(document);
    }

    @Override
    public boolean isValid() {
        return file.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public @NotNull VirtualFile getFile() {
        return file;
    }

    @Override
    public void dispose() {
    }
}
