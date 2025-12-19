package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 移动端安全设置结果
 * <p>
 * 封装移动端安全设置响应结果
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
@Schema(description = "移动端安全设置结果")
public class MobileSecuritySettingsResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 安全设置
     */
    @Schema(description = "安全设置")
    private Map<String, Object> securitySettings;
}
