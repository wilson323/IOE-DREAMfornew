package net.lab1024.sa.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.monitoring.BusinessMetrics;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 设备通行记录上传控制器
 * <p>
 * 接收设备端上传的通行记录（边缘自主验证模式）
 * 严格遵循CLAUDE.md规范：
 * - 设备端完成：识别+权限验证+开门
 * - 软件端处理：存储记录+事件推送+异常检测
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/access/device")
@Tag(name = "设备通行记录", description = "接收设备上传的通行记录")
public class DeviceAccessRecordController {

    @Resource
    private AccessEventService accessEventService;

    @Resource
    private BusinessMetrics businessMetrics;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 接收设备上传的通行记录
     * <p>
     * ⭐ 设备端已完成：识别+权限验证+开门
     * ⭐ 软件端只需：存储记录+事件推送+异常检测
     * </p>
     *
     * @param form 通行记录表单
     * @return 处理结果
     */
    @PostMapping("/upload-record")
    @Operation(summary = "接收设备上传的通行记录", description = "设备端已完成识别和验证，软件端只处理记录存储和事件推送")
    public ResponseDTO<Void> uploadAccessRecord(@Valid @RequestBody AccessRecordAddForm form) {
        long startTime = System.currentTimeMillis();
        log.info("[设备通行记录] 接收设备上传的通行记录, deviceId={}, userId={}, accessTime={}",
                form.getDeviceId(), form.getUserId(), form.getPassTime());

        try {
            // 1. 保存通行记录
            ResponseDTO<Long> saveResult = accessEventService.createAccessRecord(form);
            if (!saveResult.isSuccess()) {
                log.error("[设备通行记录] 保存通行记录失败: {}", saveResult.getMessage());
                return ResponseDTO.error(saveResult.getCode(), saveResult.getMessage());
            }

            // 2. 记录业务指标
            businessMetrics.recordAccessEvent(
                    form.getAccessResult() != null && form.getAccessResult() == 1 ? "SUCCESS" : "FAILURE");

            // 3. 实时推送到监控大屏（通过WebSocket）
            pushAccessEvent(form);

            // 4. 异常检测
            if (form.getAccessResult() != null && form.getAccessResult() == 1) {
                checkUnauthorizedTimeAccess(form);
            }

            // 5. 视频联动（成功通行时录像5分钟）
            // 注意：AccessRecordAddForm中没有doorOpened字段，根据accessResult判断
            if (form.getAccessResult() != null && form.getAccessResult() == 1) {
                triggerVideoLinkage(form);
            }

            long duration = System.currentTimeMillis() - startTime;
            businessMetrics.recordResponseTime("access.device.upload-record", duration);
            log.info("[设备通行记录] 处理完成, duration={}ms", duration);

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[设备通行记录] 处理异常", e);
            businessMetrics.recordAccessEvent("ERROR");
            return ResponseDTO.error("ACCESS_RECORD_UPLOAD_ERROR", "处理通行记录失败: " + e.getMessage());
        }
    }

    /**
     * 推送通行事件
     */
    private void pushAccessEvent(AccessRecordAddForm form) {
        // 通过WebSocket推送实时通行事件
        // 实现逻辑：调用WebSocketService发送事件
        log.debug("[设备通行记录] 推送通行事件, userId={}, deviceId={}", form.getUserId(), form.getDeviceId());
    }

    /**
     * 检查非授权时间通行
     */
    private void checkUnauthorizedTimeAccess(AccessRecordAddForm form) {
        // 检查是否在授权时间外通行
        // 实现逻辑：查询用户权限，检查通行时间是否在允许时间段内
        log.debug("[设备通行记录] 检查非授权时间通行, userId={}, deviceId={}", form.getUserId(), form.getDeviceId());
    }

    /**
     * 触发视频联动
     */
    private void triggerVideoLinkage(AccessRecordAddForm form) {
        // 成功通行时触发视频录像
        try {
            rabbitTemplate.convertAndSend(
                    "video.linkage.exchange",
                    "video.record.route",
                    VideoLinkageRequest.builder()
                            .deviceId(form.getDeviceId())
                            .eventType("ACCESS_GRANTED")
                            .duration(Duration.ofMinutes(5))
                            .build()
            );
            log.debug("[设备通行记录] 触发视频联动, deviceId={}", form.getDeviceId());
        } catch (Exception e) {
            log.error("[设备通行记录] 触发视频联动失败", e);
        }
    }

    /**
     * 视频联动请求
     */
    @lombok.Data
    @lombok.Builder
    private static class VideoLinkageRequest {
        private Long deviceId;
        private String eventType;
        private Duration duration;
    }
}
