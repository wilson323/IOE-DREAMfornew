# 四层架构守护专家技能
## Four Tier Architecture Guardian

**🎯 技能定位**: IOE-DREAM智慧园区四层架构守护专家，严格确保Controller→Service→Manager→DAO架构规范的执行，防止跨层访问和架构违规

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 当前状态**: 🚨 项目存在严重架构违规，需要立即修复
**🎯 适用场景**: 架构合规检查、代码审查、规范培训、架构重构、违规修复
**📊 技能覆盖**: 架构验证 | 跨层检查 | 规范执行 | 违规修复 | 代码审查 | 架构培训

---

## 📋 技能概述

### **核心专长**
- **四层架构规范**: Controller→Service→Manager→DAO分层验证
- **依赖注入检查**: @Resource vs @Autowired规范验证
- **DAO层命名规范**: @Mapper注解和Dao后缀验证
- **事务管理检查**: 事务注解使用规范验证
- **Jakarta EE包名检查**: javax vs jakarta包名验证
- **跨层访问检测**: 禁止跨层直接访问的违规检测
- **技术栈统一检查**: 强制执行统一技术栈规范

### **解决能力**
- **架构违规预防**: 在代码开发阶段预防架构违规
- **架构合规诊断**: 深度分析现有代码的架构合规性
- **违规代码修复**: 系统性修复架构违规问题
- **架构规范培训**: 团队架构规范培训和指导
- **代码质量保障**: 确保代码符合企业级架构标准
- **技术栈统一**: 确保所有代码使用统一技术栈标准
- **依赖注入规范**: 强制使用@Resource注解，禁止@Autowired
- **Mapper规范**: 统一使用@Mapper注解，禁止@Repository
- **Spring Boot 3.x兼容**: 确保jakarta包名规范，禁用javax包名

---

## 🎯 四层架构规范详解

### 📋 架构分层定义

#### 🎯 Controller层 - 接口控制层
**核心职责**：
- 接收HTTP请求，参数验证(@Valid)
- 调用Service层，暴露REST API
- 封装ResponseDTO，处理HTTP状态码
- 异常统一处理和错误码返回

**代码规范**：
```java
// ✅ 正确示例 - 强制技术栈规范
@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "用户管理")
public class UserController {

    @Resource  // 🔴 强制：必须使用@Resource，禁止@Autowired
    private UserService userService;

    @PostMapping("/create")
    @Operation(summary = "创建用户")
    public ResponseDTO<UserVO> createUser(@Valid @RequestBody CreateUserRequestDTO request) {
        UserVO user = userService.createUser(request);
        return ResponseDTO.ok(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ResponseDTO<UserDetailVO> getUserDetail(@PathVariable Long id) {
        UserDetailVO userDetail = userService.getUserDetail(id);
        return ResponseDTO.ok(userDetail);
    }

    // ❌ 严格禁止示例
    // 1. ❌ 禁止使用@Autowired注解
    // 2. ❌ 禁止直接调用Manager层
    // 3. ❌ 禁止直接调用DAO层
    // 4. ❌ 禁止在Controller中处理业务逻辑
    // 5. ❌ 禁止在Controller中管理事务
    // 6. ❌ 禁止使用javax包名，必须使用jakarta包名
}
```

#### ⚙️ Service层 - 核心业务层
**核心职责**：
- 核心业务逻辑实现
- 事务管理(@Transactional)
- 调用Manager层进行复杂流程编排
- 业务规则验证和数据转换

**代码规范**：
```java
// ✅ 正确示例 - 强制技术栈规范
@Service
@Transactional(rollbackFor = Exception.class)  // 类级别事务
public class UserServiceImpl implements UserService {

    @Resource  // 🔴 强制：必须使用@Resource，禁止@Autowired
    private UserManager userManager;

    @Resource  // 🔴 强制：必须使用@Resource，禁止@Autowired
    private UserDao userDao;

    @Override
    public UserVO createUser(CreateUserRequestDTO request) {
        // 业务规则验证
        validateCreateUserRequest(request);

        // 核心业务逻辑
        return userManager.createUser(request);
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        // 业务规则验证
        validateUserId(userId);

        // 调用Manager层进行复杂业务处理
        return userManager.buildUserDetail(userId);
    }

    // ❌ 严格禁止示例
    // 1. ❌ 禁止使用@Autowired注解
    // 2. ❌ 禁止跨过Manager直接调用复杂业务
    // 3. ❌ 禁止在Service中处理数据库直接查询（简单查询除外）
    // 4. ❌ 禁止在Service中包含Controller层逻辑
    // 5. ❌ 禁止使用javax包名，必须使用jakarta包名
}
```

#### 🔧 Manager层 - 复杂流程管理层
**核心职责**：
- 复杂业务流程编排
- 多DAO数据组装和计算
- 缓存策略管理
- 第三方服务集成
- SAGA分布式事务协调

**代码规范**：
```java
// ✅ 正确示例 - Manager类为纯Java类，不使用Spring注解
public class UserManager {

    private final UserDao userDao;
    private final DepartmentDao departmentDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    // 构造函数注入依赖 - Manager层不使用Spring注解
    public UserManager(UserDao userDao, DepartmentDao departmentDao,
                      GatewayServiceClient gatewayServiceClient,
                      RedisTemplate<String, Object> redisTemplate) {
        this.userDao = userDao;
        this.departmentDao = departmentDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.redisTemplate = redisTemplate;
    }

    public UserVO createUser(CreateUserRequestDTO request) {
        // 1. 多级缓存查询
        DepartmentEntity department = getDepartmentWithCache(request.getDepartmentId());

        // 2. 复杂业务流程
        UserEntity user = buildUserEntity(request, department);

        // 3. 数据持久化
        Long userId = saveUser(user);

        // 4. 缓存更新
        updateUserCache(userId, user);

        // 5. 第三方服务调用
        callThirdPartyServices(user);

        return convertToUserVO(user);
    }

    private DepartmentEntity getDepartmentWithCache(Long departmentId) {
        String cacheKey = "department:info:" + departmentId;

        // L1: 尝试从Redis缓存获取
        DepartmentEntity cached = (DepartmentEntity) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // L2: 从数据库查询
        DepartmentEntity department = departmentDao.selectById(departmentId);
        if (department != null) {
            // 缓存30分钟
            redisTemplate.opsForValue().set(cacheKey, department, Duration.ofMinutes(30));
        }

        return department;
    }

    // ❌ Manager层禁止使用Spring注解
    // ❌ @Component
    // ❌ @Service
    // ❌ @Resource
    // ❌ @Autowired
}
```

#### 🗄️ DAO层 - 数据访问层
**核心职责**：
- 数据库CRUD操作
- 复杂SQL查询实现
- 继承BaseMapper<Entity>
- 数据库事务边界控制

**代码规范**：
```java
// ✅ 正确示例 - 强制技术栈规范
@Mapper  // 🔴 强制：必须使用@Mapper注解，禁止@Repository
public interface UserDao extends BaseMapper<UserEntity> {  // 🔴 强制：必须继承BaseMapper

    @Transactional(readOnly = true)
    UserEntity selectByUsername(@Param("username") String username);

    @Transactional(readOnly = true)
    List<UserEntity> selectByDepartmentId(@Param("departmentId") Long departmentId);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);

    @Select("SELECT * FROM t_common_user WHERE deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<UserEntity> selectRecentUsers(@Param("limit") int limit);

    // ❌ 严格禁止示例
    // 1. ❌ 禁止使用@Repository注解
    // 2. ❌ 禁止使用JpaRepository和JPA
    // 3. ❌ 禁止使用Repository后缀命名
    // 4. ❌ 禁止包含业务逻辑
    // 5. ❌ 禁止使用@Service或@Component注解
    // 6. ❌ 禁止处理事务外的业务逻辑
    // 7. ❌ 禁止使用javax包名，必须使用jakarta包名
}

// ❌ 严重错误示例 - 技术栈违规
@Repository  // 🔴 严重违规：禁止使用@Repository注解
public interface UserRepository extends JpaRepository<UserEntity, Long> {  // 🔴 严重违规：禁止使用JPA
    // JPA相关代码被完全禁止
    // 必须改为：@Mapper public interface UserDao extends BaseMapper<UserEntity>
}
```

---

## 🔍 架构违规检测和修复

### 跨层访问检测
```java
// 跨层访问检测工具
@Component
@Slf4j
public class ArchitectureViolationDetector {

    private static final Map<String, List<String>> ALLOWED_CALLS = Map.of(
        "Controller", List.of("Service"),
        "Service", List.of("Service", "Manager", "Dao"),
        "Manager", List.of("Manager", "Dao"),
        "Dao", List.of("Dao")
    );

    /**
     * 检测架构违规
     */
    public ArchitectureViolationReport detectViolations(String projectPath) {
        ArchitectureViolationReport report = new ArchitectureViolationReport();

        // 1. 扫描Java文件
        List<File> javaFiles = scanJavaFiles(projectPath);

        // 2. 解析每个文件
        for (File javaFile : javaFiles) {
            try {
                JavaFileObject fileObject = new SimpleJavaFileObject(javaFile.toURI(), JavaFileObject.Kind.SOURCE);
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                JavacTask task = (JavacTask) compiler.getTask(null, null, null, null, null, List.of(fileObject));

                // 解析AST
                Iterable<? extends CompilationUnitTree> trees = task.parse();
                for (CompilationUnitTree tree : trees) {
                    analyzeCompilationUnit(tree, report);
                }

            } catch (Exception e) {
                log.warn("解析文件失败: {}", javaFile.getPath(), e);
            }
        }

        return report;
    }

    private void analyzeCompilationUnit(CompilationUnitTree tree, ArchitectureViolationReport report) {
        // 获取类型声明
        List<? extends Tree> typeDecls = tree.getTypeDecls();
        for (Tree typeDecl : typeDecls) {
            if (typeDecl instanceof ClassTree) {
                analyzeClassTree((ClassTree) typeDecl, report);
            }
        }
    }

    private void analyzeClassTree(ClassTree classTree, ArchitectureViolationReport report) {
        String className = getSimpleClassName(classTree);
        LayerType callerLayer = determineLayerType(className, classTree);

        // 检查字段注入
        analyzeFieldInjections(classTree, callerLayer, report);

        // 检查方法调用
        analyzeMethodCalls(classTree, callerLayer, report);
    }

    private void analyzeFieldInjections(ClassTree classTree, LayerType callerLayer, ArchitectureViolationReport report) {
        for (Tree member : classTree.getMembers()) {
            if (member instanceof VariableTree) {
                VariableTree variable = (VariableTree) member;

                // 检查@Resource和@Autowired使用
                analyzeDependencyInjection(variable, callerLayer, report);
            }
        }
    }

    private void analyzeDependencyInjection(VariableTree variable, LayerType callerLayer, ArchitectureViolationReport report) {
        // 检查注解
        for (AnnotationTree annotation : variable.getModifiers().getAnnotations()) {
            String annotationName = getAnnotationName(annotation);

            if ("Autowired".equals(annotationName)) {
                // ❌ 违规：使用了@Autowired
                report.addViolation(new ArchitectureViolation(
                    ViolationType.ILLEGAL_DEPENDENCY_INJECTION,
                    "禁止使用@Autowired，必须使用@Resource",
                    variable.toString(),
                    callerLayer
                ));
            }

            if ("Repository".equals(annotationName)) {
                // ❌ 违规：使用了@Repository注解
                report.addViolation(new ArchitectureViolation(
                    ViolationType.ILLEGAL_REPOSITORY_ANNOTATION,
                    "DAO接口必须使用@Mapper注解，禁止使用@Repository",
                    variable.toString(),
                    callerLayer
                ));
            }
        }
    }

    private LayerType determineLayerType(String className, ClassTree classTree) {
        // 根据类名和注解确定层级
        if (className.endsWith("Controller") || hasAnnotation(classTree, "RestController")) {
            return LayerType.CONTROLLER;
        } else if (className.endsWith("Service") || className.endsWith("ServiceImpl") || hasAnnotation(classTree, "Service")) {
            return LayerType.SERVICE;
        } else if (className.endsWith("Manager")) {
            return LayerType.MANAGER;
        } else if (className.endsWith("Dao") || hasAnnotation(classTree, "Mapper")) {
            return LayerType.DAO;
        }

        return LayerType.UNKNOWN;
    }

    /**
     * 修复架构违规
     */
    public void fixViolations(ArchitectureViolationReport report, String projectPath) {
        for (ArchitectureViolation violation : report.getViolations()) {
            try {
                switch (violation.getType()) {
                    case ILLEGAL_DEPENDENCY_INJECTION:
                        fixDependencyInjection(violation, projectPath);
                        break;
                    case ILLEGAL_REPOSITORY_ANNOTATION:
                        fixRepositoryAnnotation(violation, projectPath);
                        break;
                    case ILLEGAL_IMPORTS:
                        fixIllegalImports(violation, projectPath);
                        break;
                    default:
                        log.warn("未支持的违规类型修复: {}", violation.getType());
                }
            } catch (Exception e) {
                log.error("修复违规失败: {}", violation, e);
            }
        }
    }

    private void fixDependencyInjection(ArchitectureViolation violation, String projectPath) {
        // 将@Autowired替换为@Resource
        Path filePath = Paths.get(projectPath, violation.getFilePath());
        try {
            String content = Files.readString(filePath);
            content = content.replaceAll("@Autowired", "@Resource");
            Files.writeString(filePath, content);
            log.info("已修复依赖注入违规: {}", filePath);
        } catch (IOException e) {
            log.error("修复文件失败: {}", filePath, e);
        }
    }

    private void fixRepositoryAnnotation(ArchitectureViolation violation, String projectPath) {
        // 将@Repository替换为@Mapper，Repository后缀替换为Dao
        Path filePath = Paths.get(projectPath, violation.getFilePath());
        try {
            String content = Files.readString(filePath);

            // 替换注解
            content = content.replaceAll("@Repository", "@Mapper");

            // 替换接口名称（Repository -> Dao）
            content = content.replaceAll("(\\w+)Repository", "$1Dao");

            Files.writeString(filePath, content);
            log.info("已修复Repository注解违规: {}", filePath);
        } catch (IOException e) {
            log.error("修复文件失败: {}", filePath, e);
        }
    }
}
```

### Jakarta EE包名检查
```java
// Jakarta EE包名合规检查工具
@Component
@Slf4j
public class JakartaPackageChecker {

    private static final Map<String, String> JAKARTA_MAPPINGS = Map.of(
        "javax.annotation.Resource", "jakarta.annotation.Resource",
        "javax.validation.Valid", "jakarta.validation.Valid",
        "javax.validation.constraints", "jakarta.validation.constraints",
        "javax.transaction.Transactional", "jakarta.transaction.Transactional",
        "javax.servlet.http.HttpServletRequest", "jakarta.servlet.http.HttpServletRequest",
        "javax.servlet.http.HttpServletResponse", "jakarta.servlet.http.HttpServletResponse"
    );

    // MyBatis-Plus注解替换JPA注解
    private static final Map<String, String> JPA_TO_MYBATIS_MAPPINGS = Map.of(
        "jakarta.persistence.Entity", "@Data\n@TableName(\"table_name\")",
        "jakarta.persistence.Table", "@TableName(\"table_name\")",
        "jakarta.persistence.Column", "@TableField(\"column_name\")",
        "jakarta.persistence.Id", "@TableId(type = IdType.AUTO)",
        "jakarta.persistence.GeneratedValue", "@TableId(type = IdType.AUTO)",
        "jakarta.persistence.OneToOne", "@TableField",
        "jakarta.persistence.OneToMany", "@TableField",
        "jakarta.persistence.ManyToOne", "@TableField",
        "jakarta.persistence.ManyToMany", "@TableField"
    );

    /**
     * 检查Jakarta EE包名合规性
     */
    public JakartaPackageReport checkJakartaPackageCompliance(String projectPath) {
        JakartaPackageReport report = new JakartaPackageReport();

        List<File> javaFiles = scanJavaFiles(projectPath);

        for (File javaFile : javaFiles) {
            checkFileCompliance(javaFile, report);
        }

        return report;
    }

    private void checkFileCompliance(File javaFile, JakartaPackageReport report) {
        try {
            String content = Files.readString(javaFile.toPath());
            List<String> lines = Files.readAllLines(javaFile.toPath());

            // 检查import语句
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                if (line.startsWith("import javax.")) {
                    // 找到javax导入
                    String javaxImport = line.substring(7); // 移除"import "

                    if (JAKARTA_MAPPINGS.containsKey(javaxImport)) {
                        String jakartaImport = JAKARTA_MAPPINGS.get(javaxImport);

                        report.addViolation(new JakartaPackageViolation(
                            javaFile.getPath(),
                            i + 1, // 行号
                            javaxImport,
                            jakartaImport,
                            "需要将javax包名替换为jakarta包名"
                        ));
                    }
                }
            }

        } catch (IOException e) {
            log.warn("读取文件失败: {}", javaFile.getPath(), e);
        }
    }

    /**
     * 自动修复Jakarta EE包名问题
     */
    public void fixJakartaPackageIssues(JakartaPackageReport report, String projectPath) {
        Map<String, List<JakartaPackageViolation>> violationsByFile = report.getViolationsByFile();

        for (Map.Entry<String, List<JakartaPackageViolation>> entry : violationsByFile.entrySet()) {
            String filePath = entry.getKey();
            List<JakartaPackageViolation> violations = entry.getValue();

            try {
                Path path = Paths.get(projectPath, filePath);
                String content = Files.readString(path);

                // 替换所有违规的javax导入为jakarta导入
                for (JakartaPackageViolation violation : violations) {
                    content = content.replace(
                        "import " + violation.getjavaxPackage(),
                        "import " + violation.getJakartaPackage()
                    );
                }

                Files.writeString(path, content);
                log.info("已修复Jakarta包名问题: {} ({}个修复)", filePath, violations.size());

            } catch (IOException e) {
                log.error("修复Jakarta包名失败: {}", filePath, e);
            }
        }
    }
}
```

### 依赖注入规范检查
```java
// 依赖注入规范检查工具
@Component
@Slf4j
public class DependencyInjectionChecker {

    /**
     * 检查依赖注入规范
     */
    public DependencyInjectionReport checkDependencyInjection(String projectPath) {
        DependencyInjectionReport report = new DependencyInjectionReport();

        List<File> javaFiles = scanJavaFiles(projectPath);

        for (File javaFile : javaFiles) {
            checkFileDependencyInjection(javaFile, report);
        }

        return report;
    }

    private void checkFileDependencyInjection(File javaFile, DependencyInjectionReport report) {
        try {
            List<String> lines = Files.readAllLines(javaFile.toPath());

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();

                // 检查@Autowired使用
                if (line.contains("@Autowired")) {
                    report.addViolation(new DependencyInjectionViolation(
                        javaFile.getPath(),
                        i + 1,
                        "@Autowired",
                        "必须使用@Resource注解替换@Autowired",
                        ViolationSeverity.HIGH
                    ));
                }

                // 检查构造函数注入
                if (line.contains("public") && line.contains("(") && line.contains(")") &&
                    !line.contains("Controller")) { // Controller允许构造函数注入

                    // 检查是否包含依赖注入注解
                    int nextFewLines = Math.min(i + 5, lines.size());
                    for (int j = i; j < nextFewLines; j++) {
                        String followingLine = lines.get(j).trim();
                        if (followingLine.contains("@Resource") || followingLine.contains("@Autowired")) {
                            // 找到了依赖注入，可能是构造函数注入
                            if (followingLine.contains("@Autowired")) {
                                report.addViolation(new DependencyInjectionViolation(
                                    javaFile.getPath(),
                                    j + 1,
                                    "构造函数注入使用@Autowired",
                                    "必须使用@Resource注解或字段注入",
                                    ViolationSeverity.MEDIUM
                                ));
                            }
                            break;
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.warn("读取文件失败: {}", javaFile.getPath(), e);
        }
    }

    /**
     * 修复依赖注入问题
     */
    public void fixDependencyInjectionIssues(DependencyInjectionReport report, String projectPath) {
        for (DependencyInjectionViolation violation : report.getViolations()) {
            try {
                Path filePath = Paths.get(projectPath, violation.getFilePath());
                String content = Files.readString(filePath);

                // 替换@Autowired为@Resource
                content = content.replace("@Autowired", "@Resource");

                Files.writeString(filePath, content);
                log.info("已修复依赖注入问题: {} - 行{}", violation.getFilePath(), violation.getLineNumber());

            } catch (IOException e) {
                log.error("修复依赖注入失败: {}", violation.getFilePath(), e);
            }
        }
    }
}
```

---

## 📊 架构质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **四层架构合规率** | 100% | 代码符合四层架构规范比例 | 架构合规检查 |
| **依赖注入规范率** | 100% | 使用@Resource注解的比例 | 依赖注入检查 |
| **DAO层规范率** | 100% | DAO使用@Mapper和Dao后缀比例 | DAO规范检查 |
| **Jakarta EE合规率** | 100% | 使用jakarta包名的比例 | 包名合规检查 |
| **跨层访问违规数** | 0 | 跨层直接访问违规数量 | 跨层访问检查 |

### 架构健康指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **循环依赖检测** | 0 | 循环依赖问题数量 | 循环依赖检查 |
| **事务管理规范率** | ≥95% | 事务注解使用规范比例 | 事务管理检查 |
| **异常处理覆盖率** | ≥90% | 异常处理机制覆盖比例 | 异常处理检查 |
| **接口设计规范率** | ≥95% | REST接口设计规范比例 | 接口设计检查 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新架构守护技能

---

## 🛠️ 开发规范和最佳实践

### 项目结构规范（重要更新 2025-01-15）

**统一业务微服务包结构**:
```
net.lab1024.sa.{service}/
├── config/                   # 配置类
│   ├── DatabaseConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/              # REST控制器
│   ├── {Module}Controller.java
│   └── support/             # 支撑控制器
├── service/                 # 服务接口和实现
│   ├── {Module}Service.java
│   └── impl/
│       └── {Module}ServiceImpl.java
├── manager/                 # 业务编排层
│   ├── {Module}Manager.java
│   └── impl/
│       └── {Module}ManagerImpl.java
├── dao/                     # 数据访问层
│   ├── {Module}Dao.java
│   └── custom/              # 自定义查询
├── domain/                  # 领域对象
│   ├── form/               # 请求表单
│   │   ├── {Module}AddForm.java
│   │   ├── {Module}UpdateForm.java
│   │   └── {Module}QueryForm.java
│   └── vo/                 # 响应视图
│       ├── {Module}VO.java
│       ├── {Module}DetailVO.java
│       └── {Module}ListVO.java
└── {Service}Application.java
```

**严格禁止事项**:
- ❌ **禁止重复包名**: 如`access.access.entity`、`consume.consume.entity`等冗余命名
- ❌ **禁止Entity分散存储**: 所有Entity必须统一在公共模块管理
- ❌ **禁止Manager使用Spring注解**: Manager必须是纯Java类，使用构造函数注入
- ❌ **禁止包结构不统一**: 所有微服务必须遵循统一的包结构规范

**相关技能**:
- 📦 [Package Structure Guardian](package-structure-guardian.md) - 包目录结构守护专家
- 🔧 [自动化工具](../../../scripts/fix-package-structure.ps1) - 包结构修复脚本
- 🔍 [检查工具](../../../scripts/check-package-structure.ps1) - 包结构检查脚本

### 命名规范
- **Controller**: `XxxController`
- **Service接口**: `XxxService`
- **Service实现**: `XxxServiceImpl`
- **Manager**: `XxxManager` (纯Java类)
- **DAO**: `XxxDao` (@Mapper注解)
- **Entity**: `XxxEntity`

### 依赖注入规范
```java
// ✅ 正确示例
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserManager userManager;  // 统一使用@Resource
}

// ❌ 错误示例
@Service
public class UserServiceImpl implements UserService {
    @Autowired  // 禁止使用
    private UserManager userManager;
}
```

### DAO层规范
```java
// ✅ 正确示例
@Mapper  // 必须使用@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // DAO方法
}

// ❌ 错误示例
@Repository  // 禁止使用@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // JPA方式被禁止
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 技术规范文档
- **Spring Boot 3.5.8**: 微服务框架文档
- **Jakarta EE 3.0**: Jakarta EE包名规范文档
- **MyBatis-Plus**: ORM框架文档
- **Spring Framework**: 依赖注入和事务管理文档

### 质量保障文档
- **📊 代码质量规范**: 代码编写和审查规范
- **🔍 架构审查清单**: 架构合规性检查清单
- **🛠️ 重构最佳实践**: 代码重构指导原则

---

**📋 重要提醒**:
1. 本技能严格守护IOE-DREAM四层架构规范
2. 所有代码必须使用Jakarta EE 3.0+包名规范
3. 统一使用@Resource依赖注入，严格禁止@Autowired
4. 统一使用@Mapper注解和Dao后缀命名
5. Manager层必须为纯Java类，禁止使用Spring注解
6. 严格防止跨层访问和架构违规
7. 定期进行架构合规性检查和修复

**让我们一起建设规范、优质的四层架构体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + Jakarta EE 3.0 + MyBatis-Plus