package net.lab1024.sa.admin.module.consume.domain.request;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 消费请求对象
 * 核心消费引擎的统一请求参数
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
public class ConsumeRequest {

    /**
     * 人员ID（必填）
     */
    @NotNull(message = "人员ID不能为空")
    private Long personId;

    /**
     * 人员姓名（必填）
     */
    @NotBlank(message = "人员姓名不能为空")
    @Size(max = 64, message = "人员姓名长度不能超过64个字符")
    private String personName;

    /**
     * 消费金额（必填，必须大于0）
     */
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    /**
     * 消费模式（必填）
     * 支持的模式：FIXED_AMOUNT, FREE_AMOUNT, METERING, PRODUCT, ORDERING, SMART
     */
    @NotBlank(message = "消费模式不能为空")
    private String consumptionMode;

    /**
     * 支付方式（必填）
     * 支持的方式：CARD, WALLET, CASH
     */
    @NotBlank(message = "支付方式不能为空")
    private String payMethod;

    /**
     * 设备ID（必填）
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 设备名称（可选）
     */
    private String deviceName;

    /**
     * 区域ID（可选）
     */
    private String regionId;

    /**
     * 区域名称（可选）
     */
    private String regionName;

    /**
     * 订单号（可选，不传则自动生成）
     */
    private String orderNo;

    /**
     * 币种（默认CNY）
     */
    private String currency = "CNY";

    /**
     * 账户类型（可选）
     */
    private String accountType;

    /**
     * 消费类型（兼容旧系统）
     */
    private String consumeType;

    /**
     * 模式配置JSON（可选）
     */
    private String modeConfig;

    /**
     * 扩展数据JSON（可选）
     */
    private String extendData;

    /**
     * 客户端IP地址
     */
    private String clientIp;

    /**
     * 备注（可选）
     */
    @Size(max = 512, message = "备注长度不能超过512个字符")
    private String remark;
}