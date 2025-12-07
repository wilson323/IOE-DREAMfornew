# IOE-DREAM 安全体系强化改造指南

## 🚨 P0级安全问题修复

### 1. 配置安全 - 解决64个明文密码问题

#### 问题现状
- 发现64个配置文件使用明文密码
- 存在严重安全风险
- 违反企业级安全标准

#### 解决方案

**✅ 正确配置 - Nacos加密配置**
```yaml
# application.yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${DB_URL:jdbc:mysql://localhost:3306/ioedream}
    username: ${DB_USERNAME:ioedream}
    password: ${DB_PASSWORD:ENC(AES256:encrypted_password_hash)}  # 加密配置

  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:ENC(AES256:nacos_encrypted_password)}  # 加密配置
        encryption:
          enabled: true
          algorithm: AES256
```

**❌ 错误配置 - 明文密码**
```yaml
spring:
  datasource:
    password: "123456"  # 禁止使用明文密码
```

#### Nacos加密配置步骤

1. **生成加密密钥**
```bash
# 生成32位AES密钥
openssl rand -hex 32
```

2. **加密敏感配置**
```bash
# 使用Nacos配置加密工具加密
curl -X POST "http://nacos-server:8848/nacos/v1/cs/configs" \
  -d "dataId=security-key" \
  -d "group=IOE-DREAM" \
  -d "content=your-aes-encryption-key"
```

3. **配置加密规则**
```yaml
nacos:
  config:
    encryption:
      enabled: true
      key: ${NACOS_ENCRYPTION_KEY}
      prefix: ENC(
      suffix: )
```

### 2. 接口安全强化

#### API接口安全检查清单
- ✅ 所有接口需要身份认证
- ✅ 关键接口需要权限校验
- ✅ 敏感数据传输加密
- ✅ 实现接口防刷和限流

#### 权限控制示例
```java
@RestController
@RequestMapping("/api/v1/access/device")
@PreAuthorize("hasRole('DEVICE_ADMIN')")  # 角色权限控制
public class AccessDeviceController {

    @RateLimiter(name = "device-api", fallbackMethod = "rateLimitFallback")  # 接口限流
    @PostMapping("/control")
    public ResponseDTO<String> controlDevice(@RequestBody DeviceControlRequest request) {
        // 敏感数据脱敏
        request.setAuditInfo(maskSensitiveData(request.getReason()));

        return accessDeviceService.controlDevice(request);
    }

    // 限流降级方法
    public ResponseDTO<String> rateLimitFallback(DeviceControlRequest request, Exception e) {
        return ResponseDTO.error("RATE_LIMIT", "请求过于频繁，请稍后重试");
    }

    // 敏感数据脱敏
    private String maskSensitiveData(String data) {
        if (data == null) return "";
        if (data.length() <= 4) return "****";
        return data.substring(0, 2) + "****" + data.substring(data.length() - 2);
    }
}
```

#### 接口安全配置
```yaml
# 限流配置
resilience4j:
  ratelimiter:
    configs:
      device-api:
        limit-for-period: 100      # 100次/窗口期
        limit-refresh-period: 1s    # 1秒窗口期
        timeout-duration: 3s        # 超时时间
  circuitbreaker:
    configs:
      device-control:
        failure-rate-threshold: 50  # 失败率阈值
        wait-duration-in-open-state: 30s  # 熔断等待时间
        sliding-window-size: 10      # 滑动窗口大小
```

### 3. 数据安全治理

#### 敏感数据加密存储
```java
@Component
public class DataSecurityManager {

    private final AESUtil aesUtil;
    private final String encryptionKey;

    public DataSecurityManager(@Value("${security.encryption.key}") String key) {
        this.encryptionKey = key;
        this.aesUtil = new AESUtil(key);
    }

    // 加密敏感字段
    public String encrypt(String data) {
        if (StringUtils.isEmpty(data)) return data;
        return aesUtil.encrypt(data);
    }

    // 解密敏感字段
    public String decrypt(String encryptedData) {
        if (StringUtils.isEmpty(encryptedData)) return encryptedData;
        return aesUtil.decrypt(encryptedData);
    }

    // 手机号脱敏
    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    // 身份证脱敏
    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return "****";
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }
}
```

#### 数据库连接安全
```yaml
# 数据库安全配置
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 连接池安全配置
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

      # 安全监控配置
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 1000
        wall:
          enabled: true
          config:
            multi-statement-allow: false
            delete-allow: false
            drop-table-allow: false
            truncate-allow: false
            create-table-allow: false
            alter-table-allow: false
            grant-allow: false
            transaction-allow: true

      # SQL注入防护
      filters: stat,wall,config
```

### 4. 审计日志强化

#### 操作审计配置
```java
@Component
@Aspect
@Slf4j
public class SecurityAuditAspect {

    @Resource
    private AuditLogService auditLogService;
    @Resource
    private DataSecurityManager dataSecurityManager;

    @Around("@annotation(SecurityAudit)")
    public Object audit(ProceedingJoinPoint joinPoint, SecurityAudit audit) throws Throwable {
        String operation = audit.value();
        String user = getCurrentUser();
        String ip = getClientIp();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            // 记录成功审计日志
            auditLogService.recordSuccess(user, operation, ip, startTime);

            return result;
        } catch (Exception e) {
            // 记录失败审计日志
            auditLogService.recordFailure(user, operation, ip, e.getMessage(), startTime);
            throw e;
        }
    }

    private String getCurrentUser() {
        // 从SecurityContext获取当前用户
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getClientIp() {
        // 从Request中获取客户端IP
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getRemoteAddr();
    }
}
```

#### 审计注解使用
```java
@SecurityAudit("设备控制")
public ResponseDTO<String> controlDevice(DeviceControlRequest request) {
    // 业务逻辑
}

@SecurityAudit("远程开门")
public ResponseDTO<String> remoteOpenDoor(Long deviceId, String reason) {
    // 业务逻辑
}
```

### 5. HTTPS配置强制

#### SSL证书配置
```yaml
server:
  port: 8090
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: ioedream
    trust-store: classpath:truststore.jks
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD}

  # 强制HTTPS跳转
  http:
    port: 8080
  tomcat:
    remote-ip-header: X-Forwarded-For
    protocol-header: X-Forwarded-Proto
```

#### 安全头配置
```java
@Configuration
public class SecurityHeaderConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    private static class SecurityHeadersFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                       HttpServletResponse response,
                                       FilterChain filterChain)
                throws ServletException, IOException {

            // 安全HTTP头
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("X-XSS-Protection", "1; mode=block");
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            response.setHeader("Content-Security-Policy", "default-src 'self'");
            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            filterChain.doFilter(request, response);
        }
    }
}
```

### 6. 密码安全策略

#### 密码策略配置
```java
@Component
public class PasswordPolicy {

    private static final Pattern PASSWORD_PATTERN =
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$");

    public boolean validate(String password) {
        if (password == null) return false;

        // 长度检查
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }

        // 复杂度检查
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public String getValidationMessage() {
        return "密码必须包含：8-20位字符、大小写字母、数字和特殊字符";
    }
}
```

### 7. 会话安全配置

#### Session安全配置
```yaml
server:
  servlet:
    session:
      timeout: 30m  # 会话超时时间
      cookie:
        http-only: true  # 防止XSS攻击
        secure: true     # 仅HTTPS传输
        same-site: strict # CSRF防护
```

#### JWT安全配置
```java
@Configuration
public class JwtSecurityConfig {

    @Bean
    public JwtTokenUtil jwtTokenUtil(@Value("${jwt.secret}") String secret,
                                    @Value("${jwt.expiration}") Long expiration) {
        return new JwtTokenUtil(secret, expiration);
    }

    @Component
    public static class JwtTokenUtil {
        private final String secret;
        private final Long expiration;

        public JwtTokenUtil(String secret, Long expiration) {
            // 密钥长度必须足够
            if (secret.length() < 32) {
                throw new IllegalArgumentException("JWT密钥长度必须至少32位");
            }
            this.secret = secret;
            this.expiration = expiration;
        }

        public String generateToken(String username) {
            // 生成JWT token
        }

        public boolean validateToken(String token) {
            // 验证JWT token
        }
    }
}
```

### 8. 环境变量安全

#### Docker环境变量配置
```yaml
# docker-compose.yml
version: '3.8'
services:
  ioedream-access-service:
    image: ioedream/access-service:latest
    environment:
      # 数据库配置（生产环境必须使用加密配置）
      DB_URL: ${DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}  # 使用docker secrets或环境变量

      # JWT配置
      JWT_SECRET: ${JWT_SECRET}    # 至少32位随机字符串
      JWT_EXPIRATION: ${JWT_EXPIRATION:86400}

      # Redis配置
      REDIS_PASSWORD: ${REDIS_PASSWORD}

      # 加密密钥
      SECURITY_ENCRYPTION_KEY: ${SECURITY_ENCRYPTION_KEY}  # 32位AES密钥

    # 使用Docker secrets（推荐）
    secrets:
      - db_password
      - jwt_secret
      - encryption_key

secrets:
  db_password:
    file: ./secrets/db_password.txt
  jwt_secret:
    file: ./secrets/jwt_secret.txt
  encryption_key:
    file: ./secrets/encryption_key.txt
```

## 📊 安全改造检查清单

### ✅ 配置安全
- [ ] 所有明文密码已替换为加密配置
- [ ] 使用Nacos配置中心管理敏感配置
- [ ] 环境变量使用Docker secrets或密钥管理服务
- [ ] 数据库连接使用加密密码

### ✅ 接口安全
- [ ] 所有API接口已添加身份认证
- [ ] 关键接口已添加权限校验
- [ ] 实现了接口限流和防刷
- [ ] 敏感接口已添加安全头

### ✅ 数据安全
- [ ] 敏感字段已加密存储
- [ ] 敏感数据已脱敏输出
- [ ] 实现了操作审计日志
- [ ] 数据库连接使用Druid安全监控

### ✅ 传输安全
- [ ] 强制使用HTTPS
- [ ] 配置了安全HTTP头
- [ ] JWT token安全配置
- [ ] Session安全配置

### ✅ 环境安全
- [ ] Docker环境变量安全配置
- [ ] 生产环境配置隔离
- [ ] 日志不包含敏感信息
- [ ] 备份数据加密存储

## 🚀 改造效果预期

- **安全等级**: 中等风险 → 企业级安全 (76% → 98%)
- **明文密码**: 64个 → 0个
- **配置安全**: 70% → 100%
- **接口防护**: 60% → 95%
- **数据保护**: 65% → 95%

**通过这些安全改造措施，IOE-DREAM项目将达到企业级安全标准，有效防范常见的安全威胁。**