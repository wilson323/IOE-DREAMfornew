package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 权限统计视图对象
 * <p>
 * 用于表示权限数据的统计信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "权限统计视图对象")
public class PermissionStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总用户数", example = "1000")
    private Long totalUsers;

    @Schema(description = "总角色数", example = "10")
    private Long totalRoles;

    @Schema(description = "总权限数", example = "100")
    private Long totalPermissions;

    @Schema(description = "总菜单数", example = "50")
    private Long totalMenus;

    @Schema(description = "活跃用户数", example = "800")
    private Long activeUsers;
}

