package net.lab1024.sa.attendance.template;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import net.lab1024.sa.attendance.domain.form.AttendancePunchForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.gateway.domain.response.DeviceResponse;
import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 考勤处理流程模板
 * <p>
 * 使用模板方法模式定义考勤处理的标准流程
 * 严格遵循CLAUDE.md规范：
 * - 使用模板方法模式实现
 * - 定义标准流程，子类实现具体步骤
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
public abstract class AbstractAttendanceProcessTemplate {

    @Resource
    protected DeviceDao deviceDao;

    @Resource
    protected GatewayServiceClient gatewayServiceClient;

    /**
     * 模板方法: 考勤处理流程
     * <p>
     * 定义考勤处理的标准流程，子类不能重写此方法
     * </p>
     *
     * @param punchForm 打卡表单
     * @return 考勤结果
     */
    public final AttendanceProcessResult processAttendance(AttendancePunchForm punchForm) {
        try {
            // 1. 参数校验
            validate(punchForm);

            // 2. 设备验证
            DeviceResponse device = validateDevice(punchForm.getDeviceId());

            // 3. 用户识别(抽象方法 - 子类实现)
            UserIdentityResult identity = identifyUser(punchForm);
            if (!identity.isSuccess()) {
                return AttendanceProcessResult.denied("身份识别失败: " + identity.getMessage());
            }

            // 4. 打卡记录(抽象方法 - 子类实现)
            AttendanceRecordResult recordResult = recordPunch(identity, device, punchForm);

            // 5. 考勤计算(抽象方法 - 子类实现)
            AttendanceResultVO attendanceResult = calculateAttendance(identity, recordResult, punchForm);

            // 6. 记录考勤结果
            saveAttendanceResult(identity, attendanceResult);

            // 7. 事件通知(钩子方法)
            notifyAttendanceEvent(identity, device, attendanceResult);

            return AttendanceProcessResult.success(attendanceResult);

        } catch (Exception e) {
            log.error("[考勤处理流程异常] punchForm={}", punchForm, e);
            return AttendanceProcessResult.error("系统异常: " + e.getMessage());
        }
    }

    /**
     * 抽象方法: 用户识别
     * <p>
     * 子类必须实现具体的用户识别逻辑
     * </p>
     *
     * @param punchForm 打卡表单
     * @return 用户识别结果
     */
    protected abstract UserIdentityResult identifyUser(AttendancePunchForm punchForm);

    /**
     * 抽象方法: 记录打卡
     * <p>
     * 子类必须实现具体的打卡记录逻辑
     * </p>
     *
     * @param identity  用户识别结果
     * @param device    设备实体
     * @param punchForm 打卡表单
     * @return 打卡记录结果
     */
    protected abstract AttendanceRecordResult recordPunch(
            UserIdentityResult identity, DeviceResponse device, AttendancePunchForm punchForm);

    /**
     * 抽象方法: 计算考勤
     * <p>
     * 子类必须实现具体的考勤计算逻辑（结合排班、规则等）
     * </p>
     *
     * @param identity     用户识别结果
     * @param recordResult 打卡记录结果
     * @param punchForm    打卡表单
     * @return 考勤结果
     */
    protected abstract AttendanceResultVO calculateAttendance(
            UserIdentityResult identity, AttendanceRecordResult recordResult, AttendancePunchForm punchForm);

    /**
     * 钩子方法: 事件通知(可选覆盖)
     * <p>
     * 子类可以选择覆盖此方法以实现自定义通知逻辑
     * </p>
     *
     * @param identity         用户识别结果
     * @param device           设备实体
     * @param attendanceResult 考勤结果
     */
    protected void notifyAttendanceEvent(UserIdentityResult identity,
            DeviceResponse device, AttendanceResultVO attendanceResult) {
        // 默认空实现
    }

    /**
     * 参数校验
     */
    private void validate(AttendancePunchForm punchForm) {
        if (punchForm == null) {
            throw new IllegalArgumentException("打卡表单不能为空");
        }
        if (punchForm.getDeviceId() == null) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
    }

    /**
     * 设备验证
     */
    private DeviceResponse validateDevice(Long deviceId) {
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("设备不存在: " + deviceId);
        }
        if (device.getDeviceStatus() != null && device.getDeviceStatus() != 1) {
            throw new IllegalArgumentException("设备未启用: " + deviceId);
        }
        return convertToDeviceResponse(device);
    }

    /**
     * 将DeviceEntity转换为DeviceResponse
     */
    private DeviceResponse convertToDeviceResponse(DeviceEntity deviceEntity) {
        return DeviceResponse.builder()
                .deviceId(deviceEntity.getDeviceId())
                .deviceCode(deviceEntity.getDeviceCode())
                .deviceName(deviceEntity.getDeviceName())
                .deviceType(String.valueOf(deviceEntity.getDeviceType()))
                .deviceSubType(String.valueOf(deviceEntity.getDeviceSubType()))
                .status(deviceEntity.getDeviceStatus())
                .areaId(deviceEntity.getAreaId())
                .location("") // Location字段不存在，暂时使用空字符串
                .ipAddress(deviceEntity.getIpAddress())
                .port(deviceEntity.getPort())
                .manufacturer(deviceEntity.getBrand()) // 使用brand字段替代manufacturer
                .model(deviceEntity.getModel())
                .online(deviceEntity.getDeviceStatus() == 1) // 根据deviceStatus判断是否在线
                .createTime(deviceEntity.getCreateTime())
                .updateTime(deviceEntity.getUpdateTime())
                .build();
    }

    /**
     * 保存考勤结果
     */
    private void saveAttendanceResult(UserIdentityResult identity, AttendanceResultVO attendanceResult) {
        log.info("[考勤处理] 保存考勤结果 userId={}, date={}, status={}",
                identity.getUserId(), attendanceResult.getDate(), attendanceResult.getStatus());
    }

    /**
     * 考勤处理结果
     */
    public static class AttendanceProcessResult {
        private final boolean success;
        private final String message;
        private final AttendanceResultVO attendanceResult;

        private AttendanceProcessResult(boolean success, String message, AttendanceResultVO attendanceResult) {
            this.success = success;
            this.message = message;
            this.attendanceResult = attendanceResult;
        }

        public static AttendanceProcessResult success(AttendanceResultVO attendanceResult) {
            return new AttendanceProcessResult(true, "处理成功", attendanceResult);
        }

        public static AttendanceProcessResult denied(String message) {
            return new AttendanceProcessResult(false, message, null);
        }

        public static AttendanceProcessResult error(String message) {
            return new AttendanceProcessResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public AttendanceResultVO getAttendanceResult() {
            return attendanceResult;
        }
    }

    /**
     * 用户识别结果
     */
    public static class UserIdentityResult {
        private final boolean success;
        private final Long userId;
        private final String message;

        private UserIdentityResult(boolean success, Long userId, String message) {
            this.success = success;
            this.userId = userId;
            this.message = message;
        }

        public static UserIdentityResult success(Long userId) {
            return new UserIdentityResult(true, userId, "识别成功");
        }

        public static UserIdentityResult failed(String message) {
            return new UserIdentityResult(false, null, message);
        }

        public static UserIdentityResult error(String message) {
            return new UserIdentityResult(false, null, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public Long getUserId() {
            return userId;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 打卡记录结果
     */
    public static class AttendanceRecordResult {
        private final Long recordId;
        private final LocalDateTime punchTime;
        private final String punchType;

        private AttendanceRecordResult(Long recordId, LocalDateTime punchTime, String punchType) {
            this.recordId = recordId;
            this.punchTime = punchTime;
            this.punchType = punchType;
        }

        public static AttendanceRecordResult of(Long recordId, LocalDateTime punchTime, String punchType) {
            return new AttendanceRecordResult(recordId, punchTime, punchType);
        }

        public Long getRecordId() {
            return recordId;
        }

        public LocalDateTime getPunchTime() {
            return punchTime;
        }

        public String getPunchType() {
            return punchType;
        }
    }
}
