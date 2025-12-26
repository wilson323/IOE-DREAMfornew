package net.lab1024.sa.consume.client.fallback;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.client.AccountServiceClient;
import net.lab1024.sa.consume.client.dto.BalanceChangeResult;
import net.lab1024.sa.consume.client.dto.BalanceCheckResult;
import net.lab1024.sa.consume.client.dto.BalanceDecreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceIncreaseRequest;
import net.lab1024.sa.consume.client.dto.BalanceCheckRequest;
import net.lab1024.sa.consume.dao.AccountCompensationDao;
import net.lab1024.sa.common.entity.consume.AccountCompensationEntity;

/**
 * 账户服务Client降级工厂
 * <p>
 * 当账户服务不可用时，提供降级策略：
 * - 记录到本地补偿表，异步重试
 * - 返回友好的错误信息
 * - 避免级联失败
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Slf4j
@Component
public class AccountServiceClientFallback implements FallbackFactory<AccountServiceClient> {

    @Resource
    private AccountCompensationDao accountCompensationDao;

    @Override
    public AccountServiceClient create(Throwable cause) {
        return new AccountServiceClientFallbackImpl(cause, accountCompensationDao);
    }

    /**
     * 降级实现类
     */
    @Slf4j
    private static class AccountServiceClientFallbackImpl implements AccountServiceClient {

        private final Throwable cause;
        private final AccountCompensationDao accountCompensationDao;

        public AccountServiceClientFallbackImpl(Throwable cause, AccountCompensationDao accountCompensationDao) {
            this.cause = cause;
            this.accountCompensationDao = accountCompensationDao;
        }

        @Override
        public ResponseDTO<BalanceChangeResult> increaseBalance(BalanceIncreaseRequest request) {
            log.error("[账户服务降级] 余额增加失败: userId={}, amount={}, businessNo={}, error={}",
                request.getUserId(), request.getAmount(), request.getBusinessNo(),
                cause.getMessage(), cause);

            // 记录到本地补偿表
            saveCompensationRecord("INCREASE", request, cause.getMessage());

            // 返回服务不可用错误
            BalanceChangeResult result = BalanceChangeResult.failure(
                "SERVICE_UNAVAILABLE",
                "账户服务暂时不可用，已记录补偿记录，稍后自动重试"
            );

            return ResponseDTO.error(result.getErrorCode(), result.getErrorMessage());
        }

        @Override
        public ResponseDTO<BalanceChangeResult> decreaseBalance(BalanceDecreaseRequest request) {
            log.error("[账户服务降级] 余额扣减失败: userId={}, amount={}, businessNo={}, error={}",
                request.getUserId(), request.getAmount(), request.getBusinessNo(),
                cause.getMessage(), cause);

            // 余额扣减失败，记录到本地补偿表
            saveCompensationRecord("DECREASE", request, cause.getMessage());

            BalanceChangeResult result = BalanceChangeResult.failure(
                "SERVICE_UNAVAILABLE",
                "账户服务暂时不可用，已记录补偿记录，稍后自动重试"
            );

            return ResponseDTO.error(result.getErrorCode(), result.getErrorMessage());
        }

        @Override
        public ResponseDTO<BalanceCheckResult> checkBalance(BalanceCheckRequest request) {
            log.error("[账户服务降级] 余额检查失败: userId={}, amount={}, error={}",
                request.getUserId(), request.getAmount(), cause.getMessage(), cause);

            // 余额检查失败，默认返回余额不足（安全策略）
            BalanceCheckResult result = BalanceCheckResult.builder()
                .sufficient(false)
                .build();

            return ResponseDTO.userErrorParam("账户服务暂时不可用，无法查询余额");
        }

        @Override
        public ResponseDTO<BalanceChangeResult> queryBalance(Long userId) {
            log.error("[账户服务降级] 余额查询失败: userId={}, error={}",
                userId, cause.getMessage(), cause);

            BalanceChangeResult result = BalanceChangeResult.failure(
                "SERVICE_UNAVAILABLE",
                "账户服务暂时不可用"
            );

            return ResponseDTO.error(result.getErrorCode(), result.getErrorMessage());
        }

        @Override
        public ResponseDTO<BalanceChangeResult> freezeBalance(Long userId, java.math.BigDecimal amount, String businessNo) {
            log.error("[账户服务降级] 余额冻结失败: userId={}, amount={}, businessNo={}, error={}",
                userId, amount, businessNo, cause.getMessage(), cause);

            BalanceChangeResult result = BalanceChangeResult.failure(
                "SERVICE_UNAVAILABLE",
                "账户服务暂时不可用"
            );

            return ResponseDTO.error(result.getErrorCode(), result.getErrorMessage());
        }

        @Override
        public ResponseDTO<BalanceChangeResult> unfreezeBalance(Long userId, java.math.BigDecimal amount, String businessNo) {
            log.error("[账户服务降级] 余额解冻失败: userId={}, amount={}, businessNo={}, error={}",
                userId, amount, businessNo, cause.getMessage(), cause);

            BalanceChangeResult result = BalanceChangeResult.failure(
                "SERVICE_UNAVAILABLE",
                "账户服务暂时不可用"
            );

            return ResponseDTO.error(result.getErrorCode(), result.getErrorMessage());
        }

        /**
         * 保存补偿记录到本地数据库
         *
         * @param operation 操作类型（INCREASE/DECREASE）
         * @param request 请求对象
         * @param errorMessage 错误信息
         */
        private void saveCompensationRecord(String operation, Object request, String errorMessage) {
            try {
                if (request instanceof BalanceIncreaseRequest) {
                    BalanceIncreaseRequest req = (BalanceIncreaseRequest) request;

                    // 检查是否已存在相同业务编号的补偿记录（幂等性）
                    LambdaQueryWrapper<AccountCompensationEntity> queryWrapper =
                        new LambdaQueryWrapper<AccountCompensationEntity>()
                            .eq(AccountCompensationEntity::getBusinessNo, req.getBusinessNo())
                            .eq(AccountCompensationEntity::getDeletedFlag, 0);

                    AccountCompensationEntity existing = accountCompensationDao.selectOne(queryWrapper);
                    if (existing != null) {
                        log.warn("[补偿记录] 已存在相同业务编号的补偿记录，跳过保存: businessNo={}, status={}",
                            req.getBusinessNo(), existing.getStatus());
                        return;
                    }

                    // 创建新的补偿记录
                    AccountCompensationEntity compensation = AccountCompensationEntity.forIncrease(
                        req.getUserId(),
                        req.getAmount(),
                        req.getBusinessType(),
                        req.getBusinessNo(),
                        errorMessage
                    );

                    accountCompensationDao.insert(compensation);
                    log.info("[补偿记录] 已保存余额增加补偿: compensationId={}, userId={}, amount={}, businessNo={}",
                        compensation.getCompensationId(), req.getUserId(), req.getAmount(), req.getBusinessNo());

                } else if (request instanceof BalanceDecreaseRequest) {
                    BalanceDecreaseRequest req = (BalanceDecreaseRequest) request;

                    // 检查是否已存在相同业务编号的补偿记录（幂等性）
                    LambdaQueryWrapper<AccountCompensationEntity> queryWrapper =
                        new LambdaQueryWrapper<AccountCompensationEntity>()
                            .eq(AccountCompensationEntity::getBusinessNo, req.getBusinessNo())
                            .eq(AccountCompensationEntity::getDeletedFlag, 0);

                    AccountCompensationEntity existing = accountCompensationDao.selectOne(queryWrapper);
                    if (existing != null) {
                        log.warn("[补偿记录] 已存在相同业务编号的补偿记录，跳过保存: businessNo={}, status={}",
                            req.getBusinessNo(), existing.getStatus());
                        return;
                    }

                    // 创建新的补偿记录
                    AccountCompensationEntity compensation = AccountCompensationEntity.forDecrease(
                        req.getUserId(),
                        req.getAmount(),
                        req.getBusinessType(),
                        req.getBusinessNo(),
                        errorMessage
                    );

                    accountCompensationDao.insert(compensation);
                    log.info("[补偿记录] 已保存余额扣减补偿: compensationId={}, userId={}, amount={}, businessNo={}",
                        compensation.getCompensationId(), req.getUserId(), req.getAmount(), req.getBusinessNo());
                }
            } catch (Exception e) {
                log.error("[补偿记录] 保存补偿记录失败: operation={}, error={}",
                    operation, e.getMessage(), e);
            }
        }
    }
}
