package net.lab1024.sa.consume.service.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.service.payment.AlipayPayService;
import net.lab1024.sa.consume.service.payment.adapter.AlipayPayAdapter;

/**
 * 支付宝支付服务实现类
 * <p>
 * 专门处理支付宝支付相关业务，从PaymentService拆分而来
 * 严格遵循CLAUDE.md规范：
 * - Service实现类使用@Service注解
 * - 依赖注入使用@Resource注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
public class AlipayPayServiceImpl implements AlipayPayService {

    @Resource
    private AlipayPayAdapter alipayPayAdapter;

    @Override
    public Map<String, Object> createAlipayOrder(
            String orderId,
            BigDecimal amount,
            String subject,
            String payType) {
        log.info("[支付宝] 创建支付订单，orderId={}, amount={}, payType={}", orderId, amount, payType);
        return alipayPayAdapter.createAlipayOrder(orderId, amount, subject, payType);
    }

    @Override
    public Map<String, Object> createAppPayOrder(String orderId, BigDecimal amount, String subject) {
        log.info("[支付宝] 创建APP支付订单，orderId={}, amount={}", orderId, amount);
        return alipayPayAdapter.createAlipayOrder(orderId, amount, subject, "APP");
    }

    @Override
    public Map<String, Object> createWebPayOrder(String orderId, BigDecimal amount, String subject) {
        log.info("[支付宝] 创建Web支付订单，orderId={}, amount={}", orderId, amount);
        return alipayPayAdapter.createAlipayOrder(orderId, amount, subject, "Web");
    }

    @Override
    public Map<String, Object> createWapPayOrder(String orderId, BigDecimal amount, String subject) {
        log.info("[支付宝] 创建Wap支付订单，orderId={}, amount={}", orderId, amount);
        return alipayPayAdapter.createAlipayOrder(orderId, amount, subject, "Wap");
    }

    @Override
    public Map<String, Object> alipayRefund(
            String orderId,
            BigDecimal refundAmount,
            String reason) {
        log.info("[支付宝] 申请退款，orderId={}, refundAmount={}", orderId, refundAmount);
        return alipayPayAdapter.alipayRefund(orderId, refundAmount, reason);
    }

    @Override
    public Map<String, Object> queryOrderStatus(String orderId) {
        log.info("[支付宝] 查询订单状态，orderId={}", orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "QUERY_NOT_IMPLEMENTED");
        result.put("message", "订单查询功能待实现");
        return result;
    }
}
