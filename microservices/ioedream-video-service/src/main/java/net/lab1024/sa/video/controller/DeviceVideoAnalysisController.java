package net.lab1024.sa.video.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备视频分析上传控制器
 * <p>
 * 接收设备端上传的结构化分析数据（边缘AI计算模式）
 * 严格遵循CLAUDE.md规范：
 * - 设备端完成：AI分析+结构化数据提取
 * - 软件端处理：存储数据+告警联动+视频回调
 * - ⚠️ 只上传结构化数据，不上传原始视频
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/device")
@Tag(name = "设备视频分析", description = "接收设备上传的结构化分析数据")
public class DeviceVideoAnalysisController {

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 接收设备上传的结构化分析数据
     * <p>
     * ⭐ 设备端已完成：AI分析+结构化数据提取
     * ⭐ 软件端处理：存储数据+告警联动+视频回调
     * ⚠️ 只上传结构化数据，不上传原始视频
     * </p>
     *
     * @param analysisData 分析数据
     * @return 处理结果
     */
    @PostMapping("/upload-analysis")
    @Operation(summary = "接收设备上传的结构化分析数据", description = "设备端完成AI分析，只上传结构化数据")
    public ResponseDTO<Void> uploadVideoAnalysis(@RequestBody VideoAnalysisData analysisData) {
        long startTime = System.currentTimeMillis();
        log.info("[设备视频分析] 接收设备上传的结构化分析数据, deviceId={}, analysisType={}, timestamp={}",
                analysisData.getDeviceId(), analysisData.getAnalysisType(), analysisData.getTimestamp());

        try {
            // 1. 存储结构化数据
            saveAnalysisData(analysisData);

            // 2. 告警规则匹配
            if (analysisData.hasAlerts()) {
                processAlerts(analysisData);
            }

            // 3. 视频联动（告警时回调原始视频）
            if (analysisData.requiresVideoCallback()) {
                triggerVideoCallback(analysisData);
            }

            // 4. 记录业务指标（使用Micrometer标准方式）
            long duration = System.currentTimeMillis() - startTime;
            Timer.builder("video.device.upload.analysis.time")
                    .tag("analysisType", analysisData.getAnalysisType())
                    .tag("success", "true")
                    .description("设备视频分析处理耗时")
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            Timer.builder("api.response.time")
                    .tag("api", "video.device.upload-analysis")
                    .description("API响应时间")
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            log.info("[设备视频分析] 处理完成, duration={}ms", duration);

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[设备视频分析] 处理异常", e);
            long duration = System.currentTimeMillis() - startTime;
            // 记录失败指标
            Timer.builder("video.device.upload.analysis.time")
                    .tag("analysisType", analysisData.getAnalysisType())
                    .tag("success", "false")
                    .description("设备视频分析处理耗时")
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);

            Counter.builder("video.device.upload.analysis.error")
                    .tag("analysisType", analysisData.getAnalysisType())
                    .description("设备视频分析错误计数")
                    .register(meterRegistry)
                    .increment();

            return ResponseDTO.error("VIDEO_ANALYSIS_ERROR", "处理视频分析数据失败: " + e.getMessage());
        }
    }

    /**
     * 存储结构化数据
     */
    private void saveAnalysisData(VideoAnalysisData analysisData) {
        // 存储人脸抓拍、行为事件等结构化数据
        log.debug("[设备视频分析] 存储结构化数据, deviceId={}, analysisType={}",
                analysisData.getDeviceId(), analysisData.getAnalysisType());
    }

    /**
     * 处理告警
     */
    private void processAlerts(VideoAnalysisData analysisData) {
        // 告警规则匹配和推送
        for (AlertInfo alert : analysisData.getAlerts()) {
            // 发送告警通知
            rabbitTemplate.convertAndSend(
                    "video.alert.exchange",
                    "video.alert.route",
                    VideoAlertEvent.builder()
                            .deviceId(analysisData.getDeviceId())
                            .alertType(alert.getType())
                            .alertLevel(alert.getLevel())
                            .alertTime(LocalDateTime.now())
                            .alertData(alert.getData())
                            .build()
            );
            log.info("[设备视频分析] 告警推送, deviceId={}, alertType={}, level={}",
                    analysisData.getDeviceId(), alert.getType(), alert.getLevel());
        }
    }

    /**
     * 触发视频回调
     */
    private void triggerVideoCallback(VideoAnalysisData analysisData) {
        // 告警时回调原始视频
        try {
            rabbitTemplate.convertAndSend(
                    "video.callback.exchange",
                    "video.callback.route",
                    VideoCallbackRequest.builder()
                            .deviceId(analysisData.getDeviceId())
                            .startTime(analysisData.getTimestamp().minusMinutes(5))
                            .endTime(analysisData.getTimestamp().plusMinutes(5))
                            .reason("ALERT_CALLBACK")
                            .build()
            );
            log.info("[设备视频分析] 触发视频回调, deviceId={}, timeRange={}",
                    analysisData.getDeviceId(), analysisData.getTimestamp());
        } catch (Exception e) {
            log.error("[设备视频分析] 触发视频回调失败", e);
        }
    }

    /**
     * 视频分析数据
     */
    @lombok.Data
    private static class VideoAnalysisData {
        private Long deviceId;
        private String analysisType; // FACE_DETECTION, BEHAVIOR_ANALYSIS, INTRUSION_DETECTION
        private LocalDateTime timestamp;
        private Map<String, Object> analysisResult;
        private List<AlertInfo> alerts;

        public boolean hasAlerts() {
            return alerts != null && !alerts.isEmpty();
        }

        public boolean requiresVideoCallback() {
            return hasAlerts() && alerts.stream()
                    .anyMatch(a -> "HIGH".equals(a.getLevel()) || "CRITICAL".equals(a.getLevel()));
        }
    }

    /**
     * 告警信息
     */
    @lombok.Data
    private static class AlertInfo {
        private String type;
        private String level; // LOW, MEDIUM, HIGH, CRITICAL
        private Map<String, Object> data;
    }

    /**
     * 视频告警事件
     */
    @lombok.Data
    @lombok.Builder
    private static class VideoAlertEvent {
        private Long deviceId;
        private String alertType;
        private String alertLevel;
        private LocalDateTime alertTime;
        private Map<String, Object> alertData;
    }

    /**
     * 视频回调请求
     */
    @lombok.Data
    @lombok.Builder
    private static class VideoCallbackRequest {
        private Long deviceId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String reason;
    }
}
