/*
 * 消费限额配置服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.ConsumeLimitConfig;
import net.lab1024.sa.admin.module.consume.domain.vo.LimitValidationResult;
import net.lab1024.sa.admin.module.consume.domain.vo.ConsumeStatistics;
import net.lab1024.sa.admin.module.consume.domain.vo.LimitCheckResult;
import net.lab1024.sa.admin.module.consume.domain.vo.LimitChangeHistory;
import net.lab1024.sa.admin.module.consume.domain.vo.BatchLimitSetting;
import net.lab1024.sa.admin.module.consume.domain.vo.BatchLimitSetResult;
import net.lab1024.sa.admin.module.consume.domain.vo.GlobalLimitConfig;
import net.lab1024.sa.admin.module.consume.domain.vo.LimitUsageReport;
import net.lab1024.sa.admin.module.consume.domain.vo.LimitConflictCheckResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * 消费限额配置服务接口
 * 提供动态消费限额配置和管理功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface ConsumeLimitConfigService {

    /**
     * 获取用户的消费限额配置
     *
     * @param personId 人员ID
     * @return 限额配置
     */
    ConsumeLimitConfig getUserLimitConfig(@NotNull Long personId);

    /**
     * 获取指定设备的消费限额配置
     *
     * @param deviceId 设备ID
     * @return 限额配置
     */
    ConsumeLimitConfig getDeviceLimitConfig(@NotNull Long deviceId);

    /**
     * 获取指定区域的消费限额配置
     *
     * @param regionId 区域ID
     * @return 限额配置
     */
    ConsumeLimitConfig getRegionLimitConfig(@NotNull String regionId);

    /**
     * 获取指定消费模式的限额配置
     *
     * @param consumptionMode 消费模式
     * @return 限额配置
     */
    ConsumeLimitConfig getModeLimitConfig(@NotNull String consumptionMode);

    /**
     * 设置用户的消费限额
     *
     * @param personId 人员ID
     * @param limitConfig 限额配置
     * @param reason 修改原因
     * @return 设置结果
     */
    boolean setUserLimitConfig(@NotNull Long personId, @NotNull ConsumeLimitConfig limitConfig, String reason);

    /**
     * 设置设备的消费限额
     *
     * @param deviceId 设备ID
     * @param limitConfig 限额配置
     * @param reason 修改原因
     * @return 设置结果
     */
    boolean setDeviceLimitConfig(@NotNull Long deviceId, @NotNull ConsumeLimitConfig limitConfig, String reason);

    /**
     * 设置区域的消费限额
     *
     * @param regionId 区域ID
     * @param limitConfig 限额配置
     * @param reason 修改原因
     * @return 设置结果
     */
    boolean setRegionLimitConfig(@NotNull String regionId, @NotNull ConsumeLimitConfig limitConfig, String reason);

    /**
     * 验证消费金额是否符合限额要求
     *
     * @param personId 人员ID
     * @param amount 消费金额
     * @param deviceId 设备ID
     * @param regionId 区域ID
     * @param consumptionMode 消费模式
     * @return 验证结果
     */
    LimitValidationResult validateConsumeLimit(@NotNull Long personId, @NotNull BigDecimal amount,
                                              Long deviceId, String regionId, String consumptionMode);

    /**
     * 获取用户当前消费统计信息
     *
     * @param personId 人员ID
     * @return 消费统计
     */
    ConsumeStatistics getUserConsumeStatistics(@NotNull Long personId);

    /**
     * 检查用户是否超出限额
     *
     * @param personId 人员ID
     * @param amount 消费金额
     * @return 检查结果
     */
    LimitCheckResult checkUserLimit(@NotNull Long personId, @NotNull BigDecimal amount);

    /**
     * 动态调整用户限额（基于信用等级等）
     *
     * @param personId 人员ID
     * @param adjustType 调整类型（INCREASE/DECREASE/RESET）
     * @param adjustRatio 调整比例
     * @param reason 调整原因
     * @return 调整结果
     */
    boolean adjustUserLimit(@NotNull Long personId, @NotNull String adjustType, Double adjustRatio, String reason);

    /**
     * 临时调整用户限额（限时有效）
     *
     * @param personId 人员ID
     * @param temporaryLimits 临时限额配置
     * @param expireMinutes 有效时长（分钟）
     * @param reason 调整原因
     * @return 调整结果
     */
    boolean setTemporaryLimit(@NotNull Long personId, @NotNull ConsumeLimitConfig temporaryLimits,
                              Integer expireMinutes, String reason);

    /**
     * 获取限额变更历史记录
     *
     * @param personId 人员ID
     * @param limit 记录数量限制
     * @return 变更历史记录
     */
    List<LimitChangeHistory> getLimitChangeHistory(@NotNull Long personId, Integer limit);

    /**
     * 批量设置限额
     *
     * @param limitSettings 限额设置列表
     * @param reason 设置原因
     * @return 设置结果
     */
    BatchLimitSetResult batchSetLimits(@NotNull List<BatchLimitSetting> limitSettings, String reason);

    /**
     * 获取全局限额配置
     *
     * @return 全局限额配置
     */
    GlobalLimitConfig getGlobalLimitConfig();

    /**
     * 设置全局限额配置
     *
     * @param globalConfig 全局限额配置
     * @return 设置结果
     */
    boolean setGlobalLimitConfig(@NotNull GlobalLimitConfig globalConfig);

    /**
     * 重新计算用户限额（基于规则引擎）
     *
     * @param personId 人员ID
     * @return 计算结果
     */
    boolean recalculateUserLimit(@NotNull Long personId);

    /**
     * 获取限额使用率报告
     *
     * @param personId 人员ID
     * @return 使用率报告
     */
    LimitUsageReport getLimitUsageReport(@NotNull Long personId);

    /**
     * 检查是否存在限额冲突
     *
     * @param personId 人员ID
     * @param limitConfig 限额配置
     * @return 冲突检查结果
     */
    LimitConflictCheckResult checkLimitConflict(@NotNull Long personId, @NotNull ConsumeLimitConfig limitConfig);
}