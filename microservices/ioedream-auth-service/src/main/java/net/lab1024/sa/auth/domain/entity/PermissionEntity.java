package net.lab1024.sa.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.common.entity.BaseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 权限实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Entity
@Table(name = "sys_permission", indexes = {
    @Index(name = "idx_permission_code", columnList = "permission_code"),
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_permission_type", columnList = "permission_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity extends BaseEntity {

    /**
     * 权限ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    @Column(name = "permission_code", nullable = false, length = 100, unique = true)
    private String permissionCode;

    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 100, message = "权限名称长度不能超过100个字符")
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    /**
     * 权限类型：1-菜单，2-按钮，3-接口
     */
    @Column(name = "permission_type", nullable = false)
    private Integer permissionType;

    /**
     * 路由路径
     */
    @Size(max = 500, message = "路由路径长度不能超过500个字符")
    @Column(name = "path", length = 500)
    private String path;

    /**
     * 组件路径
     */
    @Size(max = 500, message = "组件路径长度不能超过500个字符")
    @Column(name = "component", length = 500)
    private String component;

    /**
     * 权限标识
     */
    @Size(max = 200, message = "权限标识长度不能超过200个字符")
    @Column(name = "perms", length = 200)
    private String perms;

    /**
     * 图标
     */
    @Size(max = 100, message = "图标长度不能超过100个字符")
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 是否可见：1-是，0-否
     */
    @Column(name = "visible", nullable = false)
    private Integer visible;

    /**
     * 是否缓存：1-是，0-否
     */
    @Column(name = "keep_alive", nullable = false)
    private Integer keepAlive;

    /**
     * 是否外链：1-是，0-否
     */
    @Column(name = "is_external", nullable = false)
    private Integer isExternal;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 子权限
     */
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, permissionId ASC")
    private List<PermissionEntity> children;

    /**
     * 角色权限关联
     */
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermissionEntity> rolePermissions;

    /**
     * 父权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "permission_id", insertable = false, updatable = false)
    private PermissionEntity parent;

    /**
     * 检查权限是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查权限是否可见
     */
    public boolean isVisible() {
        return visible != null && visible == 1;
    }

    /**
     * 检查是否为菜单类型
     */
    public boolean isMenu() {
        return permissionType != null && permissionType == 1;
    }

    /**
     * 检查是否为按钮类型
     */
    public boolean isButton() {
        return permissionType != null && permissionType == 2;
    }

    /**
     * 检查是否为接口类型
     */
    public boolean isApi() {
        return permissionType != null && permissionType == 3;
    }

    /**
     * 检查是否需要缓存
     */
    public boolean isKeepAlive() {
        return keepAlive != null && keepAlive == 1;
    }

    /**
     * 检查是否为外链
     */
    public boolean isExternal() {
        return isExternal != null && isExternal == 1;
    }

    /**
     * 获取默认排序号
     */
    public int getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : 999;
    }

    /**
     * 权限类型常量
     */
    public static class PermissionTypes {
        public static final int MENU = 1;    // 菜单
        public static final int BUTTON = 2;  // 按钮
        public static final int API = 3;      // 接口
    }

    /**
     * 系统预定义权限编码
     */
    public static class PermissionCodes {
        // 用户管理
        public static final String USER_VIEW = "user:view";
        public static final String USER_ADD = "user:add";
        public static final String USER_EDIT = "user:edit";
        public static final String USER_DELETE = "user:delete";

        // 角色管理
        public static final String ROLE_VIEW = "role:view";
        public static final String ROLE_ADD = "role:add";
        public static final String ROLE_EDIT = "role:edit";
        public static final String ROLE_DELETE = "role:delete";

        // 权限管理
        public static final String PERMISSION_VIEW = "permission:view";
        public static final String PERMISSION_ADD = "permission:add";
        public static final String PERMISSION_EDIT = "permission:edit";
        public static final String PERMISSION_DELETE = "permission:delete";

        // 系统管理
        public static final String SYSTEM_CONFIG = "system:config";
        public static final String SYSTEM_LOG = "system:log";
        public static final String SYSTEM_MONITOR = "system:monitor";
    }
}