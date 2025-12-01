# 统一缓存策略使用示例

**技能名称**: unified-cache-strategy
**技能等级**: ★★★ 高级
**适用角色**: 后端开发人员、架构师、缓存管理员
**前置技能**: Spring Boot开发、Redis基础、缓存设计模式
**预计学时**: 4小时

---

## 🎯 概述

本文档提供了IOE-DREAM项目中统一缓存策略的详细使用示例，确保开发团队在后续工作中保持全局缓存一致性。所有示例都严格遵循repowiki规范和统一缓存管理系统的最佳实践。

---

## 📋 目录

1. [基础缓存操作](#基础缓存操作)
2. [高级缓存模式](#高级缓存模式)
3. [注解驱动开发](#注解驱动开发)
4. [批量操作优化](#批量操作优化)
5. [性能监控实践](#性能监控实践)
6. [业务场景示例](#业务场景示例)
7. [故障处理指南](#故障处理指南)

---

## 🚀 基础缓存操作

### 1.1 获取和设置缓存

```java
@Service
public class UserService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 获取用户信息（基础操作）
     */
    public User getUserById(Long userId) {
        // 使用USER命名空间，30分钟过期
        UnifiedCacheManager.CacheResult<User> result = cacheManager.get(
            UnifiedCacheManager.CacheNamespace.USER,
            "user:" + userId,
            User.class
        );

        if (result.isSuccess()) {
            return result.getData();
        }

        // 缓存未命中，从数据库查询
        User user = userDao.findById(userId);

        if (user != null) {
            // 设置缓存，使用默认过期时间
            cacheManager.set(UnifiedCacheManager.CacheNamespace.USER, "user:" + userId, user);
        }

        return user;
    }

    /**
     * 设置用户信息（自定义过期时间）
     */
    public void cacheUserInfo(User user) {
        // 使用60分钟过期时间
        cacheManager.set(
            UnifiedCacheManager.CacheNamespace.USER,
            "user:" + user.getId(),
            user,
            60,
            TimeUnit.MINUTES
        );
    }

    /**
     * 删除用户缓存
     */
    public void evictUserCache(Long userId) {
        cacheManager.delete(UnifiedCacheManager.CacheNamespace.USER, "user:" + userId);
    }
}
```

### 1.2 缓存存在性检查

```java
@Service
public class ConfigurationService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 检查配置缓存是否存在
     */
    public boolean isConfigurationCached(String configKey) {
        return cacheManager.exists(
            UnifiedCacheManager.CacheNamespace.CONFIG,
            "config:" + configKey
        );
    }

    /**
     * 获取配置或返回默认值
     */
    public String getConfiguration(String configKey, String defaultValue) {
        UnifiedCacheManager.CacheResult<String> result = cacheManager.get(
            UnifiedCacheManager.CacheNamespace.CONFIG,
            "config:" + configKey,
            String.class
        );

        return result.isSuccess() ? result.getData() : defaultValue;
    }
}
```

---

## 🔧 高级缓存模式

### 2.1 缓存穿透保护

```java
@Service
public class ProductService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 获取产品信息（带缓存穿透保护）
     */
    public Product getProductWithProtection(Long productId) {
        // 使用getOrSet方法，自动处理缓存穿透
        UnifiedCacheManager.CacheResult<Product> result = cacheManager.getOrSet(
            UnifiedCacheManager.CacheNamespace.PRODUCT,
            "product:" + productId,
            () -> loadProductFromDatabase(productId), // 数据加载器
            Product.class
        );

        return result.isSuccess() ? result.getData() : null;
    }

    /**
     * 数据加载器（从数据库加载产品）
     */
    private Product loadProductFromDatabase(Long productId) {
        try {
            Product product = productDao.findById(productId);
            log.info("从数据库加载产品信息: productId={}", productId);
            return product;
        } catch (Exception e) {
            log.error("加载产品信息失败: productId={}", productId, e);
            return null;
        }
    }
}
```

### 2.2 泛型类型处理

```java
@Service
public class ReportService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 获取用户报表列表（泛型处理）
     */
    public List<UserReport> getUserReports(Long userId) {
        // 使用TypeReference处理泛型
        UnifiedCacheManager.CacheResult<List<UserReport>> result = cacheManager.get(
            UnifiedCacheManager.CacheNamespace.USER,
            "reports:" + userId,
            new TypeReference<List<UserReport>>() {}
        );

        return result.isSuccess() ? result.getData() : Collections.emptyList();
    }

    /**
     * 缓存报表数据
     */
    public void cacheUserReports(Long userId, List<UserReport> reports) {
        cacheManager.set(
            UnifiedCacheManager.CacheNamespace.USER,
            "reports:" + userId,
            reports
        );
    }
}
```

### 2.3 异步缓存操作

```java
@Service
public class AsyncCacheService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 异步设置缓存
     */
    public CompletableFuture<Void> cacheProductAsync(Product product) {
        return cacheManager.setAsync(
            UnifiedCacheManager.CacheNamespace.PRODUCT,
            "product:" + product.getId(),
            product
        ).thenAccept(result -> {
            if (result.isSuccess()) {
                log.debug("异步缓存设置成功: productId={}", product.getId());
            } else {
                log.warn("异步缓存设置失败: productId={}, error={}",
                    product.getId(), result.getErrorMessage());
            }
        });
    }

    /**
     * 异步获取或设置缓存
     */
    public CompletableFuture<Product> getProductAsync(Long productId) {
        return cacheManager.getOrSetAsync(
            UnifiedCacheManager.CacheNamespace.PRODUCT,
            "product:" + productId,
            () -> loadProductFromDatabase(productId),
            Product.class
        ).thenApply(result -> result.isSuccess() ? result.getData() : null);
    }
}
```

---

## 📝 注解驱动开发

### 3.1 基础注解使用

```java
@Service
public class AnnotationBasedService {

    /**
     * 简单缓存注解
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.USER,
        key = "#userId",
        ttl = 1800 // 30分钟
    )
    public User getUserById(Long userId) {
        return userDao.findById(userId);
    }

    /**
     * 复杂键表达式
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.USER,
        key = "#user.id + ':' + #user.department.id",
        ttl = 3600 // 1小时
    )
    public User updateUser(User user) {
        return userDao.update(user);
    }

    /**
     * 条件缓存
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.USER,
        key = "#userId",
        condition = "#userId > 0",
        unless = "#result == null"
    )
    public User getUserWithCondition(Long userId) {
        return userDao.findById(userId);
    }
}
```

### 3.2 异步缓存注解

```java
@Service
public class AsyncAnnotationService {

    /**
     * 异步缓存注解
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.PRODUCT,
        key = "#productId",
        async = true,
        ttl = 900 // 15分钟
    )
    public Product updateProductAsync(Product product) {
        return productDao.update(product);
    }

    /**
     * 异步条件缓存
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.PRODUCT,
        key = "#productId",
        async = true,
        condition = "#product.status == 'ACTIVE'",
        cacheNull = false
    )
    public Product updateActiveProduct(Product product) {
        return productDao.update(product);
    }
}
```

---

## ⚡ 批量操作优化

### 4.1 批量获取

```java
@Service
public class BatchOperationService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 批量获取用户信息
     */
    public Map<Long, User> batchGetUsers(List<Long> userIds) {
        // 构建缓存键列表
        List<String> cacheKeys = userIds.stream()
            .map(id -> "user:" + id)
            .collect(Collectors.toList());

        // 批量获取缓存
        UnifiedCacheManager.BatchCacheResult<User> batchResult = cacheManager.mGet(
            UnifiedCacheManager.CacheNamespace.USER,
            cacheKeys,
            User.class
        );

        // 处理结果
        Map<Long, User> resultMap = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (UnifiedCacheManager.CacheResult<User> result : batchResult.getResults()) {
            if (result.isSuccess()) {
                Long userId = extractUserIdFromKey(result.getKey());
                resultMap.put(userId, result.getData());
            } else {
                Long userId = extractUserIdFromKey(result.getKey());
                missedIds.add(userId);
            }
        }

        // 查询数据库获取未命中的用户
        if (!missedIds.isEmpty()) {
            List<User> dbUsers = userDao.findByIds(missedIds);
            for (User user : dbUsers) {
                resultMap.put(user.getId(), user);
                // 异步设置缓存
                cacheManager.setAsync(
                    UnifiedCacheManager.CacheNamespace.USER,
                    "user:" + user.getId(),
                    user
                );
            }
        }

        return resultMap;
    }

    private Long extractUserIdFromKey(String cacheKey) {
        return Long.parseLong(cacheKey.split(":")[1]);
    }
}
```

### 4.2 批量设置和清理

```java
@Service
public class BatchCacheManagementService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 批量预热用户缓存
     */
    public void warmupUserCache(List<User> users) {
        Map<String, Object> warmupData = users.stream()
            .collect(Collectors.toMap(
                user -> "user:" + user.getId(),
                user -> user
            ));

        UnifiedCacheManager.BatchCacheResult<Object> batchResult = cacheManager.mSet(
            UnifiedCacheManager.CacheNamespace.USER,
            warmupData
        );

        log.info("用户缓存预热完成: 总数={}, 成功数={}, 失败数={}, 耗时={}ms",
                batchResult.getTotalCount(),
                batchResult.getSuccessCount(),
                batchResult.getFailureCount(),
                batchResult.getTotalTime());
    }

    /**
     * 批量清理用户缓存
     */
    public int clearUserCache(List<Long> userIds) {
        List<String> cacheKeys = userIds.stream()
            .map(id -> "user:*") // 使用模式匹配清理
            .collect(Collectors.toList());

        return cacheManager.deleteByPattern(UnifiedCacheManager.CacheNamespace.USER, "user:*");
    }

    /**
     * 按模式清理缓存
     */
    public int clearCacheByPattern(UnifiedCacheManager.CacheNamespace namespace, String pattern) {
        return cacheManager.deleteByPattern(namespace, pattern);
    }
}
```

---

## 📊 性能监控实践

### 5.1 缓存指标收集

```java
@Service
public class CacheMonitoringService {

    @Resource
    private CacheMetricsCollector metricsCollector;

    /**
     * 获取缓存统计信息
     */
    public void logCacheStatistics() {
        Map<String, Map<String, Object>> allStats = metricsCollector.getAllStatistics();

        // 记录全局统计
        Map<String, Object> globalStats = allStats.get("global");
        log.info("全局缓存统计: 命中率={}, 错误率={}, 平均响应时间={}ms",
                globalStats.get("hitRate"),
                globalStats.get("errorRate"),
                globalStats.get("avgResponseTime"));

        // 记录各命名空间统计
        for (Map.Entry<String, Map<String, Object>> entry : allStats.entrySet()) {
            if (!"global".equals(entry.getKey())) {
                Map<String, Object> namespaceStats = entry.getValue();
                log.info("命名空间 {} 统计: 命中率={}, 请求次数={}",
                        entry.getKey(),
                        namespaceStats.get("hitRate"),
                        namespaceStats.get("requests"));
            }
        }
    }

    /**
     * 定时监控任务
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行
    public void monitorCacheHealth() {
        Map<String, Object> healthAssessment = metricsCollector.getHealthAssessment();

        @SuppressWarnings("unchecked")
        List<String> warnings = (List<String>) healthAssessment.get("warnings");

        Double globalHealthScore = (Double) healthAssessment.get("globalHealthScore");

        log.info("缓存健康检查: 健康分数={}, 告警数量={}",
                globalHealthScore, warnings.size());

        if (globalHealthScore < 70) {
            log.warn("缓存健康度过低，需要优化");
            // 触发优化流程
            optimizeCachePerformance();
        }

        if (!warnings.isEmpty()) {
            log.warn("缓存告警: {}", warnings);
            // 发送告警通知
            sendCacheAlert(warnings);
        }
    }

    private void optimizeCachePerformance() {
        // 实现缓存性能优化逻辑
        log.info("开始缓存性能优化...");

        // 分析慢查询
        Map<String, Map<String, Object>> allStats = metricsCollector.getAllStatistics();
        for (Map.Entry<String, Map<String, Object>> entry : allStats.entrySet()) {
            Map<String, Object> stats = entry.getValue();
            Double avgResponseTime = (Double) stats.get("avgResponseTime");
            if (avgResponseTime != null && avgResponseTime > 100) {
                log.warn("命名空间 {} 响应时间过慢: {}ms", entry.getKey(), avgResponseTime);
                // 清理或优化该命名空间的缓存
                clearCacheByPattern(
                    UnifiedCacheManager.CacheNamespace.valueOf(entry.getKey().toUpperCase()),
                    "*"
                );
            }
        }
    }

    private void sendCacheAlert(List<String> warnings) {
        // 实现告警通知逻辑
        log.warn("发送缓存告警通知: {}", warnings);
        // alertService.sendAlert("缓存告警", warnings);
    }
}
```

### 5.2 缓存健康检查

```java
@RestController
@RequestMapping("/api/cache/health")
public class CacheHealthController {

    @Resource
    private UnifiedCacheManager cacheManager;

    @Resource
    private CacheMetricsCollector metricsCollector;

    /**
     * 执行缓存健康检查
     */
    @GetMapping
    public ResponseDTO<Map<String, Object>> healthCheck() {
        Map<String, Object> healthResult = new HashMap<>();

        try {
            // 测试缓存连通性
            boolean connectivityTest = testCacheConnectivity();

            // 获取健康度评估
            Map<String, Object> assessment = metricsCollector.getHealthAssessment();

            // 获取统计信息
            Map<String, Map<String, Object>> statistics = metricsCollector.getAllStatistics();

            healthResult.put("connectivity", connectivityTest ? "正常" : "异常");
            healthResult.put("assessment", assessment);
            healthResult.put("statistics", statistics);
            healthResult.put("checkTime", System.currentTimeMillis());

            return ResponseDTO.ok(healthResult);

        } catch (Exception e) {
            log.error("缓存健康检查失败", e);
            return ResponseDTO.error("健康检查失败: " + e.getMessage());
        }
    }

    private boolean testCacheConnectivity() {
        try {
            // 简单的连通性测试
            String testKey = "health-check-" + System.currentTimeMillis();
            String testValue = "ping";

            UnifiedCacheManager.CacheResult<String> setResult = cacheManager.set(
                UnifiedCacheManager.CacheNamespace.TEMP,
                testKey,
                testValue,
                5,
                TimeUnit.SECONDS
            );

            if (!setResult.isSuccess()) {
                return false;
            }

            UnifiedCacheManager.CacheResult<String> getResult = cacheManager.get(
                UnifiedCacheManager.CacheNamespace.TEMP,
                testKey,
                String.class
            );

            return getResult.isSuccess() && testValue.equals(getResult.getData());

        } catch (Exception e) {
            log.error("缓存连通性测试失败", e);
            return false;
        }
    }
}
```

---

## 🏢 业务场景示例

### 6.1 用户会话管理

```java
@Service
public class UserSessionService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 缓存用户会话
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.USER,
        key = "'session:' + #sessionId",
        ttl = 1800 // 30分钟
    )
    public UserSession cacheUserSession(String sessionId, UserSession session) {
        return session;
    }

    /**
     * 获取用户会话
     */
    public UserSession getUserSession(String sessionId) {
        UnifiedCacheManager.CacheResult<UserSession> result = cacheManager.get(
            UnifiedCacheManager.CacheNamespace.USER,
            "session:" + sessionId,
            UserSession.class
        );

        return result.isSuccess() ? result.getData() : null;
    }

    /**
     * 批量清理过期会话
     */
    @Scheduled(cron = "0 0 */5 * ?") // 每5小时执行一次
    public void cleanExpiredSessions() {
        log.info("开始清理过期用户会话");

        // 使用模式匹配清理所有会话
        int deletedCount = cacheManager.deleteByPattern(
            UnifiedCacheManager.CacheNamespace.USER,
            "session:*"
        );

        log.info("过期会话清理完成，删除数量: {}", deletedCount);
    }
}
```

### 6.2 设备状态缓存

```java
@Service
public class DeviceStatusService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 获取设备状态（短期缓存，高实时性要求）
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.DEVICE,
        key = "'status:' + #deviceId",
        ttl = 60, // 1分钟
        penetrationProtection = true
    )
    public DeviceStatus getDeviceStatus(Long deviceId) {
        // 实时性要求高，使用短期缓存
        return deviceService.getRealTimeStatus(deviceId);
    }

    /**
     * 获取设备配置（长期缓存，变更频率低）
     */
    @UnifiedCache(
        namespace = UnifiedCacheManager.CacheNamespace.DEVICE,
        key = "'config:' + #deviceId",
        ttl = 3600 // 1小时
    )
    public DeviceConfig getDeviceConfig(Long deviceId) {
        // 配置变更较少，使用长期缓存
        return deviceService.getDeviceConfig(deviceId);
    }

    /**
     * 设备状态变更时自动清理缓存
     */
    public void onDeviceStatusChanged(Long deviceId, DeviceStatus newStatus) {
        // 删除短期缓存
        cacheManager.delete(
            UnifiedCacheManager.CacheNamespace.DEVICE,
            "status:" + deviceId
        );

        log.info("设备状态变更，清理状态缓存: deviceId={}, newStatus={}",
                deviceId, newStatus.getStatus());
    }

    /**
     * 设备配置变更时自动清理缓存
     */
    public void onDeviceConfigChanged(Long deviceId) {
        // 删除长期缓存
        cacheManager.delete(
            UnifiedCacheManager.CacheNamespace.DEVICE,
            "config:" + deviceId
        );

        log.info("设备配置变更，清理配置缓存: deviceId", deviceId);
    }
}
```

### 6.3 报表数据缓存

```java
@Service
public class ReportCacheService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 缓存日报表数据
     */
    public void cacheDailyReport(LocalDate reportDate, Map<String, Object> reportData) {
        String cacheKey = "daily_report:" + reportDate.toString();

        // 设置12小时过期时间
        cacheManager.set(
            UnifiedCacheManager.CacheNamespace.SYSTEM,
            cacheKey,
            reportData,
            12,
            TimeUnit.HOURS
        );

        log.info("日报表数据已缓存: reportDate={}", reportDate);
    }

    /**
     * 获取日报表数据
     */
    public Map<String, Object> getDailyReport(LocalDate reportDate) {
        String cacheKey = "daily_report:" + reportDate.toString();

        UnifiedCacheManager.CacheResult<Map<String, Object>> result = cacheManager.get(
            UnifiedCacheManager.CacheNamespace.SYSTEM,
            cacheKey,
            new TypeReference<Map<String, Object>>() {}
        );

        return result.isSuccess() ? result.getData() : null;
    }

    /**
     * 批量预热报表缓存
     */
    public void warmupReportCache(int days) {
        Map<String, Object> warmupData = new HashMap<>();

        // 预热最近几天的报表
        for (int i = 0; i < days; i++) {
            LocalDate reportDate = LocalDate.now().minusDays(i);
            Map<String, Object> reportData = reportService.generateDailyReport(reportDate);
            if (reportData != null) {
                warmupData.put("daily_report:" + reportDate.toString(), reportData);
            }
        }

        cacheManager.warmUp(UnifiedCacheManager.CacheNamespace.SYSTEM, warmupData);
        log.info("报表缓存预热完成: 天数={}, 数据条数={}", days, warmupData.size());
    }
}
```

---

## 🛠️ 故障处理指南

### 7.1 缓存异常处理

```java
@Service
public class CacheFaultToleranceService {

    @Resource
    private UnifiedCacheManager cacheManager;

    /**
     * 降级处理：缓存失败时的备选方案
     */
    public User getUserWithFallback(Long userId) {
        try {
            // 尝试从缓存获取
            UnifiedCacheManager.CacheResult<User> cacheResult = cacheManager.get(
                UnifiedCacheManager.CacheNamespace.USER,
                "user:" + userId,
                User.class
            );

            if (cacheResult.isSuccess()) {
                return cacheResult.getData();
            }

        } catch (Exception e) {
            log.warn("缓存操作异常，使用降级处理: userId={}, error={}", userId, e.getMessage());
        }

        // 降级到数据库查询
        try {
            return userDao.findById(userId);
        } catch (Exception dbException) {
            log.error("数据库查询也失败，返回默认用户: userId={}", userId, dbException);
            return createDefaultUser(userId);
        }
    }

    /**
     * 默认用户（降级处理）
     */
    private User createDefaultUser(Long userId) {
        User defaultUser = new User();
        defaultUser.setId(userId);
        defaultUser.setUsername("unknown");
        defaultUser.setStatus("TEMPORARY");
        defaultUser.setRemark("缓存降级创建的临时用户");
        return defaultUser;
    }

    /**
     * 缓存重试机制
     */
    public Product getProductWithRetry(Long productId, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                UnifiedCacheManager.CacheResult<Product> result = cacheManager.get(
                    UnifiedCacheManager.CacheNamespace.PRODUCT,
                    "product:" + productId,
                    Product.class
                );

                if (result.isSuccess()) {
                    return result.getData();
                }

                if (attempt == maxRetries) {
                    log.warn("缓存重试失败，达到最大重试次数: productId={}, attempts={}",
                            productId, maxRetries);
                    break;
                }

                // 等待重试
                Thread.sleep(100 * attempt);

            } catch (Exception e) {
                log.warn("缓存重试异常: attempt={}, productId={}, error={}",
                            attempt, productId, e.getMessage());
                if (attempt == maxRetries) {
                    break;
                }
            }
        }

        // 最终降级到数据库查询
        return productDao.findById(productId);
    }
}
```

### 7.2 缓存一致性保障

```java
@Service
public class CacheConsistencyService {

    @Resource
    private UnifiedCacheManager cacheManager;

    @Resource
    private MessageProducer messageProducer;

    /**
     * 双删策略：删除缓存并延迟删除
     */
    public void evictWithDoubleDelete(String namespace, String key) {
        // 第一次删除
        boolean firstDelete = cacheManager.delete(
            UnifiedCacheManager.CacheNamespace.valueOf(namespace.toUpperCase()),
            key
        );

        // 延迟500ms后第二次删除
        CompletableFuture.delayedExecutor().schedule(() -> {
            boolean secondDelete = cacheManager.delete(
                UnifiedCacheManager.CacheNamespace.valueOf(namespace.toUpperCase()),
                key
            );
            log.debug("双删策略执行完成: namespace={}, key={}, 第一次={}, 第二次={}",
                    namespace, key, firstDelete, secondDelete);
        }, 500, TimeUnit.MILLISECONDS);

        // 发布缓存变更事件
        publishCacheEvictionEvent(namespace, key);
    }

    /**
     * 发布缓存变更事件
     */
    private void publishCacheEvictionEvent(String namespace, String key) {
        try {
            CacheChangeEvent event = new CacheChangeEvent();
            event.setNamespace(namespace);
            event.setKey(key);
            event.setTimestamp(System.currentTimeMillis());
            event.setEventType("EVICTION");

            messageProducer.send(event);
            log.debug("缓存变更事件已发布: namespace={}, key={}", namespace, key);

        } catch (Exception e) {
            log.error("发布缓存变更事件失败: namespace={}, key={}", namespace, key, e);
        }
    }

    /**
     * 缓存一致性检查
     */
    public void validateCacheConsistency(String namespace, String key, Object expectedValue) {
        try {
            UnifiedCacheManager.CacheResult<Object> result = cacheManager.get(
                UnifiedCacheManager.CacheNamespace.valueOf(namespace.toUpperCase()),
                    key,
                    Object.class
            );

            if (result.isSuccess()) {
                Object actualValue = result.getData();
                if (!Objects.equals(expectedValue, actualValue)) {
                    log.warn("缓存数据不一致: namespace={}, key={}, 期望值={}, 实际值={}",
                            namespace, key, expectedValue, actualValue);

                    // 清理不一致的缓存
                    cacheManager.delete(
                        UnifiedCacheManager.CacheNamespace.valueOf(namespace.toUpperCase()),
                        key
                    );
                }
            }

        } catch (Exception e) {
            log.error("缓存一致性检查失败: namespace={}, key={}", namespace, key, e);
        }
    }
}
```

---

## 🎯 最佳实践总结

### ✅ 推荐做法

1. **命名空间规范**：使用标准化的缓存命名空间，避免命名冲突
2. **过期时间合理**：根据数据特性设置合适的过期时间
3. **批量操作**：优先使用批量API提升性能
4. **异步处理**：对性能敏感的操作使用异步模式
5. **监控告警**：实时监控缓存性能和健康状态

### ❌ 避免做法

1. **过度缓存**：不经常变化的数据不要缓存
2. **大对象缓存**：避免缓存过大的对象
3. **缓存雪崩**：避免大量缓存同时失效
4. **忽略异常**：必须正确处理缓存异常
5. **硬编码**：避免硬编码缓存键和过期时间

### 🔧 性能优化技巧

1. **预热策略**：应用启动时预加载热点数据
2. **批量操作**：减少网络调用次数
3. **连接池优化**：合理配置连接池参数
4. **序列化优化**：使用高效的序列化器
5. **本地缓存**：结合L1缓存提升性能

---

*最后更新: 2025-11-16*
*版本: 1.0.0*
*维护者: SmartAdmin Team*