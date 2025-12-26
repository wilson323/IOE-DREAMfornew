package net.lab1024.sa.device.comm.protocol.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 考勤协议处理器
 * <p>
 * 处理考勤设备的协议命令，包括打卡、状态查询、配置管理等操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Component
public class AttendanceProtocolHandler extends BaseProtocolHandler {

    /**
     * 考勤协议命令类型常量
     */
    public static final String COMMAND_PUNCH_IN = "PUNCH_IN";
    public static final String COMMAND_PUNCH_OUT = "PUNCH_OUT";
    public static final String COMMAND_PUNCH_BREAK = "PUNCH_BREAK";
    public static final String COMMAND_PUNCH_RESUME = "PUNCH_RESUME";
    public static final String COMMAND_GET_PUNCH_RECORD = "GET_PUNCH_RECORD";
    public static final String COMMAND_GET_DEVICE_STATUS = "GET_DEVICE_STATUS";
    public static final String COMMAND_SET_DEVICE_CONFIG = "SET_DEVICE_CONFIG";
    public static final String COMMAND_SYNC_TIME = "SYNC_TIME";
    public static final String COMMAND_GET_USER_INFO = "GET_USER_INFO";

    public AttendanceProtocolHandler() {
        super("ATTENDANCE");
    }

    @Override
    protected void doValidateCommand(DeviceCommandRequest request) {
        String commandType = request.getCommandType();

        // 校验考勤协议特定的命令类型
        switch (commandType) {
            case COMMAND_PUNCH_IN:
            case COMMAND_PUNCH_OUT:
            case COMMAND_PUNCH_BREAK:
            case COMMAND_PUNCH_RESUME:
                validatePunchCommand(request);
                break;
            case COMMAND_GET_PUNCH_RECORD:
                validateGetPunchRecordCommand(request);
                break;
            case COMMAND_SET_DEVICE_CONFIG:
                validateSetDeviceConfigCommand(request);
                break;
            case COMMAND_GET_DEVICE_STATUS:
            case COMMAND_SYNC_TIME:
            case COMMAND_GET_USER_INFO:
                // 这些命令只需要基本的参数校验
                break;
            default:
                throw ProtocolProcessException.processFailed(
                        String.format("不支持的考勤协议命令类型: %s", commandType));
        }
    }

    @Override
    protected ProtocolProcessResult processCommand(DeviceCommandRequest request) {
        String commandType = request.getCommandType();
        Long deviceId = request.getDeviceId();

        log.info("[考勤协议] 执行考勤命令: deviceId={}, commandType={}", deviceId, commandType);

        switch (commandType) {
            case COMMAND_PUNCH_IN:
                return processPunchInCommand(request);
            case COMMAND_PUNCH_OUT:
                return processPunchOutCommand(request);
            case COMMAND_PUNCH_BREAK:
                return processPunchBreakCommand(request);
            case COMMAND_PUNCH_RESUME:
                return processPunchResumeCommand(request);
            case COMMAND_GET_PUNCH_RECORD:
                return processGetPunchRecordCommand(request);
            case COMMAND_GET_DEVICE_STATUS:
                return processGetDeviceStatusCommand(request);
            case COMMAND_SET_DEVICE_CONFIG:
                return processSetDeviceConfigCommand(request);
            case COMMAND_SYNC_TIME:
                return processSyncTimeCommand(request);
            case COMMAND_GET_USER_INFO:
                return processGetUserInfoCommand(request);
            default:
                throw ProtocolProcessException.processFailed(
                        String.format("未实现的考勤协议命令: %s", commandType));
        }
    }

    /**
     * 校验打卡命令参数
     */
    private void validatePunchCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || !commandData.containsKey("userId")) {
            throw ProtocolProcessException.processFailed("打卡命令缺少用户ID参数");
        }

        Object userId = commandData.get("userId");
        if (!(userId instanceof String) && !(userId instanceof Long)) {
            throw ProtocolProcessException.processFailed("用户ID参数类型错误");
        }

        // 检查位置信息（可选）
        if (commandData.containsKey("location")) {
            Object location = commandData.get("location");
            if (location != null && !(location instanceof Map)) {
                throw ProtocolProcessException.processFailed("位置信息参数格式错误");
            }
        }
    }

    /**
     * 校验获取打卡记录命令参数
     */
    private void validateGetPunchRecordCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData != null) {
            Object startDate = commandData.get("startDate");
            Object endDate = commandData.get("endDate");

            if (startDate != null && !(startDate instanceof String) && !(startDate instanceof LocalDateTime)) {
                throw ProtocolProcessException.processFailed("开始日期参数类型错误");
            }

            if (endDate != null && !(endDate instanceof String) && !(endDate instanceof LocalDateTime)) {
                throw ProtocolProcessException.processFailed("结束日期参数类型错误");
            }
        }
    }

    /**
     * 校验设置设备配置命令参数
     */
    private void validateSetDeviceConfigCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || commandData.isEmpty()) {
            throw ProtocolProcessException.processFailed("设置设备配置命令缺少配置参数");
        }
    }

    /**
     * 处理上班打卡命令
     */
    private ProtocolProcessResult processPunchInCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("punchType", "IN");
        resultData.put("punchTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        // 获取位置信息
        if (commandData.containsKey("location")) {
            resultData.put("location", commandData.get("location"));
        }

        // 获取生物识别信息
        if (commandData.containsKey("biometricType")) {
            resultData.put("biometricType", commandData.get("biometricType"));
        }

        // 模拟打卡验证
        boolean punchSuccess = simulatePunchValidation(userId, request.getDeviceId(), "IN");
        resultData.put("success", punchSuccess);

        if (punchSuccess) {
            log.info("[考勤协议] 上班打卡成功: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createSuccessResult("上班打卡成功", resultData);
        } else {
            log.warn("[考勤协议] 上班打卡失败: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createFailureResult("PUNCH_FAILED", "上班打卡失败");
        }
    }

    /**
     * 处理下班打卡命令
     */
    private ProtocolProcessResult processPunchOutCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("punchType", "OUT");
        resultData.put("punchTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        if (commandData.containsKey("location")) {
            resultData.put("location", commandData.get("location"));
        }

        boolean punchSuccess = simulatePunchValidation(userId, request.getDeviceId(), "OUT");
        resultData.put("success", punchSuccess);

        if (punchSuccess) {
            log.info("[考勤协议] 下班打卡成功: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createSuccessResult("下班打卡成功", resultData);
        } else {
            log.warn("[考勤协议] 下班打卡失败: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createFailureResult("PUNCH_FAILED", "下班打卡失败");
        }
    }

    /**
     * 处理休息打卡命令
     */
    private ProtocolProcessResult processPunchBreakCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("punchType", "BREAK");
        resultData.put("punchTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        boolean punchSuccess = simulatePunchValidation(userId, request.getDeviceId(), "BREAK");
        resultData.put("success", punchSuccess);

        if (punchSuccess) {
            log.info("[考勤协议] 休息打卡成功: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createSuccessResult("休息打卡成功", resultData);
        } else {
            log.warn("[考勤协议] 休息打卡失败: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createFailureResult("PUNCH_FAILED", "休息打卡失败");
        }
    }

    /**
     * 处理恢复工作打卡命令
     */
    private ProtocolProcessResult processPunchResumeCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("punchType", "RESUME");
        resultData.put("punchTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        boolean punchSuccess = simulatePunchValidation(userId, request.getDeviceId(), "RESUME");
        resultData.put("success", punchSuccess);

        if (punchSuccess) {
            log.info("[考勤协议] 恢复工作打卡成功: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createSuccessResult("恢复工作打卡成功", resultData);
        } else {
            log.warn("[考勤协议] 恢复工作打卡失败: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createFailureResult("PUNCH_FAILED", "恢复工作打卡失败");
        }
    }

    /**
     * 处理获取打卡记录命令
     */
    private ProtocolProcessResult processGetPunchRecordCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        // 模拟获取打卡记录
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("totalRecords", 10); // 模拟数据

        if (commandData != null && commandData.containsKey("userId")) {
            String userId = String.valueOf(commandData.get("userId"));
            resultData.put("userId", userId);
        }

        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[考勤协议] 获取打卡记录: deviceId={}", request.getDeviceId());

        return createSuccessResult("获取打卡记录成功", resultData);
    }

    /**
     * 处理获取设备状态命令
     */
    private ProtocolProcessResult processGetDeviceStatusCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();

        // 模拟获取设备状态
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("isOnline", true);
        resultData.put("batteryLevel", 85);
        resultData.put("lastSyncTime", LocalDateTime.now());
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[考勤协议] 查询设备状态: deviceId={}", request.getDeviceId());

        return createSuccessResult("设备状态查询成功", resultData);
    }

    /**
     * 处理设置设备配置命令
     */
    private ProtocolProcessResult processSetDeviceConfigCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        resultData.put("deviceId", request.getDeviceId());
        resultData.put("configUpdated", true);
        resultData.put("updatedConfig", commandData);
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[考勤协议] 设置设备配置: deviceId={}, config={}",
                request.getDeviceId(), commandData);

        return createSuccessResult("设备配置设置成功", resultData);
    }

    /**
     * 处理时间同步命令
     */
    private ProtocolProcessResult processSyncTimeCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();

        LocalDateTime currentTime = LocalDateTime.now();
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("syncTime", currentTime);
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[考勤协议] 同步设备时间: deviceId={}, syncTime={}", request.getDeviceId(), currentTime);

        return createSuccessResult("设备时间同步成功", resultData);
    }

    /**
     * 处理获取用户信息命令
     */
    private ProtocolProcessResult processGetUserInfoCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        if (commandData != null && commandData.containsKey("userId")) {
            String userId = String.valueOf(commandData.get("userId"));
            resultData.put("userId", userId);
            resultData.put("userName", "User_" + userId); // 模拟用户名
            resultData.put("department", "技术部"); // 模拟部门
        }

        resultData.put("deviceId", request.getDeviceId());
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[考勤协议] 获取用户信息: deviceId={}", request.getDeviceId());

        return createSuccessResult("用户信息获取成功", resultData);
    }

    /**
     * 模拟打卡验证逻辑
     * <p>
     * 实际实现中应该调用数据库或考勤服务进行验证
     * </p>
     */
    private boolean simulatePunchValidation(String userId, Long deviceId, String punchType) {
        // 这里是模拟实现，实际应该：
        // 1. 验证用户是否有打卡权限
        // 2. 检查设备是否在有效位置
        // 3. 验证打卡时间是否合理
        // 4. 记录打卡数据到数据库

        // 简单起见，假设用户ID不为空就有权限
        return userId != null && !userId.trim().isEmpty();
    }
}