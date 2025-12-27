package dev.campuscompanionbackend.service.impl;

import dev.campuscompanionbackend.exception.FileUploadFailedException;
import dev.campuscompanionbackend.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

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