package net.lab1024.sa.video.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.video.dao.DeviceAIEventDao;
import net.lab1024.sa.video.domain.entity.DeviceAIEventEntity;
import net.lab1024.sa.video.domain.form.DeviceAIEventForm;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * 设备AI事件管理器
 * <p>
 * 边缘计算架构：接收设备上报的结构化AI事件
 * </p>
 * <p>
 * 核心职责：
 * 1. 接收设备上报的AI事件
 * 2. 存储事件到数据库
 * 3. 触发告警规则匹配
 * 4. 推送实时事件到前端
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class DeviceAIEventManager {

    private final DeviceAIEventDao deviceAIEventDao;
    private final AlarmRuleEngine alarmRuleEngine;

    public DeviceAIEventManager(DeviceAIEventDao deviceAIEventDao, AlarmRuleEngine alarmRuleEngine) {
        this.deviceAIEventDao = deviceAIEventDao;
        this.alarmRuleEngine = alarmRuleEngine;
    }

    /**
     * 接收设备AI事件
     *
     * @param form 事件表单
     * @return 事件实体
     */
    public DeviceAIEventEntity receiveDeviceAIEvent(DeviceAIEventForm form) {
        log.info("[设备AI事件] 接收设备事件: deviceId={}, eventType={}, confidence={}",
                form.getDeviceId(), form.getEventType(), form.getConfidence());

        try {
            // 转换为实体
            DeviceAIEventEntity entity = convertToEntity(form);

            // 存储到数据库
            deviceAIEventDao.insert(entity);

            log.info("[设备AI事件] 事件已存储: eventId={}", entity.getEventId());

            // ============================================================
            // 边缘计算架构：事件存储后触发告警规则匹配
            // ============================================================
            int matchedRules = alarmRuleEngine.processDeviceEvent(entity);
            log.info("[设备AI事件] 告警规则匹配完成: eventId={}, matchedRules={}",
                    entity.getEventId(), matchedRules);

            // TODO: 推送实时事件到前端（阶段2实现）

            return entity;

        } catch (Exception e) {
            log.error("[设备AI事件] 接收事件失败: deviceId={}, eventType={}, error={}",
                    form.getDeviceId(), form.getEventType(), e.getMessage(), e);
            throw new BusinessException("DEVICE_AI_EVENT_RECEIVE_ERROR", "接收设备AI事件失败: " + e.getMessage());
        }
    }

    /**
     * 查询设备的AI事件列表
     *
     * @param deviceId  设备ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param eventType 事件类型（可选）
     * @return 事件列表
     */
    public List<DeviceAIEventEntity> queryDeviceEvents(String deviceId, LocalDateTime startTime, LocalDateTime endTime, String eventType) {
        log.debug("[设备AI事件] 查询设备事件: deviceId={}, startTime={}, endTime={}, eventType={}",
                deviceId, startTime, endTime, eventType);

        LambdaQueryWrapper<DeviceAIEventEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceAIEventEntity::getDeviceId, deviceId);
        queryWrapper.ge(DeviceAIEventEntity::getEventTime, startTime);
        queryWrapper.le(DeviceAIEventEntity::getEventTime, endTime);
        if (eventType != null && !eventType.isEmpty()) {
            queryWrapper.eq(DeviceAIEventEntity::getEventType, eventType);
        }
        queryWrapper.orderByDesc(DeviceAIEventEntity::getEventTime);

        return deviceAIEventDao.selectList(queryWrapper);
    }

    /**
     * 更新事件处理状态
     *
     * @param eventId     事件ID
     * @param eventStatus 事件状态
     * @param alarmId     告警ID（可选）
     */
    public void updateEventStatus(String eventId, Integer eventStatus, String alarmId) {
        log.debug("[设备AI事件] 更新事件状态: eventId={}, eventStatus={}, alarmId={}",
                eventId, eventStatus, alarmId);

        LambdaUpdateWrapper<DeviceAIEventEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DeviceAIEventEntity::getEventId, eventId);
        updateWrapper.set(DeviceAIEventEntity::getEventStatus, eventStatus);
        updateWrapper.set(DeviceAIEventEntity::getProcessTime, LocalDateTime.now());
        if (alarmId != null) {
            updateWrapper.set(DeviceAIEventEntity::getAlarmId, alarmId);
        }

        int rows = deviceAIEventDao.update(null, updateWrapper);
        if (rows > 0) {
            log.info("[设备AI事件] 事件状态已更新: eventId={}, eventStatus={}", eventId, eventStatus);
        }
    }

    /**
     * 转换表单为实体
     */
    private DeviceAIEventEntity convertToEntity(DeviceAIEventForm form) {
        DeviceAIEventEntity entity = new DeviceAIEventEntity();

        // 基本信息
        entity.setDeviceId(form.getDeviceId());
        entity.setDeviceCode(form.getDeviceCode());
        entity.setEventType(form.getEventType());
        entity.setConfidence(form.getConfidence());
        entity.setBbox(form.getBbox());
        entity.setEventTime(form.getEventTime());
        entity.setExtendedAttributes(form.getExtendedAttributes());

        // 解码Base64图片
        if (form.getSnapshot() != null && !form.getSnapshot().isEmpty()) {
            try {
                byte[] snapshotBytes = Base64.getDecoder().decode(form.getSnapshot());
                entity.setSnapshot(snapshotBytes);
            } catch (IllegalArgumentException e) {
                log.warn("[设备AI事件] Base64解码失败: deviceId={}, error={}",
                        form.getDeviceId(), e.getMessage());
                entity.setSnapshot(null);
            }
        }

        // 初始状态
        entity.setEventStatus(0); // 待处理
        entity.setProcessTime(null);
        entity.setAlarmId(null);

        return entity;
    }

    /**
     * 事件类型描述
     */
    public String getEventTypeName(String eventType) {
        return switch (eventType) {
            case "FALL_DETECTION" -> "跌倒检测";
            case "LOITERING_DETECTION" -> "徘徊检测";
            case "GATHERING_DETECTION" -> "聚集检测";
            case "FIGHTING_DETECTION" -> "打架检测";
            case "RUNNING_DETECTION" -> "奔跑检测";
            case "CLIMBING_DETECTION" -> "攀爬检测";
            case "FACE_DETECTION" -> "人脸检测";
            case "INTRUSION_DETECTION" -> "入侵检测";
            default -> "未知事件";
        };
    }
}
