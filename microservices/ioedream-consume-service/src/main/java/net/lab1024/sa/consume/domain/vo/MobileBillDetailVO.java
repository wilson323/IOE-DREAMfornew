package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 移动端账单详情VO
 * 移动端消费账单详细信息展示
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端账单详情")
public class MobileBillDetailVO {

    @Schema(description = "订单ID", example = "MOBILE_20250130001")
    private String orderId;

    @Schema(description = "账单号", example = "BILL2025013000001")
    private String billNumber;

    @Schema(description = "交易流水号", example = "TXN202501301430001")
    private String transactionNumber;

    @Schema(description = "消费金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "原始金额", example = "55.00")
    private BigDecimal originalAmount;

    @Schema(description = "优惠金额", example = "5.00")
    private BigDecimal discountAmount;

    @Schema(description = "手续费", example = "0.00")
    private BigDecimal fee;

    @Schema(description = "积分抵扣", example = "0.00")
    private BigDecimal pointsDeduction;

    @Schema(description = "实际支付金额", example = "50.00")
    private BigDecimal actualAmount;

    @Schema(description = "消费类型", example = "DINING")
    private String consumeType;

    @Schema(description = "消费类型名称", example = "餐饮")
    private String consumeTypeName;

    @Schema(description = "消费类型图标", example = "🍽️")
    private String consumeTypeIcon;

    @Schema(description = "消费描述", example = "午餐消费")
    private String description;

    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @Schema(description = "商户名称", example = "食堂一楼")
    private String merchantName;

    @Schema(description = "商户地址", example = "公司园区A栋1楼")
    private String merchantAddress;

    @Schema(description = "商户电话", example = "010-12345678")
    private String merchantPhone;

    @Schema(description = "商户图标", example = "https://example.com/merchant.jpg")
    private String merchantIcon;

    @Schema(description = "消费时间", example = "2025-01-30 12:30:00")
    private LocalDateTime consumeTime;

    @Schema(description = "支付时间", example = "2025-01-30 12:30:05")
    private LocalDateTime paymentTime;

    @Schema(description = "完成时间", example = "2025-01-30 12:30:10")
    private LocalDateTime completeTime;

    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    @Schema(description = "账户编号", example = "ACC1001")
    private String accountNumber;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg")
    private String userAvatar;

    @Schema(description = "支付方式", example = "BALANCE")
    private String paymentMethod;

    @Schema(description = "支付方式描述", example = "余额支付")
    private String paymentMethodDescription;

    @Schema(description = "支付方式图标", example = "💳")
    private String paymentMethodIcon;

    @Schema(description = "支付状态", example = "SUCCESS")
    private String status;

    @Schema(description = "状态描述", example = "支付成功")
    private String statusDescription;

    @Schema(description = "状态图标", example = "✅")
    private String statusIcon;

    @Schema(description = "设备ID", example = "POS_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "POS机001")
    private String deviceName;

    @Schema(description = "设备类型", example = "POS")
    private String deviceType;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "区域名称", example = "食堂区域")
    private String areaName;

    @Schema(description = "消费地点", example = "食堂一楼")
    private String location;

    @Schema(description = "地理位置经度", example = "116.404")
    private Double longitude;

    @Schema(description = "地理位置纬度", example = "39.915")
    private Double latitude;

    @Schema(description = "获得积分", example = "5")
    private Integer pointsEarned;

    @Schema(description = "积分倍数", example = "1")
    private Integer pointsMultiplier;

    @Schema(description = "使用的优惠券ID", example = "1001")
    private Long couponId;

    @Schema(description = "优惠券描述", example = "满50减5")
    private String couponDescription;

    @Schema(description = "优惠券金额", example = "5.00")
    private BigDecimal couponAmount;

    @Schema(description = "是否可退款", example = "true")
    private Boolean refundable;

    @Schema(description = "退款截止时间", example = "2025-01-31 12:30:00")
    private LocalDateTime refundDeadline;

    @Schema(description = "已退款金额", example = "0.00")
    private BigDecimal refundedAmount;

    @Schema(description = "退款次数", example = "0")
    private Integer refundCount;

    @Schema(description = "账单来源", example = "移动端")
    private String source;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "备注", example = "员工午餐")
    private String remark;

    @Schema(description = "创建时间", example = "2025-01-30 12:30:00")
    private LocalDateTime createTime;

    @Schema(description = "最后更新时间", example = "2025-01-30 12:30:10")
    private LocalDateTime updateTime;

    @Schema(description = "账单标签", example = "午餐,工作日")
    private List<String> tags;

    @Schema(description = "账单详情图片")
    private List<String> images;

    @Schema(description = "电子票据URL", example = "https://example.com/receipt/123")
    private String receiptUrl;

    @Schema(description = "发票信息")
    private InvoiceInfo invoiceInfo;

    /**
     * 发票信息内部类
     */
    @Data
    @Schema(description = "发票信息")
    public static class InvoiceInfo {
        @Schema(description = "发票ID", example = "INV2025013000001")
        private String invoiceId;

        @Schema(description = "发票类型", example = "ELECTRONIC")
        private String invoiceType;

        @Schema(description = "发票抬头", example = "北京某某科技有限公司")
        private String invoiceTitle;

        @Schema(description = "纳税人识别号", example = "91110105MA12345678")
        private String taxNumber;

        @Schema(description = "发票金额", example = "50.00")
        private BigDecimal invoiceAmount;

        @Schema(description = "开票时间", example = "2025-01-30 12:35:00")
        private LocalDateTime invoiceTime;

        @Schema(description = "发票状态", example = "ISSUED")
        private String status;

        @Schema(description = "下载URL", example = "https://example.com/invoice/123")
        private String downloadUrl;
    }
}


