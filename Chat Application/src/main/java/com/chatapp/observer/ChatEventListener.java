
package com.chatapp.observer;

import java.time.LocalDateTime;

/**
 * Interface for chat event observers following the Observer design pattern
 */
public interface ChatEventListener {
    
    /**
     * Called when a new message is received in the chat
     */
    void onNewMessage(long userId, String nickName, String message, LocalDateTime timestamp);
    
    /**
     * Called when a user joins the chat
     */
    void onUserJoined(long userId, String nickName, LocalDateTime timestamp);
    
    /**
     * Called when a user leaves the chat
     */
    void onUserLeft(long userId, String nickName, LocalDateTime timestamp);
    
    /**
     * Called when a chat starts
     */
    void onChatStarted(long chatId, String chatName, LocalDateTime timestamp);
    
    /**
     * Called when a chat ends
     */
    void onChatEnded(long chatId, String chatName, LocalDateTime timestamp);
}
