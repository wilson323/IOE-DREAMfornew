# 内部同步调用策略（网关 + 直连混合）

## 目标
- 在“统一治理一致性”和“性能/故障域”之间取得平衡。
- 网关回归南北向入口定位，避免变成内部同步总线。
- 对同域高频热路径，允许东西向直连，但必须补齐企业级护栏（鉴权/白名单/追踪/审计）。

## 调用分层规则
### 1) 南北向（外部/前端 → 服务）
- **必须经网关**。
- 统一鉴权、审计、限流、灰度、协议适配等边界能力集中落在网关。

### 2) 东西向低频/粗粒度跨域同步调用
- **默认经网关**（`GatewayServiceClient`）。
- 适用于管理类、编排类、非实时链路，避免形成服务间“蜘蛛网”。

### 3) 东西向高频/低延迟/强一致同域热路径
- **允许直连**（`DirectServiceClient`），但必须满足：
  - 路径在服务端 **allowlist**（白名单）内；
  - 强制 **S2S HMAC** 鉴权与重放防护；
  - 必须携带追踪头，保证链路可观测；
  - 失败必须可回退（默认回退经网关或降级缓存）。

## 直连护栏（第一阶段：HMAC）
### Header 约定
客户端（直连）必须注入：
- `X-Direct-Call: true`
- `X-Trace-Id`
- `X-Source-Service`
- `X-Timestamp`
- `X-Nonce`
- `X-Signature`

签名计算（概念）：
- `Signature = HMAC(secret, method + "\n" + path + "\n" + bodyHash + "\n" + timestamp + "\n" + nonce)`

服务端校验要求：
- 时间窗校验（默认 5 分钟）
- nonce 去重防重放
- path allowlist 校验（前缀匹配）

## 直连试点白名单（同域热路径）
以下白名单为**第一阶段试点**，仅用于同域热路径：
- `GET /api/v1/account-kind/{id}`（消费链路计价/配置热路径）
- `GET /api/v1/device/{id}`、`GET /api/v1/device/code/{code}`（设备协议缓存未命中/刷新热路径）

说明：
- 白名单默认值位于 `ioedream-common-service` 的直连鉴权配置中，可通过配置中心覆盖。
- 试点仅放开“查询类”接口，避免把跨域写操作直连化。

## 代码落点
### 客户端（调用方）
- 直连客户端：`microservices/microservices-common/src/main/java/net/lab1024/sa/common/gateway/DirectServiceClient.java`
- 调用方接入示例：
  - `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/client/AccountKindConfigClient.java`
  - `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/devicecomm/cache/ProtocolCacheServiceImpl.java`

### 服务端（被调用方）
- 直连鉴权过滤器：`microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/filter/DirectCallAuthFilter.java`
- 直连配置属性（allowlist + secret 映射）：`microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/config/DirectCallProperties.java`

## 运行配置（建议）
在配置中心/环境配置中配置：
- `ioedream.direct-call.enabled=true`
- `ioedream.direct-call.service-secrets.<sourceServiceName>=<sharedSecret>`
- `ioedream.direct-call.allowlist-paths=[...]`（覆盖默认 allowlist）

## 演进建议（第二阶段）
- 当直连范围扩展到多服务、多域，或希望统一下沉限流/熔断/鉴权数据面时，再评估：
  - mTLS
  - 服务网格（Istio/Linkerd）
- 同时要求网关彻底移除阻塞 DB 依赖，保持“入口 + 控制面”的稳定定位。

