package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.repository.UserRepository;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.service.VerifyService;
import dev.campuscompanionbackend.service.exception.EmailVerifyException;
import dev.campuscompanionbackend.utils.EmailTemplateUtil;
import dev.campuscompanionbackend.utils.VerifyCodeUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    // 验证码有效期（分钟）
    private static final int EXPIRE_MINUTES = 5;


    @Transactional
    public void verifyEmail(String email) throws EmailVerifyException {
        String code = VerifyCodeUtil.generateCode6Num();

        LocalDateTime expireTime =
                LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);

        String content = EmailTemplateUtil.buildVerifyCodeEmail(code, EXPIRE_MINUTES);
        sendEmail(email, "邮箱验证码", content);
        userRepository.save(User.buildRegisteringUser(email, code, expireTime));
    }


    /**
     * 发送邮件
     * @param to 收件邮箱
     * @param subject 标题
     * @param content 内容
     */
    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailVerifyException("发送邮件失败", e);
        }
    }
}

