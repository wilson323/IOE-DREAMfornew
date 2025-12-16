package net.lab1024.sa.consume.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订餐订单VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
public class MealOrderVO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

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
     * 预约日期
     */
    private LocalDateTime orderDate;

    /**
     * 取餐开始时间
     */
    private LocalDateTime pickupStartTime;

    /**
     * 取餐结束时间
     */
    private LocalDateTime pickupEndTime;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 取餐方式
     */
    private String pickupMethod;

    /**
     * 取餐时间
     */
    private LocalDateTime pickupTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
