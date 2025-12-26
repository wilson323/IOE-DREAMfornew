# Spring配置键一致性整改指南

## 📋 概述

本文档记录IOE-DREAM项目全局配置层根因级整改计划，旨在彻底消除三类配置问题：

1. **YAML_DEPRECATED_ERROR**: 已弃用的Prometheus导出键
2. **YAML_UNKNOWN_PROPERTY**: 未知配置属性
3. **YAML_SHOULD_ESCAPE**: YAML map key特殊字符未转义

## 🎯 目标与验收标准

### 目标

彻底消除配置键不一致问题，使"配置文件—依赖版本—代码绑定"三者一致。

### 验收标准

- ✅ 全仓 `microservices/**/src/main/resources/**/*.{yml,yaml,properties}` 不再包含已弃用Prometheus导出键
- ✅ 所有Micrometer map key（含`.`等特殊字符）统一使用 `"[key.with.dots]"` 写法
- ✅ 自定义前缀（如 `access.*`、`device.*`、`ioedream.*`）在对应服务/公共模块中均存在可生成元数据的 `@ConfigurationProperties`
- ✅ CI/Pre-commit强门禁：出现弃用键/未知键/未转义键直接失败

## 🔧 修复策略

### 1. Spring Boot/Actuator键迁移（版本不匹配）

**统一规则**：Prometheus导出一律使用：

```yaml
management:
  prometheus:
    metrics:
      export:
        enabled: true
        step: 30s
```

**禁止使用**：

```yaml
# ❌ 已弃用
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

**涉及文件**：

- `microservices/common-config/nacos/common-monitoring.yaml` ✅ 已修复
- `microservices/common-config/prometheus-application.yml` ✅ 已修复
- `microservices/common-config/application-performance-optimized.yml` ✅ 已修复
- `microservices/common-config/resilience4j-application.yml` ✅ 已修复
- `microservices/common-config/rabbitmq-application.yml` ✅ 已修复
- `microservices/common-config/redis-application.yml` ✅ 已修复
- `microservices/common-config/rocketmq-application.yml` ✅ 已修复
- `microservices/config-templates/application-monitoring-template.yml` ✅ 已修复
- `deployment/kubernetes/configmap.yaml` ✅ 已修复
- `nacos-config/exception-metrics.yml` ✅ 已修复

### 2. YAML map key特殊字符未转义（语法规范）

**统一规则**：凡出现在map下的key含 `.`、`:`、`/` 等特殊字符，统一写成：

```yaml
management:
  metrics:
    distribution:
      percentiles-histogram:
        "[http.server.requests]": true
        "[spring.data.redis]": true
      percentiles:
        "[http.server.requests]": 0.5,0.9,0.95,0.99
        "[spring.data.redis]": 0.5,0.9,0.95,0.99
      sla:
        "[http.server.requests]": 100ms,200ms,500ms,1s,2s,5s
```

**禁止使用**：

```yaml
# ❌ 未转义
management:
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true  # 错误！
```

### 3. 自定义配置unknown（绑定/元数据缺失）

**统一规则**：

- 每个自定义前缀必须有 `@ConfigurationProperties(prefix=...)` 类
- 通过Maven `spring-boot-configuration-processor` 生成 `spring-configuration-metadata.json`
- 绑定字段名与YAML键严格一致（kebab-case ↔ camelCase）

**示例**：

```java
@ConfigurationProperties(prefix = "access.verification")
@Validated
public class AccessVerificationProperties {
    private Mode mode = new Mode();
    
    @Data
    public static class Mode {
        @NotBlank
        private String defaultMode;  // 对应 access.verification.mode.default-mode
        
        private Boolean backendEnabled;
        private Boolean edgeEnabled;
    }
}
```

### 4. Tracing配置一致性

**统一方向**：Spring Boot 3.x使用Micrometer Tracing

**正确配置**：

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

**禁止使用**：

```yaml
# ❌ Spring Cloud Sleuth（已废弃）
spring:
  sleuth:
    enabled: true
    zipkin:
      base-url: http://localhost:9411
```

## 🚨 强门禁落地

### A. 配置一致性检查脚本

**脚本位置**：`scripts/check-spring-config-keys.ps1`

**功能**：

1. 检查已弃用的Prometheus导出键
2. 检查YAML map key特殊字符未转义
3. 检查自定义配置前缀元数据

**使用方法**：

```powershell
# 本地检查
.\scripts\check-spring-config-keys.ps1

# CI模式（失败时退出码非0）
.\scripts\check-spring-config-keys.ps1 -CI

# 指定输出目录
.\scripts\check-spring-config-keys.ps1 -OutputDir reports
```

### B. CI/CD集成

**GitHub Actions**：`.github/workflows/code-quality.yml`

已添加配置检查步骤：

```yaml
- name: Run Spring Config Keys Check
  shell: pwsh
  continue-on-error: false
  run: |
    .\scripts\check-spring-config-keys.ps1 -CI -OutputDir reports
  id: config_check
```

### C. Git Pre-commit钩子

**脚本位置**：`scripts/git-hooks/pre-commit-check.ps1`

已添加配置检查：

- 检查已弃用的Prometheus导出键
- 检查YAML map key特殊字符未转义

**安装方法**：

```bash
# 复制到.git/hooks/
cp scripts/git-hooks/pre-commit-check.ps1 .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

## 📝 实施顺序

### 阶段1：公共配置/模板修复 ✅

- [x] 修复 `common-config/` 目录下所有配置文件
- [x] 修复 `config-templates/` 目录下所有模板文件
- [x] 修复 `nacos-config/` 目录下配置文件
- [x] 修复 `deployment/kubernetes/configmap.yaml`

### 阶段2：各微服务资源文件修复 ⏳

- [ ] 扫描所有微服务 `src/main/resources/` 目录
- [ ] 修复已弃用键
- [ ] 修复未转义键
- [ ] 验证绑定一致性

### 阶段3：补齐自定义配置元数据 ⏳

- [ ] 为所有自定义前缀创建 `@ConfigurationProperties` 类
- [ ] 确保构建能生成 `spring-configuration-metadata.json`
- [ ] 验证元数据完整性

### 阶段4：强门禁启用 ✅

- [x] CI检查已启用
- [x] Pre-commit检查已添加
- [ ] 团队培训完成

## 🔍 检查清单

### 配置文件检查

- [ ] 无 `management.metrics.export.prometheus.*` 键
- [ ] 所有 `distribution.percentiles-histogram` 下的key已转义
- [ ] 所有 `distribution.percentiles` 下的key已转义
- [ ] 所有 `distribution.sla` 下的key已转义
- [ ] Tracing配置使用 `management.tracing.*` 和 `management.zipkin.tracing.*`
- [ ] 无 `spring.sleuth.*` 配置

### 元数据检查

- [ ] 所有自定义前缀都有对应的 `@ConfigurationProperties` 类
- [ ] 构建后生成 `spring-configuration-metadata.json`
- [ ] 元数据文件包含所有自定义配置键

### 代码绑定检查

- [ ] 字段名与YAML键名一致（kebab-case ↔ camelCase）
- [ ] 使用 `@Validated` 和约束注解
- [ ] 必要时设置 `ignoreUnknownFields=false`

## 📚 相关文档

- [Spring Boot配置元数据](https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html)
- [Micrometer配置](https://micrometer.io/docs/registry/prometheus)
- [Spring Boot 3.x迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)

## 🐛 常见问题

### Q1: 为什么需要转义map key？

**A**: YAML中，包含特殊字符（如`.`）的key会被解析为嵌套对象。使用 `"[key.with.dots]"` 可以确保key被正确解析为字符串。

### Q2: 如何验证配置是否正确？

**A**: 运行检查脚本：

```powershell
.\scripts\check-spring-config-keys.ps1
```

### Q3: 自定义配置如何生成元数据？

**A**:

1. 创建 `@ConfigurationProperties` 类
2. 添加 `spring-boot-configuration-processor` 依赖
3. 构建项目，元数据会自动生成到 `target/classes/META-INF/spring-configuration-metadata.json`

## 📞 支持

如有问题，请联系：

- 架构委员会
- 技术专家团队

---

**最后更新**: 2025-01-30  
**版本**: v1.0.0
