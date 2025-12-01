package net.lab1024.sa.admin.module.consume.service.validator;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.smart.access.service.AccessAreaService;
import net.lab1024.sa.base.authorization.util.AuthorizationContextHolder;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.exception.UserErrorCode;
import net.lab1024.sa.base.common.util.SmartRedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckPermission;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * 消费权限验证器
 * <p>
 * 基于RAC统一权限中间件和Sa-Token的消费权限验证
 * 支持多维度权限控制：人员状态、设备权限、区域权限、时间权限
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Slf4j
@Component
public class ConsumePermissionValidator {

    @Resource
    private AccountService accountService;

    @Resource
    private AccessAreaService accessAreaService;

    /**
     * 验证消费权限
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param regionId 区域ID
     * @return 验证结果
     */
    public ConsumePermissionResult validateConsumePermission(Long personId, Long deviceId, String regionId) {
        try {
            log.debug("开始验证消费权限: personId={}, deviceId={}, regionId={}", personId, deviceId, regionId);

            // 1. 基础权限检查（Sa-Token登录验证）
            if (!validateBasicPermission()) {
                return ConsumePermissionResult.failure("BASIC_PERMISSION_DENIED", "用户未登录或权限验证失败");
            }

            // 2. 账户状态验证
            ConsumePermissionResult accountResult = validateAccountStatus(personId);
            if (!accountResult.isSuccess()) {
                return accountResult;
            }

            // 3. 设备权限验证
            ConsumePermissionResult deviceResult = validateDevicePermission(personId, deviceId);
            if (!deviceResult.isSuccess()) {
                return deviceResult;
            }

            // 4. 区域权限验证
            ConsumePermissionResult regionResult = validateRegionPermission(personId, regionId);
            if (!regionResult.isSuccess()) {
                return regionResult;
            }

            // 5. 时间权限验证
            ConsumePermissionResult timeResult = validateTimePermission(personId);
            if (!timeResult.isSuccess()) {
                return timeResult;
            }

            log.debug("消费权限验证成功: personId={}", personId);
            return ConsumePermissionResult.success();

        } catch (Exception e) {
            log.error("消费权限验证异常: personId={}, deviceId={}, regionId={}", personId, deviceId, regionId, e);
            return ConsumePermissionResult.failure("VALIDATION_ERROR", "权限验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证层级区域权限（支持父子区域权限继承）
     *
     * @param personId 人员ID
     * @param regionId 区域ID
     * @return 验证结果
     */
    public ConsumePermissionResult validateHierarchicalRegionPermission(Long personId, String regionId) {
        try {
            log.debug("开始验证层级区域权限: personId={}, regionId={}", personId, regionId);

            if (regionId == null) {
                log.debug("区域ID为空，跳过层级区域权限验证: personId={}", personId);
                return ConsumePermissionResult.success();
            }

            // 1. 直接验证当前区域权限
            ConsumePermissionResult directResult = validateRegionPermission(personId, regionId);
            if (directResult.isSuccess()) {
                return directResult;
            }

            // 2. 验证父级区域权限（区域权限继承）
            ConsumePermissionResult parentResult = validateParentRegionPermission(personId, regionId);
            if (parentResult.isSuccess()) {
                return parentResult;
            }

            // 3. 验证子级区域权限（管理员权限）
            ConsumePermissionResult childResult = validateChildRegionPermission(personId, regionId);
            if (childResult.isSuccess()) {
                return childResult;
            }

            log.warn("层级区域权限验证失败: personId={}, regionId={}", personId, regionId);
            return ConsumePermissionResult.failure("HIERARCHICAL_REGION_PERMISSION_DENIED",
                    "您无权限访问该区域及其相关区域");

        } catch (Exception e) {
            log.error("层级区域权限验证异常: personId={}, regionId={}", personId, regionId, e);
            return ConsumePermissionResult.failure("HIERARCHICAL_REGION_VALIDATION_ERROR", "层级区域权限验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证父级区域权限
     */
    private ConsumePermissionResult validateParentRegionPermission(Long personId, String regionId) {
        try {
            // 获取区域的父级权限路径
            List<String> parentRegionIds = accessAreaService.getParentRegionPath(regionId);

            if (parentRegionIds != null && !parentRegionIds.isEmpty()) {
                for (String parentRegionId : parentRegionIds) {
                    boolean hasParentPermission = StpUtil.hasPermission("CONSUME_REGION_" + parentRegionId);
                    if (hasParentPermission) {
                        log.debug("父级区域权限验证通过: personId={}, regionId={}, parentRegionId={}", personId, regionId, parentRegionId);
                        return ConsumePermissionResult.success();
                    }
                }
            }

            return ConsumePermissionResult.failure("PARENT_REGION_PERMISSION_DENIED", "无父级区域权限");

        } catch (Exception e) {
            log.error("父级区域权限验证失败: personId={}, regionId={}", personId, regionId, e);
            return ConsumePermissionResult.failure("PARENT_REGION_VALIDATION_ERROR", "父级区域权限验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证子级区域权限（区域管理员）
     */
    private ConsumePermissionResult validateChildRegionPermission(Long personId, String regionId) {
        try {
            // 检查是否为区域管理员
            boolean hasManagePermission = StpUtil.hasPermission("ACCESS_AREA_MANAGE");
            if (hasManagePermission) {
                // 区域管理员可以管理所有子级区域
                List<String> childRegionIds = accessAreaService.getChildRegionIds(regionId);
                if (childRegionIds != null && !childRegionIds.isEmpty()) {
                    log.debug("区域管理员权限验证通过，可管理子级区域: personId={}, regionId={}, childCount={}",
                            personId, regionId, childRegionIds.size());
                    return ConsumePermissionResult.success();
                }
            }

            return ConsumePermissionResult.failure("CHILD_REGION_PERMISSION_DENIED", "无子级区域管理权限");

        } catch (Exception e) {
            log.error("子级区域权限验证失败: personId={}, regionId={}", personId, regionId, e);
            return ConsumePermissionResult.failure("CHILD_REGION_VALIDATION_ERROR", "子级区域权限验证失败: " + e.getMessage());
        }
    }

    /**
     * 批量验证区域权限
     *
     * @param personId 人员ID
     * @param regionIds 区域ID列表
     * @return 验证结果，包含每个区域的验证状态
     */
    public ConsumePermissionBatchResult validateBatchRegionPermissions(Long personId, List<String> regionIds) {
        ConsumePermissionBatchResult batchResult = new ConsumePermissionBatchResult();

        if (regionIds == null || regionIds.isEmpty()) {
            return batchResult;
        }

        for (String regionId : regionIds) {
            ConsumePermissionResult result = validateRegionPermission(personId, regionId);
            batchResult.addResult(regionId, result);
        }

        return batchResult;
    }

    /**
     * 验证多条件消费权限
     *
     * @param consumeRequest 消费请求对象
     * @return 验证结果
     */
    public ConsumePermissionResult validateMultiConditionConsumePermission(Object consumeRequest) {
        try {
            // 从消费请求中提取人员ID、设备ID、区域ID等信息
            Long personId = extractPersonId(consumeRequest);
            Long deviceId = extractDeviceId(consumeRequest);
            String regionId = extractRegionId(consumeRequest);

            return validateConsumePermission(personId, deviceId, regionId);

        } catch (Exception e) {
            log.error("多条件消费权限验证失败", e);
            return ConsumePermissionResult.failure("MULTI_CONDITION_VALIDATION_ERROR", "多条件权限验证失败: " + e.getMessage());
        }
    }

    /**
     * 从消费请求中提取人员ID
     */
    private Long extractPersonId(Object consumeRequest) {
        try {
            // 这里需要根据实际的ConsumeRequest对象结构来提取
            // 简化处理，假设可以通过反射或getter方法获取
            return null; // 实际实现应该从consumeRequest中提取personId
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从消费请求中提取设备ID
     */
    private Long extractDeviceId(Object consumeRequest) {
        try {
            // 这里需要根据实际的ConsumeRequest对象结构来提取
            return null; // 实际实现应该从consumeRequest中提取deviceId
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从消费请求中提取区域ID
     */
    private String extractRegionId(Object consumeRequest) {
        try {
            // 这里需要根据实际的ConsumeRequest对象结构来提取
            return null; // 实际实现应该从consumeRequest中提取regionId
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证基础权限（Sa-Token登录验证）
     */
    private boolean validateBasicPermission() {
        try {
            // 使用Sa-Token验证用户是否登录
            return StpUtil.isLogin();
        } catch (Exception e) {
            log.error("基础权限验证失败", e);
            return false;
        }
    }

    /**
     * 验证账户状态
     */
    private ConsumePermissionResult validateAccountStatus(Long personId) {
        try {
            AccountEntity account = accountService.getByPersonId(personId);
            if (account == null) {
                log.warn("账户不存在: personId={}", personId);
                return ConsumePermissionResult.failure("ACCOUNT_NOT_FOUND", "账户不存在");
            }

            String status = account.getStatus();
            if (!"ACTIVE".equals(status)) {
                log.warn("账户状态异常: personId={}, status={}", personId, status);
                String message = getAccountStatusMessage(status);
                return ConsumePermissionResult.failure("ACCOUNT_INACTIVE", message);
            }

            // 检查账户是否被冻结
            if ("FROZEN".equals(account.getAccountStatus())) {
                log.warn("账户被冻结: personId={}", personId);
                return ConsumePermissionResult.failure("ACCOUNT_FROZEN", "账户已被冻结，请联系管理员");
            }

            // 检查账户是否过期
            if (account.getExpireTime() != null && account.getExpireTime().isBefore(LocalDateTime.now())) {
                log.warn("账户已过期: personId={}, expireTime={}", personId, account.getExpireTime());
                return ConsumePermissionResult.failure("ACCOUNT_EXPIRED", "账户已过期");
            }

            return ConsumePermissionResult.success();

        } catch (Exception e) {
            log.error("账户状态验证失败: personId={}", personId, e);
            return ConsumePermissionResult.failure("ACCOUNT_VALIDATION_ERROR", "账户状态验证失败");
        }
    }

    /**
     * 验证设备权限
     */
    private ConsumePermissionResult validateDevicePermission(Long personId, Long deviceId) {
        try {
            if (deviceId == null) {
                log.debug("设备ID为空，跳过设备权限验证: personId={}", personId);
                return ConsumePermissionResult.success();
            }

            // 使用RAC权限中间件验证设备权限
            String devicePermissionCode = "CONSUME_DEVICE_" + deviceId;
            boolean hasDevicePermission = StpUtil.hasPermission(devicePermissionCode);
            if (!hasDevicePermission) {
                // 检查通用设备权限
                boolean hasGeneralPermission = StpUtil.hasPermission("CONSUME_DEVICE_ALL");
                if (!hasGeneralPermission) {
                    log.warn("设备权限验证失败: personId={}, deviceId={}", personId, deviceId);
                    return ConsumePermissionResult.failure("DEVICE_PERMISSION_DENIED", "无权限使用该设备进行消费");
                }
            }

            // 检查设备是否可用（从缓存或数据库查询）
            String deviceStatusKey = "device:status:" + deviceId;
            String deviceStatus = SmartRedisUtil.get(deviceStatusKey);
            if (StringUtils.hasText(deviceStatus) && !"ACTIVE".equals(deviceStatus)) {
                log.warn("设备状态异常: deviceId={}, status={}", deviceId, deviceStatus);
                return ConsumePermissionResult.failure("DEVICE_INACTIVE", "设备不可用，状态: " + deviceStatus);
            }

            return ConsumePermissionResult.success();

        } catch (Exception e) {
            log.error("设备权限验证失败: personId={}, deviceId={}", personId, deviceId, e);
            return ConsumePermissionResult.failure("DEVICE_VALIDATION_ERROR", "设备权限验证失败");
        }
    }

    /**
     * 验证区域权限
     */
    private ConsumePermissionResult validateRegionPermission(Long personId, String regionId) {
        try {
            if (regionId == null) {
                log.debug("区域ID为空，跳过区域权限验证: personId={}", personId);
                return ConsumePermissionResult.success();
            }

            // 1. 检查区域是否存在且有效
            ConsumePermissionResult regionExistsResult = validateRegionExists(regionId);
            if (!regionExistsResult.isSuccess()) {
                return regionExistsResult;
            }

            // 2. 使用Sa-Token验证区域权限
            boolean hasRegionPermission = StpUtil.hasPermission("CONSUME_REGION_" + regionId);
            if (hasRegionPermission) {
                log.debug("Sa-Token区域权限验证通过: personId={}, regionId={}", personId, regionId);
                return ConsumePermissionResult.success();
            }

            // 3. 检查通用区域权限
            boolean hasGeneralPermission = StpUtil.hasPermission("CONSUME_REGION_ALL");
            if (hasGeneralPermission) {
                log.debug("通用区域权限验证通过: personId={}, regionId={}", personId, regionId);
                return ConsumePermissionResult.success();
            }

            // 4. 使用RAC权限中间件检查数据域权限
            try {
                Long regionIdLong = Long.parseLong(regionId);
                boolean canAccessRegion = AuthorizationContextHolder.canAccessArea(regionIdLong);
                if (canAccessRegion) {
                    log.debug("RAC数据域权限验证通过: personId={}, regionId={}", personId, regionId);
                    return ConsumePermissionResult.success();
                }
            } catch (NumberFormatException e) {
                log.warn("区域ID格式错误，无法进行数据域权限验证: regionId={}", regionId);
            }

            // 5. 使用AccessAreaService进行精细的区域权限检查
            ConsumePermissionResult areaServiceResult = validateAreaServicePermission(personId, regionId);
            if (areaServiceResult.isSuccess()) {
                return areaServiceResult;
            }

            log.warn("区域权限验证失败: personId={}, regionId={}", personId, regionId);
            return ConsumePermissionResult.failure("REGION_PERMISSION_DENIED",
                    String.format("您无权限在区域 %s 进行消费", regionId));

        } catch (Exception e) {
            log.error("区域权限验证失败: personId={}, regionId={}", personId, regionId, e);
            return ConsumePermissionResult.failure("REGION_VALIDATION_ERROR", "区域权限验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证区域是否存在且有效
     */
    private ConsumePermissionResult validateRegionExists(String regionId) {
        try {
            // 从缓存或数据库查询区域状态
            String regionStatusKey = "region:status:" + regionId;
            String regionStatus = SmartRedisUtil.get(regionStatusKey);

            if (StringUtils.hasText(regionStatus)) {
                if ("ACTIVE".equals(regionStatus)) {
                    return ConsumePermissionResult.success();
                } else if ("INACTIVE".equals(regionStatus) || "DISABLED".equals(regionStatus)) {
                    return ConsumePermissionResult.failure("REGION_INACTIVE",
                            String.format("区域 %s 已停用，状态: %s", regionId, regionStatus));
                } else {
                    return ConsumePermissionResult.failure("REGION_STATUS_INVALID",
                            String.format("区域 %s 状态异常: %s", regionId, regionStatus));
                }
            }

            // 如果缓存中没有，尝试从AccessAreaService查询
            // 这里简化处理，实际应该调用accessAreaService.isRegionActive(regionId)
            boolean regionActive = true; // 临时假设，实际应该查询数据库
            if (!regionActive) {
                return ConsumePermissionResult.failure("REGION_NOT_FOUND", "区域不存在或已删除");
            }

            return ConsumePermissionResult.success();

        } catch (Exception e) {
            log.error("区域存在性验证失败: regionId={}", regionId, e);
            return ConsumePermissionResult.failure("REGION_EXISTENCE_ERROR", "区域验证失败: " + e.getMessage());
        }
    }

    /**
     * 使用AccessAreaService进行区域权限验证
     */
    private ConsumePermissionResult validateAreaServicePermission(Long personId, String regionId) {
        try {
            // 获取用户可访问的区域列表
            List<Long> accessibleAreaIds = accessAreaService.getUserAccessibleAreas(personId);

            if (accessibleAreaIds != null && !accessibleAreaIds.isEmpty()) {
                try {
                    Long targetRegionId = Long.parseLong(regionId);
                    if (accessibleAreaIds.contains(targetRegionId)) {
                        log.debug("AccessAreaService权限验证通过: personId={}, regionId={}", personId, regionId);
                        return ConsumePermissionResult.success();
                    }
                } catch (NumberFormatException e) {
                    log.warn("区域ID格式错误: regionId={}", regionId);
                }
            }

            // 检查是否具有区域管理权限（管理员权限）
            boolean hasManagePermission = StpUtil.hasPermission("ACCESS_AREA_MANAGE");
            if (hasManagePermission) {
                log.debug("区域管理权限验证通过，允许所有区域: personId={}", personId);
                return ConsumePermissionResult.success();
            }

            return ConsumePermissionResult.failure("AREA_SERVICE_PERMISSION_DENIED",
                    "AccessAreaService权限验证失败，无权限访问该区域");

        } catch (Exception e) {
            log.error("AccessAreaService权限验证失败: personId={}, regionId={}", personId, regionId, e);
            return ConsumePermissionResult.failure("AREA_SERVICE_ERROR", "区域服务权限验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证时间权限
     */
    private ConsumePermissionResult validateTimePermission(Long personId) {
        try {
            LocalTime now = LocalTime.now();

            // 检查全局消费时间限制
            String globalTimeLimitKey = "consume:global:time:limit";
            String globalTimeLimit = SmartRedisUtil.get(globalTimeLimitKey);
            if (StringUtils.hasText(globalTimeLimit)) {
                if (!isWithinTimeLimit(now, globalTimeLimit)) {
                    log.warn("超出全局消费时间限制: personId={}, currentTime={}, limit={}", personId, now, globalTimeLimit);
                    return ConsumePermissionResult.failure("TIME_LIMIT_EXCEEDED", "当前时间不允许消费");
                }
            }

            // 检查个人消费时间限制
            String personalTimeLimitKey = "consume:personal:" + personId + ":time:limit";
            String personalTimeLimit = SmartRedisUtil.get(personalTimeLimitKey);
            if (StringUtils.hasText(personalTimeLimit)) {
                if (!isWithinTimeLimit(now, personalTimeLimit)) {
                    log.warn("超出个人消费时间限制: personId={}, currentTime={}, limit={}", personId, now, personalTimeLimit);
                    return ConsumePermissionResult.failure("PERSONAL_TIME_LIMIT_EXCEEDED", "当前时间不允许您消费");
                }
            }

            return ConsumePermissionResult.success();

        } catch (Exception e) {
            log.error("时间权限验证失败: personId={}", personId, e);
            return ConsumePermissionResult.failure("TIME_VALIDATION_ERROR", "时间权限验证失败");
        }
    }

    /**
     * 检查是否在时间限制内
     */
    private boolean isWithinTimeLimit(LocalTime currentTime, String timeLimit) {
        try {
            // 格式: "09:00-21:00"
            String[] times = timeLimit.split("-");
            if (times.length != 2) {
                return true; // 格式错误，默认允许
            }

            LocalTime startTime = LocalTime.parse(times[0]);
            LocalTime endTime = LocalTime.parse(times[1]);

            return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);

        } catch (Exception e) {
            log.warn("时间限制格式解析失败: timeLimit={}", timeLimit, e);
            return true; // 解析失败，默认允许
        }
    }

    /**
     * 获取账户状态对应的提示信息
     */
    private String getAccountStatusMessage(String status) {
        switch (status) {
            case "INACTIVE":
                return "账户未激活";
            case "SUSPENDED":
                return "账户已暂停";
            case "CLOSED":
                return "账户已关闭";
            case "FROZEN":
                return "账户已冻结";
            default:
                return "账户状态异常: " + status;
        }
    }

    /**
     * 权限验证结果类
     */
    public static class ConsumePermissionResult {
        private boolean success;
        private String errorCode;
        private String errorMessage;

        private ConsumePermissionResult(boolean success, String errorCode, String errorMessage) {
            this.success = success;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public static ConsumePermissionResult success() {
            return new ConsumePermissionResult(true, null, null);
        }

        public static ConsumePermissionResult failure(String errorCode, String errorMessage) {
            return new ConsumePermissionResult(false, errorCode, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}