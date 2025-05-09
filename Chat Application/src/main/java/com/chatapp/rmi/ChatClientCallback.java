
package com.chatapp.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;


 //Remote interface for client callbacks

public interface ChatClientCallback extends Remote {
    

     //Notify client about a new message in the chat

    void receiveMessage(Map<String, Object> messageData) throws RemoteException;

     // Notify client about a user joining the chat

    void userJoined(Map<String, Object> userData) throws RemoteException;
    

     //Notify client about a user leaving the chat

    void userLeft(Map<String, Object> userData) throws RemoteException;
    

     //Notify client about chat started event

    void chatStarted(Map<String, Object> chatData) throws RemoteException;
    

      //Notify client about chat ended event

    void chatEnded(Map<String, Object> chatData) throws RemoteException;
    

     //Notify client about subscription changes

    void subscriptionChanged(boolean subscribed, long chatId) throws RemoteException;
}
