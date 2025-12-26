package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 补贴计算DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@Schema(description = "补贴计算参数")
public class SubsidyCalculationDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "消费金额")
    private BigDecimal consumeAmount;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "餐别 1-早餐 2-午餐 3-晚餐")
    private Integer mealType;

    @Schema(description = "消费记录ID")
    private Long consumeId;

    @Schema(description = "补贴类型 1-餐补 2-交通补 3-其他")
    private Integer subsidyType;

    @Schema(description = "用户组ID")
    private Long userGroupId;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "区域ID")
    private Long areaId;
}
