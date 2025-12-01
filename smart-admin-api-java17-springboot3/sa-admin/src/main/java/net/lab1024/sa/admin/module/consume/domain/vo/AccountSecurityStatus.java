package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 账户安全状态VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户安全状态")
public class AccountSecurityStatus {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "安全状态：1-安全，2-警告，3-危险，4-冻结")
    private Integer securityStatus;

    @Schema(description = "状态描述")
    private String statusDescription;

    @Schema(description = "最后检查时间")
    private LocalDateTime lastCheckTime;

    @Schema(description = "风险评分")
    private Integer riskScore;

    // 手动添加的getter/setter方法 (Lombok失效备用)










}
