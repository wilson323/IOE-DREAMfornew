package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.dto.AccessRecordBatchUploadRequest;
import net.lab1024.sa.access.service.AccessRecordBatchService;
import net.lab1024.sa.common.response.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;

/**
 * 门禁记录批量上传控制器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 接收设备批量上传的通行记录
 * </p>
 * <p>
 * 核心职责：
 * - 接收设备批量上传的通行记录（设备端验证模式）
 * - 批量插入数据库
 * - 幂等性检查（防止重复上传）
 * - 异常处理和日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/record/batch")
@Tag(name = "门禁记录批量上传", description = "接收设备批量上传的通行记录")
public class AccessRecordBatchController {

    @Resource
    private AccessRecordBatchService accessRecordBatchService;

    /**
     * 批量上传通行记录
     * <p>
     * 接收设备批量上传的通行记录，支持幂等性检查
     * </p>
     *
     * @param request 批量上传请求
     * @return 处理结果
     */
    @PostMapping("/upload")
    @Operation(summary = "批量上传通行记录", description = "接收设备批量上传的通行记录，支持幂等性检查")
    public ResponseDTO<AccessRecordBatchUploadRequest.BatchUploadResult> batchUpload(
            @Valid @RequestBody AccessRecordBatchUploadRequest request) {
        log.info("[批量上传] 接收批量上传请求: deviceId={}, recordCount={}", 
                request.getDeviceId(), request.getRecords() != null ? request.getRecords().size() : 0);

        try {
            return accessRecordBatchService.batchUploadRecords(request);
        } catch (Exception e) {
            log.error("[批量上传] 批量上传异常: deviceId={}, error={}", 
                    request.getDeviceId(), e.getMessage(), e);
            return ResponseDTO.error("BATCH_UPLOAD_ERROR", "批量上传异常: " + e.getMessage());
        }
    }

    /**
     * 查询批量上传状态
     * <p>
     * 查询指定批次的上传状态
     * </p>
     *
     * @param batchId 批次ID
     * @return 上传状态
     */
    @GetMapping("/status/{batchId}")
    @Operation(summary = "查询批量上传状态", description = "查询指定批次的上传状态")
    public ResponseDTO<AccessRecordBatchUploadRequest.BatchUploadResult> getUploadStatus(
            @Parameter(description = "批次ID", required = true) @PathVariable String batchId) {
        log.debug("[批量上传] 查询上传状态: batchId={}", batchId);

        try {
            return accessRecordBatchService.getUploadStatus(batchId);
        } catch (Exception e) {
            log.error("[批量上传] 查询上传状态异常: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_STATUS_ERROR", "查询上传状态异常: " + e.getMessage());
        }
    }
}
