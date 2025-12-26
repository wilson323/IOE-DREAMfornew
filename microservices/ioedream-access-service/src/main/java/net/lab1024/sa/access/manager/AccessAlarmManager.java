package net.lab1024.sa.access.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 门禁报警管理器
 * <p>
 * 管理门禁系统各类报警：
 * - 非法闯入报警
 * - 门长时间未关闭报警
 * - 强行开门报警
 * - 设备离线报警
 * - 尾随检测报警
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class AccessAlarmManager {

    // 显式添加logger声明以确保编译通过

    // 报警记录存储（生产环境应使用数据库）
    private final Map<String, AlarmRecord> alarmRecords = new ConcurrentHashMap<>();

    // 报警配置
    private static final long DOOR_OPEN_TIMEOUT_SECONDS = 30; // 门开启超时时间
    private static final int TAILGATING_DETECTION_WINDOW_SECONDS = 10; // 尾随检测时间窗口

    /**
     * 处理非法闯入报警
     *
     * @param deviceId 设备ID
     * @param locationInfo 位置信息
     * @return 报警记录ID
     */
    public String handleUnauthorizedIntrusion(String deviceId, String locationInfo) {
        log.warn("[门禁报警] 检测到非法闯入: deviceId={}, location={}", deviceId, locationInfo);

        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(generateAlarmId());
        record.setAlarmType(AlarmType.UNAUTHORIZED_INTRUSION);
        record.setDeviceId(deviceId);
        record.setLocationInfo(locationInfo);
        record.setAlarmTime(LocalDateTime.now());
        record.setSeverity(AlarmSeverity.HIGH);
        record.setStatus(AlarmStatus.ACTIVE);

        alarmRecords.put(record.getAlarmId(), record);

        // 触发实时通知
        triggerAlarmNotification(record);

        // 联动视频监控
        linkageVideoSurveillance(record);

        log.info("[门禁报警] 非法闯入报警已创建: alarmId={}", record.getAlarmId());
        return record.getAlarmId();
    }

    /**
     * 处理门长时间未关闭报警
     *
     * @param deviceId 设备ID
     * @param openDurationSeconds 开启时长（秒）
     * @return 报警记录ID
     */
    public String handleDoorOpenTimeout(String deviceId, long openDurationSeconds) {
        log.warn("[门禁报警] 检测到门长时间未关闭: deviceId={}, duration={}s",
                deviceId, openDurationSeconds);

        if (openDurationSeconds < DOOR_OPEN_TIMEOUT_SECONDS) {
            log.debug("[门禁报警] 门开启时长未超时，无需报警: {}s", openDurationSeconds);
            return null;
        }

        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(generateAlarmId());
        record.setAlarmType(AlarmType.DOOR_OPEN_TIMEOUT);
        record.setDeviceId(deviceId);
        record.setAlarmTime(LocalDateTime.now());
        record.setSeverity(AlarmSeverity.MEDIUM);
        record.setStatus(AlarmStatus.ACTIVE);
        record.setDescription(String.format("门已开启%d秒，超过阈值%d秒",
                openDurationSeconds, DOOR_OPEN_TIMEOUT_SECONDS));

        alarmRecords.put(record.getAlarmId(), record);

        triggerAlarmNotification(record);

        log.info("[门禁报警] 门开启超时报警已创建: alarmId={}", record.getAlarmId());
        return record.getAlarmId();
    }

    /**
     * 处理强行开门报警
     *
     * @param deviceId 设备ID
     * @param locationInfo 位置信息
     * @return 报警记录ID
     */
    public String handleForcedOpen(String deviceId, String locationInfo) {
        log.error("[门禁报警] 检测到强行开门: deviceId={}, location={}", deviceId, locationInfo);

        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(generateAlarmId());
        record.setAlarmType(AlarmType.FORCED_OPEN);
        record.setDeviceId(deviceId);
        record.setLocationInfo(locationInfo);
        record.setAlarmTime(LocalDateTime.now());
        record.setSeverity(AlarmSeverity.CRITICAL);
        record.setStatus(AlarmStatus.ACTIVE);

        alarmRecords.put(record.getAlarmId(), record);

        // 高危报警：触发更多联动
        triggerAlarmNotification(record);
        linkageVideoSurveillance(record);
        notifySecurityPersonnel(record);

        log.info("[门禁报警] 强行开门报警已创建: alarmId={}", record.getAlarmId());
        return record.getAlarmId();
    }

    /**
     * 处理设备离线报警
     *
     * @param deviceId 设备ID
     * @param lastOnlineTime 最后在线时间
     * @return 报警记录ID
     */
    public String handleDeviceOffline(String deviceId, LocalDateTime lastOnlineTime) {
        log.warn("[门禁报警] 检测到设备离线: deviceId={}, lastOnline={}", deviceId, lastOnlineTime);

        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(generateAlarmId());
        record.setAlarmType(AlarmType.DEVICE_OFFLINE);
        record.setDeviceId(deviceId);
        record.setAlarmTime(LocalDateTime.now());
        record.setSeverity(AlarmSeverity.MEDIUM);
        record.setStatus(AlarmStatus.ACTIVE);
        record.setDescription("设备自" + lastOnlineTime + "起离线");

        alarmRecords.put(record.getAlarmId(), record);

        triggerAlarmNotification(record);

        log.info("[门禁报警] 设备离线报警已创建: alarmId={}", record.getAlarmId());
        return record.getAlarmId();
    }

    /**
     * 处理尾随检测报警
     *
     * @param deviceId 设备ID
     * @param authorizedUserId 已授权用户ID
     * @param tailgatingCount 尾随人数
     * @return 报警记录ID
     */
    public String handleTailgatingDetection(String deviceId, Long authorizedUserId, int tailgatingCount) {
        log.warn("[门禁报警] 检测到尾随: deviceId={}, authorizedUserId={}, count={}",
                deviceId, authorizedUserId, tailgatingCount);

        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(generateAlarmId());
        record.setAlarmType(AlarmType.TAILGATING);
        record.setDeviceId(deviceId);
        record.setAlarmTime(LocalDateTime.now());
        record.setSeverity(AlarmSeverity.HIGH);
        record.setStatus(AlarmStatus.ACTIVE);
        record.setDescription(String.format("检测到%d人尾随用户%d通过", tailgatingCount, authorizedUserId));

        alarmRecords.put(record.getAlarmId(), record);

        triggerAlarmNotification(record);
        linkageVideoSurveillance(record);

        log.info("[门禁报警] 尾随检测报警已创建: alarmId={}", record.getAlarmId());
        return record.getAlarmId();
    }

    /**
     * 处理反潜回违规报警
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param locationInfo 位置信息
     * @return 报警记录ID
     */
    public String handleAntiPassbackViolation(String deviceId, Long userId, String locationInfo) {
        log.warn("[门禁报警] 检测到反潜回违规: deviceId={}, userId={}, location={}",
                deviceId, userId, locationInfo);

        AlarmRecord record = new AlarmRecord();
        record.setAlarmId(generateAlarmId());
        record.setAlarmType(AlarmType.ANTI_PASSBACK_VIOLATION);
        record.setDeviceId(deviceId);
        record.setUserId(userId);
        record.setLocationInfo(locationInfo);
        record.setAlarmTime(LocalDateTime.now());
        record.setSeverity(AlarmSeverity.MEDIUM);
        record.setStatus(AlarmStatus.ACTIVE);
        record.setDescription("用户" + userId + "违反反潜回规则");

        alarmRecords.put(record.getAlarmId(), record);

        triggerAlarmNotification(record);

        log.info("[门禁报警] 反潜回违规报警已创建: alarmId={}", record.getAlarmId());
        return record.getAlarmId();
    }

    /**
     * 查询报警记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param alarmType 报警类型（可选）
     * @return 报警记录列表
     */
    public List<AlarmRecord> queryAlarmRecords(LocalDateTime startTime, LocalDateTime endTime,
                                               String alarmType) {
        log.info("[门禁报警] 查询报警记录: startTime={}, endTime={}, type={}", startTime, endTime, alarmType);

        return alarmRecords.values().stream()
                .filter(record -> record.getAlarmTime().isAfter(startTime))
                .filter(record -> record.getAlarmTime().isBefore(endTime))
                .filter(record -> alarmType == null || alarmType.equals(record.getAlarmType().name()))
                .sorted((r1, r2) -> r2.getAlarmTime().compareTo(r1.getAlarmTime()))
                .toList();
    }

    /**
     * 处理报警（消除报警）
     *
     * @param alarmId 报警ID
     * @param handlerId 处理人ID
     * @param handleNote 处理备注
     * @return 是否处理成功
     */
    public boolean handleAlarm(String alarmId, Long handlerId, String handleNote) {
        log.info("[门禁报警] 处理报警: alarmId={}, handlerId={}, note={}", alarmId, handlerId, handleNote);

        AlarmRecord record = alarmRecords.get(alarmId);
        if (record == null) {
            log.warn("[门禁报警] 报警记录不存在: alarmId={}", alarmId);
            return false;
        }

        record.setStatus(AlarmStatus.HANDLED);
        record.setHandledBy(handlerId);
        record.setHandledTime(LocalDateTime.now());
        record.setHandleNote(handleNote);

        log.info("[门禁报警] 报警已处理: alarmId={}", alarmId);
        return true;
    }

    /**
     * 触发报警通知
     */
    private void triggerAlarmNotification(AlarmRecord record) {
        // TODO: 集成通知服务，发送实时通知
        log.info("[门禁报警] 触发报警通知: alarmId={}, type={}, severity={}",
                record.getAlarmId(), record.getAlarmType(), record.getSeverity());
    }

    /**
     * 联动视频监控
     */
    private void linkageVideoSurveillance(AlarmRecord record) {
        // TODO: 联动视频监控，抓拍或录像
        log.info("[门禁报警] 联动视频监控: alarmId={}", record.getAlarmId());
    }

    /**
     * 通知安保人员
     */
    private void notifySecurityPersonnel(AlarmRecord record) {
        // TODO: 通知安保人员（高危报警）
        log.warn("[门禁报警] 通知安保人员: alarmId={}, severity={}",
                record.getAlarmId(), record.getSeverity());
    }

    /**
     * 生成报警ID
     */
    private String generateAlarmId() {
        return "ALM-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 报警类型枚举
     */
    public enum AlarmType {
        UNAUTHORIZED_INTRUSION("非法闯入"),
        DOOR_OPEN_TIMEOUT("门开启超时"),
        FORCED_OPEN("强行开门"),
        DEVICE_OFFLINE("设备离线"),
        TAILGATING("尾随检测"),
        ANTI_PASSBACK_VIOLATION("反潜回违规");

        private final String description;

        AlarmType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 报警级别枚举
     */
    public enum AlarmSeverity {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("严重");

        private final String description;

        AlarmSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 报警状态枚举
     */
    public enum AlarmStatus {
        ACTIVE("待处理"),
        HANDLED("已处理"),
        IGNORED("已忽略");

        private final String description;

        AlarmStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 报警记录
     */
    public static class AlarmRecord {
        private String alarmId;
        private AlarmType alarmType;
        private AlarmSeverity severity;
        private AlarmStatus status;
        private String deviceId;
        private Long userId;
        private String locationInfo;
        private LocalDateTime alarmTime;
        private String description;
        private Long handledBy;
        private LocalDateTime handledTime;
        private String handleNote;

        public String getAlarmId() {
            return alarmId;
        }

        public void setAlarmId(String alarmId) {
            this.alarmId = alarmId;
        }

        public AlarmType getAlarmType() {
            return alarmType;
        }

        public void setAlarmType(AlarmType alarmType) {
            this.alarmType = alarmType;
        }

        public AlarmSeverity getSeverity() {
            return severity;
        }

        public void setSeverity(AlarmSeverity severity) {
            this.severity = severity;
        }

        public AlarmStatus getStatus() {
            return status;
        }

        public void setStatus(AlarmStatus status) {
            this.status = status;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getLocationInfo() {
            return locationInfo;
        }

        public void setLocationInfo(String locationInfo) {
            this.locationInfo = locationInfo;
        }

        public LocalDateTime getAlarmTime() {
            return alarmTime;
        }

        public void setAlarmTime(LocalDateTime alarmTime) {
            this.alarmTime = alarmTime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getHandledBy() {
            return handledBy;
        }

        public void setHandledBy(Long handledBy) {
            this.handledBy = handledBy;
        }

        public LocalDateTime getHandledTime() {
            return handledTime;
        }

        public void setHandledTime(LocalDateTime handledTime) {
            this.handledTime = handledTime;
        }

        public String getHandleNote() {
            return handleNote;
        }

        public void setHandleNote(String handleNote) {
            this.handleNote = handleNote;
        }

        @Override
        public String toString() {
            return "AlarmRecord{" +
                    "alarmId='" + alarmId + '\'' +
                    ", alarmType=" + alarmType +
                    ", severity=" + severity +
                    ", status=" + status +
                    ", deviceId='" + deviceId + '\'' +
                    ", userId=" + userId +
                    ", alarmTime=" + alarmTime +
                    '}';
        }
    }
}
