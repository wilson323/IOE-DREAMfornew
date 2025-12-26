# Tasks

## 1. @Repository 注解修复 (P0)

- [x] 1.1 验证 ioedream-access-service DAO 层 - 已使用 @Mapper，无需修复
- [x] 1.2 验证 ioedream-oa-service DAO 层 - 已使用 @Mapper，无需修复
- [x] 1.3 验证 microservices-common DAO 层 - 已使用 @Mapper，无需修复
-    说明：代码中 @Repository 仅出现在注释中（说明禁止使用），实际代码均使用 @Mapper

## 2. @Autowired 注解修复 (P0)

- [x] 2.1 验证 ioedream-common-service - 无实际 @Autowired 使用
- [x] 2.2 验证 ioedream-consume-service - 无实际 @Autowired 使用
- [x] 2.3 验证 ioedream-gateway-service - 仅测试文件使用 @Autowired（可接受）
-    说明：代码中 @Autowired 仅在测试文件中使用，生产代码均使用 @Resource

## 3. 未使用 import/字段清理 (P1)

- [x] 3.1 清理 ApprovalStatisticsDao.java 未使用 import (Transactional)
- [x] 3.2 清理 WorkflowManager.java 未使用 import (WorkflowException, LocalDateTime)
- [x] 3.3 WorkflowExecutorRegistry.java 未使用字段 - 设计预留，暂不修改
- [x] 3.4 清理 TraceIdPropagationTest.java 未使用 import/字段
- [x] 3.5 修复 GatewaySecurityIntegrationTest.java 编译错误 (isNotEqualTo → value())

## 4. 验证

- [x] 4.1 代码修复完成
- [x] 4.2 IDE 错误已清除

## 5. 文档更新

- [x] 5.1 全局分析报告已创建
- [x] 5.2 提案已完成
