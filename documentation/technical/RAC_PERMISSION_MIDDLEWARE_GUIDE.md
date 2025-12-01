# RAC权限中间层使用指南

## 概述

RAC（Resource-Action-Condition）权限中间层为SmartAdmin v3提供了企业级的细粒度权限控制能力，支持资源-动作-条件三位一体的权限模型和数据域权限控制。

## 核心概念

### 1. RAC权限模型
- **Resource（资源）**: 系统中的受控资源，如 `smart:access:device`
- **Action（动作）**: 对资源的操作类型，如 `READ`、`WRITE`、`DELETE`
- **Condition（条件）**: 权限生效的条件，包括数据域、时间、IP等

### 2. 数据域权限
- **ALL**: 全部数据权限
- **AREA**: 区域数据权限，限定可访问的区域范围
- **DEPT**: 部门数据权限，限定可访问的部门范围
- **SELF**: 个人数据权限，仅限访问本人数据
- **CUSTOM**: 自定义数据域权限

## 后端使用

### 1. 数据库模型

RAC权限中间层依赖以下核心数据表：

```sql
-- 人员多凭证表
CREATE TABLE `t_person_credential` (
  `credential_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '凭证ID',
  `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
  `credential_type` VARCHAR(32) NOT NULL COMMENT '凭证类型(CARD|BIOMETRIC|PASSWORD)',
  `credential_value` VARCHAR(256) NOT NULL COMMENT '凭证值',
  UNIQUE KEY `uk_person_type_value` (`person_id`, `credential_type`, `credential_value`)
) ENGINE=InnoDB COMMENT='人员多凭证表';

-- 人员区域授权表
CREATE TABLE `t_area_person` (
  `area_id` BIGINT(20) NOT NULL COMMENT '区域ID',
  `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
  `data_scope` VARCHAR(32) NOT NULL DEFAULT 'SELF' COMMENT '数据域',
  `start_time` DATETIME NOT NULL COMMENT '生效时间',
  `end_time` DATETIME COMMENT '失效时间',
  PRIMARY KEY (`area_id`, `person_id`)
) ENGINE=InnoDB COMMENT='人员区域授权表';

-- RBAC资源表
CREATE TABLE `t_rbac_resource` (
  `resource_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `resource_code` VARCHAR(128) NOT NULL COMMENT '资源编码',
  `resource_name` VARCHAR(256) NOT NULL COMMENT '资源名称',
  `resource_type` VARCHAR(32) NOT NULL COMMENT '资源类型(API|MENU|BUTTON|DATA)',
  PRIMARY KEY (`resource_id`),
  UNIQUE KEY `uk_resource_code` (`resource_code`)
) ENGINE=InnoDB COMMENT='RBAC资源表';
```

### 2. @RequireResource注解

在Controller方法上使用RAC权限注解：

```java
@RestController
@RequestMapping("/api/smart/access/device")
public class AccessDeviceController {

    @PostMapping("/page")
    @SaCheckPermission("smart:access:device:query")
    @RequireResource(
        resource = "smart:access:device",
        action = "READ",
        dataScope = DataScope.AREA,
        message = "您没有权限查询门禁设备列表"
    )
    public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(@RequestBody PageParam pageParam) {
        // 业务逻辑
    }
}
```

**注解参数说明**：
- `resource`: 资源编码，格式为 `模块:子模块:资源`
- `action`: 动作类型，如 `READ`、`WRITE`、`DELETE`
- `dataScope`: 数据域类型，如 `AREA`、`DEPT`、`SELF`
- `message`: 权限拒绝时的提示消息

### 3. 编程式权限检查

在Service或Manager层进行程序化权限检查：

```java
@Service
public class AccessDeviceService {

    @Resource
    private ResourcePermissionUtil permissionUtil;

    public List<AccessDeviceEntity> getDevicesByArea(Long areaId) {
        // 检查区域权限
        if (!permissionUtil.hasAreaPermission(areaId)) {
            throw new SmartException("您没有权限访问该区域");
        }

        // 查询设备列表
        return deviceMapper.selectByAreaId(areaId);
    }
}
```

## 前端使用

### 1. v-permission指令扩展

支持RAC权限模型的Vue指令：

```vue
<template>
  <!-- 基础用法：资源-动作权限检查 -->
  <button v-permission="'smart:access:device:write'">编辑设备</button>

  <!-- 数组权限检查（OR逻辑） -->
  <div v-permission="['smart:access:device:write', 'smart:access:device:delete']">
    <button>编辑</button>
    <button>删除</button>
  </div>

  <!-- 对象格式权限检查 -->
  <button v-permission="{
    resource: 'smart:access:device',
    action: 'WRITE',
    dataScope: 'AREA',
    areaId: currentAreaId
  }">
    编辑设备
  </button>

  <!-- 修饰符用法 -->
  <div v-permission:resource="'DEVICE_CODE'">设备操作</div>
  <div v-permission:dataScope="'AREA'">区域数据</div>
  <div v-permission:role="'ADMIN'">管理员功能</div>
</template>
```

### 2. 权限组件使用

#### PermissionWrapper - 权限包装组件

```vue
<template>
  <PermissionWrapper
    :resource="'smart:access:device'"
    :action="'WRITE'"
    :dataScope="'AREA'"
    :areaId="currentAreaId"
    :show-fallback="true"
  >
    <DeviceForm />

    <template #fallback>
      <a-empty description="您没有权限操作设备" />
    </template>
  </PermissionWrapper>
</template>
```

#### PermissionButton - 权限按钮组件

```vue
<template>
  <PermissionButton
    :resource="'smart:access:device'"
    :action="'DELETE'"
    :dataScope="'AREA'"
    :areaId="device.areaId"
    type="danger"
    @click="deleteDevice"
    @permission-denied="showPermissionError"
  >
    删除设备
  </PermissionButton>
</template>
```

### 3. 路由守卫

```javascript
import { createPermissionGuard } from '@/router/permission-guard'

const router = createRouter({
  routes: [
    {
      path: '/access/device',
      component: () => import('@/views/access/DeviceList.vue'),
      meta: {
        requireAuth: true,
        permission: {
          resource: 'smart:access:device',
          action: 'READ',
          dataScope: 'AREA'
        }
      }
    }
  ]
})

// 注册权限守卫
router.beforeEach(createPermissionGuard(router))
```

### 4. 组合式API

```javascript
import { usePermission, useDataScope } from '@/components/permission'

export default {
  setup() {
    const { hasPermission, canRead, canWrite, isSuperAdmin } = usePermission()
    const { hasDataScope, hasAreaPermission } = useDataScope()

    const checkDevicePermission = async () => {
      const canEdit = await hasPermission('smart:access:device', 'WRITE')
      const canAccessArea = await hasAreaPermission(areaId)
      return canEdit && canAccessArea
    }

    return {
      checkDevicePermission,
      hasPermission,
      canRead,
      canWrite,
      isSuperAdmin
    }
  }
}
```

### 5. 权限工具函数

```javascript
import { permissionHelper } from '@/utils/permission-helper'

// 批量权限检查
const checkPermissions = async () => {
  const result = await permissionHelper.batchCheckPermissions([
    'smart:access:device:read',
    'smart:access:device:write',
    'smart:access:device:delete'
  ])

  console.log('权限检查结果:', result.hasPermission)
}

// 获取权限概览
const getPermissionOverview = async () => {
  const overview = await permissionHelper.getPermissionOverview()
  console.log('权限概览:', overview)
}
```

## 常见使用场景

### 1. 设备管理

```vue
<template>
  <div>
    <!-- 设备列表 - 支持区域权限过滤 -->
    <PermissionWrapper
      resource="smart:access:device"
      action="READ"
      dataScope="AREA"
      :areaId="currentAreaId"
    >
      <DeviceTable />
    </PermissionWrapper>

    <!-- 远程开门 - 需要区域权限 -->
    <PermissionButton
      resource="smart:access:device"
      action="WRITE"
      dataScope="AREA"
      :areaId="device.areaId"
      @click="remoteOpen"
    >
      远程开门
    </PermissionButton>
  </div>
</template>
```

### 2. 考勤管理

```vue
<template>
  <div>
    <!-- 打卡操作 - 仅限个人权限 -->
    <PermissionWrapper
      resource="smart:attendance:punch"
      action="WRITE"
      dataScope="SELF"
    >
      <PunchButton />
    </PermissionWrapper>

    <!-- 考勤记录查询 - 支持部门权限 -->
    <PermissionWrapper
      resource="smart:attendance:record"
      action="READ"
      dataScope="DEPT"
      :deptId="userDeptId"
    >
      <AttendanceTable />
    </PermissionWrapper>
  </div>
</template>
```

### 3. 消费管理

```vue
<template>
  <div>
    <!-- 消费支付 - 仅限个人权限 -->
    <PermissionWrapper
      resource="smart:consume:account"
      action="WRITE"
      dataScope="SELF"
    >
      <ConsumeForm />
    </PermissionWrapper>

    <!-- 消费记录查询 - 支持部门权限 -->
    <PermissionWrapper
      resource="smart:consume:record"
      action="READ"
      dataScope="DEPT"
    >
      <ConsumeTable />
    </PermissionWrapper>
  </div>
</template>
```

## 权限配置示例

### 1. 门禁管理员权限

```javascript
// 资源权限配置
const accessAdminPermissions = [
  'smart:access:device:read',      // 查看设备
  'smart:access:device:write',     // 管理设备
  'smart:access:device:control',   // 控制设备
  'smart:access:area:read',        // 查看区域
  'smart:access:area:write',       // 管理区域
  'smart:access:person:read',      // 查看人员
  'smart:access:person:write'       // 管理人员
]
```

### 2. 考勤管理员权限

```javascript
// 考勤管理员权限
const attendanceAdminPermissions = [
  'smart:attendance:schedule:read',   // 查看排班
  'smart:attendance:schedule:write',  // 管理排班
  'smart:attendance:rule:read',       // 查看规则
  'smart:attendance:rule:write',      // 管理规则
  'smart:attendance:record:read',     // 查看记录
  'smart:attendance:punch:write'      // 打卡操作
]
```

### 3. 消费管理员权限

```javascript
// 消费管理员权限
const consumeAdminPermissions = [
  'smart:consume:record:read',       // 查看消费记录
  'smart:consume:account:read',      // 查看账户
  'smart:consume:account:write',     // 管理账户
  'smart:consume:refund:write',      // 退款操作
  'smart:consume:statistics:read'    // 查看统计
]
```

## 最佳实践

### 1. 权限设计原则

- **最小权限原则**: 只授予必要的最小权限
- **职责分离**: 不同角色分离权限范围
- **数据域隔离**: 合理使用数据域控制数据访问范围

### 2. 前端权限控制

- **多层级控制**: 路由级 + 组件级 + 按钮级权限控制
- **用户体验**: 无权限时提供友好的提示信息
- **性能优化**: 合理使用权限缓存，减少重复检查

### 3. 后端权限验证

- **双重验证**: Controller注解 + Service程序化验证
- **安全日志**: 记录权限检查和拒绝的详细日志
- **异常处理**: 统一处理权限异常，避免信息泄露

### 4. 测试建议

- **权限测试**: 覆盖各种权限组合和数据域场景
- **边界测试**: 测试权限边界情况和异常情况
- **集成测试**: 验证前后端权限控制的一致性

## 故障排查

### 1. 权限检查失败

```javascript
// 启用调试模式
console.log('权限检查失败:', {
  resource: 'smart:access:device',
  action: 'READ',
  userPermissions: permissionManager.getUserPermissions(),
  hasPermission: await permissionManager.hasPermission('smart:access:device', 'READ')
})
```

### 2. 数据域权限问题

```javascript
// 检查数据域权限
const checkDataScope = async () => {
  const accessibleAreas = permissionManager.getAccessibleAreas()
  const accessibleDepts = permissionManager.getAccessibleDepts()

  console.log('可访问区域:', accessibleAreas)
  console.log('可访问部门:', accessibleDepts)
}
```

### 3. 权限缓存问题

```javascript
// 清除权限缓存
permissionHelper.clearCache()

// 刷新权限缓存
await permissionHelper.refreshCache()
```

## 总结

RAC权限中间层提供了完整的前后端权限控制解决方案，支持细粒度的资源权限控制和灵活的数据域权限管理。通过合理使用，可以有效保障系统安全性和数据的访问控制。