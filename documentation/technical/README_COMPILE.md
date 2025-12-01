# 一键编译脚本使用说明

## 脚本说明

本项目提供了两个一键编译脚本，用于简化 Maven 编译流程：

1. **compile.ps1** - PowerShell 脚本（功能强大，推荐使用）
2. **compile.bat** - Batch 脚本（简单易用，兼容性好）

## 功能特点

- ✅ 自动设置 UTF-8 编码环境变量
- ✅ 自动检查 Java 和 Maven 环境
- ✅ 支持编译指定模块
- ✅ 支持跳过测试或运行测试
- ✅ 支持清理或不清理编译
- ✅ 支持选择不同的 Profile
- ✅ 详细的编译输出和错误信息
- ✅ 编译时间统计

## 使用方法

### PowerShell 版本

#### 基本用法

```powershell
# 编译所有模块（默认）
scripts\compile.ps1

# 或使用完整路径
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1"
```

#### 编译指定模块

```powershell
# 编译 sa-base 模块（包含依赖）
scripts\compile.ps1 -Module base

# 编译 sa-support 模块（包含依赖）
scripts\compile.ps1 -Module support

# 编译 sa-admin 模块（包含依赖）
scripts\compile.ps1 -Module admin

# 编译所有模块
scripts\compile.ps1 -Module all
```

#### 编译选项

```powershell
# 跳过测试（默认）
scripts\compile.ps1 -SkipTests

# 运行测试
scripts\compile.ps1 -SkipTests:$false

# 清理后编译（默认）
scripts\compile.ps1 -Clean

# 不清理直接编译
scripts\compile.ps1 -Clean:$false

# 使用指定 Profile（默认：dev）
scripts\compile.ps1 -Profile dev
scripts\compile.ps1 -Profile test
scripts\compile.ps1 -Profile pre
scripts\compile.ps1 -Profile prod
```

#### 组合使用

```powershell
# 编译 base 模块，运行测试，使用 test profile
scripts\compile.ps1 -Module base -SkipTests:$false -Profile test

# 编译所有模块，不清理，跳过测试
scripts\compile.ps1 -Module all -Clean:$false -SkipTests
```

### Batch 版本

#### 基本用法

```cmd
# 编译所有模块（默认）
scripts\compile.bat
```

#### 编译指定模块

```cmd
# 编译指定模块
scripts\compile.bat base
scripts\compile.bat support
scripts\compile.bat admin
scripts\compile.bat all
```

#### 编译选项

```cmd
# 清理后编译（默认）
scripts\compile.bat all /clean

# 不清理直接编译
scripts\compile.bat all /no-clean

# 运行测试
scripts\compile.bat all /test

# 跳过测试（默认）
scripts\compile.bat all /no-test

# 使用指定 Profile
scripts\compile.bat all /profile dev
scripts\compile.bat all /profile test
```

#### 组合使用

```cmd
# 编译 base 模块，运行测试，使用 test profile
scripts\compile.bat base /test /profile test

# 编译所有模块，不清理，跳过测试
scripts\compile.bat all /no-clean /no-test
```

## 参数说明

### PowerShell 参数

| 参数 | 类型 | 说明 | 默认值 |
|------|------|------|--------|
| `-Module` | String | 编译的模块：all, base, support, admin | all |
| `-SkipTests` | Switch | 是否跳过测试 | true |
| `-Clean` | Switch | 是否清理后编译 | true |
| `-Profile` | String | Maven Profile：dev, test, pre, prod | dev |

### Batch 参数

| 参数 | 说明 | 示例 |
|------|------|------|
| 第一个参数 | 模块名称：all, base, support, admin | `base` |
| `/clean` | 清理后编译（默认） | `/clean` |
| `/no-clean` | 不清理直接编译 | `/no-clean` |
| `/test` | 运行测试 | `/test` |
| `/no-test` | 跳过测试（默认） | `/no-test` |
| `/profile` | Maven Profile | `/profile test` |

## 输出说明

### 成功输出

```
=== 编译成功 ===
编译时间: 11.26 秒
结束时间: 2025-11-19 07:35:04
```

### 失败输出

```
=== 编译失败 ===
编译时间: 5.43 秒
结束时间: 2025-11-19 07:35:04

错误信息：
  [ERROR] /path/to/file.java:[10,20] 找不到符号
  [ERROR]   符号:   类 DataSource
  [ERROR]   位置: 类 net.lab1024.sa.base.config.DataSourceConfig
```

## 环境要求

- **Java**: 17 或更高版本
- **Maven**: 3.9 或更高版本
- **操作系统**: Windows 10/11
- **PowerShell**: 5.1 或更高版本（PowerShell 脚本需要）

## 常见问题

### 1. PowerShell 执行策略限制

如果遇到执行策略限制，使用以下命令：

```powershell
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1"
```

### 2. 编码问题

脚本会自动设置 UTF-8 编码环境变量，如果仍然出现乱码，请检查：
- PowerShell 控制台编码设置
- 系统区域设置

### 3. Java 或 Maven 未找到

确保 Java 和 Maven 已正确安装并配置到系统 PATH 环境变量中。

### 4. 编译失败

检查：
- 代码是否有编译错误
- 依赖是否正确下载
- 网络连接是否正常

## 技术细节

### 环境变量设置

脚本会自动设置以下环境变量：

```powershell
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

### Maven 命令构建

根据参数构建 Maven 命令：

```powershell
# 编译指定模块
mvn clean compile -pl sa-base -am -Pdev -DskipTests

# 编译所有模块
mvn clean compile -Pdev -DskipTests
```

## 相关文档

- [编译环境配置说明](../docs/COMPILATION_ENV_SETUP.md)
- [项目开发规范](../docs/DEV_STANDARDS.md)
- [CLAUDE.md](../CLAUDE.md)

