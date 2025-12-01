package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 自动冻结规则VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "自动冻结规则")
public class AutoFreezeRule {

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "单日消费次数限制")
    private Integer dailyCountLimit;

    @Schema(description = "单日消费金额限制")
    private BigDecimal dailyAmountLimit;

    @Schema(description = "连续失败次数限制")
    private Integer consecutiveFailLimit;

    @Schema(description = "异常行为次数限制")
    private Integer abnormalBehaviorLimit;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    // 手动添加的getter/setter方法 (Lombok失效备用)














}
