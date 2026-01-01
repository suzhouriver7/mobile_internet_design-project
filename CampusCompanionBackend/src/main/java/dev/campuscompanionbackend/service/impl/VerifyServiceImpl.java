package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.entity.VerifyCodeRecord;
import dev.campuscompanionbackend.enums.VerifyCodeRecordStatus;
import dev.campuscompanionbackend.enums.VerifyCodeRecordType;
import dev.campuscompanionbackend.exception.UserExistException;
import dev.campuscompanionbackend.exception.UserNotExistException;
import dev.campuscompanionbackend.repository.UserRepository;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.repository.VerifyCodeRecordRepository;
import dev.campuscompanionbackend.service.VerifyService;
import dev.campuscompanionbackend.exception.EmailInvalidException;
import dev.campuscompanionbackend.utils.EmailTemplateUtil;
import dev.campuscompanionbackend.utils.VerifyCodeUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final JavaMailSender mailSender;
    private final VerifyCodeRecordRepository verifyCodeRecordRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 发件人邮箱地址，需与 spring.mail.username 保持一致
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    // 验证码有效期（分钟）
    private static final int EXPIRE_MINUTES = 5;


    @Transactional
    public void verifyEmail(String email, VerifyCodeRecordType type) throws EmailInvalidException, UserExistException {
        String code = VerifyCodeUtil.generateCode6Num();
        LocalDateTime expireTime =
                LocalDateTime.now().plusMinutes(EXPIRE_MINUTES);
        String content = EmailTemplateUtil.buildVerifyCodeEmail(code, EXPIRE_MINUTES);
        sendEmail(email, "邮箱验证码", content);

        VerifyCodeRecord record = new VerifyCodeRecord();
        record.setEmail(email);
        record.setCode(passwordEncoder.encode(code));
        record.setType(type);
        record.setStatus(VerifyCodeRecordStatus.UNUSED);
        record.setExpiredAt(expireTime);
        verifyCodeRecordRepository.save(record);
        log.info("发送验证码: email={}, code={}", email, code);
    }


    /**
     * 发送邮件
     * @param to 收件邮箱
     * @param subject 标题
     * @param content 内容
     */
    private void sendEmail(String to, String subject, String content) throws EmailInvalidException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            // 一些邮箱服务（如 163）要求 From 与认证账号一致，否则会报 553
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailInvalidException("无法发送邮件: email=" + to, e);
        }
    }
}

