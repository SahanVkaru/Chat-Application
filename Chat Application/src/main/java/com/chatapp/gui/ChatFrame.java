package com.chatapp.gui;

import com.chatapp.rmi.UserRemoteInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.rmi.RemoteException;

/**
 * Chat Interface GUI — iMessage-style with message bubbles
 */
public class ChatFrame extends JFrame {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 650;

    private final long chatId;
    private final String chatName;
    private final long userId;
    private final String nickName;
    private final Object remoteService;
    private final Object parent;
    private final boolean isAdmin;

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;
    private JLabel typingLabel;

    public ChatFrame(long chatId, String chatName, long userId, String nickName,
                     Object remoteService, Object parent, boolean isAdmin) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.userId = userId;
        this.nickName = nickName;
        this.remoteService = remoteService;
        this.parent = parent;
        this.isAdmin = isAdmin;

        setupUI();

        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                leaveChat();
            }
        });
    }

    private void setupUI() {
        setTitle(chatName);
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ModernTheme.BG_PRIMARY);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BG_PRIMARY);

        // ── Top Bar (iOS-style navigation bar) ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.BG_SECONDARY);
        topBar.setBorder(new EmptyBorder(12, 16, 12, 16));
        topBar.setPreferredSize(new Dimension(WIDTH, 60));

        JLabel chatTitle = new JLabel(chatName, SwingConstants.CENTER);
        chatTitle.setFont(ModernTheme.FONT_SUBHEAD);
        chatTitle.setForeground(ModernTheme.TEXT_PRIMARY);
        topBar.add(chatTitle, BorderLayout.CENTER);

        JLabel onlineIndicator = new JLabel("● Live");
        onlineIndicator.setFont(ModernTheme.FONT_SMALL);
        onlineIndicator.setForeground(ModernTheme.ACCENT_GREEN);
        topBar.add(onlineIndicator, BorderLayout.EAST);

        mainPanel.add(topBar, BorderLayout.NORTH);

        // ── Messages Area (scrollable panel of bubbles) ──
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(ModernTheme.BG_PRIMARY);
        messagesPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ModernTheme.BG_PRIMARY);
        scrollPane.getViewport().setBackground(ModernTheme.BG_PRIMARY);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // ── Input Area (iOS-style message bar) ──
        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(ModernTheme.BG_SECONDARY);
        inputContainer.setBorder(new EmptyBorder(8, 12, 8, 12));

        JPanel inputBar = new JPanel(new BorderLayout(8, 0));
        inputBar.setOpaque(false);

        messageField = ModernTheme.createTextField("Message");
        messageField.setPreferredSize(new Dimension(0, 38));
        inputBar.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("↑") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ModernTheme.ACCENT_BLUE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                String text = "↑";
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        sendButton.setPreferredSize(new Dimension(36, 36));
        sendButton.setBorderPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        inputBar.add(sendButton, BorderLayout.EAST);

        inputContainer.add(inputBar, BorderLayout.CENTER);

        // Status & typing
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(4, 4, 0, 4));

        statusLabel = new JLabel("Connected as: " + nickName + (isAdmin ? " (Admin)" : ""));
        statusLabel.setFont(ModernTheme.FONT_SMALL);
        statusLabel.setForeground(ModernTheme.TEXT_TERTIARY);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        inputContainer.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(inputContainer, BorderLayout.SOUTH);

        // ── Action listeners ──
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        add(mainPanel);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        messageField.setText("");
        messageField.requestFocus();

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (isAdmin) {
                        appendSystemMessage("Admin messaging not implemented in this version");
                    } else {
                        ((UserRemoteInterface) remoteService).sendMessage(userId, message);
                    }

                    if ("Bye".equalsIgnoreCase(message)) {
                        SwingUtilities.invokeLater(() -> {
                            notifyParentChatClosed();
                            dispose();
                        });
                    }
                } catch (RemoteException e) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(ChatFrame.this,
                                "Failed to send message: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
                return null;
            }
        }.execute();
    }

    private void leaveChat() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (!isAdmin) {
                        ((UserRemoteInterface) remoteService).leaveChat(userId);
                    }
                } catch (RemoteException e) {
                    System.err.println("Error leaving chat: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                notifyParentChatClosed();
            }
        }.execute();
    }

    private void notifyParentChatClosed() {
        if (parent instanceof AdminDashboard) {
            ((AdminDashboard) parent).chatClosed();
        } else if (parent instanceof UserDashboard) {
            ((UserDashboard) parent).chatClosed();
        }
    }

    // ── Message Bubble Rendering ──

    /**
     * Display a system message (join/leave/info) — centered, subtle.
     */
    public void appendSystemMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

            JLabel label = new JLabel(message) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(60, 60, 67, 80));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            label.setFont(ModernTheme.FONT_SMALL);
            label.setForeground(ModernTheme.TEXT_SECONDARY);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(new EmptyBorder(4, 14, 4, 14));
            label.setOpaque(false);

            wrapper.add(label);
            messagesPanel.add(wrapper);
            messagesPanel.add(Box.createVerticalStrut(4));
            scrollToBottom();
        });
    }

    /**
     * Display a user message as an iMessage-style bubble.
     * Self messages = blue, right-aligned. Others = gray, left-aligned.
     */
    public void appendUserMessage(String userName, String message, String timestamp) {
        SwingUtilities.invokeLater(() -> {
            boolean isSelf = userName.equals(nickName);

            JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
            wrapper.setOpaque(false);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

            JPanel bubbleContainer = new JPanel();
            bubbleContainer.setLayout(new BoxLayout(bubbleContainer, BoxLayout.Y_AXIS));
            bubbleContainer.setOpaque(false);

            // Sender name (only for others)
            if (!isSelf) {
                JLabel nameLabel = new JLabel(userName);
                nameLabel.setFont(ModernTheme.FONT_CAPTION_BOLD);
                nameLabel.setForeground(ModernTheme.ACCENT_TEAL);
                nameLabel.setBorder(new EmptyBorder(0, 6, 2, 0));
                nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                bubbleContainer.add(nameLabel);
            }

            // Message bubble
            Color bubbleColor = isSelf ? ModernTheme.BUBBLE_SENT : ModernTheme.BUBBLE_RECEIVED;

            JPanel bubble = new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(bubbleColor);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                    g2.dispose();
                }
            };
            bubble.setOpaque(false);
            bubble.setBorder(new EmptyBorder(8, 14, 8, 14));

            // Calculate preferred width based on text
            FontMetrics fm = getFontMetrics(ModernTheme.FONT_BODY);
            int textWidth = fm.stringWidth(message);
            int maxBubbleWidth = (int) (WIDTH * 0.65);
            int bubbleWidth = Math.min(textWidth + 32, maxBubbleWidth);

            JTextArea messageText = new JTextArea(message);
            messageText.setFont(ModernTheme.FONT_BODY);
            messageText.setForeground(isSelf ? Color.WHITE : ModernTheme.TEXT_PRIMARY);
            messageText.setOpaque(false);
            messageText.setEditable(false);
            messageText.setLineWrap(true);
            messageText.setWrapStyleWord(true);
            messageText.setMaximumSize(new Dimension(maxBubbleWidth - 32, Integer.MAX_VALUE));
            messageText.setBorder(null);

            bubble.add(messageText, BorderLayout.CENTER);

            // Timestamp
            JLabel timeLabel = new JLabel(timestamp);
            timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            timeLabel.setForeground(isSelf ? new Color(255, 255, 255, 160) : ModernTheme.TEXT_TERTIARY);
            timeLabel.setBorder(new EmptyBorder(2, 0, 0, 0));
            bubble.add(timeLabel, BorderLayout.SOUTH);

            bubble.setMaximumSize(new Dimension(maxBubbleWidth, Integer.MAX_VALUE));
            bubble.setAlignmentX(isSelf ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            bubbleContainer.add(bubble);

            wrapper.add(bubbleContainer);
            messagesPanel.add(wrapper);
            messagesPanel.add(Box.createVerticalStrut(4));
            scrollToBottom();
        });
    }

    /**
     * @deprecated Use appendUserMessage or appendSystemMessage instead
     */
    public void appendToChat(String text, SimpleAttributeSet style) {
        // Legacy compatibility — treat as system message
        appendSystemMessage(text.trim());
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            messagesPanel.revalidate();
            messagesPanel.repaint();
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void disableChat() {
        SwingUtilities.invokeLater(() -> {
            messageField.setEnabled(false);
            sendButton.setEnabled(false);
            statusLabel.setText("Chat has ended");
            statusLabel.setForeground(ModernTheme.ACCENT_RED);
            revalidate();
            repaint();
        });
    }
}