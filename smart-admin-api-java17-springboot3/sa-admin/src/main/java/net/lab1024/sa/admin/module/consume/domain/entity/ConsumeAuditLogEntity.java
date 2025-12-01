package net.lab1024.sa.admin.module.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import net.lab1024.sa.base.common.entity.BaseEntity;

/**
 * 消费审计日志实体
 * 记录所有消费相关操作的详细审计信息
 * 满足企业级安全合规和审计要求
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */

public class ConsumeAuditLogEntity extends BaseEntity {

    /**
     * 审计日志ID
     */
    private Long auditLogId;

    /**
     * 人员ID
     */
    private Long personId;

    /**
     * 操作类型
     * CONSUME - 消费
     * REFUND - 退款
     * RECHARGE - 充值
     * PERMISSION_CHECK - 权限检查
     * SECURITY_EVENT - 安全事件
     * ACCOUNT_FREEZE - 账户冻结
     * ACCOUNT_UNFREEZE - 账户解冻
     * PASSWORD_RESET - 密码重置
     */
    private String operationType;

    /**
     * 操作金额（可选）
     */
    private BigDecimal operationAmount;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 订单号（可选）
     */
    private String orderNo;

    /**
     * 操作结果
     * SUCCESS - 成功
     * FAILED - 失败
     * PARTIAL - 部分成功
     * TIMEOUT - 超时
     * CANCELLED - 已取消
     */
    private String operationResult;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 操作详细信息（JSON格式）
     */
    private String operationDetails;

    /**
     * 风险等级
     * LOW - 低风险
     * MEDIUM - 中等风险
     * HIGH - 高风险
     * CRITICAL - 严重风险
     */
    private String riskLevel;

    /**
     * 是否需要人工审核
     */
    private Boolean needReview;

    /**
     * 审核状态
     * PENDING - 待审核
     * APPROVED - 已审核
     * REJECTED - 已拒绝
     */
    private String reviewStatus;

    /**
     * 审核人ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;

    /**
     * 审核备注
     */
    private String reviewComment;

    /**
     * 关联的安全事件ID
     */
    private Long securityEventId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 请求ID（用于追踪分布式请求）
     */
    private String requestId;

    /**
     * 业务模块
     */
    private String businessModule;

    /**
     * 异常信息（如果有）
     */
    private String exceptionInfo;

    // Note: createUserId, createTime, updateTime, updateUserId, deletedFlag, version
    // are inherited from BaseEntity, do not redefine them

    /**
     * 检查是否为高风险操作
     *
     * @return 是否高风险
     */
    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /**
     * 检查是否需要审核
     *
     * @return 是否需要审核
     */
    public boolean needsReview() {
        return Boolean.TRUE.equals(needReview) && "PENDING".equals(reviewStatus);
    }

    /**
     * 检查操作是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(operationResult);
    }

    /**
     * 检查是否为敏感操作
     *
     * @return 是否敏感
     */
    public boolean isSensitiveOperation() {
        return "ACCOUNT_FREEZE".equals(operationType) ||
               "ACCOUNT_UNFREEZE".equals(operationType) ||
               "PASSWORD_RESET".equals(operationType) ||
               "SECURITY_EVENT".equals(operationType);
    }
}