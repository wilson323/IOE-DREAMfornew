# IOE-DREAM 全局TODO深度分析与企业级实施方案

> **文档版本**: v1.0.0
> **创建日期**: 2025-01-30
> **更新日期**: 2025-01-30
> **负责人**: IOE-DREAM架构委员会

---

## 📊 执行摘要

### 1. 问题概述

通过对整个代码库的深度扫描分析，识别出**42个关键TODO项**需要实现。这些TODO主要集中在**跨服务集成、业务逻辑完善、通知服务集成**三大领域。

### 2. 影响评估

| 维度 | 影响程度 | 紧急程度 | 业务价值 |
|------|---------|---------|---------|
| **系统完整性** | 🔴 高 | P0 | 核心功能缺失 |
| **用户体验** | 🟡 中 | P1 | 增强功能 |
| **运维监控** | 🟡 中 | P1 | 可观测性提升 |
| **系统安全性** | 🟡 中 | P0 | 安全加固 |

### 3. 优先级分类

- **P0级（关键）**: 15项 - 影响核心业务流程
- **P1级（重要）**: 18项 - 影响用户体验
- **P2级（优化）**: 9项 - 性能和代码质量优化

---

## 🎯 P0级关键TODO（必须实现）

### 模块1: 认证与安全集成（3项）

#### TODO-001: JWT/Sa-Token解析器集成

**文件位置**:
- `microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/util/SmartRequestUtil.java:253`
- `microservices-common-gateway-client/src/main/java/net/lab1024/sa/common/util/SmartRequestUtil.java:285`

**当前状态**:
```java
// 这里可以添加JWT解析逻辑，暂时返回null
// TODO: 集成Sa-Token或JWT解析器
```

**业务需求分析**:
1. **用户身份识别**: 从HTTP请求头中解析JWT token，获取用户身份信息
2. **服务间调用认证**: 微服务间调用时传递用户上下文
3. **安全性保障**: 验证token有效性，防止伪造和篡改

**业务流程图**:
```
┌─────────────┐
│  HTTP请求   │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│ 提取Authorization   │
│ Bearer {token}      │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ 解析JWT Token       │
│ - 验证签名          │
│ - 检查有效期        │
│ - 提取Claims        │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ 构建UserContext     │
│ - userId            │
│ - username          │
│ - roles/permissions │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ 存储到ThreadLocal   │
│ 供后续使用          │
└─────────────────────┘
```

**技术实现方案**:

```java
// 1. 添加依赖（pom.xml）
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
    <version>1.37.0</version>
</dependency>

// 2. 实现JWT解析工具类
@Component
@Slf4j
public class JwtTokenParser {

    /**
     * 解析JWT token
     */
    public UserContext parseToken(String token) {
        try {
            // 使用Sa-Token解析
            SaTokenInfo tokenInfo = StpUtil.stpLogic.getTokenInfoByToken(token);
            if (tokenInfo == null || !tokenInfo.isLogin()) {
                log.warn("[JWT解析] Token无效或已过期");
                return null;
            }

            // 构建用户上下文
            UserContext context = new UserContext();
            context.setUserId(tokenInfo.getLoginIdAsLong());
            context.setUserName(tokenInfo.getTokenValue());
            context.setRoles((List<String>) tokenInfo.getExtra("roles"));

            log.info("[JWT解析] 解析成功: userId={}", context.getUserId());
            return context;

        } catch (Exception e) {
            log.error("[JWT解析] 解析失败: {}", e.getMessage(), e);
            return null;
        }
    }
}

// 3. 修改SmartRequestUtil
@Resource
private JwtTokenParser jwtTokenParser;

public static UserContext getUserContext(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");

    if (StringUtils.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
        String token = authorization.substring(7);
        return jwtTokenParser.parseToken(token);
    }

    return null;
}
```

**开发规范**:
- ✅ 使用`@Resource`注解进行依赖注入
- ✅ 使用SLF4J进行日志记录：`log.info("[JWT解析] 解析成功: userId={}", userId)`
- ✅ 异常处理要捕获并记录详细日志
- ✅ 返回null而非抛异常，让调用方决定如何处理
- ✅ 日志中禁止记录token完整内容，防止敏感信息泄露

**注意事项**:
1. **性能优化**: JWT解析结果应该缓存，避免重复解析
2. **安全考虑**: 验证token签名时使用公钥，不要在代码中硬编码密钥
3. **异常处理**: token过期、无效等情况要优雅处理
4. **日志记录**: 记录解析失败的原因，但不要记录完整token

**测试用例**:
```java
@Test
void testParseValidToken() {
    // Given
    String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

    // When
    UserContext context = jwtTokenParser.parseToken(validToken);

    // Then
    assertNotNull(context);
    assertEquals(1001L, context.getUserId());
    assertEquals("admin", context.getUserName());
}

@Test
void testParseExpiredToken() {
    // Given
    String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired...";

    // When
    UserContext context = jwtTokenParser.parseToken(expiredToken);

    // Then
    assertNull(context);
}
```

**验收标准**:
- ✅ 能正确解析有效的JWT token
- ✅ 能识别并拒绝过期的token
- ✅ 能识别并拒绝篡改的token
- ✅ 解析结果包含userId、username、roles等核心信息
- ✅ 单元测试覆盖率≥90%

---

#### TODO-002: 用户锁定状态数据库更新

**文件位置**:
- `microservices-common-security/src/main/java/net/lab1024/sa/common/auth/service/UserLockService.java:182`

**当前状态**:
```java
// TODO: 同时需要更新数据库中的用户锁定状态
// 这里应该调用用户服务的API更新数据库
```

**业务需求分析**:
1. **状态同步**: 用户被锁定时，需要同步更新数据库中的用户状态
2. **跨服务调用**: 安全服务调用用户服务的API更新数据库
3. **事务一致性**: 确保缓存和数据库状态一致

**业务流程图**:
```
用户登录失败
     │
     ▼
┌────────────────────┐
│  累计失败次数      │
│  Redis计数器+1     │
└──────┬─────────────┘
       │
       ▼
   达到阈值？
  ┌────┴────┐
  │         │
 YES       NO
  │         │
  ▼         ▼
┌─────────────┐  返回
│锁定用户账号  │ 认证失败
└──────┬──────┘
       │
       ▼
┌────────────────────┐
│ 1. 更新Redis锁定   │
│    状态            │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│ 2. 调用用户服务API │
│    更新数据库状态  │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│ 3. 发送锁定通知    │
│    邮件/短信       │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. UserLockService实现
@Service
@Slf4j
public class UserLockServiceImpl implements UserLockService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private UserLockCacheManager userLockCacheManager;

    /**
     * 锁定用户
     */
    @Override
    public void lockUser(String username, int failureCount, int lockMinutes) {
        log.info("[用户锁定] 开始锁定用户: username={}, failureCount={}, lockMinutes={}",
                 username, failureCount, lockMinutes);

        try {
            // 1. 更新Redis缓存中的锁定状态
            LocalDateTime lockExpireTime = LocalDateTime.now().plusMinutes(lockMinutes);
            userLockCacheManager.lockUser(username, failureCount, lockExpireTime);

            log.info("[用户锁定] Redis锁定状态已更新: username={}, expireTime={}",
                     username, lockExpireTime);

            // 2. 调用用户服务API更新数据库
            updateUserLockStatusInDB(username, true, lockExpireTime);

            // 3. 发送锁定通知
            sendLockNotification(username, failureCount, lockMinutes);

            log.info("[用户锁定] 用户锁定完成: username={}", username);

        } catch (Exception e) {
            log.error("[用户锁定] 锁定用户失败: username={}, error={}",
                     username, e.getMessage(), e);
            throw new SystemException("USER_LOCK_ERROR", "锁定用户失败", e);
        }
    }

    /**
     * 更新数据库中的用户锁定状态
     */
    private void updateUserLockStatusInDB(String username, boolean locked,
                                          LocalDateTime lockExpireTime) {
        try {
            log.info("[用户锁定] 调用用户服务更新数据库: username={}, locked={}",
                     username, locked);

            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("locked", locked);
            params.put("lockExpireTime", lockExpireTime != null ?
                      lockExpireTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);

            // 调用用户服务API
            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                "/api/user/update-lock-status",
                HttpMethod.PUT,
                params,
                Void.class
            );

            if (!response.isSuccessful()) {
                log.error("[用户锁定] 更新数据库失败: username={}, response={}",
                         username, response.getMessage());
                throw new BusinessException("UPDATE_DB_FAILED", "更新数据库失败");
            }

            log.info("[用户锁定] 数据库更新成功: username={}", username);

        } catch (Exception e) {
            log.error("[用户锁定] 更新数据库异常: username={}, error={}",
                     username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 发送用户锁定通知
     */
    private void sendLockNotification(String username, int failureCount,
                                     int lockMinutes) {
        try {
            log.info("[用户锁定] 发送锁定通知: username={}, failureCount={}",
                     username, failureCount);

            // 构建通知内容
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "USER_LOCKED");
            notification.put("username", username);
            notification.put("failureCount", failureCount);
            notification.put("lockMinutes", lockMinutes);
            notification.put("lockTime", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 调用通知服务
            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                "/api/notification/send",
                HttpMethod.POST,
                notification,
                Void.class
            );

            if (!response.isSuccessful()) {
                log.warn("[用户锁定] 发送通知失败: username={}, error={}",
                        username, response.getMessage());
            }

            log.info("[用户锁定] 锁定通知已发送: username={}", username);

        } catch (Exception e) {
            log.error("[用户锁定] 发送通知异常: username={}, error={}",
                     username, e.getMessage(), e);
            // 通知发送失败不影响主流程
        }
    }
}

// 2. 用户服务接口（ioedream-common-service）
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 更新用户锁定状态
     */
    @PutMapping("/update-lock-status")
    public ResponseDTO<Void> updateLockStatus(@RequestBody Map<String, Object> params) {
        log.info("[用户服务] 更新用户锁定状态: params={}", params);

        try {
            String username = (String) params.get("username");
            Boolean locked = (Boolean) params.get("locked");
            String lockExpireTimeStr = (String) params.get("lockExpireTime");

            LocalDateTime lockExpireTime = null;
            if (lockExpireTimeStr != null) {
                lockExpireTime = LocalDateTime.parse(lockExpireTimeStr,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            userService.updateLockStatus(username, locked, lockExpireTime);

            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[用户服务] 更新锁定状态失败: error={}", e.getMessage(), e);
            return ResponseDTO.error("UPDATE_FAILED", e.getMessage());
        }
    }
}
```

**开发规范**:
- ✅ 使用GatewayServiceClient进行跨服务调用
- ✅ 日志记录使用`[用户锁定]`模块标识
- ✅ 异常处理要区分日志级别（warn/error）
- ✅ 关键步骤都要有日志记录
- ✅ 通知失败不影响主流程

**注意事项**:
1. **事务一致性**: Redis和数据库的状态更新要保持一致
2. **服务降级**: 如果用户服务不可用，要考虑降级策略
3. **性能考虑**: 数据库更新可以异步化
4. **重试机制**: 网络异常时要有合理的重试策略

**验收标准**:
- ✅ 用户锁定后，数据库状态正确更新
- ✅ Redis和数据库状态保持一致
- ✅ 锁定通知能正常发送
- ✅ 单元测试覆盖率≥85%

---

#### TODO-003: 用户锁定通知服务集成

**文件位置**:
- `microservices-common-security/src/main/java/net/lab1024/sa/common/auth/service/UserLockService.java:197`

**当前状态**:
```java
// TODO: 集成通知服务，发送邮件或短信
// notificationService.sendUserLockedNotification(username, failureCount, lockMinutes);
```

**业务需求分析**:
1. **实时通知**: 用户账号被锁定时，立即通知用户
2. **多渠道通知**: 支持邮件、短信、钉钉等多种通知渠道
3. **通知模板**: 使用预定义的通知模板，确保通知内容规范

**业务流程图**:
```
┌────────────────────┐
│  用户账号被锁定    │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  查询用户联系方式  │
│  - 邮箱            │
│  - 手机号          │
│  - 钉钉ID          │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  构建通知内容      │
│  - 使用模板引擎    │
│  - 填充动态数据    │
└──────┬─────────────┘
       │
       ▼
   通知渠道路由
  ┌────┴────┬─────────┐
  │         │         │
 邮件      短信      钉钉
  │         │         │
  ▼         ▼         ▼
发送邮件  发送短信   发送钉钉
  │         │         │
  └────┬────┴─────────┘
       │
       ▼
┌────────────────────┐
│  记录通知历史      │
│  用于审计追踪      │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. 通知服务接口
public interface NotificationService {

    /**
     * 发送用户锁定通知
     */
    void sendUserLockedNotification(String username, int failureCount,
                                    int lockMinutes);

    /**
     * 发送邮件通知
     */
    void sendEmail(String to, String subject, String content);

    /**
     * 发送短信通知
     */
    void sendSms(String phone, String content);

    /**
     * 发送钉钉通知
     */
    void sendDingTalk(String userId, String content);
}

// 2. 通知服务实现
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private EmailService emailService;

    @Resource
    private SmsService smsService;

    @Resource
    private DingTalkService dingTalkService;

    @Resource
    private NotificationHistoryManager notificationHistoryManager;

    @Resource
    private TemplateEngine templateEngine;

    /**
     * 发送用户锁定通知
     */
    @Override
    public void sendUserLockedNotification(String username, int failureCount,
                                          int lockMinutes) {
        log.info("[通知服务] 准备发送用户锁定通知: username={}, failureCount={}",
                 username, failureCount);

        try {
            // 1. 查询用户信息（包括联系方式）
            UserInfoVO userInfo = getUserInfo(username);
            if (userInfo == null) {
                log.warn("[通知服务] 用户不存在，跳过通知: username={}", username);
                return;
            }

            // 2. 准备通知模板数据
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("username", username);
            templateData.put("failureCount", failureCount);
            templateData.put("lockMinutes", lockMinutes);
            templateData.put("lockTime", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            templateData.put("unlockTime", LocalDateTime.now().plusMinutes(lockMinutes)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 3. 渲染通知内容
            String emailContent = templateEngine.process("user-locked-email", templateData);
            String smsContent = templateEngine.process("user-locked-sms", templateData);
            String dingTalkContent = templateEngine.process("user-locked-dingtalk", templateData);

            // 4. 并发发送多种通知
            CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(() -> {
                if (StringUtils.isNotBlank(userInfo.getEmail())) {
                    sendEmail(userInfo.getEmail(),
                            "账号安全提醒：您的账号已被临时锁定",
                            emailContent);
                }
            });

            CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(() -> {
                if (StringUtils.isNotBlank(userInfo.getPhone())) {
                    sendSms(userInfo.getPhone(), smsContent);
                }
            });

            CompletableFuture<Void> dingTalkFuture = CompletableFuture.runAsync(() -> {
                if (StringUtils.isNotBlank(userInfo.getDingTalkId())) {
                    sendDingTalk(userInfo.getDingTalkId(), dingTalkContent);
                }
            });

            // 5. 等待所有通知发送完成
            CompletableFuture.allOf(emailFuture, smsFuture, dingTalkFuture).join();

            // 6. 记录通知历史
            saveNotificationHistory(username, templateData);

            log.info("[通知服务] 用户锁定通知已发送: username={}", username);

        } catch (Exception e) {
            log.error("[通知服务] 发送用户锁定通知失败: username={}, error={}",
                     username, e.getMessage(), e);
            // 通知发送失败不影响主流程
        }
    }

    /**
     * 发送邮件
     */
    @Override
    public void sendEmail(String to, String subject, String content) {
        try {
            log.info("[通知服务-邮件] 发送邮件: to={}, subject={}", to, subject);

            EmailRequest request = new EmailRequest();
            request.setTo(to);
            request.setSubject(subject);
            request.setContent(content);
            request.setContentType("text/html;charset=UTF-8");

            emailService.sendEmail(request);

            log.info("[通知服务-邮件] 邮件发送成功: to={}", to);

        } catch (Exception e) {
            log.error("[通知服务-邮件] 邮件发送失败: to={}, error={}",
                     to, e.getMessage(), e);
        }
    }

    /**
     * 发送短信
     */
    @Override
    public void sendSms(String phone, String content) {
        try {
            log.info("[通知服务-短信] 发送短信: phone={}", phone);

            SmsRequest request = new SmsRequest();
            request.setPhone(phone);
            request.setContent(content);

            smsService.sendSms(request);

            log.info("[通知服务-短信] 短信发送成功: phone={}", phone);

        } catch (Exception e) {
            log.error("[通知服务-短信] 短信发送失败: phone={}, error={}",
                     phone, e.getMessage(), e);
        }
    }

    /**
     * 发送钉钉通知
     */
    @Override
    public void sendDingTalk(String userId, String content) {
        try {
            log.info("[通知服务-钉钉] 发送钉钉通知: userId={}", userId);

            DingTalkRequest request = new DingTalkRequest();
            request.setUserId(userId);
            request.setContent(content);
            request.setMsgType("text");

            dingTalkService.sendMessage(request);

            log.info("[通知服务-钉钉] 钉钉通知发送成功: userId={}", userId);

        } catch (Exception e) {
            log.error("[通知服务-钉钉] 钉钉通知发送失败: userId={}, error={}",
                     userId, e.getMessage(), e);
        }
    }

    /**
     * 保存通知历史
     */
    private void saveNotificationHistory(String username, Map<String, Object> data) {
        try {
            NotificationHistoryEntity history = new NotificationHistoryEntity();
            history.setUsername(username);
            history.setNotificationType("USER_LOCKED");
            history.setContent(JSON.toJSONString(data));
            history.setSendTime(LocalDateTime.now());
            history.setStatus("SUCCESS");

            notificationHistoryManager.save(history);

        } catch (Exception e) {
            log.error("[通知服务] 保存通知历史失败: error={}", e.getMessage(), e);
        }
    }
}

// 3. 邮件模板（src/main/resources/templates/user-locked-email.html）
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>账号锁定通知</title>
</head>
<body>
    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
        <h2 style="color: #f44336;">账号安全提醒</h2>
        <p>尊敬的<strong>${username}</strong>用户：</p>
        <p>您的账号于<strong>${lockTime}</strong>因连续登录失败<strong>${failureCount}</strong>次而被临时锁定。</p>
        <p>锁定时长：<strong>${lockMinutes}</strong>分钟</p>
        <p>预计解锁时间：<strong>${unlockTime}</strong></p>
        <p style="color: #666; font-size: 14px;">如果您认为这是误操作，请联系管理员。</p>
        <hr>
        <p style="color: #999; font-size: 12px;">此邮件由系统自动发送，请勿直接回复。</p>
    </div>
</body>
</html>

// 4. 短信模板（user-locked-sms.txt）
【账号安全提醒】尊敬的${username}，您的账号因连续登录失败${failureCount}次已被临时锁定，锁定时长${lockMinutes}分钟，预计${unlockTime}解锁。如有疑问请联系管理员。

// 5. 钉钉模板（user-locked-dingtalk.txt）
### ⚠️ 账号安全提醒
**用户**：${username}
**时间**：${lockTime}
**原因**：连续登录失败${failureCount}次
**锁定时长**：${lockMinutes}分钟
**预计解锁**：${unlockTime}

如有疑问请联系管理员。
```

**开发规范**:
- ✅ 使用异步方式发送通知，避免阻塞主流程
- ✅ 每个通知渠道独立try-catch，互不影响
- ✅ 使用模板引擎生成通知内容，便于维护
- ✅ 记录通知历史，便于审计和问题追踪
- ✅ 通知失败记录日志但不抛异常

**注意事项**:
1. **频率控制**: 避免同一事件重复发送通知
2. **模板管理**: 通知模板应该支持配置化，便于修改
3. **性能优化**: 通知发送应该异步化，使用线程池
4. **降级策略**: 某个通知渠道失败不影响其他渠道

**验收标准**:
- ✅ 用户锁定后能同时发送邮件、短信、钉钉通知
- ✅ 通知内容格式规范，信息完整
- ✅ 通知历史正确记录
- ✅ 单元测试覆盖率≥85%

---

### 模块2: 考勤模块完善（4项）

#### TODO-004: 生物识别逻辑实现

**文件位置**:
- `microservices-ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/template/impl/StandardAttendanceProcess.java:48`

**当前状态**:
```java
// TODO: 实现生物识别逻辑
// 1. 从punchForm获取生物特征数据
// 2. 调用生物识别服务进行1:N比对
// 3. 返回识别结果（userId或null）
```

**业务需求分析**:
1. **生物特征采集**: 从考勤设备获取生物特征数据（人脸、指纹等）
2. **身份识别**: 调用生物识别服务进行1:N比对，识别用户身份
3. **结果返回**: 返回识别结果（userId）或识别失败信息

**业务流程图**:
```
┌────────────────────┐
│  用户打卡          │
│  （生物识别）      │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  设备采集生物特征  │
│  - 人脸图像        │
│  - 指纹图像        │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  调用生物识别服务  │
│  进行1:N比对       │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  匹配    未匹配
   │       │
   ▼       ▼
返回userId  返回null
   │       │
   └───┬───┘
       │
       ▼
┌────────────────────┐
│  记录考勤打卡      │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. 生物识别服务接口
public interface BiometricService {

    /**
     * 1:N人脸识别
     * @param faceImageData 人脸图像数据（Base64编码）
     * @param deviceId 设备ID
     * @return 识别结果（userId或null）
     */
    Long recognizeFace(String faceImageData, String deviceId);

    /**
     * 1:N指纹识别
     * @param fingerprintData 指纹特征数据
     * @param deviceId 设备ID
     * @return 识别结果（userId或null）
     */
    Long recognizeFingerprint(String fingerprintData, String deviceId);
}

// 2. 生物识别服务实现
@Service
@Slf4j
public class BiometricServiceImpl implements BiometricService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private BiometricCacheManager biometricCacheManager;

    /**
     * 1:N人脸识别
     */
    @Override
    public Long recognizeFace(String faceImageData, String deviceId) {
        log.info("[生物识别] 开始人脸识别: deviceId={}", deviceId);

        try {
            // 1. 参数校验
            if (StringUtils.isBlank(faceImageData)) {
                log.warn("[生物识别] 人脸图像数据为空");
                return null;
            }

            // 2. 调用生物识别服务
            Map<String, Object> params = new HashMap<>();
            params.put("faceImageData", faceImageData);
            params.put("deviceId", deviceId);
            params.put("recognizeType", "1:N");  // 1:N识别

            log.debug("[生物识别] 调用生物识别API: deviceId={}", deviceId);

            ResponseDTO<BiometricResultVO> response = gatewayServiceClient.callBiometricService(
                "/api/biometric/recognize-face",
                HttpMethod.POST,
                params,
                new TypeReference<ResponseDTO<BiometricResultVO>>() {}
            );

            // 3. 处理识别结果
            if (response == null || !response.isSuccessful()) {
                log.warn("[生物识别] 人脸识别失败: deviceId={}, error={}",
                        deviceId, response != null ? response.getMessage() : "response is null");
                return null;
            }

            BiometricResultVO result = response.getData();
            if (result == null || result.getMatchedUserId() == null) {
                log.info("[生物识别] 未识别到匹配用户: deviceId={}", deviceId);
                return null;
            }

            Long userId = result.getMatchedUserId();
            double confidence = result.getConfidence();

            log.info("[生物识别] 人脸识别成功: deviceId={}, userId={}, confidence={}",
                     deviceId, userId, confidence);

            // 4. 缓存识别结果（用于防重复打卡）
            biometricCacheManager.cacheRecognizeResult(deviceId, faceImageData, userId);

            return userId;

        } catch (Exception e) {
            log.error("[生物识别] 人脸识别异常: deviceId={}, error={}",
                     deviceId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 1:N指纹识别
     */
    @Override
    public Long recognizeFingerprint(String fingerprintData, String deviceId) {
        log.info("[生物识别] 开始指纹识别: deviceId={}", deviceId);

        try {
            // 1. 参数校验
            if (StringUtils.isBlank(fingerprintData)) {
                log.warn("[生物识别] 指纹数据为空");
                return null;
            }

            // 2. 调用生物识别服务
            Map<String, Object> params = new HashMap<>();
            params.put("fingerprintData", fingerprintData);
            params.put("deviceId", deviceId);
            params.put("recognizeType", "1:N");

            ResponseDTO<BiometricResultVO> response = gatewayServiceClient.callBiometricService(
                "/api/biometric/recognize-fingerprint",
                HttpMethod.POST,
                params,
                new TypeReference<ResponseDTO<BiometricResultVO>>() {}
            );

            // 3. 处理识别结果
            if (response == null || !response.isSuccessful()) {
                log.warn("[生物识别] 指纹识别失败: deviceId={}, error={}",
                        deviceId, response != null ? response.getMessage() : "response is null");
                return null;
            }

            BiometricResultVO result = response.getData();
            if (result == null || result.getMatchedUserId() == null) {
                log.info("[生物识别] 未识别到匹配用户: deviceId={}", deviceId);
                return null;
            }

            Long userId = result.getMatchedUserId();
            double confidence = result.getConfidence();

            log.info("[生物识别] 指纹识别成功: deviceId={}, userId={}, confidence={}",
                     deviceId, userId, confidence);

            return userId;

        } catch (Exception e) {
            log.error("[生物识别] 指纹识别异常: deviceId={}, error={}",
                     deviceId, e.getMessage(), e);
            return null;
        }
    }
}

// 3. 修改StandardAttendanceProcess实现生物识别
@Service
@Slf4j
public class StandardAttendanceProcess implements AttendanceProcessTemplate {

    @Resource
    private BiometricService biometricService;

    @Override
    public AttendanceResult process(PunchForm punchForm) {
        log.info("[标准考勤流程] 开始处理: punchType={}, deviceId={}",
                 punchForm.getPunchType(), punchForm.getDeviceId());

        try {
            // 1. 生物识别（人脸或指纹）
            Long userId = performBiometricRecognition(punchForm);

            if (userId == null) {
                log.warn("[标准考勤流程] 生物识别失败: deviceId={}",
                         punchForm.getDeviceId());
                return AttendanceResult.fail("生物识别失败，无法识别用户身份");
            }

            log.info("[标准考勤流程] 生物识别成功: userId={}", userId);

            // 2. 构建身份信息
            UserIdentity identity = new UserIdentity();
            identity.setUserId(userId);
            identity.setRecognizeType(punchForm.getBiometricType()); // FACE/FINGERPRINT
            identity.setDeviceId(punchForm.getDeviceId());
            identity.setRecognizeTime(LocalDateTime.now());

            // 3. 执行考勤计算
            return calculateAttendance(identity, punchForm);

        } catch (Exception e) {
            log.error("[标准考勤流程] 处理异常: error={}", e.getMessage(), e);
            return AttendanceResult.error("考勤处理异常: " + e.getMessage());
        }
    }

    /**
     * 执行生物识别
     */
    private Long performBiometricRecognition(PunchForm punchForm) {
        String biometricType = punchForm.getBiometricType();
        String biometricData = punchForm.getBiometricData();
        String deviceId = punchForm.getDeviceId();

        log.info("[标准考勤流程] 开始生物识别: type={}, deviceId={}",
                 biometricType, deviceId);

        // 根据生物识别类型调用不同的识别方法
        if ("FACE".equalsIgnoreCase(biometricType)) {
            return biometricService.recognizeFace(biometricData, deviceId);

        } else if ("FINGERPRINT".equalsIgnoreCase(biometricType)) {
            return biometricService.recognizeFingerprint(biometricData, deviceId);

        } else {
            log.warn("[标准考勤流程] 不支持的生物识别类型: type={}", biometricType);
            return null;
        }
    }

    /**
     * 计算考勤结果
     */
    private AttendanceResult calculateAttendance(UserIdentity identity, PunchForm punchForm) {
        // ... 考勤计算逻辑
        return AttendanceResult.success(identity.getUserId());
    }
}
```

**开发规范**:
- ✅ 使用GatewayServiceClient调用生物识别服务
- ✅ 日志记录识别过程和结果
- ✅ 异常处理要捕获并记录详细日志
- ✅ 返回null表示识别失败，不要抛异常
- ✅ 识别结果可以缓存，防止短时间内重复识别

**注意事项**:
1. **性能优化**: 生物识别是耗时操作，考虑异步处理
2. **设备兼容**: 不同厂商设备的生物特征数据格式可能不同
3. **准确率**: 设置置信度阈值，低于阈值的识别结果拒绝
4. **防作弊**: 同一人短时间多次打卡需要防作弊机制

**验收标准**:
- ✅ 能正确识别用户人脸或指纹
- ✅ 识别失败时返回null
- ✅ 识别置信度低于阈值时拒绝
- ✅ 日志记录完整
- ✅ 单元测试覆盖率≥85%

---

#### TODO-005: WebSocket实时推送和RabbitMQ消息

**文件位置**:
- `microservices-ioedream-attendance-service/src/main/java/net/lab1024/sa/attendance/template/impl/StandardAttendanceProcess.java:120`

**当前状态**:
```java
// TODO: 实现WebSocket推送、RabbitMQ消息等
```

**业务需求分析**:
1. **实时推送**: 用户打卡后，实时推送考勤结果到前端
2. **消息队列**: 使用RabbitMQ异步处理考勤事件
3. **多端同步**: 打卡结果实时同步到Web、移动端等多个终端

**业务流程图**:
```
┌────────────────────┐
│  用户打卡完成      │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  记录考勤数据      │
│  保存到数据库      │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  发送RabbitMQ消息  │
│  - 考勤事件队列    │
│  - 异步处理        │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  WebSocket实时推送 │
│  - 推送到前端      │
│  - 多端同步        │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  后续异步处理      │
│  - 考勤统计        │
│  - 异常检测        │
│  - 报表生成        │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. WebSocket推送服务
@Service
@Slf4j
public class AttendanceWebSocketService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 推送考勤结果到用户
     */
    public void pushAttendanceResultToUser(Long userId, AttendanceResultVO result) {
        try {
            log.info("[WebSocket推送] 推送考勤结果: userId={}, status={}",
                     userId, result.getStatus());

            // 构建推送消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ATTENDANCE_RESULT");
            message.put("userId", userId);
            message.put("data", result);
            message.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 推送到用户专属频道
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/attendance",
                message
            );

            log.info("[WebSocket推送] 推送成功: userId={}", userId);

        } catch (Exception e) {
            log.error("[WebSocket推送] 推送失败: userId={}, error={}",
                     userId, e.getMessage(), e);
        }
    }

    /**
     * 推送考勤异常告警
     */
    public void pushAttendanceAlert(AttendanceAlertVO alert) {
        try {
            log.info("[WebSocket推送] 推送考勤告警: alertType={}, userId={}",
                     alert.getAlertType(), alert.getUserId());

            // 构建告警消息
            Map<String, Object> message = new HashMap<>();
            message.put("type", "ATTENDANCE_ALERT");
            message.put("alert", alert);
            message.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 推送到管理员频道
            messagingTemplate.convertAndSend("/topic/admin/attendance-alerts", message);

            log.info("[WebSocket推送] 告警推送成功");

        } catch (Exception e) {
            log.error("[WebSocket推送] 告警推送失败: error={}", e.getMessage(), e);
        }
    }
}

// 2. RabbitMQ消息发送服务
@Service
@Slf4j
public class AttendanceMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送考勤事件消息
     */
    public void sendAttendanceEvent(AttendanceEventVO event) {
        try {
            log.info("[RabbitMQ] 发送考勤事件: userId={}, eventType={}",
                     event.getUserId(), event.getEventType());

            // 设置消息ID
            Message message = MessageBuilder
                .withBody(JSON.toJSONString(event).getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(UUID.randomUUID().toString())
                .setTimestamp(new Date())
                .build();

            // 发送到考勤事件队列
            rabbitTemplate.send("attendance.event.exchange",
                               "attendance.event.routing.key",
                               message);

            log.info("[RabbitMQ] 考勤事件发送成功: messageId={}", message.getMessageId());

        } catch (Exception e) {
            log.error("[RabbitMQ] 发送考勤事件失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 发送考勤异常消息
     */
    public void sendAttendanceAnomaly(AttendanceAnomalyVO anomaly) {
        try {
            log.info("[RabbitMQ] 发送考勤异常: anomalyType={}, userId={}",
                     anomaly.getAnomalyType(), anomaly.getUserId());

            Message message = MessageBuilder
                .withBody(JSON.toJSONString(anomaly).getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(UUID.randomUUID().toString())
                .setTimestamp(new Date())
                .build();

            // 发送到异常处理队列
            rabbitTemplate.send("attendance.anomaly.exchange",
                               "attendance.anomaly.routing.key",
                               message);

            log.info("[RabbitMQ] 考勤异常发送成功");

        } catch (Exception e) {
            log.error("[RabbitMQ] 发送考勤异常失败: error={}", e.getMessage(), e);
        }
    }
}

// 3. RabbitMQ消息消费者
@Component
@Slf4j
public class AttendanceMessageConsumer {

    @Resource
    private AttendanceStatisticsManager attendanceStatisticsManager;

    @Resource
    private AttendanceAnomalyDetector attendanceAnomalyDetector;

    /**
     * 消费考勤事件消息
     */
    @RabbitListener(queues = "attendance.event.queue")
    public void handleAttendanceEvent(String message) {
        try {
            log.info("[RabbitMQ消费] 收到考勤事件: message={}", message);

            AttendanceEventVO event = JSON.parseObject(message,
                AttendanceEventVO.class);

            // 异步更新考勤统计
            attendanceStatisticsManager.updateStatisticsAsync(event);

            log.info("[RabbitMQ消费] 考勤事件处理完成: userId={}", event.getUserId());

        } catch (Exception e) {
            log.error("[RabbitMQ消费] 处理考勤事件失败: error={}", e.getMessage(), e);
        }
    }

    /**
     * 消费考勤异常消息
     */
    @RabbitListener(queues = "attendance.anomaly.queue")
    public void handleAttendanceAnomaly(String message) {
        try {
            log.info("[RabbitMQ消费] 收到考勤异常: message={}", message);

            AttendanceAnomalyVO anomaly = JSON.parseObject(message,
                AttendanceAnomalyVO.class);

            // 异常检测和处理
            attendanceAnomalyDetector.detectAndHandle(anomaly);

            log.info("[RabbitMQ消费] 考勤异常处理完成");

        } catch (Exception e) {
            log.error("[RabbitMQ消费] 处理考勤异常失败: error={}", e.getMessage(), e);
        }
    }
}

// 4. 修改StandardAttendanceProcess集成推送和消息
@Service
@Slf4j
public class StandardAttendanceProcess implements AttendanceProcessTemplate {

    @Resource
    private AttendanceWebSocketService webSocketService;

    @Resource
    private AttendanceMessageProducer messageProducer;

    @Override
    public AttendanceResult process(PunchForm punchForm) {
        // ... 生物识别和考勤计算逻辑 ...

        AttendanceResult result = calculateAttendance(identity, punchForm);

        // 考勤完成后，推送结果和发送消息
        notifyAttendanceResult(identity, result);

        return result;
    }

    /**
     * 通知考勤结果（WebSocket + RabbitMQ）
     */
    private void notifyAttendanceResult(UserIdentity identity, AttendanceResult result) {
        try {
            // 1. WebSocket实时推送到用户
            AttendanceResultVO resultVO = buildResultVO(identity, result);
            webSocketService.pushAttendanceResultToUser(identity.getUserId(), resultVO);

            // 2. 发送RabbitMQ消息进行异步处理
            AttendanceEventVO event = buildEventVO(identity, result);
            messageProducer.sendAttendanceEvent(event);

            log.info("[标准考勤流程] 考勤结果通知完成: userId={}", identity.getUserId());

        } catch (Exception e) {
            log.error("[标准考勤流程] 通知考勤结果失败: error={}", e.getMessage(), e);
            // 通知失败不影响主流程
        }
    }

    /**
     * 构建考勤结果VO
     */
    private AttendanceResultVO buildResultVO(UserIdentity identity,
                                             AttendanceResult result) {
        AttendanceResultVO vo = new AttendanceResultVO();
        vo.setUserId(identity.getUserId());
        vo.setUserName(result.getUserName());
        vo.setStatus(result.getStatus()); // NORMAL/LATE/EARLY_LEAVE/ABSENT
        vo.setPunchTime(identity.getRecognizeTime());
        vo.setDeviceId(identity.getDeviceId());
        vo.setShiftName(result.getShiftName());
        return vo;
    }

    /**
     * 构建考勤事件VO
     */
    private AttendanceEventVO buildEventVO(UserIdentity identity,
                                           AttendanceResult result) {
        AttendanceEventVO event = new AttendanceEventVO();
        event.setUserId(identity.getUserId());
        event.setEventType("PUNCH");
        event.setEventTime(identity.getRecognizeTime());
        event.setDeviceId(identity.getDeviceId());
        event.setAttendaceStatus(result.getStatus());
        return event;
    }
}
```

**WebSocket配置**:

```java
// 5. WebSocket配置类
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理
        config.enableSimpleBroker("/topic", "/queue");

        // 设置应用目标前缀
        config.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，允许跨域
        registry.addEndpoint("/ws/attendance")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
```

**RabbitMQ配置**:

```java
// 6. RabbitMQ配置类
@Configuration
public class RabbitMQConfig {

    /**
     * 考勤事件交换机
     */
    @Bean
    public DirectExchange attendanceEventExchange() {
        return new DirectExchange("attendance.event.exchange", true, false);
    }

    /**
     * 考勤事件队列
     */
    @Bean
    public Queue attendanceEventQueue() {
        return QueueBuilder.durable("attendance.event.queue").build();
    }

    /**
     * 考勤事件绑定
     */
    @Bean
    public Binding attendanceEventBinding() {
        return BindingBuilder.bind(attendanceEventQueue())
                .to(attendanceEventExchange())
                .with("attendance.event.routing.key");
    }

    /**
     * 考勤异常交换机
     */
    @Bean
    public DirectExchange attendanceAnomalyExchange() {
        return new DirectExchange("attendance.anomaly.exchange", true, false);
    }

    /**
     * 考勤异常队列
     */
    @Bean
    public Queue attendanceAnomalyQueue() {
        return QueueBuilder.durable("attendance.anomaly.queue").build();
    }

    /**
     * 考勤异常绑定
     */
    @Bean
    public Binding attendanceAnomalyBinding() {
        return BindingBuilder.bind(attendanceAnomalyQueue())
                .to(attendanceAnomalyExchange())
                .with("attendance.anomaly.routing.key");
    }
}
```

**开发规范**:
- ✅ WebSocket和RabbitMQ使用独立的Service类
- ✅ 推送和消息发送失败不影响主流程
- ✅ 使用异步方式进行消息处理
- ✅ 消息消费者要记录日志和异常处理
- ✅ 消息要设置唯一ID，便于追踪

**注意事项**:
1. **消息幂等性**: 消息可能重复消费，要保证幂等性
2. **消息顺序**: 消费者要保证消息的顺序性
3. **死信队列**: 配置死信队列处理失败消息
4. **连接管理**: WebSocket断线要能自动重连
5. **性能优化**: 批量推送消息，减少网络开销

**验收标准**:
- ✅ 用户打卡后能实时收到WebSocket推送
- ✅ RabbitMQ消息能正常发送和消费
- ✅ 消息消费失败能进入死信队列
- ✅ WebSocket断线后能自动重连
- ✅ 单元测试覆盖率≥80%

---

### 模块3: 访客模块完善（2项）

#### TODO-006: 临时访客中心验证逻辑

**文件位置**:
- `microservices-ioedream-visitor-service/src/main/java/net/lab1024/sa/visitor/strategy/impl/TemporaryVisitorStrategy.java:32`

**当前状态**:
```java
// TODO: 实现临时访客中心验证逻辑
// 1. 查询预约记录
// 2. 验证预约状态和时间范围
// 3. 检查访问次数限制
// 4. 返回验证结果
```

**业务需求分析**:
1. **预约验证**: 验证访客是否有有效的预约记录
2. **时间验证**: 检查访问时间是否在预约时间范围内
3. **次数限制**: 验证访问次数是否超过预约限制
4. **状态管理**: 管理访客的访问状态（待访问、访问中、已完成）

**业务流程图**:
```
┌────────────────────┐
│  访客到达/扫码     │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  查询预约记录      │
│  - 访客ID          │
│  - 访客码          │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  存在    不存在
   │       │
   ▼       ▼
验证预约  返回失败
   │
   ▼
┌────────────────────┐
│  验证预约状态      │
│  - 是否已审批      │
│  - 是否已失效      │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  有效    无效
   │       │
   ▼       ▼
验证时间  返回失败
   │
   ▼
┌────────────────────┐
│  验证访问时间      │
│  - 是否在有效期    │
│  - 是否在时段内    │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  有效    无效
   │       │
   ▼       ▼
验证次数  返回失败
   │
   ▼
┌────────────────────┐
│  验证访问次数      │
│  - 是否超限        │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  正常    超限
   │       │
   ▼       ▼
返回成功  返回失败
```

**技术实现方案**:

```java
// 1. 临时访客验证策略实现
@Service
@Slf4j
public class TemporaryVisitorStrategy implements VisitorVerificationStrategy {

    @Resource
    private VisitorAppointmentManager visitorAppointmentManager;

    @Resource
    private VisitorAccessRecordManager visitorAccessRecordManager;

    @Resource
    private VisitorBiometricManager visitorBiometricManager;

    /**
     * 验证临时访客
     */
    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        log.info("[临时访客验证] 开始验证: visitorId={}", visitorId);

        try {
            // 1. 查询预约记录
            VisitorAppointmentEntity appointment = visitorAppointmentManager
                .getValidAppointmentByVisitorId(visitorId);

            if (appointment == null) {
                log.warn("[临时访客验证] 预约记录不存在: visitorId={}", visitorId);
                return VisitorVerificationResult.fail("预约记录不存在");
            }

            log.info("[临时访客验证] 找到预约记录: appointmentId={}, status={}",
                     appointment.getAppointmentId(), appointment.getStatus());

            // 2. 验证预约状态
            if (!isValidAppointmentStatus(appointment)) {
                log.warn("[临时访客验证] 预约状态无效: status={}",
                         appointment.getStatus());
                return VisitorVerificationResult.fail("预约状态无效: " +
                    appointment.getStatus());
            }

            // 3. 验证访问时间
            LocalDateTime now = LocalDateTime.now();
            if (!isWithinValidTimeRange(appointment, now)) {
                log.warn("[临时访客验证] 不在有效访问时间内: now={}, validStart={}, validEnd={}",
                         now, appointment.getValidStartTime(), appointment.getValidEndTime());
                return VisitorVerificationResult.fail("不在有效访问时间内");
            }

            // 4. 验证访问次数
            int accessCount = visitorAccessRecordManager.countAccessByAppointmentId(
                appointment.getAppointmentId());

            if (accessCount >= appointment.getMaxAccessCount()) {
                log.warn("[临时访客验证] 访问次数已达上限: accessCount={}, maxCount={}",
                         accessCount, appointment.getMaxAccessCount());
                return VisitorVerificationResult.fail("访问次数已达上限");
            }

            log.info("[临时访客验证] 验证通过: visitorId={}, appointmentId={}",
                     visitorId, appointment.getAppointmentId());

            // 5. 构建验证结果
            return VisitorVerificationResult.success()
                .visitorId(visitorId)
                .visitorType(VISITOR_TYPE_TEMPORARY)
                .appointmentId(appointment.getAppointmentId())
                .accessCount(accessCount + 1)
                .maxAccessCount(appointment.getMaxAccessCount())
                .validStartTime(appointment.getValidStartTime())
                .validEndTime(appointment.getValidEndTime())
                .visitedAreas(appointment.getVisitedAreas());

        } catch (Exception e) {
            log.error("[临时访客验证] 验证异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
            return VisitorVerificationResult.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证预约状态是否有效
     */
    private boolean isValidAppointmentStatus(VisitorAppointmentEntity appointment) {
        Integer status = appointment.getStatus();

        // 1-待审批 2-已审批 3-访问中 4-已完成 5-已拒绝 6-已取消
        return status != null &&
               (status == 2 || status == 3); // 已审批或访问中
    }

    /**
     * 验证是否在有效时间范围内
     */
    private boolean isWithinValidTimeRange(VisitorAppointmentEntity appointment,
                                          LocalDateTime currentTime) {
        // 1. 验证日期范围
        LocalDate currentDate = currentTime.toLocalDate();
        LocalDate validStartDate = appointment.getValidStartTime().toLocalDate();
        LocalDate validEndDate = appointment.getValidEndTime().toLocalDate();

        if (currentDate.isBefore(validStartDate) || currentDate.isAfter(validEndDate)) {
            return false;
        }

        // 2. 验证时间范围（同一天）
        if (currentDate.equals(validStartDate)) {
            LocalTime currentTimeOnly = currentTime.toLocalTime();
            LocalTime validStartTimeOnly = appointment.getValidStartTime().toLocalTime();
            LocalTime validEndTimeOnly = appointment.getValidEndTime().toLocalTime();

            if (currentTimeOnly.isBefore(validStartTimeOnly) ||
                currentTimeOnly.isAfter(validEndTimeOnly)) {
                return false;
            }
        }

        return true;
    }
}

// 2. 访客预约Manager
@Component
@Slf4j
public class VisitorAppointmentManager {

    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    /**
     * 根据访客ID查询有效预约
     */
    public VisitorAppointmentEntity getValidAppointmentByVisitorId(Long visitorId) {
        try {
            log.debug("[访客预约] 查询有效预约: visitorId={}", visitorId);

            LambdaQueryWrapper<VisitorAppointmentEntity> queryWrapper =
                new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorAppointmentEntity::getVisitorId, visitorId)
                       .in(VisitorAppointmentEntity::getStatus, 2, 3) // 已审批或访问中
                       .ge(VisitorAppointmentEntity::getValidEndTime, LocalDateTime.now())
                       .orderByDesc(VisitorAppointmentEntity::getCreateTime)
                       .last("LIMIT 1");

            VisitorAppointmentEntity appointment = visitorAppointmentDao.selectOne(queryWrapper);

            if (appointment != null) {
                log.debug("[访客预约] 找到有效预约: appointmentId={}", appointment.getAppointmentId());
            }

            return appointment;

        } catch (Exception e) {
            log.error("[访客预约] 查询异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 更新预约状态
     */
    public void updateAppointmentStatus(Long appointmentId, Integer newStatus) {
        try {
            log.info("[访客预约] 更新预约状态: appointmentId={}, newStatus={}",
                     appointmentId, newStatus);

            VisitorAppointmentEntity appointment = new VisitorAppointmentEntity();
            appointment.setAppointmentId(appointmentId);
            appointment.setStatus(newStatus);
            appointment.setUpdateTime(LocalDateTime.now());

            visitorAppointmentDao.updateById(appointment);

            log.info("[访客预约] 预约状态更新成功");

        } catch (Exception e) {
            log.error("[访客预约] 更新状态异常: appointmentId={}, error={}",
                     appointmentId, e.getMessage(), e);
            throw new SystemException("UPDATE_APPOINTMENT_FAILED", "更新预约状态失败", e);
        }
    }
}

// 3. 访客访问记录Manager
@Component
@Slf4j
public class VisitorAccessRecordManager {

    @Resource
    private VisitorAccessRecordDao visitorAccessRecordDao;

    /**
     * 统计预约的访问次数
     */
    public int countAccessByAppointmentId(Long appointmentId) {
        try {
            log.debug("[访问记录] 统计访问次数: appointmentId={}", appointmentId);

            LambdaQueryWrapper<VisitorAccessRecordEntity> queryWrapper =
                new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorAccessRecordEntity::getAppointmentId, appointmentId);

            Integer count = visitorAccessRecordDao.selectCount(queryWrapper);

            log.debug("[访问记录] 访问次数: appointmentId={}, count={}", appointmentId, count);

            return count != null ? count : 0;

        } catch (Exception e) {
            log.error("[访问记录] 统计异常: appointmentId={}, error={}",
                     appointmentId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 创建访问记录
     */
    public VisitorAccessRecordEntity createAccessRecord(Long visitorId,
                                                       VisitorVerificationResult result) {
        try {
            log.info("[访问记录] 创建访问记录: visitorId={}, appointmentId={}",
                     visitorId, result.getAppointmentId());

            VisitorAccessRecordEntity record = new VisitorAccessRecordEntity();
            record.setVisitorId(visitorId);
            record.setAppointmentId(result.getAppointmentId());
            record.setAccessType(result.getVisitorType());
            record.setAccessTime(LocalDateTime.now());
            record.setAccessStatus("SUCCESS");
            record.setAccessCount(result.getAccessCount());
            record.setValidStartTime(result.getValidStartTime());
            record.setValidEndTime(result.getValidEndTime());

            visitorAccessRecordDao.insert(record);

            log.info("[访问记录] 访问记录创建成功: recordId={}", record.getRecordId());

            return record;

        } catch (Exception e) {
            log.error("[访问记录] 创建异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
            throw new SystemException("CREATE_ACCESS_RECORD_FAILED", "创建访问记录失败", e);
        }
    }
}
```

**开发规范**:
- ✅ 使用策略模式实现不同类型访客的验证逻辑
- ✅ 每个验证步骤都要有详细日志
- ✅ 验证失败要明确返回失败原因
- ✅ 使用Builder模式构建验证结果
- ✅ 异常处理要捕获并转换为业务异常

**注意事项**:
1. **时区问题**: 访问时间验证要注意时区问题
2. **并发控制**: 同一访客短时间多次验证要防止并发问题
3. **缓存优化**: 预约记录可以缓存，减少数据库查询
4. **安全考虑**: 验证数据要进行防篡改处理

**验收标准**:
- ✅ 能正确验证临时访客的预约信息
- ✅ 能准确判断访问时间是否有效
- ✅ 能正确统计访问次数
- ✅ 验证失败时返回明确原因
- ✅ 单元测试覆盖率≥90%

---

#### TODO-007: 常客边缘验证逻辑

**文件位置**:
- `microservices-ioedream-visitor-service/src/main/java/net/lab1024/sa/visitor/strategy/impl/RegularVisitorStrategy.java:32`

**当前状态**:
```java
// TODO: 实现常客边缘验证逻辑
// 1. 查询电子通行证
// 2. 验证有效期
// 3. 验证访问权限（区域、时间段）
// 4. 返回验证结果
```

**业务需求分析**:
1. **电子通行证**: 常客拥有电子通行证，存储在设备端
2. **边缘验证**: 设备端直接验证，无需联网
3. **权限控制**: 验证常客是否有权访问当前区域
4. **有效期管理**: 验证通行证是否在有效期内

**业务流程图**:
```
┌────────────────────┐
│  常客到达/刷卡     │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  设备端读取        │
│  电子通行证        │
│  - 通行证ID        │
│  - 用户ID          │
│  - 有效期          │
│  - 访问权限        │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  设备端验证有效期  │
│  - 是否过期        │
│  - 是否在时段内    │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  有效    过期
   │       │
   ▼       ▼
验证权限  返回失败
   │
   ▼
┌────────────────────┐
│  设备端验证权限    │
│  - 是否可访问区域  │
│  - 是否在允许时段  │
└──────┬─────────────┘
       │
   ┌───┴───┐
   │       │
  有权    无权
   │       │
   ▼       ▼
允许通行  返回失败
   │
       ▼
┌────────────────────┐
│  本地记录访问日志  │
└──────┬─────────────┘
       │
       ▼
┌────────────────────┐
│  后续批量上传到    │
│  服务器            │
└────────────────────┘
```

**技术实现方案**:

```java
// 1. 常客验证策略实现
@Service
@Slf4j
public class RegularVisitorStrategy implements VisitorVerificationStrategy {

    @Resource
    private VisitorPassManager visitorPassManager;

    @Resource
    private VisitorAccessRecordManager visitorAccessRecordManager;

    /**
     * 验证常客（设备端边缘验证）
     */
    @Override
    public VisitorVerificationResult verify(Long visitorId, String verificationData) {
        log.info("[常客验证] 开始验证: visitorId={}", visitorId);

        try {
            // 1. 查询电子通行证
            VisitorPassEntity pass = visitorPassManager.getValidPassByVisitorId(visitorId);

            if (pass == null) {
                log.warn("[常客验证] 电子通行证不存在: visitorId={}", visitorId);
                return VisitorVerificationResult.fail("电子通行证不存在");
            }

            log.info("[常客验证] 找到通行证: passId={}, status={}",
                     pass.getPassId(), pass.getStatus());

            // 2. 验证通行证状态
            if (!isValidPassStatus(pass)) {
                log.warn("[常客验证] 通行证状态无效: status={}", pass.getStatus());
                return VisitorVerificationResult.fail("通行证状态无效: " + pass.getStatus());
            }

            // 3. 验证有效期
            LocalDateTime now = LocalDateTime.now();
            if (!isWithinValidPeriod(pass, now)) {
                log.warn("[常客验证] 通行证已过期: now={}, expireTime={}",
                         now, pass.getExpireTime());
                return VisitorVerificationResult.fail("通行证已过期");
            }

            // 4. 验证访问权限（区域、时间段）
            String deviceId = extractDeviceId(verificationData);
            if (!hasAccessPermission(pass, deviceId, now)) {
                log.warn("[常客验证] 无访问权限: deviceId={}", deviceId);
                return VisitorVerificationResult.fail("无访问权限");
            }

            log.info("[常客验证] 验证通过: visitorId={}, passId={}",
                     visitorId, pass.getPassId());

            // 5. 构建验证结果
            return VisitorVerificationResult.success()
                .visitorId(visitorId)
                .visitorType(VISITOR_TYPE_REGULAR)
                .passId(pass.getPassId())
                .passType(pass.getPassType())
                .accessAreas(pass.getAccessAreas())
                .validStartTime(pass.getStartTime())
                .validEndTime(pass.getExpireTime());

        } catch (Exception e) {
            log.error("[常客验证] 验证异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
            return VisitorVerificationResult.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 验证通行证状态是否有效
     */
    private boolean isValidPassStatus(VisitorPassEntity pass) {
        Integer status = pass.getStatus();

        // 1-正常 2-挂失 3-注销 4-过期
        return status != null && status == 1; // 正常状态
    }

    /**
     * 验证是否在有效期内
     */
    private boolean isWithinValidPeriod(VisitorPassEntity pass, LocalDateTime currentTime) {
        // 1. 验证开始时间
        if (pass.getStartTime() != null && currentTime.isBefore(pass.getStartTime())) {
            return false;
        }

        // 2. 验证过期时间
        if (pass.getExpireTime() != null && currentTime.isAfter(pass.getExpireTime())) {
            return false;
        }

        return true;
    }

    /**
     * 验证是否有访问权限
     */
    private boolean hasAccessPermission(VisitorPassEntity pass, String deviceId,
                                       LocalDateTime currentTime) {
        try {
            // 1. 获取设备所属区域
            String areaId = getDeviceAreaId(deviceId);
            if (StringUtils.isBlank(areaId)) {
                log.warn("[常客验证] 设备未关联区域: deviceId={}", deviceId);
                return false;
            }

            // 2. 检查通行证是否包含该区域的访问权限
            String accessAreas = pass.getAccessAreas(); // JSON数组字符串
            if (StringUtils.isBlank(accessAreas)) {
                log.warn("[常客验证] 通行证无访问区域: passId={}", pass.getPassId());
                return false;
            }

            List<String> areaList = JSON.parseArray(accessAreas, String.class);
            if (!areaList.contains(areaId)) {
                log.warn("[常客验证] 通行证不包含该区域: passId={}, areaId={}",
                         pass.getPassId(), areaId);
                return false;
            }

            // 3. 验证时间段权限（如果有）
            if (StringUtils.isNotBlank(pass.getTimeSlots())) {
                if (!isWithinTimeSlots(pass.getTimeSlots(), currentTime)) {
                    log.warn("[常客验证] 当前时间不在允许时段内: currentTime={}", currentTime);
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("[常客验证] 验证访问权限异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证是否在允许的时间段内
     */
    private boolean isWithinTimeSlots(String timeSlotsJson, LocalDateTime currentTime) {
        try {
            List<TimeSlot> timeSlots = JSON.parseArray(timeSlotsJson, TimeSlot.class);

            LocalTime currentTimeOnly = currentTime.toLocalTime();
            DayOfWeek currentDayOfWeek = currentTime.getDayOfWeek();

            for (TimeSlot timeSlot : timeSlots) {
                // 1. 检查星期几
                if (!timeSlot.getDaysOfWeek().contains(currentDayOfWeek.getValue())) {
                    continue;
                }

                // 2. 检查时间范围
                if (!currentTimeOnly.isBefore(timeSlot.getStartTime()) &&
                    !currentTimeOnly.isAfter(timeSlot.getEndTime())) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("[常客验证] 解析时间段异常: error={}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 从验证数据中提取设备ID
     */
    private String extractDeviceId(String verificationData) {
        try {
            if (StringUtils.isBlank(verificationData)) {
                return null;
            }

            // 假设verificationData是JSON格式
            Map<String, Object> data = JSON.parseObject(verificationData,
                new TypeReference<Map<String, Object>>() {});

            return (String) data.get("deviceId");

        } catch (Exception e) {
            log.error("[常客验证] 提取设备ID异常: error={}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取设备所属区域ID
     */
    private String getDeviceAreaId(String deviceId) {
        // 调用设备服务获取设备信息
        // 这里简化处理，实际应该通过GatewayServiceClient调用设备服务
        return "AREA_001";
    }

    /**
     * 时间段内部类
     */
    @Data
    public static class TimeSlot {
        private List<Integer> daysOfWeek; // 1-7 (周一到周日)
        private LocalTime startTime;
        private LocalTime endTime;
    }
}

// 2. 电子通行证Manager
@Component
@Slf4j
public class VisitorPassManager {

    @Resource
    private VisitorPassDao visitorPassDao;

    @Resource
    private VisitorPassCacheManager visitorPassCacheManager;

    /**
     * 根据访客ID查询有效通行证
     */
    public VisitorPassEntity getValidPassByVisitorId(Long visitorId) {
        try {
            log.debug("[通行证] 查询有效通行证: visitorId={}", visitorId);

            // 1. 先从缓存查询
            VisitorPassEntity cachedPass = visitorPassCacheManager.getPassByVisitorId(visitorId);
            if (cachedPass != null) {
                log.debug("[通行证] 缓存命中: passId={}", cachedPass.getPassId());
                return cachedPass;
            }

            // 2. 缓存未命中，查询数据库
            LambdaQueryWrapper<VisitorPassEntity> queryWrapper =
                new LambdaQueryWrapper<>();
            queryWrapper.eq(VisitorPassEntity::getVisitorId, visitorId)
                       .eq(VisitorPassEntity::getStatus, 1) // 正常状态
                       .gt(VisitorPassEntity::getExpireTime, LocalDateTime.now()) // 未过期
                       .orderByDesc(VisitorPassEntity::getCreateTime)
                       .last("LIMIT 1");

            VisitorPassEntity pass = visitorPassDao.selectOne(queryWrapper);

            if (pass != null) {
                log.debug("[通行证] 找到有效通行证: passId={}", pass.getPassId());

                // 3. 缓存通行证
                visitorPassCacheManager.cachePass(pass);
            }

            return pass;

        } catch (Exception e) {
            log.error("[通行证] 查询异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 创建电子通行证
     */
    public VisitorPassEntity createPass(VisitorPassCreateForm form) {
        try {
            log.info("[通行证] 创建电子通行证: visitorId={}, passType={}",
                     form.getVisitorId(), form.getPassType());

            VisitorPassEntity pass = new VisitorPassEntity();
            pass.setVisitorId(form.getVisitorId());
            pass.setPassType(form.getPassType()); // PERMANENT/TEMPORARY
            pass.setPassNo(generatePassNo());
            pass.setStatus(1); // 正常
            pass.setStartTime(form.getStartTime());
            pass.setExpireTime(form.getExpireTime());
            pass.setAccessAreas(JSON.toJSONString(form.getAccessAreas()));
            pass.setTimeSlots(JSON.toJSONString(form.getTimeSlots()));
            pass.setCreateTime(LocalDateTime.now());
            pass.setUpdateTime(LocalDateTime.now());

            visitorPassDao.insert(pass);

            log.info("[通行证] 通行证创建成功: passId={}, passNo={}",
                     pass.getPassId(), pass.getPassNo());

            // 下发通行证到设备
            issuePassToDevice(pass);

            return pass;

        } catch (Exception e) {
            log.error("[通行证] 创建异常: error={}", e.getMessage(), e);
            throw new SystemException("CREATE_PASS_FAILED", "创建通行证失败", e);
        }
    }

    /**
     * 下发通行证到设备
     */
    private void issuePassToDevice(VisitorPassEntity pass) {
        try {
            log.info("[通行证] 下发通行证到设备: passId={}", pass.getPassId());

            // 1. 解析访问区域
            List<String> areaIds = JSON.parseArray(pass.getAccessAreas(), String.class);

            // 2. 查询每个区域的所有门禁设备
            // 3. 下发通行证到这些设备
            // 这里简化处理，实际应该通过设备通讯服务下发

            log.info("[通行证] 通行证下发完成: passId={}", pass.getPassId());

        } catch (Exception e) {
            log.error("[通行证] 下发通行证异常: passId={}, error={}",
                     pass.getPassId(), e.getMessage(), e);
            // 下发失败不影响主流程
        }
    }

    /**
     * 生成通行证编号
     */
    private String generatePassNo() {
        return "PASS_" + System.currentTimeMillis() +
               "_" + RandomUtil.randomNumbers(6);
    }
}

// 3. 通行证缓存Manager
@Component
@Slf4j
public class VisitorPassCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PASS_CACHE_PREFIX = "visitor:pass:";
    private static final long PASS_CACHE_EXPIRE_SECONDS = 3600; // 1小时

    /**
     * 缓存通行证
     */
    public void cachePass(VisitorPassEntity pass) {
        try {
            String cacheKey = PASS_CACHE_PREFIX + pass.getVisitorId();

            redisTemplate.opsForValue().set(
                cacheKey,
                pass,
                PASS_CACHE_EXPIRE_SECONDS,
                TimeUnit.SECONDS
            );

            log.debug("[通行证缓存] 缓存成功: visitorId={}", pass.getVisitorId());

        } catch (Exception e) {
            log.error("[通行证缓存] 缓存异常: visitorId={}, error={}",
                     pass.getVisitorId(), e.getMessage(), e);
        }
    }

    /**
     * 获取缓存的通行证
     */
    public VisitorPassEntity getPassByVisitorId(Long visitorId) {
        try {
            String cacheKey = PASS_CACHE_PREFIX + visitorId;

            Object cachedObj = redisTemplate.opsForValue().get(cacheKey);
            if (cachedObj != null) {
                return (VisitorPassEntity) cachedObj;
            }

            return null;

        } catch (Exception e) {
            log.error("[通行证缓存] 获取异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 删除通行证缓存
     */
    public void evictPass(Long visitorId) {
        try {
            String cacheKey = PASS_CACHE_PREFIX + visitorId;
            redisTemplate.delete(cacheKey);

            log.debug("[通行证缓存] 删除成功: visitorId={}", visitorId);

        } catch (Exception e) {
            log.error("[通行证缓存] 删除异常: visitorId={}, error={}",
                     visitorId, e.getMessage(), e);
        }
    }
}
```

**开发规范**:
- ✅ 使用策略模式实现不同类型访客的验证
- ✅ 边缘验证逻辑要考虑设备端能力限制
- ✅ 通行证信息要缓存，提高验证性能
- ✅ 通行证下发要异步处理，不阻塞主流程
- ✅ 验证失败要返回明确原因

**注意事项**:
1. **设备存储**: 设备端存储容量有限，要控制通行证数量
2. **离线验证**: 设备断网时仍能完成验证
3. **同步机制**: 服务器和设备端通行证信息要保持同步
4. **安全防护**: 通行证数据要加密存储，防止篡改
5. **性能优化**: 验证逻辑要高效，不影响通行速度

**验收标准**:
- ✅ 能正确验证常客的电子通行证
- ✅ 能准确判断通行证是否过期
- ✅ 能正确验证区域访问权限
- ✅ 能正确验证时间段访问权限
- ✅ 通行证下发到设备成功
- ✅ 单元测试覆盖率≥90%

---

## 📊 P1级重要TODO（建议实现）

由于内容较多，我将在后续部分继续分析P1和P2级别的TODO项。现在让我更新任务状态并继续：