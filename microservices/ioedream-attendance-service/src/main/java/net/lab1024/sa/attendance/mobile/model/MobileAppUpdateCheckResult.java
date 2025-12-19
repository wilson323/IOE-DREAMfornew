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
     * 当前版本
     */
    @Schema(description = "当前版本", example = "2.0.0")
    private String currentVersion;

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
     * 下载URL
     */
    @Schema(description = "下载URL", example = "http://example.com/app/download.apk")
    private String downloadUrl;

    /**
     * 是否强制更新
     */
    @Schema(description = "是否强制更新", example = "false")
    private Boolean forceUpdate;

    /**
     * 是否需要更新（兼容字段）
     */
    @Schema(description = "是否需要更新", example = "false")
    private Boolean updateRequired;

    /**
     * 操作是否成功
     */
    @Schema(description = "操作是否成功")
    private Boolean success;

    /**
     * 操作消息
     */
    @Schema(description = "操作消息")
    private String message;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    public boolean isUpdateRequired() {
        return Boolean.TRUE.equals(updateRequired) || Boolean.TRUE.equals(hasUpdate);
    }

    public String getMessage() {
        return message;
    }
}

