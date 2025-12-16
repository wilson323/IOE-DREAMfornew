package net.lab1024.sa.common.permission.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 菜单权限数据展示对象
 * <p>
 * 前端菜单权限管理的核心数据结构，支持：
 * - 树形菜单结构（支持无限级嵌套）
 * - 菜单权限控制（显示/隐藏/禁用状态）
 * - 前端路由配置（支持动态路由生成）
 * - 按钮级权限控制（webPerms集成）
 * - 菜单图标和样式配置
 * - 权限依赖关系（父菜单权限控制子菜单）
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
@Schema(description = "菜单权限数据展示对象")
public class MenuPermissionVO {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID", example = "1001")
    private Long menuId;

    /**
     * 菜单编码
     */
    @Schema(description = "菜单编码", example = "SYSTEM_USER_MANAGE")
    private String menuCode;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称", example = "用户管理")
    private String menuName;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", example = "UserOutlined")
    private String icon;

    /**
     * 菜单类型
     */
    @Schema(description = "菜单类型", example = "1")
    private Integer menuType; // 1-目录 2-菜单 3-按钮

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID", example = "1000")
    private Long parentId;

    /**
     * 菜单层级
     */
    @Schema(description = "菜单层级", example = "2")
    private Integer level;

    /**
     * 排序号
     */
    @Schema(description = "排序号", example = "1")
    private Integer orderNum;

    /**
     * 路由路径
     */
    @Schema(description = "路由路径", example = "/system/user")
    private String path;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "system/user/index")
    private String component;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识", example = "system:user:view")
    private String permission;

    /**
     * 前端权限标识（webPerms格式）
     */
    @Schema(description = "前端权限标识（webPerms格式）", example = "system:user:view")
    private String webPermission;

    /**
     * 菜单状态
     */
    @Schema(description = "菜单状态", example = "1")
    private Integer status; // 0-隐藏 1-显示

    /**
     * 是否可见
     */
    @Schema(description = "是否可见", example = "true")
    private Boolean visible;

    /**
     * 是否缓存
     */
    @Schema(description = "是否缓存", example = "true")
    private Boolean keepAlive;

    /**
     * 是否外链
     */
    @Schema(description = "是否外链", example = "false")
    private Boolean isExternal;

    /**
     * 外链地址
     */
    @Schema(description = "外链地址", example = "https://www.example.com")
    private String externalUrl;

    /**
     * 查询参数
     */
    @Schema(description = "查询参数", example = "?type=admin")
    private String query;

    /**
     * 菜单描述
     */
    @Schema(description = "菜单描述", example = "系统用户管理功能")
    private String description;

    /**
     * 菜单备注
     */
    @Schema(description = "菜单备注", example = "系统管理员专用菜单")
    private String remark;

    /**
     * 子菜单列表
     */
    @Schema(description = "子菜单列表")
    private List<MenuPermissionVO> children;

    /**
     * 按钮权限列表（仅当menuType=3时有效）
     */
    @Schema(description = "按钮权限列表")
    private List<ButtonPermission> buttons;

    /**
     * 菜单元数据（路由配置、样式等）
     */
    @Schema(description = "菜单元数据")
    private MenuMetadata metadata;

    /**
     * 权限控制信息
     */
    @Schema(description = "权限控制信息")
    private PermissionControl permissionControl;

    /**
     * 按钮权限信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "按钮权限信息")
    public static class ButtonPermission {

        /**
         * 按钮ID
         */
        @Schema(description = "按钮ID", example = "10011")
        private Long buttonId;

        /**
         * 按钮编码
         */
        @Schema(description = "按钮编码", example = "SYSTEM_USER_ADD")
        private String buttonCode;

        /**
         * 按钮名称
         */
        @Schema(description = "按钮名称", example = "新增用户")
        private String buttonName;

        /**
         * 权限标识
         */
        @Schema(description = "权限标识", example = "system:user:add")
        private String permission;

        /**
         * 前端权限标识（webPerms格式）
         */
        @Schema(description = "前端权限标识（webPerms格式）", example = "system.user.add")
        private String webPermission;

        /**
         * 按钮类型
         */
        @Schema(description = "按钮类型", example = "PRIMARY")
        private String buttonType;

        /**
         * 按钮图标
         */
        @Schema(description = "按钮图标", example = "PlusOutlined")
        private String icon;

        /**
         * 排序号
         */
        @Schema(description = "排序号", example = "1")
        private Integer orderNum;

        /**
         * 是否显示
         */
        @Schema(description = "是否显示", example = "true")
        private Boolean visible;

        /**
         * 按钮状态
         */
        @Schema(description = "按钮状态", example = "1")
        private Integer status;

        /**
         * 按钮事件
         */
        @Schema(description = "按钮事件", example = "handleAdd")
        private String event;

        /**
         * 确认提示
         */
        @Schema(description = "确认提示", example = "确定要新增用户吗？")
        private String confirmText;
    }

    /**
     * 菜单元数据内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "菜单元数据")
    public static class MenuMetadata {

        /**
         * 路由名称
         */
        @Schema(description = "路由名称", example = "SystemUser")
        private String routeName;

        /**
         * 路由别名
         */
        @Schema(description = "路由别名", example = "系统用户管理")
        private String routeAlias;

        /**
         * 重定向地址
         */
        @Schema(description = "重定向地址", example = "/system/user/list")
        private String redirect;

        /**
         * 面包屑导航
         */
        @Schema(description = "面包屑导航", example = "系统 > 用户管理")
        private String breadcrumb;

        /**
         * 页面标题
         */
        @Schema(description = "页面标题", example = "用户管理")
        private String pageTitle;

        /**
         * 页面关键词
         */
        @Schema(description = "页面关键词", example = "用户,管理,系统")
        private String keywords;

        /**
         * CSS类名
         */
        @Schema(description = "CSS类名", example = "user-manage-page")
        private String className;

        /**
         * 菜单样式配置
         */
        @Schema(description = "菜单样式配置", example = "{\"color\": \"#1890ff\", \"background\": \"#f0f2f5\"}")
        private String styleConfig;

        /**
         * 自定义属性
         */
        @Schema(description = "自定义属性", example = "{\"requiresAuth\": true, \"adminOnly\": false}")
        private Map<String, Object> customProps;
    }

    /**
     * 权限控制信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限控制信息")
    public static class PermissionControl {

        /**
         * 需要的角色列表
         */
        @Schema(description = "需要的角色列表", example = "[\"ADMIN\", \"USER_MANAGER\"]")
        private List<String> requiredRoles;

        /**
         * 需要的权限列表
         */
        @Schema(description = "需要的权限列表", example = "[\"system:user:view\", \"system:user:manage\"]")
        private List<String> requiredPermissions;

        /**
         * 需要的webPerms列表
         */
        @Schema(description = "需要的webPerms列表", example = "[\"system.user.view\", \"system.user.manage\"]")
        private List<String> requiredWebPerms;

        /**
         * 权限检查模式
         */
        @Schema(description = "权限检查模式", example = "ANY")
        private String checkMode; // ANY-任一权限 ALL-所有权限

        /**
         * 是否需要登录
         */
        @Schema(description = "是否需要登录", example = "true")
        private Boolean requireAuth;

        /**
         * 权限继承模式
         */
        @Schema(description = "权限继承模式", example = "INHERIT")
        private String inheritMode; // INHERIT-继承父权限 NONE-不继承

        /**
         * 数据权限控制
         */
        @Schema(description = "数据权限控制", example = "OWN_DEPT")
        private String dataScope; // ALL-全部 OWN_DEPT-本部门 OWN-仅自己

        /**
         * 时间限制
         */
        @Schema(description = "时间限制", example = "{\"startTime\": \"09:00\", \"endTime\": \"18:00\", \"workdaysOnly\": true}")
        private Map<String, Object> timeRestriction;
    }

    /**
     * 检查是否为根菜单
     */
    public boolean isRootMenu() {
        return parentId == null || parentId == 0;
    }

    /**
     * 检查是否为目录类型
     */
    public boolean isDirectory() {
        return Integer.valueOf(1).equals(menuType);
    }

    /**
     * 检查是否为菜单类型
     */
    public boolean isMenu() {
        return Integer.valueOf(2).equals(menuType);
    }

    /**
     * 检查是否为按钮类型
     */
    public boolean isButton() {
        return Integer.valueOf(3).equals(menuType);
    }

    /**
     * 检查是否具有子菜单
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * 检查是否可见
     */
    public boolean isVisible() {
        return Integer.valueOf(1).equals(status) && (visible == null || visible);
    }

    /**
     * 检查是否具有按钮权限
     */
    public boolean hasButtonPermission(String permission) {
        if (buttons == null || permission == null) {
            return false;
        }
        return buttons.stream()
            .anyMatch(button -> permission.equals(button.getWebPermission()) || permission.equals(button.getPermission()));
    }

    /**
     * 获取菜单完整路径
     */
    public String getFullPath() {
        if (path == null || path.isEmpty()) {
            return "";
        }

        StringBuilder fullPath = new StringBuilder(path);
        if (query != null && !query.isEmpty()) {
            fullPath.append(query);
        }
        return fullPath.toString();
    }

    /**
     * 构建前端路由对象
     */
    public Object buildRouteObject() {
        // 这里可以构建前端路由所需的对象结构
        // 实际使用时可以根据前端框架要求进行调整
        return null;
    }
}