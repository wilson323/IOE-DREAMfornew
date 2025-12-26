package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁全局互锁规则 VO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "门禁全局互锁规则VO")
public class AccessInterlockRuleVO {

    @Schema(description = "互锁规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "互锁类型")
    private String ruleType;

    @Schema(description = "区域A ID")
    private Long areaAId;

    @Schema(description = "区域A名称")
    private String areaAName;

    @Schema(description = "门A ID")
    private Long doorAId;

    @Schema(description = "区域B ID")
    private Long areaBId;

    @Schema(description = "区域B名称")
    private String areaBName;

    @Schema(description = "门B ID")
    private Long doorBId;

    @Schema(description = "互锁模式")
    private String interlockMode;

    @Schema(description = "解锁条件")
    private String unlockCondition;

    @Schema(description = "自动解锁延迟秒数")
    private Integer unlockDelaySeconds;

    @Schema(description = "启用状态")
    private Integer enabled;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
