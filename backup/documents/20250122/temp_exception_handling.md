# 统一异常处理专家技能
## Exception Handling Specialist

**🎯 技能定位**: IOE-DREAM智慧园区统一异常处理专家，确保GlobalExceptionHandler的统一使用，实现企业级异常处理架构

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: 异常处理架构设计、异常处理规范化、容错集成、异常监控、故障排查
**📊 技能覆盖**: 异常处理架构 | 异常分类 | 容错集成 | TraceId追踪 | 监控告警 | 配置中心化

---

## 📋 技能概述

### **核心专长**
- **统一异常处理**: GlobalExceptionHandler使用规范和验证
- **异常分类体系**: BusinessException、SystemException、ParamException分类管理
- **容错集成**: Resilience4j与异常处理的深度集成
- **TraceId追踪**: 分布式追踪和异常链路分析
- **配置中心化**: Nacos统一管理异常和容错配置
- **监控告警**: 异常指标收集和告警规则配置

### **解决能力**
- **异常处理统一**: 确保所有微服务使用统一的异常处理器
- **架构违规修复**: 移除重复的异常处理实现
- **容错优化**: 异常处理与熔断、重试、限流机制的协调
- **可观测性增强**: 完整的异常追踪、监控和分析
- **故障排查指导**: 异常处理问题的快速定位和解决
- **团队培训**: 异常处理最佳实践培训和指导

---

## 🎯 统一异常处理架构

### 📋 异常处理器统一使用

**GlobalExceptionHandler使用规范**:
```java
// ✅ 正确示例 - 使用统一的GlobalExceptionHandler
// 1. 确保依赖common-service
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>ioedream-common-service</artifactId>
    <version>1.0.0</version>
</dependency>

// 2. 业务代码直接抛出标准异常
@Service
public class ConsumeServiceImpl implements ConsumeService {

    public ResponseDTO<ConsumeResultDTO> consume(ConsumeRequestDTO request) {
        // 业务验证失败，抛出业务异常
        if (!validateRequest(request)) {
            throw new BusinessException("INVALID_REQUEST", "请求参数无效");
        }

        // 系统错误，抛出系统异常
        try {
            return consumeManager.executeConsume(request);
        } catch (Exception e) {
            throw new SystemException("CONSUME_ERROR", "消费处理失败", e);
        }
    }
}

// ❌ 错误示例 - 重复实现异常处理器
@RestControllerAdvice  // 禁止重复实现！
public class ConsumeExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        // 这个逻辑应该在common-service的GlobalExceptionHandler中
    }
}
```

**异常检查清单**:
- [ ] 确认微服务依赖common-service
- [ ] 确认未重复实现@ControllerAdvice
- [ ] 确认使用标准异常类
- [ ] 确认Controller层无异常吞噬

### 📋 异常分类标准

**BusinessException（业务异常）**:
```java
// 用于处理业务逻辑中的预期错误
throw new BusinessException("USER_NOT_FOUND", "用户不存在");
throw new BusinessException("INVALID_AMOUNT", "无效的消费金额");
throw new BusinessException("INSUFFICIENT_BALANCE", "账户余额不足");
```

**SystemException（系统异常）**:
```java
// 用于处理系统级错误
throw new SystemException("DATABASE_ERROR", "数据库连接失败", e);
throw new SystemException("NETWORK_ERROR", "网络请求超时", e);
throw new SystemException("EXTERNAL_SERVICE_ERROR", "第三方服务错误", e);
```

**ParamException（参数异常）**:
```java
// 用于处理HTTP请求参数错误
throw new ParamException("PARAM_MISSING", "缺少必需参数");
throw new ParamException("PARAM_INVALID", "参数格式无效");
throw new ParamException("VALIDATION_ERROR", "参数验证失败");
```

---

## 🔧 容错集成规范

### 📋 Resilience4j与异常处理集成

**容错配置示例**:
```java
@Service
public class ExternalServiceClient {

    @Retry(name = "external-service", fallbackMethod = "fallback")
    @CircuitBreaker(name = "external-service")
    @RateLimiter(name = "external-service")
    public ResponseDTO<String> callExternal(RequestDTO request) {
        try {
            return externalService.call(request);
        } catch (BusinessException e) {
            // 业务异常直接向上抛，由GlobalExceptionHandler处理
            throw e;
        } catch (Exception e) {
            // 系统异常包装后向上抛
            throw new SystemException("EXTERNAL_CALL_FAILED", "外部服务调用失败", e);
        }
    }

    public ResponseDTO<String> fallback(RequestDTO request, Exception e) {
        log.warn("[降级] 外部服务调用失败，使用降级方案, traceId={}", MDC.get("traceId"), e);
        return ResponseDTO.ok("降级响应");
    }
}
```

**Resilience4j配置中心化**:
```yaml
# Nacos配置中心：resilience4j-common.yml
resilience4j:
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1000ms
        exponentialBackoffMultiplier: 2
        ignoreExceptions:
          - net.lab1024.sa.common.exception.BusinessException
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        slidingWindowSize: 100
  ratelimiter:
    configs:
      default:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
```

---

## 📊 监控与告警

### 📋 异常指标收集

**Micrometer异常指标配置**:
```java
@Component
public class ExceptionMetrics {

    private final Counter businessExceptionCounter;
    private final Counter systemExceptionCounter;

    public ExceptionMetrics(MeterRegistry meterRegistry) {
        this.businessExceptionCounter = Counter.builder("business.exception.count")
            .description("业务异常计数")
            .register(meterRegistry);
        this.systemExceptionCounter = Counter.builder("system.exception.count")
            .description("系统异常计数")
            .register(meterRegistry);
    }

    @EventListener
    public void handleBusinessException(BusinessExceptionEvent event) {
        businessExceptionCounter.increment(
            Tags.of("code", event.getCode(), "module", event.getModule())
        );
    }
}
```

**告警规则配置**:
```yaml
# Prometheus告警规则
groups:
  - name: exception.alerts
    rules:
      - alert: BusinessExceptionRate
        expr: rate(business_exception_count_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "业务异常率过高"
          description: "服务 {{ $labels.application }} 业务异常率超过10%"

      - alert: SystemExceptionSpike
        expr: rate(system_exception_count_total[1m]) > 0.05
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "系统异常激增"
          description: "服务 {{ $labels.application }} 系统异常激增"
```

---

## 🔍 故障排查指南

### 📋 常见问题诊断

**问题1: GlobalExceptionHandler不生效**
```bash
# 检查步骤
1. 确认依赖common-service
   grep -r "ioedream-common-service" microservices/*/pom.xml

2. 检查重复的异常处理器
   grep -r "@RestControllerAdvice" microservices/

3. 验证包扫描路径
   grep -r "@ComponentScan" microservices/
```

**问题2: 异常响应格式不统一**
```bash
# 检查异常吞噬
grep -r "catch.*Exception.*{" microservices/ --include="*.java" | grep -v "throw"

# 检查ResponseDTO使用
grep -r "ResponseDTO.error" microservices/
```

**问题3: TraceId丢失**
```java
// 检查MDC配置
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception e) {
        String traceId = MDC.get("traceId");  // 确保获取TraceId
        log.error("[异常] traceId={}", traceId, e);
        // ...
    }
}
```

---

## 📚 最佳实践

### ✅ 推荐做法
- 使用统一的GlobalExceptionHandler
- 按异常类型分类处理
- 所有异常包含TraceId追踪
- 对用户提供友好错误信息
- 与Resilience4j集成实现容错
- 配置中心化管理异常规则

### ❌ 禁止做法
- 各微服务重复实现异常处理器
- 在Controller层吞噬异常
- 直接暴露系统内部错误信息
- 缺少TraceId的异常处理
- 硬编码异常配置
- 混淆业务异常和系统异常

---

## 🎯 技能检查清单

### 🔍 代码审查检查项
- [ ] 微服务依赖common-service
- [ ] 无重复的@ControllerAdvice实现
- [ ] 使用标准异常分类
- [ ] Controller层无异常吞噬
- [ ] 异常包含TraceId追踪

### 🔧 配置检查项
- [ ] Resilience4j配置在Nacos中
- [ ] 异常指标通过Micrometer收集
- [ ] 告警规则配置正确
- [ ] 日志格式包含异常信息

### 📊 监控检查项
- [ ] 异常统计指标正常
- [ ] 告警规则生效
- [ ] TraceId在日志中完整
- [ ] 异常响应格式统一

---

## 🔄 技能执行流程

### 📋 异常处理评估流程
```bash
# 1. 异常处理现状分析
analyze-exception-handling-architecture

# 2. 异常分类检查
verify-exception-classification

# 3. GlobalExceptionHandler使用检查
check-global-exception-handler-usage

# 4. 容错集成验证
validate-resilience4j-integration

# 5. 监控配置检查
verify-monitoring-configuration
```

### 📋 异常处理优化流程
```bash
# 1. 移除重复异常处理器
remove-duplicate-exception-handlers

# 2. 统一异常分类使用
standardize-exception-types

# 3. 配置中心化迁移
migrate-to-centralized-configuration

# 4. 监控告警配置
setup-monitoring-alerts

# 5. 测试验证执行
execute-comprehensive-testing
```

---

**📋 技能等级评估**:
- **初级**: 理解异常处理概念，能使用基本异常类型
- **中级**: 掌握异常分类，能集成容错机制
- **高级**: 精通异常处理架构，能设计统一方案
- **专家**: 企业级异常处理专家，能指导团队实践

**🎯 技能维护**:
- **定期更新**: 跟踪Spring Boot异常处理最佳实践
- **案例积累**: 收集项目中的异常处理案例
- **团队培训**: 定期进行异常处理规范培训
- **工具优化**: 持续优化异常处理检查工具

---

**📞 技能支持**:
- **架构咨询**: 异常处理架构设计咨询
- **问题诊断**: 异常处理问题快速诊断
- **最佳实践**: 企业级异常处理最佳实践分享
- **培训指导**: 团队异常处理规范培训