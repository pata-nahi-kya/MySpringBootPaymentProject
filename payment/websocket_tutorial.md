# End-to-End Private Messaging with WebSockets in Spring Boot

Based on the code in your `payment` project, you have already set up a solid foundation for WebSocket communication. This guide will walk you through how WebSockets work in Spring Boot and how your implementation enables private, 1-on-1 messaging.

---

## 1. How WebSockets Work in Spring Boot
Traditional HTTP requests are stateless and one-directional (Client -> Server -> Client). WebSockets create a **persistent, bi-directional connection**. This means both the client and the server can send messages to each other at any time without the client having to constantly refresh or poll for new data.

Spring Boot implements WebSockets using **STOMP** (Simple Text Oriented Messaging Protocol) over **SockJS**. STOMP acts like a "post office" routing messages between clients based on "destinations" (like topics or queues).

### Key Concepts:
- **Broker**: A message broker routes messages. `spring-boot-starter-websocket` provides a simple, in-memory broker out of the box.
- **/topic**: Used for broadcasting messages to *multiple* users (like a public chat room).
- **/queue**: Used for routing messages to a *specific* user (private messaging).
- **/app**: The prefix for messages sent *from* the client *to* your `@MessageMapping` controllers.
- **/user**: A special prefix built into Spring to route messages exclusively to a specific authenticated user.

---

## 2. Your Project Configuration

Your project configuration is perfectly set up for private messaging. Here is how your code makes it happen:

### The WebSocket Configuration (`WebSocketConfig.java`)

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // Exposes the endpoint clients will connect to
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // Configures the message broker
        registry.enableSimpleBroker("/topic", "/queue", "/user");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
```
**Why this matters:**
1. Clients will connect to the `http://localhost:8080/ws` endpoint.
2. If a client sends a message to `/app/something`, it will be routed to your `ChatController`.
3. If the server sends a message to `/queue/...` or `/topic/...`, the broker delivers it to any subscribed clients.

### The Controller (`ChatController.java`)

Your `ChatController` uses `SimpMessagingTemplate` to route private messages.

```java
@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/private-message")
    public void handlePrivateMessage(ChatMessage message) {
        // This is the magic line for private messaging!
        messagingTemplate.convertAndSendToUser(
            message.getReceiver(), 
            "/queue/private-messages", 
            message
        );
    }
}
```

**How `convertAndSendToUser` works:**
When you call `convertAndSendToUser("Bob", "/queue/private-messages", message)`, Spring automatically transforms the destination. It prepends `/user/{username}` to it. 
So, internally, the message is routed to the destination:
`/user/Bob/queue/private-messages`

Only the user who is authenticated as "Bob" and subscribed to that specific queue will receive it.

---

## 3. The Missing Piece: Identifying the "User"

> [!IMPORTANT]  
> For `convertAndSendToUser` to work, Spring needs to know **who** is connected to the WebSocket session.

Since you have `spring-boot-starter-security` and `jjwt` (JWT) in your `pom.xml`, Spring Security is likely managing your authentication.

When a client connects to the WebSocket endpoint (`/ws`), Spring Security automatically associates the WebSocket session with the authenticated `Principal` (the user's identity). 

If a user named `Alice` logs in and establishes a WebSocket connection, Spring knows that session belongs to `Alice`. If `Bob` logs in, Spring knows his session belongs to `Bob`.

---

## 4. How the Frontend Communicates

To complete the end-to-end flow, your frontend (React, Angular, Vanilla JS) needs to connect using **SockJS** and a **STOMP Client**.

### Step 1: Connecting and Subscribing

The client establishes the connection and subscribes to their own private queue. Notice that the client subscribes to `/user/queue/private-messages`. They **do not** include their own username in the subscription string—Spring handles resolving `/user` to the currently authenticated user automatically.

```javascript
// Connect to the WebSocket endpoint
var socket = new SockJS('http://localhost:8080/ws');
var stompClient = Stomp.over(socket);

stompClient.connect({/* auth headers if needed */}, function (frame) {
    console.log('Connected: ' + frame);

    // Subscribe to private messages specifically for this user
    stompClient.subscribe('/user/queue/private-messages', function (messageOutput) {
        // This function triggers when a private message is received
        var message = JSON.parse(messageOutput.body);
        console.log("Received private message from " + message.sender + ": " + message.content);
    });
});
```

### Step 2: Sending a Private Message

When Alice wants to send a message to Bob, she sends a JSON object matching your `ChatMessage` model to the `/app/private-message` destination.

```javascript
function sendPrivateMessage() {
    var chatMessage = {
        sender: "Alice",
        receiver: "Bob",
        content: "Hey Bob, this is a private message!",
        type: "CHAT"
    };

    // Send the message to the Spring Controller
    stompClient.send("/app/private-message", {}, JSON.stringify(chatMessage));
}
```

---

## 5. Summary of the End-to-End Flow

1. **Alice** and **Bob** log into your app and are authenticated by Spring Security.
2. Both establish a WebSocket connection via `/ws`.
3. Alice subscribes to `/user/queue/private-messages`. (Spring knows this is Alice).
4. Bob subscribes to `/user/queue/private-messages`. (Spring knows this is Bob).
5. Alice sends a STOMP message to `/app/private-message` with receiver="Bob".
6. Spring routes this to `ChatController.handlePrivateMessage()`.
7. `ChatController` executes `messagingTemplate.convertAndSendToUser("Bob", "/queue/private-messages", message)`.
8. The Message Broker transforms this destination to `/user/Bob/queue/private-messages`.
9. The Message Broker looks for active sessions belonging to "Bob" who are subscribed to that queue.
10. **Bob's** frontend receives the message instantly. Alice's frontend does not, nor does any other user.
