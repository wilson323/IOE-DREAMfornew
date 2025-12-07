# Phase 3 Task 3.2: 异常处理完善执行计划

**制定日期**: 2025-12-03  
**执行目标**: 完善全局异常处理器和异常处理规范  
**计划状态**: 📋 准备执行  
**优先级**: 🟠 P1

---

## 📊 Task 3.2 任务概览

| 子任务 | 目标 | 工作量 | 优先级 | 状态 |
|--------|------|--------|--------|------|
| **Task 3.2.1** | 创建全局异常处理器 | 2-3小时 | 🟠 P1 | ⏳ 待开始 |
| **Task 3.2.2** | 替换RuntimeException为业务异常 | 2-3小时 | 🟠 P1 | ⏳ 待开始 |
| **Task 3.2.3** | 统一异常处理规范 | 1-2小时 | 🟠 P1 | ⏳ 待开始 |

**总工作量**: 5-8小时  
**预计完成时间**: 1个工作日

---

## 🎯 Task 3.2.1: 创建全局异常处理器

### 目标
为每个微服务创建独立的全局异常处理器，统一异常处理规范。

### 需要创建的GlobalExceptionHandler

1. **ioedream-consume-service** - `ConsumeGlobalExceptionHandler`
2. **ioedream-attendance-service** - `AttendanceGlobalExceptionHandler`
3. **ioedream-access-service** - `AccessGlobalExceptionHandler`
4. **ioedream-common-service** - `CommonGlobalExceptionHandler`

### 标准模板

```java
@Slf4j
@RestControllerAdvice
public class XxxGlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常 - @Valid
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("参数验证异常: {}", e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, String> errors = fieldErrors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "参数验证失败"
                ));

        return ResponseDTO.error(400, "参数验证失败", errors);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Map<String, String>> handleBindException(BindException e) {
        log.warn("参数绑定异常: {}", e.getMessage());

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, String> errors = fieldErrors.stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "参数绑定失败"
                ));

        return ResponseDTO.error(400, "参数绑定失败", errors);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("约束违反异常: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(fieldName, violation.getMessage());
        });

        return ResponseDTO.error(400, "约束验证失败", errors);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDTO<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage(), e);
        return ResponseDTO.error(400, e.getMessage());
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return ResponseDTO.error(500, "系统内部错误: " + e.getMessage());
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseDTO.error(500, "系统异常，请联系管理员");
    }
}
```

---

## 🎯 Task 3.2.2: 替换RuntimeException为业务异常

### 目标
将Service层中的`throw new RuntimeException`替换为业务异常（`BusinessException`或`ConsumeBusinessException`）。

### 需要修复的文件（ioedream-consume-service）

1. `ReportAnalysisService.java` - 2处
2. `ReportExportService.java` - 2处
3. `ReportServiceImpl.java` - 4处
4. `RechargeService.java` - 3处
5. `RefundServiceImpl.java` - 3处
6. `IndexOptimizationService.java` - 2处

**总计**: 16处需要修复

### 修复规范

```java
// ❌ 错误示例
catch (Exception e) {
    log.error("操作失败", e);
    throw new RuntimeException("操作失败: " + e.getMessage(), e);
}

// ✅ 正确示例
catch (BusinessException e) {
    throw e;
} catch (Exception e) {
    log.error("操作失败", e);
    throw new ConsumeBusinessException("OPERATION_ERROR", "操作失败: " + e.getMessage(), e);
}
```

---

## 🎯 Task 3.2.3: 统一异常处理规范

### 目标
统一各微服务的异常处理规范，确保：
1. 所有微服务都有全局异常处理器
2. Service层统一使用业务异常
3. Controller层移除@ExceptionHandler（由全局处理器统一处理）
4. 异常日志记录规范统一

---

## 📋 执行步骤

### Step 1: 创建全局异常处理器
1. 为ioedream-consume-service创建`ConsumeGlobalExceptionHandler`
2. 为ioedream-attendance-service创建`AttendanceGlobalExceptionHandler`
3. 为ioedream-access-service创建`AccessGlobalExceptionHandler`
4. 为ioedream-common-service创建`CommonGlobalExceptionHandler`

### Step 2: 修复RuntimeException
1. 扫描所有Service实现类
2. 替换`throw new RuntimeException`为业务异常
3. 确保异常信息完整

### Step 3: 清理Controller中的@ExceptionHandler
1. 移除Controller中的@ExceptionHandler方法
2. 确保异常由全局处理器统一处理

### Step 4: 验证和测试
1. 编译验证
2. 异常处理测试

---

**Phase 3 Task 3.2 状态**: ⏳ **准备执行**

