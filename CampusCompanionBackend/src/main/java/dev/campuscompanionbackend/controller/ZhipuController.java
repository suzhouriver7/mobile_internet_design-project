package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.request.ChatRequest;
import dev.campuscompanionbackend.dto.request.SystemChatRequest;
import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.dto.response.ChatResponse;
import dev.campuscompanionbackend.model.ChatMessage;
import dev.campuscompanionbackend.model.Choice;
import dev.campuscompanionbackend.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
public class ZhipuController extends BaseController {

    @Autowired
    private ZhipuAIService aiService;

    /**
     * 简单对话
     * @param message 用户对话内容
     * @return ApiResponse<String>
     */
    @GetMapping("/chat")
    public ApiResponse<String> simpleChat(@RequestParam String message) {
        log.info("收到AI对话请求: {}", message);
        try {
            String reply = aiService.simpleChat(message);
            log.info("AI回复: {}", reply);
            return success(reply);
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return error("抱歉，AI服务暂时不可用: " + e.getMessage());
        }
    }

    /**
     * 带系统提示的对话
     * @param request 带系统提示的用户对话请求
     * @return ApiResponse<String>
     */
    @PostMapping("/chat/system")
    public ApiResponse<String> chatWithSystem(@RequestBody SystemChatRequest request) {
        log.info("收到带系统提示的AI对话: system={}, user={}",
                request.getSystem(), request.getUser());
        try {
            String reply = aiService.chatWithSystem(request.getSystem(), request.getUser());
            log.info("AI回复: {}", reply);
            return success(reply);
        } catch (Exception e) {
            log.error("AI对话失败", e);
            return error("抱歉，AI服务暂时不可用: " + e.getMessage());
        }
    }

    /**
     * 完整的聊天请求
     * @param request 用户对话请求
     * @return ChatResponse
     */
    @PostMapping("/chat/full")
    public ChatResponse fullChat(@RequestBody ChatRequest request) {
        log.info("收到完整AI对话请求");
        try {
            ChatResponse response = aiService.chat(request);
            log.info("AI回复成功，消耗token: {}", response.getUsage().getTotalTokens());
            return response;
        } catch (Exception e) {
            log.error("AI对话失败", e);
            ChatResponse errorResponse = new ChatResponse();
            Choice errorChoice = new Choice();
            ChatMessage errorMessage = new ChatMessage();
            errorMessage.setContent("AI服务错误: " + e.getMessage());
            errorChoice.setMessage(errorMessage);
            errorResponse.setChoices(List.of(errorChoice));
            return errorResponse;
        }
    }

    /**
     * AI服务健康检查
     * @return ApiResponse<String>
     */
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        try {
            String reply = aiService.simpleChat("你好");
            if (reply != null && !reply.trim().isEmpty()) {
                return success("AI服务正常");
            } else {
                return error("AI服务响应异常");
            }
        } catch (Exception e) {
            return error("AI服务测试不通过: " + e.getMessage());
        }
    }
}
