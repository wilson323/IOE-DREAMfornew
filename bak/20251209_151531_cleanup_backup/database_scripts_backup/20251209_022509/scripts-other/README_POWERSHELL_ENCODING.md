# PowerShell 脚本编码规范

## 问题描述

在Windows PowerShell中执行包含中文的脚本时，可能会出现乱码问题，例如：
- "修复" 显示为 "淇"
- "步骤" 显示为 "姝ラ"
- "成功" 显示为 "鎴愬姛"

## 根本原因

PowerShell默认使用的编码可能不是UTF-8，导致中文字符无法正确显示。

## 解决方案

在所有PowerShell脚本文件的开头（在param语句之前）添加以下编码设置：

```powershell
# 设置PowerShell输出编码为UTF-8，解决中文乱码问题
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null
```

## 标准模板

```powershell
# 脚本描述
# 功能说明

# 设置PowerShell输出编码为UTF-8，解决中文乱码问题
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

param(
    # 参数定义
)

# 脚本主体
```

## 已修复的脚本

以下脚本已经添加了UTF-8编码设置：
- ✅ `fix-attendance-build.ps1`
- ✅ `build-all.ps1`

## 检查清单

创建新的PowerShell脚本时，请确保：
- [ ] 脚本文件保存为UTF-8编码（带BOM或不带BOM都可以）
- [ ] 脚本开头包含UTF-8编码设置代码
- [ ] 测试脚本执行时中文显示正常

## 验证方法

执行脚本后，检查输出中的中文是否正常显示：
- 如果看到乱码，说明编码设置未生效
- 如果中文正常显示，说明编码设置成功

## 推荐执行方式

### 方法1: 使用英文版本（最可靠，无编码问题）⭐ 推荐

如果中文显示乱码，请使用英文版本：

```cmd
.\scripts\fix-attendance-build-en.bat
```

或直接执行：
```powershell
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\fix-attendance-build-en.ps1"
```

### 方法2: 使用中文版本批处理文件

```cmd
.\scripts\fix-attendance-build.bat
```

批处理文件会自动设置编码并执行PowerShell脚本。如果仍然出现乱码，请使用方法1（英文版本）。

### 方法2: 使用启动脚本

```powershell
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\run-fix-attendance-build.ps1"
```

### 方法3: 直接执行（如果方法1和方法2都不行）

```powershell
# 先设置编码
chcp 65001
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

# 再执行脚本
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\fix-attendance-build.ps1"
```

## 注意事项

1. **编码设置位置**：必须在脚本的最开始，在param语句之前
2. **文件编码**：脚本文件本身也应该保存为UTF-8编码
3. **PowerShell版本**：此方法适用于PowerShell 5.1及以上版本
4. **执行方式**：使用 `powershell -ExecutionPolicy Bypass -File script.ps1` 执行脚本

## 相关文档

- [PowerShell编码问题官方文档](https://docs.microsoft.com/powershell/module/microsoft.powershell.core/about/about_character_encoding)
- [项目构建规范](../documentation/technical/BUILD_ORDER_MANDATORY_STANDARD.md)
