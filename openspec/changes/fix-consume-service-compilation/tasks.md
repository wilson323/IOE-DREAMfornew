# Tasks: 修复 Consume Service 编译错误并对齐领域模型

## 1. 编译错误基线
- [ ] 1.1 汇总当前 consume-service 编译错误清单（按 Entity/Service/Controller/Test 分类）
- [ ] 1.2 建立最小可验证路径：仅 `ioedream-consume-service` 可编译

## 2. Entity 对齐（P0）
- [ ] 2.1 补齐 `ConsumeRecordEntity`：修复 recordId/paymentStatus/paymentMethod 等引用不一致
- [ ] 2.2 补齐 `PaymentRecordEntity`：修复 paymentFee/settlementAmount 等引用不一致
- [ ] 2.3 补齐 `PaymentRefundRecordEntity`：修复 refundTransactionNo/applyTime/refundType 等引用不一致
- [ ] 2.4 补齐 `QrCodeEntity`：补齐业务/安全/关联/状态/扩展/使用记录字段
- [ ] 2.5 补齐 `ConsumeProductEntity` 等其它缺失字段（以编译错误驱动）

## 3. DAO/Service 修复（P0）
- [ ] 3.1 补齐关键 DAO 缺失方法（如 `AccountDao.deductBalance`）
- [ ] 3.2 修复 `PaymentRecordServiceImpl` 类型不一致导致的编译错误
- [ ] 3.3 修复 `OnlineConsumeFlow` 类型不一致与字段调用错误

## 4. 消费流程业务逻辑补齐（P1）
- [ ] 4.1 实现 `AbstractConsumeFlowTemplate` 设备验证逻辑
- [ ] 4.2 实现 `AbstractConsumeFlowTemplate` 保存逻辑
- [ ] 4.3 实现 `OnlineConsumeFlow` 通知/消息推送的最小必需逻辑（WebSocket/RabbitMQ 适配）

## 5. Controller 修复（P1）
- [ ] 5.1 修复 `MobileConsumeController` 三元表达式/类型不一致
- [ ] 5.2 修复其它 Controller 编译错误（以错误清单驱动）

## 6. 测试修复（P2）
- [ ] 6.1 修复 consume 相关单测导入与包路径
- [ ] 6.2 对“压测/过期集成测试”采用 `@Disabled` 占位策略，避免编译失败

## 7. 验证
- [ ] 7.1 `mvn -f microservices/pom.xml -pl ioedream-consume-service -am test` 通过
- [ ] 7.2 回归扫描：确保无新增循环依赖与公共模块边界泄漏
