package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能推荐VO
 *
 * @Author: IOE-DREAM Team
 * @Date: 2025-12-24
 * @Copyright: IOE-DREAM智慧园区一卡通管理平台
 */
@Data
@Schema(description = "智能推荐")
public class SmartRecommendationVO {

    @Schema(description = "推荐类型: ordering-订餐, recharge-充值, discount-优惠, vip-会员")
    private String recommendType;

    @Schema(description = "推荐图标")
    private String icon;

    @Schema(description = "推荐标题")
    private String title;

    @Schema(description = "推荐描述")
    private String description;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "优先级（数字越小优先级越高）")
    private Integer priority;

    @Schema(description = "推荐理由")
    private String reason;

    @Schema(description = "是否可操作")
    private Boolean actionable;
}
