package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户权限数据展示对象
 * <p>
 * 前后端权限一致性保障的核心数据结构，包含：
 * - 用户基础权限信息（角色、权限集合）
 * - 前端权限标识（webPerms格式，支持按钮级权限控制）
 * - 菜单权限树结构（支持动态菜单和权限控制）
 * - 权限数据版本信息（支持增量同步和缓存优化）
 * - 权限统计信息（支持权限分析和监控）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户权限数据展示对象")
public class UserPermissionVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "管理员")
    private String realName;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态", example = "1")
    private Integer status;

    /**
     * 用户角色列表
     */
    @Schema(description = "用户角色列表")
    private Set<String> roles;

    /**
     * 用户权限标识列表（系统权限标识）
     */
    @Schema(description = "用户权限标识列表")
    private Set<String> permissions;

    /**
     * 前端权限标识列表（webPerms格式，用于按钮级权限控制）
     * 格式：["user:add", "user:edit", "user:delete", "system:config:view"]
     */
    @Schema(description = "前端权限标识列表（webPerms格式）")
    private Set<String> webPerms;

    /**
     * 菜单权限树结构
     */
    @Schema(description = "菜单权限树结构")
    private List<MenuPermissionVO> menuTree;

    /**
     * 权限数据版本号
     */
    @Schema(description = "权限数据版本号", example = "v1.2.3.20251216")
    private String dataVersion;

    /**
     * 权限数据最后更新时间
     */
    @Schema(description = "权限数据最后更新时间", example = "2025-12-16T10:30:00")
    private LocalDateTime lastUpdateTime;

    /**
     * 缓存过期时间
     */
    @Schema(description = "缓存过期时间", example = "2025-12-16T11:30:00")
    private LocalDateTime cacheExpireTime;

    /**
     * 权限数据摘要（用于变更检测）
     */
    @Schema(description = "权限数据摘要", example = "abc123def456")
    private String dataChecksum;

    /**
     * 权限统计信息
     */
    @Schema(description = "权限统计信息")
    private PermissionStats permissionStats;

    /**
     * 扩展属性（JSON格式，存储额外权限信息）
     */
    @Schema(description = "扩展属性", example = "{\"specialPermissions\": [\"data:export\"], \"timeRestrictions\": {\"startTime\": \"09:00\", \"endTime\": \"18:00\"}}")
    private String extendedAttributes;

    /**
     * 权限统计信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限统计信息")
    public static class PermissionStats {

        /**
         * 角色总数
         */
        @Schema(description = "角色总数", example = "5")
        private Integer totalRoles;

        /**
         * 权限总数
         */
        @Schema(description = "权限总数", example = "128")
        private Integer totalPermissions;

        /**
         * webPerms总数
         */
        @Schema(description = "webPerms总数", example = "89")
        private Integer totalWebPerms;

        /**
         * 菜单权限总数
         */
        @Schema(description = "菜单权限总数", example = "42")
        private Integer totalMenuPermissions;

        /**
         * 可访问的顶级菜单数
         */
        @Schema(description = "可访问的顶级菜单数", example = "8")
        private Integer accessibleTopMenus;

        /**
         * 数据获取耗时（毫秒）
         */
        @Schema(description = "数据获取耗时（毫秒）", example = "150")
        private Long dataFetchTime;

        /**
         * 缓存命中率
         */
        @Schema(description = "缓存命中率", example = "0.85")
        private Double cacheHitRate;
    }

    /**
     * 验证权限数据完整性
     */
    public boolean isValid() {
        return userId != null
            && username != null
            && !username.isEmpty()
            && roles != null
            && permissions != null
            && webPerms != null
            && menuTree != null;
    }

    /**
     * 检查是否具有指定权限
     */
    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 检查是否具有指定webPerms权限
     */
    public boolean hasWebPermission(String webPermission) {
        return webPerms != null && webPerms.contains(webPermission);
    }

    /**
     * 检查是否具有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * 获取权限简要信息（用于日志和调试）
     */
    public String getPermissionSummary() {
        return String.format("User[id=%d, username=%s, roles=%d, permissions=%d, webPerms=%d]",
                userId, username,
                roles != null ? roles.size() : 0,
                permissions != null ? permissions.size() : 0,
                webPerms != null ? webPerms.size() : 0);
    }
}