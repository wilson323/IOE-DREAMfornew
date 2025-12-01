# 全局异常处理器专家 (Global Exception Handler Specialist)

**技能等级**: 🛡️ 核心守护专家
**版本**: v1.0 (2025-11-18创建)
**核心能力**: 全局异常处理、统一异常管理、Controller层代码清理
**遵循规范**: repowiki四层架构、Spring Boot 3.x、企业级异常处理标准

## 🎯 技能专长领域

### 1. 全局异常处理架构设计
- **统一异常拦截**: 使用@RestControllerAdvice实现全局异常拦截
- **异常分类处理**: 业务异常、系统异常、参数验证异常等分类处理
- **错误响应标准化**: 统一ResponseDTO格式，API响应一致性
- **日志规范**: 异常日志标准化记录，便于问题追踪

### 2. Controller层异常处理优化
- **消除重复try-catch**: 移除Controller中的重复异常处理代码
- **异常映射**: 将底层异常映射为用户友好的错误信息
- **安全异常处理**: 避免敏感信息泄露，生产环境异常信息脱敏
- **异常链追踪**: 完整保留异常链，便于问题排查

### 3. 异常处理策略
- **业务异常 (BusinessException)**: 业务逻辑异常，返回业务错误码和消息
- **Smart系统异常 (SmartException)**: 系统预定义异常
- **参数验证异常**: @Valid、@Validated注解验证失败处理
- **系统异常**: 未预期异常的兜底处理

## 🏗️ 标准架构设计

### 全局异常处理器标准模板

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常: {} - {}", getRequestUrl(request), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数验证异常处理 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("参数验证失败: {} - {}", getRequestUrl(request), e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");

        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            errorMessage.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
            if (i < fieldErrors.size() - 1) {
                errorMessage.append("; ");
            }
        }

        return ResponseDTO.error("PARAM_ERROR", errorMessage.toString());
    }

    /**
     * 系统异常兜底处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", getRequestUrl(request), e.getMessage(), e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统异常，请联系管理员");
    }

    private String getRequestUrl(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
```

## 🔧 异常类型处理策略

### 1. 业务异常 (BusinessException)
```java
// 使用场景: 业务逻辑不符合预期
throw new BusinessException("USER_NOT_FOUND", "用户不存在");
throw new BusinessException("ACCOUNT_FROZEN", "账户已被冻结");
throw new BusinessException("INSUFFICIENT_BALANCE", "账户余额不足");
```

### 2. 参数验证异常
```java
// @Valid 异常处理
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseDTO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e)

// @Validated 异常处理
@ExceptionHandler(ConstraintViolationException.class)
public ResponseDTO<Void> handleConstraintViolationException(ConstraintViolationException e)

// 参数绑定异常
@ExceptionHandler(BindException.class)
public ResponseDTO<Void> handleBindException(BindException e)
```

### 3. 系统异常
```java
// 空指针异常
@ExceptionHandler(NullPointerException.class)
public ResponseDTO<Void> handleNullPointerException(NullPointerException e)

// 运行时异常
@ExceptionHandler(RuntimeException.class)
public ResponseDTO<Void> handleRuntimeException(RuntimeException e)

// 通用异常兜底
@ExceptionHandler(Exception.class)
public ResponseDTO<Void> handleException(Exception e)
```

## 📋 Controller层重构指南

### 重构前 (有重复try-catch)
```java
@RestController
public class ConsumeController {

    @PostMapping("/consume")
    public ResponseDTO<String> consume(@RequestBody ConsumeRequestDTO request) {
        try {
            // 业务逻辑
            String result = consumeService.executeConsume(request);
            return ResponseDTO.ok(result);
        } catch (BusinessException e) {
            log.warn("消费失败: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("系统异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常");
        }
    }
}
```

### 重构后 (无异常处理代码)
```java
@RestController
public class ConsumeController {

    @PostMapping("/consume")
    public ResponseDTO<String> consume(@RequestBody @Valid ConsumeRequestDTO request) {
        // 直接执行业务逻辑，异常由GlobalExceptionHandler处理
        String result = consumeService.executeConsume(request);
        return ResponseDTO.ok(result);
    }
}
```

## ✅ 实施检查清单

### 1. 全局异常处理器创建
- [ ] 创建GlobalExceptionHandler类，使用@RestControllerAdvice注解
- [ ] 实现所有必要的异常处理方法
- [ ] 统一异常响应格式为ResponseDTO
- [ ] 规范化异常日志记录

### 2. Controller层重构
- [ ] 移除Controller中的重复try-catch块
- [ ] 在方法参数上添加@Valid或@Validated注解
- [ ] 简化Controller代码，只保留业务逻辑调用
- [ ] 确保异常处理100%覆盖

### 3. 异常处理验证
- [ ] 测试业务异常处理
- [ ] 测试参数验证异常处理
- [ ] 测试系统异常兜底处理
- [ ] 验证异常响应格式一致性

### 4. 安全性检查
- [ ] 生产环境避免敏感信息泄露
- [ ] 异常日志记录不包含敏感数据
- [ ] 异常信息用户友好化

## 🚀 实施流程

### 第一阶段: 创建全局异常处理器
1. 创建GlobalExceptionHandler类
2. 实现核心异常处理方法
3. 统一异常响应格式
4. 测试异常处理效果

### 第二阶段: Controller层重构
1. 识别有重复try-catch的Controller
2. 移除异常处理代码
3. 添加参数验证注解
4. 验证重构效果

### 第三阶段: 验证和优化
1. 全面测试异常处理
2. 优化异常消息
3. 完善日志记录
4. 性能优化

## 📊 成功指标

- **异常处理覆盖率**: 100%
- **Controller重复代码减少**: >80%
- **异常响应一致性**: 100%
- **用户友好错误消息**: >90%
- **异常日志规范性**: 100%

## 🔗 相关技能协作

- **compilation-error-specialist**: 编译错误修复专家
- **code-quality-protector**: 代码质量守护专家
- **four-tier-architecture-guardian**: 四层架构守护专家

## 🎯 质量承诺

**应用此技能后保证**:
- Controller层代码简化80%以上
- 异常处理100%统一规范
- 用户体验显著提升
- 系统维护成本大幅降低
- 企业级异常处理标准达成

---

**📞 调用方式**: 在项目中遇到异常处理问题时，使用 `Skill("global-exception-handler-specialist")` 获得专业的全局异常处理支持。

**⚠️ 重要提醒**: 此技能必须与repowiki规范严格配合，确保异常处理架构符合项目整体标准。