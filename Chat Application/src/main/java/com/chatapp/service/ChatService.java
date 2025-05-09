
package com.chatapp.service;

import com.chatapp.model.dao.ChatDAO;
import com.chatapp.model.dao.ChatSubscriptionDAO;
import com.chatapp.model.entity.Chat;
import com.chatapp.model.entity.ChatSubscription;
import com.chatapp.model.entity.User;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Chat related operations
 */
public class ChatService {
    
    private final ChatDAO chatDAO;
    private final ChatSubscriptionDAO subscriptionDAO;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CHAT_LOGS_DIR = "chat_logs";
    
    public ChatService() {
        this.chatDAO = new ChatDAO();
        this.subscriptionDAO = new ChatSubscriptionDAO();
        
        // Create chat logs directory if it doesn't exist
        createChatLogsDirectory();
    }
    
    private void createChatLogsDirectory() {
        try {
            Path dirPath = Paths.get(CHAT_LOGS_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create chat logs directory: " + e.getMessage());
        }
    }
    
    /**
     * Create a new chat
     */
    public Chat createChat(String name) {
        Chat chat = new Chat(name);
        return chatDAO.save(chat);
    }
    
    /**
     * Subscribe a user to a chat
     */
    public ChatSubscription subscribeUserToChat(User user, Chat chat) {
        Optional<ChatSubscription> existingSubscription = subscriptionDAO.findByUserAndChat(user, chat);
        
        if (existingSubscription.isPresent()) {
            ChatSubscription subscription = existingSubscription.get();
            if (!subscription.isActive()) {
                subscription.setActive(true);
                subscription.setUnsubscribedAt(null);
                subscription.setSubscribedAt(LocalDateTime.now());
                return subscriptionDAO.save(subscription);
            }
            return subscription;
        } else {
            ChatSubscription newSubscription = new ChatSubscription(user, chat);
            return subscriptionDAO.save(newSubscription);
        }
    }
    
    /**
     * Unsubscribe a user from a chat
     */
    public void unsubscribeUserFromChat(User user, Chat chat) {
        subscriptionDAO.unsubscribe(user, chat);
    }
    
    /**
     * Start a chat
     */
    public Chat startChat(Chat chat) {
        // Check if there's already an active chat
        Optional<Chat> activeChat = chatDAO.findActiveChat();
        if (activeChat.isPresent() && !activeChat.get().getId().equals(chat.getId())) {
            throw new IllegalStateException("Another chat is already active");
        }
        
        chat.setActive(true);
        chat.setStartedAt(LocalDateTime.now());
        
        return chatDAO.save(chat);
    }
    
    /**
     * End a chat
     */
    public Chat endChat(Chat chat) throws IOException {
        if (!chat.isActive()) {
            throw new IllegalStateException("Chat is not active");
        }
        
        LocalDateTime endTime = LocalDateTime.now();
        chat.setActive(false);
        chat.setEndedAt(endTime);
        
        // Save chat transcript
        String filePath = saveChatTranscript(chat);
        chat.setFilePath(filePath);
        
        return chatDAO.save(chat);
    }

    /**
     * Save chat transcript to a file
     */
    private String saveChatTranscript(Chat chat) throws IOException {
        String fileName = String.format("%s_chat_%d.txt",
                chat.getStartedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                chat.getId());

        String filePath = Paths.get(CHAT_LOGS_DIR, fileName).toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Chat Name: " + chat.getName() + "\n");
            writer.write("Started at: " + chat.getStartedAt().format(TIME_FORMATTER) + "\n");
            writer.write("Ended at: " + chat.getEndedAt().format(TIME_FORMATTER) + "\n");
            writer.write("----------------------------------------\n");
            // The actual chat content would be written during the chat session
        }

        return filePath;
    }

    /**
     * Get all chats
     */
    public List<Chat> getAllChats() {
        return chatDAO.findAll();
    }
    
    /**
     * Get chat by ID
     */
    public Optional<Chat> getChatById(Long id) {
        return chatDAO.findById(id);
    }
    
    /**
     * Get active chat
     */
    public Optional<Chat> getActiveChat() {
        return chatDAO.findActiveChat();
    }
    
    /**
     * Get all subscribers of a chat
     */
    public List<ChatSubscription> getChatSubscribers(Chat chat) {
        return subscriptionDAO.findActiveSubscriptionsByChat(chat);
    }
    
    /**
     * Check if user is subscribed to chat
     */
    public boolean isUserSubscribedToChat(User user, Chat chat) {
        Optional<ChatSubscription> subscription = subscriptionDAO.findByUserAndChat(user, chat);
        return subscription.isPresent() && subscription.get().isActive();
    }
    
    /**
     * Append message to chat transcript
     */
    public void appendMessageToChatTranscript(Chat chat, User user, String message) throws IOException {
        if (chat.getFilePath() == null) {
            String fileName = String.format("%s_chat_%d.txt", 
                    chat.getStartedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")), 
                    chat.getId());
            
            chat.setFilePath(Paths.get(CHAT_LOGS_DIR, fileName).toString());
            chatDAO.save(chat);
        }
        
        File file = new File(chat.getFilePath());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chat.getFilePath(), true))) {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String formattedMessage = String.format("[%s] %s: %s\n", timestamp, user.getNickName(), message);
            writer.write(formattedMessage);
        }
    }
    
    /**
     * Record user join event in chat transcript
     */
    public void recordUserJoinedChat(Chat chat, User user) throws IOException {
        if (chat.getFilePath() == null) {
            String fileName = String.format("%s_chat_%d.txt", 
                    chat.getStartedAt().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")), 
                    chat.getId());
            
            chat.setFilePath(Paths.get(CHAT_LOGS_DIR, fileName).toString());
            chatDAO.save(chat);
        }
        
        File file = new File(chat.getFilePath());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chat.getFilePath(), true))) {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String message = String.format("[%s] \"%s\" has joined\n", timestamp, user.getNickName());
            writer.write(message);
        }
    }
    
    /**
     * Record user leave event in chat transcript
     */
    public void recordUserLeftChat(Chat chat, User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(chat.getFilePath(), true))) {
            String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
            String message = String.format("[%s] \"%s\" left\n", timestamp, user.getNickName());
            writer.write(message);
        }
    }
}
