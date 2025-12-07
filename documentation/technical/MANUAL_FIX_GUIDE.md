# IOE-DREAM 架构违规手动修复指南

**重要原则**: ⚠️ **禁止使用脚本自动修改代码，所有修复必须手动完成**

---

## 🎯 修复原则

### 核心原则

1. **禁止自动修改**: 不使用正则表达式或脚本批量替换代码
2. **手动审查**: 每个文件必须人工审查后修复
3. **确保质量**: 修复后必须验证编译和测试通过
4. **全局一致性**: 确保修复后代码符合项目规范

### 为什么禁止自动修改？

- ✅ **代码质量**: 手动修复可以确保代码逻辑正确
- ✅ **上下文理解**: 理解代码上下文，避免误修改
- ✅ **规范遵循**: 确保修复符合项目具体规范
- ✅ **可追溯性**: 每个修改都有明确的审查记录

---

## 📋 修复流程

### 步骤1: 检查违规

```powershell
# 运行检查脚本（仅检查，不修改）
.\scripts\fix-architecture-violations.ps1
```

**输出**: 生成修复报告 `documentation/technical/ARCHITECTURE_VIOLATIONS_FIX_REPORT.md`

### 步骤2: 查看修复报告

打开修复报告，了解需要修复的文件和位置。

### 步骤3: 逐个文件手动修复

**修复顺序**:
1. 优先修复P0级违规（@Autowired、@Repository）
2. 然后修复命名违规
3. 最后修复其他问题

**修复方法**:
- 打开IDE（IntelliJ IDEA / Eclipse）
- 使用IDE的重构功能（Find & Replace）
- 逐个文件审查和修复
- 确保import语句正确

### 步骤4: 验证修复

```powershell
# 编译验证
mvn clean compile -DskipTests

# 架构合规性检查
.\scripts\architecture-compliance-check.ps1

# 单元测试
mvn test
```

---

## 🔧 具体修复方法

### 1. @Autowired → @Resource 修复

#### 修复步骤

1. **打开文件**: 在IDE中打开需要修复的文件

2. **查找@Autowired**: 使用IDE的Find功能查找 `@Autowired`

3. **替换注解**:
   ```java
   // ❌ 错误示例
   @Autowired
   private UserService userService;
   
   // ✅ 正确示例
   @Resource
   private UserService userService;
   ```

4. **更新import语句**:
   ```java
   // ❌ 删除旧的import
   import org.springframework.beans.factory.annotation.Autowired;
   
   // ✅ 添加新的import（如果还没有）
   import jakarta.annotation.Resource;
   ```

5. **验证**: 确保代码编译通过

#### IDE操作示例（IntelliJ IDEA）

1. 按 `Ctrl+Shift+F` 打开全局搜索
2. 搜索 `@Autowired`
3. 在结果中双击打开文件
4. 使用 `Ctrl+R` 打开替换对话框
5. 在文件中逐个替换（不要使用"Replace All"）
6. 检查import语句，使用 `Alt+Enter` 自动导入

### 2. @Repository → @Mapper 修复

#### 修复步骤

1. **打开文件**: 在IDE中打开DAO接口文件

2. **替换注解**:
   ```java
   // ❌ 错误示例
   @Repository
   public interface UserRepository extends BaseMapper<UserEntity> {
   }
   
   // ✅ 正确示例
   @Mapper
   public interface UserDao extends BaseMapper<UserEntity> {
   }
   ```

3. **更新import语句**:
   ```java
   // ❌ 删除旧的import
   import org.springframework.stereotype.Repository;
   
   // ✅ 添加新的import
   import org.apache.ibatis.annotations.Mapper;
   ```

4. **重命名类**: 如果类名包含Repository，需要重命名
   - 使用IDE的重构功能（Shift+F6）
   - 确保所有引用都已更新

#### IDE操作示例（IntelliJ IDEA）

1. 打开DAO接口文件
2. 将光标放在 `@Repository` 上
3. 使用 `Ctrl+R` 替换为 `@Mapper`
4. 将光标放在类名上
5. 使用 `Shift+F6` 重命名类（如果包含Repository）
6. IDE会自动更新所有引用

### 3. Repository命名 → Dao命名修复

#### 修复步骤

1. **重命名文件**:
   - 在IDE中右键点击文件
   - 选择 "Refactor" → "Rename"
   - 将 `*Repository.java` 改为 `*Dao.java`

2. **重命名类**:
   ```java
   // ❌ 错误示例
   public interface UserRepository extends BaseMapper<UserEntity> {
   }
   
   // ✅ 正确示例
   public interface UserDao extends BaseMapper<UserEntity> {
   }
   ```

3. **更新所有引用**:
   - IDE会自动更新同一模块内的引用
   - 需要手动检查跨模块引用
   - 使用全局搜索查找所有引用

#### IDE操作示例（IntelliJ IDEA）

1. 右键点击文件 → "Refactor" → "Rename"
2. 输入新名称（如 `UserDao`）
3. 选择 "Rename file" 和 "Rename class"
4. 预览更改，确认无误后应用
5. 使用 `Ctrl+Shift+F` 全局搜索，检查是否有遗漏的引用

---

## ✅ 修复验证清单

每个文件修复后，请确认：

- [ ] **注解已正确替换**
  - [ ] @Autowired → @Resource
  - [ ] @Repository → @Mapper

- [ ] **import语句已更新**
  - [ ] 删除了旧的import
  - [ ] 添加了新的import

- [ ] **命名已规范**
  - [ ] Repository后缀 → Dao后缀
  - [ ] 类名已更新
  - [ ] 所有引用已更新

- [ ] **代码编译通过**
  ```powershell
  mvn clean compile -DskipTests
  ```

- [ ] **架构合规性检查通过**
  ```powershell
  .\scripts\architecture-compliance-check.ps1
  ```

- [ ] **单元测试通过**
  ```powershell
  mvn test
  ```

---

## 🚨 常见问题

### Q1: 如何确保不遗漏任何文件？

**A**: 
1. 运行检查脚本生成完整报告
2. 按照报告逐个文件修复
3. 修复后再次运行检查脚本验证

### Q2: 修复后编译失败怎么办？

**A**:
1. 检查import语句是否正确
2. 检查类名是否已更新
3. 检查是否有遗漏的引用
4. 查看编译错误信息，逐个修复

### Q3: 如何确保全局一致性？

**A**:
1. 使用统一的修复方法
2. 参考项目规范文档
3. 修复后运行合规性检查
4. 代码审查确保一致性

### Q4: 修复需要多长时间？

**A**:
- @Autowired违规（114个文件）: 约2-3小时
- @Repository违规（78个文件）: 约1-2小时
- Repository命名违规（4个文件）: 约30分钟
- **总计**: 约4-6小时（取决于文件复杂度）

---

## 📚 相关文档

- [架构规范文档](../../CLAUDE.md)
- [全局深度分析报告](./GLOBAL_DEEP_ANALYSIS_ROOT_CAUSE_SOLUTION.md)
- [执行计划文档](./ARCHITECTURE_FIX_EXECUTION_PLAN.md)

---

## 🎯 修复优先级

### P0级（立即修复）

1. **@Autowired违规** - 114个文件
2. **@Repository违规** - 78个文件
3. **Repository命名违规** - 4个文件

### P1级（本周完成）

1. 代码冗余清理
2. 架构边界优化

### P2级（持续优化）

1. 代码质量提升
2. 性能优化

---

**文档版本**: v1.0.0  
**最后更新**: 2025-01-30  
**维护团队**: IOE-DREAM架构委员会
