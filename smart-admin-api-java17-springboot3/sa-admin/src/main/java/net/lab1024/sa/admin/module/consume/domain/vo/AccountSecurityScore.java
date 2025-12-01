package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 账户安全评分VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户安全评分")
public class AccountSecurityScore {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "总评分")
    private Integer totalScore;

    @Schema(description = "密码评分")
    private Integer passwordScore;

    @Schema(description = "行为评分")
    private Integer behaviorScore;

    @Schema(description = "设备评分")
    private Integer deviceScore;

    @Schema(description = "位置评分")
    private Integer locationScore;

    @Schema(description = "评分时间")
    private LocalDateTime scoreTime;

    @Schema(description = "评分等级：1-优秀，2-良好，3-一般，4-较差")
    private Integer scoreLevel;

    // 手动添加的getter/setter方法 (Lombok失效备用)
















}
