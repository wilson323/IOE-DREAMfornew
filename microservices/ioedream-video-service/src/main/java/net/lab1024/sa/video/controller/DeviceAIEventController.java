package net.lab1024.sa.video.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.domain.entity.DeviceAIEventEntity;
import net.lab1024.sa.video.domain.form.DeviceAIEventForm;
import net.lab1024.sa.video.domain.vo.DeviceAIEventVO;
import net.lab1024.sa.video.manager.DeviceAIEventManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备AI事件控制器
 * <p>
 * 边缘计算架构：接收设备上报的结构化AI事件
 * </p>
 * <p>
 * 核心API：
 * - POST /api/v1/video/device/ai/event - 接收设备AI事件
 * - GET /api/v1/video/device/ai/events - 查询设备AI事件
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/video/device/ai")
@Tag(name = "设备AI事件管理")
public class DeviceAIEventController {

    @Resource
    private DeviceAIEventManager deviceAIEventManager;

    /**
     * 接收设备AI事件
     * <p>
     * 边缘计算架构：设备端完成AI分析，服务器接收结构化事件
     * </p>
     *
     * @param form AI事件表单
     * @return 事件ID
     */
    @PostMapping("/event")
    @Operation(summary = "接收设备AI事件", description = "接收设备上报的AI分析结果（边缘计算架构）")
    public ResponseDTO<String> receiveDeviceAIEvent(
            @Parameter(description = "AI事件表单", required = true) @Valid @RequestBody DeviceAIEventForm form) {

        log.info("[设备AI事件] 接收设备AI事件: deviceId={}, eventType={}, confidence={}",
                form.getDeviceId(), form.getEventType(), form.getConfidence());

        try {
            // 接收事件
            DeviceAIEventEntity event = deviceAIEventManager.receiveDeviceAIEvent(form);

            log.info("[设备AI事件] 事件接收成功: eventId={}, deviceId={}, eventType={}",
                    event.getEventId(), event.getDeviceId(), event.getEventType());

            return ResponseDTO.ok(event.getEventId());

        } catch (BusinessException e) {
            log.error("[设备AI事件] 业务异常: deviceId={}, eventType={}, error={}",
                    form.getDeviceId(), form.getEventType(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[设备AI事件] 系统异常: deviceId={}, eventType={}, error={}",
                    form.getDeviceId(), form.getEventType(), e.getMessage(), e);
            throw new BusinessException("DEVICE_AI_EVENT_ERROR", "接收设备AI事件失败: " + e.getMessage());
        }
    }

    /**
     * 查询设备AI事件列表
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param eventType 事件类型（可选）
     * @return 事件列表
     */
    @GetMapping("/events")
    @Operation(summary = "查询设备AI事件", description = "查询指定设备的AI事件列表")
    public ResponseDTO<List<DeviceAIEventVO>> queryDeviceEvents(
            @Parameter(description = "设备ID", required = true) @RequestParam String deviceId,
            @Parameter(description = "开始时间", required = true) @RequestParam LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true) @RequestParam LocalDateTime endTime,
            @Parameter(description = "事件类型（可选）") @RequestParam(required = false) String eventType) {

        log.info("[设备AI事件] 查询设备事件: deviceId={}, startTime={}, endTime={}, eventType={}",
                deviceId, startTime, endTime, eventType);

        try {
            // 查询事件
            List<DeviceAIEventEntity> events = deviceAIEventManager.queryDeviceEvents(
                    deviceId, startTime, endTime, eventType);

            // 转换为VO
            List<DeviceAIEventVO> eventVOs = events.stream()
                    .map(this::convertToVO)
                    .toList();

            log.info("[设备AI事件] 查询成功: deviceId={}, count={}", deviceId, eventVOs.size());

            return ResponseDTO.ok(eventVOs);

        } catch (Exception e) {
            log.error("[设备AI事件] 查询失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new BusinessException("QUERY_DEVICE_AI_EVENTS_ERROR", "查询设备AI事件失败: " + e.getMessage());
        }
    }

    /**
     * 转换实体为VO
     */
    private DeviceAIEventVO convertToVO(DeviceAIEventEntity entity) {
        return DeviceAIEventVO.builder()
                .eventId(entity.getEventId())
                .deviceId(entity.getDeviceId())
                .deviceCode(entity.getDeviceCode())
                .eventType(entity.getEventType())
                .eventTypeName(deviceAIEventManager.getEventTypeName(entity.getEventType()))
                .confidence(entity.getConfidence())
                .bbox(entity.getBbox())
                .snapshotUrl(null) // TODO: 实现图片URL生成
                .eventTime(entity.getEventTime())
                .extendedAttributes(entity.getExtendedAttributes())
                .eventStatus(entity.getEventStatus())
                .processTime(entity.getProcessTime())
                .alarmId(entity.getAlarmId())
                .createTime(entity.getCreateTime())
                .build();
    }
}
