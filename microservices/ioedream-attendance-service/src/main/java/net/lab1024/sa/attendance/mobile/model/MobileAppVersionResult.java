package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端应用版本结果
 * <p>
 * 封装移动端应用版本响应结果
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
@Schema(description = "移动端应用版本结果")
public class MobileAppVersionResult {

    /**
     * 当前版本
     */
    @Schema(description = "当前版本", example = "2.1.0")
    private String currentVersion;

    /**
     * 最新版本
     */
    @Schema(description = "最新版本", example = "2.1.0")
    private String latestVersion;

    /**
     * 是否需要更新
     */
    @Schema(description = "是否需要更新", example = "false")
    private Boolean updateRequired;
}
