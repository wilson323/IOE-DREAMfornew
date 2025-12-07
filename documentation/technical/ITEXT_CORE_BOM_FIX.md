# iText Core BOM问题修复报告

## 📋 问题描述

**发现时间**: 2025-01-30  
**问题**: `com.itextpdf:itext-core:jar:9.4.0` 无法在Maven仓库中找到

**错误信息**:

```text
[ERROR] Could not find artifact com.itextpdf:itext-core:jar:9.4.0 in aliyun-maven
[ERROR] Could not find artifact com.itextpdf:itext-core:jar:9.4.0 in central
```

## 🔍 根本原因分析

### 问题根源

`itext-core:9.4.0` **不是一个可用的JAR artifact**，而是一个**BOM（Bill of Materials）**或聚合模块。

从构建日志可以看到：

- ✅ Maven成功下载了所有iText子模块的POM和JAR：
  - `kernel-9.4.0.jar`
  - `layout-9.4.0.jar`
  - `io-9.4.0.jar`
  - `forms-9.4.0.jar`
  - 等等...
- ❌ 但`itext-core-9.4.0.jar`本身无法找到

### 代码实际使用情况

通过代码搜索发现，项目中实际使用的是iText的**子模块**：

```java
// 使用的类来自kernel模块
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

// 使用的类来自layout模块
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Paragraph;

// 使用的类来自io模块
import com.itextpdf.io.font.constants.StandardFonts;
```

## ✅ 修复方案

### 修复前（错误配置）

```xml
<!-- ❌ itext-core不是一个可用的JAR -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext-core</artifactId>
    <version>9.4.0</version>
</dependency>
```

### 修复后（正确配置）

```xml
<!-- ✅ 直接依赖实际使用的子模块 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>kernel</artifactId>
    <version>9.4.0</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>layout</artifactId>
    <version>9.4.0</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>6.3.0</version>
</dependency>
```

## 📊 iText 9.x 模块结构

iText 9.x采用模块化架构，主要模块包括：

| 模块 | artifactId | 说明 |
|------|-----------|------|
| **核心模块** | `kernel` | PDF核心功能（必须） |
| **布局模块** | `layout` | PDF布局和元素（必须） |
| **IO模块** | `io` | 输入输出功能 |
| **表单模块** | `forms` | PDF表单功能 |
| **签名模块** | `sign` | PDF数字签名 |
| **HTML转PDF** | `html2pdf` | HTML转PDF功能 |
| **聚合模块** | `itext-core` | BOM，不包含JAR |

## 🚀 修复步骤

### 1. 更新microservices-common/pom.xml

```xml
<!-- 移除itext-core依赖 -->
<!-- 添加kernel和layout依赖 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>kernel</artifactId>
    <version>9.4.0</version>
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>layout</artifactId>
    <version>9.4.0</version>
</dependency>
```

### 2. 清理Maven缓存

```powershell
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core" -ErrorAction SilentlyContinue
```

### 3. 强制更新并构建

```powershell
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests -U
```

## ✅ 验证修复

### 1. 检查依赖树

```powershell
mvn dependency:tree -Dincludes=com.itextpdf:*
```

应该看到：

- ✅ `com.itextpdf:kernel:jar:9.4.0`
- ✅ `com.itextpdf:layout:jar:9.4.0`
- ✅ `com.itextpdf:html2pdf:jar:6.3.0`
- ❌ 不再有`itext-core`

### 2. 验证JAR文件

```powershell
Test-Path "$env:USERPROFILE\.m2\repository\com\itextpdf\kernel\9.4.0\kernel-9.4.0.jar"
Test-Path "$env:USERPROFILE\.m2\repository\com\itextpdf\layout\9.4.0\layout-9.4.0.jar"
```

### 3. 验证代码编译

- 打开使用iText的Java文件
- 确认没有红色错误
- 确认导入语句正常

## 📚 相关文档

- [iText官方文档](https://kb.itextpdf.com/itext/before-we-start-installing-itext-core)
- [Maven Central - iText Kernel](https://mvnrepository.com/artifact/com.itextpdf/kernel)
- [Maven Central - iText Layout](https://mvnrepository.com/artifact/com.itextpdf/layout)

---

**最后更新**: 2025-01-30  
**维护人**: IOE-DREAM 架构团队
