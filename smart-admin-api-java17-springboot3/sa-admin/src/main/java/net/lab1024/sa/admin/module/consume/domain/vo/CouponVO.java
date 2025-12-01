package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券VO
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 提供完整的展示字段
 * - 包含格式化后的显示信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Data
@Accessors(chain = true)
public class CouponVO {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券编号
     */
    private String couponNo;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠券描述
     */
    private String description;

    /**
     * 优惠券类型代码
     */
    private String couponTypeCode;

    /**
     * 优惠券类型名称
     */
    private String couponTypeName;

    /**
     * 优惠券面额
     */
    private BigDecimal couponAmount;

    /**
     * 优惠券折扣率
     */
    private BigDecimal discountRate;

    /**
     * 最低消费金额
     */
    private BigDecimal minimumAmount;

    /**
     * 最高减免金额
     */
    private BigDecimal maximumDiscount;

    /**
     * 优惠券适用范围
     */
    private String applicableScope;

    /**
     * 适用商品ID列表
     */
    private String applicableProductIds;

    /**
     * 适用分类ID列表
     */
    private String applicableCategoryIds;

    /**
     * 适用品牌ID列表
     */
    private String applicableBrandIds;

    /**
     * 适用商品数量
     */
    private Integer applicableProductCount;

    /**
     * 发行数量
     */
    private Integer totalQuantity;

    /**
     * 已领取数量
     */
    private Integer receivedQuantity;

    /**
     * 已使用数量
     */
    private Integer usedQuantity;

    /**
     * 剩余数量
     */
    private Integer remainingQuantity;

    /**
     * 用户限领数量
     */
    private Integer userLimit;

    /**
     * 每人限领数量
     */
    private Integer perUserLimit;

    /**
     * 每日限领数量
     */
    private Integer dailyLimit;

    /**
     * 有效期开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 有效期天数
     */
    private Integer effectiveDays;

    /**
     * 领取开始时间
     */
    private LocalDateTime claimStartTime;

    /**
     * 领取结束时间
     */
    private LocalDateTime claimEndTime;

    /**
     * 领取期天数
     */
    private Integer claimDays;

    /**
     * 使用条件
     */
    private String usageConditions;

    /**
     * 使用说明
     */
    private String usageInstructions;

    /**
     * 排除条件
     */
    private String exclusionConditions;

    /**
     * 优惠券状态代码
     */
    private String statusCode;

    /**
     * 优惠券状态名称
     */
    private String statusName;

    /**
     * 优惠券图片URL
     */
    private String imageUrl;

    /**
     * 优惠券颜色代码
     */
    private String colorCode;

    /**
     * 优惠券标签
     */
    private String tags;

    /**
     * 推荐指数
     */
    private Integer recommendationScore;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 发放方式
     */
    private String distributionMethod;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓名
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 审核备注
     */
    private String auditRemarks;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 同步状态
     */
    private String syncStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;
}