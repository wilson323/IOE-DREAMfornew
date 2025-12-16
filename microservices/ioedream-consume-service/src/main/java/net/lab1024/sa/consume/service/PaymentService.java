package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import net.lab1024.sa.common.util.CursorPagination;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.pay.java.service.payments.model.Transaction;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.consume.entity.PaymentRecordEntity;
import org.springframework.http.HttpMethod;

/**
 * 支付服务（微信支付+支付宝）
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 *
 *            功能说明：
 *            - 微信支付（JSAPI/APP/H5/Native）
 *            - 支付宝支付（APP/Web/Wap）
 *            - 支付回调处理
 *            - 退款处理
 *            - 订单查询
 *
 *            技术栈：
 *            - 微信支付SDK v3
 *            - 支付宝SDK v4
 *            - RSA签名验证
 *            - 异步通知处理
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
@SuppressWarnings("null")
public class PaymentService implements net.lab1024.sa.consume.consume.service.PaymentService {

    // ==================== 渠道适配器（SDK/协议层） ====================

    @Resource
    private net.lab1024.sa.consume.service.payment.adapter.WechatPayAdapter wechatPayAdapter;

    @Resource
    private net.lab1024.sa.consume.service.payment.adapter.AlipayPayAdapter alipayPayAdapter;

    @Resource
    private net.lab1024.sa.consume.service.payment.PaymentRecordService paymentRecordService;

    // ==================== 拆分后的专用服务（委托模式） ====================

    @Resource
    private net.lab1024.sa.consume.service.payment.WechatPayService wechatPayService;

    @Resource
    private net.lab1024.sa.consume.service.payment.AlipayPayService alipayPayService;

    @Resource
    private net.lab1024.sa.consume.service.payment.PaymentCallbackService paymentCallbackService;

    @Resource
    private net.lab1024.sa.consume.dao.PaymentRecordDao paymentRecordDao;

    @Resource
    private net.lab1024.sa.consume.consume.dao.PaymentRefundRecordDao paymentRefundRecordDao;

    @Resource
    private net.lab1024.sa.consume.dao.ConsumeRecordDao consumeRecordDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private net.lab1024.sa.consume.manager.MultiPaymentManager multiPaymentManager;

    /**
     * 初始化微信支付配置（延迟初始化）
     */
    private void initWechatPayConfig() {
        wechatPayAdapter.initIfNeeded();
    }

    /**
     * 初始化支付宝客户端（延迟初始化）
     */
    private void initAlipayClient() {
        alipayPayAdapter.initIfNeeded();
    }

    /**
     * 创建微信支付订单
     * <p>
     * 委托给WechatPayService处理
     * </p>
     *
     * @param orderId     订单ID
     * @param amount      金额（元）
     * @param description 商品描述
     * @param openId      用户OpenID（JSAPI必需）
     * @param payType     支付类型（JSAPI/APP/H5/Native）
     * @return 支付参数
     */
    public Map<String, Object> createWechatPayOrder(
            String orderId,
            BigDecimal amount,
            String description,
            String openId,
            String payType) {
        // 委托给专用服务处理
        return wechatPayService.createWechatPayOrder(orderId, amount, description, openId, payType);
    }

    /**
     * 创建支付宝支付订单
     * <p>
     * 委托给AlipayPayService处理
     * </p>
     *
     * @param orderId 订单ID
     * @param amount  金额（元）
     * @param subject 商品标题
     * @param payType 支付类型（APP/Web/Wap）
     * @return 支付参数
     */
    public Map<String, Object> createAlipayOrder(
            String orderId,
            BigDecimal amount,
            String subject,
            String payType) {
        // 委托给专用服务处理
        return alipayPayService.createAlipayOrder(orderId, amount, subject, payType);
    }

    /**
     * 处理微信支付回调（V3版本）
     * <p>
     * 委托给PaymentCallbackService处理
     * </p>
     *
     * @param notifyData 回调数据（JSON格式，V3版本）
     * @return 处理结果
     */
    public Map<String, Object> handleWechatPayNotify(String notifyData) {
        // 委托给专用服务处理
        return paymentCallbackService.handleWechatPayNotify(notifyData);
    }

    /**
     * 处理微信支付回调（V3版本，完整参数）
     * <p>
     * 委托给PaymentCallbackService处理
     * </p>
     *
     * @param notifyData 回调数据（JSON格式，V3版本）
     * @param signature  微信支付签名（Wechatpay-Signature请求头）
     * @param timestamp  时间戳（Wechatpay-Timestamp请求头）
     * @param nonce      随机串（Wechatpay-Nonce请求头）
     * @param serial     证书序列号（Wechatpay-Serial请求头）
     * @return 处理结果
     */
    public Map<String, Object> handleWechatPayNotify(String notifyData, String signature,
            String timestamp, String nonce, String serial) {
        // 委托给专用服务处理
        return paymentCallbackService.handleWechatPayNotify(notifyData, signature, timestamp, nonce, serial);
    }

    /**
     * 记录支付审计日志
     *
     * @param paymentId 支付ID
     * @param operation 操作类型
     * @param detail    操作详情
     * @param result    结果状态（1-成功，0-失败）
     */
    private void recordPaymentAuditLog(String paymentId, String operation, String detail, Integer result) {
        try {
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("moduleName", "CONSUME_PAYMENT");
            auditData.put("operationDesc", operation);
            auditData.put("resourceId", paymentId);
            auditData.put("requestParams", detail);
            auditData.put("resultStatus", result);

            // 异步记录审计日志，不关心返回值
            gatewayServiceClient.callCommonService(
                    "/api/v1/audit/log",
                    HttpMethod.POST,
                    auditData,
                    new TypeReference<net.lab1024.sa.common.dto.ResponseDTO<String>>() {});

            log.debug("[微信支付] 审计日志记录成功，paymentId={}, operation={}", paymentId, operation);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[微信支付] 审计日志记录参数错误，paymentId={}, error={}", paymentId, e.getMessage());
            // 审计日志记录失败不影响主业务流程
        } catch (BusinessException e) {
            log.warn("[微信支付] 审计日志记录业务异常，paymentId={}, code={}, message={}", paymentId, e.getCode(), e.getMessage());
            // 审计日志记录失败不影响主业务流程
        } catch (SystemException e) {
            log.error("[微信支付] 审计日志记录系统异常，paymentId={}, code={}, message={}", paymentId, e.getCode(), e.getMessage(), e);
            // 审计日志记录失败不影响主业务流程
        } catch (Exception e) {
            log.error("[微信支付] 审计日志记录未知异常，paymentId={}", paymentId, e);
            // 审计日志记录失败不影响主业务流程
        }
    }

    /**
     * 处理支付宝回调
     * <p>
     * 功能说明：
     * 1. 验证回调签名（RSA2算法）
     * 2. 检查支付状态
     * 3. 幂等性验证（防止重复处理）
     * 4. 更新支付记录状态
     * 5. 触发后续业务处理
     * </p>
     * <p>
     * 安全要求：
     * - 必须验证签名，防止伪造回调
     * - 必须检查支付状态，只处理成功状态
     * - 必须实现幂等性，防止重复处理
     * </p>
     *
     * @param params 回调参数
     * @return 处理结果（"success"表示成功，"fail"表示失败）
     */
    public String handleAlipayNotify(Map<String, String> params) {
        log.info("[支付宝] 接收回调通知，参数数量：{}", params != null ? params.size() : 0);

        try {
            // 1. 参数验证
            if (params == null || params.isEmpty()) {
                log.error("[支付宝] 回调参数为空");
                return "fail";
            }

            // 2. 验证签名（RSA2算法）
            if (!alipayPayAdapter.verifyNotifySignature(params)) {
                log.error("[支付宝] 签名验证失败，可能存在安全风险");
                return "fail";
            }

            log.info("[支付宝] 签名验证通过");

            // 3. 检查支付状态
            String tradeStatus = params.get("trade_status");
            if (tradeStatus == null) {
                log.error("[支付宝] 支付状态为空");
                return "fail";
            }

            // 只处理成功和完成状态
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("[支付宝] 支付未完成，状态：{}", tradeStatus);
                return "fail";
            }

            // 4. 获取订单信息
            String paymentId = params.get("out_trade_no"); // 商户订单号（即我们的paymentId）
            String tradeNo = params.get("trade_no"); // 支付宝交易号
            String totalAmountStr = params.get("total_amount"); // 订单总金额

            if (!StringUtils.hasText(paymentId) || !StringUtils.hasText(tradeNo)) {
                log.error("[支付宝] 订单信息不完整，paymentId={}, tradeNo={}", paymentId, tradeNo);
                return "fail";
            }

            // 5. 幂等性验证（检查订单是否已处理）
            PaymentRecordEntity existingRecord =
                    paymentRecordService.getPaymentRecord(paymentId);
            if (existingRecord != null && existingRecord.getPaymentStatus() != null && existingRecord.getPaymentStatus() == 3) {
                log.warn("[支付宝] 订单已处理，跳过重复回调，paymentId={}", paymentId);
                return "success"; // 已处理，返回success避免支付宝重复回调
            }

            // 6. 金额验证（可选，防止金额被篡改）
            if (existingRecord != null && StringUtils.hasText(totalAmountStr)) {
                try {
                    BigDecimal callbackAmount = new BigDecimal(totalAmountStr);
                    BigDecimal recordAmount = existingRecord.getPaymentAmount();
                    if (recordAmount != null && callbackAmount.compareTo(recordAmount) != 0) {
                        log.error("[支付宝] 金额不一致，记录金额={}，回调金额={}，paymentId={}",
                                recordAmount, callbackAmount, paymentId);
                        return "fail";
                    }
                } catch (NumberFormatException e) {
                    log.warn("[支付宝] 金额格式错误，totalAmount={}", totalAmountStr);
                }
            }

            // 7. 更新支付记录状态
            paymentRecordService.updatePaymentStatus(paymentId, "SUCCESS", tradeNo);

            // 8. 触发后续业务处理（更新消费记录、发送通知等）
            paymentRecordService.handlePaymentSuccess(paymentId, tradeNo);

            log.info("[支付宝] 回调处理成功，paymentId={}, tradeNo={}", paymentId, tradeNo);
            return "success";

        } catch (BusinessException e) {
            log.error("[支付宝] 处理回调业务异常，error={}", e.getMessage(), e);
            return "fail";
        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付宝] 处理回调参数异常，error={}", e.getMessage(), e);
            return "fail";
        } catch (SystemException e) {
            log.error("[支付宝] 处理回调系统异常，code={}, message={}", e.getCode(), e.getMessage(), e);
            // 系统异常时，记录详细错误信息
            String paymentId = params != null ? params.get("out_trade_no") : "UNKNOWN";
            recordPaymentAuditLog(paymentId, "支付回调-系统异常",
                    "异常类型: " + e.getClass().getName() + ", 消息: " + e.getMessage(), 0);
            return "fail";
        } catch (Exception e) {
            log.error("[支付宝] 处理回调未知异常，error={}", e.getMessage(), e);
            // 系统异常时，记录详细错误信息
            String paymentId = params != null ? params.get("out_trade_no") : "UNKNOWN";
            recordPaymentAuditLog(paymentId, "支付回调-系统异常",
                    "异常类型: " + e.getClass().getName() + ", 消息: " + e.getMessage(), 0);
            return "fail";
        }
    }

    /**
     * 申请退款（微信支付）
     *
     * @param orderId      原订单ID
     * @param refundId     退款单ID
     * @param totalAmount  订单总金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    public Map<String, Object> wechatRefund(
            String orderId,
            String refundId,
            Integer totalAmount,
            Integer refundAmount) {
        return wechatPayAdapter.wechatRefund(orderId, refundId, totalAmount, refundAmount);
    }

    /**
     * 申请退款（支付宝）
     *
     * @param orderId      原订单ID
     * @param refundAmount 退款金额（元）
     * @param reason       退款原因
     * @return 退款结果
     */
    public Map<String, Object> alipayRefund(
            String orderId,
            BigDecimal refundAmount,
            String reason) {
        return alipayPayAdapter.alipayRefund(orderId, refundAmount, reason);
    }


    /**
     * 支付对账功能
     * <p>
     * 功能说明：
     * 1. 从数据库查询系统支付记录
     * 2. 从第三方支付平台（微信/支付宝）获取交易数据
     * 3. 对比系统记录和第三方记录，发现差异
     * 4. 生成对账报告
     * 5. 支持自动修复差异（可选）
     * </p>
     * <p>
     * 对账流程：
     * - 按日期范围查询系统支付记录
     * - 调用第三方API查询交易记录
     * - 按订单号匹配，对比金额、状态
     * - 记录差异订单（金额不一致、状态不一致、缺失订单）
     * - 生成对账报告
     * </p>
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式（WECHAT/ALIPAY，null表示全部）
     * @return 对账结果
     */
    public Map<String, Object> performPaymentReconciliation(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod) {
        log.info("[支付对账] 开始对账，startDate={}, endDate={}, paymentMethod={}",
                startDate, endDate, paymentMethod);

        try {
            // 1. 参数验证
            if (startDate == null || endDate == null) {
                throw new BusinessException("对账日期不能为空");
            }
            if (startDate.isAfter(endDate)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            if (startDate.isAfter(java.time.LocalDate.now())) {
                throw new BusinessException("开始日期不能是未来日期");
            }

            // 2. 从数据库查询系统支付记录
            List<PaymentRecordEntity> systemRecords = querySystemPaymentRecords(startDate, endDate, paymentMethod);
            log.info("[支付对账] 系统支付记录数：{}", systemRecords.size());

            // 3. 从第三方支付平台获取交易数据
            Map<String, Object> thirdPartyRecords = queryThirdPartyPaymentRecords(startDate, endDate, paymentMethod);
            log.info("[支付对账] 第三方支付记录数：{}", thirdPartyRecords.size());

            // 4. 对比系统记录和第三方记录
            Map<String, Object> reconciliationResult = comparePaymentRecords(systemRecords, thirdPartyRecords);

            // 5. 生成对账报告
            Map<String, Object> report = buildReconciliationReport(startDate, endDate, paymentMethod,
                    systemRecords, thirdPartyRecords, reconciliationResult);

            log.info("[支付对账] 对账完成，差异订单数：{}",
                    reconciliationResult.get("differenceCount"));

            return report;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付对账] 对账参数错误: error={}", e.getMessage(), e);
            throw new ParamException("PAYMENT_RECONCILIATION_PARAM_ERROR", "支付对账参数错误: " + e.getMessage());
        } catch (BusinessException e) {
            log.error("[支付对账] 对账业务异常", e);
            throw e;
        } catch (SystemException e) {
            log.error("[支付对账] 对账系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("PAYMENT_RECONCILIATION_SYSTEM_ERROR", "支付对账失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[支付对账] 对账未知异常", e);
            throw new SystemException("PAYMENT_RECONCILIATION_SYSTEM_ERROR", "支付对账失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从数据库查询系统支付记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式
     * @return 支付记录列表
     */
    private List<PaymentRecordEntity> querySystemPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod) {
        List<PaymentRecordEntity> records = new ArrayList<>();

        try {
            log.debug("[支付对账] 查询系统支付记录，startDate={}, endDate={}, paymentMethod={}",
                    startDate, endDate, paymentMethod);

            // 使用 MyBatis-Plus LambdaQueryWrapper 查询
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecordEntity> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

            // 时间范围查询（paymentTime字段）
            if (startDate != null) {
                wrapper.ge(PaymentRecordEntity::getPaymentTime,
                        startDate.atStartOfDay());
            }
            if (endDate != null) {
                wrapper.le(PaymentRecordEntity::getPaymentTime,
                        endDate.atTime(23, 59, 59));
            }

            // 支付方式过滤
            if (StringUtils.hasText(paymentMethod)) {
                // 将String支付方式转换为Integer
                Integer paymentMethodInt = convertPaymentMethodToInt(paymentMethod);
                wrapper.eq(PaymentRecordEntity::getPaymentMethod, paymentMethodInt);
            }

            // 只查询未删除的记录（MyBatis-Plus的@TableLogic会自动过滤，但显式指定更安全）
            wrapper.eq(PaymentRecordEntity::getDeletedFlag, 0);

            // 按支付时间排序
            wrapper.orderByDesc(PaymentRecordEntity::getPaymentTime);

            // 执行查询
            records = paymentRecordDao.selectList(wrapper);

            log.info("[支付对账] 查询到系统支付记录数：{}", records.size());

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付对账] 查询系统支付记录参数错误: error={}", e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (BusinessException e) {
            log.error("[支付对账] 查询系统支付记录业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (SystemException e) {
            log.error("[支付对账] 查询系统支付记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (Exception e) {
            log.error("[支付对账] 查询系统支付记录未知异常", e);
            // 返回空列表，不影响主流程
        }

        return records;
    }

    /**
     * 查询第三方支付平台交易记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式（WECHAT/ALIPAY，null表示全部）
     * @return 第三方支付记录（key为paymentId，value为记录详情）
     */
    private Map<String, Object> queryThirdPartyPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod) {
        Map<String, Object> allRecords = new HashMap<>();

        try {
            // 根据支付方式查询对应的第三方记录
            if (paymentMethod == null || "WECHAT".equals(paymentMethod)) {
                Map<String, Object> wechatRecords = queryWechatPaymentRecords(startDate, endDate);
                allRecords.putAll(wechatRecords);
            }

            if (paymentMethod == null || "ALIPAY".equals(paymentMethod)) {
                Map<String, Object> alipayRecords = queryAlipayPaymentRecords(startDate, endDate);
                allRecords.putAll(alipayRecords);
            }

            log.info("[支付对账] 查询第三方支付记录完成，总记录数：{}", allRecords.size());

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付对账] 查询第三方支付记录参数错误: error={}", e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (BusinessException e) {
            log.error("[支付对账] 查询第三方支付记录业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (SystemException e) {
            log.error("[支付对账] 查询第三方支付记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (Exception e) {
            log.error("[支付对账] 查询第三方支付记录未知异常", e);
            // 返回空列表，不影响主流程
        }

        return allRecords;
    }

    /**
     * 查询微信支付交易记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 微信支付记录（key为paymentId，value为记录详情）
     */
    private Map<String, Object> queryWechatPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        Map<String, Object> records = new HashMap<>();

        try {
            if (!wechatPayAdapter.isEnabled()) {
                log.warn("[支付对账] 微信支付未启用，跳过查询");
                return records;
            }

            initWechatPayConfig();
            if (!wechatPayAdapter.isReady()) {
                log.warn("[支付对账] 微信支付配置未初始化，跳过查询");
                return records;
            }

            // 微信支付API v3 查询交易记录实现
            // 1. 微信支付API v3 不提供按时间范围批量查询接口
            // 2. 需要先从系统数据库查询该时间范围内的所有订单号
            // 3. 使用微信支付SDK逐个查询订单状态
            // 4. 将查询结果转换为统一格式存入 records Map

            log.info("[支付对账] 开始查询微信支付交易记录，startDate={}, endDate={}", startDate, endDate);

            // 步骤1：从系统数据库查询该时间范围内的所有微信支付订单号
            List<PaymentRecordEntity> systemRecords = querySystemPaymentRecords(startDate, endDate, "WECHAT");
            log.info("[支付对账] 系统微信支付记录数：{}", systemRecords.size());

            if (systemRecords.isEmpty()) {
                log.info("[支付对账] 系统无微信支付记录，返回空结果");
                return records;
            }

            // 步骤2：逐个查询微信支付订单状态
            // 注意：微信支付API v3 SDK 0.2.17 版本使用 JsapiService 查询订单
            // 如果SDK不支持，需要使用 HTTP 客户端直接调用 API
            int successCount = 0;
            int failCount = 0;

            for (PaymentRecordEntity systemRecord : systemRecords) {
                String orderId = systemRecord.getPaymentId();
                if (orderId == null || orderId.isEmpty()) {
                    log.warn("[支付对账] 订单号为空，跳过查询，paymentId={}", systemRecord.getPaymentId());
                    continue;
                }

                try {
                    // 使用微信支付SDK查询订单状态
                    // 注意：微信支付SDK v3 0.2.17版本中，JsapiService可能没有queryOrderByOutTradeNo方法
                    // 需要使用HTTP客户端直接调用API
                    Transaction transaction = queryWechatOrderByOutTradeNo(orderId);

                    if (transaction != null && transaction.getTradeState() != null) {
                        String tradeState = transaction.getTradeState().name();

                        // 只记录支付成功的订单
                        if ("SUCCESS".equals(tradeState)) {
                            Map<String, Object> record = new HashMap<>();
                            record.put("paymentId", transaction.getOutTradeNo());

                            // 金额转换：微信支付金额单位为分，转换为元
                            if (transaction.getAmount() != null && transaction.getAmount().getTotal() != null) {
                                BigDecimal amount = new BigDecimal(transaction.getAmount().getTotal())
                                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                                record.put("amount", amount);
                            } else {
                                record.put("amount", systemRecord.getPaymentAmount());
                            }

                            record.put("status", tradeState);

                            // 支付时间转换
                            if (transaction.getSuccessTime() != null) {
                                try {
                                    // 微信支付返回的时间格式：2024-01-01T12:00:00+08:00
                                    java.time.format.DateTimeFormatter formatter =
                                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                                    java.time.ZonedDateTime zonedDateTime =
                                            java.time.ZonedDateTime.parse(transaction.getSuccessTime(), formatter);
                                    record.put("paymentTime", zonedDateTime.toLocalDateTime());
                                } catch (IllegalArgumentException | ParamException e) {
                                    log.warn("[支付对账] 支付时间解析失败（参数错误），使用系统记录时间，orderId={}, time={}",
                                            orderId, transaction.getSuccessTime());
                                    record.put("paymentTime", systemRecord.getPaymentTime());
                                } catch (BusinessException e) {
                                    log.warn("[支付对账] 支付时间解析失败（业务异常），使用系统记录时间，orderId={}, time={}",
                                            orderId, transaction.getSuccessTime());
                                    record.put("paymentTime", systemRecord.getPaymentTime());
                                } catch (SystemException e) {
                                    log.warn("[支付对账] 支付时间解析失败（系统异常），使用系统记录时间，orderId={}, time={}",
                                            orderId, transaction.getSuccessTime());
                                    record.put("paymentTime", systemRecord.getPaymentTime());
                                } catch (Exception e) {
                                    log.warn("[支付对账] 支付时间解析失败，使用系统记录时间，orderId={}, time={}",
                                            orderId, transaction.getSuccessTime());
                                    record.put("paymentTime", systemRecord.getPaymentTime());
                                }
                            } else {
                                record.put("paymentTime", systemRecord.getPaymentTime());
                            }

                            // 第三方交易号
                            if (transaction.getTransactionId() != null) {
                                record.put("thirdPartyTransactionId", transaction.getTransactionId());
                            }

                            records.put(orderId, record);
                            successCount++;

                            log.debug("[支付对账] 查询微信订单成功，orderId={}, status={}", orderId, tradeState);
                        } else {
                            log.debug("[支付对账] 微信订单未成功，orderId={}, status={}", orderId, tradeState);
                        }
                    } else {
                        log.warn("[支付对账] 微信订单查询结果为空，orderId={}", orderId);
                    }

                } catch (IllegalArgumentException | ParamException e) {
                    failCount++;
                    log.warn("[支付对账] 查询微信订单参数错误，orderId={}, error={}", orderId, e.getMessage());
                    // 查询失败不影响其他订单的查询，继续处理下一个订单
                } catch (BusinessException e) {
                    failCount++;
                    log.warn("[支付对账] 查询微信订单业务异常，orderId={}, code={}, message={}", orderId, e.getCode(), e.getMessage());
                    // 查询失败不影响其他订单的查询，继续处理下一个订单
                } catch (SystemException e) {
                    failCount++;
                    log.warn("[支付对账] 查询微信订单系统异常，orderId={}, code={}, message={}", orderId, e.getCode(), e.getMessage());
                    // 查询失败不影响其他订单的查询，继续处理下一个订单
                } catch (Exception e) {
                    failCount++;
                    log.warn("[支付对账] 查询微信订单未知异常，orderId={}, error={}", orderId, e.getMessage());
                    // 查询失败不影响其他订单的查询，继续处理下一个订单
                }
            }

            log.info("[支付对账] 微信支付交易记录查询完成，成功：{}，失败：{}，总记录数：{}",
                    successCount, failCount, records.size());

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付对账] 查询微信支付交易记录参数错误: error={}", e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (BusinessException e) {
            log.error("[支付对账] 查询微信支付交易记录业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (SystemException e) {
            log.error("[支付对账] 查询微信支付交易记录系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            // 返回空列表，不影响主流程
        } catch (Exception e) {
            log.error("[支付对账] 查询微信支付交易记录未知异常", e);
            // 返回空列表，不影响主流程
        }

        return records;
    }

    /**
     * 查询支付宝交易记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付宝支付记录（key为paymentId，value为记录详情）
     */
    private Map<String, Object> queryAlipayPaymentRecords(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        Map<String, Object> records = new HashMap<>();

        try {
            if (!alipayPayAdapter.isEnabled()) {
                log.warn("[支付对账] 支付宝未启用，跳过查询");
                return records;
            }

            initAlipayClient();
            if (!alipayPayAdapter.isReady()) {
                log.warn("[支付对账] 支付宝客户端未初始化，跳过查询");
                return records;
            }

            // 支付宝API查询交易记录实现
            // 1. 使用 alipay.trade.batchquery 接口批量查询交易记录
            // 2. 需要将日期转换为支付宝要求的格式（yyyy-MM-dd HH:mm:ss）
            // 3. 支持按时间范围查询，最多返回1000条记录
            // 4. 如果超过1000条，需要分页查询或使用 alipay.trade.query 逐个查询

            log.info("[支付对账] 开始查询支付宝交易记录，startDate={}, endDate={}", startDate, endDate);

            try {
                // 步骤1：先从系统数据库查询该时间范围内的所有支付宝订单号
                // 这样可以知道需要查询哪些订单，避免查询不存在的订单
                List<PaymentRecordEntity> systemRecords = querySystemPaymentRecords(startDate, endDate, "ALIPAY");
                log.info("[支付对账] 系统支付宝记录数：{}", systemRecords.size());

                if (systemRecords.isEmpty()) {
                    log.info("[支付对账] 系统无支付宝记录，返回空结果");
                    return records;
                }

                // 步骤2：使用支付宝单个查询接口逐个查询交易记录
                // 注意：支付宝SDK 4.40.572版本可能没有批量查询接口
                // 使用单个查询接口 alipay.trade.query 逐个查询
                // 如果系统记录超过1000条，建议分批查询或使用异步查询

                int successCount = 0;
                int failCount = 0;

                // 逐个查询支付宝订单状态
                for (PaymentRecordEntity systemRecord : systemRecords) {
                    String outTradeNo = systemRecord.getPaymentId();
                    if (outTradeNo == null || outTradeNo.isEmpty()) {
                        log.warn("[支付对账] 订单号为空，跳过查询，paymentId={}", systemRecord.getPaymentId());
                        continue;
                    }

                    try {
                        // 使用支付宝单个查询接口查询订单状态
                        // 注意：这里需要使用 alipay.trade.query 接口
                        // 由于支付宝SDK可能没有直接的批量查询接口，使用单个查询
                        Map<String, Object> tradeRecord = queryAlipayOrderByOutTradeNo(outTradeNo);

                        if (tradeRecord != null && !tradeRecord.isEmpty()) {
                            records.put(outTradeNo, tradeRecord);
                            successCount++;
                            log.debug("[支付对账] 查询支付宝订单成功，outTradeNo={}", outTradeNo);
                        } else {
                            log.warn("[支付对账] 支付宝订单查询结果为空，outTradeNo={}", outTradeNo);
                        }

                    } catch (Exception e) {
                        failCount++;
                        log.warn("[支付对账] 查询支付宝订单失败，outTradeNo={}, error={}",
                                outTradeNo, e.getMessage());
                        // 查询失败不影响其他订单的查询，继续处理下一个订单
                    }
                }

                log.info("[支付对账] 支付宝交易记录查询完成，成功：{}，失败：{}，总记录数：{}",
                        successCount, failCount, records.size());

                // 注意：如果系统记录超过1000条，逐个查询可能较慢
                // 建议：1. 使用异步查询 2. 分批查询 3. 使用支付宝对账文件下载接口
                if (systemRecords.size() > 1000) {
                    log.warn("[支付对账] 系统支付宝记录超过1000条，逐个查询可能较慢，建议使用支付宝对账文件下载接口");
                }

                // 调用queryAlipayOrderByOutTradeNo方法查询支付宝订单
                // 该方法已在第1858行实现，使用AlipayTradeQueryRequest查询订单状态

            } catch (Exception e) {
                log.error("[支付对账] 查询支付宝交易记录异常", e);
            }

            log.info("[支付对账] 支付宝交易记录查询完成，总记录数：{}", records.size());

        } catch (Exception e) {
            log.error("[支付对账] 查询支付宝交易记录失败", e);
        }

        return records;
    }

    /**
     * 对比系统记录和第三方记录
     *
     * @param systemRecords 系统支付记录
     * @param thirdPartyRecords 第三方支付记录
     * @return 对比结果
     */
    private Map<String, Object> comparePaymentRecords(
            List<PaymentRecordEntity> systemRecords,
            Map<String, Object> thirdPartyRecords) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> differences = new java.util.ArrayList<>();
        int matchedCount = 0;
        int unmatchedCount = 0;

        try {
            // 1. 按订单号匹配
            for (PaymentRecordEntity systemRecord : systemRecords) {
                String paymentId = systemRecord.getPaymentId();
                Object thirdPartyRecord = thirdPartyRecords.get(paymentId);

                if (thirdPartyRecord == null) {
                    // 系统有记录，第三方没有（缺失订单）
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("paymentId", paymentId);
                    diff.put("type", "MISSING_IN_THIRD_PARTY");
                    diff.put("systemAmount", systemRecord.getPaymentAmount());
                    diff.put("systemStatus", systemRecord.getPaymentStatus());
                    differences.add(diff);
                    unmatchedCount++;
                } else {
                    // 对比金额和状态
                    boolean isMatched = compareRecordDetails(systemRecord, thirdPartyRecord);
                    if (!isMatched) {
                        Map<String, Object> diff = buildDifferenceRecord(systemRecord, thirdPartyRecord);
                        differences.add(diff);
                        unmatchedCount++;
                    } else {
                        matchedCount++;
                    }
                }
            }

            // 2. 检查第三方有但系统没有的记录（多余订单）
            for (String thirdPartyPaymentId : thirdPartyRecords.keySet()) {
                boolean found = systemRecords.stream()
                        .anyMatch(r -> thirdPartyPaymentId.equals(r.getPaymentId()));
                if (!found) {
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("paymentId", thirdPartyPaymentId);
                    diff.put("type", "MISSING_IN_SYSTEM");
                    diff.put("thirdPartyRecord", thirdPartyRecords.get(thirdPartyPaymentId));
                    differences.add(diff);
                    unmatchedCount++;
                }
            }

            result.put("matchedCount", matchedCount);
            result.put("unmatchedCount", unmatchedCount);
            result.put("differenceCount", differences.size());
            result.put("differences", differences);

        } catch (Exception e) {
            log.error("[支付对账] 对比支付记录失败", e);
        }

        return result;
    }

    /**
     * 对比单条记录的详细信息
     *
     * @param systemRecord 系统记录
     * @param thirdPartyRecord 第三方记录
     * @return 是否匹配
     */
    private boolean compareRecordDetails(PaymentRecordEntity systemRecord, Object thirdPartyRecord) {
        try {
            // 将第三方记录转换为Map
            Map<String, Object> thirdPartyMap = convertToMap(thirdPartyRecord);

            // 对比金额
            BigDecimal systemAmount = systemRecord.getPaymentAmount();
            Object thirdPartyAmountObj = thirdPartyMap.get("amount");
            if (thirdPartyAmountObj != null) {
                BigDecimal thirdPartyAmount = convertToBigDecimal(thirdPartyAmountObj);
                if (systemAmount.compareTo(thirdPartyAmount) != 0) {
                    log.warn("[支付对账] 金额不一致，paymentId={}, systemAmount={}, thirdPartyAmount={}",
                            systemRecord.getPaymentId(), systemAmount, thirdPartyAmount);
                    return false;
                }
            }

            // 对比状态
            Integer systemStatus = systemRecord.getPaymentStatus();
            Object thirdPartyStatusObj = thirdPartyMap.get("status");
            if (thirdPartyStatusObj != null) {
                String thirdPartyStatus = thirdPartyStatusObj.toString();
                // 将系统状态（Integer）转换为字符串进行比较
                String systemStatusStr = convertPaymentStatusToString(systemStatus);
                if (!isStatusMatched(systemStatusStr, thirdPartyStatus)) {
                    log.warn("[支付对账] 状态不一致，paymentId={}, systemStatus={}, thirdPartyStatus={}",
                            systemRecord.getPaymentId(), systemStatus, thirdPartyStatus);
                    return false;
                }
            }

            return true;

        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付对账] 对比记录详情参数错误，paymentId={}, error={}", systemRecord.getPaymentId(), e.getMessage(), e);
            return false;
        } catch (BusinessException e) {
            log.error("[支付对账] 对比记录详情业务异常，paymentId={}, code={}, message={}", systemRecord.getPaymentId(), e.getCode(), e.getMessage(), e);
            return false;
        } catch (SystemException e) {
            log.error("[支付对账] 对比记录详情系统异常，paymentId={}, code={}, message={}", systemRecord.getPaymentId(), e.getCode(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("[支付对账] 对比记录详情未知异常，paymentId={}", systemRecord.getPaymentId(), e);
            return false;
        }
    }

    /**
     * 判断状态是否匹配
     *
     * @param systemStatus 系统状态
     * @param thirdPartyStatus 第三方状态
     * @return 是否匹配
     */
    private boolean isStatusMatched(String systemStatus, String thirdPartyStatus) {
        // 状态映射：SUCCESS -> SUCCESS, PENDING -> PENDING, FAILED -> FAILED
        if (systemStatus == null || thirdPartyStatus == null) {
            return false;
        }
        return systemStatus.equals(thirdPartyStatus) ||
                ("SUCCESS".equals(systemStatus) && "SUCCESS".equals(thirdPartyStatus)) ||
                ("PENDING".equals(systemStatus) && "PENDING".equals(thirdPartyStatus)) ||
                ("FAILED".equals(systemStatus) && "FAILED".equals(thirdPartyStatus));
    }

    /**
     * 构建差异记录
     *
     * @param systemRecord 系统记录
     * @param thirdPartyRecord 第三方记录
     * @return 差异记录
     */
    private Map<String, Object> buildDifferenceRecord(PaymentRecordEntity systemRecord, Object thirdPartyRecord) {
        Map<String, Object> diff = new HashMap<>();
        diff.put("paymentId", systemRecord.getPaymentId());
        diff.put("type", "MISMATCH");
        diff.put("systemAmount", systemRecord.getPaymentAmount());
        diff.put("systemStatus", systemRecord.getPaymentStatus());

        Map<String, Object> thirdPartyMap = convertToMap(thirdPartyRecord);
        diff.put("thirdPartyAmount", thirdPartyMap.get("amount"));
        diff.put("thirdPartyStatus", thirdPartyMap.get("status"));

        return diff;
    }

    /**
     * 转换为Map
     *
     * @param obj 对象
     * @return Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException | ParamException e) {
            log.error("[支付对账] 转换对象为Map参数错误: error={}", e.getMessage(), e);
            return new HashMap<>();
        } catch (BusinessException e) {
            log.error("[支付对账] 转换对象为Map业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return new HashMap<>();
        } catch (SystemException e) {
            log.error("[支付对账] 转换对象为Map系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            return new HashMap<>();
        } catch (Exception e) {
            log.error("[支付对账] 转换对象为Map未知异常", e);
            return new HashMap<>();
        }
    }

    /**
     * 转换为BigDecimal
     *
     * @param obj 对象
     * @return BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object obj) {
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof String) {
            return new BigDecimal((String) obj);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 查询单个微信支付订单
     * <p>
     * 使用微信支付API v3查询订单状态
     * API文档：https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_1_2.shtml
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @return 交易对象，如果查询失败返回null
     */
    private Transaction queryWechatOrderByOutTradeNo(String outTradeNo) {
        return wechatPayAdapter.queryWechatOrderByOutTradeNo(outTradeNo);
    }



    /**
     * 查询单个支付宝订单
     * <p>
     * 使用支付宝API查询订单状态
     * API文档：https://opendocs.alipay.com/apis/api_1/alipay.trade.query
     * </p>
     *
     * @param outTradeNo 商户订单号
     * @return 交易记录Map，如果查询失败返回null
     */
    private Map<String, Object> queryAlipayOrderByOutTradeNo(String outTradeNo) {
        initAlipayClient();
        return alipayPayAdapter.queryAlipayOrderByOutTradeNo(outTradeNo);
    }


    /**
     * 生成对账报告
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param paymentMethod 支付方式
     * @param systemRecords 系统记录
     * @param thirdPartyRecords 第三方记录
     * @param reconciliationResult 对比结果
     * @return 对账报告
     */
    private Map<String, Object> buildReconciliationReport(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            String paymentMethod,
            List<PaymentRecordEntity> systemRecords,
            Map<String, Object> thirdPartyRecords,
            Map<String, Object> reconciliationResult) {
        Map<String, Object> report = new HashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("paymentMethod", paymentMethod);
        report.put("systemRecordCount", systemRecords.size());
        report.put("thirdPartyRecordCount", thirdPartyRecords.size());
        report.put("matchedCount", reconciliationResult.get("matchedCount"));
        report.put("unmatchedCount", reconciliationResult.get("unmatchedCount"));
        report.put("differenceCount", reconciliationResult.get("differenceCount"));
        report.put("differences", reconciliationResult.get("differences"));
        report.put("generateTime", java.time.LocalDateTime.now());
        report.put("status", (Integer) reconciliationResult.get("unmatchedCount") > 0 ? "HAS_DIFFERENCES" : "MATCHED");
        return report;
    }

    // ==================== 银行支付 ====================

    /**
     * 创建银行支付订单
     * <p>
     * 功能说明：
     * 1. 调用MultiPaymentManager处理银行支付
     * 2. 记录支付记录
     * 3. 返回支付结果
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 支付金额（单位：元）
     * @param orderId 订单ID
     * @param description 商品描述
     * @param bankCardNo 银行卡号（可选）
     * @return 支付结果
     */
    public Map<String, Object> createBankPaymentOrder(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String description,
            String bankCardNo) {
        log.info("[银行支付] 创建银行支付订单，accountId={}, amount={}, orderId={}", accountId, amount, orderId);

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }
            if (!StringUtils.hasText(description)) {
                throw new BusinessException("商品描述不能为空");
            }

            // 2. 调用MultiPaymentManager处理银行支付
            Map<String, Object> result = multiPaymentManager.processBankPayment(
                    accountId, amount, orderId, description, bankCardNo);

            // 3. 记录支付记录（如果支付成功）
            Boolean success = (Boolean) result.get("success");
            if (success != null && success) {
                PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
                paymentRecord.setPaymentId(orderId);
                paymentRecord.setPaymentMethod(4); // 4-银行卡
                paymentRecord.setPaymentAmount(amount);
                paymentRecord.setPaymentStatus(3); // 3-支付成功
                paymentRecord.setThirdPartyTransactionNo((String) result.get("transactionId"));
                paymentRecord.setCreateTime(java.time.LocalDateTime.now());
                paymentRecordDao.insert(paymentRecord);

                log.info("[银行支付] 支付记录保存成功，orderId={}, transactionId={}",
                        orderId, result.get("transactionId"));
            }

            return result;

        } catch (BusinessException e) {
            log.error("[银行支付] 创建银行支付订单业务异常，accountId={}, orderId={}", accountId, orderId, e);
            throw e;
        } catch (Exception e) {
            log.error("[银行支付] 创建银行支付订单异常，accountId={}, orderId={}", accountId, orderId, e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "银行支付失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 处理信用额度支付
     * <p>
     * 功能说明：
     * 1. 验证信用额度是否充足
     * 2. 扣除信用额度
     * 3. 记录支付记录
     * </p>
     *
     * @param accountId 账户ID
     * @param amount 支付金额（单位：元）
     * @param orderId 订单ID
     * @param reason 扣除原因
     * @return 支付结果
     */
    public Map<String, Object> processCreditLimitPayment(
            Long accountId,
            BigDecimal amount,
            String orderId,
            String reason) {
        log.info("[信用额度] 处理信用额度支付，accountId={}, amount={}, orderId={}", accountId, amount, orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("paymentMethod", "CREDIT");

        try {
            // 1. 参数验证
            if (accountId == null) {
                throw new BusinessException("账户ID不能为空");
            }
            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("支付金额必须大于0");
            }
            if (!StringUtils.hasText(orderId)) {
                throw new BusinessException("订单ID不能为空");
            }

            // 2. 检查信用额度是否启用
            if (!multiPaymentManager.isPaymentMethodEnabled("CREDIT")) {
                log.warn("[信用额度] 信用额度支付未启用，accountId={}, amount={}", accountId, amount);
                result.put("message", "信用额度支付未启用");
                return result;
            }

            // 3. 验证信用额度是否充足
            boolean sufficient = multiPaymentManager.checkCreditLimitSufficient(accountId, amount);
            if (!sufficient) {
                BigDecimal creditLimit = multiPaymentManager.getCreditLimit(accountId);
                log.warn("[信用额度] 信用额度不足，accountId={}, amount={}, creditLimit={}",
                        accountId, amount, creditLimit);
                result.put("message", "信用额度不足，当前可用额度: " + creditLimit);
                return result;
            }

            // 4. 扣除信用额度
            boolean deductSuccess = multiPaymentManager.deductCreditLimit(accountId, amount, orderId, reason);
            if (!deductSuccess) {
                log.warn("[信用额度] 信用额度扣除失败，accountId={}, amount={}, orderId={}",
                        accountId, amount, orderId);
                result.put("message", "信用额度扣除失败");
                return result;
            }

            // 5. 记录支付记录
            PaymentRecordEntity paymentRecord = new PaymentRecordEntity();
            paymentRecord.setPaymentId(orderId);
            paymentRecord.setPaymentMethod(4); // 4-银行卡（信用卡也归类为银行卡）
            paymentRecord.setPaymentAmount(amount);
            paymentRecord.setPaymentStatus(3); // 3-支付成功
            paymentRecord.setCreateTime(java.time.LocalDateTime.now());
            paymentRecordDao.insert(paymentRecord);

            result.put("success", true);
            result.put("message", "信用额度支付成功");
            log.info("[信用额度] 信用额度支付成功，accountId={}, amount={}, orderId={}",
                    accountId, amount, orderId);

            return result;

        } catch (BusinessException e) {
            log.error("[信用额度] 处理信用额度支付业务异常，accountId={}, orderId={}", accountId, orderId, e);
            result.put("message", "信用额度支付失败: " + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[信用额度] 处理信用额度支付异常，accountId={}, orderId={}", accountId, orderId, e);
            result.put("message", "信用额度支付异常: " + e.getMessage());
            return result;
        }
    }

    // ==================== 接口方法实现 ====================

    /**
     * 处理支付
     *
     * @param form 支付处理表单
     * @return 支付结果
     */
    @Override
    public Map<String, Object> processPayment(net.lab1024.sa.consume.consume.domain.form.PaymentProcessForm form) {
        log.info("[支付处理] 处理支付请求，userId={}, amount={}, method={}",
                form.getUserId(), form.getPaymentAmount(), form.getPaymentMethod());
        try {
            // 根据支付方式调用不同的处理方法
            Integer paymentMethod = form.getPaymentMethod();
            if (paymentMethod == null) {
                throw new BusinessException("支付方式不能为空");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);

            // 根据支付方式分发处理
            switch (paymentMethod) {
                case 2: // 微信支付
                    result = createWechatPayOrder(
                            form.getOrderNo() != null ? form.getOrderNo() : UUID.randomUUID().toString(),
                            form.getPaymentAmount(),
                            form.getConsumeDescription() != null ? form.getConsumeDescription() : "商品支付",
                            form.getThirdPartyParams(), // openId可能在这里
                            "JSAPI" // 默认JSAPI
                    );
                    break;
                case 3: // 支付宝
                    result = createAlipayOrder(
                            form.getOrderNo() != null ? form.getOrderNo() : UUID.randomUUID().toString(),
                            form.getPaymentAmount(),
                            form.getConsumeDescription() != null ? form.getConsumeDescription() : "商品支付",
                            "APP" // 默认APP
                    );
                    break;
                case 4: // 银行卡
                    // 从扩展参数中解析银行卡号，如果没有则使用默认值
                    String bankCardNo = null;
                    if (form.getExtendedParams() != null && !form.getExtendedParams().isEmpty()) {
                        try {
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Object> params = mapper.readValue(form.getExtendedParams(),
                                new TypeReference<Map<String, Object>>() {});
                            bankCardNo = (String) params.get("bankCardNo");
                        } catch (Exception e) {
                            log.warn("[支付处理] 解析扩展参数失败", e);
                        }
                    }
                    result = createBankPaymentOrder(
                            form.getAccountId(),
                            form.getPaymentAmount(),
                            form.getOrderNo() != null ? form.getOrderNo() : UUID.randomUUID().toString(),
                            form.getConsumeDescription() != null ? form.getConsumeDescription() : "商品支付",
                            bankCardNo
                    );
                    break;
                case 8: // 信用额度
                    result = processCreditLimitPayment(
                            form.getAccountId(),
                            form.getPaymentAmount(),
                            form.getOrderNo() != null ? form.getOrderNo() : UUID.randomUUID().toString(),
                            form.getConsumeDescription() != null ? form.getConsumeDescription() : "信用额度支付"
                    );
                    break;
                default:
                    // 其他支付方式通过MultiPaymentManager处理
                    // 注意：MultiPaymentManager可能没有processPayment方法，需要调用其他方法
                    log.warn("[支付处理] 不支持的支付方式: {}", paymentMethod);
                    result.put("success", false);
                    result.put("message", "不支持的支付方式");
                    break;
            }

            return result;
        } catch (Exception e) {
            log.error("[支付处理] 处理支付失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "支付处理失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 申请退款
     *
     * @param form 退款申请表单
     * @return 申请结果
     */
    @Override
    public Map<String, Object> applyRefund(net.lab1024.sa.consume.consume.domain.form.RefundApplyForm form) {
        log.info("[退款申请] 申请退款，paymentId={}, userId={}, amount={}",
                form.getPaymentId(), form.getUserId(), form.getRefundAmount());
        try {
            // form参数由Spring框架保证非null，无需检查
            if (!StringUtils.hasText(form.getPaymentId())) {
                throw new BusinessException("支付记录ID不能为空");
            }
            if (form.getRefundAmount() == null) {
                throw new BusinessException("退款金额不能为空");
            }
            if (form.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("退款金额必须大于0");
            }

            // 幂等性：同一 paymentId 存在未完成退款时，直接返回已有申请
            java.util.List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> existedRefunds =
                    paymentRefundRecordDao.selectByPaymentId(form.getPaymentId());
            if (existedRefunds != null && !existedRefunds.isEmpty()) {
                net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity latest = existedRefunds.get(0);
                Integer refundStatus = latest.getRefundStatus();
                // 1-待审核 2-审核中 3-待处理 4-已拒绝 5-处理中 6-成功 7-失败（以 common 模型为准）
                if (refundStatus != null && refundStatus < 6) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("refundId", latest.getRefundId());
                    result.put("message", "退款申请已存在");
                    return result;
                }
            }

            // 校验原支付记录存在且满足退款条件
            PaymentRecordEntity paymentRecord = paymentRecordService.getPaymentRecord(form.getPaymentId());
            if (paymentRecord == null) {
                throw new BusinessException("支付记录不存在");
            }
            if (paymentRecord.getPaymentStatus() == null || paymentRecord.getPaymentStatus() != 3) {
                throw new BusinessException("当前支付状态不允许退款");
            }
            if (paymentRecord.getPaymentAmount() != null && form.getRefundAmount().compareTo(paymentRecord.getPaymentAmount()) > 0) {
                throw new BusinessException("退款金额不能超过原支付金额");
            }

            // 创建退款记录
            net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity refundRecord =
                    new net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity();
            refundRecord.setRefundId(UUID.randomUUID().toString());
            refundRecord.setPaymentId(form.getPaymentId());
            refundRecord.setUserId(form.getUserId());
            refundRecord.setRefundAmount(form.getRefundAmount());
            refundRecord.setRefundMethod(form.getRefundMethod());
            refundRecord.setRefundType(form.getRefundType());
            refundRecord.setRefundReasonType(form.getRefundReasonType());
            refundRecord.setRefundReasonDesc(form.getRefundReasonDesc());
            refundRecord.setRefundStatus(1); // 待审核
            refundRecord.setCreateTime(java.time.LocalDateTime.now());

            paymentRefundRecordDao.insert(refundRecord);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("refundId", refundRecord.getRefundId());
            result.put("message", "退款申请已提交，等待审核");

            return result;
        } catch (Exception e) {
            log.error("[退款申请] 申请退款失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "退款申请失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 审核退款
     *
     * @param refundId 退款记录ID
     * @param auditStatus 审核状态
     * @param auditComment 审核意见
     * @return 审核结果
     */
    @Override
    public Map<String, Object> auditRefund(String refundId, Integer auditStatus, String auditComment) {
        log.info("[退款审核] 审核退款，refundId={}, auditStatus={}", refundId, auditStatus);
        try {
            net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity refundRecord =
                    paymentRefundRecordDao.selectById(refundId);
            if (refundRecord == null) {
                throw new BusinessException("退款记录不存在");
            }

            refundRecord.setRefundStatus(auditStatus == 1 ? 3 : 4); // 1-通过->3待处理, 2-拒绝->4已拒绝
            refundRecord.setAuditComment(auditComment);
            refundRecord.setAuditTime(java.time.LocalDateTime.now());

            paymentRefundRecordDao.updateById(refundRecord);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", auditStatus == 1 ? "审核通过" : "审核拒绝");

            return result;
        } catch (Exception e) {
            log.error("[退款审核] 审核退款失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "审核退款失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 执行退款
     *
     * @param refundId 退款记录ID
     * @return 执行结果
     */
    @Override
    public Map<String, Object> executeRefund(String refundId) {
        log.info("[退款执行] 执行退款，refundId={}", refundId);
        try {
            net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity refundRecord =
                    paymentRefundRecordDao.selectById(refundId);
            if (refundRecord == null) {
                throw new BusinessException("退款记录不存在");
            }

            // 根据原支付方式执行退款
            PaymentRecordEntity paymentRecord = paymentRecordDao.selectById(refundRecord.getPaymentId());
            if (paymentRecord == null) {
                throw new BusinessException("原支付记录不存在");
            }

            Map<String, Object> result = new HashMap<>();
            Integer paymentMethod = paymentRecord.getPaymentMethod();

            if (paymentMethod != null && paymentMethod == 2) { // 2-微信支付
                result = wechatRefund(
                        paymentRecord.getPaymentId(),
                        refundId,
                        paymentRecord.getPaymentAmount().multiply(new BigDecimal("100")).intValue(),
                        refundRecord.getRefundAmount().multiply(new BigDecimal("100")).intValue()
                );
            } else if (paymentMethod != null && paymentMethod == 3) { // 3-支付宝
                result = alipayRefund(
                        paymentRecord.getPaymentId(),
                        refundRecord.getRefundAmount(),
                        refundRecord.getRefundReasonDesc()
                );
            } else {
                // 其他支付方式的退款处理
                result.put("success", true);
                result.put("message", "退款处理成功");
            }

            // 更新退款记录状态
            if (result.get("success") != null && (Boolean) result.get("success")) {
                refundRecord.setRefundStatus(6); // 退款成功
                refundRecord.setCompleteTime(java.time.LocalDateTime.now());
                paymentRefundRecordDao.updateById(refundRecord);
            }

            return result;
        } catch (Exception e) {
            log.error("[退款执行] 执行退款失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "执行退款失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 查询支付记录
     *
     * @param paymentId 支付记录ID
     * @return 支付记录
     */
    @Override
    public net.lab1024.sa.common.consume.entity.PaymentRecordEntity getPaymentRecord(String paymentId) {
        PaymentRecordEntity localEntity = paymentRecordDao.selectById(paymentId);
        if (localEntity == null) {
            return null;
        }
        // 直接返回，因为已经是正确的实体类型
        return localEntity;
    }

    /**
     * 查询用户支付记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 支付记录列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> getUserPaymentRecords(Long userId, Integer pageNum, Integer pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecordEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecordEntity::getUserId, userId);
        wrapper.orderByDesc(PaymentRecordEntity::getCreateTime);

        Page<PaymentRecordEntity> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20);
        IPage<PaymentRecordEntity> pageResult = paymentRecordDao.selectPage(page, wrapper);

        // 直接返回，因为已经是正确的实体类型
        return pageResult.getRecords();
    }

    /**
     * 查询退款记录
     *
     * @param refundId 退款记录ID
     * @return 退款记录
     */
    @Override
    public net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity getRefundRecord(String refundId) {
        return paymentRefundRecordDao.selectById(refundId);
    }

    /**
     * 查询用户退款记录
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 退款记录列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> getUserRefundRecords(
            Long userId, Integer pageNum, Integer pageSize) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getUserId, userId);
        wrapper.orderByDesc(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getCreateTime);

        Page<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> page =
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20);
        IPage<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> pageResult =
                paymentRefundRecordDao.selectPage(page, wrapper);
        return pageResult.getRecords();
    }

    /**
     * 游标分页查询用户支付记录（推荐用于深度分页）
     * <p>
     * 适用于需要查询大量数据的场景，性能优于传统分页
     * </p>
     *
     * @param userId 用户ID
     * @param pageSize 每页大小（默认20，最大100）
     * @param lastTime 上一页最后一条记录的创建时间（首次查询传null）
     * @return 游标分页结果
     */
    public CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> cursorPageUserPaymentRecords(
            Long userId, Integer pageSize, LocalDateTime lastTime) {
        log.debug("[支付服务] 游标分页查询用户支付记录，userId={}, pageSize={}, lastTime={}",
                userId, pageSize, lastTime);

        try {
            if (userId == null) {
                throw new BusinessException("用户ID不能为空");
            }

            // 1. 构建查询条件
            LambdaQueryWrapper<PaymentRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentRecordEntity::getUserId, userId);

            // 2. 使用游标分页（基于时间）
            // 使用新方法，传入SFunction参数
            // 注意：PaymentRecordEntity的paymentId是String类型，需要转换为Long
            CursorPagination.CursorPageResult<PaymentRecordEntity> entityResult =
                CursorPagination.queryByTimeCursor(
                    paymentRecordDao,
                    queryWrapper,
                    CursorPagination.CursorPageRequest.<PaymentRecordEntity>builder()
                        .pageSize(pageSize)
                        .lastTime(lastTime)
                        .desc(true)
                        .build(),
                    PaymentRecordEntity::getCreateTime,  // 获取创建时间的Lambda表达式
                    entity -> {
                        // 将String类型的paymentId转换为Long（用于游标分页）
                        // 如果paymentId是数字字符串，则转换；否则返回null
                        try {
                            String paymentId = entity.getPaymentId();
                            if (paymentId != null && paymentId.matches("\\d+")) {
                                return Long.parseLong(paymentId);
                            }
                        } catch (NumberFormatException e) {
                            // 忽略转换失败
                        }
                        return null;
                    }
                );

            // 3. 直接使用查询结果，因为已经是正确的实体类型
            // 4. 构建公共实体游标分页结果
            return CursorPagination.CursorPageResult.<net.lab1024.sa.common.consume.entity.PaymentRecordEntity>builder()
                    .list(entityResult.getList())
                    .hasNext(entityResult.getHasNext())
                    .lastId(entityResult.getLastId())
                    .lastTime(entityResult.getLastTime())
                    .size(entityResult.getList().size())
                    .build();

        } catch (Exception e) {
            log.error("[支付服务] 游标分页查询用户支付记录失败，userId={}", userId, e);
            throw new BusinessException("查询支付记录失败：" + e.getMessage());
        }
    }

    /**
     * 游标分页查询用户退款记录（推荐用于深度分页）
     * <p>
     * 适用于需要查询大量数据的场景，性能优于传统分页
     * </p>
     *
     * @param userId 用户ID
     * @param pageSize 每页大小（默认20，最大100）
     * @param lastTime 上一页最后一条记录的创建时间（首次查询传null）
     * @return 游标分页结果
     */
    public CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> cursorPageUserRefundRecords(
            Long userId, Integer pageSize, LocalDateTime lastTime) {
        log.debug("[支付服务] 游标分页查询用户退款记录，userId={}, pageSize={}, lastTime={}",
                userId, pageSize, lastTime);

        try {
            // 1. 构建查询条件
            LambdaQueryWrapper<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> queryWrapper =
                new LambdaQueryWrapper<>();
            queryWrapper.eq(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getUserId, userId);

            // 2. 使用游标分页（基于时间）
            // 使用新方法，传入SFunction参数
            // 注意：PaymentRefundRecordEntity的refundId是String类型，但getCreateTime来自BaseEntity
            CursorPagination.CursorPageResult<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> result =
                CursorPagination.queryByTimeCursor(
                    paymentRefundRecordDao,
                    queryWrapper,
                    CursorPagination.CursorPageRequest.<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity>builder()
                        .pageSize(pageSize)
                        .lastTime(lastTime)
                        .desc(true)
                        .build(),
                    net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getCreateTime,  // 获取创建时间的Lambda表达式
                    entity -> {  // 获取ID的Lambda表达式（refundId是String类型，转换为Long）
                        try {
                            String refundIdStr = entity.getRefundId();
                            return refundIdStr != null ? Long.parseLong(refundIdStr) : null;
                        } catch (NumberFormatException e) {
                            log.debug("[支付服务] 退款ID不是数字格式，返回null: refundId={}", entity.getRefundId());
                            return null;
                        }
                    }
                );

            log.info("[支付服务] 游标分页查询用户退款记录成功，size={}, hasNext={}",
                    result.getList().size(), result.getHasNext());

            return result;

        } catch (Exception e) {
            log.error("[支付服务] 游标分页查询用户退款记录失败，userId={}", userId, e);
            throw new BusinessException("查询退款记录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户支付统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Override
    public Map<String, Object> getUserPaymentStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecordEntity> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(PaymentRecordEntity::getUserId, userId);
            if (startTime != null) {
                wrapper.ge(PaymentRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(PaymentRecordEntity::getCreateTime, endTime);
            }

            List<PaymentRecordEntity> records = paymentRecordDao.selectList(wrapper);
            BigDecimal totalAmount = records.stream()
                    .filter(r -> r.getPaymentStatus() != null && r.getPaymentStatus() == 3) // 3-支付成功
                    .map(PaymentRecordEntity::getPaymentAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            statistics.put("totalCount", records.size());
            statistics.put("successCount", records.stream()
                    .filter(r -> r.getPaymentStatus() != null && r.getPaymentStatus() == 3).count());
            statistics.put("totalAmount", totalAmount);
            statistics.put("successAmount", totalAmount);

            return statistics;
        } catch (Exception e) {
            log.error("[支付统计] 获取用户支付统计失败", e);
            return statistics;
        }
    }

    /**
     * 获取用户退款统计
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Override
    public Map<String, Object> getUserRefundStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                    net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getUserId, userId);
            if (startTime != null) {
                wrapper.ge(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getCreateTime, endTime);
            }

            List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> records =
                    paymentRefundRecordDao.selectList(wrapper);
            BigDecimal totalAmount = records.stream()
                    .filter(r -> r.getRefundStatus() != null && r.getRefundStatus() == 6)
                    .map(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            statistics.put("totalCount", records.size());
            statistics.put("successCount", records.stream()
                    .filter(r -> r.getRefundStatus() != null && r.getRefundStatus() == 6).count());
            statistics.put("totalAmount", totalAmount);
            statistics.put("successAmount", totalAmount);

            return statistics;
        } catch (Exception e) {
            log.error("[退款统计] 获取用户退款统计失败", e);
            return statistics;
        }
    }

    /**
     * 执行对账
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param merchantId 商户ID（可选）
     * @return 对账结果
     */
    @Override
    public Map<String, Object> performReconciliation(LocalDateTime startTime, LocalDateTime endTime, Long merchantId) {
        log.info("[对账] 执行对账，startTime={}, endTime={}, merchantId={}", startTime, endTime, merchantId);
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 参数验证
            if (startTime == null || endTime == null) {
                throw new BusinessException("对账时间不能为空");
            }
            if (startTime.isAfter(endTime)) {
                throw new BusinessException("开始时间不能晚于结束时间");
            }

            // 2. 查询支付记录（只查询支付成功的记录）
            LambdaQueryWrapper<PaymentRecordEntity> paymentWrapper =
                    new LambdaQueryWrapper<>();
            paymentWrapper.ge(PaymentRecordEntity::getCreateTime, startTime)
                    .le(PaymentRecordEntity::getCreateTime, endTime)
                    .eq(PaymentRecordEntity::getPaymentStatus, 3) // 3-支付成功
                    .eq(PaymentRecordEntity::getDeletedFlag, 0);
            paymentWrapper.orderByDesc(PaymentRecordEntity::getCreateTime);
            List<PaymentRecordEntity> paymentRecords =
                    paymentRecordDao.selectList(paymentWrapper);
            log.info("[对账] 查询到支付记录数：{}", paymentRecords.size());

            // 3. 查询消费记录
            List<net.lab1024.sa.common.consume.entity.ConsumeRecordEntity> consumeRecords =
                    consumeRecordDao.selectByTimeRange(startTime, endTime);
            log.info("[对账] 查询到消费记录数：{}", consumeRecords.size());

            // 4. 构建对账结果
            Map<String, Object> reconciliationResult = buildReconciliationResult(
                    paymentRecords, consumeRecords, merchantId);

            // 5. 组装返回结果
            result.put("success", true);
            result.put("message", "对账完成");
            result.put("startTime", startTime);
            result.put("endTime", endTime);
            result.put("merchantId", merchantId);
            result.put("paymentRecordCount", paymentRecords.size());
            result.put("consumeRecordCount", consumeRecords.size());
            result.put("reconciliationResult", reconciliationResult);

            log.info("[对账] 对账完成，支付记录数={}，消费记录数={}，匹配数={}，差异数={}",
                    paymentRecords.size(),
                    consumeRecords.size(),
                    reconciliationResult.get("matchedCount"),
                    reconciliationResult.get("differenceCount"));

            return result;
        } catch (BusinessException e) {
            log.error("[对账] 对账业务异常", e);
            result.put("success", false);
            result.put("message", "对账失败: " + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[对账] 执行对账失败", e);
            result.put("success", false);
            result.put("message", "对账失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 构建对账结果
     *
     * @param paymentRecords 支付记录列表
     * @param consumeRecords 消费记录列表
     * @param merchantId 商户ID（可选）
     * @return 对账结果
     */
    private Map<String, Object> buildReconciliationResult(
            List<PaymentRecordEntity> paymentRecords,
            List<net.lab1024.sa.common.consume.entity.ConsumeRecordEntity> consumeRecords,
            Long merchantId) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> matchedRecords = new ArrayList<>();
        List<Map<String, Object>> differenceRecords = new ArrayList<>();
        List<Map<String, Object>> missingPaymentRecords = new ArrayList<>();
        List<Map<String, Object>> missingConsumeRecords = new ArrayList<>();

        // 构建支付记录索引（按交易ID或支付ID）
        Map<String, PaymentRecordEntity> paymentByTransactionMap = new HashMap<>();
        Map<String, PaymentRecordEntity> paymentByPaymentIdMap = new HashMap<>();
        for (PaymentRecordEntity payment : paymentRecords) {
            if (payment.getTransactionNo() != null) {
                paymentByTransactionMap.put(payment.getTransactionNo(), payment);
            }
            if (payment.getPaymentId() != null) {
                paymentByPaymentIdMap.put(payment.getPaymentId(), payment);
            }
        }

        // 构建消费记录索引（按交易流水号或订单号）
        Map<String, net.lab1024.sa.common.consume.entity.ConsumeRecordEntity> consumeByTransactionMap = new HashMap<>();
        Map<String, net.lab1024.sa.common.consume.entity.ConsumeRecordEntity> consumeByOrderMap = new HashMap<>();
        for (net.lab1024.sa.common.consume.entity.ConsumeRecordEntity consume : consumeRecords) {
            if (consume.getTransactionNo() != null) {
                consumeByTransactionMap.put(consume.getTransactionNo(), consume);
            }
            if (consume.getOrderNo() != null) {
                consumeByOrderMap.put(consume.getOrderNo(), consume);
            }
        }

        // 对比支付记录和消费记录
        for (PaymentRecordEntity payment : paymentRecords) {
            net.lab1024.sa.common.consume.entity.ConsumeRecordEntity consume = null;
            String matchKey = null;

            // 优先使用交易ID匹配
            if (payment.getTransactionNo() != null) {
                consume = consumeByTransactionMap.get(payment.getTransactionNo());
                matchKey = payment.getTransactionNo();
            }
            // 如果交易ID匹配失败，尝试使用支付ID匹配订单号
            if (consume == null && payment.getPaymentId() != null) {
                consume = consumeByOrderMap.get(payment.getPaymentId());
                matchKey = payment.getPaymentId();
            }

            if (consume == null) {
                // 支付记录存在，但消费记录不存在
                Map<String, Object> diff = new HashMap<>();
                diff.put("type", "MISSING_CONSUME");
                diff.put("matchKey", matchKey);
                diff.put("paymentId", payment.getPaymentId());
                diff.put("transactionId", payment.getTransactionNo());
                diff.put("paymentAmount", payment.getPaymentAmount());
                diff.put("paymentTime", payment.getPaymentTime());
                missingConsumeRecords.add(diff);
                continue;
            }

            // 对比金额和状态
            boolean isMatched = comparePaymentAndConsume(payment, consume);
            if (isMatched) {
                // 匹配成功
                Map<String, Object> matched = new HashMap<>();
                matched.put("matchKey", matchKey);
                matched.put("paymentId", payment.getPaymentId());
                matched.put("transactionId", payment.getTransactionNo());
                matched.put("consumeId", consume.getId());
                matched.put("orderNo", consume.getOrderNo());
                matched.put("amount", payment.getPaymentAmount());
                matched.put("paymentTime", payment.getPaymentTime());
                matched.put("consumeTime", consume.getConsumeTime());
                matchedRecords.add(matched);
            } else {
                // 存在差异
                Map<String, Object> diff = new HashMap<>();
                diff.put("type", "AMOUNT_OR_STATUS_MISMATCH");
                diff.put("matchKey", matchKey);
                diff.put("paymentId", payment.getPaymentId());
                diff.put("transactionId", payment.getTransactionNo());
                diff.put("consumeId", consume.getId());
                diff.put("orderNo", consume.getOrderNo());
                diff.put("paymentAmount", payment.getPaymentAmount());
                diff.put("consumeAmount", consume.getAmount());
                diff.put("paymentStatus", payment.getPaymentStatus());
                diff.put("consumeStatus", consume.getStatus());
                diff.put("paymentTime", payment.getPaymentTime());
                diff.put("consumeTime", consume.getConsumeTime());
                differenceRecords.add(diff);
            }
        }

        // 检查消费记录中是否有未匹配的支付记录
        for (net.lab1024.sa.common.consume.entity.ConsumeRecordEntity consume : consumeRecords) {
            boolean found = false;
            if (consume.getTransactionNo() != null && paymentByTransactionMap.containsKey(consume.getTransactionNo())) {
                found = true;
            }
            if (!found && consume.getOrderNo() != null && paymentByPaymentIdMap.containsKey(consume.getOrderNo())) {
                found = true;
            }
            if (!found) {
                // 消费记录存在，但支付记录不存在
                Map<String, Object> diff = new HashMap<>();
                diff.put("type", "MISSING_PAYMENT");
                diff.put("orderNo", consume.getOrderNo());
                diff.put("transactionNo", consume.getTransactionNo());
                diff.put("consumeId", consume.getId());
                diff.put("consumeAmount", consume.getAmount());
                diff.put("consumeTime", consume.getConsumeTime());
                missingPaymentRecords.add(diff);
            }
        }

        // 汇总统计
        result.put("matchedCount", matchedRecords.size());
        result.put("differenceCount", differenceRecords.size() + missingPaymentRecords.size() + missingConsumeRecords.size());
        result.put("missingPaymentCount", missingPaymentRecords.size());
        result.put("missingConsumeCount", missingConsumeRecords.size());
        result.put("matchedRecords", matchedRecords);
        result.put("differenceRecords", differenceRecords);
        result.put("missingPaymentRecords", missingPaymentRecords);
        result.put("missingConsumeRecords", missingConsumeRecords);

        return result;
    }

    /**
     * 对比支付记录和消费记录
     *
     * @param payment 支付记录
     * @param consume 消费记录
     * @return 是否匹配
     */
    /**
     * 对比支付记录和消费记录
     *
     * @param payment 支付记录
     * @param consume 消费记录
     * @return 是否匹配
     */
    private boolean comparePaymentAndConsume(
            PaymentRecordEntity payment,
            net.lab1024.sa.common.consume.entity.ConsumeRecordEntity consume) {

        // 对比金额（允许0.01的误差）
        BigDecimal paymentAmount = payment.getPaymentAmount() != null ? payment.getPaymentAmount() : BigDecimal.ZERO;
        BigDecimal consumeAmount = consume.getAmount() != null ? consume.getAmount() : BigDecimal.ZERO;
        BigDecimal difference = paymentAmount.subtract(consumeAmount).abs();
        if (difference.compareTo(new BigDecimal("0.01")) > 0) {
            log.warn("[对账] 金额不一致，paymentId={}, transactionId={}, paymentAmount={}, consumeAmount={}",
                    payment.getPaymentId(), payment.getTransactionNo(), paymentAmount, consumeAmount);
            return false;
        }

        // 对比状态：支付成功(3)对应消费成功状态
        if (payment.getPaymentStatus() != null && payment.getPaymentStatus() == 3) {
            // 支付成功，消费记录状态应该是成功
            String consumeStatus = consume.getStatus();
            if (consumeStatus == null || (!"SUCCESS".equals(consumeStatus) && !"COMPLETED".equals(consumeStatus))) {
                String paymentStatusStr = convertPaymentStatusToString(payment.getPaymentStatus());
                log.warn("[对账] 状态不一致，paymentId={}, transactionId={}, paymentStatus={}, consumeStatus={}",
                        payment.getPaymentId(), payment.getTransactionNo(), paymentStatusStr, consumeStatus);
                return false;
            }
        }

        return true;
    }

    /**
     * 获取商户结算统计
     *
     * @param merchantId 商户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 结算统计
     */
    @Override
    public Map<String, Object> getMerchantSettlementStatistics(
            Long merchantId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[结算统计] 获取商户结算统计，merchantId={}, startTime={}, endTime={}", merchantId, startTime, endTime);
        Map<String, Object> statistics = new HashMap<>();
        try {
            // 1. 参数验证
            if (merchantId == null) {
                throw new BusinessException("商户ID不能为空");
            }
            if (startTime == null || endTime == null) {
                throw new BusinessException("时间范围不能为空");
            }
            if (startTime.isAfter(endTime)) {
                throw new BusinessException("开始时间不能晚于结束时间");
            }

            // 2. 通过网关获取商户信息
            String merchantName = getMerchantNameById(merchantId);
            if (merchantName == null) {
                log.warn("[结算统计] 商户不存在，merchantId={}", merchantId);
                statistics.put("totalAmount", BigDecimal.ZERO);
                statistics.put("settlementAmount", BigDecimal.ZERO);
                statistics.put("transactionCount", 0);
                statistics.put("merchantId", merchantId);
                statistics.put("merchantName", null);
                return statistics;
            }

            // 3. 查询该商户的消费记录
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<net.lab1024.sa.common.consume.entity.ConsumeRecordEntity> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(net.lab1024.sa.common.consume.entity.ConsumeRecordEntity::getMerchantName, merchantName)
                    .ge(net.lab1024.sa.common.consume.entity.ConsumeRecordEntity::getCreateTime, startTime)
                    .le(net.lab1024.sa.common.consume.entity.ConsumeRecordEntity::getCreateTime, endTime)
                    .eq(net.lab1024.sa.common.consume.entity.ConsumeRecordEntity::getDeletedFlag, 0)
                    .in(net.lab1024.sa.common.consume.entity.ConsumeRecordEntity::getStatus,
                            "SUCCESS", "COMPLETED"); // 只统计成功的消费记录

            List<net.lab1024.sa.common.consume.entity.ConsumeRecordEntity> consumeRecords =
                    consumeRecordDao.selectList(wrapper);
            log.info("[结算统计] 查询到消费记录数：{}", consumeRecords.size());

            // 4. 计算统计信息
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalFeeAmount = BigDecimal.ZERO;
            int transactionCount = consumeRecords.size();

            for (net.lab1024.sa.common.consume.entity.ConsumeRecordEntity record : consumeRecords) {
                // 累计总金额（实际支付金额）
                BigDecimal amount = record.getActualAmount() != null ? record.getActualAmount() : record.getAmount();
                if (amount != null) {
                    totalAmount = totalAmount.add(amount);
                }

                // 累计手续费（如果有）
                BigDecimal feeAmount = record.getFeeAmount();
                if (feeAmount != null && feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                    totalFeeAmount = totalFeeAmount.add(feeAmount);
                }
            }

            // 5. 计算结算金额（总金额 - 手续费）
            BigDecimal settlementAmount = totalAmount.subtract(totalFeeAmount);
            if (settlementAmount.compareTo(BigDecimal.ZERO) < 0) {
                settlementAmount = BigDecimal.ZERO;
            }

            // 6. 组装返回结果
            statistics.put("merchantId", merchantId);
            statistics.put("merchantName", merchantName);
            statistics.put("startTime", startTime);
            statistics.put("endTime", endTime);
            statistics.put("totalAmount", totalAmount);
            statistics.put("totalFeeAmount", totalFeeAmount);
            statistics.put("settlementAmount", settlementAmount);
            statistics.put("transactionCount", transactionCount);
            statistics.put("averageAmount", transactionCount > 0 ?
                    totalAmount.divide(new BigDecimal(transactionCount), 2, java.math.RoundingMode.HALF_UP) :
                    BigDecimal.ZERO);

            log.info("[结算统计] 统计完成，merchantId={}, totalAmount={}, settlementAmount={}, transactionCount={}",
                    merchantId, totalAmount, settlementAmount, transactionCount);

            return statistics;
        } catch (BusinessException e) {
            log.error("[结算统计] 获取商户结算统计业务异常", e);
            statistics.put("error", e.getMessage());
            return statistics;
        } catch (Exception e) {
            log.error("[结算统计] 获取商户结算统计失败", e);
            statistics.put("error", "获取商户结算统计失败: " + e.getMessage());
            return statistics;
        }
    }

    /**
     * 通过商户ID获取商户名称
     *
     * @param merchantId 商户ID
     * @return 商户名称
     */
    private String getMerchantNameById(Long merchantId) {
        try {
            // 通过网关调用公共服务获取商户信息
            net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response =
                    gatewayServiceClient.callCommonService(
                            "/api/v1/merchant/" + merchantId,
                            HttpMethod.GET,
                            null,
                            new com.fasterxml.jackson.core.type.TypeReference<net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>>>() {
                            });

            if (response != null && response.getData() != null) {
                Object merchantNameObj = response.getData().get("merchantName");
                if (merchantNameObj != null) {
                    return merchantNameObj.toString();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[结算统计] 获取商户信息失败，merchantId={}, error={}", merchantId, e.getMessage());
            return null;
        }
    }

    /**
     * 查询待审核退款列表
     *
     * @return 待审核退款列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> getPendingAuditRefunds() {
        return paymentRefundRecordDao.selectPendingAudit();
    }

    /**
     * 查询待处理退款列表
     *
     * @return 待处理退款列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> getPendingProcessRefunds() {
        return paymentRefundRecordDao.selectPendingProcess();
    }

    /**
     * 查询高风险支付记录
     *
     * @param hours 小时数
     * @return 高风险支付记录列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> getHighRiskPayments(Integer hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours != null ? hours : 24);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecordEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.ge(PaymentRecordEntity::getCreateTime, startTime);
        // 高风险支付：金额大于1000元或状态为失败
        wrapper.and(w -> w.gt(PaymentRecordEntity::getPaymentAmount, BigDecimal.valueOf(1000))
                .or().eq(PaymentRecordEntity::getPaymentStatus, 4)); // 4-支付失败

        List<PaymentRecordEntity> localList = paymentRecordDao.selectList(wrapper);
        // 直接返回，因为已经是正确的实体类型
        return localList;
    }

    /**
     * 查询高风险退款记录
     *
     * @param hours 小时数
     * @return 高风险退款记录列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> getHighRiskRefunds(Integer hours) {
        log.info("[高风险退款] 查询高风险退款记录，hours={}", hours);
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours != null ? hours : 24);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<
                net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.ge(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getCreateTime, startTime)
                // 风险等级：3-高风险 4-极高风险
                .ge(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getRiskLevel, 3)
                .eq(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getDeletedFlag, 0)
                .orderByDesc(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getRiskLevel)
                .orderByDesc(net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity::getCreateTime);

        List<net.lab1024.sa.common.consume.entity.PaymentRefundRecordEntity> result =
                paymentRefundRecordDao.selectList(wrapper);
        log.info("[高风险退款] 查询到高风险退款记录数：{}", result.size());
        return result;
    }

    /**
     * 查询异常支付记录
     *
     * @param hours 小时数
     * @return 异常支付记录列表
     */
    @Override
    public List<net.lab1024.sa.common.consume.entity.PaymentRecordEntity> getAbnormalPayments(Integer hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours != null ? hours : 24);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecordEntity> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.ge(PaymentRecordEntity::getCreateTime, startTime);
        // 异常支付：状态为失败、已退款
        wrapper.in(PaymentRecordEntity::getPaymentStatus, 4, 5); // 4-支付失败, 5-已退款

        List<PaymentRecordEntity> localList = paymentRecordDao.selectList(wrapper);
        // 直接返回，因为已经是正确的实体类型
        return localList;
    }

    /**
     * 转换支付方式：String -> Integer
     *
     * @param paymentMethod 支付方式字符串
     * @return 支付方式整数
     */
    private Integer convertPaymentMethodToInt(String paymentMethod) {
        if (paymentMethod == null) {
            return 1; // 默认余额支付
        }
        switch (paymentMethod.toUpperCase()) {
            case "WECHAT":
                return 2; // 微信支付
            case "ALIPAY":
                return 3; // 支付宝
            default:
                return 1; // 默认余额支付
        }
    }


    /**
     * 转换支付状态：Integer -> String
     *
     * @param status 支付状态整数
     * @return 支付状态字符串
     */
    private String convertPaymentStatusToString(Integer status) {
        if (status == null) {
            return "PENDING";
        }
        switch (status) {
            case 1:
                return "PENDING"; // 待支付
            case 2:
                return "PROCESSING"; // 支付中
            case 3:
                return "SUCCESS"; // 支付成功
            case 4:
                return "FAILED"; // 支付失败
            case 5:
                return "REFUNDED"; // 已退款
            case 6:
                return "PARTIAL_REFUNDED"; // 部分退款
            case 7:
                return "CANCELLED"; // 已取消
            default:
                return "PENDING";
        }
    }
}



