# 需求共识文档 - 门禁模块完善

## 明确的需求描述

完善门禁模块的5个待实现功能：
1. **互锁验证逻辑完善** - 从框架到完整实现
2. **多人验证逻辑完善** - 从框架到完整实现
3. **时间段解析完善** - JSON解析，多时间段支持
4. **设备-区域关联查询完善** - 通过GatewayServiceClient实现
5. **黑名单验证逻辑完善** - 用户黑名单查询

## 技术实现方案

### 1. 互锁验证逻辑完善

**实现方案**：
- 从`AreaAccessExtEntity.extConfig` JSON中解析互锁组配置
- 使用`t_access_interlock_record`表记录锁定状态
- 实现锁定/解锁机制，超时自动解锁
- 互锁组内设备互斥，一个设备锁定后，其他设备不能开门

**技术约束**：
- 使用Jackson解析JSON配置
- 使用Redis缓存锁定状态（可选，提升性能）
- 超时时间从配置读取，默认60秒

### 2. 多人验证逻辑完善

**实现方案**：
- 使用`t_access_multi_person_record`表管理验证会话
- 会话ID使用UUID生成
- 实现会话创建、用户加入、状态检查、超时清理
- 支持配置所需人数（从extConfig读取）

**技术约束**：
- 会话超时时间从配置读取，默认120秒
- 使用定时任务清理过期会话
- 支持并发访问（使用数据库锁或Redis分布式锁）

### 3. 时间段解析完善

**实现方案**：
- 解析`UserAreaPermissionEntity.accessTimes` JSON字段
- 支持多时间段配置
- 检查当前时间是否在允许的时间段内
- 支持工作日判断（days数组）

**技术约束**：
- JSON格式：`[{"startTime": "08:00", "endTime": "18:00", "days": [1,2,3,4,5]}]`
- 使用Jackson解析JSON
- 时间比较使用LocalTime

### 4. 设备-区域关联查询完善

**实现方案**：
- 通过GatewayServiceClient调用common-service查询设备信息
- 支持通过设备序列号（serialNumber）查询
- 支持通过设备ID查询
- DeviceEntity包含areaId字段，可直接获取区域ID

**技术约束**：
- 接口路径：`/api/v1/device/{deviceId}` 或 `/api/v1/device/serial/{serialNumber}`
- 使用GatewayServiceClient.callCommonService方法
- 添加异常处理和降级策略

### 5. 黑名单验证逻辑完善

**实现方案**：
- 通过GatewayServiceClient调用common-service查询用户信息
- 检查用户状态（UserEntity.status字段）
- 如果用户状态为禁用（status=0），视为黑名单
- 可选：查询用户扩展属性中的黑名单标记

**技术约束**：
- 接口路径：`/api/v1/user/{userId}`
- 使用GatewayServiceClient.callCommonService方法
- 添加缓存优化（可选）

## 技术约束和集成方案

### 技术栈约束

- **Java版本**：Java 17
- **框架版本**：Spring Boot 3.5.8, Spring Cloud 2025.0.0
- **数据库**：MySQL 8.0.35 + MyBatis-Plus 3.5.15
- **JSON解析**：Jackson（Spring Boot默认）
- **UUID生成**：java.util.UUID

### 架构约束

- **四层架构**：严格遵循Controller → Service → Manager → DAO
- **依赖注入**：统一使用`@Resource`
- **DAO命名**：统一使用`Dao`后缀和`@Mapper`注解
- **Manager类**：纯Java类，构造函数注入，在配置类中注册为Bean

### 集成方案

- **服务间调用**：通过GatewayServiceClient调用common-service
- **异常处理**：统一异常处理，降级策略
- **日志记录**：使用Slf4j，统一日志格式
- **配置管理**：使用application.yml配置

## 任务边界限制

### 包含范围

- ✅ 互锁验证完整逻辑实现
- ✅ 多人验证完整逻辑实现
- ✅ 时间段JSON解析实现
- ✅ 设备-区域关联查询实现
- ✅ 用户黑名单查询实现
- ✅ 单元测试编写
- ✅ 代码规范检查

### 不包含范围

- ❌ 混合验证模式（hybrid）实现（后续阶段）
- ❌ 离线验证完整实现（后续阶段）
- ❌ 集成测试完善（后续阶段）
- ❌ 性能优化（后续阶段）
- ❌ 监控告警体系（后续阶段）

## 验收标准

### 功能验收

- [ ] 互锁验证：互锁组配置正确解析，锁定/解锁机制正常工作，超时处理正常
- [ ] 多人验证：会话管理正常，人数统计准确，超时清理正常，并发访问正常
- [ ] 时间段验证：JSON解析正确，多时间段支持，工作日判断正确，时间比较准确
- [ ] 设备-区域关联：通过GatewayServiceClient正确查询设备信息，异常处理完善
- [ ] 黑名单验证：通过GatewayServiceClient正确查询用户信息，状态判断正确

### 质量验收

- [ ] 单元测试覆盖率 > 85%
- [ ] 性能基准：验证响应时间 < 500ms
- [ ] 安全扫描无高危漏洞
- [ ] 代码规范100%遵循
- [ ] 无功能重复和代码冗余

### 架构验收

- [ ] 四层架构严格遵循
- [ ] 模块化组件化设计合理
- [ ] 高复用性实现
- [ ] 扩展性良好
- [ ] 可维护性强

## 确认所有不确定性已解决

✅ **互锁验证配置存储** - 已确认存储在extConfig JSON中  
✅ **多人验证会话管理** - 已确认使用UUID和配置超时时间  
✅ **时间段JSON格式** - 已确认标准格式  
✅ **设备-区域关联查询** - 已确认接口路径和实现方式  
✅ **用户黑名单查询** - 已确认通过用户状态判断  

**所有关键决策点已明确，可以开始实施。**
