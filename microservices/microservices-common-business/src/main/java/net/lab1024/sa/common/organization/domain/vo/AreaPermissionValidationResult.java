package net.lab1024.sa.common.organization.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 区域权限配置验证结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Schema(description = "区域权限配置验证结果")
public class AreaPermissionValidationResult {

    @Schema(description = "是否有效")
    private boolean valid;

    @Schema(description = "错误列表")
    private List<String> errors;

    @Schema(description = "警告列表")
    private List<String> warnings;

    @Schema(description = "验证摘要")
    private ValidationSummary summary;

    /**
     * 验证摘要
     */
    @Data
    @Schema(description = "验证摘要")
    public static class ValidationSummary {

        @Schema(description = "总权限数")
        private int totalPermissions;

        @Schema(description = "有效权限数")
        private int validPermissions;

        @Schema(description = "无效权限数")
        private int invalidPermissions;

        @Schema(description = "过期权限数")
        private int expiredPermissions;

        @Schema(description = "即将过期权限数")
        private int soonToExpirePermissions;

        @Schema(description = "未同步权限数")
        private int notSyncedPermissions;
    }
}