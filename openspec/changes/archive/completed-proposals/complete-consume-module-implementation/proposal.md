# Change: 完善消费模块核心功能实现

## Why
当前消费模块存在严重的功能缺失和架构问题，导致无法正常支撑企业级消费场景。根据消费模块实施指南分析，存在P0级阻塞性问题4个、P1级架构问题1个，亟需系统性完善以实现7步标准消费流程和6种消费模式策略。

## What Changes
- **消费模式策略架构重构**：实现6种核心消费模式（定值消费、免费金额、计量计费、商品消费、订餐消费、智能消费）的策略模式架构
- **区域管理核心字段支持**：添加manage_mode、area_sub_type、fixed_value_config、meal_categories、inventory_flag等5个核心字段
- **7步标准消费流程实现**：身份识别→权限验证→场景识别→金额计算→余额扣除→记录交易→打印小票的完整流程
- **定值金额计算逻辑**：实现账户类别→区域→系统默认的三级优先级计算逻辑
- **企业级高可用设计**：SAGA分布式事务、降级熔断、重试机制、监控告警
- **设备识别验证流程**：设备端识别+服务端验证的企业级高可用消费验证流程
- **微服务间调用规范**：通过GatewayServiceClient统一调用，确保全局一致性

## Impact
- **Affected specs**: consume-module（新建消费模块规格）
- **Affected code**:
  - `ioedream-consume-service` 微服务全量代码
  - `ConsumptionModeEngineManager` 架构重构
  - `ConsumeController`、`ConsumeService`、`ConsumeManager` 等核心类
  - 新增策略模式实现类（6个消费策略）
  - 新增设备消费验证流程
- **BREAKING CHANGES**:
  - `ConsumptionModeEngineManager` 接口变更
  - 新增必需的数据库字段
  - 新增必需的微服务间依赖

## Implementation Scope
基于《消费模块功能完善实施指南》严格实施，确保：
- 严格遵循四层架构规范（Controller → Service → Manager → Repository）
- 统一使用@Resource依赖注入
- 实现完整的前后端移动端接口
- 单元测试覆盖率≥80%
- 遵循SmartAdmin编码规范和RepoWiki合规性

## Expected Outcomes
- 实现完整的7步标准消费流程
- 支持6种消费模式的策略切换
- 提供企业级高可用性保障（99.9%可用性）
- 完成设备识别后的消费验证流程
- 建立完善的监控告警体系
- 确保微服务间调用的一致性和可靠性