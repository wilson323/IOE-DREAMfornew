package net.lab1024.sa.common.organization.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备权限管理器
 * <p>
 * 负责管理设备级别的权限控制
 * 支持基于区域、时间、业务规则的权限验证
 * 实现权限继承和动态权限控制
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class DevicePermissionManager {

    private final AreaUserDao areaUserDao;
    private final AreaDeviceDao areaDeviceDao;

    // 权限缓存
    private final Map<String, Set<String>> deviceUserPermissionCache;

    // 构造函数注入依赖
    public DevicePermissionManager(AreaUserDao areaUserDao, AreaDeviceDao areaDeviceDao) {
        this.areaUserDao = areaUserDao;
        this.areaDeviceDao = areaDeviceDao;
        this.deviceUserPermissionCache = new HashMap<>();
    }

    /**
     * 设备权限类型枚举
     */
    public enum DevicePermissionType {
        ACCESS_CONTROL("access_control", "门禁控制"),
        ATTENDANCE_CHECK("attendance_check", "考勤打卡"),
        CONSUME_PAYMENT("consume_payment", "消费支付"),
        VIDEO_MONITOR("video_monitor", "视频监控"),
        VISITOR_MANAGEMENT("visitor_management", "访客管理"),
        SYSTEM_CONFIG("system_config", "系统配置");

        private final String code;
        private final String description;

        DevicePermissionType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 权限验证结果
     */
    public static class PermissionCheckResult {
        private final boolean allowed;
        private final String reason;
        private final String details;
        private final LocalDateTime checkTime;

        public PermissionCheckResult(boolean allowed, String reason, String details) {
            this.allowed = allowed;
            this.reason = reason;
            this.details = details;
            this.checkTime = LocalDateTime.now();
        }

        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
        public String getDetails() { return details; }
        public LocalDateTime getCheckTime() { return checkTime; }
    }

    /**
     * 检查用户是否有设备访问权限
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param permissionType 权限类型
     * @return 权限检查结果
     */
    public PermissionCheckResult checkDevicePermission(Long userId, String deviceId, DevicePermissionType permissionType) {
        log.debug("[设备权限管理] 检查设备权限, userId={}, deviceId={}, permissionType={}",
                 userId, deviceId, permissionType.getCode());

        try {
            // 1. 获取用户在设备关联区域内的所有权限
            List<AreaUserEntity> userAreaPermissions = getUserPermissionsForDevice(userId, deviceId);
            if (userAreaPermissions.isEmpty()) {
                return new PermissionCheckResult(false, "用户无区域权限", "用户未关联到该设备所在区域");
            }

            // 2. 选择最高权限级别的权限记录
            AreaUserEntity userPermission = userAreaPermissions.stream()
                    .max(Comparator.comparing(AreaUserEntity::getPermissionLevel))
                    .orElse(null);

            if (userPermission == null) {
                return new PermissionCheckResult(false, "权限获取失败", "无法获取用户权限信息");
            }

            // 3. 检查权限级别
            if (!hasRequiredPermissionLevel(userPermission, permissionType)) {
                return new PermissionCheckResult(false, "权限级别不足",
                        String.format("用户权限级别：%d，所需权限级别：%d",
                                userPermission.getPermissionLevel(), getRequiredPermissionLevel(permissionType)));
            }

            // 4. 检查时间权限
            PermissionCheckResult timeCheck = checkTimePermission(userPermission);
            if (!timeCheck.isAllowed()) {
                return timeCheck;
            }

            // 5. 检查业务权限配置
            PermissionCheckResult businessCheck = checkBusinessPermission(userPermission, permissionType);
            if (!businessCheck.isAllowed()) {
                return businessCheck;
            }

            // 6. 权限验证通过
            return new PermissionCheckResult(true, "权限验证通过", "用户拥有设备访问权限");

        } catch (Exception e) {
            log.error("[设备权限管理] 权限检查异常, userId={}, deviceId={}, permissionType={}",
                    userId, deviceId, permissionType.getCode(), e);
            return new PermissionCheckResult(false, "权限检查异常", "系统异常：" + e.getMessage());
        }
    }

    /**
     * 获取用户在设备上的所有权限
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @return 权限类型集合
     */
    public Set<DevicePermissionType> getUserDevicePermissions(Long userId, String deviceId) {
        log.debug("[设备权限管理] 获取用户设备权限, userId={}, deviceId={}", userId, deviceId);

        try {
            // 1. 获取用户区域权限
            List<AreaUserEntity> userAreaPermissions = getUserPermissionsForDevice(userId, deviceId);
            if (userAreaPermissions.isEmpty()) {
                return Collections.emptySet();
            }

            // 2. 获取最高权限级别
            int maxPermissionLevel = userAreaPermissions.stream()
                    .mapToInt(AreaUserEntity::getPermissionLevel)
                    .max()
                    .orElse(0);

            // 3. 根据权限级别确定可用的权限类型
            Set<DevicePermissionType> permissions = getPermissionsByLevel(maxPermissionLevel);

            // 4. 过滤业务权限配置
            return filterPermissionsByBusinessConfig(userAreaPermissions, permissions);

        } catch (Exception e) {
            log.error("[设备权限管理] 获取用户设备权限异常, userId={}, deviceId={}", userId, deviceId, e);
            return Collections.emptySet();
        }
    }

    /**
     * 批量检查用户设备权限
     *
     * @param userId 用户ID
     * @param deviceIds 设备ID列表
     * @param permissionType 权限类型
     * @return 权限检查结果映射
     */
    public Map<String, PermissionCheckResult> batchCheckDevicePermissions(Long userId,
                                                                        List<String> deviceIds,
                                                                        DevicePermissionType permissionType) {
        log.debug("[设备权限管理] 批量检查设备权限, userId={}, deviceCount={}", userId, deviceIds.size());

        Map<String, PermissionCheckResult> results = new HashMap<>();

        for (String deviceId : deviceIds) {
            PermissionCheckResult result = checkDevicePermission(userId, deviceId, permissionType);
            results.put(deviceId, result);
        }

        return results;
    }

    /**
     * 更新设备权限缓存
     *
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param permissions 权限集合
     */
    public void updateDevicePermissionCache(String deviceId, String userId, Set<DevicePermissionType> permissions) {
        String cacheKey = getCacheKey(deviceId, userId);
        Set<String> permissionCodes = permissions.stream()
                .map(DevicePermissionType::getCode)
                .collect(Collectors.toSet());

        deviceUserPermissionCache.put(cacheKey, permissionCodes);

        log.debug("[设备权限管理] 更新权限缓存, deviceId={}, userId={}, permissions={}",
                deviceId, userId, permissionCodes);
    }

    /**
     * 清理设备权限缓存
     *
     * @param deviceId 设备ID
     */
    public void clearDevicePermissionCache(String deviceId) {
        deviceUserPermissionCache.entrySet().removeIf(entry -> entry.getKey().startsWith(deviceId + ":"));
        log.debug("[设备权限管理] 清理设备权限缓存, deviceId={}", deviceId);
    }

    /**
     * 获取用户在设备关联区域内的权限
     */
    private List<AreaUserEntity> getUserPermissionsForDevice(Long userId, String deviceId) {
        // 1. 获取设备关联的所有区域
        List<AreaDeviceEntity> deviceAreas = areaDeviceDao.selectByDeviceId(deviceId);
        if (deviceAreas.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> areaIds = deviceAreas.stream()
                .map(AreaDeviceEntity::getAreaId)
                .distinct()
                .collect(Collectors.toList());

        // 2. 获取用户在这些区域内的权限
        return areaUserDao.selectUserAreaPermissions(userId, areaIds);
    }

    /**
     * 检查是否拥有所需权限级别
     */
    private boolean hasRequiredPermissionLevel(AreaUserEntity userPermission, DevicePermissionType permissionType) {
        int userLevel = userPermission.getPermissionLevel();
        int requiredLevel = getRequiredPermissionLevel(permissionType);
        return userLevel >= requiredLevel;
    }

    /**
     * 获取权限类型所需的最低权限级别
     */
    private int getRequiredPermissionLevel(DevicePermissionType permissionType) {
        switch (permissionType) {
            case SYSTEM_CONFIG:
                return 4; // 超级管理员
            case VIDEO_MONITOR:
                return 3; // 管理权限
            case ACCESS_CONTROL:
            case VISITOR_MANAGEMENT:
                return 2; // 读写权限
            case ATTENDANCE_CHECK:
            case CONSUME_PAYMENT:
                return 1; // 只读权限
            default:
                return 1;
        }
    }

    /**
     * 根据权限级别获取可用权限
     */
    private Set<DevicePermissionType> getPermissionsByLevel(int permissionLevel) {
        Set<DevicePermissionType> permissions = new HashSet<>();

        // 所有权限级别都有基础权限
        permissions.add(DevicePermissionType.ATTENDANCE_CHECK);
        permissions.add(DevicePermissionType.CONSUME_PAYMENT);

        if (permissionLevel >= 2) {
            // 读写权限及以上
            permissions.add(DevicePermissionType.ACCESS_CONTROL);
            permissions.add(DevicePermissionType.VISITOR_MANAGEMENT);
        }

        if (permissionLevel >= 3) {
            // 管理权限及以上
            permissions.add(DevicePermissionType.VIDEO_MONITOR);
        }

        if (permissionLevel >= 4) {
            // 超级管理员
            permissions.add(DevicePermissionType.SYSTEM_CONFIG);
        }

        return permissions;
    }

    /**
     * 过滤业务权限配置
     */
    private Set<DevicePermissionType> filterPermissionsByBusinessConfig(
            List<AreaUserEntity> userPermissions, Set<DevicePermissionType> permissions) {

        Set<DevicePermissionType> filteredPermissions = new HashSet<>(permissions);

        for (DevicePermissionType permission : permissions) {
            boolean hasPermission = userPermissions.stream()
                    .anyMatch(user -> hasBusinessPermission(user, permission));
            if (hasPermission) {
                filteredPermissions.add(permission);
            }
        }

        return filteredPermissions;
    }

    /**
     * 检查业务权限配置
     */
    private boolean hasBusinessPermission(AreaUserEntity userPermission, DevicePermissionType permissionType) {
        try {
            String accessPermissions = userPermission.getAccessPermissions();
            if (accessPermissions == null || accessPermissions.isEmpty()) {
                return true; // 没有限制，默认允许
            }

            // 解析JSON权限配置
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> permissionConfig = objectMapper.readValue(
                accessPermissions, new TypeReference<Map<String, Object>>() {});

            // 检查特定权限类型
            switch (permissionType) {
                case ACCESS_CONTROL:
                    return checkAccessPermission(permissionConfig);
                case ATTENDANCE_CHECK:
                    return checkAttendancePermission(permissionConfig);
                case CONSUME_PAYMENT:
                    return checkConsumePermission(permissionConfig);
                case VIDEO_MONITOR:
                    return checkVideoPermission(permissionConfig);
                case VISITOR_MANAGEMENT:
                    return checkVisitorPermission(permissionConfig);
                default:
                    return true; // 未知权限类型，默认允许
            }

        } catch (Exception e) {
            log.error("[权限检查] 解析业务权限配置异常: userId={}, error={}",
                userPermission.getUserId(), e.getMessage(), e);
            return true; // 异常时默认允许
        }
    }

    /**
     * 检查时间权限
     */
    private PermissionCheckResult checkTimePermission(AreaUserEntity userPermission) {
        // 检查永久有效权限
        if (userPermission.getPermanent() != null && userPermission.getPermanent()) {
            return new PermissionCheckResult(true, "永久有效", "权限永久有效");
        }

        // 检查生效时间
        LocalDateTime now = LocalDateTime.now();
        if (userPermission.getEffectiveTime() != null && now.isBefore(userPermission.getEffectiveTime())) {
            return new PermissionCheckResult(false, "权限未生效",
                    "生效时间：" + userPermission.getEffectiveTime());
        }

        // 检查失效时间
        if (userPermission.getExpireTime() != null && now.isAfter(userPermission.getExpireTime())) {
            return new PermissionCheckResult(false, "权限已过期",
                    "失效时间：" + userPermission.getExpireTime());
        }

        // 检查工作日限制
        if (userPermission.getWorkdayOnly() != null && userPermission.getWorkdayOnly()) {
            DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                return new PermissionCheckResult(false, "仅工作日有效", "当前为周末，仅工作日有效");
            }

            // 检查节假日
            if (isHolidayToday()) {
                return new PermissionCheckResult(false, "仅工作日有效", "当前为节假日，仅工作日有效");
            }
        }

        // 检查时间范围
        if (userPermission.getAllowStartTime() != null && userPermission.getAllowEndTime() != null) {
            LocalTime nowTime = LocalTime.now();
            LocalTime startTime = LocalTime.parse(userPermission.getAllowStartTime());
            LocalTime endTime = LocalTime.parse(userPermission.getAllowEndTime());

            if (nowTime.isBefore(startTime) || nowTime.isAfter(endTime)) {
                return new PermissionCheckResult(false, "不在允许时间范围内",
                        String.format("允许时间：%s-%s，当前时间：%s",
                                startTime, endTime, nowTime));
            }
        }

        return new PermissionCheckResult(true, "时间权限验证通过", "所有时间检查通过");
    }

    /**
     * 检查业务权限配置
     */
    private PermissionCheckResult checkBusinessPermission(AreaUserEntity userPermission, DevicePermissionType permissionType) {
        try {
            String accessPermissions = userPermission.getAccessPermissions();
            if (accessPermissions == null || accessPermissions.isEmpty()) {
                return new PermissionCheckResult(true, "业务权限验证通过", "无特殊权限限制");
            }

            // 解析JSON权限配置
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> permissionConfig = objectMapper.readValue(
                accessPermissions, new TypeReference<Map<String, Object>>() {});

            // 检查特定权限类型
            boolean hasPermission;
            String permissionDesc;
            String detail;

            switch (permissionType) {
                case ACCESS_CONTROL:
                    hasPermission = checkAccessPermission(permissionConfig);
                    permissionDesc = hasPermission ? "门禁权限验证通过" : "门禁权限不足";
                    detail = hasPermission ? "具有门禁访问权限" : "门禁访问权限被限制";
                    break;
                case ATTENDANCE_CHECK:
                    hasPermission = checkAttendancePermission(permissionConfig);
                    permissionDesc = hasPermission ? "考勤权限验证通过" : "考勤权限不足";
                    detail = hasPermission ? "具有考勤打卡权限" : "考勤打卡权限被限制";
                    break;
                case CONSUME_PAYMENT:
                    hasPermission = checkConsumePermission(permissionConfig);
                    permissionDesc = hasPermission ? "消费权限验证通过" : "消费权限不足";
                    detail = hasPermission ? "具有消费支付权限" : "消费支付权限被限制";
                    break;
                case VIDEO_MONITOR:
                    hasPermission = checkVideoPermission(permissionConfig);
                    permissionDesc = hasPermission ? "视频权限验证通过" : "视频权限不足";
                    detail = hasPermission ? "具有视频监控权限" : "视频监控权限被限制";
                    break;
                case VISITOR_MANAGEMENT:
                    hasPermission = checkVisitorPermission(permissionConfig);
                    permissionDesc = hasPermission ? "访客权限验证通过" : "访客权限不足";
                    detail = hasPermission ? "具有访客管理权限" : "访客管理权限被限制";
                    break;
                default:
                    hasPermission = true;
                    permissionDesc = "业务权限验证通过";
                    detail = "未知权限类型，默认允许";
                    break;
            }

            return new PermissionCheckResult(hasPermission, permissionDesc, detail);

        } catch (Exception e) {
            log.error("[权限检查] 解析业务权限配置异常: userId={}, error={}",
                userPermission.getUserId(), e.getMessage(), e);
            return new PermissionCheckResult(true, "业务权限验证通过", "权限配置异常，默认允许");
        }
    }

    /**
     * 获取缓存键
     */
    private String getCacheKey(String deviceId, String userId) {
        return deviceId + ":" + userId;
    }

    /**
     * 检查门禁权限
     */
    private boolean checkAccessPermission(Map<String, Object> permissionConfig) {
        Object accessEnabled = permissionConfig.get("accessEnabled");
        if (accessEnabled instanceof Boolean) {
            return (Boolean) accessEnabled;
        }
        return true; // 默认允许
    }

    /**
     * 检查考勤权限
     */
    private boolean checkAttendancePermission(Map<String, Object> permissionConfig) {
        Object attendanceEnabled = permissionConfig.get("attendanceEnabled");
        if (attendanceEnabled instanceof Boolean) {
            return (Boolean) attendanceEnabled;
        }
        return true; // 默认允许
    }

    /**
     * 检查消费权限
     */
    private boolean checkConsumePermission(Map<String, Object> permissionConfig) {
        Object consumeEnabled = permissionConfig.get("consumeEnabled");
        if (consumeEnabled instanceof Boolean) {
            return (Boolean) consumeEnabled;
        }

        // 检查消费限额
        Object consumeLimit = permissionConfig.get("consumeLimit");
        if (consumeLimit instanceof Number) {
            // 这里可以检查当前消费金额是否超过限额
            // 实际实现需要查询用户的当前消费金额
            // 暂时不进行限额检查，仅返回允许状态
        }

        return true; // 默认允许
    }

    /**
     * 检查视频权限
     */
    private boolean checkVideoPermission(Map<String, Object> permissionConfig) {
        Object videoEnabled = permissionConfig.get("videoEnabled");
        if (videoEnabled instanceof Boolean) {
            return (Boolean) videoEnabled;
        }

        // 检查视频权限级别
        Object videoLevel = permissionConfig.get("videoLevel");
        if (videoLevel instanceof Number) {
            int level = ((Number) videoLevel).intValue();
            // 根据级别判断权限
            return level >= 1; // 级别1及以上有权限
        }

        return true; // 默认允许
    }

    /**
     * 检查访客权限
     */
    private boolean checkVisitorPermission(Map<String, Object> permissionConfig) {
        Object visitorEnabled = permissionConfig.get("visitorEnabled");
        if (visitorEnabled instanceof Boolean) {
            return (Boolean) visitorEnabled;
        }
        return true; // 默认允许
    }

    /**
     * 检查今天是否是节假日
     */
    private boolean isHolidayToday() {
        try {
            LocalDate today = LocalDate.now();

            // 这里可以从系统配置或节假日服务获取节假日列表
            // 为了简化，这里使用基本的节假日判断
            Set<String> holidays = getHolidaysFromConfig();

            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return holidays.contains(todayStr);

        } catch (Exception e) {
            log.error("[节假日检查] 检查节假日异常: error={}", e.getMessage(), e);
            return false; // 异常时默认不是节假日
        }
    }

    /**
     * 从系统配置获取节假日列表
     */
    private Set<String> getHolidaysFromConfig() {
        Set<String> holidays = new HashSet<>();

        // 2025年法定节假日（实际应该从配置获取）
        holidays.addAll(Arrays.asList(
            "2025-01-01", // 元旦
            "2025-01-28", "2025-01-29", "2025-01-30", "2025-01-31", // 春节
            "2025-02-01", "2025-02-02", "2025-02-03",
            "2025-04-05", "2025-04-06", "2025-04-07", // 清明节
            "2025-05-01", "2025-05-02", "2025-05-03", // 劳动节
            "2025-06-08", "2025-06-09", "2025-06-10", // 端午节
            "2025-09-15", "2025-09-16", "2025-09-17", // 中秋节
            "2025-10-01", "2025-10-02", "2025-10-03", "2025-10-04",
            "2025-10-05", "2025-10-06", "2025-10-07" // 国庆节
        ));

        return holidays;
    }
}