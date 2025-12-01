package net.lab1024.sa.admin.module.consume.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.admin.module.smart.access.service.AccessAreaService;
import net.lab1024.sa.base.common.domain.RequestUser;
import net.lab1024.sa.base.common.domain.ResponseDTO;

/**
 * 消费权限服务
 * 处理消费相关的权限验证和业务规则检查
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Service
public class ConsumePermissionService {

    @Resource
    private AccessAreaService accessAreaService;

    /**
     * 检查用户是否有消费权限
     */
    public ResponseDTO<Boolean> checkConsumePermission(Long userId, Long deviceId, RequestUser currentUser) {
        try {
            // 1. 检查用户权限
            if (!checkUserPermission(userId, currentUser)) {
                return ResponseDTO.error("无权限为该用户执行消费操作");
            }

            // 2. 检查设备权限
            if (!checkDevicePermission(deviceId, currentUser)) {
                return ResponseDTO.error("无权限在该设备上执行消费操作");
            }

            // 3. 检查用户状态
            if (!checkUserStatus(userId)) {
                return ResponseDTO.error("用户状态异常，无法进行消费");
            }

            // 4. 检查设备状态
            if (!checkDeviceStatus(deviceId)) {
                return ResponseDTO.error("设备状态异常，无法进行消费");
            }

            return ResponseDTO.ok(true);

        } catch (Exception e) {
            log.error("检查消费权限失败: userId={}, deviceId={}", userId, deviceId, e);
            return ResponseDTO.error("权限检查失败");
        }
    }

    /**
     * 检查区域权限
     */
    public boolean checkAreaPermission(Long userId, Long areaId) {
        try {
            log.debug("开始检查区域权限: userId={}, areaId={}", userId, areaId);

            // 1. 获取区域信息
            AccessAreaEntity area = accessAreaService.getAreaById(areaId);
            if (area == null) {
                log.warn("区域不存在: areaId={}", areaId);
                return false;
            }

            // 2. 检查区域状态
            if (area.getStatus() == null || area.getStatus() != 1) {
                log.warn("区域状态异常: areaId={}, status={}", areaId, area.getStatus());
                return false;
            }

            // 3. 检查区域是否启用消费功能
            if (area.getAccessEnabled() == null || area.getAccessEnabled() != 1) {
                log.warn("区域未启用消费功能: areaId={}, accessEnabled={}", areaId, area.getAccessEnabled());
                return false;
            }

            // 4. 检查时间权限
            if (!checkAreaTimePermission(area)) {
                log.warn("用户在区域有效时间外: areaId={}", areaId);
                return false;
            }

            // 5. 检查权限级别（这里简化处理，实际应该检查用户的权限级别）
            if (!checkUserAreaPermissionLevel(userId, area)) {
                log.warn("用户权限级别不足: userId={}, areaId={}, areaLevel={}",
                        userId, areaId, area.getAccessLevel());
                return false;
            }

            log.debug("区域权限检查通过: userId={}, areaId={}", userId, areaId);
            return true;

        } catch (Exception e) {
            log.error("检查区域权限失败: userId={}, areaId={}", userId, areaId, e);
            return false;
        }
    }

    /**
     * 检查区域时间权限
     */
    private boolean checkAreaTimePermission(AccessAreaEntity area) {
        try {
            // 获取当前时间
            LocalTime currentTime = LocalTime.now();
            DayOfWeek currentDayOfWeek = java.time.LocalDate.now().getDayOfWeek();
            int dayValue = currentDayOfWeek.getValue(); // 1-7 (周一到周日)

            // 检查星期权限
            if (area.getValidWeekdays() != null && !area.getValidWeekdays().trim().isEmpty()) {
                String[] weekdays = area.getValidWeekdays().split(",");
                boolean isValidDay = false;
                for (String day : weekdays) {
                    if (String.valueOf(dayValue).equals(day.trim())) {
                        isValidDay = true;
                        break;
                    }
                }
                if (!isValidDay) {
                    log.debug("当前不在有效星期: currentDay={}, validWeekdays={}",
                            dayValue, area.getValidWeekdays());
                    return false;
                }
            }

            // 检查时间权限
            if (area.getValidTimeStart() != null && area.getValidTimeEnd() != null) {
                LocalTime startTime = LocalTime.parse(area.getValidTimeStart());
                LocalTime endTime = LocalTime.parse(area.getValidTimeEnd());

                // 处理跨天的情况
                if (endTime.isBefore(startTime)) {
                    // 跨天情况，如 22:00 - 06:00
                    if (currentTime.isBefore(startTime) && currentTime.isAfter(endTime)) {
                        log.debug("当前不在有效时间范围: currentTime={}, startTime={}, endTime={}",
                                currentTime, startTime, endTime);
                        return false;
                    }
                } else {
                    // 正常情况，如 09:00 - 18:00
                    if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                        log.debug("当前不在有效时间范围: currentTime={}, startTime={}, endTime={}",
                                currentTime, startTime, endTime);
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("检查区域时间权限失败: areaId={}", area.getAreaId(), e);
            return false;
        }
    }

    /**
     * 检查用户区域权限级别
     */
    private boolean checkUserAreaPermissionLevel(Long userId, AccessAreaEntity area) {
        try {
            // 这里简化处理，实际应该查询用户的权限级别
            // 可以集成用户权限服务或区域授权服务

            // 如果区域没有设置权限级别要求，默认允许
            if (area.getAccessLevel() == null || area.getAccessLevel() <= 0) {
                return true;
            }

            // 检查用户是否有特殊权限（如管理员等）
            Set<String> userRoles = getUserRoles(userId);
            if (userRoles.contains("ADMIN") || userRoles.contains("SUPER_ADMIN")) {
                return true;
            }

            // 这里应该查询用户在该区域的具体权限级别
            // 简化实现：假设普通用户权限级别为1
            Integer userPermissionLevel = getUserAreaPermissionLevel(userId, area.getAreaId());

            return userPermissionLevel != null && userPermissionLevel >= area.getAccessLevel();

        } catch (Exception e) {
            log.error("检查用户区域权限级别失败: userId={}, areaId={}",
                    userId, area.getAreaId(), e);
            return false;
        }
    }

    /**
     * 获取用户角色
     */
    private Set<String> getUserRoles(Long userId) {
        // TODO: 实际应该查询用户角色表
        // 这里简化实现，返回默认角色
        Set<String> roles = new java.util.HashSet<>();
        roles.add("USER");
        return roles;
    }

    /**
     * 获取用户在区域中的权限级别
     */
    private Integer getUserAreaPermissionLevel(Long userId, Long areaId) {
        // TODO: 实际应该查询用户区域权限表
        // 这里简化实现，返回默认权限级别
        return 1;
    }

    /**
     * 检查消费限额
     */
    public ResponseDTO<Boolean> checkConsumeLimit(Long userId, BigDecimal amount) {
        try {
            // TODO: 实现消费限额检查
            // 1. 检查单次消费限额
            // 2. 检查日消费限额
            // 3. 检查月消费限额
            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("检查消费限额失败: userId={}, amount={}", userId, amount, e);
            return ResponseDTO.error("限额检查失败");
        }
    }

    /**
     * 检查支付密码
     */
    public ResponseDTO<Boolean> checkPaymentPassword(Long userId, String password) {
        try {
            // TODO: 实现支付密码验证
            // 1. 检查用户是否设置了支付密码
            // 2. 验证支付密码是否正确
            // 3. 检查支付密码是否过期或被锁定
            return ResponseDTO.ok(true);
        } catch (Exception e) {
            log.error("验证支付密码失败: userId={}", userId, e);
            return ResponseDTO.error("支付密码验证失败");
        }
    }

    /**
     * 检查用户权限
     */
    private boolean checkUserPermission(Long userId, RequestUser currentUser) {
        // 如果是用户本人，允许操作
        if (currentUser.getUserId().equals(userId)) {
            return true;
        }

        // 如果是管理员或特定角色，允许操作
        Set<String> roles = currentUser.getRoles();
        return roles.contains("ADMIN") || roles.contains("CONSUME_MANAGER");
    }

    /**
     * 检查设备权限
     */
    private boolean checkDevicePermission(Long deviceId, RequestUser currentUser) {
        // TODO: 实现设备权限检查
        // 1. 检查设备是否存在且状态正常
        // 2. 检查用户是否有该设备的使用权限
        // 3. 检查设备是否在允许的时间和地点使用
        return true;
    }

    /**
     * 检查用户状态
     */
    private boolean checkUserStatus(Long userId) {
        // TODO: 实现用户状态检查
        // 1. 检查用户是否正常状态
        // 2. 检查用户账户是否被冻结或禁用
        // 3. 检查用户是否在有效期内
        return true;
    }

    /**
     * 检查设备状态
     */
    private boolean checkDeviceStatus(Long deviceId) {
        // TODO: 实现设备状态检查
        // 1. 检查设备是否在线
        // 2. 检查设备是否正常工作
        // 3. 检查设备是否需要维护或校准
        return true;
    }

    /**
     * 获取用户的消费权限列表
     */
    public List<Map<String, Object>> getUserConsumePermissions(Long userId) {
        // TODO: 返回用户的消费权限信息
        return List.of();
    }

    /**
     * 检查特殊权限（如折扣权限、特殊时段权限等）
     */
    public boolean checkSpecialPermission(Long userId, String permissionType) {
        // TODO: 实现特殊权限检查
        return false;
    }
}
