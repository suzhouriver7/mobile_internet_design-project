package dev.campuscompanionbackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传图片
     * @param image 图片文件
     * @return String 图片URL
     */
    String uploadImage(MultipartFile image);
    
    /**
     * 上传视频
     * @param video 视频文件
     * @return String 视频URL
     */
    String uploadVideo(MultipartFile video);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return boolean 是否删除成功
     */
    boolean deleteFile(String fileUrl);
}