package net.lab1024.sa.video.controller;

import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.video.domain.vo.VideoDeviceVO;
import net.lab1024.sa.video.service.VideoPlayService;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;

import java.util.List;
import java.util.Map;

/**
 * 视频监控移动端控制器
 * <p>
 * 提供移动端视频监控相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 * <p>
 * 业务场景：
 * - 设备列表查询
 * - 视频流播放
 * - 录像回放
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/video")
@Tag(name = "视频监控移动端", description = "移动端视频监控相关API")
@PermissionCheck(value = "VIDEO_MOBILE", description = "视频监控移动端模块权限")
public class VideoMobileController {

    @Resource
    private VideoPlayService videoPlayService;

    /**
     * 获取设备列表
     *
     * @param areaId 区域ID（可选）
     * @param deviceType 设备类型（可选）
     * @param status 设备状态（可选）
     * @return 设备列表
     */
    @Observed(name = "video.mobile.getDeviceList", contextualName = "video-mobile-get-device-list")
    @GetMapping("/device/list")
    @Operation(summary = "获取设备列表", description = "获取视频设备列表")
    @PermissionCheck(value = "VIDEO_USER", description = "视频使用权限")
    public ResponseDTO<List<VideoDeviceVO>> getDeviceList(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Integer status) {
        log.info("[视频监控移动端] 获取设备列表，areaId={}, deviceType={}, status={}", areaId, deviceType, status);
        try {
            List<VideoDeviceVO> deviceList = videoPlayService.getMobileDeviceList(areaId, deviceType, status);
            return ResponseDTO.ok(deviceList);
        } catch (ParamException e) {
            log.warn("[视频监控移动端] 获取设备列表参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频监控移动端] 获取设备列表业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频监控移动端] 获取设备列表系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频监控移动端] 获取设备列表执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("GET_DEVICE_LIST_ERROR", "获取设备列表失败");
        }
    }

    /**
     * 获取视频流地址
     *
     * @param deviceId 设备ID
     * @param channelId 通道ID（可选）
     * @param streamType 流类型（可选）
     * @return 视频流地址
     */
    @Observed(name = "video.mobile.getVideoStream", contextualName = "video-mobile-get-stream")
    @PostMapping("/play/stream")
    @Operation(summary = "获取视频流地址", description = "获取视频设备的实时流播放地址")
    @PermissionCheck(value = "VIDEO_USER", description = "视频使用权限")
    public ResponseDTO<Map<String, Object>> getVideoStream(
            @RequestParam @NotNull Long deviceId,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String streamType) {
        log.info("[视频监控移动端] 获取视频流地址，deviceId={}, channelId={}, streamType={}", deviceId, channelId, streamType);
        try {
            Map<String, Object> result = videoPlayService.getVideoStream(deviceId, channelId, streamType);
            return ResponseDTO.ok(result);
        } catch (ParamException e) {
            log.warn("[视频监控移动端] 获取视频流地址参数错误，deviceId={}: {}", deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[视频监控移动端] 获取视频流地址业务异常，deviceId={}: {}", deviceId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[视频监控移动端] 获取视频流地址系统异常，deviceId={}: {}", deviceId, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[视频监控移动端] 获取视频流地址执行异常，deviceId={}: {}", deviceId, e.getMessage(), e);
            return ResponseDTO.error("GET_STREAM_ERROR", "获取视频流地址失败");
        }
    }
}

