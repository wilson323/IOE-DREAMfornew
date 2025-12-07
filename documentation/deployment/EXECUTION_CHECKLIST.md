# 执行检查清单

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待执行

---

## 📋 执行前准备

### 环境检查

- [ ] MySQL数据库已启动
- [ ] Redis服务已启动
- [ ] Nacos服务已启动
- [ ] 各微服务已启动
- [ ] 数据库已备份（重要！）

### 工具准备

- [ ] MySQL客户端工具（Navicat/DBeaver/命令行）
- [ ] Redis客户端工具（redis-cli）
- [ ] Nacos控制台访问权限
- [ ] JMeter（性能测试）

---

## ✅ 执行清单

### 1. 执行索引优化SQL

**执行方式**:

```powershell
# Windows PowerShell脚本（推荐）
cd scripts\database
$env:DB_HOST = "localhost"
$env:DB_PORT = "3306"
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_password"
$env:DB_NAME = "ioedream"
.\execute_index_optimization.ps1

# Linux/Mac Shell脚本
cd scripts/database
chmod +x execute_index_optimization.sh
export DB_HOST=localhost DB_USER=root DB_PASSWORD=xxx DB_NAME=ioedream
./execute_index_optimization.sh

# 方式3: 手动执行SQL文件
mysql -h localhost -u root -p ioedream < execute_index_optimization.sql
```

**执行文件**:

- [ ] `microservices/ioedream-access-service/src/main/resources/sql/access_index_optimization.sql`
- [ ] `microservices/ioedream-attendance-service/src/main/resources/sql/attendance_index_optimization.sql`
- [ ] `microservices/ioedream-visitor-service/src/main/resources/sql/visitor_index_optimization.sql`
- [ ] `microservices/ioedream-video-service/src/main/resources/sql/video_index_optimization.sql`
- [ ] `microservices/ioedream-consume-service/src/main/resources/sql/consume_index_optimization.sql`

**验证方法**:

```sql
-- 检查索引是否创建成功
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS
FROM 
    INFORMATION_SCHEMA.STATISTICS
WHERE 
    TABLE_SCHEMA = 'ioedream'
    AND INDEX_NAME LIKE 'idx_%'
GROUP BY 
    TABLE_NAME, INDEX_NAME
ORDER BY 
    TABLE_NAME, INDEX_NAME;
```

**预期结果**: 所有索引创建成功，无错误

---

### 2. 配置Druid连接池

**配置位置**: Nacos配置中心

**配置指南**: 详细配置步骤请参考 `documentation/deployment/DRUID_NACOS_CONFIG_TEMPLATE.md`

**配置步骤**:

1. [ ] 登录Nacos控制台 (`http://localhost:8848/nacos`)
2. [ ] 创建或更新配置 (Data ID: `ioedream-{service-name}-dev.yaml`)
3. [ ] 添加Druid配置（参考 `application-druid-template.yml` 或配置指南）

**需要配置的服务**:

- [ ] `ioedream-common-service`
- [ ] `ioedream-consume-service`
- [ ] `ioedream-access-service`
- [ ] `ioedream-attendance-service`
- [ ] `ioedream-visitor-service`
- [ ] `ioedream-video-service`
- [ ] `ioedream-device-comm-service`
- [ ] `ioedream-oa-service`

**验证方法**:

1. [ ] 重启服务
2. [ ] 访问Druid监控页面: `http://localhost:{port}/druid/index.html`
3. [ ] 检查连接池状态（活跃连接数、等待连接数）

**预期结果**: 连接池配置生效，监控页面可访问

---

### 3. 验证Redisson配置

**验证步骤**:

1. [ ] 检查Redis连接: `redis-cli ping` (预期: PONG)
2. [ ] 启动服务，查看日志: `Redisson客户端配置成功`
3. [ ] 使用测试接口验证分布式锁功能

**测试接口**（已创建）:

测试接口已创建在: `microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/controller/RedissonTestController.java`

**接口路径**:
- 测试分布式锁: `GET /api/v1/test/redisson`
- 检查连接状态: `GET /api/v1/test/redisson/status`

**使用示例**:
```bash
# 测试Redisson分布式锁
curl http://localhost:8088/api/v1/test/redisson

# 检查Redisson连接状态
curl http://localhost:8088/api/v1/test/redisson/status
```

**验证方法**:

- [ ] 调用测试接口，返回"Redisson工作正常"
- [ ] 检查缓存击穿防护功能（查看日志）

**预期结果**: Redisson客户端正常工作，分布式锁可用

---

### 4. 执行性能测试

**测试工具**: JMeter

**测试指南**: 详细测试步骤请参考 `documentation/deployment/PERFORMANCE_TEST_GUIDE.md`

**测试场景**:

1. [ ] **缓存性能测试**
   - 接口: 用户信息查询接口
   - 并发: 1000次请求
   - 预期: 缓存命中率≥90%，平均响应时间≤5ms

2. [ ] **数据库查询性能测试**
   - 接口: 分页查询接口
   - 并发: 100次请求
   - 预期: 平均响应时间≤150ms，慢查询数量=0

3. [ ] **连接池性能测试**
   - 并发: 1000个请求
   - 预期: 连接池利用率≥90%，连接等待时间≤100ms

4. [ ] **系统TPS测试**
   - 并发用户: 逐步增加到100
   - 预期: TPS≥2000，响应时间≤200ms（P95）

**验证方法**:

- [ ] 查看JMeter测试报告
- [ ] 查看Spring Boot Actuator指标
- [ ] 查看Druid监控页面

**预期结果**: 所有性能指标达到目标值

---

### 5. 补充单元测试

**测试覆盖率目标**:

- [ ] Service层: ≥80%
- [ ] Manager层: ≥75%
- [ ] DAO层: ≥70%
- [ ] Controller层: ≥60%

**已创建的测试**:

- [x] `PaymentServiceTest.java` - 支付服务测试
- [x] `VideoDeviceServiceImplTest.java` - 视频设备服务测试
- [x] `AccessPermissionApplyServiceImplTest.java` - 门禁权限申请服务测试

**需要补充的测试**:

- [ ] 消费服务其他方法测试
- [ ] 门禁服务其他方法测试
- [ ] 考勤服务测试
- [ ] 访客服务测试
- [ ] 视频服务其他方法测试

**验证方法**:

```bash
# 运行测试覆盖率检查
cd scripts/test
chmod +x check_test_coverage.sh
./check_test_coverage.sh
```

**预期结果**: 测试覆盖率达到目标值

---

### 6. 完善API文档

**完善方式**: 使用Swagger/OpenAPI注解

**已完善的接口**:

- [x] `VideoDeviceController` - 视频设备管理接口
- [x] `AccessPermissionApplyController` - 门禁权限申请接口

**需要完善的接口**:

- [ ] 消费服务接口
- [ ] 门禁服务其他接口
- [ ] 考勤服务接口
- [ ] 访客服务接口
- [ ] 视频服务其他接口

**验证方法**:

1. [ ] 启动服务
2. [ ] 访问Swagger UI: `http://localhost:{port}/swagger-ui.html`
3. [ ] 检查接口文档是否完整

**预期结果**: 所有接口都有完整的文档说明

---

### 7. 完善使用指南

**已创建的文档**:

- [x] `documentation/guide/DEVELOPMENT_GUIDE.md` - 开发指南

**需要创建的文档**:

- [ ] 部署指南（Docker/Kubernetes）
- [ ] 运维指南（监控/日志/故障排查）
- [ ] 用户使用手册

**验证方法**: 检查文档是否完整、准确

**预期结果**: 所有文档完整，易于理解

---

## 📊 执行进度跟踪

| 任务 | 状态 | 完成时间 | 备注 |
|------|------|---------|------|
| 执行索引优化SQL | ✅ 已准备 | 2025-01-30 | PowerShell脚本已创建: `scripts/database/execute_index_optimization.ps1` |
| 配置Druid连接池 | ✅ 已准备 | 2025-01-30 | 配置指南已创建: `documentation/deployment/DRUID_NACOS_CONFIG_TEMPLATE.md` |
| 验证Redisson配置 | ✅ 已准备 | 2025-01-30 | 测试接口已创建: `RedissonTestController.java` |
| 执行性能测试 | ✅ 已准备 | 2025-01-30 | 测试指南已创建: `documentation/deployment/PERFORMANCE_TEST_GUIDE.md` |
| 补充单元测试 | ✅ 进行中 | - | 已创建3个示例测试 |
| 完善API文档 | ✅ 进行中 | - | 已完善2个控制器 |
| 完善使用指南 | ✅ 进行中 | - | 已创建开发指南 |

---

## ⚠️ 注意事项

1. **执行顺序**: 建议按照清单顺序执行
2. **环境隔离**: 使用独立的测试环境，避免影响生产环境
3. **数据备份**: 执行SQL前必须备份数据库
4. **逐步验证**: 每完成一项任务，立即验证结果
5. **记录问题**: 遇到问题及时记录，便于后续排查

---

**执行完成后，请更新执行状态**

