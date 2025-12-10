package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 移动端扫码消费请求DTO
 * 基于二维码的扫码消费请求数据结构
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端扫码消费请求")
public class MobileScanConsumeRequestDTO {

    @Schema(description = "二维码内容", example = "QR_CONSUME_MERCHANT_001_AREA_001")
    @NotBlank(message = "二维码内容不能为空")
    private String qrCode;

    @Schema(description = "消费金额", example = "25.00")
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    @Schema(description = "消费类型", example = "DINING")
    private String consumeType;

    @Schema(description = "消费描述", example = "午餐消费")
    private String description;

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "扫码时间", example = "2025-01-30 12:30:00")
    private java.time.LocalDateTime scanTime;

    @Schema(description = "二维码类型", example = "CONSUME")
    private String qrType;

    @Schema(description = "二维码生成时间", example = "2025-01-30 08:00:00")
    private java.time.LocalDateTime qrGenerateTime;

    @Schema(description = "二维码过期时间", example = "2025-01-30 18:00:00")
    private java.time.LocalDateTime qrExpireTime;

    @Schema(description = "是否验证二维码有效性", example = "true")
    private Boolean validateQRCode = true;

    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "是否需要密码验证", example = "false")
    private Boolean requirePassword = false;

    @Schema(description = "支付密码", example = "123456")
    private String paymentPassword;

    @Schema(description = "是否使用积分", example = "false")
    private Boolean usePoints = false;

    @Schema(description = "使用积分数量", example = "100")
    private Integer pointsUsed = 0;

    @Schema(description = "是否使用优惠券", example = "false")
    private Boolean useCoupon = false;

    @Schema(description = "优惠券ID", example = "1001")
    private Long couponId;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "地理位置经度", example = "116.404")
    private Double longitude;

    @Schema(description = "地理位置纬度", example = "39.915")
    private Double latitude;

    @Schema(description = "地理位置描述", example = "北京市朝阳区")
    private String locationDescription;

    @Schema(description = "是否开启定位验证", example = "false")
    private Boolean enableLocationValidation = false;

    @Schema(description = "备注", example = "扫码支付")
    private String remark;

    @Schema(description = "扩展信息")
    private Object extendedInfo;

    // 二维码类型枚举
    public static final String QR_TYPE_CONSUME = "CONSUME";        // 消费二维码
    public static final String QR_TYPE_MERCHANT = "MERCHANT";      // 商户二维码
    public static final String QR_TYPE_AREA = "AREA";              // 区域二维码
    public static final String QR_TYPE_DEVICE = "DEVICE";          // 设备二维码
    public static final String QR_TYPE_PROMOTION = "PROMOTION";    // 促销二维码
}