package dev.campuscompanionbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 安全配置类
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 基础安全配置：
     * - 关闭 CSRF（前后端分离，使用的是 JSON API）
     * - 开启 CORS，允许本地前端调试
     * - 暂时放行所有请求，避免 Spring Security 把接口重定向到 /login 页面
     *   造成前端看到的 Network Error / CORS 错误。
     *
     * 后续如果要接入真正的 JWT 鉴权，可以在这里补充认证过滤器和具体的权限规则。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // 如有需要，可以在这里对 /api/v1/auth/** 等接口做更细粒度配置
                        .anyRequest().permitAll()
                )
                // 关闭默认的表单登录和 HTTP Basic，避免重定向到 /login
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /**
     * CORS 配置，允许前端本地开发地址访问后端接口。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            // 本地开发环境 Vite 前端
            "http://localhost:5173",
            // 本地直连后端调试
            "http://localhost:8080",
            // Docker 部署后 Nginx 前端（对外暴露 8081）
            "http://localhost:8081"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
