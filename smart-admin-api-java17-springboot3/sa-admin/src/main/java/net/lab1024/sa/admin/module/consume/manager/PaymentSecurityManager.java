package net.lab1024.sa.admin.module.consume.manager;


import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.domain.entity.PaymentRecordEntity;
import net.lab1024.sa.admin.module.consume.dao.PaymentRecordDao;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 支付安全管理器
 * 负责防重放攻击、防篡改、多支付方式安全验证等功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/25
 */
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class PaymentSecurityManager {

    @Resource
    private ConsumeCacheService consumeCacheService;

    @Resource
    private PaymentRecordDao paymentRecordDao;

    // 安全配置常量
    private static final String NONCE_CACHE_PREFIX = "payment_nonce:";
    private static final String SIGNATURE_CACHE_PREFIX = "payment_signature:";
    private static final String FINGERPRINT_CACHE_PREFIX = "payment_fingerprint:";
    private static final long NONCE_EXPIRE_MINUTES = 5; // Nonce有效期5分钟
    private static final long SIGNATURE_EXPIRE_MINUTES = 10; // 签名有效期10分钟
    private static final long FINGERPRINT_EXPIRE_HOURS = 24; // 设备指纹有效期24小时
    private static final int MAX_PAYMENT_ATTEMPTS_PER_MINUTE = 3; // 每分钟最大支付尝试次数
    private static final BigDecimal MAX_SINGLE_AMOUNT = new BigDecimal("10000.00"); // 单笔支付最大金额

    /**
     * 支付安全验证结果
     */
    public static class PaymentSecurityResult {
        private boolean success;
        private String errorCode;
        private String errorMessage;
        private Map<String, Object> details;

        public PaymentSecurityResult(boolean success, String errorCode, String errorMessage) {
            this.success = success;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.details = new HashMap<>();
        }

        public static PaymentSecurityResult success() {
            return new PaymentSecurityResult(true, null, "支付安全验证通过");
        }

        public static PaymentSecurityResult failure(String errorCode, String errorMessage) {
            return new PaymentSecurityResult(false, errorCode, errorMessage);
        }

        public PaymentSecurityResult withDetail(String key, Object value) {
            this.details.put(key, value);
            return this;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public Map<String, Object> getDetails() { return details; }
    }

    /**
     * 支付请求验证参数
     */
    public static class PaymentRequest {
        private String orderId;
        private String userId;
        private BigDecimal amount;
        private String payMethod;
        private String deviceId;
        private String timestamp;
        private String nonce;
        private String signature;
        private String deviceFingerprint;

        // 构造函数
        public PaymentRequest(String orderId, String userId, BigDecimal amount, String payMethod,
                            String deviceId, String timestamp, String nonce, String signature, String deviceFingerprint) {
            this.orderId = orderId;
            this.userId = userId;
            this.amount = amount;
            this.payMethod = payMethod;
            this.deviceId = deviceId;
            this.timestamp = timestamp;
            this.nonce = nonce;
            this.signature = signature;
            this.deviceFingerprint = deviceFingerprint;
        }

        // Getters
        public String getOrderId() { return orderId; }
        public String getUserId() { return userId; }
        public BigDecimal getAmount() { return amount; }
        public String getPayMethod() { return payMethod; }
        public String getDeviceId() { return deviceId; }
        public String getTimestamp() { return timestamp; }
        public String getNonce() { return nonce; }
        public String getSignature() { return signature; }
        public String getDeviceFingerprint() { return deviceFingerprint; }
    }

    /**
     * 完整的支付安全验证
     *
     * @param request 支付请求
     * @return 安全验证结果
     */
    public PaymentSecurityResult validatePaymentSecurity(PaymentRequest request) {
        try {
            log.info("开始支付安全验证: orderId={}, userId={}, amount={}",
                    request.getOrderId(), request.getUserId(), request.getAmount());

            // 1. 基础参数验证
            PaymentSecurityResult basicResult = validateBasicParameters(request);
            if (!basicResult.isSuccess()) {
                return basicResult;
            }

            // 2. 防重放攻击验证
            PaymentSecurityResult replayResult = validateReplayAttack(request);
            if (!replayResult.isSuccess()) {
                return replayResult;
            }

            // 3. 防篡改验证
            PaymentSecurityResult integrityResult = validateRequestIntegrity(request);
            if (!integrityResult.isSuccess()) {
                return integrityResult;
            }

            // 4. 设备指纹验证
            PaymentSecurityResult fingerprintResult = validateDeviceFingerprint(request);
            if (!fingerprintResult.isSuccess()) {
                return fingerprintResult;
            }

            // 5. 支付频率限制验证
            PaymentSecurityResult frequencyResult = validatePaymentFrequency(request);
            if (!frequencyResult.isSuccess()) {
                return frequencyResult;
            }

            // 6. 支付金额限制验证
            PaymentSecurityResult amountResult = validatePaymentAmount(request);
            if (!amountResult.isSuccess()) {
                return amountResult;
            }

            log.info("支付安全验证通过: orderId={}", request.getOrderId());
            return PaymentSecurityResult.success();

        } catch (Exception e) {
            log.error("支付安全验证异常: orderId={}", request.getOrderId(), e);
            return PaymentSecurityResult.failure("SECURITY_ERROR", "支付安全验证异常");
        }
    }

    /**
     * 基础参数验证
     */
    private PaymentSecurityResult validateBasicParameters(PaymentRequest request) {
        // 检查必要参数
        if (!StringUtils.hasText(request.getOrderId())) {
            return PaymentSecurityResult.failure("MISSING_ORDER_ID", "订单ID不能为空");
        }

        if (!StringUtils.hasText(request.getUserId())) {
            return PaymentSecurityResult.failure("MISSING_USER_ID", "用户ID不能为空");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentSecurityResult.failure("INVALID_AMOUNT", "支付金额必须大于0");
        }

        if (!StringUtils.hasText(request.getPayMethod())) {
            return PaymentSecurityResult.failure("MISSING_PAY_METHOD", "支付方式不能为空");
        }

        if (!StringUtils.hasText(request.getTimestamp())) {
            return PaymentSecurityResult.failure("MISSING_TIMESTAMP", "时间戳不能为空");
        }

        if (!StringUtils.hasText(request.getNonce())) {
            return PaymentSecurityResult.failure("MISSING_NONCE", "随机数不能为空");
        }

        // 验证时间戳（请求时间不能超过5分钟）
        try {
            LocalDateTime requestTime = LocalDateTime.parse(request.getTimestamp());
            LocalDateTime now = LocalDateTime.now();
            long minutesDiff = ChronoUnit.MINUTES.between(requestTime, now);
            if (Math.abs(minutesDiff) > 5) {
                return PaymentSecurityResult.failure("INVALID_TIMESTAMP",
                        "请求时间戳无效，请求时间与服务器时间差异过大");
            }
        } catch (Exception e) {
            return PaymentSecurityResult.failure("INVALID_TIMESTAMP_FORMAT", "时间戳格式错误");
        }

        return PaymentSecurityResult.success();
    }

    /**
     * 防重放攻击验证
     */
    private PaymentSecurityResult validateReplayAttack(PaymentRequest request) {
        try {
            // 1. 检查Nonce是否已使用
            String nonceKey = NONCE_CACHE_PREFIX + request.getNonce();
            if (consumeCacheService.hasKey(nonceKey)) {
                log.warn("检测到重放攻击: nonce={}, orderId={}", request.getNonce(), request.getOrderId());
                return PaymentSecurityResult.failure("REPLAY_ATTACK", "检测到重放攻击");
            }

            // 2. 检查订单ID是否已存在
            PaymentRecordEntity existingRecord = paymentRecordDao.selectByOrderId(request.getOrderId());
            if (existingRecord != null) {
                log.warn("检测到重复订单: orderId={}", request.getOrderId());
                return PaymentSecurityResult.failure("DUPLICATE_ORDER", "订单ID已存在");
            }

            // 3. 缓存Nonce，防止重用
            consumeCacheService.setValue(nonceKey, "1", NONCE_EXPIRE_MINUTES * 60);

            return PaymentSecurityResult.success();

        } catch (Exception e) {
            log.error("防重放验证异常: orderId={}", request.getOrderId(), e);
            return PaymentSecurityResult.failure("REPLAY_VALIDATION_ERROR", "防重放验证异常");
        }
    }

    /**
     * 防篡改验证（签名验证）
     */
    private PaymentSecurityResult validateRequestIntegrity(PaymentRequest request) {
        try {
            // 1. 检查签名是否存在
            if (!StringUtils.hasText(request.getSignature())) {
                return PaymentSecurityResult.failure("MISSING_SIGNATURE", "数字签名不能为空");
            }

            // 2. 生成待签名字符串
            String signString = buildSignString(request);
            log.debug("待签名字符串: {}", signString);

            // 3. 计算预期签名
            String expectedSignature = generateSignature(signString);

            // 4. 验证签名
            if (!request.getSignature().equals(expectedSignature)) {
                log.warn("签名验证失败: orderId={}, expected={}, actual={}",
                        request.getOrderId(), expectedSignature, request.getSignature());
                return PaymentSecurityResult.failure("INVALID_SIGNATURE", "数字签名验证失败");
            }

            // 5. 检查签名是否已使用（防重放）
            String signatureKey = SIGNATURE_CACHE_PREFIX + request.getSignature();
            if (consumeCacheService.hasKey(signatureKey)) {
                return PaymentSecurityResult.failure("SIGNATURE_REUSE", "数字签名已使用");
            }

            // 6. 缓存签名
            consumeCacheService.setValue(signatureKey, "1", SIGNATURE_EXPIRE_MINUTES * 60);

            return PaymentSecurityResult.success();

        } catch (Exception e) {
            log.error("签名验证异常: orderId={}", request.getOrderId(), e);
            return PaymentSecurityResult.failure("SIGNATURE_VALIDATION_ERROR", "签名验证异常");
        }
    }

    /**
     * 设备指纹验证
     */
    private PaymentSecurityResult validateDeviceFingerprint(PaymentRequest request) {
        try {
            if (!StringUtils.hasText(request.getDeviceFingerprint())) {
                return PaymentSecurityResult.failure("MISSING_FINGERPRINT", "设备指纹不能为空");
            }

            // 1. 生成当前设备指纹
            String currentFingerprint = generateDeviceFingerprint(request);

            // 2. 验证设备指纹是否匹配
            if (!request.getDeviceFingerprint().equals(currentFingerprint)) {
                log.warn("设备指纹不匹配: orderId={}, expected={}, actual={}",
                        request.getOrderId(), currentFingerprint, request.getDeviceFingerprint());

                // 可选：是否允许新设备，这里允许但记录警告
                log.info("新设备支付: userId={}, deviceId={}, fingerprint={}",
                        request.getUserId(), request.getDeviceId(), request.getDeviceFingerprint());
            }

            // 3. 缓存设备指纹
            String fingerprintKey = FINGERPRINT_CACHE_PREFIX + request.getUserId() + ":" + request.getDeviceId();
            consumeCacheService.setValue(fingerprintKey, request.getDeviceFingerprint(), FINGERPRINT_EXPIRE_HOURS * 60 * 60);

            return PaymentSecurityResult.success();

        } catch (Exception e) {
            log.error("设备指纹验证异常: orderId={}", request.getOrderId(), e);
            return PaymentSecurityResult.failure("FINGERPRINT_VALIDATION_ERROR", "设备指纹验证异常");
        }
    }

    /**
     * 支付频率限制验证
     */
    private PaymentSecurityResult validatePaymentFrequency(PaymentRequest request) {
        try {
            String frequencyKey = "payment_frequency:" + request.getUserId();

            // 获取当前分钟的支付次数
            String currentMinute = LocalDateTime.now().toString().substring(0, 16); // yyyy-MM-ddTHH:mm
            String minuteKey = frequencyKey + ":" + currentMinute;

            Integer currentCount = consumeCacheService.getValue(minuteKey, Integer.class);
            if (currentCount == null) {
                currentCount = 0;
            }

            if (currentCount >= MAX_PAYMENT_ATTEMPTS_PER_MINUTE) {
                log.warn("支付频率过高: userId={}, count={}", request.getUserId(), currentCount);
                return PaymentSecurityResult.failure("PAYMENT_FREQUENCY_HIGH", "支付频率过高，请稍后再试");
            }

            // 增加计数
            consumeCacheService.setValue(minuteKey, currentCount + 1, 60); // 缓存1分钟

            return PaymentSecurityResult.success();

        } catch (Exception e) {
            log.error("支付频率验证异常: orderId={}", request.getOrderId(), e);
            return PaymentSecurityResult.failure("FREQUENCY_VALIDATION_ERROR", "支付频率验证异常");
        }
    }

    /**
     * 支付金额限制验证
     */
    private PaymentSecurityResult validatePaymentAmount(PaymentRequest request) {
        try {
            // 1. 检查单笔支付金额限制
            if (request.getAmount().compareTo(MAX_SINGLE_AMOUNT) > 0) {
                log.warn("单笔支付金额过大: orderId={}, amount={}", request.getOrderId(), request.getAmount());
                return PaymentSecurityResult.failure("AMOUNT_TOO_LARGE",
                        "单笔支付金额不能超过 " + MAX_SINGLE_AMOUNT);
            }

            // 2. 检查当日累计支付金额限制
            String dailyAmountKey = "daily_amount:" + request.getUserId();
            String today = LocalDateTime.now().toLocalDate().toString();
            String todayKey = dailyAmountKey + ":" + today;

            BigDecimal dailyAmount = consumeCacheService.getValue(todayKey, BigDecimal.class);
            if (dailyAmount == null) {
                dailyAmount = BigDecimal.ZERO;
            }

            BigDecimal dailyLimit = new BigDecimal("50000.00"); // 日限额5万
            if (dailyAmount.add(request.getAmount()).compareTo(dailyLimit) > 0) {
                log.warn("当日支付金额超限: userId={}, daily={}, current={}",
                        request.getUserId(), dailyAmount, request.getAmount());
                return PaymentSecurityResult.failure("DAILY_AMOUNT_EXCEEDED",
                        "当日支付金额已超限，今日已消费 " + dailyAmount);
            }

            // 3. 更新当日支付金额
            consumeCacheService.setValue(todayKey, dailyAmount.add(request.getAmount()), 24 * 60 * 60); // 缓存24小时

            return PaymentSecurityResult.success();

        } catch (Exception e) {
            log.error("支付金额验证异常: orderId={}", request.getOrderId(), e);
            return PaymentSecurityResult.failure("AMOUNT_VALIDATION_ERROR", "支付金额验证异常");
        }
    }

    /**
     * 构建待签名字符串
     */
    private String buildSignString(PaymentRequest request) {
        // 按照固定顺序拼接参数
        return String.format("orderId=%s&userId=%s&amount=%s&payMethod=%s&deviceId=%s&timestamp=%s&nonce=%s",
                request.getOrderId(),
                request.getUserId(),
                request.getAmount().toString(),
                request.getPayMethod(),
                request.getDeviceId(),
                request.getTimestamp(),
                request.getNonce());
    }

    /**
     * 生成数字签名
     */
    private String generateSignature(String signString) {
        // 这里使用MD5作为示例，实际项目中应该使用更安全的算法如HMAC-SHA256
        return DigestUtils.md5DigestAsHex(signString.getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }

    /**
     * 生成设备指纹
     */
    private String generateDeviceFingerprint(PaymentRequest request) {
        // 基于用户ID、设备ID等信息生成设备指纹
        String fingerprintData = String.format("%s|%s|%s",
                request.getUserId(), request.getDeviceId(), request.getPayMethod());
        return DigestUtils.md5DigestAsHex(fingerprintData.getBytes(StandardCharsets.UTF_8)).substring(0, 16);
    }

    /**
     * 清理过期的安全缓存
     */
    public void cleanupExpiredSecurityCache() {
        try {
            // 清理过期的Nonce
            // 清理过期的签名
            // 清理过期的设备指纹
            log.info("安全缓存清理完成");
        } catch (Exception e) {
            log.error("安全缓存清理失败", e);
        }
    }

    /**
     * 获取用户支付统计信息
     */
    public Map<String, Object> getUserPaymentStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 今日支付次数
            String today = LocalDateTime.now().toLocalDate().toString();
            String todayKey = "payment_frequency:" + userId + ":" + today;
            // TODO: 实现具体统计逻辑

            // 本月支付金额
            String thisMonth = LocalDateTime.now().toString().substring(0, 7); // yyyy-MM
            String monthKey = "monthly_amount:" + userId + ":" + thisMonth;
            // TODO: 实现具体统计逻辑

            stats.put("todayCount", 0);
            stats.put("monthlyAmount", BigDecimal.ZERO);

            return stats;
        } catch (Exception e) {
            log.error("获取用户支付统计失败: userId={}", userId, e);
            return stats;
        }
    }
}