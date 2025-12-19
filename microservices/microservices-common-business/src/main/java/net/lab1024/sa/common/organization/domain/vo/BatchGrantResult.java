package net.lab1024.sa.common.organization.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量分配结果
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Data
@Schema(description = "批量分配结果")
public class BatchGrantResult {

    @Schema(description = "总数")
    private int totalCount;

    @Schema(description = "成功数")
    private int successCount;

    @Schema(description = "失败数")
    private int failedCount;

    @Schema(description = "成功用户列表")
    private List<String> successUsers;

    @Schema(description = "失败用户列表")
    private List<BatchGrantError> failedUsers;

    /**
     * 批量分配错误
     */
    @Data
    @Schema(description = "批量分配错误")
    public static class BatchGrantError {

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "错误信息")
        private String errorMessage;
    }
}