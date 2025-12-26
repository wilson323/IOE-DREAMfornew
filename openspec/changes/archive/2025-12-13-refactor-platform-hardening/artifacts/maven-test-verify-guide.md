# Maven Test/Verify 与契约回归验证指南

## 概述

本文档定义 `refactor-platform-hardening` 提案的 Maven test/verify 执行和契约回归验证的完整指南。

## Maven 测试命令

### 1. 编译验证

**目标**：确保所有代码能正确编译

```bash
# 编译所有模块
mvn -pl microservices -am clean compile

# 编译特定模块
mvn -pl ioedream-gateway-service -am clean compile
mvn -pl ioedream-oa-service -am clean compile
mvn -pl ioedream-access-service -am clean compile
```

**预期结果**：
- ✅ 编译成功，无错误
- ✅ 无循环依赖警告
- ✅ 所有依赖正确解析

### 2. 单元测试

**目标**：运行所有单元测试，确保功能正确

```bash
# 运行所有单元测试
mvn -pl microservices -am clean test

# 运行特定模块的测试
mvn -pl ioedream-gateway-service -am clean test
mvn -pl ioedream-oa-service -am clean test

# 运行特定测试类
mvn -pl ioedream-gateway-service -am test -Dtest=GatewaySecurityIntegrationTest
mvn -pl microservices-common -am test -Dtest=TraceIdPropagationTest
```

**预期结果**：
- ✅ 所有测试通过
- ✅ 测试覆盖率 ≥ 70%
- ✅ 无测试失败或跳过

### 3. 集成测试

**目标**：运行集成测试，验证组件间的交互

```bash
# 运行所有集成测试
mvn -pl microservices -am clean verify -DskipIntegrationTests=false

# 运行特定模块的集成测试
mvn -pl ioedream-gateway-service -am verify -DskipIntegrationTests=false
```

**预期结果**：
- ✅ 集成测试通过
- ✅ 网关路由正常
- ✅ 服务间调用正常

### 4. 代码质量检查

**目标**：运行代码质量检查，确保代码符合规范

```bash
# 运行 verify 目标（包含所有检查）
mvn -pl microservices -am clean verify

# 运行特定检查
mvn -pl microservices -am checkstyle:check
mvn -pl microservices -am pmd:check
mvn -pl microservices -am spotbugs:check
```

**预期结果**：
- ✅ Checkstyle 检查通过
- ✅ PMD 检查通过
- ✅ SpotBugs 检查通过
- ✅ Jacoco 覆盖率 ≥ 70%

## 契约回归验证

### 1. API 契约验证

**目标**：验证 API 契约的一致性

#### 1.1 网关路由验证

```bash
# 验证网关路由配置
mvn -pl ioedream-gateway-service test -Dtest=GatewaySecurityIntegrationTest

# 验证路由前缀一致性
# 检查所有路由使用 /api/v1 前缀
grep -r "path:" microservices/ioedream-gateway-service/src/main/resources/application*.yml | grep -v "/api/v1"
```

**检查清单**：
- [ ] 所有路由使用 `/api/v1` 前缀
- [ ] 网关路由与 Controller 路径一致
- [ ] 兼容路由在 30 天内可用
- [ ] 路由优先级正确

#### 1.2 Controller 路径验证

```bash
# 扫描所有 Controller 的路由前缀
grep -r "@RequestMapping\|@GetMapping\|@PostMapping" microservices/*/src/main/java --include="*Controller.java" | grep -v "/api/v1"

# 验证 OA 服务的工作流接口
grep -r "@RequestMapping\|@PostMapping" microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/controller/
```

**检查清单**：
- [ ] 所有 Controller 使用 `/api/v1` 前缀
- [ ] OA 工作流接口路径为 `/api/v1/workflow`
- [ ] 各业务服务的路径前缀正确

#### 1.3 服务间调用验证

```bash
# 验证 GatewayServiceClient 的调用路径
grep -r "gatewayServiceClient\." microservices/*/src/main/java --include="*.java" | head -20

# 验证路径格式
grep -r '"/api/v1/' microservices/*/src/main/java --include="*.java" | grep -E "(gatewayServiceClient|directServiceClient)"
```

**检查清单**：
- [ ] 所有服务间调用使用 `/api/v1` 前缀
- [ ] 调用路径与 Controller 路径一致
- [ ] 无硬编码域名

### 2. 鉴权验证

**目标**：验证鉴权和权限检查的正确性

#### 2.1 401 未授权验证

```bash
# 运行 401 测试
mvn -pl ioedream-gateway-service test -Dtest=GatewaySecurityIntegrationTest#testMissingTokenReturns401

# 手动测试
curl -X GET http://localhost:8080/api/v1/users -v
# 预期：401 Unauthorized
```

**检查清单**：
- [ ] 缺少 Token 返回 401
- [ ] 无效 Token 返回 401
- [ ] 刷新令牌访问业务接口返回 401
- [ ] 响应包含统一的 JSON 格式

#### 2.2 403 权限不足验证

```bash
# 运行 403 测试
mvn -pl ioedream-gateway-service test -Dtest=GatewaySecurityIntegrationTest#testForbiddenByRbac

# 手动测试
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer <user-token>" -v
# 预期：403 Forbidden（如果用户缺少权限）
```

**检查清单**：
- [ ] 权限不足返回 403
- [ ] RBAC 规则正确应用
- [ ] 响应包含统一的 JSON 格式

### 3. TraceId 验证

**目标**：验证 TraceId 的跨服务透传

#### 3.1 TraceId 注入验证

```bash
# 运行 TraceId 测试
mvn -pl microservices-common test -Dtest=TraceIdPropagationTest

# 手动测试
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer <token>" \
  -H "X-Trace-Id: test-trace-123" -v
# 预期：响应头包含 X-Trace-Id: test-trace-123
```

**检查清单**：
- [ ] 请求包含 X-Trace-Id 时，响应头返回相同的 traceId
- [ ] 请求不包含 X-Trace-Id 时，自动生成 traceId
- [ ] 日志中包含 traceId（MDC）

#### 3.2 TraceId 透传验证

```bash
# 检查服务间调用的 TraceId 透传
grep -r "X-Trace-Id\|traceId" microservices/microservices-common/src/main/java/net/lab1024/sa/common/gateway/

# 验证 GatewayServiceClient 从 MDC 读取 traceId
grep -r "MDC.get" microservices/microservices-common/src/main/java/net/lab1024/sa/common/gateway/
```

**检查清单**：
- [ ] GatewayServiceClient 从 MDC 读取 traceId
- [ ] DirectServiceClient 从 MDC 读取 traceId
- [ ] 服务间调用时 traceId 正确透传
- [ ] 完整链路中 traceId 一致

### 4. 白名单验证

**目标**：验证白名单配置的正确性

#### 4.1 白名单路径验证

```bash
# 扫描网关白名单配置
grep -r "whitelist\|permitAll" microservices/ioedream-gateway-service/src/main/resources/

# 验证白名单路径与实际路由一致
grep -r "@RequestMapping\|@GetMapping\|@PostMapping" microservices/*/src/main/java --include="*Controller.java" | grep -E "(login|register|public)"
```

**检查清单**：
- [ ] 登录接口在白名单中
- [ ] 注册接口在白名单中
- [ ] 文档接口在白名单中
- [ ] 白名单路径与实际路由一致

#### 4.2 白名单一致性验证

```bash
# 检查各服务中的重复白名单配置
grep -r "permitAll\|anonymous" microservices/*/src/main/java --include="*.java" | grep -v "gateway"

# 验证白名单已从各服务中删除
grep -r "whitelist" microservices/ioedream-access-service/src/
grep -r "whitelist" microservices/ioedream-attendance-service/src/
```

**检查清单**：
- [ ] 网关是白名单的唯一真源
- [ ] 各服务中的重复白名单已删除
- [ ] 白名单清单与实际路由一致

## 完整验证流程

### 第 1 步：编译验证

```bash
mvn -pl microservices -am clean compile
```

**预期**：编译成功，无错误

### 第 2 步：单元测试

```bash
mvn -pl microservices -am clean test
```

**预期**：所有测试通过，覆盖率 ≥ 70%

### 第 3 步：集成测试

```bash
mvn -pl microservices -am clean verify -DskipIntegrationTests=false
```

**预期**：集成测试通过

### 第 4 步：契约回归验证

```bash
# 4.1 API 契约验证
mvn -pl ioedream-gateway-service test -Dtest=GatewaySecurityIntegrationTest

# 4.2 鉴权验证
curl -X GET http://localhost:8080/api/v1/users -v

# 4.3 TraceId 验证
mvn -pl microservices-common test -Dtest=TraceIdPropagationTest

# 4.4 白名单验证
curl -X POST http://localhost:8080/api/v1/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin"}'
```

**预期**：所有验证通过

## 故障排查

### 编译失败

**问题**：`mvn clean compile` 失败

**排查步骤**：
1. 检查 Java 版本：`java -version`（应为 Java 17+）
2. 检查 Maven 版本：`mvn -version`（应为 3.8.1+）
3. 清理本地仓库：`mvn clean -DskipTests`
4. 检查依赖：`mvn dependency:tree`

### 测试失败

**问题**：单元测试或集成测试失败

**排查步骤**：
1. 查看测试日志：`mvn test -X`
2. 运行特定测试：`mvn test -Dtest=TestClassName`
3. 检查测试依赖：`mvn dependency:tree -Dscope=test`
4. 检查数据库连接（集成测试）

### 路由冲突

**问题**：网关路由冲突或 404

**排查步骤**：
1. 检查网关路由配置：`grep -r "path:" application*.yml`
2. 检查 Controller 路径：`grep -r "@RequestMapping" *Controller.java`
3. 验证路由优先级：查看网关配置中的路由顺序
4. 检查兼容路由：确认旧前缀的重写规则

## 验证清单

### 编译验证
- [ ] `mvn -pl microservices -am clean compile` 通过
- [ ] 无循环依赖
- [ ] 无编译警告

### 测试验证
- [ ] `mvn -pl microservices -am clean test` 通过
- [ ] 测试覆盖率 ≥ 70%
- [ ] `mvn -pl microservices -am clean verify` 通过

### 契约验证
- [ ] API 路由前缀统一为 `/api/v1`
- [ ] 网关路由与 Controller 路径一致
- [ ] 服务间调用使用正确的路径

### 鉴权验证
- [ ] 401 测试通过
- [ ] 403 测试通过
- [ ] 白名单配置正确

### TraceId 验证
- [ ] TraceId 注入正确
- [ ] TraceId 跨服务透传
- [ ] 日志包含 traceId

## 相关文档

- [API 契约基线](../../documentation/api/API-CONTRACT-BASELINE.md)
- [API 契约回归测试清单](../../documentation/testing/API-CONTRACT-REGRESSION-TESTS.md)
- [网关安全基线](../../documentation/security/GATEWAY-SECURITY-BASELINE.md)
