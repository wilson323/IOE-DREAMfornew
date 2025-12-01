# 企业级代码质量标准

## 概述

基于IOE-DREAM项目系统性编译错误修复的成功实践，建立企业级代码质量标准体系，确保代码质量、一致性和可维护性。

## 实体类方法标准

### 1.1 标准方法命名规范

#### 1.1.1 基础Getter/Setter方法
```java
// 标准字段访问方法
public String getName() { return name; }
public void setName(String name) { this.name = name; }
```

#### 1.1.2 业务描述方法
```java
// 业务描述方法模式
public String getDescription() {
    return String.format("%s-%s", this.getClass().getSimpleName(), this.getId());
}

public String getText() {
    return this.name != null ? this.name : "";
}

public Integer getWeight() {
    return 1; // 默认权重
}
```

#### 1.1.3 验证方法
```java
// 验证方法模式
public boolean isValid() {
    return this.getId() != null && this.getId() > 0;
}

public boolean isValidName() {
    return this.name != null && !this.name.trim().isEmpty();
}
```

### 1.2 实体类结构标准

#### 1.2.1 继承规范
```java
@Entity
@Table(name = "t_business_entity")
public class BusinessEntity extends BaseEntity {
    // 业务字段
    private String name;
    private String description;

    // 标准业务方法
    public String getDescription() { /* 实现 */ }
    public String getText() { /* 实现 */ }
    public Integer getWeight() { /* 实现 */ }
    public boolean isValid() { /* 实现 */ }
}
```

## 服务类方法标准

### 2.1 标准CRUD操作

#### 2.1.1 基础CRUD方法签名
```java
public interface BaseService<Entity, VO, ID> {

    // 基础查询操作
    ResponseDTO<VO> getById(ID id);
    ResponseDTO<PageResult<VO>> getPage(PageParam pageParam);

    // 基础修改操作
    ResponseDTO<Boolean> delete(ID id);
    ResponseDTO<Boolean> batchDelete(List<ID> ids);

    // 业务扩展方法
    ResponseDTO<VO> getDetail(ID id);
    ResponseDTO<Boolean> updateStatus(ID id, String status);
}
```

#### 2.1.2 事务管理标准
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class BusinessServiceImpl implements BusinessService {

    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<VO> getById(ID id) {
        // 查询操作
    }

    @Override
    public ResponseDTO<Boolean> update(VO vo) {
        // 更新操作，自动事务管理
    }
}
```

### 2.2 异常处理标准

#### 2.2.1 统一异常处理
```java
try {
    // 业务逻辑
    return ResponseDTO.ok(result);
} catch (SmartBusinessException e) {
    log.error("业务异常: {}", e.getMessage());
    return ResponseDTO.userErrorParam(e.getMessage());
} catch (Exception e) {
    log.error("系统异常", e);
    return ResponseDTO.error("系统异常，请稍后重试");
}
```

## 控制器方法标准

### 3.1 RESTful API标准

#### 3.1.1 标准接口设计
```java
@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @GetMapping("/{id}")
    @SaCheckPermission("business:detail")
    public ResponseDTO<VO> getDetail(@PathVariable Long id) {
        return businessService.getDetail(id);
    }

    @PostMapping
    @SaCheckPermission("business:add")
    public ResponseDTO<Long> add(@Valid @RequestBody AddForm form) {
        return businessService.add(form);
    }
}
```

### 3.2 参数验证标准

#### 3.2.1 参数验证注解
```java
public class BusinessForm {

    @NotBlank(message = "名称不能为空")
    @Length(max = 50, message = "名称长度不能超过50个字符")
    private String name;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
```

## 缓存使用标准

### 4.1 缓存键命名规范

#### 4.1.1 键命名模式
```
{module}:{entity}:{id}:{type}
示例:
cache:user:123:info
cache:attendance:2024-01-01:statistics
```

### 4.2 缓存操作标准

#### 4.2.1 统一缓存服务使用
```java
@Service
public class BusinessService {

    @Resource
    private UnifiedCacheService<String, Object> cacheService;

    public VO getDetail(Long id) {
        String cacheKey = String.format("business:detail:%d", id);

        // 尝试从缓存获取
        VO cached = cacheService.getCached(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        VO vo = databaseQuery(id);

        // 设置缓存，过期30分钟
        cacheService.cache(cacheKey, vo, Duration.ofMinutes(30));

        return vo;
    }
}
```

## 代码质量检查清单

### 5.1 编码规范检查

#### 5.1.1 强制检查项
- [ ] 使用@Resource注解，禁止使用@Autowired
- [ ] 使用jakarta.*包，禁止使用javax.*
- [ ] 使用@Slf4j注解，禁止使用System.out
- [ ] 实体类必须继承BaseEntity
- [ ] 服务类必须使用@Transactional注解

#### 5.1.2 代码结构检查
- [ ] 严格遵循四层架构：Controller → Service → Manager → DAO
- [ ] 禁止跨层访问（Controller直接访问DAO）
- [ ] 统一使用ResponseDTO返回格式
- [ ] 统一异常处理机制

### 5.2 安全规范检查

#### 5.2.1 权限控制
- [ ] 所有API接口必须添加@SaCheckPermission注解
- [ ] 敏感操作必须添加@SaCheckLogin注解
- [ ] 参数必须使用@Valid注解验证

#### 5.2.2 数据安全
- [ ] 敏感信息禁止硬编码
- [ ] 数据库密码使用配置文件管理
- [ ] SQL注入防护

## CI/CD集成标准

### 6.1 质量门禁配置

#### 6.1.1 编译检查
```bash
# 编译错误检查（必须通过）
mvn clean compile

# 包名合规检查（javax使用必须为0）
find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l

# 依赖注入检查（@Autowired使用必须为0）
find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l
```

#### 6.1.2 代码质量检查
```bash
# 代码格式检查
./scripts/check-code-format.sh

# 代码规范检查
./scripts/check-code-standards.sh

# 测试覆盖率检查
mvn test jacoco:report
```

## 质量指标要求

### 7.1 代码质量指标

| 指标项目 | 目标值 | 检查方式 |
|---------|--------|---------|
| 编译错误数量 | ≤ 5个 | Maven编译 |
| 单元测试覆盖率 | ≥ 80% | JaCoCo报告 |
| 代码复用率 | ≥ 70% | SonarQube |
| 代码规范符合度 | ≥ 90% | Checkstyle |

### 7.2 开发效率指标

| 指标项目 | 目标值 | 测量方式 |
|---------|--------|---------|
| 新功能开发时间 | 减少30% | 开工时长统计 |
| Bug修复时间 | 减少50% | 缺陷管理统计 |
| 代码审查时间 | 减少40% | Pull Request统计 |

## 实施指南

### 8.1 开发流程

#### 8.1.1 开发前检查
1. 阅读相关代码质量标准
2. 选择合适的开发模板
3. 确认技术栈规范要求

#### 8.1.2 开发中检查
1. 实时使用IDE代码检查
2. 定期运行质量检查脚本
3. 遵循架构设计规范

#### 8.1.3 开发后验证
1. 运行完整质量门禁检查
2. 执行单元测试和集成测试
3. 代码审查和文档更新

### 8.2 模板使用指南

#### 8.2.1 实体类模板使用
```bash
# 复制模板
cp templates/entity/business-entity.java src/main/java/com/example/entity/NewEntity.java

# 替换占位符
# 1. BusinessEntity → NewEntity
# 2. business_entity → new_entity
# 3. 更新字段定义和业务方法
```

#### 8.2.2 服务类模板使用
```bash
# 复制模板
cp templates/service/business-service.java src/main/java/com/example/service/NewService.java

# 替换占位符并实现业务逻辑
```

## 附录

### A. 参考文档

- IOE-DREAM开发规范：`docs/DEV_STANDARDS.md`
- repowiki规范体系：`docs/repowiki/zh/content/`
- Spring Boot最佳实践
- Java编码规范

### B. 工具和脚本

- 代码质量检查脚本：`scripts/quality-check.sh`
- 模板生成工具：`tools/template-generator.sh`
- CI/CD配置模板：`.github/workflows/quality-check.yml`

---

**文档版本**: 1.0.0
**创建时间**: 2025-11-23
**更新时间**: 2025-11-23
**维护团队**: SmartAdmin Team