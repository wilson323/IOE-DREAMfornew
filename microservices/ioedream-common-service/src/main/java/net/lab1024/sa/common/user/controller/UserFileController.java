package net.lab1024.sa.common.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * 人员管理 - 文件上传Controller
 *
 * 业务场景:
 * 1. 人员头像照片 (10000人, 500KB/张)
 * 2. 身份证照片 (10000人, 1MB×2)
 *
 * 存储需求:
 * - 日增: 5MB/天 (新增/更新)
 * - 总存储: 25GB (头像5GB + 身份证20GB)
 * - 特殊需求: CDN加速(头像), 加密存储(身份证)
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/user/file")
@Tag(name = "人员文件管理")
public class UserFileController {

    @Resource
    private FileStorageStrategy fileStorageStrategy;

    /**
     * 上传人员头像
     */
    @PostMapping("/upload/avatar")
    @Operation(summary = "上传人员头像")
    public ResponseDTO<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageStrategy.uploadFile(file, "common/avatars");
            log.info("[人员管理] 头像上传成功: {}", fileUrl);
            return ResponseDTO.ok(fileUrl);
        } catch (Exception e) {
            log.error("[人员管理] 头像上传失败", e);
            return ResponseDTO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传身份证照片
     */
    @PostMapping("/upload/id-card")
    @Operation(summary = "上传身份证照片")
    public ResponseDTO<String> uploadIdCard(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageStrategy.uploadFile(file, "common/id-cards");
            log.info("[人员管理] 身份证上传成功: {}", fileUrl);
            return ResponseDTO.ok(fileUrl);
        } catch (Exception e) {
            log.error("[人员管理] 身份证上传失败", e);
            return ResponseDTO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名上传URL
     */
    @GetMapping("/presigned-url")
    @Operation(summary = "获取预签名上传URL")
    public ResponseDTO<Map<String, String>> getPresignedUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "type", defaultValue = "avatar") String type) {
        try {
            String folder = "avatar".equals(type) ? "common/avatars" : "common/id-cards";
            Map<String, String> result = fileStorageStrategy.getPresignedUploadUrl(
                folder, fileName, 3600);

            log.info("[人员管理] 生成预签名URL: type={}, fileName={}", type, fileName);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[人员管理] 生成预签名URL失败", e);
            return ResponseDTO.error("生成预签名URL失败: " + e.getMessage());
        }
    }
}
