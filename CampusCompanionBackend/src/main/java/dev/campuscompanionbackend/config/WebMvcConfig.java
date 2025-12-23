package dev.campuscompanionbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 相关配置
 *
 * 将本地 uploads 目录映射为静态资源，
 * 便于前端通过形如 /uploads/avatars/xxx.png 的 URL 直接访问头像等文件。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 项目根目录下的 uploads 文件夹
        String uploadDir = Paths.get("uploads").toAbsolutePath().toString() + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }
}
