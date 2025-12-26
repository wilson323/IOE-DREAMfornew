package net.lab1024.sa.access.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.storage.FileStorageStrategy;

/**
 * 门禁服务 - 文件上传Controller
 *
 * 业务场景:
 * 1. 门禁通行抓拍 (15000次/天, 1-2MB/张)
 * 2. 异常抓拍 (100次/天, 2MB/张)
 *
 * 存储需求:
 * - 日增: 22.7GB/天
 * - 保留: 30天(通行) + 90天(异常)
 * - 总需: 711GB
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/access/file")
@Tag(name = "门禁文件管理")
public class AccessFileController {

    @Resource
    private FileStorageStrategy fileStorageStrategy;

    /**
     * 上传门禁通行抓拍照片
     */
    @PostMapping("/upload/snapshot")
    @Operation(summary = "上传通行抓拍")
    public ResponseDTO<String> uploadSnapshot(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageStrategy.uploadFile(file, "access/snapshots");
            log.info("[门禁服务] 通行抓拍上传成功: {}", fileUrl);
            return ResponseDTO.ok(fileUrl);
        } catch (Exception e) {
            log.error("[门禁服务] 通行抓拍上传失败", e);
            return ResponseDTO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传门禁异常抓拍照片
     */
    @PostMapping("/upload/alert")
    @Operation(summary = "上传异常抓拍")
    public ResponseDTO<String> uploadAlert(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageStrategy.uploadFile(file, "access/alerts");
            log.info("[门禁服务] 异常抓拍上传成功: {}", fileUrl);
            return ResponseDTO.ok(fileUrl);
        } catch (Exception e) {
            log.error("[门禁服务] 异常抓拍上传失败", e);
            return ResponseDTO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名上传URL (设备直传)
     */
    @GetMapping("/presigned-url")
    @Operation(summary = "获取预签名上传URL")
    public ResponseDTO<Map<String, String>> getPresignedUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "type", defaultValue = "snapshot") String type) {
        try {
            String folder = "snapshot".equals(type) ? "access/snapshots" : "access/alerts";
            Map<String, String> result = fileStorageStrategy.getPresignedUploadUrl(
                    folder, fileName, 3600); // 1小时有效期

            log.info("[门禁服务] 生成预签名URL: folder={}, fileName={}", folder, fileName);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[门禁服务] 生成预签名URL失败", e);
            return ResponseDTO.error("生成预签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{filePath}")
    @Operation(summary = "删除文件")
    public ResponseDTO<Boolean> deleteFile(@PathVariable String filePath) {
        try {
            boolean success = fileStorageStrategy.deleteFile(filePath);
            return ResponseDTO.ok(success);
        } catch (Exception e) {
            log.error("[门禁服务] 删除文件失败: {}", filePath, e);
            return ResponseDTO.error("删除文件失败: " + e.getMessage());
        }
    }
}
