package net.lab1024.sa.consume.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 消费WebSocket推送服务接口
 * <p>
 * 提供实时消息推送功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-24
 */
public interface ConsumeWebSocketService {

    /**
     * 推送消费成功通知
     *
     * @param userId 用户ID
     * @param amount 消费金额
     * @param merchantName 商户名称
     * @param balance 剩余余额
     */
    void pushConsumeSuccess(Long userId, BigDecimal amount, String merchantName, BigDecimal balance);

    /**
     * 推送充值成功通知
     *
     * @param userId 用户ID
     * @param amount 充值金额
     * @param balance 充值后余额
     * @param bonus 赠送金额
     */
    void pushRechargeSuccess(Long userId, BigDecimal amount, BigDecimal balance, BigDecimal bonus);

    /**
     * 推送退款成功通知
     *
     * @param userId 用户ID
     * @param amount 退款金额
     * @param reason 退款原因
     * @param balance 退款后余额
     */
    void pushRefundSuccess(Long userId, BigDecimal amount, String reason, BigDecimal balance);

    /**
     * 推送余额预警
     *
     * @param userId 用户ID
     * @param balance 当前余额
     * @param threshold 预警阈值
     */
    void pushBalanceWarning(Long userId, BigDecimal balance, BigDecimal threshold);

    /**
     * 推送账户变动通知
     *
     * @param userId 用户ID
     * @param balanceData 账户数据
     */
    void pushBalanceUpdate(Long userId, Map<String, Object> balanceData);

    /**
     * 广播系统公告
     *
     * @param title 公告标题
     * @param content 公告内容
     */
    void broadcastSystemNotification(String title, String content);

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    boolean isUserOnline(Long userId);

    /**
     * 获取在线用户数
     *
     * @return 在线用户数
     */
    int getOnlineUserCount();
}
