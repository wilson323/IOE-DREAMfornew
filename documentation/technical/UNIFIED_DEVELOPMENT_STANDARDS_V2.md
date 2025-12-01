# 📚 统一开发规范 V2.0

> 基于IOE-DREAM项目深度反思制定的最新开发规范
>
> **版本**: 2.0
> **更新日期**: 2025-11-20
> **适用范围**: 全项目
> **状态**: 正式生效

## 🎯 规范体系概览

### 📋 规范层次结构
```
UNIFIED_DEVELOPMENT_STANDARDS_V2.md (本文档)
├── 1. 编码基础规范
│   ├── 1.1 类型安全规范
│   ├── 1.2 命名规范
│   ├── 1.3 注释规范
│   └── 1.4 编码格式规范
├── 2. 架构设计规范
│   ├── 2.1 四层架构规范
│   ├── 2.2 API设计规范
│   ├── 2.3 数据库设计规范
│   └── 2.4 依赖管理规范
├── 3. 代码质量规范
│   ├── 3.1 代码审查规范
│   ├── 3.2 测试规范
│   ├── 3.3 异常处理规范
│   └── 3.4 性能优化规范
├── 4. 第三方集成规范
│   ├── 4.1 SDK集成规范
│   ├── 4.2 版本管理规范
│   ├── 4.3 安全集成规范
│   └── 4.4 适配器模式规范
└── 5. 重构与维护规范
    ├── 5.1 重构时机规范
    ├── 5.2 技术债务管理
    ├── 5.3 代码演进规范
    └── 5.4 文档同步规范
```

---

## 1. 编码基础规范

### 1.1 🔒 类型安全规范（新增核心规范）

#### 1.1.1 ID字段类型标准
```java
// ✅ 统一标准
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 统一使用Long类型

    @Column(name = "department_id")
    private Long departmentId;  // 外键统一Long类型
}

// ✅ API接口标准
@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public ResponseDTO<UserDTO> getUser(@PathVariable String id) {
        // 前端传递String，后端自动转换
        Long userId = TypeConverter.convertToLong(id);
        UserDTO user = userService.getUser(userId);
        return ResponseDTO.ok(user);
    }
}
```

#### 1.1.2 类型转换规范
```java
// ❌ 禁止的直接转换
entity.setStatus(status.toString());  // 可能抛空指针
entity.setUserId(Integer.parseInt(userIdStr));  // 可能抛NumberFormatException

// ✅ 强制使用的安全转换
entity.setStatus(TypeConverter.convertToString(status));
entity.setUserId(TypeConverter.convertToLong(userIdStr));
```

#### 1.1.3 枚举使用规范
```java
// ✅ 枚举定义标准
public enum UserStatus {
    ACTIVE(1, "激活", "用户处于激活状态"),
    INACTIVE(0, "未激活", "用户尚未激活"),
    DELETED(-1, "已删除", "用户已被删除");

    private final Integer code;
    private final String desc;
    private final String remark;

    UserStatus(Integer code, String desc, String remark) {
        this.code = code;
        this.desc = desc;
        this.remark = remark;
    }

    // 标准getter方法
    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
    public String getRemark() { return remark; }

    // 业务方法
    public boolean isActive() {
        return this == ACTIVE;
    }

    // 静态工厂方法
    public static UserStatus fromCode(Integer code) {
        for (UserStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
```

### 1.2 📝 命名规范（强化版）

#### 1.2.1 包名规范
```java
// ✅ 统一包名结构
net.lab1024.sa.{module}.{layer}.{subpackage}

// 示例
net.lab1024.sa.admin.module.consume.service.impl.ConsumeServiceImpl
net.lab1024.sa.base.common.domain.ResponseDTO
net.lab1024.sa.base.util.TypeConverter
```

#### 1.2.2 类名规范（强化）
```java
// ✅ Controller层
@RestController
public class UserController { }
public class ConsumeController { }

// ✅ Service层
@Service
public class UserService { }
public class ConsumeService { }

// ✅ Manager层
@Component
public class UserManager { }
public class ConsumeManager { }

// ✅ DAO层
@Repository
public interface UserDao { }
public interface ConsumeDao { }
```

#### 1.2.3 方法名规范（强化）
```java
// ✅ 查询方法
public User getUser(Long id) { }
public List<User> queryUsers(UserQuery query) { }
public User findUserByEmail(String email) { }

// ✅ 修改方法
public void updateUser(User user) { }
public void modifyPassword(Long userId, String password) { }
public void resetUserStatus(Long userId) { }

// ✅ 删除方法（软删除）
public void deleteUser(Long userId) { }
public void removeUser(Long userId) { }

// ✅ 布尔方法
public boolean isUserActive(Long userId) { }
public boolean hasPermission(Long userId, String permission) { }
```

### 1.3 💬 注释规范（强化版）

#### 1.3.1 类注释模板
```java
/**
 * 用户服务
 *
 * <p>提供用户管理相关功能，包括：</p>
 * <ul>
 *   <li>用户查询和搜索</li>
 *   <li>用户信息更新</li>
 *   <li>用户状态管理</li>
 * </ul>
 *
 * <p><strong>业务规则：</strong></p>
 * <ul>
 *   <li>用户ID必须唯一</li>
   <li>邮箱必须格式正确</li>
   <li>用户删除后不能恢复</li>
 * </ul>
 *
 * <p><strong>示例：</strong></p>
 * <pre>{@code
 * UserDTO user = userService.getUser(123L);
 * List<UserDTO> users = userService.queryActiveUsers();
 * }</code></pre>
 *
 * @author SmartAdmin Team
 * @since 2.0.0
 * @version 1.0.0
 * @see com.example.security.PermissionService
 */
@Slf4j
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /**
     * 根据用户ID获取用户信息
     *
     * <p>查询用户基本信息，包括状态和权限信息。</p>
     *
     * @param userId 用户ID，不能为null
     * @return 用户信息，用户不存在时返回null
     * @throws IllegalArgumentException 当userId为null或小于等于0时
     * @since 1.0.0
     */
    public UserDTO getUser(Long userId) {
        // 实现逻辑
    }
}
```

#### 1.3.2 方法注释模板
```java
/**
 * 更新用户信息
 *
 * <p>更新用户的基本信息，包括姓名、邮箱、电话等。</p>
 * <p>更新后会自动记录更新时间和操作人。</p>
 *
 * @param userId 用户ID，不能为null
 * @param updateUserRequest 更新请求对象，包含需要更新的字段
 * @return 更新后的用户信息
 * @throws BusinessException 业务异常，当：
 * <ul>
 *   <li>用户不存在</li>
   * <li>邮箱已被其他用户使用</li>
 *   <li>状态不合法</li>
 * </ul>
 * @throws IllegalArgumentException 参数异常，当userId为null或无效时
 */
@Transactional(rollbackFor = Exception.class)
public UserDTO updateUser(Long userId, UpdateUserRequest updateUserRequest) {
    // 实现逻辑
}
```

### 1.4 🎨 编码格式规范

#### 1.4.1 代码格式标准
```java
// ✅ 正确的代码格式
public class ExampleService {

    @Resource
    private ExampleDao exampleDao;

    /**
     * 方法注释
     */
    public Result processExample(Request request) {
        try {
            // 1. 参数验证
            validateRequest(request);

            // 2. 业务处理
            ExampleEntity entity = exampleDao.selectById(request.getId());
            if (entity == null) {
                throw new BusinessException("示例不存在");
            }

            // 3. 更新实体
            entity.setStatus(request.getStatus());
            entity.setUpdateTime(LocalDateTime.now());

            // 4. 保存结果
            exampleDao.update(entity);

            return Result.success();

        } catch (BusinessException e) {
            log.error("业务异常: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("系统异常: {}", e.getMessage(), e);
            return Result.error("系统异常，请稍后重试");
        }
    }
}
```

---

## 2. 架构设计规范

### 2.1 🏗️ 四层架构规范（强化版）

#### 2.1.1 层次职责定义
```java
// ✅ Controller层 - 接收HTTP请求，参数校验
@RestController
@RequestMapping("/api/v1/consume")
public class ConsumeController {

    @Resource
    private ConsumeService consumeService;

    @PostMapping("/order")
    @SaCheckPermission("consume:order:create")
    public ResponseDTO<ConsumeResult> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        // 只负责：参数验证、调用Service、返回结果
        ConsumeResult result = consumeService.createOrder(request);
        return ResponseDTO.ok(result);
    }
}

// ✅ Service层 - 业务逻辑处理，事务管理
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeService {

    @Resource
    private ConsumeManager consumeManager;

    @Resource
    private ConsumeCacheManager cacheManager;

    public ConsumeResult createOrder(CreateOrderRequest request) {
        // 只负责：业务逻辑、事务管理、协调Manager
        validateBusinessRules(request);

        ConsumeEntity entity = consumeManager.prepareOrder(request);
        ConsumeResult result = consumeManager.executeOrder(entity);

        cacheManager.invalidateOrderCache(entity.getUserId());
        return result;
    }
}

// ✅ Manager层 - 复杂业务逻辑封装，跨模块协调
@Component
public class ConsumeManager {

    @Resource
    private ConsumeDao consumeDao;
    @Resource
    private AccountManager accountManager;
    @Resource
    private PaymentManager paymentManager;

    public ConsumeEntity prepareOrder(CreateOrderRequest request) {
        // 复杂业务逻辑：多步骤处理
        ConsumeEntity entity = buildOrderEntity(request);
        validateOrderBalance(entity);
        reserveInventory(entity);
        return entity;
    }
}

// ✅ DAO层 - 数据访问，使用MyBatis Plus
@Repository
public interface ConsumeDao extends BaseMapper<ConsumeEntity> {
    // 只负责：数据库操作，复杂查询
    List<ConsumeEntity> selectByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end);
}
```

#### 2.1.2 层间调用规范（强化）
```java
// ✅ 正确的跨层调用
@Controller
public class Controller {
    @Resource
    private Service service; // 只调用Service层

    @Resource
    private Dao dao;      // ❌ 禁止：Controller直接调用DAO
}

// ✅ 正确的事务边界
@Service
@Transactional(rollbackFor = Exception.class)
public class Service {
    @Resource
    private Manager manager; // 调用Manager层

    public void complexOperation() {
        manager.step1();  // 事务内
        manager.step2();  // 事务内
        // 事务结束
    }
}

// ✅ 正确的缓存边界
@Service
public class Service {
    @Resource
    private CacheManager cacheManager;

    public List<Entity> getEntities() {
        List<Entity> entities = dao.selectAll();
        return cacheManager.enrichEntities(entities);
    }
}
```

### 2.2 🔌 API设计规范（强化版）

#### 2.2.1 RESTful API规范
```java
// ✅ 资源命名规范
@RestController
@RequestMapping("/api/v1/users/{userId}/orders")  // 资源路径
public class UserOrderController {

    // ✅ HTTP方法规范
    @GetMapping
    public ResponseDTO<List<OrderDTO>> getUserOrders(
            @PathVariable Long userId) {
        // 查询操作
    }

    @PostMapping
    public ResponseDTO<OrderDTO> createOrder(
            @PathVariable Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        // 创建操作
    }

    @PutMapping("/{orderId}")
    public ResponseDTO<OrderDTO> updateOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderRequest request) {
        // 更新操作
    }

    @DeleteMapping("/{orderId}")
    public ResponseDTO<Void> deleteOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        // 删除操作（软删除）
    }
}
```

#### 2.2.2 参数类型规范（新增）
```java
// ✅ 统一参数类型
public class CreateOrderRequest {
    @NotBlank(message = "订单名称不能为空")
    private String orderName;

    @NotNull(message = "用户ID不能为空")
    private Long userId;  // 统一Long类型

    @Min(value = 1, message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "订单类型不能为空")
    private OrderType orderType;  // 枚举类型

    // 避免使用基本类型作为参数
    // private int status;  // ❌ 错误
    // private Integer status;  // ✅ 正确
}
```

#### 2.2.3 响应格式规范
```java
// ✅ 统一响应格式
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    /**
     * 成功响应
     */
    public static <T> ResponseDTO<T> ok(T data) {
        return ResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .message("操作成功")
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ResponseDTO<T> error(String message) {
        return ResponseDTO.<T>builder()
                .success(false)
                .data(null)
                .message(message)
                .build();
    }

    /**
     * 分页响应
     */
    public static <T> ResponseDTO<PageResult<T>> page(PageResult<T> page) {
        return ResponseDTO.<PageResult<T>>builder()
                .success(true)
                .data(page)
                .message("查询成功")
                .build();
    }
}
```

### 2.3 🗄️ 数据库设计规范（强化版）

#### 2.3.1 表设计标准
```sql
-- ✅ 表命名规范
CREATE TABLE `t_consume_record` (
    -- ✅ 主键标准
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',

    -- ✅ 业务字段
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `consume_amount` BIGINT NOT NULL COMMENT '消费金额(分)',
    `consume_status` VARCHAR(20) NOT NULL COMMENT '消费状态',

    -- ✅ 审计字段（必须继承BaseEntity）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建用户ID',
    `update_user_id` BIGINT COMMENT '更新用户ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
    `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本号',

    -- ✅ 索引
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_status_deleted` (`consume_status`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';
```

#### 2.3.2 字段类型标准（新增）
```java
// ✅ ID字段标准
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

// ✅ 外键字段标准
@Column(name = "user_id")
private Long userId;

// ✅ 金额字段标准（单位：分）
@Column(name = "amount")
private Long amount;  // 使用Long避免精度问题

// ✅ 状态字段标准（枚举）
@Enumerated(EnumType.STRING)
@Column(name = "status")
private OrderStatus status;

// ✅ 标记字段标准
@Column(name = "deleted_flag")
private Boolean deletedFlag;

// ✅ 版本字段标准（乐观锁）
@Version
private Long version;
```

### 2.4 📦 依赖管理规范（新增）

#### 2.4.1 Maven依赖管理
```xml
<!-- 父pom - 统一依赖版本管理 -->
<properties>
    <!-- 框架版本 -->
    <spring-boot.version>3.2.0</spring-boot.version>
    <spring-cloud.version>2023.0.1.0</spring-cloud.version>

    <!-- 第三方版本 -->
    <wechatpay.version>0.4.9</wechatpay.version>
    <alipay.version>4.38.157.ALL</alipay.version>
</properties>

<!-- 依赖版本锁定 -->
<dependencyManagement>
    <dependencies>
        <!-- Spring Boot统一管理 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <!-- 第三方SDK统一版本 -->
        <dependency>
            <groupId>com.github.wechatpay-apiv3</groupId>
            <artifactId>wechatpay-java</artifactId>
            <version>${wechatpay.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 2.4.2 子模块依赖规范
```xml
<!-- 子模块只定义业务依赖，不指定版本 -->
<dependencies>
    <!-- ✅ 正确：只定义groupId和artifactId -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- ❌ 错误：子模块定义版本 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>2.7.0</version>  <!-- 版本冲突 -->
    </dependency>
</dependencies>
```

---

## 3. 代码质量规范

### 3.1 🔍 代码审查规范（强化版）

#### 3.1.1 强制审查项
- **类型安全检查**：所有类型转换必须使用TypeConverter
- **空值检查**：所有可能为null的参数必须检查
- **异常处理**：业务异常必须用BusinessException
- **事务边界**：事务必须在Service层开始和结束

#### 3.1.2 禁止模式（新增）
```java
// ❌ 禁止：System.out输出
System.out.println("debug info");

// ✅ 推荐：使用日志框架
log.info("debug info: {}", data);

// ❌ 禁止：直接捕获Exception
try {
    // 业务逻辑
} catch (Exception e) {
    // 捕获所有异常
}

// ✅ 推荐：捕获具体异常
try {
    // 业务逻辑
} catch (BusinessException e) {
    // 业务异常处理
} catch (Exception e) {
    // 系统异常处理
    log.error("系统异常", e);
    throw new BusinessException("系统异常", e);
}
```

### 3.2 🧪 测试规范（强化版）

#### 3.2.1 测试覆盖率要求
```java
// ✅ 单元测试覆盖率目标：≥ 80%
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("根据ID查询用户 - 成功场景")
    void testGetUserById_Success() {
        // Given
        UserEntity entity = createUserEntity();
        when(userDao.selectById(1L)).thenReturn(entity);

        // When
        UserDTO result = userService.getUser(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        verify(userDao).selectById(1L);
    }
}
```

### 3.3 ⚠️ 异常处理规范（强化版）

#### 3.3.1 异常分类
```java
// ✅ 业务异常 - 用户感知的异常
public class BusinessException extends RuntimeException {
    private final String code;
    private final Object data;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
        this.data = null;
    }

    public BusinessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }
}

// ✅ 系统异常 - 不直接暴露给用户
public class SystemException extends RuntimeException {
    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = "SYSTEM_ERROR";
        this.errorCode = "E500";
    }
}
```

---

## 4. 第三方集成规范

### 4.1 🔌 SDK集成规范（新增）

#### 4.1.1 第三方SDK集成流程
```java
// ✅ 统一集成流程
@Component
public class ThirdPartyIntegrationService {

    /**
     * 集成第三方SDK
     *
     * @param provider SDK提供者
     * @param version SDK版本
     * @return 集成结果
     */
    public IntegrationResult integrateSDK(String provider, String version) {
        try {
            // 1. 版本兼容性检查
            checkVersionCompatibility(provider, version);

            // 2. 依赖冲突检测
            DependencyConflictReport conflictReport = checkDependencyConflicts();
            if (conflictReport.hasConflicts()) {
                return IntegrationResult.failure("依赖冲突", conflictReport);
            }

            // 3. SDK适配器实现
            SDKAdapter adapter = createAdapter(provider);

            // 4. 功能测试验证
            testSDKFunctionality(adapter);

            return IntegrationResult.success("集成成功");

        } catch (Exception e) {
            log.error("SDK集成失败: provider={}, version={}", provider, version, e);
            return IntegrationResult.failure("集成异常: " + e.getMessage());
        }
    }
}
```

#### 4.1.2 适配器模式标准
```java
// ✅ 统一适配器接口
public interface PaymentProvider {

    /**
     * 创建支付订单
     */
    PaymentResult createOrder(PaymentRequest request);

    /**
     * 查询支付状态
     */
    PaymentStatus queryStatus(String orderId);

    /**
     * 处理退款
     */
    RefundResult processRefund(RefundRequest request);
}

// ✅ 微信支付适配器
@Component("wechat")
public class WechatPaymentProvider implements PaymentProvider {

    @Resource
    private WechatConfig config;

    @Override
    public PaymentResult createOrder(PaymentRequest request) {
        // 实现微信支付逻辑
    }
}
```

### 4.2 📋 版本管理规范（新增）

#### 4.2.1 版本升级流程
```bash
#!/bin/bash
# SDK版本升级脚本

echo "开始SDK版本升级流程..."

# 1. 备份当前版本
echo "步骤1: 备份当前版本"
git checkout -b backup/$(date +%Y%m%d-%H%M%S)

# 2. 检查版本兼容性
echo "步骤2: 检查版本兼容性"
mvn versions:display-dependency-updates | grep "${SDK_NAME}"

# 3. 更新版本号
echo "步骤3: 更新版本号"
mvn versions:use-releases -Dincludes="${SDK_NAME}"

# 4. 编译测试
echo "步骤4: 编译测试"
mvn clean compile test

# 5. 功能验证
echo "步骤5: 功能验证"
mvn integration-test

echo "SDK版本升级完成"
```

---

## 5. 重构与维护规范

### 5.1 🔄 重构时机规范（新增）

#### 5.1.1 重构触发条件
```java
// ✅ 主动重构时机
- 新功能开发前
- 性能优化需求
- 代码审查发现问题
- 技术债务积累

// ❌ 避免重构时机
- 紧急修复期间
- 生产发布前24小时内
- 团队紧张时期
```

#### 5.1.2 重构风险评估
```java
// ✅ 风险评估矩阵
public enum RefactoringRiskLevel {
    LOW(1, "低风险", "简单提取方法，修改注释"),
    MEDIUM(2, "中等风险", "重构业务逻辑，修改接口"),
    HIGH(3, "高风险", "重构核心架构，修改数据库结构"),
    CRITICAL(4, "极高风险", "重构基础框架，修改核心API");

    private final int level;
    private final String description;
    private final String example;

    // 风险缓解措施
    public String[] getMitigationMeasures() {
        switch (this) {
            case LOW:
                return new String[]{"编写单元测试", "代码审查", "小步提交"};
            case MEDIUM:
                return new String[]{"编写完整测试", "分阶段重构", "回滚机制"};
            case HIGH:
                return new String[]{"制定详细计划", "分模块重构", "灰度发布"};
            case CRITICAL:
                return new String[]{"架构评审", "原型验证", "分阶段发布"};
            default:
                return new String[]{"评估替代方案"};
        }
    }
}
```

---

## 🔧 技能应用指南

### 使用技能解决编译错误

#### 1. 类型转换错误
```java
// 应用技能：TypeSafetySpecialist
Skill("type-safety-specialist");

// 自动修复类型转换问题
// 会自动将 toString() 转换为 TypeConverter 调用
```

#### 2. 依赖冲突问题
```java
// 应用技能：DependencyManagementSpecialist
Skill("dependency-management-specialist");

// 自动分析和解决依赖冲突
// 提供版本升级和冲突排除建议
```

#### 3. 重构实施
```java
// 应用技能：RefactoringStrategist
Skill("refactoring-strategist");

// 提供重构计划和最佳实践指导
// 包含风险控制和验证步骤
```

---

## 📊 规范执行保障

### 自动化检查工具
```bash
# 编译规范检查脚本
#!/bin/bash

echo "执行开发规范检查..."

# 1. 类型安全检查
echo "检查类型转换规范..."
mvn compile 2>&1 | grep "toString()" | wc -l
echo "类型转换违规数量: $(($?))"

# 2. 依赖冲突检查
echo "检查依赖冲突..."
mvn dependency:tree -Dverbose | grep "conflict" | wc -l

# 3. 编码规范检查
echo "检查编码规范..."
checkstyle checkstyle.xml

# 4. 测试覆盖率检查
echo "检查测试覆盖率..."
mvn jacoco:report
```

### 持续集成检查
```yaml
# .github/workflows/standards-check.yml
name: Development Standards Check

on: [push, pull_request]

jobs:
  standards-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Compile Check
        run: mvn clean compile

      - name: Type Safety Check
        run: |
          mvn compile 2>&1 | grep "toString()" | wc -l
          echo "Type conversion violations: $(($?))"

      - name: Dependency Check
        run: mvn dependency:tree -Dverbose

      - name: Test Coverage Check
        run: mvn test jacoco:report
```

---

## 📞 支持与反馈

### 规范支持团队
- **技术咨询**: standards-support@lab1024.com
- **问题报告**: standards-issues@lab1024.com
- **最佳实践**: standards-bestpractices@lab1024.com

### 反馈改进机制
- **季度规范评估**: 每季度评估规范执行效果
- **年度规范更新**: 每年更新规范版本
- **团队培训**: 定期进行规范培训

---

## 📝 版本历史

| 版本 | 日期 | 更新内容 | 更新人 |
|------|------|----------|--------|
| 2.0 | 2025-11-20 | 深度反思重构，新增类型安全、依赖管理、第三方集成规范 | SmartAdmin Team |
| 1.0 | 2025-11-01 | 基础开发规范，包含编码规范、架构规范、API规范 | SmartAdmin Team |

---

**🎯 规范生效**: 本规范自发布之日起正式生效，所有新代码必须遵循本规范。现有代码应在3个月内完成重构改造。