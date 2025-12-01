# 架构完整性专家 - Architecture Integrity Specialist

**🎯 技能定位**: 确保分层架构完整性和依赖关系正确的专家

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: 新项目架构设计、现有项目架构重构、依赖关系诊断、开发流程优化

**📊 技能覆盖**: 分层架构 | 依赖分析 | 开发顺序指导 | 完整性验证

---

## 📋 核心能力

### **架构完整性检查**
- **依赖关系分析**: 自动检测缺失的Entity、DAO、Manager组件
- **分层架构验证**: 确保Controller→Service→Manager→DAO→Entity的完整链路
- **依赖倒置检查**: 验证上层不依赖下层具体实现的原则

### **开发顺序指导**
- **正确开发流程**: Entity→DAO→Manager→Service→Controller
- **依赖关系优化**: 避免循环依赖和不合理依赖
- **架构演进指导**: 从简单到复杂的渐进式架构建设

### **完整性修复策略**
- **缺失组件识别**: 自动识别所有缺失的架构组件
- **批量创建支持**: 批量创建Entity、DAO、Manager的基础模板
- **依赖关系修复**: 自动修复破损的依赖关系

---

## 🔧 核心诊断能力

### **架构完整性分析**
```bash
# 检查架构完整性
analyze_architecture_integrity() {
    1. 检查Entity层完整性
    2. 检查DAO层完整性
    3. 检查Manager层完整性
    4. 检查Service层完整性
    5. 检查Controller层完整性
    6. 分析依赖关系
    7. 识别缺失组件
    8. 生成修复方案
}
```

### **依赖关系检查**
```bash
# 依赖关系分析
check_dependency_relationships() {
    for each Service implementation:
        - 检查依赖的Entity是否存在
        - 检查依赖的DAO是否存在
        - 检查依赖的Manager是否存在
        - 验证依赖关系的合理性
        - 生成依赖关系报告
}
```

---

## 🛠️ 自动化修复工具

### **缺失组件批量创建**
```bash
# 自动创建缺失的Entity
create_missing_entities() {
    识别Service中引用的Entity
    生成Entity基础模板
    批量创建Entity文件
    更新依赖关系
}

# 自动创建缺失的DAO
create_missing_daos() {
    识别Entity对应的DAO
    生成DAO基础模板
    批量创建DAO文件
    配置MyBatis-Plus映射
}
```

### **依赖关系修复**
```bash
# 自动修复破损依赖
fix_broken_dependencies() {
    分析破损的import语句
    查找正确的组件路径
    批量修复import引用
    验证修复效果
}
```

---

## 🎯 最佳实践指导

### **正确的开发顺序**
```
Phase 1: Entity层 (数据模型)
├── 定义业务实体
├── 建立数据关系
└── 实体完整性验证

Phase 2: DAO层 (数据访问)
├── 创建Repository接口
├── 配置ORM映射
└── 数据访问测试

Phase 3: Manager层 (业务逻辑)
├── 复杂业务逻辑封装
├── 跨模块业务协调
└── 业务规则验证

Phase 4: Service层 (服务接口)
├── 业务接口定义
├── 事务管理配置
└── 异常处理

Phase 5: Controller层 (控制接口)
├── REST API接口
├── 参数验证
└── 响应格式化
```

### **完整性检查清单**
```bash
✅ Entity完整性: 所有业务实体都已定义
✅ DAO完整性: 每个Entity都有对应的DAO
✅ Manager完整性: 复杂业务都有Manager支持
✅ Service完整性: 所有业务服务都有接口和实现
✅ Controller完整性: 所有外部接口都有Controller
✅ 依赖完整性: 所有引用的组件都存在
✅ 架构合规: 严格遵循分层架构原则
```

---

## 📊 使用场景

### **场景1: 新项目架构设计**
```bash
# 调用方式
Skill("architecture-integrity-specialist")

# 预期结果
- 完整的分层架构设计
- 正确的开发顺序指导
- 依赖关系优化建议
- 架构完整性保障机制
```

### **场景2: 现有项目架构问题诊断**
```bash
# 问题诊断
analyze_existing_architecture()

# 输出结果
- 架构完整性报告
- 缺失组件清单
- 依赖关系问题
- 修复建议和优先级
```

### **场景3: 编译错误根因分析**
```bash
# 当遇到大量编译错误时
check_architecture_root_cause()

# 分析维度
- 是否为Entity层缺失
- 是否为DAO层缺失
- 是否为Manager层缺失
- 是否为依赖关系问题
- 生成根本原因报告
```

---

## 🎓 成功指标

### **架构完整性指标**
- **Entity完整性**: 100%的业务实体都存在
- **DAO完整性**: 100%的Entity都有对应的DAO
- **Manager完整性**: 复杂业务逻辑都有Manager支持
- **依赖完整性**: 0个缺失的依赖引用

### **开发效率指标**
- **一次性编译通过**: 新项目能够一次性编译通过
- **重构效率**: 架构重构能够快速完成
- **维护成本**: 降低后续维护成本
- **扩展性**: 支持新功能的快速扩展

---

**掌握此技能，您将成为架构完整性专家，能够确保项目的分层架构完整性，避免开发顺序错误导致的系统性问题。**