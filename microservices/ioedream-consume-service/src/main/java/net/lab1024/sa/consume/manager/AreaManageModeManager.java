package net.lab1024.sa.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.domain.config.AreaConfig;
import net.lab1024.sa.consume.domain.config.FixedValueConfig;
import net.lab1024.sa.consume.entity.PosidAreaEntity;
import net.lab1024.sa.common.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 区域经营模式管理器
 *
 * 职责：处理3种区域经营模式的业务逻辑
 *
 * 支持的区域经营模式：
 * 1. 餐别制模式（manage_mode=1）：
 *    - 检查用户是否有当前时段餐别权限
 *    - 按餐别定值金额扣款
 *    - 每餐只能消费一次
 *
 * 2. 超市制模式（manage_mode=2）：
 *    - 不检查餐别权限
 *    - 支持多次消费
 *    - 按实际商品金额扣款
 *
 * 3. 混合模式（manage_mode=3）：
 *    - 根据 area_config 中的子区域配置决定使用哪种模式
 *    - 餐吧使用餐别制
 *    - 超市使用超市制
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
public class AreaManageModeManager {

    /**
     * 检查用户是否有权在指定区域消费
     *
     * @param userId 用户ID
     * @param area 区域信息
     * @param mealType 餐别类型（餐别制模式需要）
     * @return 是否有权消费
     */
    public boolean checkConsumePermission(Long userId, PosidAreaEntity area, String mealType) {
        Integer manageMode = area.getManageMode();
        log.info("[区域经营模式] 检查消费权限: userId={}, areaId={}, manageMode={}, mealType={}",
                userId, area.getAreaId(), manageMode, mealType);

        switch (manageMode) {
            case 1: // 餐别制
                return checkMealModePermission(userId, area, mealType);

            case 2: // 超市制
                // 超市制不需要餐别权限
                return true;

            case 3: // 混合模式
                // 混合模式根据子区域配置决定
                return checkHybridModePermission(userId, area, mealType);

            default:
                log.warn("[区域经营模式] 未知的经营模式: manageMode={}", manageMode);
                return false;
        }
    }

    /**
     * 获取消费金额
     *
     * @param area 区域信息
     * @param mealType 餐别类型（餐别制模式需要）
     * @param amount 用户输入金额（超市制模式需要）
     * @return 消费金额
     */
    public BigDecimal getConsumeAmount(PosidAreaEntity area, String mealType, BigDecimal amount) {
        Integer manageMode = area.getManageMode();
        log.info("[区域经营模式] 获取消费金额: areaId={}, manageMode={}, mealType={}, amount={}",
                area.getAreaId(), manageMode, mealType, amount);

        switch (manageMode) {
            case 1: // 餐别制
                return getMealModeAmount(area, mealType);

            case 2: // 超市制
                // 超市制使用用户输入金额
                return amount;

            case 3: // 混合模式
                // 混合模式需要根据子区域配置决定
                return getHybridModeAmount(area, mealType, amount);

            default:
                throw new BusinessException("未知的经营模式: " + manageMode);
        }
    }

    /**
     * 检查餐别制模式权限
     */
    private boolean checkMealModePermission(Long userId, PosidAreaEntity area, String mealType) {
        log.info("[餐别制模式] 检查权限: userId={}, areaId={}, mealType={}",
                userId, area.getAreaId(), mealType);

        // 检查区域权限配置
        AreaConfig areaConfig = area.getAreaConfig();
        if (areaConfig == null) {
            // 无配置则允许所有餐别
            return true;
        }

        AreaConfig.MealPermissionConfig mealPermissions = areaConfig.getMealPermissions();
        if (mealPermissions == null) {
            return true;
        }

        // 检查禁止的餐别
        List<String> deniedMeals = mealPermissions.getDeniedMeals();
        if (deniedMeals != null && deniedMeals.contains(mealType)) {
            log.warn("[餐别制模式] 餐别被禁止: mealType={}", mealType);
            return false;
        }

        // 检查允许的餐别
        List<String> allowedMeals = mealPermissions.getAllowedMeals();
        if (allowedMeals != null && !allowedMeals.isEmpty() && !allowedMeals.contains(mealType)) {
            log.warn("[餐别制模式] 餐别不在允许列表: mealType={}", mealType);
            return false;
        }

        log.info("[餐别制模式] 权限检查通过: mealType={}", mealType);
        return true;
    }

    /**
     * 获取餐别制模式金额
     */
    private BigDecimal getMealModeAmount(PosidAreaEntity area, String mealType) {
        log.info("[餐别制模式] 获取定值金额: areaId={}, mealType={}", area.getAreaId(), mealType);

        FixedValueConfig fixedValueConfig = area.getFixedValueConfig();
        if (fixedValueConfig == null) {
            throw new BusinessException("区域定值配置不存在");
        }

        // 优先级1: 餐别映射金额
        if (fixedValueConfig.getMealAmounts() != null) {
            BigDecimal amount = fixedValueConfig.getMealAmounts().get(mealType);
            if (amount != null) {
                log.info("[餐别制模式] 餐别定值金额: mealType={}, amount={}", mealType, amount);
                return amount;
            }
        }

        // 优先级2: 全局默认金额
        BigDecimal defaultAmount = fixedValueConfig.getDefaultAmount();
        if (defaultAmount != null) {
            log.info("[餐别制模式] 默认定值金额: amount={}", defaultAmount);
            return defaultAmount;
        }

        throw new BusinessException("未配置定值金额: mealType=" + mealType);
    }

    /**
     * 检查混合模式权限
     */
    private boolean checkHybridModePermission(Long userId, PosidAreaEntity area, String mealType) {
        log.info("[混合模式] 检查权限: userId={}, areaId={}, mealType={}",
                userId, area.getAreaId(), mealType);

        // 混合模式需要检查子区域配置
        AreaConfig areaConfig = area.getAreaConfig();
        if (areaConfig == null || areaConfig.getSubAreas() == null) {
            // 无配置则默认为餐别制
            return checkMealModePermission(userId, area, mealType);
        }

        // TODO: 根据子区域类型决定使用哪种模式
        // 这里简化处理，假设子区域配置中包含了模式信息
        return true;
    }

    /**
     * 获取混合模式金额
     */
    private BigDecimal getHybridModeAmount(PosidAreaEntity area, String mealType, BigDecimal amount) {
        log.info("[混合模式] 获取金额: areaId={}, mealType={}, amount={}",
                area.getAreaId(), mealType, amount);

        // TODO: 根据子区域配置决定使用哪种模式的金额计算方式
        // 这里简化处理，先尝试餐别制金额，如果配置不存在则使用用户输入金额
        try {
            return getMealModeAmount(area, mealType);
        } catch (Exception e) {
            return amount;
        }
    }

    /**
     * 检查时间段权限
     *
     * @param area 区域信息
     * @return 是否在允许消费的时间段内
     */
    public boolean checkTimePermission(PosidAreaEntity area) {
        LocalTime now = LocalTime.now();
        log.info("[区域经营模式] 检查时间段权限: areaId={}, currentTime={}", area.getAreaId(), now);

        AreaConfig areaConfig = area.getAreaConfig();
        if (areaConfig == null || areaConfig.getTimePermissions() == null) {
            // 无配置则允许任何时间
            return true;
        }

        AreaConfig.TimePermissionConfig timePermissions = areaConfig.getTimePermissions();

        // 检查禁止的时间段
        List<String> deniedRanges = timePermissions.getDeniedTimeRanges();
        if (deniedRanges != null) {
            for (String range : deniedRanges) {
                if (isTimeInRange(now, range)) {
                    log.warn("[区域经营模式] 当前时间在禁止时段: time={}, range={}", now, range);
                    return false;
                }
            }
        }

        // 检查允许的时间段
        List<String> allowedRanges = timePermissions.getAllowedTimeRanges();
        if (allowedRanges != null && !allowedRanges.isEmpty()) {
            for (String range : allowedRanges) {
                if (isTimeInRange(now, range)) {
                    log.info("[区域经营模式] 当前时间在允许时段: time={}, range={}", now, range);
                    return true;
                }
            }
            log.warn("[区域经营模式] 当前时间不在任何允许时段: time={}", now);
            return false;
        }

        return true;
    }

    /**
     * 检查时间是否在指定范围内
     *
     * @param time 当前时间
     * @param range 时间范围（格式：HH:mm-HH:mm）
     * @return 是否在范围内
     */
    private boolean isTimeInRange(LocalTime time, String range) {
        try {
            String[] parts = range.split("-");
            if (parts.length != 2) {
                log.warn("[区域经营模式] 无效的时间范围格式: range={}", range);
                return false;
            }

            LocalTime startTime = LocalTime.parse(parts[0].trim());
            LocalTime endTime = LocalTime.parse(parts[1].trim());

            return !time.isBefore(startTime) && !time.isAfter(endTime);

        } catch (Exception e) {
            log.error("[区域经营模式] 解析时间范围异常: range={}, error={}", range, e.getMessage());
            return false;
        }
    }

    /**
     * 检查消费限额
     *
     * @param area 区域信息
     * @param currentAmount 当前消费金额
     * @param dailyAmount 今日已消费金额
     * @param dailyCount 今日已消费次数
     * @return 是否在限额内
     */
    public boolean checkConsumeLimit(PosidAreaEntity area,
                                    BigDecimal currentAmount,
                                    BigDecimal dailyAmount,
                                    int dailyCount) {
        log.info("[区域经营模式] 检查消费限额: areaId={}, currentAmount={}, dailyAmount={}, dailyCount={}",
                area.getAreaId(), currentAmount, dailyAmount, dailyCount);

        AreaConfig areaConfig = area.getAreaConfig();
        if (areaConfig == null || areaConfig.getConsumeLimits() == null) {
            // 无配置则不限制
            return true;
        }

        AreaConfig.ConsumeLimitConfig limits = areaConfig.getConsumeLimits();

        // 检查单笔消费限额
        if (limits.getSingleAmountLimit() != null) {
            if (currentAmount.compareTo(BigDecimal.valueOf(limits.getSingleAmountLimit())) > 0) {
                log.warn("[区域经营模式] 超过单笔限额: current={}, limit={}",
                        currentAmount, limits.getSingleAmountLimit());
                return false;
            }
        }

        // 检查每日消费限额
        if (limits.getDailyAmountLimit() != null) {
            BigDecimal totalAmount = dailyAmount.add(currentAmount);
            if (totalAmount.compareTo(BigDecimal.valueOf(limits.getDailyAmountLimit())) > 0) {
                log.warn("[区域经营模式] 超过每日限额: total={}, limit={}",
                        totalAmount, limits.getDailyAmountLimit());
                return false;
            }
        }

        // 检查每日消费次数限制
        if (limits.getDailyCountLimit() != null) {
            if (dailyCount >= limits.getDailyCountLimit()) {
                log.warn("[区域经营模式] 超过每日次数限制: count={}, limit={}",
                        dailyCount, limits.getDailyCountLimit());
                return false;
            }
        }

        log.info("[区域经营模式] 消费限额检查通过");
        return true;
    }
}
