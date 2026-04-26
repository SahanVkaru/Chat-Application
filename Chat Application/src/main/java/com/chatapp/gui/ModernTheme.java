package com.chatapp.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * iOS / iPhone-inspired design system for the Chat Application.
 * Provides centralized colors, fonts, and styled component factory methods.
 */
public class ModernTheme {

    // ─── iOS Dark Mode Color Palette ────────────────────────────────────
    public static final Color BG_PRIMARY = new Color(0, 0, 0);               // Pure black (iPhone OLED)
    public static final Color BG_SECONDARY = new Color(28, 28, 30);           // #1C1C1E
    public static final Color BG_TERTIARY = new Color(44, 44, 46);            // #2C2C2E
    public static final Color BG_ELEVATED = new Color(58, 58, 60);            // #3A3A3C
    public static final Color BG_GROUPED = new Color(28, 28, 30);             // Grouped table bg

    public static final Color ACCENT_BLUE = new Color(0, 122, 255);           // #007AFF  (iOS Blue)
    public static final Color ACCENT_GREEN = new Color(48, 209, 88);          // #30D158
    public static final Color ACCENT_RED = new Color(255, 69, 58);            // #FF453A
    public static final Color ACCENT_ORANGE = new Color(255, 159, 10);        // #FF9F0A
    public static final Color ACCENT_PURPLE = new Color(175, 82, 222);        // #AF52DE
    public static final Color ACCENT_TEAL = new Color(100, 210, 255);         // #64D2FF

    public static final Color TEXT_PRIMARY = new Color(255, 255, 255);         // White text
    public static final Color TEXT_SECONDARY = new Color(142, 142, 147);       // #8E8E93
    public static final Color TEXT_TERTIARY = new Color(99, 99, 102);          // #636366
    public static final Color TEXT_PLACEHOLDER = new Color(72, 72, 74);        // #48484A

    public static final Color SEPARATOR = new Color(56, 56, 58);              // #38383A
    public static final Color BUBBLE_SENT = new Color(0, 122, 255);           // iMessage blue bubble
    public static final Color BUBBLE_RECEIVED = new Color(44, 44, 46);        // iMessage gray bubble
    public static final Color BUBBLE_SYSTEM = new Color(28, 28, 30);          // System message bg

    public static final Color INPUT_BG = new Color(44, 44, 46);               // Text field bg
    public static final Color INPUT_BORDER = new Color(56, 56, 58);           // Text field border

    // ─── Typography ─────────────────────────────────────────────────────
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_HEADLINE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI Semibold", Font.PLAIN, 15);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_CAPTION = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_CAPTION_BOLD = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI Semibold", Font.PLAIN, 15);

    // ─── Dimensions ─────────────────────────────────────────────────────
    public static final int CORNER_RADIUS = 14;
    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_LARGE = 24;
    public static final int BUTTON_HEIGHT = 44;     // iOS standard touch target
    public static final int INPUT_HEIGHT = 44;

    // ─── Styled Component Factories ─────────────────────────────────────

    /**
     * Creates an iOS-style rounded button with hover animation.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new RoundedButton(text, ACCENT_BLUE, Color.WHITE);
        return button;
    }

    /**
     * Creates a secondary (outline-style) button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new RoundedButton(text, BG_TERTIARY, ACCENT_BLUE);
        return button;
    }

    /**
     * Creates a destructive (red) button.
     */
    public static JButton createDestructiveButton(String text) {
        JButton button = new RoundedButton(text, ACCENT_RED, Color.WHITE);
        return button;
    }

    /**
     * Creates a subtle text-only button (like iOS navigation links).
     */
    public static JButton createTextButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setForeground(ACCENT_BLUE);
        button.setBackground(BG_PRIMARY);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACCENT_TEAL);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(ACCENT_BLUE);
            }
        });
        return button;
    }

    /**
     * Creates an iOS-style rounded text field with dark background.
     */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, INPUT_HEIGHT));
        field.setOpaque(false);
        // Placeholder behavior
        field.setToolTipText(placeholder);
        return field;
    }

    /**
     * Creates an iOS-style rounded password field.
     */
    public static JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setCaretColor(ACCENT_BLUE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, INPUT_HEIGHT));
        field.setOpaque(false);
        field.setToolTipText(placeholder);
        return field;
    }

    /**
     * Creates an iOS-style grouped card panel (like Settings rows).
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        return panel;
    }

    /**
     * Creates a section header label (like iOS Settings section titles).
     */
    public static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(FONT_CAPTION);
        label.setForeground(TEXT_SECONDARY);
        label.setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_SMALL, PADDING_SMALL, 0));
        return label;
    }

    /**
     * Creates a primary label.
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    /**
     * Creates a secondary/subtitle label.
     */
    public static JLabel createSecondaryLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_CAPTION);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    /**
     * Applies iOS-style dark theme to a JTable.
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_SECONDARY);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(SEPARATOR);
        table.setFont(FONT_BODY);
        table.setRowHeight(48);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setBorder(null);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_TERTIARY);
        header.setForeground(TEXT_SECONDARY);
        header.setFont(FONT_CAPTION_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SEPARATOR));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? BG_SECONDARY : BG_TERTIARY);
                }
                c.setForeground(isSelected ? Color.WHITE : TEXT_PRIMARY);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });
    }

    /**
     * Creates an iOS-style scroll pane (no border, dark track).
     */
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_SECONDARY);
        sp.setBackground(BG_SECONDARY);
        return sp;
    }

    /**
     * Creates a styled separator line.
     */
    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(SEPARATOR);
        sep.setBackground(BG_PRIMARY);
        return sep;
    }

    /**
     * Creates the main content panel with iOS dark background.
     */
    public static JPanel createMainPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_PRIMARY);
        panel.setBorder(new EmptyBorder(0, 0, 0, 0));
        return panel;
    }

    /**
     * Styles a JTabbedPane to look like iOS segmented control.
     */
    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setBackground(BG_PRIMARY);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(FONT_SUBHEAD);
        tabbedPane.setBorder(null);
    }

    /**
     * Creates a status badge (Active/Inactive indicator).
     */
    public static JLabel createStatusBadge(String text, boolean isActive) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isActive ? new Color(48, 209, 88, 40) : new Color(142, 142, 147, 40));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_CAPTION_BOLD);
        badge.setForeground(isActive ? ACCENT_GREEN : TEXT_SECONDARY);
        badge.setHorizontalAlignment(SwingConstants.CENTER);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setOpaque(false);
        return badge;
    }

    // ─── Custom Rounded Button Component ────────────────────────────────

    public static class RoundedButton extends JButton {
        private Color bgColor;
        private Color hoverColor;
        private Color pressedColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text, Color bgColor, Color fgColor) {
            super(text);
            this.bgColor = bgColor;
            this.hoverColor = bgColor.brighter();
            this.pressedColor = bgColor.darker();

            setFont(FONT_BUTTON);
            setForeground(fgColor);
            setBackground(bgColor);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(getPreferredSize().width, BUTTON_HEIGHT));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentBg;
            if (isPressed) {
                currentBg = pressedColor;
            } else if (isHovered) {
                currentBg = hoverColor;
            } else {
                currentBg = bgColor;
            }

            if (!isEnabled()) {
                currentBg = BG_ELEVATED;
                setForeground(TEXT_TERTIARY);
            }

            g2.setColor(currentBg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CORNER_RADIUS, CORNER_RADIUS));

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
