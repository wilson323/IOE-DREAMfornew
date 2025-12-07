package net.lab1024.sa.common.identity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色详情VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Schema(description = "角色详情VO")
public class RoleDetailVO {

    @Schema(description = "角色ID", example = "1")
    private Long roleId;

    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String roleCode;

    @Schema(description = "角色名称", example = "系统管理员")
    private String roleName;

    @Schema(description = "角色描述", example = "拥有系统所有权限")
    private String roleDesc;

    @Schema(description = "数据权限范围：1-全部 2-自定义 3-本部门 4-本部门及子部门 5-仅本人", example = "1")
    private Integer dataScope;

    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;

    @Schema(description = "是否系统角色：0-否 1-是", example = "0")
    private Integer isSystem;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "权限列表")
    private List<PermissionTreeVO> permissions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "系统角色")
    private String remark;
}

