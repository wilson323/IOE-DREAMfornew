package net.lab1024.sa.consume.consume.domain.form;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * 二维码消费表单
 * <p>
 * 企业级二维码消费请求表单，支持标准JSON格式二维码解析
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class QrCodeConsumeForm {

    /**
     * 二维码内容（JSON格式或加密字符串）
     */
    @NotBlank(message = "二维码内容不能为空")
    @Size(max = 2000, message = "二维码内容长度不能超过2000个字符")
    private String qrContent;

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 100, message = "设备ID长度不能超过100个字符")
    private String deviceId;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 100, message = "设备编码长度不能超过100个字符")
    private String deviceCode;

    /**
     * 消费金额
     */
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal consumeAmount;

    /**
     * 消费类型：1-餐费 2-购物 3-服务 4-其他
     */
    @NotNull(message = "消费类型不能为空")
    private Integer consumeType;

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
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    @Size(max = 200, message = "区域名称长度不能超过200个字符")
    private String areaName;

    /**
     * 消费描述
     */
    @Size(max = 500, message = "消费描述长度不能超过500个字符")
    private String consumeDescription;

    /**
     * 生物验证数据（可选）
     */
    private String biometricData;

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
     * 交易时间戳（客户端时间）
     */
    private Long clientTimestamp;

    /**
     * 扩展参数（JSON格式）
     */
    @Size(max = 1000, message = "扩展参数长度不能超过1000个字符")
    private String extendedParams;
}



