# IOE-DREAM iText依赖统一管理规范

## 📋 概述

本文档规范IOE-DREAM项目中iText PDF库的依赖管理，确保所有微服务统一使用正确的依赖版本，避免IDE缓存和依赖解析问题。

## 🎯 核心原则

1. **统一依赖管理**：所有iText相关依赖统一在父POM中管理
2. **版本一致性**：所有服务使用相同的iText版本
3. **间接依赖优先**：服务通过`microservices-common`间接依赖，避免直接引用
4. **IDE兼容性**：考虑IDE的Maven插件限制，必要时在子POM中显式指定版本

## 📦 依赖版本

| 依赖 | 版本 | 说明 |
|------|------|------|
| `itext7-core` | 9.4.0 | iText 7核心库（最新稳定版） |
| `html2pdf` | 6.3.0 | HTML转PDF库（依赖itext7-core） |

## 🏗️ 架构设计

### 依赖层次结构

```
父POM (pom.xml)
├── dependencyManagement
│   └── itext7-core:9.4.0
│   └── html2pdf:6.3.0
│
microservices-common
├── 直接依赖 itext7-core
├── 直接依赖 html2pdf
│
业务微服务
├── 依赖 microservices-common（间接获得itext依赖）
└── 如需直接使用，通过父POM的dependencyManagement管理版本
```

### 正确的依赖配置

#### 父POM配置（microservices/pom.xml）

```xml
<properties>
    <!-- iText PDF -->
    <itext7-core.version>9.4.0</itext7-core.version>
    <html2pdf.version>6.3.0</html2pdf.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- iText PDF -->
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>itext7-core</artifactId>
            <version>${itext7-core.version}</version>
        </dependency>
        <dependency>
            <groupId>com.itextpdf</groupId>
            <artifactId>html2pdf</artifactId>
            <version>${html2pdf.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### microservices-common配置

```xml
<!-- iText PDF for PDF export -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <!-- 版本由父POM的dependencyManagement管理 -->
</dependency>
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <!-- 版本由父POM的dependencyManagement管理 -->
</dependency>
```

#### 业务微服务配置（推荐方式）

**方式1：通过microservices-common间接依赖（推荐）**

```xml
<!-- 通过microservices-common间接获得itext依赖 -->
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>microservices-common</artifactId>
    <version>${project.version}</version>
</dependency>
```

**方式2：直接依赖（仅在特殊需求时使用）**

```xml
<!-- 仅在需要直接使用itext且不依赖microservices-common时使用 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <!-- 版本由父POM的dependencyManagement管理 -->
</dependency>
```

## ❌ 禁止事项

### 1. 禁止使用错误的artifactId

```xml
<!-- ❌ 错误：使用itext-core（不存在） -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext-core</artifactId>  <!-- 错误！应该是itext7-core -->
    <version>9.4.0</version>
</dependency>

<!-- ✅ 正确：使用itext7-core -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>9.4.0</version>
</dependency>
```

### 2. 禁止使用错误的版本号

```xml
<!-- ❌ 错误：html2pdf没有9.4.0版本 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>9.4.0</version>  <!-- 错误！应该是6.3.0 -->
</dependency>

<!-- ✅ 正确：使用6.3.0版本 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>6.3.0</version>
</dependency>
```

### 3. 禁止在子POM中覆盖父POM版本（除非必要）

```xml
<!-- ❌ 错误：在子POM中覆盖版本，破坏版本统一性 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.0</version>  <!-- 错误！应该使用父POM的9.4.0 -->
</dependency>
```

## 🔧 问题排查

### 常见错误

#### 错误1：Missing artifact com.itextpdf:itext-core:jar:9.4.0

**原因**：
- IDE缓存了错误的依赖信息
- 某些地方错误地引用了`itext-core`而不是`itext7-core`

**解决方案**：
1. 运行修复脚本：`.\fix-itext-dependencies.ps1 -ForceUpdate`
2. 清理IDE缓存（参考IDE_REFRESH_GUIDE.md）
3. 检查所有pom.xml文件，确保没有错误的依赖引用

#### 错误2：IDE无法解析${itext7-core.version}

**原因**：
- IDE的Maven插件无法正确解析父POM的属性变量

**解决方案**：
- 在子POM中直接指定版本号（仅在IDE无法解析时使用）
- 添加注释说明版本应与父POM保持一致

```xml
<!-- 注意：版本号应与父POM保持一致，父POM: itext7-core.version=9.4.0 -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>9.4.0</version>  <!-- IDE兼容性：直接指定版本 -->
</dependency>
```

### 验证步骤

1. **检查父POM配置**
   ```powershell
   # 验证父POM中是否正确定义了版本
   Select-String -Path "microservices\pom.xml" -Pattern "itext7-core\.version.*9\.4\.0"
   ```

2. **检查依赖解析**
   ```powershell
   # 验证Maven能否正确解析依赖
   cd microservices
   mvn dependency:tree -Dincludes=com.itextpdf:itext7-core
   ```

3. **检查IDE错误**
   - 打开IDE的Maven项目视图
   - 检查是否还有`itext-core`相关的错误
   - 确认所有服务都能正确识别`itext7-core`

## 📚 使用示例

### 在代码中使用iText

```java
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class PdfGenerator {
    public void generatePdf(String outputPath) throws IOException {
        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        document.add(new Paragraph("Hello, iText 7!"));
        
        document.close();
    }
}
```

### 在微服务中使用PDF功能

**推荐方式**：通过microservices-common的Manager类使用

```java
@Service
public class ReportService {
    @Resource
    private AuditManager auditManager;  // 使用common模块的PDF功能
    
    public void exportReport() {
        // auditManager已经封装了PDF导出功能
        auditManager.exportToPdf(...);
    }
}
```

## 🔄 版本升级流程

当需要升级iText版本时：

1. **更新父POM版本属性**
   ```xml
   <itext7-core.version>9.5.0</itext7-core.version>  <!-- 新版本 -->
   ```

2. **验证新版本兼容性**
   - 检查Maven Central是否有新版本
   - 测试API兼容性
   - 运行单元测试

3. **更新所有子POM（如果直接指定了版本）**
   - 搜索所有包含`itext7-core`的pom.xml
   - 更新版本号
   - 更新注释说明

4. **清理并重新构建**
   ```powershell
   .\fix-itext-dependencies.ps1 -ForceUpdate
   mvn clean install -DskipTests
   ```

## 📊 依赖使用情况

| 微服务 | 使用方式 | 说明 |
|--------|---------|------|
| microservices-common | 直接依赖 | 提供PDF导出功能 |
| ioedream-consume-service | 间接依赖 | 通过common使用PDF报表功能 |
| ioedream-attendance-service | 间接依赖 | 通过common使用PDF导出功能 |
| ioedream-access-service | 间接依赖 | 通过common使用PDF功能 |
| ioedream-visitor-service | 间接依赖 | 通过common使用PDF功能 |
| ioedream-video-service | 间接依赖 | 通过common使用PDF功能 |

## 🛠️ 维护工具

### 修复脚本

位置：`microservices/fix-itext-dependencies.ps1`

功能：
- 检查所有服务的pom.xml配置
- 清理错误的Maven缓存
- 验证依赖解析
- 生成IDE刷新指南

使用方法：
```powershell
# 基本检查
.\fix-itext-dependencies.ps1

# 强制更新依赖
.\fix-itext-dependencies.ps1 -ForceUpdate

# 清理缓存并更新
.\fix-itext-dependencies.ps1 -CleanCache -ForceUpdate
```

## 📝 相关文档

- [Maven依赖管理最佳实践](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)
- [iText 7官方文档](https://itextpdf.com/en/products/itext-7)
- [IDE刷新指南](./IDE_REFRESH_GUIDE.md)
- [消费服务iText修复报告](../microservices/ioedream-consume-service/ITEXT_DEPENDENCY_FIX.md)

## ✅ 检查清单

在添加新的PDF功能或修改依赖时，请确认：

- [ ] 使用`itext7-core`而非`itext-core`
- [ ] 版本号与父POM保持一致
- [ ] 优先通过`microservices-common`间接依赖
- [ ] 如果直接依赖，版本由父POM的`dependencyManagement`管理
- [ ] 运行修复脚本验证配置
- [ ] 更新本文档记录变更

---

**最后更新**：2025-01-30  
**维护责任人**：架构委员会  
**版本**：1.0.0
