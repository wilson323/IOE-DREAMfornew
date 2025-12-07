# ResponseDTO 统一使用规范

**版本**: v1.0.0  
**更新日期**: 2025-12-02  
**适用范围**: IOE-DREAM 所有微服务

---

## 📋 核心规范

### 1. 统一导入路径（强制执行）

**✅ 正确导入**:
```java
import net.lab1024.sa.common.dto.ResponseDTO;
```

**❌ 禁止使用**:
```java
import net.lab1024.sa.common.domain.ResponseDTO;  // 已废弃
```

### 2. ResponseDTO 类位置

**标准位置**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/dto/ResponseDTO.java`

**包路径**: `net.lab1024.sa.common.dto.ResponseDTO`

---

## 🎯 成功响应方法

### 1. 成功响应（带数据）
```java
// 返回数据，默认消息"操作成功"
ResponseDTO<UserVO> response = ResponseDTO.ok(userVO);

// 返回数据，自定义消息
ResponseDTO<UserVO> response = ResponseDTO.ok("用户查询成功", userVO);

// 返回数据，自定义状态码和消息
ResponseDTO<UserVO> response = ResponseDTO.ok(200, "用户查询成功", userVO);
```

### 2. 成功响应（无数据）
```java
// 无数据，默认消息"操作成功"
ResponseDTO<Void> response = ResponseDTO.ok();
```

### 3. 用户信息响应
```java
// 用户信息响应（默认消息"用户信息获取成功"）
ResponseDTO<UserVO> response = ResponseDTO.userOk(userVO);

// 用户信息响应（自定义消息）
ResponseDTO<UserVO> response = ResponseDTO.userOk("用户信息获取成功", userVO);
```

---

## ❌ 错误响应方法

### 1. 错误响应（整数错误码）

```java
// 使用整数错误码
ResponseDTO<Void> response = ResponseDTO.error(400, "参数错误");
ResponseDTO<Void> response = ResponseDTO.error(401, "未授权");
ResponseDTO<Void> response = ResponseDTO.error(404, "资源不存在");
ResponseDTO<Void> response = ResponseDTO.error(500, "服务器内部错误");
```

### 2. 错误响应（字符串错误码）✅ 新增

```java
// 使用字符串错误码（优先尝试解析为整数，失败则使用hashCode生成）
ResponseDTO<Void> response = ResponseDTO.error("UNAUTHORIZED", "访问被拒绝，请先进行身份认证");
ResponseDTO<Void> response = ResponseDTO.error("ACCESS_DENIED", "访问被拒绝，您没有权限执行此操作");
ResponseDTO<Void> response = ResponseDTO.error("SERVICE_CALL_ERROR", "服务调用失败");

// 如果错误码是数字字符串，会直接解析为整数
ResponseDTO<Void> response = ResponseDTO.error("400", "参数错误");  // 等同于 error(400, "参数错误")
```

**错误码转换规则**:
- **优先解析**: 如果错误码是数字字符串（如"400"），直接解析为整数错误码
- **HashCode生成**: 如果无法解析为整数（如"UNAUTHORIZED"），使用hashCode生成错误码
- **错误码范围**: 生成的错误码在40000-139999范围内，避免与HTTP状态码冲突

### 3. 错误响应（带数据）

```java
// 错误响应带数据（整数错误码）
ResponseDTO<Map<String, Object>> response = ResponseDTO.error(500, "服务异常", errorData);

// 错误响应带数据（字符串错误码）
ResponseDTO<Map<String, Object>> response = ResponseDTO.error("SERVICE_ERROR", "服务异常", errorData);
```

### 4. 便捷错误方法

```java
// 业务失败响应（默认400错误码）
ResponseDTO<Void> response = ResponseDTO.error("操作失败");

// 参数错误响应
ResponseDTO<Void> response = ResponseDTO.errorParam("用户名不能为空");

// 未授权响应
ResponseDTO<Void> response = ResponseDTO.errorUnauthorized("请先登录");

// 禁止访问响应
ResponseDTO<Void> response = ResponseDTO.errorForbidden("无权限访问");

// 资源不存在响应
ResponseDTO<Void> response = ResponseDTO.errorNotFound("用户不存在");

// 服务器错误响应
ResponseDTO<Void> response = ResponseDTO.errorService("系统异常");
```

---

## 📝 使用示例

### Controller层使用示例

```java
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 查询用户信息
     */
    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUser(@PathVariable Long id) {
        try {
            UserVO user = userService.getUserById(id);
            if (user == null) {
                return ResponseDTO.errorNotFound("用户不存在");
            }
            return ResponseDTO.ok(user);
        } catch (BusinessException e) {
            // BusinessException.getCode()返回Integer
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return ResponseDTO.errorService("查询用户失败");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseDTO<Long> createUser(@Valid @RequestBody UserAddForm form) {
        try {
            Long userId = userService.createUser(form);
            return ResponseDTO.ok(userId, "用户创建成功");
        } catch (BusinessException e) {
            return ResponseDTO.error(e.getCode(), e.getMessage());
        }
    }
}
```

### 异常处理器使用示例

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理BusinessException（Integer错误码）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException e) {
        // BusinessException.getCode()返回Integer
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理ConsumeBusinessException（String错误码）
     */
    @ExceptionHandler(ConsumeBusinessException.class)
    public ResponseDTO<Void> handleConsumeBusinessException(ConsumeBusinessException e) {
        // ConsumeBusinessException.getCode()返回String
        return ResponseDTO.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = extractErrors(e);
        return ResponseDTO.errorParam("参数验证失败").data(errors);
    }
}
```

### Manager层使用示例

```java
public class UserManager {

    /**
     * 获取用户信息（带缓存）
     */
    public UserEntity getUserWithCache(Long userId) {
        // Manager层不返回ResponseDTO，返回业务对象
        // ResponseDTO只在Controller层和异常处理器中使用
        return userDao.selectById(userId);
    }
}
```

---

## ⚠️ 重要注意事项

### 1. 错误码类型选择

| 场景 | 推荐方法 | 示例 |
|------|---------|------|
| **HTTP状态码** | `error(Integer code, String message)` | `error(400, "参数错误")` |
| **业务错误码（字符串）** | `error(String code, String message)` | `error("USER_NOT_FOUND", "用户不存在")` |
| **业务错误码（数字字符串）** | `error(String code, String message)` | `error("40001", "用户不存在")` |

### 2. BusinessException错误码类型

**microservices-common中的BusinessException**:
- `getCode()`返回`Integer`
- 使用`ResponseDTO.error(e.getCode(), e.getMessage())`

**业务异常（如ConsumeBusinessException）**:
- `getCode()`返回`String`
- 使用`ResponseDTO.error(e.getCode(), e.getMessage())`

### 3. 错误响应带数据

**适用场景**: 需要返回错误信息的同时，返回详细的错误数据

```java
// 健康检查失败时返回详细错误信息
Map<String, Object> errorData = new HashMap<>();
errorData.put("status", "DOWN");
errorData.put("error", e.getMessage());
return ResponseDTO.error(500, "服务异常", errorData);
```

### 4. 禁止在Manager层返回ResponseDTO

**架构规范**: Manager层返回业务对象，不返回ResponseDTO

```java
// ✅ 正确：Manager层返回业务对象
public UserEntity getUserById(Long id) {
    return userDao.selectById(id);
}

// ❌ 错误：Manager层不应返回ResponseDTO
public ResponseDTO<UserEntity> getUserById(Long id) {
    return ResponseDTO.ok(userDao.selectById(id));
}
```

---

## 🔍 错误码转换示例

### 字符串错误码转换示例

```java
// 示例1：数字字符串（直接解析）
ResponseDTO.error("400", "参数错误")
// 转换结果：code = 400

// 示例2：非数字字符串（使用hashCode）
ResponseDTO.error("UNAUTHORIZED", "未授权")
// 转换结果：code = Math.abs("UNAUTHORIZED".hashCode() % 100000) + 40000
// 范围：40000-139999

// 示例3：业务错误码（使用hashCode）
ResponseDTO.error("USER_NOT_FOUND", "用户不存在")
// 转换结果：code = Math.abs("USER_NOT_FOUND".hashCode() % 100000) + 40000
```

---

## 📊 ResponseDTO字段说明

| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| `code` | `Integer` | 响应码（200=成功，400-599=错误） | `200`, `400`, `500` |
| `message` | `String` | 响应消息 | `"操作成功"`, `"参数错误"` |
| `data` | `T` | 响应数据（泛型） | `UserVO`, `List<UserVO>` |
| `timestamp` | `Long` | 时间戳（毫秒） | `1701234567890` |
| `traceId` | `String` | 追踪ID（可选） | `"trace-123456"` |

---

## ✅ 判断方法

```java
ResponseDTO<UserVO> response = userService.getUser(1L);

// 判断是否成功
if (response.isSuccess()) {
    // 处理成功逻辑
    UserVO user = response.getData();
}

// 判断是否失败
if (response.isError()) {
    // 处理失败逻辑
    String errorMessage = response.getMessage();
}

// 判断是否为用户错误（400/401/403）
if (response.isUserError()) {
    // 处理用户错误逻辑
}
```

---

## 🚫 禁止事项

### 1. 禁止使用旧版本导入路径
```java
// ❌ 禁止
import net.lab1024.sa.common.domain.ResponseDTO;

// ✅ 正确
import net.lab1024.sa.common.dto.ResponseDTO;
```

### 2. 禁止在Manager层返回ResponseDTO
```java
// ❌ 禁止
public ResponseDTO<UserEntity> getUser(Long id) {
    return ResponseDTO.ok(userDao.selectById(id));
}

// ✅ 正确
public UserEntity getUser(Long id) {
    return userDao.selectById(id);
}
```

### 3. 禁止使用已废弃的方法
```java
// ❌ 禁止（如果存在）
response.getMsg();  // 应使用 getMessage()
response.getOk();   // 应使用 isSuccess()
```

---

## 📚 相关文档

- [CLAUDE.md - 全局架构规范](../CLAUDE.md)
- [ResponseDTO统一化修复总结报告](../../RESPONSE_DTO_UNIFICATION_SUMMARY.md)
- [错误处理规范](./错误处理.md)

---

**最后更新**: 2025-12-02  
**维护人**: IOE-DREAM 架构委员会

