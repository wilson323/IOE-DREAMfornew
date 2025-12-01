# RAC统一权限中间件使用指南

## 概述

RAC (Resource-Action-Condition) 统一权限中间件为SmartAdmin v3提供了完整的权限控制解决方案，支持：

- **资源操作权限控制**: 精确到具体操作的权限验证
- **数据域权限过滤**: AREA/DEPT/SELF/CUSTOM数据域控制
- **角色权限管理**: 灵活的角色权限检查
- **前后端统一权限**: 一套权限体系贯穿前后端

## 快速开始

### 1. 后端权限控制

#### 1.1 使用@RequireResource注解

```java
@RestController
@RequestMapping("/api/smart/access/device")
public class AccessDeviceController {

    @PostMapping("/page")
    @RequireResource(
        code = RequireResource.ResourceCode.ACCESS_DEVICE_VIEW,
        scope = RequireResource.DataScope.AREA,
        action = RequireResource.Action.READ,
        description = "查询门禁设备列表，支持区域数据域过滤"
    )
    public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(
            @Valid @RequestBody PageParam pageParam) {
        // 业务逻辑
        return ResponseDTO.ok(result);
    }

    @PostMapping("/control/{deviceId}")
    @RequireResource(
        code = RequireResource.ResourceCode.ACCESS_DEVICE_CONTROL,
        scope = RequireResource.DataScope.AREA,
        action = RequireResource.Action.WRITE,
        description = "远程控制门禁设备"
    )
    public ResponseDTO<String> controlDevice(@PathVariable Long deviceId) {
        // 业务逻辑
        return ResponseDTO.ok("设备控制成功");
    }
}
```

#### 1.2 资源码常量引用

```java
import static net.lab1024.sa.base.common.constant.ResourceCodeConst.*;

public class DeviceController {

    @GetMapping("/list")
    @RequireResource(
        code = AccessDevice.VIEW,  // 使用常量
        scope = RequireResource.DataScope.AREA,
        action = RequireResource.Action.READ
    )
    public ResponseDTO<List<DeviceEntity>> getDeviceList() {
        // 业务逻辑
    }
}
```

#### 1.3 在Service层获取权限上下文

```java
@Service
public class AccessDeviceService {

    public PageResult<AccessDeviceEntity> getDevicePage(PageParam pageParam) {
        // 获取当前权限上下文
        AuthorizationContext context = AuthorizationContextHolder.getCurrentContext();

        // 检查用户权限
        if (context == null || !context.isSuperAdmin()) {
            // 应用数据域过滤
            String dataScopeCondition = AuthorizationContextHolder.getDataScopeSqlCondition("t_device", "d");
            // 在查询中应用数据域条件
        }

        // 业务逻辑实现
        return deviceRepository.findPage(pageParam, dataScopeCondition);
    }
}
```

### 2. 前端权限控制

#### 2.1 Vue指令权限控制

```vue
<template>
  <!-- 基础权限检查 -->
  <button v-permission="'ACCESS_DEVICE_VIEW'">查看设备</button>

  <!-- 数据域权限检查 -->
  <button v-permission:dataScope="'AREA'">区域设备管理</button>
  <button v-permission:dataScope.self>个人设备查看</button>

  <!-- 角色权限检查 -->
  <button v-permission:role="'ADMIN'">管理员功能</button>
  <button v-permission:role.any="['ADMIN', 'MANAGER']">管理员或经理</button>
  <button v-permission:role.all="['USER', 'MEMBER']">用户且会员</button>

  <!-- 权限显示指令（无权限时移除元素） -->
  <div v-permission-show="'ACCESS_DEVICE_CONTROL'">
    <button>设备控制面板</button>
  </div>

  <!-- 权限禁用指令（无权限时禁用元素） -->
  <button v-permission-disabled="'ACCESS_DEVICE_DELETE'">删除设备</button>
</template>
```

#### 2.2 JavaScript权限检查

```javascript
import { usePermissionStore } from '@/stores/smart-permission'
import ResourceCode from '@/constants/resource-code'

export default {
  setup() {
    const permissionStore = usePermissionStore()

    // 检查资源权限
    const canViewDevice = permissionStore.hasPermission(ResourceCode.AccessDevice.VIEW)
    const canControlDevice = permissionStore.hasPermission(ResourceCode.AccessDevice.CONTROL)

    // 检查数据域权限
    const hasAreaScope = permissionStore.hasDataScope(ResourceCode.DataScope.AREA)
    const hasSelfScope = permissionStore.hasDataScope(ResourceCode.DataScope.SELF)

    // 检查角色权限
    const isAdmin = permissionStore.hasRole('ADMIN')
    const isManager = permissionStore.hasRole('MANAGER')

    // 异步权限检查
    const checkPermission = async () => {
      const result = await permissionStore.checkPermission('ACCESS_DEVICE_CONTROL', {
        deviceId: 123,
        action: 'remote_open'
      })

      if (result.granted) {
        console.log('权限检查通过')
      } else {
        console.warn('权限被拒绝:', result.errorMessage)
      }
    }

    return {
      canViewDevice,
      canControlDevice,
      hasAreaScope,
      hasSelfScope,
      isAdmin,
      isManager,
      checkPermission
    }
  }
}
```

#### 2.3 路由权限配置

```javascript
// router/index.js
import { setupPermissionGuard } from '@/router/permission-guard'
import ResourceCode from '@/constants/resource-code'

const routes = [
  {
    path: '/smart/access',
    component: () => import('@/views/smart/access/index.vue'),
    meta: {
      title: '门禁管理',
      requireAuth: true,
      resourceCode: ResourceCode.AccessDevice.VIEW,
      dataScope: ResourceCode.DataScope.AREA,
      requiredRoles: ['ACCESS_ADMIN'],
      roleMatchMode: 'any'  // 'any' 或 'all'
    },
    children: [
      {
        path: 'device',
        component: () => import('@/views/smart/access/device/list.vue'),
        meta: {
          title: '设备列表',
          resourceCode: ResourceCode.AccessDevice.VIEW,
          dataScope: ResourceCode.DataScope.AREA
        }
      },
      {
        path: 'device/control',
        component: () => import('@/views/smart/access/device/control.vue'),
        meta: {
          title: '设备控制',
          resourceCode: ResourceCode.AccessDevice.CONTROL,
          requiredRoles: ['ACCESS_ADMIN', 'DEVICE_OPERATOR']
        }
      }
    ]
  }
]

const router = createRouter({
  routes,
  // ...其他配置
})

// 设置权限守卫
setupPermissionGuard(router)

export default router
```

## 核心组件详解

### 1. @RequireResource注解

#### 1.1 注解参数

```java
@RequireResource(
    code = "RESOURCE_CODE",           // 资源操作码（必需）
    scope = DataScope.AREA,           // 数据域范围（可选）
    action = Action.READ,             // 操作类型（可选）
    description = "权限描述"           // 权限描述（可选）
)
```

#### 1.2 资源码类型

| 模块 | 资源码 | 说明 |
|------|--------|------|
| 门禁设备 | ACCESS_DEVICE_VIEW | 查看门禁设备 |
| 门禁设备 | ACCESS_DEVICE_ADD | 添加门禁设备 |
| 门禁设备 | ACCESS_DEVICE_CONTROL | 控制门禁设备 |
| 考勤管理 | ATTENDANCE_RECORD_VIEW | 查看考勤记录 |
| 考勤管理 | ATTENDANCE_PUNCH_IN | 上班打卡 |
| 消费管理 | CONSUME_RECORD_VIEW | 查看消费记录 |
| 消费管理 | CONSUME_ACCOUNT_MANAGE | 管理消费账户 |

#### 1.3 数据域类型

| 类型 | 说明 | 适用场景 |
|------|------|----------|
| AREA | 区域数据域 | 按区域过滤数据 |
| DEPT | 部门数据域 | 按部门过滤数据 |
| SELF | 个人数据域 | 只能访问自己的数据 |
| CUSTOM | 自定义数据域 | 自定义过滤条件 |

### 2. AuthorizationContext权限上下文

#### 2.1 获取权限上下文

```java
// 获取当前权限上下文
AuthorizationContext context = AuthorizationContextHolder.getCurrentContext();

if (context != null) {
    Long userId = context.getUserId();
    Set<String> roleCodes = context.getRoleCodes();
    String resourceCode = context.getResourceCode();
    String dataScope = context.getDataScope();
    Set<Long> areaIds = context.getAreaIds();
    boolean isSuperAdmin = context.isSuperAdmin();
}
```

#### 2.2 数据域过滤条件

```java
// 获取数据域SQL条件
String sqlCondition = AuthorizationContextHolder.getDataScopeSqlCondition("device", "d");
// 生成SQL: AND (d.area_id IN (1,2,3) OR d.user_id = 123)

// 获取带表别名前缀的条件
String tableCondition = AuthorizationContextHolder.getDataScopeSqlCondition("t_access_device", "dev");

// 获取过滤参数
Map<String, Object> filterArgs = AuthorizationContextHolder.getDataScopeFilterArgs();
```

### 3. PolicyEvaluator策略评估器

#### 3.1 权限策略评估

```java
@Service
public class CustomPermissionService {

    @Resource
    private PolicyEvaluator policyEvaluator;

    public boolean checkCustomPermission(Long userId, String resourceCode, String action) {
        // 构建权限上下文
        AuthorizationContext context = AuthorizationContext.builder()
            .userId(userId)
            .resourceCode(resourceCode)
            .requestedAction(action)
            .build();

        // 评估权限策略
        PolicyEvaluationResult result = policyEvaluator.evaluate(context);

        return result.isGranted();
    }
}
```

## 最佳实践

### 1. 权限设计原则

#### 1.1 最小权限原则

```java
// ✅ 推荐：精确的权限控制
@RequireResource(
    code = AccessDevice.VIEW,
    scope = RequireResource.DataScope.AREA,
    action = RequireResource.Action.READ
)

// ❌ 不推荐：过于宽泛的权限
@RequireResource(code = "ALL_ACCESS")
```

#### 1.2 数据域优先原则

```java
// ✅ 推荐：使用数据域过滤
@RequireResource(
    code = AttendanceRecord.VIEW,
    scope = RequireResource.DataScope.DEPT,  // 按部门过滤
    action = RequireResource.Action.READ
)

// ❌ 不推荐：在业务逻辑中硬编码权限
```

### 2. 前端权限控制最佳实践

#### 2.1 组件权限控制

```vue
<template>
  <!-- 使用权限指令控制UI显示 -->
  <div class="device-actions">
    <!-- 基础操作：所有有查看权限的用户都能看到 -->
    <button v-permission="AccessDevice.VIEW" @click="viewDevice">
      查看详情
    </button>

    <!-- 管理操作：只有控制权限的用户能看到 -->
    <button v-permission="AccessDevice.CONTROL" @click="controlDevice">
      远程控制
    </button>

    <!-- 删除操作：需要删除权限且是管理员 -->
    <button
      v-permission="AccessDevice.DELETE"
      v-permission:role="'ADMIN'"
      @click="deleteDevice"
    >
      删除设备
    </button>
  </div>
</template>
```

#### 2.2 路由权限配置

```javascript
// 配置示例：分层权限控制
const accessRoutes = {
  path: '/access',
  component: AccessLayout,
  meta: {
    // 第一层：基础访问权限
    resourceCode: ResourceCode.AccessDevice.VIEW,
    requireAuth: true
  },
  children: [
    {
      path: 'list',
      component: DeviceList,
      meta: {
        // 第二层：具体功能权限
        resourceCode: ResourceCode.AccessDevice.VIEW,
        dataScope: ResourceCode.DataScope.AREA
      }
    },
    {
      path: 'control',
      component: DeviceControl,
      meta: {
        // 第三层：高级操作权限 + 角色要求
        resourceCode: ResourceCode.AccessDevice.CONTROL,
        requiredRoles: ['ACCESS_ADMIN', 'DEVICE_OPERATOR'],
        roleMatchMode: 'any'
      }
    }
  ]
}
```

### 3. 后端权限控制最佳实践

#### 3.1 Controller层权限控制

```java
@RestController
@RequestMapping("/api/access/device")
public class AccessDeviceController {

    // ✅ 推荐：统一的资源权限控制
    @GetMapping("/page")
    @RequireResource(
        code = AccessDevice.VIEW,
        scope = RequireResource.DataScope.AREA,
        action = RequireResource.Action.READ,
        description = "分页查询门禁设备，应用区域数据域过滤"
    )
    public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(
            @Valid @RequestBody DeviceQueryRequest request) {
        return ResponseDTO.ok(accessDeviceService.getDevicePage(request));
    }

    // ✅ 推荐：敏感操作增加角色检查
    @PostMapping("/batch-delete")
    @RequireResource(
        code = AccessDevice.DELETE,
        action = RequireResource.Action.DELETE,
        description = "批量删除门禁设备，需要管理员角色"
    )
    @PreAuthorize("hasRole('ACCESS_ADMIN')")  // 额外的角色检查
    public ResponseDTO<String> batchDeleteDevices(@RequestBody List<Long> deviceIds) {
        return ResponseDTO.ok(accessDeviceService.batchDeleteDevices(deviceIds));
    }
}
```

#### 3.2 Service层权限上下文使用

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessDeviceService {

    public PageResult<AccessDeviceEntity> getDevicePage(DeviceQueryRequest request) {
        // 获取权限上下文
        AuthorizationContext context = AuthorizationContextHolder.getCurrentContext();

        // 构建查询条件
        LambdaQueryWrapper<AccessDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 基础查询条件
        queryWrapper.eq(request.getDeviceName() != null,
                       AccessDeviceEntity::getDeviceName, request.getDeviceName());

        // 应用数据域权限过滤
        if (context != null && !context.isSuperAdmin()) {
            String dataScopeSql = AuthorizationContextHolder.getDataScopeSqlCondition(
                "t_access_device", "device");
            if (StringUtils.isNotBlank(dataScopeSql)) {
                queryWrapper.apply(dataScopeSql);
            }
        }

        // 执行分页查询
        return accessDeviceRepository.selectPage(request.getPageParam(), queryWrapper);
    }

    public void deleteDevice(Long deviceId) {
        AuthorizationContext context = AuthorizationContextHolder.getCurrentContext();

        // 权限检查（双重保险）
        if (context == null || !context.hasPermission("ACCESS_DEVICE_DELETE")) {
            throw new SmartException("无删除设备权限");
        }

        // 业务逻辑
        AccessDeviceEntity device = accessDeviceRepository.selectById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 执行软删除
        device.setDeletedFlag(true);
        device.setUpdateTime(LocalDateTime.now());
        device.setUpdateUserId(context.getUserId());
        accessDeviceRepository.updateById(device);
    }
}
```

## 错误处理

### 1. 权限拒绝处理

#### 1.1 后端权限异常

```java
@ControllerAdvice
public class PermissionExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseDTO<String> handleAccessDenied(AccessDeniedException e) {
        log.warn("权限访问被拒绝: {}", e.getMessage());
        return ResponseDTO.error(UserErrorCode.NO_PERMISSION, "无权限访问该资源");
    }

    @ExceptionHandler(SmartPermissionException.class)
    public ResponseDTO<String> handlePermissionException(SmartPermissionException e) {
        log.error("权限检查异常: {}", e.getMessage(), e);
        return ResponseDTO.error(UserErrorCode.PERMISSION_CHECK_FAILED,
                                "权限检查失败: " + e.getMessage());
    }
}
```

#### 1.2 前端权限拒绝处理

```javascript
// 路由权限拒绝处理
const routes = [
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      resourceCode: ResourceCode.System.USER_MANAGE,
      onPermissionDenied: (denyType, denyDetail) => {
        // 自定义权限拒绝处理
        switch (denyType) {
          case 'RESOURCE':
            message.error(`缺少资源权限: ${denyDetail}`)
            break
          case 'DATA_SCOPE':
            message.error(`缺少数据域权限: ${denyDetail}`)
            break
          case 'ROLE':
            message.error(`缺少角色权限: ${denyDetail}`)
            break
        }

        // 跳转到无权限页面
        router.push('/403')
      }
    }
  }
]
```

### 2. 权限日志记录

```java
@Component
@Slf4j
public class PermissionAuditLogger {

    @EventListener
    public void handlePermissionEvent(PermissionCheckEvent event) {
        if (!event.isGranted()) {
            log.warn("权限拒绝 - 用户: {}, 资源: {}, 操作: {}, 原因: {}",
                    event.getUserId(),
                    event.getResourceCode(),
                    event.getAction(),
                    event.getDenialReason());
        }
    }
}
```

## 性能优化

### 1. 权限缓存

#### 1.1 权限数据缓存

```java
@Service
public class PermissionCacheService {

    @Cacheable(value = "user_permissions", key = "#userId")
    public Set<String> getUserPermissions(Long userId) {
        return permissionRepository.findPermissionsByUserId(userId);
    }

    @Cacheable(value = "user_roles", key = "#userId")
    public Set<String> getUserRoles(Long userId) {
        return roleRepository.findRolesByUserId(userId);
    }

    @CacheEvict(value = {"user_permissions", "user_roles"}, key = "#userId")
    public void clearUserPermissionCache(Long userId) {
        // 清除用户权限缓存
    }
}
```

#### 1.2 前端权限缓存

```javascript
// stores/smart-permission.js
const permissionCache = new Map()

export const usePermissionStore = defineStore('smartPermission', () => {
  const hasPermission = computed(() => (permissionCode) => {
    if (!currentUser.value) return false

    // 使用缓存
    const cacheKey = `${currentUser.value.userId}_${permissionCode}`
    if (permissionCache.has(cacheKey)) {
      return permissionCache.get(cacheKey)
    }

    const hasPermission = userPermissions.value.some(permission =>
      permission.operationCode === permissionCode &&
      permission.approveStatus === 'ACTIVE'
    )

    // 缓存结果
    permissionCache.set(cacheKey, hasPermission)
    return hasPermission
  })

  const clearPermissionCache = () => {
    permissionCache.clear()
  }

  return {
    hasPermission,
    clearPermissionCache
  }
})
```

## 测试指南

### 1. 权限测试示例

#### 1.1 Controller层权限测试

```java
@SpringBootTest
@AutoConfigureTestDatabase
class AccessDeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"USER"})
    void testGetDevicePageWithoutPermission() throws Exception {
        mockMvc.perform(post("/api/smart/access/device/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ACCESS_ADMIN"})
    void testGetDevicePageWithPermission() throws Exception {
        mockMvc.perform(post("/api/smart/access/device/page")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pageNum\":1,\"pageSize\":10}"))
                .andExpect(status().isOk());
    }
}
```

#### 1.2 前端权限测试

```javascript
// tests/unit/permission.test.js
import { usePermissionStore } from '@/stores/smart-permission'
import ResourceCode from '@/constants/resource-code'

describe('Permission Store', () => {
  let permissionStore

  beforeEach(() => {
    permissionStore = usePermissionStore()
  })

  test('should check permission correctly', () => {
    // 设置测试用户
    permissionStore.setCurrentUser({
      userId: 1,
      permissions: [
        { operationCode: ResourceCode.AccessDevice.VIEW, approveStatus: 'ACTIVE' }
      ]
    })

    expect(permissionStore.hasPermission(ResourceCode.AccessDevice.VIEW)).toBe(true)
    expect(permissionStore.hasPermission(ResourceCode.AccessDevice.DELETE)).toBe(false)
  })

  test('should check data scope correctly', () => {
    permissionStore.setCurrentUser({
      userId: 1,
      permissions: [
        { dataScope: 'AREA', approveStatus: 'ACTIVE' }
      ]
    })

    expect(permissionStore.hasDataScope('AREA')).toBe(true)
    expect(permissionStore.hasDataScope('DEPT')).toBe(false)
  })
})
```

## 故障排查

### 1. 常见问题

#### 1.1 权限注解不生效

**问题**: @RequireResource注解不生效
**解决方案**:
1. 检查AuthorizationInterceptor是否正确注册
2. 确认注解使用的方法被Spring代理
3. 验证资源码是否在数据库中存在

```java
// 检查拦截器注册
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthorizationInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}
```

#### 1.2 前端权限指令失效

**问题**: v-permission指令不生效
**解决方案**:
1. 确认权限Store是否正确初始化
2. 检查用户权限数据是否加载
3. 验证权限码是否正确

```javascript
// 调试权限检查
console.log('Current user:', permissionStore.currentUser)
console.log('Permissions:', permissionStore.userPermissions)
console.log('Has permission:', permissionStore.hasPermission('RESOURCE_CODE'))
```

### 2. 调试技巧

#### 2.1 权限调试日志

```java
// 开启权限调试日志
logging.level.net.lab1024.sa.base.authorization=DEBUG
```

#### 2.2 前端权限调试

```javascript
// 权限调试工具
const permissionDebugger = {
  checkPermission(resourceCode) {
    const store = usePermissionStore()
    console.log(`Checking permission: ${resourceCode}`)
    console.log('User:', store.currentUser)
    console.log('Result:', store.hasPermission(resourceCode))
    return store.hasPermission(resourceCode)
  }
}

// 在浏览器控制台使用
// permissionDebugger.checkPermission('ACCESS_DEVICE_VIEW')
```

## 总结

RAC统一权限中间件为SmartAdmin v3提供了完整的权限控制解决方案，通过统一的资源码、数据域控制和角色管理，实现了精细化的权限管理。遵循本指南的最佳实践，可以构建安全、可维护的权限控制系统。

如需更多帮助，请参考项目文档或联系开发团队。