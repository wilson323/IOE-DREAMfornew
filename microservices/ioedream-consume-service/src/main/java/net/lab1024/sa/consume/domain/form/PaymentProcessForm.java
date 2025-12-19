package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 支付处理表单
 * <p>
 * 企业级支付处理请求表单，支持多种支付方式和业务场景
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class PaymentProcessForm {

    /**
     * 订单号（可选，为空则自动生成）
     */
    @Size(max = 100, message = "订单号长度不能超过100个字符")
    private String orderNo;

    /**
     * 账户编号（可选，用于兼容旧方法）
     */
    @Size(max = 100, message = "账户编号长度不能超过100个字符")
    private String accountNo;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    /**
     * 支付方式：1-余额支付 2-微信支付 3-支付宝 4-银行卡 5-现金 6-二维码 7- NFC 8-生物识别
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    /**
     * 支付渠道：1-线上 2-线下 3-移动端 4-自助设备 5-POS机
     */
    @NotNull(message = "支付渠道不能为空")
    private Integer paymentChannel;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal paymentAmount;

    /**
     * 优惠金额
     */
    @DecimalMin(value = "0", message = "优惠金额不能小于0")
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 业务类型：1-消费 2-充值 3-退款 4-转账 5-提现 6-补贴 7-罚款
     */
    @NotNull(message = "业务类型不能为空")
    private Integer businessType;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 商户名称
     */
    @Size(max = 200, message = "商户名称长度不能超过200个字符")
    private String merchantName;

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 100, message = "设备ID长度不能超过100个字符")
    private String deviceId;

    /**
     * 设备编码
     */
    @Size(max = 100, message = "设备编码长度不能超过100个字符")
    private String deviceCode;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    @Size(max = 200, message = "区域名称长度不能超过200个字符")
    private String areaName;

    /**
     * 商品信息（JSON格式）
     */
    @Size(max = 2000, message = "商品信息长度不能超过2000个字符")
    private String productInfo;

    /**
     * 消费描述
     */
    @Size(max = 500, message = "消费描述长度不能超过500个字符")
    private String consumeDescription;

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
     * 位置信息（纬度）
     */
    private Double latitude;

    /**
     * 位置信息（经度）
     */
    private Double longitude;

    /**
     * 位置描述
     */
    @Size(max = 200, message = "位置描述长度不能超过200个字符")
    private String locationDescription;

    /**
     * 扩展参数（JSON格式）
     */
    @Size(max = 1000, message = "扩展参数长度不能超过1000个字符")
    private String extendedParams;

    /**
     * 支付密码（余额支付时使用）
     */
    @Size(max = 100, message = "支付密码长度不能超过100个字符")
    private String paymentPassword;

    /**
     * 生物验证数据（生物识别支付时使用）
     */
    @Size(max = 1000, message = "生物验证数据长度不能超过1000个字符")
    private String biometricData;

    /**
     * 第三方支付参数（第三方支付时使用，JSON格式）
     */
    @Size(max = 2000, message = "第三方支付参数长度不能超过2000个字符")
    private String thirdPartyParams;

    /**
     * 是否需要验证风险
     */
    private Boolean requireRiskValidation;

    /**
     * 是否需要审核
     */
    private Boolean requireAudit;

    /**
     * 回调地址
     */
    @Size(max = 500, message = "回调地址长度不能超过500个字符")
    private String callbackUrl;

    /**
     * 通知地址
     */
    @Size(max = 500, message = "通知地址长度不能超过500个字符")
    private String notifyUrl;

    /**
     * 扩展时间戳（客户端时间）
     */
    private Long clientTimestamp;
}
