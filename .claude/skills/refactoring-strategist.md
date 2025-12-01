# 🔄 重构策略专家技能

**技能名称**: 重构策略专家
**技能等级**: 高级
**适用角色**: 软件架构师、技术负责人、资深开发工程师
**前置技能**: 设计模式、重构技巧、代码质量评估、测试驱动开发
**预计学时**: 18小时

---

## 📚 知识要求

### 理论知识
- **重构原则**: 重构时机、重构范围、重构风险控制
- **设计模式**: GoF设计模式、企业架构模式、反模式识别
- **代码质量度量**: 圈复杂度、耦合度、内聚度评估
- **重构技术**: 抽象提取、方法提取、类提取、接口提取

### 业务理解
- **业务架构分析**: 理解业务模块间的依赖关系
- **技术债务管理**: 识别和评估技术债务影响
- **演进式架构设计**: 支持系统渐进式改进

---

## 🛠️ 技能应用场景

### 场景1：类型转换代码重构
**问题代码**：
```java
// ❌ 随处可见的类型转换问题
public class UserService {
    public UserDTO getUser(String userIdStr) {
        Long userId = Long.parseLong(userIdStr); // 可能抛异常
        UserEntity entity = userDao.selectById(userId);
        return convert(entity); // 类型不安全
    }

    private UserDTO convert(UserEntity entity) {
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId().toString()); // Long -> String
        dto.setStatus(entity.getStatus().toString()); // Enum -> String
        return dto;
    }
}
```

**重构方案**：
```java
// ✅ 统一类型转换层
@Service
public class UserTypeConversionService {

    /**
     * 安全的用户ID转换
     */
    public Long convertUserId(String userIdStr) {
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("用户ID格式错误");
        }
    }

    /**
     * 标准化的实体转换
     */
    public UserDTO convertToDTO(UserEntity entity) {
        if (entity == null) return null;

        return UserDTO.builder()
            .id(entity.getId().toString()) // 统一String类型
            .name(entity.getName())
            .status(entity.getStatus().getCode()) // 使用枚举代码
            .statusDesc(entity.getStatus().getDesc()) // 同时提供描述
            .build();
    }
}

// 重构后的Service
@Service
public class UserService {

    @Resource
    private UserTypeConversionService conversionService;

    public UserDTO getUser(String userIdStr) {
        Long userId = conversionService.convertUserId(userIdStr);
        UserEntity entity = userDao.selectById(userId);
        return conversionService.convertToDTO(entity);
    }
}
```

### 场景2：第三方SDK集成重构
**问题代码**：
```java
// ❌ SDK类名冲突
public class PaymentService {

    public ResponseDTO createJsapiPayment(PaymentRequest request) {
        // 类名冲突：jsapi和nativepay都有PrepayRequest
        com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest req =
            new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
        // 大量重复代码
    }

    public ResponseDTO createNativePayment(PaymentRequest request) {
        com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest req =
            new com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest();
        // 重复的设置代码
    }
}
```

**重构方案**：
```java
// ✅ 抽象SDK操作
public interface PaymentAdapter {
    PrepayResponse createPrepay(PaymentRequest request);
    PaymentStatus queryStatus(String paymentId);
    RefundResult createRefund(RefundRequest request);
}

// 微信支付适配器
@Component("wechat")
public class WechatPaymentAdapter implements PaymentAdapter {

    @Resource
    private WechatConfig config;

    @Override
    public PrepayResponse createPrepay(PaymentRequest request) {
        switch (request.getPaymentType()) {
            case JSAPI:
                return createJsapiPrepay(request);
            case NATIVE:
                return createNativePrepay(request);
            default:
                throw new UnsupportedPaymentTypeException(
                    "不支持的支付类型: " + request.getPaymentType());
        }
    }

    private PrepayResponse createJsapiPrepay(PaymentRequest request) {
        JsapiService service = new JsapiService.Builder()
            .config(config.getConfig())
            .build();

        com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest req =
            buildRequest(request);

        com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse response =
            service.prepay(req);

        return convertResponse(response);
    }

    private PrepayRequest buildRequest(PaymentRequest request) {
        // 统一的请求构建逻辑
        PrepayRequest prepayRequest = new PrepayRequest();
        prepayRequest.setAppid(config.getAppId());
        prepayRequest.setMchid(config.getMchId());
        prepayRequest.setDescription(request.getDescription());
        // ...
        return prepayRequest;
    }
}

// 重构后的支付服务
@Service
public class PaymentService {

    @Resource
    private Map<String, PaymentAdapter> paymentAdapters;

    public ResponseDTO<PrepayResponse> createPayment(PaymentRequest request) {
        PaymentAdapter adapter = paymentAdapters.get(request.getPaymentProvider());
        if (adapter == null) {
            throw new UnsupportedPaymentProviderException(
                "不支持的支付渠道: " + request.getPaymentProvider());
        }

        PrepayResponse response = adapter.createPrepay(request);
        return ResponseDTO.ok(response);
    }
}
```

### 场景3：重构实体类设计
**问题代码**：
```java
// ❌ 实体类设计混乱
@Entity
public class User {
    private Integer id; // 应该用Long
    private String status; // 应该用枚举
    private Integer gender; // 应该用枚举

    // 缺少getter/setter方法
    // 没有审计字段
    // 不遵循BaseEntity继承
}
```

**重构方案**：
```java
// ✅ 标准化实体设计
@Entity
@Table(name = "t_user")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity extends BaseEntity {  // 继承审计字段

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    // 业务方法
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.updateTime = LocalDateTime.now();
    }
}

// 枚举定义
public enum UserStatus {
    ACTIVE(1, "激活"),
    INACTIVE(0, "未激活"),
    DELETED(-1, "已删除");

    private final Integer code;
    private final String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}
```

---

## 🔧 核心重构工具

### 1. 重构分析器
```java
@Component
public class RefactoringAnalyzer {

    /**
     * 分析代码质量指标
     */
    public CodeQualityReport analyzeQuality() {
        CodeQualityReport report = new CodeQualityReport();

        // 分析圈复杂度
        report.setCyclomaticComplexity(analyzeCyclomaticComplexity());

        // 分析耦合度
        report.setCoupling(analyzeCoupling());

        // 分析重复代码
        report.setDuplication(analyzeDuplication());

        return report;
    }

    /**
     * 识别重构机会
     */
    public List<RefactoringOpportunity> identifyOpportunities() {
        List<RefactoringOpportunity> opportunities = new ArrayList<>();

        // 长方法识别
        opportunities.addAll(findLongMethods());

        // 大类识别
        opportunities.addAll(findLargeClasses());

        // 重复代码识别
        opportunities.addAll(findDuplicateCode());

        // 复杂条件识别
        opportunities.addAll(findComplexConditions());

        return opportunities;
    }
}
```

### 2. 重构执行器
```java
@Component
public class RefactoringExecutor {

    /**
     * 执行类型转换重构
     */
    public void executeTypeConversionRefactoring() {
        TypeConversionRefactoring refactoring = new TypeConversionRefactoring();

        // 1. 分析当前代码
        List<TypeConversionIssue> issues = refactoring.analyze();

        // 2. 制定重构计划
        RefactoringPlan plan = refactoring.createPlan(issues);

        // 3. 执行重构
        refactoring.execute(plan);

        // 4. 验证重构结果
        refactoring.validate();
    }

    /**
     * 执行API重构
     */
    public void executeAPIRefactoring() {
        APIRefactoring refactoring = new APIRefactoring();

        refactoring.standardizeParameterTypes();
        refactoring.unifyResponseFormats();
        refactoring.addTypeConversionLayer();
        refactoring.updateAPIDocumentation();
    }
}
```

### 3. 重构验证器
```java
@Component
public class RefactoringValidator {

    /**
     * 验证重构结果
     */
    public ValidationResult validateRefactoring(RefactoringPlan plan) {
        ValidationResult result = new ValidationResult();

        // 编译检查
        result.setCompiles(isCodeCompiling());

        // 测试检查
        result.setTestsPassing(areTestsPassing());

        // 性能检查
        result.setPerformanceOK(isPerformanceAcceptable());

        // 功能检查
        result.setFunctionalityCorrect(isFunctionalityCorrect());

        return result;
    }
}
```

---

## ⚡ 快速重构指南

### 类型转换重构步骤

#### 步骤1：建立转换工具类
```java
// 创建统一的类型转换服务
@Component
public class TypeConversionService {

    public Long convertToLong(Object value) {
        return TypeConverter.convertToLong(value);
    }

    public String convertToString(Object value) {
        return TypeConverter.convertToString(value);
    }

    public Integer convertToInteger(Object value) {
        return TypeConverter.convertToInteger(value);
    }
}
```

#### 步骤2：重构Service层
```java
// 重构前
entity.setStatus(status.toString());

// 重构后
entity.setStatus(UserStatus.fromCode(status));
```

#### 步骤3：重构API层
```java
// 重构前
@GetMapping("/user/{id}")
public ResponseDTO<User> getUser(@PathVariable String id);

// 重构后
@GetMapping("/user/{id}")
public ResponseDTO<User> getUser(@PathVariable String id) {
    Long userId = typeConversionService.convertToLong(id);
    // 业务逻辑
}
```

### 第三方SDK重构步骤

#### 步骤1：抽象接口设计
```java
public interface PaymentProvider {
    PrepayResponse createOrder(PaymentRequest request);
    PaymentStatus queryStatus(String orderId);
    RefundResult processRefund(RefundRequest request);
}
```

#### 步骤2：适配器实现
```java
@Component("wechat")
public class WechatPaymentProvider implements PaymentProvider {
    // 实现微信支付逻辑
}
```

#### 步骤3：统一调用接口
```java
@Service
public class PaymentService {

    @Resource
    private Map<String, PaymentProvider> providers;

    public PrepayResponse createPayment(PaymentRequest request) {
        PaymentProvider provider = providers.get(request.getProvider());
        return provider.createOrder(request);
    }
}
```

---

## 🔍 重构审查清单

### 重构前检查

#### [ ] 代码质量评估
- 圈复杂度是否过高 (>15)
- 类的职责是否单一
- 是否有重复代码
- 是否有魔法数字

#### [ ] 测试覆盖率
- 单元测试覆盖率是否充足 (>80%)
- 集成测试是否完整
- 是否有回归测试

#### [ ] 性能影响评估
- 重构是否影响性能
- 是否需要性能测试
- 是否有性能回归风险

### 重构后验证

#### [ ] 功能正确性
- 所有原有功能是否正常
- 边界条件是否处理正确
- 异常情况是否正确处理

#### [ ] 代码质量
- 代码复杂度是否降低
- 可读性是否提升
- 可维护性是否改善

#### [ ] 测试验证
- 单元测试是否通过
- 集成测试是否通过
- 回归测试是否通过

---

## 📈 重构效果评估

### 代码质量指标

#### 重构前
- 平均圈复杂度：12.5
- 代码重复率：15%
- 单元测试覆盖率：45%

#### 重构后目标
- 平均圈复杂度：< 8
- 代码重复率：< 5%
- 单元测试覆盖率：> 85%

### 编译错误减少
- 类型转换错误：减少90%
- SDK冲突错误：减少100%
- 重复代码错误：减少80%

---

## 🚀 重构最佳实践

### 1. 渐进式重构
```java
// 小步重构，每次只改一个模块
// 1. 先提取类型转换工具
// 2. 再重构一个Service
// 3. 最后更新API层
```

### 2. 测试驱动重构
```java
// 先写测试，再重构代码
@Test
public void TypeConversionTest {
    @Test
    public void shouldConvertStringToLong() {
        Long result = typeConversionService.convertToLong("123");
        assertEquals(Long.valueOf(123), result);
    }
}

// 然后重构代码满足测试
```

### 3. 重构模式应用

#### 提取方法模式
```java
// 重构前：长方法
public void processUser() {
    // 验证参数 (10行)
    // 查询数据 (20行)
    // 业务逻辑 (50行)
    // 保存结果 (10行)
}

// 重构后：提取方法
public void processUser() {
    validateParameters();
    UserData data = queryUserData();
    BusinessResult result = processBusinessLogic(data);
    saveResult(result);
}

private void validateParameters() { /* ... */ }
private UserData queryUserData() { /* ... */ }
private BusinessResult processBusinessLogic(UserData data) { /* ... */ }
private void saveResult(BusinessResult result) { /* ... */ }
```

#### 引入适配器模式
```java
// 重构前：直接依赖具体实现
@Service
public class PaymentService {
    @Resource
    private WechatPaymentService wechatService;
    @Resource
    private AlipayPaymentService alipayService;

    public void pay(PaymentRequest request) {
        if ("WECHAT".equals(request.getType())) {
            wechatService.pay(request);
        } else if ("ALIPAY".equals(request.getType())) {
            alipayService.pay(request);
        }
    }
}

// 重构后：使用适配器模式
@Service
public class PaymentService {
    @Resource
    private Map<String, PaymentProvider> providers;

    public void pay(PaymentRequest request) {
        PaymentProvider provider = providers.get(request.getType());
        provider.pay(request);
    }
}
```

---

## 📋 重构执行流程

### 重构规划

#### 1. 分析阶段
- 识别重构机会
- 评估重构风险
- 制定重构计划
- 获得团队认可

#### 2. 准备阶段
- 建立重构环境
- 准备测试用例
- 备份代码版本

#### 3. 执行阶段
- 小步重构
- 持续验证
- 及时回滚

#### 4. 验证阶段
- 功能验证
- 性能测试
- 代码审查

---

## 🔧 技能升级路径

### 进阶技能
- **自动化重构工具**: IDE插件、静态分析工具
- **架构演进**: 微服务拆分、领域驱动设计
- **性能重构**: 算法优化、缓存策略
- **安全重构**: 安全漏洞修复、加密优化

---

## 📞 支持与反馈

如需重构支持：
- **技术咨询**: refactoring-support@example.com
- **问题报告**: refactoring-issues@example.com
- **最佳实践**: refactoring-bestpractices@example.com