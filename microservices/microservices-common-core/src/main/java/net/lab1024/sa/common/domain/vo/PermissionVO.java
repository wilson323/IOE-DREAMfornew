package net.lab1024.sa.common.domain.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 权限信息VO
 * <p>
 * 用于缓存和数据传输的权限信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionVO {

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 权限代码
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限类型：MENU-菜单 BUTTON-按钮 API-接口
     */
    private String permissionType;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限路径（URL或组件路径）
     */
    private String path;

    /**
     * 权限图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 是否需要权限验证：1-是 0-否
     */
    private Integer requireAuth;

    /**
     * 权限描述
     */
    private String description;
}
