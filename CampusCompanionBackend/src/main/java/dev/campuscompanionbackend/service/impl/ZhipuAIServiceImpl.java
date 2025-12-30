package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.config.ZhipuConfig;
import dev.campuscompanionbackend.dto.request.ChatRequest;
import dev.campuscompanionbackend.dto.response.ChatResponse;
import dev.campuscompanionbackend.exception.AIException;
import dev.campuscompanionbackend.service.ZhipuAIService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ZhipuAIServiceImpl implements ZhipuAIService {
    @Autowired
    private ZhipuConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ChatResponse chat(ChatRequest request) {
        String url = config.getBaseUrl();

        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpPost httpPost = new HttpPost(url);

            httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            String json = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode == 200) {
                    ChatResponse chatResponse = objectMapper.readValue(responseBody, ChatResponse.class);
                    log.info("AI回复成功，消耗token: {}", chatResponse.getUsage().getTotalTokens());
                    return chatResponse;
                } else {
                    log.error("智谱AI接口调用失败: status={}, body={}", statusCode, responseBody);
                    throw new AIException("AI服务调用失败，状态码: " + statusCode);
                }
            }

        } catch (Exception e) {
            log.error("调用智谱AI接口异常", e);
            throw new AIException("AI服务异常: " + e.getMessage(), e);
        }
    }

    @Override
    public String simpleChat(String userMessage) {
        ChatRequest request = ChatRequest.simpleRequest(userMessage);
        ChatResponse response = chat(request);
        return response.getReplyContent();
    }

    @Override
    public String chatWithSystem(String systemPrompt, String userMessage) {
        ChatRequest request = ChatRequest.withSystemPrompt(systemPrompt, userMessage);
        ChatResponse response = chat(request);
        return response.getReplyContent();
    }

    /**
     * 创建HTTP客户端
     *
     * @return CloseableHttpClient 可关闭的HTTP客户端
     */
    private CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getTimeout())
                .setSocketTimeout(config.getTimeout())
                .build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
