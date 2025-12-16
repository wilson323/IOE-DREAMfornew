package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 移动端消费类型VO
 * 移动端消费类型选择项
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端消费类型")
public class MobileConsumeTypeVO {

    @Schema(description = "消费类型代码", example = "DINING")
    private String code;

    @Schema(description = "消费类型名称", example = "餐饮")
    private String name;

    @Schema(description = "图标", example = "🍽️")
    private String icon;

    @Schema(description = "描述", example = "食堂用餐消费")
    private String description;

    @Schema(description = "是否可用", example = "true")
    private Boolean available;

    @Schema(description = "排序权重", example = "1")
    private Integer sortOrder;

    @Schema(description = "分类", example = "FOOD")
    private String category;

    @Schema(description = "分类名称", example = "餐饮类")
    private String categoryName;

    @Schema(description = "最低消费金额", example = "0.01")
    private java.math.BigDecimal minAmount;

    @Schema(description = "最高消费金额", example = "9999.99")
    private java.math.BigDecimal maxAmount;

    @Schema(description = "是否需要密码", example = "true")
    private Boolean requirePassword;

    @Schema(description = "是否支持分期", example = "false")
    private Boolean supportInstallment;

    @Schema(description = "最大分期期数", example = "0")
    private Integer maxInstallments;

    @Schema(description = "费率", example = "0.00")
    private java.math.BigDecimal rate;

    @Schema(description = "折扣率", example = "1.00")
    private java.math.BigDecimal discountRate;

    @Schema(description = "积分倍数", example = "1")
    private Integer pointsMultiplier;

    @Schema(description = "是否支持优惠券", example = "true")
    private Boolean supportCoupon;

    @Schema(description = "支持时间段", example = "06:00-22:00")
    private String availableTimeRange;

    @Schema(description = "适用商户", example = "食堂一楼、食堂二楼")
    private String applicableMerchants;

    @Schema(description = "适用设备", example = "POS机、扫码枪")
    private String applicableDevices;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    @Schema(description = "状态描述", example = "启用")
    private String statusDescription;

    @Schema(description = "父级类型", example = "FOOD")
    private String parentType;

    @Schema(description = "子类型列表")
    private java.util.List<MobileConsumeTypeVO> subTypes;

    @Schema(description = "扩展属性", example = "{}")
    private Object extendedAttributes;
}


