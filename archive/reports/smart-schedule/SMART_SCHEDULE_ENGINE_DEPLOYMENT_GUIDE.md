# 智能排班引擎 - 部署和验证指南

## 📋 部署前检查清单

### 1. 代码质量验证
- [x] 编译成功，0错误
- [ ] 单元测试全部通过
- [ ] 代码审查完成
- [ ] 技术文档齐全

### 2. 数据库准备
- [ ] 执行数据库迁移脚本
- [ ] 验证表结构更新
- [ ] 检查索引是否创建成功
- [ ] 备份现有数据

### 3. 配置文件准备
- [ ] Nacos配置更新
- [ ] application.yml配置检查
- [ ] 环境变量设置
- [ ] 日志配置确认

### 4. 依赖服务检查
- [ ] MySQL服务正常
- [ ] Nacos服务正常
- [ ] Redis服务正常（如使用缓存）
- [ ] 网关服务正常

---

## 🚀 部署步骤

### 步骤1: 数据库迁移

**执行脚本**: `smart_schedule_plan_entity_migration.sql`

```bash
# 1. 备份现有数据（可选但推荐）
mysqldump -u root -p ioe_dream t_smart_schedule_plan > backup_smart_schedule_plan_$(date +%Y%m%d).sql

# 2. 执行迁移脚本
mysql -u root -p ioe_dream < database-scripts/smart_schedule_plan_entity_migration.sql

# 3. 验证表结构
mysql -u root -p ioe_dream -e "DESCRIBE t_smart_schedule_plan;"

# 4. 验证索引创建
mysql -u root -p ioe_dream -e "SHOW INDEX FROM t_smart_schedule_plan;"
```

**预期结果**:
```
Field               | Type         | Null | Key | Default | Extra
---------------------|--------------|------|-----|---------|-------------------
...
unsatisfied_constraint_count | int(11)      | YES  |     | NULL    |
converged           | tinyint(1)   | YES  | MUL | 0       | 是否收敛
error_message       | varchar(500) | YES  |     | NULL    | 错误信息
```

### 步骤2: 编译和打包

```bash
# 进入microservices目录
cd microservices

# 清理旧的构建
mvn clean

# 编译所有模块
mvn compile -DskipTests

# 打包attendance-service
mvn package -pl ioedream-attendance-service -am -DskipTests

# 验证JAR包生成
ls -lh ioedream-attendance-service/target/ioedream-attendance-service-1.0.0.jar
```

**预期输出**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 01:32 min
```

### 步骤3: 部署服务

**方式1: 手动部署**
```bash
# 停止旧服务
./scripts/stop-service.sh ioedream-attendance-service

# 备份旧JAR
cp ioedream-attendance-service/target/ioedream-attendance-service-1.0.0.jar \
   ioedream-attendance-service/target/ioedream-attendance-service-1.0.0.jar.backup

# 启动新服务
./scripts/start-service.sh ioedream-attendance-service

# 查看日志
tail -f logs/ioedream-attendance-service.log
```

**方式2: Docker部署**
```bash
# 构建Docker镜像
docker build -t ioedream/attendance-service:1.0.0 \
  -f docker/Dockerfile.attendance .

# 停止旧容器
docker stop attendance-service
docker rm attendance-service

# 启动新容器
docker run -d \
  --name attendance-service \
  -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e NACOS_SERVER_ADDR=192.168.1.100:8848 \
  ioedream/attendance-service:1.0.0

# 查看日志
docker logs -f attendance-service
```

**方式3: Kubernetes部署**
```bash
# 应用配置
kubectl apply -f deployment/kubernetes/attendance-service-configmap.yaml

# 部署服务
kubectl apply -f deployment/kubernetes/attendance-service-deployment.yaml

# 查看状态
kubectl get pods -l app=attendance-service
kubectl logs -f deployment/attendance-service-xxxxx
```

### 步骤4: 健康检查

```bash
# 检查服务启动
curl http://localhost:8091/actuator/health

# 预期响应
{
  "status": "UP"
}

# 检查服务信息
curl http://localhost:8091/actuator/info

# 预期响应
{
  "app": "ioedream-attendance-service",
  "version": "1.0.0",
  "description": "智能排班服务"
}
```

---

## ✅ 功能验证

### 测试1: 创建排班计划

```bash
curl -X POST http://localhost:8091/api/v1/smart-schedule/plan \
  -H "Content-Type: application/json" \
  -d '{
    "planName": "测试排班计划",
    "startDate": "2025-02-01",
    "endDate": "2025-02-07",
    "periodDays": 7,
    "employeeIds": [1, 2, 3, 4, 5],
    "shiftIds": [10, 11],
    "optimizationGoal": 5,
    "minConsecutiveWorkDays": 1,
    "maxConsecutiveWorkDays": 7,
    "minRestDays": 2,
    "minDailyStaff": 5,
    "maxDailyStaff": 20,
    "fairnessWeight": 0.4,
    "costWeight": 0.3,
    "efficiencyWeight": 0.2,
    "satisfactionWeight": 0.1,
    "algorithmType": 1,
    "populationSize": 50,
    "maxIterations": 100,
    "crossoverRate": 0.8,
    "mutationRate": 0.1
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1001
}
```

### 测试2: 执行优化

```bash
curl -X POST http://localhost:8091/api/v1/smart-schedule/plan/1001/execute
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "bestChromosome": {...},
    "bestFitness": 0.92,
    "fairnessScore": 0.88,
    "costScore": 0.75,
    "efficiencyScore": 0.91,
    "satisfactionScore": 0.86,
    "iterations": 856,
    "converged": true,
    "executionDurationMs": 15230,
    "qualityLevel": 5,
    "qualityLevelDescription": "优秀",
    "executionSpeed": 56.2
  }
}
```

### 测试3: 查询排班结果

```bash
curl "http://localhost:8091/api/v1/smart-schedule/results?planId=1001&pageNum=1&pageSize=20"
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "resultId": 2001,
        "planId": 1001,
        "employeeId": 1,
        "scheduleDate": "2025-02-01",
        "shiftId": 10,
        "shiftName": "早班",
        "scheduleStatus": 1
      }
    ],
    "total": 35,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 2
  }
}
```

### 测试4: 规则引擎验证

```bash
# 在日志中查找规则引擎调用记录
grep "规则引擎" logs/ioedream-attendance-service.log | tail -20
```

**预期日志**:
```
[2025-01-30 14:23:45] INFO  [规则引擎] IsWorkdayFunction: date=2025-02-01, isWorkday=true
[2025-01-30 14:23:46] INFO  [规则引擎] IsWeekendFunction: date=2025-02-02, isWeekend=true
[2025-01-30 14:23:47] INFO  [规则引擎] DayOfWeekFunction: date=2025-02-01, dayOfWeek=5
```

---

## 🔍 监控指标

### 关键性能指标（KPI）

```
1. 优化执行时间
   - 目标: <30秒（50人×30天）
   - 告警阈值: >60秒

2. 算法收敛率
   - 目标: >90%
   - 告警阈值: <80%

3. 服务可用性
   - 目标: >99.9%
   - 告警阈值: <99%

4. API响应时间
   - 目标: <500ms（P95）
   - 告警阈值: >1000ms
```

### 日志监控

```bash
# 监控错误日志
tail -f logs/ioedream-attendance-service.log | grep ERROR

# 监控优化执行日志
tail -f logs/ioedream-attendance-service.log | grep "智能排班"

# 统计优化成功率
grep "优化执行成功" logs/ioedream-attendance-service.log | wc -l
grep "优化执行失败" logs/ioedream-attendance-service.log | wc -l
```

### 数据库监控

```sql
-- 查询最近的优化记录
SELECT
    plan_id,
    plan_name,
    execution_status,
    converged,
    fitness_score,
    execution_duration_ms
FROM t_smart_schedule_plan
ORDER BY create_time DESC
LIMIT 10;

-- 统计优化成功率
SELECT
    execution_status,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM t_smart_schedule_plan), 2) AS percentage
FROM t_smart_schedule_plan
GROUP BY execution_status;

-- 统计收敛率
SELECT
    converged,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM t_smart_schedule_plan WHERE execution_status = 2), 2) AS percentage
FROM t_smart_schedule_plan
WHERE execution_status = 2
GROUP BY converged;
```

---

## ⚠️ 故障排查

### 问题1: 服务启动失败

**症状**: 服务无法启动，日志报错

**排查步骤**:
```bash
# 1. 检查端口占用
netstat -ano | findstr :8091

# 2. 检查日志
tail -100 logs/ioedream-attendance-service.log

# 3. 检查配置文件
cat config/application.yml | grep -A 10 "spring:"

# 4. 检查数据库连接
mysql -h localhost -u root -p -e "SELECT 1 FROM DUAL;"
```

**常见原因**:
- 端口被占用 → 修改配置或停止占用进程
- 数据库连接失败 → 检查数据库服务状态和连接配置
- Nacos连接失败 → 检查Nacos服务状态和地址配置
- 内存不足 → 增加JVM堆内存配置

### 问题2: 优化执行失败

**症状**: API返回执行失败，error_message有值

**排查步骤**:
```sql
-- 查询失败记录
SELECT
    plan_id,
    plan_name,
    error_message,
    execution_duration_ms
FROM t_smart_schedule_plan
WHERE execution_status = 3
ORDER BY create_time DESC
LIMIT 10;
```

**常见原因**:
- 员工数据不完整 → 检查employee_ids JSON格式
- 班次数据不完整 → 检查shift_ids JSON格式
- 约束条件冲突 → 检查minDailyStaff/maxDailyStaff配置
- 算法参数不合理 → 调整populationSize、maxIterations等参数

**解决方案**:
```java
// 1. 验证输入数据
if (form.getEmployeeIds() == null || form.getEmployeeIds().isEmpty()) {
    throw new BusinessException("员工ID列表不能为空");
}

// 2. 验证约束条件合理性
if (form.getMinDailyStaff() > form.getMaxDailyStaff()) {
    throw new BusinessException("最少在岗人数不能大于最多在岗人数");
}

// 3. 验证日期范围
if (form.getEndDate().isBefore(form.getStartDate())) {
    throw new BusinessException("结束日期不能早于开始日期");
}
```

### 问题3: 规则引擎不工作

**症状**: 表达式求值返回错误或默认值

**排查步骤**:
```bash
# 检查规则引擎日志
grep "规则引擎" logs/ioedream-attendance-service.log | grep ERROR

# 检查函数注册
curl http://localhost:8091/actuator/beans | grep "Function"
```

**常见原因**:
- Aviator 5.x API使用错误 → 已修复，使用getValue(env)
- 日期参数类型错误 → 确保传入LocalDate或String类型
- 表达式语法错误 → 检查表达式格式

---

## 📊 性能优化建议

### 1. 数据库优化

```sql
-- 为常用查询添加索引
CREATE INDEX idx_plan_status_date
ON t_smart_schedule_result(plan_id, schedule_date);

CREATE INDEX idx_plan_employee
ON t_smart_schedule_result(plan_id, employee_id);

-- 分析慢查询
SELECT * FROM mysql.slow_log
WHERE sql_text LIKE '%smart_schedule%'
ORDER BY query_time DESC
LIMIT 10;
```

### 2. 缓存优化

```yaml
# application.yml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
      cache-null-values: false

# 缓存配置
smart-schedule:
  cache:
    result-cache-enabled: true
    plan-cache-enabled: true
```

### 3. 异步处理

```java
@Service
public class SmartScheduleServiceImpl {

    @Async("taskExecutor")
    public CompletableFuture<OptimizationResult> executeOptimizationAsync(Long planId) {
        OptimizationResult result = executeOptimization(planId);
        return CompletableFuture.completedFuture(result);
    }
}
```

### 4. 算法优化

```java
// 并行优化配置
@Configuration
public class OptimizerConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("optimizer-");
        executor.initialize();
        return executor;
    }
}
```

---

## 🎯 后续优化方向

### 短期优化（1个月内）

1. **性能优化**
   - [ ] 实现多线程并行优化
   - [ ] 优化初始解生成策略
   - [ ] 引入缓存机制

2. **功能增强**
   - [ ] 支持多班次复杂场景
   - [ ] 支持技能匹配约束
   - [ ] 支持员工偏好设置

3. **用户体验**
   - [ ] 实时优化进度推送
   - [ ] 可视化排班日历
   - [ ] 一键导入/导出

### 中期优化（3个月内）

1. **智能化升级**
   - [ ] 引入机器学习预测模型
   - [ ] 基于历史数据自动调参
   - [ ] 强化学习优化策略

2. **分布式计算**
   - [ ] 拆分优化任务到多节点
   - [ ] MapReduce并行计算
   - [ ] 消息队列异步处理

### 长期优化（6个月内）

1. **AI增强**
   - [ ] 深度学习优化算法
   - [ ] 神经网络适应度预测
   - [ ] 自动规则学习

2. **生态集成**
   - [ ] 与考勤系统深度集成
   - [ ] 与HR系统数据同步
   - [ ] 移动端排班查看

---

## 📚 相关文档

### 核心文档
- [SMART_SCHEDULE_ENGINE_COMPLETE_IMPLEMENTATION_REPORT.md](./SMART_SCHEDULE_ENGINE_COMPLETE_IMPLEMENTATION_REPORT.md)
- [SMART_SCHEDULE_ENGINE_FINAL_SUMMARY.md](./SMART_SCHEDULE_ENGINE_FINAL_SUMMARY.md)
- [SMART_SCHEDULE_ENGINE_COMPLETION_CHECKLIST.md](./SMART_SCHEDULE_ENGINE_COMPLETION_CHECKLIST.md)

### 部署文档
- [部署指南](./documentation/deployment/DEPLOYMENT_GUIDE.md)
- [Docker部署指南](./documentation/deployment/Docker部署指南.md)
- [Kubernetes部署指南](./documentation/deployment/Kubernetes部署指南.md)

### 监控文档
- [监控体系建设指南](./documentation/technical/监控体系建设指南.md)
- [日志标准化规范](./documentation/technical/LOGGING_PATTERN_COMPLETE_STANDARD.md)

---

**部署指南版本**: v1.0.0
**最后更新**: 2025-01-30
**维护团队**: IOE-DREAM Team
