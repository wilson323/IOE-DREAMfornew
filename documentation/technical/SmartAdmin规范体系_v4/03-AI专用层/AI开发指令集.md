# AI开发指令集（权威文档）

> **📋 文档版本**: v4.0.0 (整合版)
> **📋 文档职责**: 为AI开发助手（如Claude Code、GitHub Copilot等）提供的专用指令集，确保AI生成的代码100%符合SmartAdmin规范。

## 🎯 指令集概述

### AI开发核心原则
```markdown
🎯 目标：生成100%符合SmartAdmin规范的代码
📏 标准：严格按照核心规范层文档执行
🔍 检查：生成后必须自检规范遵循度
🔄 迭代：不达标时自动修正代码
🏗️ 架构：严格遵循四层架构设计
```

### 指令优先级体系
```markdown
🔴 级别1：强制执行（违规立即阻断）
   - 架构约束、命名规范、安全要求
   - 违规时拒绝生成代码

🟡 级别2：推荐执行（违规需要解释）
   - 性能优化、代码质量、最佳实践
   - 违规时警告但允许生成

🟢 级别3：可选执行（违规允许但提示）
   - 代码风格、注释规范、文档建议
   - 违规时仅提示建议
```

## 🔴 级别1：强制执行指令

### 架构约束指令
```yaml
architecture_constraints:
  ARCH_001:
    description: "严格遵循四层架构"
    rule: "Controller → Service → Manager → Repository"
    violation: "BLOCKER"
    check: "检查类依赖关系和import语句"

  ARCH_002:
    description: "禁止跨层直接访问"
    rule: "Controller不能直接访问Repository/Dao"
    violation: "BLOCKER"
    check: "分析import语句，检查跨层调用"

  ARCH_003:
    description: "依赖注入规范"
    rule: "必须使用@Resource，禁止@Autowired"
    violation: "BLOCKER"
    check: "检查所有依赖注入注解"

  ARCH_004:
    description: "事务边界规范"
    rule: "事务只能在Service层使用"
    violation: "BLOCKER"
    check: "检查@Transactional注解位置"

  ARCH_005:
    description: "分层职责规范"
    rule: "每层只能处理自己职责范围内的逻辑"
    violation: "BLOCKER"
    check: "检查每层代码内容是否符合职责"
```

### 命名规范指令
```yaml
naming_constraints:
  NAME_001:
    description: "Controller命名规范"
    rule: "Controller类必须命名为{Module}Controller"
    violation: "BLOCKER"
    pattern: "^[A-Z][a-zA-Z0-9]*Controller$"

  NAME_002:
    description: "Service命名规范"
    rule: "Service类必须命名为{Module}Service"
    violation: "BLOCKER"
    pattern: "^[A-Z][a-zA-Z0-9]*Service$"

  NAME_003:
    description: "Manager命名规范"
    rule: "Manager类必须命名为{Module}Manager"
    violation: "BLOCKER"
    pattern: "^[A-Z][a-zA-Z0-9]*Manager$"

  NAME_004:
    description: "Dao命名规范"
    rule: "Dao类必须命名为{Module}Dao"
    violation: "BLOCKER"
    pattern: "^[A-Z][a-zA-Z0-9]*Dao$"

  NAME_005:
    description: "主键命名规范"
    rule: "主键字段必须命名为{entity}Id"
    violation: "BLOCKER"
    pattern: "^[a-z]+Id$"

  NAME_006:
    description: "表名规范"
    rule: "表名必须为t_{business}_{entity}"
    violation: "BLOCKER"
    pattern: "^t_[a-z]+_[a-z]+$"

  NAME_007:
    description: "Form命名规范"
    rule: "Form类必须命名为{Module}{Action}Form"
    violation: "BLOCKER"
    pattern: "^[A-Z][a-zA-Z0-9]*Form$"

  NAME_008:
    description: "VO命名规范"
    rule: "VO类必须命名为{Module}VO"
    violation: "BLOCKER"
    pattern: "^[A-Z][a-zA-Z0-9]*VO$"
```

### API设计指令
```yaml
api_constraints:
  API_001:
    description: "API路径规范"
    rule: "API路径格式为/api/{module}/{action}"
    violation: "BLOCKER"
    pattern: "^/api/[a-z]+/[a-z]+$"

  API_002:
    description: "HTTP方法规范"
    rule: "查询用GET，添加用POST，更新用POST，删除用POST"
    violation: "BLOCKER"
    mapping:
      query: "GET"
      add: "POST"
      update: "POST"
      delete: "POST"

  API_003:
    description: "响应格式规范"
    rule: "必须返回ResponseDTO格式"
    violation: "BLOCKER"
    format: "ResponseDTO<T>"

  API_004:
    description: "权限控制规范"
    rule: "必须使用@SaCheckPermission注解"
    violation: "BLOCKER"
    annotation: "@SaCheckPermission"
```

### 安全规范指令
```yaml
security_constraints:
  SEC_001:
    description: "输入验证规范"
    rule: "必须使用@Valid和@NotBlank等验证注解"
    violation: "BLOCKER"
    annotations: ["@Valid", "@NotBlank", "@NotNull", "@Length"]

  SEC_002:
    description: "密码加密规范"
    rule: "密码必须使用BCrypt加密"
    violation: "BLOCKER"
    encryption: "BCrypt"

  SEC_003:
    description: "敏感信息脱敏规范"
    rule: "日志中不能输出敏感信息"
    violation: "BLOCKER"
    sensitive_fields: ["password", "token", "secret"]

  SEC_004:
    description: "SQL注入防护规范"
    rule: "必须使用预编译SQL或MyBatis参数绑定"
    violation: "BLOCKER"
    method: "Prepared Statement or MyBatis"
```

## 🟡 级别2：推荐执行指令

### 性能优化指令
```yaml
performance_optimization:
  PERF_001:
    description: "分页查询优化"
    rule: "使用游标分页替代OFFSET"
    violation: "WARNING"
    recommendation: "使用id < lastId LIMIT size"

  PERF_002:
    description: "缓存使用规范"
    rule: "热点数据必须使用缓存"
    violation: "WARNING"
    cache_layers: ["Caffeine", "Redis"]

  PERF_003:
    description: "批量操作优化"
    rule: "批量操作每批不超过1000条"
    violation: "WARNING"
    batch_size: 1000

  PERF_004:
    description: "索引使用规范"
    rule: "查询条件必须使用索引"
    violation: "WARNING"
    check: "EXPLAIN查询计划"
```

### 代码质量指令
```yaml
code_quality:
  QUAL_001:
    description: "方法长度限制"
    rule: "单个方法不超过50行"
    violation: "WARNING"
    max_lines: 50

  QUAL_002:
    description: "圈复杂度限制"
    rule: "圈复杂度不超过10"
    violation: "WARNING"
    max_complexity: 10

  QUAL_003:
    description: "参数个数限制"
    rule: "方法参数不超过5个"
    violation: "WARNING"
    max_params: 5

  QUAL_004:
    description: "异常处理规范"
    rule: "必须处理所有受检异常"
    violation: "WARNING"
    requirement: "Complete exception handling"
```

## 🟢 级别3：可选执行指令

### 代码风格指令
```yaml
code_style:
  STYLE_001:
    description: "注释规范"
    rule: "公共方法必须有JavaDoc注释"
    violation: "SUGGESTION"
    requirement: "JavaDoc for public methods"

  STYLE_002:
    description: "日志规范"
    rule: "关键操作必须有日志记录"
    violation: "SUGGESTION"
    logger: "Slf4j with @Slf4j"

  STYLE_003:
    description: "代码格式规范"
    rule: "遵循统一的代码格式"
    violation: "SUGGESTION"
    formatter: "Prettier/EditorConfig"
```

## 🔧 代码生成模板

### Controller生成模板
```java
@RestController
@RequestMapping("/api/{module}")
@Tag(name = "{模块名称}管理", description = "{模块名称}的增删改查操作")
@SaCheckLogin
@Slf4j
public class {Module}Controller {

    @Resource
    private {Module}Service {module}Service;

    @Operation(summary = "新增{实体}")
    @PostMapping("/add")
    @SaCheckPermission("{module}:add")
    public ResponseDTO<String> add(@RequestBody @Valid {Module}AddForm addForm) {
        log.info("新增{实体}, param: {}", addForm);
        return ResponseDTO.ok({module}Service.add(addForm));
    }

    @Operation(summary = "更新{实体}")
    @PostMapping("/update")
    @SaCheckPermission("{module}:update")
    public ResponseDTO<String> update(@RequestBody @Valid {Module}UpdateForm updateForm) {
        log.info("更新{实体}, param: {}", updateForm);
        return ResponseDTO.ok({module}Service.update(updateForm));
    }

    @Operation(summary = "删除{实体}")
    @PostMapping("/delete")
    @SaCheckPermission("{module}:delete")
    public ResponseDTO<String> delete(@RequestBody @Valid IdForm idForm) {
        log.info("删除{实体}, id: {}", idForm.getId());
        return ResponseDTO.ok({module}Service.delete(idForm.getId()));
    }

    @Operation(summary = "分页查询{实体}")
    @PostMapping("/page")
    @SaCheckPermission("{module}:query")
    public ResponseDTO<PageResult<{Module}VO>> page(@RequestBody @Valid {Module}QueryForm queryForm) {
        log.info("分页查询{实体}, param: {}", queryForm);
        return ResponseDTO.ok({module}Service.page(queryForm));
    }
}
```

### Service生成模板
```java
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class {Module}ServiceImpl implements {Module}Service {

    @Resource
    private {Module}Dao {module}Dao;

    @Resource
    private {Module}Manager {module}Manager;

    @Override
    public String add({Module}AddForm addForm) {
        // 1. 参数验证
        this.validateAddForm(addForm);

        // 2. 构建实体
        {Module}Entity entity = SmartBeanUtil.copy(addForm, {Module}Entity.class);

        // 3. 保存数据
        {module}Dao.insert(entity);

        // 4. 清除缓存
        {module}Manager.removeCache();

        // 5. 记录日志
        log.info("新增{实体}成功, id: {}", entity.get{Module}Id());

        return ResponseStringConst.SUCCESS;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<{Module}VO> page({Module}QueryForm queryForm) {
        // 1. 查询数据
        Page<{Module}Entity> page = {module}Dao.queryPage(queryForm);

        // 2. 转换VO
        List<{Module}VO> voList = SmartBeanUtil.copyList(page.getRecords(), {Module}VO.class);

        // 3. 返回结果
        return new PageResult<>(voList, page.getTotal());
    }
}
```

## 📋 AI自检清单

### 生成后自检流程
```markdown
1. 架构检查：
   □ 是否遵循四层架构？
   □ 是否使用@Resource注入？
   □ 是否跨层访问？
   □ 事务边界是否正确？

2. 命名检查：
   □ 类名是否符合规范？
   □ 方法名是否符合规范？
   □ 字段名是否符合规范？
   □ 表名是否符合规范？

3. API检查：
   □ 路径是否符合规范？
   □ HTTP方法是否正确？
   ▅ 响应格式是否正确？
   □ 权限控制是否配置？

4. 安全检查：
   □ 参数验证是否完整？
   □ 敏感信息是否脱敏？
   □ SQL注入是否防护？
   □ 日志是否安全？

5. 质量检查：
   □ 方法长度是否合理？
   □ 圈复杂度是否达标？
   □ 异常处理是否完善？
   □ 日志记录是否完整？
```

### 修正策略
```markdown
🔴 强制违规：
   - 立即停止生成
   - 分析违规原因
   - 重新生成代码

🟡 推荐违规：
   - 生成代码但标记警告
   - 提供修正建议
   - 允许手动调整

🟢 可选违规：
   - 正常生成代码
   - 提供优化建议
   - 不影响功能
```

---

**🎯 使用指南**：
1. **严格遵循** - 严格按照指令集执行
2. **主动检查** - 生成后主动执行自检清单
3. **持续学习** - 根据反馈不断优化指令
4. **保持更新** - 同步更新规范变化

**📖 相关文档**：
- [架构规范](../01-核心规范层/架构规范.md) - 架构设计标准
- [编码规范](../01-核心规范层/编码规范.md) - 编码标准规范
- [API规范](../01-核心规范层/API规范.md) - API设计规范
- [安全规范](../01-核心规范层/安全规范.md) - 安全相关规范
- [AI约束检查清单](./AI约束检查清单.md) - 详细检查清单