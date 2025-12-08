# 消费管理服务专家技能
## Consume Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区消费管理业务专家，精通账户管理、消费结算、补贴发放、数据分析、财务对账等核心消费业务

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: 消费系统开发、支付集成、账户管理、结算对账、数据分析
**📊 技能覆盖**: 账户管理 | 消费结算 | 支付集成 | 补贴发放 | 数据分析 | 财务对账 | 风控管理

---

## 📋 技能概述

### **核心专长**
- **多账户体系**: 个人账户、企业账户、临时账户、虚拟账户管理
- **支付方式集成**: 刷脸支付、刷卡支付、NFC支付、扫码支付、在线支付
- **实时结算引擎**: 高并发消费处理、实时余额更新、交易流水管理
- **补贴发放系统**: 自动补贴、手动补贴、批量发放、补贴策略配置
- **数据分析平台**: 消费趋势分析、商户数据分析、用户行为分析
- **财务对账体系**: 自动对账、异常交易处理、财务报表生成

### **解决能力**
- **消费系统开发**: 完整的智慧园区消费管理系统实现
- **支付渠道集成**: 多种支付方式的统一接入和管理
- **账户体系设计**: 灵活的多层级账户管理和权限控制
- **实时结算优化**: 高性能、高可用的实时结算引擎
- **数据分析洞察**: 深度的消费数据分析和商业智能

---

## 🎯 业务场景覆盖

### 💳 账户管理体系
```java
// 多账户体系管理核心流程
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountManager accountManager;

    @Resource
    private AccountDao accountDao;

    @Resource
    private AccountBalanceDao balanceDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountVO createAccount(CreateAccountRequestDTO request) {
        try {
            // 1. 验证账户类型和规则
            validateAccountType(request.getAccountType(), request.getUserId());

            // 2. 检查用户是否已有同类型账户
            validateUniqueAccount(request.getUserId(), request.getAccountType());

            // 3. 创建账户
            AccountEntity account = convertToAccountEntity(request);
            account.setAccountNo(generateAccountNo());
            account.setStatus(AccountStatusEnum.ACTIVE.getCode());
            account.setCreateTime(LocalDateTime.now());

            accountDao.insert(account);

            // 4. 创建账户余额记录
            AccountBalanceEntity balance = AccountBalanceEntity.builder()
                .accountId(account.getAccountId())
                .accountType(account.getAccountType())
                .availableBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .totalBalance(BigDecimal.ZERO)
                .lastUpdateTime(LocalDateTime.now())
                .build();

            balanceDao.insert(balance);

            // 5. 初始化账户配置
            initializeAccountConfig(account.getAccountId(), request);

            // 6. 发送账户创建事件
            publishAccountCreatedEvent(account);

            return convertToAccountVO(account);

        } catch (Exception e) {
            log.error("账户创建失败: userId={}, accountType={}", request.getUserId(), request.getAccountType(), e);
            throw new BusinessException("ACCOUNT_CREATE_FAILED", "账户创建失败", e);
        }
    }

    @Override
    public AccountDetailVO getAccountDetail(Long accountId) {
        // 获取账户信息
        AccountEntity account = accountDao.selectById(accountId);
        if (account == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        }

        // 获取账户余额
        AccountBalanceEntity balance = balanceDao.selectByAccountId(accountId);

        // 获取账户统计信息
        AccountStatistics statistics = accountManager.getAccountStatistics(accountId);

        // 获取账户交易记录（最近10条）
        List<ConsumeRecordEntity> recentRecords = accountManager.getRecentConsumeRecords(accountId, 10);

        return AccountDetailVO.builder()
            .account(convertToAccountVO(account))
            .balance(convertToBalanceVO(balance))
            .statistics(convertToStatisticsVO(statistics))
            .recentRecords(convertToRecordVOList(recentRecords))
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rechargeAccount(RechargeAccountRequestDTO request) {
        try {
            // 1. 验证账户状态
            AccountEntity account = validateAccountStatus(request.getAccountId());

            // 2. 验证充值金额
            validateRechargeAmount(request.getAmount());

            // 3. 执行充值操作
            accountManager.rechargeAccount(account, request);

            // 4. 记录充值流水
            recordRechargeTransaction(account, request);

            // 5. 发送充值成功通知
            sendRechargeNotification(account, request);

        } catch (Exception e) {
            log.error("账户充值失败: accountId={}, amount={}", request.getAccountId(), request.getAmount(), e);
            throw new BusinessException("ACCOUNT_RECHARGE_FAILED", "账户充值失败", e);
        }
    }

    private void validateAccountType(String accountType, Long userId) {
        AccountTypeEnum type = AccountTypeEnum.fromCode(accountType);
        if (type == null) {
            throw new BusinessException("INVALID_ACCOUNT_TYPE", "无效的账户类型");
        }

        // 验证个人账户规则
        if (type == AccountTypeEnum.PERSONAL) {
            if (accountDao.existsByUserIdAndType(userId, accountType)) {
                throw new BusinessException("PERSONAL_ACCOUNT_EXISTS", "个人账户已存在");
            }
        }

        // 验证企业账户规则
        if (type == AccountTypeEnum.ENTERPRISE) {
            if (!hasEnterpriseAccountPermission(userId)) {
                throw new BusinessException("NO_ENTERPRISE_PERMISSION", "无权限创建企业账户");
            }
        }
    }
}
```

### 🛒 实时消费结算
```java
// 实时消费结算引擎
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeManager consumeManager;

    @Resource
    private AccountDao accountDao;

    @Resource
    private MerchantDao merchantDao;

    @Resource
    private PaymentChannelManager paymentChannelManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConsumeResultVO consume(ConsumeRequestDTO request) {
        try {
            // 1. 验证消费请求
            ConsumeValidationResult validation = validateConsumeRequest(request);
            if (!validation.isValid()) {
                return ConsumeResultVO.failure(validation.getErrorMessage());
            }

            // 2. 获取账户信息
            AccountEntity account = getAccountForConsume(request);

            // 3. 获取商户信息
            MerchantEntity merchant = getMerchantForConsume(request.getMerchantId());

            // 4. 执行消费结算
            ConsumeSettlementResult settlementResult = consumeManager.executeSettlement(account, merchant, request);

            // 5. 处理支付方式
            PaymentResult paymentResult = processPayment(settlementResult, request);

            // 6. 生成消费结果
            ConsumeResultVO result = generateConsumeResult(settlementResult, paymentResult);

            // 7. 异步处理后续业务
            handlePostConsumeTasks(settlementResult, request);

            return result;

        } catch (InsufficientBalanceException e) {
            log.warn("账户余额不足: accountId={}, amount={}", request.getAccountId(), request.getAmount());
            return ConsumeResultVO.insufficientBalance("账户余额不足");

        } catch (Exception e) {
            log.error("消费处理失败: accountId={}, amount={}", request.getAccountId(), request.getAmount(), e);
            throw new BusinessException("CONSUME_FAILED", "消费处理失败", e);
        }
    }

    private ConsumeValidationResult validateConsumeRequest(ConsumeRequestDTO request) {
        // 验证基础参数
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ConsumeValidationResult.invalid("消费金额必须大于0");
        }

        if (StringUtils.isBlank(request.getMerchantId())) {
            return ConsumeValidationResult.invalid("商户ID不能为空");
        }

        if (StringUtils.isBlank(request.getPaymentMethod())) {
            return ConsumeValidationResult.invalid("支付方式不能为空");
        }

        // 验证消费限额
        if (request.getAmount().compareTo(new BigDecimal("50000")) > 0) {
            return ConsumeValidationResult.invalid("单笔消费金额不能超过50000元");
        }

        // 验证时间窗口（防止重复提交）
        if (isDuplicateRequest(request)) {
            return ConsumeValidationResult.invalid("请勿重复提交消费请求");
        }

        return ConsumeValidationResult.valid();
    }

    private AccountEntity getAccountForConsume(ConsumeRequestDTO request) {
        AccountEntity account = accountDao.selectById(request.getAccountId());
        if (account == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        }

        if (account.getStatus() != AccountStatusEnum.ACTIVE.getCode()) {
            throw new BusinessException("ACCOUNT_INACTIVE", "账户已停用");
        }

        return account;
    }

    private PaymentResult processPayment(ConsumeSettlementResult settlementResult, ConsumeRequestDTO request) {
        PaymentMethodEnum paymentMethod = PaymentMethodEnum.fromCode(request.getPaymentMethod());

        switch (paymentMethod) {
            case FACE_RECOGNITION:
                return processFaceRecognitionPayment(settlementResult, request);
            case CARD_SWIPE:
                return processCardSwipePayment(settlementResult, request);
            case NFC_PAYMENT:
                return processNfcPayment(settlementResult, request);
            case QR_CODE:
                return processQrCodePayment(settlementResult, request);
            case ONLINE_PAYMENT:
                return processOnlinePayment(settlementResult, request);
            default:
                throw new BusinessException("UNSUPPORTED_PAYMENT_METHOD", "不支持的支付方式");
        }
    }

    private PaymentResult processFaceRecognitionPayment(ConsumeSettlementResult settlementResult, ConsumeRequestDTO request) {
        try {
            // 1. 人脸识别验证
            FaceRecognitionResult faceResult = paymentChannelManager.verifyFaceRecognition(
                request.getFaceData(), request.getAccountId());

            if (!faceResult.isSuccess()) {
                return PaymentResult.failure("人脸识别验证失败: " + faceResult.getErrorMessage());
            }

            // 2. 活体检测
            LivenessDetectionResult livenessResult = paymentChannelManager.verifyLiveness(
                request.getLivenessData());

            if (!livenessResult.isSuccess()) {
                return PaymentResult.failure("活体检测失败: " + livenessResult.getErrorMessage());
            }

            // 3. 扣款操作
            DeductionResult deductionResult = accountManager.deductBalance(
                settlementResult.getAccount().getAccountId(),
                settlementResult.getSettlementAmount(),
                settlementResult.getConsumeRecord().getRecordId(),
                "人脸识别消费"
            );

            if (!deductionResult.isSuccess()) {
                return PaymentResult.failure("扣款失败: " + deductionResult.getErrorMessage());
            }

            return PaymentResult.success("人脸识别支付成功", deductionResult.getTransactionId());

        } catch (Exception e) {
            log.error("人脸识别支付处理失败", e);
            return PaymentResult.failure("人脸识别支付处理失败");
        }
    }

    private void handlePostConsumeTasks(ConsumeSettlementResult settlementResult, ConsumeRequestDTO request) {
        // 异步处理后续业务任务
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 更新商户统计数据
                updateMerchantStatistics(settlementResult);

                // 2. 处理补贴抵扣
                handleSubsidyDeduction(settlementResult);

                // 3. 发送消费通知
                sendConsumeNotification(settlementResult, request);

                // 4. 更新用户消费习惯数据
                updateUserConsumptionProfile(settlementResult);

                // 5. 实时风控检查
                performRealTimeRiskCheck(settlementResult);

            } catch (Exception e) {
                log.error("消费后续任务处理失败: recordId={}", settlementResult.getConsumeRecord().getRecordId(), e);
            }
        });
    }
}
```

### 🎁 补贴发放系统
```java
// 补贴发放和管理
@Service
@Transactional(rollbackFor = Exception.class)
public class SubsidyServiceImpl implements SubsidyService {

    @Resource
    private SubsidyManager subsidyManager;

    @Resource
    private SubsidyRuleDao subsidyRuleDao;

    @Resource
    private AccountDao accountDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubsidyDistributionVO distributeSubsidy(SubsidyDistributionRequestDTO request) {
        try {
            // 1. 验证补贴规则
            SubsidyRuleEntity rule = validateSubsidyRule(request.getRuleId());

            // 2. 获取目标账户列表
            List<AccountEntity> targetAccounts = getTargetAccounts(request, rule);

            // 3. 执行补贴发放
            SubsidyDistributionResult distributionResult = subsidyManager.distributeSubsidy(
                rule, targetAccounts, request);

            // 4. 生成发放报告
            SubsidyDistributionVO report = generateDistributionReport(distributionResult);

            // 5. 发送补贴通知
            sendSubsidyNotifications(distributionResult);

            return report;

        } catch (Exception e) {
            log.error("补贴发放失败: ruleId={}", request.getRuleId(), e);
            throw new BusinessException("SUBSIDY_DISTRIBUTION_FAILED", "补贴发放失败", e);
        }
    }

    @Override
    public List<SubsidyRecordVO> getSubsidyRecords(GetSubsidyRecordsRequestDTO request) {
        // 构建查询条件
        LambdaQueryWrapper<SubsidyRecordEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getAccountId() != null, SubsidyRecordEntity::getAccountId, request.getAccountId())
               .eq(request.getRuleId() != null, SubsidyRecordEntity::getRuleId, request.getRuleId())
               .between(request.getStartDate() != null && request.getEndDate() != null,
                       SubsidyRecordEntity::getDistributionTime, request.getStartDate(), request.getEndDate())
               .orderByDesc(SubsidyRecordEntity::getDistributionTime);

        // 分页查询
        Page<SubsidyRecordEntity> page = subsidyRecordDao.selectPage(
            new Page<>(request.getPageNum(), request.getPageSize()), wrapper);

        return page.getRecords().stream()
            .map(this::convertToSubsidyRecordVO)
            .collect(Collectors.toList());
    }

    private SubsidyRuleEntity validateSubsidyRule(Long ruleId) {
        SubsidyRuleEntity rule = subsidyRuleDao.selectById(ruleId);
        if (rule == null) {
            throw new BusinessException("SUBSIDY_RULE_NOT_FOUND", "补贴规则不存在");
        }

        if (rule.getStatus() != SubsidyRuleStatusEnum.ACTIVE.getCode()) {
            throw new BusinessException("SUBSIDY_RULE_INACTIVE", "补贴规则已停用");
        }

        if (rule.getDistributionType() == SubsidyDistributionTypeEnum.MANUAL.getCode()) {
            // 手动发放规则需要验证发放时间窗口
            validateManualDistributionTimeWindow(rule);
        }

        return rule;
    }

    private List<AccountEntity> getTargetAccounts(SubsidyDistributionRequestDTO request, SubsidyRuleEntity rule) {
        if (request.getAccountIds() != null && !request.getAccountIds().isEmpty()) {
            // 指定账户发放
            return accountDao.selectBatchIds(request.getAccountIds());
        }

        // 根据规则自动筛选账户
        return subsidyManager.selectAccountsByRule(rule);
    }
}
```

---

## 🏗️ 架构设计规范

### 四层架构实现

#### Controller层 - 接口控制层
```java
@RestController
@RequestMapping("/api/v1/consume/account")
@Tag(name = "账户管理")
@Validated
public class AccountController {

    @Resource
    private AccountService accountService;

    @PostMapping("/create")
    @Operation(summary = "创建账户")
    public ResponseDTO<AccountVO> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        AccountVO account = accountService.createAccount(request);
        return ResponseDTO.ok(account);
    }

    @GetMapping("/detail/{accountId}")
    @Operation(summary = "获取账户详情")
    public ResponseDTO<AccountDetailVO> getAccountDetail(@PathVariable Long accountId) {
        AccountDetailVO detail = accountService.getAccountDetail(accountId);
        return ResponseDTO.ok(detail);
    }

    @PostMapping("/recharge")
    @Operation(summary = "账户充值")
    public ResponseDTO<Void> rechargeAccount(@Valid @RequestBody RechargeAccountRequestDTO request) {
        accountService.rechargeAccount(request);
        return ResponseDTO.ok();
    }
}

@RestController
@RequestMapping("/api/v1/consume/payment")
@Tag(name = "消费支付")
@Validated
public class PaymentController {

    @Resource
    private ConsumeService consumeService;

    @PostMapping("/consume")
    @Operation(summary = "消费支付")
    public ResponseDTO<ConsumeResultVO> consume(@Valid @RequestBody ConsumeRequestDTO request) {
        ConsumeResultVO result = consumeService.consume(request);
        return ResponseDTO.ok(result);
    }

    @PostMapping("/refund")
    @Operation(summary = "消费退款")
    public ResponseDTO<RefundResultVO> refund(@Valid @RequestBody RefundRequestDTO request) {
        RefundResultVO result = consumeService.refund(request);
        return ResponseDTO.ok(result);
    }
}
```

#### Service层 - 核心业务层
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeManager consumeManager;

    @Override
    public ConsumeResultVO consume(ConsumeRequestDTO request) {
        // 业务规则验证
        validateConsumeRequest(request);

        // 核心业务逻辑
        return consumeManager.executeConsume(request);
    }

    private void validateConsumeRequest(ConsumeRequestDTO request) {
        // 验证账户状态
        AccountEntity account = accountDao.selectById(request.getAccountId());
        if (account == null || account.getStatus() != AccountStatusEnum.ACTIVE.getCode()) {
            throw new BusinessException("ACCOUNT_INVALID", "账户状态异常");
        }

        // 验证商户状态
        MerchantEntity merchant = merchantDao.selectById(request.getMerchantId());
        if (merchant == null || merchant.getStatus() != MerchantStatusEnum.ACTIVE.getCode()) {
            throw new BusinessException("MERCHANT_INVALID", "商户状态异常");
        }

        // 验证消费金额
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "消费金额无效");
        }
    }
}
```

#### Manager层 - 复杂流程管理层
```java
// ✅ 正确：Manager类为纯Java类，不使用Spring注解
public class ConsumeManager {

    private final AccountDao accountDao;
    private final MerchantDao merchantDao;
    private final ConsumeRecordDao consumeRecordDao;
    private final AccountBalanceDao accountBalanceDao;
    private final GatewayServiceClient gatewayServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    // 构造函数注入依赖
    public ConsumeManager(AccountDao accountDao, MerchantDao merchantDao,
                        ConsumeRecordDao consumeRecordDao, AccountBalanceDao accountBalanceDao,
                        GatewayServiceClient gatewayServiceClient,
                        RedisTemplate<String, Object> redisTemplate,
                        RabbitTemplate rabbitTemplate) {
        this.accountDao = accountDao;
        this.merchantDao = merchantDao;
        this.consumeRecordDao = consumeRecordDao;
        this.accountBalanceDao = accountBalanceDao;
        this.gatewayServiceClient = gatewayServiceClient;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public ConsumeSettlementResult executeSettlement(AccountEntity account, MerchantEntity merchant, ConsumeRequestDTO request) {
        try {
            // 1. 检查账户余额
            AccountBalanceEntity balance = checkAccountBalance(account.getAccountId(), request.getAmount());

            // 2. 计算结算金额（考虑折扣、补贴等）
            SettlementAmountResult amountResult = calculateSettlementAmount(account, merchant, request);

            // 3. 冻结账户余额
            freezeAccountBalance(account.getAccountId(), amountResult.getPayableAmount());

            // 4. 创建消费记录
            ConsumeRecordEntity consumeRecord = createConsumeRecord(account, merchant, request, amountResult);

            // 5. 保存消费记录
            consumeRecordDao.insert(consumeRecord);

            // 6. 实际扣款（在支付成功后调用）
            // deductAccountBalance(account.getAccountId(), amountResult.getPayableAmount(), consumeRecord.getRecordId());

            return ConsumeSettlementResult.builder()
                .account(account)
                .merchant(merchant)
                .consumeRecord(consumeRecord)
                .settlementAmount(amountResult)
                .balance(balance)
                .build();

        } catch (Exception e) {
            log.error("消费结算失败: accountId={}, merchantId={}, amount={}",
                account.getAccountId(), merchant.getMerchantId(), request.getAmount(), e);
            throw new BusinessException("SETTLEMENT_FAILED", "消费结算失败", e);
        }
    }

    private AccountBalanceEntity checkAccountBalance(Long accountId, BigDecimal amount) {
        AccountBalanceEntity balance = accountBalanceDao.selectByAccountId(accountId);
        if (balance == null) {
            throw new BusinessException("BALANCE_NOT_FOUND", "账户余额信息不存在");
        }

        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("账户余额不足");
        }

        return balance;
    }

    private SettlementAmountResult calculateSettlementAmount(AccountEntity account, MerchantEntity merchant, ConsumeRequestDTO request) {
        BigDecimal originalAmount = request.getAmount();
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal subsidyAmount = BigDecimal.ZERO;

        // 1. 计算商户折扣
        MerchantDiscountResult discountResult = calculateMerchantDiscount(merchant, originalAmount, account);
        discountAmount = discountResult.getDiscountAmount();

        BigDecimal afterDiscountAmount = originalAmount.subtract(discountAmount);

        // 2. 计算用户补贴
        UserSubsidyResult subsidyResult = calculateUserSubsidy(account, afterDiscountAmount, merchant);
        subsidyAmount = subsidyResult.getSubsidyAmount();

        BigDecimal payableAmount = afterDiscountAmount.subtract(subsidyAmount);

        // 3. 确保支付金额不为负数
        if (payableAmount.compareTo(BigDecimal.ZERO) < 0) {
            payableAmount = BigDecimal.ZERO;
        }

        return SettlementAmountResult.builder()
            .originalAmount(originalAmount)
            .discountAmount(discountAmount)
            .subsidyAmount(subsidyAmount)
            .payableAmount(payableAmount)
            .discountRate(discountResult.getDiscountRate())
            .subsidyRate(subidyResult.getSubsidyRate())
            .build();
    }

    private MerchantDiscountResult calculateMerchantDiscount(MerchantEntity merchant, BigDecimal amount, AccountEntity account) {
        try {
            // 调用商户服务获取折扣信息
            ResponseDTO<MerchantDiscountInfo> response = gatewayServiceClient.callMerchantService(
                "/api/v1/merchant/" + merchant.getMerchantId() + "/discount",
                HttpMethod.GET,
                null,
                new TypeReference<ResponseDTO<MerchantDiscountInfo>>() {}
            );

            if (response.isSuccess() && response.getData() != null) {
                MerchantDiscountInfo discountInfo = response.getData();

                // 计算折扣金额
                BigDecimal discountAmount = calculateDiscountAmount(amount, discountInfo);

                return MerchantDiscountResult.builder()
                    .discountAmount(discountAmount)
                    .discountRate(discountInfo.getDiscountRate())
                    .discountRule(discountInfo.getRuleName())
                    .build();
            }

        } catch (Exception e) {
            log.warn("获取商户折扣信息失败: merchantId={}", merchant.getMerchantId(), e);
        }

        // 默认无折扣
        return MerchantDiscountResult.builder()
            .discountAmount(BigDecimal.ZERO)
            .discountRate(BigDecimal.ZERO)
            .build();
    }

    private UserSubsidyResult calculateUserSubsidy(AccountEntity account, BigDecimal amount, MerchantEntity merchant) {
        try {
            // 调用补贴服务获取补贴信息
            ResponseDTO<UserSubsidyInfo> response = gatewayServiceClient.callSubsidyService(
                "/api/v1/subsidy/user/" + account.getUserId() + "/available",
                HttpMethod.GET,
                null,
                new TypeReference<ResponseDTO<UserSubsidyInfo>>() {}
            );

            if (response.isSuccess() && response.getData() != null) {
                UserSubsidyInfo subsidyInfo = response.getData();

                // 计算补贴金额
                BigDecimal subsidyAmount = calculateSubsidyAmount(amount, subsidyInfo);

                return UserSubsidyResult.builder()
                    .subsidyAmount(subsidyAmount)
                    .subsidyRate(subsidyInfo.getSubsidyRate())
                    .subsidyRule(subsidyInfo.getRuleName())
                    .build();
            }

        } catch (Exception e) {
            log.warn("获取用户补贴信息失败: userId={}", account.getUserId(), e);
        }

        // 默认无补贴
        return UserSubsidyResult.builder()
            .subsidyAmount(BigDecimal.ZERO)
            .subsidyRate(BigDecimal.ZERO)
            .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public DeductionResult deductBalance(Long accountId, BigDecimal amount, String recordId, String remark) {
        try {
            // 1. 获取当前余额
            AccountBalanceEntity balance = accountBalanceDao.selectByAccountIdForUpdate(accountId);
            if (balance == null) {
                throw new BusinessException("BALANCE_NOT_FOUND", "账户余额不存在");
            }

            // 2. 检查余额是否充足
            if (balance.getAvailableBalance().compareTo(amount) < 0) {
                throw new BusinessException("INSUFFICIENT_BALANCE", "账户余额不足");
            }

            // 3. 扣减余额
            BigDecimal newAvailableBalance = balance.getAvailableBalance().subtract(amount);
            BigDecimal newTotalBalance = balance.getTotalBalance().subtract(amount);

            balance.setAvailableBalance(newAvailableBalance);
            balance.setTotalBalance(newTotalBalance);
            balance.setLastUpdateTime(LocalDateTime.now());

            int updated = accountBalanceDao.updateById(balance);
            if (updated != 1) {
                throw new BusinessException("BALANCE_UPDATE_FAILED", "余额更新失败");
            }

            // 4. 记录余额变动流水
            BalanceChangeRecordEntity changeRecord = BalanceChangeRecordEntity.builder()
                .accountId(accountId)
                .changeType(BalanceChangeTypeEnum.DEDUCT.getCode())
                .changeAmount(amount.negate())
                .beforeBalance(balance.getAvailableBalance().add(amount))
                .afterBalance(newAvailableBalance)
                .relatedRecordId(recordId)
                .remark(remark)
                .createTime(LocalDateTime.now())
                .build();

            balanceChangeRecordDao.insert(changeRecord);

            return DeductionResult.success(changeRecord.getRecordId());

        } catch (Exception e) {
            log.error("余额扣减失败: accountId={}, amount={}", accountId, amount, e);
            throw new BusinessException("DEDUCT_FAILED", "余额扣减失败", e);
        }
    }
}
```

#### DAO层 - 数据访问层
```java
@Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    @Transactional(readOnly = true)
    AccountEntity selectByAccountNo(@Param("accountNo") String accountNo);

    @Transactional(readOnly = true)
    List<AccountEntity> selectByUserId(@Param("userId") Long userId);

    @Transactional(readOnly = true)
    boolean existsByUserIdAndType(@Param("userId") Long userId, @Param("accountType") String accountType);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("accountId") Long accountId, @Param("status") Integer status);

    @Select("SELECT * FROM t_consume_account WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<AccountEntity> selectRecentActiveAccounts(@Param("limit") int limit);

    @Transactional(readOnly = true)
    List<AccountEntity> selectByAccountType(@Param("accountType") String accountType);
}

@Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {

    @Transactional(readOnly = true)
    List<ConsumeRecordEntity> selectByAccountId(@Param("accountId") Long accountId,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    @Transactional(readOnly = true)
    List<ConsumeRecordEntity> selectByMerchantId(@Param("merchantId") Long merchantId,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    @Transactional(readOnly = true)
    BigDecimal selectTotalAmountByAccountId(@Param("accountId") Long accountId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Transactional(rollbackFor = Exception.class)
    int updateRefundStatus(@Param("recordId") Long recordId, @Param("refundStatus") Integer refundStatus);

    @Select("SELECT * FROM t_consume_record WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<ConsumeRecordEntity> selectRecentRecords(@Param("limit") int limit);
}
```

---

## 📊 技能质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **交易成功率** | ≥99.9% | 消费交易成功比例 | 交易成功率监控 |
| **支付响应时间** | ≤200ms | 支付处理响应时间 | 支付性能监控 |
| **账户余额准确率** | 100% | 账户余额计算准确性 | 余额一致性检查 |
| **结算处理效率** | ≤500ms | 实时结算处理时间 | 结算性能测试 |
| **系统可用性** | ≥99.95% | 消费系统可用性 | 系统可用性监控 |

### 性能指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **并发交易处理能力** | ≥10000 TPS | 同时处理交易数 | 并发性能测试 |
| **账户查询响应时间** | ≤100ms | 账户信息查询时间 | 查询性能测试 |
| **补贴发放处理时间** | ≤2s | 批量补贴发放时间 | 发放性能测试 |
| **对账处理效率** | ≤10分钟 | 日终对账处理时间 | 对账效率测试 |

### 安全指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **支付安全等级** | 金融级 | 支付安全防护能力 | 安全评估测试 |
| **数据加密覆盖率** | 100% | 敏感数据加密比例 | 数据安全检查 |
| **风控拦截准确率** | ≥95% | 风险交易拦截准确率 | 风控效果分析 |
| **用户资金安全** | 100% | 用户资金安全保障 | 资金安全审计 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新技能创建

---

## 🛠️ 开发规范和最佳实践

### 资金安全最佳实践
```java
// ✅ 正确的资金安全处理
@Service
public class AccountServiceImpl implements AccountService {

    @Transactional(rollbackFor = Exception.class)
    public void transferBalance(TransferRequestDTO request) {
        try {
            // 1. 验证转出账户
            AccountEntity fromAccount = validateTransferAccount(request.getFromAccountId());

            // 2. 验证转入账户
            AccountEntity toAccount = validateTransferAccount(request.getToAccountId());

            // 3. 检查余额（使用悲观锁）
            AccountBalanceEntity fromBalance = accountBalanceDao.selectByAccountIdForUpdate(
                request.getFromAccountId());

            if (fromBalance.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足");
            }

            // 4. 执行转出（先扣款）
            accountManager.deductBalance(request.getFromAccountId(), request.getAmount(),
                generateRecordId(), "账户转账转出");

            // 5. 执行转入（后增加）
            accountManager.addBalance(request.getToAccountId(), request.getAmount(),
                generateRecordId(), "账户转账转入");

            // 6. 记录转账流水
            recordTransferFlow(request);

        } catch (Exception e) {
            log.error("账户转账失败: from={}, to={}, amount={}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount(), e);
            throw new BusinessException("TRANSFER_FAILED", "账户转账失败", e);
        }
    }

    // ✅ 正确的余额检查（使用悲观锁）
    private AccountBalanceEntity checkBalanceWithLock(Long accountId, BigDecimal amount) {
        AccountBalanceEntity balance = accountBalanceDao.selectByAccountIdForUpdate(accountId);
        if (balance == null) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "账户不存在");
        }
        if (balance.getAvailableBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足");
        }
        return balance;
    }
}
```

### 高并发处理最佳实践
```java
// ✅ 正确的高并发处理
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public ConsumeResultVO consume(ConsumeRequestDTO request) {
        // 1. 防重复提交检查
        String requestKey = generateRequestKey(request);
        if (redisTemplate.hasKey(requestKey)) {
            return ConsumeResultVO.failure("请勿重复提交");
        }

        // 2. 设置请求锁（30秒过期）
        redisTemplate.opsForValue().set(requestKey, "1", Duration.ofSeconds(30));

        try {
            // 3. 分布式锁处理账户操作
            String lockKey = "account:lock:" + request.getAccountId();
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", Duration.ofSeconds(10));

            if (locked) {
                try {
                    // 执行消费逻辑
                    return doConsume(request);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                return ConsumeResultVO.failure("系统繁忙，请稍后重试");
            }

        } finally {
            redisTemplate.delete(requestKey);
        }
    }
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 技术栈文档
- **Spring Boot 3.5.8**: 微服务框架文档
- **Redis**: 分布式缓存和锁文档
- **RabbitMQ**: 消息队列文档
- **MySQL**: 关系数据库文档

### 业务模块文档
- **💳 消费管理系统**: 消费和支付相关业务
- **🎁 补贴管理系统**: 补贴发放和管理业务
- **📊 数据分析系统**: 消费数据分析和报表

### 安全规范文档
- **🔒 支付安全规范**: 支付系统安全要求
- **💰 资金安全规范**: 资金安全保障措施
- **🛡️ 风控管理规范**: 实时风控和反欺诈

---

**📋 重要提醒**:
1. 本技能严格遵循IOE-DREAM四层架构规范
2. 所有代码示例使用Jakarta EE 3.0+包名规范
3. 统一使用@Resource依赖注入，禁止使用@Autowired
4. 统一使用@Mapper注解和Dao后缀命名
5. 重点关注资金安全和交易一致性
6. 必须支持高并发和高可用的支付处理
7. 严格遵循金融级安全和风控要求

**让我们一起建设安全、高效的消费支付体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + Redis + RabbitMQ + MySQL