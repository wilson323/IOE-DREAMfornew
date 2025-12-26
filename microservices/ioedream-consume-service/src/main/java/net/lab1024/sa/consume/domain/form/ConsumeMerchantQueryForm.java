package net.lab1024.sa.consume.domain.form;

import lombok.Data;

import net.lab1024.sa.common.domain.form.BaseQueryForm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费商户查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费商户查询表单")
public class ConsumeMerchantQueryForm extends BaseQueryForm {

    @Schema(description = "商户ID", example = "2001")
    private Long merchantId;

    @Schema(description = "商户名称", example = "一楼餐厅")
    private String merchantName;

    @Schema(description = "商户编码", example = "MERCHANT001")
    private String merchantCode;

    @Schema(description = "商户类型", example = "RESTAURANT")
    private String merchantType;

    @Schema(description = "所属区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "负责人", example = "李经理")
    private String managerName;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "商户状态", example = "ACTIVE")
    private String status;

    @Schema(description = "关键词搜索", example = "餐厅")
    private String keyword;
}