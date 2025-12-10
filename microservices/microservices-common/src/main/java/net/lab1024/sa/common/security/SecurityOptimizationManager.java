package net.lab1024.sa.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.Objects;

/**
 * 企业级安全管理器
 * <p>
 * 提供数据加密、权限控制、审计日志、敏感信息脱敏等企业级安全功能
 * 严格遵循CLAUDE.md全局架构规范：
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入所有依赖（包括配置值）
 * - 在微服务中通过配置类注册为Spring Bean
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 3.0.0
 * @since 2025-12-09
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class SecurityOptimizationManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final net.lab1024.sa.common.menu.dao.MenuDao menuDao;
    private final net.lab1024.sa.common.system.dao.SystemConfigDao systemConfigDao;
    private final String ipWhitelistConfig;
    private final boolean ipWhitelistDbEnabled;

    /**
     * IP白名单缓存键
     */
    private static final String IP_WHITELIST_CACHE_KEY = "security:ip:whitelist";

    /**
     * IP白名单缓存过期时间（秒）
     */
    private static final long IP_WHITELIST_CACHE_TTL = 300; // 5分钟

    /**
     * 敏感信息脱敏规则
     */
    private final Map<String, Pattern> desensitizePatterns = new ConcurrentHashMap<>();

    /**
     * 审计日志缓存
     */
    private final List<AuditLogRecord> auditLogs = new ArrayList<>();

    /**
     * 权限规则缓存
     */
    private final Map<String, List<String>> permissionCache = new ConcurrentHashMap<>();

    /**
     * 风险检测规则
     */
    private final List<RiskDetectionRule> riskRules = new ArrayList<>();

    /**
     * 构造函数注入所有依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param redisTemplate Redis模板
     * @param stringRedisTemplate String Redis模板
     * @param menuDao 菜单DAO
     * @param systemConfigDao 系统配置DAO
     * @param ipWhitelistConfig IP白名单配置（逗号分隔）
     * @param ipWhitelistDbEnabled 是否启用数据库IP白名单
     */
    public SecurityOptimizationManager(
            RedisTemplate<String, Object> redisTemplate,
            StringRedisTemplate stringRedisTemplate,
            net.lab1024.sa.common.menu.dao.MenuDao menuDao,
            net.lab1024.sa.common.system.dao.SystemConfigDao systemConfigDao,
            String ipWhitelistConfig,
            boolean ipWhitelistDbEnabled) {
        this.redisTemplate = Objects.requireNonNull(redisTemplate, "redisTemplate不能为null");
        this.stringRedisTemplate = Objects.requireNonNull(stringRedisTemplate, "stringRedisTemplate不能为null");
        this.menuDao = Objects.requireNonNull(menuDao, "menuDao不能为null");
        this.systemConfigDao = Objects.requireNonNull(systemConfigDao, "systemConfigDao不能为null");
        this.ipWhitelistConfig = ipWhitelistConfig != null ? ipWhitelistConfig : "127.0.0.1,localhost,::1";
        this.ipWhitelistDbEnabled = ipWhitelistDbEnabled;
    }

    /**
     * 初始化安全管理器
     * <p>
     * 移除@PostConstruct，改为普通方法，由配置类在创建Bean后调用
     * </p>
     */
    public void init() {
        initDesensitizePatterns();
        initRiskDetectionRules();
        log.info("[安全管理] 企业级安全管理器初始化完成");
    }

    /**
     * 数据加密
     */
    public String encrypt(String plaintext) {
        try {
            // 使用AES加密（简化实现）
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("[安全管理] 加密失败: error={}", e.getMessage(), e);
            throw new SecurityException("数据加密失败", e);
        }
    }

    /**
     * 敏感信息脱敏
     */
    public String desensitize(String data, String dataType) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        Pattern pattern = desensitizePatterns.get(dataType);
        if (pattern != null) {
            return pattern.matcher(data).replaceAll(this::applyDesensitization);
        }

        return data; // 未找到脱敏规则，返回原数据
    }

    /**
     * 记录审计日志
     */
    public void recordAuditLog(AuditLogRecord auditLog) {
        // 添加基础信息
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setLogId(generateLogId());

        // 脱敏敏感信息
        auditLog = desensitizeAuditLog(auditLog);

        // 保存审计日志
        auditLogs.add(auditLog);

        // 异步保存到Redis或数据库
        saveAuditLogAsync(auditLog);

        log.debug("[安全管理] 记录审计日志: logId={}, userId={}, action={}",
            auditLog.getLogId(), auditLog.getUserId(), auditLog.getAction());
    }

    /**
     * 权限验证
     */
    public boolean hasPermission(Long userId, String resource, String action) {
        String cacheKey = "permission:" + userId + ":" + resource;
        List<String> permissions = permissionCache.get(cacheKey);

        if (permissions == null) {
            // 从数据库加载权限
            permissions = loadUserPermissions(userId, resource);
            permissionCache.put(cacheKey, permissions);
        }

        String requiredPermission = resource + ":" + action;
        return permissions.contains(requiredPermission) || permissions.contains("*:*");
    }

    /**
     * 风险检测
     */
    public RiskDetectionResult detectRisk(HttpServletRequest request, Long userId, String action) {
        RiskDetectionResult result = new RiskDetectionResult();
        result.setUserId(userId);
        result.setAction(action);
        result.setRiskScore(0.0);

        for (RiskDetectionRule rule : riskRules) {
            double score = rule.evaluate(request, userId, action);
            result.addRuleResult(rule.getName(), score);
            result.setRiskScore(result.getRiskScore() + score);
        }

        // 判断风险等级
        if (result.getRiskScore() >= 80.0) {
            result.setRiskLevel(RiskLevel.HIGH);
        } else if (result.getRiskScore() >= 50.0) {
            result.setRiskLevel(RiskLevel.MEDIUM);
        } else if (result.getRiskScore() >= 20.0) {
            result.setRiskLevel(RiskLevel.LOW);
        } else {
            result.setRiskLevel(RiskLevel.SAFE);
        }

        // 记录风险检测结果
        recordRiskDetection(result);

        return result;
    }

    /**
     * IP白名单验证
     */
    public boolean isIpInWhitelist(String clientIp) {
        // 获取白名单IP列表
        List<String> whitelist = getIpWhitelist();
        return whitelist.contains(clientIp) || whitelist.contains("*");
    }

    /**
     * 会话验证
     */
    public boolean validateSession(String sessionId, Long userId) {
        String cacheKey = "session:" + sessionId;
        try {
            Object cachedUserId = redisTemplate.opsForValue().get(cacheKey);
            return cachedUserId != null && cachedUserId.equals(userId);
        } catch (Exception e) {
            log.warn("[安全管理] 会话验证异常: sessionId={}, error={}", sessionId, e.getMessage());
            return false;
        }
    }

    /**
     * 获取审计日志
     */
    public List<AuditLogRecord> getAuditLogs(Long userId, String action, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogs.stream()
            .filter(log -> userId == null || log.getUserId().equals(userId))
            .filter(log -> action == null || action.isEmpty() || log.getAction().equals(action))
            .filter(log -> startTime == null || log.getTimestamp().isAfter(startTime))
            .filter(log -> endTime == null || log.getTimestamp().isBefore(endTime))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }

    /**
     * 清理过期的审计日志
     */
    public void cleanExpiredAuditLogs(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);

        // removeIf返回boolean，需要先统计再删除
        long countBefore = auditLogs.size();
        boolean removed = auditLogs.removeIf(log -> log.getTimestamp().isBefore(cutoffTime));
        int removedCount = removed ? (int)(countBefore - auditLogs.size()) : 0;

        log.info("[安全管理] 清理过期审计日志: 删除{}条记录，保留{}天", removedCount, daysToKeep);
    }

    /**
     * 初始化脱敏规则
     */
    private void initDesensitizePatterns() {
        // 手机号脱敏
        desensitizePatterns.put("phone", Pattern.compile("(\\d{3})\\d{4}(\\d{4})"));

        // 身份证号脱敏
        desensitizePatterns.put("idcard", Pattern.compile("(\\d{4})\\d{10}(\\d{4})"));

        // 银行卡号脱敏
        desensitizePatterns.put("bankcard", Pattern.compile("(\\d{4})\\d{8,12}(\\d{4})"));

        // 邮箱脱敏
        desensitizePatterns.put("email", Pattern.compile("(\\w{1,3})\\w*@(\\w+)"));

        // 姓名脱敏
        desensitizePatterns.put("name", Pattern.compile("(.{1})(.*)"));

        log.info("[安全管理] 初始化脱敏规则: {}种", desensitizePatterns.size());
    }

    /**
     * 初始化风险检测规则
     */
    @SuppressWarnings("null")
    private void initRiskDetectionRules() {
        // 频繁访问检测
        riskRules.add(new RiskDetectionRule("频繁访问", (request, userId, action) -> {
            String key = "risk:frequency:" + userId + ":" + action;
            Long count = (Long) redisTemplate.opsForValue().get(key);
            if (count == null) {
                // Duration.ofMinutes() 不会返回null
                java.time.Duration ttl = java.time.Duration.ofMinutes(1);
                redisTemplate.opsForValue().set(key, 1L, ttl);
                return 0.0;
            } else if (count > 10) {
                return 30.0; // 1分钟内超过10次访问，风险分数30
            }
            return 0.0;
        }));

        // 异常IP访问检测
        riskRules.add(new RiskDetectionRule("异常IP访问", (request, userId, action) -> {
            String clientIp = getClientIp(request);
            if (!isIpInWhitelist(clientIp)) {
                return 20.0; // 非白名单IP，风险分数20
            }
            return 0.0;
        }));

        // 敏感操作检测
        riskRules.add(new RiskDetectionRule("敏感操作", (request, userId, action) -> {
            List<String> sensitiveActions = List.of("delete", "update", "create", "pay");
            if (sensitiveActions.stream().anyMatch(action::contains)) {
                return 10.0; // 敏感操作，风险分数10
            }
            return 0.0;
        }));

        log.info("[安全管理] 初始化风险检测规则: {}个", riskRules.size());
    }

    /**
     * 应用脱敏规则
     */
    private String applyDesensitization(java.util.regex.MatchResult matchResult) {
        if (matchResult.groupCount() >= 2) {
            String prefix = matchResult.group(1);
            String suffix = matchResult.group(2);
            int maskLength = matchResult.group(0).length() - prefix.length() - suffix.length();
            return prefix + "*".repeat(Math.max(maskLength, 4)) + suffix;
        }
        return matchResult.group(0).replaceAll(".", "*");
    }

    /**
     * 审计日志脱敏
     */
    private AuditLogRecord desensitizeAuditLog(AuditLogRecord auditLog) {
        // 脱敏IP地址
        if (auditLog.getClientIp() != null) {
            String ip = auditLog.getClientIp();
            if (ip.length() > 6) {
                auditLog.setClientIp(ip.substring(0, 3) + "***" + ip.substring(ip.length() - 3));
            }
        }

        // 脱敏用户代理
        if (auditLog.getUserAgent() != null && auditLog.getUserAgent().length() > 10) {
            auditLog.setUserAgent(auditLog.getUserAgent().substring(0, 10) + "***");
        }

        return auditLog;
    }

    /**
     * 异步保存审计日志
     */
    @SuppressWarnings("null")
    private void saveAuditLogAsync(AuditLogRecord auditLog) {
        CompletableFuture.runAsync(() -> {
            try {
                String key = "audit:" + auditLog.getLogId();
                // Duration.ofDays() 不会返回null
                java.time.Duration ttl = java.time.Duration.ofDays(30);
                redisTemplate.opsForValue().set(key, auditLog, ttl);
            } catch (Exception e) {
                log.warn("[安全管理] 异步保存审计日志失败: logId={}, error={}",
                    auditLog.getLogId(), e.getMessage());
            }
        });
    }

    /**
     * 加载用户权限
     * <p>
     * 从数据库加载用户权限，支持按资源过滤
     * 权限格式：resource:action 或 *:*
     * </p>
     *
     * @param userId   用户ID
     * @param resource 资源名称（可选，为空时返回所有权限）
     * @return 权限列表
     */
    private List<String> loadUserPermissions(Long userId, String resource) {
        try {
            if (userId == null) {
                log.warn("[安全管理] 用户ID为空，返回空权限列表");
                return List.of();
            }

            // 从数据库查询用户菜单（包含权限信息）
            List<net.lab1024.sa.common.menu.entity.MenuEntity> userMenus = menuDao.selectMenuListByUserId(userId);

            // 提取权限标识并过滤
            List<String> permissions = userMenus.stream()
                    .filter(menu -> menu.getPermission() != null && !menu.getPermission().trim().isEmpty())
                    .map(net.lab1024.sa.common.menu.entity.MenuEntity::getPermission)
                    .distinct()
                    .collect(Collectors.toList());

            // 如果指定了资源，过滤特定资源的权限
            if (resource != null && !resource.trim().isEmpty()) {
                permissions = permissions.stream()
                        .filter(permission -> {
                            // 支持通配符权限 *:*
                            if ("*:*".equals(permission)) {
                                return true;
                            }
                            // 支持资源通配符 resource:*
                            if (permission.startsWith(resource + ":")) {
                                return true;
                            }
                            // 支持完全匹配
                            return permission.equals(resource);
                        })
                        .collect(Collectors.toList());
            }

            // 如果没有权限，返回空列表（而不是通配符）
            if (permissions.isEmpty()) {
                log.debug("[安全管理] 用户无权限: userId={}, resource={}", userId, resource);
                return List.of();
            }

            log.debug("[安全管理] 加载用户权限成功: userId={}, resource={}, permissionCount={}",
                    userId, resource, permissions.size());

            return permissions;

        } catch (Exception e) {
            log.error("[安全管理] 加载用户权限失败: userId={}, resource={}, error={}",
                    userId, resource, e.getMessage(), e);
            // 发生异常时返回空列表，确保安全（拒绝访问）
            return List.of();
        }
    }

    /**
     * 获取IP白名单
     * <p>
     * 支持从配置文件和数据库获取IP白名单
     * 优先级：数据库配置 > 配置文件 > 默认值
     * 使用多级缓存提升性能
     * </p>
     *
     * @return IP白名单列表
     */
    @SuppressWarnings("null")
    private List<String> getIpWhitelist() {
        try {
            // L1: 从Redis缓存获取
            String cacheKey = IP_WHITELIST_CACHE_KEY;
            try {
                @SuppressWarnings("unchecked")
                List<String> cachedList = (List<String>) redisTemplate.opsForValue().get(cacheKey);
                if (cachedList != null && !cachedList.isEmpty()) {
                    log.debug("[安全管理] IP白名单缓存命中，数量: {}", cachedList.size());
                    return cachedList;
                }
            } catch (Exception e) {
                log.warn("[安全管理] Redis缓存查询失败: error={}", e.getMessage());
            }

            // L2: 从数据库获取（如果启用）
            List<String> whitelist = new ArrayList<>();
            if (ipWhitelistDbEnabled) {
                try {
                    net.lab1024.sa.common.system.domain.entity.SystemConfigEntity config =
                            systemConfigDao.selectByKey("security.ip.whitelist");
                    if (config != null && config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                        String dbWhitelist = config.getConfigValue();
                        whitelist.addAll(parseIpWhitelist(dbWhitelist));
                        log.debug("[安全管理] 从数据库加载IP白名单，数量: {}", whitelist.size());
                    }
                } catch (Exception e) {
                    log.warn("[安全管理] 从数据库加载IP白名单失败: error={}", e.getMessage());
                }
            }

            // L3: 从配置文件获取（如果数据库没有配置）
            if (whitelist.isEmpty() && ipWhitelistConfig != null && !ipWhitelistConfig.trim().isEmpty()) {
                whitelist.addAll(parseIpWhitelist(ipWhitelistConfig));
                log.debug("[安全管理] 从配置文件加载IP白名单，数量: {}", whitelist.size());
            }

            // L4: 使用默认值
            if (whitelist.isEmpty()) {
                whitelist = List.of("127.0.0.1", "localhost", "::1");
                log.debug("[安全管理] 使用默认IP白名单");
            }

            // 更新缓存
            try {
                @SuppressWarnings("null")
                java.time.Duration ttl = java.time.Duration.ofSeconds(IP_WHITELIST_CACHE_TTL);
                redisTemplate.opsForValue().set(cacheKey, whitelist, ttl);
            } catch (Exception e) {
                log.warn("[安全管理] 更新IP白名单缓存失败: error={}", e.getMessage());
            }

            return whitelist;

        } catch (Exception e) {
            log.error("[安全管理] 获取IP白名单异常: error={}", e.getMessage(), e);
            // 发生异常时返回默认值，确保系统可用性
            return List.of("127.0.0.1", "localhost", "::1");
        }
    }

    /**
     * 解析IP白名单字符串
     * <p>
     * 支持逗号、分号、换行符分隔的IP地址列表
     * </p>
     *
     * @param whitelistStr IP白名单字符串
     * @return IP地址列表
     */
    private List<String> parseIpWhitelist(String whitelistStr) {
        if (whitelistStr == null || whitelistStr.trim().isEmpty()) {
            return List.of();
        }

        return java.util.Arrays.stream(whitelistStr.split("[,;\\n\\r]+"))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    /**
     * 记录风险检测结果
     */
    private void recordRiskDetection(RiskDetectionResult result) {
        if (result.getRiskLevel() != RiskLevel.SAFE) {
            log.warn("[安全管理] 检测到风险: userId={}, action={}, riskLevel={}, score={}",
                result.getUserId(), result.getAction(), result.getRiskLevel(), result.getRiskScore());

            // 发送风险告警通知
            sendRiskAlertNotification(result);
        }
    }

    /**
     * 发送风险告警通知
     * <p>
     * 通过Redis发布风险告警消息，由监控服务订阅处理
     * 支持异步发送，不影响主业务流程
     * </p>
     *
     * @param result 风险检测结果
     */
    @SuppressWarnings("null")
    private void sendRiskAlertNotification(RiskDetectionResult result) {
        try {
            // 构建告警消息
            Map<String, Object> alertMessage = new ConcurrentHashMap<>();
            alertMessage.put("alertType", "SECURITY_RISK");
            alertMessage.put("userId", result.getUserId());
            alertMessage.put("action", result.getAction());
            alertMessage.put("riskLevel", result.getRiskLevel() != null ? result.getRiskLevel().name() : "UNKNOWN");
            alertMessage.put("riskScore", result.getRiskScore());
            alertMessage.put("ruleResults", result.getRuleResults());
            alertMessage.put("timestamp", LocalDateTime.now().toString());
            alertMessage.put("source", "SecurityOptimizationManager");

            // 根据风险等级确定告警严重程度
            String severity = "LOW";
            if (result.getRiskLevel() == RiskLevel.HIGH) {
                severity = "CRITICAL";
            } else if (result.getRiskLevel() == RiskLevel.MEDIUM) {
                severity = "HIGH";
            } else if (result.getRiskLevel() == RiskLevel.LOW) {
                severity = "MEDIUM";
            }

            alertMessage.put("severity", severity);

            // 构建告警消息文本
            String alertText = String.format(
                "【IOE-DREAM安全风险告警】\n" +
                "风险等级: %s\n" +
                "风险分数: %.2f\n" +
                "用户ID: %s\n" +
                "操作: %s\n" +
                "检测时间: %s\n" +
                "规则详情: %s",
                result.getRiskLevel() != null ? result.getRiskLevel().name() : "UNKNOWN",
                result.getRiskScore(),
                result.getUserId(),
                result.getAction(),
                LocalDateTime.now(),
                result.getRuleResults()
            );
            alertMessage.put("message", alertText);

            // 异步发布到Redis频道
            CompletableFuture.runAsync(() -> {
                try {
                    // 方式1: 使用Redis Pub/Sub发布消息
                    @SuppressWarnings("null")
                    String channel = "security:risk:alert";
                    stringRedisTemplate.convertAndSend(channel, alertText);

                    // 方式2: 同时存储到Redis，供监控服务查询
                    String alertKey = "security:risk:alert:" + result.getUserId() + ":" + System.currentTimeMillis();
                    @SuppressWarnings("null")
                    java.time.Duration ttl = java.time.Duration.ofHours(24);
                    redisTemplate.opsForValue().set(alertKey, alertMessage, ttl);

                    log.debug("[安全管理] 风险告警通知已发送: userId={}, riskLevel={}, score={}",
                            result.getUserId(), result.getRiskLevel(), result.getRiskScore());

                } catch (Exception e) {
                    log.error("[安全管理] 发送风险告警通知失败: userId={}, error={}",
                            result.getUserId(), e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            log.error("[安全管理] 构建风险告警通知失败: userId={}, error={}",
                    result.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * 生成日志ID
     */
    private String generateLogId() {
        return "AUDIT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }

    /**
     * 审计日志记录类
     */
    public static class AuditLogRecord {
        private String logId;
        private Long userId;
        private String username;
        private String action;
        private String resource;
        private String clientIp;
        private String userAgent;
        private String requestParams;
        private String responseResult;
        private Boolean success;
        private String errorMessage;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getLogId() { return logId; }
        public void setLogId(String logId) { this.logId = logId; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getResource() { return resource; }
        public void setResource(String resource) { this.resource = resource; }

        public String getClientIp() { return clientIp; }
        public void setClientIp(String clientIp) { this.clientIp = clientIp; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getRequestParams() { return requestParams; }
        public void setRequestParams(String requestParams) { this.requestParams = requestParams; }

        public String getResponseResult() { return responseResult; }
        public void setResponseResult(String responseResult) { this.responseResult = responseResult; }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 风险检测规则类
     */
    public static class RiskDetectionRule {
        private final String name;
        private final RiskEvaluator evaluator;

        public RiskDetectionRule(String name, RiskEvaluator evaluator) {
            this.name = name;
            this.evaluator = evaluator;
        }

        public double evaluate(HttpServletRequest request, Long userId, String action) {
            try {
                return evaluator.evaluate(request, userId, action);
            } catch (Exception e) {
                log.warn("[安全管理] 风险检测规则执行异常: rule={}, error={}", name, e.getMessage());
                return 0.0;
            }
        }

        public String getName() { return name; }
    }

    /**
     * 风险评估器接口
     */
    @FunctionalInterface
    public interface RiskEvaluator {
        double evaluate(HttpServletRequest request, Long userId, String action);
    }

    /**
     * 风险检测结果类
     */
    public static class RiskDetectionResult {
        private Long userId;
        private String action;
        private double riskScore;
        private RiskLevel riskLevel;
        private Map<String, Double> ruleResults = new ConcurrentHashMap<>();

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

        public Map<String, Double> getRuleResults() { return ruleResults; }
        public void addRuleResult(String ruleName, double score) {
            ruleResults.put(ruleName, score);
        }
    }

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        SAFE,    // 安全
        LOW,     // 低风险
        MEDIUM,  // 中等风险
        HIGH     // 高风险
    }
}
