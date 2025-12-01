package net.lab1024.sa.admin.module.consume.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 限制冲突检查结果VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "限制冲突检查结果")
public class LimitConflictCheckResult {

    @Schema(description = "是否有冲突")
    private Boolean hasConflict;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "冲突的限制列表")
    private List<ConflictInfo> conflicts;

    @Schema(description = "检查时间")
    private LocalDateTime checkTime;

    @Schema(description = "建议解决方式")
    private List<String> suggestions;

    @Schema(description = "严重程度")
    private String severity;

    @Schema(description = "消息")
    private String message;

    /**
     * 检查是否有冲突
     */
    public boolean hasConflicts() {
        return Boolean.TRUE.equals(hasConflict) &&
                conflicts != null && !conflicts.isEmpty();
    }

    /**
     * 获取冲突数量
     */

    /**
     * 检查是否为严重冲突
     */
    public boolean isSevere() {
        return "HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity);
    }

    /**
     * 获取严重程度的数值
     */

    /**
     * 创建无冲突结果
     */
    public static LimitConflictCheckResult noConflict(Long userId) {
        return LimitConflictCheckResult.builder()
                .hasConflict(false)
                .userId(userId)
                .severity("LOW")
                .message("无限制冲突")
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建有冲突结果
     */
    public static LimitConflictCheckResult hasConflict(Long userId, List<ConflictInfo> conflicts, String severity) {
        return LimitConflictCheckResult.builder()
                .hasConflict(true)
                .userId(userId)
                .conflicts(conflicts)
                .severity(severity)
                .message("检测到限制冲突")
                .suggestions(List.of("请调整限制配置以解决冲突"))
                .checkTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)
    public Boolean isHasConflict() {
        return hasConflict;
    }














    /**
     * 冲突信息
     */
    
    
    
    
    @Schema(description = "冲突信息")
    public static class ConflictInfo {

        @Schema(description = "限制类型1")
        private String limitType1;

        @Schema(description = "限制值1")
        private String limitValue1;

        @Schema(description = "限制类型2")
        private String limitType2;

        @Schema(description = "限制值2")
        private String limitValue2;

        @Schema(description = "冲突描述")
        private String conflictDescription;

        @Schema(description = "严重程度")
        private String conflictSeverity;

        // 手动添加的getter/setter方法 (Lombok失效备用)











    }

}
