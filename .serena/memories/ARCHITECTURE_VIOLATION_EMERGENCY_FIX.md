# P0架构违规紧急修复记录

## 问题描述
在移除microservices-common聚合依赖后，发现SmartRequestUtil类不可用，导致编译失败。

## 影响范围
- ioedream-device-comm-service (VendorSupportController)
- ioedream-oa-service (WorkflowEngineServiceImpl)
- 其他使用SmartRequestUtil的服务

## 根本原因
1. SmartRequestUtil原本在microservices-common聚合模块中
2. 移除聚合依赖后，该类不可用
3. 该类依赖Spring Web，不适合放入common-core（最小稳定内核）

## 解决方案
将SmartRequestUtil迁移到microservices-common-gateway-client模块，因为：
1. 所有业务服务都依赖gateway-client
2. 该模块已包含Spring Web依赖
3. 符合细粒度架构原则

## 修复步骤
1. 将SmartRequestUtil复制到gateway-client模块
2. 更新import语句
3. 验证编译通过

## 状态
- 发现问题：SmartRequestUtil不可用
- 分析影响：多个服务编译失败
- 制定方案：迁移到gateway-client模块
- 执行状态：待实施