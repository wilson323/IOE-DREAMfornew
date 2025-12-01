# IntelliJ IDEA 批量优化导入指南

## 📋 操作步骤

### 1️⃣ **单文件优化**
- **Windows快捷键**: `Ctrl + Alt + O`
- **功能**: 优化当前打开文件的导入语句

### 2️⃣ **批量优化整个项目**（重点推荐）

#### 方法A：使用菜单操作
```
1. 在Project面板中，右键点击 "smart-admin-api-java17-springboot3" 根目录
2. 选择 "Code" → "Optimize Imports"
3. 在弹出对话框中选择：
   ✅ Run inspection by name
   ✅ Optimize imports
4. 点击 "Run" 开始批量优化
```

#### 方法B：使用快捷键（更快速）
```
1. 在Project面板中，点击项目根目录
2. 按 Ctrl + Alt + L（格式化代码）
3. 在弹出对话框中勾选：
   ✅ Optimize imports
   ✅ Rearrange entries
4. 点击 "Run"
```

#### 方法C：使用Code Cleanup（最彻底）
```
1. 点击菜单：Code → Code Cleanup
2. 选择 "smart-admin-api-java17-springboot3" 目录
3. 在弹出对话框中配置：
   ✅ Optimize imports
   ✅ Remove unused imports
   ✅ Rearrange code
   ✅ Reformat code
4. 点击 "Run" 开始清理
```

### 3️⃣ **配置自动优化（预防措施）**

#### 配置保存时自动优化
```
1. File → Settings (Ctrl + Alt + S)
2. Editor → General → Auto Import
3. 勾选以下选项：
   ✅ Optimize imports on the fly
   ✅ Add unambiguous imports on the fly
4. 点击 "Apply" → "OK"
```

#### 配置提交前自动优化
```
1. File → Settings (Ctrl + Alt + S)
2. Version Control → Commit
3. 在 "Before Commit" 区域勾选：
   ✅ Optimize imports
   ✅ Reformat code
   ✅ Rearrange code
   ✅ Perform code analysis
4. 点击 "Apply" → "OK"
```

## ⚙️ IDEA配置建议

### 1. 设置导入排序规则
```
File → Settings → Editor → Code Style → Java → Imports

配置如下：
- General: Use single class import
- Class count to use import with '*': 99
- Names count to use static import with '*': 99
- Import Layout:
  1. import jakarta.*
  2. <blank line>
  3. import com.baomidou.*
  4. <blank line>
  5. import net.lab1024.sa.*
  6. <blank line>
  7. import org.*
  8. <blank line>
  9. import java.*
  10. <blank line>
  11. import javax.*
  12. <blank line>
  13. all other imports
  14. <blank line>
  15. import static all other imports
```

### 2. 启用警告提示
```
File → Settings → Editor → Inspections

搜索并启用以下检查：
✅ Unused import
✅ Unused declaration
✅ Redundant type arguments
✅ Redundant cast
✅ Deprecated API usage
```

## 🔍 检查优化效果

### 在IDEA中查看问题统计
```
1. Analyze → Inspect Code
2. 选择 "smart-admin-api-java17-springboot3"
3. 点击 "OK"
4. 在Inspection Results面板查看：
   - Unused imports
   - Unused declarations
   - Code quality issues
```

## 📝 注意事项

1. **备份代码**: 批量优化前建议提交当前代码到Git
2. **逐步验证**: 优化后运行编译测试确保功能正常
3. **分模块处理**: 如果项目很大，建议分模块逐个优化
4. **检查删除**: 确保删除的导入确实未使用

## 🚀 批量优化命令（终极方案）

如果需要更彻底的批量处理，可以使用IDEA的命令行模式：

### 创建批量优化脚本
```powershell
# optimize-all-imports.ps1
$projectPath = "D:\IOE-DREAM\smart-admin-api-java17-springboot3"
$ideaPath = "C:\Program Files\JetBrains\IntelliJ IDEA\bin\idea64.exe"

# 使用IDEA命令行工具批量优化
& $ideaPath format -r -settings $projectPath\.idea -mask "*.java" $projectPath

Write-Host "✅ 批量优化完成！" -ForegroundColor Green
```

## 📊 预期效果

优化后应该解决：
- ✅ 删除所有未使用的导入（约80处）
- ✅ 统一导入排序规则
- ✅ 清除重复的导入
- ✅ 合并wildcard导入
- ✅ 提升代码质量评分

## 🔧 故障排除

### 问题1：快捷键不生效
**解决方案**: 
```
File → Settings → Keymap
搜索 "Optimize Imports"
检查快捷键是否被其他功能占用
```

### 问题2：优化后编译失败
**解决方案**:
```
1. 使用 Ctrl + Z 撤销修改
2. 先运行 Maven Clean Compile 确保项目正常
3. 逐个模块优化并测试
```

### 问题3：某些导入被误删
**解决方案**:
```
1. 检查是否有通过反射使用的类
2. 在该类上添加 @SuppressWarnings("unused") 注解
3. 或在Settings → Editor → Inspections中排除特定包
```

## ✨ 最佳实践

1. **每日优化**: 养成每天下班前优化一次的习惯
2. **提交前检查**: 使用Pre-commit Hook自动优化
3. **Code Review**: 在代码审查时关注导入整洁度
4. **持续集成**: 在CI流水线中加入代码质量检查

---

**文档生成时间**: 2025-11-16
**适用项目**: IOE-DREAM 智慧园区一卡通管理平台
**技术栈**: Java 17 + Spring Boot 3.5.4 + IntelliJ IDEA

