package net.lab1024.sa.admin.module.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录VO
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
public class RechargeRecordVO {

    /**
     * 充值记录ID
     */
    private Long rechargeId;

    /**
     * 充值编号
     */
    private String rechargeNo;

    /**
     * 外部充值编号
     */
    private String externalRechargeNo;

    /**
     * 充值类型代码
     */
    private String rechargeTypeCode;

    /**
     * 充值类型名称
     */
    private String rechargeTypeName;

    /**
     * 账户ID
     */
    private Long accountId;

    /**
     * 账户编号
     */
    private String accountNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 用户手机号
     */
    private String userPhone;

    /**
     * 用户卡号
     */
    private String userCardNo;

    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;

    /**
     * 实际到账金额
     */
    private BigDecimal actualAmount;

    /**
     * 手续费
     */
    private BigDecimal serviceFee;

    /**
     * 赠送金额
     */
    private BigDecimal bonusAmount;

    /**
     * 充值前余额
     */
    private BigDecimal beforeBalance;

    /**
     * 充值后余额
     */
    private BigDecimal afterBalance;

    /**
     * 支付方式代码
     */
    private String paymentMethodCode;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 第三方交易号
     */
    private String thirdPartyTransactionNo;

    /**
     * 第三方平台
     */
    private String thirdPartyPlatform;

    /**
     * 充值渠道
     */
    private String rechargeChannel;

    /**
     * 充值状态代码
     */
    private String statusCode;

    /**
     * 充值状态名称
     */
    private String statusName;

    /**
     * 充值时间
     */
    private LocalDateTime rechargeTime;

    /**
     * 到账时间
     */
    private LocalDateTime arrivalTime;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 失败时间
     */
    private LocalDateTime failureTime;

    /**
     * 充值网点
     */
    private String rechargeStation;

    /**
     * 充值设备
     */
    private String rechargeDevice;

    /**
     * 充值地点
     */
    private String rechargeLocation;

    /**
     * 充值备注
     */
    private String remarks;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 退款状态代码
     */
    private String refundStatusCode;

    /**
     * 退款状态名称
     */
    private String refundStatusName;

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
     * 退款编号
     */
    private String refundNo;

    /**
     * 操作员ID
     */
    private Long operatorId;

    /**
     * 操作员姓名
     */
    private String operatorName;

    /**
     * 审核状态
     */
    private Integer auditStatus;

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
     * 审核备注
     */
    private String auditRemarks;

    /**
     * 异常标志
     */
    private Boolean isAbnormal;

    /**
     * 异常描述
     */
    private String abnormalDescription;

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
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    private Integer deletedFlag;
}