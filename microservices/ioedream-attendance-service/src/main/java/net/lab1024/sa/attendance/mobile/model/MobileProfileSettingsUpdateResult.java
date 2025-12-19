package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端用户配置更新结果
 * <p>
 * 封装移动端用户配置更新响应结果
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
@Schema(description = "移动端用户配置更新结果")
public class MobileProfileSettingsUpdateResult {

    /**
     * 是否成功
     */
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    /**
     * 消息
     */
    @Schema(description = "消息", example = "配置更新成功")
    private String message;

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    public String getMessage() {
        return message;
    }
}


