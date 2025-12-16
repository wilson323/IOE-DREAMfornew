# microservices-common 拆分迁移顺序

## 概述

本文档定义 `microservices-common` 的拆分迁移顺序，确保依赖关系清晰，避免循环依赖。

## 拆分目标

将 `microservices-common` 拆分为以下模块：
- **common-core**：基础能力（异常、DTO、工具类）
- **common-auth**：认证授权（JWT、权限、RBAC）
- **common-tracing**：链路追踪（TraceId、MDC）
- **common-gateway**：网关客户端（GatewayServiceClient、DirectServiceClient）
- **common-workflow**：工作流（contract 和 Manager）
- **common-entity**：业务域对象（Entity、DAO）

## 迁移顺序

### 第 1 阶段：基础模块（第 1-2 周）

#### 1.1 创建 common-core 模块

**内容**：
- 异常类（BusinessException、SystemException、ParamException）
- DTO 基类（ResponseDTO、PageResult）
- 工具类（StringUtils、DateUtils、CollectionUtils）
- 常量定义

**依赖**：无（独立模块）

**迁移步骤**：
1. 创建 `common-core` 模块目录和 pom.xml
2. 复制基础类到 `common-core`
3. 更新其他模块的依赖指向 `common-core`
4. 删除 `microservices-common` 中的重复代码

**验证**：
- `mvn -pl common-core clean compile` 通过
- 其他模块能正确依赖 `common-core`

#### 1.2 创建 common-tracing 模块

**内容**：
- TraceIdMdcFilter
- 链路追踪工具类
- MDC 管理

**依赖**：common-core

**迁移步骤**：
1. 创建 `common-tracing` 模块
2. 复制追踪相关代码
3. 更新依赖关系

**验证**：
- `mvn -pl common-tracing clean compile` 通过
- TraceId 透传正常

### 第 2 阶段：认证授权模块（第 2-3 周）

#### 2.1 创建 common-auth 模块

**内容**：
- JWT 工具类（JwtTokenUtil）
- 权限模型（Permission、Role）
- RBAC 配置
- 认证管理器

**依赖**：common-core、common-tracing

**迁移步骤**：
1. 创建 `common-auth` 模块
2. 复制认证授权代码
3. 更新依赖关系
4. 更新 Spring Security 配置

**验证**：
- `mvn -pl common-auth clean compile` 通过
- 鉴权和权限检查正常

### 第 3 阶段：网关和工作流模块（第 3-4 周）

#### 3.1 创建 common-gateway 模块

**内容**：
- GatewayServiceClient
- DirectServiceClient
- 服务调用工具

**依赖**：common-core、common-tracing、common-auth

**迁移步骤**：
1. 创建 `common-gateway` 模块
2. 复制网关客户端代码
3. 更新依赖关系

**验证**：
- `mvn -pl common-gateway clean compile` 通过
- 服务间调用正常

#### 3.2 创建 common-workflow 模块

**内容**：
- WorkflowDefinitionConstants
- BusinessTypeEnum
- WorkflowApprovalManager

**依赖**：common-core、common-gateway

**迁移步骤**：
1. 创建 `common-workflow` 模块
2. 复制工作流 contract 和 Manager
3. 更新依赖关系

**验证**：
- `mvn -pl common-workflow clean compile` 通过
- 工作流调用正常

### 第 4 阶段：业务域模块（第 4-6 周）

#### 4.1 创建 common-entity 模块（可选）

**内容**：
- 通用 Entity 基类
- 通用 DAO 基类
- 通用查询条件

**依赖**：common-core

**迁移步骤**：
1. 创建 `common-entity` 模块（可选）
2. 复制通用基类
3. 各服务逐步迁移业务域 Entity/DAO

**验证**：
- 编译通过
- 业务功能正常

## 依赖关系图

```
common-core
    ↓
common-tracing
    ↓
common-auth
    ↓
common-gateway
    ↓
common-workflow

common-entity（独立）
```

## 迁移检查清单

### 第 1 阶段检查
- [ ] common-core 创建并编译通过
- [ ] 其他模块依赖指向 common-core
- [ ] microservices-common 中的重复代码已删除
- [ ] 测试通过

### 第 2 阶段检查
- [ ] common-tracing 创建并编译通过
- [ ] common-auth 创建并编译通过
- [ ] 鉴权和权限检查正常
- [ ] 测试通过

### 第 3 阶段检查
- [ ] common-gateway 创建并编译通过
- [ ] common-workflow 创建并编译通过
- [ ] 服务间调用正常
- [ ] 工作流调用正常
- [ ] 测试通过

### 第 4 阶段检查
- [ ] 业务域 Entity/DAO 逐步下沉到各服务
- [ ] microservices-common 仅包含纯公共能力
- [ ] 无循环依赖
- [ ] 编译和测试通过

## 风险与缓解

| 风险 | 缓解方案 |
|------|---------|
| 循环依赖 | 严格遵循依赖关系图，使用接口隔离 |
| 编译失败 | 逐步迁移，充分测试，保持兼容 |
| 性能下降 | 监控编译时间和运行时性能 |
| 版本冲突 | 统一管理版本，定期检查 |

## 验证命令

```bash
# 检查循环依赖
mvn dependency:tree -pl microservices-common

# 编译所有模块
mvn -pl microservices -am clean compile

# 运行测试
mvn -pl microservices -am test

# 检查依赖一致性
mvn -pl microservices -am dependency:check
```

## 完成标志

- ✅ 所有模块创建并编译通过
- ✅ 无循环依赖
- ✅ 所有测试通过
- ✅ microservices-common 仅包含纯公共能力
- ✅ 业务域对象已下沉到各服务
