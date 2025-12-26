package net.lab1024.sa.consume.monitor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionVO;
import net.lab1024.sa.consume.domain.vo.ConsumeAccountVO;

/**
 * 消费业务日志记录器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 提供统一的业务日志记录
 * - 结构化日志格式，便于分析
 * - 关键业务操作全链路日志
 * - 集成审计日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Slf4j
@Component
public class ConsumeBusinessLogger {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 分类日志记录器

    /**
     * 记录交易开始日志
     *
     * @param transactionId 交易ID
     * @param userId       用户ID
     * @param amount       交易金额
     * @param deviceId     设备ID
     * @param mealId       餐次ID
     */
    public void logTransactionStart(String transactionId, Long userId,
                                  java.math.BigDecimal amount, String deviceId, String mealId) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("transactionId", transactionId);
        logData.put("userId", userId);
        logData.put("amount", amount);
        logData.put("deviceId", deviceId);
        logData.put("mealId", mealId);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        logData.put("operation", "TRANSACTION_START");

        log.info("[交易开始] {}", formatLog(logData));
        log.info("[性能] 交易开始: transactionId={}", transactionId);
    }

    /**
     * 记录交易成功日志
     *
     * @param transaction 交易信息
     * @param durationMs   处理耗时
     */
    public void logTransactionSuccess(ConsumeTransactionVO transaction, long durationMs) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("transactionId", transaction.getTransactionId());
        logData.put("userId", transaction.getUserId());
        logData.put("amount", transaction.getAmount());
        logData.put("deviceId", transaction.getDeviceId());
        logData.put("balanceBefore", transaction.getBalanceBefore());
        logData.put("balanceAfter", transaction.getBalanceAfter());
        logData.put("status", transaction.getStatus());
        logData.put("durationMs", durationMs);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        logData.put("operation", "TRANSACTION_SUCCESS");

        log.info("[交易成功] {}", formatLog(logData));
        log.info("[审计] 交易执行成功: transactionId={}, userId={}, amount={}, balanceAfter={}",
            transaction.getTransactionId(), transaction.getUserId(), transaction.getAmount(), transaction.getBalanceAfter());
        log.info("[性能] 交易成功: transactionId={}, durationMs={}ms",
            transaction.getTransactionId(), durationMs);
    }

    /**
     * 记录交易失败日志
     *
     * @param transactionId 交易ID
     * @param userId       用户ID
     * @param amount       交易金额
     * @param deviceId     设备ID
     * @param errorType    错误类型
     * @param errorMessage 错误信息
     * @param durationMs   处理耗时
     */
    public void logTransactionFailure(String transactionId, Long userId,
                                    java.math.BigDecimal amount, String deviceId,
                                    String errorType, String errorMessage, long durationMs) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("transactionId", transactionId);
        logData.put("userId", userId);
        logData.put("amount", amount);
        logData.put("deviceId", deviceId);
        logData.put("errorType", errorType);
        logData.put("errorMessage", errorMessage);
        logData.put("durationMs", durationMs);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        logData.put("operation", "TRANSACTION_FAILURE");

        log.error("[交易失败] {}", formatLog(logData));
        log.error("[审计] 交易执行失败: transactionId={}, userId={}, errorType={}, errorMessage={}",
            transactionId, userId, errorType, errorMessage);
        log.warn("[性能] 交易失败: transactionId={}, durationMs={}ms, errorType={}",
            transactionId, durationMs, errorType);
    }

    /**
     * 记录账户操作日志
     *
     * @param operation   操作类型
     * @param accountId   账户ID
     * @param userId      用户ID
     * @param amount      操作金额
     * @param balanceBefore 操作前余额
     * @param balanceAfter  操作后余额
     * @param description 操作描述
     */
    public void logAccountOperation(String operation, Long accountId, Long userId,
                                  java.math.BigDecimal amount, java.math.BigDecimal balanceBefore,
                                  java.math.BigDecimal balanceAfter, String description) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", operation);
        logData.put("accountId", accountId);
        logData.put("userId", userId);
        logData.put("amount", amount);
        logData.put("balanceBefore", balanceBefore);
        logData.put("balanceAfter", balanceAfter);
        logData.put("description", description);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        log.info("[账户操作] {}", formatLog(logData));
        log.info("[审计] 账户操作: operation={}, accountId={}, userId={}, amount={}, balanceAfter={}",
            operation, accountId, userId, amount, balanceAfter);
    }

    /**
     * 记录账户状态变更日志
     *
     * @param accountId   账户ID
     * @param userId      用户ID
     * @param oldStatus   原状态
     * @param newStatus   新状态
     * @param reason      变更原因
     */
    public void logAccountStatusChange(Long accountId, Long userId,
                                     Integer oldStatus, Integer newStatus, String reason) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("accountId", accountId);
        logData.put("userId", userId);
        logData.put("oldStatus", oldStatus);
        logData.put("newStatus", newStatus);
        logData.put("reason", reason);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        logData.put("operation", "ACCOUNT_STATUS_CHANGE");

        log.info("[状态变更] {}", formatLog(logData));
        log.warn("[审计] 账户状态变更: accountId={}, userId={}, oldStatus={}, newStatus={}, reason={}",
            accountId, userId, oldStatus, newStatus, reason);
    }

    /**
     * 记录分布式锁操作日志
     *
     * @param lockType    锁类型
     * @param resourceId  资源ID
     * @param operation   操作类型（ACQUIRE, RELEASE, TIMEOUT）
     * @param durationMs  持有时间
     * @param success     是否成功
     */
    public void logDistributedLockOperation(String lockType, String resourceId,
                                          String operation, long durationMs, boolean success) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("lockType", lockType);
        logData.put("resourceId", resourceId);
        logData.put("operation", operation);
        logData.put("durationMs", durationMs);
        logData.put("success", success);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        if (success) {
            log.debug("[分布式锁] {}", formatLog(logData));
        } else {
            log.warn("[分布式锁] {}", formatLog(logData));
        }

        // 锁超时需要记录审计日志
        if ("TIMEOUT".equals(operation)) {
            log.warn("[审计] 分布式锁超时: lockType={}, resourceId={}, durationMs={}ms",
                lockType, resourceId, durationMs);
        }
    }

    /**
     * 记录API调用日志
     *
     * @param apiPath     API路径
     * @param method      HTTP方法
     * @param userId      用户ID（可选）
     * @param durationMs  处理耗时
     * @param result      调用结果
     * @param responseCode 响应状态码
     */
    public void logApiCall(String apiPath, String method, Long userId,
                          long durationMs, ResponseDTO<?> result, Integer responseCode) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("apiPath", apiPath);
        logData.put("method", method);
        logData.put("userId", userId);
        logData.put("durationMs", durationMs);
        logData.put("responseCode", responseCode);
        logData.put("success", result != null && result.isSuccess());
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        log.info("[API调用] {}", formatLog(logData));

        // 慢查询日志（超过1秒）
        if (durationMs > 1000) {
            log.warn("[性能] 慢API: apiPath={}, method={}, durationMs={}ms",
                apiPath, method, durationMs);
        }

        // 错误API调用记录审计日志
        if (result != null && !result.isSuccess()) {
            log.error("[审计] API调用失败: apiPath={}, method={}, userId={}, responseCode={}, error={}",
                apiPath, method, userId, responseCode, result.getMessage());
        }
    }

    /**
     * 记录数据库操作性能日志
     *
     * @param operation   操作类型（SELECT, INSERT, UPDATE, DELETE）
     * @param table       表名
     * @param durationMs  执行耗时
     * @param recordCount 影响记录数
     */
    public void logDatabasePerformance(String operation, String table,
                                     long durationMs, int recordCount) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", operation);
        logData.put("table", table);
        logData.put("durationMs", durationMs);
        logData.put("recordCount", recordCount);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        // 慢查询日志（超过500ms）
        if (durationMs > 500) {
            log.warn("[性能] 慢SQL: operation={}, table={}, durationMs={}ms, recordCount={}",
                operation, table, durationMs, recordCount);
        } else {
            log.debug("[性能] SQL执行: operation={}, table={}, durationMs={}ms",
                operation, table, durationMs);
        }
    }

    /**
     * 记录缓存操作日志
     *
     * @param operation  操作类型（HIT, MISS, PUT, EVICT）
     * @param cacheKey   缓存键
     * @param cacheType  缓存类型（L1, L2, L3）
     */
    public void logCacheOperation(String operation, String cacheKey, String cacheType) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", operation);
        logData.put("cacheKey", cacheKey);
        logData.put("cacheType", cacheType);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        log.debug("[缓存操作] {}", formatLog(logData));

        // 缓存未命中需要特别关注
        if ("MISS".equals(operation)) {
            log.debug("[性能] 缓存未命中: cacheKey={}, cacheType={}", cacheKey, cacheType);
        }
    }

    /**
     * 记录系统异常日志
     *
     * @param exception   异常对象
     * @param operation   操作类型
     * @param context     上下文信息
     */
    public void logSystemException(Exception exception, String operation, Map<String, Object> context) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("operation", operation);
        logData.put("exceptionType", exception.getClass().getSimpleName());
        logData.put("exceptionMessage", exception.getMessage());
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        if (context != null) {
            logData.putAll(context);
        }

        log.error("[系统异常] {}", formatLog(logData), exception);
        log.error("[审计] 系统异常: operation={}, exceptionType={}, exceptionMessage={}, context={}",
            operation, exception.getClass().getSimpleName(), exception.getMessage(), context);
    }

    /**
     * 记录安全相关日志
     *
     * @param securityEvent 安全事件类型
     * @param userId       用户ID
     * @param ipAddress    IP地址
     * @param userAgent    用户代理
     * @param details      详细信息
     */
    public void logSecurityEvent(String securityEvent, Long userId,
                               String ipAddress, String userAgent, String details) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("securityEvent", securityEvent);
        logData.put("userId", userId);
        logData.put("ipAddress", ipAddress);
        logData.put("userAgent", userAgent);
        logData.put("details", details);
        logData.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));

        // 安全事件使用ERROR级别
        log.error("[安全事件] {}", formatLog(logData));
        log.error("[审计] 安全事件: securityEvent={}, userId={}, ipAddress={}, details={}",
            securityEvent, userId, ipAddress, details);
    }

    /**
     * 格式化日志数据
     *
     * @param logData 日志数据
     * @return 格式化后的字符串
     */
    private String formatLog(Map<String, Object> logData) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : logData.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 获取当前请求链路追踪ID（如果有）
     *
     * @return 追踪ID
     */
    private String getTraceId() {
        // 可以从MDC中获取追踪ID
        // return MDC.get("traceId");
        return "unknown";
    }
}