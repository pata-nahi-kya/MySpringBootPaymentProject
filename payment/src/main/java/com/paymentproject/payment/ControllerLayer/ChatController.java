package com.paymentproject.payment.ControllerLayer;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.paymentproject.payment.Model.ChatMessage;



@Controller
public class ChatController {

    // SimpMessagingTemplate is used to send messages to clients connected via WebSocket
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/private-message")
    public void handlePrivateMessage(ChatMessage message , Principal principal) {
        String sender = principal.getName();
        // Log the received message and sender
        System.out.println("Received private message from " + sender + ": " + message.getContent());
        messagingTemplate.convertAndSendToUser(message.getReceiver(), "/queue/private-messages", message);
    }

    // step 3 : this controller method will receive the message sent by client and then it will broadcast the message to all clients subscribed to /topic/messages
    // topic/messages is the destination that clients will subscribe to receive messages broadcasted by the server
    @MessageMapping("/hello")
    @SendTo("/topic/messages")
    public String testMessage(String message, Principal principal) {
        System.out.println("Received: " + message);
        if (principal != null) {
            return "Server received from " + principal.getName() + ": " + message;
        }
        return "Server received from Anonymous: " + message;
    }
}
