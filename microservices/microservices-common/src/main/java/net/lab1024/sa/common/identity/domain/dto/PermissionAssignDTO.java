package net.lab1024.sa.common.identity.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 权限分配DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Schema(description = "权限分配DTO")
public class PermissionAssignDTO {

    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", example = "[1, 2, 3]")
    private List<Long> permissionIds;
}

