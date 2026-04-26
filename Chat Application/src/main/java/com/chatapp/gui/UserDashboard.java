package com.chatapp.gui;

import com.chatapp.rmi.UserClientCallback;
import com.chatapp.rmi.UserRemoteInterface;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * User Dashboard GUI — iPhone-style dark mode design
 */
public class UserDashboard extends JFrame implements UserClientCallback {

    private static final int WIDTH = 860;
    private static final int HEIGHT = 640;

    private final long userId;
    private final UserRemoteInterface userService;
    private final String nickName;
    private UserClientCallback callbackStub;

    private JTabbedPane tabbedPane;
    private JTable chatsTable;
    private DefaultTableModel chatsTableModel;
    private JButton joinChatButton;
    private JButton subscribeButton;
    private JButton unsubscribeButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nickNameField;
    private JButton updateProfileButton;
    private JButton chooseImageButton;
    private JLabel imageLabel;
    private byte[] profilePictureBytes;

    private boolean isInChat = false;
    private ChatFrame chatFrame;

    public UserDashboard(long userId, UserRemoteInterface userService, String nickName) {
        this.userId = userId;
        this.userService = userService;
        this.nickName = nickName;

        setupCallbacks();
        setupUI();
        setVisible(true);
        loadChatsData();
    }

    private void setupCallbacks() {
        try {
            callbackStub = (UserClientCallback) UnicastRemoteObject.exportObject(this, 0);
            userService.registerClient(userId, callbackStub);

            // Add shutdown hook to unregister client
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        userService.unregisterClient(userId);
                        if (isInChat) {
                            try {
                                userService.leaveChat(userId);
                            } catch (Exception ex) {
                                // Ignore if already left
                            }
                        }
                        UnicastRemoteObject.unexportObject(UserDashboard.this, true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to register for notifications: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupUI() {
        setTitle("Chat App");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ModernTheme.BG_PRIMARY);

        // Main layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BG_PRIMARY);

        // ── Top Bar ──
        JPanel topBar = createTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);

        // ── Tabbed Content ──
        tabbedPane = new JTabbedPane();
        ModernTheme.styleTabbedPane(tabbedPane);

        tabbedPane.addTab("  💬  Chats  ", createChatsPanel());
        tabbedPane.addTab("  👤  Profile  ", createProfilePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.BG_SECONDARY);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));
        topBar.setPreferredSize(new Dimension(WIDTH, 60));

        // Welcome message
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        // Avatar circle
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ModernTheme.ACCENT_BLUE,
                        getWidth(), getHeight(), ModernTheme.ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String initial = nickName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (36 - fm.stringWidth(initial)) / 2,
                        (36 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        leftPanel.add(avatar);
        leftPanel.add(Box.createHorizontalStrut(12));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        JLabel welcomeLabel = new JLabel("Welcome back,");
        welcomeLabel.setFont(ModernTheme.FONT_SMALL);
        welcomeLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        namePanel.add(welcomeLabel);
        JLabel nameLabel = new JLabel(nickName);
        nameLabel.setFont(ModernTheme.FONT_SUBHEAD);
        nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        namePanel.add(nameLabel);
        leftPanel.add(namePanel);

        topBar.add(leftPanel, BorderLayout.WEST);

        // Online status
        JLabel statusLabel = ModernTheme.createStatusBadge("● Online", true);
        topBar.add(statusLabel, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createChatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ModernTheme.BG_PRIMARY);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Section header
        JLabel sectionHeader = ModernTheme.createSectionHeader("Available Chats");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Name", "Created At", "Status"};
        chatsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        chatsTable = new JTable(chatsTableModel);
        chatsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernTheme.styleTable(chatsTable);

        chatsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        chatsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        chatsTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        chatsTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scrollPane = ModernTheme.createScrollPane(chatsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonBar.setOpaque(false);
        buttonBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        joinChatButton = ModernTheme.createPrimaryButton("Join Active Chat");
        joinChatButton.setPreferredSize(new Dimension(160, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(joinChatButton);

        subscribeButton = ModernTheme.createSecondaryButton("Subscribe");
        subscribeButton.setPreferredSize(new Dimension(120, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(subscribeButton);

        unsubscribeButton = ModernTheme.createSecondaryButton("Unsubscribe");
        unsubscribeButton.setPreferredSize(new Dimension(130, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(unsubscribeButton);

        JButton refreshButton = ModernTheme.createTextButton("⟳ Refresh");
        buttonBar.add(refreshButton);

        panel.add(buttonBar, BorderLayout.SOUTH);

        // Action listeners
        joinChatButton.addActionListener(e -> joinActiveChat());
        subscribeButton.addActionListener(e -> subscribeToChat());
        unsubscribeButton.addActionListener(e -> unsubscribeFromChat());
        refreshButton.addActionListener(e -> loadChatsData());

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ModernTheme.BG_PRIMARY);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Avatar section
        JPanel avatarSection = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarSection.setOpaque(false);
        avatarSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel avatarLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ModernTheme.ACCENT_BLUE,
                        getWidth(), getHeight(), ModernTheme.ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 80, 80);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                String initial = nickName.substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (80 - fm.stringWidth(initial)) / 2,
                        (80 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatarLabel.setPreferredSize(new Dimension(80, 80));
        avatarSection.add(avatarLabel);
        centerPanel.add(avatarSection);
        centerPanel.add(Box.createVerticalStrut(8));

        JLabel profileName = new JLabel(nickName, SwingConstants.CENTER);
        profileName.setFont(ModernTheme.FONT_HEADLINE);
        profileName.setForeground(ModernTheme.TEXT_PRIMARY);
        profileName.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(profileName);

        centerPanel.add(Box.createVerticalStrut(24));

        // Form card
        JLabel sectionHeader = ModernTheme.createSectionHeader("Edit Profile");
        sectionHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(sectionHeader);
        centerPanel.add(Box.createVerticalStrut(8));

        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setMaximumSize(new Dimension(500, 350));

        // Username
        formCard.add(createFormRow("USERNAME", usernameField = ModernTheme.createTextField("Username")));
        formCard.add(Box.createVerticalStrut(12));

        // Password
        formCard.add(createFormRow("NEW PASSWORD", passwordField = ModernTheme.createPasswordField("Enter new password")));
        formCard.add(Box.createVerticalStrut(12));

        // Nickname
        formCard.add(createFormRow("NICKNAME", nickNameField = ModernTheme.createTextField("Display name")));
        formCard.add(Box.createVerticalStrut(12));

        // Profile Picture
        JLabel picLabel = new JLabel("PROFILE PICTURE");
        picLabel.setFont(ModernTheme.FONT_CAPTION_BOLD);
        picLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        picLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(picLabel);
        formCard.add(Box.createVerticalStrut(6));

        JPanel picRow = new JPanel(new BorderLayout(10, 0));
        picRow.setOpaque(false);
        picRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.BUTTON_HEIGHT));
        picRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        chooseImageButton = ModernTheme.createSecondaryButton("Choose Image");
        chooseImageButton.setPreferredSize(new Dimension(140, ModernTheme.BUTTON_HEIGHT));
        picRow.add(chooseImageButton, BorderLayout.WEST);

        imageLabel = new JLabel("No image selected");
        imageLabel.setFont(ModernTheme.FONT_CAPTION);
        imageLabel.setForeground(ModernTheme.TEXT_TERTIARY);
        picRow.add(imageLabel, BorderLayout.CENTER);

        formCard.add(picRow);

        centerPanel.add(formCard);
        centerPanel.add(Box.createVerticalStrut(20));

        // Update button
        updateProfileButton = ModernTheme.createPrimaryButton("Update Profile");
        updateProfileButton.setMaximumSize(new Dimension(500, ModernTheme.BUTTON_HEIGHT));
        updateProfileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(updateProfileButton);

        panel.add(centerPanel, BorderLayout.NORTH);

        // Action listeners
        chooseImageButton.addActionListener(e -> selectProfilePicture());
        updateProfileButton.addActionListener(e -> updateProfile());

        nickNameField.setText(nickName);
        loadUserProfile();

        return panel;
    }

    private JPanel createFormRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(ModernTheme.FONT_CAPTION_BOLD);
        label.setForeground(ModernTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(label);
        row.add(Box.createVerticalStrut(6));

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.INPUT_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(field);

        return row;
    }

    private void loadUserProfile() {
        try {
            Map<String, Object> profile = userService.getUserProfile(userId);
            usernameField.setText((String) profile.get("username"));
            nickNameField.setText((String) profile.get("nickName"));
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load profile data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChatsData() {
        try {
            List<Map<String, Object>> chats = userService.getAllChats();
            chatsTableModel.setRowCount(0);

            for (Map<String, Object> chat : chats) {
                Vector<Object> row = new Vector<>();
                row.add(chat.get("id"));
                row.add(chat.get("name"));
                row.add(chat.get("createdAt"));

                boolean isActive = (boolean) chat.get("isActive");
                row.add(isActive ? "● Active" : "Inactive");

                chatsTableModel.addRow(row);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load chats: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void joinActiveChat() {
        if (isInChat) {
            JOptionPane.showMessageDialog(this,
                    "You are already in a chat",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Map<String, Object> chatData = userService.joinChat(userId);
            String chatName = (String) chatData.get("chatName");
            long chatId = (long) chatData.get("chatId");
            String startTime = (String) chatData.get("startTime");

            chatFrame = new ChatFrame(chatId, chatName, userId, nickName, userService, this, false);
            chatFrame.appendSystemMessage("Chat started at: " + startTime);
            chatFrame.setVisible(true);
            isInChat = true;

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to join chat: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void subscribeToChat() {
        int selectedRow = chatsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a chat to subscribe to",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        long chatId = (long) chatsTableModel.getValueAt(selectedRow, 0);
        String chatName = (String) chatsTableModel.getValueAt(selectedRow, 1);
        subscribeButton.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userService.subscribeToChat(userId, chatId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(UserDashboard.this,
                            "Subscribed to '" + chatName + "'",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserDashboard.this,
                            "Failed to subscribe: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    subscribeButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void unsubscribeFromChat() {
        int selectedRow = chatsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a chat to unsubscribe from",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final long chatId = (long) chatsTableModel.getValueAt(selectedRow, 0);
        final String chatName = (String) chatsTableModel.getValueAt(selectedRow, 1);
        unsubscribeButton.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                userService.unsubscribeFromChat(userId, chatId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(UserDashboard.this,
                            "Unsubscribed from '" + chatName + "'",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(UserDashboard.this,
                            "Failed to unsubscribe: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    unsubscribeButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void selectProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image Files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(selectedFile);
                if (img != null) {
                    BufferedImage resizedImg = resizeImage(img, 64, 64);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resizedImg, "jpg", baos);
                    profilePictureBytes = baos.toByteArray();
                    imageLabel.setText(selectedFile.getName());
                    imageLabel.setForeground(ModernTheme.ACCENT_GREEN);
                    ImageIcon icon = new ImageIcon(resizedImg);
                    imageLabel.setIcon(icon);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading image: " + e.getMessage(),
                        "Image Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private void updateProfile() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String nickName = nickNameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || nickName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username, password, and nickname are required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            userService.updateUserProfile(userId, username, password, nickName, profilePictureBytes);
            JOptionPane.showMessageDialog(this,
                    "Profile updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            passwordField.setText("");
            profilePictureBytes = null;
            imageLabel.setIcon(null);
            imageLabel.setText("No image selected");
            imageLabel.setForeground(ModernTheme.TEXT_TERTIARY);

        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update profile: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void chatClosed() {
        isInChat = false;
        chatFrame = null;
    }

    // ── UserClientCallback implementation ──

    @Override
    public void receiveMessage(Map<String, Object> messageData) throws RemoteException {
        if (isInChat && chatFrame != null) {
            String nickName = (String) messageData.get("nickName");
            String message = (String) messageData.get("message");
            String timestamp = (String) messageData.get("timestamp");
            chatFrame.appendUserMessage(nickName, message, timestamp);
        }
    }

    @Override
    public void userJoined(Map<String, Object> userData) throws RemoteException {
        if (isInChat && chatFrame != null) {
            String nickName = (String) userData.get("nickName");
            chatFrame.appendSystemMessage(nickName + " joined the chat");
        }
    }

    @Override
    public void userLeft(Map<String, Object> userData) throws RemoteException {
        if (isInChat && chatFrame != null) {
            String nickName = (String) userData.get("nickName");
            chatFrame.appendSystemMessage(nickName + " left the chat");
        }
    }

    @Override
    public void chatStarted(Map<String, Object> chatData) throws RemoteException {
        String chatName = (String) chatData.get("chatName");
        String startTime = (String) chatData.get("startTime");

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Chat '" + chatName + "' has started at " + startTime,
                    "Chat Started",
                    JOptionPane.INFORMATION_MESSAGE);
            loadChatsData();
        });
    }

    @Override
    public void chatEnded(Map<String, Object> chatData) throws RemoteException {
        String chatName = (String) chatData.get("chatName");
        String endTime = (String) chatData.get("endTime");

        SwingUtilities.invokeLater(() -> {
            if (isInChat && chatFrame != null) {
                chatFrame.appendSystemMessage("Chat ended at: " + endTime);
                chatFrame.disableChat();
            }

            JOptionPane.showMessageDialog(this,
                    "Chat '" + chatName + "' has ended at " + endTime,
                    "Chat Ended",
                    JOptionPane.INFORMATION_MESSAGE);

            loadChatsData();
            isInChat = false;
        });
    }

    @Override
    public void subscriptionChanged(boolean subscribed, long chatId) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            loadChatsData();
            String chatName = "the chat";
            for (int i = 0; i < chatsTableModel.getRowCount(); i++) {
                if ((long) chatsTableModel.getValueAt(i, 0) == chatId) {
                    chatName = (String) chatsTableModel.getValueAt(i, 1);
                    break;
                }
            }

            JOptionPane.showMessageDialog(this,
                    subscribed ?
                            "You have been subscribed to '" + chatName + "'" :
                            "You have been unsubscribed from '" + chatName + "'",
                    "Subscription Changed",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Override
    public void userRemoved() throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Your account has been removed by the administrator.",
                    "Account Removed",
                    JOptionPane.WARNING_MESSAGE);
            dispose();
        });
    }
}
