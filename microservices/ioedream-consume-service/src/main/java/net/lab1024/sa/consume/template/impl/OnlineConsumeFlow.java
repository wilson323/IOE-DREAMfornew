package net.lab1024.sa.consume.template.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.form.PaymentProcessForm;
import net.lab1024.sa.consume.entity.AccountEntity;
import net.lab1024.sa.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.template.AbstractConsumeFlowTemplate;

/**
 * 在线消费流程实现
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 实现中心实时验证模式的在线消费流程
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Slf4j
@Component("onlineConsumeFlow")
public class OnlineConsumeFlow extends AbstractConsumeFlowTemplate {

    @Resource
    private AccountDao accountDao;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    protected AccountEntity validateAccount(PaymentProcessForm form) {
        // 查询账户信息
        AccountEntity account = accountDao.selectById(form.getAccountId());
        if (account == null) {
            log.warn("[在线消费流程] 账户不存在, accountId={}", form.getAccountId());
            return null;
        }

        if (account.getStatus() == null || account.getStatus() != 1) {
            log.warn("[在线消费流程] 账户状态异常, accountId={}, status={}", form.getAccountId(), account.getStatus());
            return null;
        }

        log.debug("[在线消费流程] 账户验证通过, accountId={}, balance={}", form.getAccountId(), account.getBalance());
        return account;
    }

    @Override
    protected boolean checkBalance(AccountEntity account, PaymentProcessForm form) {
        BigDecimal balance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal amount = form.getPaymentAmount();

        boolean sufficient = balance.compareTo(amount) >= 0;
        log.debug("[在线消费流程] 余额检查, accountId={}, balance={}, amount={}, sufficient={}",
                account.getAccountId(), balance, amount, sufficient);
        return sufficient;
    }

    @Override
    protected ConsumeRecordEntity executePayment(
            AccountEntity account, PaymentProcessForm form, DeviceEntity device) {

        // 执行扣款（使用乐观锁）
        int updated = accountDao.deductBalance(
                account.getAccountId(),
                form.getPaymentAmount(),
                account.getVersion());

        if (updated == 0) {
            log.warn("[在线消费流程] 账户余额更新失败（并发冲突）, accountId={}", account.getAccountId());
            throw new RuntimeException("账户余额更新失败,请重试");
        }

        // 创建消费记录
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setUserId(form.getUserId());
        record.setAccountId(form.getAccountId());
        record.setAmount(form.getPaymentAmount());
        record.setConsumeTime(LocalDateTime.now());

        // 修复类型转换：deviceId从String转换为Long
        // DeviceEntity.getDeviceId()返回String，ConsumeRecordEntity.deviceId是Long类型
        // 根据业务模块文档，deviceId应该是String类型，但ConsumeRecordEntity使用Long
        // 这里进行类型转换：如果deviceId是纯数字字符串，则转换为Long；否则使用null
        String deviceIdStr = device != null ? device.getDeviceId() : form.getDeviceId();
        if (deviceIdStr != null && !deviceIdStr.trim().isEmpty()) {
            try {
                Long deviceIdLong = Long.parseLong(deviceIdStr);
                record.setDeviceId(deviceIdLong);
            } catch (NumberFormatException e) {
                // 如果deviceId不是纯数字，记录警告但继续处理
                log.warn("[在线消费流程] 设备ID格式错误，无法转换为Long类型，deviceId={}", deviceIdStr);
                record.setDeviceId(null);
            }
        } else {
            record.setDeviceId(null);
        }

        record.setPaymentStatus(1); // 已支付
        record.setPaymentMethod(form.getPaymentMethod());

        consumeRecordDao.insert(record);

        log.debug("[在线消费流程] 扣款成功, accountId={}, amount={}, recordId={}",
                account.getAccountId(), form.getPaymentAmount(), record.getRecordId());

        return record;
    }

    @Override
    protected void notifyConsumeEvent(AccountEntity account, ConsumeRecordEntity record, PaymentProcessForm form) {
        // 在线消费流程的事件通知逻辑
        log.info("[在线消费流程] 消费事件通知: userId={}, accountId={}, amount={}, recordId={}",
                form.getUserId(), form.getAccountId(), form.getPaymentAmount(), record.getRecordId());

        // 根据chonggou.txt和业务模块文档要求实现通知功能
        // 注意：这里使用异步方式发送通知，不影响主流程性能
        try {
            if (form.getUserId() == null) {
                log.warn("[在线消费流程] 用户ID为空，跳过通知发送, recordId={}", record.getRecordId());
                return;
            }

            // 1. WebSocket实时推送（如果用户在线）
            sendWebSocketNotification(form, record, account);

            // 2. RabbitMQ消息队列（异步处理）
            sendRabbitMQMessage(form, record, account);

            // 3. 站内信通知
            sendStationMessage(form, record, account);

            log.debug("[在线消费流程] 消费事件通知处理完成, recordId={}", record.getRecordId());
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            log.warn("[在线消费流程] 消费事件通知发送失败, recordId={}, error={}", record.getRecordId(), e.getMessage());
        }
    }

    /**
     * 发送WebSocket实时推送
     * <p>
     * 通过GatewayServiceClient调用通知服务推送WebSocket消息
     * 如果用户在线，可以实时收到消费通知
     * </p>
     *
     * @param form    支付处理表单
     * @param record  消费记录
     * @param account 账户信息
     */
    private void sendWebSocketNotification(PaymentProcessForm form, ConsumeRecordEntity record, AccountEntity account) {
        try {
            if (gatewayServiceClient == null) {
                log.debug("[在线消费流程] GatewayServiceClient未注入，跳过WebSocket推送");
                return;
            }

            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "CONSUME_SUCCESS");
            wsMessage.put("userId", form.getUserId());
            wsMessage.put("message", String.format("消费成功，金额：%.2f 元，余额：%.2f 元",
                    form.getPaymentAmount() != null ? form.getPaymentAmount() : BigDecimal.ZERO,
                    account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO));
            wsMessage.put("data", Map.of(
                    "recordId", record.getRecordId() != null ? record.getRecordId() : record.getId(),
                    "amount", form.getPaymentAmount() != null ? form.getPaymentAmount() : BigDecimal.ZERO,
                    "balance", account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO,
                    "transactionNo", record.getTransactionNo() != null ? record.getTransactionNo() : ""));
            wsMessage.put("timestamp", System.currentTimeMillis());

            // 通过网关调用通知服务的WebSocket推送接口
            gatewayServiceClient.callCommonService(
                    "/api/v1/notification/websocket/push",
                    HttpMethod.POST,
                    wsMessage,
                    Object.class);

            log.debug("[在线消费流程] WebSocket推送成功, userId={}, recordId={}", form.getUserId(), record.getRecordId());
        } catch (Exception e) {
            // WebSocket推送失败不影响主流程
            log.warn("[在线消费流程] WebSocket推送失败, userId={}, recordId={}, error={}",
                    form.getUserId(), record.getRecordId(), e.getMessage());
        }
    }

    /**
     * 发送RabbitMQ消息
     * <p>
     * 发送消费事件消息到RabbitMQ队列，由消费者异步处理
     * 支持消费统计分析、报表生成等异步任务
     * </p>
     *
     * @param form    支付处理表单
     * @param record  消费记录
     * @param account 账户信息
     */
    private void sendRabbitMQMessage(PaymentProcessForm form, ConsumeRecordEntity record, AccountEntity account) {
        try {
            if (gatewayServiceClient == null) {
                log.debug("[在线消费流程] GatewayServiceClient未注入，跳过RabbitMQ消息发送");
                return;
            }

            Map<String, Object> mqMessage = new HashMap<>();
            mqMessage.put("eventType", "CONSUME_SUCCESS");
            mqMessage.put("userId", form.getUserId());
            mqMessage.put("accountId", form.getAccountId());
            mqMessage.put("recordId", record.getRecordId() != null ? record.getRecordId() : record.getId());
            mqMessage.put("transactionNo", record.getTransactionNo());
            mqMessage.put("amount", form.getPaymentAmount());
            mqMessage.put("balanceAfter", account.getBalance());
            mqMessage.put("deviceId", form.getDeviceId());
            mqMessage.put("areaId", form.getAreaId());
            mqMessage.put("consumeTime", record.getConsumeTime());
            mqMessage.put("timestamp", System.currentTimeMillis());

            // 通过网关调用通知服务发送RabbitMQ消息
            // 注意：实际的RabbitMQ发送由通知服务处理
            gatewayServiceClient.callCommonService(
                    "/api/v1/notification/mq/send",
                    HttpMethod.POST,
                    Map.of(
                            "queue", "consume.event.queue",
                            "routingKey", "consume.success",
                            "message", mqMessage),
                    Object.class);

            log.debug("[在线消费流程] RabbitMQ消息发送成功, userId={}, recordId={}", form.getUserId(), record.getRecordId());
        } catch (Exception e) {
            // RabbitMQ消息发送失败不影响主流程
            log.warn("[在线消费流程] RabbitMQ消息发送失败, userId={}, recordId={}, error={}",
                    form.getUserId(), record.getRecordId(), e.getMessage());
        }
    }

    /**
     * 发送站内信通知
     * <p>
     * 发送站内信通知，用户可以在系统中查看
     * </p>
     *
     * @param form    支付处理表单
     * @param record  消费记录
     * @param account 账户信息
     */
    private void sendStationMessage(PaymentProcessForm form, ConsumeRecordEntity record, AccountEntity account) {
        try {
            if (gatewayServiceClient == null) {
                log.debug("[在线消费流程] GatewayServiceClient未注入，跳过站内信发送");
                return;
            }

            String notificationContent = String.format(
                    "您的消费已完成，金额：%.2f 元，余额：%.2f 元。感谢您的使用！",
                    form.getPaymentAmount() != null ? form.getPaymentAmount() : BigDecimal.ZERO,
                    account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO);

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", form.getUserId());
            notificationData.put("channel", 4); // 站内信
            notificationData.put("subject", "消费成功通知");
            notificationData.put("content", notificationContent);
            notificationData.put("businessType", "CONSUME_SUCCESS");
            notificationData.put("businessId",
                    record.getTransactionNo() != null ? record.getTransactionNo() : String.valueOf(record.getId()));
            notificationData.put("messageType", 2); // 业务通知
            notificationData.put("priority", 2); // 普通优先级

            gatewayServiceClient.callCommonService(
                    "/api/v1/notification/send",
                    HttpMethod.POST,
                    notificationData,
                    Object.class);

            log.debug("[在线消费流程] 站内信通知发送成功, userId={}, recordId={}", form.getUserId(), record.getRecordId());
        } catch (Exception e) {
            // 站内信发送失败不影响主流程
            log.warn("[在线消费流程] 站内信通知发送失败, userId={}, recordId={}, error={}",
                    form.getUserId(), record.getRecordId(), e.getMessage());
        }
    }
}
