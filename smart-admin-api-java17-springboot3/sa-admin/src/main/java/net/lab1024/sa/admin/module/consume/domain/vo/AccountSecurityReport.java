package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户安全报告VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户安全报告")
public class AccountSecurityReport {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "报告生成时间")
    private LocalDateTime reportTime;

    @Schema(description = "安全等级")
    private Integer securityLevel;

    @Schema(description = "安全评分")
    private Integer securityScore;

    @Schema(description = "风险事件数量")
    private Integer riskEventCount;

    @Schema(description = "安全建议列表")
    private List<String> suggestions;

    @Schema(description = "最近安全事件列表")
    private List<AccountSecurityEvent> recentEvents;

    // 手动添加的getter/setter方法 (Lombok失效备用)














}
