package dev.campuscompanionbackend.controller;

import dev.campuscompanionbackend.dto.response.ApiResponse;
import dev.campuscompanionbackend.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传相关接口
 */
@RestController
@RequestMapping("/api/v1/upload")
public class FileController extends BaseController {
    
    private final FileService fileService;
    
    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    
    /**
     * 上传图片
     * @param image 图片文件
     * @return ApiResponse<String>
     */
    @PostMapping("/image")
    public ApiResponse<String> uploadImage(@RequestParam("image") MultipartFile image) {
        String url = fileService.uploadImage(image);
        return success("上传成功", url);
    }
    
    /**
     * 上传视频
     * @param video 视频文件
     * @return ApiResponse<String>
     */
    @PostMapping("/video")
    public ApiResponse<String> uploadVideo(@RequestParam("video") MultipartFile video) {
        String url = fileService.uploadVideo(video);
        return success("上传成功", url);
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     * @return ApiResponse<Boolean>
     */
    @DeleteMapping
    public ApiResponse<Boolean> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        boolean deleted = fileService.deleteFile(fileUrl);
        String message = deleted ? "删除成功" : "文件不存在";
        return success(message, deleted);
    }
}