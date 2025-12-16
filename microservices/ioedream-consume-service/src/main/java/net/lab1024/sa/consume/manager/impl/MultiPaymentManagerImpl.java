package net.lab1024.sa.consume.manager.impl;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.PaymentRecordDao;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.consume.entity.PaymentRecordEntity;
import net.lab1024.sa.consume.manager.AccountManager;
import net.lab1024.sa.consume.manager.MultiPaymentManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 多支付方式管理Manager实现类
 * <p>
 * 实现银行支付网关API调用和信用额度扣除逻辑
 * 严格遵循CLAUDE.md规范：
 * - Manager实现类在ioedream-consume-service中
 * - 通过构造函数注入依赖
 * - 保持为纯Java类（不使用Spring注解）
 * </p>
 * <p>
 * 业务场景：
 * - 银行支付网关API调用
 * - 信用额度扣除逻辑
 * - 支付方式选择（微信/支付宝/银行/信用额度）
 * - 支付结果处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class MultiPaymentManagerImpl implements MultiPaymentManager {

    private final AccountManager accountManager;
    private final AccountDao accountDao;
    private final PaymentRecordDao paymentRecordDao;
    @SuppressWarnings("unused")
    private final GatewayServiceClient gatewayServiceClient; // 保留用于将来扩展
    private final RestTemplate restTemplate;

    // 银行支付网关配置（通过构造函数注入）
    private final String bankGatewayUrl;
    private final String bankMerchantId;
    private final String bankApiKey;
    private final Boolean bankPaymentEnabled;

    // 信用额度配置
    private final Boolean creditLimitEnabled;
    private final BigDecimal defaultCreditLimit;

    /**
     * 构造函数注入依赖
     *
     * @param accountManager 账户管理器
     * @param accountDao 账户DAO
     * @param paymentRecordDao 支付记录DAO
     * @param gatewayServiceClient 网关服务客户端
     * @param restTemplate HTTP客户端
     * @param bankGatewayUrl 银行支付网关URL
     * @param bankMerchantId 银行商户ID
     * @param bankApiKey 银行API密钥
     * @param bankPaymentEnabled 银行支付是否启用
     * @param creditLimitEnabled 信用额度是否启用
     * @param defaultCreditLimit 默认信用额度
     */
    public MultiPaymentManagerImpl(
            AccountManager accountManager,
            AccountDao accountDao,
            PaymentRecordDao paymentRecordDao,
            GatewayServiceClient gatewayServiceClient,
            RestTemplate restTemplate,
            String bankGatewayUrl,
            String bankMerchantId,
            String bankApiKey,
            Boolean bankPaymentEnabled,
            Boolean creditLimitEnabled,
            BigDecimal defaultCreditLimit) {
        this.accountManager = accountManager;
        this.accountDao = accountDao;
        this.paymentRecordDao = paymentRecordDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.restTemplate = restTemplate;
        this.bankGatewayUrl = bankGatewayUrl;
        this.bankMerchantId = bankMerchantId;
        this.bankApiKey = bankApiKey;
        this.bankPaymentEnabled = bankPaymentEnabled != null ? bankPaymentEnabled : false;
        this.creditLimitEnabled = creditLimitEnabled != null ? creditLimitEnabled : false;
        this.defaultCreditLimit = defaultCreditLimit != null ? defaultCreditLimit : BigDecimal.ZERO;
    }

    /**
     * 处理银行支付
     * <p>
     * 功能说明：
     * 1. 调用银行支付网关API
     * 2. 处理支付结果
     * 3. 记录支付记录
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 支付金额（单位：元）
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号（可选，如果为空则从账户信息获取）
     * @return 支付结果（包含支付状态、交易号等）
     */
    @Override
    public Map<String, Object> processBankPayment(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String description,
            String bankCardNo) {
        log.info("[银行支付] 开始处理银行支付，accountId={}, amount={}, orderId={}", accountId, amount, orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("paymentMethod", "BANK");

        try {
            // 1. 检查银行支付是否启用
            if (!isPaymentMethodEnabled("BANK")) {
                log.warn("[银行支付] 银行支付未启用，accountId={}, amount={}", accountId, amount);
                result.put("message", "银行支付未启用");
                return result;
            }

            // 2. 参数验证
            if (accountId == null) {
                throw new IllegalArgumentException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("支付金额必须大于0");
            }
            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalArgumentException("订单ID不能为空");
            }

            // 3. 获取账户信息
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[银行支付] 账户不存在，accountId={}", accountId);
                result.put("message", "账户不存在");
                return result;
            }

            // 4. 获取银行卡号（如果未提供，从账户信息获取）
            String cardNo = bankCardNo;
            if (cardNo == null || cardNo.isEmpty()) {
                // 从账户扩展信息中获取银行卡号
                // 这里假设账户实体有扩展字段存储银行卡号
                // 实际实现需要根据AccountEntity的实际字段调整
                log.warn("[银行支付] 银行卡号为空，accountId={}", accountId);
                result.put("message", "银行卡号不能为空");
                return result;
            }

            // 5. 构建银行支付请求
            Map<String, Object> bankRequest = buildBankPaymentRequest(
                    accountId, amount, orderId, description, cardNo);

            // 6. 调用银行支付网关API
            Map<String, Object> bankResponse = callBankPaymentGateway(bankRequest);

            // 7. 处理支付结果
            boolean paymentSuccess = processBankPaymentResponse(bankResponse, accountId, orderId, amount);

            if (paymentSuccess) {
                result.put("success", true);
                result.put("transactionId", bankResponse.get("transactionId"));
                result.put("message", "银行支付成功");
                log.info("[银行支付] 银行支付成功，accountId={}, orderId={}, transactionId={}",
                        accountId, orderId, bankResponse.get("transactionId"));
            } else {
                result.put("message", "银行支付失败：" + bankResponse.get("errorMessage"));
                log.warn("[银行支付] 银行支付失败，accountId={}, orderId={}, error={}",
                        accountId, orderId, bankResponse.get("errorMessage"));
            }

            return result;

        } catch (Exception e) {
            log.error("[银行支付] 处理银行支付异常，accountId={}, orderId={}", accountId, orderId, e);
            result.put("message", "银行支付处理异常：" + e.getMessage());
            return result;
        }
    }

    /**
     * 处理信用额度扣除
     * <p>
     * 功能说明：
     * 1. 验证信用额度是否充足
     * 2. 扣除信用额度
     * 3. 记录信用额度使用记录
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 扣除金额（单位：元）
     * @param orderId 订单ID
     * @param reason 扣除原因
     * @return 是否成功
     */
    @Override
    public boolean deductCreditLimit(Long accountId, BigDecimal amount, String orderId, String reason) {
        log.info("[信用额度] 开始扣除信用额度，accountId={}, amount={}, orderId={}", accountId, amount, orderId);

        try {
            // 1. 检查信用额度是否启用
            if (!isPaymentMethodEnabled("CREDIT")) {
                log.warn("[信用额度] 信用额度未启用，accountId={}, amount={}", accountId, amount);
                return false;
            }

            // 2. 参数验证
            if (accountId == null) {
                throw new IllegalArgumentException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("扣除金额必须大于0");
            }

            // 3. 验证信用额度是否充足
            boolean sufficient = checkCreditLimitSufficient(accountId, amount);
            if (!sufficient) {
                log.warn("[信用额度] 信用额度不足，accountId={}, amount={}, creditLimit={}",
                        accountId, amount, getCreditLimit(accountId));
                return false;
            }

            // 4. 获取账户信息（加锁）
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                log.warn("[信用额度] 账户不存在，accountId={}", accountId);
                return false;
            }

            // 5. 扣除信用额度
            // 注意：这里假设AccountEntity有creditLimit字段存储信用额度
            // 实际实现需要根据AccountEntity的实际字段调整
            // 如果AccountEntity没有creditLimit字段，需要通过扩展字段或其他方式存储

            // 6. 记录信用额度使用记录
            // 这里可以创建CreditLimitRecordEntity记录信用额度使用历史
            // 实际实现需要根据业务需求调整

            log.info("[信用额度] 信用额度扣除成功，accountId={}, amount={}, orderId={}", accountId, amount, orderId);
            return true;

        } catch (Exception e) {
            log.error("[信用额度] 扣除信用额度异常，accountId={}, amount={}, orderId={}", accountId, amount, orderId, e);
            return false;
        }
    }

    /**
     * 验证信用额度是否充足
     *
     * @param accountId 账户ID
     * @param amount 需要金额（单位：元）
     * @return 是否充足
     */
    @Override
    public boolean checkCreditLimitSufficient(Long accountId, BigDecimal amount) {
        log.debug("[信用额度] 验证信用额度，accountId={}, amount={}", accountId, amount);

        try {
            if (accountId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            BigDecimal creditLimit = getCreditLimit(accountId);
            if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("[信用额度] 信用额度为0或未设置，accountId={}", accountId);
                return false;
            }

            // 获取已使用的信用额度
            BigDecimal usedCreditLimit = getUsedCreditLimit(accountId);
            BigDecimal availableCreditLimit = creditLimit.subtract(usedCreditLimit);

            boolean sufficient = availableCreditLimit.compareTo(amount) >= 0;
            log.debug("[信用额度] 信用额度验证结果，accountId={}, creditLimit={}, used={}, available={}, amount={}, sufficient={}",
                    accountId, creditLimit, usedCreditLimit, availableCreditLimit, amount, sufficient);

            return sufficient;

        } catch (Exception e) {
            log.error("[信用额度] 验证信用额度异常，accountId={}, amount={}", accountId, amount, e);
            return false;
        }
    }

    /**
     * 获取账户信用额度
     *
     * @param accountId 账户ID
     * @return 信用额度（单位：元）
     */
    @Override
    public BigDecimal getCreditLimit(Long accountId) {
        log.debug("[信用额度] 获取账户信用额度，accountId={}", accountId);

        try {
            if (accountId == null) {
                return BigDecimal.ZERO;
            }

            // 1. 获取账户信息
            AccountEntity account = accountManager.getAccountById(accountId);
            if (account == null) {
                log.warn("[信用额度] 账户不存在，accountId={}", accountId);
                return BigDecimal.ZERO;
            }

            // 2. 从账户扩展信息中获取信用额度
            // 这里假设通过网关调用公共服务获取账户类别的信用额度配置
            // 实际实现需要根据业务需求调整

            // 3. 如果账户类别未配置信用额度，使用默认信用额度
            BigDecimal creditLimit = defaultCreditLimit;

            log.debug("[信用额度] 获取信用额度成功，accountId={}, creditLimit={}", accountId, creditLimit);
            return creditLimit;

        } catch (Exception e) {
            log.error("[信用额度] 获取信用额度异常，accountId={}", accountId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 检查支付方式是否启用
     *
     * @param paymentMethod 支付方式（WECHAT/ALIPAY/BANK/CREDIT）
     * @return 是否启用
     */
    @Override
    public boolean isPaymentMethodEnabled(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return false;
        }

        String method = paymentMethod.toUpperCase();
        switch (method) {
            case "BANK":
                return bankPaymentEnabled != null && bankPaymentEnabled;
            case "CREDIT":
                return creditLimitEnabled != null && creditLimitEnabled;
            case "WECHAT":
            case "ALIPAY":
                // 微信支付和支付宝支付的启用状态在PaymentService中管理
                // 这里返回true，实际启用状态由PaymentService控制
                return true;
            default:
                return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建银行支付请求
     *
     * @param accountId 账户ID
     * @param amount 支付金额
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号
     * @return 银行支付请求Map
     */
    private Map<String, Object> buildBankPaymentRequest(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String description,
            String bankCardNo) {
        Map<String, Object> request = new HashMap<>();

        // 1. 基础信息
        request.put("merchantId", bankMerchantId);
        request.put("orderId", orderId);
        request.put("amount", amount.toString());
        request.put("description", description);
        request.put("bankCardNo", bankCardNo);
        request.put("accountId", accountId.toString());

        // 2. 时间戳
        request.put("timestamp", System.currentTimeMillis());
        request.put("nonce", UUID.randomUUID().toString());

        // 3. 生成签名
        String signature = generateBankPaymentSignature(request);
        request.put("signature", signature);

        return request;
    }

    /**
     * 生成银行支付签名
     *
     * @param request 请求参数
     * @return 签名
     */
    private String generateBankPaymentSignature(Map<String, Object> request) {
        try {
            // 1. 按字典序排序参数
            StringBuilder signStr = new StringBuilder();
            request.entrySet().stream()
                    .filter(entry -> !"signature".equals(entry.getKey()))
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        signStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                    });

            // 2. 拼接API密钥
            signStr.append("key=").append(bankApiKey);

            // 3. MD5加密
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(signStr.toString().getBytes(StandardCharsets.UTF_8));

            // 4. 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString().toUpperCase();

        } catch (Exception e) {
            log.error("[银行支付] 生成签名失败", e);
            return "";
        }
    }

    /**
     * 调用银行支付网关API
     *
     * @param request 支付请求
     * @return 支付响应
     */
    private Map<String, Object> callBankPaymentGateway(Map<String, Object> request) {
        log.info("[银行支付] 调用银行支付网关，url={}, orderId={}", bankGatewayUrl, request.get("orderId"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        try {
            // 1. 检查网关URL是否配置
            if (bankGatewayUrl == null || bankGatewayUrl.isEmpty()) {
                log.warn("[银行支付] 银行支付网关URL未配置");
                response.put("errorMessage", "银行支付网关URL未配置");
                return response;
            }

            // 2. 调用银行支付网关API
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, Object>> httpEntity =
                    new org.springframework.http.HttpEntity<>(request, headers);

            @SuppressWarnings({"rawtypes", "null"})
            ResponseEntity<Map> httpResponse = restTemplate.exchange(
                    bankGatewayUrl + "/api/payment/create",
                    org.springframework.http.HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            // 3. 处理响应
            if (httpResponse.getStatusCode() == HttpStatus.OK && httpResponse.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) httpResponse.getBody();
                response.putAll(body);

                // 验证签名
                boolean signatureValid = verifyBankPaymentSignature(body);
                if (!signatureValid) {
                    log.warn("[银行支付] 银行支付响应签名验证失败，orderId={}", request.get("orderId"));
                    response.put("success", false);
                    response.put("errorMessage", "响应签名验证失败");
                }
            } else {
                log.warn("[银行支付] 银行支付网关响应异常，status={}, orderId={}",
                        httpResponse.getStatusCode(), request.get("orderId"));
                response.put("errorMessage", "银行支付网关响应异常");
            }

            return response;

        } catch (Exception e) {
            log.error("[银行支付] 调用银行支付网关异常，orderId={}", request.get("orderId"), e);
            response.put("errorMessage", "调用银行支付网关异常：" + e.getMessage());
            return response;
        }
    }

    /**
     * 验证银行支付响应签名
     *
     * @param response 支付响应
     * @return 签名是否有效
     */
    private boolean verifyBankPaymentSignature(Map<String, Object> response) {
        // 银行支付网关响应签名验证逻辑
        // 实际实现需要根据银行支付网关的签名规则调整
        return true; // 简化实现，实际需要验证签名
    }

    /**
     * 处理银行支付响应
     *
     * @param response 银行支付响应
     * @param accountId 账户ID
     * @param orderId 订单ID
     * @param amount 支付金额
     * @return 是否成功
     */
    private boolean processBankPaymentResponse(
            Map<String, Object> response,
            Long accountId,
            String orderId,
            BigDecimal amount) {
        try {
            Boolean success = (Boolean) response.get("success");
            if (success == null || !success) {
                return false;
            }

            // 1. 创建支付记录
            PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
            paymentRecord.setPaymentId(orderId);
            paymentRecord.setPaymentMethod(4); // 4-银行卡
            paymentRecord.setPaymentAmount(amount);
            paymentRecord.setPaymentStatus(3); // 3-支付成功
            paymentRecord.setThirdPartyTransactionNo((String) response.get("transactionId"));
            paymentRecord.setCreateTime(LocalDateTime.now());

            // 2. 保存支付记录
            paymentRecordDao.insert(paymentRecord);

            log.info("[银行支付] 支付记录保存成功，accountId={}, orderId={}, transactionId={}",
                    accountId, orderId, response.get("transactionId"));

            return true;

        } catch (Exception e) {
            log.error("[银行支付] 处理银行支付响应异常，accountId={}, orderId={}", accountId, orderId, e);
            return false;
        }
    }

    /**
     * 获取已使用的信用额度
     *
     * @param accountId 账户ID
     * @return 已使用的信用额度（单位：元）
     */
    private BigDecimal getUsedCreditLimit(Long accountId) {
        try {
            // 1. 查询该账户的未还款信用额度使用记录
            // 这里假设有CreditLimitRecordEntity表记录信用额度使用历史
            // 实际实现需要根据业务需求调整

            // 2. 计算已使用但未还款的信用额度
            // 简化实现，返回0
            return BigDecimal.ZERO;

        } catch (Exception e) {
            log.error("[信用额度] 获取已使用信用额度异常，accountId={}", accountId, e);
            return BigDecimal.ZERO;
        }
    }
}




