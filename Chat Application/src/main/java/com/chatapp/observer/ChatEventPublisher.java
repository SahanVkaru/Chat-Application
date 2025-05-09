
package com.chatapp.observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Publisher for chat events following the Observer design pattern
 */
public class ChatEventPublisher {
    
    private final List<ChatEventListener> listeners = new ArrayList<>();
    
    /**
     * Add a listener for chat events
     */
    public void addListener(ChatEventListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Remove a listener
     */
    public void removeListener(ChatEventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners about a new message
     */
    public void notifyNewMessage(long userId, String nickName, String message, LocalDateTime timestamp) {
        for (ChatEventListener listener : listeners) {
            listener.onNewMessage(userId, nickName, message, timestamp);
        }
    }
    
    /**
     * Notify all listeners about a user join event
     */
    public void notifyUserJoined(long userId, String nickName, LocalDateTime timestamp) {
        for (ChatEventListener listener : listeners) {
            listener.onUserJoined(userId, nickName, timestamp);
        }
    }
    
    /**
     * Notify all listeners about a user leave event
     */
    public void notifyUserLeft(long userId, String nickName, LocalDateTime timestamp) {
        for (ChatEventListener listener : listeners) {
            listener.onUserLeft(userId, nickName, timestamp);
        }
    }
    
    /**
     * Notify all listeners about chat start event
     */
    public void notifyChatStarted(long chatId, String chatName, LocalDateTime timestamp) {
        for (ChatEventListener listener : listeners) {
            listener.onChatStarted(chatId, chatName, timestamp);
        }
    }
    
    /**
     * Notify all listeners about chat end event
     */
    public void notifyChatEnded(long chatId, String chatName, LocalDateTime timestamp) {
        for (ChatEventListener listener : listeners) {
            listener.onChatEnded(chatId, chatName, timestamp);
        }
    }
}
