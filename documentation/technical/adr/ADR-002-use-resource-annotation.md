# ADR-002: 依赖注入使用@Resource注解

## 状态
**已接受 (Accepted)** - 2025-11-14

## 背景
Spring框架提供了多种依赖注入方式：
- @Autowired（Spring特有）
- @Resource（Jakarta EE标准）
- @Inject（Jakarta EE标准）
- 构造器注入

需要统一选择一种方式，确保代码风格一致。

## 决策
**强制使用@Resource注解进行依赖注入，禁止使用@Autowired**

## 理由

### 1. 标准化
- @Resource是Jakarta EE标准（原Java EE），不依赖Spring框架
- @Autowired是Spring特有注解，与框架强耦合
- 使用标准注解有利于代码移植性

### 2. 匹配策略清晰
```java
// @Resource匹配策略：先按名称，再按类型
@Resource
private DeviceService deviceService;  // 先查找名为"deviceService"的Bean

// @Autowired匹配策略：先按类型，再按名称
@Autowired
private DeviceService deviceService;  // 先查找DeviceService类型的Bean
```

### 3. Spring Boot 3兼容性
- Spring Boot 3使用Jakarta EE规范
- @Resource属于jakarta.annotation包，完全兼容
- @Autowired仍可用，但为保持一致性统一使用@Resource

### 4. AI代码生成友好
- 明确的规范使AI能够自动遵守
- 自动化检查脚本易于实现
- 代码审查标准清晰

## 约束

### 强制约束
- ✅ 必须使用@Resource进行依赖注入
- ❌ 禁止使用@Autowired
- ❌ 禁止使用@Inject
- ⚠️ 特殊情况（集合注入）可使用构造器注入

### 命名约束
- Bean名称使用小驼峰：deviceService、deviceManager
- 字段名称与Bean名称保持一致

## 实施示例

### 正确示例 ✅

```java
@RestController
public class DeviceController {
    
    @Resource
    private DeviceService deviceService;  // ✅ 正确
    
    @Resource
    private DeviceManager deviceManager;  // ✅ 正确
}
```

### 错误示例 ❌

```java
@RestController
public class DeviceController {
    
    @Autowired  // ❌ 错误：禁止使用@Autowired
    private DeviceService deviceService;
    
    @Inject  // ❌ 错误：禁止使用@Inject
    private DeviceManager deviceManager;
}
```

## 自动化验证

### 检查脚本
```bash
# 检查是否使用了@Autowired
find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l
# 结果应该为0

# 检查是否正确使用@Resource
find . -name "*.java" -exec grep -l "@Resource" {} \;
```

### Pre-commit Hook
```bash
#!/bin/bash
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现使用@Autowired，请改用@Resource"
    exit 1
fi
```

## 相关决策
- [ADR-001: 使用四层架构](ADR-001-use-four-layer-architecture.md)
- [ADR-003: 统一响应格式使用ResponseDTO](ADR-003-use-response-dto.md)

## 参考资料
- [Spring Framework文档](https://docs.spring.io/spring-framework/docs/current/reference/html/)
- [Jakarta EE规范](https://jakarta.ee/specifications/)
