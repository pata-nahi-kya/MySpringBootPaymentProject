package com.paymentproject.payment.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
@Builder 
public class ChatMessage { 
   private MessageType type; 
   private String content; 
   private String sender; 
   private String receiver;
}

// example of a chat message:
// {
//     "type": "CHAT",
//     "content": "Hello, how are you?",
//     "sender": "Alice",
//     "receiver": "Bob"
// }

