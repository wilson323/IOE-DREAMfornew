package net.lab1024.sa.consume.util;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.entity.BaseEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.domain.entity.ConsumeTransactionEntity;

/**
 * 设备实体类型转换工具类
 * <p>
 * 提供设备实体的类型转换和兼容性方法，解决Entity字段类型不匹配问题
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Slf4j
public class DeviceEntityConverter {

    /**
     * 设置设备ID（支持String类型兼容性）
     *
     * @param entity    设备实体
     * @param deviceId  设备ID（String类型）
     * @throws IllegalArgumentException 参数无效时抛出异常
     */
    public static void setDeviceId(DeviceEntity entity, String deviceId) {
        if (entity == null) {
            throw new IllegalArgumentException("设备实体不能为空");
        }

        if (deviceId == null || deviceId.trim().isEmpty()) {
            entity.setDeviceId(null);
            return;
        }

        try {
            Long deviceLongId = parseDeviceId(deviceId);
            entity.setDeviceId(deviceLongId);
            log.debug("[设备实体转换] 设置设备ID: deviceId={}, convertedLong={}", deviceId, deviceLongId);
        } catch (NumberFormatException e) {
            log.error("[设备实体转换] 设备ID格式错误: deviceId={}, error={}", deviceId, e.getMessage());
            throw new IllegalArgumentException("设备ID格式无效: " + deviceId);
        }
    }

    /**
     * 设置设备ID（支持Object类型兼容性）
     *
     * @param entity    设备实体
     * @param deviceId  设备ID（Object类型）
     */
    public static void setDeviceId(DeviceEntity entity, Object deviceId) {
        if (entity == null) {
            throw new IllegalArgumentException("设备实体不能为空");
        }

        if (deviceId == null) {
            entity.setDeviceId(null);
            return;
        }

        if (deviceId instanceof String) {
            setDeviceId(entity, (String) deviceId);
        } else if (deviceId instanceof Long) {
            entity.setDeviceId((Long) deviceId);
        } else if (deviceId instanceof Integer) {
            entity.setDeviceId(((Integer) deviceId).longValue());
        } else {
            throw new IllegalArgumentException("设备ID类型不支持: " + deviceId.getClass().getSimpleName());
        }
    }

    /**
     * 设置设备类型（支持String类型兼容性）
     *
     * @param entity     设备实体
     * @param deviceType 设备类型（String类型）
     */
    public static void setDeviceType(DeviceEntity entity, String deviceType) {
        if (entity == null) {
            throw new IllegalArgumentException("设备实体不能为空");
        }

        if (deviceType == null || deviceType.trim().isEmpty()) {
            entity.setDeviceType(null);
            return;
        }

        try {
            Integer deviceTypeInt = parseDeviceType(deviceType);
            entity.setDeviceType(deviceTypeInt);
            log.debug("[设备实体转换] 设置设备类型: deviceType={}, convertedInt={}", deviceType, deviceTypeInt);
        } catch (NumberFormatException e) {
            log.error("[设备实体转换] 设备类型格式错误: deviceType={}, error={}", deviceType, e.getMessage());
            throw new IllegalArgumentException("设备类型格式无效: " + deviceType);
        }
    }

    /**
     * 设置设备类型（支持Object类型兼容性）
     *
     * @param entity     设备实体
     * @param deviceType 设备类型（Object类型）
     */
    public static void setDeviceType(DeviceEntity entity, Object deviceType) {
        if (entity == null) {
            throw new IllegalArgumentException("设备实体不能为空");
        }

        if (deviceType == null) {
            entity.setDeviceType(null);
            return;
        }

        if (deviceType instanceof String) {
            setDeviceType(entity, (String) deviceType);
        } else if (deviceType instanceof Integer) {
            entity.setDeviceType((Integer) deviceType);
        } else if (deviceType instanceof Long) {
            entity.setDeviceType(((Long) deviceType).intValue());
        } else {
            throw new IllegalArgumentException("设备类型类型不支持: " + deviceType.getClass().getSimpleName());
        }
    }

    /**
     * 解析设备ID
     *
     * @param deviceIdStr 设备ID字符串
     * @return 设备ID长整型
     * @throws NumberFormatException 格式错误时抛出异常
     */
    public static Long parseDeviceId(String deviceIdStr) {
        if (deviceIdStr == null || deviceIdStr.trim().isEmpty()) {
            return null;
        }

        // 移除可能的前缀
        String cleanedId = deviceIdStr.trim();
        if (cleanedId.startsWith("DEV_")) {
            cleanedId = cleanedId.substring(4);
        }

        try {
            return Long.valueOf(cleanedId);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("设备ID格式错误: " + deviceIdStr);
        }
    }

    /**
     * 解析设备类型
     *
     * @param deviceTypeStr 设备类型字符串
     * @return 设备类型整型
     * @throws NumberFormatException 格式错误时抛出异常
     */
    public static Integer parseDeviceType(String deviceTypeStr) {
        if (deviceTypeStr == null || deviceTypeStr.trim().isEmpty()) {
            return null;
        }

        // 支持设备类型名称映射
        String cleanedType = deviceTypeStr.trim().toUpperCase();
        switch (cleanedType) {
            case "ACCESS":
            case "1":
                return 1;
            case "ATTENDANCE":
            case "2":
                return 2;
            case "CONSUME":
            case "3":
                return 3;
            case "CAMERA":
            case "VIDEO":
            case "4":
                return 4;
            case "VISITOR":
            case "5":
                return 5;
            case "BIOMETRIC":
            case "6":
                return 6;
            case "INTERCOM":
            case "7":
                return 7;
            case "ALARM":
            case "8":
                return 8;
            case "SENSOR":
            case "9":
                return 9;
            default:
                // 尝试直接解析数字
                try {
                    return Integer.valueOf(cleanedType);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("设备类型格式错误: " + deviceTypeStr);
                }
        }
    }

    /**
     * 获取设备ID字符串表示
     *
     * @param deviceId 设备ID长整型
     * @return 设备ID字符串
     */
    public static String getDeviceIdString(Long deviceId) {
        if (deviceId == null) {
            return null;
        }
        return String.valueOf(deviceId);
    }

    /**
     * 获取设备ID字符串表示（带前缀）
     *
     * @param deviceId 设备ID长整型
     * @param prefix   前缀
     * @return 带前缀的设备ID字符串
     */
    public static String getDeviceIdString(Long deviceId, String prefix) {
        String deviceIdStr = getDeviceIdString(deviceId);
        if (deviceIdStr == null) {
            return null;
        }
        return prefix != null ? prefix + deviceIdStr : deviceIdStr;
    }

    /**
     * 获取设备类型字符串表示
     *
     * @param deviceType 设备类型整型
     * @return 设备类型字符串
     */
    public static String getDeviceTypeString(Integer deviceType) {
        if (deviceType == null) {
            return null;
        }

        switch (deviceType) {
            case 1:
                return "ACCESS";
            case 2:
                return "ATTENDANCE";
            case 3:
                return "CONSUME";
            case 4:
                return "CAMERA";
            case 5:
                return "VISITOR";
            case 6:
                return "BIOMETRIC";
            case 7:
                return "INTERCOM";
            case 8:
                return "ALARM";
            case 9:
                return "SENSOR";
            default:
                return String.valueOf(deviceType);
        }
    }

    /**
     * 验证设备ID是否有效
     *
     * @param deviceId 设备ID
     * @return 是否有效
     */
    public static boolean isValidDeviceId(Long deviceId) {
        return deviceId != null && deviceId > 0;
    }

    /**
     * 验证设备类型是否有效
     *
     * @param deviceType 设备类型
     * @return 是否有效
     */
    public static boolean isValidDeviceType(Integer deviceType) {
        return deviceType != null && deviceType >= 1 && deviceType <= 9;
    }

    /**
     * 批量转换设备ID
     *
     * @param entities  设备实体列表
     * @param deviceIds 设备ID字符串列表
     */
    public static void batchSetDeviceIds(java.util.List<? extends DeviceEntity> entities, java.util.List<String> deviceIds) {
        if (entities == null || deviceIds == null || entities.size() != deviceIds.size()) {
            throw new IllegalArgumentException("实体列表和设备ID列表长度不匹配");
        }

        for (int i = 0; i < entities.size(); i++) {
            setDeviceId(entities.get(i), deviceIds.get(i));
        }
    }

    /**
     * 创建设备ID转换日志
     *
     * @param fromType 原始类型
     * @param toType   目标类型
     * @param fromValue 原始值
     * @param toValue   目标值
     * @param context  上下文信息
     */
    private static void logConversion(String fromType, String toType, Object fromValue, Object toValue, String context) {
        log.debug("[设备实体转换] {}→{}: {} → {}, 上下文: {}",
                fromType, toType, fromValue, toValue, context);
    }
}