package com.chatapp;

import com.chatapp.gui.LoginFrame;
import com.chatapp.gui.ModernTheme;
import com.chatapp.rmi.RMIServer;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main application class that launches both the RMI server and client UI
 * Automatically starts both components in the correct sequence
 */
public class MainApplication {

    public static void main(String[] args) {
        // Set up FlatLaf Dark theme with iOS-inspired customizations
        try {
            // Configure FlatLaf properties for iPhone-style appearance
            UIManager.put("Button.arc", ModernTheme.CORNER_RADIUS);
            UIManager.put("Component.arc", ModernTheme.CORNER_RADIUS);
            UIManager.put("TextComponent.arc", ModernTheme.CORNER_RADIUS);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.track", ModernTheme.BG_SECONDARY);
            UIManager.put("ScrollBar.thumb", ModernTheme.BG_ELEVATED);
            UIManager.put("TabbedPane.selectedBackground", ModernTheme.BG_SECONDARY);
            UIManager.put("TabbedPane.underlineColor", ModernTheme.ACCENT_BLUE);
            UIManager.put("TabbedPane.inactiveUnderlineColor", ModernTheme.BG_TERTIARY);
            UIManager.put("TabbedPane.tabHeight", 40);
            UIManager.put("TabbedPane.showTabSeparators", false);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));

            // Install FlatLaf Dark
            FlatDarkLaf.setup();

            // Override default colors
            UIManager.put("Panel.background", ModernTheme.BG_PRIMARY);
            UIManager.put("TextField.background", ModernTheme.INPUT_BG);
            UIManager.put("PasswordField.background", ModernTheme.INPUT_BG);
            UIManager.put("TextField.foreground", ModernTheme.TEXT_PRIMARY);
            UIManager.put("PasswordField.foreground", ModernTheme.TEXT_PRIMARY);
            UIManager.put("TextField.caretForeground", ModernTheme.ACCENT_BLUE);
            UIManager.put("PasswordField.caretForeground", ModernTheme.ACCENT_BLUE);
            UIManager.put("Table.background", ModernTheme.BG_SECONDARY);
            UIManager.put("Table.selectionBackground", ModernTheme.ACCENT_BLUE);
            UIManager.put("Table.gridColor", ModernTheme.SEPARATOR);
            UIManager.put("TableHeader.background", ModernTheme.BG_TERTIARY);
            UIManager.put("TabbedPane.background", ModernTheme.BG_PRIMARY);
            UIManager.put("OptionPane.background", ModernTheme.BG_SECONDARY);
            UIManager.put("OptionPane.messageForeground", ModernTheme.TEXT_PRIMARY);
            UIManager.put("ComboBox.background", ModernTheme.INPUT_BG);
            UIManager.put("List.background", ModernTheme.BG_SECONDARY);
            UIManager.put("List.selectionBackground", ModernTheme.ACCENT_BLUE);

            // Set default font
            UIManager.put("defaultFont", ModernTheme.FONT_BODY);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create an executor service for the server
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Start both server and client automatically
        System.out.println("Starting Chat Application (Server + Client)...");

        // First start the server
        startServer(executor);

        try {
            // Wait for server to fully initialize
            System.out.println("Waiting for server to initialize...");
            Thread.sleep(3000); // 3-second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then start the client
        startClient();
    }

    /**
     * Starts the RMI server in a separate thread
     */
    private static void startServer(ExecutorService executor) {
        executor.submit(() -> {
            try {
                System.out.println("Starting RMI Server...");
                RMIServer.start();
            } catch (Exception e) {
                System.err.println("Error starting RMI server: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Starts the chat client UI
     */
    private static void startClient() {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Starting chat client...");
            new LoginFrame();
        });
    }
}