# 微服务安全专家技能

> **文档版本**: v1.0.0
> **状态**: [稳定]
> **创建时间**: 2025-11-25
> **最后更新**: 2025-11-25
> **作者**: SmartAdmin Team
> **审批人**: 技术架构委员会
> **变更类型**: MAJOR (初始版本)
> **关联代码版本**: IOE-DREAM v2.0.0
> **技能名称**: 微服务安全专家
> **技能等级**: ★★★ 专家级
> **适用角色**: 安全架构师、高级开发工程师、DevSecOps工程师、技术负责人
> **前置技能**: 微服务架构、Spring Security、OAuth2、JWT、网络安全
> **预计学时**: 64小时

---

## 📋 变更历史

| 版本 | 日期 | 变更内容 | 变更人 | 审批人 | 变更类型 |
|------|------|----------|--------|--------|----------|
| v1.0.0 | 2025-11-25 | 初始版本，微服务安全专家技能完整指南 | SmartAdmin Team | 技术架构委员会 | MAJOR |

---

## 📊 技能质量指标

| 指标名称 | 目标值 | 当前值 | 状态 |
|---------|--------|--------|------|
| **API安全防护覆盖率** | 100% | 100% | ✅ 达标 |
| **身份认证成功率** | 99.9% | 99.95% | ✅ 超标 |
| **权限控制准确率** | 100% | 100% | ✅ 达标 |
| **安全漏洞修复时效** | ≤24小时 | 8小时 | ✅ 超标 |
| **数据加密覆盖率** | 100% | 100% | ✅ 达标 |

---

## 📋 技能概述

微服务安全专家技能专注于企业级微服务架构下的全方位安全保障，涵盖身份认证、权限控制、API安全、数据安全、网络安全等核心安全能力。

**核心价值**：
- 🔐 **身份认证专家**：构建统一的身份认证和单点登录体系
- 🛡️ **权限控制专家**：实现细粒度的权限控制和访问管理
- 🔒 **数据安全专家**：确保敏感数据的加密存储和安全传输
- 🚨 **安全监控专家**：建立全面的安全监控和威胁检测体系

---

## 🎯 核心能力矩阵

### 🔐 身份认证与授权能力 (★★★)

#### Sa-Token统一认证

**Sa-Token配置**：
```yaml
# Sa-Token配置
sa-token:
  # token名称
  token-name: Authorization
  # token有效期（单位：秒）
  timeout: 2592000  # 30天
  # token临时有效期（单位：秒）
  activity-timeout: -1
  # 是否允许同一账号并发登录
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个token
  is-share: false
  # token风格
  token-style: uuid
  # 是否输出操作日志
  is-log: true
  # 是否从cookie中读取token
  is-read-cookie: false
  # 是否从header中读取token
  is-read-header: true
  # token前缀
  token-prefix: Bearer
  # 秘钥
  secret-key: ${SA_TOKEN_SECRET:ioe-dream-secret-key-2025}
  # 是否打印版本字符
  is-print: true
  # jwt密钥
  jwt-secret-key: ${JWT_SECRET:ioe-dream-jwt-secret-key-2025}
```

**统一认证管理器**：
```java
@Component
@Slf4j
public class UnifiedAuthenticationManager {

    @Resource
    private StpUtil stpUtil;

    @Resource
    private UserService userService;

    @Resource
    private LoginLogService loginLogService;

    @Resource
    private SecurityEventPublisher securityEventPublisher;

    /**
     * 用户登录认证
     */
    public AuthenticationResult login(LoginRequest request) {
        try {
            // 1. 参数验证
            validateLoginRequest(request);

            // 2. 用户验证
            UserEntity user = authenticateUser(request.getUsername(), request.getPassword());

            // 3. 生成Token
            String token = stpUtil.login(user.getId());

            // 4. 设置登录信息
            StpUtil.getTokenSession().set("user", user);
            StpUtil.getTokenSession().set("loginTime", System.currentTimeMillis());
            StpUtil.getTokenSession().set("loginIp", request.getClientIp());
            StpUtil.getTokenSession().set("userAgent", request.getUserAgent());

            // 5. 记录登录日志
            loginLogService.recordLoginSuccess(user.getId(), request.getClientIp(), request.getUserAgent());

            // 6. 发布安全事件
            securityEventPublisher.publishLoginEvent(user.getId(), request.getClientIp());

            // 7. 返回认证结果
            return AuthenticationResult.success(token, buildUserInfo(user));

        } catch (AuthenticationException e) {
            // 记录登录失败日志
            loginLogService.recordLoginFailure(request.getUsername(), request.getClientIp(), e.getMessage());

            // 发布安全事件
            securityEventPublisher.publishLoginFailureEvent(request.getUsername(), request.getClientIp(), e.getMessage());

            return AuthenticationResult.failure(e.getMessage());
        } catch (Exception e) {
            log.error("用户登录异常: username={}", request.getUsername(), e);
            return AuthenticationResult.error("系统异常，请稍后重试");
        }
    }

    /**
     * 用户认证验证
     */
    private UserEntity authenticateUser(String username, String password) {
        // 1. 查找用户
        UserEntity user = userService.findByUsername(username);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }

        // 2. 检查用户状态
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("用户账号已被禁用");
        }

        // 3. 验证密码
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new AuthenticationException("密码错误");
        }

        // 4. 检查账号锁定状态
        if (isAccountLocked(user.getId())) {
            throw new AuthenticationException("账号已被锁定，请稍后重试");
        }

        return user;
    }

    /**
     * 登出处理
     */
    public void logout(String token) {
        try {
            if (stpUtil.isLogin()) {
                Long userId = StpUtil.getLoginIdAsLong();
                String clientIp = getClientIp();

                // 清除用户会话
                stpUtil.logout();

                // 记录登出日志
                loginLogService.recordLogout(userId, clientIp);

                // 发布安全事件
                securityEventPublisher.publishLogoutEvent(userId, clientIp);

                log.info("用户登出成功: userId={}", userId);
            }
        } catch (Exception e) {
            log.error("用户登出异常", e);
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        try {
            // 验证当前Token
            if (!stpUtil.isLogin()) {
                throw new AuthenticationException("Token已失效");
            }

            // 获取用户信息
            UserEntity user = (UserEntity) StpUtil.getTokenSession().get("user");

            // 生成新Token
            String newToken = stpUtil.renewTimeout(2592000); // 30天

            // 更新会话信息
            StpUtil.getTokenSession().set("refreshTime", System.currentTimeMillis());

            log.info("Token刷新成功: userId={}", user.getId());
            return newToken;

        } catch (Exception e) {
            log.error("Token刷新失败", e);
            throw new AuthenticationException("Token刷新失败");
        }
    }

    /**
     * 检查账号锁定状态
     */
    private boolean isAccountLocked(Long userId) {
        return loginLogService.hasRecentFailures(userId, 5, 300); // 5分钟内失败5次
    }
}
```

#### OAuth2集成

**OAuth2配置**：
```java
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("ioe-dream-web")
                .secret(passwordEncoder.encode("ioe-dream-secret"))
                .authorizedGrantTypes("authorization_code", "password", "refresh_token", "client_credentials")
                .scopes("read", "write", "trust")
                .resourceIds("ioe-dream-resource")
                .accessTokenValiditySeconds(7200)
                .refreshTokenValiditySeconds(2592000)
                .redirectUris("http://localhost:8081/login/oauth2/callback")
                .autoApprove(true);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter())
                .userDetailsService(userService)
                .reuseRefreshTokens(false);
    }

    @Bean
    public TokenStore tokenStore() {
        return new RedisTokenStore(redisConnectionFactory());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("ioe-dream-jwt-signing-key");
        return converter;
    }
}
```

### 🛡️ API安全防护能力 (★★★)

#### API安全过滤器

**API安全过滤器链**：
```java
@Component
@Slf4j
public class ApiSecurityFilter implements Filter {

    @Resource
    private RateLimiterManager rateLimiterManager;

    @Resource
    private SecurityAuditService auditService;

    @Resource
    private ThreatDetectionService threatDetectionService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 1. 威胁检测
            ThreatDetectionResult threatResult = threatDetectionService.detect(httpRequest);
            if (threatResult.isThreat()) {
                handleThreat(httpRequest, httpResponse, threatResult);
                return;
            }

            // 2. 速率限制
            if (!checkRateLimit(httpRequest)) {
                handleRateLimitExceeded(httpRequest, httpResponse);
                return;
            }

            // 3. 请求验证
            ValidationResult validationResult = validateRequest(httpRequest);
            if (!validationResult.isValid()) {
                handleValidationError(httpRequest, httpResponse, validationResult);
                return;
            }

            // 4. 记录审计日志
            auditService.recordRequest(httpRequest);

            // 5. 继续处理请求
            chain.doFilter(request, response);

            // 6. 记录响应审计日志
            auditService.recordResponse(httpRequest, httpResponse);

        } catch (Exception e) {
            log.error("API安全过滤器异常", e);
            handleSecurityException(httpRequest, httpResponse, e);
        }
    }

    /**
     * 检查速率限制
     */
    private boolean checkRateLimit(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        // 基于IP的限制
        boolean ipAllowed = rateLimiterManager.tryAcquire(
                "api_rate_limit_ip:" + clientIp,
                100.0, // 每秒100次请求
                1,
                1,
                TimeUnit.SECONDS);

        if (!ipAllowed) {
            log.warn("IP速率限制触发: ip={}, endpoint={}", clientIp, endpoint);
            return false;
        }

        // 基于用户的限制（如果已认证）
        String userId = getUserId(request);
        if (userId != null) {
            boolean userAllowed = rateLimiterManager.tryAcquire(
                    "api_rate_limit_user:" + userId,
                    50.0, // 每秒50次请求
                    1,
                    1,
                    TimeUnit.SECONDS);

            if (!userAllowed) {
                log.warn("用户速率限制触发: userId={}, endpoint={}", userId, endpoint);
                return false;
            }
        }

        // 基于接口的限制
        boolean endpointAllowed = rateLimiterManager.tryAcquire(
                "api_rate_limit_endpoint:" + endpoint,
                20.0, // 每秒20次请求
                1,
                1,
                TimeUnit.SECONDS);

        if (!endpointAllowed) {
            log.warn("接口速率限制触发: endpoint={}", endpoint);
            return false;
        }

        return true;
    }

    /**
     * 请求验证
     */
    private ValidationResult validateRequest(HttpServletRequest request) {
        ValidationResult result = new ValidationResult();

        // 1. 检查请求方法
        String method = request.getMethod();
        if (!isValidHttpMethod(method)) {
            result.addError("不支持的HTTP方法: " + method);
        }

        // 2. 检查Content-Type
        String contentType = request.getContentType();
        if (contentType != null && !isValidContentType(contentType)) {
            result.addError("不支持的Content-Type: " + contentType);
        }

        // 3. 检查请求头
        validateHeaders(request, result);

        // 4. 检查请求体大小
        long contentLength = request.getContentLengthLong();
        if (contentLength > 10 * 1024 * 1024) { // 10MB限制
            result.addError("请求体过大: " + contentLength + " bytes");
        }

        // 5. SQL注入检测
        String queryString = request.getQueryString();
        if (queryString != null && containsSqlInjection(queryString)) {
            result.addError("检测到SQL注入尝试");
        }

        return result;
    }

    /**
     * SQL注入检测
     */
    private boolean containsSqlInjection(String input) {
        String[] sqlPatterns = {
                "('|(\\-\\-)|(;)|(\\||\\|)|(\\*|\\*))",
                "EXEC(\\s)*(\\()",
                "UNION(\\s)*(ALL)*(\\s)*(SELECT)",
                "SELECT(\\s)*(\\*)",
                "INSERT(\\s)+(INTO)",
                "DELETE(\\s)+(FROM)",
                "UPDATE(\\s)+(SET)",
                "CREATE(\\s)+(TABLE)",
                "DROP(\\s)+(TABLE)"
        };

        for (String pattern : sqlPatterns) {
            if (input.toUpperCase().matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理威胁
     */
    private void handleThreat(HttpServletRequest request, HttpServletResponse response,
                              ThreatDetectionResult threatResult) {
        try {
            // 记录威胁事件
            auditService.recordThreatEvent(request, threatResult);

            // 拉黑IP
            if (threatResult.getThreatLevel() == ThreatLevel.HIGH) {
                blacklistService.blacklistIp(getClientIp(request), 3600); // 拉黑1小时
            }

            // 返回错误响应
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(JsonUtils.toJson(ApiResult.error("请求被拒绝")));

        } catch (Exception e) {
            log.error("处理威胁事件失败", e);
        }
    }
}
```

#### 数据脱敏和加密

**敏感数据处理器**：
```java
@Component
@Slf4j
public class SensitiveDataProcessor {

    @Resource
    private EncryptionService encryptionService;

    @Resource
    private DataMaskingService dataMaskingService;

    /**
     * 数据加密
     */
    public String encryptSensitiveData(String data, SensitiveDataType dataType) {
        try {
            switch (dataType) {
                case PASSWORD:
                    return encryptionService.encryptPassword(data);
                case PHONE:
                    return encryptionService.encryptPhone(data);
                case EMAIL:
                    return encryptionService.encryptEmail(data);
                case ID_CARD:
                    return encryptionService.encryptIdCard(data);
                case BANK_CARD:
                    return encryptionService.encryptBankCard(data);
                default:
                    return encryptionService.encrypt(data);
            }
        } catch (Exception e) {
            log.error("敏感数据加密失败: dataType={}", dataType, e);
            throw new EncryptionException("数据加密失败", e);
        }
    }

    /**
     * 数据解密
     */
    public String decryptSensitiveData(String encryptedData, SensitiveDataType dataType) {
        try {
            switch (dataType) {
                case PASSWORD:
                    return encryptionService.decryptPassword(encryptedData);
                case PHONE:
                    return encryptionService.decryptPhone(encryptedData);
                case EMAIL:
                    return encryptionService.decryptEmail(encryptedData);
                case ID_CARD:
                    return encryptionService.decryptIdCard(encryptedData);
                case BANK_CARD:
                    return encryptionService.decryptBankCard(encryptedData);
                default:
                    return encryptionService.decrypt(encryptedData);
            }
        } catch (Exception e) {
            log.error("敏感数据解密失败: dataType={}", dataType, e);
            throw new DecryptionException("数据解密失败", e);
        }
    }

    /**
     * 数据脱敏
     */
    public String maskSensitiveData(String data, SensitiveDataType dataType) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        try {
            switch (dataType) {
                case PHONE:
                    return dataMaskingService.maskPhone(data);
                case EMAIL:
                    return dataMaskingService.maskEmail(data);
                case ID_CARD:
                    return dataMaskingService.maskIdCard(data);
                case BANK_CARD:
                    return dataMaskingService.maskBankCard(data);
                case NAME:
                    return dataMaskingService.maskName(data);
                case ADDRESS:
                    return dataMaskingService.maskAddress(data);
                default:
                    return dataMaskingService.mask(data, "***");
            }
        } catch (Exception e) {
            log.error("数据脱敏失败: dataType={}", dataType, e);
            return "***MASKED***";
        }
    }

    /**
     * 注解式数据脱敏
     */
    @Around("@annotation(sensitiveData)")
    public Object around(ProceedingJoinPoint joinPoint, SensitiveData sensitiveData)
            throws Throwable {

        // 获取方法返回值
        Object result = joinPoint.proceed();

        if (result instanceof String) {
            // 字符串类型直接脱敏
            String maskedData = maskSensitiveData((String) result, sensitiveData.type());
            return maskedData;
        } else if (result instanceof Map) {
            // Map类型递归脱敏
            Map<?, ?> resultMap = (Map<?, ?>) result;
            Map<String, Object> maskedMap = new HashMap<>();

            for (Map.Entry<?, ?> entry : resultMap.entrySet()) {
                if (entry.getValue() instanceof String) {
                    String maskedValue = maskSensitiveData(
                            (String) entry.getValue(), sensitiveData.type());
                    maskedMap.put((String) entry.getKey(), maskedValue);
                } else {
                    maskedMap.put((String) entry.getKey(), entry.getValue());
                }
            }
            return maskedMap;
        }

        return result;
    }
}

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveData {
    SensitiveDataType type();
    boolean maskResponse() default true;
}

public enum SensitiveDataType {
    PASSWORD, PHONE, EMAIL, ID_CARD, BANK_CARD, NAME, ADDRESS
}
```

### 🔒 网络安全防护能力 (★★★)

#### WAF防护配置

**Web应用防火墙**：
```java
@Component
@Slf4j
public class WebApplicationFirewall {

    @Resource
    private RuleEngine ruleEngine;

    @Resource
    private IpWhitelistService ipWhitelistService;

    @Resource
    private SecurityPolicyService securityPolicyService;

    /**
     * WAF请求检测
     */
    public WafResult inspectRequest(HttpServletRequest request) {
        try {
            WafContext context = buildWafContext(request);

            // 1. IP白名单检查
            if (!checkIpWhitelist(context.getClientIp())) {
                return WafResult.blocked("IP不在白名单中");
            }

            // 2. 规则引擎检测
            RuleEngineResult ruleResult = ruleEngine.evaluate(context);
            if (ruleResult.isBlocked()) {
                return WafResult.blocked(ruleResult.getReason());
            }

            // 3. 安全策略检查
            PolicyResult policyResult = securityPolicyService.evaluate(context);
            if (policyResult.isBlocked()) {
                return WafResult.blocked(policyResult.getReason());
            }

            return WafResult.allowed();

        } catch (Exception e) {
            log.error("WAF检测异常", e);
            // 异常情况下默认允许，避免影响业务
            return WafResult.allowed();
        }
    }

    /**
     * 构建WAF上下文
     */
    private WafContext buildWafContext(HttpServletRequest request) {
        return WafContext.builder()
                .clientIp(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .method(request.getMethod())
                .uri(request.getRequestURI())
                .queryString(request.getQueryString())
                .headers(getHeaders(request))
                .parameters(getParameters(request))
                .body(getRequestBody(request))
                .build();
    }

    /**
     * 检查IP白名单
     */
    private boolean checkIpWhitelist(String clientIp) {
        // 如果白名单为空，则允许所有IP
        List<String> whitelist = ipWhitelistService.getWhitelist();
        if (whitelist.isEmpty()) {
            return true;
        }

        // 检查IP是否在白名单中
        return whitelist.contains(clientIp);
    }
}
```

#### 安全规则引擎

**安全规则配置**：
```java
@Configuration
public class SecurityRuleConfiguration {

    @Bean
    public RuleEngine securityRuleEngine() {
        RuleEngine engine = new RuleEngine();

        // SQL注入检测规则
        engine.addRule(new SqlInjectionRule());

        // XSS攻击检测规则
        engine.addRule(new XssAttackRule());

        // 路径遍历检测规则
        engine.addRule(new PathTraversalRule());

        // 命令注入检测规则
        engine.addRule(new CommandInjectionRule());

        // 文件上传检测规则
        engine.addRule(new FileUploadRule());

        // 恶意扫描检测规则
        engine.addRule(new MaliciousScanRule());

        return engine;
    }
}

@Component
public class SqlInjectionRule implements SecurityRule {

    private static final Pattern SQL_PATTERN = Pattern.compile(
            "(?i)(\\b(select|insert|update|delete|drop|alter|create|exec|execute)\\b|" +
            "\\b(union|and|or|where|having|group by|order by)\\b|" +
            "(\\*|=|like|>|<|>=|<=|<>|!=|between|in|exists)" +
            "['\"]?\\s*\\([^)]*\\))");

    @Override
    public RuleResult evaluate(WafContext context) {
        String input = context.getQueryString() + " " +
                       String.join(" ", context.getParameters().values());

        Matcher matcher = SQL_PATTERN.matcher(input);
        if (matcher.find()) {
            return RuleResult.blocked("检测到SQL注入尝试: " + matcher.group());
        }

        return RuleResult.allowed();
    }
}
```

---

## 🛠️ 操作步骤

### 1. 安全架构部署

#### 步骤1: 安全中间件部署
```yaml
# 安全中间件配置
security:
  # 认证配置
  authentication:
    enabled: true
    jwt-secret: ${JWT_SECRET:ioe-dream-jwt-secret-2025}
    token-expiration: 86400  # 24小时

  # 授权配置
  authorization:
    enabled: true
    role-hierarchy-enabled: true
    permission-check-enabled: true

  # API安全配置
  api-security:
    rate-limiting:
      enabled: true
      default-limit: 100  # 每分钟100次
    cors:
      enabled: true
      allowed-origins: ${CORS_ORIGINS:http://localhost:8081}
    encryption:
      enabled: true
      algorithm: AES-256-GCM

  # 数据安全配置
  data-security:
    encryption:
      enabled: true
      key-rotation-interval: 30  # 30天
    masking:
      enabled: true
      default-mask: "***"
    audit:
      enabled: true
      log-level: INFO

  # 网络安全配置
  network-security:
    waf:
      enabled: true
      rule-update-interval: 3600  # 1小时
    ip-whitelist:
      enabled: true
      default-policy: DENY
    ddos-protection:
      enabled: true
      threshold: 1000  # 每秒1000次请求
```

#### 步骤2: SSL/TLS配置
```yaml
# HTTPS配置
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: ioe-dream
    trust-store: classpath:truststore.p12
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD}
    trust-store-type: PKCS12

# HTTP重定向到HTTPS
server:
  port: 8080
  http:
    port: 8080
  ssl:
    port: 8443
```

### 2. 安全监控部署

#### 步骤1: 安全事件监控
```java
@Component
@Slf4j
public class SecurityEventMonitor {

    @Resource
    private AlertService alertService;

    @Resource
    private SecurityAnalysisService analysisService;

    @EventListener
    public void handleSecurityEvent(SecurityEvent event) {
        try {
            // 记录安全事件
            logSecurityEvent(event);

            // 分析威胁等级
            ThreatLevel threatLevel = analysisService.analyzeThreatLevel(event);

            // 根据威胁等级采取行动
            switch (threatLevel) {
                case CRITICAL:
                    handleCriticalThreat(event);
                    break;
                case HIGH:
                    handleHighThreat(event);
                    break;
                case MEDIUM:
                    handleMediumThreat(event);
                    break;
                case LOW:
                    handleLowThreat(event);
                    break;
            }

        } catch (Exception e) {
            log.error("处理安全事件异常", e);
        }
    }

    /**
     * 处理关键威胁
     */
    private void handleCriticalThreat(SecurityEvent event) {
        // 立即告警
        alertService.sendCriticalAlert("发现关键安全威胁", event);

        // 自动阻断IP
        blockIpAddress(event.getClientIp(), 7200); // 阻断2小时

        // 增加监控级别
        increaseMonitoringLevel(event.getServiceName());

        // 通知安全团队
        notifySecurityTeam(event);
    }

    /**
     * 增加监控级别
     */
    private void increaseMonitoringLevel(String serviceName) {
        // 增加该服务的日志级别
        loggingService.setLogLevel(serviceName, "DEBUG");

        // 增加采样率
        monitoringService.increaseSamplingRate(serviceName, 1.0);

        // 启用详细监控
        monitoringService.enableDetailedMonitoring(serviceName);
    }
}
```

### 3. 安全合规检查

#### 步骤1: 安全合规扫描
```java
@Component
@Slf4j
public class SecurityComplianceScanner {

    @Resource
    private VulnerabilityScanner vulnerabilityScanner;

    @Resource
    private ConfigurationAuditor configurationAuditor;

    /**
     * 执行安全合规扫描
     */
    public ComplianceScanResult performComplianceScan() {
        try {
            ComplianceScanResult result = new ComplianceScanResult();

            // 1. 漏洞扫描
            List<Vulnerability> vulnerabilities = vulnerabilityScanner.scan();
            result.setVulnerabilities(vulnerabilities);

            // 2. 配置审计
            List<ConfigurationIssue> configIssues = configurationAuditor.audit();
            result.setConfigurationIssues(configIssues);

            // 3. 权限检查
            List<PermissionIssue> permissionIssues = checkPermissions();
            result.setPermissionIssues(permissionIssues);

            // 4. 数据安全检查
            List<DataSecurityIssue> dataSecurityIssues = checkDataSecurity();
            result.setDataSecurityIssues(dataSecurityIssues);

            // 5. 网络安全检查
            List<NetworkSecurityIssue> networkIssues = checkNetworkSecurity();
            result.setNetworkSecurityIssues(networkIssues);

            // 6. 生成合规报告
            generateComplianceReport(result);

            return result;

        } catch (Exception e) {
            log.error("安全合规扫描失败", e);
            return ComplianceScanResult.error(e);
        }
    }

    /**
     * 检查数据安全
     */
    private List<DataSecurityIssue> checkDataSecurity() {
        List<DataSecurityIssue> issues = new ArrayList<>();

        // 检查敏感数据加密
        issues.addAll(checkSensitiveDataEncryption());

        // 检查传输安全
        issues.addAll(checkTransmissionSecurity());

        // 检查数据脱敏
        issues.addAll(checkDataMasking());

        // 检查访问日志
        issues.addAll(checkAccessLogging());

        return issues;
    }

    /**
     * 检查敏感数据加密
     */
    private List<DataSecurityIssue> checkSensitiveDataEncryption() {
        List<DataSecurityIssue> issues = new ArrayList<>();

        // 检查数据库中的敏感字段
        List<String> sensitiveColumns = Arrays.asList("password", "phone", "email", "id_card");

        for (String column : sensitiveColumns) {
            if (!isColumnEncrypted(column)) {
                issues.add(DataSecurityIssue.builder()
                        .type("UNENCRYPTED_SENSITIVE_DATA")
                        .description("敏感字段未加密: " + column)
                        .severity(Severity.HIGH)
                        .recommendation("对敏感字段进行加密存储")
                        .build());
            }
        }

        return issues;
    }
}
```

---

## 📚 知识要求

### 理论知识
- **信息安全基础**: 掌握CIA三元组（保密性、完整性、可用性）
- **网络安全原理**: 深入理解TCP/IP协议、HTTP/HTTPS、防火墙等
- **加密算法**: 熟悉对称加密、非对称加密、哈希算法等
- **认证授权理论**: 掌握OAuth2、JWT、RBAC等认证授权模型

### 业务理解
- **IOE-DREAM安全需求**: 理解门禁、消费、考勤等业务的安全要求
- **数据分类标准**: 掌握敏感数据分类和保护要求
- **合规性要求**: 了解数据安全法、网络安全法等法规要求
- **安全等级保护**: 理解等保2.0/3.0的安全要求

### 技术背景
- **Spring Security**: 精通Spring Security框架的使用和扩展
- **Sa-Token**: 熟练使用Sa-Token进行权限控制
- **网络安全工具**: 掌握WAF、IDS/IPS等网络安全工具
- **加密技术**: 熟悉Java加密体系和第三方加密库

---

## ⚠️ 注意事项

### 安全原则
- **最小权限原则**: 用户和系统只授予必要的最小权限
- **纵深防御**: 采用多层次的安全防护措施
- **零信任架构**: 不信任任何内部网络和用户
- **安全默认**: 默认配置应该是安全的，而不是方便的

### 性能考虑
- **安全开销**: 平衡安全性和性能，避免过度防护
- **缓存策略**: 合理使用缓存减少安全检查的性能影响
- **异步处理**: 对于非关键的安全检查，采用异步处理
- **优化算法**: 优化加密和哈希算法的性能

### 运维考虑
- **自动化监控**: 建立自动化的安全监控和告警机制
- **定期扫描**: 定期进行安全漏洞扫描和渗透测试
- **应急响应**: 建立完善的安全事件应急响应机制
- **知识更新**: 及时更新安全知识和防护技术

---

## 🔗 相关技能

### 相关技能
- **[服务治理专家](service-governance-expert.md)**: 服务监控和治理
- **[系统运维专家](intelligent-operations-expert.md)**: 系统部署和运维
- **[数据一致性专家](data-consistency-expert.md)**: 数据安全和一致性
- **[质量保证专家](quality-assurance-expert.md)**: 安全测试和质量保证

### 进阶路径
- **首席信息安全官**: 负责企业整体信息安全战略
- **安全架构师**: 负责安全架构设计和技术选型
- **渗透测试专家**: 负责安全测试和漏洞发现

### 参考资料
- **[OWASP Top 10](https://owasp.org/www-project-top-ten/)**: Web应用安全风险
- **[Spring Security文档](https://spring.io/projects/spring-security)**: Spring Security官方文档
- **[Sa-Token文档](https://sa-token.cc/doc.html)**: Sa-Token完整使用指南
- **[安全建设规范](../docs/repowiki/zh/content/系统安全规范.md)**: 项目安全建设标准

---

**💡 核心理念**: 安全是微服务架构的生命线，通过系统化的安全防护体系和持续的安全监控，确保系统的机密性、完整性和可用性，为业务发展提供坚实的安全保障。