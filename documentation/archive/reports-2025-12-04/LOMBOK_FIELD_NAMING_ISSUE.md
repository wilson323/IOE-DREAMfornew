# Lombok字段命名问题分析报告

**发现时间**: 2025-12-02
**问题类型**: 字段名称与方法引用不匹配
**严重级别**: 🔴 P0 - 阻塞编译

---

## 🚨 根本原因

### Entity字段 vs DAO方法引用不匹配

#### 问题1: UserEntity 字段命名

**Entity定义**:
```java
@Data
public class UserEntity extends BaseEntity {
    private Boolean enableMfa;          // ✅ Entity字段名
    private Boolean needChangePassword; // ✅ Entity字段名
    private String description;         // ❌ 字段不存在
}
```

**DAO使用**:
```java
public interface UserDao extends BaseMapper<UserEntity> {
    queryWrapper.eq(UserEntity::getMfaEnabled, 1);          // ❌ 错误：字段是enableMfa
    queryWrapper.eq(UserEntity::getPasswordResetRequired, 1); // ❌ 错误：字段是needChangePassword
    queryWrapper.eq(UserEntity::getDescription, "xxx");     // ❌ 错误：字段不存在
}
```

#### 问题2: AreaPersonEntity 字段命名

**Entity定义**:
```java
@Data
public class AreaPersonEntity extends BaseEntity {
    private Integer permissionLevel; // ✅ Entity字段名
    // ❌ 缺少 accessLevel 字段
    // ❌ 缺少 authorizedBy 字段  
    // ❌ 缺少 authorizedTime 字段
}
```

**DAO使用**:
```java
queryWrapper.eq(AreaPersonEntity::getAccessLevel, level);      // ❌ 字段不存在
queryWrapper.eq(AreaPersonEntity::getAuthorizedBy, userId);    // ❌ 字段不存在
queryWrapper.ge(AreaPersonEntity::getAuthorizedTime, startTime); // ❌ 字段不存在
```

---

## 🔧 解决方案

### 方案1: 统一字段命名（推荐）✅

#### UserEntity 修复
```java
// 添加缺失字段或修改DAO引用
@Data
public class UserEntity extends BaseEntity {
    private Boolean enableMfa;          // getEnableMfa() / isEnableMfa()
    private Boolean needChangePassword; // getNeedChangePassword() / isNeedChangePassword()
    private String remark;              // 使用remark而非description
}
```

**DAO修复**:
```java
// 修改方法引用以匹配实际字段
queryWrapper.eq(UserEntity::getEnableMfa, true);  // 或 isEnableMfa
queryWrapper.eq(UserEntity::getNeedChangePassword, true);
queryWrapper.like(UserEntity::getRemark, keyword);
```

#### AreaPersonEntity 修复
```java
// 添加缺失字段
@Data
public class AreaPersonEntity extends BaseEntity {
    private Integer permissionLevel;  // 保留
    private Integer accessLevel;      // ✅ 新增
    private Long authorizedBy;        // ✅ 新增
    private LocalDateTime authorizedTime; // ✅ 新增
}
```

### 方案2: 手动添加getter方法（不推荐）

直接在Entity中手动添加缺失的getter方法：
```java
// ❌ 不推荐：破坏Lombok的自动化
public Boolean getMfaEnabled() {
    return this.enableMfa;
}
```

---

## 📋 待修复清单

### UserEntity字段问题
1. `enableMfa` → 修复DAO引用为 `getEnableMfa()` 或添加别名字段
2. `needChangePassword` → 修复DAO引用
3. `description` → 字段不存在，检查是否应该是 `remark`

### AreaPersonEntity字段问题
1. `accessLevel` → 字段缺失，需要添加
2. `authorizedBy` → 字段缺失，需要添加
3. `authorizedTime` → 字段缺失，需要添加

---

**修复策略**: 优先检查Entity定义，补充缺失字段，统一命名规范
**预期效果**: 消除所有"找不到方法"的编译错误

