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
 * 余额变动记录实体类
 * 严格遵循repowiki规范：记录所有账户余额变动，支持审计和追溯
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_balance_change")
public class BalanceChangeEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long changeId;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 变动类型（CONSUME/RECHARGE/REFUND/FREEZE/UNFREEZE）
     */
    private String changeType;

    /**
     * 变动金额（正数表示增加，负数表示减少）
     */
    private BigDecimal changeAmount;

    /**
     * 变动前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 变动后余额
     */
    private BigDecimal afterBalance;

    /**
     * 变动前冻结金额
     */
    private BigDecimal beforeFrozenAmount;

    /**
     * 变动后冻结金额
     */
    private BigDecimal afterFrozenAmount;

    /**
     * 业务编号（订单号、充值单号等）
     */
    private String businessNo;

    /**
     * 业务类型（消费、充值、退款等）
     */
    private String businessType;

    /**
     * 业务描述
     */
    private String businessDesc;

    /**
     * 关联的交易ID
     */
    private Long transactionId;

    /**
     * 关联的消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 操作地点
     */
    private String location;

    /**
     * 操作IP
     */
    private String operationIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 变动原因
     */
    private String changeReason;

    /**
     * 操作人员ID
     */
    private Long operatorId;

    /**
     * 操作人员姓名
     */
    private String operatorName;

    /**
     * 审批状态（PENDING/APPROVED/REJECTED）
     */
    private String approvalStatus;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批时间
     */
    private LocalDateTime approveTime;

    /**
     * 审批意见
     */
    private String approveRemark;

    /**
     * 乐观锁版本号
     */
    private Integer version;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 交易批次号
     */
    private String batchNo;

    /**
     * 关联的外部系统ID
     */
    private String externalSystemId;

    /**
     * 数据来源（WEB/APP/API/SYSTEM）
     */
    private String dataSource;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否为补偿交易（0：否，1：是）
     */
    private Boolean isCompensation;

    /**
     * 关联的原始变动记录ID（用于补偿交易）
     */
    private Long originalChangeId;

    /**
     * 检查是否为余额增加
     */
    public boolean isBalanceIncrease() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 检查是否为余额减少
     */
    public boolean isBalanceDecrease() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 检查是否需要审批
     */
    public boolean needsApproval() {
        return "PENDING".equals(approvalStatus);
    }

    /**
     * 检查是否已审批
     */
    public boolean isApproved() {
        return "APPROVED".equals(approvalStatus);
    }

    /**
     * 检查是否被拒绝
     */
    public boolean isRejected() {
        return "REJECTED".equals(approvalStatus);
    }

    /**
     * 获取绝对变动金额（正数）
     */
    public BigDecimal getAbsoluteChangeAmount() {
        return changeAmount != null ? changeAmount.abs() : BigDecimal.ZERO;
    }

    /**
     * 获取变动类型的描述
     */
    public String getChangeTypeDesc() {
        switch (changeType) {
            case "CONSUME":
                return "消费扣减";
            case "RECHARGE":
                return "充值增加";
            case "REFUND":
                return "退款返还";
            case "FREEZE":
                return "冻结";
            case "UNFREEZE":
                return "解冻";
            case "ADJUST":
                return "余额调整";
            default:
                return "其他";
        }
    }
}