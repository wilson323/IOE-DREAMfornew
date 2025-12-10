# IOE-DREAM 合规性扫描工具

## 📋 简介

本目录包含IOE-DREAM项目的合规性扫描脚本，用于检测代码是否符合项目架构规范。

## 🎯 扫描内容

| 扫描脚本 | 检查内容 | 违规类型 |
|---------|---------|---------|
| `scan-repository-violations.ps1` | @Repository注解使用、Repository命名 | P0架构违规 |
| `scan-autowired-violations.ps1` | @Autowired注解使用 | P0架构违规 |
| `scan-architecture-violations.ps1` | Controller层架构边界 | P0架构违规 |
| `run-all-scans.ps1` | 执行所有扫描并生成综合报告 | 综合检查 |

## 🚀 快速开始

### 方式1: 执行单个扫描

```powershell
# 进入脚本目录
cd D:\IOE-DREAM\scripts\compliance-scan

# 执行@Repository违规扫描
powershell -ExecutionPolicy Bypass -File scan-repository-violations.ps1

# 执行@Autowired违规扫描
powershell -ExecutionPolicy Bypass -File scan-autowired-violations.ps1

# 执行架构违规扫描
powershell -ExecutionPolicy Bypass -File scan-architecture-violations.ps1
```

### 方式2: 执行全部扫描（推荐）

```powershell
# 进入脚本目录
cd D:\IOE-DREAM\scripts\compliance-scan

# 执行所有扫描并生成综合报告
powershell -ExecutionPolicy Bypass -File run-all-scans.ps1
```

## 📊 报告位置

所有扫描报告保存在：`D:\IOE-DREAM\reports\`

### 报告类型

- `repository-violations-[timestamp].txt` - @Repository违规详细报告
- `autowired-violations-[timestamp].txt` - @Autowired违规详细报告
- `architecture-violations-[timestamp].txt` - 架构违规详细报告
- `compliance-summary-[timestamp].md` - 综合扫描报告（推荐先看这个）

## 🔍 扫描结果示例

### 正常输出

```
✅ 扫描完成！
📊 统计结果:
   - @Repository注解违规: 0
   - Repository命名违规: 0
   - 总违规数: 0
📄 报告已保存: D:\IOE-DREAM\reports\repository-violations-20251203-173000.txt
```

### 发现违规

```
❌ 发现违规: microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\repository\AccessAreaRepository.java
❌ 发现命名违规: microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\repository\AccessDeviceRepository.java

✅ 扫描完成！
📊 统计结果:
   - @Repository注解违规: 5
   - Repository命名违规: 8
   - 总违规数: 13
```

## 🛠️ 故障排查

### 问题1: 执行策略错误

**错误信息**:
```
无法加载文件 scan-repository-violations.ps1，因为在此系统上禁止运行脚本。
```

**解决方案**:
```powershell
# 使用-ExecutionPolicy Bypass参数
powershell -ExecutionPolicy Bypass -File scan-repository-violations.ps1

# 或者临时设置执行策略
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
```

### 问题2: 路径不正确

确保项目路径正确：
- 项目根目录: `D:\IOE-DREAM`
- 微服务目录: `D:\IOE-DREAM\microservices`

如果路径不同，请修改脚本中的 `$projectRoot` 变量。

### 问题3: 报告目录不存在

脚本会自动创建 `D:\IOE-DREAM\reports` 目录，无需手动创建。

## 📈 修复建议

### @Repository违规修复

**自动修复命令**（谨慎使用）：
```powershell
# 替换@Repository为@Mapper
Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content $_.FullName) -replace '@Repository', '@Mapper' |
    Set-Content $_.FullName
}

# 更新import语句
Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content $_.FullName) -replace 'org.springframework.stereotype.Repository', 'org.apache.ibatis.annotations.Mapper' |
    Set-Content $_.FullName
}
```

**手动修复示例**：
```java
// ❌ 修复前
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends BaseMapper<AccountEntity> {
}

// ✅ 修复后
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {
}
```

### @Autowired违规修复

**自动修复命令**（谨慎使用）：
```powershell
# 替换@Autowired为@Resource
Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content $_.FullName) -replace '@Autowired', '@Resource' |
    Set-Content $_.FullName
}

# 更新import语句
Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Recurse -Filter "*.java" | 
ForEach-Object {
    (Get-Content $_.FullName) -replace 'org.springframework.beans.factory.annotation.Autowired', 'jakarta.annotation.Resource' |
    Set-Content $_.FullName
}
```

**手动修复示例**：
```java
// ❌ 修复前
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
}

// ✅ 修复后
import jakarta.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserDao userDao;
}
```

### 架构违规修复

**需要手动修复**，不能自动化：

```java
// ❌ 违规代码
@RestController
public class UserController {
    @Resource
    private UserDao userDao;  // Controller不应直接注入DAO
}

// ✅ 修复后
@RestController
public class UserController {
    @Resource
    private UserService userService;  // 通过Service层访问
}
```

## ⚠️ 注意事项

1. **备份代码**: 执行自动修复前，请先备份代码或确保Git提交干净
2. **测试验证**: 修复后必须运行测试验证功能正常
3. **逐步修复**: 建议先修复一小部分，验证无误后再批量修复
4. **架构违规**: 需要手动创建Service层，不能简单替换

## 📚 相关文档

- [完整修复计划](../../COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md) - 详细的Phase-by-Phase修复计划
- [架构规范](../../CLAUDE.md) - IOE-DREAM项目架构规范
- [全局分析报告](../../GLOBAL_PROJECT_DEEP_ANALYSIS_2025-12-03.md) - 项目深度分析

## 🔄 定期扫描建议

### 开发阶段
- 每日提交前运行扫描
- 使用Git Pre-commit Hook自动检查

### 集成阶段
- CI/CD流程中集成扫描
- 每次Pull Request必须通过扫描

### 维护阶段
- 每周执行一次全面扫描
- 每月生成合规性报告

## 📞 支持

如有问题或建议，请：
1. 查看 [CLAUDE.md](../../CLAUDE.md) 了解规范细节
2. 查看 [修复计划](../../COMPREHENSIVE_REMEDIATION_PLAN_2025-12-03.md) 了解修复步骤
3. 联系架构团队获取支持

---

**工具版本**: 1.0.0  
**创建日期**: 2025-12-03  
**维护团队**: IOE-DREAM 架构委员会

