# 故障排查指南

**版本**: v1.0.0  
**更新日期**: 2025-01-30  
**适用范围**: IOE-DREAM智慧园区一卡通管理平台

---

## 📋 目录

- [常见问题排查](#常见问题排查)
- [日志分析指南](#日志分析指南)
- [性能问题排查](#性能问题排查)
- [错误码说明](#错误码说明)
- [数据库问题排查](#数据库问题排查)
- [缓存问题排查](#缓存问题排查)
- [服务间调用问题排查](#服务间调用问题排查)

---

## 🔍 常见问题排查

### 1. 服务启动失败

#### 问题现象
- 服务无法启动
- 启动后立即退出
- 端口被占用

#### 排查步骤

1. **检查端口占用**
```bash
# Windows
netstat -ano | findstr :8080

# Linux
lsof -i :8080
```

2. **检查配置文件**
- 检查 `application.yml` 配置是否正确
- 检查 Nacos 配置中心连接是否正常
- 检查数据库连接配置是否正确

3. **检查依赖服务**
- 检查 Nacos 服务是否启动
- 检查 Redis 服务是否启动
- 检查 MySQL 服务是否启动

4. **查看启动日志**
```bash
# 查看服务启动日志
tail -f logs/application.log

# 查看错误日志
grep ERROR logs/application.log
```

#### 解决方案

**端口被占用**:
```bash
# Windows - 杀死占用端口的进程
taskkill /PID <进程ID> /F

# Linux - 杀死占用端口的进程
kill -9 <进程ID>
```

**配置错误**:
- 检查 `application.yml` 中的配置项
- 检查 Nacos 配置中心中的配置
- 检查环境变量是否正确设置

---

### 2. 数据库连接失败

#### 问题现象
- 服务启动后无法连接数据库
- 出现 "Connection refused" 错误
- 出现 "Access denied" 错误

#### 排查步骤

1. **检查数据库服务**
```bash
# 检查MySQL服务是否启动
systemctl status mysql

# 检查数据库端口是否开放
telnet <数据库IP> 3306
```

2. **检查连接配置**
- 检查数据库地址、端口、用户名、密码是否正确
- 检查数据库名称是否正确
- 检查连接池配置是否合理

3. **检查数据库权限**
```sql
-- 检查用户权限
SHOW GRANTS FOR 'username'@'host';

-- 检查数据库是否存在
SHOW DATABASES;
```

#### 解决方案

**连接配置错误**:
```yaml
# 检查 application.yml 中的数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: your_password
```

**权限问题**:
```sql
-- 授予用户权限
GRANT ALL PRIVILEGES ON ioedream.* TO 'username'@'%';
FLUSH PRIVILEGES;
```

---

### 3. Redis连接失败

#### 问题现象
- 缓存功能无法使用
- 出现 "Connection refused" 错误
- 出现 "NOAUTH" 错误

#### 排查步骤

1. **检查Redis服务**
```bash
# 检查Redis服务是否启动
systemctl status redis

# 检查Redis端口是否开放
telnet <RedisIP> 6379
```

2. **检查连接配置**
- 检查Redis地址、端口是否正确
- 检查Redis密码是否正确
- 检查Redis数据库编号是否正确

3. **测试Redis连接**
```bash
# 使用redis-cli测试连接
redis-cli -h <RedisIP> -p 6379 -a <password> ping
```

#### 解决方案

**连接配置错误**:
```yaml
# 检查 application.yml 中的Redis配置
spring:
  redis:
    host: 127.0.0.1
    port: 6379
    password: your_password
    database: 0
```

**密码问题**:
```bash
# 检查Redis密码
redis-cli -h <RedisIP> -p 6379
AUTH your_password
```

---

## 📊 日志分析指南

### 1. 日志级别说明

| 级别 | 说明 | 使用场景 |
|------|------|---------|
| ERROR | 错误日志 | 系统错误、异常捕获 |
| WARN | 警告日志 | 警告信息、潜在问题 |
| INFO | 信息日志 | 业务关键节点 |
| DEBUG | 调试日志 | 调试信息 |
| TRACE | 追踪日志 | 详细追踪 |

### 2. 日志格式

**标准日志格式**:
```
[时间] [级别] [线程] [类名] - [消息] [异常堆栈]
```

**示例**:
```
2025-01-30 12:34:56.789 [INFO] [http-nio-8080-exec-1] [ConsumeServiceImpl] - [消费服务] 开始执行消费交易，userId=1001, deviceId=3001, areaId=AREA001
```

### 3. 日志查询技巧

#### 查询错误日志
```bash
# 查询ERROR级别日志
grep ERROR logs/application.log

# 查询特定服务的错误日志
grep ERROR logs/application.log | grep "ConsumeService"
```

#### 查询业务日志
```bash
# 查询特定业务操作的日志
grep "消费交易" logs/application.log

# 查询特定用户的日志
grep "userId=1001" logs/application.log
```

#### 查询时间范围日志
```bash
# 查询特定时间段的日志
sed -n '/2025-01-30 12:00:00/,/2025-01-30 13:00:00/p' logs/application.log
```

### 4. 日志分析工具

**推荐工具**:
- **ELK Stack** (Elasticsearch + Logstash + Kibana) - 企业级日志分析
- **Grafana Loki** - 轻量级日志聚合
- **Splunk** - 商业日志分析平台

---

## ⚡ 性能问题排查

### 1. 接口响应慢

#### 问题现象
- 接口响应时间超过1秒
- 用户反馈系统卡顿
- 数据库查询慢

#### 排查步骤

1. **检查数据库查询**
```sql
-- 查看慢查询日志
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 查看正在执行的查询
SHOW PROCESSLIST;
```

2. **检查缓存命中率**
```bash
# 查看Redis缓存统计
redis-cli INFO stats

# 查看缓存命中率
# 通过监控系统查看缓存指标
```

3. **检查JVM性能**
```bash
# 查看JVM内存使用
jmap -heap <进程ID>

# 查看GC情况
jstat -gc <进程ID> 1000
```

#### 解决方案

**数据库优化**:
- 添加合适的索引
- 优化SQL查询
- 使用连接池
- 启用查询缓存

**缓存优化**:
- 提高缓存命中率
- 优化缓存过期时间
- 使用多级缓存

**JVM优化**:
- 调整堆内存大小
- 选择合适的GC算法
- 优化GC参数

---

### 2. 内存泄漏

#### 问题现象
- 内存使用持续增长
- 频繁Full GC
- 服务响应变慢

#### 排查步骤

1. **生成堆转储**
```bash
# 生成堆转储文件
jmap -dump:format=b,file=heap.dump <进程ID>
```

2. **分析堆转储**
- 使用 **Eclipse MAT** 分析堆转储文件
- 查找内存泄漏对象
- 分析对象引用关系

3. **检查线程**
```bash
# 查看线程堆栈
jstack <进程ID> > thread.dump
```

#### 解决方案

**常见内存泄漏原因**:
- 未关闭的资源（数据库连接、文件流等）
- 集合对象未清理
- 监听器未移除
- 缓存未设置过期时间

**修复方法**:
- 使用 try-with-resources 自动关闭资源
- 定期清理集合对象
- 移除不需要的监听器
- 设置合理的缓存过期时间

---

## 🚨 错误码说明

### 1. 通用错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 200 | 成功 | - |
| 400 | 参数错误 | 检查请求参数 |
| 401 | 未授权 | 检查Token是否有效 |
| 403 | 禁止访问 | 检查用户权限 |
| 404 | 资源不存在 | 检查资源ID是否正确 |
| 500 | 服务器错误 | 查看服务器日志 |

### 2. 业务错误码

#### 消费模块错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| CONSUME_ACCOUNT_NOT_FOUND | 账户不存在 | 检查账户ID是否正确 |
| CONSUME_BALANCE_INSUFFICIENT | 余额不足 | 提醒用户充值 |
| CONSUME_DEVICE_OFFLINE | 设备离线 | 检查设备连接状态 |
| CONSUME_TRANSACTION_FAILED | 交易失败 | 查看交易日志 |

#### 考勤模块错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| ATTENDANCE_LOCATION_INVALID | 位置无效 | 检查GPS定位是否准确 |
| ATTENDANCE_TIME_INVALID | 时间无效 | 检查打卡时间是否在允许范围内 |
| ATTENDANCE_ALREADY_CLOCKED | 已打卡 | 检查是否重复打卡 |

#### 门禁模块错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| ACCESS_PERMISSION_DENIED | 权限不足 | 检查用户是否有门禁权限 |
| ACCESS_DEVICE_OFFLINE | 设备离线 | 检查设备连接状态 |
| ACCESS_VERIFY_FAILED | 验证失败 | 检查识别信息是否正确 |

---

## 🗄️ 数据库问题排查

### 1. 连接池问题

#### 问题现象
- 数据库连接数不足
- 连接超时
- 连接泄漏

#### 排查步骤

1. **检查连接池配置**
```yaml
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
```

2. **查看连接池监控**
- 访问 Druid 监控页面：`http://localhost:8080/druid/index.html`
- 查看活跃连接数
- 查看等待连接数

3. **检查连接泄漏**
```sql
-- 查看当前连接
SHOW PROCESSLIST;

-- 查看长时间运行的查询
SELECT * FROM information_schema.processlist WHERE TIME > 60;
```

#### 解决方案

**连接池配置优化**:
```yaml
spring:
  datasource:
    druid:
      # 增加最大连接数
      max-active: 50
      # 增加等待时间
      max-wait: 120000
      # 启用连接泄漏检测
      remove-abandoned: true
      remove-abandoned-timeout: 300
```

---

### 2. 慢查询问题

#### 问题现象
- 查询响应慢
- 数据库CPU使用率高
- 用户反馈系统卡顿

#### 排查步骤

1. **启用慢查询日志**
```sql
-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- 查看慢查询日志位置
SHOW VARIABLES LIKE 'slow_query_log_file';
```

2. **分析慢查询**
```sql
-- 查看慢查询统计
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;
```

3. **使用EXPLAIN分析**
```sql
-- 分析查询计划
EXPLAIN SELECT * FROM t_consume_record WHERE user_id = 1001;
```

#### 解决方案

**索引优化**:
```sql
-- 添加索引
CREATE INDEX idx_user_id ON t_consume_record(user_id);

-- 添加复合索引
CREATE INDEX idx_user_area_time ON t_consume_record(user_id, area_id, create_time);
```

**SQL优化**:
- 避免全表扫描
- 使用索引覆盖
- 优化JOIN查询
- 使用分页查询

---

## 💾 缓存问题排查

### 1. 缓存命中率低

#### 问题现象
- 缓存命中率低于80%
- 数据库压力大
- 响应时间慢

#### 排查步骤

1. **查看缓存统计**
```bash
# 查看Redis统计信息
redis-cli INFO stats

# 查看缓存命中率
# 通过监控系统查看缓存指标
```

2. **分析缓存使用**
- 检查缓存key设计是否合理
- 检查缓存过期时间是否合理
- 检查缓存预热是否执行

#### 解决方案

**缓存优化**:
- 优化缓存key设计
- 设置合理的过期时间
- 实现缓存预热
- 使用多级缓存

---

### 2. 缓存击穿

#### 问题现象
- 热点数据缓存失效
- 大量请求直接访问数据库
- 数据库压力突然增大

#### 排查步骤

1. **检查缓存过期时间**
- 热点数据是否设置了较短的过期时间
- 是否同时过期导致缓存击穿

2. **检查缓存更新策略**
- 是否使用缓存穿透防护
- 是否使用分布式锁

#### 解决方案

**缓存击穿防护**:
- 使用分布式锁（Redisson）
- 设置合理的过期时间
- 实现缓存预热
- 使用多级缓存

---

## 🔗 服务间调用问题排查

### 1. 服务调用失败

#### 问题现象
- 服务间调用超时
- 服务调用返回错误
- 服务不可用

#### 排查步骤

1. **检查服务注册**
```bash
# 检查服务是否注册到Nacos
# 访问Nacos控制台：http://localhost:8848/nacos
```

2. **检查服务健康状态**
```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health
```

3. **检查网络连接**
```bash
# 测试服务间网络连接
telnet <服务IP> <服务端口>
```

#### 解决方案

**服务调用优化**:
- 配置合理的超时时间
- 实现服务降级
- 实现服务熔断
- 使用负载均衡

---

### 2. 网关路由问题

#### 问题现象
- 请求无法路由到目标服务
- 返回404错误
- 返回503错误

#### 排查步骤

1. **检查网关配置**
- 检查路由规则是否正确
- 检查服务名称是否正确
- 检查路径匹配是否正确

2. **检查服务发现**
- 检查服务是否注册到Nacos
- 检查服务名称是否匹配

#### 解决方案

**网关配置优化**:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: consume-service
          uri: lb://ioedream-consume-service
          predicates:
            - Path=/api/v1/consume/**
```

---

## 📞 技术支持

### 联系方式

- **技术支持邮箱**: support@ioe-dream.com
- **技术支持电话**: 400-XXX-XXXX
- **在线文档**: https://docs.ioe-dream.com

### 问题反馈

如遇到无法解决的问题，请提供以下信息：
1. 问题描述
2. 错误日志
3. 复现步骤
4. 环境信息（操作系统、JDK版本、服务版本等）

---

**维护人**: IOE-DREAM Team  
**最后更新**: 2025-01-30

