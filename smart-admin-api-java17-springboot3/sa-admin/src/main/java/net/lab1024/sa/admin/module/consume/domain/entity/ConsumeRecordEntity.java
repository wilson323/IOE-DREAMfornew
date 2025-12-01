package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_record")
public class ConsumeRecordEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    /**
     * 员工ID（兼容旧系统）
     */
    private Long employeeId;

    /**
     * 人员ID（主键关联）
     */
    private Long personId;

    /**
     * 人员姓名
     */
    private String personName;

    /**
     * 消费日期
     */
    private LocalDate consumeDate;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费金额别名（与amount相同，兼容旧系统）
     */
    private BigDecimal consumeAmount;

    /**
     * 消费前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 消费后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 币种（默认CNY）
     */
    private String currency;

    /**
     * 消费类型（兼容旧系统：MEAL/SNACK/DRINK/OTHER）
     */
    private String consumeType;

    /**
     * 支付方式（CARD/WALLET/CASH）
     */
    private String payMethod;

    /**
     * 订单号（唯一标识）
     */
    private String orderNo;

    /**
     * 交易状态（SUCCESS/FAILED/REFUND/PENDING）
     */
    private String status;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 消费模式（FIXED_AMOUNT/FREE_AMOUNT/METERING/PRODUCT/ORDERING/SMART）
     */
    private String consumptionMode;

    /**
     * 区域ID
     */
    private String regionId;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 账户类型（STAFF/STUDENT/VISITOR/TEMP）
     */
    private String accountType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 消费模式配置JSON（存储使用的配置参数）
     */
    private String modeConfig;

    /**
     * 扩展数据JSON（存储扩展字段）
     */
    private String extendData;

    /**
     * 终端IP地址
     */
    private String clientIp;

    /**
     * 交易流水号（银行或第三方支付返回）
     */
    private String transactionId;

    /**
     * 第三方支付订单号
     */
    private String thirdPartyOrderNo;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 关联的原始交易记录ID（用于退款）
     */
    private Long originalRecordId;
}

