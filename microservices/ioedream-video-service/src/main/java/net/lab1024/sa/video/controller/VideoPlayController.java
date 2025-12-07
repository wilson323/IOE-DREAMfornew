package net.lab1024.sa.video.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.service.VideoPlayService;

import java.util.Map;

/**
 * 视频播放管理PC端控制器
 * <p>
 * 提供PC端视频播放相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 实时视频流播放
 * - 录像回放
 * - 视频截图
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/play")
@Tag(name = "视频播放管理PC端", description = "实时视频流、录像回放、视频截图等API")
public class VideoPlayController {

    @Resource
    private VideoPlayService videoPlayService;

    /**
     * 获取视频流地址
     *
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @param streamType 流类型（可选）
     * @return 视频流地址
     */
    @PostMapping("/stream")
    @Operation(summary = "获取视频流地址", description = "获取视频设备的实时流播放地址")
    @PreAuthorize("hasRole('VIDEO_MANAGER') or hasRole('VIDEO_USER')")
    public ResponseDTO<Map<String, Object>> getVideoStream(
            @RequestParam @NotNull Long deviceId,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String streamType) {
        log.info("[视频播放] 获取视频流地址，deviceId={}, channelId={}, streamType={}", deviceId, channelId, streamType);
        try {
            Map<String, Object> result = videoPlayService.getVideoStream(deviceId, channelId, streamType);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频播放] 获取视频流地址失败，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_STREAM_ERROR", "获取视频流地址失败: " + e.getMessage());
        }
    }

    /**
     * 获取视频截图
     *
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @return 截图数据
     */
    @GetMapping("/snapshot/{deviceId}")
    @Operation(summary = "获取视频截图", description = "获取视频设备的实时截图")
    @PreAuthorize("hasRole('VIDEO_MANAGER') or hasRole('VIDEO_USER')")
    public ResponseDTO<Map<String, Object>> getSnapshot(
            @Parameter(description = "设备ID", required = true) @PathVariable @NotNull Long deviceId,
            @RequestParam(required = false) Long channelId) {
        log.info("[视频播放] 获取视频截图，deviceId={}, channelId={}", deviceId, channelId);
        try {
            Map<String, Object> result = videoPlayService.getSnapshot(deviceId, channelId);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[视频播放] 获取视频截图失败，deviceId={}", deviceId, e);
            return ResponseDTO.error("GET_SNAPSHOT_ERROR", "获取视频截图失败: " + e.getMessage());
        }
    }
}

