package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户交易记录实体类
 * 记录所有账户的资金变动交易
 *
 * @author IOE-DREAM Architecture Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_account_transaction")
public class AccountTransactionEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long transactionId;

    /**
     * 交易流水号（系统生成的唯一标识）
     */
    private String transactionNo;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 交易类型（RECHARGE-充值, CONSUME-消费, REFUND-退款, FREEZE-冻结, UNFREEZE-解冻, ADJUST-调整）
     */
    private String transactionType;

    /**
     * 交易金额（正数为入账，负数为出账）
     */
    private BigDecimal amount;

    /**
     * 交易前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 交易前冻结金额
     */
    private BigDecimal frozenAmountBefore;

    /**
     * 交易后冻结金额
     */
    private BigDecimal frozenAmountAfter;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 支付方式（CASH-现金, WECHAT-微信, ALIPAY-支付宝, CARD-刷卡, FACE-人脸, FINGERPRINT-指纹）
     */
    private String paymentMethod;

    /**
     * 交易状态（PENDING-待处理, SUCCESS-成功, FAILED-失败, CANCELLED-已取消）
     */
    private String status;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 交易来源（POS机, 手机APP, 网页, 管理后台）
     */
    private String source;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 门店名称
     */
    private String storeName;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 交易描述
     */
    private String description;

    /**
     * 交易备注
     */
    private String remark;

    /**
     * 退款原交易ID（仅退款交易有值）
     */
    private Long refundTransactionId;

    /**
     * 退款原交易流水号（仅退款交易有值）
     */
    private String refundTransactionNo;

    /**
     * 退款原因（仅退款交易有值）
     */
    private String refundReason;

    /**
     * 扩展数据JSON（存储额外信息）
     */
    private String extendData;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 批次号（用于批量交易）
     */
    private String batchNo;

    /**
     * 第三方交易号（如微信、支付宝的交易号）
     */
    private String thirdPartyTransactionNo;

    /**
     * 手续费
     */
    private BigDecimal fee;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 积分变动
     */
    private Integer pointsChange;

    /**
     * 是否为异常交易
     */
    private Boolean isAbnormal;

    /**
     * 异常原因
     */
    private String abnormalReason;

    /**
     * 审核状态（PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝）
     */
    private String auditStatus;

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
     * 审核意见
     */
    private String auditComment;
}