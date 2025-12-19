package net.lab1024.sa.attendance.controller;

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
 * 考勤服务 - 文件上传Controller
 *
 * 业务场景:
 * 1. 考勤打卡照片 (12000次/天, 1MB/张)
 *
 * 存储需求:
 * - 日增: 12GB/天
 * - 保留: 180天(半年)
 * - 总需: 2.16TB
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/attendance/file")
@Tag(name = "考勤文件管理")
public class AttendanceFileController {

    @Resource
    private FileStorageStrategy fileStorageStrategy;

    /**
     * 上传打卡照片
     */
    @PostMapping("/upload/photo")
    @Operation(summary = "上传打卡照片")
    public ResponseDTO<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageStrategy.uploadFile(file, "attendance/photos");
            log.info("[考勤服务] 打卡照片上传成功: {}", fileUrl);
            return ResponseDTO.ok(fileUrl);
        } catch (Exception e) {
            log.error("[考勤服务] 打卡照片上传失败", e);
            return ResponseDTO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名上传URL
     */
    @GetMapping("/presigned-url")
    @Operation(summary = "获取预签名上传URL")
    public ResponseDTO<Map<String, String>> getPresignedUrl(@RequestParam("fileName") String fileName) {
        try {
            Map<String, String> result = fileStorageStrategy.getPresignedUploadUrl(
                "attendance/photos", fileName, 3600);

            log.info("[考勤服务] 生成预签名URL: fileName={}", fileName);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[考勤服务] 生成预签名URL失败", e);
            return ResponseDTO.error("生成预签名URL失败: " + e.getMessage());
        }
    }
}
