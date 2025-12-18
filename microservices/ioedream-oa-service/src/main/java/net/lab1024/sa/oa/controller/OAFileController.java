package net.lab1024.sa.oa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.storage.FileStorageStrategy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.Map;

/**
 * OA服务 - 文件上传Controller
 *
 * 业务场景:
 * 1. 工作流审批附件 (200次/天, Word/PDF/Excel, 平均5MB/个)
 *
 * 存储需求:
 * - 日增: 1GB/天
 * - 保留: 5年(长期归档)
 * - 总需: 1.83TB
 *
 * @author IOE-DREAM Team
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/oa/file")
@Tag(name = "OA文件管理")
public class OAFileController {

    @Resource
    private FileStorageStrategy fileStorageStrategy;

    /**
     * 上传审批附件
     */
    @PostMapping("/upload/attachment")
    @Operation(summary = "上传审批附件")
    public ResponseDTO<String> uploadAttachment(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileStorageStrategy.uploadFile(file, "oa/attachments");
            log.info("[OA服务] 审批附件上传成功: {}, size={}KB",
                    fileUrl, file.getSize() / 1024);
            return ResponseDTO.ok(fileUrl);
        } catch (Exception e) {
            log.error("[OA服务] 审批附件上传失败", e);
            return ResponseDTO.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传附件
     */
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传附件")
    public ResponseDTO<String[]> uploadBatch(@RequestParam("files") MultipartFile[] files) {
        try {
            String[] urls = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = fileStorageStrategy.uploadFile(files[i], "oa/attachments");
            }
            log.info("[OA服务] 批量上传成功: count={}", files.length);
            return ResponseDTO.ok(urls);
        } catch (Exception e) {
            log.error("[OA服务] 批量上传失败", e);
            return ResponseDTO.error("批量上传失败: " + e.getMessage());
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
                "oa/attachments", fileName, 3600);

            log.info("[OA服务] 生成预签名URL: fileName={}", fileName);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[OA服务] 生成预签名URL失败", e);
            return ResponseDTO.error("生成预签名URL失败: " + e.getMessage());
        }
    }
}
