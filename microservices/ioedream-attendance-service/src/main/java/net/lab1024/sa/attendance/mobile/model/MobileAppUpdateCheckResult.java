package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端应用更新检查结果
 * <p>
 * 封装移动端应用更新检查响应结果
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
@Schema(description = "移动端应用更新检查结果")
public class MobileAppUpdateCheckResult {

    /**
     * 是否有更新
     */
    @Schema(description = "是否有更新", example = "false")
    private Boolean hasUpdate;

    /**
     * 最新版本
     */
    @Schema(description = "最新版本", example = "2.1.0")
    private String latestVersion;

    /**
     * 更新URL
     */
    @Schema(description = "更新URL", example = "http://example.com/app/update.apk")
    private String updateUrl;

    /**
     * 更新说明
     */
    @Schema(description = "更新说明", example = "修复了一些已知问题")
    private String updateDescription;

    /**
     * 是否强制更新
     */
    @Schema(description = "是否强制更新", example = "false")
    private Boolean forceUpdate;
}
