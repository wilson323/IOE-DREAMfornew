package net.lab1024.sa.device.comm.protocol.handler;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.device.comm.protocol.domain.DeviceCommandRequest;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.exception.ProtocolProcessException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 门禁协议处理器
 * <p>
 * 处理门禁设备的协议命令，包括开门、关门、权限验证等操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
@Component
public class AccessProtocolHandler extends BaseProtocolHandler {

    /**
     * 门禁协议命令类型常量
     */
    public static final String COMMAND_OPEN_DOOR = "OPEN_DOOR";
    public static final String COMMAND_CLOSE_DOOR = "CLOSE_DOOR";
    public static final String COMMAND_VERIFY_PERMISSION = "VERIFY_PERMISSION";
    public static final String COMMAND_LOCK_DOWN = "LOCK_DOWN";
    public static final String COMMAND_UNLOCK = "UNLOCK";
    public static final String COMMAND_GET_STATUS = "GET_STATUS";
    public static final String COMMAND_SET_CONFIG = "SET_CONFIG";

    public AccessProtocolHandler() {
        super("ACCESS");
    }

    @Override
    protected void doValidateCommand(DeviceCommandRequest request) {
        String commandType = request.getCommandType();

        // 校验门禁协议特定的命令类型
        switch (commandType) {
            case COMMAND_OPEN_DOOR:
                validateOpenDoorCommand(request);
                break;
            case COMMAND_VERIFY_PERMISSION:
                validateVerifyPermissionCommand(request);
                break;
            case COMMAND_SET_CONFIG:
                validateSetConfigCommand(request);
                break;
            case COMMAND_CLOSE_DOOR:
            case COMMAND_LOCK_DOWN:
            case COMMAND_UNLOCK:
            case COMMAND_GET_STATUS:
                // 这些命令只需要基本的参数校验
                break;
            default:
                throw ProtocolProcessException.processFailed(
                        String.format("不支持的门禁协议命令类型: %s", commandType));
        }
    }

    @Override
    protected ProtocolProcessResult processCommand(DeviceCommandRequest request) {
        String commandType = request.getCommandType();
        Long deviceId = request.getDeviceId();

        log.info("[门禁协议] 执行门禁命令: deviceId={}, commandType={}", deviceId, commandType);

        switch (commandType) {
            case COMMAND_OPEN_DOOR:
                return processOpenDoorCommand(request);
            case COMMAND_CLOSE_DOOR:
                return processCloseDoorCommand(request);
            case COMMAND_VERIFY_PERMISSION:
                return processVerifyPermissionCommand(request);
            case COMMAND_LOCK_DOWN:
                return processLockDownCommand(request);
            case COMMAND_UNLOCK:
                return processUnlockCommand(request);
            case COMMAND_GET_STATUS:
                return processGetStatusCommand(request);
            case COMMAND_SET_CONFIG:
                return processSetConfigCommand(request);
            default:
                throw ProtocolProcessException.processFailed(
                        String.format("未实现的门禁协议命令: %s", commandType));
        }
    }

    /**
     * 校验开门命令参数
     */
    private void validateOpenDoorCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData != null && commandData.containsKey("openDuration")) {
            Object duration = commandData.get("openDuration");
            if (duration != null && duration instanceof Integer) {
                int openDuration = (Integer) duration;
                if (openDuration <= 0 || openDuration > 60) {
                    throw ProtocolProcessException.processFailed("开门时长必须在1-60秒之间");
                }
            }
        }
    }

    /**
     * 校验权限验证命令参数
     */
    private void validateVerifyPermissionCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || !commandData.containsKey("userId")) {
            throw ProtocolProcessException.processFailed("权限验证命令缺少用户ID参数");
        }

        Object userId = commandData.get("userId");
        if (!(userId instanceof String) && !(userId instanceof Long)) {
            throw ProtocolProcessException.processFailed("用户ID参数类型错误");
        }
    }

    /**
     * 校验配置设置命令参数
     */
    private void validateSetConfigCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        if (commandData == null || commandData.isEmpty()) {
            throw ProtocolProcessException.processFailed("配置设置命令缺少配置参数");
        }
    }

    /**
     * 处理开门命令
     */
    private ProtocolProcessResult processOpenDoorCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        // 默认开门时长3秒
        int openDuration = 3;
        if (commandData != null && commandData.containsKey("openDuration")) {
            openDuration = (Integer) commandData.get("openDuration");
        }

        // 模拟开门操作
        resultData.put("action", "DOOR_OPENED");
        resultData.put("openDuration", openDuration);
        resultData.put("timestamp", System.currentTimeMillis());

        // 这里应该调用实际的门禁设备协议接口
        log.info("[门禁协议] 执行开门操作: deviceId={}, openDuration={}秒",
                request.getDeviceId(), openDuration);

        return createSuccessResult("门禁开门命令执行成功", resultData);
    }

    /**
     * 处理关门命令
     */
    private ProtocolProcessResult processCloseDoorCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();

        // 模拟关门操作
        resultData.put("action", "DOOR_CLOSED");
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[门禁协议] 执行关门操作: deviceId={}", request.getDeviceId());

        return createSuccessResult("门禁关门命令执行成功", resultData);
    }

    /**
     * 处理权限验证命令
     */
    private ProtocolProcessResult processVerifyPermissionCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        String userId = String.valueOf(commandData.get("userId"));

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("userId", userId);
        resultData.put("timestamp", System.currentTimeMillis());

        // 模拟权限验证逻辑
        boolean hasPermission = simulatePermissionCheck(userId, request.getDeviceId());
        resultData.put("hasPermission", hasPermission);

        if (hasPermission) {
            log.info("[门禁协议] 权限验证通过: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createSuccessResult("权限验证通过", resultData);
        } else {
            log.warn("[门禁协议] 权限验证失败: deviceId={}, userId={}", request.getDeviceId(), userId);
            return createFailureResult("PERMISSION_DENIED", "权限验证失败");
        }
    }

    /**
     * 处理锁死命令
     */
    private ProtocolProcessResult processLockDownCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("action", "DOOR_LOCKED_DOWN");
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[门禁协议] 执行锁死操作: deviceId={}", request.getDeviceId());

        return createSuccessResult("门禁锁死命令执行成功", resultData);
    }

    /**
     * 处理解锁命令
     */
    private ProtocolProcessResult processUnlockCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("action", "DOOR_UNLOCKED");
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[门禁协议] 执行解锁操作: deviceId={}", request.getDeviceId());

        return createSuccessResult("门禁解锁命令执行成功", resultData);
    }

    /**
     * 处理状态查询命令
     */
    private ProtocolProcessResult processGetStatusCommand(DeviceCommandRequest request) {
        Map<String, Object> resultData = new HashMap<>();

        // 模拟获取设备状态
        resultData.put("deviceId", request.getDeviceId());
        resultData.put("isOnline", true);
        resultData.put("isLocked", false);
        resultData.put("lastUpdate", System.currentTimeMillis());

        log.info("[门禁协议] 查询设备状态: deviceId={}", request.getDeviceId());

        return createSuccessResult("设备状态查询成功", resultData);
    }

    /**
     * 处理配置设置命令
     */
    private ProtocolProcessResult processSetConfigCommand(DeviceCommandRequest request) {
        Map<String, Object> commandData = request.getCommandData();
        Map<String, Object> resultData = new HashMap<>();

        // 模拟设置设备配置
        resultData.put("configUpdated", true);
        resultData.put("updatedConfig", commandData);
        resultData.put("timestamp", System.currentTimeMillis());

        log.info("[门禁协议] 设置设备配置: deviceId={}, config={}",
                request.getDeviceId(), commandData);

        return createSuccessResult("设备配置设置成功", resultData);
    }

    /**
     * 模拟权限检查逻辑
     * <p>
     * 实际实现中应该调用数据库或权限服务进行验证
     * </p>
     */
    private boolean simulatePermissionCheck(String userId, Long deviceId) {
        // 这里是模拟实现，实际应该查询用户的权限
        // 简单起见，假设用户ID不为空就有权限
        return userId != null && !userId.trim().isEmpty();
    }
}