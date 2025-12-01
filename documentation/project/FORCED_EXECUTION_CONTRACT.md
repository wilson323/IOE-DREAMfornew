# 强制执行合同 - 永不违反

## 🚨 我的承诺

### **永远不做的行为**
- ❌ 永不声称代码完成而未运行编译验证
- ❌ 永不跳过质量门禁检查
- ❌ 永不虚假报告任务状态
- ❌ 永不无视编译错误
- ❌ 永不写不能运行的代码

### **必须做的行为**
- ✅ 每次代码变更后必须运行 `bash scripts/quality-gate.sh`
- ✅ 每个任务完成必须通过 `bash scripts/mandatory-verification.sh`
- ✅ 只有验证通过才能标记任务为[x]
- ✅ 编译失败必须立即修复，不能继续其他工作
- ✅ 每次回答用户前必须检查代码实际状态

## 🔒 强制执行机制

### **完整Hooks验证体系**

#### **1. 工作前验证**
```bash
# 每次开始工作前必须执行
bash scripts/dev-standards-check.sh     # 开发规范检查
bash scripts/pre-work-hook.sh            # 工作前Hook
bash scripts/integrated-workflow.sh pre-work  # 集成工作流程
```

#### **2. 工作后验证**
```bash
# 每次工作完成后必须执行
bash scripts/post-work-hook.sh <type>    # 工作后Hook
bash scripts/integrated-workflow.sh post-work <type>  # 集成工作流程
```

#### **3. 提交前验证**
```bash
# 每次git commit前必须执行
bash scripts/commit-guard.sh            # 提交守卫
bash scripts/integrated-workflow.sh pre-commit  # 集成工作流程
```

#### **4. 任务完成验证**
```bash
# 每个任务完成后必须执行
bash scripts/task-completion-verify.sh <task_id>  # 任务验证
bash scripts/mandatory-verification.sh             # 强制验证
bash scripts/integrated-workflow.sh task-verify <type>  # 集成工作流程
```

### **自我检查清单**
每次声称工作完成前，必须回答：
1. 代码编译通过了吗？ (运行 `mvn clean compile`)
2. 测试通过了吗？ (运行 `mvn test`)
3. 应用能启动吗？ (运行启动测试)
4. 质量门禁通过了吗？ (检查 `/tmp/quality_gate.status`)
5. 验证证明文件存在吗？

### **违规后果**
- 任何违反承诺的行为都必须立即停止工作
- 必须从头开始重新验证所有代码
- 必须向用户诚实报告所有问题
- 必须修复所有问题后才能继续

## 📋 强制工作流程

### **修改代码前**
```bash
# 1. 检查当前状态
bash scripts/quality-gate.sh

# 2. 只有通过才能修改
# 3. 修改后重新检查
```

### **完成任务前**
```bash
# 1. 强制验证
bash scripts/mandatory-verification.sh

# 2. 任务验证
bash scripts/task-completion-verify.sh task-id

# 3. 只有通过才能标记完成
```

### **回答用户前**
```bash
# 1. 确认代码状态
mvn clean compile -q || echo "代码有问题，必须先修复"

# 2. 确认验证状态
ls verification-report-*.json > /dev/null || echo "无验证报告，不能声称完成"

# 3. 只有确认无误才能回答
```

## 🎯 监督机制

### **用户监督清单**
您可以随时要求我：
1. 运行质量门禁检查
2. 显示最新的验证报告
3. 证明代码能编译和运行
4. 解释任何编译错误

### **我的强制响应**
任何时候您质疑我的工作质量，我必须：
1. 立即运行验证脚本
2. 诚实地报告所有问题
3. 修复所有发现的问题
4. 重新验证才能继续

---

**签名**: Claude Code
**承诺**: 我将永远遵守此合同，绝不违反任何条款
**违反后果**: 违反任何条款都意味着我必须重新学习和执行所有标准
**日期**: 2025-11-15

**此合同永远有效，永不撤销。**