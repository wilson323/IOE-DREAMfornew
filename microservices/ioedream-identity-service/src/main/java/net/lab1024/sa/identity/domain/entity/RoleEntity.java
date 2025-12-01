package net.lab1024.sa.identity.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 角色实体
 * 基于IOE-DREAM现有权限体系设计
 *
 * @author IOE-DREAM Team
 * @since 2025-11-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_role")
public class RoleEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态：1-启用,0-禁用
     */
    private Integer status;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 是否内置角色(1-是，0-否)
     */
    private Integer isBuiltin;

    /**
     * 数据权限范围(1-全部,2-本部门，3-本部门及子部门，4-仅本人)
     */
    private Integer dataScope;

    /**
     * 备注
     */
    private String remark;

    // 业务方法

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
     * 获取默认排序值
     */
    public int getSortOrderOrDefault() {
        return sortOrder != null ? sortOrder : 999;
    }

    // 系统预定义角色编码
    public static class RoleCodes {
        public static final String SUPER_ADMIN = "SUPER_ADMIN";
        public static final String ADMIN = "ADMIN";
        public static final String HR_ADMIN = "HR_ADMIN";
        public static final String ACCESS_ADMIN = "ACCESS_ADMIN";
        public static final String CONSUME_ADMIN = "CONSUME_ADMIN";
        public static final String ATTENDANCE_ADMIN = "ATTENDANCE_ADMIN";
        public static final String EMPLOYEE = "EMPLOYEE";
        public static final String VISITOR = "VISITOR";
    }

    // 数据权限范围常量
    public static final class DataScopes {
        public static final int ALL = 1; // 全部数据
        public static final int DEPT = 2; // 本部门数据
        public static final int DEPT_WITH_CHILD = 3; // 本部门及子部门数据
        public static final int SELF = 4; // 仅本人数据
    }
}
