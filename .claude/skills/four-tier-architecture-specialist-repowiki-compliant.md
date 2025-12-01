# 四层架构专家 (Repowiki标准版)

## 🎯 技能定位
**核心职责**: 基于 `.qoder/repowiki` 中的《四层架构详解》规范，确保IOE-DREAM项目严格遵循Controller→Service→Manager→DAO四层架构模式

**⚡ 技能等级**: ★★★★ (架构守护专家)
**🎯 适用场景**: 架构设计审查、代码分层检查、跨层访问检测、架构合规验证
**📊 技能覆盖**: 四层架构设计 | 架构违规预防 | 跨层访问修复 | 模块化架构

---

## 📋 技能概述 (基于Repowiki规范)

### **核心专长 (基于.qoder/repowiki/zh/content/后端架构/四层架构详解.md)**
- **四层架构守护**: 严格确保Controller→Service→Manager→DAO分层模式
- **跨层访问预防**: 检测并修复任何违反分层架构的代码
- **模块化设计**: 基于.qoder/repowiki模块化设计规范实施架构优化
- **数据模型验证**: 确保ORM层与数据库设计的一致性

### **解决能力**
- **架构合规性检查**: 100%符合四层架构规范
- **跨层访问修复**: Controller/Service直接访问DAO的违规检测和修复
- **依赖倒置预防**: 确保依赖方向符合分层架构原则
- **接口设计优化**: 基于分层架构的API设计规范化

---

## 🏗️ Repowiki四层架构规范

### **第一层：Controller层 (控制层)**
- **职责**: 处理HTTP请求、参数验证、响应格式化
- **规范来源**: `.qoder/repowiki/zh/content/后端架构/四层架构详解.md`
- **依赖**: 只能依赖Service层，严禁直接依赖Manager或DAO层
- **输出**: 统一API响应格式，不包含业务逻辑

### **第二层：Service层 (服务层)**
- **职责**: 业务逻辑处理、事务管理、业务规则实现
- **规范来源**: `.qoder/repowiki/zh/content/后端架构/四层架构详解.md`
- **依赖**: 依赖Manager层和Service层内部依赖
- **输出**: 业务处理结果，不涉及数据访问细节

### **第三层：Manager层 (管理层)**
- **职责**: 复杂业务逻辑编排、多Service协作、缓存管理
- **规范来源**: `.qoder/repowiki/zh/content/后端架构/四层架构详解.md`
- **依赖**: 依赖DAO层和Manager层内部依赖
- **输出**: 组装后的业务数据，协调多个Service

### **第四层：DAO层 (数据访问层)**
- **职责**: 数据库操作、ORM映射、基础查询逻辑
- **规范来源**: `.qoder/repowiki/zh/content/后端架构/数据模型与ORM/`
- **依赖**: 只依赖数据库和实体类
- **输出**: 原始数据或简单处理后的数据

---

## 🛠️ 核心工作流程 (基于Repowiki)

### **Phase 1: 架构合规性诊断**
```bash
# 检查四层架构合规性
./scripts/check-four-tier-architecture.sh

# 检测跨层访问违规
grep -r "@Resource.*Dao" --include="*Controller.java" .
grep -r "@Resource.*Manager" --include="*Service.java" .

# 验证依赖方向
./scripts/validate-dependency-direction.sh
```

### **Phase 2: 架构违规修复**
```bash
# 修复Controller跨层访问
# 错误: @Resource private UserDao userDao;
# 正确: @Resource private UserService userService;

# 修复Service直接DAO访问
# 错误: @Resource private OrderDao orderDao;
# 正确: @Resource private OrderManager orderManager;
```

### **Phase 3: 架构优化建议**
```bash
# 基于repowiki规范的架构优化
./suggest-architecture-optimization.sh

# 模块化设计建议
./suggest-modularization.sh
```

---

## 🔍 架构合规性检查清单 (基于Repowiki)

### **✅ 强制性规范 (必须100%遵循)**

#### **依赖注入规范**
- [ ] Controller只能注入Service (`@Resource private XxxService`)
- [ ] Service只能注入Manager和其他Service (`@Resource private XxxManager`)
- [ ] Manager只能注入DAO和其他Manager (`@Resource private XxxDao`)
- [ ] DAO层不注入任何业务层组件

#### **方法调用规范**
- [ ] Controller方法调用 → Service方法
- [ ] Service方法调用 → Manager方法
- [ ] Manager方法调用 → DAO方法
- [ ] 禁止Controller直接调用Manager/DAO
- [ ] 禁止Service直接调用DAO

#### **业务逻辑分层规范**
- [ ] Controller: 无业务逻辑，只做参数验证和格式转换
- [ ] Service: 简单业务逻辑，单个业务场景
- [ ] Manager: 复杂业务逻辑，多Service协作
- [ ] DAO: 数据操作，无业务逻辑

### **⚠️ 推荐性规范**

#### **接口设计规范**
- [ ] 统一使用DTO进行数据传输
- [ ] Controller返回Result包装类
- [ ] 避免Entity直接暴露到API层

#### **异常处理规范**
- [ ] Controller层异常处理和HTTP状态码转换
- [ ] Service层业务异常处理
- [ ] Manager层异常协调和恢复
- [ ] DAO层数据访问异常处理

---

## 🚀 架构违规检测与修复

### **常见架构违规类型**
```bash
# 1. Controller直接访问DAO
# ❌ 错误示例
@RestController
public class UserController {
    @Resource
    private UserDao userDao;  // 违规！
}

# ✅ 正确示例
@RestController
public class UserController {
    @Resource
    private UserService userService;
}

# 2. Service直接访问DAO
# ❌ 错误示例
@Service
public class UserService {
    @Resource
    private OrderDao orderDao;  // 违规！
}

# ✅ 正确示例
@Service
public class UserService {
    @Resource
    private OrderManager orderManager;
}
```

### **自动化检测脚本**
```bash
#!/bin/bash
# 四层架构合规性检测脚本
echo "=== 四层架构合规性检测 ==="

# 检测Controller违规
controller_violations=$(grep -r "@Resource.*Dao\|@Autowired.*Dao" \
    --include="*Controller.java" . | wc -l)

# 检测Service违规
service_violations=$(grep -r "@Resource.*Dao\|@Autowired.*Dao" \
    --include="*Service*.java" . | grep -v "Test" | wc -l)

# 检测Manager使用
manager_usage=$(grep -r "@Resource.*Manager\|@Autowired.*Manager" \
    --include="*Service*.java" . | wc -l)

echo "Controller跨层违规: $controller_violations 个"
echo "Service跨层违规: $service_violations 个"
echo "Manager使用规范: $manager_usage 个"
```

---

## 📊 架构质量评估标准

### **架构合规性评分**
| 维度 | 权重 | 评分标准 |
|------|------|----------|
| 分层合规性 | 30% | 无跨层访问得满分 |
| 依赖方向正确性 | 25% | 依赖方向完全正确 |
| 接口设计规范 | 20% | DTO和包装类使用规范 |
| 异常处理完整性 | 15% | 各层异常处理完善 |
| 代码可维护性 | 10% | 分层清晰，职责明确 |

### **质量等级**
- **A级 (90-100分)**: 完全符合四层架构规范
- **B级 (80-89分)**: 基本合规，存在轻微违规
- **C级 (70-79分)**: 部分合规，需要重点改进
- **D级 (60-69分)**: 架构混乱，需要重构
- **E级 (0-59分)**: 严重违反架构规范

---

## 🎯 使用指南

### **何时调用**
- 新增模块时进行架构设计审查
- 代码审查时检查架构合规性
- 重构现有代码时验证分层合理性
- 性能优化时评估架构影响

### **调用方式**
```bash
# 基于repowiki的四层架构专家
Skill("four-tier-architecture-specialist-repowiki-compliant")

# 将立即执行：
# 1. 基于.qoder/repowiki规范的架构合规检查
# 2. 架构违规检测和修复建议
# 3. 四层架构优化方案生成
```

### **预期结果**
- 100%符合`.qoder/repowiki`四层架构规范
- 零跨层访问违规
- 清晰的分层职责划分
- 高质量的架构设计文档

---

**🏆 技能等级**: 架构守护专家 (★★★)
**⏰ 预期效果**: 基于249个repowiki权威文档，确保IOE-DREAM项目架构100%合规
**🎯 核心价值**: 权威规范守护，架构质量保障，技术债务预防