package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 移动端健康检查结果
 * <p>
 * 封装移动端健康检查响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端健康检查结果")
public class MobileHealthCheckResult {

    /**
     * 健康状态
     */
    @Schema(description = "健康状态", example = "HEALTHY", allowableValues = {"HEALTHY", "DEGRADED", "UNHEALTHY"})
    private String status;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "系统运行正常")
    private String message;

    /**
     * 检查时间
     */
    @Schema(description = "检查时间", example = "2025-01-30T10:00:00")
    private java.time.LocalDateTime checkTime;

    /**
     * 健康信息
     */
    @Schema(description = "健康信息")
    private Map<String, Object> healthInfo;
}
