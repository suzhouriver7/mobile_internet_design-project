package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordVerifyEmailRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordVerifyCodeRequest;
import dev.campuscompanionbackend.dto.request.ForgotPasswordResetPasswordRequest;
import dev.campuscompanionbackend.dto.response.LoginResponse;
import dev.campuscompanionbackend.dto.response.UserInfoResponse;
import dev.campuscompanionbackend.dto.response.ForgotPasswordVerifyEmailResponse;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.entity.VerifyCodeRecord;
import dev.campuscompanionbackend.enums.UserStatus;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.enums.VerifyCodeRecordStatus;
import dev.campuscompanionbackend.enums.VerifyCodeRecordType;
import dev.campuscompanionbackend.exception.*;
import dev.campuscompanionbackend.repository.UserRepository;
import dev.campuscompanionbackend.repository.VerifyCodeRecordRepository;
import dev.campuscompanionbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerifyCodeRecordRepository verifyCodeRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        User user = userRepository.findByEmail(identifier)
                .orElseThrow(() -> new UserNotExistException("用户不存在: identifier=" + identifier));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new PasswordErrorException("用户密码错误: identifier=" + identifier);
        }

        // TODO 占线处理

        user.setUserStatus(UserStatus.ONLINE);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = "mock_token_" + user.getUid() + "_" + System.currentTimeMillis();

        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(user.getUid());
        userInfo.setEmail(user.getEmail());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatarUrl(user.getAvatarUrl());
        userInfo.setUserType(user.getUserType() == UserType.ADMIN ? 1 : 0);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(userInfo);

        log.info("用户登录: userId={}, identifier={}", user.getUid(), identifier);
        return response;
    }

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        String email = request.getEmail();
        String verifyCode = request.getVerifycode();

        VerifyCodeRecord record = verifyCodeRecordRepository.findFirstByEmailAndTypeAndStatusOrderByCreatedAtDesc(
                email, VerifyCodeRecordType.REGISTER, VerifyCodeRecordStatus.UNUSED
                ).orElseThrow(() -> new RegisterFailedException("用户未请求验证码: email=" + email));

        if (record.getExpiredAt().isBefore(LocalDateTime.now())) {
            record.setStatus(VerifyCodeRecordStatus.EXPIRED);
            verifyCodeRecordRepository.save(record);
            throw new RegisterFailedException("验证码已过期: email=" + email);
        }


        if(!passwordEncoder.matches(verifyCode, record.getCode())) {
            throw new VerifyCodeErrorException(
                    String.format("验证码错误: email=%s, receivedVerifyCode=%s", email, verifyCode)
            );
        }
        record.setStatus(VerifyCodeRecordStatus.USED);
        verifyCodeRecordRepository.save(record);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setUserType(UserType.COMMON);
        user.setUserStatus(UserStatus.ONLINE);
        User savedUser = userRepository.save(user);

        log.info("用户注册: userId= {}, email={}, nickname={}", user.getUid(), user.getEmail(), user.getNickname());
        return savedUser.getUid();
    }

    @Override
    public String refreshToken(String refreshToken) {
        log.info("刷新Token");
        return "new_mock_token_" + System.currentTimeMillis();
    }

    @Override
    @Transactional
    public void logout(Long userId) {
        if (userId == null) {
            throw new SomethingHappenedException("用户退出登录但未提供 userId");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("用户不存在: userId=" + userId));

        user.setUserStatus(UserStatus.OFFLINE);
        userRepository.save(user);

        log.info("用户退出登录: userId={}", userId);
    }


    @Override
    public ForgotPasswordVerifyEmailResponse verifyEmailForReset(ForgotPasswordVerifyEmailRequest request) {
        String email = request.getEmail();
        ForgotPasswordVerifyEmailResponse resp = new ForgotPasswordVerifyEmailResponse();

        if(!userRepository.existsByEmail(email)) {
            throw new UserNotExistException("邮箱未注册: email=" + email);
        }

        int atIndex = email.indexOf('@');
        String namePart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        if (namePart.length() <= 2) {
            resp.setEmailMasked(namePart.charAt(0) + "***" + domainPart);
        } else {
            resp.setEmailMasked(namePart.substring(0, 2) + "***" + domainPart);
        }

        return resp;
    }

    @Override
    public void verifyResetCode(ForgotPasswordVerifyCodeRequest request) {
        String email = request.getEmail();
        String code = String.valueOf(request.getVerifyCode());
        VerifyCodeRecord record = verifyCodeRecordRepository.findFirstByEmailAndTypeAndStatusOrderByCreatedAtDesc(
                email, VerifyCodeRecordType.FORGET_PWD, VerifyCodeRecordStatus.UNUSED
        ).orElseThrow(()->
                new SomethingHappenedException("邮箱未验证: email=" + email)
        );
        if(record.getExpiredAt().isBefore(LocalDateTime.now())){
            record.setStatus(VerifyCodeRecordStatus.EXPIRED);
            verifyCodeRecordRepository.save(record);
            throw new RegisterFailedException("验证码已过期: email=" + email);
        }
        if(!passwordEncoder.matches(code, record.getCode())) {
            throw new VerifyCodeErrorException("验证码错误: email=" + email);
        }
        record.setStatus(VerifyCodeRecordStatus.USED);
        verifyCodeRecordRepository.save(record);
        log.info("验证验证码: email={}, code={}", request.getEmail(), request.getVerifyCode());
    }

    @Override
    public void resetPassword(ForgotPasswordResetPasswordRequest request) {
        String email = request.getEmail();
        String password = request.getNewPassword();
        User user = userRepository.findByEmail(email).orElseThrow(()->
                new UserNotExistException("邮箱未注册: email=" + email)
                );
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        log.info("已重置密码: email={}", request.getEmail());
    }
}