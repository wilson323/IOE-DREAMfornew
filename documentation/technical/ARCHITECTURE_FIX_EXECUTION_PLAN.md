# IOE-DREAM 架构修复执行计划

**制定时间**: 2025-01-30  
**执行周期**: 1-2周  
**目标**: 100%架构合规，企业级生产标准

---

## 📋 执行概览

### 核心目标

1. **消除所有P0级架构违规**（@Autowired、@Repository）
2. **清理代码冗余**（Service重复、策略类重复）
3. **建立自动化检查机制**（CI/CD集成）
4. **确保企业级代码质量**（100%合规）

### 执行原则

- ✅ **安全第一**: 所有修改前备份
- ✅ **分批执行**: 避免一次性大范围修改
- ✅ **及时验证**: 每步修改后立即验证
- ✅ **可追溯**: 记录所有修改和问题

---

## 🚀 执行阶段

### 阶段1: P0级紧急修复（第1-2天）

#### 任务1.1: 批量替换@Autowired为@Resource

**执行步骤**:
```powershell
# 1. 备份代码
git commit -am "备份: 修复前代码快照"

# 2. 执行修复脚本
.\scripts\fix-architecture-violations.ps1

# 3. 验证修复结果
.\scripts\architecture-compliance-check.ps1

# 4. 编译验证
mvn clean compile -DskipTests

# 5. 提交修复
git add .
git commit -m "fix: 批量替换@Autowired为@Resource (114个实例)"
```

**预期结果**:
- ✅ 114个@Autowired实例全部替换为@Resource
- ✅ 0个编译错误
- ✅ 架构合规性检查通过

**验证标准**:
- [ ] 0个@Autowired残留
- [ ] 所有import语句正确
- [ ] 编译通过
- [ ] 单元测试通过

#### 任务1.2: 批量替换@Repository为@Mapper

**执行步骤**:
```powershell
# 1. 执行修复脚本（已包含在fix-architecture-violations.ps1中）
.\scripts\fix-architecture-violations.ps1

# 2. 验证修复结果
.\scripts\architecture-compliance-check.ps1

# 3. 编译验证
mvn clean compile -DskipTests

# 4. 提交修复
git add .
git commit -m "fix: 批量替换@Repository为@Mapper (78个实例)"
```

**预期结果**:
- ✅ 78个@Repository实例全部替换为@Mapper
- ✅ 0个编译错误
- ✅ MyBatis-Plus正确识别所有Mapper

**验证标准**:
- [ ] 0个@Repository残留
- [ ] 所有import语句正确
- [ ] 编译通过
- [ ] DAO接口正确识别

#### 任务1.3: 修复Repository命名违规

**执行步骤**:
1. 查找所有Repository后缀的文件
2. 重命名为Dao后缀
3. 更新所有引用
4. 验证编译

**预期结果**:
- ✅ 4个Repository命名违规全部修复
- ✅ 所有文件使用Dao后缀

---

### 阶段2: 代码冗余清理（第3-5天）

#### 任务2.1: UserService重复实现合并

**分析结果**:
- `common-service`: 117行，基础CRUD
- `identity-service`: 78行，完整实现（推荐保留）
- `auth-service`: 559行，JPA实现（需删除）

**执行步骤**:
1. 确定保留`identity-service`的实现
2. 创建适配器类适配`common-service`的调用
3. 删除`auth-service`的实现
4. 更新所有调用方

**预期结果**:
- ✅ 统一使用`identity-service`的UserService
- ✅ 其他服务通过适配器调用
- ✅ 代码冗余减少

#### 任务2.2: NotificationService重复实现合并

**执行步骤**:
1. 分析3个实现的差异
2. 确定保留的权威实现
3. 创建适配器或合并功能
4. 更新调用方

#### 任务2.3: 消费策略类重构

**执行步骤**:
1. 创建`BaseConsumptionModeStrategy`基类
2. 提取公共方法（getModeConfig、validateDailyLimit）
3. 统一依赖注入模式
4. 子类继承基类

**预期结果**:
- ✅ 代码重复减少60%+
- ✅ 维护成本降低
- ✅ 代码可读性提升

---

### 阶段3: 架构一致性强化（第6-7天）

#### 任务3.1: 建立自动化检查机制

**执行步骤**:
1. 完善`architecture-compliance-check.ps1`脚本
2. 添加更多检查项（跨层访问、命名规范等）
3. 生成HTML报告
4. 集成到CI/CD流程

#### 任务3.2: 更新CI/CD流程

**执行步骤**:
1. 在`.github/workflows/`中添加架构检查
2. 在Git pre-commit钩子中添加检查
3. PR合并前强制检查
4. 构建失败时阻止合并

---

### 阶段4: 质量保障机制（持续）

#### 任务4.1: 代码审查检查清单

**创建检查清单文档**:
- [ ] 使用@Resource而非@Autowired
- [ ] 使用@Mapper而非@Repository
- [ ] DAO使用Dao后缀
- [ ] 遵循四层架构边界
- [ ] 无代码冗余

#### 任务4.2: 定期架构扫描

**执行频率**: 每周一次  
**执行方式**: 自动化脚本  
**报告格式**: HTML + Markdown

---

## 📊 进度跟踪

### 执行状态看板

| 阶段 | 任务 | 状态 | 完成时间 | 负责人 |
|------|------|------|---------|--------|
| **阶段1** | 替换@Autowired | ⏳ 待执行 | - | 架构团队 |
| **阶段1** | 替换@Repository | ⏳ 待执行 | - | 架构团队 |
| **阶段1** | 修复命名违规 | ⏳ 待执行 | - | 架构团队 |
| **阶段2** | UserService合并 | ⏳ 待执行 | - | 后端团队 |
| **阶段2** | NotificationService合并 | ⏳ 待执行 | - | 后端团队 |
| **阶段2** | 策略类重构 | ⏳ 待执行 | - | 消费模块团队 |
| **阶段3** | 自动化检查 | ⏳ 待执行 | - | DevOps团队 |
| **阶段3** | CI/CD集成 | ⏳ 待执行 | - | DevOps团队 |

---

## ✅ 验收标准

### 阶段1验收标准

- [ ] 0个@Autowired使用
- [ ] 0个@Repository使用
- [ ] 0个Repository命名违规
- [ ] 编译通过
- [ ] 单元测试通过
- [ ] 架构合规性检查通过

### 阶段2验收标准

- [ ] UserService统一实现
- [ ] NotificationService统一实现
- [ ] 策略类代码重复减少60%+
- [ ] 功能测试通过
- [ ] 性能测试通过

### 阶段3验收标准

- [ ] 自动化检查脚本完善
- [ ] CI/CD集成完成
- [ ] 检查报告生成正常
- [ ] 团队培训完成

---

## 🚨 风险控制

### 潜在风险

1. **大规模修改导致编译错误**
   - **缓解措施**: 分批执行，及时验证
   - **回滚方案**: Git回滚到修复前版本

2. **功能回归**
   - **缓解措施**: 完整的单元测试和集成测试
   - **回滚方案**: 功能测试失败立即回滚

3. **团队协作冲突**
   - **缓解措施**: 提前通知团队，协调修改时间
   - **回滚方案**: 分支隔离，独立验证

### 应急预案

1. **编译错误**: 立即停止，分析原因，修复后继续
2. **测试失败**: 回滚到上一个稳定版本
3. **性能下降**: 性能测试，分析原因，优化

---

## 📝 执行日志

### 2025-01-30

- ✅ 创建全局深度分析报告
- ✅ 创建架构修复脚本
- ✅ 创建合规性检查脚本
- ✅ 创建执行计划文档

### 下一步行动

1. **立即执行**: 运行`fix-architecture-violations.ps1`修复P0级违规
2. **验证修复**: 运行`architecture-compliance-check.ps1`验证
3. **提交代码**: 修复后提交到版本控制
4. **通知团队**: 通知团队修复完成，开始阶段2

---

## 📚 相关文档

- [全局深度分析报告](./GLOBAL_DEEP_ANALYSIS_ROOT_CAUSE_SOLUTION.md)
- [架构规范文档](../../CLAUDE.md)
- [OpenSpec提案](../../openspec/changes/fix-critical-architecture-violations/)

---

**文档版本**: v1.0.0  
**最后更新**: 2025-01-30  
**维护团队**: IOE-DREAM架构委员会
