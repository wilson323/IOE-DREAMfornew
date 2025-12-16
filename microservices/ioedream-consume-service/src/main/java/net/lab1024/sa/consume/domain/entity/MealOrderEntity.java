package net.lab1024.sa.common.consume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订餐订单实体
 * <p>
 * 用于存储用户的订餐预约信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Data
@TableName("t_meal_order")
public class MealOrderEntity {

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
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
     * 账户ID
     */
    private Long accountId;

    /**
     * 区域ID（食堂）
     */
    private Long areaId;

    /**
     * 餐别ID（早餐/午餐/晚餐）
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
     * 订单状态：PENDING-待取餐, COMPLETED-已完成, CANCELLED-已取消, EXPIRED-已过期
     */
    private String status;

    /**
     * 取餐方式：QR_CODE-二维码, FACE-刷脸, CARD-刷卡
     */
    private String pickupMethod;

    /**
     * 取餐时间
     */
    private LocalDateTime pickupTime;

    /**
     * 取餐设备ID
     */
    private Long pickupDeviceId;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;
}
