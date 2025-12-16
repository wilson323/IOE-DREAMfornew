# Proposal: 补充离线消费同步逻辑

## 变更ID

`add-offline-consume-sync`

## 优先级

**P1** - 高优先级

## 背景

根据文档`11-离线消费模块重构设计.md`，系统需要支持网络中断情况下的离线消费功能，当前代码仅有基础框架。

## 变更原因

1. 网络不稳定时消费设备需要继续工作
2. 离线数据需要在网络恢复后自动同步
3. 需要处理离线期间的数据冲突
4. 保证消费数据最终一致性

## 变更内容

### 新增功能

1. **离线消费记录**
   - 本地存储消费记录
   - 离线余额扣减
   - 离线交易编号生成

2. **数据同步**
   - 网络恢复检测
   - 批量数据上传
   - 冲突检测与解决
   - 同步状态追踪

3. **异常处理**
   - 重复消费检测
   - 余额不一致处理
   - 同步失败重试

### 涉及服务

- `ioedream-consume-service`
- `ioedream-device-comm-service`

### 涉及文件

- 新增: `OfflineConsumeService.java`
- 新增: `OfflineConsumeServiceImpl.java`
- 新增: `OfflineSyncManager.java`
- 新增: `OfflineConsumeDao.java`
- 修改: `ConsumeProtocolHandler.java`

## 影响范围

- 消费设备通讯协议
- 消费记录同步机制
- 账户余额一致性

## 验收标准

- [ ] 离线消费记录正常存储
- [ ] 网络恢复后自动同步
- [ ] 冲突检测与解决正确
- [ ] 数据最终一致性保证
- [ ] 单元测试覆盖率>80%
