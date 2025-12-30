package dev.campuscompanionbackend.dto.request;

import dev.campuscompanionbackend.model.ChatMessage;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private String model = "glm-4-flash-250414";
    private List<ChatMessage> messages;
    private Boolean stream = false;
    private Float temperature = 0.95f;
    private Integer maxTokens = 2048;

    public static ChatRequest simpleRequest(String userMessage) {
        ChatRequest request = new ChatRequest();
        ChatMessage message = new ChatMessage();
        message.setRole("user");
        message.setContent(userMessage);
        request.setMessages(List.of(message));
        return request;
    }

    public static ChatRequest withSystemPrompt(String systemPrompt, String userMessage) {
        ChatRequest request = new ChatRequest();
        ChatMessage system = new ChatMessage();
        system.setRole("system");
        system.setContent(systemPrompt);

        ChatMessage user = new ChatMessage();
        user.setRole("user");
        user.setContent(userMessage);

        request.setMessages(List.of(system, user));
        return request;
    }
}
