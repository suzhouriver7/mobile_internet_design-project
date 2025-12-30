package dev.campuscompanionbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zhipu")
public class ZhipuConfig {
    private String apiKey;
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private String model = "glm-4-flash-250414";
    private int timeout = 30000;
}
