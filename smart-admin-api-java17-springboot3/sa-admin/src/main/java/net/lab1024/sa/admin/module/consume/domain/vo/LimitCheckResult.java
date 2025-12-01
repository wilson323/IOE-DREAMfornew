/*
 * 限额检查结果
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 限额检查结果
 * 封装限额检查的执行结果
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */




public class LimitCheckResult {

    /**
     * 是否通过检查
     */
    private Boolean passed;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 检查类型
     */
    private String checkType;

    /**
     * 检查金额
     */
    private BigDecimal checkAmount;

    /**
     * 当前金额
     */
    private BigDecimal currentAmount;

    /**
     * 限额值
     */
    private BigDecimal limitValue;

    /**
     * 超出金额
     */
    private BigDecimal exceededAmount;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 限制类型
     */
    private String limitType;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 检查是否通过（业务逻辑方法）
     */
    public boolean isPassed() {
        return Boolean.TRUE.equals(passed);
    }

    /**
     * 获取结果消息（业务逻辑方法）
     */

    /**
     * 检查是否超过限额
     */
    public boolean isOverLimit() {
        return Boolean.FALSE.equals(passed);
    }

    /**
     * 获取超出比例（百分比）
     */

    /**
     * 获取剩余可用额度
     */

    /**
     * 创建通过结果
     */
    public static LimitCheckResult success(Long userId, String checkType, BigDecimal checkAmount,
                                          BigDecimal currentAmount, BigDecimal limitValue) {
        return LimitCheckResult.builder()
                .userId(userId)
                .checkType(checkType)
                .checkAmount(checkAmount)
                .currentAmount(currentAmount)
                .limitValue(limitValue)
                .passed(true)
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败结果
     */
    public static LimitCheckResult failure(Long userId, String checkType, BigDecimal checkAmount,
                                          BigDecimal currentAmount, BigDecimal limitValue) {
        BigDecimal exceeded = checkAmount.add(currentAmount).subtract(limitValue);
        return LimitCheckResult.builder()
                .userId(userId)
                .checkType(checkType)
                .checkAmount(checkAmount)
                .currentAmount(currentAmount)
                .limitValue(limitValue)
                .exceededAmount(exceeded)
                .passed(false)
                .checkTime(LocalDateTime.now())
                .build();
    }

    // ========== 手动添加的getter/setter方法 (Lombok失效备用) ==========



















}
