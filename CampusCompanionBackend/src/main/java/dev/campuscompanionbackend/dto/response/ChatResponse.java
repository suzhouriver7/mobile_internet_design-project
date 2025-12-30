package dev.campuscompanionbackend.dto.response;

import dev.campuscompanionbackend.model.Choice;
import dev.campuscompanionbackend.model.Usage;
import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    public String getReplyContent() {
        if (choices != null && !choices.isEmpty()) {
            Choice choice = choices.getFirst();
            if (choice.getMessage() != null) {
                return choice.getMessage().getContent();
            }
        }
        return null;
    }
}
