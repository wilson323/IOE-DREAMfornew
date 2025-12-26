package net.lab1024.sa.video.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.video.dao.AlarmRecordDao;
import net.lab1024.sa.video.dao.AlarmRuleDao;
import net.lab1024.sa.common.entity.video.AlarmRecordEntity;
import net.lab1024.sa.common.entity.video.AlarmRuleEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 告警规则引擎
 * <p>
 * 边缘计算架构：接收设备 AI 事件，匹配告警规则，创建告警记录
 * </p>
 * <p>
 * 核心功能：
 * 1. 规则匹配：根据事件类型、置信度、设备、区域等条件匹配规则
 * 2. 告警创建：匹配成功后创建告警记录
 * 3. 通知推送：根据规则配置推送告警通知
 * 4. 优先级排序：按规则优先级和事件置信度排序
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Slf4j
@Component
public class AlarmRuleEngine {

    @Resource
    private AlarmRuleDao alarmRuleDao;

    @Resource
    private AlarmRecordDao alarmRecordDao;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 处理设备 AI 事件，匹配告警规则
     * <p>
     * 核心流程：
     * 1. 查询启用的告警规则
     * 2. 按优先级排序
     * 3. 逐个匹配规则条件
     * 4. 匹配成功则创建告警记录
     * </p>
     *
     * @param event 设备 AI 事件
     * @return 匹配的告警规则数量
     */
    public int processDeviceEvent(DeviceAIEventEntity event) {
        log.debug("[告警规则引擎] 开始处理设备AI事件: eventId={}, eventType={}, confidence={}",
                event.getEventId(), event.getEventType(), event.getConfidence());

        // 查询启用的告警规则
        LambdaQueryWrapper<AlarmRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AlarmRuleEntity::getRuleStatus, 1) // 启用状态
                .eq(AlarmRuleEntity::getEventType, event.getEventType()) // 匹配事件类型
                .orderByDesc(AlarmRuleEntity::getPriority); // 按优先级降序

        List<AlarmRuleEntity> rules = alarmRuleDao.selectList(queryWrapper);

        if (rules.isEmpty()) {
            log.debug("[告警规则引擎] 未找到匹配的告警规则: eventType={}", event.getEventType());
            return 0;
        }

        int matchedCount = 0;

        // 逐个匹配规则
        for (AlarmRuleEntity rule : rules) {
            if (matchRule(event, rule)) {
                // 创建告警记录
                AlarmRecordEntity alarm = createAlarmRecord(event, rule);
                alarmRecordDao.insert(alarm);

                log.info("[告警规则引擎] 告警创建成功: alarmId={}, ruleId={}, eventId={}, alarmLevel={}",
                        alarm.getAlarmId(), rule.getRuleId(), event.getEventId(), alarm.getAlarmLevel());

                matchedCount++;

                // TODO: 推送告警通知
                // if (rule.getPushNotification() == 1) {
                //     pushAlarmNotification(alarm, rule);
                // }
            }
        }

        log.info("[告警规则引擎] 事件处理完成: eventId={}, matchedRules={}",
                event.getEventId(), matchedCount);

        return matchedCount;
    }

    /**
     * 匹配告警规则
     * <p>
     * 匹配条件：
     * 1. 事件类型匹配
     * 2. 置信度 >= 规则阈值
     * 3. 时间段在生效范围内
     * 4. 设备ID匹配（如果规则指定了设备）
     * </p>
     *
     * @param event 设备 AI 事件
     * @param rule 告警规则
     * @return 是否匹配
     */
    private boolean matchRule(DeviceAIEventEntity event, AlarmRuleEntity rule) {
        // 1. 置信度检查
        if (event.getConfidence().doubleValue() < rule.getConfidenceThreshold()) {
            log.trace("[告警规则引擎] 置信度不匹配: eventConfidence={}, ruleThreshold={}",
                    event.getConfidence(), rule.getConfidenceThreshold());
            return false;
        }

        // 2. 设备ID检查（如果规则指定了设备）
        if (rule.getDeviceId() != null && !rule.getDeviceId().isEmpty()) {
            if (!rule.getDeviceId().equals(event.getDeviceId())) {
                log.trace("[告警规则引擎] 设备ID不匹配: eventDeviceId={}, ruleDeviceId={}",
                        event.getDeviceId(), rule.getDeviceId());
                return false;
            }
        }

        // 3. 时间段检查
        if (!isInEffectiveTime(event.getEventTime(), rule)) {
            log.trace("[告警规则引擎] 时间段不匹配: eventTime={}, effectiveTime={}-{}",
                    event.getEventTime(), rule.getEffectiveStartTime(), rule.getEffectiveEndTime());
            return false;
        }

        log.debug("[告警规则引擎] 规则匹配成功: ruleId={}, ruleName={}",
                rule.getRuleId(), rule.getRuleName());

        return true;
    }

    /**
     * 检查事件时间是否在规则生效时间段内
     *
     * @param eventTime 事件时间
     * @param rule 告警规则
     * @return 是否在生效时间内
     */
    private boolean isInEffectiveTime(LocalDateTime eventTime, AlarmRuleEntity rule) {
        if (rule.getEffectiveStartTime() == null || rule.getEffectiveEndTime() == null) {
            // 未设置时间限制，全天生效
            return true;
        }

        LocalTime eventLocalTime = eventTime.toLocalTime();
        LocalTime startTime = rule.getEffectiveStartTime();
        LocalTime endTime = rule.getEffectiveEndTime();

        // 判断事件时间是否在 [startTime, endTime] 范围内
        if (startTime.isBefore(endTime)) {
            // 同一天内：10:00 - 18:00
            return !eventLocalTime.isBefore(startTime) && !eventLocalTime.isAfter(endTime);
        } else {
            // 跨天：22:00 - 06:00
            return !eventLocalTime.isBefore(startTime) || !eventLocalTime.isAfter(endTime);
        }
    }

    /**
     * 创建告警记录
     *
     * @param event 设备 AI 事件
     * @param rule 告警规则
     * @return 告警记录
     */
    private AlarmRecordEntity createAlarmRecord(DeviceAIEventEntity event, AlarmRuleEntity rule) {
        AlarmRecordEntity alarm = new AlarmRecordEntity();
        alarm.setAlarmId(UUID.randomUUID().toString().replace("-", ""));
        alarm.setRuleId(rule.getRuleId());
        alarm.setRuleName(rule.getRuleName());
        alarm.setEventId(event.getEventId());
        alarm.setDeviceId(event.getDeviceId());
        alarm.setDeviceCode(event.getDeviceCode());
        alarm.setEventType(event.getEventType());
        alarm.setAlarmLevel(rule.getAlarmLevel());
        alarm.setAlarmStatus(0); // 待处理
        alarm.setConfidence(event.getConfidence().doubleValue());
        alarm.setBbox(event.getBbox());
        alarm.setSnapshotUrl(null); // TODO: 生成图片URL
        alarm.setAlarmMessage(generateAlarmMessage(event, rule));
        alarm.setAlarmTime(event.getEventTime());
        alarm.setNotificationSent(0); // 未推送

        return alarm;
    }

    /**
     * 生成告警消息
     *
     * @param event 设备 AI 事件
     * @param rule 告警规则
     * @return 告警消息
     */
    private String generateAlarmMessage(DeviceAIEventEntity event, AlarmRuleEntity rule) {
        String template = rule.getAlarmMessageTemplate();

        if (template == null || template.isEmpty()) {
            template = "检测到{eventType}，置信度{confidence}，设备{deviceName}";
        }

        // 简单的模板变量替换
        String message = template
                .replace("{eventType}", getEventTypeName(event.getEventType()))
                .replace("{confidence}", event.getConfidence().toString())
                .replace("{deviceCode}", event.getDeviceCode())
                .replace("{deviceName}", event.getDeviceCode())
                .replace("{eventTime}", event.getEventTime().toString());

        return message;
    }

    /**
     * 获取事件类型中文名称
     */
    private String getEventTypeName(String eventType) {
        return switch (eventType) {
            case "FALL_DETECTION" -> "跌倒检测";
            case "LOITERING_DETECTION" -> "徘徊检测";
            case "GATHERING_DETECTION" -> "聚集检测";
            case "FIGHTING_DETECTION" -> "打架检测";
            case "RUNNING_DETECTION" -> "奔跑检测";
            case "CLIMBING_DETECTION" -> "攀爬检测";
            case "FACE_DETECTION" -> "人脸检测";
            case "INTRUSION_DETECTION" -> "入侵检测";
            default -> eventType;
        };
    }

    /**
     * 查询告警规则列表
     *
     * @param ruleType 规则类型（可选）
     * @param ruleStatus 规则状态（可选）
     * @return 告警规则列表
     */
    public List<AlarmRuleEntity> queryRules(String ruleType, Integer ruleStatus) {
        LambdaQueryWrapper<AlarmRuleEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (ruleType != null && !ruleType.isEmpty()) {
            queryWrapper.eq(AlarmRuleEntity::getRuleType, ruleType);
        }

        if (ruleStatus != null) {
            queryWrapper.eq(AlarmRuleEntity::getRuleStatus, ruleStatus);
        }

        queryWrapper.orderByDesc(AlarmRuleEntity::getPriority);

        return alarmRuleDao.selectList(queryWrapper);
    }

    /**
     * 查询告警记录列表
     *
     * @param deviceId 设备ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param alarmStatus 告警状态（可选）
     * @return 告警记录列表
     */
    public List<AlarmRecordEntity> queryAlarmRecords(
            String deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer alarmStatus) {

        LambdaQueryWrapper<AlarmRecordEntity> queryWrapper = new LambdaQueryWrapper<>();

        if (deviceId != null && !deviceId.isEmpty()) {
            queryWrapper.eq(AlarmRecordEntity::getDeviceId, deviceId);
        }

        if (startTime != null) {
            queryWrapper.ge(AlarmRecordEntity::getAlarmTime, startTime);
        }

        if (endTime != null) {
            queryWrapper.le(AlarmRecordEntity::getAlarmTime, endTime);
        }

        if (alarmStatus != null) {
            queryWrapper.eq(AlarmRecordEntity::getAlarmStatus, alarmStatus);
        }

        queryWrapper.orderByDesc(AlarmRecordEntity::getAlarmTime);

        return alarmRecordDao.selectList(queryWrapper);
    }

    /**
     * 更新告警状态
     *
     * @param alarmId 告警ID
     * @param alarmStatus 告警状态
     * @param handlerId 处理人ID
     * @param handlerName 处理人姓名
     * @param handleRemark 处理备注
     */
    public void updateAlarmStatus(
            String alarmId,
            Integer alarmStatus,
            Long handlerId,
            String handlerName,
            String handleRemark) {

        AlarmRecordEntity alarm = new AlarmRecordEntity();
        alarm.setAlarmId(alarmId);
        alarm.setAlarmStatus(alarmStatus);
        alarm.setHandlerId(handlerId);
        alarm.setHandlerName(handlerName);
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setHandleRemark(handleRemark);

        alarmRecordDao.updateById(alarm);

        log.info("[告警规则引擎] 告警状态更新: alarmId={}, alarmStatus={}, handlerId={}",
                alarmId, alarmStatus, handlerId);
    }
}
