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
 * 角色实体类
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Entity
@Table(name = "sys_role", indexes = {
    @Index(name = "idx_role_code", columnList = "role_code"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity extends BaseEntity {

    /**
     * 角色ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 50, message = "角色编码长度必须在2-50个字符之间")
    @Column(name = "role_code", nullable = false, length = 50, unique = true)
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 100, message = "角色名称长度必须在2-100个字符之间")
    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    /**
     * 角色描述
     */
    @Size(max = 500, message = "角色描述长度不能超过500个字符")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * 是否内置角色：1-是，0-否
     */
    @Column(name = "is_builtin", nullable = false)
    private Integer isBuiltin;

    /**
     * 数据权限范围：1-全部，2-本部门，3-本部门及子部门，4-仅本人，5-自定义
     */
    @Column(name = "data_scope", nullable = false)
    private Integer dataScope;

    /**
     * 用户角色关联
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRoleEntity> userRoles;

    /**
     * 角色权限关联
     */
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RolePermissionEntity> rolePermissions;

    /**
     * 检查角色是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 检查是否为内置角色
     */
    public boolean isBuiltin() {
        return isBuiltin != null && isBuiltin == 1;
    }

    /**
     * 获取默认排序号
     */
    public int getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : 999;
    }

    /**
     * 系统预定义角色常量
     */
    public static class RoleCodes {
        public static final String SUPER_ADMIN = "SUPER_ADMIN";
        public static final String ADMIN = "ADMIN";
        public static final String USER = "USER";
        public static final String MANAGER = "MANAGER";
        public static final String OPERATOR = "OPERATOR";
        public static final String AUDITOR = "AUDITOR";
    }

    /**
     * 数据权限范围常量
     */
    public static class DataScopes {
        public static final int ALL = 1;           // 全部数据
        public static final int DEPT = 2;          // 本部门数据
        public static final int DEPT_WITH_CHILD = 3; // 本部门及子部门数据
        public static final int SELF = 4;          // 仅本人数据
        public static final int CUSTOM = 5;        // 自定义数据
    }
}