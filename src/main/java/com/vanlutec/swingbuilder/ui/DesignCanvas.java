package com.vanlutec.swingbuilder.ui;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.vanlutec.swingbuilder.model.FormModel;
import com.vanlutec.swingbuilder.model.WidgetModel;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * El canvas central: dibuja el JFrame con sus componentes reales y permite
 * seleccionar, mover y redimensionar como en WindowBuilder.
 * <p>
 * Los componentes Swing reales viven en {@link #contentHost}; encima hay una capa
 * transparente ({@code overlay}) que se queda con todos los eventos de raton y
 * pinta la seleccion, los tiradores y las guias de alineacion.
 */
public final class DesignCanvas extends JPanel {

    public interface Listener {
        /** {@code null} = esta seleccionado el propio JFrame. */
        void selectionChanged(@Nullable WidgetModel widget);

        /** Un cambio terminado (soltar el raton, anadir, borrar): hay que persistirlo. */
        void modelCommitted(String commandName);

        void widgetDoubleClicked(WidgetModel widget);
    }

    private static final int MARGIN = 16;
    private static final int TITLE_H = 26;
    private static final int FRAME_BORDER = 1;
    private static final int HANDLE = 7;
    private static final int SNAP = 6;
    private static final int CONTAINER_MARGIN = 10;

    private static final int NW = 0, N = 1, NE = 2, E = 3, SE = 4, S = 5, SW = 6, W = 7;

    private static final Color CANVAS_BG = new JBColor(new Color(0xE9EBEE), new Color(0x2B2D30));
    private static final Color FRAME_BORDER_COLOR = new JBColor(new Color(0x8A8F98), new Color(0x1E1F22));
    private static final Color TITLE_TOP = new JBColor(new Color(0xF5F6F7), new Color(0x4E5157));
    private static final Color TITLE_BOTTOM = new JBColor(new Color(0xD9DCE1), new Color(0x3C3F42));
    private static final Color TITLE_FG = new JBColor(new Color(0x2B2D30), new Color(0xDFE1E5));
    private static final Color SELECTION = new JBColor(new Color(0x3574F0), new Color(0x548AF7));
    private static final Color GUIDE = new JBColor(new Color(0xE05E5E), new Color(0xF08A8A));
    private static final Color HANDLE_FILL = new JBColor(Color.BLACK, Color.WHITE);
    private static final Color HANDLE_BORDER = new JBColor(Color.WHITE, Color.BLACK);
    private static final Color TIP_BG = new JBColor(new Color(0x3C3F42), new Color(0x1E1F22));

    private final JPanel contentHost = new JPanel(null);
    private final Overlay overlay = new Overlay();
    private final Map<WidgetModel, WidgetRenderer> rendered = new IdentityHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    private Color defaultContentBackground;
    private FormModel model = new FormModel();
    private @Nullable PalettePanel palette;
    private @Nullable WidgetModel selected;
    private boolean frameSelected = true;

    private int mode = MODE_NONE;
    private static final int MODE_NONE = 0, MODE_MOVE = 1, MODE_RESIZE = 2, MODE_FRAME_RESIZE = 3;
    private int activeHandle = -1;
    private Point pressPoint = new Point();
    private Rectangle pressBounds = new Rectangle();
    private Dimension pressFrameSize = new Dimension();
    private @Nullable Rectangle ghost;
    private final List<int[]> guides = new ArrayList<>();
    private @Nullable String sizeTip;

    public DesignCanvas() {
        setLayout(null);
        setOpaque(true);
        setBackground(CANVAS_BG);

        contentHost.setOpaque(true);
        Color contentBg = UIManager.getColor("Panel.background");
        defaultContentBackground = contentBg != null ? contentBg : UIUtil.getPanelBackground();
        contentHost.setBackground(defaultContentBackground);
        contentHost.setFocusable(false);

        add(contentHost);
        add(overlay);
        setComponentZOrder(overlay, 0);

        installShortcuts();
        new DropTarget(overlay, DnDConstants.ACTION_COPY, overlay, true);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void setPalette(PalettePanel palette) {
        this.palette = palette;
    }

    public FormModel getModel() {
        return model;
    }

    /** Reconstruye el canvas entero desde el modelo (al abrir o al recargar el .sbe). */
    public void setModel(FormModel model) {
        String previousSelection = selected != null ? selected.getName() : null;
        this.model = model;
        rebuild();
        WidgetModel restored = null;
        if (previousSelection != null) {
            for (WidgetModel widget : model.getWidgets()) {
                if (widget.getName().equals(previousSelection)) {
                    restored = widget;
                    break;
                }
            }
        }
        selected = restored;
        frameSelected = restored == null;
        fireSelectionChanged();
        revalidate();
        repaint();
    }

    private void rebuild() {
        contentHost.removeAll();
        rendered.clear();
        // El fondo del formulario tambien es parte del tema.
        java.awt.Color themed = model.getTheme().getPanelBackground();
        contentHost.setBackground(themed != null ? themed : defaultContentBackground);
        for (WidgetModel widget : model.getWidgets()) {
            WidgetRenderer renderer = WidgetRenderer.create(widget, model.getTheme());
            rendered.put(widget, renderer);
            contentHost.add(renderer.outer());
        }
        contentHost.revalidate();
        contentHost.repaint();
    }

    public @Nullable WidgetModel getSelectedWidget() {
        return selected;
    }

    public void select(@Nullable WidgetModel widget) {
        selected = widget;
        frameSelected = widget == null;
        overlay.requestFocusInWindow();
        fireSelectionChanged();
        repaint();
    }

    /** Vuelve a aplicar las propiedades de un componente (lo llama la tabla de propiedades). */
    public void refreshWidget(WidgetModel widget) {
        WidgetRenderer renderer = rendered.get(widget);
        if (renderer != null) {
            renderer.apply(widget, model.getTheme());
            contentHost.repaint();
        }
        repaint();
    }

    /** El JFrame cambio de tamano o de titulo. */
    public void refreshFrame() {
        revalidate();
        repaint();
        if (getParent() != null) {
            getParent().revalidate();
        }
    }

    public void addWidget(WidgetType type, int formX, int formY) {
        WidgetModel widget = new WidgetModel(type, model.nextName(type), Math.max(0, formX), Math.max(0, formY));
        model.addWidget(widget);
        WidgetRenderer renderer = WidgetRenderer.create(widget, model.getTheme());
        rendered.put(widget, renderer);
        contentHost.add(renderer.outer());
        contentHost.repaint();
        select(widget);
        commit("Anadir " + type.getSimpleName());
    }

    public void deleteSelected() {
        if (selected == null) {
            return;
        }
        WidgetRenderer renderer = rendered.remove(selected);
        if (renderer != null) {
            contentHost.remove(renderer.outer());
        }
        String name = selected.getName();
        model.removeWidget(selected);
        selected = null;
        frameSelected = true;
        contentHost.revalidate();
        contentHost.repaint();
        fireSelectionChanged();
        repaint();
        commit("Borrar " + name);
    }

    // ---------------------------------------------------------------- geometria

    private Rectangle frameRect() {
        return new Rectangle(MARGIN, MARGIN, model.getFrameWidth(), model.getFrameHeight());
    }

    private Rectangle contentRect() {
        Rectangle frame = frameRect();
        return new Rectangle(
                frame.x + FRAME_BORDER,
                frame.y + TITLE_H,
                Math.max(1, frame.width - 2 * FRAME_BORDER),
                Math.max(1, frame.height - TITLE_H - FRAME_BORDER));
    }

    private Rectangle toCanvas(Rectangle formBounds) {
        Rectangle content = contentRect();
        return new Rectangle(content.x + formBounds.x, content.y + formBounds.y, formBounds.width, formBounds.height);
    }

    private Point toForm(Point canvasPoint) {
        Rectangle content = contentRect();
        return new Point(canvasPoint.x - content.x, canvasPoint.y - content.y);
    }

    @Override
    public Dimension getPreferredSize() {
        Rectangle frame = frameRect();
        return new Dimension(frame.x + frame.width + MARGIN + HANDLE, frame.y + frame.height + MARGIN + HANDLE);
    }

    @Override
    public void doLayout() {
        contentHost.setBounds(contentRect());
        overlay.setBounds(0, 0, getWidth(), getHeight());
        for (Map.Entry<WidgetModel, WidgetRenderer> entry : rendered.entrySet()) {
            entry.getValue().outer().setBounds(entry.getKey().getBounds());
        }
    }

    // ---------------------------------------------------------------- pintado

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            UIUtil.applyRenderingHints(g2);
            Rectangle frame = frameRect();

            // sombra suave bajo la ventana
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f));
            g2.setColor(Color.BLACK);
            g2.fillRoundRect(frame.x + 3, frame.y + 4, frame.width, frame.height, 6, 6);
            g2.setComposite(AlphaComposite.SrcOver);

            // barra de titulo
            g2.setPaint(new GradientPaint(0, frame.y, TITLE_TOP, 0, frame.y + TITLE_H, TITLE_BOTTOM));
            g2.fillRect(frame.x, frame.y, frame.width, TITLE_H);

            g2.setFont(JBUI.Fonts.label().deriveFont(Font.BOLD));
            g2.setColor(TITLE_FG);
            FontMetrics fm = g2.getFontMetrics();
            String title = model.getTitle();
            int maxTextWidth = Math.max(0, frame.width - 70);
            String shown = title;
            while (fm.stringWidth(shown) > maxTextWidth && shown.length() > 1) {
                shown = shown.substring(0, shown.length() - 1);
            }
            g2.drawString(shown, frame.x + 8, frame.y + (TITLE_H + fm.getAscent()) / 2 - 1);
            paintWindowButtons(g2, frame);

            // borde de la ventana
            g2.setColor(FRAME_BORDER_COLOR);
            g2.drawRect(frame.x, frame.y, frame.width - 1, frame.height - 1);
            g2.drawLine(frame.x, frame.y + TITLE_H, frame.x + frame.width - 1, frame.y + TITLE_H);
        } finally {
            g2.dispose();
        }
    }

    private void paintWindowButtons(Graphics2D g2, Rectangle frame) {
        int cy = frame.y + TITLE_H / 2;
        int right = frame.x + frame.width - 12;
        g2.setColor(new JBColor(new Color(0x6B7079), new Color(0xB4B8BF)));
        g2.setStroke(new BasicStroke(1.2f));
        // cerrar
        g2.drawLine(right - 4, cy - 4, right + 4, cy + 4);
        g2.drawLine(right + 4, cy - 4, right - 4, cy + 4);
        // maximizar
        int m = right - 20;
        g2.drawRect(m - 4, cy - 4, 8, 8);
        // minimizar
        int mi = right - 40;
        g2.drawLine(mi - 4, cy + 4, mi + 4, cy + 4);
        g2.setStroke(new BasicStroke(1f));
    }

    // ---------------------------------------------------------------- eventos

    private void installShortcuts() {
        overlay.setFocusable(true);
        overlay.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "sbe.delete");
        overlay.getActionMap().put("sbe.delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        nudge(KeyEvent.VK_LEFT, -1, 0, 0);
        nudge(KeyEvent.VK_RIGHT, 1, 0, 0);
        nudge(KeyEvent.VK_UP, 0, -1, 0);
        nudge(KeyEvent.VK_DOWN, 0, 1, 0);
        nudge(KeyEvent.VK_LEFT, -10, 0, KeyEvent.SHIFT_DOWN_MASK);
        nudge(KeyEvent.VK_RIGHT, 10, 0, KeyEvent.SHIFT_DOWN_MASK);
        nudge(KeyEvent.VK_UP, 0, -10, KeyEvent.SHIFT_DOWN_MASK);
        nudge(KeyEvent.VK_DOWN, 0, 10, KeyEvent.SHIFT_DOWN_MASK);
    }

    private void nudge(int keyCode, int dx, int dy, int modifiers) {
        String key = "sbe.nudge." + keyCode + "." + modifiers;
        overlay.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(keyCode, modifiers), key);
        overlay.getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selected == null) {
                    return;
                }
                selected.setX(Math.max(0, selected.getX() + dx));
                selected.setY(Math.max(0, selected.getY() + dy));
                refreshWidget(selected);
                commit("Mover " + selected.getName());
            }
        });
    }

    private @Nullable WidgetModel widgetAt(Point canvasPoint) {
        Point p = toForm(canvasPoint);
        List<WidgetModel> widgets = model.getWidgets();
        for (int i = widgets.size() - 1; i >= 0; i--) {
            if (widgets.get(i).getBounds().contains(p)) {
                return widgets.get(i);
            }
        }
        return null;
    }

    private Point handleCenter(Rectangle r, int handle) {
        int mx = r.x + r.width / 2;
        int my = r.y + r.height / 2;
        return switch (handle) {
            case NW -> new Point(r.x, r.y);
            case N -> new Point(mx, r.y);
            case NE -> new Point(r.x + r.width, r.y);
            case E -> new Point(r.x + r.width, my);
            case SE -> new Point(r.x + r.width, r.y + r.height);
            case S -> new Point(mx, r.y + r.height);
            case SW -> new Point(r.x, r.y + r.height);
            default -> new Point(r.x, my);
        };
    }

    private int hitHandle(Rectangle canvasRect, Point p, int[] candidates) {
        for (int handle : candidates) {
            Point c = handleCenter(canvasRect, handle);
            if (Math.abs(p.x - c.x) <= HANDLE / 2 + 1 && Math.abs(p.y - c.y) <= HANDLE / 2 + 1) {
                return handle;
            }
        }
        return -1;
    }

    private static final int[] ALL_HANDLES = {NW, N, NE, E, SE, S, SW, W};
    private static final int[] FRAME_HANDLES = {E, SE, S};

    private static int cursorFor(int handle) {
        return switch (handle) {
            case NW -> Cursor.NW_RESIZE_CURSOR;
            case N -> Cursor.N_RESIZE_CURSOR;
            case NE -> Cursor.NE_RESIZE_CURSOR;
            case E -> Cursor.E_RESIZE_CURSOR;
            case SE -> Cursor.SE_RESIZE_CURSOR;
            case S -> Cursor.S_RESIZE_CURSOR;
            case SW -> Cursor.SW_RESIZE_CURSOR;
            default -> Cursor.W_RESIZE_CURSOR;
        };
    }

    private void commit(String commandName) {
        for (Listener listener : listeners) {
            listener.modelCommitted(commandName);
        }
    }

    private void fireSelectionChanged() {
        for (Listener listener : listeners) {
            listener.selectionChanged(selected);
        }
    }

    /** Capa transparente: seleccion, tiradores, guias y drop de la paleta. */
    private final class Overlay extends JComponent implements DropTargetListener {

        private Overlay() {
            setOpaque(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handlePressed(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    handleReleased(e);
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                        WidgetModel widget = widgetAt(e.getPoint());
                        if (widget != null) {
                            select(widget);
                            for (Listener listener : listeners) {
                                listener.widgetDoubleClicked(widget);
                            }
                        }
                    }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    handleDragged(e);
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    updateCursor(e.getPoint());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                UIUtil.applyRenderingHints(g2);
                if (frameSelected) {
                    Rectangle frame = frameRect();
                    g2.setColor(SELECTION);
                    g2.drawRect(frame.x - 1, frame.y - 1, frame.width + 1, frame.height + 1);
                    for (int handle : FRAME_HANDLES) {
                        paintHandle(g2, handleCenter(frame, handle));
                    }
                }
                if (selected != null) {
                    Rectangle r = toCanvas(selected.getBounds());
                    g2.setColor(SELECTION);
                    g2.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
                    for (int handle : ALL_HANDLES) {
                        paintHandle(g2, handleCenter(r, handle));
                    }
                }
                for (int[] guide : guides) {
                    g2.setColor(GUIDE);
                    g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f,
                            new float[]{3f, 3f}, 0f));
                    g2.drawLine(guide[0], guide[1], guide[2], guide[3]);
                    g2.setStroke(new BasicStroke(1f));
                }
                if (ghost != null) {
                    g2.setColor(SELECTION);
                    g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f,
                            new float[]{4f, 3f}, 0f));
                    g2.drawRect(ghost.x, ghost.y, ghost.width, ghost.height);
                    g2.setStroke(new BasicStroke(1f));
                }
                if (sizeTip != null) {
                    paintTip(g2);
                }
            } finally {
                g2.dispose();
            }
        }

        private void paintHandle(Graphics2D g2, Point center) {
            int half = HANDLE / 2;
            g2.setColor(HANDLE_BORDER);
            g2.fillRect(center.x - half - 1, center.y - half - 1, HANDLE + 2, HANDLE + 2);
            g2.setColor(HANDLE_FILL);
            g2.fillRect(center.x - half, center.y - half, HANDLE, HANDLE);
            g2.setColor(SELECTION);
        }

        private void paintTip(Graphics2D g2) {
            Rectangle anchor = ghost != null ? ghost
                    : selected != null ? toCanvas(selected.getBounds()) : frameRect();
            g2.setFont(JBUI.Fonts.label(11f));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(sizeTip) + 10;
            int h = fm.getHeight() + 4;
            int x = anchor.x + anchor.width + 6;
            int y = anchor.y + anchor.height + 6;
            g2.setColor(TIP_BG);
            g2.fillRoundRect(x, y, w, h, 4, 4);
            g2.setColor(Color.WHITE);
            g2.drawString(sizeTip, x + 5, y + fm.getAscent() + 2);
        }

        // ------------------------------------------------------------ drop de la paleta

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            dragOver(dtde);
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            WidgetType type = draggedType(dtde.getTransferable());
            if (type == null || !contentRect().contains(dtde.getLocation())) {
                ghost = null;
                sizeTip = null;
                repaint();
                dtde.rejectDrag();
                return;
            }
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
            ghost = new Rectangle(dtde.getLocation().x, dtde.getLocation().y,
                    type.getDefaultWidth(), type.getDefaultHeight());
            Point form = toForm(dtde.getLocation());
            sizeTip = form.x + ", " + form.y;
            repaint();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            ghost = null;
            sizeTip = null;
            repaint();
        }

        @Override
        public void drop(DropTargetDropEvent dtde) {
            WidgetType type = draggedType(dtde.getTransferable());
            ghost = null;
            sizeTip = null;
            repaint();
            if (type == null) {
                dtde.rejectDrop();
                return;
            }
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            Point form = toForm(dtde.getLocation());
            addWidget(type, form.x, form.y);
            dtde.dropComplete(true);
            if (palette != null) {
                palette.disarm();
            }
        }

        private @Nullable WidgetType draggedType(Transferable transferable) {
            try {
                if (!transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return null;
                }
                Object data = transferable.getTransferData(DataFlavor.stringFlavor);
                if (data instanceof String s && s.startsWith(PalettePanel.DND_PREFIX)) {
                    return WidgetType.fromId(s.substring(PalettePanel.DND_PREFIX.length()), null);
                }
            } catch (Exception ignored) {
                // Un drop de otra procedencia: simplemente no es nuestro.
            }
            return null;
        }
    }

    // ---------------------------------------------------------------- raton

    private void handlePressed(MouseEvent e) {
        overlay.requestFocusInWindow();
        Point p = e.getPoint();

        if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
            WidgetModel widget = widgetAt(p);
            select(widget);
            showContextMenu(e, widget);
            return;
        }
        if (!SwingUtilities.isLeftMouseButton(e)) {
            return;
        }

        WidgetType armed = palette != null ? palette.getArmedType() : null;
        if (armed != null) {
            if (contentRect().contains(p)) {
                Point form = toForm(p);
                addWidget(armed, form.x, form.y);
            }
            palette.disarm();
            return;
        }

        if (selected != null) {
            int handle = hitHandle(toCanvas(selected.getBounds()), p, ALL_HANDLES);
            if (handle >= 0) {
                mode = MODE_RESIZE;
                activeHandle = handle;
                pressPoint = p;
                pressBounds = selected.getBounds();
                return;
            }
        }
        if (frameSelected) {
            int handle = hitHandle(frameRect(), p, FRAME_HANDLES);
            if (handle >= 0) {
                mode = MODE_FRAME_RESIZE;
                activeHandle = handle;
                pressPoint = p;
                pressFrameSize = new Dimension(model.getFrameWidth(), model.getFrameHeight());
                return;
            }
        }

        WidgetModel widget = widgetAt(p);
        select(widget);
        if (widget != null) {
            mode = MODE_MOVE;
            pressPoint = p;
            pressBounds = widget.getBounds();
        } else {
            mode = MODE_NONE;
        }
    }

    private void handleDragged(MouseEvent e) {
        if (mode == MODE_NONE) {
            return;
        }
        int dx = e.getX() - pressPoint.x;
        int dy = e.getY() - pressPoint.y;

        if (mode == MODE_FRAME_RESIZE) {
            if (activeHandle == E || activeHandle == SE) {
                model.setFrameWidth(pressFrameSize.width + dx);
            }
            if (activeHandle == S || activeHandle == SE) {
                model.setFrameHeight(pressFrameSize.height + dy);
            }
            sizeTip = model.getFrameWidth() + " x " + model.getFrameHeight();
            refreshFrame();
            return;
        }
        if (selected == null) {
            return;
        }

        guides.clear();
        Rectangle bounds;
        if (mode == MODE_MOVE) {
            bounds = new Rectangle(pressBounds);
            bounds.x = Math.max(0, pressBounds.x + dx);
            bounds.y = Math.max(0, pressBounds.y + dy);
            if (!e.isAltDown()) {
                snapMove(bounds);
            }
            sizeTip = bounds.x + ", " + bounds.y;
        } else {
            bounds = resizedBounds(dx, dy);
            sizeTip = bounds.width + " x " + bounds.height;
        }
        selected.setBounds(bounds);
        refreshWidget(selected);
    }

    private Rectangle resizedBounds(int dx, int dy) {
        Rectangle r = new Rectangle(pressBounds);
        switch (activeHandle) {
            case NW -> {
                r.x += dx;
                r.y += dy;
                r.width -= dx;
                r.height -= dy;
            }
            case N -> {
                r.y += dy;
                r.height -= dy;
            }
            case NE -> {
                r.y += dy;
                r.width += dx;
                r.height -= dy;
            }
            case E -> r.width += dx;
            case SE -> {
                r.width += dx;
                r.height += dy;
            }
            case S -> r.height += dy;
            case SW -> {
                r.x += dx;
                r.width -= dx;
                r.height += dy;
            }
            default -> {
                r.x += dx;
                r.width -= dx;
            }
        }
        if (r.width < 8) {
            if (activeHandle == NW || activeHandle == W || activeHandle == SW) {
                r.x = pressBounds.x + pressBounds.width - 8;
            }
            r.width = 8;
        }
        if (r.height < 8) {
            if (activeHandle == NW || activeHandle == N || activeHandle == NE) {
                r.y = pressBounds.y + pressBounds.height - 8;
            }
            r.height = 8;
        }
        r.x = Math.max(0, r.x);
        r.y = Math.max(0, r.y);
        return r;
    }

    /** Imanta el componente a los bordes de sus hermanos y al margen del contenedor. */
    private void snapMove(Rectangle bounds) {
        Rectangle content = contentRect();
        List<Integer> xTargets = new ArrayList<>();
        List<Integer> yTargets = new ArrayList<>();
        xTargets.add(CONTAINER_MARGIN);
        yTargets.add(CONTAINER_MARGIN);
        xTargets.add(content.width - CONTAINER_MARGIN - bounds.width);
        yTargets.add(content.height - CONTAINER_MARGIN - bounds.height);

        for (WidgetModel other : model.getWidgets()) {
            if (other == selected) {
                continue;
            }
            xTargets.add(other.getX());
            xTargets.add(other.getX() + other.getWidth() - bounds.width);
            yTargets.add(other.getY());
            yTargets.add(other.getY() + other.getHeight() - bounds.height);
        }
        for (int target : xTargets) {
            if (Math.abs(bounds.x - target) <= SNAP) {
                bounds.x = target;
                break;
            }
        }
        for (int target : yTargets) {
            if (Math.abs(bounds.y - target) <= SNAP) {
                bounds.y = target;
                break;
            }
        }
        // guias visuales cuando hay alineacion exacta con un hermano
        for (WidgetModel other : model.getWidgets()) {
            if (other == selected) {
                continue;
            }
            if (other.getX() == bounds.x) {
                int x = content.x + bounds.x;
                guides.add(new int[]{x, content.y, x, content.y + content.height});
            }
            if (other.getY() == bounds.y) {
                int y = content.y + bounds.y;
                guides.add(new int[]{content.x, y, content.x + content.width, y});
            }
        }
    }

    private void handleReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showContextMenu(e, widgetAt(e.getPoint()));
        }
        boolean changed = mode != MODE_NONE;
        int previousMode = mode;
        mode = MODE_NONE;
        activeHandle = -1;
        guides.clear();
        sizeTip = null;
        ghost = null;
        repaint();
        if (!changed) {
            return;
        }
        switch (previousMode) {
            case MODE_MOVE -> commit("Mover " + (selected != null ? selected.getName() : "componente"));
            case MODE_RESIZE -> commit("Redimensionar " + (selected != null ? selected.getName() : "componente"));
            case MODE_FRAME_RESIZE -> commit("Redimensionar formulario");
            default -> {
            }
        }
    }

    private void updateCursor(Point p) {
        if (palette != null && palette.getArmedType() != null) {
            overlay.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            return;
        }
        if (selected != null) {
            int handle = hitHandle(toCanvas(selected.getBounds()), p, ALL_HANDLES);
            if (handle >= 0) {
                overlay.setCursor(Cursor.getPredefinedCursor(cursorFor(handle)));
                return;
            }
        }
        if (frameSelected) {
            int handle = hitHandle(frameRect(), p, FRAME_HANDLES);
            if (handle >= 0) {
                overlay.setCursor(Cursor.getPredefinedCursor(cursorFor(handle)));
                return;
            }
        }
        overlay.setCursor(Cursor.getPredefinedCursor(
                widgetAt(p) != null ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void showContextMenu(MouseEvent e, @Nullable WidgetModel widget) {
        JPopupMenu menu = new JPopupMenu();
        if (widget != null) {
            JMenuItem delete = new JMenuItem("Borrar   (Supr)");
            delete.addActionListener(a -> deleteSelected());
            menu.add(delete);
            if (widget.getType() == WidgetType.BUTTON) {
                JMenuItem event = new JMenuItem("Anadir manejador de evento (doble clic)");
                event.addActionListener(a -> {
                    for (Listener listener : listeners) {
                        listener.widgetDoubleClicked(widget);
                    }
                });
                menu.add(event);
            }
            JMenuItem toFront = new JMenuItem("Traer al frente");
            toFront.addActionListener(a -> {
                model.getWidgets().remove(widget);
                model.getWidgets().add(widget);
                rebuild();
                doLayout();
                repaint();
                commit("Traer al frente");
            });
            menu.add(toFront);
        } else {
            JMenuItem info = new JMenuItem("Formulario seleccionado (JFrame)");
            info.setEnabled(false);
            menu.add(info);
        }
        menu.show(overlay, e.getX(), e.getY());
    }
}
