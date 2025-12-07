# IDE依赖问题修复指南

**版本**: v1.0.0  
**更新时间**: 2025-01-30  
**问题**: IDE报告找不到 `itext-core:9.4.0` 和 `html2pdf:9.4.0`

---

## 🔍 问题分析

### 根本原因

IDE错误信息显示查找的是 `itext-core`，但实际配置使用的是 `itext7-core`。这说明：

1. **IDE缓存问题**: IDE可能缓存了旧的错误配置
2. **Maven依赖未下载**: 依赖可能未正确下载到本地仓库
3. **IDE索引问题**: IDE的Maven索引可能过期

### 关键发现

- ✅ POM配置正确：使用的是 `itext7-core` 和 `html2pdf`
- ❌ IDE查找错误：IDE在查找 `itext-core`（错误的artifactId）
- ⚠️ 依赖未下载：本地Maven仓库可能缺少依赖

---

## 🚀 完整修复步骤

### 步骤1: 清理Maven缓存

```powershell
# 清理iText依赖缓存
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\itextpdf" -ErrorAction SilentlyContinue

# 清理腾讯云OCR依赖缓存
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\tencentcloudapi\tencentcloud-sdk-java-ocr" -ErrorAction SilentlyContinue
```

### 步骤2: 强制下载依赖

```powershell
# 运行强制下载脚本
cd D:\IOE-DREAM
.\scripts\force-download-dependencies.ps1

# 或手动下载
cd microservices\microservices-common
mvn dependency:get -Dartifact=com.itextpdf:itext7-core:9.4.0
mvn dependency:get -Dartifact=com.itextpdf:html2pdf:6.3.0

cd ..\ioedream-visitor-service
mvn dependency:get -Dartifact=com.tencentcloudapi:tencentcloud-sdk-java-ocr:3.1.1373
```

### 步骤3: 重新构建common模块

```powershell
cd D:\IOE-DREAM\microservices\microservices-common
mvn clean install -DskipTests -U
```

### 步骤4: 刷新IDE项目

**IntelliJ IDEA**:

1. 右键项目根目录 → Maven → Reload Project
2. File → Invalidate Caches / Restart
3. 选择 "Invalidate and Restart"

**Eclipse**:

1. 右键项目根目录 → Maven → Update Project
2. 勾选 "Force Update of Snapshots/Releases"
3. Project → Clean → 选择所有项目

### 步骤5: 验证依赖

```powershell
# 验证iText依赖
cd D:\IOE-DREAM\microservices\microservices-common
mvn dependency:tree -Dincludes=com.itextpdf:*

# 验证腾讯云OCR依赖
cd ..\ioedream-visitor-service
mvn dependency:tree -Dincludes=com.tencentcloudapi:*
```

---

## 🔧 代码修复

### RedisUtil导入修复 ✅

**文件**: `TransactionManagementManager.java:900`

**修复前**:

```java
java.util.Set<String> keys = net.lab1024.sa.common.util.RedisUtil.keys(fullPattern);
```

**修复后**:

```java
import java.util.Set;
import net.lab1024.sa.common.util.RedisUtil;

Set<String> keys = RedisUtil.keys(fullPattern);
```

---

## 📋 验证清单

- [ ] 清理Maven缓存
- [ ] 强制下载依赖
- [ ] 重新构建common模块
- [ ] 刷新IDE项目
- [ ] 验证依赖解析
- [ ] 检查IDE错误是否消失
- [ ] 编译所有微服务
- [ ] 运行单元测试

---

## 🐛 常见问题

### Q1: IDE仍然报错找不到依赖

**解决方案**:

1. 检查Maven仓库路径是否正确
2. 验证依赖是否真的下载到了本地仓库
3. 检查IDE的Maven配置（Settings → Build → Build Tools → Maven）

### Q2: 依赖下载失败

**解决方案**:

1. 检查网络连接
2. 检查Maven镜像配置（settings.xml）
3. 尝试使用阿里云Maven镜像

### Q3: 编译成功但IDE报错

**解决方案**:

1. 这是IDE索引问题，不影响实际编译
2. 刷新Maven项目并重启IDE
3. 如果问题持续，检查IDE的Maven插件版本

---

## 📚 相关文档

- [完整修复报告](./COMPREHENSIVE_FIX_REPORT.md)
- [最终修复总结](./FINAL_FIX_SUMMARY.md)
- [强制下载脚本](../scripts/force-download-dependencies.ps1)

---

**最后更新**: 2025-01-30
