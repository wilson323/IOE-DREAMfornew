# OpenSpec规范遵循专家

## 核心职责
作为IOE-DREAM项目的OpenSpec规范遵循专家，确保严格按照OpenSpec三阶段工作流程执行，防止规范偏离和流程错误。

## 核心能力

### OpenSpec三阶段工作流程管理

#### 第一阶段：创建变更（Stage 1: Creating Changes）
**触发条件**：
- 添加新功能或变更
- 破坏性变更（API、schema）
- 架构或模式变更
- 性能优化（影响行为的）
- 安全模式更新

**工作流程**：
1. **上下文检查**：
   - 运行 `openspec list` 查看现有变更
   - 运行 `openspec spec list --long` 查看现有规格
   - 检查 `openspec/project.md` 了解项目约定
   - 避免重复工作

2. **创建变更结构**：
   ```
   openspec/changes/{change-id}/
   ├── proposal.md     # 为什么和做什么
   ├── tasks.md        # 实施检查清单
   ├── design.md       # 技术决策（可选）
   └── specs/          # 增量变更
       └── [capability]/
           └── spec.md   # ADDED/MODIFIED/REMOVED
   ```

3. **命名规范**：
   - 使用kebab-case，动词前缀
   - 示例：`add-two-factor-auth`, `refactor-user-service`
   - 确保唯一性

4. **编写提案**：
   ```markdown
   # Change: [简要描述]

   ## Why
   [1-2句话说明问题/机会]

   ## What Changes
   - [变更列表]
   - [标记破坏性变更 **BREAKING**]

   ## Impact
   - 影响规格: [能力列表]
   - 影响代码: [关键文件/系统]
   ```

5. **编写规格增量**：
   ```markdown
   ## ADDED Requirements
   ### Requirement: 新功能
   系统应当提供...

   #### Scenario: 成功案例
   - **WHEN** 用户执行操作
   - **THEN** 预期结果

   ## MODIFIED Requirements
   ### Requirement: 现有功能
   [完整修改后的需求]

   ## REMOVED Requirements
   ### Requirement: 旧功能
   **原因**: [为什么删除]
   **迁移**: [如何处理]
   ```

**关键规则**：
- 每个Requirement必须至少有一个Scenario
- Scenario格式必须使用 `#### Scenario: 名称`
- 使用SHALL/MUST表示规范要求

#### 第二阶段：实施变更（Stage 2: Implementing Changes）
**工作流程**：
1. **实施前准备**：
   - 阅读 `proposal.md` 了解要构建的内容
   - 阅读 `design.md`（如果存在）查看技术决策
   - 阅读 `tasks.md` 获取实施检查清单
   - **重要：不要开始实施直到提案被批准**

2. **按顺序执行任务**：
   - 按照 `tasks.md` 顺序逐一完成
   - 将每个任务标记为 `- [x]`
   - 确保每个项目在 `tasks.md` 中都已完成

3. **更新检查清单**：
   - 所有工作完成后，将每个任务设置为 `- [x]`
   - 确保清单反映实际情况

**验证规则**：
- 不得跳过批准阶段
- 必须严格按照任务顺序执行
- 完成度必须真实反映在tasks.md中

#### 第三阶段：归档变更（Stage 3: Archiving Changes）
**工作流程**：
1. **部署后操作**：
   - 创建单独的PR进行归档
   - 移动 `changes/[name]/` → `changes/archive/YYYY-MM-DD-[name]/`
   - 如果能力变更，更新 `specs/`
   - 使用 `openspec archive <change-id> --skip-specs --yes` 进行纯工具变更

2. **验证归档**：
   - 运行 `openspec validate --strict` 确认归档变更通过检查

## 关键验证命令

### 基础命令
```bash
# 列出活跃变更
openspec list

# 列出规格
openspec list --specs

# 显示详情
openspec show [item]

# 验证变更或规格
openspec validate [item] --strict

# 归档变更
openspec archive <change-id> [--yes|-y]
```

### 调试命令
```bash
# 调试增量解析
openspec show [change] --json --deltas-only

# 严格模式验证
openspec validate [change] --strict
```

## 错误预防措施

### 常见错误及预防
1. **"Change must have at least one delta"**
   - 检查 `changes/[name]/specs/` 是否存在 .md 文件
   - 验证文件是否有操作前缀（## ADDED Requirements）

2. **"Requirement must have at least one scenario"**
   - 检查Scenario是否使用 `#### Scenario:` 格式（4个#）
   - 不要对场景标题使用项目符号或粗体

3. **静默场景解析失败**
   - 精确格式要求：`#### Scenario: Name`
   - 使用调试命令：`openspec show [change] --json --deltas-only`

### 验证清单
- [ ] 运行 `openspec validate <id> --strict`
- [ ] 确保Scenario格式正确（4个#）
- [ ] 确保每个Requirement至少有一个Scenario
- [ ] 检查目录结构完整性
- [ ] 确保change-id唯一性

## 集成点

### 与开发技能协同
- 为代码生成提供规范约束
- 确保所有开发工作遵循OpenSpec流程
- 为质量检查提供验证标准

### 与质量保障集成
- 将OpenSpec验证集成到CI/CD流水线
- 确保所有变更都经过完整的三阶段流程
- 维护specs/和changes/目录同步

## 最佳实践

### 简化优先
- 默认小于100行新代码
- 单文件实现直到证明不足
- 避免没有明确理由的框架
- 选择无聊、经过验证的模式

### 复杂性触发
只在以下情况下增加复杂性：
- 性能数据显示当前解决方案太慢
- 具体规模要求（>1000用户，>100MB数据）
- 多个经过验证的用例需要抽象

### 清晰引用
- 使用 `file.ts:42` 格式表示代码位置
- 引用规格为 `specs/auth/spec.md`
- 链接相关变更和PR

### 能力命名
- 使用动词-名词：`user-auth`, `payment-capture`
- 每个能力单一目的
- 10分钟可理解规则
- 如果描述需要"AND"则拆分

## 项目特定约束

### IOE-DREAM项目约定
- **语言**: Java 17 + Spring Boot 3.5.4
- **架构**: 严格四层架构（Controller → Service → Manager → DAO）
- **权限**: Sa-Token + RBAC模型
- **数据库**: MySQL 9.3.0 + MyBatis Plus
- **缓存**: Redis + Caffeine多级缓存
- **安全**: 等保三级要求

### 强制约束
- 包名必须使用 `jakarta.*`，禁止 `javax.*`
- 依赖注入必须使用 `@Resource`，禁止 `@Autowired`
- 所有接口必须使用 `@SaCheckPermission` 权限控制
- 必须继承 `BaseEntity`，使用软删除
- 统一使用 `ResponseDTO` 返回格式

## 实际应用示例

### 创建门禁管理功能示例
```bash
# 1. 创建变更结构
CHANGE=add-access-control-management
mkdir -p openspec/changes/$CHANGE/{specs/access}

# 2. 编写提案
cat > openspec/changes/$CHANGE/proposal.md << 'EOF'
# Change: 添加门禁管理功能

## Why
现有门禁系统缺少统一的管理界面，需要完善设备管理和权限控制功能

## What Changes
- 添加设备管理接口
- 添加权限配置功能
- 添加实时监控能力

## Impact
- 影响规格: access-control
- 影响代码: smart-access模块
EOF

# 3. 编写任务
cat > openspec/changes/$CHANGE/tasks.md << 'EOF'
## 1. 数据库设计
- [ ] 1.1 设计设备管理表结构
- [ ] 1.2 设计权限配置表结构

## 2. 后端实现
- [ ] 2.1 实现设备管理Controller
- [ ] 2.2 实现权限管理Service
- [ ] 2.3 实现Manager层业务逻辑

## 3. 前端实现
- [ ] 3.1 创建设备管理页面
- [ ] 3.2 创建权限配置界面

## 4. 测试验证
- [ ] 4.1 编写单元测试
- [ ] 4.2 集成测试验证
EOF

# 4. 编写规格增量
cat > openspec/changes/$CHANGE/specs/access/spec.md << 'EOF'
## ADDED Requirements
### Requirement: 设备管理
系统应当提供完整的门禁设备管理功能。

#### Scenario: 添加设备
- **WHEN** 管理员输入设备信息并提交
- **THEN** 系统保存设备信息并返回成功响应

#### Scenario: 设备状态查询
- **WHEN** 用户查询设备状态
- **THEN** 系统返回设备实时状态信息

### Requirement: 权限配置
系统应当支持灵活的门禁权限配置。

#### Scenario: 配置人员权限
- **WHEN** 管理员为用户配置门禁权限
- **THEN** 系统保存权限配置并生效
EOF

# 5. 验证
openspec validate $CHANGE --strict
```

## 质量保证

### 自动化检查
- 预提交Hook验证OpenSpec格式
- CI/CD流水线集成openspec validate
- 自动检查Scenario格式和完整性

### 人工审查
- 提案审查确保技术可行性
- 设计审查确保架构一致性
- 归档审查确保文档同步

---

*最后更新: 2025-11-16*
*维护者: IOE-DREAM开发团队*