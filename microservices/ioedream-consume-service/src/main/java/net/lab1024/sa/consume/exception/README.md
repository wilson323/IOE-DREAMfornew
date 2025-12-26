# 消费服务异常处理机制

## 📋 概述

消费服务采用统一的异常处理机制，包括：

- **异常类层次结构** - 完整的异常类型体系
- **全局异常处理器** - 统一异常捕获和响应
- **异常日志规范** - 标准化的异常日志记录
- **错误码管理** - 规范化的错误码定义

## 🏗️ 异常类层次结构

```
RuntimeException
└── BusinessException (全局业务异常基类)
    └── ConsumeBusinessException (消费业务异常基类)
        ├── ConsumeAccountException (账户异常)
        ├── ConsumeDeviceException (设备异常)
        ├── ConsumeProductException (产品异常)
        ├── ConsumeMealCategoryException (餐次分类异常)
        ├── ConsumeTransactionException (交易异常)
        ├── ConsumeRechargeException (充值异常)
        └── ConsumeSubsidyException (补贴异常)
```

## 🎯 全局异常处理器

### ConsumeGlobalExceptionHandler

专门处理消费服务模块的异常，具有以下特性：

- **高优先级处理** - `@Order(1)` 确保优先于通用异常处理器
- **模块化日志** - 所有异常都包含 `[消费XX异常]` 模块标识
- **追踪链支持** - 自动生成和管理 traceId
- **详细上下文** - 记录异常详情和业务上下文

### 支持的异常类型

| 异常类型 | HTTP状态码 | 错误码前缀 | 描述 |
|---------|-----------|-----------|------|
| ConsumeBusinessException | 200 OK | CONSUME_* | 消费业务异常 |
| ConsumeAccountException | 200 OK | ACCOUNT_* | 账户相关异常 |
| ConsumeDeviceException | 200 OK | DEVICE_* | 设备相关异常 |
| ConsumeProductException | 200 OK | PRODUCT_* | 产品相关异常 |
| ConsumeMealCategoryException | 200 OK | MEAL_CATEGORY_* | 餐次分类异常 |
| ConsumeTransactionException | 200 OK | TRANSACTION_* | 交易相关异常 |
| ConsumeRechargeException | 200 OK | RECHARGE_* | 充值相关异常 |
| ConsumeSubsidyException | 200 OK | SUBSIDY_* | 补贴相关异常 |
| ConstraintViolationException | 400 BAD_REQUEST | CONSUME_VALIDATION_ERROR | 参数验证异常 |
| IllegalArgumentException | 400 BAD_REQUEST | CONSUME_ILLEGAL_ARGUMENT | 非法参数异常 |
| IllegalStateException | 500 INTERNAL_SERVER_ERROR | CONSUME_ILLEGAL_STATE | 非法状态异常 |
| Exception | 500 INTERNAL_SERVER_ERROR | CONSUME_SYSTEM_ERROR | 未知异常 |

## 📝 异常使用规范

### 1. 创建异常类

```java
// 继承ConsumeBusinessException
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumeXxxException extends ConsumeBusinessException {

    // 特定字段
    private Long xxxId;

    public ConsumeXxxException(String code, String message, Long xxxId) {
        super(code, message);
        this.xxxId = xxxId;
    }

    // 静态工厂方法
    public static ConsumeXxxException notFound(Long xxxId) {
        return new ConsumeXxxException("XXX_NOT_FOUND",
            "XXX不存在: " + xxxId, xxxId);
    }
}
```

### 2. 在Service中使用异常

```java
@Service
public class ConsumeXxxServiceImpl implements ConsumeXxxService {

    @Override
    public XxxVO getById(Long xxxId) {
        try {
            log.info("[XXX服务] 开始查询: xxxId={}", xxxId);

            XxxEntity entity = xxxDao.selectById(xxxId);
            if (entity == null) {
                log.warn("[XXX服务] 记录不存在: xxxId={}", xxxId);
                throw ConsumeXxxException.notFound(xxxId);
            }

            XxxVO result = convertToVO(entity);
            log.info("[XXX服务] 查询成功: xxxId={}", xxxId);
            return result;

        } catch (ConsumeXxxException e) {
            throw e; // 重新抛出业务异常
        } catch (Exception e) {
            log.error("[XXX服务] 查询异常: xxxId={}, error={}", xxxId, e.getMessage(), e);
            throw ConsumeXxxException.systemError("查询XXX", e.getMessage());
        }
    }
}
```

### 3. 在Controller中处理

```java
@RestController
public class ConsumeXxxController {

    @GetMapping("/{xxxId}")
    @Operation(summary = "获取XXX详情")
    public ResponseDTO<XxxVO> getById(@PathVariable Long xxxId) {
        // 直接调用，异常会被全局处理器捕获
        XxxVO result = xxxService.getById(xxxId);
        return ResponseDTO.ok(result);
    }
}
```

## 🔧 错误码规范

### 错误码命名规则

```
{模块}_{错误类型}_{具体错误}
```

### 常用错误码

| 模块 | 错误码 | 描述 |
|------|--------|------|
| ACCOUNT | ACCOUNT_NOT_FOUND | 账户不存在 |
| ACCOUNT | ACCOUNT_STATUS_INVALID | 账户状态异常 |
| ACCOUNT | INSUFFICIENT_BALANCE | 余额不足 |
| DEVICE | DEVICE_OFFLINE | 设备离线 |
| DEVICE | DEVICE_NOT_FOUND | 设备不存在 |
| PRODUCT | PRODUCT_NOT_FOUND | 产品不存在 |
| PRODUCT | PRODUCT_DISABLED | 产品已禁用 |
| TRANSACTION | TRANSACTION_NOT_FOUND | 交易不存在 |
| TRANSACTION | TRANSACTION_STATUS_INVALID | 交易状态异常 |
| RECHARGE | RECHARGE_FAILED | 充值失败 |
| SUBSIDY | SUBSIDY_NOT_FOUND | 补贴不存在 |

## 📊 异常监控

### 异常日志格式

```
[消费业务异常] traceId={traceId}, code={errorCode}, message={errorMessage}, details={errorDetails}
[消费账户异常] traceId={traceId}, code={errorCode}, message={errorMessage}, details={errorDetails}
[消费设备异常] traceId={traceId}, code={errorCode}, message={errorMessage}, details={errorDetails}
```

### 监控指标

- 异常类型分布
- 异常发生频率
- 异常处理时间
- 异常上下文信息

## 🚀 最佳实践

### 1. 异常分类

- **业务异常** - 可预期的业务错误，使用具体异常类
- **系统异常** - 不可预期的系统错误，使用通用异常

### 2. 异常信息

- 包含足够的上下文信息
- 避免暴露敏感信息
- 提供用户友好的错误消息

### 3. 日志记录

- 关键操作记录INFO级别日志
- 异常情况记录WARN或ERROR级别日志
- 包含traceId便于追踪

### 4. 异常传播

- Service层抛出具体异常
- Controller层不捕获异常，交给全局处理器
- 避免异常转换和信息丢失

## 🔍 调试指南

### 1. 查看异常日志

```bash
# 按异常类型过滤
grep "消费业务异常" application.log

# 按traceId追踪
grep "traceId=123456789" application.log
```

### 2. 异常处理流程

1. **异常发生** - 业务逻辑中抛出具体异常
2. **日志记录** - Service层记录详细上下文
3. **异常传播** - 异常向上传播到Controller层
4. **全局捕获** - ConsumeGlobalExceptionHandler捕获异常
5. **统一响应** - 返回标准格式的错误响应
6. **监控记录** - 记录异常指标用于监控

### 3. 常见问题

- **异常信息丢失** - 确保使用正确的异常类型和消息
- **日志不完整** - 检查是否正确记录了异常上下文
- **响应格式不统一** - 全局处理器会统一格式，无需手动处理

---

**版本**: 1.0.0
**更新时间**: 2025-12-22
**维护者**: IOE-DREAM Team