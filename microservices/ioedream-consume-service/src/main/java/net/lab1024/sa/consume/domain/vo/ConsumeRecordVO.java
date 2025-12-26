package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费记录视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消费记录视图对象")
public class ConsumeRecordVO {

    @Schema(description = "记录ID", example = "1001")
    private Long recordId;

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "用户编号", example = "EMP001")
    private String userCode;

    @Schema(description = "设备ID", example = "POS001")
    private String deviceId;

    @Schema(description = "设备名称", example = "一楼餐厅POS机")
    private String deviceName;

    @Schema(description = "商户ID", example = "2001")
    private Long merchantId;

    @Schema(description = "商户名称", example = "一楼餐厅")
    private String merchantName;

    @Schema(description = "消费金额", example = "25.50")
    private BigDecimal amount;

    @Schema(description = "消费类型", example = "MEAL")
    private String consumeType;

    @Schema(description = "消费类型名称", example = "餐饮")
    private String consumeTypeName;

    @Schema(description = "商品明细", example = "红烧牛肉饭+可乐")
    private String productDetail;

    @Schema(description = "支付方式", example = "BALANCE")
    private String paymentMethod;

    @Schema(description = "支付方式名称", example = "余额支付")
    private String paymentMethodName;

    @Schema(description = "订单号", example = "ORDER202512210001")
    private String orderNo;

    @Schema(description = "交易流水号", example = "TXN202512210001")
    private String transactionNo;

    @Schema(description = "消费地点", example = "一楼餐厅")
    private String consumeLocation;

    @Schema(description = "消费时间", example = "2025-12-21T12:30:00")
    private LocalDateTime consumeTime;

    @Schema(description = "交易状态", example = "SUCCESS")
    private String status;

    @Schema(description = "交易状态名称", example = "交易成功")
    private String statusName;

    @Schema(description = "退款状态", example = "NO_REFUND")
    private String refundStatus;

    @Schema(description = "退款状态名称", example = "未退款")
    private String refundStatusName;

    @Schema(description = "退款金额", example = "0.00")
    private BigDecimal refundAmount;

    @Schema(description = "账户余额（交易前）", example = "284.00")
    private BigDecimal balanceBefore;

    @Schema(description = "账户余额（交易后）", example = "258.50")
    private BigDecimal balanceAfter;

    @Schema(description = "创建时间", example = "2025-12-21T12:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T12:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "午餐")
    private String remark;

    @Schema(description = "操作人", example = "系统")
    private String operator;
}