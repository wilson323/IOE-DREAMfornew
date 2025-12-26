package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessPermissionImportQueryForm;
import net.lab1024.sa.access.domain.vo.*;
import net.lab1024.sa.access.service.AccessPermissionImportService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.util.SmartRequestUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 门禁权限批量导入控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/permission-import")
@Tag(name = "门禁权限批量导入", description = "提供Excel批量导入门禁权限功能")
public class AccessPermissionImportController {

    @Resource
    private AccessPermissionImportService accessPermissionImportService;

    /**
     * 上传并解析Excel文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传并解析Excel文件", description = "上传Excel文件，解析数据并验证，返回批次ID")
    public ResponseDTO<Long> uploadAndParse(
            @Parameter(description = "Excel文件", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "批次名称", required = true)
            @RequestParam("batchName") String batchName
    ) {
        log.info("[权限导入] 上传并解析文件: batchName={}, fileName={}", batchName, file.getOriginalFilename());

        try {
            Long operatorId = SmartRequestUtil.getRequestUserId();
            String operatorName = SmartRequestUtil.getRequestUserName();

            Long batchId = accessPermissionImportService.uploadAndParse(file, batchName, operatorId, operatorName);
            log.info("[权限导入] 文件上传成功: batchId={}", batchId);

            return ResponseDTO.ok(batchId);
        } catch (Exception e) {
            log.error("[权限导入] 文件上传失败: batchName={}, error={}", batchName, e.getMessage(), e);
            return ResponseDTO.error("UPLOAD_FAILED", "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 执行导入（同步）
     */
    @PostMapping("/{batchId}/execute")
    @Operation(summary = "执行导入", description = "同步执行导入操作，返回导入结果")
    public ResponseDTO<AccessPermissionImportResultVO> executeImport(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId
    ) {
        log.info("[权限导入] 执行导入: batchId={}", batchId);

        try {
            AccessPermissionImportResultVO result = accessPermissionImportService.executeImport(batchId);
            log.info("[权限导入] 导入完成: batchId={}, successCount={}, errorCount={}",
                    batchId, result.getSuccessCount(), result.getErrorCount());

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[权限导入] 导入失败: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("IMPORT_FAILED", "导入失败: " + e.getMessage());
        }
    }

    /**
     * 执行导入（异步）
     */
    @PostMapping("/{batchId}/execute-async")
    @Operation(summary = "异步执行导入", description = "异步执行导入操作，返回任务ID")
    public ResponseDTO<String> executeImportAsync(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId
    ) {
        log.info("[权限导入] 异步执行导入: batchId={}", batchId);

        try {
            String taskId = accessPermissionImportService.executeImportAsync(batchId);
            log.info("[权限导入] 异步任务已创建: taskId={}, batchId={}", taskId, batchId);

            return ResponseDTO.ok(taskId);
        } catch (Exception e) {
            log.error("[权限导入] 异步任务创建失败: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("ASYNC_TASK_FAILED", "异步任务创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询导入批次列表
     */
    @GetMapping("/batches")
    @Operation(summary = "查询导入批次列表", description = "分页查询导入批次记录")
    public ResponseDTO<PageResult<AccessPermissionImportBatchVO>> queryImportBatches(
            AccessPermissionImportQueryForm queryForm
    ) {
        log.info("[权限导入] 查询导入批次: queryForm={}", queryForm);

        try {
            PageResult<AccessPermissionImportBatchVO> result = accessPermissionImportService.queryImportBatches(queryForm);
            log.info("[权限导入] 查询成功: total={}", result.getTotal());

            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[权限导入] 查询失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_FAILED", "查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询批次详情
     */
    @GetMapping("/batches/{batchId}")
    @Operation(summary = "查询批次详情", description = "根据批次ID查询详细信息")
    public ResponseDTO<AccessPermissionImportBatchVO> getImportBatchDetail(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId
    ) {
        log.info("[权限导入] 查询批次详情: batchId={}", batchId);

        try {
            AccessPermissionImportBatchVO batch = accessPermissionImportService.getImportBatchDetail(batchId);
            if (batch == null) {
                log.warn("[权限导入] 批次不存在: batchId={}", batchId);
                return ResponseDTO.error("BATCH_NOT_FOUND", "导入批次不存在");
            }

            return ResponseDTO.ok(batch);
        } catch (Exception e) {
            log.error("[权限导入] 查询批次详情失败: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_DETAIL_FAILED", "查询批次详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询批次错误记录
     */
    @GetMapping("/batches/{batchId}/errors")
    @Operation(summary = "查询批次错误记录", description = "查询指定批次的错误记录列表")
    public ResponseDTO<List<AccessPermissionImportErrorVO>> queryBatchErrors(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId
    ) {
        log.info("[权限导入] 查询批次错误: batchId={}", batchId);

        try {
            List<AccessPermissionImportErrorVO> errors = accessPermissionImportService.queryBatchErrors(batchId);
            log.info("[权限导入] 错误记录查询成功: batchId={}, errorCount={}", batchId, errors.size());

            return ResponseDTO.ok(errors);
        } catch (Exception e) {
            log.error("[权限导入] 查询错误记录失败: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_ERRORS_FAILED", "查询错误记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取导入统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取导入统计信息", description = "获取整体导入统计数据")
    public ResponseDTO<AccessPermissionImportStatisticsVO> getImportStatistics() {
        log.info("[权限导入] 查询统计信息");

        try {
            AccessPermissionImportStatisticsVO statistics = accessPermissionImportService.getImportStatistics();
            log.info("[权限导入] 统计信息查询成功: totalBatches={}, successRate={}%",
                    statistics.getTotalBatches(), statistics.getSuccessRate());

            return ResponseDTO.ok(statistics);
        } catch (Exception e) {
            log.error("[权限导入] 查询统计信息失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("QUERY_STATS_FAILED", "查询统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除导入批次
     */
    @DeleteMapping("/batches/{batchId}")
    @Operation(summary = "删除导入批次", description = "删除指定的导入批次及其关联数据")
    public ResponseDTO<Integer> deleteImportBatch(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId
    ) {
        log.info("[权限导入] 删除导入批次: batchId={}", batchId);

        try {
            Integer count = accessPermissionImportService.deleteImportBatch(batchId);
            log.info("[权限导入] 批次删除成功: batchId={}, count={}", batchId, count);

            return ResponseDTO.ok(count);
        } catch (Exception e) {
            log.error("[权限导入] 删除批次失败: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("DELETE_FAILED", "删除批次失败: " + e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/template")
    @Operation(summary = "下载导入模板", description = "下载Excel导入模板文件")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        log.info("[权限导入] 下载导入模板");

        try {
            byte[] templateData = accessPermissionImportService.downloadTemplate();

            String fileName = URLEncoder.encode("门禁权限导入模板.xlsx", StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + fileName);
            response.setContentLength(templateData.length);

            response.getOutputStream().write(templateData);
            response.getOutputStream().flush();

            log.info("[权限导入] 模板下载成功");
        } catch (Exception e) {
            log.error("[权限导入] 模板下载失败: error={}", e.getMessage(), e);
            throw new IOException("模板下载失败: " + e.getMessage());
        }
    }

    /**
     * 导出错误记录
     */
    @GetMapping("/batches/{batchId}/export-errors")
    @Operation(summary = "导出错误记录", description = "导出指定批次的错误记录到Excel文件")
    public void exportErrors(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId,
            HttpServletResponse response
    ) throws IOException {
        log.info("[权限导入] 导出错误记录: batchId={}", batchId);

        try {
            byte[] errorData = accessPermissionImportService.exportErrors(batchId);

            String fileName = URLEncoder.encode("权限导入错误记录_" + batchId + ".csv", StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + fileName);
            response.setContentLength(errorData.length);

            response.getOutputStream().write(errorData);
            response.getOutputStream().flush();

            log.info("[权限导入] 错误记录导出成功: batchId={}", batchId);
        } catch (Exception e) {
            log.error("[权限导入] 错误记录导出失败: batchId={}, error={}", batchId, e.getMessage(), e);
            throw new IOException("错误记录导出失败: " + e.getMessage());
        }
    }

    /**
     * 取消导入
     */
    @PostMapping("/{batchId}/cancel")
    @Operation(summary = "取消导入", description = "取消待处理或处理中的导入任务")
    public ResponseDTO<Boolean> cancelImport(
            @Parameter(description = "批次ID", required = true)
            @PathVariable("batchId") Long batchId
    ) {
        log.info("[权限导入] 取消导入: batchId={}", batchId);

        try {
            Boolean cancelled = accessPermissionImportService.cancelImport(batchId);
            log.info("[权限导入] 导入取消操作完成: batchId={}, cancelled={}", batchId, cancelled);

            return ResponseDTO.ok(cancelled);
        } catch (Exception e) {
            log.error("[权限导入] 取消导入失败: batchId={}, error={}", batchId, e.getMessage(), e);
            return ResponseDTO.error("CANCEL_FAILED", "取消导入失败: " + e.getMessage());
        }
    }

    /**
     * 查询异步任务状态
     */
    @GetMapping("/tasks/{taskId}/status")
    @Operation(summary = "查询异步任务状态", description = "查询异步导入任务的执行状态")
    public ResponseDTO<com.alibaba.fastjson2.JSONObject> getTaskStatus(
            @Parameter(description = "任务ID", required = true)
            @PathVariable("taskId") String taskId
    ) {
        log.info("[权限导入] 查询任务状态: taskId={}", taskId);

        try {
            com.alibaba.fastjson2.JSONObject taskStatus = accessPermissionImportService.getTaskStatus(taskId);
            if (taskStatus == null) {
                log.warn("[权限导入] 任务不存在: taskId={}", taskId);
                return ResponseDTO.error("TASK_NOT_FOUND", "任务不存在");
            }

            return ResponseDTO.ok(taskStatus);
        } catch (Exception e) {
            log.error("[权限导入] 查询任务状态失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return ResponseDTO.error("QUERY_TASK_STATUS_FAILED", "查询任务状态失败: " + e.getMessage());
        }
    }
}
