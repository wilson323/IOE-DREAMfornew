package net.lab1024.sa.consume.domain.form;

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费商户更新表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费商户更新表单")
public class ConsumeMerchantUpdateForm {

    @Schema(description = "商户名称", example = "一楼餐厅")
    private String merchantName;

    @Schema(description = "商户类型", example = "RESTAURANT")
    private String merchantType;

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

    @Schema(description = "费率", example = "0.02")
    private java.math.BigDecimal commissionRate;

    @Schema(description = "商户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "备注", example = "主营中式快餐")
    private String remark;
}