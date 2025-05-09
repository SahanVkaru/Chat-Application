
package com.chatapp.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

  //Remote interface for chat operations

public interface ChatRemoteInterface extends Remote {
    

     // Register a new user

    long registerUser(String email, String username, String password, String nickName, byte[] profilePicture) throws RemoteException;
    

     //Authenticate a user

    Map<String, Object> login(String username, String password) throws RemoteException;
    

     // Create a new chat (admin only)

    long createChat(long adminId, String chatName) throws RemoteException;
    

     //Start a chat (admin only)

    void startChat(long adminId, long chatId) throws RemoteException;
    

    // Send a message to the chat

    void sendMessage(long userId, String message) throws RemoteException;
    

     //Subscribe user to a chat

    void subscribeToChat(long userId, long chatId) throws RemoteException;
    

     // Unsubscribe user from a chat

    void unsubscribeFromChat(long userId, long chatId) throws RemoteException;
    

     // Get all available chats

    List<Map<String, Object>> getAllChats() throws RemoteException;
    

      //Get all users (admin only)

    List<Map<String, Object>> getAllUsers(long adminId) throws RemoteException;
    

     //Remove a user from the system (admin only)
    void removeUser(long adminId, long userId) throws RemoteException;
    

     //Register client for receiving notifications

    void registerClient(long userId, ChatClientCallback callback) throws RemoteException;
    

     // Unregister client from receiving notifications

    void unregisterClient(long userId) throws RemoteException;
    

      //Join the active chat

    Map<String, Object> joinChat(long userId) throws RemoteException;
    

     // Leave the active chat

    void leaveChat(long userId) throws RemoteException;
    

    //Update user profile

    void updateUserProfile(long userId, String username, String password, String nickName, byte[] profilePicture) throws RemoteException;
}
