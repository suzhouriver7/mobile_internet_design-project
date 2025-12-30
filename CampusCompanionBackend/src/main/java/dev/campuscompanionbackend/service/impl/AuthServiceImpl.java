package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.response.LoginResponse;
import dev.campuscompanionbackend.dto.response.UserInfoResponse;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.UserStatus;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.exception.*;
import dev.campuscompanionbackend.repository.UserRepository;
import dev.campuscompanionbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
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

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new RegisterFailedException("邮箱未验证: email=" + email);
        }
        User user = optionalUser.get();

        if(optionalUser.get().getUserStatus() != UserStatus.REGISTERING){
            throw new UserExistException("邮箱已注册");
        }
        if(!passwordEncoder.matches(request.getPassword(), verifyCode)) {
            throw new VerifyCodeErrorException(
                    String.format("验证码错误: email=%s, receivedVerifyCode=%s", email, verifyCode)
            );
        }

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
    public void logout() {
        log.info("用户退出登录");
        // TODO 用户退出
    }
}