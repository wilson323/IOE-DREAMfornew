package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 用户权限视图对象
 * <p>
 * 用于表示用户的完整权限信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户权限视图对象")
public class UserPermissionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "权限标识列表", example = "[\"user:view\", \"user:edit\"]")
    private Set<String> permissions;

    @Schema(description = "角色标识列表", example = "[\"ADMIN\", \"USER\"]")
    private Set<String> roles;

    @Schema(description = "菜单权限列表")
    private List<MenuPermissionVO> menuPermissions;

    @Schema(description = "权限数据版本", example = "1.0.0")
    private String version;

    @Schema(description = "权限数据更新时间戳", example = "1703059200000")
    private Long updateTimestamp;
}

