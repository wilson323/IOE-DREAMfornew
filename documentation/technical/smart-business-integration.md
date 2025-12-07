# 业务模块与权限集成开发文档

## 1. 模块概述

### 1.1 集成目标

本文档描述SmartAdmin系统中业务模块（门禁、考勤、消费、智能视频、访客）与权限系统的深度集成方案，包括：

- 基于角色的模块权限控制
- 业务模块菜单和按钮权限管理
- 审批流程在业务模块中的应用
- 数据权限和功能权限的综合管控

### 1.2 设计原则

- **统一权限模型**: 所有业务模块遵循统一的权限控制规范
- **细粒度控制**: 支持模块、菜单、按钮、数据四级权限控制
- **审批流集成**: 业务流程与warm-flow审批引擎无缝集成
- **可扩展性**: 支持新业务模块的快速接入

### 1.3 核心业务模块

1. **门禁系统** (smart-access)
2. **考勤系统** (smart-attendance)
3. **消费系统** (smart-consumption)
4. **智能视频系统** (smart-video)
5. **访客系统** (smart-visitor)

## 2. 权限与业务模块集成架构

### 2.1 权限层级结构

```
角色权限体系
├── 模块权限 (Module Permission)
│   ├── 门禁系统权限
│   ├── 考勤系统权限
│   ├── 消费系统权限
│   ├── 智能视频权限
│   └── 访客系统权限
├── 菜单权限 (Menu Permission)
│   ├── 模块主菜单
│   ├── 功能子菜单
│   └── 操作页面
├── 按钮权限 (Button Permission)
│   ├── 新增按钮
│   ├── 编辑按钮
│   ├── 删除按钮
│   ├── 审批按钮
│   └── 导出按钮
└── 数据权限 (Data Permission)
    ├── 部门数据权限
    ├── 区域数据权限
    ├── 设备数据权限
    └── 个人数据权限
```

### 2.2 数据库设计

#### 2.2.1 业务模块权限表 (smart_business_module_permission)

```sql
CREATE TABLE smart_business_module_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    module_code VARCHAR(50) NOT NULL COMMENT '模块编码',
    module_name VARCHAR(100) NOT NULL COMMENT '模块名称',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：module-模块，menu-菜单，button-按钮，data-数据',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(200) NOT NULL COMMENT '权限名称',
    permission_value TINYINT DEFAULT 1 COMMENT '权限值：0-无权限，1-有权限',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_role_id (role_id),
    INDEX idx_module_code (module_code),
    INDEX idx_permission_type (permission_type),
    UNIQUE KEY uk_role_module_permission (role_id, module_code, permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务模块权限表';
```

#### 2.2.2 业务菜单权限表 (smart_business_menu_permission)

```sql
CREATE TABLE smart_business_menu_permission (
    menu_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    module_code VARCHAR(50) NOT NULL COMMENT '所属模块编码',
    menu_code VARCHAR(100) NOT NULL COMMENT '菜单编码',
    menu_name VARCHAR(200) NOT NULL COMMENT '菜单名称',
    menu_type VARCHAR(20) NOT NULL COMMENT '菜单类型：directory-目录，menu-菜单，button-按钮',
    menu_path VARCHAR(500) COMMENT '菜单路径',
    menu_icon VARCHAR(100) COMMENT '菜单图标',
    component_path VARCHAR(500) COMMENT '组件路径',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_visible TINYINT DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    permission_code VARCHAR(100) COMMENT '权限标识',
    description TEXT COMMENT '描述',

    INDEX idx_parent_id (parent_id),
    INDEX idx_module_code (module_code),
    INDEX idx_menu_type (menu_type),
    INDEX idx_sort_order (sort_order),
    UNIQUE KEY uk_menu_code (menu_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务菜单权限表';
```

#### 2.2.3 角色菜单权限关联表 (smart_role_menu_permission)

```sql
CREATE TABLE smart_role_menu_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：view-查看，add-新增，edit-编辑，delete-删除，export-导出，approve-审批',
    is_granted TINYINT DEFAULT 0 COMMENT '是否授权：0-未授权，1-已授权',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    UNIQUE KEY uk_role_menu_permission (role_id, menu_id, permission_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单权限关联表';
```

### 2.3 业务模块权限配置

#### 2.3.1 门禁系统权限配置

```sql
-- 门禁系统菜单权限
INSERT INTO smart_business_menu_permission (menu_code, menu_name, module_code, menu_type, menu_path, permission_code, sort_order) VALUES
-- 主菜单
('access_main', '门禁管理', 'ACCESS', 'menu', '/access', 'access:manage', 1),
-- 子菜单
('access_device', '门禁设备', 'ACCESS', 'menu', '/access/device', 'access:device:view', 11),
('access_record', '通行记录', 'ACCESS', 'menu', '/access/record', 'access:record:view', 12),
('access_auth', '门禁授权', 'ACCESS', 'menu', '/access/auth', 'access:auth:view', 13),
('access_monitor', '实时监控', 'ACCESS', 'menu', '/access/monitor', 'access:monitor:view', 14),
('access_report', '统计报表', 'ACCESS', 'menu', '/access/report', 'access:report:view', 15),

-- 按钮权限
('access_device_add', '新增设备', 'ACCESS', 'button', null, 'access:device:add', 111),
('access_device_edit', '编辑设备', 'ACCESS', 'button', null, 'access:device:edit', 112),
('access_device_delete', '删除设备', 'ACCESS', 'button', null, 'access:device:delete', 113),
('access_device_control', '远程控制', 'ACCESS', 'button', null, 'access:device:control', 114),

('access_auth_add', '新增授权', 'ACCESS', 'button', null, 'access:auth:add', 131),
('access_auth_edit', '编辑授权', 'ACCESS', 'button', null, 'access:auth:edit', 132),
('access_auth_delete', '删除授权', 'ACCESS', 'button', null, 'access:auth:delete', 133),
('access_auth_approve', '授权审批', 'ACCESS', 'button', null, 'access:auth:approve', 134);
```

#### 2.3.2 考勤系统权限配置

```sql
-- 考勤系统菜单权限
INSERT INTO smart_business_menu_permission (menu_code, menu_name, module_code, menu_type, menu_path, permission_code, sort_order) VALUES
-- 主菜单
('attendance_main', '考勤管理', 'ATTENDANCE', 'menu', '/attendance', 'attendance:manage', 2),
-- 子菜单
('attendance_schedule', '排班管理', 'ATTENDANCE', 'menu', '/attendance/schedule', 'attendance:schedule:view', 21),
('attendance_record', '打卡记录', 'ATTENDANCE', 'menu', '/attendance/record', 'attendance:record:view', 22),
('attendance_leave', '请假管理', 'ATTENDANCE', 'menu', '/attendance/leave', 'attendance:leave:view', 23),
('attendance_overtime', '加班管理', 'ATTENDANCE', 'menu', '/attendance/overtime', 'attendance:overtime:view', 24),
('attendance_report', '考勤报表', 'ATTENDANCE', 'menu', '/attendance/report', 'attendance:report:view', 25),

-- 按钮权限
('attendance_leave_add', '申请请假', 'ATTENDANCE', 'button', null, 'attendance:leave:add', 231),
('attendance_leave_edit', '编辑请假', 'ATTENDANCE', 'button', null, 'attendance:leave:edit', 232),
('attendance_leave_approve', '审批请假', 'ATTENDANCE', 'button', null, 'attendance:leave:approve', 233),
('attendance_leave_cancel', '取消请假', 'ATTENDANCE', 'button', null, 'attendance:leave:cancel', 234),

('attendance_overtime_add', '申请加班', 'ATTENDANCE', 'button', null, 'attendance:overtime:add', 241),
('attendance_overtime_approve', '审批加班', 'ATTENDANCE', 'button', null, 'attendance:overtime:approve', 242);
```

## 3. 角色权限管理服务
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token

## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
### 3.1 角色权限服务
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！

```java
package net.lab1024.sa.base.module.service.permission;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.base.module.entity.permission.BusinessModulePermissionEntity;
import net.lab1024.sa.base.module.entity.permission.RoleMenuPermissionEntity;
import net.lab1024.sa.base.module.mapper.permission.BusinessModulePermissionMapper;
import net.lab1024.sa.base.module.service.permission.dto.RolePermissionDTO;
import net.lab1024.sa.base.module.service.permission.dto.ModulePermissionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessPermissionService extends ServiceImpl<BusinessModulePermissionMapper, BusinessModulePermissionEntity> {

    private final RoleMenuPermissionService roleMenuPermissionService;
    private final BusinessMenuPermissionService menuPermissionService;

    /**
     * 获取角色业务模块权限
     */
    public Map<String, ModulePermissionVO> getRoleModulePermissions(Long roleId) {
        Map<String, ModulePermissionVO> result = new HashMap<>();

        // 获取所有业务模块
        List<String> modules = Arrays.asList("ACCESS", "ATTENDANCE", "CONSUMPTION", "VIDEO", "VISITOR");

        for (String moduleCode : modules) {
            ModulePermissionVO modulePermission = new ModulePermissionVO();
            modulePermission.setModuleCode(moduleCode);
            modulePermission.setModuleName(getModuleName(moduleCode));

            // 获取模块菜单权限
            List<BusinessMenuPermissionEntity> menus = menuPermissionService.getMenusByModule(moduleCode);
            modulePermission.setMenus(convertToMenuVOs(menus, roleId));

            // 获取模块权限状态
            modulePermission.setHasPermission(hasModulePermission(roleId, moduleCode));

            result.put(moduleCode, modulePermission);
        }

        return result;
    }

    /**
     * 保存角色业务权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleBusinessPermissions(RolePermissionDTO rolePermissionDTO) {
        Long roleId = rolePermissionDTO.getRoleId();

        try {
            // 1. 删除原有权限配置
            roleMenuPermissionService.deleteByRoleId(roleId);

            // 2. 保存新的权限配置
            if (rolePermissionDTO.getPermissions() != null) {
                for (RolePermissionDTO.PermissionItem item : rolePermissionDTO.getPermissions()) {
                    // 保存模块权限
                    saveModulePermission(roleId, item.getModuleCode(), item.isModuleEnabled());

                    // 保存菜单权限
                    if (item.getMenuPermissions() != null) {
                        for (RolePermissionDTO.MenuPermission menuPerm : item.getMenuPermissions()) {
                            saveMenuPermission(roleId, menuPerm.getMenuId(), menuPerm.getPermissions());
                        }
                    }
                }
            }

            log.info("保存角色业务权限成功：{}", roleId);

        } catch (Exception e) {
            log.error("保存角色业务权限失败", e);
            throw new BusinessException("保存权限配置失败");
        }
    }

    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        // 获取用户角色
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return false;
        }

        // 检查角色权限
        return roleMenuPermissionService.hasPermission(roleIds, permissionCode);
    }

    /**
     * 检查用户是否有模块访问权限
     */
    public boolean hasModulePermission(Long userId, String moduleCode) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return false;
        }

        return roleIds.stream().anyMatch(roleId -> hasModulePermission(roleId, moduleCode));
    }

    /**
     * 获取用户可访问的菜单列表
     */
    public List<BusinessMenuPermissionEntity> getUserMenus(Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        return menuPermissionService.getMenusByRoleIds(roleIds);
    }

    /**
     * 保存模块权限
     */
    private void saveModulePermission(Long roleId, String moduleCode, boolean enabled) {
        BusinessModulePermissionEntity entity = new BusinessModulePermissionEntity();
        entity.setRoleId(roleId);
        entity.setModuleCode(moduleCode);
        entity.setModuleName(getModuleName(moduleCode));
        entity.setPermissionType("module");
        entity.setPermissionCode(moduleCode.toLowerCase() + ":manage");
        entity.setPermissionName(getModuleName(moduleCode) + "管理");
        entity.setPermissionValue(enabled ? 1 : 0);
        this.saveOrUpdate(entity, this.lambdaQuery()
                .eq(BusinessModulePermissionEntity::getRoleId, roleId)
                .eq(BusinessModulePermissionEntity::getModuleCode, moduleCode)
                .eq(BusinessModulePermissionEntity::getPermissionType, "module"));
    }

    /**
     * 保存菜单权限
     */
    private void saveMenuPermission(Long roleId, Long menuId, List<String> permissions) {
        for (String permission : permissions) {
            RoleMenuPermissionEntity entity = new RoleMenuPermissionEntity();
            entity.setRoleId(roleId);
            entity.setMenuId(menuId);
            entity.setPermissionType(permission);
            entity.setIsGranted(1);
            roleMenuPermissionService.save(entity);
        }
    }

    /**
     * 检查模块权限
     */
    private boolean hasModulePermission(Long roleId, String moduleCode) {
        return this.lambdaQuery()
                .eq(BusinessModulePermissionEntity::getRoleId, roleId)
                .eq(BusinessModulePermissionEntity::getModuleCode, moduleCode)
                .eq(BusinessModulePermissionEntity::getPermissionType, "module")
                .eq(BusinessModulePermissionEntity::getPermissionValue, 1)
                .exists();
    }

    /**
     * 获取模块名称
     */
    private String getModuleName(String moduleCode) {
        switch (moduleCode) {
            case "ACCESS": return "门禁系统";
            case "ATTENDANCE": return "考勤系统";
            case "CONSUMPTION": return "消费系统";
            case "VIDEO": return "智能视频";
            case "VISITOR": return "访客系统";
            default: return "未知模块";
        }
    }

    /**
     * 获取用户角色ID列表
     */
    private List<Long> getUserRoleIds(Long userId) {
        // TODO: 调用用户服务获取用户角色
        return Arrays.asList(1L, 2L); // 示例数据
    }

    /**
     * 转换菜单VO
     */
    private List<ModulePermissionVO.MenuVO> convertToMenuVOs(List<BusinessMenuPermissionEntity> menus, Long roleId) {
        return menus.stream().map(menu -> {
            ModulePermissionVO.MenuVO menuVO = new ModulePermissionVO.MenuVO();
            menuVO.setMenuId(menu.getMenuId());
            menuVO.setMenuCode(menu.getMenuCode());
            menuVO.setMenuName(menu.getMenuName());
            menuVO.setMenuType(menu.getMenuType());
            menuVO.setMenuPath(menu.getMenuPath());
            menuVO.setMenuIcon(menu.getMenuIcon());
            menuVO.setSortOrder(menu.getSortOrder());

            // 获取菜单权限
            List<String> permissions = roleMenuPermissionService.getMenuPermissions(roleId, menu.getMenuId());
            menuVO.setPermissions(permissions);

            return menuVO;
        }).collect(Collectors.toList());
    }
}
```

### 3.2 权限注解和拦截器

```java
package net.lab1024.sa.base.common.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务权限注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessPermission {

    /**
     * 权限编码
     */
    String value();

    /**
     * 模块编码
     */
    String moduleCode() default "";

    /**
     * 权限描述
     */
    String desc() default "";

    /**
     * 是否检查数据权限
     */
    boolean checkDataPermission() default false;
}
```

```java
package net.lab1024.sa.base.common.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartRequestUtil;
import net.lab1024.sa.base.module.service.permission.BusinessPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 业务权限拦截器
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BusinessPermissionInterceptor {

    private final BusinessPermissionService businessPermissionService;

    @Around("@annotation(businessPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, BusinessPermission businessPermission) throws Throwable {
        try {
            // 获取当前用户ID
            Long userId = getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("用户未登录");
            }

            // 检查权限
            String permissionCode = businessPermission.value();
            if (!businessPermissionService.hasPermission(userId, permissionCode)) {
                log.warn("用户{}无权限访问：{}", userId, permissionCode);
                throw new BusinessException("无权限执行此操作");
            }

            // 检查模块权限
            if (SmartStringUtil.isNotEmpty(businessPermission.moduleCode())) {
                if (!businessPermissionService.hasModulePermission(userId, businessPermission.moduleCode())) {
                    log.warn("用户{}无模块权限：{}", userId, businessPermission.moduleCode());
                    throw new BusinessException("无权限访问此模块");
                }
            }

            // 检查数据权限
            if (businessPermission.checkDataPermission()) {
                checkDataPermission(userId, joinPoint);
            }

            return joinPoint.proceed();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("权限检查失败", e);
            throw new BusinessException("权限验证失败");
        }
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从请求上下文获取用户ID
        return SmartRequestUtil.getUserId();
    }

    /**
     * 检查数据权限
     */
    private void checkDataPermission(Long userId, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // TODO: 实现数据权限检查逻辑
        // 根据方法参数和用户权限检查数据访问权限
    }
}
```

## 4. 门禁系统集成方案

### 4.1 门禁权限控制

```java
package net.lab1024.sa.base.module.controller.access;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.base.common.domain.ResponseResult;
import net.lab1024.sa.base.common.permission.BusinessPermission;
import net.lab1024.sa.base.module.service.access.AccessDeviceService;
import net.lab1024.sa.base.module.service.access.dto.AccessDeviceDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "门禁设备管理", description = "门禁设备管理接口")
@RequestMapping("/api/access/device")
public class AccessDeviceController {

    private final AccessDeviceService deviceService;

    @Operation(summary = "获取设备列表")
    @GetMapping("/list")
    @BusinessPermission(value = "access:device:view", moduleCode = "ACCESS")
    public ResponseResult<List<AccessDeviceDTO>> getDeviceList() {
        List<AccessDeviceDTO> devices = deviceService.getDeviceList();
        return ResponseResult.ok(devices);
    }

    @Operation(summary = "新增设备")
    @PostMapping("/add")
    @BusinessPermission(value = "access:device:add", moduleCode = "ACCESS")
    public ResponseResult<Void> addDevice(@RequestBody AccessDeviceDTO deviceDTO) {
        deviceService.addDevice(deviceDTO);
        return ResponseResult.ok();
    }

    @Operation(summary = "编辑设备")
    @PostMapping("/edit")
    @BusinessPermission(value = "access:device:edit", moduleCode = "ACCESS")
    public ResponseResult<Void> editDevice(@RequestBody AccessDeviceDTO deviceDTO) {
        deviceService.updateDevice(deviceDTO);
        return ResponseResult.ok();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{deviceId}")
    @BusinessPermission(value = "access:device:delete", moduleCode = "ACCESS")
    public ResponseResult<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseResult.ok();
    }

    @Operation(summary = "远程开门")
    @PostMapping("/{deviceId}/open")
    @BusinessPermission(value = "access:device:control", moduleCode = "ACCESS")
    public ResponseResult<Void> remoteOpenDoor(@PathVariable Long deviceId) {
        deviceService.remoteOpenDoor(deviceId);
        return ResponseResult.ok();
    }
}
```

### 4.2 门禁审批流程集成

```java
package net.lab1024.sa.base.module.service.access;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.base.module.entity.access.AccessAuthEntity;
import net.lab1024.sa.base.module.service.workflow.ApprovalBusinessService;
import net.lab1024.sa.base.module.service.workflow.dto.ApprovalStartDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 门禁授权服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessAuthService {

    private final ApprovalBusinessService approvalService;
    private final AccessDeviceService deviceService;
    private final SmartPersonService personService;

    /**
     * 申请门禁授权
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyAccessAuth(AccessAuthDTO authDTO, Long userId) {
        try {
            // 1. 生成业务编码
            String businessCode = generateBusinessCode("ACCESS_AUTH");

            // 2. 构建审批参数
            Map<String, Object> variables = new HashMap<>();
            variables.put("personId", authDTO.getPersonId());
            variables.put("deviceId", authDTO.getDeviceId());
            variables.put("authType", authDTO.getAuthType());
            variables.put("startTime", authDTO.getStartTime());
            variables.put("endTime", authDTO.getEndTime());
            variables.put("reason", authDTO.getReason());

            // 获取人员和设备信息
            var person = personService.getPersonById(authDTO.getPersonId());
            var device = deviceService.getDeviceById(authDTO.getDeviceId());

            // 3. 启动审批流程
            ApprovalStartDTO startDTO = new ApprovalStartDTO();
            startDTO.setBusinessType("ACCESS_AUTH");
            startDTO.setBusinessCode(businessCode);
            startDTO.setBusinessTitle(String.format("%s申请门禁授权", person.getPersonName()));
            startDTO.setVariables(variables);
            startDTO.setFormData(variables);
            startDTO.setRemark(authDTO.getReason());

            approvalService.startApproval(startDTO, userId);

            // 4. 创建临时授权记录（待审批状态）
            createTempAuth(authDTO, businessCode, userId);

            log.info("门禁授权申请提交成功：{}", businessCode);

        } catch (Exception e) {
            log.error("门禁授权申请失败", e);
            throw new BusinessException("门禁授权申请失败");
        }
    }

    /**
     * 审批通过后激活授权
     */
    @Transactional(rollbackFor = Exception.class)
    public void activateAuthAfterApproval(String businessCode) {
        try {
            // 获取临时授权记录
            AccessAuthEntity tempAuth = getTempAuthByBusinessCode(businessCode);
            if (tempAuth == null) {
                log.warn("未找到临时授权记录：{}", businessCode);
                return;
            }

            // 激活授权
            tempAuth.setStatus(1); // 已激活
            updateAuth(tempAuth);

            // 下发授权到设备
            deviceService.grantAccess(tempAuth.getDeviceId(), tempAuth.getPersonId(),
                    tempAuth.getStartTime(), tempAuth.getEndTime());

            log.info("门禁授权激活成功：{}", businessCode);

        } catch (Exception e) {
            log.error("激活门禁授权失败", e);
            throw new BusinessException("激活授权失败");
        }
    }

    /**
     * 审批拒绝后取消授权
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelAuthAfterRejection(String businessCode) {
        try {
            AccessAuthEntity tempAuth = getTempAuthByBusinessCode(businessCode);
            if (tempAuth != null) {
                tempAuth.setStatus(2); // 已拒绝
                updateAuth(tempAuth);
            }

            log.info("门禁授权已取消：{}", businessCode);

        } catch (Exception e) {
            log.error("取消门禁授权失败", e);
        }
    }

    /**
     * 生成业务编码
     */
    private String generateBusinessCode(String businessType) {
        return businessType + "_" + System.currentTimeMillis();
    }

    /**
     * 创建临时授权记录
     */
    private void createTempAuth(AccessAuthDTO authDTO, String businessCode, Long userId) {
        AccessAuthEntity auth = new AccessAuthEntity();
        auth.setBusinessCode(businessCode);
        auth.setPersonId(authDTO.getPersonId());
        auth.setDeviceId(authDTO.getDeviceId());
        auth.setAuthType(authDTO.getAuthType());
        auth.setStartTime(authDTO.getStartTime());
        auth.setEndTime(authDTO.getEndTime());
        auth.setStatus(0); // 待审批
        auth.setReason(authDTO.getReason());
        auth.setCreateBy(userId);
        saveAuth(auth);
    }
}
```

## 5. 考勤系统集成方案

### 5.1 考勤权限控制

```java
package net.lab1024.sa.base.module.controller.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.base.common.domain.ResponseResult;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.permission.BusinessPermission;
import net.lab1024.sa.base.module.service.attendance.AttendanceLeaveService;
import net.lab1024.sa.base.module.service.attendance.dto.AttendanceLeaveDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "考勤请假管理", description = "考勤请假管理接口")
@RequestMapping("/api/attendance/leave")
public class AttendanceLeaveController {

    private final AttendanceLeaveService leaveService;

    @Operation(summary = "获取请假列表")
    @GetMapping("/list")
    @BusinessPermission(value = "attendance:leave:view", moduleCode = "ATTENDANCE")
    public ResponseResult<PageResult<AttendanceLeaveDTO>> getLeaveList(AttendanceLeaveQueryDTO queryDTO) {
        PageResult<AttendanceLeaveDTO> result = leaveService.getLeaveList(queryDTO);
        return ResponseResult.ok(result);
    }

    @Operation(summary = "申请请假")
    @PostMapping("/apply")
    @BusinessPermission(value = "attendance:leave:add", moduleCode = "ATTENDANCE")
    public ResponseResult<Void> applyLeave(@RequestBody AttendanceLeaveDTO leaveDTO) {
        leaveService.applyLeave(leaveDTO);
        return ResponseResult.ok();
    }

    @Operation(summary = "审批请假")
    @PostMapping("/approve")
    @BusinessPermission(value = "attendance:leave:approve", moduleCode = "ATTENDANCE")
    public ResponseResult<Void> approveLeave(@RequestBody AttendanceLeaveApproveDTO approveDTO) {
        leaveService.approveLeave(approveDTO);
        return ResponseResult.ok();
    }

    @Operation(summary = "取消请假")
    @PostMapping("/{leaveId}/cancel")
    @BusinessPermission(value = "attendance:leave:cancel", moduleCode = "ATTENDANCE")
    public ResponseResult<Void> cancelLeave(@PathVariable Long leaveId) {
        leaveService.cancelLeave(leaveId);
        return ResponseResult.ok();
    }
}
```

### 5.2 考勤审批流程集成

```java
package net.lab1024.sa.base.module.service.attendance.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.base.module.entity.attendance.AttendanceLeaveEntity;
import net.lab1024.sa.base.module.service.attendance.AttendanceLeaveService;
import net.lab1024.sa.base.module.service.workflow.ApprovalBusinessService;
import org.dromara.warm.flow.core.handler.DataHandler;
import org.dromara.warm.flow.core.task.Task;
import org.dromara.warm.flow.orm.entity.Instance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 考勤请假流程处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceLeaveHandler implements DataHandler {

    private final AttendanceLeaveService leaveService;
    private final ApprovalBusinessService approvalService;

    @Override
    public List<Task> handle(Instance instance, List<Data> dataList, Map<String, Object> variables) {
        List<Task> taskList = new ArrayList<>();

        try {
            String businessCode = (String) variables.get("businessCode");
            Long personId = Long.valueOf(variables.get("personId").toString());
            Integer leaveDays = (Integer) variables.get("leaveDays");
            String leaveType = (String) variables.get("leaveType");

            // 创建请假记录
            AttendanceLeaveEntity leave = new AttendanceLeaveEntity();
            leave.setBusinessCode(businessCode);
            leave.setPersonId(personId);
            leave.setLeaveType(leaveType);
            leave.setLeaveDays(leaveDays);
            leave.setStartTime((java.time.LocalDateTime) variables.get("startTime"));
            leave.setEndTime((java.time.LocalDateTime) variables.get("endTime"));
            leave.setReason((String) variables.get("reason"));
            leave.setStatus(0); // 待审批
            leave.setInstanceId(instance.getId());
            leaveService.save(leave);

            // 根据请假天数确定审批流程
            if (leaveDays <= 1) {
                // 1天以内：直属上级审批
                taskList.add(createSupervisorTask(instance, leave, variables));
            } else if (leaveDays <= 3) {
                // 1-3天：直属上级 + 部门经理审批
                taskList.add(createSupervisorTask(instance, leave, variables));
                taskList.add(createManagerTask(instance, leave, variables));
            } else {
                // 3天以上：直属上级 + 部门经理 + HR审批
                taskList.add(createSupervisorTask(instance, leave, variables));
                taskList.add(createManagerTask(instance, leave, variables));
                taskList.add(createHrTask(instance, leave, variables));
            }

            log.info("考勤请假流程处理完成，生成{}个任务", taskList.size());

        } catch (Exception e) {
            log.error("考勤请假流程处理失败", e);
            throw new RuntimeException("请假流程处理失败", e);
        }

        return taskList;
    }

    /**
     * 创建直属上级审批任务
     */
    private Task createSupervisorTask(Instance instance, AttendanceLeaveEntity leave, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("supervisor_approval");
        task.setNodeName("直属上级审批");
        task.setHandlerType(0);

        Long supervisorId = getSupervisorId(leave.getPersonId());
        task.setHandler(supervisorId.toString());

        Map<String, Object> taskVariables = Map.of(
            "leaveId", leave.getLeaveId(),
            "businessCode", leave.getBusinessCode(),
            "taskType", "supervisor_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    /**
     * 创建部门经理审批任务
     */
    private Task createManagerTask(Instance instance, AttendanceLeaveEntity leave, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("manager_approval");
        task.setNodeName("部门经理审批");
        task.setHandlerType(1);
        task.setHandler("attendanceManagerApprovalHandler");

        Map<String, Object> taskVariables = Map.of(
            "leaveId", leave.getLeaveId(),
            "businessCode", leave.getBusinessCode(),
            "taskType", "manager_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    /**
     * 创建HR审批任务
     */
    private Task createHrTask(Instance instance, AttendanceLeaveEntity leave, Map<String, Object> variables) {
        Task task = new Task();
        task.setInstance(instance);
        task.setNodeCode("hr_approval");
        task.setNodeName("HR审批");
        task.setHandlerType(0);
        task.setHandler(getHrManagerId().toString());

        Map<String, Object> taskVariables = Map.of(
            "leaveId", leave.getLeaveId(),
            "businessCode", leave.getBusinessCode(),
            "taskType", "hr_approval"
        );
        task.setVariables(SmartStringUtil.toJsonString(taskVariables));

        return task;
    }

    private Long getSupervisorId(Long personId) {
        // TODO: 获取直属上级ID
        return 1001L;
    }

    private Long getHrManagerId() {
        // TODO: 获取HR经理ID
        return 2001L;
    }
}
```

## 6. 前端权限控制

### 6.1 角色权限配置页面

```vue
<!-- src/views/system/role/RolePermission.vue -->
<template>
  <div class="role-permission">
    <div class="permission-header">
      <h3>角色业务权限配置</h3>
      <a-space>
        <a-button type="primary" @click="savePermissions" :loading="saving">
          保存权限
        </a-button>
        <a-button @click="resetPermissions">重置</a-button>
      </a-space>
    </div>

    <div class="permission-content">
      <a-collapse v-model:activeKey="activeKey" accordion>
        <!-- 门禁系统权限 -->
        <a-collapse-panel key="ACCESS" header="门禁系统权限">
          <div class="module-section">
            <div class="module-control">
              <a-checkbox
                v-model:checked="permissions.ACCESS.moduleEnabled"
                @change="onModuleChange('ACCESS', $event)"
              >
                启用门禁系统模块
              </a-checkbox>
            </div>

            <div v-if="permissions.ACCESS.moduleEnabled" class="menu-permissions">
              <h4>菜单权限配置</h4>
              <a-tree
                v-model:checkedKeys="permissions.ACCESS.selectedMenus"
                :tree-data="accessMenuTree"
                checkable
                :check-strictly="false"
                @check="onMenuCheck('ACCESS', $event)"
              >
                <template #title="{ title, key, permissions }">
                  <span>{{ title }}</span>
                  <div v-if="permissions && permissions.length" class="permission-tags">
                    <a-tag
                      v-for="perm in permissions"
                      :key="perm.code"
                      :color="getPermissionColor(perm.code)"
                      size="small"
                    >
                      {{ perm.name }}
                    </a-tag>
                  </div>
                </template>
              </a-tree>
            </div>
          </div>
        </a-collapse-panel>

        <!-- 考勤系统权限 -->
        <a-collapse-panel key="ATTENDANCE" header="考勤系统权限">
          <div class="module-section">
            <div class="module-control">
              <a-checkbox
                v-model:checked="permissions.ATTENDANCE.moduleEnabled"
                @change="onModuleChange('ATTENDANCE', $event)"
              >
                启用考勤系统模块
              </a-checkbox>
            </div>

            <div v-if="permissions.ATTENDANCE.moduleEnabled" class="menu-permissions">
              <h4>菜单权限配置</h4>
              <a-tree
                v-model:checkedKeys="permissions.ATTENDANCE.selectedMenus"
                :tree-data="attendanceMenuTree"
                checkable
                @check="onMenuCheck('ATTENDANCE', $event)"
              >
                <template #title="{ title, key, permissions }">
                  <span>{{ title }}</span>
                  <div v-if="permissions && permissions.length" class="permission-tags">
                    <a-tag
                      v-for="perm in permissions"
                      :key="perm.code"
                      :color="getPermissionColor(perm.code)"
                      size="small"
                    >
                      {{ perm.name }}
                    </a-tag>
                  </div>
                </template>
              </a-tree>
            </div>
          </div>
        </a-collapse-panel>

        <!-- 其他业务模块... -->
      </a-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getRolePermissions, saveRolePermissions } from '@/api/system/role';

// 响应式数据
const saving = ref(false);
const activeKey = ref(['ACCESS']);
const roleId = ref(null);

const permissions = reactive({
  ACCESS: {
    moduleEnabled: false,
    selectedMenus: [],
    selectedPermissions: []
  },
  ATTENDANCE: {
    moduleEnabled: false,
    selectedMenus: [],
    selectedPermissions: []
  },
  CONSUMPTION: {
    moduleEnabled: false,
    selectedMenus: [],
    selectedPermissions: []
  },
  VIDEO: {
    moduleEnabled: false,
    selectedMenus: [],
    selectedPermissions: []
  },
  VISITOR: {
    moduleEnabled: false,
    selectedMenus: [],
    selectedPermissions: []
  }
});

// 菜单树数据
const accessMenuTree = ref([
  {
    title: '门禁设备',
    key: 'access_device',
    permissions: [
      { code: 'view', name: '查看' },
      { code: 'add', name: '新增' },
      { code: 'edit', name: '编辑' },
      { code: 'delete', name: '删除' },
      { code: 'control', name: '控制' }
    ]
  },
  {
    title: '通行记录',
    key: 'access_record',
    permissions: [
      { code: 'view', name: '查看' },
      { code: 'export', name: '导出' }
    ]
  },
  {
    title: '门禁授权',
    key: 'access_auth',
    permissions: [
      { code: 'view', name: '查看' },
      { code: 'add', name: '新增' },
      { code: 'edit', name: '编辑' },
      { code: 'delete', name: '删除' },
      { code: 'approve', name: '审批' }
    ]
  }
]);

const attendanceMenuTree = ref([
  {
    title: '排班管理',
    key: 'attendance_schedule',
    permissions: [
      { code: 'view', name: '查看' },
      { code: 'add', name: '新增' },
      { code: 'edit', name: '编辑' },
      { code: 'delete', name: '删除' }
    ]
  },
  {
    title: '打卡记录',
    key: 'attendance_record',
    permissions: [
      { code: 'view', name: '查看' },
      { code: 'export', name: '导出' }
    ]
  },
  {
    title: '请假管理',
    key: 'attendance_leave',
    permissions: [
      { code: 'view', name: '查看' },
      { code: 'add', name: '申请' },
      { code: 'edit', name: '编辑' },
      { code: 'approve', name: '审批' },
      { code: 'cancel', name: '取消' }
    ]
  }
]);

// 方法
const loadPermissions = async () => {
  try {
    const result = await getRolePermissions(roleId.value);
    if (result.data) {
      // 更新权限数据
      Object.keys(result.data).forEach(moduleCode => {
        if (permissions[moduleCode]) {
          permissions[moduleCode].moduleEnabled = result.data[moduleCode].hasPermission;
          permissions[moduleCode].selectedMenus = extractSelectedMenus(result.data[moduleCode].menus);
        }
      });
    }
  } catch (error) {
    message.error('加载权限配置失败');
  }
};

const savePermissions = async () => {
  saving.value = true;
  try {
    const permissionData = {
      roleId: roleId.value,
      permissions: Object.keys(permissions).map(moduleCode => ({
        moduleCode,
        moduleEnabled: permissions[moduleCode].moduleEnabled,
        menuPermissions: buildMenuPermissions(moduleCode)
      }))
    };

    await saveRolePermissions(permissionData);
    message.success('权限配置保存成功');
  } catch (error) {
    message.error('保存权限配置失败');
  } finally {
    saving.value = false;
  }
};

const onModuleChange = (moduleCode, event) => {
  if (!event.target.checked) {
    // 禁用模块时，清空所有菜单权限
    permissions[moduleCode].selectedMenus = [];
    permissions[moduleCode].selectedPermissions = [];
  }
};

const onMenuCheck = (moduleCode, checkedKeys) => {
  permissions[moduleCode].selectedMenus = checkedKeys;
};

const extractSelectedMenus = (menus) => {
  // 递归提取选中的菜单项
  const result = [];
  const extract = (items) => {
    items.forEach(item => {
      if (item.permissions && item.permissions.length > 0) {
        result.push(item.menuId);
      }
      if (item.children && item.children.length > 0) {
        extract(item.children);
      }
    });
  };
  extract(menus);
  return result;
};

const buildMenuPermissions = (moduleCode) => {
  // 构建菜单权限数据
  return permissions[moduleCode].selectedMenus.map(menuId => ({
    menuId,
    permissions: getPermissionsByMenuId(moduleCode, menuId)
  }));
};

const getPermissionsByMenuId = (moduleCode, menuId) => {
  // 根据菜单ID获取权限列表
  const menuTree = moduleCode === 'ACCESS' ? accessMenuTree.value : attendanceMenuTree.value;
  const findMenu = (items) => {
    for (const item of items) {
      if (item.key === menuId) {
        return item.permissions || [];
      }
      if (item.children) {
        const found = findMenu(item.children);
        if (found) return found;
      }
    }
    return [];
  };
  return findMenu(menuTree);
};

const getPermissionColor = (permissionCode) => {
  const colors = {
    view: 'blue',
    add: 'green',
    edit: 'orange',
    delete: 'red',
    control: 'purple',
    approve: 'cyan',
    export: 'geekblue'
  };
  return colors[permissionCode] || 'default';
};

const resetPermissions = () => {
  Object.keys(permissions).forEach(moduleCode => {
    permissions[moduleCode].moduleEnabled = false;
    permissions[moduleCode].selectedMenus = [];
    permissions[moduleCode].selectedPermissions = [];
  });
};

// 生命周期
onMounted(() => {
  // 从路由参数获取角色ID
  roleId.value = parseInt(route.params.roleId);
  if (roleId.value) {
    loadPermissions();
  }
});
</script>

<style scoped>
.role-permission {
  padding: 24px;
}

.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.permission-content {
  background: #fff;
  border-radius: 8px;
}

.module-section {
  padding: 16px 0;
}

.module-control {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.menu-permissions h4 {
  margin-bottom: 16px;
  color: #262626;
}

.permission-tags {
  margin-left: 8px;
}

.permission-tags .ant-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}
</style>
```

### 6.2 菜单权限指令

```javascript
// src/directives/permission.js
import { useUserStore } from '@/store/modules/user';

/**
 * 权限指令
 */
export const permission = {
  mounted(el, binding) {
    const userStore = useUserStore();
    const { value } = binding;

    if (value && value instanceof Array && value.length > 0) {
      const permissions = value;
      const hasPermission = userStore.hasPermissions(permissions);

      if (!hasPermission) {
        el.parentNode && el.parentNode.removeChild(el);
      }
    } else {
      throw new Error('权限指令需要传入权限编码数组');
    }
  }
};

/**
 * 模块权限指令
 */
export const modulePermission = {
  mounted(el, binding) {
    const userStore = useUserStore();
    const { value } = binding;

    if (value) {
      const hasModulePermission = userStore.hasModulePermission(value);

      if (!hasModulePermission) {
        el.parentNode && el.parentNode.removeChild(el);
      }
    } else {
      throw new Error('模块权限指令需要传入模块编码');
    }
  }
};
```

## 7. 权限配置示例

### 7.1 系统管理员角色权限配置

```json
{
  "roleId": 1,
  "roleName": "系统管理员",
  "permissions": [
    {
      "moduleCode": "ACCESS",
      "moduleEnabled": true,
      "menuPermissions": [
        {
          "menuId": 1,
          "menuCode": "access_device",
          "permissions": ["view", "add", "edit", "delete", "control"]
        },
        {
          "menuId": 2,
          "menuCode": "access_record",
          "permissions": ["view", "export"]
        },
        {
          "menuId": 3,
          "menuCode": "access_auth",
          "permissions": ["view", "add", "edit", "delete", "approve"]
        }
      ]
    },
    {
      "moduleCode": "ATTENDANCE",
      "moduleEnabled": true,
      "menuPermissions": [
        {
          "menuId": 11,
          "menuCode": "attendance_leave",
          "permissions": ["view", "approve"]
        }
      ]
    }
  ]
}
```

### 7.2 普通用户角色权限配置

```json
{
  "roleId": 2,
  "roleName": "普通员工",
  "permissions": [
    {
      "moduleCode": "ACCESS",
      "moduleEnabled": true,
      "menuPermissions": [
        {
          "menuId": 2,
          "menuCode": "access_record",
          "permissions": ["view"]
        }
      ]
    },
    {
      "moduleCode": "ATTENDANCE",
      "moduleEnabled": true,
      "menuPermissions": [
        {
          "menuId": 12,
          "menuCode": "attendance_record",
          "permissions": ["view"]
        },
        {
          "menuId": 13,
          "menuCode": "attendance_leave",
          "permissions": ["view", "add", "cancel"]
        }
      ]
    }
  ]
}
```

## 8. 总结

### 8.1 集成特点

1. **统一权限模型**: 所有业务模块遵循统一的权限控制标准
2. **细粒度控制**: 支持模块、菜单、按钮、数据四级权限控制
3. **审批流集成**: 业务流程与warm-flow审批引擎无缝集成
4. **前端权限控制**: 基于Vue的权限指令和组件级权限控制
5. **角色权限配置**: 可视化的角色权限配置界面

### 8.2 应用场景

1. **门禁系统**: 设备管理、权限控制、远程操作、授权审批
2. **考勤系统**: 打卡记录、请假申请、加班管理、审批流程
3. **消费系统**: 消费记录、充值管理、退款审批
4. **智能视频**: 监控查看、录像回放、设备管理
5. **访客系统**: 访客预约、来访登记、审批流程

### 8.3 技术优势

1. **模块化设计**: 各业务模块独立，易于维护和扩展
2. **权限继承**: 支持权限继承和权限组合
3. **动态权限**: 支持动态权限配置和实时生效
4. **审批集成**: 业务流程与审批引擎深度集成
5. **用户体验**: 直观的权限配置和管理界面

通过这个完整的业务模块与权限集成方案，SmartAdmin系统能够为不同业务场景提供灵活、安全、易用的权限管理能力，确保系统的安全性和可管理性。