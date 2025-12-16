package net.lab1024.sa.consume.service.payment.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.service.payment.WechatPayService;
import net.lab1024.sa.consume.service.payment.adapter.WechatPayAdapter;

/**
 * 微信支付服务实现类
 * <p>
 * 专门处理微信支付相关业务，从PaymentService拆分而来
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
public class WechatPayServiceImpl implements WechatPayService {

    @Resource
    private WechatPayAdapter wechatPayAdapter;

    @Override
    public Map<String, Object> createWechatPayOrder(
            String orderId,
            BigDecimal amount,
            String description,
            String openId,
            String payType) {
        log.info("[微信支付] 创建支付订单，orderId={}, amount={}, payType={}", orderId, amount, payType);
        return wechatPayAdapter.createWechatPayOrder(orderId, amount, description, openId, payType);
    }

    @Override
    public Map<String, Object> createJsapiPayOrder(String orderId, BigDecimal amount,
            String description, String openId) {
        log.info("[微信支付] 创建JSAPI支付订单，orderId={}, amount={}", orderId, amount);
        return wechatPayAdapter.createWechatPayOrder(orderId, amount, description, openId, "JSAPI");
    }

    @Override
    public Map<String, Object> createNativePayOrder(String orderId, BigDecimal amount, String description) {
        log.info("[微信支付] 创建Native支付订单，orderId={}, amount={}", orderId, amount);
        return wechatPayAdapter.createWechatPayOrder(orderId, amount, description, null, "Native");
    }

    @Override
    public Map<String, Object> createAppPayOrder(String orderId, BigDecimal amount, String description) {
        log.info("[微信支付] 创建APP支付订单，orderId={}, amount={}", orderId, amount);
        return wechatPayAdapter.createWechatPayOrder(orderId, amount, description, null, "APP");
    }

    @Override
    public Map<String, Object> createH5PayOrder(String orderId, BigDecimal amount, String description) {
        log.info("[微信支付] 创建H5支付订单，orderId={}, amount={}", orderId, amount);
        return wechatPayAdapter.createWechatPayOrder(orderId, amount, description, null, "H5");
    }

    @Override
    public Map<String, Object> wechatRefund(
            String orderId,
            String refundId,
            Integer totalAmount,
            Integer refundAmount) {
        log.info("[微信支付] 申请退款，orderId={}, refundId={}, refundAmount={}", orderId, refundId, refundAmount);
        return wechatPayAdapter.wechatRefund(orderId, refundId, totalAmount, refundAmount);
    }

    @Override
    public Map<String, Object> queryOrderStatus(String orderId) {
        log.info("[微信支付] 查询订单状态，orderId={}", orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("status", "QUERY_NOT_IMPLEMENTED");
        result.put("message", "订单查询功能待实现");
        return result;
    }
}
