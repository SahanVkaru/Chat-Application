
package com.chatapp.rmi;

import com.chatapp.model.entity.Chat;
import com.chatapp.model.entity.ChatSubscription;
import com.chatapp.model.entity.User;
import com.chatapp.service.ChatService;
import com.chatapp.service.UserService;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


  //Implementation of the remote chat interface

public class ChatRemoteImpl extends UnicastRemoteObject implements ChatRemoteInterface {
    
    private final UserService userService;
    private final ChatService chatService;
    private final Map<Long, ChatClientCallback> connectedClients;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ChatRemoteImpl() throws RemoteException {
        super();
        this.userService = new UserService();
        this.chatService = new ChatService();
        this.connectedClients = new ConcurrentHashMap<>();
    }
    
    @Override
    public long registerUser(String email, String username, String password, String nickName, byte[] profilePicture) throws RemoteException {
        try {
            User user = userService.register(email, username, password, nickName, profilePicture);
            return user.getId();
        } catch (Exception e) {
            throw new RemoteException("Registration failed: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> login(String username, String password) throws RemoteException {
        Optional<User> optionalUser = userService.authenticate(username, password);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("email", user.getEmail());
            userData.put("username", user.getUsername());
            userData.put("nickName", user.getNickName());
            userData.put("isAdmin", user.isAdmin());
            
            return userData;
        } else {
            throw new RemoteException("Invalid credentials");
        }
    }
    
    @Override
    public long createChat(long adminId, String chatName) throws RemoteException {
        Optional<User> optionalAdmin = userService.getUserById(adminId);
        
        if (optionalAdmin.isPresent() && optionalAdmin.get().isAdmin()) {
            Chat chat = chatService.createChat(chatName);
            return chat.getId();
        } else {
            throw new RemoteException("Only admin can create chats");
        }
    }
    
    @Override
    public void startChat(long adminId, long chatId) throws RemoteException {
        Optional<User> optionalAdmin = userService.getUserById(adminId);
        Optional<Chat> optionalChat = chatService.getChatById(chatId);
        
        if (optionalAdmin.isPresent() && optionalAdmin.get().isAdmin() && optionalChat.isPresent()) {
            try {
                Chat chat = chatService.startChat(optionalChat.get());
                
                // Notify all subscribed users about chat start
                for (ChatSubscription subscription : chatService.getChatSubscribers(chat)) {
                    User subscriber = subscription.getUser();
                    ChatClientCallback callback = connectedClients.get(subscriber.getId());
                    
                    if (callback != null) {
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("chatId", chat.getId());
                        chatData.put("chatName", chat.getName());
                        chatData.put("startTime", chat.getStartedAt().format(formatter));
                        
                        try {
                            callback.chatStarted(chatData);
                        } catch (RemoteException e) {
                            // Client might be disconnected, remove from connected clients
                            connectedClients.remove(subscriber.getId());
                        }
                    }
                }
            } catch (IllegalStateException e) {
                throw new RemoteException(e.getMessage());
            }
        } else {
            throw new RemoteException("Invalid admin ID or chat ID");
        }
    }
    
    @Override
    public void sendMessage(long userId, String message) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        Optional<Chat> optionalActiveChat = chatService.getActiveChat();
        
        if (optionalUser.isPresent() && optionalActiveChat.isPresent()) {
            User user = optionalUser.get();
            Chat activeChat = optionalActiveChat.get();
            
            if (chatService.isUserSubscribedToChat(user, activeChat)) {
                try {
                    // Record message in chat transcript
                    chatService.appendMessageToChatTranscript(activeChat, user, message);
                    
                    // Check if this is a "Bye" message to leave the chat
                    if ("Bye".equalsIgnoreCase(message.trim())) {
                        leaveChat(userId);
                        return;
                    }
                    
                    // Broadcast message to all participants
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("userId", user.getId());
                    messageData.put("nickName", user.getNickName());
                    messageData.put("message", message);
                    messageData.put("timestamp", LocalDateTime.now().format(formatter));
                    messageData.put("hasProfilePicture", user.getProfilePicture() != null && user.getProfilePicture().length > 0);
                    
                    for (ChatSubscription subscription : chatService.getChatSubscribers(activeChat)) {
                        User subscriber = subscription.getUser();
                        ChatClientCallback callback = connectedClients.get(subscriber.getId());
                        
                        if (callback != null) {
                            try {
                                callback.receiveMessage(messageData);
                            } catch (RemoteException e) {
                                // Client might be disconnected, remove from connected clients
                                connectedClients.remove(subscriber.getId());
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RemoteException("Failed to record message: " + e.getMessage());
                }
            } else {
                throw new RemoteException("You are not subscribed to the active chat");
            }
        } else {
            throw new RemoteException("Invalid user ID or no active chat");
        }
    }
    
    @Override
    public void subscribeToChat(long userId, long chatId) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        Optional<Chat> optionalChat = chatService.getChatById(chatId);
        
        if (optionalUser.isPresent() && optionalChat.isPresent()) {
            User user = optionalUser.get();
            Chat chat = optionalChat.get();
            
            chatService.subscribeUserToChat(user, chat);
            
            // Notify the user about subscription change
            ChatClientCallback callback = connectedClients.get(userId);
            if (callback != null) {
                try {
                    callback.subscriptionChanged(true, chatId);
                } catch (RemoteException e) {
                    connectedClients.remove(userId);
                }
            }
        } else {
            throw new RemoteException("Invalid user ID or chat ID");
        }
    }
    
    @Override
    public void unsubscribeFromChat(long userId, long chatId) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        Optional<Chat> optionalChat = chatService.getChatById(chatId);
        
        if (optionalUser.isPresent() && optionalChat.isPresent()) {
            User user = optionalUser.get();
            Chat chat = optionalChat.get();
            
            chatService.unsubscribeUserFromChat(user, chat);
            
            // Notify the user about subscription change
            ChatClientCallback callback = connectedClients.get(userId);
            if (callback != null) {
                try {
                    callback.subscriptionChanged(false, chatId);
                } catch (RemoteException e) {
                    connectedClients.remove(userId);
                }
            }
        } else {
            throw new RemoteException("Invalid user ID or chat ID");
        }
    }
    
    @Override
    public List<Map<String, Object>> getAllChats() throws RemoteException {
        List<Chat> chats = chatService.getAllChats();
        List<Map<String, Object>> chatDataList = new ArrayList<>();
        
        for (Chat chat : chats) {
            Map<String, Object> chatData = new HashMap<>();
            chatData.put("id", chat.getId());
            chatData.put("name", chat.getName());
            chatData.put("isActive", chat.isActive());
            chatData.put("createdAt", chat.getCreatedAt().format(formatter));
            
            if (chat.getStartedAt() != null) {
                chatData.put("startedAt", chat.getStartedAt().format(formatter));
            }
            
            if (chat.getEndedAt() != null) {
                chatData.put("endedAt", chat.getEndedAt().format(formatter));
            }
            
            chatDataList.add(chatData);
        }
        
        return chatDataList;
    }
    
    @Override
    public List<Map<String, Object>> getAllUsers(long adminId) throws RemoteException {
        Optional<User> optionalAdmin = userService.getUserById(adminId);
        
        if (optionalAdmin.isPresent() && optionalAdmin.get().isAdmin()) {
            List<User> users = userService.getAllUsers();
            List<Map<String, Object>> userDataList = new ArrayList<>();
            
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("email", user.getEmail());
                userData.put("username", user.getUsername());
                userData.put("nickName", user.getNickName());
                userData.put("isAdmin", user.isAdmin());
                userData.put("hasProfilePicture", user.getProfilePicture() != null && user.getProfilePicture().length > 0);
                
                userDataList.add(userData);
            }
            
            return userDataList;
        } else {
            throw new RemoteException("Only admin can view all users");
        }
    }
    
    @Override
    public void removeUser(long adminId, long userId) throws RemoteException {
        Optional<User> optionalAdmin = userService.getUserById(adminId);
        Optional<User> optionalUser = userService.getUserById(userId);
        
        if (optionalAdmin.isPresent() && optionalAdmin.get().isAdmin() && optionalUser.isPresent()) {
            User userToRemove = optionalUser.get();
            
            // Cannot remove the admin
            if (userToRemove.isAdmin()) {
                throw new RemoteException("Cannot remove the admin user");
            }
            
            userService.deleteUser(userToRemove);
            
            // If user is connected, disconnect them
            if (connectedClients.containsKey(userId)) {
                connectedClients.remove(userId);
            }
        } else {
            throw new RemoteException("Only admin can remove users");
        }
    }
    
    @Override
    public void registerClient(long userId, ChatClientCallback callback) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        
        if (optionalUser.isPresent()) {
            connectedClients.put(userId, callback);
        } else {
            throw new RemoteException("Invalid user ID");
        }
    }
    
    @Override
    public void unregisterClient(long userId) throws RemoteException {
        connectedClients.remove(userId);
    }
    
    @Override
    public Map<String, Object> joinChat(long userId) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        Optional<Chat> optionalActiveChat = chatService.getActiveChat();
        
        if (optionalUser.isPresent() && optionalActiveChat.isPresent()) {
            User user = optionalUser.get();
            Chat activeChat = optionalActiveChat.get();
            
            if (chatService.isUserSubscribedToChat(user, activeChat)) {
                try {
                    // Record user join in chat transcript
                    chatService.recordUserJoinedChat(activeChat, user);
                    
                    // Prepare response data
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("chatId", activeChat.getId());
                    chatData.put("chatName", activeChat.getName());
                    chatData.put("startTime", activeChat.getStartedAt().format(formatter));
                    
                    // Notify other participants about the user joining
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", user.getId());
                    userData.put("nickName", user.getNickName());
                    userData.put("timestamp", LocalDateTime.now().format(formatter));
                    userData.put("hasProfilePicture", user.getProfilePicture() != null && user.getProfilePicture().length > 0);
                    
                    for (ChatSubscription subscription : chatService.getChatSubscribers(activeChat)) {
                        User subscriber = subscription.getUser();
                        if (!subscriber.getId().equals(user.getId())) {
                            ChatClientCallback callback = connectedClients.get(subscriber.getId());
                            
                            if (callback != null) {
                                try {
                                    callback.userJoined(userData);
                                } catch (RemoteException e) {
                                    connectedClients.remove(subscriber.getId());
                                }
                            }
                        }
                    }
                    
                    return chatData;
                } catch (IOException e) {
                    throw new RemoteException("Failed to join chat: " + e.getMessage());
                }
            } else {
                throw new RemoteException("You are not subscribed to the active chat");
            }
        } else {
            throw new RemoteException("Invalid user ID or no active chat");
        }
    }
    
    @Override
    public void leaveChat(long userId) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        Optional<Chat> optionalActiveChat = chatService.getActiveChat();
        
        if (optionalUser.isPresent() && optionalActiveChat.isPresent()) {
            User user = optionalUser.get();
            Chat activeChat = optionalActiveChat.get();
            
            try {
                // Record user leaving in chat transcript
                chatService.recordUserLeftChat(activeChat, user);
                
                // Notify other participants about the user leaving
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", user.getId());
                userData.put("nickName", user.getNickName());
                userData.put("timestamp", LocalDateTime.now().format(formatter));
                
                for (ChatSubscription subscription : chatService.getChatSubscribers(activeChat)) {
                    User subscriber = subscription.getUser();
                    if (!subscriber.getId().equals(user.getId())) {
                        ChatClientCallback callback = connectedClients.get(subscriber.getId());
                        
                        if (callback != null) {
                            try {
                                callback.userLeft(userData);
                            } catch (RemoteException e) {
                                connectedClients.remove(subscriber.getId());
                            }
                        }
                    }
                }
                
                // Check if this was the last user in the chat
                List<ChatSubscription> activeSubscribers = chatService.getChatSubscribers(activeChat);
                boolean anyOtherActive = false;
                
                for (ChatSubscription subscription : activeSubscribers) {
                    if (!subscription.getUser().getId().equals(user.getId()) && 
                            connectedClients.containsKey(subscription.getUser().getId())) {
                        anyOtherActive = true;
                        break;
                    }
                }
                
                if (!anyOtherActive) {
                    // End the chat since this was the last active user
                    Chat endedChat = chatService.endChat(activeChat);
                    
                    // Notify the user who left about chat ending
                    ChatClientCallback callback = connectedClients.get(userId);
                    if (callback != null) {
                        Map<String, Object> chatData = new HashMap<>();
                        chatData.put("chatId", endedChat.getId());
                        chatData.put("chatName", endedChat.getName());
                        chatData.put("endTime", endedChat.getEndedAt().format(formatter));
                        
                        try {
                            callback.chatEnded(chatData);
                        } catch (RemoteException e) {
                            connectedClients.remove(userId);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RemoteException("Failed to leave chat: " + e.getMessage());
            }
        } else {
            throw new RemoteException("Invalid user ID or no active chat");
        }
    }
    
    @Override
    public void updateUserProfile(long userId, String username, String password, String nickName, byte[] profilePicture) throws RemoteException {
        Optional<User> optionalUser = userService.getUserById(userId);
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            try {
                userService.updateProfile(user, username, password, nickName, profilePicture);
            } catch (Exception e) {
                throw new RemoteException("Failed to update profile: " + e.getMessage());
            }
        } else {
            throw new RemoteException("Invalid user ID");
        }
    }
}
