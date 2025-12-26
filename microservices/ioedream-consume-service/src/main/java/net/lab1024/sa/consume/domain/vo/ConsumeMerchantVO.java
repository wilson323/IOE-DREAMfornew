package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费商户视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@Schema(description = "消费商户视图对象")
public class ConsumeMerchantVO {

    @Schema(description = "商户ID", example = "2001")
    private Long merchantId;

    @Schema(description = "商户名称", example = "一楼餐厅")
    private String merchantName;

    @Schema(description = "商户编码", example = "MERCHANT001")
    private String merchantCode;

    @Schema(description = "商户类型", example = "RESTAURANT")
    private String merchantType;

    @Schema(description = "商户类型名称", example = "餐厅")
    private String merchantTypeName;

    @Schema(description = "所属区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "所属区域名称", example = "A栋一楼")
    private String areaName;

    @Schema(description = "负责人", example = "李经理")
    private String managerName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "商户地址", example = "A栋一楼大厅")
    private String address;

    @Schema(description = "营业开始时间", example = "07:00")
    private String businessStartHour;

    @Schema(description = "营业结束时间", example = "21:00")
    private String businessEndHour;

    @Schema(description = "结算方式", example = "DAILY")
    private String settlementMethod;

    @Schema(description = "结算方式名称", example = "日结")
    private String settlementMethodName;

    @Schema(description = "费率", example = "0.02")
    private BigDecimal commissionRate;

    @Schema(description = "商户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "商户状态名称", example = "营业中")
    private String statusName;

    @Schema(description = "创建时间", example = "2025-01-01T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "主营中式快餐")
    private String remark;

    @Schema(description = "今日交易笔数", example = "156")
    private Integer todayTransactionCount;

    @Schema(description = "今日交易金额", example = "4680.50")
    private BigDecimal todayTransactionAmount;

    @Schema(description = "本月交易笔数", example = "3256")
    private Integer monthTransactionCount;

    @Schema(description = "本月交易金额", example = "92580.00")
    private BigDecimal monthTransactionAmount;

    @Schema(description = "设备数量", example = "3")
    private Integer deviceCount;
}