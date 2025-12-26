package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.service.VideoRecordingPlaybackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/video/recording/playback")
@Tag(name = "视频录像回放管理")
@Slf4j
public class VideoRecordingPlaybackController {

    @Resource
    private VideoRecordingPlaybackService videoRecordingPlaybackService;

    @GetMapping("/{taskId}")
    @Operation(summary = "流式播放录像", description = "支持HTTP Range请求，边下边播")
    public ResponseEntity<StreamingResponseBody> playbackRecording(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            HttpServletRequest request) {

        log.info("[录像回放] 接收到播放请求: taskId={}", taskId);

        return videoRecordingPlaybackService.playbackRecording(taskId, request);
    }

    @GetMapping("/info/{taskId}")
    @Operation(summary = "获取播放信息", description = "包括时长、分辨率、码率等")
    public ResponseDTO<Map<String, Object>> getPlaybackInfo(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId) {

        log.info("[录像回放] 查询播放信息: taskId={}", taskId);

        Map<String, Object> playbackInfo = videoRecordingPlaybackService.getPlaybackInfo(taskId);

        return ResponseDTO.ok(playbackInfo);
    }

    @GetMapping("/metadata/{taskId}")
    @Operation(summary = "获取视频元数据", description = "包括编码格式、分辨率、帧率等")
    public ResponseDTO<Map<String, Object>> getVideoMetadata(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId) {

        log.info("[录像回放] 查询视频元数据: taskId={}", taskId);

        Map<String, Object> metadata = videoRecordingPlaybackService.getVideoMetadata(taskId);

        return ResponseDTO.ok(metadata);
    }

    @GetMapping("/check/{taskId}")
    @Operation(summary = "检查文件是否可播放", description = "验证文件是否存在且可播放")
    public ResponseDTO<Boolean> isPlayable(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId) {

        log.info("[录像回放] 检查可播放性: taskId={}", taskId);

        boolean playable = videoRecordingPlaybackService.isPlayable(taskId);

        return ResponseDTO.ok(playable);
    }

    @GetMapping("/url/{taskId}")
    @Operation(summary = "获取播放URL", description = "生成带令牌的临时播放链接")
    public ResponseDTO<String> getPlaybackUrl(
            @Parameter(description = "任务ID", required = true)
            @PathVariable Long taskId,
            @Parameter(description = "URL有效期（秒）", required = false)
            @RequestParam(required = false, defaultValue = "3600") Integer expireSeconds) {

        log.info("[录像回放] 生成播放URL: taskId={}, expireSeconds={}", taskId, expireSeconds);

        String playbackUrl = videoRecordingPlaybackService.getPlaybackUrl(taskId, expireSeconds);

        return ResponseDTO.ok(playbackUrl);
    }

    @PostMapping("/session/create")
    @Operation(summary = "创建播放会话", description = "创建播放会话用于跟踪用户播放行为")
    public ResponseDTO<String> createPlaybackSession(
            @Parameter(description = "任务ID", required = true)
            @RequestParam Long taskId,
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {

        log.info("[录像回放] 创建播放会话: taskId={}, userId={}", taskId, userId);

        String sessionId = videoRecordingPlaybackService.createPlaybackSession(taskId, userId);

        return ResponseDTO.ok(sessionId);
    }

    @PostMapping("/session/end")
    @Operation(summary = "结束播放会话", description = "结束播放会话并记录统计数据")
    public ResponseDTO<Void> endPlaybackSession(
            @Parameter(description = "会话ID", required = true)
            @RequestParam String sessionId) {

        log.info("[录像回放] 结束播放会话: sessionId={}", sessionId);

        videoRecordingPlaybackService.endPlaybackSession(sessionId);

        return ResponseDTO.ok();
    }

    @PostMapping("/session/progress")
    @Operation(summary = "记录播放进度", description = "记录用户播放进度用于断点续播")
    public ResponseDTO<Void> recordPlaybackProgress(
            @Parameter(description = "会话ID", required = true)
            @RequestParam String sessionId,
            @Parameter(description = "播放进度（秒）", required = true)
            @RequestParam Integer progress) {

        log.debug("[录像回放] 记录播放进度: sessionId={}, progress={}秒", sessionId, progress);

        videoRecordingPlaybackService.recordPlaybackProgress(sessionId, progress);

        return ResponseDTO.ok();
    }
}
