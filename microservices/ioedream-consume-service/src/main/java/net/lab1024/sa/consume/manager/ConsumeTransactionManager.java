package net.lab1024.sa.consume.manager;

import io.seata.spring.annotation.GlobalTransactional;
// 注意：GlobalTransactional在Seata 2.x中已标记为过时，但仍可正常使用
// Seata官方建议继续使用此注解，未来版本会提供新的替代方案
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.common.consume.entity.AccountEntity;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费事务管理器（使用Seata分布式事务）
 * <p>
 * 替换原有的SagaManager，使用Seata的@GlobalTransactional实现分布式事务
 * 严格遵循CLAUDE.md全局架构规范：纯Java类，不使用Spring注解
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 2.0.0
 * @since 2025-01-30
 * @updated 2025-12-17 移除@Component注解，改为纯Java类，使用构造函数注入
 */
@Slf4j
public class ConsumeTransactionManager {

    private final ConsumeRecordDao consumeRecordDao;
    private final AccountDao accountDao;
    private final GatewayServiceClient gatewayServiceClient;

    /**
     * 构造函数注入依赖
     */
    public ConsumeTransactionManager(ConsumeRecordDao consumeRecordDao,
                                   AccountDao accountDao,
                                   GatewayServiceClient gatewayServiceClient) {
        this.consumeRecordDao = consumeRecordDao;
        this.accountDao = accountDao;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    /**
     * 执行消费事务（使用Seata分布式事务）
     * <p>
     * 使用@GlobalTransactional注解，Seata会自动管理分布式事务
     * 如果任何步骤失败，Seata会自动回滚所有已执行的操作
     * </p>
     *
     * @param consumeRequest 消费请求
     * @return 消费记录ID
     */
    @SuppressWarnings("deprecation")  // Seata 2.x标记为过时，但官方建议继续使用
    @GlobalTransactional(
            name = "consume-transaction",
            rollbackFor = Exception.class,
            timeoutMills = 30000
    )
    @Transactional(rollbackFor = Exception.class)
    public Long executeConsumeTransaction(ConsumeRequestDTO consumeRequest) {
        log.info("[消费事务] 开始执行消费事务，orderId={}, amount={}, accountId={}",
                consumeRequest.getOrderId(), consumeRequest.getAmount(), consumeRequest.getAccountId());

        try {
            // 1. 验证账户状态
            AccountEntity account = validateAccount(consumeRequest.getAccountId());

            // 2. 扣减账户余额
            BigDecimal newBalance = deductBalance(consumeRequest, account);

            // 3. 创建消费记录
            Long recordId = createConsumeRecord(consumeRequest);

            // 4. 发送通知（异步，不影响事务）
            sendNotificationAsync(consumeRequest);

            log.info("[消费事务] 消费事务执行成功，orderId={}, recordId={}, newBalance={}",
                    consumeRequest.getOrderId(), recordId, newBalance);

            return recordId;
        } catch (BusinessException e) {
            log.error("[消费事务] 消费事务业务异常，orderId={}, error={}",
                    consumeRequest.getOrderId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[消费事务] 消费事务系统异常，orderId={}", consumeRequest.getOrderId(), e);
            throw new BusinessException("CONSUME_TRANSACTION_ERROR", "消费事务执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证账户状态
     *
     * @param accountId 账户ID
     * @return 账户实体
     */
    private AccountEntity validateAccount(Long accountId) {
        AccountEntity account = accountDao.selectById(accountId);
        if (account == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在: " + accountId);
        }

        // 账户状态：1-正常 2-冻结 3-注销
        if (account.getStatus() == null || !Integer.valueOf(1).equals(account.getStatus())) {
            throw new BusinessException("ACCOUNT_INACTIVE", "账户未激活或已冻结: " + accountId);
        }

        return account;
    }

    /**
     * 扣减余额
     *
     * @param consumeRequest 消费请求
     * @param account 账户实体
     * @return 新余额
     */
    private BigDecimal deductBalance(ConsumeRequestDTO consumeRequest, AccountEntity account) {
        BigDecimal oldBalance = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
        BigDecimal amount = consumeRequest.getAmount() != null ? consumeRequest.getAmount() : BigDecimal.ZERO;
        BigDecimal newBalance = oldBalance.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE",
                    String.format("余额不足，当前余额: %.2f, 消费金额: %.2f", oldBalance, amount));
        }

        int updateCount = accountDao.updateBalance(
                consumeRequest.getAccountId(),
                amount.negate(),
                consumeRequest.getUserId()
        );

        if (updateCount <= 0) {
            throw new BusinessException("BALANCE_UPDATE_FAILED", "余额更新失败");
        }

        log.info("[消费事务] 余额扣减成功，accountId={}, oldBalance={}, amount={}, newBalance={}",
                consumeRequest.getAccountId(), oldBalance, amount, newBalance);

        return newBalance;
    }

    /**
     * 创建消费记录
     *
     * @param consumeRequest 消费请求
     * @return 消费记录ID
     */
    private Long createConsumeRecord(ConsumeRequestDTO consumeRequest) {
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setOrderNo(consumeRequest.getOrderId());
        record.setAccountId(consumeRequest.getAccountId());
        record.setAmount(consumeRequest.getAmount());

        // 设备ID类型转换：String -> Long
        String deviceIdStr = consumeRequest.getDeviceId();
        if (deviceIdStr != null && !deviceIdStr.isEmpty()) {
            try {
                record.setDeviceId(Long.parseLong(deviceIdStr));
            } catch (NumberFormatException e) {
                log.warn("[消费事务] 设备ID格式错误: {}", deviceIdStr);
                record.setDeviceId(null);
            }
        }

        record.setAreaId(consumeRequest.getAreaId());
        record.setConsumeType(consumeRequest.getConsumeType());
        record.setStatus("SUCCESS");
        record.setConsumeTime(LocalDateTime.now());

        int insertCount = consumeRecordDao.insert(record);
        if (insertCount <= 0) {
            throw new BusinessException("CREATE_RECORD_FAILED", "消费记录创建失败");
        }

        log.info("[消费事务] 消费记录创建成功，orderId={}, recordId={}",
                consumeRequest.getOrderId(), record.getId());

        return record.getId();
    }

    /**
     * 异步发送通知（不影响事务）
     *
     * @param consumeRequest 消费请求
     */
    private void sendNotificationAsync(ConsumeRequestDTO consumeRequest) {
        try {
            if (consumeRequest.getUserId() == null) {
                log.warn("[消费事务] 用户ID为空，跳过通知发送: orderId={}", consumeRequest.getOrderId());
                return;
            }

            String notificationContent = String.format(
                    "您的消费订单 %s 已完成，金额：%.2f 元。感谢您的使用！",
                    consumeRequest.getOrderId(),
                    consumeRequest.getAmount() != null ? consumeRequest.getAmount() : BigDecimal.ZERO
            );

            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", consumeRequest.getUserId());
            notificationData.put("channel", 4); // 站内信
            notificationData.put("subject", "消费成功通知");
            notificationData.put("content", notificationContent);
            notificationData.put("businessType", "CONSUME_SUCCESS");
            notificationData.put("businessId", consumeRequest.getOrderId());
            notificationData.put("messageType", 2); // 业务通知
            notificationData.put("priority", 2); // 普通优先级

            if (gatewayServiceClient != null) {
                gatewayServiceClient.callCommonService(
                        "/api/v1/notification/send",
                        HttpMethod.POST,
                        notificationData,
                        Long.class
                );
                log.info("[消费事务] 消费成功通知发送成功: orderId={}, userId={}",
                        consumeRequest.getOrderId(), consumeRequest.getUserId());
            }
        } catch (Exception e) {
            // 通知发送失败不影响主流程
            log.warn("[消费事务] 发送通知失败，但不影响主流程: orderId={}, error={}",
                    consumeRequest.getOrderId(), e.getMessage());
        }
    }
}