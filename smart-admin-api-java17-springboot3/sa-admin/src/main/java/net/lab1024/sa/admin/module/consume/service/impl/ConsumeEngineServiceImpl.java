package net.lab1024.sa.admin.module.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.ConsumeRecordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import net.lab1024.sa.admin.module.consume.engine.ConsumeRequest;
import net.lab1024.sa.admin.module.consume.engine.ConsumeResult;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.ConsumeEngineService;
import net.lab1024.sa.admin.module.consume.service.validator.ConsumePermissionValidator;
import net.lab1024.sa.admin.module.consume.service.security.AccountSecurityManager;
import net.lab1024.sa.admin.module.consume.service.consistency.DataConsistencyManager;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartVerificationUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 核心消费引擎服务实现
 * 负责处理所有消费交易的核心逻辑
 * 确保资金安全和数据一致性
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Service
@Slf4j
public class ConsumeEngineServiceImpl implements ConsumeEngineService {

    @Resource
    private AccountService accountService;

    @Resource
    private ConsumeRecordDao consumeRecordDao;

    @Resource
    private ConsumePermissionValidator permissionValidator;

    @Resource
    private AccountSecurityManager securityManager;

    @Resource
    private DataConsistencyManager consistencyManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResult processConsume(@Valid ConsumeRequest request) {
        log.info("开始处理消费请求: personId={}, amount={}, device={}",
                 request.getPersonId(), request.getAmount(), request.getDeviceId());

        try {
            // 1. 参数预处理和验证
            ConsumeRequest processedRequest = preprocessRequest(request);

            // 2. 幂等性检查 - 防止重复扣费
            ConsumeResult existingResult = checkIdempotency(processedRequest.getOrderNo());
            if (existingResult != null) {
                log.info("发现重复订单，返回已有结果: orderNo={}", processedRequest.getOrderNo());
                return existingResult;
            }

            // 3. 权限验证（集成Sa-Token和RAC权限中间件）
            ConsumePermissionValidator.ConsumePermissionResult permissionResult =
                permissionValidator.validateConsumePermission(
                    processedRequest.getPersonId(),
                    processedRequest.getDeviceId(),
                    processedRequest.getRegionId()
                );

            if (!permissionResult.isSuccess()) {
                log.warn("消费权限验证失败: personId={}, errorCode={}, error={}",
                         processedRequest.getPersonId(), permissionResult.getErrorCode(), permissionResult.getErrorMessage());
                return ConsumeResult.failure(permissionResult.getErrorCode(), permissionResult.getErrorMessage());
            }

            // 4. 账户状态验证（权限验证器已包含，但这里保留余额检查所需的账户信息）
            AccountEntity account = validateAndGetAccount(processedRequest.getPersonId());

            // 5. 安全检查：账户冻结状态
            if (securityManager.isAccountFrozen(account.getPersonId())) {
                AccountSecurityManager.FreezeInfo freezeInfo = securityManager.getFreezeInfo(account.getPersonId());
                log.warn("账户已被冻结: personId={}, reason={}", account.getPersonId(),
                        freezeInfo != null ? freezeInfo.getReason() : "未知原因");
                return ConsumeResult.failure("ACCOUNT_FROZEN",
                    "账户已被冻结：" + (freezeInfo != null ? freezeInfo.getReason() : "请联系管理员"));
            }

            // 6. 安全检查：消费限额验证
            AccountSecurityManager.LimitCheckResult limitResult = securityManager.checkConsumeLimit(account, processedRequest.getAmount());
            if (limitResult.isExceeded()) {
                log.warn("消费限额检查失败: personId={}, reason={}",
                        processedRequest.getPersonId(), limitResult.getReason());
                return ConsumeResult.failure(limitResult.getReason(), limitResult.getMessage());
            }

            // 7. 安全检查：支付密码验证（如果需要）
            if (processedRequest.isPaymentPasswordRequired()) {
                if (!StringUtils.hasText(processedRequest.getPaymentPassword())) {
                    return ConsumeResult.failure("PAYMENT_PASSWORD_REQUIRED", "此交易需要支付密码");
                }

                AccountSecurityManager.PaymentPasswordResult pwdResult =
                    securityManager.verifyPaymentPassword(account.getPersonId(), processedRequest.getPaymentPassword());
                if (!pwdResult.isSuccess()) {
                    log.warn("支付密码验证失败: personId={}, error={}", account.getPersonId(), pwdResult.getErrorCode());
                    return ConsumeResult.failure(pwdResult.getErrorCode(), pwdResult.getErrorMessage());
                }
            }

            // 8. 余额验证
            if (!validateBalance(account, processedRequest.getAmount())) {
                log.warn("余额不足: personId={}, balance={}, amount={}",
                         account.getAccountId(), account.getBalance(), processedRequest.getAmount());
                return ConsumeResult.failure("INSUFFICIENT_BALANCE",
                    String.format("余额不足，当前余额: %s，消费金额: %s", account.getBalance(), processedRequest.getAmount()));
            }

            // 9. 执行核心消费逻辑（带数据一致性保障）
            String lockKey = "consume:account:" + account.getAccountId();
            String dataKey = "account:" + account.getAccountId();

            ConsumeResult result = consistencyManager.executeTransactional(lockKey, dataKey, (currentVersion) ->
                executeConsumeTransactionWithConsistency(account, processedRequest, currentVersion)
            );

            log.info("消费处理成功: orderNo={}, amount={}, balanceAfter={}",
                    result.getOrderNo(), result.getAmount(), result.getBalanceAfter());

            return result;

        } catch (SmartException e) {
            log.error("消费处理失败: {}", e.getMessage(), e);
            return ConsumeResult.failure(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("消费处理异常: {}", e.getMessage(), e);
            return ConsumeResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 参数预处理
     */
    private ConsumeRequest preprocessRequest(ConsumeRequest request) {
        ConsumeRequest processed = new ConsumeRequest();
        SmartBeanUtil.copyProperties(request, processed);

        // 生成订单号（如果没有提供）
        if (!StringUtils.hasText(processed.getOrderNo())) {
            processed.setOrderNo(generateOrderNo());
        }

        // 设置默认币种
        if (!StringUtils.hasText(processed.getCurrency())) {
            processed.setCurrency("CNY");
        }

        // 金额验证（必须大于0）
        if (processed.getAmount() == null || processed.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SmartException("消费金额必须大于0");
        }

        return processed;
    }

    /**
     * 幂等性检查
     */
    private ConsumeResult checkIdempotency(String orderNo) {
        try {
            // 根据订单号查询是否已有消费记录
            ConsumeRecordEntity record = consumeRecordDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ConsumeRecordEntity>()
                    .eq("order_no", orderNo)
            );

            if (record != null) {
                log.info("发现重复订单，返回已有结果: orderNo={}, status={}", orderNo, record.getStatus());
                return convertToResult(record);
            }
        } catch (Exception e) {
            log.error("幂等性检查异常: orderNo={}", orderNo, e);
            // 幂等性检查异常时，允许继续处理（避免影响正常消费）
        }
        return null;
    }

    /**
     * 将消费记录转换为结果对象
     */
    private ConsumeResult convertToResult(ConsumeRecordEntity record) {
        ConsumeResult result;

        if ("SUCCESS".equals(record.getStatus())) {
            result = ConsumeResult.success(record.getAmount());
        } else if ("FAILED".equals(record.getStatus())) {
            result = ConsumeResult.failure(record.getErrorCode(), record.getErrorMsg());
        } else {
            result = ConsumeResult.pending(record.getStatus());
        }

        result.setRecordId(record.getRecordId());
        result.setOrderNo(record.getOrderNo());
        result.setThirdPartyOrderNo(record.getThirdPartyOrderNo());
        result.setActualAmount(record.getAmount());
        result.setBalanceBefore(record.getBalanceBefore());
        result.setBalanceAfter(record.getBalanceAfter());
        result.setConsumeTime(record.getConsumeTime());
        result.setConsumptionMode(record.getConsumptionMode());
        result.setPayMethod(record.getPayMethod());
        result.setDeviceInfo(record.getDeviceName());

        return result;
    }

    /**
     * 验证并获取账户信息
     */
    private AccountEntity validateAndGetAccount(Long personId) {
        try {
            AccountEntity account = accountService.getByPersonId(personId);
            if (account == null) {
                throw new SmartException("账户不存在");
            }
            if (!"ACTIVE".equals(account.getStatus())) {
                throw new SmartException("账户状态异常: " + account.getStatus());
            }
            return account;
        } catch (SmartException e) {
            throw e;
        } catch (Exception e) {
            log.error("账户查询异常: personId={}", personId, e);
            throw new SmartException("账户查询失败");
        }
    }

    /**
     * 余额验证
     */
    private boolean validateBalance(AccountEntity account, BigDecimal amount) {
        BigDecimal availableBalance = account.getBalance()
                .add(account.getCreditLimit())
                .subtract(account.getFrozenAmount());

        return availableBalance.compareTo(amount) >= 0;
    }

    /**
     * 执行带一致性保障的核心消费交易
     */
    private ConsumeResult executeConsumeTransactionWithConsistency(AccountEntity account, ConsumeRequest request, long currentVersion) {
        log.debug("执行一致性保障消费交易: accountId={}, orderNo={}, version={}",
                 account.getAccountId(), request.getOrderNo(), currentVersion);

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal amount = request.getAmount();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // 1. 验证账户版本（确保数据没有被其他操作修改）
        if (!consistencyManager.validateDataVersion("account:" + account.getAccountId(), currentVersion)) {
            throw new SmartException("账户数据已被修改，请重试");
        }

        // 2. 扣减账户余额（带乐观锁检查）
        boolean deductSuccess = accountService.deductBalanceWithVersion(
            account.getAccountId(), amount, request.getOrderNo(), currentVersion
        );
        if (!deductSuccess) {
            throw new SmartException("余额扣减失败，可能数据已被修改");
        }

        // 3. 创建消费记录
        ConsumeRecordEntity record = createConsumeRecordWithVersion(account, request, balanceBefore, balanceAfter, currentVersion);
        try {
            int insertResult = consumeRecordDao.insert(record);
            if (insertResult <= 0) {
                throw new SmartException("消费记录创建失败");
            }
        } catch (Exception e) {
            // 记录创建失败，补偿余额
            log.error("消费记录创建失败，进行余额补偿: orderNo={}", request.getOrderNo(), e);
            accountService.addBalance(account.getAccountId(), amount, "COMPENSATE_" + request.getOrderNo());
            throw new SmartException("消费记录创建失败");
        }

        // 4. 更新数据版本号
        long newVersion = consistencyManager.getDataVersion("account:" + account.getAccountId());

        // 5. 安全检测：异常操作检测
        try {
            AccountSecurityManager.RiskDetectionResult riskResult =
                securityManager.detectAnomalousOperation(account.getPersonId(), record);

            if (riskResult.getRiskLevel() == AccountSecurityManager.RiskLevel.HIGH) {
                log.warn("检测到高风险操作，建议人工审核: personId={}, orderNo={}, riskScore={}",
                        account.getPersonId(), request.getOrderNo(), riskResult.getRiskScore());
                // 这里可以触发人工审核流程或发送告警
            }
        } catch (Exception e) {
            log.warn("异常操作检测失败，不影响主流程: personId={}", account.getPersonId(), e);
        }

        // 6. 返回成功结果
        ConsumeResult result = ConsumeResult.success(amount);
        result.setOrderNo(request.getOrderNo());
        result.setConsumeTime(LocalDateTime.now());
        result.setCompleteTime(LocalDateTime.now());
        result.setBalanceBefore(balanceBefore);
        result.setBalanceAfter(balanceAfter);
        result.setConsumptionMode(request.getConsumptionMode());
        result.setPayMethod(request.getPayMethod());
        result.setStatus("SUCCESS");
        result.setVersion(newVersion);

        log.info("一致性保障消费交易完成: orderNo={}, version={}", request.getOrderNo(), newVersion);
        return result;
    }

    /**
     * 执行核心消费交易（原子性操作）- 保留原方法以兼容旧代码
     */
    @Transactional(rollbackFor = Exception.class)
    private ConsumeResult executeConsumeTransaction(AccountEntity account, ConsumeRequest request) {
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal amount = request.getAmount();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        // 1. 扣减账户余额
        boolean deductSuccess = accountService.deductBalance(account.getAccountId(), amount, request.getOrderNo());
        if (!deductSuccess) {
            throw new SmartException("余额扣减失败");
        }

        // 2. 创建消费记录
        ConsumeRecordEntity record = createConsumeRecord(account, request, balanceBefore, balanceAfter);
        try {
            int insertResult = consumeRecordDao.insert(record);
            if (insertResult <= 0) {
                throw new SmartException("消费记录创建失败");
            }
        } catch (Exception e) {
            // 记录创建失败，补偿余额
            log.error("消费记录创建失败，进行余额补偿: orderNo={}", request.getOrderNo(), e);
            accountService.addBalance(account.getAccountId(), amount, "COMPENSATE_" + request.getOrderNo());
            throw new SmartException("消费记录创建失败");
        }

        // 3. 安全检测：异常操作检测
        try {
            AccountSecurityManager.RiskDetectionResult riskResult =
                securityManager.detectAnomalousOperation(account.getPersonId(), record);

            if (riskResult.getRiskLevel() == AccountSecurityManager.RiskLevel.HIGH) {
                log.warn("检测到高风险操作，建议人工审核: personId={}, orderNo={}, riskScore={}",
                        account.getPersonId(), request.getOrderNo(), riskResult.getRiskScore());
                // 这里可以触发人工审核流程或发送告警
            }
        } catch (Exception e) {
            log.warn("异常操作检测失败，不影响主流程: personId={}", account.getPersonId(), e);
        }

        // 4. 更新缓存（如果需要）
        // cacheManager.updateAccountBalance(account.getAccountId(), balanceAfter);

        // 5. 发送通知和事件
        // sendConsumeNotification(record);

        // 返回成功结果
        ConsumeResult result = ConsumeResult.success(amount);
        result.setOrderNo(request.getOrderNo());
        result.setConsumeTime(LocalDateTime.now());
        result.setCompleteTime(LocalDateTime.now());
        result.setBalanceBefore(balanceBefore);
        result.setBalanceAfter(balanceAfter);
        result.setConsumptionMode(request.getConsumptionMode());
        result.setPayMethod(request.getPayMethod());
        result.setStatus("SUCCESS");

        return result;
    }

    /**
     * 创建带版本控制的消费记录
     */
    private ConsumeRecordEntity createConsumeRecordWithVersion(AccountEntity account, ConsumeRequest request,
                                                            BigDecimal balanceBefore, BigDecimal balanceAfter, long version) {
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setPersonId(request.getPersonId());
        record.setPersonName(request.getPersonName());
        record.setAmount(request.getAmount());
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setCurrency(request.getCurrency());
        record.setPayMethod(request.getPayMethod());
        record.setOrderNo(request.getOrderNo());
        record.setStatus("SUCCESS");
        record.setPayTime(LocalDateTime.now());
        record.setDeviceId(request.getDeviceId());
        record.setDeviceName(request.getDeviceName());
        record.setConsumptionMode(request.getConsumptionMode());
        record.setRegionId(request.getRegionId());
        record.setRegionName(request.getRegionName());
        record.setAccountType(account.getAccountType());
        record.setRemark(request.getRemark());
        record.setModeConfig(request.getModeConfig());
        record.setExtendData(request.getExtendData());
        record.setClientIp(request.getClientIp());

        // 添加数据版本信息到扩展字段
        String versionInfo = String.format("{\"dataVersion\":%d,\"processTime\":\"%s\"}",
            version, LocalDateTime.now());
        record.setExtendData(record.getExtendData() != null ?
            record.getExtendData() + "," + versionInfo : versionInfo);

        return record;
    }

    /**
     * 创建消费记录 - 保留原方法以兼容旧代码
     */
    private ConsumeRecordEntity createConsumeRecord(AccountEntity account, ConsumeRequest request,
                                                  BigDecimal balanceBefore, BigDecimal balanceAfter) {
        ConsumeRecordEntity record = new ConsumeRecordEntity();
        record.setPersonId(request.getPersonId());
        record.setPersonName(request.getPersonName());
        record.setAmount(request.getAmount());
        record.setBalanceBefore(balanceBefore);
        record.setBalanceAfter(balanceAfter);
        record.setCurrency(request.getCurrency());
        record.setPayMethod(request.getPayMethod());
        record.setOrderNo(request.getOrderNo());
        record.setStatus("SUCCESS");
        record.setPayTime(LocalDateTime.now());
        record.setDeviceId(request.getDeviceId());
        record.setDeviceName(request.getDeviceName());
        record.setConsumptionMode(request.getConsumptionMode());
        record.setRegionId(request.getRegionId());
        record.setRegionName(request.getRegionName());
        record.setAccountType(account.getAccountType());
        record.setRemark(request.getRemark());
        record.setModeConfig(request.getModeConfig());
        record.setExtendData(request.getExtendData());
        record.setClientIp(request.getClientIp());

        return record;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 格式: CONSUME + 年月日时分秒 + 8位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%08d", (int)(Math.random() * 100000000));
        return "CONSUME" + timestamp + random;
    }

    @Override
    public ConsumeResult queryConsumeResult(String orderNo) {
        try {
            if (!StringUtils.hasText(orderNo)) {
                return ConsumeResult.failure("INVALID_PARAM", "订单号不能为空");
            }

            ConsumeRecordEntity record = consumeRecordDao.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ConsumeRecordEntity>()
                    .eq("order_no", orderNo)
            );

            if (record == null) {
                return ConsumeResult.failure("NOT_FOUND", "订单不存在");
            }

            return convertToResult(record);
        } catch (Exception e) {
            log.error("查询消费结果异常: orderNo={}", orderNo, e);
            return ConsumeResult.failure("SYSTEM_ERROR", "查询异常，请稍后重试");
        }
    }

    @Override
    public boolean checkConsumePermission(Long personId, Long deviceId, String regionId) {
        // 使用新的权限验证器
        ConsumePermissionValidator.ConsumePermissionResult result =
                permissionValidator.validateConsumePermission(personId, deviceId, regionId);
        return result.isSuccess();
    }

    @Override
    public boolean validateConsumeLimit(Long personId, BigDecimal amount) {
        try {
            AccountEntity account = accountService.getByPersonId(personId);
            if (account == null) {
                log.warn("账户不存在，无法验证限额: personId={}", personId);
                return false;
            }

            // 1. 检查单次限额
            if (account.getSingleLimit() != null && amount.compareTo(account.getSingleLimit()) > 0) {
                log.warn("超出单次消费限额: personId={}, amount={}, limit={}",
                    personId, amount, account.getSingleLimit());
                return false;
            }

            // 2. 检查日度限额
            if (account.getDailyLimit() != null) {
                BigDecimal todayAmount = accountService.getTodayConsumeAmount(personId);
                if (todayAmount.add(amount).compareTo(account.getDailyLimit()) > 0) {
                    log.warn("超出日度消费限额: personId={}, todayAmount={}, amount={}, limit={}",
                        personId, todayAmount, amount, account.getDailyLimit());
                    return false;
                }
            }

            // 3. 检查月度限额
            if (account.getMonthlyLimit() != null) {
                BigDecimal monthlyAmount = accountService.getMonthlyConsumeAmount(personId);
                if (monthlyAmount.add(amount).compareTo(account.getMonthlyLimit()) > 0) {
                    log.warn("超出月度消费限额: personId={}, monthlyAmount={}, amount={}, limit={}",
                        personId, monthlyAmount, amount, account.getMonthlyLimit());
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            log.error("限额检查异常: personId={}, amount={}", personId, amount, e);
            return false;
        }
    }
}