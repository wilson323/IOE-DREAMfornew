# repowiki规范遵循专家技能

**技能名称**: repowiki-compliance-specialist
**技能等级**: ★★★ 高级
**适用角色**: 代码质量工程师、技术负责人、架构师
**前置技能**: Spring Boot开发、Java编码规范、代码审查
**预计学时**: 3小时

---

## 📋 技能概述

本技能专门针对IOE-DREAM项目的repowiki规范遵循，确保所有开发工作严格符合`D:\IOE-DREAM\docs\repowiki`下的权威规范要求。通过本技能，开发者能够实现100%规范合规，建立零容忍的代码质量保障体系。

**技术基础**: 严格基于repowiki权威规范体系
**合规标准**: 100%一级规范合规，95%二级规范达标
**质量保证**: 零容忍政策，违规必修复

## 🎯 核心能力

### 🔍 规范识别与解读
- **repowiki体系理解**: 完整理解repowiki规范体系和层级结构
- **规范条款掌握**: 精确掌握一级、二级、三级规范要求
- **违规模式识别**: 快速识别常见的规范违规模式
- **最佳实践应用**: 将规范要求转化为具体开发实践

### 🔧 规范实施能力
- **编码标准执行**: 100%符合Jakarta EE、@Resource注入等规范
- **架构规范遵循**: 严格遵循四层架构、事务边界等规范
- **安全规范实施**: 完整的权限控制、参数校验、异常处理
- **文档规范应用**: JavaDoc、Swagger注释、API文档完整

### 🛡️ 规范验证能力
- **自动化检查**: 构建自动化规范检查工具和流程
- **质量门禁**: 建立规范合规的质量门禁机制
- **违规修复**: 系统性的规范违规问题识别和修复
- **持续监控**: 规范合规性的持续监控和改进

### 📊 质量保证能力
- **零容忍执行**: 实施规范违规的零容忍政策
- **预防机制**: 建立规范违规的预防机制
- **团队培训**: 规范知识的团队培训和推广
- **文化建设**: 建立规范导向的开发文化

---

## 📖 学习内容

### 第一部分：repowiki规范体系 (1小时)

#### 1.1 repowiki规范结构
```
IOE-DREAM repowiki规范体系：

┌─────────────────────────────────────────────────────────────────┐
│                        repowiki规范体系                          │
├─────────────────────────────────────────────────────────────────┤
│  一级规范（强制执行 - 零容忍）                                   │
│  ├─ Jakarta EE包名规范                                          │
│  ├─ @Resource依赖注入规范                                        │
│  ├─ 四层架构调用链规范                                            │
│  ├─ 编码字符集规范（UTF-8）                                      │
│  └─ 安全权限控制规范                                              │
├─────────────────────────────────────────────────────────────────┤
│  二级规范（严格遵循 - 95%达标）                                   │
│  ├─ 事务管理规范                                                  │
│  ├─ 异常处理规范                                                  │
│  ├─ 日志记录规范                                                  │
│  ├─ 缓存使用规范                                                  │
│  └─ API设计规范                                                   │
├─────────────────────────────────────────────────────────────────┤
│  三级规范（建议遵循 - 持续改进）                                   │
│  ├─ 代码注释规范                                                  │
│  ├─ 性能优化规范                                                  │
│  ├─ 测试覆盖率规范                                                │
│  ├─ 代码风格规范                                                  │
│  └─ 文档完整性规范                                                │
└─────────────────────────────────────────────────────────────────┘
```

#### 1.2 一级规范详解（零容忍）

##### Jakarta EE包名规范
```java
/**
 * 一级规范：Jakarta EE包名规范（零容忍）
 *
 * 规则：
 * 1. 严禁使用javax.* EE相关包
 * 2. 必须使用jakarta.* EE相关包
 * 3. JDK标准库的javax.*包可以保留
 *
 * 违规示例：
 * - ❌ import javax.servlet.*; → jakarta.servlet.*
 * - ❌ import javax.validation.*; → jakarta.validation.*
 * - ❌ import javax.annotation.*; → jakarta.annotation.*
 * - ❌ import javax.persistence.*; → jakarta.persistence.*
 * - ✅ import javax.crypto.*; (JDK标准库，允许)
 * - ✅ import javax.security.*; (JDK标准库，允许)
 */

// ✅ 正确示例
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;

// ❌ 错误示例（违规）
import javax.servlet.http.HttpServletRequest;  // 违规
import javax.validation.Valid;              // 违规
import javax.annotation.Resource;          // 违规
import javax.persistence.Entity;            // 违规
import javax.transaction.Transactional;      // 违规

@RestController
public class ComplianceExampleController {

    @Resource  // ✅ 正确：使用@Resource
    private UserService userService;

    @PostMapping("/api/example")
    public ResponseDTO<String> createExample(
            @Valid  // ✅ 正确：使用jakarta.validation.Valid
            @RequestBody ExampleForm form) {

        HttpServletRequest request = getCurrentRequest(); // ✅ 正确：jakarta

        return userService.createExample(form);
    }

    // ❌ 错误示例：
    // @Autowired  // 违规：必须使用@Resource
    // private UserService userService;
}
```

##### 四层架构调用链规范
```java
/**
 * 一级规范：四层架构调用链规范（零容忍）
 *
 * 规则：
 * 1. 严格遵循 Controller → Service → Manager → DAO 调用链
 * 2. 禁止跨层访问
 * 3. 禁止反向调用
 * 4. 禁止平行层调用
 */

// ✅ 正确示例：标准四层架构
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService; // ✅ 只依赖Service层

    @PostMapping("/add")
    public ResponseDTO<String> addEmployee(@Valid @RequestBody EmployeeAddForm form) {
        // ✅ 只调用Service层
        return employeeService.addEmployee(form);
    }

    // ❌ 错误示例：
    // @Resource
    // private EmployeeDao employeeDao; // 违规：直接依赖DAO层
    //
    // @PostMapping("/add")
    // public ResponseDTO<String> addEmployee(@RequestBody EmployeeAddForm form) {
    //     // ❌ 违规：直接访问DAO层
    //     employeeDao.insert(convertToEntity(form));
    //     return ResponseDTO.ok("success");
    // }
}

@Service
@Transactional(rollbackFor = Throwable.class)
public class EmployeeServiceImpl implements EmployeeService {
    @Resource
    private EmployeeDao employeeDao; // ✅ 可以依赖DAO层

    @Resource
    private EmployeeManager employeeManager; // ✅ 可以依赖Manager层

    @Override
    public ResponseDTO<String> addEmployee(EmployeeAddForm form) {
        // ✅ 业务逻辑处理
        EmployeeEntity entity = convertToEntity(form);

        // ✅ 委托复杂逻辑给Manager层
        employeeManager.saveEmployeeWithRelated(entity, form.getRoleIds());

        return ResponseDTO.ok("success");
    }

    // ❌ 错误示例：
    // @Resource
    // private OtherController otherController; // 违规：依赖Controller层
    //
    // public void someMethod() {
    //     // ❌ 违规：调用Controller层
    //     otherController.someApi();
    // }
}
```

##### @Resource依赖注入规范
```java
/**
 * 一级规范：@Resource依赖注入规范（零容忍）
 *
 * 规则：
 * 1. 严禁使用@Autowired
 * 2. 必须使用@Resource进行依赖注入
 * 3. 优先基于名称注入
 * 4. 循环依赖使用@Lazy注解
 */

// ✅ 正确示例
@Service
public class ResourceInjectionService {

    // ✅ 正确：使用@Resource
    @Resource
    private UserDao userDao;

    // ✅ 正确：指定注入名称
    @Resource(name = "redisTemplate")
    private RedisTemplate<String, Object> redisTemplate;

    // ✅ 正确：处理循环依赖
    @Resource
    @Lazy
    private CircularDependencyService circularService;

    // ✅ 正确：构造器注入（可选）
    private final ExternalApiClient apiClient;

    public ResourceInjectionService(ExternalApiClient apiClient) {
        this.apiClient = apiClient;
    }
}

// ❌ 错误示例：
@Service
public class AutowiredService {

    // ❌ 错误：使用@Autowired
    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ❌ 错误：多种注入方式混合
    @Inject
    private OtherService otherService;
}
```

##### 编码字符集规范
```java
/**
 * 一级规范：编码字符集规范（零容忍）
 *
 * 规则：
 * 1. 所有Java文件必须使用UTF-8编码
 * 2. 禁止BOM标记
 * 3. 禁止中文字符乱码
 * 4. 注释和字符串使用UTF-8
 */

/**
 * ✅ 正确示例：UTF-8编码，无BOM
 *
 * 员工管理服务实现类
 * 负责处理员工相关的业务逻辑
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Service
public class 员工管理服务 {

    private static final Logger log = LoggerFactory.getLogger(员工管理服务.class);

    /**
     * 添加员工
     *
     * @param 员工信息 员工信息表单
     * @return 添加结果
     */
    public String 添加员工(EmployeeForm 员工信息) {
        // ✅ 正确：使用UTF-8编码的中文字符串
        log.info("开始添加员工：{}", 员工信息.get姓名());

        // 业务逻辑处理
        return "员工添加成功";
    }
}

// ❌ 错误示例：编码问题
// ??·??í??·??·??·??·??±?·??§??·??±??·?????????±?????±?????????±??????
// （UTF-8编码错误的示例）
```

### 第二部分：规范实施与检查 (1小时)

#### 2.1 自动化规范检查工具

##### repowiki合规检查器
```java
/**
 * repowiki规范合规检查器
 */
@Component
@Slf4j
public class RepowikiComplianceChecker {

    /**
     * 检查一级规范合规性
     */
    public RepowikiComplianceReport checkLevel1Compliance() {
        RepowikiComplianceReport report = new RepowikiComplianceReport();

        try {
            // 1. 检查Jakarta EE包名规范
            List<String> jakartaViolations = checkJakartaPackageCompliance();
            report.setJakartaViolations(jakartaViolations);

            // 2. 检查@Resource注入规范
            List<String> resourceViolations = checkResourceInjectionCompliance();
            report.setResourceViolations(resourceViolations);

            // 3. 检查四层架构规范
            List<String> architectureViolations = checkArchitectureCompliance();
            report.setArchitectureViolations(architectureViolations);

            // 4. 检查编码字符集规范
            List<String> encodingViolations = checkEncodingCompliance();
            report.setEncodingViolations(encodingViolations);

            // 5. 计算合规分数
            report.setComplianceScore(calculateComplianceScore(report));
            report.setLevel1Compliant(report.getComplianceScore() >= 100);

        } catch (Exception e) {
            log.error("repowiki合规检查失败", e);
            report.setErrorMessage("合规检查执行失败: " + e.getMessage());
        }

        return report;
    }

    /**
     * 检查Jakarta EE包名合规性
     */
    private List<String> checkJakartaPackageCompliance() {
        List<String> violations = new ArrayList<>();

        try {
            // 扫描所有Java文件
            List<File> javaFiles = scanJavaFiles();

            for (File javaFile : javaFiles) {
                violations.addAll(checkFileJakartaCompliance(javaFile));
            }

        } catch (Exception e) {
            log.error("检查Jakarta包名合规性失败", e);
            violations.add("检查过程中发生异常: " + e.getMessage());
        }

        return violations;
    }

    private List<String> checkFileJakartaCompliance(File javaFile) {
        List<String> violations = new ArrayList<>();

        try {
            String content = Files.readString(javaFile.toPath(), StandardCharsets.UTF_8);

            // 检查禁用的javax包
            String[] forbiddenPackages = {
                "javax.servlet",
                "javax.validation",
                "javax.annotation",
                "javax.persistence",
                "javax.transaction",
                "javax.ejb",
                "javax.ws.rs"
            };

            for (String forbiddenPackage : forbiddenPackages) {
                String pattern = "import\\s+" + forbiddenPackage.replace(".", "\\.") + "\\.";
                if (Pattern.compile(pattern).matcher(content).find()) {
                    violations.add(String.format(
                        "文件 %s 使用了禁用的javax包: %s，应使用jakarta.%s",
                        javaFile.getPath(),
                        forbiddenPackage,
                        forbiddenPackage.substring(7) // 去掉"javax."
                    ));
                }
            }

        } catch (IOException e) {
            violations.add("读取文件失败: " + javaFile.getPath());
        }

        return violations;
    }

    /**
     * 检查@Resource注入合规性
     */
    private List<String> checkResourceInjectionCompliance() {
        List<String> violations = new ArrayList<>();

        try {
            List<File> javaFiles = scanJavaFiles();

            for (File javaFile : javaFiles) {
                violations.addAll(checkFileResourceCompliance(javaFile));
            }

        } catch (Exception e) {
            log.error("检查@Resource注入合规性失败", e);
            violations.add("检查过程中发生异常: " + e.getMessage());
        }

        return violations;
    }

    private List<String> checkFileResourceCompliance(File javaFile) {
        List<String> violations = new ArrayList<>();

        try {
            String content = Files.readString(javaFile.toPath(), StandardCharsets.UTF_8);

            // 检查@Autowired使用
            if (content.contains("@Autowired")) {
                violations.add(String.format(
                    "文件 %s 使用了@Autowired，应使用@Resource",
                    javaFile.getPath()
                ));
            }

            // 检查@Inject使用
            if (content.contains("@Inject")) {
                violations.add(String.format(
                    "文件 %s 使用了@Inject，应使用@Resource",
                    javaFile.getPath()
                ));
            }

        } catch (IOException e) {
            violations.add("读取文件失败: " + javaFile.getPath());
        }

        return violations;
    }

    /**
     * 检查四层架构合规性
     */
    private List<String> checkArchitectureCompliance() {
        List<String> violations = new ArrayList<>();

        try {
            // 检查Controller层是否直接访问DAO层
            violations.addAll(checkControllerToDaoViolations());

            // 检查Service层事务边界
            violations.addAll(checkServiceTransactionViolations());

            // 检查跨层访问
            violations.addAll(checkCrossLayerViolations());

        } catch (Exception e) {
            log.error("检查四层架构合规性失败", e);
            violations.add("检查过程中发生异常: " + e.getMessage());
        }

        return violations;
    }

    private List<String> checkControllerToDaoViolations() {
        List<String> violations = new ArrayList<>();

        try {
            // 扫描所有Controller类
            List<Class<?>> controllers = scanControllers();

            for (Class<?> controller : controllers) {
                // 检查字段注入
                for (Field field : controller.getDeclaredFields()) {
                    if (isDaoClass(field.getType()) && hasInjectAnnotation(field)) {
                        violations.add(String.format(
                            "Controller %s 直接注入DAO %s，违反四层架构规范",
                            controller.getSimpleName(),
                            field.getType().getSimpleName()
                        ));
                    }
                }

                // 检查方法调用
                violations.addAll(checkControllerMethodCalls(controller));
            }

        } catch (Exception e) {
            violations.add("Controller层检查失败: " + e.getMessage());
        }

        return violations;
    }

    /**
     * 检查编码字符集合规性
     */
    private List<String> checkEncodingCompliance() {
        List<String> violations = new ArrayList<>();

        try {
            List<File> javaFiles = scanJavaFiles();

            for (File javaFile : javaFiles) {
                violations.addAll(checkFileEncoding(javaFile));
            }

        } catch (Exception e) {
            log.error("检查编码合规性失败", e);
            violations.add("检查过程中发生异常: " + e.getMessage());
        }

        return violations;
    }

    private List<String> checkFileEncoding(File javaFile) {
        List<String> violations = new ArrayList<>();

        try {
            // 检查文件编码
            String contentType = Files.probeContentType(javaFile.toPath());
            byte[] bytes = Files.readAllBytes(javaFile.toPath());

            // 检查BOM标记
            if (bytes.length >= 3 &&
                (bytes[0] & 0xFF) == 0xEF &&
                (bytes[1] & 0xFF) == 0xBB &&
                (bytes[2] & 0xFF) == 0xBF) {
                violations.add(String.format(
                    "文件 %s 包含BOM标记，应移除",
                    javaFile.getPath()
                ));
            }

            // 检查是否为UTF-8编码
            String content = new String(bytes, StandardCharsets.UTF_8);

            // 检查乱码模式
            if (content.contains("锟斤拷") || content.contains("涓?") || content.contains("鏂?")) {
                violations.add(String.format(
                    "文件 %s 包含乱码字符，需要修复编码",
                    javaFile.getPath()
                ));
            }

        } catch (Exception e) {
            violations.add("编码检查失败: " + javaFile.getPath());
        }

        return violations;
    }

    private double calculateComplianceScore(RepowikiComplianceReport report) {
        int totalViolations = report.getJakartaViolations().size() +
                             report.getResourceViolations().size() +
                             report.getArchitectureViolations().size() +
                             report.getEncodingViolations().size();

        // 一级规范违规每个扣25分，确保零容忍
        return Math.max(0, 100 - (totalViolations * 25));
    }
}
```

#### 2.2 质量门禁实现

##### repowiki质量门禁
```java
/**
 * repowiki规范质量门禁
 */
@Component
@Slf4j
public class RepowikiQualityGate {

    @Resource
    private RepowikiComplianceChecker complianceChecker;

    /**
     * 执行质量门禁检查
     */
    public QualityGateResult executeQualityGate() {
        QualityGateResult result = new QualityGateResult();

        try {
            log.info("🔍 开始执行repowiki规范质量门禁检查...");

            // 1. 执行一级规范检查
            RepowikiComplianceReport complianceReport = complianceChecker.checkLevel1Compliance();
            result.setComplianceReport(complianceReport);

            // 2. 执行二级规范检查
            SecondLevelComplianceReport secondLevelReport = checkSecondLevelCompliance();
            result.setSecondLevelReport(secondLevelReport);

            // 3. 计算总体结果
            result.setOverallScore(calculateOverallScore(complianceReport, secondLevelReport));
            result.setPassed(isQualityGatePassed(result));

            // 4. 生成报告
            generateQualityReport(result);

            // 5. 记录指标
            recordQualityMetrics(result);

            log.info("🎯 repowiki质量门禁检查完成，结果: {}, 分数: {}",
                result.isPassed() ? "通过" : "失败",
                result.getOverallScore());

        } catch (Exception e) {
            log.error("执行质量门禁失败", e);
            result.setErrorMessage("质量门禁执行失败: " + e.getMessage());
            result.setPassed(false);
        }

        return result;
    }

    /**
     * 检查质量门禁是否通过
     */
    private boolean isQualityGatePassed(QualityGateResult result) {
        // 一级规范必须100%合规（零容忍）
        if (!result.getComplianceReport().isLevel1Compliant()) {
            log.error("❌ 一级规范合规性检查失败");
            return false;
        }

        // 总体分数必须≥95分
        if (result.getOverallScore() < 95) {
            log.error("❌ 质量分数不足: {} < 95", result.getOverallScore());
            return false;
        }

        // 二级规范违规数量必须≤5个
        if (result.getSecondLevelReport().getViolationCount() > 5) {
            log.error("❌ 二级规范违规过多: {}",
                result.getSecondLevelReport().getViolationCount());
            return false;
        }

        return true;
    }

    /**
     * 生成质量门禁报告
     */
    private void generateQualityReport(QualityGateResult result) {
        try {
            String reportPath = "repowiki-quality-report-" +
                System.currentTimeMillis() + ".json";

            String reportJson = JsonUtils.toJsonString(result);
            Files.write(Paths.get(reportPath), reportJson.getBytes(StandardCharsets.UTF_8));

            log.info("📄 repowiki质量报告已生成: {}", reportPath);

        } catch (Exception e) {
            log.error("生成质量报告失败", e);
        }
    }

    /**
     * 记录质量指标
     */
    private void recordQualityMetrics(QualityGateResult result) {
        try {
            // 记录合规分数
            meterRegistry.gauge("repowiki.compliance.score",
                result.getComplianceReport().getComplianceScore());

            // 记录违规数量
            meterRegistry.gauge("repowiki.violations.count",
                result.getTotalViolationCount());

            // 记录质量门禁结果
            meterRegistry.counter("repowiki.quality.gate.result",
                "passed", String.valueOf(result.isPassed()))
                .increment();

        } catch (Exception e) {
            log.error("记录质量指标失败", e);
        }
    }
}
```

### 第三部分：规范违规修复 (1小时)

#### 3.1 系统性修复工具

##### 自动修复工具
```java
/**
 * repowiki规范自动修复工具
 */
@Component
@Slf4j
public class RepowikiAutoFixer {

    /**
     * 自动修复所有规范违规
     */
    public AutoFixResult fixAllViolations() {
        AutoFixResult result = new AutoFixResult();

        try {
            log.info("🔧 开始自动修复repowiki规范违规...");

            // 1. 修复Jakarta EE包名违规
            JakartaFixResult jakartaResult = fixJakartaPackageViolations();
            result.setJakartaFixResult(jakartaResult);

            // 2. 修复@Resource注入违规
            ResourceFixResult resourceResult = fixResourceInjectionViolations();
            result.setResourceFixResult(resourceResult);

            // 3. 修复编码违规
            EncodingFixResult encodingResult = fixEncodingViolations();
            result.setEncodingFixResult(encodingResult);

            // 4. 修复架构违规
            ArchitectureFixResult architectureResult = fixArchitectureViolations();
            result.setArchitectureFixResult(architectureResult);

            // 5. 计算修复统计
            result.calculateStatistics();

            log.info("🎉 repowiki规范自动修复完成，修复数量: {}",
                result.getTotalFixedCount());

        } catch (Exception e) {
            log.error("自动修复失败", e);
            result.setErrorMessage("自动修复执行失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 修复Jakarta EE包名违规
     */
    public JakartaFixResult fixJakartaPackageViolations() {
        JakartaFixResult result = new JakartaFixResult();

        try {
            List<File> javaFiles = scanJavaFiles();

            for (File javaFile : javaFiles) {
                if (fixFileJakartaPackages(javaFile)) {
                    result.addFixedFile(javaFile.getPath());
                }
            }

            log.info("Jakarta EE包名修复完成，修复文件数: {}",
                result.getFixedFiles().size());

        } catch (Exception e) {
            log.error("修复Jakarta EE包名失败", e);
            result.setErrorMessage("修复失败: " + e.getMessage());
        }

        return result;
    }

    private boolean fixFileJakartaPackages(File javaFile) {
        try {
            String content = Files.readString(javaFile.toPath(), StandardCharsets.UTF_8);
            String originalContent = content;

            // 修复包名映射
            Map<String, String> packageMapping = Map.of(
                "javax.servlet", "jakarta.servlet",
                "javax.validation", "jakarta.validation",
                "javax.annotation", "jakarta.annotation",
                "javax.persistence", "jakarta.persistence",
                "javax.transaction", "jakarta.transaction",
                "javax.ejb", "jakarta.ejb",
                "javax.ws.rs", "jakarta.ws.rs"
            );

            // 执行包名替换
            for (Map.Entry<String, String> entry : packageMapping.entrySet()) {
                String oldPattern = "import\\s+" + entry.getKey().replace(".", "\\.") + "\\.";
                String newImport = "import " + entry.getValue() + ".";

                content = content.replaceAll(oldPattern, newImport);
            }

            // 如果内容有变化，写回文件
            if (!originalContent.equals(content)) {
                Files.write(javaFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                return true;
            }

        } catch (Exception e) {
            log.error("修复文件Jakarta包名失败: {}", javaFile.getPath(), e);
        }

        return false;
    }

    /**
     * 修复@Resource注入违规
     */
    public ResourceFixResult fixResourceInjectionViolations() {
        ResourceFixResult result = new ResourceFixResult();

        try {
            List<File> javaFiles = scanJavaFiles();

            for (File javaFile : javaFiles) {
                if (fixFileResourceInjection(javaFile)) {
                    result.addFixedFile(javaFile.getPath());
                }
            }

            log.info("Resource注入修复完成，修复文件数: {}",
                result.getFixedFiles().size());

        } catch (Exception e) {
            log.error("修复Resource注入失败", e);
            result.setErrorMessage("修复失败: " + e.getMessage());
        }

        return result;
    }

    private boolean fixFileResourceInjection(File javaFile) {
        try {
            String content = Files.readString(javaFile.toPath(), StandardCharsets.UTF_8);
            String originalContent = content;

            // 替换@Autowired为@Resource
            content = content.replaceAll("@Autowired", "@Resource");

            // 替换@Inject为@Resource
            content = content.replaceAll("@Inject", "@Resource");

            // 如果内容有变化，写回文件
            if (!originalContent.equals(content)) {
                Files.write(javaFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                return true;
            }

        } catch (Exception e) {
            log.error("修复文件Resource注入失败: {}", javaFile.getPath(), e);
        }

        return false;
    }

    /**
     * 修复编码违规
     */
    public EncodingFixResult fixEncodingViolations() {
        EncodingFixResult result = new EncodingFixResult();

        try {
            List<File> javaFiles = scanJavaFiles();

            for (File javaFile : javaFiles) {
                if (fixFileEncoding(javaFile)) {
                    result.addFixedFile(javaFile.getPath());
                }
            }

            log.info("编码修复完成，修复文件数: {}",
                result.getFixedFiles().size());

        } catch (Exception e) {
            log.error("修复编码失败", e);
            result.setErrorMessage("修复失败: " + e.getMessage());
        }

        return result;
    }

    private boolean fixFileEncoding(File javaFile) {
        try {
            // 读取原始字节
            byte[] originalBytes = Files.readAllBytes(javaFile.toPath());

            // 检查并移除BOM
            byte[] processedBytes = removeBom(originalBytes);

            // 尝试UTF-8解码
            String content = new String(processedBytes, StandardCharsets.UTF_8);

            // 修复常见乱码
            content = fixGarbledText(content);

            // 重新写入文件
            Files.write(javaFile.toPath(), content.getBytes(StandardCharsets.UTF_8));

            return true;

        } catch (Exception e) {
            log.error("修复文件编码失败: {}", javaFile.getPath(), e);
        }

        return false;
    }

    private byte[] removeBom(byte[] bytes) {
        if (bytes.length >= 3 &&
            (bytes[0] & 0xFF) == 0xEF &&
            (bytes[1] & 0xFF) == 0xBB &&
            (bytes[2] & 0xFF) == 0xBF) {
            return Arrays.copyOfRange(bytes, 3, bytes.length);
        }
        return bytes;
    }

    private String fixGarbledText(String content) {
        // 修复常见乱码模式
        content = content.replace("锟斤拷", "");
        content = content.replace("涓?", "中");
        content = content.replace("鏂?", "新");
        content = content.replace("??", "");

        return content;
    }
}
```

#### 3.2 预防机制建立

##### Git Pre-commit Hook
```bash
#!/bin/bash
# repowiki规范Pre-commit Hook

echo "🔍 repowiki规范检查..."

# 1. 获取待提交的Java文件
java_files=$(git diff --cached --name-only --diff-filter=ACM | grep "\.java$")

if [ -z "$java_files" ]; then
    echo "✅ 无Java文件需要检查"
    exit 0
fi

echo "📋 检查文件数量: $(echo $java_files | wc -l)"

# 2. 执行repowiki规范检查
echo "🔧 执行Jakarta EE包名检查..."
jakarta_violations=0
for file in $java_files; do
    if grep -q "import javax\.\(servlet\|validation\|annotation\|persistence\|transaction\|ejb\|ws\.rs\)\." "$file"; then
        echo "❌ $file: 发现禁用的javax包"
        jakarta_violations=$((jakarta_violations + 1))
    fi
done

echo "🔧 执行@Resource注入检查..."
resource_violations=0
for file in $java_files; do
    if grep -q "@Autowired\|@Inject" "$file"; then
        echo "❌ $file: 发现@Autowired或@Inject，应使用@Resource"
        resource_violations=$((resource_violations + 1))
    fi
done

echo "🔧 执行编码检查..."
encoding_violations=0
for file in $java_files; do
    # 检查BOM
    if [ -f "$file" ] && file "$file" | grep -q "UTF-8.*with BOM"; then
        echo "❌ $file: 包含BOM标记"
        encoding_violations=$((encoding_violations + 1))
    fi

    # 检查乱码
    if grep -q "锟斤拷\|涓?\|鏂?" "$file"; then
        echo "❌ $file: 包含乱码字符"
        encoding_violations=$((encoding_violations + 1))
    fi
done

# 3. 计算总违规数
total_violations=$((jakarta_violations + resource_violations + encoding_violations))

echo "📊 检查结果:"
echo "  Jakarta违规: $jakarta_violations"
echo "  Resource违规: $resource_violations"
echo "  编码违规: $encoding_violations"
echo "  总违规数: $total_violations"

# 4. 质量门禁检查
if [ $total_violations -gt 0 ]; then
    echo ""
    echo "❌ repowiki规范检查失败！"
    echo "发现 $total_violations 个规范违规，请修复后重新提交"
    echo ""
    echo "💡 修复建议："
    echo "  1. 运行: ./scripts/fix-repowiki-violations.sh"
    echo "  2. 或使用: Skill('repowiki-compliance-specialist')"
    exit 1
fi

echo "✅ repowiki规范检查通过！"
exit 0
```

##### IDE插件配置
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="repowiki" version="173">
    <!-- Java代码风格配置 -->
    <JavaCodeStyleSettings>
      <!-- 强制使用@Resource而不是@Autowired -->
      <option name="PREFER_RESOURCE_OVER_AUTOWIRED" value="true" />

      <!-- 强制使用jakarta包名 -->
      <option name="ENFORCE_JAKARTA_PACKAGES" value="true" />

      <!-- UTF-8编码强制 -->
      <option name="RIGHT_MARGIN" value="120" />
      <option name="WRAP_WHEN_TYPING_REACHES_RIGHT_MARGIN" value="true" />
    </JavaCodeStyleSettings>

    <!-- 文件编码配置 -->
    <codeStyleSettings language="JAVA">
      <option name="USE_SAME_INDENTS" value="true" />
      <option name="INDENT_SIZE" value="4" />
      <option name="CONTINUATION_INDENT_SIZE" value="8" />
      <option name="TAB_SIZE" value="4" />
    </codeStyleSettings>
  </code_scheme>
</component>
```

### 第四部分：团队培训与文化建设 (1小时)

#### 4.1 规范培训体系

##### 分层培训计划
```java
/**
 * repowiki规范培训管理器
 */
@Component
@Slf4j
public class RepowikiTrainingManager {

    /**
     * 新员工规范培训
     */
    public TrainingResult trainNewEmployee(String employeeId) {
        TrainingResult result = new TrainingResult();

        try {
            // 第一阶段：理论培训（2小时）
            result.addStage(theoryTraining(employeeId));

            // 第二阶段：实践培训（3小时）
            result.addStage(practicalTraining(employeeId));

            // 第三阶段：考核评估（1小时）
            result.addStage(assessmentTraining(employeeId));

            result.setCompleted(result.isAllStagesPassed());

            log.info("新员工 {} repowiki规范培训完成，结果: {}",
                employeeId, result.isCompleted() ? "通过" : "未通过");

        } catch (Exception e) {
            log.error("新员工规范培训失败", e);
            result.setErrorMessage("培训失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 理论培训阶段
     */
    private TrainingStage theoryTraining(String employeeId) {
        TrainingStage stage = new TrainingStage("理论培训");

        // 培训内容
        List<TrainingModule> modules = Arrays.asList(
            new TrainingModule("repowiki规范体系概述", 30),
            new TrainingModule("一级规范详解（零容忍）", 60),
            new TrainingModule("二级规范详解（严格遵循）", 45),
            new TrainingModule("规范检查工具使用", 15)
        );

        // 执行培训
        for (TrainingModule module : modules) {
            boolean passed = executeTrainingModule(employeeId, module);
            stage.addModuleResult(module.getName(), passed);

            if (!passed) {
                log.warn("员工 {} 模块 {} 培训未通过", employeeId, module.getName());
            }
        }

        stage.setPassed(stage.getPassRate() >= 0.8);
        return stage;
    }

    /**
     * 实践培训阶段
     */
    private TrainingStage practicalTraining(String employeeId) {
        TrainingStage stage = new TrainingStage("实践培训");

        // 实践任务
        List<PracticalTask> tasks = Arrays.asList(
            new PracticalTask("修复Jakarta包名违规", "jakarta-fix-exercise", 60),
            new PracticalTask("修复@Resource注入违规", "resource-fix-exercise", 45),
            new PracticalTask("修复编码违规", "encoding-fix-exercise", 30),
            new PracticalTask("四层架构代码重构", "architecture-refactor-exercise", 90)
        );

        // 执行实践任务
        for (PracticalTask task : tasks) {
            boolean passed = executePracticalTask(employeeId, task);
            stage.addTaskResult(task.getName(), passed);

            if (!passed) {
                log.warn("员工 {} 实践任务 {} 未通过", employeeId, task.getName());
            }
        }

        stage.setPassed(stage.getPassRate() >= 0.75);
        return stage;
    }

    /**
     * 考核评估阶段
     */
    private TrainingStage assessmentTraining(String employeeId) {
        TrainingStage stage = new TrainingStage("考核评估");

        // 理论考核
        TheoryAssessmentResult theoryResult = conductTheoryAssessment(employeeId);
        stage.addAssessmentResult("理论考核", theoryResult);

        // 实践考核
        PracticalAssessmentResult practicalResult = conductPracticalAssessment(employeeId);
        stage.addAssessmentResult("实践考核", practicalResult);

        // 综合评估
        boolean overallPassed = (theoryResult.getScore() >= 85) &&
                               (practicalResult.getScore() >= 80);
        stage.setPassed(overallPassed);

        return stage;
    }

    /**
     * 持续规范教育
     */
    @Scheduled(fixedRate = 7 * 24 * 3600 * 1000) // 每周执行
    public void ongoingComplianceEducation() {
        try {
            log.info("📚 开始每周规范教育...");

            // 1. 分析近期违规趋势
            ViolationTrendAnalysis trend = analyzeRecentViolations();

            // 2. 识别重点培训领域
            List<String> focusAreas = identifyTrainingFocusAreas(trend);

            // 3. 生成培训内容
            List<TrainingContent> contents = generateTrainingContents(focusAreas);

            // 4. 分发培训材料
            distributeTrainingMaterials(contents);

            // 5. 组织培训会议
            organizeTrainingSession(contents);

            log.info("📚 每周规范教育完成，培训领域: {}", focusAreas);

        } catch (Exception e) {
            log.error("持续规范教育失败", e);
        }
    }
}
```

#### 4.2 规范文化建设

##### 规范文化推广活动
```java
/**
 * repowiki规范文化推广器
 */
@Component
@Slf4j
public class RepowikiCulturePromoter {

    /**
     * 规范文化月活动
     */
    public void promoteComplianceCulture() {
        try {
            log.info("🎯 开始repowiki规范文化推广活动...");

            // 第一周：规范宣传周
            promoteComplianceAwareness();

            // 第二周：规范培训周
            conductComplianceTraining();

            // 第三周：规范实践周
            organizeCompliancePractice();

            // 第四周：规范评估周
            conductComplianceAssessment();

            log.info("🎉 repowiki规范文化推广活动完成");

        } catch (Exception e) {
            log.error("规范文化推广失败", e);
        }
    }

    /**
     * 规范意识宣传
     */
    private void promoteComplianceAwareness() {
        // 1. 发布规范宣传海报
        publishCompliancePosters();

        // 2. 组织规范知识竞赛
        organizeCompetition();

        // 3. 分享规范最佳实践
        shareBestPractices();

        // 4. 设立规范榜样
        recognizeComplianceChampions();
    }

    /**
     * 规范合规表彰
     */
    @Scheduled(cron = "0 0 9 * * MON") // 每周一早上9点
    public void weeklyComplianceRecognition() {
        try {
            // 1. 统计上周规范数据
            WeeklyComplianceStats stats = calculateWeeklyStats();

            // 2. 识别优秀个人和团队
            List<ComplianceChampion> champions = identifyChampions(stats);

            // 3. 发布表彰公告
            publishRecognitionAnnouncement(champions);

            // 4. 发放奖励
            distributeRewards(champions);

            log.info("🏆 周规范合规表彰完成，表彰人数: {}", champions.size());

        } catch (Exception e) {
            log.error("周规范合规表彰失败", e);
        }
    }

    /**
     * 规范违规预警机制
     */
    @EventListener
    @Async
    public void handleViolationAlert(ViolationDetectedEvent event) {
        try {
            // 1. 评估违规严重程度
            ViolationSeverity severity = assessViolationSeverity(event);

            // 2. 确定处理策略
            HandlingStrategy strategy = determineHandlingStrategy(severity);

            // 3. 执行处理策略
            executeHandlingStrategy(event, strategy);

            // 4. 记录处理结果
            recordHandlingResult(event, strategy);

            log.info("⚠️ 规范违规处理完成: {}, 策略: {}",
                event.getViolationType(), strategy.getName());

        } catch (Exception e) {
            log.error("处理规范违规预警失败", e);
        }
    }

    /**
     * 建立规范社区
     */
    public void buildComplianceCommunity() {
        try {
            // 1. 创建规范交流群组
            createComplianceGroups();

            // 2. 组织技术分享会
            organizeTechnicalSharing();

            // 3. 建立规范知识库
            buildKnowledgeBase();

            // 4. 开展导师制度
            establishMentorshipProgram();

            log.info("🤝 规范社区建立完成");

        } catch (Exception e) {
            log.error("建立规范社区失败", e);
        }
    }
}
```

---

## 🛠️ 实践案例

### 案例1：repowiki规范全面整改项目
```java
/**
 * repowiki规范全面整改项目实施
 */
@Service
public class RepowikiComplianceProject {

    /**
     * 项目阶段1：现状评估
     */
    public ProjectResult phase1Assessment() {
        ProjectResult result = new ProjectResult();

        try {
            // 1. 全量扫描规范违规
            ComplianceScanResult scanResult = scanAllViolations();
            result.setScanResult(scanResult);

            // 2. 分析违规分布
            ViolationAnalysis analysis = analyzeViolationDistribution(scanResult);
            result.setAnalysis(analysis);

            // 3. 评估整改成本
            CostEstimation cost = estimateRemediationCost(analysis);
            result.setCostEstimation(cost);

            // 4. 制定整改计划
            RemediationPlan plan = createRemediationPlan(analysis, cost);
            result.setRemediationPlan(plan);

            log.info("📊 现状评估完成，发现违规: {} 个",
                scanResult.getTotalViolations());

        } catch (Exception e) {
            log.error("现状评估失败", e);
            result.setErrorMessage("评估失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 项目阶段2：自动修复
     */
    public ProjectResult phase2AutoFix() {
        ProjectResult result = new ProjectResult();

        try {
            // 1. 执行批量自动修复
            AutoFixResult autoFixResult = executeBatchAutoFix();
            result.setAutoFixResult(autoFixResult);

            // 2. 验证修复效果
            FixValidationResult validation = validateFixes(autoFixResult);
            result.setValidationResult(validation);

            // 3. 处理修复失败项
            ManualFixPlan manualPlan = handleFailedFixes(validation);
            result.setManualFixPlan(manualPlan);

            log.info("🔧 自动修复完成，修复: {} 项，失败: {} 项",
                autoFixResult.getFixedCount(),
                autoFixResult.getFailedCount());

        } catch (Exception e) {
            log.error("自动修复失败", e);
            result.setErrorMessage("自动修复失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 项目阶段3：人工修复
     */
    public ProjectResult phase3ManualFix() {
        ProjectResult result = new ProjectResult();

        try {
            // 1. 分配人工修复任务
            TaskAssignmentResult assignment = assignManualFixTasks();
            result.setTaskAssignment(assignment);

            // 2. 监控修复进度
            ProgressMonitorResult progress = monitorFixProgress();
            result.setProgressMonitor(progress);

            // 3. 质量检查
            QualityCheckResult qualityCheck = performQualityCheck();
            result.setQualityCheck(qualityCheck);

            // 4. 最终验收
            AcceptanceResult acceptance = conductFinalAcceptance();
            result.setAcceptanceResult(acceptance);

            log.info("👥 人工修复完成，修复进度: {}%",
                progress.getCompletionPercentage());

        } catch (Exception e) {
            log.error("人工修复失败", e);
            result.setErrorMessage("人工修复失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 项目阶段4：持续改进
     */
    public ProjectResult phase4ContinuousImprovement() {
        ProjectResult result = new ProjectResult();

        try {
            // 1. 建立持续监控机制
            MonitoringSetupResult monitoring = setupContinuousMonitoring();
            result.setMonitoringSetup(monitoring);

            // 2. 制定预防措施
            PreventionMeasureResult prevention = implementPreventionMeasures();
            result.setPreventionMeasures(prevention);

            // 3. 培训团队
            TeamTrainingResult training = trainDevelopmentTeam();
            result.setTeamTraining(training);

            // 4. 建立文化
            CultureBuildingResult culture = buildComplianceCulture();
            result.setCultureBuilding(culture);

            log.info("🔄 持续改进机制建立完成");

        } catch (Exception e) {
            log.error("持续改进失败", e);
            result.setErrorMessage("持续改进失败: " + e.getMessage());
        }

        return result;
    }
}
```

---

## 🎓 评估标准

### 理论知识评估 (40%)
- [ ] 完全理解repowiki规范体系和层级结构
- [ ] 精确掌握一级、二级、三级规范要求
- [ ] 熟练识别常见规范违规模式
- [ ] 了解规范质量保证方法论

### 实践技能评估 (60%)
- [ ] 能够实施100%repowiki规范合规
- [ ] 能够建立自动化规范检查工具
- [ ] 能够系统性修复规范违规
- [ ] 能够建立规范文化团队

### 质量标准
- **规范合规率**: 一级规范100%合规，二级规范95%达标
- **违规修复率**: 发现违规100%修复
- **工具覆盖率**: 规范检查工具100%覆盖
- **团队规范意识**: 100%团队成员具备规范意识

---

## ⚠️ 注意事项

### 零容忍政策提醒
- 任何一级规范违规都应立即修复
- 建立违规必报、必查、必改机制
- 违规修复是团队共同责任
- 规范合规是底线要求

### 团队合作提醒
- 规范遵循是全团队的责任
- 建立规范互助和监督机制
- 鼓励规范改进和创新实践
- 建立规范知识共享平台

### 持续改进提醒
- 定期评估规范执行效果
- 及时更新规范要求和工具
- 持续优化规范检查流程
- 建立规范反馈改进机制

---

## 🚀 进阶学习

### 扩展技能
- **代码质量度量**: 更深度的代码质量度量和分析
- **静态代码分析**: 静态代码分析工具开发和应用
- **DevOps规范**: DevOps流程中的规范管理
- **AI辅助编码**: AI辅助的规范遵循和检查

### 相关技能
- **代码审查专家**: 专业的代码审查技能
- **技术债务管理**: 技术债务识别和管理技能
- **软件工程**: 软件工程最佳实践
- **团队管理**: 技术团队管理和培训技能

---

## 📞 支持与反馈

如需repowiki规范遵循支持：
- **规范咨询**: repowiki-support@example.com
- **违规报告**: violation-report@example.com
- **工具支持**: repowiki-tools@example.com
- **培训咨询**: repowiki-training@example.com

---

*最后更新: 2025-11-16*
*版本: 1.0.0*
*维护者: SmartAdmin Team*
*基于repowiki权威规范体系*
*合规标准: 100%一级规范合规*
