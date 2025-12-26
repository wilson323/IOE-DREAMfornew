# Change: 补齐移动端消费控制器与测试重构

## Why
当前消费服务缺失可用的移动端 Controller 实现，相关测试长期禁用，导致接口契约不可验证且存在回归风险。

## What Changes
- 新增/补齐 `ConsumeMobileController` 接口实现，遵循 `documentation` 中移动端消费接口约定
- 将现有 Controller 测试迁移至 `@WebMvcTest` + `@MockBean`，移除禁用标记并重写用例
- 统一测试与真实 Controller 绑定，避免已删除 Controller 的无效测试

## Impact
- Affected specs: consume-mobile
- Affected code: ioedream-consume-service Controller + 测试；ioedream-device-comm-service 测试
