package dev.campuscompanionbackend.utils;

import org.springframework.stereotype.Component;

@Component
public class EmailTemplateUtil {
    public static String buildVerifyCodeEmail(String code, int minutes) {
        return """
                <html>
                  <body>
                    <h2>邮箱验证码</h2>
                    <p>您的验证码是：</p>
                    <h1 style="letter-spacing: 5px;">%s</h1>
                    <p>验证码 <b>%d 分钟</b> 内有效，请勿泄露。</p>
                  </body>
                </html>
                """.formatted(code, minutes);
    }
}