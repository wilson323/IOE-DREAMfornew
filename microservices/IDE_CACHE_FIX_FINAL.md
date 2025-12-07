# IDE iText依赖缓存问题 - 最终解决方案

## 🎯 问题确认

**IDE报错**：`Missing artifact com.itextpdf:itext-core:jar:9.4.0`

**实际情况**：
- ✅ pom.xml配置正确（使用`itext7-core:9.4.0`）
- ✅ Maven命令行构建正常
- ✅ 没有错误的传递依赖
- ❌ **IDE缓存了错误的依赖信息**

## ✅ 根源性解决方案

### 方案1：一键修复（推荐）

```powershell
cd microservices

# 步骤1: 清理错误的Maven缓存
Remove-Item -Recurse -Force "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core" -ErrorAction SilentlyContinue

# 步骤2: 强制更新依赖
mvn dependency:purge-local-repository -DmanualInclude="com.itextpdf:itext7-core,com.itextpdf:html2pdf"
mvn clean install -DskipTests -pl microservices-common

# 步骤3: 刷新IDE（根据你的IDE类型选择）
```

### 方案2：使用修复脚本

```powershell
cd microservices

# 运行诊断
.\diagnose-ide-dependencies.ps1

# 运行强制刷新
.\force-refresh-ide.ps1 -CleanMavenCache -ForceMavenUpdate

## IDE刷新步骤（必须执行）

### IntelliJ IDEA

1. **File → Invalidate Caches / Restart...**
2. **选择 "Invalidate and Restart"**
3. **等待IDE重启**
4. **右键项目根目录 → Maven → Reload Project**
5. **等待项目重新索引完成**

### Eclipse/STS

1. **右键项目根目录 → Maven → Update Project...**
2. **勾选 "Force Update of Snapshots/Releases"**
3. **勾选所有子项目**
4. **点击 OK**
5. **如果问题仍然存在**：右键项目 → Maven → Reload Project

### VS Code

1. **打开命令面板** (Ctrl+Shift+P)
2. **执行**: `Java: Clean Java Language Server Workspace`
3. **选择 "Restart and delete"**
4. **重启VS Code**
5. **等待Java扩展重新索引**

## 🔍 验证修复

运行验证脚本：

```powershell
cd microservices
.\verify-dependencies.ps1
```

**成功标准**：
- ✅ IDE不再显示`itext-core:9.4.0`错误
- ✅ IDE正确识别`itext7-core:9.4.0`
- ✅ Maven构建成功：`mvn clean compile -DskipTests`

## 🚨 如果问题仍然存在

### 完全清理方案

#### IntelliJ IDEA

1. **关闭IDE**
2. **删除缓存**：
   ```powershell
   Remove-Item -Recurse -Force "$env:USERPROFILE\.IntelliJIdea*\system\caches" -ErrorAction SilentlyContinue
   Remove-Item -Recurse -Force "$env:USERPROFILE\.IntelliJIdea*\system\index" -ErrorAction SilentlyContinue
   ```
3. **删除项目中的.idea目录**（可选）
4. **重新打开IDE并导入项目**

#### Eclipse

1. **关闭IDE**
2. **删除工作空间的.metadata目录**（会丢失工作空间设置）
3. **删除项目中的.settings目录**
4. **重新打开IDE并导入项目**

#### VS Code

1. **关闭VS Code**
2. **删除Java扩展缓存**：
   ```powershell
   Get-ChildItem "$env:USERPROFILE\.vscode\extensions" -Directory -Filter "redhat.java-*" | ForEach-Object {
       Remove-Item -Recurse -Force "$($_.FullName)\workspace" -ErrorAction SilentlyContinue
   }
   ```
3. **重新打开VS Code**

## 📚 相关文档

- [IDE缓存修复指南](../documentation/technical/ITEXT_IDE_CACHE_FIX.md)
- [依赖管理规范](../documentation/technical/ITEXT_DEPENDENCY_MANAGEMENT.md)
- [全局修复报告](../documentation/technical/GLOBAL_DEPENDENCY_FIX_REPORT.md)

---

**重要提示：IDE缓存问题是IDE的Maven插件导致的，不是项目配置问题。按照上述步骤刷新IDE后，问题应该会解决。
