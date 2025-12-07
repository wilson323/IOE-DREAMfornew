# iText依赖配置修复报告

## 📋 问题描述

**发现时间**: 2025-01-30  
**问题**: `itext7-core:9.4.0` 配置不正确

**根本原因**:
- iText 9.x版本使用 `itext-core` 作为artifactId
- iText 8.x及以下版本使用 `itext7-core` 作为artifactId
- 项目配置中错误地使用了 `itext7-core:9.4.0`，应该使用 `itext-core:9.4.0`

## 🔍 版本对应关系

| iText版本 | artifactId | 说明 |
|-----------|-----------|------|
| 9.x | `itext-core` | iText 9.x系列 |
| 8.x | `itext7-core` | iText 8.x系列 |
| 7.x | `itext7-core` | iText 7.x系列 |

## ✅ 已修复的配置

### 1. 父POM配置 (`microservices/pom.xml`)

**修复前**:
```xml
<itext7-core.version>9.4.0</itext7-core.version>

<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>${itext7-core.version}</version>
</dependency>
```

**修复后**:
```xml
<!-- 注意：iText 9.x使用itext-core，iText 8.x使用itext7-core -->
<itext-core.version>9.4.0</itext-core.version>

<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext-core</artifactId>
    <version>${itext-core.version}</version>
</dependency>
```

### 2. microservices-common配置

**修复前**:
```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
</dependency>
```

**修复后**:
```xml
<!-- iText 9.x使用itext-core artifactId -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext-core</artifactId>
</dependency>
```

### 3. consume-service注释更新

更新了注释说明，明确iText 9.x使用`itext-core`。

## 📊 Maven Central验证

通过Maven Tools验证：
- ✅ `com.itextpdf:itext-core:9.4.0` - 存在且稳定
- ✅ `com.itextpdf:itext7-core:8.0.5` - 存在（iText 8.x版本）
- ❌ `com.itextpdf:itext7-core:9.4.0` - **不存在**（这是错误的配置）

## 🚀 后续操作

### 1. 清理Maven缓存

```powershell
# 清理错误的itext7-core缓存
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\itextpdf\itext7-core\9.4.0" -ErrorAction SilentlyContinue

# 强制下载正确的itext-core
cd D:\IOE-DREAM\microservices
mvn dependency:purge-local-repository -DmanualInclude="com.itextpdf:itext-core,com.itextpdf:html2pdf"
mvn clean install -pl microservices-common -am -DskipTests
```

### 2. 刷新IDE

**IntelliJ IDEA**:
1. `File → Invalidate Caches / Restart...`
2. 选择 `Invalidate and Restart`
3. 等待IDE重启并重新索引

**Maven项目刷新**:
1. 打开Maven工具窗口
2. 点击 `Reload All Maven Projects`

### 3. 验证修复

```powershell
# 验证依赖树
cd D:\IOE-DREAM\microservices
mvn dependency:tree -Dincludes=com.itextpdf:itext-core -pl microservices-common

# 检查JAR文件是否存在
Test-Path "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core\9.4.0\itext-core-9.4.0.jar"
```

## 📝 注意事项

1. **版本选择**: 
   - 如果使用iText 9.x → 使用 `itext-core`
   - 如果使用iText 8.x → 使用 `itext7-core:8.0.5`

2. **向后兼容性**: 
   - iText 9.x的API与8.x基本兼容
   - 但artifactId发生了变化

3. **html2pdf版本**: 
   - `html2pdf:6.3.0` 会传递依赖 `itext-core`（不是itext7-core）
   - 确保版本匹配

## 🔄 如果需要降级到iText 8.x

如果发现iText 9.x存在兼容性问题，可以降级到8.x：

```xml
<!-- 使用iText 8.x -->
<itext7-core.version>8.0.5</itext7-core.version>

<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>${itext7-core.version}</version>
</dependency>
```

---

**修复完成时间**: 2025-01-30  
**修复状态**: ✅ 已完成  
**验证状态**: ⏳ 待验证
