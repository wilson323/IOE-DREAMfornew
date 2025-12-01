# 🚀 技能快速调用手册 (场景化版)

> **版本**: v1.0.0 - 铁腕执行版
> **更新时间**: 2025-11-23
> **用途**: 遇到问题立即调用对应技能，零思考、零延迟

---

## 🔴 紧急场景 (立即调用，10分钟解决)

### 场景1: 编译错误 > 50个
**症状**: `mvn compile` 报错，ERROR数量超过50个
**识别方式**: 编译失败，控制台大量ERROR信息
**立即调用**: `Skill("code-quality-protector")`
**预期效果**: 30分钟内减少75%编译错误
**验证命令**: `./scripts/skill-effectiveness-validator.sh code-quality-protector`

```bash
# 立即执行
Skill("code-quality-protector")
```

### 场景2: Jakarta包名违规
**症状**: 代码中出现 `javax.annotation`、`javax.validation`、`javax.persistence` 等
**识别方式**: IDE中javax包名被标记为红色
**立即调用**: `Skill("spring-boot-jakarta-guardian")`
**预期效果**: 5分钟内完成所有包名迁移
**验证命令**: `./scripts/skill-effectiveness-validator.sh spring-boot-jakarta-guardian`

```bash
# 立即执行
Skill("spring-boot-jakarta-guardian")
```

### 场景3: @Autowired违规
**症状**: 代码中使用 `@Autowired` 注解
**识别方式**: 代码审查发现@Autowired标记
**立即调用**: `Skill("spring-boot-jakarta-guardian")`
**预期效果**: 2分钟内完成所有依赖注入修复
**验证命令**: `./scripts/skill-effectiveness-validator.sh spring-boot-jakarta-guardian`

```bash
# 立即执行
Skill("spring-boot-jakarta-guardian")
```

### 场景4: 架构违规
**症状**: Controller直接访问DAO层
**识别方式**: 代码中出现 `@Resource private XxxDao`
**立即调用**: `Skill("four-tier-architecture-guardian")`
**预期效果**: 10分钟内完成架构重构
**验证命令**: `./scripts/skill-effectiveness-validator.sh four-tier-architecture-guardian`

```bash
# 立即执行
Skill("four-tier-architecture-guardian")
```

---

## 🟡 重要场景 (1小时内解决)

### 场景5: 编译错误 10-50个
**症状**: 中等数量编译错误
**识别方式**: 编译失败但ERROR数量可控
**调用顺序**:
1. `Skill("spring-boot-jakarta-guardian")` (先解决基础问题)
2. `Skill("code-quality-protector")` (再解决编译错误)

```bash
# 按顺序执行
Skill("spring-boot-jakarta-guardian")
Skill("code-quality-protector")
```

### 场景6: 实体类设计问题
**症状**: Entity类缺少getter/setter，Lombok失效
**识别方式**: 找不到符号错误集中在Entity相关
**立即调用**: `Skill("code-quality-protector")`
**预期效果**: 15分钟内修复所有Entity类问题

```bash
# 立即执行
Skill("code-quality-protector")
```

### 场景7: 依赖冲突问题
**症状**: 依赖版本冲突，方法签名不匹配
**识别方式**: 编译错误提示方法不存在
**立即调用**: `Skill("code-quality-protector")`
**预期效果**: 20分钟内解决依赖冲突

```bash
# 立即执行
Skill("code-quality-protector")
```

---

## 🟢 建议场景 (提升质量)

### 场景8: 代码质量优化
**症状**: 项目能运行但代码质量不高
**识别方式**: 代码复杂度高，重复代码多
**调用**: `Skill("code-quality-protector")`
**预期效果**: 提升代码质量，减少技术债务

```bash
# 质量优化
Skill("code-quality-protector")
```

### 场景9: 开发规范统一
**症状**: 团队代码风格不统一
**识别方式**: 代码审查发现规范不一致
**调用**: `Skill("development-standards-specialist")`
**预期效果**: 统一团队开发规范

```bash
# 规范统一
Skill("development-standards-specialist")
```

---

## 🤖 自动化诊断流程

### 智能问题诊断 (推荐首选)
```bash
# 运行智能诊断，自动推荐技能
./scripts/intelligent-skill-mapper.sh
```

### 强制技能检查
```bash
# 强制检查是否需要调用技能
./scripts/mandatory-skill-check.sh
```

### 完整AI推荐
```bash
# AI智能推荐最佳技能序列
./scripts/intelligent-skill-recommender.sh
```

---

## 📊 技能效果验证

### 单个技能验证
```bash
# 验证特定技能效果
./scripts/skill-effectiveness-validator.sh <skill_name>
```

### 整体效果验证
```bash
# 验证所有调用技能的总体效果
./scripts/skill-effectiveness-validator.sh all
```

---

## ⚡ 快速决策矩阵

| 问题类型 | 紧急程度 | 推荐技能 | 解决时间 | 验证命令 |
|---------|---------|---------|---------|---------|
| 编译错误 > 100 | 🔴 紧急 | code-quality-protector | 30分钟 | validator |
| Jakarta违规 | 🔴 紧急 | spring-boot-jakarta-guardian | 5分钟 | validator |
| @Autowired违规 | 🔴 紧急 | spring-boot-jakarta-guardian | 2分钟 | validator |
| 架构违规 | 🔴 紧急 | four-tier-architecture-guardian | 10分钟 | validator |
| 编译错误 10-100 | 🟡 重要 | jakarta-guardian + code-protector | 45分钟 | validator |
| 代码质量问题 | 🟢 建议 | code-quality-protector | 60分钟 | validator |

---

## 🎯 技能调用最佳实践

### 1. 诊断优先原则
```bash
# 总是先运行诊断
./scripts/problem-skill-mapper.sh
```

### 2. 优先级执行原则
```bash
# 按紧急程度执行技能
# P1: spring-boot-jakarta-guardian
# P2: code-quality-protector
# P3: four-tier-architecture-guardian
```

### 3. 效果验证原则
```bash
# 每个技能调用后都要验证
./scripts/skill-effectiveness-validator.sh <skill_name>
```

### 4. 持续监控原则
```bash
# 定期运行健康检查
./scripts/intelligent-skill-recommender.sh
```

---

## 📞 紧急联系

如果遇到以下情况，请立即停止并联系技术负责人：
- 技能调用后问题数量增加
- 技能调用导致新问题出现
- 技能调用效果与预期严重不符

---

## 🔄 更新记录

- **v1.0.0** (2025-11-23): 初始版本，包含所有核心场景
- **更新责任人**: 老王 - 专治各种代码不规范

---

**🎯 核心原则**: 遇到问题不要犹豫，立即调用对应技能！时间就是金钱，效率就是生命！