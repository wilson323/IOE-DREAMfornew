# IOE-DREAM 架构合规性指南

**版本**: v1.0.0  
**生效日期**: 2025-01-30  
**适用范围**: 所有微服务和模块

---

## 概述

本指南说明如何确保代码符合IOE-DREAM项目架构规范，包括检查方法、修复流程和最佳实践。

---

## 架构合规性检查

### 快速检查

运行统一检查脚本：

```powershell
.\scripts\architecture-compliance-check.ps1 -Detailed
```

### 专项检查

#### 检查@Repository违规

```powershell
.\scripts\check-repository-violations.ps1 -Detailed -OutputFormat both
```

#### 检查@Autowired违规

```powershell
.\scripts\check-autowired-violations.ps1 -Detailed -OutputFormat both
```

#### 检查Jakarta EE迁移

```powershell
.\scripts\check-jakarta-violations.ps1 -Detailed -OutputFormat both
```

#### 检查技术栈统一性

```powershell
.\scripts\tech-stack-consistency-check.ps1 -Detailed
```

---

## 常见违规及修复方法

### 1. @Repository违规

**违规示例**:

```java
@Repository  // ❌ 错误
public interface UserRepository extends BaseMapper<UserEntity> {
}
```

**修复方法**:

```java
@Mapper  // ✅ 正确
public interface UserDao extends BaseMapper<UserEntity> {
}
```

**修复步骤**:

1. 将 `@Repository` 替换为 `@Mapper`
2. 将接口名从 `*Repository` 改为 `*Dao`
3. 更新所有引用

---

### 2. @Autowired违规

**违规示例**:

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired  // ❌ 错误
    private UserDao userDao;
}
```

**修复方法**:

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Resource  // ✅ 正确
    private UserDao userDao;
}
```

**修复步骤**:

1. 将 `@Autowired` 替换为 `@Resource`
2. 更新import语句（如果需要）

---

### 3. Jakarta EE迁移违规

**违规示例**:

```java
import javax.annotation.Resource;  // ❌ 错误
import javax.validation.Valid;
```

**修复方法**:

```java
import jakarta.annotation.Resource;  // ✅ 正确
import jakarta.validation.Valid;
```

**修复步骤**:

1. 将 `javax.annotation.*` 替换为 `jakarta.annotation.*`
2. 将 `javax.validation.*` 替换为 `jakarta.validation.*`
3. 将 `javax.persistence.*` 替换为 `jakarta.persistence.*`
4. 将 `javax.servlet.*` 替换为 `jakarta.servlet.*`
5. 将 `javax.transaction.*` 替换为 `jakarta.transaction.*`

**注意**: 以下包允许继续使用 `javax.*`（Java SE标准库）：

- `javax.crypto.*`
- `javax.sql.*`
- `javax.imageio.*`
- `javax.net.ssl.*`

---

### 4. Manager类Spring注解违规

**违规示例**:

```java
@Component  // ❌ 错误
public class UserManager {
    
    @Resource  // ❌ 错误
    private UserDao userDao;
}
```

**修复方法**:

```java
// ✅ 正确：Manager类不使用Spring注解
public class UserManager {
    
    private final UserDao userDao;
    
    // 构造函数注入依赖
    public UserManager(UserDao userDao) {
        this.userDao = userDao;
    }
}

// 在配置类中注册为Bean
@Configuration
public class ManagerConfiguration {
    @Bean
    public UserManager userManager(UserDao userDao) {
        return new UserManager(userDao);
    }
}
```

---

## 自动检查集成

### Git Pre-commit钩子

每次提交前自动检查staged文件：

```powershell
# 提交时会自动执行检查
git commit -m "feat: 添加新功能"
```

如果发现违规，提交会被阻止。可以使用 `--no-verify` 跳过（不推荐）。

### CI/CD检查

PR合并前自动执行完整检查：

- GitHub Actions工作流自动执行
- 检查结果在PR中显示
- 违规会阻止合并

---

## 修复流程

### 发现违规后

1. **运行检查脚本**获取详细报告
2. **查看违规清单**了解需要修复的文件
3. **逐个文件修复**确保代码质量
4. **验证修复**重新运行检查脚本
5. **提交代码**通过pre-commit和CI/CD检查

### 批量修复建议

对于大量违规，建议：

- 按模块分批修复
- 优先修复新代码
- 历史代码逐步修复
- 确保每次修复后测试通过

---

## 最佳实践

### 开发时

1. **IDE配置**: 配置代码模板使用正确的注解
2. **编码规范**: 遵循项目编码规范
3. **定期检查**: 开发过程中定期运行检查脚本

### 提交前

1. **运行检查**: 提交前运行架构合规性检查
2. **修复违规**: 确保无违规后再提交
3. **更新文档**: 如有架构变更，更新相关文档

### 代码审查

1. **审查检查**: 代码审查时关注架构合规性
2. **指出问题**: 明确指出版规位置和修复方法
3. **验证修复**: 确保所有违规已修复

---

## 工具和脚本

### 检查脚本位置

- `scripts/architecture-compliance-check.ps1` - 统一检查入口
- `scripts/check-repository-violations.ps1` - @Repository检查
- `scripts/check-autowired-violations.ps1` - @Autowired检查
- `scripts/check-jakarta-violations.ps1` - Jakarta EE迁移检查
- `scripts/tech-stack-consistency-check.ps1` - 技术栈统一性检查

### 报告位置

- JSON报告: `reports/architecture-compliance-summary_*.json`
- Markdown报告: `reports/architecture-compliance-summary_*.md`
- CSV报告: `reports/*-violations_*.csv`

---

## 持续改进

### 定期审查

- 每周运行完整检查
- 每月生成合规性报告
- 每季度评估合规性趋势

### 规范更新

- 根据项目发展更新规范
- 收集团队反馈优化检查脚本
- 持续完善自动化检查机制

---

**维护责任人**: IOE-DREAM 架构委员会
