# 权限管理公共模块开发文档

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 适用范围**: 所有业务模块的权限管理功能

---

## 📖 模块概述

### 模块简介
smart-permission 是 SmartAdmin 项目的权限管理公共模块，提供统一的5级安全级别权限控制功能，支持数据权限隔离、临时权限管理和权限审计等功能。

### 核心特性
- **5级安全级别**: 绝密级、机密级、秘密级、内部级、公开级
- **数据权限隔离**: 基于区域、设备、用户的数据权限控制
- **临时权限管理**: 支持短期访问权限的申请和审批
- **权限审计日志**: 完整的权限变更和访问记录
- **多级缓存策略**: 高性能的权限验证机制

---

## 🏗️ 架构设计

### 模块结构

```
smart-permission/
├── controller/                    # 权限控制器
│   ├── SecurityLevelController.java     # 安全级别管理
│   ├── DataPermissionController.java   # 数据权限管理
│   ├── TemporaryPermissionController.java # 临时权限管理
│   └── PermissionAuditController.java    # 权限审计
├── service/                      # 权限服务层
│   ├── SecurityLevelService.java       # 安全级别服务
│   ├── DataPermissionService.java     # 数据权限服务
│   ├── TemporaryPermissionService.java # 临时权限服务
│   └── PermissionAuditService.java     # 权限审计服务
├── manager/                      # 权限管理层
│   ├── SecurityLevelManager.java      # 安全级别管理器
│   ├── PermissionCacheManager.java    # 权限缓存管理器
│   ├── PermissionValidator.java        # 权限验证器
│   └── PermissionEngine.java          # 权限引擎
├── dao/                          # 权限数据层
│   ├── SecurityLevelDao.java          # 安全级别DAO
│   ├── DataPermissionDao.java        # 数据权限DAO
│   ├── TemporaryPermissionDao.java   # 临时权限DAO
│   └── PermissionAuditDao.java       # 权限审计DAO
└── entity/                       # 权限实体
    ├── SecurityLevelEntity.java      # 安全级别实体
    ├── DataPermissionEntity.java    # 数据权限实体
    ├── TemporaryPermissionEntity.java # 临时权限实体
    └── PermissionAuditEntity.java  # 权限审计实体
```

### 核心设计模式

```java
// 权限验证器模式
@Component
public class PermissionValidator {

    @Resource
    private SecurityLevelManager securityLevelManager;

    @Resource
    private DataPermissionService dataPermissionService;

    @Resource
    private PermissionCacheManager cacheManager;

    /**
     * 验证用户权限
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @param dataScope 数据范围
     * @return 验证结果
     */
    public PermissionResult validate(Long userId, String permissionCode, DataScope dataScope) {
        // 1. 从缓存获取权限信息
        PermissionInfo permissionInfo = cacheManager.getPermission(userId, permissionCode);

        // 2. 缓存不存在则从数据库加载
        if (permissionInfo == null) {
            permissionInfo = loadPermissionFromDB(userId, permissionCode);
            cacheManager.cachePermission(userId, permissionCode, permissionInfo);
        }

        // 3. 验证安全级别
        boolean securityValid = securityLevelManager.validateSecurityLevel(
            userId, permissionInfo.getRequiredSecurityLevel());

        // 4. 验证数据权限
        boolean dataValid = dataPermissionService.validateDataPermission(
            userId, dataScope, permissionInfo.getDataPermissionScope());

        // 5. 返回验证结果
        return PermissionResult.builder()
            .securityValid(securityValid)
            .dataValid(dataValid)
            .permissionInfo(permissionInfo)
            .build();
    }

    // ... 其他方法
}
```

---

## 🗄️ 数据库设计

### 安全级别表 (t_security_level)

```sql
CREATE TABLE t_security_level (
    level_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '安全级别ID',
    level_name VARCHAR(50) NOT NULL COMMENT '安全级别名称',
    level_code VARCHAR(20) NOT NULL UNIQUE COMMENT '安全级别编码',
    level_value INT NOT NULL COMMENT '安全级别值',
    description TEXT COMMENT '级别描述',
    config_json JSON COMMENT '级别配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_level_code (level_code),
    INDEX idx_level_value (level_value),
    INDEX idx_status (status)
) COMMENT = '安全级别表';

-- 初始化安全级别数据
INSERT INTO t_security_level (level_name, level_code, level_value, description, sort_order) VALUES
('绝密级', 'TOP_SECRET', 50, '最高安全级别，仅限核心人员访问', 1),
('机密级', 'SECRET', 40, '高级安全级别，仅限管理人员访问', 2),
('秘密级', 'CONFIDENTIAL', 30, '中级安全级别，仅限授权人员访问', 3),
('内部级', 'INTERNAL', 20, '基础安全级别，内部员工可访问', 4),
('公开级', 'PUBLIC', 10, '最低安全级别，所有人员可访问', 5);
```

### 数据权限表 (t_data_permission)

```sql
CREATE TABLE t_data_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT COMMENT '角色ID',
    security_level_id BIGINT NOT NULL COMMENT '安全级别ID',
    permission_type VARCHAR(50) NOT NULL COMMENT '权限类型',
    permission_scope JSON NOT NULL COMMENT '权限范围JSON',
    grant_user_id BIGINT COMMENT '授权人ID',
    grant_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-生效，0-失效',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    INDEX idx_security_level (security_level_id),
    INDEX idx_permission_type (permission_type),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time)
) COMMENT = '数据权限表';
```

### 临时权限表 (t_temporary_permission)

```sql
CREATE TABLE t_temporary_permission (
    temp_permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '临时权限ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    original_permission_id BIGINT NOT NULL COMMENT '原权限ID',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    temp_security_level_id BIGINT NOT NULL COMMENT '临时安全级别ID',
    temp_permission_scope JSON COMMENT '临时权限范围',
    apply_user_id BIGINT NOT NULL COMMENT '申请人ID',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approve_user_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_status TINYINT DEFAULT 0 COMMENT '审批状态：0-待审批，1-已批准，2-已拒绝',
    approve_remark TEXT COMMENT '审批备注',
    start_time DATETIME NOT NULL COMMENT '生效开始时间',
    end_time DATETIME NOT NULL COMMENT '生效结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_permission_code (permission_code),
    INDEX idx_approve_status (approve_status),
    INDEX idx_time_range (start_time, end_time),
    INDEX idx_apply_user_id (apply_user_id)
) COMMENT = '临时权限表';
```

### 权限审计表 (t_permission_audit)

```sql
CREATE TABLE t_permission_audit (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审计ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_desc TEXT COMMENT '操作描述',
    operation_result TINYINT NOT NULL COMMENT '操作结果：1-成功，0-失败',
    security_level_before INT COMMENT '变更前安全级别',
    security_level_after INT COMMENT '变更后安全级别',
    data_scope_before JSON COMMENT '变更前数据范围',
    data_scope_after JSON COMMENT '变更后数据范围',
    ip_address VARCHAR(50) COMMENT '操作IP',
    user_agent VARCHAR(500) COMMENT '用户代理',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_permission_code (permission_code),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_time (operation_time),
    INDEX idx_result (operation_result)
) COMMENT = '权限审计表';
```

---

## 🔧 后端实现

### 核心控制器 (SecurityLevelController)

```java
@RestController
@RequestMapping("/api/security/level")
@Tag(name = "安全级别管理", description = "5级安全级别相关接口")
public class SecurityLevelController {

    @Resource
    private SecurityLevelService securityLevelService;

    @Resource
    private PermissionAuditService auditService;

    @GetMapping("/list")
    @Operation(summary = "获取安全级别列表")
    @SaCheckPermission("security:level:list")
    public ResponseDTO<List<SecurityLevelVO>> getSecurityLevels() {
        List<SecurityLevelVO> levels = securityLevelService.getAllSecurityLevels();
        return ResponseDTO.ok(levels);
    }

    @PostMapping("/user/level")
    @Operation(summary = "获取用户安全级别")
    @SaCheckLogin
    public ResponseDTO<Integer> getUserSecurityLevel(@RequestParam Long userId) {
        Integer level = securityLevelService.getUserSecurityLevel(userId);
        return ResponseDTO.ok(level);
    }

    @PostMapping("/user/grant")
    @Operation(summary = "授权用户安全级别")
    @SaCheckPermission("security:level:grant")
    public ResponseDTO<String> grantUserSecurityLevel(@Valid @RequestBody SecurityLevelGrantDTO grantDTO) {
        // 记录审计日志
        auditService.recordPermissionLog(grantDTO.getUserId(), "security:level:grant",
            "授权安全级别: " + grantDTO.getSecurityLevelId());

        securityLevelService.grantUserSecurityLevel(grantDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/validate")
    @Operation(summary = "验证权限")
    @SaCheckLogin
    public ResponseDTO<PermissionValidateVO> validatePermission(
            @RequestParam String permissionCode,
            @RequestParam(required = false) String dataScope) {

        Long userId = SmartRequestUtil.getCurrentUserId();
        PermissionValidateVO result = securityLevelService.validatePermission(
            userId, permissionCode, dataScope);
        return ResponseDTO.ok(result);
    }
}
```

### 核心服务层 (SecurityLevelService)

```java
@Service
@Transactional(readOnly = true)
public class SecurityLevelService {

    @Resource
    private SecurityLevelManager securityLevelManager;
    @Resource
    private PermissionCacheManager cacheManager;
    @Resource
    private DataPermissionService dataPermissionService;

    public List<SecurityLevelVO> getAllSecurityLevels() {
        return securityLevelManager.getAllSecurityLevels();
    }

    public Integer getUserSecurityLevel(Long userId) {
        // 1. 先从缓存获取
        Integer cachedLevel = cacheManager.getUserSecurityLevel(userId);
        if (cachedLevel != null) {
            return cachedLevel;
        }

        // 2. 从数据库获取
        Integer level = securityLevelManager.getUserSecurityLevel(userId);

        // 3. 缓存结果
        cacheManager.cacheUserSecurityLevel(userId, level);

        return level;
    }

    @Transactional(rollbackFor = Exception.class)
    public void grantUserSecurityLevel(SecurityLevelGrantDTO grantDTO) {
        // 1. 验证参数
        validateGrantDTO(grantDTO);

        // 2. 授权安全级别
        securityLevelManager.grantUserSecurityLevel(grantDTO);

        // 3. 清除用户权限缓存
        cacheManager.clearUserPermissionCache(grantDTO.getUserId());

        // 4. 发布权限变更事件
        eventPublisher.publishEvent(new PermissionChangeEvent(grantDTO.getUserId()));
    }

    public PermissionValidateVO validatePermission(Long userId, String permissionCode, String dataScope) {
        // 1. 获取用户安全级别
        Integer userLevel = getUserSecurityLevel(userId);

        // 2. 获取权限所需级别
        Integer requiredLevel = securityLevelManager.getPermissionRequiredLevel(permissionCode);

        // 3. 验证安全级别
        boolean securityValid = userLevel != null && userLevel >= requiredLevel;

        // 4. 验证数据权限
        boolean dataValid = true;
        if (dataScope != null) {
            dataValid = dataPermissionService.validateDataPermission(userId, dataScope);
        }

        return PermissionValidateVO.builder()
            .securityValid(securityValid)
            .dataValid(dataValid)
            .userSecurityLevel(userLevel)
            .requiredSecurityLevel(requiredLevel)
            .build();
    }

    private void validateGrantDTO(SecurityLevelGrantDTO grantDTO) {
        // 验证授权规则
        if (grantDTO.getSecurityLevelId() > getOperationUserSecurityLevel()) {
            throw new SmartException("不能授权高于自己安全级别的权限");
        }
    }
}
```

### 核心管理层 (SecurityLevelManager)

```java
@Component
public class SecurityLevelManager {

    @Resource
    private SecurityLevelDao securityLevelDao;
    @Resource
    private DataPermissionDao dataPermissionDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存常量
    private static final String CACHE_PREFIX = "security:level:";
    private static final String USER_LEVEL_PREFIX = "user:security:level:";
    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(30);

    @Cacheable(value = "security", key = "'all:levels'")
    public List<SecurityLevelVO> getAllSecurityLevels() {
        return securityLevelDao.selectList(new QueryWrapper<SecurityLevelEntity>()
            .eq("status", 1)
            .eq("deleted_flag", 0)
            .orderByAsc("sort_order")
        ).stream().map(entity -> {
            SecurityLevelVO vo = new SecurityLevelVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Cacheable(value = "security", key = "#userId")
    public Integer getUserSecurityLevel(Long userId) {
        DataPermissionEntity permission = dataPermissionDao.selectOne(
            new QueryWrapper<DataPermissionEntity>()
                .eq("user_id", userId)
                .eq("status", 1)
                .eq("deleted_flag", 0)
                .apply("expire_time IS NULL OR expire_time > NOW()")
                .orderByDesc("security_level_id")
        );

        if (permission == null) {
            return null; // 无权限
        }

        SecurityLevelEntity level = securityLevelDao.selectById(permission.getSecurityLevelId());
        return level != null ? level.getLevelValue() : null;
    }

    @CacheEvict(value = "security", allEntries = true)
    public void grantUserSecurityLevel(SecurityLevelGrantDTO grantDTO) {
        // 1. 检查是否已有权限
        DataPermissionEntity existing = dataPermissionDao.selectOne(
            new QueryWrapper<DataPermissionEntity>()
                .eq("user_id", grantDTO.getUserId())
                .eq("status", 1)
                .eq("deleted_flag", 0)
        );

        if (existing != null) {
            // 更新现有权限
            existing.setSecurityLevelId(grantDTO.getSecurityLevelId());
            existing.setUpdateTime(LocalDateTime.now());
            dataPermissionDao.updateById(existing);
        } else {
            // 创建新权限
            DataPermissionEntity permission = new DataPermissionEntity();
            permission.setUserId(grantDTO.getUserId());
            permission.setSecurityLevelId(grantDTO.getSecurityLevelId());
            permission.setPermissionType("DEFAULT");
            permission.setPermissionScope("{}"); // 默认无数据范围限制
            permission.setGrantUserId(SmartRequestUtil.getCurrentUserId());
            permission.setStatus(1);
            dataPermissionDao.insert(permission);
        }
    }

    @Cacheable(value = "security", key = "'permission:level:' + #permissionCode")
    public Integer getPermissionRequiredLevel(String permissionCode) {
        // 从菜单权限表获取权限所需级别
        MenuEntity menu = menuDao.selectOne(
            new QueryWrapper<MenuEntity>()
                .eq("permission_code", permissionCode)
                .eq("status", 1)
                .eq("deleted_flag", 0)
        );

        if (menu == null) {
            return 10; // 默认公开级
        }

        // 返回权限配置的安全级别
        JsonNode configNode = JsonUtils.parseObject(menu.getMenuConfig());
        return configNode.has("securityLevel") ? configNode.get("securityLevel").asInt() : 10;
    }
}
```

---

## 🎨 前端实现

### 权限状态管理 (usePermissionStore)

```javascript
// /store/permission.js
import { defineStore } from 'pinia'
import { permissionApi } from '/@/api/permission'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    // 安全级别列表
    securityLevels: [],
    // 用户当前安全级别
    userSecurityLevel: null,
    // 权限验证结果缓存
    permissionCache: new Map(),
    // 临时权限列表
    temporaryPermissions: []
  }),

  getters: {
    // 获取安全级别名称
    getSecurityLevelName: (state) => (level) => {
      const levelInfo = state.securityLevels.find(l => l.levelValue === level)
      return levelInfo ? levelInfo.levelName : '未知'
    },

    // 获取权限验证结果
    getPermissionResult: (state) => (permissionCode, dataScope) => {
      const cacheKey = `${permissionCode}:${dataScope || ''}`
      return state.permissionCache.get(cacheKey)
    },

    // 检查是否有权限
    hasPermission: (state) => (permissionCode, dataScope) => {
      const result = state.getPermissionResult(permissionCode, dataScope)
      return result && result.securityValid && result.dataValid
    }
  },

  actions: {
    // 初始化安全级别
    async initSecurityLevels() {
      try {
        const result = await permissionApi.getSecurityLevels()
        this.securityLevels = result.data
      } catch (error) {
        console.error('获取安全级别失败:', error)
      }
    },

    // 获取用户安全级别
    async fetchUserSecurityLevel() {
      try {
        const result = await permissionApi.getUserSecurityLevel(SmartUser.getUserId())
        this.userSecurityLevel = result.data
      } catch (error) {
        console.error('获取用户安全级别失败:', error)
      }
    },

    // 验证权限
    async validatePermission(permissionCode, dataScope = null) {
      const cacheKey = `${permissionCode}:${dataScope || ''}`

      // 检查缓存
      if (this.permissionCache.has(cacheKey)) {
        return this.permissionCache.get(cacheKey)
      }

      try {
        const result = await permissionApi.validatePermission(permissionCode, dataScope)
        this.permissionCache.set(cacheKey, result.data)
        return result.data
      } catch (error) {
        console.error('权限验证失败:', error)
        return { securityValid: false, dataValid: false }
      }
    },

    // 授权用户安全级别
    async grantUserSecurityLevel(userId, securityLevelId) {
      try {
        await permissionApi.grantUserSecurityLevel({
          userId,
          securityLevelId
        })

        // 清除相关缓存
        this.permissionCache.clear()

        return true
      } catch (error) {
        console.error('授权失败:', error)
        return false
      }
    },

    // 申请临时权限
    async applyTemporaryPermission(applyData) {
      try {
        const result = await permissionApi.applyTemporaryPermission(applyData)
        this.temporaryPermissions.push(result.data)
        return result.data
      } catch (error) {
        console.error('申请临时权限失败:', error)
        return null
      }
    }
  }
})
```

### 权限指令 (v-permission)

```javascript
// /directives/permission.js
import { usePermissionStore } from '/@/store/permission'

export default {
  mounted(el, binding) {
    const { value, modifiers } = binding
    const permissionStore = usePermissionStore()

    // 权限配置
    const permissionConfig = {
      code: value, // 权限编码
      dataScope: modifiers.data, // 数据范围
      hide: modifiers.hide, // 无权限时隐藏元素
      disable: modifiers.disable // 无权限时禁用元素
    }

    // 异步验证权限
    permissionStore.validatePermission(permissionConfig.code, permissionConfig.dataScope)
      .then(result => {
        if (!result.securityValid || !result.dataValid) {
          if (permissionConfig.hide) {
            el.style.display = 'none'
          } else if (permissionConfig.disable) {
            el.disabled = true
            el.classList.add('permission-disabled')
          } else {
            el.style.opacity = '0.5'
            el.style.pointerEvents = 'none'
          }
        }
      })
  }
}
```

### 安全级别选择器组件 (SecurityLevelSelector)

```vue
<template>
  <a-select
    v-model:value="selectedLevel"
    :loading="loading"
    :disabled="disabled"
    @change="handleChange"
    placeholder="请选择安全级别"
  >
    <a-select-option
      v-for="level in securityLevels"
      :key="level.levelId"
      :value="level.levelValue"
      :disabled="level.levelValue > maxAllowedLevel"
    >
      <a-tag :color="getLevelColor(level.levelValue)">
        {{ level.levelName }}
      </a-tag>
      <span class="ml-2">{{ level.description }}</span>
    </a-select-option>
  </a-select>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePermissionStore } from '/@/store/permission'

const props = defineProps({
  modelValue: {
    type: Number,
    default: null
  },
  maxAllowedLevel: {
    type: Number,
    default: 50 // 默认可以授权绝密级
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const permissionStore = usePermissionStore()

const loading = ref(false)
const selectedLevel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const securityLevels = computed(() => permissionStore.securityLevels)

const getLevelColor = (level) => {
  const colorMap = {
    50: 'red',    // 绝密级
    40: 'orange', // 机密级
    30: 'yellow', // 秘密级
    20: 'blue',   // 内部级
    10: 'green'   // 公开级
  }
  return colorMap[level] || 'default'
}

const handleChange = (value) => {
  emit('change', value)
}

onMounted(async () => {
  if (securityLevels.value.length === 0) {
    loading.value = true
    await permissionStore.initSecurityLevels()
    loading.value = false
  }
})
</script>

<style lang="less" scoped>
.permission-disabled {
  cursor: not-allowed;
  opacity: 0.5;
}
</style>
```

---

## 🔐 安全性考虑

### 1. 权限缓存安全

```java
@Component
public class PermissionCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存权限信息
     * @param userId 用户ID
     * @param permissionCode 权限编码
     * @param permissionInfo 权限信息
     */
    public void cachePermission(Long userId, String permissionCode, PermissionInfo permissionInfo) {
        String cacheKey = buildCacheKey(userId, permissionCode);

        // 加密存储敏感权限信息
        String encryptedData = encryptPermissionInfo(permissionInfo);

        redisTemplate.opsForValue().set(
            cacheKey,
            encryptedData,
            Duration.ofMinutes(30)
        );
    }

    /**
     * 从缓存获取权限信息
     */
    public PermissionInfo getPermission(Long userId, String permissionCode) {
        String cacheKey = buildCacheKey(userId, permissionCode);
        String encryptedData = (String) redisTemplate.opsForValue().get(cacheKey);

        if (encryptedData == null) {
            return null;
        }

        // 解密权限信息
        return decryptPermissionInfo(encryptedData);
    }

    private String encryptPermissionInfo(PermissionInfo permissionInfo) {
        // 使用AES加密敏感权限信息
        String jsonData = JsonUtils.toJsonString(permissionInfo);
        return AesUtil.encrypt(jsonData, getPermissionCacheKey());
    }

    private PermissionInfo decryptPermissionInfo(String encryptedData) {
        try {
            String jsonData = AesUtil.decrypt(encryptedData, getPermissionCacheKey());
            return JsonUtils.parseObject(jsonData, PermissionInfo.class);
        } catch (Exception e) {
            log.error("权限缓存解密失败", e);
            return null;
        }
    }

    private String buildCacheKey(Long userId, String permissionCode) {
        return String.format("permission:%d:%s", userId, permissionCode);
    }

    private String getPermissionCacheKey() {
        // 从安全配置中获取加密密钥
        return securityConfig.getPermissionCacheKey();
    }
}
```

### 2. 权限审计安全

```java
@Component
public class PermissionAuditService {

    @Resource
    private PermissionAuditDao auditDao;

    /**
     * 记录权限操作日志
     */
    @Async
    public CompletableFuture<Void> recordPermissionLog(Long userId, String operationType, String operationDesc) {
        return CompletableFuture.runAsync(() -> {
            try {
                PermissionAuditEntity audit = new PermissionAuditEntity();
                audit.setUserId(userId);
                audit.setPermissionCode(operationType);
                audit.setOperationType(operationType);
                audit.setOperationDesc(operationDesc);
                audit.setOperationResult(1); // 默认成功
                audit.setIpAddress(SmartRequestUtil.getClientIp());
                audit.setUserAgent(SmartRequestUtil.getUserAgent());
                audit.setOperationTime(LocalDateTime.now());

                auditDao.insert(audit);
            } catch (Exception e) {
                log.error("记录权限审计日志失败", e);
                // 审计失败不影响主业务流程
            }
        });
    }

    /**
     * 记录权限验证失败
     */
    public void recordPermissionFailed(Long userId, String permissionCode, String reason) {
        PermissionAuditEntity audit = new PermissionAuditEntity();
        audit.setUserId(userId);
        audit.setPermissionCode(permissionCode);
        audit.setOperationType("PERMISSION_VALIDATE");
        audit.setOperationDesc("权限验证失败: " + reason);
        audit.setOperationResult(0); // 失败
        audit.setIpAddress(SmartRequestUtil.getClientIp());
        audit.setOperationTime(LocalDateTime.now());

        auditDao.insert(audit);
    }
}
```

### 3. 数据权限过滤

```java
@Component
public class DataPermissionInterceptor implements InnerInterceptor {

    @Resource
    private DataPermissionService dataPermissionService;

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                           RowBounds rowBounds, ResultHandler resultHandler,
                           BoundSql boundSql) throws SQLException {

        if (parameter instanceof BaseQueryDTO) {
            BaseQueryDTO queryDTO = (BaseQueryDTO) parameter;
            Long userId = SmartRequestUtil.getCurrentUserId();

            if (userId != null) {
                // 获取用户数据权限范围
                DataPermissionScope scope = dataPermissionService.getUserDataPermission(userId);

                if (scope != null && scope.hasPermission()) {
                    // 构建权限SQL条件
                    String permissionSql = buildPermissionSql(boundSql.getSql(), scope);
                    setSql(boundSql, permissionSql);
                }
            }
        }
    }

    private String buildPermissionSql(String originalSql, DataPermissionScope scope) {
        StringBuilder sqlBuilder = new StringBuilder(originalSql);

        // 数据范围权限
        if (scope.getAreaIds() != null && !scope.getAreaIds().isEmpty()) {
            sqlBuilder.append(" AND area_id IN (")
                     .append(scope.getAreaIds().stream()
                         .map(String::valueOf)
                         .collect(Collectors.joining(",")))
                     .append(")");
        }

        // 设备权限
        if (scope.getDeviceIds() != null && !scope.getDeviceIds().isEmpty()) {
            sqlBuilder.append(" AND device_id IN (")
                     .append(scope.getDeviceIds().stream()
                         .map(String::valueOf)
                         .collect(Collectors.joining(",")))
                     .append(")");
        }

        return sqlBuilder.toString();
    }
}
```

---

## 🧪 测试策略

### 1. 单元测试

```java
@SpringBootTest
class SecurityLevelServiceTest {

    @Resource
    private SecurityLevelService securityLevelService;

    @Resource
    private SecurityLevelDao securityLevelDao;

    @Test
    void testGetAllSecurityLevels() {
        // 测试获取所有安全级别
        List<SecurityLevelVO> levels = securityLevelService.getAllSecurityLevels();

        assertNotNull(levels);
        assertFalse(levels.isEmpty());
        assertEquals(5, levels.size()); // 应该有5个级别

        // 验证级别顺序
        for (int i = 0; i < levels.size() - 1; i++) {
            assertTrue(levels.get(i).getLevelValue() < levels.get(i + 1).getLevelValue());
        }
    }

    @Test
    void testGrantUserSecurityLevel() {
        Long userId = 1L;
        Integer securityLevelId = 30; // 秘密级

        SecurityLevelGrantDTO grantDTO = new SecurityLevelGrantDTO();
        grantDTO.setUserId(userId);
        grantDTO.setSecurityLevelId(securityLevelId);

        // 测试授权
        assertDoesNotThrow(() -> securityLevelService.grantUserSecurityLevel(grantDTO));

        // 验证授权结果
        Integer userLevel = securityLevelService.getUserSecurityLevel(userId);
        assertEquals(securityLevelId, userLevel);
    }

    @Test
    void testValidatePermission() {
        Long userId = 1L;
        String permissionCode = "user:view";

        PermissionValidateVO result = securityLevelService.validatePermission(userId, permissionCode, null);

        assertNotNull(result);
        assertTrue(result.isSecurityValid());
        assertNotNull(result.getUserSecurityLevel());
        assertNotNull(result.getRequiredSecurityLevel());
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PermissionIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testPermissionValidationFlow() {
        // 1. 用户登录获取token
        String token = authenticate("admin", "123456");

        // 2. 获取用户安全级别
        ResponseEntity<ResponseDTO<Integer>> levelResponse = restTemplate.exchange(
            "/api/security/level/user/level",
            HttpMethod.POST,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<Integer>>() {}
        );

        assertEquals(200, levelResponse.getStatusCodeValue());
        assertNotNull(levelResponse.getBody().getData());

        // 3. 验证权限
        ResponseEntity<ResponseDTO<PermissionValidateVO>> validateResponse = restTemplate.exchange(
            "/api/security/level/validate?permissionCode=user:view",
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<PermissionValidateVO>>() {}
        );

        assertEquals(200, validateResponse.getStatusCodeValue());
        assertTrue(validateResponse.getBody().getData().isSecurityValid());
    }
}
```

---

## 📊 性能优化

### 1. 多级缓存策略

```java
@Configuration
@EnableCaching
public class PermissionCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 一级缓存：Caffeine本地缓存
        CaffeineCacheManager localCacheManager = new CaffeineCacheManager();
        localCacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000));

        // 二级缓存：Redis分布式缓存
        RedisCacheConfiguration redisConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(redisConfig)
            .build();

        // 组合缓存管理器
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager(localCacheManager, redisCacheManager);
        compositeCacheManager.setFallbackToNoOpCache(true);

        return compositeCacheManager;
    }
}
```

### 2. 异步审计日志

```java
@Configuration
@EnableAsync
public class AsyncAuditConfig {

    @Bean("auditExecutor")
    public TaskExecutor auditExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("permission-audit-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 📋 检查清单

### 开发前检查

- [ ] 是否已理解5级安全级别的业务含义？
- [ ] 是否已确认数据权限隔离的需求？
- [ ] 是否已了解临时权限的申请流程？
- [ ] 是否已确认权限审计的要求？

### 开发中检查

- [ ] 是否实现了多级缓存策略？
- [ ] 是否添加了权限验证逻辑？
- [ ] 是否实现了数据权限过滤？
- [ ] 是否添加了审计日志记录？
- [ ] 是否进行了安全性测试？

### 部署前检查

- [ ] 权限缓存配置是否正确？
- [ ] 审计日志是否正常记录？
- [ ] 权限验证性能是否达标？
- [ ] 数据权限过滤是否有效？
- [ ] 安全级别控制是否严格？

---

**📞 技术支持**：架构师团队

**📚 相关文档**：
- [综合开发规范文档](../DEV_STANDARDS.md)
- [项目开发指南](../PROJECT_GUIDE.md)
- [通用开发检查清单](../CHECKLISTS/通用开发检查清单.md)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*