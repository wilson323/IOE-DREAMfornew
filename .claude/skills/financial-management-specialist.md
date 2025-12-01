# 财务管理专家技能

## 基本信息
- **技能名称**: 财务管理专家
- **技能等级**: 高级
- **适用角色**: 财务系统开发工程师、企业应用架构师、财务管理业务专家
- **前置技能**: four-tier-architecture-guardian, cache-architecture-specialist, oa-system-specialist
- **预计学时**: 40小时

## 知识要求

### 理论知识
- **财务管理**: 企业会计准则、财务报表、成本核算、预算管理
- **税务知识**: 增值税、企业所得税、个人所得税、税务合规
- **银行业务**: 银行账户管理、资金调拨、电子支付、对账管理
- **审计合规**: 内部审计、外部审计、合规管理、风险控制

### 业务理解
- **账户管理**: 企业银行账户、内部账户、资金账户、虚拟账户
- **收支管理**: 收入确认、支出审批、费用报销、资金计划
- **对账管理**: 银企对账、内部对账、客户对账、供应商对账
- **报表分析**: 财务报表、资金报表、经营分析、预算执行分析

### 技术背景
- **财务系统**: ERP财务模块、资金管理系统、报账系统
- **支付集成**: 银行支付接口、第三方支付、电子支付
- **数据处理**: 财务数据建模、账务处理、凭证管理
- **报表工具**: 报表设计、数据可视化、财务分析

## 操作步骤

### 1. 银行账户管理
```java
// 1.1 银行账户服务
@Service
public class BankAccountService {

    @Resource
    private BankAccountRepository bankAccountRepository;

    @Resource
    private BankIntegrationService bankIntegrationService;

    @Resource
    private FinancialCacheService financialCache;

    /**
     * 开立银行账户
     */
    @Transactional
    public BankAccountResult openBankAccount(BankAccountOpenRequest request) {
        try {
            // 1. 验证开户信息
            validateAccountOpenRequest(request);

            // 2. 检查账户唯一性
            if (bankAccountRepository.existsByAccountNo(request.getAccountNo())) {
                return BankAccountResult.failure("银行账号已存在: " + request.getAccountNo());
            }

            // 3. 验证银行信息
            BankEntity bank = validateBankInfo(request.getBankId());

            // 4. 创建银行账户实体
            BankAccountEntity bankAccount = BankAccountEntity.builder()
                .accountId(generateAccountId())
                .accountName(request.getAccountName())
                .accountNo(request.getAccountNo())
                .bankId(request.getBankId())
                .bankName(bank.getBankName())
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .accountPurpose(request.getAccountPurpose())
                .authorizedPersons(request.getAuthorizedPersons())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .createTime(LocalDateTime.now())
                .createUserId(SecurityUtils.getCurrentUserId())
                .build();

            // 5. 保存账户信息
            bankAccountRepository.save(bankAccount);

            // 6. 建立银行连接（如果支持）
            if (request.isEnableBankIntegration()) {
                BankIntegrationResult integrationResult = bankIntegrationService.establishConnection(
                    bankAccount.getAccountId(),
                    request.getBankIntegrationConfig()
                );

                if (!integrationResult.isSuccess()) {
                    log.warn("银行连接建立失败: accountId={}, error={}", bankAccount.getAccountId(), integrationResult.getErrorMessage());
                }
            }

            // 7. 初始化账户余额（如果提供）
            if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
                initializeAccountBalance(bankAccount, request.getInitialBalance(), request.getBalanceType());
            }

            // 8. 清理账户缓存
            financialCache.clearBankAccountCache();

            // 9. 记录操作日志
            operationLogService.record("开立银行账户", bankAccount.getAccountId(),
                                     "开立账户: " + bankAccount.getAccountName());

            log.info("银行账户开立成功: accountId={}, accountNo={}", bankAccount.getAccountId(), bankAccount.getAccountNo());
            return BankAccountResult.success(bankAccount.getAccountId());

        } catch (Exception e) {
            log.error("银行账户开立失败: accountNo={}", request.getAccountNo(), e);
            return BankAccountResult.failure("银行账户开立失败: " + e.getMessage());
        }
    }

    /**
     * 账户余额查询
     */
    public AccountBalanceResult getAccountBalance(String accountId, BalanceQueryType queryType) {
        try {
            // 1. 获取账户信息
            BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("银行账户不存在: " + accountId));

            // 2. 检查查询权限
            validateBalanceQueryPermission(account);

            AccountBalance balance;

            switch (queryType) {
                case CACHE:
                    // 3. 从缓存查询
                    balance = financialCache.getAccountBalance(accountId);
                    if (balance != null) {
                        return AccountBalanceResult.success(balance);
                    }
                    // 缓存未命中，继续查询银行
                    break;

                case BANK:
                    // 4. 直接查询银行
                    break;

                case SYSTEM:
                    // 5. 查询系统记录
                    return AccountBalanceResult.success(getSystemBalance(accountId));

                default:
                    return AccountBalanceResult.failure("不支持的查询类型: " + queryType);
            }

            // 6. 查询银行余额
            BankBalanceQueryResult bankResult = bankIntegrationService.queryBalance(accountId);
            if (!bankResult.isSuccess()) {
                return AccountBalanceResult.failure("银行余额查询失败: " + bankResult.getErrorMessage());
            }

            // 7. 构建余额信息
            balance = AccountBalance.builder()
                .accountId(accountId)
                .accountNo(account.getAccountNo())
                .availableBalance(bankResult.getAvailableBalance())
                .bookBalance(bankResult.getBookBalance())
                .freezeAmount(bankResult.getFreezeAmount())
                .currency(account.getCurrency())
                .queryTime(LocalDateTime.now())
                .dataSource(DataSource.BANK)
                .build();

            // 8. 更新缓存
            financialCache.setAccountBalance(accountId, balance);

            // 9. 保存余额记录
            saveBalanceRecord(balance);

            return AccountBalanceResult.success(balance);

        } catch (Exception e) {
            log.error("账户余额查询失败: accountId={}", accountId, e);
            return AccountBalanceResult.failure("账户余额查询失败: " + e.getMessage());
        }
    }

    /**
     * 账户状态管理
     */
    @Transactional
    public AccountStatusResult updateAccountStatus(String accountId, AccountStatusUpdateRequest request) {
        try {
            // 1. 获取账户信息
            BankAccountEntity account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("银行账户不存在: " + accountId));

            // 2. 验证状态变更权限
            validateStatusUpdatePermission(account, request.getNewStatus());

            // 3. 记录变更前状态
            AccountStatus beforeStatus = account.getStatus();

            // 4. 执行状态变更前置操作
            executeStatusChangePreActions(account, request.getNewStatus());

            // 5. 更新账户状态
            account.setStatus(request.getNewStatus());
            account.setUpdateTime(LocalDateTime.now());
            account.setUpdateUserId(SecurityUtils.getCurrentUserId());

            // 6. 保存更新
            bankAccountRepository.update(account);

            // 7. 执行状态变更后置操作
            executeStatusChangePostActions(account, beforeStatus, request.getNewStatus());

            // 8. 清理缓存
            financialCache.clearBankAccountCache(accountId);

            // 9. 记录状态变更历史
            saveAccountStatusHistory(accountId, beforeStatus, request.getNewStatus(), request.getReason());

            // 10. 发送状态变更通知
            sendStatusChangeNotification(account, beforeStatus, request.getNewStatus());

            log.info("账户状态更新成功: accountId={}, {} -> {}", accountId, beforeStatus, request.getNewStatus());
            return AccountStatusResult.success();

        } catch (Exception e) {
            log.error("账户状态更新失败: accountId={}", accountId, e);
            return AccountStatusResult.failure("账户状态更新失败: " + e.getMessage());
        }
    }
}
```

### 2. 资金调拨管理
```java
// 2.1 资金调拨服务
@Service
public class FundTransferService {

    @Resource
    private FundTransferRepository transferRepository;

    @Resource
    private BankAccountService bankAccountService;

    @Resource
    private ApprovalService approvalService;

    /**
     * 创建资金调拨
     */
    @Transactional
    public FundTransferResult createFundTransfer(FundTransferCreateRequest request) {
        try {
            // 1. 验证调拨请求
            validateTransferRequest(request);

            // 2. 验证账户状态
            BankAccountEntity fromAccount = validateAccountStatus(request.getFromAccountId());
            BankAccountEntity toAccount = validateAccountStatus(request.getToAccountId());

            // 3. 检查资金充足性
            if (request.getAmount().compareTo(getAccountBalance(request.getFromAccountId()).getAvailableBalance()) > 0) {
                return FundTransferResult.failure("账户余额不足");
            }

            // 4. 检查调拨限额
            if (checkTransferLimit(request)) {
                return FundTransferResult.failure("超出单笔调拨限额");
            }

            // 5. 创建调拨记录
            FundTransferEntity transfer = FundTransferEntity.builder()
                .transferId(generateTransferId())
                .fromAccountId(request.getFromAccountId())
                .fromAccountNo(fromAccount.getAccountNo())
                .toAccountId(request.getToAccountId())
                .toAccountNo(toAccount.getAccountNo())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transferType(request.getTransferType())
                .purpose(request.getPurpose())
                .remark(request.getRemark())
                .requestedBy(SecurityUtils.getCurrentUserId())
                .requestedTime(LocalDateTime.now())
                .status(TransferStatus.PENDING_APPROVAL)
                .build();

            // 6. 保存调拨记录
            transferRepository.save(transfer);

            // 7. 启动审批流程
            ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .businessType(BusinessType.FUND_TRANSFER)
                .businessId(transfer.getTransferId())
                .title("资金调拨审批")
                .content(buildApprovalContent(transfer))
                .amount(request.getAmount())
                .requestor(SecurityUtils.getCurrentUserId())
                .build();

            ApprovalResult approvalResult = approvalService.startApproval(approvalRequest);
            if (!approvalResult.isSuccess()) {
                transfer.setStatus(TransferStatus.FAILED);
                transfer.setFailureReason("审批流程启动失败: " + approvalResult.getErrorMessage());
                transferRepository.update(transfer);
                return FundTransferResult.failure("审批流程启动失败");
            }

            transfer.setApprovalInstanceId(approvalResult.getInstanceId());
            transferRepository.update(transfer);

            log.info("资金调拨创建成功: transferId={}, amount={}", transfer.getTransferId(), request.getAmount());
            return FundTransferResult.success(transfer.getTransferId());

        } catch (Exception e) {
            log.error("资金调拨创建失败", e);
            return FundTransferResult.failure("资金调拨创建失败: " + e.getMessage());
        }
    }

    /**
     * 执行资金调拨
     */
    @Transactional
    public FundTransferResult executeFundTransfer(String transferId) {
        try {
            // 1. 获取调拨记录
            FundTransferEntity transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new FundTransferNotFoundException("资金调拨记录不存在: " + transferId));

            // 2. 验证调拨状态
            if (transfer.getStatus() != TransferStatus.APPROVED) {
                return FundTransferResult.failure("调拨状态不正确: " + transfer.getStatus());
            }

            // 3. 更新状态为执行中
            transfer.setStatus(TransferStatus.EXECUTING);
            transfer.setExecuteTime(LocalDateTime.now());
            transferRepository.update(transfer);

            // 4. 执行银行转账
            BankTransferResult bankResult = bankIntegrationService.executeTransfer(
                transfer.getFromAccountId(),
                transfer.getToAccountId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getPurpose()
            );

            if (!bankResult.isSuccess()) {
                transfer.setStatus(TransferStatus.FAILED);
                transfer.setFailureReason("银行转账失败: " + bankResult.getErrorMessage());
                transferRepository.update(transfer);
                return FundTransferResult.failure("银行转账失败: " + bankResult.getErrorMessage());
            }

            // 5. 更新调拨状态
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setCompleteTime(LocalDateTime.now());
            transfer.setBankTransactionId(bankResult.getTransactionId());
            transferRepository.update(transfer);

            // 6. 创建会计凭证
            createAccountingVoucher(transfer, bankResult);

            // 7. 更新账户余额（系统记录）
            updateAccountBalances(transfer);

            // 8. 发送执行完成通知
            sendTransferExecutionNotification(transfer);

            log.info("资金调拨执行成功: transferId={}, bankTransactionId={}", transferId, bankResult.getTransactionId());
            return FundTransferResult.success(transferId);

        } catch (Exception e) {
            log.error("资金调拨执行失败: transferId={}", transferId, e);

            // 更新失败状态
            updateTransferFailureStatus(transferId, e.getMessage());

            return FundTransferResult.failure("资金调拨执行失败: " + e.getMessage());
        }
    }

    /**
     * 批量资金调拨
     */
    @Transactional
    public BatchTransferResult executeBatchTransfer(List<String> transferIds) {
        try {
            BatchTransferResult.Builder resultBuilder = BatchTransferResult.builder()
                .totalTransfers(transferIds.size());

            int successCount = 0;
            int failureCount = 0;
            List<TransferFailure> failures = new ArrayList<>();

            // 并行执行调拨
            List<CompletableFuture<TransferExecutionResult>> executionTasks = transferIds.stream()
                .map(transferId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        FundTransferResult result = executeFundTransfer(transferId);
                        return TransferExecutionResult.builder()
                            .transferId(transferId)
                            .success(result.isSuccess())
                            .errorMessage(result.getErrorMessage())
                            .build();
                    } catch (Exception e) {
                        return TransferExecutionResult.builder()
                            .transferId(transferId)
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build();
                    }
                }))
                .collect(Collectors.toList());

            // 等待所有执行完成
            CompletableFuture.allOf(executionTasks.toArray(new CompletableFuture[0])).join();

            // 汇总结果
            for (CompletableFuture<TransferExecutionResult> task : executionTasks) {
                TransferExecutionResult executionResult = task.join();
                if (executionResult.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                    failures.add(TransferFailure.builder()
                        .transferId(executionResult.getTransferId())
                        .errorMessage(executionResult.getErrorMessage())
                        .build());
                }
            }

            return resultBuilder
                .successCount(successCount)
                .failureCount(failureCount)
                .failures(failures)
                .build();

        } catch (Exception e) {
            log.error("批量资金调拨失败", e);
            return BatchTransferResult.failure("批量资金调拨失败: " + e.getMessage());
        }
    }
}
```

### 3. 费用报销管理
```java
// 3.1 费用报销服务
@Service
public class ExpenseReimbursementService {

    @Resource
    private ExpenseReimbursementRepository reimbursementRepository;

    @Resource
    private ApprovalService approvalService;

    @Resource
    private PaymentService paymentService;

    /**
     * 创建费用报销申请
     */
    @Transactional
    public ReimbursementResult createReimbursement(ReimbursementCreateRequest request) {
        try {
            // 1. 验证报销申请
            validateReimbursementRequest(request);

            // 2. 计算报销总额
            BigDecimal totalAmount = calculateTotalAmount(request.getExpenseItems());

            // 3. 检查报销限额
            if (checkReimbursementLimit(request.getEmployeeId(), totalAmount)) {
                return ReimbursementResult.failure("超出报销限额");
            }

            // 4. 创建报销记录
            ReimbursementEntity reimbursement = ReimbursementEntity.builder()
                .reimbursementId(generateReimbursementId())
                .employeeId(request.getEmployeeId())
                .employeeName(request.getEmployeeName())
                .departmentId(request.getDepartmentId())
                .departmentName(request.getDepartmentName())
                .reimbursementType(request.getReimbursementType())
                .totalAmount(totalAmount)
                .currency(request.getCurrency())
                .applicationDate(LocalDateTime.now())
                .applicationReason(request.getApplicationReason())
                .applicant(SecurityUtils.getCurrentUserId())
                .status(ReimbursementStatus.PENDING_APPROVAL)
                .build();

            // 5. 保存报销记录
            reimbursementRepository.save(reimbursement);

            // 6. 保存费用明细
            saveExpenseItems(reimbursement.getReimbursementId(), request.getExpenseItems());

            // 7. 保存附件信息
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                saveAttachments(reimbursement.getReimbursementId(), request.getAttachments());
            }

            // 8. 启动审批流程
            ApprovalRequest approvalRequest = ApprovalRequest.builder()
                .businessType(BusinessType.EXPENSE_REIMBURSEMENT)
                .businessId(reimbursement.getReimbursementId())
                .title("费用报销审批")
                .content(buildReimbursementApprovalContent(reimbursement))
                .amount(totalAmount)
                .requestor(SecurityUtils.getCurrentUserId())
                .build();

            ApprovalResult approvalResult = approvalService.startApproval(approvalRequest);
            if (!approvalResult.isSuccess()) {
                reimbursement.setStatus(ReimbursementStatus.FAILED);
                reimbursement.setFailureReason("审批流程启动失败: " + approvalResult.getErrorMessage());
                reimbursementRepository.update(reimbursement);
                return ReimbursementResult.failure("审批流程启动失败");
            }

            reimbursement.setApprovalInstanceId(approvalResult.getInstanceId());
            reimbursementRepository.update(reimbursement);

            log.info("费用报销申请创建成功: reimbursementId={}, amount={}", reimbursement.getReimbursementId(), totalAmount);
            return ReimbursementResult.success(reimbursement.getReimbursementId());

        } catch (Exception e) {
            log.error("费用报销申请创建失败", e);
            return ReimbursementResult.failure("费用报销申请创建失败: " + e.getMessage());
        }
    }

    /**
     * 报销付款处理
     */
    @Transactional
    public ReimbursementResult processReimbursementPayment(String reimbursementId, ReimbursementPaymentRequest request) {
        try {
            // 1. 获取报销记录
            ReimbursementEntity reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new ReimbursementNotFoundException("报销记录不存在: " + reimbursementId));

            // 2. 验证报销状态
            if (reimbursement.getStatus() != ReimbursementStatus.APPROVED) {
                return ReimbursementResult.failure("报销状态不正确: " + reimbursement.getStatus());
            }

            // 3. 验证付款账户
            validatePaymentAccount(request.getPaymentAccountId());

            // 4. 更新状态为付款中
            reimbursement.setStatus(ReimbursementStatus.PAYING);
            reimbursement.setPaymentAccountId(request.getPaymentAccountId());
            reimbursement.setPaymentTime(LocalDateTime.now());
            reimbursementRepository.update(reimbursement);

            // 5. 执行付款
            PaymentRequest paymentRequest = PaymentRequest.builder()
                .paymentType(PaymentType.REIMBURSEMENT)
                .businessId(reimbursementId)
                .payeeId(reimbursement.getEmployeeId())
                .payeeName(reimbursement.getEmployeeName())
                .amount(reimbursement.getTotalAmount())
                .currency(reimbursement.getCurrency())
                .paymentAccountId(request.getPaymentAccountId())
                .paymentMethod(request.getPaymentMethod())
                .remark("费用报销付款")
                .build();

            PaymentResult paymentResult = paymentService.executePayment(paymentRequest);
            if (!paymentResult.isSuccess()) {
                reimbursement.setStatus(ReimbursementStatus.PAYMENT_FAILED);
                reimbursement.setFailureReason("付款失败: " + paymentResult.getErrorMessage());
                reimbursementRepository.update(reimbursement);
                return ReimbursementResult.failure("付款失败: " + paymentResult.getErrorMessage());
            }

            // 6. 更新报销状态
            reimbursement.setStatus(ReimbursementStatus.COMPLETED);
            reimbursement.setPaymentId(paymentResult.getPaymentId());
            reimbursement.setCompleteTime(LocalDateTime.now());
            reimbursementRepository.update(reimbursement);

            // 7. 创建会计凭证
            createReimbursementVoucher(reimbursement, paymentResult);

            // 8. 发送付款完成通知
            sendReimbursementPaymentNotification(reimbursement);

            log.info("费用报销付款完成: reimbursementId={}, paymentId={}", reimbursementId, paymentResult.getPaymentId());
            return ReimbursementResult.success(reimbursementId);

        } catch (Exception e) {
            log.error("费用报销付款处理失败: reimbursementId={}", reimbursementId, e);

            // 更新付款失败状态
            updatePaymentFailureStatus(reimbursementId, e.getMessage());

            return ReimbursementResult.failure("费用报销付款处理失败: " + e.getMessage());
        }
    }
}
```

### 4. 财务报表管理
```java
// 4.1 财务报表服务
@Service
public class FinancialReportService {

    @Resource
    private FinancialReportRepository reportRepository;

    @Resource
    private ReportTemplateService templateService;

    @Resource
    private DataCalculationService calculationService;

    /**
     * 生成资产负债表
     */
    public BalanceSheetReportResult generateBalanceSheet(BalanceSheetRequest request) {
        try {
            // 1. 验证报表参数
            validateBalanceSheetRequest(request);

            // 2. 获取报表模板
            ReportTemplate template = templateService.getReportTemplate(
                ReportType.BALANCE_SHEET,
                request.getTemplateId()
            );

            // 3. 计算资产项目
            List<BalanceSheetItem> assetItems = calculateBalanceSheetAssets(
                request.getReportDate(),
                request.getOrganizationIds()
            );

            // 4. 计算负债项目
            List<BalanceSheetItem> liabilityItems = calculateBalanceSheetLiabilities(
                request.getReportDate(),
                request.getOrganizationIds()
            );

            // 5. 计算所有者权益项目
            List<BalanceSheetItem> equityItems = calculateBalanceSheetEquity(
                request.getReportDate(),
                request.getOrganizationIds()
            );

            // 6. 计算总计
            BigDecimal totalAssets = assetItems.stream()
                .map(BalanceSheetItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalLiabilitiesAndEquity = liabilityItems.stream()
                .map(BalanceSheetItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(equityItems.stream()
                    .map(BalanceSheetItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            // 7. 构建资产负债表
            BalanceSheetReport balanceSheet = BalanceSheetReport.builder()
                .reportId(generateReportId())
                .reportDate(request.getReportDate())
                .organizationIds(request.getOrganizationIds())
                .currency(request.getCurrency())
                .reportUnit(request.getReportUnit())
                .assetItems(assetItems)
                .liabilityItems(liabilityItems)
                .equityItems(equityItems)
                .totalAssets(totalAssets)
                .totalLiabilitiesAndEquity(totalLiabilitiesAndEquity)
                .isBalanced(totalAssets.compareTo(totalLiabilitiesAndEquity) == 0)
                .generationTime(LocalDateTime.now())
                .generatedBy(SecurityUtils.getCurrentUserId())
                .build();

            // 8. 保存报表记录
            reportRepository.saveBalanceSheet(balanceSheet);

            // 9. 生成报表文件
            ReportFileResult fileResult = generateReportFile(balanceSheet, template);
            if (fileResult.isSuccess()) {
                balanceSheet.setReportFileId(fileResult.getFileId());
                balanceSheet.setReportFilePath(fileResult.getFilePath());
                reportRepository.updateBalanceSheet(balanceSheet);
            }

            return BalanceSheetReportResult.success(balanceSheet);

        } catch (Exception e) {
            log.error("资产负债表生成失败", e);
            return BalanceSheetReportResult.failure("资产负债表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成损益表
     */
    public IncomeStatementReportResult generateIncomeStatement(IncomeStatementRequest request) {
        try {
            // 1. 验证报表参数
            validateIncomeStatementRequest(request);

            // 2. 计算收入项目
            List<IncomeStatementItem> revenueItems = calculateRevenueItems(
                request.getStartDate(),
                request.getEndDate(),
                request.getOrganizationIds()
            );

            // 3. 计算成本项目
            List<IncomeStatementItem> costItems = calculateCostItems(
                request.getStartDate(),
                request.getEndDate(),
                request.getOrganizationIds()
            );

            // 4. 计算费用项目
            List<IncomeStatementItem> expenseItems = calculateExpenseItems(
                request.getStartDate(),
                request.getEndDate(),
                request.getOrganizationIds()
            );

            // 5. 计算利润指标
            BigDecimal totalRevenue = revenueItems.stream()
                .map(IncomeStatementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = costItems.stream()
                .map(IncomeStatementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalExpense = expenseItems.stream()
                .map(IncomeStatementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal grossProfit = totalRevenue.subtract(totalCost);
            BigDecimal operatingProfit = grossProfit.subtract(totalExpense);

            // 6. 构建损益表
            IncomeStatementReport incomeStatement = IncomeStatementReport.builder()
                .reportId(generateReportId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organizationIds(request.getOrganizationIds())
                .currency(request.getCurrency())
                .reportUnit(request.getReportUnit())
                .revenueItems(revenueItems)
                .costItems(costItems)
                .expenseItems(expenseItems)
                .totalRevenue(totalRevenue)
                .totalCost(totalCost)
                .totalExpense(totalExpense)
                .grossProfit(grossProfit)
                .operatingProfit(operatingProfit)
                .generationTime(LocalDateTime.now())
                .generatedBy(SecurityUtils.getCurrentUserId())
                .build();

            // 7. 保存报表记录
            reportRepository.saveIncomeStatement(incomeStatement);

            // 8. 生成报表文件
            ReportTemplate template = templateService.getReportTemplate(
                ReportType.INCOME_STATEMENT,
                request.getTemplateId()
            );

            ReportFileResult fileResult = generateReportFile(incomeStatement, template);
            if (fileResult.isSuccess()) {
                incomeStatement.setReportFileId(fileResult.getFileId());
                incomeStatement.setReportFilePath(fileResult.getFilePath());
                reportRepository.updateIncomeStatement(incomeStatement);
            }

            return IncomeStatementReportResult.success(incomeStatement);

        } catch (Exception e) {
            log.error("损益表生成失败", e);
            return IncomeStatementReportResult.failure("损益表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成现金流量表
     */
    public CashFlowReportResult generateCashFlowStatement(CashFlowRequest request) {
        try {
            // 1. 验证报表参数
            validateCashFlowRequest(request);

            // 2. 计算经营活动现金流
            List<CashFlowItem> operatingItems = calculateOperatingCashFlow(
                request.getStartDate(),
                request.getEndDate(),
                request.getOrganizationIds()
            );

            // 3. 计算投资活动现金流
            List<CashFlowItem> investingItems = calculateInvestingCashFlow(
                request.getStartDate(),
                request.getEndDate(),
                request.getOrganizationIds()
            );

            // 4. 计算筹资活动现金流
            List<CashFlowItem> financingItems = calculateFinancingCashFlow(
                request.getStartDate(),
                request.getEndDate(),
                request.getOrganizationIds()
            );

            // 5. 计算现金流净额
            BigDecimal operatingNet = calculateNetCashFlow(operatingItems);
            BigDecimal investingNet = calculateNetCashFlow(investingItems);
            BigDecimal financingNet = calculateNetCashFlow(financingItems);
            BigDecimal totalNetCashFlow = operatingNet.add(investingNet).add(financingNet);

            // 6. 构建现金流量表
            CashFlowReport cashFlowReport = CashFlowReport.builder()
                .reportId(generateReportId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .organizationIds(request.getOrganizationIds())
                .currency(request.getCurrency())
                .reportUnit(request.getReportUnit())
                .operatingItems(operatingItems)
                .investingItems(investingItems)
                .financingItems(financingItems)
                .operatingNetCashFlow(operatingNet)
                .investingNetCashFlow(investingNet)
                .financingNetCashFlow(financingNet)
                .totalNetCashFlow(totalNetCashFlow)
                .generationTime(LocalDateTime.now())
                .generatedBy(SecurityUtils.getCurrentUserId())
                .build();

            // 7. 保存报表记录
            reportRepository.saveCashFlow(cashFlowReport);

            // 8. 生成报表文件
            ReportTemplate template = templateService.getReportTemplate(
                ReportType.CASH_FLOW,
                request.getTemplateId()
            );

            ReportFileResult fileResult = generateReportFile(cashFlowReport, template);
            if (fileResult.isSuccess()) {
                cashFlowReport.setReportFileId(fileResult.getFileId());
                cashFlowReport.setReportFilePath(fileResult.getFilePath());
                reportRepository.updateCashFlow(cashFlowReport);
            }

            return CashFlowReportResult.success(cashFlowReport);

        } catch (Exception e) {
            log.error("现金流量表生成失败", e);
            return CashFlowReportResult.failure("现金流量表生成失败: " + e.getMessage());
        }
    }
}
```

## 注意事项

### 安全提醒
- **数据安全**: 财务数据敏感，需要严格加密存储和传输
- **权限控制**: 财务操作需要严格的权限控制和审批流程
- **审计追踪**: 所有财务操作必须完整记录审计日志
- **合规要求**: 严格遵守财务法规和税务合规要求

### 质量要求
- **数据准确性**: 财务数据准确率必须达到100%
- **处理时效**: 财务处理及时性，确保业务正常运转
- **系统稳定**: 财务系统可用性≥99.99%
- **报表质量**: 财务报表数据准确、格式规范

### 最佳实践
- **双人复核**: 重要财务操作需要双人复核机制
- **自动化处理**: 重复性财务工作自动化处理
- **异常监控**: 建立财务异常实时监控和告警机制
- **备份恢复**: 财务数据定期备份和灾难恢复机制

## 评估标准

### 操作时间
- **银行开户**: 2小时内完成银行账户开立
- **资金调拨**: 30分钟内完成资金调拨审批和执行
- **费用报销**: 2个工作日内完成费用报销处理
- **报表生成**: 10分钟内生成标准财务报表

### 准确率
- **账户信息准确率**: 100%
- **资金调拨准确率**: 100%
- **报表数据准确率**: 100%
- **税务计算准确率**: 99.9%

### 质量标准
- **系统响应时间**: P95≤1秒
- **并发处理能力**: 支持1000+并发财务操作
- **数据处理能力**: 支持千万级财务数据处理
- **报表生成性能**: 10分钟内生成复杂财务报表

## 与其他技能的协作

### 与four-tier-architecture-guardian协作
- 严格遵循四层架构，财务业务逻辑在Service层实现
- 复杂财务计算在Manager层封装
- 财务数据访问通过DAO层统一管理

### 与cache-architecture-specialist协作
- 账户余额信息使用BusinessDataType.REALTIME缓存
- 财务报表数据使用BusinessDataType.STABLE缓存
- 权限信息使用BusinessDataType.NORMAL缓存

### 与oa-system-specialist协作
- 集成员工管理信息进行财务操作
- 复用组织架构权限管理体系
- 统一的审批流程和通知机制

## 质量保障

### 单元测试要求
```java
@Test
public void testFundTransfer() {
    // 测试资金调拨功能
    FundTransferCreateRequest request = createTestFundTransferRequest();
    FundTransferResult result = fundTransferService.createFundTransfer(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getTransferId()).isNotEmpty();
}

@Test
public void testBalanceSheetGeneration() {
    // 测试资产负债表生成
    BalanceSheetRequest request = createTestBalanceSheetRequest();
    BalanceSheetReportResult result = financialReportService.generateBalanceSheet(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getBalanceSheet().isBalanced()).isTrue();
}
```

### 集成测试要求
- **端到端财务流程测试**: 测试完整的资金调拨流程
- **银行集成测试**: 测试与银行系统的集成效果
- **审批集成测试**: 测试财务审批流程集成
- **报表准确性测试**: 验证财务报表数据的准确性

### 数据安全要求
- **敏感数据加密**: 银行账户、交易金额等敏感信息加密
- **访问日志审计**: 记录所有财务数据访问和操作日志
- **权限最小化**: 遵循最小权限原则分配财务权限
- **数据完整性**: 确保财务数据的完整性和一致性