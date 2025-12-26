package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.service.VideoRecordingDownloadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 视频录像下载控制器
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@RestController
@RequestMapping("/api/v1/video/recording/download")
@Tag(name = "视频录像下载管理")
@Slf4j
public class VideoRecordingDownloadController {

    @Resource
    private VideoRecordingDownloadService videoRecordingDownloadService;

    @GetMapping("/{taskId}")
    @Operation(summary = "下载录像文件", description = "支持断点续传，通过Range头实现")
    public ResponseEntity<byte[]> downloadRecording(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            HttpServletRequest request) {

        log.info("[录像下载] 接收到下载请求: taskId={}", taskId);

        try {
            return videoRecordingDownloadService.downloadRecording(taskId, request);
        } catch (Exception e) {
            log.error("[录像下载] 下载失败: taskId={}, error={}", taskId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/url/{taskId}")
    @Operation(summary = "获取临时下载URL", description = "生成带令牌的临时下载链接")
    public ResponseDTO<String> getDownloadUrl(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "URL有效期（秒），默认3600秒")
            @RequestParam(required = false, defaultValue = "3600") Integer expireSeconds) {

        log.info("[录像下载] 生成下载URL: taskId={}, expireSeconds={}", taskId, expireSeconds);

        String downloadUrl = videoRecordingDownloadService.getDownloadUrl(taskId, expireSeconds);

        return ResponseDTO.ok(downloadUrl);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量下载录像文件", description = "将多个录像文件打包为ZIP下载")
    public ResponseEntity<byte[]> batchDownloadRecordings(
            @Parameter(description = "任务ID列表", required = true)
            @RequestBody List<Long> taskIds,
            HttpServletRequest request) {

        log.info("[录像下载] 批量下载请求: taskIds={}, count={}", taskIds, taskIds.size());

        return videoRecordingDownloadService.batchDownloadRecordings(taskIds, request);
    }

    @GetMapping("/file/info/{taskId}")
    @Operation(summary = "获取录像文件信息", description = "包括文件大小、时长、格式化信息等")
    public ResponseDTO<Map<String, Object>> getRecordingFileInfo(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId) {

        log.info("[录像下载] 查询文件信息: taskId={}", taskId);

        Map<String, Object> fileInfo = videoRecordingDownloadService.getRecordingFileInfo(taskId);

        return ResponseDTO.ok(fileInfo);
    }

    @GetMapping("/file/check/{taskId}")
    @Operation(summary = "检查录像文件是否存在", description = "验证物理文件是否存在")
    public ResponseDTO<Boolean> checkFileExists(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId) {

        log.info("[录像下载] 检查文件存在性: taskId={}", taskId);

        boolean exists = videoRecordingDownloadService.checkFileExists(taskId);

        return ResponseDTO.ok(exists);
    }

    @DeleteMapping("/file/delete/{taskId}")
    @Operation(summary = "删除录像文件", description = "删除物理文件并更新任务记录")
    public ResponseDTO<Void> deleteRecordingFile(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId) {

        log.info("[录像下载] 删除录像文件: taskId={}", taskId);

        boolean deleted = videoRecordingDownloadService.deleteRecordingFile(taskId);

        if (deleted) {
            log.info("[录像下载] 文件删除成功: taskId={}", taskId);
            return ResponseDTO.ok();
        } else {
            log.warn("[录像下载] 文件删除失败: taskId={}", taskId);
            return ResponseDTO.error("DELETE_FAILED", "文件删除失败");
        }
    }

    @GetMapping("/permission/validate/{taskId}")
    @Operation(summary = "验证下载权限", description = "检查当前用户是否有权限下载指定录像")
    public ResponseDTO<Boolean> validateDownloadPermission(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {

        log.info("[录像下载] 验证下载权限: taskId={}, userId={}", taskId, userId);

        boolean hasPermission = videoRecordingDownloadService.validateDownloadPermission(taskId, userId);

        return ResponseDTO.ok(hasPermission);
    }

    @PostMapping("/log/record")
    @Operation(summary = "记录下载日志", description = "记录用户下载行为用于统计分析")
    public ResponseDTO<Void> recordDownloadLog(
            @Parameter(description = "任务ID", required = true)
            @RequestParam Long taskId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "IP地址", required = true)
            @RequestParam String ipAddress) {

        log.info("[录像下载] 记录下载日志: taskId={}, userId={}, ip={}", taskId, userId, ipAddress);

        videoRecordingDownloadService.recordDownloadLog(taskId, userId, ipAddress);

        return ResponseDTO.ok();
    }
}
