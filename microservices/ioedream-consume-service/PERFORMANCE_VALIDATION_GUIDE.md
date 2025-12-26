# 消费服务性能验证方案

**文档版本**: v1.0
**创建时间**: 2025-12-23
**适用范围**: 生产环境部署后的性能验证

---

## 📊 验证目标

| 指标 | 目标值 | 验证方法 | 验证工具 |
|------|--------|---------|---------|
| **TPS** | ≥ 1000 | JMeter压力测试 | JMeter + Python脚本 |
| **平均响应时间** | ≤ 50ms | JMeter响应时间统计 | JMeter + Prometheus |
| **P95响应时间** | ≤ 100ms | JMeter百分位统计 | JMeter |
| **缓存命中率** | ≥ 90% | MultiLevelCacheManager统计 | Prometheus + Grafana |
| **双写一致性** | ≥ 99.9% | DualWriteValidationManager验证 | MySQL查询 |
| **错误率** | ≤ 1% | JMeter错误统计 | JMeter |
| **成功率** | ≥ 99% | JMeter成功率统计 | JMeter |

---

## 🚀 验证前准备

### 1. 环境准备

```bash
# 1.1 确保数据库已迁移
cd D:\IOE-DREAM\microservices\ioedream-consume-service\scripts
.\execute-migration.ps1

# 1.2 验证表创建成功
mysql -h127.0.0.1 -uroot -p ioedream -e "SHOW TABLES LIKE 'POSID_%';"

# 1.3 启动消费服务
cd D:\IOE-DREAM\microservices\ioedream-consume-service
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# 1.4 验证服务健康状态
curl http://localhost:8094/actuator/health
```

### 2. 数据准备

```sql
-- 2.1 创建测试账户
INSERT INTO POSID_ACCOUNT (user_id, account_code, balance, account_type, account_status)
VALUES
(1, 'TEST001', 1000.00, 1, 1),
(2, 'TEST002', 1000.00, 1, 1),
(3, 'TEST003', 1000.00, 1, 1);

-- 2.2 创建测试区域
INSERT INTO POSID_AREA (area_id, area_code, area_name, manage_mode, area_status)
VALUES
(1, 'AREA001', '测试区域1', 1, 1),
(2, 'AREA002', '测试区域2', 2, 1);

-- 2.3 创建测试商品（用于商品模式消费）
INSERT INTO POSID_PRODUCT (product_id, product_code, product_name, price, product_status)
VALUES
(1, 'PROD001', '测试商品1', 10.00, 1),
(2, 'PROD002', '测试商品2', 20.00, 1);

-- 2.4 验证数据准备
SELECT * FROM POSID_ACCOUNT;
SELECT * FROM POSID_AREA;
SELECT * FROM POSID_PRODUCT;
```

### 3. 监控准备

```bash
# 3.1 启动Prometheus（如果未启动）
cd D:\IOE-DREAM\tools\prometheus
.\prometheus.exe --config.file=prometheus.yml

# 3.2 导入Grafana仪表盘
# 访问 http://localhost:3000
# 导入文件: D:\IOE-DREAM\microservices\ioedream-consume-service\config\grafana-dashboard.json

# 3.3 验证Prometheus指标采集
curl http://localhost:9090/api/v1/targets
```

---

## ✅ 验证项1: TPS ≥ 1000

### 验证方法

使用JMeter进行并发压力测试，验证系统TPS是否达到目标值。

### 执行步骤

```bash
# 1.1 进入性能测试目录
cd D:\IOE-DREAM\microservices\ioedream-consume-service\scripts

# 1.2 执行性能测试脚本
bash performance-test.sh

# 或手动执行JMeter
jmeter -n -t consume-test.jmx \
       -l test-results.jtl \
       -e -o html-report \
       -JHOST=localhost -JPORT=8094
```

### 验证标准

```python
# 自动验证脚本（已内置在performance-test.sh中）
if tps >= 1000:
    print("✓ TPS达标")
else:
    print(f"✗ TPS未达标: {tps} < 1000")
```

### 预期结果

```
✓ TPS达标: 1234.56 >= 1000
✓ 平均响应时间达标: 35.24ms <= 50ms
✓ P95响应时间达标: 82.15ms <= 100ms
✓ 成功率达标: 99.85% >= 99%
```

### 故障排查

**如果TPS不达标**：

1. **检查数据库连接池**
   ```sql
   SHOW PROCESSLIST;
   -- 查看活跃连接数
   ```

2. **检查慢查询**
   ```sql
   SELECT * FROM information_schema.PROCESSLIST
   WHERE TIME > 1;
   ```

3. **优化缓存配置**
   ```yaml
   spring:
     cache:
       type: redis
       redis:
         time-to-live: 3600000  # 1小时
   ```

4. **检查批处理配置**
   ```yaml
   consume:
     batch:
       enabled: true
       size: 100
       thread-pool-size: 10
   ```

---

## ✅ 验证项2: 平均响应时间 ≤ 50ms

### 验证方法

通过JMeter测试结果和Prometheus监控指标验证响应时间。

### Prometheus查询

```promql
# 平均响应时间（1分钟窗口）
rate(consume_response_time_sum[1m]) / rate(consume_response_time_count[1m]) * 1000

# P95响应时间
histogram_quantile(0.95, rate(consume_response_time_bucket[1m])) * 1000
```

### 执行步骤

```bash
# 2.1 执行JMeter测试（已在TPS验证中执行）
bash performance-test.sh

# 2.2 查看Prometheus指标
curl http://localhost:9090/api/v1/query?query=rate(consume_response_time_sum%5B1m%5D)%20%2F%20rate(consume_response_time_count%5B1m%5D)%20*%201000

# 2.3 查看Grafana仪表盘
# 访问 http://localhost:3000/d/consume-service-dashboard
```

### 验证标准

| 指标 | 目标值 | 实际值 | 状态 |
|------|--------|--------|------|
| 平均响应时间 | ≤ 50ms | __ms | ⏳ 待验证 |
| P50响应时间 | ≤ 30ms | __ms | ⏳ 待验证 |
| P95响应时间 | ≤ 100ms | __ms | ⏳ 待验证 |
| P99响应时间 | ≤ 200ms | __ms | ⏳ 待验证 |

### 优化建议

**如果响应时间过长**：

1. **启用二级缓存**
   ```java
   @Cacheable(value = "account", key = "#accountId")
   public PosidAccountEntity getAccountById(Long accountId) {
       return accountDao.selectById(accountId);
   }
   ```

2. **优化数据库查询**
   ```sql
   -- 添加索引
   CREATE INDEX idx_account_user_id ON POSID_ACCOUNT(user_id);
   CREATE INDEX idx_record_create_time ON POSID_CONSUME_RECORD(create_time);
   ```

3. **使用批量插入**
   ```java
   // 使用JDBC批处理
   sqlSession.flushStatements();
   sqlSession.clearCache();
   ```

---

## ✅ 验证项3: 缓存命中率 ≥ 90%

### 验证方法

通过MultiLevelCacheManager的统计功能和Prometheus监控验证缓存命中率。

### Prometheus查询

```promql
# L1缓存命中率
consume_cache_l1_hit_rate

# L2缓存命中率
consume_cache_l2_hit_rate

# 综合缓存命中率
consume_cache_hit_rate
```

### 执行步骤

```bash
# 3.1 执行缓存预热
curl -X POST http://localhost:8094/api/consume/cache/warmup

# 3.2 执行多次消费请求
for i in {1..1000}; do
  curl -X POST http://localhost:8094/api/consume/transaction \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"areaId":1,"amount":10.00}'
done

# 3.3 查询缓存统计
curl http://localhost:8094/actuator/metrics/cache.hit.ratio

# 3.4 查看Prometheus指标
curl http://localhost:9090/api/v1/query?query=consume_cache_hit_rate
```

### 验证标准

```
L1缓存命中率 ≥ 80%
L2缓存命中率 ≥ 10%
综合缓存命中率 ≥ 90%
```

### 优化建议

**如果缓存命中率不达标**：

1. **增大缓存容量**
   ```yaml
   spring:
     cache:
       cache-names: account,area,product
       caffeine:
           spec: maximumSize=10000,expireAfterWrite=3600s
   ```

2. **调整缓存过期时间**
   ```java
   @Cacheable(value = "account", key = "#accountId", unless = "#result == null")
   @CacheEvict(value = "account", key = "#accountId", allEntries = false)
   ```

3. **使用缓存穿透防护**
   ```java
   // 空值缓存
   if (value == null) {
       cache.put(key, NULL_VALUE);
   }
   ```

---

## ✅ 验证项4: 双写一致性 ≥ 99.9%

### 验证方法

通过DualWriteValidationManager的验证日志和MySQL查询验证双写数据一致性。

### 执行步骤

```bash
# 4.1 确保双写模式已启用
# 检查配置文件
grep "consume.write.mode" application-docker.yml
# 应该是: consume.write.mode: dual

# 4.2 查看双写验证日志（最近24小时）
mysql -h127.0.0.1 -uroot -p ioedream -e "
SELECT
    validation_type,
    COUNT(*) AS total_validations,
    SUM(CASE WHEN validation_status = 1 THEN 1 ELSE 0 END) AS passed_count,
    AVG(consistency_rate) AS avg_consistency_rate,
    MIN(consistency_rate) AS min_consistency_rate
FROM dual_write_validation_log
WHERE validate_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY validation_type;
"

# 4.3 检查未解决的数据差异
mysql -h127.0.0.1 -uroot -p ioedream -e "
SELECT
    data_type,
    COUNT(*) AS unresolved_count
FROM dual_write_difference_record
WHERE resolved = 0
GROUP BY data_type;
"

# 4.4 对比新旧表数据量
mysql -h127.0.0.1 -uroot -p ioedream -e "
SELECT
    '旧表账户' AS label,
    COUNT(*) AS count
FROM t_consume_account
WHERE deleted_flag = 0
UNION ALL
SELECT
    '新表账户' AS label,
    COUNT(*) AS count
FROM POSID_ACCOUNT
WHERE deleted_flag = 0;
"
```

### 验证标准

```
✓ passed_count = total_validations（全部通过）
✓ avg_consistency_rate >= 0.999（平均一致性≥99.9%）
✓ min_consistency_rate >= 0.999（最小一致性≥99.9%）
✓ unresolved_count = 0（无未解决差异）
✓ 旧表数据量 = 新表数据量
```

### 手动验证脚本

```bash
# 保存为 validate-dual-write.sh
#!/bin/bash

mysql -h127.0.0.1 -uroot -p ioedream << 'EOF'
-- 检查账户表一致性
SELECT
    'ACCOUNT' AS table_name,
    (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) =
             (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0)
        THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS result;

-- 检查消费记录表一致性
SELECT
    'CONSUME_RECORD' AS table_name,
    (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS old_count,
    (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0) AS new_count,
    CASE
        WHEN (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) =
             (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0)
        THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS result;
EOF
```

### 故障排查

**如果双写一致性不达标**：

1. **检查双写配置**
   ```yaml
   consume:
     write:
       mode: dual  # 确保是dual模式
       async: true  # 异步双写
       retry-times: 3  # 重试次数
   ```

2. **查看双写失败日志**
   ```bash
   # 查看应用日志
   tail -f logs/consume-service.log | grep "DualWrite"

   # 查看验证失败记录
   mysql -h127.0.0.1 -uroot -p ioedream -e "
   SELECT * FROM dual_write_difference_record
   WHERE resolved = 0
   ORDER BY discover_time DESC
   LIMIT 10;
   "
   ```

3. **手动修复数据差异**
   ```java
   // 调用修复接口
   @PostMapping("/admin/dual-write/fix")
   public ResponseDTO<Void> fixDifferences(@RequestParam Long recordId) {
       dualWriteValidationManager.fixDifference(recordId);
       return ResponseDTO.ok();
   }
   ```

---

## 📋 验证检查清单

### 验证前检查

- [ ] 数据库迁移脚本已执行成功
- [ ] 11个POSID表创建成功
- [ ] 数据迁移行数一致
- [ ] 消费服务启动成功
- [ ] 服务健康检查通过
- [ ] 测试数据准备完成
- [ ] Prometheus指标采集正常
- [ ] Grafana仪表盘显示正常

### 验证中检查

- [ ] JMeter测试计划执行成功
- [ ] TPS ≥ 1000
- [ ] 平均响应时间 ≤ 50ms
- [ ] P95响应时间 ≤ 100ms
- [ ] 成功率 ≥ 99%
- [ ] 缓存命中率 ≥ 90%
- [ ] 双写一致性 ≥ 99.9%
- [ ] 错误率 ≤ 1%

### 验证后检查

- [ ] 性能测试报告生成
- [ ] Grafana监控截图保存
- [ ] 双写验证日志导出
- [ ] 验证结果汇总报告
- [ ] 性能优化建议整理

---

## 📊 验证报告模板

### 验证结果汇总

```
========================================
消费服务性能验证报告
========================================
验证时间: 2025-12-23 10:00-11:00
验证环境: 生产环境
验证人员: ____________

1. TPS验证
   目标值: ≥ 1000
   实际值: _____
   状态: ✓ 通过 / ✗ 未通过

2. 响应时间验证
   目标值: ≤ 50ms
   实际值: _____ ms
   状态: ✓ 通过 / ✗ 未通过

3. 缓存命中率验证
   目标值: ≥ 90%
   实际值: _____ %
   状态: ✓ 通过 / ✗ 未通过

4. 双写一致性验证
   目标值: ≥ 99.9%
   实际值: _____ %
   状态: ✓ 通过 / ✗ 未通过

========================================
总体结论: ✓ 全部通过 / ⚠️ 部分通过 / ✗ 未通过
========================================
```

---

## 🚨 应急预案

### 如果验证失败

1. **回滚到旧表**
   ```bash
   # 执行回滚脚本
   mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\rollback-to-old-tables.sql

   # 修改配置
   consume.write.mode: old  # 改为只写旧表
   ```

2. **性能不达标**
   ```bash
   # 执行性能优化脚本
   bash D:\IOE-DREAM\scripts\performance-optimization.sh

   # 重启服务
   mvn spring-boot:run -Dspring-boot.run.profiles=docker
   ```

3. **数据不一致**
   ```bash
   # 暂停双写，只写旧表
   consume.write.mode: old

   # 数据修复
   mysql -h127.0.0.1 -uroot -p ioedream < D:\IOE-DREAM\scripts\fix-data-differences.sql
   ```

---

## 📞 技术支持

**文档位置**：
- `D:\IOE-DREAM\microservices\ioedream-consume-service\PERFORMANCE_VALIDATION_GUIDE.md` (本文档)
- `D:\IOE-DREAM\microservices\ioedream-consume-service\DATABASE_MIGRATION_GUIDE.md`
- `D:\IOE-DREAM\microservices\ioedream-consume-service\TASKS_COMPLETION_REPORT.md`

**执行脚本**：
- `D:\IOE-DREAM\microservices\ioedream-consume-service\scripts\performance-test.sh`
- `D:\IOE-DREAM\scripts\validate-dual-write.sql`

**监控工具**：
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000
- JMeter: 命令行或GUI模式

---

**文档版本**: v1.0
**最后更新**: 2025-12-23
**维护人**: IOE-DREAM架构团队
