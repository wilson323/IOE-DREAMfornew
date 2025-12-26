# Proposal: 提升单元测试覆盖率

## 变更ID

`add-unit-test-coverage`

## 优先级

**P3** - 低优先级

## 背景

当前项目单元测试覆盖率不足，需要系统性地提升测试覆盖率，确保代码质量和系统稳定性。

## 变更原因

1. 当前测试覆盖率低于行业标准
2. 缺少关键业务逻辑的测试用例
3. 需要建立持续集成测试机制
4. 提高代码重构的信心

## 变更内容

### 目标

- 核心业务逻辑测试覆盖率 > 80%
- Service层测试覆盖率 > 85%
- Manager层测试覆盖率 > 90%

### 测试范围

1. **consume-service**
   - PaymentService测试
   - ConsumeTransactionManager测试
   - AccountService测试

2. **access-service**
   - AccessEventService测试
   - AdvancedAccessControlService测试

3. **visitor-service**
   - VisitorAppointmentService测试
   - VisitorStatisticsService测试

4. **attendance-service**
   - AttendanceRecordService测试
   - AttendanceScheduleService测试

5. **device-comm-service**
   - ProtocolHandler测试
   - 协议解析测试

### 涉及服务

- 所有微服务

## 影响范围

- 测试代码
- CI/CD流程

## 验收标准

- [ ] 核心业务逻辑覆盖率>80%
- [ ] Service层覆盖率>85%
- [ ] Manager层覆盖率>90%
- [ ] CI/CD集成测试通过
- [ ] 测试报告生成
