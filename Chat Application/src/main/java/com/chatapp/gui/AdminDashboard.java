package com.chatapp.gui;

import com.chatapp.rmi.AdminClientCallback;
import com.chatapp.rmi.AdminRemoteInterface;
import com.chatapp.rmi.UserClientCallback;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Admin Dashboard GUI — iPhone-style dark mode design
 */
public class AdminDashboard extends JFrame implements AdminClientCallback {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 720;

    private final long adminId;
    private final AdminRemoteInterface adminService;
    private AdminClientCallback callbackStub;

    private JTabbedPane tabbedPane;
    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JTable chatsTable;
    private DefaultTableModel chatsTableModel;
    private JButton createChatButton;
    private JButton startChatButton;
    private JButton endChatButton;
    private JButton subscribeUserButton;
    private JButton unsubscribeUserButton;
    private JButton removeUserButton;
    private JButton refreshButton;

    private boolean isInChat = false;
    private ChatFrame chatFrame;

    public AdminDashboard(long adminId, AdminRemoteInterface adminService) {
        this.adminId = adminId;
        this.adminService = adminService;

        setupCallbacks();
        setupUI();
        setVisible(true);
        loadData();
    }

    private void setupCallbacks() {
        try {
            callbackStub = (AdminClientCallback) UnicastRemoteObject.exportObject(this, 0);
            adminService.registerAdminClient(adminId, callbackStub);

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        adminService.unregisterAdminClient(adminId);
                        UnicastRemoteObject.unexportObject(AdminDashboard.this, true);
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
        setTitle("Chat App — Admin");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ModernTheme.BG_PRIMARY);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BG_PRIMARY);

        // ── Top Bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(ModernTheme.BG_SECONDARY);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));
        topBar.setPreferredSize(new Dimension(WIDTH, 60));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        // Admin shield icon
        JLabel shieldIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ModernTheme.ACCENT_ORANGE,
                        getWidth(), getHeight(), ModernTheme.ACCENT_RED);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String text = "⚙";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (36 - fm.stringWidth(text)) / 2,
                        (36 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        shieldIcon.setPreferredSize(new Dimension(36, 36));
        leftPanel.add(shieldIcon);
        leftPanel.add(Box.createHorizontalStrut(12));

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);
        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(ModernTheme.FONT_SMALL);
        roleLabel.setForeground(ModernTheme.ACCENT_ORANGE);
        namePanel.add(roleLabel);
        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(ModernTheme.FONT_SUBHEAD);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        namePanel.add(titleLabel);
        leftPanel.add(namePanel);

        topBar.add(leftPanel, BorderLayout.WEST);

        JLabel adminBadge = ModernTheme.createStatusBadge("Admin", true);
        topBar.add(adminBadge, BorderLayout.EAST);

        mainPanel.add(topBar, BorderLayout.NORTH);

        // ── Tabbed Content ──
        tabbedPane = new JTabbedPane();
        ModernTheme.styleTabbedPane(tabbedPane);

        tabbedPane.addTab("  👥  Users  ", createUsersPanel());
        tabbedPane.addTab("  💬  Chats  ", createChatsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ModernTheme.BG_PRIMARY);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel sectionHeader = ModernTheme.createSectionHeader("Registered Users");
        panel.add(sectionHeader, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Email", "Username", "Nickname", "Admin"};
        usersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        usersTable = new JTable(usersTableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ModernTheme.styleTable(usersTable);

        usersTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        usersTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        usersTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        usersTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        usersTable.getColumnModel().getColumn(4).setPreferredWidth(60);

        JScrollPane scrollPane = ModernTheme.createScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonBar.setOpaque(false);
        buttonBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        removeUserButton = ModernTheme.createDestructiveButton("Remove User");
        removeUserButton.setPreferredSize(new Dimension(140, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(removeUserButton);

        refreshButton = ModernTheme.createTextButton("⟳ Refresh");
        buttonBar.add(refreshButton);

        panel.add(buttonBar, BorderLayout.SOUTH);

        removeUserButton.addActionListener(e -> removeSelectedUser());
        refreshButton.addActionListener(e -> loadUsersData());

        return panel;
    }

    private JPanel createChatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(ModernTheme.BG_PRIMARY);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // ── Create Chat Section ──
        JPanel createSection = new JPanel();
        createSection.setLayout(new BoxLayout(createSection, BoxLayout.Y_AXIS));
        createSection.setOpaque(false);

        JLabel createHeader = ModernTheme.createSectionHeader("Create New Chat");
        createHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        createSection.add(createHeader);
        createSection.add(Box.createVerticalStrut(8));

        JPanel createCard = ModernTheme.createCardPanel();
        createCard.setLayout(new BorderLayout(10, 0));
        createCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        createCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField chatNameField = ModernTheme.createTextField("Enter chat name...");
        createCard.add(chatNameField, BorderLayout.CENTER);

        createChatButton = ModernTheme.createPrimaryButton("Create");
        createChatButton.setPreferredSize(new Dimension(100, ModernTheme.BUTTON_HEIGHT));
        createCard.add(createChatButton, BorderLayout.EAST);

        createSection.add(createCard);
        createSection.add(Box.createVerticalStrut(12));

        JLabel chatListHeader = ModernTheme.createSectionHeader("All Chats");
        chatListHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        createSection.add(chatListHeader);

        panel.add(createSection, BorderLayout.NORTH);

        // ── Chats Table ──
        String[] columnNames = {"ID", "Name", "Created At", "Status", "Subscribers"};
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
        chatsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        chatsTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        chatsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        chatsTable.getColumnModel().getColumn(4).setPreferredWidth(90);

        JScrollPane scrollPane = ModernTheme.createScrollPane(chatsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // ── Action Buttons ──
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        buttonBar.setOpaque(false);
        buttonBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        startChatButton = ModernTheme.createPrimaryButton("Start Chat");
        startChatButton.setPreferredSize(new Dimension(120, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(startChatButton);

        endChatButton = ModernTheme.createDestructiveButton("End Chat");
        endChatButton.setPreferredSize(new Dimension(110, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(endChatButton);

        subscribeUserButton = ModernTheme.createSecondaryButton("Subscribe User");
        subscribeUserButton.setPreferredSize(new Dimension(150, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(subscribeUserButton);

        unsubscribeUserButton = ModernTheme.createSecondaryButton("Unsubscribe User");
        unsubscribeUserButton.setPreferredSize(new Dimension(160, ModernTheme.BUTTON_HEIGHT));
        buttonBar.add(unsubscribeUserButton);

        JButton refreshChatsButton = ModernTheme.createTextButton("⟳ Refresh");
        buttonBar.add(refreshChatsButton);

        panel.add(buttonBar, BorderLayout.SOUTH);

        // ── Action Listeners ──
        createChatButton.addActionListener(e -> {
            String chatName = chatNameField.getText().trim();
            if (!chatName.isEmpty()) {
                createNewChat(chatName);
                chatNameField.setText("");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please enter a chat name",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        startChatButton.addActionListener(e -> startSelectedChat());
        endChatButton.addActionListener(e -> endSelectedChat());
        subscribeUserButton.addActionListener(e -> showSubscriptionDialog(true));
        unsubscribeUserButton.addActionListener(e -> showSubscriptionDialog(false));
        refreshChatsButton.addActionListener(e -> loadChatsData());

        return panel;
    }

    private void loadData() {
        loadUsersData();
        loadChatsData();
    }

    private void loadUsersData() {
        try {
            List<Map<String, Object>> users = adminService.getAllUsers();
            usersTableModel.setRowCount(0);

            for (Map<String, Object> user : users) {
                Vector<Object> row = new Vector<>();
                row.add(user.get("id"));
                row.add(user.get("email"));
                row.add(user.get("username"));
                row.add(user.get("nickName"));
                row.add((boolean) user.get("isAdmin") ? "✓ Admin" : "User");

                usersTableModel.addRow(row);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChatsData() {
        try {
            List<Map<String, Object>> chats = adminService.getAdminChatList();
            chatsTableModel.setRowCount(0);

            for (Map<String, Object> chat : chats) {
                Vector<Object> row = new Vector<>();
                row.add(chat.get("id"));
                row.add(chat.get("name"));
                row.add(chat.get("createdAt"));

                boolean isActive = (boolean) chat.get("isActive");
                row.add(isActive ? "● Active" : "Inactive");
                row.add(chat.get("subscriberCount"));

                chatsTableModel.addRow(row);
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load chats: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeSelectedUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a user to remove",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        long userId = (long) usersTableModel.getValueAt(selectedRow, 0);
        String role = (String) usersTableModel.getValueAt(selectedRow, 4);

        if ("✓ Admin".equals(role)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot remove admin user",
                    "Operation Not Allowed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove this user?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                adminService.removeUser(userId);
                loadUsersData();
                JOptionPane.showMessageDialog(this,
                        "User removed successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to remove user: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void createNewChat(String chatName) {
        try {
            adminService.createChat(chatName);
            loadChatsData();
            JOptionPane.showMessageDialog(this,
                    "Chat '" + chatName + "' created!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to create chat: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startSelectedChat() {
        int selectedRow = chatsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a chat to start",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final long chatId = (long) chatsTableModel.getValueAt(selectedRow, 0);
        final String chatName = (String) chatsTableModel.getValueAt(selectedRow, 1);
        String status = (String) chatsTableModel.getValueAt(selectedRow, 3);

        if ("● Active".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "This chat is already active",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        startChatButton.setEnabled(false);

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                adminService.startChat(chatId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadChatsData();
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Chat '" + chatName + "' started!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Failed to start chat: " + e.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    startChatButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void endSelectedChat() {
        int selectedRow = chatsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a chat to end",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        final long chatId = (long) chatsTableModel.getValueAt(selectedRow, 0);
        final String chatName = (String) chatsTableModel.getValueAt(selectedRow, 1);
        String status = (String) chatsTableModel.getValueAt(selectedRow, 3);

        if (!"● Active".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "This chat is not active",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to end '" + chatName + "'?",
                "Confirm End Chat",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            endChatButton.setEnabled(false);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    adminService.endChat(chatId);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadChatsData();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(AdminDashboard.this,
                                "Failed to end chat: " + e.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        endChatButton.setEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private void showSubscriptionDialog(boolean subscribe) {
        int selectedRow = chatsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a chat first",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        long chatId = (long) chatsTableModel.getValueAt(selectedRow, 0);

        try {
            List<Map<String, Object>> users = adminService.getAllUsers();
            if (users.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No users available",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] userNames = new String[users.size()];
            long[] userIds = new long[users.size()];

            for (int i = 0; i < users.size(); i++) {
                Map<String, Object> user = users.get(i);
                userNames[i] = user.get("username") + " (" + user.get("nickName") + ")";
                userIds[i] = (long) user.get("id");
            }

            String selectedUser = (String) JOptionPane.showInputDialog(
                    this,
                    subscribe ? "Select user to subscribe:" : "Select user to unsubscribe:",
                    subscribe ? "Subscribe User" : "Unsubscribe User",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    userNames,
                    userNames[0]);

            if (selectedUser != null) {
                int index = -1;
                for (int i = 0; i < userNames.length; i++) {
                    if (userNames[i].equals(selectedUser)) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) {
                    long userId = userIds[index];

                    if (subscribe) {
                        adminService.subscribeUserToChat(userId, chatId);
                        JOptionPane.showMessageDialog(this,
                                "User subscribed successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        adminService.unsubscribeUserFromChat(userId, chatId);
                        JOptionPane.showMessageDialog(this,
                                "User unsubscribed successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    loadChatsData();
                }
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this,
                    "Operation failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── AdminClientCallback implementation ──

    @Override
    public void userJoinedChat(Map<String, Object> userData) throws RemoteException {
        SwingUtilities.invokeLater(() -> loadChatsData());
    }

    @Override
    public void userLeftChat(Map<String, Object> userData) throws RemoteException {
        SwingUtilities.invokeLater(() -> loadChatsData());
    }

    @Override
    public void chatStarted(Map<String, Object> chatData) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            String chatName = (String) chatData.get("chatName");
            String startTime = (String) chatData.get("startTime");

            JOptionPane.showMessageDialog(this,
                    "Chat '" + chatName + "' started at " + startTime,
                    "Chat Started",
                    JOptionPane.INFORMATION_MESSAGE);
            loadChatsData();
        });
    }

    @Override
    public void chatEnded(Map<String, Object> chatData) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            String chatName = (String) chatData.get("chatName");
            String endTime = (String) chatData.get("endTime");

            JOptionPane.showMessageDialog(this,
                    "Chat '" + chatName + "' ended at " + endTime,
                    "Chat Ended",
                    JOptionPane.INFORMATION_MESSAGE);
            loadChatsData();
        });
    }

    @Override
    public void userRegistered(Map<String, Object> userData) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            loadUsersData();
            String username = (String) userData.get("username");
            JOptionPane.showMessageDialog(this,
                    "New user registered: " + username,
                    "User Registered",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }

    @Override
    public void chatActivityUpdate(Map<String, Object> activityData) throws RemoteException {
        SwingUtilities.invokeLater(() -> loadChatsData());
    }

    public void chatClosed() {
        isInChat = false;
        chatFrame = null;
        startChatButton.setEnabled(true);
        loadChatsData();
    }
}