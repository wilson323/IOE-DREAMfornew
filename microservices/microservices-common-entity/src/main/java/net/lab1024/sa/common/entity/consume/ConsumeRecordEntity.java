package net.lab1024.sa.common.entity.consume;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费记录实体类
 * <p>
 * 记录消费交易信息，支持在线/离线消费和退款管理
 * 提供交易流水、支付状态跟踪和同步处理等功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * 业务场景：
 * - 在线消费记录
 * - 离线消费记录
 * - 消费退款处理
 * - 离线同步管理
 * - 交易统计分析
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consume_record")
@Schema(description = "消费记录实体")
public class ConsumeRecordEntity extends BaseEntity {

    /**
     * 记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "记录ID")
    private Long recordId;

    /**
     * 订单编号（业务唯一）
     */
    @Schema(description = "订单编号")
    private String orderNo;

    /**
     * 交易流水号（支付平台返回）
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 账户ID
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号
     */
    @Schema(description = "账户编号")
    private String accountCode;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    /**
     * 消费金额（元）
     */
    @Schema(description = "消费金额")
    private BigDecimal amount;

    /**
     * 优惠金额（元）
     */
    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    /**
     * 补贴金额（元）
     */
    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    /**
     * 实付金额（元）
     */
    @Schema(description = "实付金额")
    private BigDecimal actualAmount;

    /**
     * 消费时间
     */
    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    /**
     * 消费类型（1-购物 2-餐饮 3-服务 4-其他）
     */
    @Schema(description = "消费类型（1-购物 2-餐饮 3-服务 4-其他）")
    private Integer consumeType;

    /**
     * 消费类型名称
     */
    @Schema(description = "消费类型名称")
    private String consumeTypeName;

    /**
     * 支付方式（1-余额 2-补贴 3-混合 4-现金 5-扫码 6-刷卡）
     */
    @Schema(description = "支付方式（1-余额 2-补贴 3-混合 4-现金 5-扫码 6-刷卡）")
    private Integer paymentMethod;

    /**
     * 支付方式名称
     */
    @Schema(description = "支付方式名称")
    private String paymentMethodName;

    /**
     * 交易状态（0-待支付 1-支付成功 2-支付失败 3-已退款 4-已取消）
     */
    @Schema(description = "交易状态（0-待支付 1-支付成功 2-支付失败 3-已退款 4-已取消）")
    private Integer transactionStatus;

    /**
     * 交易状态名称
     */
    @Schema(description = "交易状态名称")
    private String transactionStatusName;

    /**
     * 退款状态（0-未退款 1-退款中 2-已退款 3-退款失败）
     */
    @Schema(description = "退款状态（0-未退款 1-退款中 2-已退款 3-退款失败）")
    private Integer refundStatus;

    /**
     * 退款金额（元）
     */
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    /**
     * 离线标识（0-在线 1-离线）
     */
    @Schema(description = "离线标识（0-在线 1-离线）")
    private Integer offlineFlag;

    /**
     * 同步状态（0-待同步 1-已同步 2-同步失败）
     */
    @Schema(description = "同步状态（0-待同步 1-已同步 2-同步失败）")
    private Integer syncStatus;

    /**
     * 同步时间
     */
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    /**
     * 同步失败原因
     */
    @Schema(description = "同步失败原因")
    private String syncFailureReason;

    /**
     * 商品明细（JSON数组）
     */
    @TableField(exist = false)
    @Schema(description = "商品明细")
    private String items;

    /**
     * 消费地点
     */
    @Schema(description = "消费地点")
    private String location;

    /**
     * 消费备注
     */
    @Schema(description = "消费备注")
    private String remark;

    /**
     * 扩展属性（JSON格式，存储业务特定的扩展信息）
     */
    @TableField(exist = false)
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    // ==================== 业务方法 ====================

    /**
     * 检查是否支付成功
     *
     * @return true-支付成功，false-未支付或支付失败
     */
    public boolean isPaymentSuccess() {
        return transactionStatus != null && transactionStatus == 1;
    }

    /**
     * 检查是否离线交易
     *
     * @return true-离线交易，false-在线交易
     */
    public boolean isOffline() {
        return offlineFlag != null && offlineFlag == 1;
    }

    /**
     * 检查是否待同步
     *
     * @return true-待同步，false-已同步或不需要同步
     */
    public boolean isPendingSync() {
        return offlineFlag != null && offlineFlag == 1
            && syncStatus != null && syncStatus == 0;
    }

    /**
     * 检查是否同步成功
     *
     * @return true-已同步，false-未同步或同步失败
     */
    public boolean isSynced() {
        return syncStatus != null && syncStatus == 1;
    }

    /**
     * 检查是否可以退款
     *
     * @return true-可退款，false-不可退款
     */
    public boolean canRefund() {
        return transactionStatus != null && transactionStatus == 1
            && (refundStatus == null || refundStatus == 0);
    }

    /**
     * 检查是否已退款
     *
     * @return true-已退款，false-未退款
     */
    public boolean isRefunded() {
        return refundStatus != null && refundStatus == 2;
    }

    /**
     * 检查是否退款中
     *
     * @return true-退款中，false-未退款或退款完成
     */
    public boolean isRefunding() {
        return refundStatus != null && refundStatus == 1;
    }

    /**
     * 获取实际支付金额（扣除优惠和补贴）
     *
     * @return 实际支付金额
     */
    public BigDecimal getActualPayAmount() {
        if (actualAmount != null) {
            return actualAmount;
        }
        BigDecimal payAmount = amount != null ? amount : BigDecimal.ZERO;
        BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        BigDecimal subsidy = subsidyAmount != null ? subsidyAmount : BigDecimal.ZERO;
        return payAmount.subtract(discount).subtract(subsidy);
    }

    /**
     * 获取退款可退金额
     *
     * @return 可退金额
     */
    public BigDecimal getRefundableAmount() {
        if (refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            return getActualPayAmount().subtract(refundAmount);
        }
        return getActualPayAmount();
    }

    /**
     * 标记为同步成功
     */
    public void markAsSynced() {
        this.syncStatus = 1;
        this.syncTime = LocalDateTime.now();
        this.syncFailureReason = null;
    }

    /**
     * 标记为同步失败
     *
     * @param reason 失败原因
     */
    public void markAsSyncFailed(String reason) {
        this.syncStatus = 2;
        this.syncTime = LocalDateTime.now();
        this.syncFailureReason = reason;
    }

    /**
     * 标记为退款中
     */
    public void markAsRefunding() {
        this.refundStatus = 1;
    }

    /**
     * 标记为已退款
     *
     * @param refundAmount 退款金额
     */
    public void markAsRefunded(BigDecimal refundAmount) {
        this.refundStatus = 2;
        this.refundAmount = refundAmount;
        this.refundTime = LocalDateTime.now();
    }
}
