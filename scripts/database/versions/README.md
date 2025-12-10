# 数据库版本管理脚本

## 概述

本目录包含各个数据库版本的独立处理脚本。每个版本对应一个PowerShell脚本，主脚本 `version-manager.ps1` 负责动态加载和调用这些版本脚本。

## 版本脚本列表

| 版本 | 脚本文件 | 描述 |
|------|---------|------|
| V1.0.0 | `version-v1.0.0.ps1` | 初始架构创建 |
| V1.1.0 | `version-v1.1.0.ps1` | 初始数据插入（支持环境变量） |
| V1.0.1 | `version-v1.0.1.ps1` | 索引优化 |
| V2.0.0 | `version-v2.0.0.ps1` | 消费记录表增强 |
| V2.0.1 | `version-v2.0.1.ps1` | 账户表增强 |
| V2.0.2 | `version-v2.0.2.ps1` | 退款表创建 |
| V2.1.0 | `version-v2.1.0.ps1` | API兼容性验证 |

## 脚本结构

每个版本脚本遵循以下结构：

```powershell
# 版本信息注释
param(
    [Parameter(Mandatory=$true)]
    [hashtable]$Config
)

function Invoke-VersionVXXX {
    # 版本执行逻辑
    # 1. 检查脚本文件是否存在
    # 2. 检查版本是否已执行
    # 3. 执行SQL脚本
    # 4. 返回执行结果
}

Export-ModuleMember -Function Invoke-VersionVXXX
```

## 使用方法

### 1. 查看当前版本状态

```powershell
.\scripts\database\version-manager.ps1 -Action status
```

### 2. 执行版本升级

```powershell
# 升级到最新版本
.\scripts\database\version-manager.ps1 -Action upgrade

# 升级到指定版本
.\scripts\database\version-manager.ps1 -Action upgrade -Version V2.0.0
```

### 3. 查看迁移历史

```powershell
.\scripts\database\version-manager.ps1 -Action history
```

### 4. 验证脚本和服务配置

```powershell
.\scripts\database\version-manager.ps1 -Action validate
```

## 添加新版本

要添加新版本，请按以下步骤操作：

1. **创建版本脚本文件**
   - 文件名格式：`version-vX.Y.Z.ps1`
   - 例如：`version-v2.2.0.ps1`

2. **实现版本处理函数**
   - 函数名格式：`Invoke-VersionVXYZ`
   - 例如：`Invoke-VersionV220`

3. **导出函数**
   ```powershell
   Export-ModuleMember -Function Invoke-VersionV220
   ```

4. **在主脚本中添加版本顺序**
   - 编辑 `version-manager.ps1`
   - 在 `$versionOrder` 数组中添加新版本

5. **创建对应的SQL脚本**
   - 在 `deployment/mysql/init/` 目录下创建SQL脚本
   - 文件名格式：`0X-vX.Y.Z__description.sql`

## 版本执行顺序

版本按照以下顺序执行：

1. V1.0.0 - 初始架构
2. V1.1.0 - 初始数据
3. V1.0.1 - 索引优化
4. V2.0.0 - 消费记录表增强
5. V2.0.1 - 账户表增强
6. V2.0.2 - 退款表创建
7. V2.1.0 - API兼容性验证

## 注意事项

1. **幂等性**：每个版本脚本都会检查是否已执行，避免重复执行
2. **错误处理**：如果某个版本执行失败，后续版本将不会执行
3. **环境支持**：V1.1.0 支持环境变量（dev/test/prod）
4. **依赖关系**：确保版本执行顺序正确，避免依赖问题

## 配置参数

版本脚本接收的 `$Config` 参数包含：

- `Args`: MySQL连接参数数组
- `Database`: 数据库名称（默认：ioedream）

## 示例

### 执行单个版本

```powershell
# 加载配置
$config = @{
    Args = @("-h", "localhost", "-P", "3306", "-u", "root")
    Database = "ioedream"
}

# 导入版本脚本
Import-Module .\scripts\database\versions\version-v2.0.0.ps1

# 执行版本
Invoke-VersionV200 -Config $config
```

### 批量执行所有版本

```powershell
.\scripts\database\version-manager.ps1 -Action upgrade
```

