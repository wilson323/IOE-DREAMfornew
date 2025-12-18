# 需求对齐文档 - 门禁模块完善

## 原始需求

执行门禁模块企业级完整实现计划，完善待实现功能：
1. 互锁验证逻辑完善
2. 多人验证逻辑完善
3. 时间段解析完善
4. 设备-区域关联查询完善
5. 黑名单验证逻辑完善

## 项目上下文

### 技术栈

- 编程语言：Java 17
- 框架版本：Spring Boot 3.5.8, Spring Cloud 2025.0.0
- 数据库：MySQL 8.0.35 + MyBatis-Plus 3.5.15
- 部署环境：Windows

### 现有架构理解

- 架构模式：微服务架构，四层架构（Controller → Service → Manager → DAO）
- 核心模块：
  - `ioedream-access-service` - 门禁服务（8090端口）
  - `microservices-common-business` - 公共业务模块
- 集成点：
  - GatewayServiceClient - 服务间调用
  - Nacos - 服务注册发现

## 需求理解

### 功能边界

**包含功能：**

- [x] 互锁验证逻辑完善（从框架到完整实现）
- [x] 多人验证逻辑完善（从框架到完整实现）
- [x] 时间段解析完善（JSON解析，多时间段支持）
- [x] 设备-区域关联查询完善（通过GatewayServiceClient）
- [x] 黑名单验证逻辑完善（用户黑名单查询）

**明确不包含（Out of Scope）：**

- [ ] 混合验证模式（hybrid）实现（后续阶段）
- [ ] 离线验证完整实现（后续阶段）
- [ ] 集成测试完善（后续阶段）

## 疑问澄清

### P0级问题（已澄清）

1. **互锁验证配置存储位置** ✅
   - 背景：互锁组配置需要存储，是存储在AreaAccessExtEntity.extConfig JSON中，还是单独的表？
   - 影响：影响数据模型设计和查询性能
   - **决策方案**：存储在extConfig JSON中，便于灵活配置
   - **格式**：`{"interlockGroups": [{"groupId": 1, "deviceIds": [1001, 1002]}]}`

2. **多人验证会话管理** ✅
   - 背景：多人验证需要会话管理，会话ID如何生成？超时时间如何配置？
   - 影响：影响多人验证的可靠性和性能
   - **决策方案**：使用UUID生成会话ID，超时时间从配置读取，默认120秒
   - **实现**：使用`UUID.randomUUID().toString()`生成会话ID

3. **时间段JSON格式** ✅
   - 背景：accessTimes字段的JSON格式需要明确
   - 影响：影响时间段解析的正确性
   - **决策方案**：使用标准格式 `[{"startTime": "08:00", "endTime": "18:00", "days": [1,2,3,4,5]}]`
   - **说明**：days数组表示工作日（1=周一，7=周日）

4. **设备-区域关联查询** ✅
   - 背景：需要通过GatewayServiceClient调用common-service，具体接口路径是什么？
   - 影响：影响设备-区域关联查询的实现
   - **决策方案**：
     - 通过序列号查询：`/api/v1/device/serial/{serialNumber}`
     - 通过设备ID查询：`/api/v1/device/{deviceId}`
     - DeviceEntity包含areaId字段，可直接获取区域ID

5. **用户黑名单查询** ✅
   - 背景：用户黑名单查询接口需要明确
   - 影响：影响黑名单验证的实现
   - **决策方案**：
     - 优先查询用户状态（UserEntity.status字段）
     - 如果用户状态为禁用（status=0），视为黑名单
     - 可通过GatewayServiceClient调用`/api/v1/user/{userId}`查询用户信息

## 验收标准

### 功能验收

- [ ] 互锁验证：互锁组配置正确解析，锁定/解锁机制正常工作
- [ ] 多人验证：会话管理正常，人数统计准确，超时清理正常
- [ ] 时间段验证：JSON解析正确，多时间段支持，工作日判断正确
- [ ] 设备-区域关联：通过GatewayServiceClient正确查询设备信息
- [ ] 黑名单验证：通过GatewayServiceClient正确查询用户黑名单

### 质量验收

- [ ] 单元测试覆盖率 > 85%
- [ ] 性能基准：验证响应时间 < 500ms
- [ ] 安全扫描无高危漏洞
- [ ] 代码规范100%遵循
