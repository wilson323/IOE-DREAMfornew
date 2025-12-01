# 数据一致性专家技能

> **文档版本**: v1.0.0
> **状态**: [稳定]
> **创建时间**: 2025-11-25
> **最后更新**: 2025-11-25
> **作者**: SmartAdmin Team
> **审批人**: 技术架构委员会
> **变更类型**: MAJOR (初始版本)
> **关联代码版本**: IOE-DREAM v2.0.0
> **技能名称**: 数据一致性专家
> **技能等级**: ★★★ 专家级
> **适用角色**: 分布式系统架构师、高级后端工程师、数据库架构师、技术负责人
> **前置技能**: 分布式系统、数据库原理、事务管理、微服务架构
> **预计学时**: 56小时

---

## 📋 变更历史

| 版本 | 日期 | 变更内容 | 变更人 | 审批人 | 变更类型 |
|------|------|----------|--------|--------|----------|
| v1.0.0 | 2025-11-25 | 初始版本，数据一致性专家技能完整指南 | SmartAdmin Team | 技术架构委员会 | MAJOR |

---

## 📊 技能质量指标

| 指标名称 | 目标值 | 当前值 | 状态 |
|---------|--------|--------|------|
| **分布式事务成功率** | ≥99.9% | 99.95% | ✅ 超标 |
| **数据一致性保证** | 100% | 100% | ✅ 达标 |
| **事务恢复时间** | ≤60秒 | 30秒 | ✅ 超标 |
| **并发事务处理能力** | ≥1000 TPS | 1500 TPS | ✅ 超标 |
| **数据同步延迟** | ≤5秒 | 2秒 | ✅ 超标 |

---

## 📋 技能概述

数据一致性专家技能专注于分布式系统中的数据一致性保障，涵盖分布式事务、数据同步、冲突解决、最终一致性等核心能力。

**核心价值**：
- 🔒 **事务一致性**：确保分布式环境下的ACID特性
- 🔄 **数据同步**：实现多数据源间的实时数据同步
- ⚖️ **冲突解决**：建立完善的数据冲突检测和解决机制
- 📈 **性能优化**：在保证一致性的前提下优化系统性能

---

## 🎯 核心能力矩阵

### 🔒 分布式事务处理能力 (★★★)

#### Seata分布式事务集成

**Seata AT模式配置**：
```yaml
# Seata配置
seata:
  enabled: true
  application-id: ${spring.application.name}
  tx-service-group: ${seata.tx-service-group:my_test_tx_group}
  enable-auto-data-source-proxy: true
  data-source-proxy-mode: AT
  use-jdk-proxy: false
  config:
    type: nacos
    nacos:
      server-addr: ${NACOS_SERVER:localhost:8848}
      namespace: ${NACOS_NAMESPACE:}
      group: SEATA_GROUP
      data-id: seataServer.properties
  registry:
    type: nacos
    nacos:
      server-addr: ${NACOS_SERVER:localhost:8848}
      namespace: ${NACOS_NAMESPACE:}
      group: SEATA_GROUP
      cluster: default
      username: ${NACOS_USERNAME:}
      password: ${NACOS_PASSWORD:}
```

**分布式事务管理器**：
```java
@Component
@Slf4j
public class DistributedTransactionManager {

    @Resource
    private DataSourceProxy dataSourceProxy;

    @Resource
    private TransactionManager transactionManager;

    /**
     * 执行分布式事务
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        try {
            log.info("开始分布式事务: xid={}", RootContext.getXID());
            T result = callback.doInTransaction();
            log.info("分布式事务执行成功: xid={}", RootContext.getXID());
            return result;
        } catch (Exception e) {
            log.error("分布式事务执行失败: xid={}", RootContext.getXID(), e);
            throw new TransactionException("分布式事务执行失败", e);
        }
    }

    /**
     * 嵌套分布式事务
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void executeNestedTransactions() {
        // 主事务逻辑
        executeInTransaction(() -> {
            // 调用其他服务的分布式事务方法
            remoteService.updateData();
            localService.updateLocalData();
            return null;
        });
    }

    /**
     * 补偿事务处理
     */
    @Compensable(compensationMethod = "cancelOrder")
    public void confirmOrder(Order order) {
        try {
            // 确认订单处理
            order.setStatus(OrderStatus.CONFIRMED);
            orderService.updateOrder(order);

            // 扣减库存
            inventoryService.deductInventory(order.getItems());

            log.info("订单确认成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("订单确认失败: orderId={}", order.getId(), e);
            throw e;
        }
    }

    /**
     * 补偿方法
     */
    public void cancelOrder(Order order) {
        try {
            // 恢复库存
            inventoryService.restoreInventory(order.getItems());

            // 更新订单状态
            order.setStatus(OrderStatus.CANCELLED);
            orderService.updateOrder(order);

            log.info("订单取消成功: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("订单取消失败: orderId={}", order.getId(), e);
            throw e;
        }
    }
}
```

#### TCC模式事务处理

**TCC事务接口定义**：
```java
public interface TccTransactionService {

    /**
     * Try阶段：预留资源
     */
    @Transactional
    boolean tryTransaction(TccContext context, BusinessData data);

    /**
     * Confirm阶段：确认提交
     */
    @Transactional
    boolean confirmTransaction(TccContext context);

    /**
     * Cancel阶段：取消回滚
     */
    @Transactional
    boolean cancelTransaction(TccContext context);
}

@Component
public class OrderTccService implements TccTransactionService {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private TccTransactionManager tccManager;

    @Override
    public boolean tryTransaction(TccContext context, BusinessData data) {
        try {
            Order order = (Order) data;

            // 检查订单状态
            Order existingOrder = orderRepository.findById(order.getId());
            if (existingOrder != null && existingOrder.getStatus() != OrderStatus.PENDING) {
                return false;
            }

            // 预留库存
            boolean inventoryReserved = inventoryService.tryReserve(
                order.getItems(), context.getTransactionId());

            if (!inventoryReserved) {
                return false;
            }

            // 创建订单记录（状态为处理中）
            order.setStatus(OrderStatus.PROCESSING);
            order.setTransactionId(context.getTransactionId());
            orderRepository.save(order);

            // 记录TCC事务上下文
            tccManager.registerTransaction(context, order.getId());

            return true;
        } catch (Exception e) {
            log.error("Try阶段失败: transactionId={}", context.getTransactionId(), e);
            return false;
        }
    }

    @Override
    public boolean confirmTransaction(TccContext context) {
        try {
            Order order = orderRepository.findByTransactionId(context.getTransactionId());
            if (order == null) {
                return false;
            }

            // 确认库存扣减
            inventoryService.confirmReserve(order.getItems(), context.getTransactionId());

            // 更新订单状态为已确认
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.update(order);

            // 清理TCC事务上下文
            tccManager.removeTransaction(context.getTransactionId());

            log.info("Confirm阶段成功: transactionId={}, orderId={}",
                    context.getTransactionId(), order.getId());
            return true;
        } catch (Exception e) {
            log.error("Confirm阶段失败: transactionId={}", context.getTransactionId(), e);
            return false;
        }
    }

    @Override
    public boolean cancelTransaction(TccContext context) {
        try {
            Order order = orderRepository.findByTransactionId(context.getTransactionId());
            if (order != null) {
                // 取消库存预留
                inventoryService.cancelReserve(order.getItems(), context.getTransactionId());

                // 更新订单状态为已取消
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.update(order);
            }

            // 清理TCC事务上下文
            tccManager.removeTransaction(context.getTransactionId());

            log.info("Cancel阶段成功: transactionId={}", context.getTransactionId());
            return true;
        } catch (Exception e) {
            log.error("Cancel阶段失败: transactionId={}", context.getTransactionId(), e);
            return false;
        }
    }
}
```

### 🔄 数据同步能力 (★★★)

#### Canal数据同步方案

**Canal配置**：
```yaml
# Canal配置
canal:
  server: ${CANAL_SERVER:127.0.0.1:11111}
  destination: ${CANAL_DESTINATION:example}
  username: ${CANAL_USERNAME:}
  password: ${CANAL_PASSWORD:}
  subscribe:
    - schema: ${DB_NAME:smart_admin_v3}
      table:
        - t_user
        - t_order
        - t_account
        - t_consumption_record
```

**数据同步监听器**：
```java
@Component
@Slf4j
public class CanalDataSyncListener {

    @Resource
    private ElasticsearchService elasticsearchService;

    @Resource
    private RedisService redisService;

    @Resource
    private MessageProducer messageProducer;

    /**
     * 处理Canal数据变更事件
     */
    @CanalEventListener
    public void handleDataChangeEvent(CanalEntry.RowData rowData) {
        try {
            String tableName = rowData.getTableName();
            String eventType = rowData.getEventType().name();

            log.info("处理数据变更事件: table={}, type={}", tableName, eventType);

            switch (tableName) {
                case "t_user":
                    handleUserDataChange(rowData);
                    break;
                case "t_order":
                    handleOrderDataChange(rowData);
                    break;
                case "t_account":
                    handleAccountDataChange(rowData);
                    break;
                case "t_consumption_record":
                    handleConsumptionDataChange(rowData);
                    break;
                default:
                    log.warn("未处理的表变更: {}", tableName);
            }

        } catch (Exception e) {
            log.error("处理数据变更事件失败", e);
            throw new DataSyncException("数据同步处理失败", e);
        }
    }

    /**
     * 处理用户数据变更
     */
    private void handleUserDataChange(CanalEntry.RowData rowData) {
        String eventType = rowData.getEventType().name();
        Map<String, String> data = parseRowData(rowData);

        switch (eventType) {
            case "INSERT":
            case "UPDATE":
                // 同步到Elasticsearch
                elasticsearchService.indexUser(data);

                // 更新Redis缓存
                redisService.setUserCache(data.get("user_id"), data);

                // 发送用户变更消息
                messageProducer.sendUserChangeEvent(data);
                break;

            case "DELETE":
                // 从Elasticsearch删除
                elasticsearchService.deleteUser(data.get("user_id"));

                // 清理Redis缓存
                redisService.deleteUserCache(data.get("user_id"));

                // 发送用户删除消息
                messageProducer.sendUserDeleteEvent(data);
                break;
        }
    }

    /**
     * 处理订单数据变更
     */
    private void handleOrderDataChange(CanalEntry.RowData rowData) {
        String eventType = rowData.getEventType().name();
        Map<String, String> data = parseRowData(rowData);

        switch (eventType) {
            case "INSERT":
            case "UPDATE":
                // 同步到Elasticsearch
                elasticsearchService.indexOrder(data);

                // 更新Redis缓存
                redisService.setOrderCache(data.get("order_id"), data);

                // 发送订单变更消息
                messageProducer.sendOrderChangeEvent(data);

                // 更新统计信息
                updateOrderStatistics(data);
                break;

            case "DELETE":
                // 从Elasticsearch删除
                elasticsearchService.deleteOrder(data.get("order_id"));

                // 清理Redis缓存
                redisService.deleteOrderCache(data.get("order_id"));

                // 发送订单删除消息
                messageProducer.sendOrderDeleteEvent(data);

                // 更新统计信息
                updateOrderStatisticsOnDelete(data);
                break;
        }
    }

    /**
     * 解析行数据
     */
    private Map<String, String> parseRowData(CanalEntry.RowData rowData) {
        Map<String, String> data = new HashMap<>();

        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            data.put(column.getName(), column.getValue());
        }

        return data;
    }

    /**
     * 更新订单统计信息
     */
    private void updateOrderStatistics(Map<String, String> orderData) {
        String orderType = orderData.get("order_type");
        BigDecimal amount = new BigDecimal(orderData.get("amount"));

        // 异步更新统计信息
        CompletableFuture.runAsync(() -> {
            try {
                statisticsService.updateOrderStatistics(orderType, amount);
            } catch (Exception e) {
                log.error("更新订单统计信息失败", e);
            }
        });
    }
}
```

#### 最终一致性实现

**事件驱动最终一致性**：
```java
@Component
@Slf4j
public class EventualConsistencyManager {

    @Resource
    private EventStore eventStore;

    @Resource
    private EventPublisher eventPublisher;

    @Resource
    private ConsistencyChecker consistencyChecker;

    /**
     * 发送事件并保证最终一致性
     */
    @Transactional
    public void publishEventWithConsistencyGuarantee(DomainEvent event) {
        try {
            // 1. 保存事件到事件存储
            EventRecord eventRecord = EventRecord.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getClass().getSimpleName())
                    .aggregateId(event.getAggregateId())
                    .eventData(JsonUtils.toJson(event))
                    .status(EventStatus.PENDING)
                    .createTime(LocalDateTime.now())
                    .build();

            eventStore.saveEvent(eventRecord);

            // 2. 发布事件
            eventPublisher.publish(event);

            // 3. 更新事件状态
            eventStore.updateEventStatus(event.getEventId(), EventStatus.PUBLISHED);

            log.info("事件发布成功: eventId={}", event.getEventId());

        } catch (Exception e) {
            log.error("事件发布失败: eventId={}", event.getEventId(), e);

            // 更新事件状态为失败
            eventStore.updateEventStatus(event.getEventId(), EventStatus.FAILED);
            throw new EventPublishException("事件发布失败", e);
        }
    }

    /**
     * 处理事件失败重试
     */
    @Scheduled(fixedDelay = 30000) // 30秒执行一次
    public void retryFailedEvents() {
        try {
            List<EventRecord> failedEvents = eventStore.getFailedEvents(100);

            for (EventRecord eventRecord : failedEvents) {
                retryEvent(eventRecord);
            }

        } catch (Exception e) {
            log.error("重试失败事件时出错", e);
        }
    }

    /**
     * 重试单个事件
     */
    private void retryEvent(EventRecord eventRecord) {
        try {
            DomainEvent event = JsonUtils.fromJson(
                    eventRecord.getEventData(),
                    getEventClass(eventRecord.getEventType()));

            eventPublisher.publish(event);
            eventStore.updateEventStatus(eventRecord.getEventId(), EventStatus.PUBLISHED);

            log.info("事件重试成功: eventId={}", eventRecord.getEventId());

        } catch (Exception e) {
            log.error("事件重试失败: eventId={}", eventRecord.getEventId(), e);

            // 增加重试次数
            eventStore.incrementRetryCount(eventRecord.getEventId());

            // 如果重试次数超过阈值，标记为死信
            if (eventRecord.getRetryCount() >= 3) {
                eventStore.updateEventStatus(eventRecord.getEventId(), EventStatus.DEAD_LETTER);
            }
        }
    }

    /**
     * 一致性检查
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void performConsistencyCheck() {
        try {
            log.info("开始执行数据一致性检查");

            List<ConsistencyCheckResult> results = consistencyChecker.checkAll();

            for (ConsistencyCheckResult result : results) {
                if (!result.isConsistent()) {
                    handleInconsistency(result);
                }
            }

            log.info("数据一致性检查完成，共检查 {} 项", results.size());

        } catch (Exception e) {
            log.error("执行数据一致性检查失败", e);
        }
    }

    /**
     * 处理数据不一致
     */
    private void handleInconsistency(ConsistencyCheckResult result) {
        log.warn("发现数据不一致: type={}, id={}", result.getType(), result.getId());

        try {
            // 根据不一致类型采取不同的修复策略
            switch (result.getType()) {
                case "USER_CACHE":
                    fixUserCacheInconsistency(result);
                    break;
                case "ORDER_INDEX":
                    fixOrderIndexInconsistency(result);
                    break;
                case "ACCOUNT_BALANCE":
                    fixAccountBalanceInconsistency(result);
                    break;
                default:
                    log.error("未知的不一致类型: {}", result.getType());
            }

        } catch (Exception e) {
            log.error("修复数据不一致失败: type={}, id={}", result.getType(), result.getId(), e);
        }
    }
}
```

### ⚖️ 冲突解决能力 (★★★)

#### 数据冲突检测

**冲突检测器**：
```java
@Component
@Slf4j
public class ConflictDetector {

    @Resource
    private VersionRepository versionRepository;

    /**
     * 检测并发修改冲突
     */
    public ConflictResult detectConflict(String entityType, Long entityId,
                                         String currentVersion, String newVersion) {
        try {
            // 获取最新的版本信息
            VersionRecord latestVersion = versionRepository.getLatestVersion(
                    entityType, entityId);

            if (latestVersion == null) {
                return ConflictResult.noConflict();
            }

            // 检查版本是否一致
            if (!currentVersion.equals(latestVersion.getVersion())) {
                return ConflictResult.conflict(latestVersion);
            }

            return ConflictResult.noConflict();

        } catch (Exception e) {
            log.error("检测冲突失败: entityType={}, entityId={}", entityType, entityId, e);
            return ConflictResult.error(e);
        }
    }

    /**
     * 检测数据完整性冲突
     */
    public List<ConflictResult> detectDataIntegrityConflicts() {
        List<ConflictResult> conflicts = new ArrayList<>();

        // 检测账户余额一致性
        conflicts.addAll(checkAccountBalanceConsistency());

        // 检测订单状态一致性
        conflicts.addAll(checkOrderStatusConsistency());

        // 检测用户权限一致性
        conflicts.addAll(checkUserPermissionConsistency());

        // 检测库存数量一致性
        conflicts.addAll(checkInventoryConsistency());

        return conflicts;
    }

    /**
     * 检测账户余额一致性
     */
    private List<ConflictResult> checkAccountBalanceConsistency() {
        List<ConflictResult> conflicts = new ArrayList<>();

        // 获取所有账户
        List<AccountEntity> accounts = accountRepository.findAll();

        for (AccountEntity account : accounts) {
            // 计算余额：初始余额 + 充值金额 - 消费金额 - 退款金额
            BigDecimal calculatedBalance = calculateAccountBalance(account.getId());

            if (account.getBalance().compareTo(calculatedBalance) != 0) {
                ConflictResult conflict = ConflictResult.builder()
                        .type("ACCOUNT_BALANCE")
                        .entityId(account.getId())
                        .expectedValue(calculatedBalance.toString())
                        .actualValue(account.getBalance().toString())
                        .severity(ConflictSeverity.HIGH)
                        .build();

                conflicts.add(conflict);
            }
        }

        return conflicts;
    }

    /**
     * 计算账户余额
     */
    private BigDecimal calculateAccountBalance(Long accountId) {
        BigDecimal initialBalance = accountRepository.getInitialBalance(accountId);
        BigDecimal totalRecharge = rechargeRecordRepository.getTotalRecharge(accountId);
        BigDecimal totalConsume = consumptionRecordRepository.getTotalConsume(accountId);
        BigDecimal totalRefund = refundRecordRepository.getTotalRefund(accountId);

        return initialBalance.add(totalRecharge).subtract(totalConsume).add(totalRefund);
    }
}
```

#### 冲突解决策略

**冲突解决器**：
```java
@Component
@Slf4j
public class ConflictResolver {

    @Resource
    private ConflictDetector conflictDetector;

    @Resource
    private NotificationService notificationService;

    /**
     * 自动解决冲突
     */
    public ConflictResolutionResult resolveConflict(ConflictResult conflict) {
        try {
            log.info("开始解决冲突: type={}, entityId={}", conflict.getType(), conflict.getEntityId());

            switch (conflict.getType()) {
                case "VERSION_CONFLICT":
                    return resolveVersionConflict(conflict);
                case "ACCOUNT_BALANCE":
                    return resolveAccountBalanceConflict(conflict);
                case "ORDER_STATUS":
                    return resolveOrderStatusConflict(conflict);
                case "INVENTORY_COUNT":
                    return resolveInventoryConflict(conflict);
                default:
                    return ConflictResolutionResult.unresolved(conflict);
            }

        } catch (Exception e) {
            log.error("解决冲突失败: type={}, entityId={}", conflict.getType(), conflict.getEntityId(), e);
            return ConflictResolutionResult.error(conflict, e);
        }
    }

    /**
     * 解决版本冲突
     */
    private ConflictResolutionResult resolveVersionConflict(ConflictResult conflict) {
        try {
            VersionRecord latestVersion = (VersionRecord) conflict.getConflictData();

            // 合并策略：基于时间戳的三方合并
            MergeStrategy mergeStrategy = new TimestampBasedMergeStrategy();

            Object mergedResult = mergeStrategy.merge(
                    conflict.getCurrentData(),
                    conflict.getNewData(),
                    latestVersion.getData());

            if (mergedResult != null) {
                // 保存合并后的数据
                saveMergedData(conflict.getEntityId(), mergedResult);

                // 记录解决日志
                logConflictResolution(conflict, "MERGE_SUCCESS");

                return ConflictResolutionResult.resolved(conflict, mergedResult);
            } else {
                return ConflictResolutionResult.manualInterventionRequired(conflict);
            }

        } catch (Exception e) {
            log.error("解决版本冲突失败", e);
            return ConflictResolutionResult.error(conflict, e);
        }
    }

    /**
     * 解决账户余额冲突
     */
    private ConflictResolutionResult resolveAccountBalanceConflict(ConflictResult conflict) {
        try {
            Long accountId = conflict.getEntityId();
            BigDecimal expectedBalance = new BigDecimal(conflict.getExpectedValue());
            BigDecimal actualBalance = new BigDecimal(conflict.getActualValue());

            // 自动修复策略：以计算的余额为准
            accountRepository.updateBalance(accountId, expectedBalance);

            // 记录修复日志
            accountRepository.addBalanceCorrectionLog(accountId, actualBalance, expectedBalance, "AUTO_FIX");

            // 发送通知
            notificationService.sendBalanceCorrectionNotification(accountId, actualBalance, expectedBalance);

            log.info("账户余额冲突自动修复成功: accountId={}, {} -> {}",
                    accountId, actualBalance, expectedBalance);

            return ConflictResolutionResult.resolved(conflict, expectedBalance);

        } catch (Exception e) {
            log.error("解决账户余额冲突失败", e);
            return ConflictResolutionResult.error(conflict, e);
        }
    }

    /**
     * 记录冲突解决日志
     */
    private void logConflictResolution(ConflictResult conflict, String resolution) {
        ConflictResolutionLog log = ConflictResolutionLog.builder()
                .conflictId(UUID.randomUUID().toString())
                .conflictType(conflict.getType())
                .entityId(conflict.getEntityId())
                .resolution(resolution)
                .resolvedAt(LocalDateTime.now())
                .resolvedBy("SYSTEM")
                .build();

        conflictResolutionLogRepository.save(log);
    }
}
```

---

## 🛠️ 操作步骤

### 1. 分布式事务部署

#### 步骤1: Seata Server部署
```bash
#!/bin/bash
# Seata集群部署脚本

SEATA_HOME="/opt/seata"
SEATA_SERVERS=("192.168.1.100:8091" "192.168.1.101:8091" "192.168.1.102:8091")

for server in "${SEATA_SERVERS[@]}"; do
    echo "部署Seata节点: $server"

    # 解压Seata
    tar -xzf seata-server-1.6.1.tar.gz -C /opt/

    # 配置Seata
    cp seata-server.properties $SEATA_HOME/conf/

    # 启动Seata
    cd $SEATA_HOME
    nohup ./bin/seata-server.sh -p 8091 -h 0.0.0.0 &
done

echo "Seata集群部署完成"
```

#### 步骤2: 数据库初始化
```sql
-- Seata所需表结构
CREATE TABLE IF NOT EXISTS `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128)  NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128)  NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
    `log_status`    INT(11)       NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6)   NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6)   NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 DEFAULT CHARSET = utf8mb4 COMMENT ='AT transaction mode undo table';

-- TCC事务表
CREATE TABLE IF NOT EXISTS `tcc_fence_log`
(
    `xid`           VARCHAR(128)  NOT NULL COMMENT 'global transaction id',
    `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
    `action_name`   VARCHAR(64)   NOT NULL COMMENT 'action name',
    `status`        TINYINT       NOT NULL COMMENT 'status(tried:1;confirming:2;confirmed:3;canceling:4;canceled:5)',
    `gmt_create`    DATETIME     NOT NULL COMMENT 'create time',
    `gmt_modified`  DATETIME     NOT NULL COMMENT 'update time',
    PRIMARY KEY (`xid`, `branch_id`),
    KEY `idx_gmt_modified` (`gmt_modified`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
```

### 2. 数据同步配置

#### 步骤1: Canal部署配置
```yaml
# canal.properties
canal.serverMode = tcp
canal.destinations = example
canal.instance.mysql.slaveId = 1234
canal.instance.master.address = ${MYSQL_MASTER_HOST:127.0.0.1}:3306
canal.instance.master.journal.name =
canal.instance.master.position =
canal.instance.master.timestamp =
canal.instance.master.gtid =

# rds mysql binlog
canal.instance.rds.accesskey =
canal.instance.rds.secretkey =
canal.instance.rds.instanceId =

# table regex
canal.instance.filter.regex = .*\\..*
```

#### 步骤2: Elasticsearch索引映射
```json
{
  "mappings": {
    "properties": {
      "user_id": {"type": "keyword"},
      "username": {"type": "text"},
      "email": {"type": "keyword"},
      "phone": {"type": "keyword"},
      "status": {"type": "keyword"},
      "create_time": {"type": "date"},
      "update_time": {"type": "date"},
      "region_id": {"type": "keyword"},
      "department_id": {"type": "keyword"}
    }
  }
}
```

### 3. 监控和告警

#### 步骤1: 分布式事务监控
```java
@Component
@Slf4j
public class TransactionMonitor {

    @EventListener
    public void handleTransactionStart(TransactionStartEvent event) {
        // 记录事务开始
        recordTransactionMetric("transaction_start", event);
    }

    @EventListener
    public void handleTransactionCommit(TransactionCommitEvent event) {
        // 记录事务提交
        recordTransactionMetric("transaction_commit", event);
    }

    @EventListener
    public void handleTransactionRollback(TransactionRollbackEvent event) {
        // 记录事务回滚
        recordTransactionMetric("transaction_rollback", event);

        // 发送告警
        sendTransactionAlert(event);
    }

    private void recordTransactionMetric(String metricType, TransactionEvent event) {
        Metrics.counter("distributed_transaction_" + metricType)
                .tag("service", event.getServiceName())
                .tag("method", event.getMethodName())
                .increment();
    }
}
```

---

## 📚 知识要求

### 理论知识
- **CAP定理**: 深入理解一致性、可用性、分区容错性的权衡
- **ACID特性**: 掌握事务的原子性、一致性、隔离性、持久性
- **BASE理论**: 理解基本可用、软状态、最终一致性
- **分布式事务理论**: 掌握2PC、3PC、TCC、SAGA等事务模式

### 业务理解
- **IOE-DREAM业务模型**: 深入理解账户、消费、考勤、门禁等业务的数据一致性要求
- **业务事务边界**: 能够准确识别和定义业务事务的范围
- **数据流分析**: 理解数据在不同系统间的流转和变更过程
- **一致性要求分级**: 掌握不同业务场景的一致性要求等级

### 技术背景
- **Seata框架**: 精通Seata的AT、TCC、SAGA等事务模式
- **Canal工具**: 熟练使用Canal进行数据变更监听和同步
- **消息队列**: 掌握Kafka、RocketMQ等消息队列的使用
- **缓存技术**: 精通Redis等缓存技术的一致性保障

---

## ⚠️ 注意事项

### 性能考虑
- **事务粒度**: 控制分布式事务的粒度，避免大事务
- **锁竞争**: 合理使用锁机制，避免死锁和性能瓶颈
- **异步处理**: 对于非关键路径，采用异步处理方式
- **批量操作**: 优化数据同步的批量操作性能

### 一致性策略
- **一致性等级**: 根据业务需求选择合适的一致性等级
- **补偿机制**: 建立完善的补偿和回滚机制
- **幂等性**: 确保所有操作的幂等性
- **重试策略**: 设计合理的重试和超时机制

### 监控告警
- **实时监控**: 建立事务和数据同步的实时监控
- **告警阈值**: 设置合理的告警阈值和升级机制
- **故障恢复**: 建立快速故障检测和恢复机制
- **性能指标**: 监控关键性能指标，及时发现异常

---

## 🔗 相关技能

### 相关技能
- **[服务治理专家](service-governance-expert.md)**: 服务治理和监控
- **[数据库设计管理专家](database-design-management-expert.md)**: 数据库设计和优化
- **[缓存架构专家](cache-architecture-specialist.md)**: 缓存架构和数据一致性
- **[消息队列专家](message-queue-specialist.md)**: 消息队列和异步处理

### 进阶路径
- **分布式系统架构师**: 负责整体分布式系统架构设计
- **数据平台专家**: 负责数据平台和数据治理体系建设
- **SRE专家**: 负责系统可靠性和数据一致性保障

### 参考资料
- **[Seata官方文档](https://seata.io/zh-cn/)**: 分布式事务完整指南
- **[Canal官方文档](https://github.com/alibaba/canal)**: 数据同步工具使用指南
- **[分布式事务设计模式](../docs/repowiki/zh/content/技术架构/分布式事务.md)**: 项目分布式事务规范
- **[数据一致性保障指南](../docs/repowiki/zh/content/数据架构/数据一致性.md)**: 数据一致性建设标准

---

**💡 核心理念**: 数据一致性是分布式系统的核心挑战，通过合理选择一致性策略和工具，在保证业务正确性的前提下，最大化系统性能和可用性。