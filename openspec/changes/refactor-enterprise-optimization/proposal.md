# Change: Enterprise-Level Optimization Refactoring

## Why

基于全局项目深度分析报告（GLOBAL_PROJECT_DEEP_ANALYSIS_2025-12-14.md），发现以下待优化事项需要系统性处理：

1. **P0级问题**：`PaymentService`过大（2756行），需拆分
2. **P0级问题**：`ApprovalServiceImpl`等文件存在11处TODO待完成
3. **P1级问题**：`video-service`和`gateway-service`缺少Manager层
4. **P1级问题**：部分API路径不统一，需统一使用`/api/v1/`前缀
5. **P2级问题**：前端28处TODO、移动端4处TODO需清理

## What Changes

### P0级优化（立即整改）

- 将`PaymentService`（2756行）拆分为：
  - `WechatPayService` - 微信支付相关
  - `AlipayPayService` - 支付宝支付相关
  - `PaymentRecordService` - 支付记录管理
  - `PaymentCallbackService` - 支付回调处理
- 完成`ApprovalServiceImpl`中的11处TODO
- 完成`ApprovalController`中的4处TODO

### P1级优化（1周内整改）

- 为`video-service`补充Manager层
- 为`gateway-service`补充Manager层（如需要）
- 统一API路径前缀为`/api/v1/`

### P2级优化（持续改进）

- 清理前端28处TODO
- 清理移动端4处TODO

## Impact

- Affected specs: consume-service, oa-service, video-service, gateway-routing
- Affected code:
  - `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/service/PaymentService.java`
  - `microservices/ioedream-oa-service/src/main/java/net/lab1024/sa/oa/workflow/service/impl/ApprovalServiceImpl.java`
  - `microservices/ioedream-video-service/`
  - `smart-admin-web-javascript/src/views/`
  - `smart-app/src/`
