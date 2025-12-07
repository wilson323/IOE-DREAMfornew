package net.lab1024.sa.common.identity.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 角色创建DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Schema(description = "角色创建DTO")
public class RoleCreateDTO {

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 100, message = "角色名称长度不能超过100个字符")
    @Schema(description = "角色名称", example = "系统管理员")
    private String roleName;

    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Schema(description = "角色描述", example = "拥有系统所有权限")
    private String roleDesc;

    @NotNull(message = "数据权限范围不能为空")
    @Schema(description = "数据权限范围：1-全部 2-自定义 3-本部门 4-本部门及子部门 5-仅本人", example = "1")
    private Integer dataScope;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "备注", example = "系统角色")
    private String remark;
}

