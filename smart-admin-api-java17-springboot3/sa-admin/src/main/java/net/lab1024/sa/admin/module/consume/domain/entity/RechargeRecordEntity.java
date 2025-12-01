package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录实体
 * <p>
 * 严格遵循repowiki规范:
 * - 继承BaseEntity，包含审计字段
 * - 使用@TableName指定表名
 * - 使用@TableId标记主键
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的充值业务字段
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("t_recharge_record")
public class RechargeRecordEntity extends BaseEntity {

    /**
     * 充值记录ID
     */
    @TableId
    private Long rechargeRecordId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 充值方式：1-现金 2-银行卡 3-微信 4-支付宝 5-其他
     */
    private Integer rechargeMethod;

    /**
     * 充值方式描述
     */
    private String rechargeMethodText;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 外部交易号
     */
    private String externalTransactionNo;

    /**
     * 充值状态：1-待确认 2-成功 3-失败 4-已退款
     */
    private Integer status;

    /**
     * 充值状态描述
     */
    private String statusText;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 充值时间
     */
    private LocalDateTime rechargeTime;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 实际到账金额
     */
    private BigDecimal actualAmount;

    /**
     * 支付渠道
     */
    private String paymentChannel;

    /**
     * 终端IP
     */
    private String clientIp;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 获取充值状态描述
     *
     * @return 充值状态描述
     */
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 1:
                return "待确认";
            case 2:
                return "成功";
            case 3:
                return "失败";
            case 4:
                return "已退款";
            default:
                return "未知";
        }
    }

    /**
     * 获取充值方式描述
     *
     * @return 充值方式描述
     */
    public String getRechargeMethodText() {
        if (rechargeMethod == null) {
            return "未知";
        }
        switch (rechargeMethod) {
            case 1:
                return "现金";
            case 2:
                return "银行卡";
            case 3:
                return "微信";
            case 4:
                return "支付宝";
            case 5:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 判断是否充值成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return Integer.valueOf(2).equals(status);
    }

    /**
     * 判断是否需要确认
     *
     * @return 是否需要确认
     */
    public boolean needsConfirmation() {
        return Integer.valueOf(1).equals(status);
    }

    /**
     * 判断是否失败
     *
     * @return 是否失败
     */
    public boolean isFailed() {
        return Integer.valueOf(3).equals(status);
    }

    /**
     * 计算实际到账金额
     *
     * @return 实际到账金额
     */
    public BigDecimal calculateActualAmount() {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (fee == null) {
            return amount;
        }
        return amount.subtract(fee);
    }
}