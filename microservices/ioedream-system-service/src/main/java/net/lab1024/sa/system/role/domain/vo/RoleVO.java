package net.lab1024.sa.system.role.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

/**
 * 角色VO
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
public class RoleVO {

    /**
     * 角色ID
     */
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
     * 角色类型(SYSTEM|BUSINESS|CUSTOM)
     */
    private String roleType;

    /**
     * 角色类型名称
     */
    private String roleTypeName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 是否系统角色
     */
    private Integer isSystem;

    /**
     * 是否系统角色名称
     */
    private String isSystemName;

    /**
     * 状态(0-禁用,1-启用)
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 用户数量
     */
    private Integer userCount;

    /**
     * 权限数量
     */
    private Integer permissionCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 更新人ID
     */
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    private String updateUserName;

    /**
     * 角色权限ID列表
     */
    private List<Long> permissionIds;

    /**
     * 角色权限标识列表
     */
    private List<String> permissionCodes;

    /**
     * 角色用户列表
     */
    private List<RoleUserVO> userList;

    /**
     * 角色用户信息
     */
    @Data
    public static class RoleUserVO {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户姓名
         */
        private String userName;

        /**
         * 用户账号
         */
        private String userAccount;

        /**
         * 部门ID
         */
        private Long departmentId;

        /**
         * 部门名称
         */
        private String departmentName;

        /**
         * 用户状态
         */
        private Integer status;

        /**
         * 绑定时间
         */
        private LocalDateTime bindTime;
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (this.status == null) {
            return null;
        }
        return this.status == 1 ? "启用" : "禁用";
    }

    /**
     * 获取是否系统角色名称
     */
    public String getIsSystemName() {
        if (this.isSystem == null) {
            return null;
        }
        return this.isSystem == 1 ? "系统角色" : "自定义角色";
    }

    /**
     * 获取角色类型名称
     */
    public String getRoleTypeName() {
        if (this.roleType == null) {
            return null;
        }
        switch (this.roleType) {
            case "SYSTEM":
                return "系统角色";
            case "BUSINESS":
                return "业务角色";
            case "CUSTOM":
                return "自定义角色";
            default:
                return this.roleType;
        }
    }
}
