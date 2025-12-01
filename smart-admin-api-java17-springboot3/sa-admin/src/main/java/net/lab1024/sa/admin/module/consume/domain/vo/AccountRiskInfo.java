package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户风险信息VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户风险信息")
public class AccountRiskInfo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "风险等级：1-低风险，2-中风险，3-高风险")
    private Integer riskLevel;

    @Schema(description = "风险评分")
    private Integer riskScore;

    @Schema(description = "风险因素列表")
    private List<String> riskFactors;

    @Schema(description = "风险描述")
    private String riskDescription;

    @Schema(description = "建议措施")
    private List<String> recommendations;

    @Schema(description = "评估时间")
    private LocalDateTime assessmentTime;

    // 手动添加的getter/setter方法 (Lombok失效备用)














}
