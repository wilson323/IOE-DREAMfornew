# IOE-DREAM TODO实施开发规范与注意事项

> **文档版本**: v1.0.0
> **创建日期**: 2025-01-30
> **配套文档**:
> - GLOBAL_TODO_COMPREHENSIVE_ANALYSIS.md（P0级TODO详解）
> - GLOBAL_TODO_P1_P2_ANALYSIS.md（P1/P2级TODO详解）

---

## 📋 目录

1. [开发规范总则](#开发规范总则)
2. [代码编写规范](#代码编写规范)
3. [测试规范](#测试规范)
4. [文档规范](#文档规范)
5. [发布规范](#发布规范)
6. [常见问题FAQ](#常见问题faq)

---

## 🎯 开发规范总则

### 核心原则

1. **质量第一**: 代码质量优于开发速度
2. **规范优先**: 严格遵守项目编码规范
3. **测试驱动**: 关键功能必须有单元测试
4. **文档同步**: 代码与文档保持同步更新
5. **安全第一**: 所有功能必须考虑安全性

### 开发流程

```
需求分析 → 技术设计 → 编码实现 → 单元测试 → 代码审查 → 集成测试 → 发布部署
   ↓         ↓         ↓         ↓         ↓         ↓         ↓
 理解业务  架构设计  遵循规范  测试覆盖 同行评审  集成验证  灰度发布
```

---

## 💻 代码编写规范

### 1. 命名规范

#### 1.1 类命名

```java
// ✅ 正确的类命名
public class UserServiceImpl implements UserService { }
public class UserAddForm { }
public class UserVO { }
public class UserController { }
public class UserManager { }
public class UserDao { }

// ❌ 错误的类命名
public class user_service_impl { }  // 应使用帕斯卡命名
public class UserServiceClass { }  // 不必要的后缀
public class US { }                 // 缩写过于简短
```

#### 1.2 方法命名

```java
// ✅ 正确的方法命名
public Long addUser(UserAddForm form) { }
public void updateUser(Long userId, UserUpdateForm form) { }
public void deleteUser(Long userId) { }
public UserVO getUserById(Long userId) { }
public List<UserVO> listUsers() { }
public PageResult<UserVO> pageUsers(PageParam form) { }

// ❌ 错误的方法命名
public Long add() { }                    // 方法名不明确
public UserVO get(long id) { }            // 参数名不清晰
public void delete() { }                  // 缺少参数
public List<UserVO> getUserList() { }     // 应该用list前缀
```

#### 1.3 变量命名

```java
// ✅ 正确的变量命名
private Long userId;
private String userName;
private LocalDateTime createTime;
private List<UserVO> userList;
private Map<String, Object> dataMap;

// ❌ 错误的变量命名
private Long uid;                    // 缩写不明确
private String name;                 // 太泛化
private LocalDateTime time;          // 不明确是什么时间
private List<UserVO> list;           // 类型重复
private Map map;                     // 缺少泛型
```

### 2. 注解使用规范

#### 2.1 依赖注入规范

```java
// ✅ 正确：使用@Resource
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Resource
    private UserManager userManager;
}

// ❌ 错误：使用@Autowired
@Service
public class UserServiceImpl implements UserService {

    @Autowired  // 禁止使用
    private UserDao userDao;
}
```

#### 2.2 日志注解规范

```java
// ✅ 正确：使用@Slf4j
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public void createUser(UserAddForm form) {
        log.info("[用户服务] 创建用户: userName={}", form.getUserName());
    }
}

// ❌ 错误：使用LoggerFactory
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);  // 禁止
}
```

#### 2.3 事务注解规范

```java
// ✅ 正确：明确rollbackFor
@Service
public class UserServiceImpl implements UserService {

    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateForm form) {
        // 业务逻辑
    }

    @Transactional(rollbackFor = {BusinessException.class, SystemException.class})
    public void batchUpdateUsers(List<UserUpdateForm> forms) {
        // 业务逻辑
    }
}

// ⚠️ 可接受但不推荐：不指定rollbackFor
@Transactional
public void deleteUser(Long userId) {
    // 业务逻辑（只抛RuntimeException）
}

// ❌ 错误：rollbackFor指定错误
@Transactional(rollbackFor = IOException.class)  // 不必要的检查异常
public void updateUser(UserUpdateForm form) {
    // 业务逻辑
}
```

### 3. 日志记录规范

#### 3.1 日志级别使用

```java
// ✅ 正确的日志级别使用
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    // DEBUG: 详细调试信息
    private void debugInfo(String userName) {
        log.debug("[用户服务] 调试信息: userName={}", userName);
    }

    // INFO: 业务关键节点
    public void createUser(UserAddForm form) {
        log.info("[用户服务] 创建用户: userName={}, email={}",
                 form.getUserName(), form.getEmail());
    }

    // WARN: 警告信息
    public UserVO getUserById(Long userId) {
        UserVO user = userDao.selectById(userId);
        if (user == null) {
            log.warn("[用户服务] 用户不存在: userId={}", userId);
        }
        return user;
    }

    // ERROR: 系统错误
    public void updateUser(UserUpdateForm form) {
        try {
            // 业务逻辑
        } catch (Exception e) {
            log.error("[用户服务] 更新用户异常: userId={}, error={}",
                     form.getUserId(), e.getMessage(), e);
            throw new SystemException("UPDATE_FAILED", "更新失败", e);
        }
    }
}
```

#### 3.2 日志格式规范

```java
// ✅ 正确的日志格式
log.info("[用户服务] 创建用户: userName={}, email={}", userName, email);
log.warn("[用户服务] 用户不存在: userId={}", userId);
log.error("[用户服务] 更新用户异常: userId={}, error={}", userId, e.getMessage(), e);

// ❌ 错误的日志格式
log.info("创建用户: " + userName);  // 字符串拼接
log.info("创建用户: userName=" + userName + ", email=" + email);  // 拼接复杂
log.error("更新用户异常", e);  // 缺少关键信息
log.info("[用户服务] 密码: {}", password);  // 记录敏感信息
```

#### 3.3 敏感信息处理

```java
// ✅ 正确：脱敏处理
log.info("[用户服务] 用户登录: userId={}, phone={}",
         userId, maskPhone(user.getPhone()));

private String maskPhone(String phone) {
    if (phone == null || phone.length() < 11) {
        return "***";
    }
    return phone.substring(0, 3) + "****" + phone.substring(7);
}

// ✅ 正确：不记录敏感信息
log.info("[用户服务] 支付成功: userId={}, orderId={}, amount={}",
         userId, orderId, amount);
// 不记录：银行卡号、CVV、密码等

// ❌ 错误：记录敏感信息
log.info("[用户服务] 用户登录: username={}, password={}",
         username, password);  // 严禁记录密码
log.info("[支付服务] 银行卡支付: cardNo={}, cvv={}",
         cardNo, cvv);  // 严禁记录银行卡信息
```

### 4. 异常处理规范

#### 4.1 异常分类

```java
// ✅ 正确的异常使用

// 1. 业务异常（可预期）
public UserVO getUserById(Long userId) {
    UserEntity user = userDao.selectById(userId);
    if (user == null) {
        throw new BusinessException("USER_NOT_FOUND", "用户不存在");
    }
    return convertToVO(user);
}

// 2. 系统异常（不可预期）
public UserVO getUserById(Long userId) {
    try {
        UserEntity user = userDao.selectById(userId);
        return convertToVO(user);
    } catch (Exception e) {
        log.error("[用户服务] 查询用户异常: userId={}, error={}",
                 userId, e.getMessage(), e);
        throw new SystemException("QUERY_USER_FAILED", "查询用户失败", e);
    }
}

// 3. 参数验证异常（使用@Valid自动触发）
@PostMapping("/user")
public ResponseDTO<Long> addUser(@Valid @RequestBody UserAddForm form) {
    // Spring会自动验证参数，失败时抛出MethodArgumentNotValidException
    Long userId = userService.addUser(form);
    return ResponseDTO.ok(userId);
}
```

#### 4.2 全局异常处理

```java
// ✅ 全局异常处理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    // 参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[参数验证异常] message={}", message);
        return ResponseDTO.error("VALIDATION_ERROR", message);
    }

    // 系统异常
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("[系统异常] error={}", e.getMessage(), e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

### 5. Service接口返回类型规范

#### 5.1 标准返回类型

```java
// ✅ 正确的Service接口返回类型
public interface UserService {

    // 分页查询：返回PageResult<T>
    PageResult<UserVO> queryPage(UserQueryForm form);

    // 单个查询：返回T
    UserVO getUserById(Long userId);

    // 列表查询：返回List<T>
    List<UserVO> listUsers(UserQueryForm form);

    // 新增操作：返回Long（新增ID）
    Long addUser(UserAddForm form);

    // 更新操作：返回void
    void updateUser(Long userId, UserUpdateForm form);

    // 删除操作：返回void
    void deleteUser(Long userId);

    // 状态操作：返回Boolean
    Boolean existsUser(Long userId);

    // 复杂数据：返回Map（仅限报表类）
    Map<String, Object> statisticsReport(LocalDateTime startTime, LocalDateTime endTime);
}

// ❌ 错误的Service接口返回类型
public interface UserService {

    // Service层不应该返回ResponseDTO
    ResponseDTO<UserVO> getUserById(Long userId);  // 错误！

    // Service层不应该返回ResponseDTO包装的PageResult
    ResponseDTO<PageResult<UserVO>> queryPage(UserQueryForm form);  // 错误！
}
```

#### 5.2 Controller层包装

```java
// ✅ 正确的Controller层包装
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUserById(@PathVariable Long id) {
        UserVO result = userService.getUserById(id);
        return ResponseDTO.ok(result);  // Controller包装
    }

    @GetMapping("/page")
    public ResponseDTO<PageResult<UserVO>> queryPage(UserQueryForm form) {
        PageResult<UserVO> result = userService.queryPage(form);
        return ResponseDTO.ok(result);  // Controller包装
    }
}
```

---

## 🧪 测试规范

### 1. 单元测试规范

#### 1.1 测试类命名

```java
// ✅ 正确的测试类命名
class UserServiceTest { }
class UserManagerTest { }
class UserControllerTest { }

// ❌ 错误的测试类命名
class TestUserService { }  // Test后缀
class UserServiceTests { }  // 不必要的复数
class UserTesting { }       // 不规范的命名
```

#### 1.2 测试方法命名

```java
// ✅ 正确的测试方法命名
@Test
void test_getUserById_userExists_returnUserVO() {
    // Given
    Long userId = 1L;

    // When
    UserVO result = userService.getUserById(userId);

    // Then
    assertNotNull(result);
    assertEquals(userId, result.getUserId());
}

@Test
void test_getUserById_userNotExists_returnNull() {
    // Given
    Long userId = 999L;

    // When
    UserVO result = userService.getUserById(userId);

    // Then
    assertNull(result);
}

// ❌ 错误的测试方法命名
@Test
void test1() { }  // 不明确的测试内容
@Test
void getUser() { }  // 缺少场景和预期
@Test
void testGetUser() { }  // 不清楚测试什么场景
```

#### 1.3 测试覆盖率要求

| 模块类型 | 最低覆盖率 | 目标覆盖率 |
|---------|-----------|-----------|
| Service层 | 80% | 90% |
| Manager层 | 75% | 85% |
| DAO层 | 70% | 80% |
| Controller层 | 60% | 75% |
| 工具类 | 90% | 95% |

### 2. 集成测试规范

```java
// ✅ 正确的集成测试
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Test
    void test_getUserById_success() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(userId));
    }
}
```

---

## 📚 文档规范

### 1. 代码注释规范

#### 1.1 类注释

```java
/**
 * 用户服务实现类
 *
 * <p>负责用户的CRUD操作、用户状态管理、用户权限验证等</p>
 *
 * @author IOE-DREAM Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    // ...
}
```

#### 1.2 方法注释

```java
/**
 * 根据用户ID查询用户信息
 *
 * @param userId 用户ID
 * @return 用户信息VO，如果用户不存在返回null
 * @throws SystemException 查询异常时抛出
 */
public UserVO getUserById(Long userId) {
    // ...
}
```

#### 1.3 字段注释

```java
/**
 * 用户ID
 */
@TableId(type = IdType.AUTO)
private Long userId;

/**
 * 用户名
 */
@NotBlank(message = "用户名不能为空")
@Size(max = 50, message = "用户名长度不能超过50")
private String userName;
```

### 2. 业务文档规范

每个TODO实现后需要更新以下文档：

1. **需求文档**: 更新业务需求说明
2. **设计文档**: 更新架构设计
3. **API文档**: 更新接口文档
4. **测试文档**: 更新测试用例
5. **部署文档**: 更新部署配置

---

## 🚀 发布规范

### 1. 代码审查清单

发布前必须完成以下检查：

- [ ] 代码符合开发规范
- [ ] 单元测试覆盖率达标
- [ ] 日志记录完整清晰
- [ ] 异常处理合理完善
- [ ] 敏感信息已脱敏
- [ ] 性能测试通过
- [ ] 安全测试通过
- [ ] 文档已更新

### 2. 发布流程

```
1. 提交代码（含单元测试）
   ↓
2. 运行单元测试
   ↓
3. 代码审查
   ↓
4. 合并到开发分支
   ↓
5. 集成测试
   ↓
6. 部署到测试环境
   ↓
7. 功能验证
   ↓
8. 部署到生产环境（灰度）
   ↓
9. 监控观察
   ↓
10. 全量发布
```

---

## ❓ 常见问题FAQ

### Q1: TODO实现需要多长时间？

A: 根据TODO复杂度不同，预计时间如下：
- **P0级（简单）**: 2-3天
- **P0级（复杂）**: 5-7天
- **P1级（简单）**: 1-2天
- **P1级（复杂）**: 3-5天
- **P2级**: 2-3天

### Q2: TODO实现的优先级如何确定？

A: 按以下原则确定优先级：
1. **影响核心业务流程** → P0
2. **影响用户体验** → P1
3. **性能和代码质量优化** → P2

### Q3: 如何保证代码质量？

A: 采取以下措施：
1. 严格遵守开发规范
2. 编写完整的单元测试
3. 进行代码审查
4. 使用静态代码分析工具
5. 持续集成验证

### Q4: TODO实现后如何验证？

A: 按以下步骤验证：
1. 单元测试全部通过
2. 集成测试通过
3. 功能测试验证
4. 性能测试验证
5. 安全测试验证
6. 用户验收测试

### Q5: 遇到技术难题如何处理？

A: 按以下步骤处理：
1. 先进行技术调研
2. 与团队成员讨论
3. 咨询架构师
4. 编写技术方案文档
5. 评审后实施

---

## 📞 支持与反馈

### 技术支持
- **架构委员会**: 负责技术决策和架构评审
- **技术专家**: 各领域技术专家提供咨询
- **开发团队**: 日常开发问题讨论

### 文档维护
- **文档更新**: 随TODO实现进度持续更新
- **问题反馈**: 向架构委员会反馈文档问题
- **改进建议**: 欢迎提出改进建议

---

**文档维护**: 本文档应随项目发展持续更新
**最后更新**: 2025-01-30
**维护人**: IOE-DREAM架构委员会
**版本**: v1.0.0
