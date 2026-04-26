package com.chatapp.gui;

import com.chatapp.rmi.UserRemoteInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.rmi.RemoteException;

/**
 * Registration GUI for the Chat Application — iPhone-style dark mode design
 */
public class RegistrationFrame extends JFrame {

    private static final int WIDTH = 440;
    private static final int HEIGHT = 720;

    private JTextField emailField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField nickNameField;
    private JButton chooseImageButton;
    private JLabel imageLabel;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    private UserRemoteInterface userService;
    private LoginFrame parentFrame;
    private byte[] profilePicture;

    public RegistrationFrame(UserRemoteInterface userService, LoginFrame parentFrame) {
        this.userService = userService;
        this.parentFrame = parentFrame;
        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        setTitle("Create Account");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame);
        setResizable(false);
        getContentPane().setBackground(ModernTheme.BG_PRIMARY);

        // Main container with scrolling
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(ModernTheme.BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // ── Header ──
        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(ModernTheme.FONT_TITLE);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Fill in your details to get started", SwingConstants.CENTER);
        subtitleLabel.setFont(ModernTheme.FONT_CAPTION);
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);

        mainPanel.add(Box.createVerticalStrut(28));

        // ── Form Card ──
        JPanel formCard = ModernTheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        // Email
        formCard.add(createFieldGroup("EMAIL", emailField = ModernTheme.createTextField("your@email.com")));
        formCard.add(Box.createVerticalStrut(14));

        // Username
        formCard.add(createFieldGroup("USERNAME", usernameField = ModernTheme.createTextField("Choose a username")));
        formCard.add(Box.createVerticalStrut(14));

        // Password
        formCard.add(createFieldGroup("PASSWORD", passwordField = ModernTheme.createPasswordField("At least 6 characters")));
        formCard.add(Box.createVerticalStrut(14));

        // Confirm Password
        formCard.add(createFieldGroup("CONFIRM PASSWORD", confirmPasswordField = ModernTheme.createPasswordField("Re-enter password")));
        formCard.add(Box.createVerticalStrut(14));

        // Nickname
        formCard.add(createFieldGroup("NICKNAME", nickNameField = ModernTheme.createTextField("Display name")));
        formCard.add(Box.createVerticalStrut(14));

        // Profile Picture
        JLabel picLabel = new JLabel("PROFILE PICTURE");
        picLabel.setFont(ModernTheme.FONT_CAPTION_BOLD);
        picLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        picLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(picLabel);
        formCard.add(Box.createVerticalStrut(6));

        JPanel imageRow = new JPanel(new BorderLayout(10, 0));
        imageRow.setOpaque(false);
        imageRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.BUTTON_HEIGHT));
        imageRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        chooseImageButton = ModernTheme.createSecondaryButton("Choose Image");
        chooseImageButton.setPreferredSize(new Dimension(140, ModernTheme.BUTTON_HEIGHT));
        imageRow.add(chooseImageButton, BorderLayout.WEST);

        imageLabel = new JLabel("No image selected");
        imageLabel.setFont(ModernTheme.FONT_CAPTION);
        imageLabel.setForeground(ModernTheme.TEXT_TERTIARY);
        imageRow.add(imageLabel, BorderLayout.CENTER);

        formCard.add(imageRow);

        mainPanel.add(formCard);

        mainPanel.add(Box.createVerticalStrut(24));

        // ── Buttons ──
        registerButton = ModernTheme.createPrimaryButton("Create Account");
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.BUTTON_HEIGHT));
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(registerButton);

        mainPanel.add(Box.createVerticalStrut(10));

        cancelButton = ModernTheme.createTextButton("← Back to Sign In");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(cancelButton);

        mainPanel.add(Box.createVerticalStrut(8));

        // ── Status ──
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(ModernTheme.FONT_CAPTION);
        statusLabel.setForeground(ModernTheme.ACCENT_RED);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statusLabel);

        // ── Action Listeners ──
        registerButton.addActionListener(e -> handleRegistration());
        cancelButton.addActionListener(e -> dispose());
        chooseImageButton.addActionListener(e -> selectProfilePicture());

        add(mainPanel);
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(ModernTheme.FONT_CAPTION_BOLD);
        label.setForeground(ModernTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(label);
        group.add(Box.createVerticalStrut(6));

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, ModernTheme.INPUT_HEIGHT));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.add(field);

        return group;
    }

    private void selectProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            imageLabel.setText(selectedFile.getName());
            imageLabel.setForeground(ModernTheme.ACCENT_GREEN);

            try {
                profilePicture = Files.readAllBytes(selectedFile.toPath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to read image file: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                profilePicture = null;
                imageLabel.setText("No image selected");
                imageLabel.setForeground(ModernTheme.TEXT_TERTIARY);
            }
        }
    }

    private void handleRegistration() {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String nickName = nickNameField.getText().trim();

        // Validate input
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nickName.isEmpty()) {
            statusLabel.setText("All fields are required");
            return;
        }

        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords don't match");
            return;
        }

        // Simple email validation
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Invalid email format");
            return;
        }

        statusLabel.setText("");
        registerButton.setEnabled(false);

        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                return userService.registerUser(email, username, password, nickName, profilePicture);
            }

            @Override
            protected void done() {
                try {
                    long userId = get();
                    JOptionPane.showMessageDialog(RegistrationFrame.this,
                            "Account created successfully!\nYour user ID is: " + userId,
                            "Welcome!",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception ex) {
                    registerButton.setEnabled(true);
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    statusLabel.setText(msg != null ? msg.replace("java.rmi.RemoteException: ", "") : "Registration failed");
                }
            }
        }.execute();
    }

}