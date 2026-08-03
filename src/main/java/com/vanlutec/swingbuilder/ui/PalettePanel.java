package com.vanlutec.swingbuilder.ui;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.vanlutec.swingbuilder.model.WidgetType;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import com.vanlutec.swingbuilder.SbeIcons;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * La paleta, imitando la de WindowBuilder: una cabecera de categoria
 * ("Components") y debajo una lista vertical de icono + nombre.
 * <p>
 * Se puede usar de dos formas, como en WindowBuilder:
 * <ul>
 *     <li>clic en el elemento y luego clic en el canvas para colocarlo, o</li>
 *     <li>arrastrar el elemento directamente sobre el canvas.</li>
 * </ul>
 */
public final class PalettePanel extends JPanel {

    /** Prefijo del texto que se arrastra, para no confundirlo con otros drops. */
    public static final String DND_PREFIX = "sbe-widget:";

    private static final int ROW_HEIGHT = 24;

    private static final Color PALETTE_BG = new JBColor(new Color(0xF7F8FA), new Color(0x3C3F41));
    private static final Color HEADER_BG_TOP = new JBColor(new Color(0xEDEFF2), new Color(0x4A4D50));
    private static final Color HEADER_BG_BOTTOM = new JBColor(new Color(0xDDE0E6), new Color(0x3F4244));
    private static final Color HEADER_BORDER = new JBColor(new Color(0xC2C6CE), new Color(0x2B2D2E));
    private static final Color HOVER_BG = new JBColor(new Color(0xE3EDFB), new Color(0x4B5563));
    private static final Color ARMED_BG = new JBColor(new Color(0xCBDDF7), new Color(0x2F5C8F));
    private static final Color ARMED_BORDER = new JBColor(new Color(0x6E9BD6), new Color(0x5B87C4));
    private static final Color TITLE_FG = new JBColor(Gray._70, Gray._160);

    private final List<ItemRow> rows = new ArrayList<>();
    private final List<Listener> listeners = new ArrayList<>();
    private final JPanel itemsPanel;

    private WidgetType armedType;

    public PalettePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(PALETTE_BG);
        setOpaque(true);
        setBorder(JBUI.Borders.empty());

        add(new ViewTitle("Palette"));

        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);

        CategoryHeader header = new CategoryHeader("Components", itemsPanel);
        add(header);
        add(itemsPanel);

        for (WidgetType type : WidgetType.values()) {
            ItemRow row = new ItemRow(type);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.add(row);
            itemsPanel.add(row);
        }
        add(Box.createVerticalGlue());
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    /** Tipo actualmente "cargado" en el cursor, o {@code null}. */
    public @Nullable WidgetType getArmedType() {
        return armedType;
    }

    public void disarm() {
        setArmedType(null);
    }

    private void setArmedType(@Nullable WidgetType type) {
        if (armedType == type) {
            return;
        }
        armedType = type;
        repaint();
        for (Listener listener : listeners) {
            listener.typeArmed(type);
        }
    }

    public interface Listener {
        void typeArmed(@Nullable WidgetType type);
    }

    /** Cabecera al estilo de las "views" de Eclipse: fondo plano y titulo pequeno. */
    private static final class ViewTitle extends JComponent {

        private final String text;

        private ViewTitle(String text) {
            this.text = text;
            setPreferredSize(new Dimension(0, JBUI.scale(22)));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, JBUI.scale(22)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                UIUtil.applyRenderingHints(g2);
                g2.setColor(PALETTE_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(TITLE_FG);
                g2.setFont(JBUI.Fonts.label(11f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, JBUI.scale(8), (getHeight() + fm.getAscent()) / 2 - JBUI.scale(1));
                // El logo, discreto y pegado a la derecha del titulo de la vista.
                Icon logo = SbeIcons.cibertec(14);
                int x = getWidth() - logo.getIconWidth() - JBUI.scale(8);
                if (x > JBUI.scale(8) + fm.stringWidth(text) + JBUI.scale(6)) {
                    logo.paintIcon(this, g2, x, (getHeight() - logo.getIconHeight()) / 2);
                }
                g2.setColor(HEADER_BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
            } finally {
                g2.dispose();
            }
        }
    }

    /** Barra "Components" plegable, con el triangulo de WindowBuilder. */
    private static final class CategoryHeader extends JComponent {

        private final String text;
        private final JComponent target;
        private boolean expanded = true;

        private CategoryHeader(String text, JComponent target) {
            this.text = text;
            this.target = target;
            int h = JBUI.scale(22);
            setPreferredSize(new Dimension(0, h));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Plegar / desplegar la categoria");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    expanded = !expanded;
                    CategoryHeader.this.target.setVisible(expanded);
                    repaint();
                    revalidate();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                UIUtil.applyRenderingHints(g2);
                g2.setPaint(new java.awt.GradientPaint(0, 0, HEADER_BG_TOP, 0, getHeight(), HEADER_BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(HEADER_BORDER);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);

                int cx = JBUI.scale(9);
                int cy = getHeight() / 2;
                g2.setColor(new JBColor(Gray._90, Gray._170));
                int s = JBUI.scale(4);
                if (expanded) {
                    g2.fillPolygon(new int[]{cx - s, cx + s, cx}, new int[]{cy - s + 1, cy - s + 1, cy + s - 1}, 3);
                } else {
                    g2.fillPolygon(new int[]{cx - s + 1, cx + s - 1, cx - s + 1}, new int[]{cy - s, cy, cy + s}, 3);
                }

                g2.setFont(JBUI.Fonts.label().deriveFont(Font.BOLD));
                g2.setColor(new JBColor(Gray._50, Gray._200));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, JBUI.scale(20), (getHeight() + fm.getAscent()) / 2 - JBUI.scale(1));
            } finally {
                g2.dispose();
            }
        }
    }

    /** Una fila de la paleta: icono + nombre de la clase Swing. */
    private final class ItemRow extends JComponent {

        private final WidgetType type;
        private boolean hovered;

        private ItemRow(WidgetType type) {
            this.type = type;
            int h = JBUI.scale(ROW_HEIGHT);
            setPreferredSize(new Dimension(JBUI.scale(150), h));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("<html><b>" + type.getSimpleName() + "</b><br>"
                    + type.getQualifiedName() + "<br><br>"
                    + "Arrastralo al formulario, o haz clic aqui y luego clic en el formulario.</html>");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        setArmedType(armedType == type ? null : type);
                    }
                }
            });

            DragGestureListener gestureListener = new DragGestureListener() {
                @Override
                public void dragGestureRecognized(DragGestureEvent dge) {
                    setArmedType(type);
                    try {
                        dge.startDrag(DragSource.DefaultCopyDrop,
                                new StringSelection(DND_PREFIX + type.name()));
                    } catch (RuntimeException ignored) {
                        // Si el gestor de DnD del sistema no acepta el arrastre queda el
                        // modo "clic + clic", que funciona igual.
                    }
                }
            };
            DragSource.getDefaultDragSource()
                    .createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, gestureListener);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                UIUtil.applyRenderingHints(g2);
                boolean armed = armedType == type;
                if (armed || hovered) {
                    g2.setColor(armed ? ARMED_BG : HOVER_BG);
                    g2.fillRoundRect(JBUI.scale(2), JBUI.scale(1), getWidth() - JBUI.scale(5),
                            getHeight() - JBUI.scale(3), JBUI.scale(4), JBUI.scale(4));
                    if (armed) {
                        g2.setColor(ARMED_BORDER);
                        g2.drawRoundRect(JBUI.scale(2), JBUI.scale(1), getWidth() - JBUI.scale(5),
                                getHeight() - JBUI.scale(3), JBUI.scale(4), JBUI.scale(4));
                    }
                }
                Icon icon = type.getIcon();
                int iconY = (getHeight() - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g2, JBUI.scale(8), iconY);

                g2.setFont(JBUI.Fonts.label());
                g2.setColor(UIUtil.getLabelForeground());
                FontMetrics fm = g2.getFontMetrics();
                int textX = JBUI.scale(8) + icon.getIconWidth() + JBUI.scale(7);
                g2.drawString(type.getSimpleName(), textX, (getHeight() + fm.getAscent()) / 2 - JBUI.scale(1));
            } finally {
                g2.dispose();
            }
        }
    }
}
