package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.entity.PostMedia;
import dev.campuscompanionbackend.exception.FileDeleteFailedException;
import dev.campuscompanionbackend.exception.FileUploadFailedException;
import dev.campuscompanionbackend.repository.PostMediaRepository;
import dev.campuscompanionbackend.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final PostMediaRepository postMediaRepository;

    @Value("${file.upload-dir:uploads}")
    private String baseUploadDir;

    @Override
    public String uploadImage(MultipartFile image) {
        log.info("上传图片: filename={}, size={}KB",
                image.getOriginalFilename(),
                image.getSize() / 1024);

        return uploadFile(image, "uploads/images/", "image");
    }

    @Override
    public String uploadVideo(MultipartFile video) {
        log.info("上传视频: filename={}, size={}KB",
                video.getOriginalFilename(),
                video.getSize() / 1024);

        return uploadFile(video, "uploads/videos/", "video");
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        log.info("删除文件: fileUrl={}", fileUrl);

        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new FileDeleteFailedException("文件url为空");
        }

        try {

            String url = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;

            // 计算上传目录的绝对路径：支持 baseUploadDir 为相对路径或绝对路径
            Path cwd = Paths.get("").toAbsolutePath().normalize();
            Path baseUploadPath = Paths.get(baseUploadDir);
            if (!baseUploadPath.isAbsolute()) {
                baseUploadPath = cwd.resolve(baseUploadDir).normalize();
            } else {
                baseUploadPath = baseUploadPath.normalize();
            }

            // 如果 URL 中已经包含了 baseUploadDir（例如 "uploads/images/..."），去掉重复前缀
            String relativeUrl = url;
            String normalizedBase = baseUploadPath.getFileName() != null ? baseUploadPath.getFileName().toString() : baseUploadDir;
            if (relativeUrl.startsWith(normalizedBase + "/")) {
                relativeUrl = relativeUrl.substring(normalizedBase.length() + 1);
            }

            Path filePath = baseUploadPath.resolve(relativeUrl).normalize();

            // 安全校验：确保最终路径在上传目录之下
            if (!filePath.startsWith(baseUploadPath)) {
                log.warn("尝试访问非法文件路径: {} (baseUploadDir={})", filePath, baseUploadPath);
                throw new FileDeleteFailedException("非法文件路径");
            }

            if (!Files.exists(filePath)) {
                log.warn("文件不存在: {}", filePath);
                throw new FileDeleteFailedException("文件不存在");
            }

            if (!Files.isRegularFile(filePath)) {
                log.warn("路径不是文件: {}", filePath);
                throw new FileDeleteFailedException("指定的路径不是一个文件");
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("文件删除成功: {}", filePath);
            } else {
                log.warn("文件删除失败: {}", filePath);
                throw new FileDeleteFailedException("文件删除失败");
            }
            return deleted;
        } catch (IOException e) {
            log.error("删除文件时发生IO异常: {}", fileUrl, e);
            throw new FileDeleteFailedException("删除文件失败: " + e.getMessage(), e);
        } catch (SecurityException e) {
            log.error("删除文件时权限不足: {}", fileUrl, e);
            throw new FileDeleteFailedException("没有权限删除文件", e);
        }
    }

    @Override
    public boolean deleteFile(Long pmid) {
        log.info("删除文件: pmid={}", pmid);
        if (pmid == null) {
            throw new FileDeleteFailedException("文件pmid为空");
        }
        PostMedia media = postMediaRepository.findById(pmid).orElse(null);
        if (media == null) {
            throw new FileDeleteFailedException("未找到文件");
        }

        return deleteFile(media.getUrl());
    }

    private String uploadFile(MultipartFile file, String uploadDir, String fileType) {
        if (file.isEmpty()) {
            throw new FileUploadFailedException("文件为空");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileUploadFailedException("无法识别的文件类型: " + file.getOriginalFilename());
        }

        if ("image".equals(fileType) && !contentType.startsWith("image/")) {
            throw new FileUploadFailedException("请上传图片文件: file=" + file.getOriginalFilename());
        }

        if ("video".equals(fileType) && !contentType.startsWith("video/")) {
            throw new FileUploadFailedException("请上传视频文件: file=" + file.getOriginalFilename());
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = fileType + "_" + System.currentTimeMillis() + fileExtension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            return "/" + uploadDir + filename;
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new FileUploadFailedException("上传文件失败", e);
        }
    }
}