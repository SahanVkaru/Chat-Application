package com.chatapp.gui;

import com.chatapp.rmi.AdminRemoteInterface;
import com.chatapp.rmi.UserRemoteInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

/**
 * Login GUI for the Chat Application — iPhone-style dark mode design
 */
public class LoginFrame extends JFrame {

    private static final int WIDTH = 420;
    private static final int HEIGHT = 620;
    private static final String RMI_HOST = "localhost";
    private static final int RMI_PORT = 1099;
    private static final String USER_SERVICE_NAME = "UserService";
    private static final String ADMIN_SERVICE_NAME = "AdminService";

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton adminLoginButton;
    private JButton registerButton;
    private JLabel statusLabel;

    private UserRemoteInterface userService;
    private AdminRemoteInterface adminService;

    public LoginFrame() {
        initializeRMI();
        setupUI();
        setVisible(true);
    }

    private void initializeRMI() {
        try {
            Registry registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            userService = (UserRemoteInterface) registry.lookup(USER_SERVICE_NAME);
            adminService = (AdminRemoteInterface) registry.lookup(ADMIN_SERVICE_NAME);
        } catch (RemoteException | NotBoundException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to connect to the chat server: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupUI() {
        setTitle("Chat Application");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(ModernTheme.BG_PRIMARY);

        // Main container
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ModernTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(50, 40, 40, 40));

        // ── App Icon / Avatar Circle ──
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBackground(ModernTheme.BG_PRIMARY);
        iconPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient circle
                GradientPaint gp = new GradientPaint(0, 0, ModernTheme.ACCENT_BLUE,
                        getWidth(), getHeight(), ModernTheme.ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 80, 80);
                // Chat icon text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 32));
                FontMetrics fm = g2.getFontMetrics();
                String text = "💬";
                int textX = (80 - fm.stringWidth(text)) / 2;
                int textY = (80 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, textX, textY);
                g2.dispose();
            }
        };
        iconLabel.setPreferredSize(new Dimension(80, 80));
        iconPanel.add(iconLabel);
        mainPanel.add(iconPanel);

        mainPanel.add(Box.createVerticalStrut(16));

        // ── Title ──
        JLabel titleLabel = new JLabel("Chat App", SwingConstants.CENTER);
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitleLabel.setFont(ModernTheme.FONT_CAPTION);
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(36));

        // ── Form Card ──
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Username row
        JLabel usernameLabel = new JLabel("USERNAME");
        usernameLabel.setFont(ModernTheme.FONT_CAPTION_BOLD);
        usernameLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(usernameLabel);
        formCard.add(Box.createVerticalStrut(6));

        usernameField = ModernTheme.createTextField("Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.INPUT_HEIGHT));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(usernameField);

        formCard.add(Box.createVerticalStrut(16));

        // Password row
        JLabel passwordLabel = new JLabel("PASSWORD");
        passwordLabel.setFont(ModernTheme.FONT_CAPTION_BOLD);
        passwordLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(passwordLabel);
        formCard.add(Box.createVerticalStrut(6));

        passwordField = ModernTheme.createPasswordField("Enter your password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.INPUT_HEIGHT));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(passwordField);

        mainPanel.add(formCard);

        mainPanel.add(Box.createVerticalStrut(24));

        // ── Buttons ──
        loginButton = ModernTheme.createPrimaryButton("Sign In");
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.BUTTON_HEIGHT));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(loginButton);

        mainPanel.add(Box.createVerticalStrut(10));

        adminLoginButton = ModernTheme.createSecondaryButton("Admin Sign In");
        adminLoginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.BUTTON_HEIGHT));
        adminLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(adminLoginButton);

        mainPanel.add(Box.createVerticalStrut(20));

        // ── Register link ──
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        registerPanel.setBackground(ModernTheme.BG_PRIMARY);
        registerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel noAccountLabel = ModernTheme.createSecondaryLabel("Don't have an account?");
        registerButton = ModernTheme.createTextButton("Sign Up");
        registerPanel.add(noAccountLabel);
        registerPanel.add(registerButton);
        mainPanel.add(registerPanel);

        mainPanel.add(Box.createVerticalStrut(8));

        // ── Status Label ──
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(ModernTheme.FONT_CAPTION);
        statusLabel.setForeground(ModernTheme.ACCENT_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);

        // ── Action Listeners ──
        loginButton.addActionListener(e -> handleUserLogin());
        adminLoginButton.addActionListener(e -> handleAdminLogin());
        registerButton.addActionListener(e -> openRegistrationForm());
        passwordField.addActionListener(e -> handleUserLogin());

        add(mainPanel);
    }

    private void handleUserLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required");
            return;
        }

        statusLabel.setText("");
        loginButton.setEnabled(false);

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return userService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> userData = get();
                    long userId = (long) userData.get("id");
                    String nickName = (String) userData.get("nickName");

                    SwingUtilities.invokeLater(() -> {
                        new UserDashboard(userId, userService, nickName);
                        dispose();
                    });
                } catch (Exception ex) {
                    loginButton.setEnabled(true);
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText(msg != null ? msg.replace("java.rmi.RemoteException: ", "") : "Login failed");
                }
            }
        }.execute();
    }

    private void handleAdminLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required");
            return;
        }

        statusLabel.setText("");
        adminLoginButton.setEnabled(false);

        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return adminService.adminLogin(username, password);
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> adminData = get();
                    long adminId = (long) adminData.get("id");

                    SwingUtilities.invokeLater(() -> {
                        new AdminDashboard(adminId, adminService);
                        dispose();
                    });
                } catch (Exception ex) {
                    adminLoginButton.setEnabled(true);
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText(msg != null ? msg.replace("java.rmi.RemoteException: ", "") : "Admin login failed");
                }
            }
        }.execute();
    }

    private void openRegistrationForm() {
        SwingUtilities.invokeLater(() -> {
            new RegistrationFrame(userService, this);
        });
    }
}