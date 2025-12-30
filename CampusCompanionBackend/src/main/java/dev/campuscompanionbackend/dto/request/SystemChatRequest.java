package dev.campuscompanionbackend.dto.request;

import lombok.Data;

@Data
public class SystemChatRequest {
    private String system;
    private String user;
}