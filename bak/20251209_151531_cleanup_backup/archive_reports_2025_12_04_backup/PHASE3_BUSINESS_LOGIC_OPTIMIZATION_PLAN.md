# Phase 3: 业务逻辑优化执行计划

**制定日期**: 2025-12-03  
**执行目标**: 完善事务管理、异常处理、参数验证规范  
**计划状态**: 📋 准备执行  
**优先级**: 🟠 P1

---

## 📊 Phase 3 任务概览

| 任务 | 目标 | 工作量 | 优先级 | 状态 |
|------|------|--------|--------|------|
| **Task 3.1** | 事务管理规范全面检查 | 3-4小时 | 🟠 P1 | ⏳ 待开始 |
| **Task 3.2** | 异常处理完善 | 3-4小时 | 🟠 P1 | ⏳ 待开始 |
| **Task 3.3** | 参数验证完善 | 2-3小时 | 🟠 P1 | ⏳ 待开始 |

**总工作量**: 8-11小时  
**预计完成时间**: 1-2个工作日

---

## 🎯 Task 3.1: 事务管理规范全面检查

### 目标
确保所有Service和DAO的事务注解正确，符合CLAUDE.md规范。

### 执行步骤

#### Step 3.1.1: 扫描Service层事务注解

**检查项**:
- ✅ Service实现类是否有类级别`@Transactional(rollbackFor = Exception.class)`
- ✅ 查询方法是否使用`@Transactional(readOnly = true)`
- ✅ 写操作方法是否继承类级别事务或显式声明

**规范模板**:
```java
// ✅ 正确的Service层事务管理
@Service
@Transactional(rollbackFor = Exception.class)  // 类级别，所有写操作
public class UserServiceImpl implements UserService {
    
    @Resource
    private UserDao userDao;
    
    // 写操作：自动继承类级别事务
    @Override
    public Long createUser(UserAddForm form) {
        UserEntity user = new UserEntity();
        // ...
        userDao.insert(user);
        return user.getId();
    }
    
    // 读操作：标记为只读事务（推荐）
    @Override
    @Transactional(readOnly = true)
    public UserVO getUserById(Long id) {
        UserEntity user = userDao.selectById(id);
        return convertToVO(user);
    }
}
```

#### Step 3.1.2: 扫描DAO层事务注解

**检查项**:
- ✅ 查询方法是否使用`@Transactional(readOnly = true)`
- ✅ 写操作方法是否使用`@Transactional(rollbackFor = Exception.class)`

**规范模板**:
```java
// ✅ 正确的DAO层事务管理
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    
    // 查询操作：只读事务
    @Transactional(readOnly = true)
    UserEntity selectByUsername(@Param("username") String username);
    
    // 写操作：完整事务
    @Transactional(rollbackFor = Exception.class)
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
```

#### Step 3.1.3: 修复不符合规范的事务注解

**修复策略**:
1. 为缺少类级别事务的Service添加`@Transactional(rollbackFor = Exception.class)`
2. 为查询方法添加`@Transactional(readOnly = true)`
3. 确保DAO层方法有适当的事务注解

### 预期结果
- ✅ 100% Service类有正确的事务注解
- ✅ 100% DAO方法有适当的事务注解
- ✅ 事务边界正确
- ✅ 异常回滚正常

---

## 🎯 Task 3.2: 异常处理完善

### 目标
确保异常处理规范完整，符合CLAUDE.md规范。

### 执行步骤

#### Step 3.2.1: 检查全局异常处理器

**检查项**:
- ✅ 每个微服务是否有全局异常处理器
- ✅ 是否处理业务异常、参数验证异常、系统异常
- ✅ 异常响应格式是否统一

**标准全局异常处理器**:
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, message={}", e.getCode(), e.getMessage());
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }
    
    // 参数验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[参数验证异常] message={}", message);
        return ResponseDTO.error("VALIDATION_ERROR", message);
    }
    
    // 系统异常
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        log.error("[系统异常] error={}", e.getMessage(), e);
        return ResponseDTO.error("SYSTEM_ERROR", "系统繁忙，请稍后重试");
    }
}
```

#### Step 3.2.2: 检查业务异常使用

**检查项**:
- ✅ Service层是否使用自定义BusinessException
- ✅ 异常信息是否详细明确
- ✅ 是否有吞掉异常的情况

**规范模板**:
```java
// ✅ 正确的异常处理
try {
    // 业务逻辑
} catch (BusinessException e) {
    log.warn("[业务操作失败] operation={}, reason={}", operation, e.getMessage());
    throw e;  // 重新抛出
} catch (Exception e) {
    log.error("[系统异常] operation={}, error={}", operation, e.getMessage(), e);
    throw new SystemException("SYSTEM_ERROR", "操作失败", e);
}
```

#### Step 3.2.3: 检查日志记录

**检查项**:
- ✅ catch块中是否有日志记录
- ✅ 日志级别是否合适（ERROR/WARN/INFO）
- ✅ 日志信息是否包含关键上下文

### 预期结果
- ✅ 所有服务有全局异常处理器
- ✅ 异常处理规范统一
- ✅ 日志记录完整
- ✅ 无异常被吞掉

---

## 🎯 Task 3.3: 参数验证完善

### 目标
确保关键业务参数验证完整，符合CLAUDE.md规范。

### 执行步骤

#### Step 3.3.1: 检查Controller参数验证

**检查项**:
- ✅ POST/PUT方法是否使用`@Valid`注解
- ✅ 参数是否为Form类或DTO类
- ✅ 是否有参数验证

**规范要求**:
```java
// ✅ 正确的参数验证
@PostMapping
public ResponseDTO<Long> createUser(@Valid @RequestBody UserAddForm form) {
    return ResponseDTO.ok(userService.createUser(form));
}
```

#### Step 3.3.2: 检查Form类验证注解

**检查项**:
- ✅ Form类字段是否有验证注解（@NotNull、@NotBlank、@Size等）
- ✅ 验证消息是否友好明确
- ✅ 是否覆盖所有必填字段

**标准Form类**:
```java
@Data
public class UserAddForm {
    
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名最长50字符")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20字符")
    private String password;
    
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}
```

#### Step 3.3.3: 检查Service层业务规则验证

**检查项**:
- ✅ Service层是否有业务规则验证
- ✅ 验证逻辑是否完整
- ✅ 验证失败是否抛出BusinessException

**规范模板**:
```java
// ✅ Service层业务规则验证
@Override
public Long createUser(UserAddForm form) {
    // 业务规则验证
    if (userDao.selectByUsername(form.getUsername()) != null) {
        throw new BusinessException("USER_EXISTS", "用户名已存在");
    }
    
    Department dept = departmentDao.selectById(form.getDepartmentId());
    if (dept == null) {
        throw new BusinessException("DEPARTMENT_NOT_FOUND", "部门不存在");
    }
    
    // 创建用户
    UserEntity user = new UserEntity();
    // ...
    userDao.insert(user);
    return user.getId();
}
```

### 预期结果
- ✅ 所有Controller方法有@Valid验证
- ✅ 所有Form类有完整的验证注解
- ✅ Service层有业务规则验证
- ✅ 验证错误信息友好明确

---

## 📋 执行顺序

1. **Task 3.1**: 事务管理规范全面检查
2. **Task 3.2**: 异常处理完善
3. **Task 3.3**: 参数验证完善

**注意**: 三个任务可以并行执行，但建议按顺序执行以确保依赖关系清晰。

---

## ✅ 完成标准

### Task 3.1 完成标准
- ✅ 100% Service类有正确的事务注解
- ✅ 100% DAO方法有适当的事务注解
- ✅ 事务边界正确
- ✅ 编译通过

### Task 3.2 完成标准
- ✅ 所有服务有全局异常处理器
- ✅ 异常处理规范统一
- ✅ 日志记录完整
- ✅ 无异常被吞掉

### Task 3.3 完成标准
- ✅ 所有Controller方法有@Valid验证
- ✅ 所有Form类有完整的验证注解
- ✅ Service层有业务规则验证
- ✅ 验证错误信息友好明确

---

**Phase 3 状态**: ⏳ **准备执行**

