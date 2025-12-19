package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 移动端安全设置更新请求
 * <p>
 * 封装移动端安全设置更新的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端安全设置更新请求")
public class MobileSecuritySettingsUpdateRequest {

    /**
     * 安全设置
     */
    @Schema(description = "安全设置")
    private Map<String, Object> securitySettings;
}
