package net.lab1024.sa.consume.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.client.AccountServiceClient;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;
import net.lab1024.sa.consume.client.dto.BalanceDecreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceIncreaseRequest;
import net.lab1024.sa.consume.dao.AccountCompensationDao;
import net.lab1024.sa.common.entity.consume.AccountCompensationEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 账户服务补偿任务调度器
 * <p>
 * 功能：
 * 1. 定时扫描待处理的补偿记录
 * 2. 重试账户服务调用
 * 3. 更新补偿记录状态
 * 4. 处理最大重试次数场景
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
@Schema(description = "账户服务补偿任务调度器")
public class AccountCompensationScheduler {

    @Resource
    private AccountCompensationDao accountCompensationDao;

    @Resource
    private AccountServiceClient accountServiceClient;

    /**
     * 每分钟扫描一次待处理的补偿记录
     * <p>
     * cron表达式: 秒 分 时 日 月 周
     * 0 * * * * * = 每分钟的0秒执行
     * </p>
     */
    @Scheduled(cron = "0 * * * * *")
    public void processPendingCompensations() {
        try {
            log.debug("[补偿调度] 开始扫描待处理的补偿记录");

            // 查询待处理的补偿记录
            LocalDateTime now = LocalDateTime.now();
            List<AccountCompensationEntity> pendingCompensations =
                accountCompensationDao.selectPendingCompensations(now, 100);

            if (pendingCompensations == null || pendingCompensations.isEmpty()) {
                log.debug("[补偿调度] 没有待处理的补偿记录");
                return;
            }

            log.info("[补偿调度] 发现 {} 条待处理补偿记录", pendingCompensations.size());

            // 处理每条补偿记录
            for (AccountCompensationEntity compensation : pendingCompensations) {
                processCompensation(compensation);
            }

            log.info("[补偿调度] 本次扫描完成，处理 {} 条补偿记录", pendingCompensations.size());
        } catch (Exception e) {
            log.error("[补偿调度] 扫描补偿记录异常", e);
        }
    }

    /**
     * 处理单条补偿记录
     *
     * @param compensation 补偿记录
     */
    private void processCompensation(AccountCompensationEntity compensation) {
        try {
            log.info("[补偿处理] 开始处理: compensationId={}, userId={}, operation={}, amount={}, businessNo={}",
                compensation.getCompensationId(), compensation.getUserId(),
                compensation.getOperation(), compensation.getAmount(), compensation.getBusinessNo());

            // 检查是否可以重试
            if (!compensation.canRetry()) {
                log.warn("[补偿处理] 跳过不可重试的记录: compensationId={}, status={}, retryCount={}",
                    compensation.getCompensationId(), compensation.getStatus(), compensation.getRetryCount());
                return;
            }

            // 根据操作类型调用账户服务
            boolean success = false;
            if ("INCREASE".equals(compensation.getOperation())) {
                success = retryIncreaseBalance(compensation);
            } else if ("DECREASE".equals(compensation.getOperation())) {
                success = retryDecreaseBalance(compensation);
            } else {
                log.warn("[补偿处理] 未知操作类型: compensationId={}, operation={}",
                    compensation.getCompensationId(), compensation.getOperation());
                markAsFailed(compensation, "UNKNOWN_OPERATION", "未知操作类型: " + compensation.getOperation());
                return;
            }

            // 更新重试次数和状态
            if (success) {
                markAsSuccess(compensation);
                log.info("[补偿处理] 补偿成功: compensationId={}, businessNo={}, transactionId={}",
                    compensation.getCompensationId(), compensation.getBusinessNo(),
                    compensation.getRemark());
            } else {
                // 检查是否达到最大重试次数
                if (compensation.isMaxRetryReached()) {
                    markAsFailed(compensation, "MAX_RETRY_REACHED",
                        "已达到最大重试次数(" + compensation.getMaxRetryCount() + ")");
                    log.error("[补偿处理] 达到最大重试次数: compensationId={}, businessNo={}",
                        compensation.getCompensationId(), compensation.getBusinessNo());
                } else {
                    incrementRetry(compensation);
                    log.warn("[补偿处理] 补偿失败，将在下次重试: compensationId={}, retryCount={}, nextRetryTime={}",
                        compensation.getCompensationId(), compensation.getRetryCount(),
                        compensation.getNextRetryTime());
                }
            }
        } catch (Exception e) {
            log.error("[补偿处理] 处理补偿记录异常: compensationId=" + compensation.getCompensationId(), e);

            // 检查是否达到最大重试次数
            if (compensation.isMaxRetryReached()) {
                markAsFailed(compensation, "MAX_RETRY_REACHED",
                    "已达到最大重试次数: " + e.getMessage());
            } else {
                incrementRetry(compensation);
            }
        }
    }

    /**
     * 重试余额增加
     *
     * @param compensation 补偿记录
     * @return 是否成功
     */
    private boolean retryIncreaseBalance(AccountCompensationEntity compensation) {
        try {
            BalanceIncreaseRequest request = new BalanceIncreaseRequest();
            request.setUserId(compensation.getUserId());
            request.setAmount(compensation.getAmount());
            request.setBusinessType(compensation.getBusinessType());
            request.setBusinessNo(compensation.getBusinessNo());

            ResponseDTO<BalanceChangeResult> response = accountServiceClient.increaseBalance(request);

            if (response == null || !response.isSuccess()) {
                log.warn("[补偿重试] 余额增加失败: compensationId={}, response={}",
                    compensation.getCompensationId(),
                    response != null ? response.getMessage() : "null response");
                return false;
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                log.warn("[补偿重试] 余额增加失败: compensationId={}, errorCode={}, errorMessage={}",
                    compensation.getCompensationId(),
                    result != null ? result.getErrorCode() : "null",
                    result != null ? result.getErrorMessage() : "null result");
                return false;
            }

            // 保存交易ID到备注
            compensation.setRemark("transactionId: " + result.getTransactionId());
            return true;
        } catch (Exception e) {
            log.error("[补偿重试] 余额增加异常: compensationId=" + compensation.getCompensationId(), e);
            return false;
        }
    }

    /**
     * 重试余额扣减
     *
     * @param compensation 补偿记录
     * @return 是否成功
     */
    private boolean retryDecreaseBalance(AccountCompensationEntity compensation) {
        try {
            BalanceDecreaseRequest request = new BalanceDecreaseRequest();
            request.setUserId(compensation.getUserId());
            request.setAmount(compensation.getAmount());
            request.setBusinessType(compensation.getBusinessType());
            request.setBusinessNo(compensation.getBusinessNo());

            ResponseDTO<BalanceChangeResult> response = accountServiceClient.decreaseBalance(request);

            if (response == null || !response.isSuccess()) {
                log.warn("[补偿重试] 余额扣减失败: compensationId={}, response={}",
                    compensation.getCompensationId(),
                    response != null ? response.getMessage() : "null response");
                return false;
            }

            BalanceChangeResult result = response.getData();
            if (result == null || !result.getSuccess()) {
                log.warn("[补偿重试] 余额扣减失败: compensationId={}, errorCode={}, errorMessage={}",
                    compensation.getCompensationId(),
                    result != null ? result.getErrorCode() : "null",
                    result != null ? result.getErrorMessage() : "null result");
                return false;
            }

            // 保存交易ID到备注
            compensation.setRemark("transactionId: " + result.getTransactionId());
            return true;
        } catch (Exception e) {
            log.error("[补偿重试] 余额扣减异常: compensationId=" + compensation.getCompensationId(), e);
            return false;
        }
    }

    /**
     * 标记补偿记录为成功
     *
     * @param compensation 补偿记录
     */
    private void markAsSuccess(AccountCompensationEntity compensation) {
        compensation.markAsSuccess();

        LambdaUpdateWrapper<AccountCompensationEntity> updateWrapper =
            new LambdaUpdateWrapper<AccountCompensationEntity>()
                .eq(AccountCompensationEntity::getCompensationId, compensation.getCompensationId())
                .set(AccountCompensationEntity::getStatus, "SUCCESS")
                .set(AccountCompensationEntity::getSuccessTime, compensation.getSuccessTime())
                .set(AccountCompensationEntity::getUpdateTime, compensation.getUpdateTime())
                .set(AccountCompensationEntity::getRemark, compensation.getRemark());

        accountCompensationDao.update(null, updateWrapper);
    }

    /**
     * 标记补偿记录为失败
     *
     * @param compensation 补偿记录
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    private void markAsFailed(AccountCompensationEntity compensation, String errorCode, String errorMessage) {
        compensation.markAsFailed(errorCode, errorMessage);

        LambdaUpdateWrapper<AccountCompensationEntity> updateWrapper =
            new LambdaUpdateWrapper<AccountCompensationEntity>()
                .eq(AccountCompensationEntity::getCompensationId, compensation.getCompensationId())
                .set(AccountCompensationEntity::getStatus, "FAILED")
                .set(AccountCompensationEntity::getErrorCode, compensation.getErrorCode())
                .set(AccountCompensationEntity::getErrorMessage, compensation.getErrorMessage())
                .set(AccountCompensationEntity::getUpdateTime, compensation.getUpdateTime());

        accountCompensationDao.update(null, updateWrapper);
    }

    /**
     * 增加重试次数
     *
     * @param compensation 补偿记录
     */
    private void incrementRetry(AccountCompensationEntity compensation) {
        compensation.incrementRetry();

        LambdaUpdateWrapper<AccountCompensationEntity> updateWrapper =
            new LambdaUpdateWrapper<AccountCompensationEntity>()
                .eq(AccountCompensationEntity::getCompensationId, compensation.getCompensationId())
                .set(AccountCompensationEntity::getRetryCount, compensation.getRetryCount())
                .set(AccountCompensationEntity::getLastRetryTime, compensation.getLastRetryTime())
                .set(AccountCompensationEntity::getNextRetryTime, compensation.getNextRetryTime())
                .set(AccountCompensationEntity::getUpdateTime, compensation.getUpdateTime());

        accountCompensationDao.update(null, updateWrapper);
    }

    /**
     * 统计待处理的补偿记录数量（每5分钟执行一次）
     * <p>
     * 用于监控和告警
     * </p>
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void reportPendingCompensations() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int pendingCount = accountCompensationDao.countPendingCompensations(now);

            if (pendingCount > 0) {
                log.warn("[补偿统计] 当前有 {} 条待处理的补偿记录", pendingCount);

                // 如果待处理记录过多，发送告警
                if (pendingCount > 1000) {
                    log.error("[补偿告警] 待处理补偿记录过多: {} 条，可能需要人工介入", pendingCount);
                    // TODO: 发送告警通知（邮件、短信、钉钉等）
                }
            }
        } catch (Exception e) {
            log.error("[补偿统计] 统计待处理补偿记录异常", e);
        }
    }
}
