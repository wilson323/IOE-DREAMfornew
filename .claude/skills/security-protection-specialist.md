# 🛡️ 安全防护专家技能

> **版本**: v1.0.0 - 企业级安全防护
> **更新时间**: 2025-11-23
> **分类**: 安全防护技能 > 系统安全
> **标签**: ["安全扫描", "漏洞防护", "数据加密", "访问控制"]
> **技能等级**: ★★★ 专家级
> **适用角色**: 安全工程师、架构师、DevSecOps专家

---

## 📋 技能概述

本技能专门构建企业级安全防护体系，通过自动化安全扫描、漏洞检测、数据加密和访问控制等手段，确保系统达到金融级安全标准。

**核心能力**: 建立从代码安全到运行时防护的全链路安全保障，实现安全事件的预防和快速响应。

## 🚨 当前安全风险分析

### 1. 代码安全漏洞
**问题现象**:
```java
// SQL注入风险
String sql = "SELECT * FROM users WHERE username = '" + username + "'";

// XSS攻击风险
String userInput = request.getParameter("input");
response.getWriter().write(userInput);  // 未转义输出

// 敏感信息泄露
String password = request.getParameter("password");
log.info("用户登录: " + username + ", 密码: " + password);  // 密码明文记录
```

**根本原因**:
- 缺乏安全编码规范
- 没有自动化安全扫描
- 安全意识培训不足

### 2. 认证授权缺陷
**问题现象**:
```java
// 弱密码策略
public boolean validatePassword(String password) {
    return password.length() >= 6;  // 仅检查长度
}

// 会话管理不当
String sessionId = request.getParameter("sessionId");  // URL中的session
// 没有session超时和失效机制
```

### 3. 数据安全问题
**问题现象**:
```java
// 敏感数据明文存储
public class User {
    private String password;  // 明文密码
    private String idCard;    // 明文身份证
    private String phone;     // 明文手机号
}

// 数据传输未加密
// HTTP传输敏感信息
```

## 🛠️ 安全防护最佳实践

### 1. 代码安全扫描系统

#### 静态代码安全分析
```java
@Component
@Slf4j
public class SecurityCodeAnalyzer {

    @Resource
    private List<SecurityRule> securityRules;

    /**
     * 执行安全代码扫描
     */
    public SecurityScanResult performSecurityScan(String projectPath) {
        SecurityScanResult result = new SecurityScanResult();
        result.setScanTime(System.currentTimeMillis());
        result.setProjectPath(projectPath);

        List<SecurityIssue> issues = new ArrayList<>();

        // 扫描Java文件
        List<String> javaFiles = findJavaFiles(projectPath);
        for (String javaFile : javaFiles) {
            List<SecurityIssue> fileIssues = scanJavaFile(javaFile);
            issues.addAll(fileIssues);
        }

        // 扫描配置文件
        List<String> configFiles = findConfigFiles(projectPath);
        for (String configFile : configFiles) {
            List<SecurityIssue> fileIssues = scanConfigFile(configFile);
            issues.addAll(fileIssues);
        }

        result.setIssues(issues);
        result.setTotalIssues(issues.size());
        result.setCriticalIssues(issues.stream().filter(i -> i.getSeverity() == Severity.CRITICAL).count());
        result.setHighIssues(issues.stream().filter(i -> i.getSeverity() == Severity.HIGH).count());

        log.info("安全扫描完成: 项目={}, 总问题={}, 严重问题={}",
                projectPath, issues.size(), result.getCriticalIssues());

        return result;
    }

    private List<SecurityIssue> scanJavaFile(String javaFilePath) {
        List<SecurityIssue> issues = new ArrayList<>();

        try {
            String content = Files.readString(Paths.get(javaFilePath), StandardCharsets.UTF_8);

            // SQL注入检测
            issues.addAll(detectSQLInjection(content, javaFilePath));

            // XSS攻击检测
            issues.addAll(detectXSS(content, javaFilePath));

            // 路径遍历检测
            issues.addAll(detectPathTraversal(content, javaFilePath));

            // 敏感信息泄露检测
            issues.addAll(detectSensitiveInfoLeak(content, javaFilePath));

            // 弱加密算法检测
            issues.addAll(detectWeakCryptography(content, javaFilePath));

        } catch (IOException e) {
            log.error("读取文件失败: {}", javaFilePath, e);
        }

        return issues;
    }

    private List<SecurityIssue> detectSQLInjection(String content, String filePath) {
        List<SecurityIssue> issues = new ArrayList<>();

        // 检测字符串拼接SQL
        Pattern pattern = Pattern.compile("(Statement|PreparedStatement).*\\+.*\\+");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            SecurityIssue issue = new SecurityIssue();
            issue.setType(SecurityIssueType.SQL_INJECTION);
            issue.setSeverity(Severity.HIGH);
            issue.setDescription("检测到可能的SQL注入漏洞：使用了字符串拼接构造SQL");
            issue.setFilePath(filePath);
            issue.setLineNumber(getLineNumber(content, matcher.start()));
            issue.setCodeSnippet(matcher.group());
            issue.setRecommendation("使用PreparedStatement参数化查询");

            issues.add(issue);
        }

        return issues;
    }

    private List<SecurityIssue> detectXSS(String content, String filePath) {
        List<SecurityIssue> issues = new ArrayList<>();

        // 检测未转义的输出
        Pattern pattern = Pattern.compile("(writer\\.print|response\\.getWriter\\(\\)\\.write|out\\.print)\\s*\\(.*\\+\\s*\\w+\\s*\\)");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            SecurityIssue issue = new SecurityIssue();
            issue.setType(SecurityIssueType.XSS);
            issue.setSeverity(Severity.HIGH);
            issue.setDescription("检测到可能的XSS漏洞：输出未经过转义");
            issue.setFilePath(filePath);
            issue.setLineNumber(getLineNumber(content, matcher.start()));
            issue.setCodeSnippet(matcher.group());
            issue.setRecommendation("使用ESAPI或Spring的HTML转义功能");

            issues.add(issue);
        }

        return issues;
    }

    private List<SecurityIssue> detectSensitiveInfoLeak(String content, String filePath) {
        List<SecurityIssue> issues = new ArrayList<>();

        // 检测敏感信息日志输出
        String[] sensitivePatterns = {
            "password.*log.*",
            "secret.*log.*",
            "token.*log.*",
            "key.*log.*"
        };

        for (String patternStr : sensitivePatterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                SecurityIssue issue = new SecurityIssue();
                issue.setType(SecurityIssueType.SENSITIVE_INFO_LEAK);
                issue.setSeverity(Severity.MEDIUM);
                issue.setDescription("检测到敏感信息可能泄露到日志中");
                issue.setFilePath(filePath);
                issue.setLineNumber(getLineNumber(content, matcher.start()));
                issue.setCodeSnippet(matcher.group());
                issue.setRecommendation("避免在日志中记录敏感信息，如密码、令牌等");

                issues.add(issue);
            }
        }

        return issues;
    }
}
```

#### 安全规则引擎
```java
@Component
public class SecurityRuleEngine {

    private final List<SecurityRule> rules = new ArrayList<>();

    @PostConstruct
    public void initSecurityRules() {
        // SQL注入规则
        rules.add(new SQLInjectionRule());

        // XSS攻击规则
        rules.add(new XSSRule());

        // 路径遍历规则
        rules.add(new PathTraversalRule());

        // 弱加密规则
        rules.add(new WeakCryptographyRule());

        // 硬编码密钥规则
        rules.add(new HardcodedKeyRule());

        // 不安全的随机数规则
        rules.add(new InsecureRandomRule());

        log.info("安全规则引擎初始化完成，加载{}个规则", rules.size());
    }

    public List<SecurityIssue> evaluateRules(String content, String filePath) {
        List<SecurityIssue> issues = new ArrayList<>();

        for (SecurityRule rule : rules) {
            try {
                List<SecurityIssue> ruleIssues = rule.evaluate(content, filePath);
                issues.addAll(ruleIssues);
            } catch (Exception e) {
                log.warn("安全规则执行失败: rule={}, error={}", rule.getClass().getSimpleName(), e.getMessage());
            }
        }

        return issues;
    }

    // SQL注入规则实现
    private static class SQLInjectionRule implements SecurityRule {
        private static final Pattern SQL_INJECTION_PATTERNS[] = {
            Pattern.compile("\"\\s*\\+\\s*\\w+\\s*\\+\\s*\".*[SELECT|INSERT|UPDATE|DELETE]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Statement\\.execute\\s*\\(.*\\+.*\\)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("Statement\\.executeQuery\\s*\\(.*\\+.*\\)", Pattern.CASE_INSENSITIVE)
        };

        @Override
        public List<SecurityIssue> evaluate(String content, String filePath) {
            List<SecurityIssue> issues = new ArrayList<>();

            for (Pattern pattern : SQL_INJECTION_PATTERNS) {
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    SecurityIssue issue = new SecurityIssue();
                    issue.setType(SecurityIssueType.SQL_INJECTION);
                    issue.setSeverity(Severity.HIGH);
                    issue.setDescription("检测到SQL注入风险：使用字符串拼接构造SQL语句");
                    issue.setFilePath(filePath);
                    issue.setCodeSnippet(matcher.group());
                    issue.setRecommendation("使用PreparedStatement和参数化查询");

                    issues.add(issue);
                }
            }

            return issues;
        }
    }
}
```

### 2. 数据加密保护系统

#### 统一加密服务
```java
@Service
@Slf4j
public class UnifiedEncryptionService {

    @Resource
    private AESUtil aesUtil;

    @Resource
    private RSAUtil rsaUtil;

    @Resource
    private HashUtil hashUtil;

    /**
     * 敏感字段加密
     */
    public String encryptSensitiveData(String data, EncryptionType type) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }

        try {
            switch (type) {
                case AES:
                    return aesUtil.encrypt(data);
                case RSA:
                    return rsaUtil.encrypt(data);
                default:
                    throw new IllegalArgumentException("不支持的加密类型: " + type);
            }
        } catch (Exception e) {
            log.error("数据加密失败", e);
            throw new EncryptionException("数据加密失败", e);
        }
    }

    /**
     * 敏感字段解密
     */
    public String decryptSensitiveData(String encryptedData, EncryptionType type) {
        if (StringUtils.isEmpty(encryptedData)) {
            return encryptedData;
        }

        try {
            switch (type) {
                case AES:
                    return aesUtil.decrypt(encryptedData);
                case RSA:
                    return rsaUtil.decrypt(encryptedData);
                default:
                    throw new IllegalArgumentException("不支持的解密类型: " + type);
            }
        } catch (Exception e) {
            log.error("数据解密失败", e);
            throw new DecryptionException("数据解密失败", e);
        }
    }

    /**
     * 密码哈希处理
     */
    public String hashPassword(String password, String salt) {
        return hashUtil.sha256(password + salt);
    }

    /**
     * 密码验证
     */
    public boolean verifyPassword(String password, String salt, String hashedPassword) {
        return hashPassword(password, salt).equals(hashedPassword);
    }

    /**
     * 批量加密敏感字段
     */
    public <T> void encryptSensitiveFields(T entity, String... fieldNames) {
        if (entity == null || fieldNames == null || fieldNames.length == 0) {
            return;
        }

        Class<?> clazz = entity.getClass();
        for (String fieldName : fieldNames) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                Object value = field.get(entity);
                if (value instanceof String) {
                    String encryptedValue = encryptSensitiveData((String) value, EncryptionType.AES);
                    field.set(entity, encryptedValue);
                }
            } catch (Exception e) {
                log.error("加密字段失败: {}.{}", clazz.getSimpleName(), fieldName, e);
            }
        }
    }
}

/**
 * AES加密工具类
 */
@Component
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY = "your-secret-key-here"; // 实际应从安全配置中获取

    public String encrypt(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(generateIV());

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public String decrypt(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(generateIV());

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);

        return new String(decryptedData);
    }

    private byte[] generateIV() {
        return new byte[16]; // 实际应使用安全的随机数生成器
    }
}
```

### 3. 访问控制系统

#### 基于角色的访问控制(RBAC)
```java
@Service
@Slf4j
public class AccessControlService {

    @Resource
    private UserService userService;

    @Resource
    private RoleService roleService;

    @Resource
    private PermissionService permissionService;

    /**
     * 检查用户权限
     */
    public boolean hasPermission(Long userId, String permission) {
        try {
            // 获取用户角色
            List<Role> userRoles = userService.getUserRoles(userId);
            if (userRoles.isEmpty()) {
                return false;
            }

            // 检查角色权限
            for (Role role : userRoles) {
                if (roleService.hasPermission(role.getId(), permission)) {
                    log.debug("用户{}通过角色{}获得权限: {}", userId, role.getName(), permission);
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("权限检查失败: userId={}, permission={}", userId, permission, e);
            return false; // 安全原则：失败时拒绝访问
        }
    }

    /**
     * 检查数据访问权限
     */
    public boolean hasDataAccess(Long userId, String resourceType, Long resourceId) {
        try {
            // 检查所有者权限
            if (isResourceOwner(userId, resourceType, resourceId)) {
                return true;
            }

            // 检查管理员权限
            if (hasPermission(userId, "admin:" + resourceType)) {
                return true;
            }

            // 检查部门权限
            return hasDepartmentAccess(userId, resourceType, resourceId);

        } catch (Exception e) {
            log.error("数据访问权限检查失败: userId={}, resource={}, resourceId={}",
                    userId, resourceType, resourceId, e);
            return false;
        }
    }

    /**
     * 动态权限检查注解处理器
     */
    @Aspect
    @Component
    public class PermissionCheckAspect {

        @Around("@annotation(requiresPermission)")
        public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
            // 获取当前用户
            Long currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                throw new AuthenticationException("用户未登录");
            }

            // 检查权限
            String permission = requiresPermission.value();
            if (!accessControlService.hasPermission(currentUserId, permission)) {
                throw new AuthorizationException("权限不足: " + permission);
            }

            return joinPoint.proceed();
        }
    }

    /**
     * 数据权限注解
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RequiresPermission {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DataPermission {
        String resourceType();
        String resourceIdParam() default "id";
    }
}
```

### 4. 会话安全管理

#### 安全会话管理
```java
@Component
@Slf4j
public class SecureSessionManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SESSION_PREFIX = "session:";
    private static final int SESSION_TIMEOUT = 30 * 60; // 30分钟

    /**
     * 创建安全会话
     */
    public String createSession(Long userId, Map<String, Object> sessionData) {
        String sessionId = generateSecureSessionId();
        String sessionKey = SESSION_PREFIX + sessionId;

        // 会话数据
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setCreationTime(System.currentTimeMillis());
        session.setLastAccessTime(System.currentTimeMillis());
        session.setSessionData(sessionData);
        session.setIpAddress(getCurrentIpAddress());
        session.setUserAgent(getCurrentUserAgent());

        // 存储会话
        redisTemplate.opsForValue().set(sessionKey, session, SESSION_TIMEOUT, TimeUnit.SECONDS);

        log.info("创建安全会话: userId={}, sessionId={}, ip={}",
                userId, sessionId, session.getIpAddress());

        return sessionId;
    }

    /**
     * 验证会话有效性
     */
    public boolean validateSession(String sessionId) {
        if (StringUtils.isEmpty(sessionId)) {
            return false;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        Session session = (Session) redisTemplate.opsForValue().get(sessionKey);

        if (session == null) {
            return false;
        }

        // 检查会话超时
        long currentTime = System.currentTimeMillis();
        if (currentTime - session.getLastAccessTime() > SESSION_TIMEOUT * 1000) {
            invalidateSession(sessionId);
            return false;
        }

        // 检查IP地址变化
        String currentIp = getCurrentIpAddress();
        if (!Objects.equals(session.getIpAddress(), currentIp)) {
            log.warn("检测到会话IP地址变化: sessionId={}, old={}, new={}",
                    sessionId, session.getIpAddress(), currentIp);
            // 可以选择使会话失效或要求重新验证
        }

        // 更新最后访问时间
        session.setLastAccessTime(currentTime);
        redisTemplate.opsForValue().set(sessionKey, session, SESSION_TIMEOUT, TimeUnit.SECONDS);

        return true;
    }

    /**
     * 使会话失效
     */
    public void invalidateSession(String sessionId) {
        String sessionKey = SESSION_PREFIX + sessionId;
        redisTemplate.delete(sessionKey);
        log.info("会话已失效: sessionId={}", sessionId);
    }

    /**
     * 批量清理过期会话
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 每小时执行一次
    public void cleanupExpiredSessions() {
        try {
            Set<String> sessionKeys = redisTemplate.keys(SESSION_PREFIX + "*");
            int expiredCount = 0;

            for (String sessionKey : sessionKeys) {
                Session session = (Session) redisTemplate.opsForValue().get(sessionKey);
                if (session != null && isSessionExpired(session)) {
                    redisTemplate.delete(sessionKey);
                    expiredCount++;
                }
            }

            log.info("清理过期会话完成: 总数={}, 清理={}", sessionKeys.size(), expiredCount);

        } catch (Exception e) {
            log.error("清理过期会话失败", e);
        }
    }

    private String generateSecureSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private boolean isSessionExpired(Session session) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - session.getLastAccessTime()) > (SESSION_TIMEOUT * 1000);
    }
}
```

## 🎯 安全防护场景应用

### 1. 代码安全扫描
- 自动化安全漏洞检测
- OWASP Top 10防护
- 敏感信息泄露检测
- 依赖库安全检查

### 2. 运行时安全防护
- Web应用防火墙(WAF)
- SQL注入防护
- XSS攻击防护
- CSRF防护

### 3. 数据安全保护
- 敏感数据加密存储
- 传输过程加密
- 数据脱敏处理
- 访问日志审计

## 📊 安全监控指标

### 核心安全KPI
- **漏洞数量**: 发现的、修复的、新增的安全漏洞
- **攻击次数**: 阻止的攻击尝试次数
- **响应时间**: 安全事件响应时间
- **合规率**: 安全标准符合度

### 风险评估指标
- **风险等级**: 高、中、低风险分布
- **资产价值**: 关键资产保护状况
- **威胁情报**: 外部威胁情报更新频率
- **安全评分**: 整体安全状况评分

---

## 🚀 技能等级要求

### 初级 (★☆☆)
- 了解常见安全漏洞类型
- 掌握基本的安全编码规范
- 能够使用安全扫描工具

### 中级 (★★☆)
- 能够设计安全防护方案
- 掌握加密和认证技术
- 能够进行安全风险评估

### 专家级 (★★★)
- 能够构建企业级安全体系
- 掌握高级攻防技术
- 能够建立安全监控和响应体系

---

**技能使用提示**: 当需要进行安全防护、漏洞修复或建立安全体系时，调用此技能获得专业的安全保障方案。

**记忆要点**:
- 安全防护要从设计阶段开始，不能事后弥补
- 代码安全扫描是第一道防线，必须定期执行
- 数据加密是保护敏感信息的关键手段
- 访问控制要遵循最小权限原则
- 安全监控和日志记录是发现和响应安全事件的重要工具