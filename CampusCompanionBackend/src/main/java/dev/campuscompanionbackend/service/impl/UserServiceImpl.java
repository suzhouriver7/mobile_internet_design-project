package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.dto.request.ChangePasswordRequest;
import dev.campuscompanionbackend.dto.request.UpdateUserRequest;
import dev.campuscompanionbackend.dto.response.UserInfoResponse;
import dev.campuscompanionbackend.entity.User;
import dev.campuscompanionbackend.enums.UserType;
import dev.campuscompanionbackend.exception.FileUploadFailedException;
import dev.campuscompanionbackend.exception.PasswordErrorException;
import dev.campuscompanionbackend.exception.UserNotExistException;
import dev.campuscompanionbackend.repository.UserRepository;
import dev.campuscompanionbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        log.info("获取用户详情: userId={}", userId);
        User user = getUserById(userId);
        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UpdateUserRequest request) {
        log.info("更新用户信息: userId={}", userId);
        User user = getUserById(userId);

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }

        user = userRepository.save(user);
        return convertToUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("修改密码: userId={}", userId);
        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new PasswordErrorException("密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile avatar) {
        log.info("上传头像: userId={}, filename={}", userId, avatar.getOriginalFilename());
        getUserById(userId);

        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadFailedException("请上传图片文件");
        }

        try {
            String uploadDir = "uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = avatar.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "avatar_" + userId + "_" + System.currentTimeMillis() + fileExtension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(avatar.getInputStream(), filePath);

            String avatarUrl = "/" + uploadDir + filename;
            User user = getUserById(userId);
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new FileUploadFailedException("上传头像失败", e);
        }
    }

    @Override
    public Object searchUsers(String keyword, Integer page, Integer size) {
        log.info("搜索用户: keyword={}, page={}, size={}", keyword, page, size);
        Iterable<User> users = userRepository.findByNicknameContaining(keyword);

        List<UserInfoResponse> userList = new ArrayList<>();
        for (User user : users) {
            userList.add(convertToUserInfoResponse(user));
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, userList.size());
        List<UserInfoResponse> pagedList = new ArrayList<>();
        for (int i = start; i < end && i < userList.size(); i++) {
            pagedList.add(userList.get(i));
        }

        return new dev.campuscompanionbackend.dto.response.PageResponse<>(
                pagedList,
                (long) userList.size(),
                page,
                size
        );
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException("用户不存在: userId=" + userId));
    }

    private UserInfoResponse convertToUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setId(user.getUid());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setUserType(user.getUserType() == UserType.ADMIN ? 1 : 0);
        // 个性签名
        response.setSignature(user.getSignature());
        // 创建时间转换为字符串，前端直接显示
        if (user.getCreatedAt() != null) {
            response.setCreatedAt(user.getCreatedAt().toString());
        }
        return response;
    }
}