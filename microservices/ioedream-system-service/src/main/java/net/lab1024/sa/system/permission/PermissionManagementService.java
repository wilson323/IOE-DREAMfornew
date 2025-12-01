package net.lab1024.sa.system.permission;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * 权限管理服务
 * 支持角色权限管理、用户角色分配、权限树管理和RBAC权限控制
 *
 * @author IOE-DREAM
 * @since 2025-11-28
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class PermissionManagementService {

    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        MENU("菜单权限", "页面菜单访问权限"),
        BUTTON("按钮权限", "页面按钮操作权限"),
        API("接口权限", "API接口访问权限"),
        DATA("数据权限", "数据范围访问权限"),
        SYSTEM("系统权限", "系统管理相关权限");

        private final String description;
        private final String comment;

        PermissionType(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 数据权限范围枚举
     */
    public enum DataScope {
        ALL("全部数据权限", "可以查看所有数据"),
        CUSTOM("自定义数据权限", "可以查看指定部门数据"),
        DEPARTMENT("本部门数据权限", "只能查看本部门数据"),
        DEPARTMENT_AND_CHILD("本部门及子部门数据权限", "可以查看本部门及子部门数据"),
        SELF("个人数据权限", "只能查看自己相关的数据"),
        NONE("无数据权限", "没有任何数据权限");

        private final String description;
        private final String comment;

        DataScope(String description, String comment) {
            this.description = description;
            this.comment = comment;
        }

        public String getDescription() {
            return description;
        }

        public String getComment() {
            return comment;
        }
    }

    /**
     * 权限信息
     */
    public static class Permission {
        private String permissionId;
        private String permissionCode;
        private String permissionName;
        private String description;
        private PermissionType permissionType;
        private String parentId;
        private Integer sortOrder;
        private String path;
        private String component;
        private String icon;
        private boolean isExternal;
        private boolean isCache;
        private boolean isHidden;
        private String apiPath;
        private String apiMethod;
        private String remark;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createUserId;
        private String updateUserId;

        // 构造函数
        public Permission(String permissionId, String permissionCode, String permissionName,
                PermissionType permissionType) {
            this.permissionId = permissionId;
            this.permissionCode = permissionCode;
            this.permissionName = permissionName;
            this.permissionType = permissionType;
            this.createTime = LocalDateTime.now();
            this.updateTime = LocalDateTime.now();
            this.isCache = true;
            this.isHidden = false;
            this.isExternal = false;
            this.sortOrder = 0;
        }

        // Getter和Setter方法
        public String getPermissionId() {
            return permissionId;
        }

        public void setPermissionId(String permissionId) {
            this.permissionId = permissionId;
        }

        public String getPermissionCode() {
            return permissionCode;
        }

        public void setPermissionCode(String permissionCode) {
            this.permissionCode = permissionCode;
        }

        public String getPermissionName() {
            return permissionName;
        }

        public void setPermissionName(String permissionName) {
            this.permissionName = permissionName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public PermissionType getPermissionType() {
            return permissionType;
        }

        public void setPermissionType(PermissionType permissionType) {
            this.permissionType = permissionType;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public boolean isExternal() {
            return isExternal;
        }

        public void setExternal(boolean external) {
            isExternal = external;
        }

        public boolean isCache() {
            return isCache;
        }

        public void setCache(boolean cache) {
            isCache = cache;
        }

        public boolean isHidden() {
            return isHidden;
        }

        public void setHidden(boolean hidden) {
            isHidden = hidden;
        }

        public String getApiPath() {
            return apiPath;
        }

        public void setApiPath(String apiPath) {
            this.apiPath = apiPath;
        }

        public String getApiMethod() {
            return apiMethod;
        }

        public void setApiMethod(String apiMethod) {
            this.apiMethod = apiMethod;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }

        public String getUpdateUserId() {
            return updateUserId;
        }

        public void setUpdateUserId(String updateUserId) {
            this.updateUserId = updateUserId;
        }
    }

    /**
     * 角色信息
     */
    public static class Role {
        private String roleId;
        private String roleCode;
        private String roleName;
        private String description;
        private Integer sortOrder;
        private DataScope dataScope;
        private Set<String> permissionIds;
        private boolean isSystem;
        private String remark;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private String createUserId;
        private String updateUserId;

        // 构造函数
        public Role(String roleId, String roleCode, String roleName) {
            this.roleId = roleId;
            this.roleCode = roleCode;
            this.roleName = roleName;
            this.permissionIds = new HashSet<>();
            this.createTime = LocalDateTime.now();
            this.updateTime = LocalDateTime.now();
            this.dataScope = DataScope.SELF;
            this.isSystem = false;
            this.sortOrder = 0;
        }

        // Getter和Setter方法
        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public String getRoleCode() {
            return roleCode;
        }

        public void setRoleCode(String roleCode) {
            this.roleCode = roleCode;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }

        public DataScope getDataScope() {
            return dataScope;
        }

        public void setDataScope(DataScope dataScope) {
            this.dataScope = dataScope;
        }

        public Set<String> getPermissionIds() {
            return permissionIds;
        }

        public void setPermissionIds(Set<String> permissionIds) {
            this.permissionIds = permissionIds;
        }

        public boolean isSystem() {
            return isSystem;
        }

        public void setSystem(boolean system) {
            isSystem = system;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public LocalDateTime getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }

        public String getUpdateUserId() {
            return updateUserId;
        }

        public void setUpdateUserId(String updateUserId) {
            this.updateUserId = updateUserId;
        }
    }

    /**
     * 用户角色关联
     */
    public static class UserRole {
        private String userRoleId;
        private String userId;
        private String roleId;
        private LocalDateTime createTime;
        private String createUserId;

        // 构造函数
        public UserRole(String userId, String roleId) {
            this.userRoleId = "UR_" + System.currentTimeMillis();
            this.userId = userId;
            this.roleId = roleId;
            this.createTime = LocalDateTime.now();
        }

        // Getter和Setter方法
        public String getUserRoleId() {
            return userRoleId;
        }

        public void setUserRoleId(String userRoleId) {
            this.userRoleId = userRoleId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public LocalDateTime getCreateTime() {
            return createTime;
        }

        public void setCreateTime(LocalDateTime createTime) {
            this.createTime = createTime;
        }

        public String getCreateUserId() {
            return createUserId;
        }

        public void setCreateUserId(String createUserId) {
            this.createUserId = createUserId;
        }
    }

    /**
     * 创建权限
     */
    public Permission createPermission(String permissionCode, String permissionName,
            PermissionType permissionType, String parentId,
            String description, String createUserId) {
        log.info("创建权限: code={}, name={}, type={}", permissionCode, permissionName, permissionType);

        // 检查权限代码是否已存在
        if (isPermissionCodeExists(permissionCode)) {
            log.error("权限代码已存在: {}", permissionCode);
            return null;
        }

        Permission permission = new Permission(
                "PERM_" + System.currentTimeMillis(),
                permissionCode,
                permissionName,
                permissionType);

        permission.setParentId(parentId);
        permission.setDescription(description);
        permission.setCreateUserId(createUserId);

        log.info("权限创建成功: permissionId={}, code={}", permission.getPermissionId(), permissionCode);
        return permission;
    }

    /**
     * 更新权限
     */
    @CacheEvict(value = "permissions", key = "#permissionId")
    public boolean updatePermission(String permissionId, String permissionName,
            String description, String updateUserId) {
        log.info("更新权限: permissionId={}, name={}", permissionId, permissionName);

        Permission permission = getPermission(permissionId);
        if (permission == null) {
            log.error("权限不存在: {}", permissionId);
            return false;
        }

        permission.setPermissionName(permissionName);
        permission.setDescription(description);
        permission.setUpdateTime(LocalDateTime.now());
        permission.setUpdateUserId(updateUserId);

        log.info("权限更新成功: permissionId={}", permissionId);
        return true;
    }

    /**
     * 删除权限
     */
    @CacheEvict(value = { "permissions", "permissionTrees" }, allEntries = true)
    public boolean deletePermission(String permissionId, String operatorId) {
        log.info("删除权限: permissionId={}, operator={}", permissionId, operatorId);

        Permission permission = getPermission(permissionId);
        if (permission == null) {
            log.error("权限不存在: {}", permissionId);
            return false;
        }

        // 检查是否有子权限
        if (hasChildPermissions(permissionId)) {
            log.error("权限存在子权限，无法删除: {}", permissionId);
            return false;
        }

        // 检查是否被角色使用
        if (isPermissionUsedByRoles(permissionId)) {
            log.error("权限正在被角色使用，无法删除: {}", permissionId);
            return false;
        }

        // 执行删除（实际应该从数据库删除）
        log.info("权限删除成功: permissionId={}", permissionId);
        return true;
    }

    /**
     * 创建角色
     */
    public Role createRole(String roleCode, String roleName, String description,
            DataScope dataScope, String createUserId) {
        log.info("创建角色: code={}, name={}, dataScope={}", roleCode, roleName, dataScope);

        // 检查角色代码是否已存在
        if (isRoleCodeExists(roleCode)) {
            log.error("角色代码已存在: {}", roleCode);
            return null;
        }

        Role role = new Role(
                "ROLE_" + System.currentTimeMillis(),
                roleCode,
                roleName);

        role.setDescription(description);
        role.setDataScope(dataScope);
        role.setCreateUserId(createUserId);

        log.info("角色创建成功: roleId={}, code={}", role.getRoleId(), roleCode);
        return role;
    }

    /**
     * 更新角色
     */
    @CacheEvict(value = "roles", key = "#roleId")
    public boolean updateRole(String roleId, String roleName, String description,
            DataScope dataScope, String updateUserId) {
        log.info("更新角色: roleId={}, name={}", roleId, roleName);

        Role role = getRole(roleId);
        if (role == null) {
            log.error("角色不存在: {}", roleId);
            return false;
        }

        if (role.isSystem()) {
            log.error("系统角色不允许修改: {}", roleId);
            return false;
        }

        role.setRoleName(roleName);
        role.setDescription(description);
        role.setDataScope(dataScope);
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateUserId(updateUserId);

        log.info("角色更新成功: roleId={}", roleId);
        return true;
    }

    /**
     * 分配角色权限
     */
    public boolean assignRolePermissions(String roleId, Set<String> permissionIds, String operatorId) {
        log.info("分配角色权限: roleId={}, permissionCount={}, operator={}",
                roleId, permissionIds.size(), operatorId);

        Role role = getRole(roleId);
        if (role == null) {
            log.error("角色不存在: {}", roleId);
            return false;
        }

        if (role.isSystem()) {
            log.error("系统角色权限不允许修改: {}", roleId);
            return false;
        }

        // 验证权限是否存在
        for (String permissionId : permissionIds) {
            if (getPermission(permissionId) == null) {
                log.error("权限不存在: {}", permissionId);
                return false;
            }
        }

        // 分配权限
        role.setPermissionIds(new HashSet<>(permissionIds));
        role.setUpdateTime(LocalDateTime.now());
        role.setUpdateUserId(operatorId);

        log.info("角色权限分配成功: roleId={}, permissions={}", roleId, permissionIds.size());
        return true;
    }

    /**
     * 分配用户角色
     */
    public boolean assignUserRoles(String userId, Set<String> roleIds, String operatorId) {
        log.info("分配用户角色: userId={}, roleCount={}, operator={}",
                userId, roleIds.size(), operatorId);

        // 验证角色是否存在
        for (String roleId : roleIds) {
            Role role = getRole(roleId);
            if (role == null) {
                log.error("角色不存在: {}", roleId);
                return false;
            }
        }

        // 先清除用户现有角色
        clearUserRoles(userId);

        // 分配新角色
        for (String roleId : roleIds) {
            UserRole userRole = new UserRole(userId, roleId);
            userRole.setCreateUserId(operatorId);
            // 保存用户角色关联（实际应该保存到数据库）
        }

        log.info("用户角色分配成功: userId={}, roles={}", userId, roleIds.size());
        return true;
    }

    /**
     * 获取权限列表
     */
    @Cacheable(value = "permissions", key = "#permissionType + '_' + #parentId")
    public List<Permission> getPermissionList(PermissionType permissionType, String parentId) {
        log.info("获取权限列表: type={}, parentId={}", permissionType, parentId);

        // 模拟数据 - 实际应该从数据库查询
        List<Permission> permissions = new ArrayList<>();

        if (permissionType == null) {
            // 返回所有类型的权限
            for (PermissionType type : PermissionType.values()) {
                permissions.addAll(createSamplePermissions(type));
            }
        } else {
            permissions.addAll(createSamplePermissions(permissionType));
        }

        return permissions;
    }

    /**
     * 获取权限树
     */
    @Cacheable(value = "permissionTrees", key = "#permissionType")
    public List<Map<String, Object>> getPermissionTree(PermissionType permissionType) {
        log.info("获取权限树: type={}", permissionType);

        List<Permission> allPermissions = getPermissionList(permissionType, null);
        return buildPermissionTree(allPermissions);
    }

    /**
     * 获取角色列表
     */
    @Cacheable(value = "roles", key = "#roleCode + '_' + #status")
    public List<Role> getRoleList(String roleCode, String status) {
        log.info("获取角色列表: roleCode={}, status={}", roleCode, status);

        // 模拟数据 - 实际应该从数据库查询
        List<Role> roles = new ArrayList<>();

        roles.add(createSampleRole("ROLE_001", "ADMIN", "系统管理员", DataScope.ALL));
        roles.add(createSampleRole("ROLE_002", "MANAGER", "部门经理", DataScope.DEPARTMENT_AND_CHILD));
        roles.add(createSampleRole("ROLE_003", "EMPLOYEE", "普通员工", DataScope.SELF));
        roles.add(createSampleRole("ROLE_004", "HR", "人力资源", DataScope.DEPARTMENT));

        return roles;
    }

    /**
     * 获取用户角色
     */
    @Cacheable(value = "userRoles", key = "#userId")
    public List<Role> getUserRoles(String userId) {
        log.info("获取用户角色: userId={}", userId);

        // 模拟数据 - 实际应该从数据库查询
        List<Role> roles = new ArrayList<>();
        roles.add(createSampleRole("ROLE_003", "EMPLOYEE", "普通员工", DataScope.SELF));

        return roles;
    }

    /**
     * 获取用户权限
     */
    @Cacheable(value = "userPermissions", key = "#userId + '_' + #permissionType")
    public Set<String> getUserPermissions(String userId, PermissionType permissionType) {
        log.info("获取用户权限: userId={}, type={}", userId, permissionType);

        Set<String> permissions = new HashSet<>();

        // 获取用户角色
        List<Role> userRoles = getUserRoles(userId);

        // 收集角色权限
        for (Role role : userRoles) {
            for (String permissionId : role.getPermissionIds()) {
                Permission permission = getPermission(permissionId);
                if (permission != null &&
                        (permissionType == null || permission.getPermissionType() == permissionType)) {
                    permissions.add(permission.getPermissionCode());
                }
            }
        }

        return permissions;
    }

    /**
     * 检查用户权限
     */
    public boolean hasPermission(String userId, String permissionCode) {
        log.info("检查用户权限: userId={}, permission={}", userId, permissionCode);

        Set<String> userPermissions = getUserPermissions(userId, null);
        return userPermissions.contains(permissionCode);
    }

    /**
     * 获取角色权限
     */
    @Cacheable(value = "rolePermissions", key = "#roleId")
    public List<Permission> getRolePermissions(String roleId) {
        log.info("获取角色权限: roleId={}", roleId);

        Role role = getRole(roleId);
        if (role == null) {
            return new ArrayList<>();
        }

        List<Permission> permissions = new ArrayList<>();
        for (String permissionId : role.getPermissionIds()) {
            Permission permission = getPermission(permissionId);
            if (permission != null) {
                permissions.add(permission);
            }
        }

        return permissions;
    }

    /**
     * 获取权限统计信息
     */
    public Map<String, Object> getPermissionStatistics() {
        log.info("获取权限统计信息");

        Map<String, Object> statistics = new HashMap<>();

        // 权限总数
        statistics.put("totalPermissions", 156);

        // 按类型统计
        Map<String, Integer> typeStatistics = new HashMap<>();
        typeStatistics.put("MENU", 45);
        typeStatistics.put("BUTTON", 89);
        typeStatistics.put("API", 120);
        typeStatistics.put("DATA", 12);
        typeStatistics.put("SYSTEM", 8);
        statistics.put("typeStatistics", typeStatistics);

        // 角色总数
        statistics.put("totalRoles", 12);

        // 系统角色数
        statistics.put("systemRoles", 5);

        // 自定义角色数
        statistics.put("customRoles", 7);

        return statistics;
    }

    // 私有辅助方法

    /**
     * 获取权限信息
     */
    private Permission getPermission(String permissionId) {
        // 模拟数据 - 实际应该从数据库查询
        if (permissionId == null)
            return null;

        return createSamplePermission(permissionId, "PERM_001", PermissionType.MENU);
    }

    /**
     * 获取角色信息
     */
    private Role getRole(String roleId) {
        // 模拟数据 - 实际应该从数据库查询
        if (roleId == null)
            return null;

        return createSampleRole(roleId, "EMPLOYEE", "普通员工", DataScope.SELF);
    }

    /**
     * 检查权限代码是否存在
     */
    private boolean isPermissionCodeExists(String permissionCode) {
        // 模拟检查 - 实际应该查询数据库
        return false;
    }

    /**
     * 检查角色代码是否存在
     */
    private boolean isRoleCodeExists(String roleCode) {
        // 模拟检查 - 实际应该查询数据库
        return false;
    }

    /**
     * 检查是否有子权限
     */
    private boolean hasChildPermissions(String permissionId) {
        // 模拟检查 - 实际应该查询数据库
        return false;
    }

    /**
     * 检查权限是否被角色使用
     */
    private boolean isPermissionUsedByRoles(String permissionId) {
        // 模拟检查 - 实际应该查询数据库
        return false;
    }

    /**
     * 清除用户角色
     */
    private void clearUserRoles(String userId) {
        // 实际应该从数据库删除用户角色关联
        log.info("清除用户现有角色: userId={}", userId);
    }

    /**
     * 构建权限树
     */
    private List<Map<String, Object>> buildPermissionTree(List<Permission> permissions) {
        List<Map<String, Object>> tree = new ArrayList<>();

        // 按parentId分组
        Map<String, List<Permission>> groupedPermissions = permissions.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? "ROOT" : p.getParentId()));

        // 构建根节点
        List<Permission> rootPermissions = groupedPermissions.getOrDefault("ROOT", new ArrayList<>());
        for (Permission rootPermission : rootPermissions) {
            Map<String, Object> treeNode = buildTreeNode(rootPermission, groupedPermissions);
            tree.add(treeNode);
        }

        return tree;
    }

    /**
     * 构建树节点
     */
    private Map<String, Object> buildTreeNode(Permission permission,
            Map<String, List<Permission>> groupedPermissions) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", permission.getPermissionId());
        node.put("code", permission.getPermissionCode());
        node.put("name", permission.getPermissionName());
        node.put("type", permission.getPermissionType().name());
        node.put("sortOrder", permission.getSortOrder());

        // 递归构建子节点
        List<Permission> children = groupedPermissions.getOrDefault(permission.getPermissionId(), new ArrayList<>());
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = new ArrayList<>();
            for (Permission child : children) {
                childNodes.add(buildTreeNode(child, groupedPermissions));
            }
            node.put("children", childNodes);
        }

        return node;
    }

    /**
     * 创建示例权限
     */
    private List<Permission> createSamplePermissions(PermissionType type) {
        List<Permission> permissions = new ArrayList<>();

        switch (type) {
            case MENU:
                permissions.add(new Permission("PERM_001", "system:manage", "系统管理", type));
                permissions.add(new Permission("PERM_002", "user:manage", "用户管理", type));
                permissions.add(new Permission("PERM_003", "role:manage", "角色管理", type));
                break;
            case BUTTON:
                permissions.add(new Permission("PERM_004", "user:add", "用户新增", type));
                permissions.add(new Permission("PERM_005", "user:edit", "用户编辑", type));
                permissions.add(new Permission("PERM_006", "user:delete", "用户删除", type));
                break;
            case API:
                permissions.add(new Permission("PERM_007", "api:user:list", "用户列表API", type));
                permissions.add(new Permission("PERM_008", "api:user:save", "用户保存API", type));
                break;
            default:
                break;
        }

        return permissions;
    }

    /**
     * 创建示例权限
     */
    private Permission createSamplePermission(String permissionId, String permissionCode,
            PermissionType permissionType) {
        Permission permission = new Permission(permissionId, permissionCode, "权限名称", permissionType);
        permission.setDescription("这是权限描述");
        return permission;
    }

    /**
     * 创建示例角色
     */
    private Role createSampleRole(String roleId, String roleCode, String roleName, DataScope dataScope) {
        Role role = new Role(roleId, roleCode, roleName);
        role.setDescription("这是" + roleName + "的描述");
        role.setDataScope(dataScope);
        role.setSystem("ADMIN".equals(roleCode));
        return role;
    }

    /**
     * 获取权限类型列表
     */
    public List<Map<String, Object>> getPermissionTypes() {
        return Arrays.stream(PermissionType.values())
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("code", type.name());
                    typeInfo.put("description", type.getDescription());
                    typeInfo.put("comment", type.getComment());
                    return typeInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取数据权限范围列表
     */
    public List<Map<String, Object>> getDataScopes() {
        return Arrays.stream(DataScope.values())
                .map(scope -> {
                    Map<String, Object> scopeInfo = new HashMap<>();
                    scopeInfo.put("code", scope.name());
                    scopeInfo.put("description", scope.getDescription());
                    scopeInfo.put("comment", scope.getComment());
                    return scopeInfo;
                })
                .collect(Collectors.toList());
    }
}
