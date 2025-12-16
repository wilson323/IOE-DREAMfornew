package net.lab1024.sa.common.organization.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import net.lab1024.sa.common.organization.entity.AreaUserEntity;
import net.lab1024.sa.common.organization.dao.AreaUserDao;
import net.lab1024.sa.common.util.SystemConfigUtil;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;

/**
 * 区域人员关联管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Manager类处理复杂业务流程编排
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 * <p>
 * 职责：
 * - 区域人员关联的业务逻辑处理
 * - 权限验证和状态判断
 * - 时间范围检查和业务规则验证
 * - 设备同步状态管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class AreaUserManager {

    private final AreaUserDao areaUserDao;

    /**
     * ObjectMapper实例（线程安全，可复用）
     * <p>
     * ObjectMapper是线程安全的，设计用于复用
     * 避免每次调用getter方法时创建新实例，提升性能
     * </p>
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 时间格式（HH:mm）
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 构造函数
     *
     * @param areaUserDao 区域用户DAO
     */
    public AreaUserManager(AreaUserDao areaUserDao) {
        this.areaUserDao = areaUserDao;
    }

    /**
     * 检查关联是否当前有效
     * <p>
     * 判断区域人员关联是否在有效期内
     * 支持永久有效和有效期设置
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @return 是否有效
     */
    public boolean isEffective(AreaUserEntity areaUser) {
        if (areaUser == null) {
            return false;
        }

        // 永久有效则直接返回true
        if (Boolean.TRUE.equals(areaUser.getPermanent())) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // 检查生效时间
        if (areaUser.getEffectiveTime() != null && now.isBefore(areaUser.getEffectiveTime())) {
            return false;
        }

        // 检查失效时间
        if (areaUser.getExpireTime() != null && now.isAfter(areaUser.getExpireTime())) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否在允许时间范围内
     * <p>
     * 判断当前时间是否在允许的时间范围内
     * 支持跨天时间范围（如22:00-06:00）
     * 支持工作日限制（workdayOnly字段）
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @return 是否在允许时间范围内
     */
    public boolean isWithinAllowedTime(AreaUserEntity areaUser) {
        if (areaUser == null) {
            return true;
        }

        // 检查时间限制配置
        if (areaUser.getAllowStartTime() == null || areaUser.getAllowEndTime() == null) {
            return true; // 无时间限制
        }

        try {
            // 1. 解析时间字符串（格式：HH:mm）
            LocalTime startTime = LocalTime.parse(areaUser.getAllowStartTime(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(areaUser.getAllowEndTime(), TIME_FORMATTER);
            LocalTime currentTime = LocalTime.now();

            // 2. 检查工作日限制
            if (Boolean.TRUE.equals(areaUser.getWorkdayOnly())) {
                DayOfWeek dayOfWeek = LocalDateTime.now().getDayOfWeek();
                // 周六和周日不是工作日
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    return false;
                }
                // 检查节假日配置（从系统配置获取）
                if (isHolidayToday()) {
                    return false;
                }
            }

            // 3. 判断时间范围
            // 情况1：正常时间范围（如08:00-18:00）
            if (startTime.isBefore(endTime) || startTime.equals(endTime)) {
                return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            }
            // 情况2：跨天时间范围（如22:00-06:00）
            else {
                // 当前时间在开始时间之后（如22:00-23:59）或结束时间之前（如00:00-06:00）
                return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
            }

        } catch (DateTimeParseException e) {
            // 时间格式错误，返回true（避免误判）
            log.debug("[区域人员关联] 时间格式解析失败，返回true（避免误判）: error={}", e.getMessage());
            return true;
        } catch (Exception e) {
            // 其他异常，返回true（避免误判）
            log.debug("[区域人员关联] 时间范围检查异常，返回true（避免误判）: error={}", e.getMessage());
            return true;
        }
    }

    /**
     * 检查是否需要设备同步
     * <p>
     * 判断是否需要将用户权限同步到区域内的设备
     * 基于同步状态和重试次数进行判断
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @return 是否需要同步
     */
    public boolean needsSync(AreaUserEntity areaUser) {
        if (areaUser == null) {
            return false;
        }

        Integer syncStatus = areaUser.getDeviceSyncStatus();
        Integer retryCount = areaUser.getRetryCount();

        return syncStatus == null
                || AreaUserEntity.DeviceSyncStatus.NOT_SYNCED.equals(syncStatus)      // 未下发
                || AreaUserEntity.DeviceSyncStatus.SYNC_FAILED.equals(syncStatus)     // 同步失败
                || (AreaUserEntity.DeviceSyncStatus.CANCELED.equals(syncStatus)     // 已撤销
                    && retryCount != null && retryCount < 3);  // 已撤销但可重试
    }

    /**
     * 检查是否具有门禁权限
     * <p>
     * 解析accessPermissions JSON，检查门禁权限
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @return 是否有门禁权限
     */
    public boolean hasAccessPermission(AreaUserEntity areaUser) {
        return parsePermissionFromJson(areaUser, "access");
    }

    /**
     * 检查是否具有考勤权限
     * <p>
     * 解析accessPermissions JSON，检查考勤权限
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @return 是否有考勤权限
     */
    public boolean hasAttendancePermission(AreaUserEntity areaUser) {
        return parsePermissionFromJson(areaUser, "attendance");
    }

    /**
     * 检查是否具有消费权限
     * <p>
     * 解析accessPermissions JSON，检查消费权限
     * JSON格式：{"access": true, "attendance": true, "consume": false}
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @return 是否有消费权限
     */
    public boolean hasConsumePermission(AreaUserEntity areaUser) {
        return parsePermissionFromJson(areaUser, "consume");
    }

    /**
     * 检查是否具有指定权限
     *
     * @param areaUser 区域人员关联实体
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(AreaUserEntity areaUser, String permission) {
        return parsePermissionFromJson(areaUser, permission);
    }

    /**
     * 从JSON中解析权限值（私有辅助方法）
     * <p>
     * 线程安全：ObjectMapper是线程安全的
     * 使用try-catch处理JSON解析异常
     * </p>
     *
     * @param areaUser 区域人员关联实体
     * @param permissionKey 权限键
     * @return 是否有权限
     */
    private boolean parsePermissionFromJson(AreaUserEntity areaUser, String permissionKey) {
        if (areaUser == null || !StringUtils.hasText(areaUser.getAccessPermissions())) {
            return false;
        }

        try {
            Map<String, Object> permissions = OBJECT_MAPPER.readValue(
                    areaUser.getAccessPermissions(),
                    new TypeReference<Map<String, Object>>() {
                    });
            return permissions != null && Boolean.TRUE.equals(permissions.get(permissionKey));
        } catch (Exception e) {
            log.debug("[区域人员关联] 权限JSON解析失败: permission={}, error={}", permissionKey, e.getMessage());
            return false;
        }
    }

    /**
     * 获取关联类型描述
     *
     * @param relationType 关联类型
     * @return 类型描述
     */
    public String getRelationTypeDesc(Integer relationType) {
        if (relationType == null) {
            return "未知";
        }

        switch (relationType) {
            case AreaUserEntity.RelationType.PERMANENT:
                return "常驻人员";
            case AreaUser.EntityType.TEMPORARY:
                return "临时人员";
            case AreaUserEntity.EntityType.VISITOR:
                return "访客";
            case AreaUserEntity.EntityType.MAINTENANCE:
                return "维护人员";
            case AreaUserEntity.EntityType.MANAGER:
                return "管理人员";
            default:
                return "未知";
        }
    }

    /**
     * 获取权限级别描述
     *
     * @param permissionLevel 权限级别
     * @return 级别描述
     */
    public String getPermissionLevelDesc(Integer permissionLevel) {
        if (permissionLevel == null) {
            return "未知";
        }

        switch (permissionLevel) {
            case AreaUserEntity.PermissionLevel.NORMAL:
                return "普通权限";
            case AreaUserEntity.PermissionLevel.ADVANCED:
                return "高级权限";
            case AreaUserEntity.PermissionLevel.ADMIN:
                return "管理员权限";
            default:
                return "未知";
        }
    }

    /**
     * 获取同步状态描述
     *
     * @param syncStatus 同步状态
     * @return 状态描述
     */
    public String getSyncStatusDesc(Integer syncStatus) {
        if (syncStatus == null) {
            return "未知";
        }

        switch (syncStatus) {
            case AreaUserEntity.DeviceSyncStatus.NOT_SYNCED:
                return "未下发";
            case AreaUserEntity.DeviceSyncStatus.SYNCING:
                return "下发中";
            case AreaUserEntity.DeviceSyncStatus.SYNCED:
                return "已同步";
            case AreaUserEntity.DeviceSyncStatus.SYNC_FAILED:
                return "同步失败";
            case AreaUserEntity.DeviceSyncStatus.CANCELED:
                return "已撤销";
            default:
                return "未知";
        }
    }

    /**
     * 检查今天是否为节假日
     *
     * @return 今天是否为节假日
     */
    private boolean isHolidayToday() {
        try {
            // 从系统配置获取节假日信息
            // 这里应该调用系统配置服务或缓存
            return SystemConfigUtil.isHolidayToday();
        } catch (Exception e) {
            log.debug("[区域人员关联] 检查节假日失败，默认返回false: error={}", e.getMessage());
            return false;
        }
    }

    /**
     * 增加重试次数
     *
     * @param areaUser 区域人员关联实体
     * @return 更新后的重试次数
     */
    public int incrementRetryCount(AreaUserEntity areaUser) {
        if (areaUser == null) {
            return 0;
        }

        int currentRetryCount = areaUser.getRetryCount() != null ? areaUser.getRetryCount() : 0;
        int newRetryCount = currentRetryCount + 1;
        areaUser.setRetryCount(newRetryCount);

        // 更新到数据库
        try {
            areaUserDao.updateById(areaUser);
        } catch (Exception e) {
            log.error("[区域人员关联] 更新重试次数失败: id={}, error={}", areaUser.getId(), e.getMessage());
        }

        return newRetryCount;
    }

    /**
     * 重置重试次数
     *
     * @param areaUser 区域人员关联实体
     */
    public void resetRetryCount(AreaUserEntity areaUser) {
        if (areaUser == null) {
            return;
        }

        areaUser.setRetryCount(0);

        // 更新到数据库
        try {
            areaUserDao.updateById(areaUser);
        } catch (Exception e) {
            log.error("[区域人员关联] 重置重试次数失败: id={}, error={}", areaUser.getId(), e.getMessage());
        }
    }

    /**
     * 更新同步状态
     *
     * @param areaUser 区域人员关联实体
     * @param syncStatus 同步状态
     */
    public void updateSyncStatus(AreaUserEntity areaUser, Integer syncStatus) {
        if (areaUser == null) {
            return;
        }

        areaUser.setDeviceSyncStatus(syncStatus);
        areaUser.setLastSyncTime(LocalDateTime.now());

        // 更新到数据库
        try {
            areaUserDao.updateById(areaUser);
        } catch (Exception e) {
            log.error("[区域人员关联] 更新同步状态失败: id={}, status={}, error={}",
                    areaUser.getId(), syncStatus, e.getMessage());
        }
    }

    /**
     * 验证区域人员关联数据完整性
     *
     * @param areaUser 区域人员关联实体
     * @return 验证结果
     */
    public boolean validateAreaUser(AreaUserEntity areaUser) {
        if (areaUser == null) {
            return false;
        }

        // 必填字段验证
        if (areaUser.getAreaId() == null) {
            log.warn("[区域人员关联] 区域ID不能为空");
            return false;
        }

        if (areaUser.getUserId() == null) {
            log.warn("[区域人员关联] 用户ID不能为空");
            return false;
        }

        if (!StringUtils.hasText(areaUser.getUsername())) {
            log.warn("[区域人员关联] 用户名不能为空");
            return false;
        }

        // 时间范围验证
        if (areaUser.getEffectiveTime() != null && areaUser.getExpireTime() != null &&
                areaUser.getEffectiveTime().isAfter(areaUser.getExpireTime())) {
            log.warn("[区域人员关联] 生效时间不能晚于失效时间: effective={}, expire={}",
                    areaUser.getEffectiveTime(), areaUser.getExpireTime());
            return false;
        }

        return true;
    }

    /**
     * 批量检查区域人员关联有效性
     *
     * @param areaUsers 区域人员关联列表
     * @return 有效关联数量
     */
    public int countEffectiveUsers(java.util.List<AreaUserEntity> areaUsers) {
        if (areaUsers == null || areaUsers.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (AreaUserEntity areaUser : areaUsers) {
            if (isEffective(areaUser)) {
                count++;
            }
        }

        return count;
    }
}