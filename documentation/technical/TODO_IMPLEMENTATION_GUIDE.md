# TODO事项实施指南

## 📋 快速导航

本文档提供所有TODO事项的详细实施指南，确保高质量企业级实现。

## ✅ 已完成的修复

### 1. ConsumeAreaManager重复方法修复 ✅

**问题描述**:
- 存在两个`checkRelatedData(String areaId)`方法
- 第462行：空实现，只有TODO注释
- 第838行：部分实现，缺少设备和交易记录检查

**修复内容**:
1. ✅ 删除第462行的空方法
2. ✅ 完善第838行方法，实现完整关联检查：
   - 子区域检查（已实现）
   - 设备关联检查（通过GatewayServiceClient调用设备服务）
   - 交易记录检查（通过ConsumeTransactionDao）
   - 账户类别引用检查（通过GatewayServiceClient调用公共服务）

**代码位置**: `ConsumeAreaManager.java:840-906`

**验证方法**:
```bash
# 编译验证
mvn compile -pl microservices/ioedream-consume-service -DskipTests

# 运行单元测试
mvn test -pl microservices/ioedream-consume-service -Dtest=ConsumeAreaManagerTest
```

## 🔄 待实施事项详细指南

### P0级 - 关键业务功能（立即处理）

#### 1. 支付服务集成

**文件列表**:
- `MultiPaymentManager.java` (20+ TODO)
- `PaymentService.java` (6 TODO)
- `RechargeManager.java` (3 TODO)

**实施步骤**:

##### 1.1 微信支付集成

**参考文档**:
- 微信支付官方文档: https://pay.weixin.qq.com/docs/merchant/apis
- 微信支付SDK: `com.github.wechatpay-apiv3:wechatpay-java`

**实施代码模板**:
```java
// MultiPaymentManager.java
/**
 * 微信支付处理
 * 
 * @param request 支付请求
 * @return 支付结果
 */
private PaymentResult processWeChatPayment(PaymentRequest request) {
    try {
        // 1. 构建微信支付请求
        WeChatPayRequest wechatRequest = buildWeChatPayRequest(request);
        
        // 2. 调用微信支付API
        WeChatPayClient client = weChatPayClientFactory.create();
        WeChatPayResponse response = client.createOrder(wechatRequest);
        
        // 3. 处理支付结果
        if (response.isSuccess()) {
            return PaymentResult.success(response.getPrepayId());
        } else {
            throw new BusinessException("微信支付失败: " + response.getErrorMessage());
        }
    } catch (Exception e) {
        log.error("微信支付异常", e);
        throw new BusinessException("微信支付异常: " + e.getMessage());
    }
}
```

**关键TODO位置**:
- `MultiPaymentManager.java:360` - 调用微信支付API
- `MultiPaymentManager.java:247` - 验证openid
- `MultiPaymentManager.java:248` - 检查微信支付配置

##### 1.2 支付宝集成

**参考文档**:
- 支付宝开放平台: https://open.alipay.com/
- 支付宝SDK: `com.alipay.sdk:alipay-sdk-java`

**实施代码模板**:
```java
// MultiPaymentManager.java
/**
 * 支付宝支付处理
 */
private PaymentResult processAlipayPayment(PaymentRequest request) {
    try {
        // 1. 构建支付宝请求
        AlipayTradeAppPayRequest alipayRequest = buildAlipayRequest(request);
        
        // 2. 调用支付宝API
        AlipayClient client = alipayClientFactory.create();
        AlipayTradeAppPayResponse response = client.execute(alipayRequest);
        
        // 3. 处理支付结果
        return PaymentResult.success(response.getBody());
    } catch (Exception e) {
        log.error("支付宝支付异常", e);
        throw new BusinessException("支付宝支付异常: " + e.getMessage());
    }
}
```

**关键TODO位置**:
- `MultiPaymentManager.java:397` - 调用支付宝支付API
- `MultiPaymentManager.java:269` - 验证buyer_id
- `MultiPaymentManager.java:270` - 检查支付宝配置

##### 1.3 银行卡支付集成

**实施代码模板**:
```java
// MultiPaymentManager.java
/**
 * 银行卡支付处理
 */
private PaymentResult processBankCardPayment(PaymentRequest request) {
    try {
        // 1. 验证银行卡信息
        validateBankCard(request.getBankCard());
        
        // 2. 调用银行支付网关
        BankPaymentGateway gateway = bankPaymentGatewayFactory.create();
        BankPaymentResponse response = gateway.processPayment(request);
        
        // 3. 处理支付结果
        return PaymentResult.success(response.getTransactionId());
    } catch (Exception e) {
        log.error("银行卡支付异常", e);
        throw new BusinessException("银行卡支付异常: " + e.getMessage());
    }
}
```

**关键TODO位置**:
- `MultiPaymentManager.java:431` - 调用银行支付网关API
- `MultiPaymentManager.java:312-314` - 银行卡验证

#### 2. 账户服务方法实现

**文件**: `AccountServiceImpl.java` (30+ TODO)

**实施步骤**:

##### 2.1 账户CRUD操作

**实施代码模板**:
```java
// AccountServiceImpl.java
@Override
public ResponseDTO<Long> createAccount(AccountCreateForm form) {
    log.info("创建账户: {}", form.getUserId());
    
    // 1. 参数验证
    validateAccountCreateForm(form);
    
    // 2. 检查账户是否已存在
    ConsumeAccountEntity existingAccount = accountDao.selectByUserId(form.getUserId());
    if (existingAccount != null) {
        throw new BusinessException("账户已存在: " + form.getUserId());
    }
    
    // 3. 创建账户实体
    ConsumeAccountEntity account = new ConsumeAccountEntity();
    account.setUserId(form.getUserId());
    account.setAccountType(form.getAccountType());
    account.setBalance(BigDecimal.ZERO);
    account.setStatus(AccountStatus.ACTIVE);
    
    // 4. 保存账户
    accountDao.insert(account);
    
    // 5. 清除缓存
    accountCacheManager.clearAccountCache(account.getId());
    
    log.info("账户创建成功: accountId={}", account.getId());
    return ResponseDTO.ok(account.getId());
}
```

**关键TODO位置**:
- `AccountServiceImpl.java:39` - 实现账户创建逻辑
- `AccountServiceImpl.java:49` - 实现账户列表查询逻辑
- `AccountServiceImpl.java:60` - 实现账户详情查询逻辑

##### 2.2 余额管理

**实施代码模板**:
```java
// AccountServiceImpl.java
@Override
@Transactional(rollbackFor = Exception.class)
public ResponseDTO<BigDecimal> deductBalance(Long accountId, BigDecimal amount) {
    log.info("扣减余额: accountId={}, amount={}", accountId, amount);
    
    // 1. 参数验证
    if (accountId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException("参数错误");
    }
    
    // 2. 获取账户（加锁）
    ConsumeAccountEntity account = accountDao.selectByIdForUpdate(accountId);
    if (account == null) {
        throw new BusinessException("账户不存在");
    }
    
    // 3. 验证余额
    if (account.getBalance().compareTo(amount) < 0) {
        throw new BusinessException("余额不足");
    }
    
    // 4. 扣减余额
    BigDecimal newBalance = account.getBalance().subtract(amount);
    account.setBalance(newBalance);
    accountDao.updateById(account);
    
    // 5. 清除缓存
    accountCacheManager.clearAccountCache(accountId);
    
    log.info("余额扣减成功: accountId={}, 原余额={}, 扣减={}, 新余额={}", 
            accountId, account.getBalance().add(amount), amount, newBalance);
    return ResponseDTO.ok(newBalance);
}
```

**关键TODO位置**:
- `AccountServiceImpl.java:187` - 实现余额扣减逻辑
- `AccountServiceImpl.java:163` - 实现余额增加逻辑
- `AccountServiceImpl.java:244` - 实现余额验证逻辑

#### 3. 报表服务方法实现

**文件**: `ReportServiceImpl.java` (30+ TODO)

**实施步骤**:

##### 3.1 基础报表生成

**实施代码模板**:
```java
// ReportServiceImpl.java
@Override
public ResponseDTO<ConsumeReportVO> generateConsumeReport(ReportQueryForm form) {
    log.info("生成消费报表: {}", form);
    
    // 1. 参数验证
    validateReportQueryForm(form);
    
    // 2. 查询交易数据
    List<ConsumeTransactionEntity> transactions = queryTransactions(form);
    
    // 3. 统计数据
    ConsumeReportVO report = new ConsumeReportVO();
    report.setTotalAmount(calculateTotalAmount(transactions));
    report.setTotalCount(transactions.size());
    report.setTotalUsers(countDistinctUsers(transactions));
    
    // 4. 按维度统计
    report.setAreaStatistics(statisticsByArea(transactions));
    report.setMealStatistics(statisticsByMeal(transactions));
    report.setTimeStatistics(statisticsByTime(transactions));
    
    // 5. 生成报表文件（如果需要）
    if (form.isNeedExport()) {
        String filePath = exportReportToExcel(report, form);
        report.setExportFilePath(filePath);
    }
    
    log.info("消费报表生成成功: 总金额={}, 总笔数={}", 
            report.getTotalAmount(), report.getTotalCount());
    return ResponseDTO.ok(report);
}
```

**关键TODO位置**:
- `ReportServiceImpl.java:269` - 实现消费报表生成逻辑
- `ReportServiceImpl.java:276` - 实现充值报表生成逻辑
- `ReportServiceImpl.java:440` - 实现Excel报表导出逻辑

### P1级 - 重要功能

#### 4. Saga分布式事务

**文件**: `SagaTransactionController.java` (7 TODO)

**实施步骤**:

##### 4.1 Saga事务管理器

**实施代码模板**:
```java
// 新建 SagaTransactionManager.java
@Component
public class SagaTransactionManager {
    
    @Resource
    private SagaTransactionDao sagaTransactionDao;
    
    /**
     * 创建Saga事务
     */
    public SagaTransaction createSagaTransaction(SagaTransactionRequest request) {
        // 1. 创建事务记录
        SagaTransactionEntity transaction = new SagaTransactionEntity();
        transaction.setTransactionId(generateTransactionId());
        transaction.setStatus(SagaTransactionStatus.INIT);
        transaction.setSteps(buildSagaSteps(request));
        
        // 2. 保存事务
        sagaTransactionDao.insert(transaction);
        
        // 3. 执行第一步
        executeSagaStep(transaction, 0);
        
        return convertToVO(transaction);
    }
    
    /**
     * 执行Saga步骤
     */
    private void executeSagaStep(SagaTransactionEntity transaction, int stepIndex) {
        SagaStep step = transaction.getSteps().get(stepIndex);
        
        try {
            // 执行步骤
            step.execute();
            
            // 更新步骤状态
            step.setStatus(SagaStepStatus.COMPLETED);
            
            // 执行下一步
            if (stepIndex < transaction.getSteps().size() - 1) {
                executeSagaStep(transaction, stepIndex + 1);
            } else {
                // 所有步骤完成
                transaction.setStatus(SagaTransactionStatus.COMPLETED);
            }
        } catch (Exception e) {
            // 执行补偿
            compensateSagaTransaction(transaction, stepIndex);
        }
    }
}
```

**关键TODO位置**:
- `SagaTransactionController.java:51` - 实现Saga事务创建逻辑
- `SagaTransactionController.java:117` - 实现Saga事务补偿逻辑
- `SagaTransactionController.java:136` - 实现Saga事务重试逻辑

#### 5. 设备连接测试

**文件列表**:
- `DahuaAdapter.java` (4 TODO)
- `HikvisionAdapter.java` (3 TODO)
- `ZKTecoAdapter.java` (3 TODO)

**实施步骤**:

##### 5.1 大华HTTP连接测试

**实施代码模板**:
```java
// DahuaAdapter.java
private boolean testHttpConnection(DeviceEntity device) throws Exception {
    log.debug("测试大华HTTP连接: http://{}:{}/api/version", 
            device.getIpAddress(), device.getPort());
    
    try {
        // 1. 构建HTTP请求
        String url = String.format("http://%s:%d/api/version", 
                device.getIpAddress(), device.getPort());
        
        // 2. 发送HTTP GET请求
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(device.getUsername(), device.getPassword());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
        
        // 3. 验证响应
        if (response.getStatusCode().is2xxSuccessful()) {
            String body = response.getBody();
            log.debug("大华HTTP连接成功: {}", body);
            return true;
        } else {
            log.warn("大华HTTP连接失败: status={}", response.getStatusCode());
            return false;
        }
    } catch (Exception e) {
        log.error("大华HTTP连接异常", e);
        throw e;
    }
}
```

**关键TODO位置**:
- `DahuaAdapter.java:574` - 实现大华HTTP连接测试
- `DahuaAdapter.java:585` - 实现大华SDK连接测试
- `DahuaAdapter.java:597` - 实现GB28181连接测试
- `DahuaAdapter.java:609` - 实现ONVIF连接测试

## 📚 参考资源

### 技术文档
- [区域管理模块重构设计](./documentation/03-业务模块/消费/01-区域管理模块重构设计.md)
- [设备管理模块重构设计](./documentation/03-业务模块/消费/14-设备管理模块重构设计.md)
- [消费流水数据准确性设计](./documentation/03-业务模块/消费/15-消费流水数据准确性与性能设计.md)

### 第三方SDK文档
- 微信支付: https://pay.weixin.qq.com/docs/merchant/apis
- 支付宝: https://open.alipay.com/
- 大华SDK: 大华官方文档
- 海康SDK: 海康威视官方文档

## ✅ 质量检查清单

每个TODO实现后必须检查：

- [ ] 代码编译通过
- [ ] 单元测试通过
- [ ] 遵循四层架构规范
- [ ] 使用@Resource依赖注入
- [ ] 完整的异常处理
- [ ] 详细的日志记录
- [ ] 参数验证完整
- [ ] 无代码冗余
- [ ] 符合命名规范
- [ ] 添加JavaDoc注释

---

**文档版本**: v1.0
**创建时间**: 2025-01-30
**最后更新**: 2025-01-30
