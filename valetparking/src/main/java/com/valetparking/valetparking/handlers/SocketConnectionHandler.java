package com.valetparking.valetparking.handlers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.valetparking.valetparking.helpers.UGVHelper;

public class SocketConnectionHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> webSocketSessions = new ConcurrentHashMap<>();
    UGVHelper ugvHelper = new UGVHelper();

    // This method is executed when client tries to connect
    // to the sockets
    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {

        super.afterConnectionEstablished(session);
        // Logging the connection ID with Connected Message
        System.out.println(session.getId() + " Connected");

        // Adding the session into the list
        webSocketSessions.put(session.getId(), session);

        System.out.println(this.hashCode());

        session.sendMessage(new TextMessage(session.getId()));
    }

    // When client disconnect from WebSocket then this
    // method is called
    @Override
    public void afterConnectionClosed(WebSocketSession session,
            CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        System.out.println(session.getId()
                + " DisConnected");

        // Removing the connection info from the list
        webSocketSessions.remove(session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        super.handleMessage(session, message);

        // TODO - Handle messages sent from UGV

    }

    public void sendMessageToClient(String sessionId, WebSocketMessage<?> message) throws Exception {
        WebSocketSession session = webSocketSessions.getOrDefault(sessionId, null);

        System.out.println(this.hashCode());

        if(session==null || !session.isOpen()) {
            System.out.println("Client has disconnected or does not exist.......");
            return;
        }

        session.sendMessage(message);
        
    }
}
