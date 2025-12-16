package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订餐统计VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class MealOrderStatisticsVO {

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 餐别ID
     */
    private Long mealTypeId;

    /**
     * 餐别名称
     */
    private String mealTypeName;

    /**
     * 统计日期
     */
    private String date;

    /**
     * 订餐总数
     */
    private Integer totalOrders;

    /**
     * 已完成数
     */
    private Integer completedOrders;

    /**
     * 已取消数
     */
    private Integer cancelledOrders;

    /**
     * 已过期数
     */
    private Integer expiredOrders;

    /**
     * 待取餐数
     */
    private Integer pendingOrders;

    /**
     * 订餐总金额
     */
    private BigDecimal totalAmount;

    /**
     * 实收金额
     */
    private BigDecimal actualAmount;

    /**
     * 完成率
     */
    private BigDecimal completionRate;

    /**
     * 取消率
     */
    private BigDecimal cancellationRate;
}
