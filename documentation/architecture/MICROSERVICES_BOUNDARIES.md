# IOE-DREAM 微服务边界文档

> **版本**: v1.0.0  
> **更新日期**: 2025-12-14  
> **维护团队**: IOE-DREAM 架构委员会  
> **适用范围**: 所有微服务开发、架构决策、服务拆分

---

## 🎯 核心原则

### 7微服务架构严格限制

**强制要求**: 项目严格限制为**7个核心微服务**，禁止新增微服务。

**架构清单**:
1. `ioedream-gateway-service` (8080) - API网关
2. `ioedream-common-service` (8088) - 公共业务服务
3. `ioedream-device-comm-service` (8087) - 设备通讯服务
4. `ioedream-oa-service` (8089) - OA办公服务
5. `ioedream-access-service` (8090) - 门禁服务
6. `ioedream-attendance-service` (8091) - 考勤服务
7. `ioedream-video-service` (8092) - 视频服务
8. `ioedream-consume-service` (8094) - 消费服务
9. `ioedream-visitor-service` (8095) - 访客服务

**注意**: 实际为9个微服务（包含gateway），但业务服务为7个。

---

## 📋 各微服务职责边界

### 1. ioedream-gateway-service (8080)

**核心职责**:
- API网关统一入口
- 路由转发和负载均衡
- 限流熔断
- 统一认证授权
- 请求日志记录

**禁止职责**:
- ❌ 不处理业务逻辑
- ❌ 不直接访问数据库
- ❌ 不存储业务数据

**服务边界**:
- **输入**: 外部HTTP请求
- **输出**: 转发到后端微服务
- **调用**: 不调用其他服务（仅转发）

---

### 2. ioedream-common-service (8088)

**核心职责**:
- 用户认证与授权
- 组织架构管理（部门、区域、员工）
- 权限管理（RBAC）
- 字典管理
- 菜单管理
- 审计日志
- 系统配置
- 通知管理
- 任务调度
- 监控告警
- 文件管理
- 工作流管理

**禁止职责**:
- ❌ 不处理业务特定逻辑（门禁、考勤、消费等）
- ❌ 不直接管理设备（通过设备通讯服务）

**服务边界**:
- **输入**: 通过GatewayServiceClient调用
- **输出**: 提供公共业务API
- **调用**: 不调用其他业务服务（仅提供公共服务）

---

### 3. ioedream-device-comm-service (8087)

**核心职责**:
- 设备协议适配（TCP/UDP/HTTP）
- 设备连接管理
- 设备数据采集
- 设备指令下发
- 生物识别数据管理
- 协议缓存管理

**禁止职责**:
- ❌ 不处理业务逻辑（门禁控制、考勤打卡等）
- ❌ 不直接管理设备实体（通过common-service的DeviceEntity）

**服务边界**:
- **输入**: 设备数据、业务服务指令
- **输出**: 设备状态、采集数据
- **调用**: 通过GatewayServiceClient调用业务服务

---

### 4. ioedream-oa-service (8089)

**核心职责**:
- 工作流引擎
- 审批流程管理
- 审批配置管理
- 审批实例管理
- 审批任务管理
- 审批统计

**禁止职责**:
- ❌ 不处理业务特定审批（门禁申请、消费退款等通过业务服务处理）
- ❌ 不直接访问其他服务数据库

**服务边界**:
- **输入**: 工作流定义、审批请求
- **输出**: 审批结果、工作流状态
- **调用**: 通过GatewayServiceClient调用common-service获取用户/部门信息

---

### 5. ioedream-access-service (8090)

**核心职责**:
- 门禁控制
- 通行记录管理
- 门禁权限申请
- 门禁设备管理（业务层面）
- 紧急权限管理
- 门禁统计分析

**禁止职责**:
- ❌ 不直接管理设备协议（通过device-comm-service）
- ❌ 不直接管理设备实体（通过common-service的DeviceEntity）
- ❌ 不处理考勤/消费业务

**服务边界**:
- **输入**: 门禁控制请求、权限申请
- **输出**: 通行记录、门禁状态
- **调用**: 
  - 通过GatewayServiceClient调用device-comm-service（设备指令）
  - 通过GatewayServiceClient调用common-service（用户/区域信息）

---

### 6. ioedream-attendance-service (8091)

**核心职责**:
- 考勤打卡
- 排班管理
- 考勤记录管理
- 考勤统计
- 请假管理
- 加班管理
- 补卡管理
- 出差管理

**禁止职责**:
- ❌ 不处理门禁/消费业务
- ❌ 不直接管理设备（通过device-comm-service）

**服务边界**:
- **输入**: 打卡请求、排班配置
- **输出**: 考勤记录、考勤统计
- **调用**: 
  - 通过GatewayServiceClient调用device-comm-service（设备数据）
  - 通过GatewayServiceClient调用common-service（用户/部门信息）

---

### 7. ioedream-video-service (8092)

**核心职责**:
- 视频监控
- 录像回放
- 视频设备管理
- 视频流管理
- AI视频分析（人脸识别、行为分析）

**禁止职责**:
- ❌ 不处理门禁/考勤业务
- ❌ 不直接管理设备协议（通过device-comm-service）

**服务边界**:
- **输入**: 视频查询请求、设备控制指令
- **输出**: 视频流、录像文件、分析结果
- **调用**: 
  - 通过GatewayServiceClient调用device-comm-service（设备控制）
  - 通过GatewayServiceClient调用common-service（用户/区域信息）

---

### 8. ioedream-consume-service (8094)

**核心职责**:
- 消费管理
- 账户管理
- 支付管理
- 退款管理
- 消费记录
- 消费统计
- 订餐管理
- 对账管理

**禁止职责**:
- ❌ 不处理门禁/考勤业务
- ❌ 不直接管理设备（通过device-comm-service）

**服务边界**:
- **输入**: 消费请求、支付请求
- **输出**: 消费记录、账户余额、支付结果
- **调用**: 
  - 通过GatewayServiceClient调用device-comm-service（设备数据）
  - 通过GatewayServiceClient调用common-service（用户/区域信息）

---

### 9. ioedream-visitor-service (8095)

**核心职责**:
- 访客预约
- 访客登记
- 访客通行
- 访客记录
- 访客统计
- 访客区域管理

**禁止职责**:
- ❌ 不处理门禁控制（通过access-service）
- ❌ 不直接管理设备（通过device-comm-service）

**服务边界**:
- **输入**: 访客预约、登记请求
- **输出**: 访客记录、通行权限
- **调用**: 
  - 通过GatewayServiceClient调用access-service（门禁权限）
  - 通过GatewayServiceClient调用device-comm-service（设备数据）
  - 通过GatewayServiceClient调用common-service（用户/区域信息）

---

## 🚫 服务间调用规范

### 统一调用模式

**强制要求**: 所有服务间调用必须通过`GatewayServiceClient`，禁止使用Feign直连。

**调用模式**:
```java
@Resource
private GatewayServiceClient gatewayServiceClient;

// ✅ 正确：通过网关调用
public UserVO getUser(Long userId) {
    return gatewayServiceClient.callCommonService(
        "/api/v1/users/" + userId,
        HttpMethod.GET,
        null,
        UserVO.class
    );
}
```

**禁止模式**:
```java
// ❌ 错误：Feign直连
@FeignClient(name = "ioedream-common-service")
public interface UserServiceClient { }
```

---

## 📊 服务依赖关系图

```
ioedream-gateway-service (8080)
    ↓ (路由转发)
    ├── ioedream-common-service (8088) [公共服务，被其他服务调用]
    ├── ioedream-device-comm-service (8087) [设备服务，被业务服务调用]
    ├── ioedream-oa-service (8089) [OA服务，独立]
    ├── ioedream-access-service (8090) → device-comm + common
    ├── ioedream-attendance-service (8091) → device-comm + common
    ├── ioedream-video-service (8092) → device-comm + common
    ├── ioedream-consume-service (8094) → device-comm + common
    └── ioedream-visitor-service (8095) → access + device-comm + common
```

---

## ✅ 边界检查清单

### 新增功能前检查

- [ ] 功能是否属于现有服务职责范围？
- [ ] 是否需要创建新服务？（禁止，必须整合到现有服务）
- [ ] 服务间调用是否通过GatewayServiceClient？
- [ ] 是否直接访问其他服务数据库？（禁止）

### 服务拆分检查

- [ ] 是否违反7微服务架构限制？
- [ ] 新服务是否在架构清单中？
- [ ] 是否已通过架构委员会审批？

---

## 📚 相关文档

- [CLAUDE.md - 全局架构规范](../../CLAUDE.md)
- [全局一致性优化路线图](../../.trae/plans/global-consistency-optimization-roadmap.md)
- [微服务统一规范](../microservices/UNIFIED_MICROSERVICES_STANDARDS.md)

---

**最后更新**: 2025-12-14  
**维护团队**: IOE-DREAM 架构委员会
