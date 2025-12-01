# Java编码规范

> **版本**: v1.0
> **更新时间**: 2025-11-13
> **分类**: 开发规范体系 > 核心规范
> **标签**: ["编码规范", "Java", "代码质量", "命名规范", "异常处理"]
> **作者**: SmartAdmin规范治理委员会
> **描述**: IOE-DREAM智慧园区一卡通管理平台的Java编码标准规范，整合了SmartAdmin各版本的精华内容

## 📋 文档概述

本文档是IOE-DREAM智慧园区一卡通管理平台项目的唯一Java编码规范权威来源，整合了SmartAdmin v1基础规范、v2质量标准和v3编码精华。为所有Java开发团队提供统一、权威、专业的编码指导。

## ⚠️ 核心约束（不可违反）

### 🚫 绝对禁止
```markdown
❌ 禁止使用 @Autowired 注入，必须使用 @Resource
❌ 禁止使用魔法数字，必须定义为常量
❌ 禁止在Controller中编写业务逻辑
❌ 禁止跨层直接访问（如Controller直接访问Dao）
❌ 禁止使用System.out.println()，必须使用日志框架
❌ 禁止硬编码字符串，必须定义为常量
❌ 禁止忽略异常处理
❌ 禁止使用 @NotEmpty、@Size 等非常用验证注解
❌ 禁止代码复杂度超过10
❌ 禁止重复代码率超过3%
```

### ✅ 必须执行
```markdown
✅ 必须使用 @Resource 注入依赖
✅ 必须为每个公共方法编写注释
✅ 必须处理所有异常情况
✅ 必须使用日志框架记录重要操作
✅ 必须遵循统一的命名规范
✅ 必须保持代码复杂度 ≤ 10
✅ 必须为公共方法编写单元测试
✅ 必须使用 @NotBlank + @NotNull 进行必填字段验证
✅ 必须遵循四层架构规范
✅ 必须代码覆盖率 ≥ 80%
```

## 🏗️ 命名规范（唯一权威）

### 类命名规范
```markdown
✅ Controller: {Module}Controller（UserController）
✅ Service: {Module}Service（UserService）
✅ Manager: {Module}Manager（UserManager）
✅ Dao: {Module}Dao（UserDao）
✅ Entity: {Module}Entity（UserEntity）
✅ Form: {Module}{Action}Form（UserAddForm）
✅ VO: {Module}VO 或 {Module}SpecialVO（UserVO、UserTreeVO）
✅ 常量类: {Module}Const（UserConst）
✅ 枚举类: {Module}TypeEnum（UserTypeEnum）
❌ 禁止：CategoryForm, CategoryRequest, CategoryDTO
```

### 方法命名规范
```markdown
✅ 添加操作: add, insert, create
✅ 删除操作: delete, remove
✅ 更新操作: update, modify, edit
✅ 查询操作: query, get, find, select
✅ 分页查询: page, queryPage
✅ 统计操作: count, statistics
✅ 验证操作: validate, check
✅ 转换操作: convert, transform, to
```

### 变量命名规范
```markdown
✅ 普通变量: 小驼峰命名（userName, categoryId）
✅ 常量变量: 全大写下划线分隔（MAX_SIZE, DEFAULT_VALUE）
✅ 静态变量: 小驼峰命名（instance, factory）
✅ 临时变量: 有意义的名称（tempUser, result）
✅ 集合变量: 复数形式或List后缀（users, userList）
✅ 布尔变量: is/has/can前缀（isActive, hasPermission）
✅ 主键字段: {entity}Id（userId, orderId）
✅ 标记字段: {business}Flag（disabledFlag, deletedFlag）
```

## 📝 代码结构规范

### Controller结构模板
```java
@RestController
@RequestMapping("/api/{module}")
@Tag(name = "{模块名称}管理", description = "{模块名称}的增删改查操作")
@SaCheckLogin
public class {Module}Controller {

    @Resource
    private {Module}Service {module}Service;

    @Operation(summary = "新增{实体}")
    @PostMapping("/add")
    @SaCheckPermission("{module}:add")
    public ResponseDTO<String> add(@RequestBody @Valid {Module}AddForm addForm) {
        return ResponseDTO.ok({module}Service.add(addForm));
    }

    @Operation(summary = "更新{实体}")
    @PostMapping("/update")
    @SaCheckPermission("{module}:update")
    public ResponseDTO<String> update(@RequestBody @Valid {Module}UpdateForm updateForm) {
        return ResponseDTO.ok({module}Service.update(updateForm));
    }

    @Operation(summary = "删除{实体}")
    @PostMapping("/delete")
    @SaCheckPermission("{module}:delete")
    public ResponseDTO<String> delete(@RequestBody @Valid IdForm idForm) {
        return ResponseDTO.ok({module}Service.delete(idForm.getId()));
    }

    @Operation(summary = "分页查询{实体}")
    @PostMapping("/page")
    @SaCheckPermission("{module}:query")
    public ResponseDTO<PageResult<{Module}VO>> page(@RequestBody @Valid {Module}QueryForm queryForm) {
        return ResponseDTO.ok({module}Service.page(queryForm));
    }
}
```

### Service结构模板
```java
@Service
@Transactional(rollbackFor = Exception.class)
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

        // 5. 返回结果
        return ResponseStringConst.SUCCESS;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<{Module}VO> page({Module}QueryForm queryForm) {
        // 查询数据
        Page<{Module}Entity> page = {module}Dao.queryPage(queryForm);

        // 转换VO
        List<{Module}VO> voList = SmartBeanUtil.copyList(page.getRecords(), {Module}VO.class);

        return new PageResult<>(voList, page.getTotal());
    }
}
```

## 🔍 代码质量规范

### 复杂度控制
```markdown
✅ 圈复杂度 ≤ 10
✅ 嵌套层级 ≤ 4层
✅ 方法行数 ≤ 50行
✅ 类行数 ≤ 500行
✅ 参数个数 ≤ 5个
✅ 返回值类型明确
❌ 禁止深度嵌套的if-else
❌ 禁止过长的参数列表
❌ 禁止过大的类和方法
```

### 异常处理规范
```java
// 自定义业务异常
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    private final String code;
    private final Object[] args;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.args = args;
    }
}

// 全局异常处理
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统异常，请联系管理员");
    }
}
```

## 🔧 Spring Boot 3.x 特殊规范

### Jakarta EE 包名规范
```markdown
✅ 必须使用 jakarta.annotation.Resource
✅ 必须使用 jakarta.validation.Valid
✅ 必须使用 jakarta.persistence.Entity
✅ 必须使用 jakarta.servlet.http.HttpServletRequest
✅ 必须使用 jakarta.transaction.Transactional
❌ 禁止使用 javax.* 包（会导致编译错误）
```

### 依赖注入规范
```java
@Service
public class UserService {

    @Resource  // ✅ 正确：使用 @Resource
    private UserDao userDao;

    @Resource  // ✅ 正确：使用 @Resource
    private UserManager userManager;

    // ❌ 禁止：@Autowired
    // @Autowired
    // private UserDao userDao;
}
```

### 实体类规范
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_info")
public class UserEntity extends BaseEntity {  // ✅ 必须继承 BaseEntity

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @TableField("user_name")
    @NotBlank(message = "用户名不能为空")
    private String userName;

    // 其他字段...
}
```

## 📊 代码度量标准

### 质量指标
```markdown
✅ 代码覆盖率 ≥ 80%
✅ 核心业务覆盖率 = 100%
✅ 重复代码率 ≤ 3%
✅ 圈复杂度 ≤ 10
✅ 代码行数/方法 ≤ 50
✅ 类行数 ≤ 500
✅ 文档覆盖率 ≥ 90%
✅ 安全漏洞 = 0个高危
```

### 代码审查清单
```markdown
□ 是否遵循命名规范？
□ 是否符合架构分层？
□ 异常处理是否完善？
□ 日志记录是否规范？
□ 代码复杂度是否达标？
□ 单元测试是否覆盖？
□ 性能是否满足要求？
□ 安全是否存在漏洞？
□ 文档是否完整？
□ 是否符合团队约定？
```

### 日志记录规范
```java
@Slf4j
@Service
public class UserService {

    public String addUser(UserAddForm addForm) {
        log.info("开始添加用户, userName: {}", addForm.getUserName());  // ✅ 关键操作记录

        try {
            // 业务逻辑处理
            String result = this.processAddUser(addForm);

            log.info("添加用户成功, userId: {}, userName: {}", result, addForm.getUserName());  // ✅ 成功记录
            return result;

        } catch (BusinessException e) {
            log.warn("添加用户失败, userName: {}, error: {}", addForm.getUserName(), e.getMessage());  // ✅ 业务异常
            throw e;

        } catch (Exception e) {
            log.error("添加用户系统异常, userName: {}", addForm.getUserName(), e);  // ✅ 系统异常
            throw new BusinessException("SYSTEM_ERROR", "添加用户失败");
        }
    }
}
```

## 🔒 安全编码规范

### SQL注入防护
```java
// ✅ 正确：使用参数化查询
@Select("SELECT * FROM t_user_info WHERE user_name = #{userName} AND deleted_flag = 0")
List<UserEntity> selectByUserName(@Param("userName") String userName);

// ❌ 禁止：字符串拼接SQL
// @Select("SELECT * FROM t_user_info WHERE user_name = '" + userName + "' AND deleted_flag = 0")
```

### XSS防护
```java
@RestController
public class UserController {

    @PostMapping("/add")
    public ResponseDTO<String> add(@RequestBody @Valid UserAddForm addForm) {
        // ✅ 对用户输入进行HTML转义
        String safeUserName = HtmlUtils.htmlEscape(addForm.getUserName());
        addForm.setUserName(safeUserName);

        return ResponseDTO.ok(userService.add(addForm));
    }
}
```

## 🧪 单元测试规范

### 测试命名规范
```java
@SpringBootTest
@Transactional
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void testAddUser_Success() {  // ✅ 方法名格式：test{MethodName}_{Scenario}
        // Given - 准备测试数据
        UserAddForm addForm = new UserAddForm();
        addForm.setUserName("testUser");
        addForm.setRealName("测试用户");

        // When - 执行测试
        String result = userService.add(addForm);

        // Then - 验证结果
        assertThat(result).isEqualTo(ResponseStringConst.SUCCESS);
    }

    @Test
    void testAddUser_DuplicateUserName_ThrowBusinessException() {
        // Given
        UserAddForm addForm = new UserAddForm();
        addForm.setUserName("existingUser");

        // When & Then
        assertThatThrownBy(() -> userService.add(addForm))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
    }
}
```

## 📋 代码提交规范

### Git提交信息格式
```markdown
格式：<type>(<scope>): <subject>

type 类型：
✅ feat: 新功能
✅ fix: 修复bug
✅ docs: 文档更新
✅ style: 代码格式调整
✅ refactor: 重构代码
✅ test: 测试相关
✅ chore: 构建/工具相关

scope 范围：
✅ user: 用户模块
✅ card: 一卡通模块
✅ access: 门禁模块
✅ attendance: 考勤模块
✅ video: 视频模块

示例：
feat(user): 新增用户管理功能
fix(card): 修复卡片余额计算错误
docs(api): 更新API文档
```

### 代码检查命令
```bash
# 编译检查
mvn clean compile -DskipTests

# 检查 javax 包使用（应该为 0）
find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l

# 检查 @Autowired 使用（应该为 0）
find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l

# 运行测试
mvn test

# 代码质量检查
mvn sonar:sonar
```

## 🔗 相关文档

### 权威规范文档
- [架构设计规范](./架构设计规范.md) - 架构设计标准
- [RESTfulAPI设计规范](./RESTfulAPI设计规范.md) - API接口设计规范
- [系统安全规范](./系统安全规范.md) - 安全相关规范
- [数据库设计规范](./数据库设计规范.md) - 数据存储和处理规范

### 实施指南文档
- [开发环境配置](../实施指南/开发环境配置.md) - 环境搭建和配置
- [代码模板库](../实施指南/代码模板库/) - 标准代码模板
- [单元测试指南](../实施指南/单元测试指南.md) - 测试规范和流程

### AI开发支持
- [AI开发指令集](../AI开发支持/AI开发指令集.md) - AI辅助开发指导
- [AI约束检查清单](../AI开发支持/AI约束检查清单.md) - AI代码审查标准

---

## 🎯 核心原则

1. **可读性第一** - 代码是写给人读的
2. **一致性优先** - 遵循统一的编码风格
3. **质量保证** - 通过测试和审查保证质量
4. **性能考虑** - 编写高效的代码
5. **安全第一** - 避免安全漏洞

## 📋 版本信息

- 本文档整合自SmartAdmin v1、v2、v3、v4版本精华内容
- 整合负责人：SmartAdmin规范治理委员会
- 整合日期：2025-11-13
- 下次评审：2026-02-13

---

**🎯 IOE-DREAM Java编码规范 - 统一、权威、专业的企业级编码标准**