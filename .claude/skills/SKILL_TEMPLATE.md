---
name: skill-template-name
description: 技能描述，明确说明这个技能的核心功能和应用场景
tools: Read, Write, Glob, Grep, Bash
color: blue  # 可选颜色: blue, green, yellow, orange, red, purple, cyan, pink
---

# 技能名称

## 核心职责
作为IOE-DREAM项目的[具体领域]专家，[简要说明核心职责和目标]。

## 核心能力

### 主要能力领域1
**能力描述**: 详细说明这个能力的作用和重要性

#### 具体实现方法
**必须遵循**:
- 具体要求1
- 具体要求2
- 具体要求3

**禁止使用**:
- 禁止事项1
- 禁止事项2
- 禁止事项3

#### 标准实现模板
```java
// 标准代码示例
public class StandardExample {
    // 完整的实现代码
}
```

#### 验证脚本
```bash
#!/bin/bash
# 自动化验证脚本
echo "🔍 执行[具体]检查..."
# 具体的验证逻辑
```

### 主要能力领域2
**能力描述**: 详细说明这个能力的作用和重要性

#### 实施步骤
1. **步骤1**: 详细说明
2. **步骤2**: 详细说明
3. **步骤3**: 详细说明

#### 代码模板
```typescript
// TypeScript/JavaScript示例
const example = {
    // 完整的实现代码
}
```

### 主要能力领域3
**能力描述**: 详细说明这个能力的作用和重要性

#### 配置示例
```yaml
# 配置文件示例
key: value
nested:
  item: value
```

#### 验证方法
- 方法1: 具体描述
- 方法2: 具体描述
- 方法3: 具体描述

## 基于repowiki规范的实现

### 规范引用
- **[Java编码规范](../docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)**: Java代码编写标准
- **[RESTfulAPI设计规范](../docs/repowiki/zh/content/开发规范体系/核心规范/RESTfulAPI设计规范.md)**: API接口设计标准
- **[架构设计规范](../docs/repowiki/zh/content/开发规范体系/核心规范/架构设计规范.md)**: 系统架构设计标准
- **[数据库设计规范](../docs/repowiki/zh/content/后端架构/数据模型与ORM/数据库设计规范/数据库设计规范.md)**: 数据库设计标准

### 强制约束
- ✅ **必须**: 基于repowiki规范的强制要求
- ✅ **必须**: 另一个强制要求
- ❌ **禁止**: repowiki规范中明确禁止的内容
- ❌ **禁止**: 另一个禁止内容

### 最佳实践
- 推荐做法1: 基于repowiki的最佳实践
- 推荐做法2: 具体的实施建议
- 推荐做法3: 优化和改进建议

## 错误预防机制

### 常见错误类型
1. **错误类型1**: 描述和预防方法
2. **错误类型2**: 描述和预防方法
3. **错误类型3**: 描述和预防方法

### 预防措施
- **自动化检查**: 使用脚本自动检测常见问题
- **代码审查**: 基于repowiki标准的审查清单
- **持续集成**: 在CI/CD流水线中集成检查
- **文档更新**: 及时更新技能文档和最佳实践

## 使用指南

### 调用时机
```bash
# 何时调用这个技能
Skill("skill-template-name")  # 具体场景
```

### 协同工作
```bash
# 与其他技能的协同工作
Skill("skill-template-name")  # 本技能
Skill("related-skill-1")      # 相关技能1
Skill("related-skill-2")      # 相关技能2
```

### 验证检查
```bash
# 完成后的验证检查
./scripts/verify-skill-compliance.sh
```

## 性能和质量标准

### 性能指标
- **响应时间**: 具体要求（如：≤100ms）
- **吞吐量**: 具体要求（如：≥1000 QPS）
- **资源使用**: CPU、内存使用限制

### 质量标准
- **代码覆盖率**: ≥80%
- **架构合规**: 100%符合repowiki规范
- **安全标准**: 满足等保三级要求

## 相关资源

### 文档链接
- [项目主页](../../README.md)
- [开发规范](../../docs/DEV_STANDARDS.md)
- [技术架构](../../docs/ARCHITECTURE_STANDARDS.md)

### 工具和脚本
- [验证脚本](./validation-scripts/)
- [配置模板](./templates/)
- [最佳实践](./best-practices/)

## 版本信息

- **版本**: v1.0.0
- **创建时间**: 2025-11-16
- **最后更新**: 2025-11-16
- **维护者**: IOE-DREAM开发团队
- **审核人**: [技术负责人]

---

*本技能严格基于IOE-DREAM项目的repowiki权威规范，确保所有开发工作符合项目的技术标准和业务要求。*