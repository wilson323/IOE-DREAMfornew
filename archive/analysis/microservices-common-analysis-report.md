# microservices-common 模块分析报告

生成时间: 2025-12-25
分析范围: microservices-common/src/main/java

## 📊 文件统计

- **总文件数**: 25个Java文件
- **配置类**: 4个 (16%)
- **边缘计算**: 6个 (24%)
- **OpenAPI**: 13个 (52%)
- **工厂类**: 1个 (4%)
- **其他**: 1个 (4%)

## 📁 详细分类

### ✅ 保留（配置类 - 4个文件）

这些是纯配置类，符合microservices-common的定位：

1. `config/CommonComponentsConfiguration.java` - 通用组件配置
2. `config/JacksonConfiguration.java` - JSON序列化配置
3. `config/OpenApiConfiguration.java` - OpenAPI文档配置
4. `config/properties/IoeDreamGatewayProperties.java` - 网关属性配置

### 🔄 需要迁移（业务相关 - 20个文件）

#### 1. OpenAPI模块（13个文件）- 建议迁移到 microservices-common-gateway-client

**Domain Request (6个)**:
- ChangePasswordRequest.java
- LoginRequest.java
- RefreshTokenRequest.java
- UpdateUserProfileRequest.java
- UserExtendedInfoRequest.java
- UserQueryRequest.java

**Domain Response (6个)**:
- LoginResponse.java
- RefreshTokenResponse.java
- TokenValidationResponse.java
- UserInfoResponse.java
- UserPermissionResponse.java
- UserProfileResponse.java

**Service (1个)**:
- UserOpenApiService.java

**Manager (1个)**:
- SecurityManager.java

#### 2. 边缘计算模块（6个文件）- 建议保留或创建独立模块

**Model (5个)**:
- EdgeConfig.java
- EdgeDevice.java
- InferenceRequest.java
- InferenceResult.java
- ModelInfo.java

**Form (1个)**:
- EdgeDeviceRegisterForm.java

#### 3. 工厂类（1个文件）- 建议保留

- StrategyMarker.java - 策略模式标记接口

## 📋 依赖分析

### 被依赖情况

需要检查以下模块是否依赖microservices-common中的业务类：

- ioedream-common-service
- ioedream-access-service
- ioedream-attendance-service
- ioedream-consume-service
- ioedream-device-comm-service
- ioedream-video-service
- ioedream-visitor-service

## 🎯 清理建议

### 优先级P0（立即执行）

1. **保留配置类**: config/* 和 factory/*
2. **迁移OpenAPI**: 移动到 microservices-common-gateway-client
3. **评估edge模块**: 确定是否保留或创建新模块

### 优先级P1（2-4周内）

4. **更新所有导入路径**: 确保其他服务使用正确的导入
5. **验证编译**: 确保迁移后所有服务编译通过
6. **更新文档**: 同步CLAUDE.md架构描述

## ⚠️ 风险评估

- **低风险**: 配置类迁移
- **中风险**: OpenAPI模块迁移（需要检查依赖）
- **高风险**: 边缘计算模块（需要业务评估）

## 📝 下一步行动

1. 检查所有服务对microservices-common的依赖
2. 确定OpenAPI模块的迁移方案
3. 执行迁移并验证
4. 建立持续监控机制

