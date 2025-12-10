package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费交易实体类
 * <p>
 * 用于记录消费交易流水，支持多维度统计分析和对账
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 * <p>
 * 业务场景：
 * - 消费交易记录
 * - 对账和审计
 * - 多维度统计分析
 * - 考勤消费统计
 * </p>
 * <p>
 * 数据库表：POSID_TRANSACTION（业务文档中定义的表名）
 * 注意：根据CLAUDE.md规范，表名应使用t_consume_*格式，但业务文档中使用POSID_*格式
 * 实际使用时需要根据数据库表名调整@TableName注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_transaction")
public class ConsumeTransactionEntity extends BaseEntity {

    /**
     * 交易ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 交易流水号（唯一）
     */
    private String transactionNo;

    /**
     * 用户ID（对应person_id）
     */
    private Long userId;

    /**
     * 用户姓名（对应person_name）
     */
    private String userName;

    /**
     * 部门ID（对应dept_id）
     */
    private Long deptId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户类别ID
     */
    private Long accountKindId;

    /**
     * 是否考勤消费（冗余字段，来自账户类别）
     */
    private Boolean isAttendanceConsume;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域经营模式（1-餐别制 2-超市制 3-混合）
     */
    private Integer areaManageMode;

    /**
     * 区域细分类型
     */
    private Integer areaSubType;

    /**
     * 餐别ID
     */
    private Long mealId;

    /**
     * 餐别分类ID
     */
    private Long mealCategoryId;

    /**
     * 餐别名称
     */
    private String mealName;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 消费金额（单位：元）
     */
    private BigDecimal consumeMoney;

    /**
     * 折扣金额（单位：元）
     */
    private BigDecimal discountMoney;

    /**
     * 实际支付金额（单位：元）
     */
    private BigDecimal finalMoney;

    /**
     * 消费前余额（单位：元）
     */
    private BigDecimal balanceBefore;

    /**
     * 消费后余额（单位：元）
     */
    private BigDecimal balanceAfter;

    /**
     * 使用补贴金额（单位：元）
     */
    private BigDecimal allowanceUsed;

    /**
     * 使用现金金额（单位：元）
     */
    private BigDecimal cashUsed;

    /**
     * 消费模式
     * <p>
     * FIXED-定值
     * AMOUNT-金额
     * PRODUCT-商品
     * COUNT-计次
     * </p>
     */
    private String consumeMode;

    /**
     * 消费类型
     * <p>
     * CONSUME-正常消费
     * MAKEUP-补单
     * CORRECT-纠错
     * </p>
     */
    private String consumeType;

    /**
     * 定值规则ID
     */
    private Long fixedValueRuleId;

    /**
     * 第几次消费
     */
    private Integer fixedValueTimes;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;

    /**
     * 交易时间（与consumeTime相同，用于查询）
     */
    private LocalDateTime transactionTime;

    /**
     * 交易状态
     * <p>
     * 1-待处理
     * 2-成功
     * 3-失败
     * 4-已退款
     * </p>
     */
    private Integer transactionStatus;

    /**
     * 状态（字符串形式，兼容业务文档）
     * <p>
     * SUCCESS-成功
     * FAILED-失败
     * REFUND-已退款
     * </p>
     */
    private String status;

    /**
     * 商品ID（商品消费时使用）
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 获取消费金额（兼容方法）
     * <p>
     * 直接返回consumeMoney字段
     * </p>
     *
     * @return 消费金额
     */
    public BigDecimal getAmount() {
        return consumeMoney != null ? consumeMoney : BigDecimal.ZERO;
    }

    /**
     * 设置消费金额（兼容方法）
     * <p>
     * 直接设置consumeMoney字段
     * </p>
     *
     * @param amount 消费金额
     */
    public void setAmount(BigDecimal amount) {
        this.consumeMoney = amount;
    }

    /**
     * 商品详情（JSON格式）
     */
    private String productDetails;

    /**
     * 获取商品详情
     *
     * @return 商品详情
     */
    public String getProductDetails() {
        return productDetails;
    }

    /**
     * 设置商品详情
     *
     * @param productDetails 商品详情
     */
    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }
}
