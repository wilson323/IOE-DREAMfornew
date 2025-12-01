# IOE-DREAM 可复用组件清单

## 📋 文档说明

**目的**: 避免代码冗余,最大化复用现有功能  
**更新时间**: 2025-11-16  
**适用范围**: 所有公共模块和业务模块开发

---

## 🎯 核心复用原则

1. **优先查询本清单** - 开发前必读
2. **禁止重复造轮子** - 已有功能直接复用
3. **统一技术栈** - 严格遵循现有架构

---

## 🏗️ 后端基础设施（100%复用）

### 1. BaseEntity（强制继承）
**路径**: `sa-base/src/main/java/net/lab1024/sa/base/common/entity/BaseEntity.java`

**包含字段**:
```java
private LocalDateTime createTime;      // 创建时间
private LocalDateTime updateTime;      // 更新时间
private Long createUserId;             // 创建人ID
private Long updateUserId;             // 更新人ID
private Integer deletedFlag;           // 删除标记（软删除）
private Integer version;               // 版本号（乐观锁）
```

**使用方式**:
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_your_table")
public class YourEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long yourId;
    
    // ❌ 禁止重复定义：createTime, updateTime, createUserId等
    // ✅ 只定义业务字段
    private String businessField;
}
```

**复用率**: 100% （所有Entity必须继承）

---

### 2. ResponseDTO（统一响应）
**路径**: `sa-base/src/main/java/net/lab1024/sa/base/common/domain/ResponseDTO.java`

**核心方法**:
```java
// 成功响应
ResponseDTO.ok()                    // 成功，无数据
ResponseDTO.ok(data)                // 成功，带数据
ResponseDTO.okMsg("消息")           // 成功，带消息

// 错误响应
ResponseDTO.userErrorParam()        // 参数错误
ResponseDTO.userErrorParam("msg")   // 参数错误，带消息
ResponseDTO.error(ErrorCode.XXX)    // 业务错误
```

**使用方式**:
```java
@PostMapping("/add")
public ResponseDTO<String> add(@Valid @RequestBody AddForm form) {
    // 业务处理
    return ResponseDTO.ok();
}

@GetMapping("/detail/{id}")
public ResponseDTO<DetailVO> getDetail(@PathVariable Long id) {
    DetailVO detail = service.getDetail(id);
    return ResponseDTO.ok(detail);
}
```

**复用率**: 100% （所有Controller必须使用）

---

### 3. Caffeine缓存体系（直接注入）
**路径**: `sa-base/src/main/java/net/lab1024/sa/base/config/AccessCacheConfig.java`

**可用缓存Bean**:

| Bean名称 | 用途 | 容量 | 过期时间 | 复用场景 |
|---------|------|------|---------|---------|
| `permissionCache` | 权限信息 | 1000 | 5分钟 | 权限验证、角色管理 |
| `deviceCache` | 设备信息 | 500 | 10分钟 | 设备管理、状态查询 |
| `accessRecordCache` | 访问记录 | 2000 | 1小时 | 门禁、考勤、消费 |
| `deviceStatusCache` | 设备状态 | 500 | 2分钟 | 实时监控 |
| `alarmCache` | 报警信息 | 200 | 15分钟 | 告警管理 |
| `statisticsCache` | 统计数据 | 100 | 10分钟 | 数据统计 |
| `userSessionCache` | 用户会话 | 500 | 30分钟 | 会话管理 |
| `systemConfigCache` | 系统配置 | 50 | 1小时 | 配置管理 |
| `temporaryCache` | 临时数据 | 200 | 30秒 | 短期缓存 |
| `longTermCache` | 长期数据 | 100 | 4小时 | 长期缓存 |

**使用方式**:
```java
@Component
public class YourManager {
    
    @Resource(name = "permissionCache")
    private Cache<String, Object> permissionCache;
    
    public Object getWithCache(String key) {
        // 从缓存获取
        Object cached = permissionCache.getIfPresent(key);
        if (cached != null) {
            return cached;
        }
        
        // 从数据库加载
        Object data = loadFromDatabase(key);
        
        // 写入缓存
        permissionCache.put(key, data);
        return data;
    }
}
```

**复用率**: 95% （优先使用已有缓存Bean）

---

### 4. Sa-Token权限注解（完整体系）
**技术栈**: Sa-Token 1.38.0

**核心注解**:

| 注解 | 用途 | 示例 |
|-----|------|------|
| `@SaCheckLogin` | 登录验证 | 类级别或方法级别 |
| `@SaCheckPermission` | 权限验证 | `@SaCheckPermission("user:add")` |
| `@SaCheckRole` | 角色验证 | `@SaCheckRole("admin")` |

**使用方式**:
```java
@RestController
@RequestMapping("/api/user")
@SaCheckLogin  // 类级别：所有接口需要登录
public class UserController {
    
    @PostMapping("/add")
    @SaCheckPermission("user:add")  // 方法级别：需要特定权限
    public ResponseDTO<String> add(@Valid @RequestBody UserAddForm form) {
        return ResponseDTO.ok();
    }
}
```

**复用率**: 100% （所有Controller必须使用）

---

## 📊 数据库设计（已有表结构）

### 1. smart-permission模块表（完全可用）
**脚本路径**: `database/smart_permission.sql`

| 表名 | 用途 | 关键字段 | 状态 |
|-----|------|---------|------|
| `t_smart_security_level` | 5级安全级别 | level_code, level_value | ✅ 已创建 |
| `t_smart_data_permission` | 数据权限 | user_id, resource_type, resource_id | ✅ 已创建 |
| `t_smart_temporary_permission` | 临时权限 | approval_status, start_time, end_time | ✅ 已创建 |
| `t_smart_permission_audit` | 权限审计 | operation_type, operation_time | ✅ 已创建 |

**初始数据**:
```sql
-- 5级安全级别已预置
PUBLIC (1) → INTERNAL (2) → CONFIDENTIAL (3) → SECRET (4) → TOP_SECRET (5)
```

**复用建议**:
- ✅ 权限管理直接使用这4张表
- ✅ 安全级别无需重复创建
- ✅ 审计日志统一写入 `t_smart_permission_audit`

---

### 2. 通用员工表（可扩展复用）
**表名**: `t_hr_employee`

**核心字段**:
```sql
employee_id         BIGINT       -- 员工ID
employee_name       VARCHAR(64)  -- 姓名
gender              TINYINT      -- 性别
department_id       BIGINT       -- 部门ID
position            VARCHAR(64)  -- 职位
status              TINYINT      -- 状态
```

**复用场景**:
- 考勤系统：员工打卡记录
- 门禁系统：员工通行权限
- 消费系统：员工消费记录

---

### 3. 智能设备表（完全可用）
**表名**: `t_smart_device`

**核心字段**:
```sql
device_id           BIGINT       -- 设备ID
device_code         VARCHAR(64)  -- 设备编码
device_name         VARCHAR(128) -- 设备名称
device_type         VARCHAR(32)  -- 设备类型(CAMERA/ACCESS/CONSUME/ATTENDANCE)
device_status       VARCHAR(32)  -- 设备状态(ONLINE/OFFLINE/FAULT)
ip_address          VARCHAR(64)  -- IP地址
```

**复用场景**:
- 门禁系统：门禁设备管理
- 考勤系统：考勤设备管理
- 消费系统：消费设备管理
- 视频监控：摄像头设备管理

---

## 🎨 前端组件库（Vue 3）

### 1. smart-permission前端组件（高复用率）
**路径**: `smart-admin-web-javascript/src/views/smart-permission/components/`

| 组件名 | 功能 | 复用率 | 适用场景 |
|-------|------|--------|---------|
| `UserPermissionOverview.vue` | 用户权限总览 | 90% | 所有权限管理页面 |
| `SecurityLevelManager.vue` | 安全级别管理 | 95% | 安全级别配置 |
| `PermissionAuditLog.vue` | 权限审计日志 | 100% | 所有审计功能 |
| `AreaPermissionConfig.vue` | 区域权限配置 | 80% | 区域权限管理 |
| `DevicePermissionConfig.vue` | 设备权限配置 | 85% | 设备权限管理 |
| `GrantPermissionModal.vue` | 权限授予弹窗 | 90% | 权限分配功能 |

**使用方式**:
```vue
<template>
  <div>
    <!-- 直接导入复用 -->
    <PermissionAuditLog />
  </div>
</template>

<script setup>
import PermissionAuditLog from '@/views/smart-permission/components/PermissionAuditLog.vue'
</script>
```

---

### 2. Pinia状态管理（可直接复用）
**路径**: `smart-admin-web-javascript/src/stores/smart-permission.js`

**核心Actions**:
```javascript
// 获取用户权限
fetchUserPermissions(userId)

// 授予权限
grantPermission(permissionType, permissionData)

// 撤销权限
revokePermission(permissionId)

// 获取安全级别列表
fetchSecurityLevels()

// 权限验证
checkPermission(data)
```

**使用方式**:
```vue
<script setup>
import { useSmartPermissionStore } from '@/stores/smart-permission'

const permissionStore = useSmartPermissionStore()

// 获取用户权限
const loadPermissions = async () => {
  await permissionStore.fetchUserPermissions(userId)
}
</script>
```

**复用率**: 85%

---

### 3. 通用业务组件
**路径**: `smart-admin-web-javascript/src/components/business/`

| 组件名 | 功能 | 复用场景 |
|-------|------|---------|
| `PermissionMatrix.vue` | 权限矩阵 | 批量权限配置 |
| `DeviceSelector.vue` | 设备选择器 | 设备关联功能 |
| `EmployeeSelector.vue` | 员工选择器 | 员工关联功能 |
| `DepartmentTree.vue` | 部门树 | 部门选择 |

---

## 🔧 工具类和常量（完全复用）

### 1. MyBatis-Plus（完整配置）
**自动填充字段**:
```java
@TableField(fill = FieldFill.INSERT)       // 创建时自动填充
@TableField(fill = FieldFill.INSERT_UPDATE) // 创建和更新时自动填充
```

**内置方法**（BaseMapper提供）:
```java
int insert(T entity);
int deleteById(Serializable id);
int updateById(T entity);
T selectById(Serializable id);
List<T> selectList(Wrapper<T> queryWrapper);
```

**复用率**: 100%

---

### 2. 参数验证注解（jakarta.validation）
**常用注解**:
```java
@NotNull(message = "不能为空")
@NotBlank(message = "不能为空")
@NotEmpty(message = "不能为空")
@Min(value = 1, message = "最小值为1")
@Max(value = 100, message = "最大值为100")
@Pattern(regexp = "正则表达式", message = "格式错误")
@Valid  // 级联验证
```

**使用方式**:
```java
@Data
public class AddForm {
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄最小值为1")
    private Integer age;
}

@PostMapping("/add")
public ResponseDTO<String> add(@Valid @RequestBody AddForm form) {
    // 验证失败自动返回400错误
    return ResponseDTO.ok();
}
```

**复用率**: 100%

---

## 📋 开发模板（快速生成代码）

### 1. Entity模板
**路径**: `sa-base/src/main/resources/code-generator-template/java/entity/Entity.java.vm`

**使用方式**: 通过代码生成器自动生成，无需手写

---

### 2. Service模板
**路径**: `sa-base/src/main/resources/code-generator-template/java/service/Service.java.vm`

**特点**:
- ✅ 自动生成分页查询
- ✅ 自动生成增删改查
- ✅ 自动使用ResponseDTO

---

### 3. 前端API模板
**路径**: `sa-base/src/main/resources/code-generator-template/ts/api.ts.vm`

**特点**:
- ✅ 自动生成TypeScript接口
- ✅ 统一使用axios封装

---

## 🚀 复用最佳实践

### 1. 开发新模块前检查清单
- [ ] 是否需要BaseEntity审计字段？ → 直接继承
- [ ] 是否需要缓存？ → 注入已有Cache Bean
- [ ] 是否需要权限控制？ → 使用Sa-Token注解
- [ ] 是否需要审计日志？ → 写入 `t_smart_permission_audit`
- [ ] 前端是否有类似组件？ → 复用或微调

### 2. 复用优先级顺序
1. **优先级P0**: BaseEntity, ResponseDTO, Cache（强制复用）
2. **优先级P1**: Sa-Token注解, 数据库表（优先复用）
3. **优先级P2**: 前端组件, 工具类（建议复用）

### 3. 禁止重复开发场景
- ❌ 禁止重复定义审计字段
- ❌ 禁止重复创建缓存Bean
- ❌ 禁止重复编写权限验证逻辑
- ❌ 禁止重复开发已有前端组件

---

## 📊 复用统计（当前状态）

| 分类 | 可复用项 | 复用率 |
|-----|---------|--------|
| 后端基础设施 | 10+ | 95% |
| 数据库表 | 15+ | 80% |
| 前端组件 | 20+ | 75% |
| 工具类 | 30+ | 90% |

---

## 🔄 持续更新

**更新频率**: 每次新增可复用组件时更新本清单  
**负责人**: 架构组  
**最后更新**: 2025-11-16

---

**文档结束**
