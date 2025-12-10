package net.lab1024.sa.consume.manager;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.transaction.saga.SagaManager;
import net.lab1024.sa.common.transaction.saga.SagaResult;
import net.lab1024.sa.common.transaction.saga.SagaStepResult;
import net.lab1024.sa.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.consume.dao.AccountDao;
import net.lab1024.sa.common.consume.entity.ConsumeRecordEntity;
import net.lab1024.sa.consume.domain.entity.AccountEntity;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpMethod;

/**
 * 消费SAGA事务管理器
 * 处理消费相关的分布式事务
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class ConsumeSagaManager {

    @Resource
    private SagaManager sagaManager;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private AccountDao accountDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 执行消费SAGA事务
     *
     * @param consumeRequest 消费请求
     * @return 执行结果
     */
    public CompletableFuture<SagaResult> executeConsumeSaga(ConsumeRequestDTO consumeRequest) {
        log.info("[消费SAGA] 开始执行消费事务，businessKey={}, amount={}",
                consumeRequest.getOrderId(), consumeRequest.getAmount());

        return sagaManager.createSaga("CONSUME_" + consumeRequest.getOrderId())
                .step("validateAccount", () -> {
                    // 1. 验证账户状态
                    return validateAccount(consumeRequest.getAccountId());
                }, () -> {
                    // 补偿：无需操作
                    return SagaStepResult.success();
                })
                .step("deductBalance", () -> {
                    // 2. 扣减账户余额
                    return deductBalance(consumeRequest);
                }, () -> {
                    // 补偿：恢复账户余额
                    return refundBalance(consumeRequest);
                })
                .step("createConsumeRecord", () -> {
                    // 3. 创建消费记录
                    return createConsumeRecord(consumeRequest);
                }, () -> {
                    // 补偿：删除消费记录
                    return deleteConsumeRecord(consumeRequest.getOrderId());
                })
                .step("sendNotification", () -> {
                    // 4. 发送通知
                    return sendNotification(consumeRequest);
                }, () -> {
                    // 补偿：发送取消通知
                    return sendCancelNotification(consumeRequest);
                })
                .retry(3)
                .execute()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("[消费SAGA] 事务执行异常: {}", throwable.getMessage(), throwable);
                    } else if (result.isSuccess()) {
                        log.info("[消费SAGA] 事务执行成功: {}", consumeRequest.getOrderId());
                    } else {
                        log.error("[消费SAGA] 事务执行失败: {}, error: {}",
                                consumeRequest.getOrderId(), result.getErrorMessage());
                    }
                });
    }

    /**
     * 验证账户状态
     *
     * @param accountId 账户ID
     * @return 验证结果
     */
    private SagaStepResult validateAccount(Long accountId) {
        try {
            AccountEntity account = accountDao.selectById(accountId);
            if (account == null) {
                return SagaStepResult.failure("ACCOUNT_NOT_FOUND", "账户不存在");
            }
            // 账户状态：1-正常 2-冻结 3-注销
            if (account.getStatus() == null || !Integer.valueOf(1).equals(account.getStatus())) {
                return SagaStepResult.failure("ACCOUNT_INACTIVE", "账户未激活");
            }
            return SagaStepResult.success(account);
        } catch (Exception e) {
            log.error("[消费SAGA] 验证账户失败: accountId={}", accountId, e);
            return SagaStepResult.failure("VALIDATE_ACCOUNT_ERROR", e.getMessage());
        }
    }

    /**
     * 扣减余额
     *
     * @param consumeRequest 消费请求
     * @return 执行结果
     */
    private SagaStepResult deductBalance(ConsumeRequestDTO consumeRequest) {
        try {
            BigDecimal oldBalance = accountDao.getBalanceById(consumeRequest.getAccountId());
            BigDecimal newBalance = oldBalance.subtract(consumeRequest.getAmount());

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                return SagaStepResult.failure("INSUFFICIENT_BALANCE", "余额不足");
            }

            int updateCount = accountDao.updateBalance(
                    consumeRequest.getAccountId(),
                    consumeRequest.getAmount().negate(),
                    consumeRequest.getUserId() // 传递更新用户ID
            );

            if (updateCount > 0) {
                return SagaStepResult.success(newBalance);
            } else {
                return SagaStepResult.failure("BALANCE_UPDATE_FAILED", "余额更新失败");
            }
        } catch (Exception e) {
            log.error("[消费SAGA] 扣减余额失败: {}", consumeRequest.getOrderId(), e);
            return SagaStepResult.failure("DEDUCT_BALANCE_ERROR", e.getMessage());
        }
    }

    /**
     * 恢复余额（补偿操作）
     *
     * @param consumeRequest 消费请求
     * @return 执行结果
     */
    private SagaStepResult refundBalance(ConsumeRequestDTO consumeRequest) {
        try {
            int updateCount = accountDao.updateBalance(
                    consumeRequest.getAccountId(),
                    consumeRequest.getAmount(),
                    consumeRequest.getUserId() // 补偿操作需要传递更新用户ID
            );
            return updateCount > 0 ? SagaStepResult.success() : SagaStepResult.failure("REFUND_FAILED", "余额恢复失败");
        } catch (Exception e) {
            log.error("[消费SAGA] 恢复余额失败: {}", consumeRequest.getOrderId(), e);
            return SagaStepResult.failure("REFUND_BALANCE_ERROR", e.getMessage());
        }
    }

    /**
     * 创建消费记录
     *
     * @param consumeRequest 消费请求
     * @return 执行结果
     */
    private SagaStepResult createConsumeRecord(ConsumeRequestDTO consumeRequest) {
        try {
            ConsumeRecordEntity record = new ConsumeRecordEntity();
            record.setOrderNo(consumeRequest.getOrderId()); // 修正：orderNo
            record.setAccountId(consumeRequest.getAccountId());
            record.setAmount(consumeRequest.getAmount());

            // 设备ID类型转换：String -> Long
            String deviceIdStr = consumeRequest.getDeviceId();
            if (deviceIdStr != null && !deviceIdStr.isEmpty()) {
                try {
                    record.setDeviceId(Long.parseLong(deviceIdStr));
                } catch (NumberFormatException e) {
                    log.warn("[消费SAGA] 设备ID格式错误: {}", deviceIdStr);
                    record.setDeviceId(null);
                }
            }

            record.setAreaId(consumeRequest.getAreaId());
            record.setConsumeType(consumeRequest.getConsumeType());
            record.setStatus("SUCCESS");
            record.setConsumeTime(java.time.LocalDateTime.now()); // 修正：consumeTime

            int insertCount = consumeRecordDao.insert(record);
            return insertCount > 0 ? SagaStepResult.success(record) : SagaStepResult.failure("INSERT_FAILED", "记录创建失败");
        } catch (Exception e) {
            log.error("[消费SAGA] 创建消费记录失败: {}", consumeRequest.getOrderId(), e);
            return SagaStepResult.failure("CREATE_RECORD_ERROR", e.getMessage());
        }
    }

    /**
     * 删除消费记录（补偿操作）
     *
     * @param orderNo 订单号
     * @return 执行结果
     */
    private SagaStepResult deleteConsumeRecord(String orderNo) {
        try {
            int deleteCount = consumeRecordDao.deleteByOrderNo(orderNo);
            return deleteCount > 0 ? SagaStepResult.success() : SagaStepResult.failure("DELETE_FAILED", "记录删除失败");
        } catch (Exception e) {
            log.error("[消费SAGA] 删除消费记录失败: {}", orderNo, e);
            return SagaStepResult.failure("DELETE_RECORD_ERROR", e.getMessage());
        }
    }

    /**
     * 发送通知
     * <p>
     * 当消费事务成功时，通知用户消费已完成
     * 通过GatewayServiceClient调用公共服务的通知接口
     * </p>
     *
     * @param consumeRequest 消费请求
     * @return 执行结果
     */
    private SagaStepResult sendNotification(ConsumeRequestDTO consumeRequest) {
        try {
            // 检查必要参数
            if (consumeRequest.getUserId() == null) {
                log.warn("[消费SAGA] 用户ID为空，跳过成功通知发送: orderId={}", consumeRequest.getOrderId());
                return SagaStepResult.success(); // 通知失败不影响主流程
            }

            // 构建成功通知内容
            String notificationContent = String.format(
                "您的消费订单 %s 已完成，金额：%.2f 元。感谢您的使用！",
                consumeRequest.getOrderId(),
                consumeRequest.getAmount() != null ? consumeRequest.getAmount() : BigDecimal.ZERO
            );

            // 构建通知数据
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", consumeRequest.getUserId());
            notificationData.put("channel", 4); // 站内信
            notificationData.put("subject", "消费成功通知");
            notificationData.put("content", notificationContent);
            notificationData.put("businessType", "CONSUME_SUCCESS");
            notificationData.put("businessId", consumeRequest.getOrderId());
            notificationData.put("messageType", 2); // 业务通知
            notificationData.put("priority", 2); // 普通优先级

            // 通过网关调用公共服务发送通知
            if (gatewayServiceClient != null) {
                try {
                    gatewayServiceClient.callCommonService(
                        "/api/v1/notification/send",
                        HttpMethod.POST,
                        notificationData,
                        Long.class
                    );
                    log.info("[消费SAGA] 消费成功通知发送成功: orderId={}, userId={}",
                        consumeRequest.getOrderId(), consumeRequest.getUserId());
                } catch (Exception e) {
                    log.warn("[消费SAGA] 调用通知服务失败，但不影响主流程: orderId={}, error={}",
                        consumeRequest.getOrderId(), e.getMessage());
                    // 通知发送失败不影响主流程，返回成功
                }
            } else {
                log.warn("[消费SAGA] GatewayServiceClient未配置，跳过成功通知发送: orderId={}",
                    consumeRequest.getOrderId());
            }

            return SagaStepResult.success();
        } catch (Exception e) {
            log.error("[消费SAGA] 发送通知异常: orderId={}, error={}",
                consumeRequest.getOrderId(), e.getMessage(), e);
            // 通知发送失败不影响主流程，返回成功
            return SagaStepResult.success();
        }
    }

    /**
     * 发送取消通知（补偿操作）
     * <p>
     * 当消费事务回滚时，通知用户消费已取消
     * 通过GatewayServiceClient调用公共服务的通知接口
     * </p>
     *
     * @param consumeRequest 消费请求
     * @return 执行结果
     */
    private SagaStepResult sendCancelNotification(ConsumeRequestDTO consumeRequest) {
        try {
            // 检查必要参数
            if (consumeRequest.getUserId() == null) {
                log.warn("[消费SAGA] 用户ID为空，跳过取消通知发送: orderId={}", consumeRequest.getOrderId());
                return SagaStepResult.success(); // 通知失败不影响补偿流程
            }

            // 构建取消通知内容
            String notificationContent = String.format(
                "您的消费订单 %s 已取消，金额：%.2f 元。如有疑问，请联系客服。",
                consumeRequest.getOrderId(),
                consumeRequest.getAmount() != null ? consumeRequest.getAmount() : BigDecimal.ZERO
            );

            // 构建通知数据
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("recipientUserId", consumeRequest.getUserId());
            notificationData.put("channel", 4); // 站内信
            notificationData.put("subject", "消费取消通知");
            notificationData.put("content", notificationContent);
            notificationData.put("businessType", "CONSUME_CANCEL");
            notificationData.put("businessId", consumeRequest.getOrderId());
            notificationData.put("messageType", 2); // 业务通知
            notificationData.put("priority", 2); // 普通优先级

            // 通过网关调用公共服务发送通知
            if (gatewayServiceClient != null) {
                try {
                    gatewayServiceClient.callCommonService(
                        "/api/v1/notification/send",
                        HttpMethod.POST,
                        notificationData,
                        Long.class
                    );
                    log.info("[消费SAGA] 消费取消通知发送成功: orderId={}, userId={}",
                        consumeRequest.getOrderId(), consumeRequest.getUserId());
                } catch (Exception e) {
                    log.warn("[消费SAGA] 调用通知服务失败，但不影响补偿流程: orderId={}, error={}",
                        consumeRequest.getOrderId(), e.getMessage());
                    // 通知发送失败不影响补偿流程，返回成功
                }
            } else {
                log.warn("[消费SAGA] GatewayServiceClient未配置，跳过取消通知发送: orderId={}",
                    consumeRequest.getOrderId());
            }

            return SagaStepResult.success();
        } catch (Exception e) {
            log.error("[消费SAGA] 发送取消通知异常: orderId={}, error={}",
                consumeRequest.getOrderId(), e.getMessage(), e);
            // 通知发送失败不影响补偿流程，返回成功
            return SagaStepResult.success();
        }
    }
}
