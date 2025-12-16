package net.lab1024.sa.consume.consume.domain.form;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/**
 * 退款申请表单
 * <p>
 * 企业级退款申请请求表单，支持多种退款场景和审核流程
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class RefundApplyForm {

    /**
     * 原支付记录ID
     */
    @NotBlank(message = "原支付记录ID不能为空")
    @Size(max = 100, message = "原支付记录ID长度不能超过100个字符")
    private String paymentId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 退款方式：1-原路退回 2-余额退款 3-银行卡退款 4-现金退款 5-人工退款
     */
    @NotNull(message = "退款方式不能为空")
    private Integer refundMethod;

    /**
     * 退款类型：1-全额退款 2-部分退款 3-订单取消 4-商品退货 5-服务退款 6-其他
     */
    @NotNull(message = "退款类型不能为空")
    private Integer refundType;

    /**
     * 退款原因：1-用户申请 2-商户退款 3-系统异常 4-风控拦截 5-其他
     */
    @NotNull(message = "退款原因不能为空")
    private Integer refundReasonType;

    /**
     * 退款原因描述
     */
    @NotBlank(message = "退款原因描述不能为空")
    @Size(max = 1000, message = "退款原因描述长度不能超过1000个字符")
    private String refundReasonDesc;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人类型：1-用户 2-商户 3-管理员 4-系统
     */
    private Integer applicantType;

    /**
     * 是否自动审核
     */
    private Boolean autoAudit;

    /**
     * 退款手续费
     */
    @DecimalMin(value = "0", message = "退款手续费不能小于0")
    private BigDecimal refundFee;

    /**
     * 银行卡信息（银行卡退款时使用）
     */
    @Size(max = 500, message = "银行卡信息长度不能超过500个字符")
    private String bankCardInfo;

    /**
     * 退款理由详细说明
     */
    @Size(max = 2000, message = "退款理由详细说明长度不能超过2000个字符")
    private String detailedReason;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    private String contactEmail;

    /**
     * 附件信息（JSON格式，存储相关凭证图片、文档等）
     */
    @Size(max = 2000, message = "附件信息长度不能超过2000个字符")
    private String attachmentInfo;

    /**
     * 紧急程度：1-普通 2-紧急 3-非常紧急
     */
    private Integer urgency;

    /**
     * 预期退款时间
     */
    private Long expectedRefundTime;

    /**
     * 备注
     */
    @Size(max = 1000, message = "备注长度不能超过1000个字符")
    private String remark;

    /**
     * 客户端IP地址
     */
    @Size(max = 45, message = "客户端IP地址长度不能超过45个字符")
    private String clientIp;

    /**
     * 用户代理
     */
    @Size(max = 500, message = "用户代理长度不能超过500个字符")
    private String userAgent;

    /**
     * 扩展参数（JSON格式）
     */
    @Size(max = 1000, message = "扩展参数长度不能超过1000个字符")
    private String extendedParams;

    /**
     * 请求时间戳（客户端时间）
     */
    private Long clientTimestamp;
}



