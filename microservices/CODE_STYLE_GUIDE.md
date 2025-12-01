# IOE-DREAM 微服务代码风格规范

## 📋 规范版本
**版本**: v1.0.0  
**生效日期**: 2025-01-30  
**适用范围**: 所有微服务

---

## 1. 注释规范

### 1.1 类级注释

**格式要求**:
```java
/**
 * 类功能描述
 * 
 * 详细说明类的职责和主要功能
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public class ExampleService {
    // ...
}
```

**必须包含**:
- 类功能描述（第一行）
- 详细说明（可选）
- @author 标注
- @version 版本号
- @since 创建日期

### 1.2 方法级注释

**格式要求**:
```java
/**
 * 方法功能描述
 *
 * 详细说明方法的作用、参数含义、返回值含义
 *
 * @param paramName 参数说明
 * @return 返回值说明
 * @throws ExceptionType 异常说明
 * @since 2025-01-30
 */
public ReturnType methodName(ParamType paramName) throws ExceptionType {
    // ...
}
```

**必须包含**:
- 方法功能描述
- @param 所有参数说明
- @return 返回值说明
- @throws 可能抛出的异常
- @since 创建日期

### 1.3 字段注释

**格式要求**:
```java
/**
 * 字段说明
 */
private String fieldName;
```

---

## 2. 命名规范

### 2.1 类命名
- **Controller**: `XxxController`
- **Service**: `XxxService` / `XxxServiceImpl`
- **Entity**: `XxxEntity`
- **DTO**: `XxxDTO` / `XxxRequestDTO` / `XxxResponseDTO`
- **VO**: `XxxVO`
- **Util**: `XxxUtil`
- **Config**: `XxxConfig`

### 2.2 方法命名
- **查询**: `getXxx()`, `listXxx()`, `findXxx()`, `queryXxx()`
- **新增**: `createXxx()`, `addXxx()`, `saveXxx()`
- **修改**: `updateXxx()`, `modifyXxx()`
- **删除**: `deleteXxx()`, `removeXxx()`
- **判断**: `isXxx()`, `hasXxx()`, `existsXxx()`

### 2.3 变量命名
- **常量**: 全大写，下划线分隔 `MAX_SIZE`
- **静态变量**: 小驼峰 `staticValue`
- **实例变量**: 小驼峰 `instanceValue`
- **局部变量**: 小驼峰 `localValue`
- **布尔变量**: `isXxx`, `hasXxx`, `canXxx`

---

## 3. 代码格式

### 3.1 缩进
- 使用 **4个空格** 缩进
- 不使用Tab

### 3.2 行长度
- 每行代码不超过 **120个字符**
- 超长行需要换行，保持对齐

### 3.3 空行
- 类成员之间：1个空行
- 方法之间：1个空行
- 逻辑块之间：1个空行

### 3.4 大括号
- 左大括号不换行
- 右大括号单独一行

```java
// ✅ 正确
if (condition) {
    // code
}

// ❌ 错误
if (condition)
{
    // code
}
```

---

## 4. 日志规范

### 4.1 日志级别
- **DEBUG**: 调试信息，开发环境使用
- **INFO**: 关键业务操作，生产环境使用
- **WARN**: 警告信息，需要关注但不影响运行
- **ERROR**: 错误信息，需要立即处理

### 4.2 日志格式
```java
log.info("操作描述: 参数1={}, 参数2={}", param1, param2);
log.error("错误描述: {}", errorMessage, exception);
```

### 4.3 日志内容
- 必须包含：操作类型、关键参数、结果状态
- 禁止包含：敏感信息（密码、token等）

---

## 5. 异常处理

### 5.1 异常类型
- **业务异常**: 使用 `BusinessException`
- **系统异常**: 使用 `SystemException`
- **参数异常**: 使用 `IllegalArgumentException`

### 5.2 异常处理
```java
try {
    // 业务逻辑
} catch (BusinessException e) {
    log.error("业务异常: {}", e.getMessage(), e);
    throw e;
} catch (Exception e) {
    log.error("系统异常", e);
    throw new SystemException("系统异常", e);
}
```

---

## 6. 代码质量要求

### 6.1 必须遵循
- ✅ 所有公共方法必须有注释
- ✅ 所有类必须有类级注释
- ✅ 异常必须记录日志
- ✅ 关键业务操作必须记录日志

### 6.2 禁止事项
- ❌ 禁止使用 `System.out.println()`
- ❌ 禁止捕获异常后不处理
- ❌ 禁止硬编码敏感信息
- ❌ 禁止使用魔法数字

---

## 7. 工具类使用

### 7.1 统一使用common模块工具类
- `SmartBeanUtil` - Bean转换
- `SmartPageUtil` - 分页处理
- `SmartStringUtil` - 字符串处理
- `SmartDateUtil` - 日期处理
- `RedisUtil` - Redis操作

### 7.2 禁止创建重复工具类
- 所有工具类应在common模块中统一管理
- 各服务不应创建自己的工具类

---

## 8. 检查清单

### 提交前检查
- [ ] 代码符合命名规范
- [ ] 所有方法都有注释
- [ ] 异常处理完善
- [ ] 日志记录完整
- [ ] 无硬编码敏感信息
- [ ] 无重复代码
- [ ] 无未使用的导入

---

**规范维护**: IOE-DREAM Team  
**最后更新**: 2025-01-30

