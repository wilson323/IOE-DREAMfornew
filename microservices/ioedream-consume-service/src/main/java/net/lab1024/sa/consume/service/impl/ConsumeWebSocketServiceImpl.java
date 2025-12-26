package net.lab1024.sa.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.consume.service.ConsumeWebSocketService;
import net.lab1024.sa.consume.websocket.ConsumeWebSocketHandler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费WebSocket推送服务实现
 * <p>
 * 封装WebSocket实时消息推送功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
@Slf4j
@Service
public class ConsumeWebSocketServiceImpl implements ConsumeWebSocketService {

    private final ConsumeWebSocketHandler webSocketHandler;

    public ConsumeWebSocketServiceImpl(ConsumeWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void pushConsumeSuccess(Long userId, BigDecimal amount, String merchantName, BigDecimal balance) {
        log.info("[WebSocket] 推送消费成功通知: userId={}, amount={}, balance={}",
                userId, amount, balance);

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("type", "consume_success");
        consumeData.put("amount", amount);
        consumeData.put("merchantName", merchantName);
        consumeData.put("balance", balance);
        consumeData.put("message", String.format("您在%s消费%.2f元，余额%.2f元",
                merchantName, amount, balance));

        webSocketHandler.sendConsumeNotification(userId, consumeData);
    }

    @Override
    public void pushRechargeSuccess(Long userId, BigDecimal amount, BigDecimal balance, BigDecimal bonus) {
        log.info("[WebSocket] 推送充值成功通知: userId={}, amount={}, bonus={}, balance={}",
                userId, amount, bonus, balance);

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("type", "recharge_success");
        consumeData.put("amount", amount);
        consumeData.put("bonus", bonus);
        consumeData.put("balance", balance);
        consumeData.put("message", String.format("充值成功%.2f元，赠送%.2f元，余额%.2f元",
                amount, bonus, balance));

        webSocketHandler.sendConsumeNotification(userId, consumeData);
    }

    @Override
    public void pushRefundSuccess(Long userId, BigDecimal amount, String reason, BigDecimal balance) {
        log.info("[WebSocket] 推送退款成功通知: userId={}, amount={}, reason={}",
                userId, amount, reason);

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("type", "refund_success");
        consumeData.put("amount", amount);
        consumeData.put("reason", reason);
        consumeData.put("balance", balance);
        consumeData.put("message", String.format("退款成功%.2f元，原因：%s，余额%.2f元",
                amount, reason, balance));

        webSocketHandler.sendConsumeNotification(userId, consumeData);
    }

    @Override
    public void pushBalanceWarning(Long userId, BigDecimal balance, BigDecimal threshold) {
        log.warn("[WebSocket] 推送余额预警: userId={}, balance={}, threshold={}",
                userId, balance, threshold);

        Map<String, Object> consumeData = new HashMap<>();
        consumeData.put("type", "balance_warning");
        consumeData.put("balance", balance);
        consumeData.put("threshold", threshold);
        consumeData.put("message", String.format("您的余额不足%.2f元，当前余额%.2f元，请及时充值",
                threshold, balance));
        consumeData.put("priority", "high");

        webSocketHandler.sendConsumeNotification(userId, consumeData);
    }

    @Override
    public void pushBalanceUpdate(Long userId, Map<String, Object> balanceData) {
        log.info("[WebSocket] 推送余额更新: userId={}", userId);

        webSocketHandler.sendBalanceUpdate(userId, balanceData);
    }

    @Override
    public void broadcastSystemNotification(String title, String content) {
        log.info("[WebSocket] 广播系统公告: title={}", title);

        webSocketHandler.broadcastSystemNotification(title, content);
    }

    @Override
    public boolean isUserOnline(Long userId) {
        return webSocketHandler.isUserOnline(userId);
    }

    @Override
    public int getOnlineUserCount() {
        return webSocketHandler.getOnlineUserCount();
    }
}
