# Smart-Permission 模块详细技术设计

## 📋 文档概述

**文档版本**: v1.0  
**创建时间**: 2025-11-16  
**优先级**: P0（立即实施）  
**设计原则**: 最大化复用现有代码，避免重复造轮子

---

## 🎯 核心设计原则

### 代码复用优先
- ✅ **完全复用** `BaseEntity` 审计字段（禁止重复定义）
- ✅ **直接使用** `AccessCacheConfig` 中的 `permissionCache` Bean
- ✅ **继承使用** `ResponseDTO` 统一响应格式
- ✅ **严格遵循** 四层架构规范（Controller→Service→Manager→DAO）

### 已有资源清单

#### 1. 数据库设计（完全可用）
**路径**: `database/smart_permission.sql`

| 表名 | 用途 | 状态 |
|------|------|------|
| `t_smart_security_level` | 5级安全级别定义 | ✅ 已创建 |
| `t_smart_data_permission` | 数据权限管理 | ✅ 已创建 |
| `t_smart_temporary_permission` | 临时权限申请 | ✅ 已创建 |
| `t_smart_permission_audit` | 权限审计日志 | ✅ 已创建 |

**关键特性**:
- ✅ 包含完整的索引优化
- ✅ 提供权限验证函数 `fn_check_permission`
- ✅ 预置5级安全数据（PUBLIC→INTERNAL→CONFIDENTIAL→SECRET→TOP_SECRET）

#### 2. 缓存基础设施（完全可用）
**路径**: `sa-base/src/main/java/net/lab1024/sa/base/config/AccessCacheConfig.java`

```java
@Bean("permissionCache")
public Cache<String, Object> permissionCache() {
    return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
}
```

**复用策略**: 直接 `@Resource(name = "permissionCache")` 注入使用

#### 3. 基础实体类（强制继承）
**路径**: `sa-base/src/main/java/net/lab1024/sa/base/common/entity/BaseEntity.java`

```java
@Data
public abstract class BaseEntity implements Serializable {
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deletedFlag;
    
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
```

**强制要求**: 所有Entity必须继承，禁止重复定义审计字段

#### 4. 前端基础组件（可直接复用）
**路径**: `smart-admin-web-javascript/src/views/smart-permission/`

| 组件名 | 功能 | 复用程度 |
|--------|------|----------|
| `UserPermissionOverview.vue` | 用户权限总览 | 90% |
| `SecurityLevelManager.vue` | 安全级别管理 | 95% |
| `PermissionAuditLog.vue` | 权限审计日志 | 100% |
| `AreaPermissionConfig.vue` | 区域权限配置 | 80% |
| `DevicePermissionConfig.vue` | 设备权限配置 | 85% |

---

## 🏗️ 后端架构设计（基于现有代码）

### 第一层：实体类（Entity Layer）

#### SecurityLevelEntity.java
**路径**: `sa-admin/src/main/java/net/lab1024/sa/admin/module/permission/domain/entity/SecurityLevelEntity.java`

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_security_level")
public class SecurityLevelEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long levelId;
    
    private String levelCode;
    private String levelName;
    private String levelDescription;
    private Integer levelValue;
    private Integer isSystem;
    
    // ❌ 禁止定义以下字段（BaseEntity已包含）
    // private LocalDateTime createTime;
    // private LocalDateTime updateTime;
    // private Long createUserId;
    // private Integer deletedFlag;
    // private Integer version;
}
```

#### DataPermissionEntity.java
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_data_permission")
public class DataPermissionEntity extends BaseEntity {
    
    @TableId(type = IdType.AUTO)
    private Long permissionId;
    
    private Long userId;
    private String resourceType;
    private Long resourceId;
    private Integer permissionType;
    private Long securityLevelId;
    private String permissionScope;
    private Integer isTemporary;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

### 第二层：DAO层（完全基于MyBatis-Plus）

#### SecurityLevelDao.java
```java
public interface SecurityLevelDao extends BaseMapper<SecurityLevelEntity> {
    
    /**
     * 根据级别代码查询
     */
    SecurityLevelEntity selectByLevelCode(@Param("levelCode") String levelCode);
    
    /**
     * 查询所有可用安全级别
     */
    List<SecurityLevelEntity> selectAllAvailableLevels();
}
```

**XML路径**: `mapper/SecurityLevelMapper.xml` （可选，优先使用MyBatis-Plus内置方法）

### 第三层：Manager层（业务逻辑封装）

#### PermissionCacheManager.java
**复用策略**: 直接注入 `permissionCache` Bean

```java
@Component
@Slf4j
public class PermissionCacheManager {
    
    @Resource(name = "permissionCache")
    private Cache<String, Object> permissionCache;
    
    private static final String USER_PERMISSION_KEY = "user:permission:";
    private static final String SECURITY_LEVEL_KEY = "security:level:";
    
    /**
     * 获取用户权限（带缓存）
     */
    public List<DataPermissionEntity> getUserPermissions(Long userId) {
        String cacheKey = USER_PERMISSION_KEY + userId;
        
        @SuppressWarnings("unchecked")
        List<DataPermissionEntity> cached = 
            (List<DataPermissionEntity>) permissionCache.getIfPresent(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        // 从数据库加载
        // ...实现逻辑...
        
        // 写入缓存
        permissionCache.put(cacheKey, permissions);
        return permissions;
    }
    
    /**
     * 清除用户权限缓存
     */
    public void clearUserPermissionCache(Long userId) {
        permissionCache.invalidate(USER_PERMISSION_KEY + userId);
    }
}
```

### 第四层：Service层（事务管理）

#### PermissionService.java
```java
@Service
@Slf4j
public class PermissionService {
    
    @Resource
    private SecurityLevelDao securityLevelDao;
    
    @Resource
    private DataPermissionDao dataPermissionDao;
    
    @Resource
    private PermissionCacheManager permissionCacheManager;
    
    /**
     * 授予用户权限（带事务）
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> grantPermission(PermissionGrantForm form) {
        // 1. 验证安全级别
        SecurityLevelEntity securityLevel = 
            securityLevelDao.selectById(form.getSecurityLevelId());
        
        if (securityLevel == null) {
            return ResponseDTO.userErrorParam("安全级别不存在");
        }
        
        // 2. 创建权限记录
        DataPermissionEntity entity = new DataPermissionEntity();
        BeanUtils.copyProperties(form, entity);
        dataPermissionDao.insert(entity);
        
        // 3. 清除缓存
        permissionCacheManager.clearUserPermissionCache(form.getUserId());
        
        // 4. 记录审计日志
        // ...审计逻辑...
        
        return ResponseDTO.ok();
    }
}
```

### 第五层：Controller层（API接口）

#### PermissionController.java
```java
@RestController
@RequestMapping("/api/permission")
@Slf4j
@SaCheckLogin
public class PermissionController {
    
    @Resource
    private PermissionService permissionService;
    
    /**
     * 授予权限
     */
    @PostMapping("/grant")
    @SaCheckPermission("permission:grant")
    public ResponseDTO<String> grantPermission(
            @Valid @RequestBody PermissionGrantForm form) {
        return permissionService.grantPermission(form);
    }
    
    /**
     * 查询用户权限
     */
    @GetMapping("/user/{userId}")
    @SaCheckPermission("permission:query")
    public ResponseDTO<List<PermissionVO>> getUserPermissions(
            @PathVariable Long userId) {
        return permissionService.getUserPermissions(userId);
    }
}
```

---

## 📊 DTO设计（统一响应格式）

### PermissionGrantForm.java（请求DTO）
```java
@Data
public class PermissionGrantForm {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "资源类型不能为空")
    private String resourceType;
    
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;
    
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;
    
    @NotNull(message = "安全级别ID不能为空")
    private Long securityLevelId;
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
```

### PermissionVO.java（响应DTO）
```java
@Data
public class PermissionVO {
    private Long permissionId;
    private Long userId;
    private String resourceType;
    private String resourceTypeName;
    private Long resourceId;
    private String resourceName;
    private Integer permissionType;
    private String permissionTypeName;
    private String securityLevelCode;
    private String securityLevelName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer isTemporary;
}
```

---

## 🎨 前端架构设计（复用现有组件）

### 状态管理（Pinia Store）
**复用**: `smart-admin-web-javascript/src/stores/smart-permission.js`

```javascript
import { defineStore } from 'pinia'
import { permissionApi } from '@/api/smart-permission'

export const useSmartPermissionStore = defineStore('smartPermission', {
  state: () => ({
    userPermissions: [],
    securityLevels: [],
    loading: false
  }),
  
  actions: {
    // 获取用户权限（已有实现）
    async fetchUserPermissions(userId) {
      // ...复用现有代码...
    },
    
    // 授予权限（已有实现）
    async grantPermission(data) {
      // ...复用现有代码...
    }
  }
})
```

### API封装
**复用**: `smart-admin-web-javascript/src/api/smart-permission.js`

```javascript
export const permissionApi = {
  // 授予权限
  grantPermission: (data) => {
    return postRequest('/api/permission/grant', data)
  },
  
  // 查询用户权限
  getUserPermissions: (userId) => {
    return getRequest(`/api/permission/user/${userId}`)
  }
}
```

---

## 📅 实施路线图（4周）

### Week 1: 后端核心功能
- [x] 数据库表已创建（`database/smart_permission.sql`）
- [ ] 创建Entity类（继承BaseEntity）
- [ ] 创建DAO接口（基于MyBatis-Plus）
- [ ] 实现PermissionCacheManager（复用permissionCache）
- [ ] 编写单元测试

### Week 2: Service和Controller
- [ ] 实现PermissionService（事务管理）
- [ ] 实现PermissionController（权限控制）
- [ ] 集成Sa-Token权限验证
- [ ] 编写集成测试

### Week 3: 前端集成
- [ ] 复用现有Vue组件
- [ ] 调整API接口调用
- [ ] 测试前后端联调

### Week 4: 测试和优化
- [ ] 完整功能测试
- [ ] 性能测试和优化
- [ ] 文档完善

---

## ✅ 验收标准

### 功能完整性
- ✅ 5级安全级别管理
- ✅ 用户权限授予/撤销
- ✅ 权限验证引擎
- ✅ 审计日志完整

### 代码质量
- ✅ 所有Entity继承BaseEntity
- ✅ 使用@Resource注入
- ✅ 使用ResponseDTO统一响应
- ✅ 添加@SaCheckPermission注解
- ✅ 单元测试覆盖率≥80%

### 性能标准
- ✅ 权限验证响应时间<50ms
- ✅ 缓存命中率≥90%
- ✅ 支持并发1000+用户

---

## 📌 关键注意事项

### 禁止事项
- ❌ 禁止在Entity中重复定义BaseEntity字段
- ❌ 禁止使用@Autowired（必须使用@Resource）
- ❌ 禁止使用javax.*包（必须使用jakarta.*）
- ❌ 禁止Controller直接访问DAO
- ❌ 禁止使用System.out.println

### 必须事项
- ✅ 必须继承BaseEntity
- ✅ 必须使用ResponseDTO
- ✅ 必须添加@Valid参数验证
- ✅ 必须添加@SaCheckPermission
- ✅ 必须编写单元测试

---

**文档结束**
