# Maven 编译环境配置说明

生成时间: 2025-11-19 07:35:04

## Windows PowerShell 环境变量设置

在 PowerShell 中执行以下命令，设置 UTF-8 编码：

```powershell
# 设置 Maven 编译编码
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN"

# 设置 Java 编译编码
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"

# 设置控制台输出编码
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

## 编译命令

使用以下命令编译项目（确保编码正确）：

```powershell
cd D:\IOE-DREAM\smart-admin-api-java17-springboot3
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
mvn clean compile -pl sa-base -am
```

## 永久环境变量设置（可选）

如果需要永久设置，可以在系统环境变量中添加：
- `MAVEN_OPTS` = `-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN`
- `JAVA_TOOL_OPTIONS` = `-Dfile.encoding=UTF-8`

或者创建 `%USERPROFILE%\.m2\settings.xml` 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <profiles>
        <profile>
            <id>utf-8</id>
            <properties>
                <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
            </properties>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>utf-8</activeProfile>
    </activeProfiles>
</settings>
```

## 一键编译脚本

### PowerShell 版本（推荐）

```powershell
# 编译所有模块（默认）
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1"

# 编译指定模块
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1" -Module base
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1" -Module support
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1" -Module admin

# 编译并运行测试
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1" -SkipTests:$false

# 不清理直接编译
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1" -Clean:$false

# 使用指定 Profile
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\compile.ps1" -Profile test
```

### Batch 版本（简单易用）

```cmd
# 编译所有模块（默认）
scripts\compile.bat

# 编译指定模块
scripts\compile.bat base
scripts\compile.bat support
scripts\compile.bat admin

# 编译并运行测试
scripts\compile.bat all /test

# 不清理直接编译
scripts\compile.bat all /no-clean

# 使用指定 Profile
scripts\compile.bat all /profile test
```

### 快速修复脚本

运行以下脚本自动设置环境并测试编译：

```powershell
powershell -ExecutionPolicy Bypass -File "D:\IOE-DREAM\scripts\fix-compilation-env.ps1"
```

## 验证编译成功

编译成功后会显示：
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  XX.XXX s
```

如果有编译错误，会显示：
```
[ERROR] BUILD FAILURE
```

## 注意事项

1. **javax.sql.DataSource** 是正确的（这是 JDK 标准库的一部分，不需要改为 jakarta.sql）
2. 编译时的警告（WARNING）可以忽略，但错误（ERROR）必须修复
3. 确保 Java 版本是 17 或更高
4. 确保 Maven 版本是 3.9 或更高

