package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限续期表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-24
 */
@Data
@Schema(description = "权限续期表单")
public class PermissionRenewForm {

    @Schema(description = "续期时长：7/15/30/90/180/365（天）", required = true)
    @NotNull(message = "续期时长不能为空")
    private Integer duration;

    @Schema(description = "续期原因", example = "工作需要")
    private String reason;
}
