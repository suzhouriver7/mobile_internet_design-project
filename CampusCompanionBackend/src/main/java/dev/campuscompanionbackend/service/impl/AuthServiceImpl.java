package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.LoginRequest;
import dev.campuscompanionbackend.dto.request.RegisterRequest;
import dev.campuscompanionbackend.dto.response.LoginResponse;
import dev.campuscompanionbackend.dto.response.UserInfoResponse;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.UserStatus;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.exception.BusinessException;
import dev.campuscompanionbackend.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录: identifier={}", request.getIdentifier());

        User user = userRepository.findByEmail(request.getIdentifier())
                .orElseThrow(() -> new BusinessException(1002, "用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(1003, "密码错误");
        }

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

        return response;
    }

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        log.info("用户注册: email={}, nickname={}", request.getEmail(), request.getNickname());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(1011, "用户已存在");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setUserType(UserType.COMMON);
        user.setUserStatus(UserStatus.ONLINE);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLoginAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
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
    }
}