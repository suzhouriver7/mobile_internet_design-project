package dev.campuscompanionbackend.service;

import dev.campuscompanionbackend.dto.request.ChatRequest;
import dev.campuscompanionbackend.dto.response.ChatResponse;

public interface ZhipuAIService {
    /**
     * 发送聊天请求
     *
     * @param request AI请求
     * @return ChatResponse AI响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 快速对话
     *
     * @param userMessage 用户对话内容
     * @return String 响应对话内容
     */
    String simpleChat(String userMessage);

    /**
     * 带系统角色的对话
     *
     * @param systemPrompt 系统提示
     * @param userMessage 用户对话内容
     * @return String 响应对话内容
     */
    String chatWithSystem(String systemPrompt, String userMessage);
}
