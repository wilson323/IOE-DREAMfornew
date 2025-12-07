# Druid连接池Nacos配置指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待配置

---

## 📋 配置说明

### 配置位置
- **Nacos控制台**: `http://localhost:8848/nacos`
- **配置模板**: `microservices/microservices-common/src/main/resources/application-druid-template.yml`

---

## 🔧 配置步骤

### 1. 登录Nacos控制台

访问 `http://localhost:8848/nacos`，使用默认账号密码登录：
- 用户名: `nacos`
- 密码: `nacos`

### 2. 创建或更新配置

对于每个微服务，需要创建或更新对应的配置文件：

**配置命名规则**: `ioedream-{service-name}-dev.yaml`

**需要配置的服务**:
- `ioedream-common-service-dev.yaml`
- `ioedream-consume-service-dev.yaml`
- `ioedream-access-service-dev.yaml`
- `ioedream-attendance-service-dev.yaml`
- `ioedream-visitor-service-dev.yaml`
- `ioedream-video-service-dev.yaml`
- `ioedream-device-comm-service-dev.yaml`
- `ioedream-oa-service-dev.yaml`

### 3. 配置内容

在Nacos配置中心添加以下Druid配置：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # ==================== 核心连接池配置 ====================
      initial-size: 10          # 初始连接数（根据并发量调整）
      min-idle: 10              # 最小空闲连接数
      max-active: 50            # 最大活跃连接数（根据数据库性能调整）
      max-wait: 60000           # 获取连接最大等待时间（毫秒）
      
      # ==================== 连接有效性检测 ====================
      validation-query: SELECT 1
      test-while-idle: true     # 空闲时检测连接有效性
      test-on-borrow: false     # 借用时检测（性能开销大，不推荐）
      test-on-return: false     # 归还时检测（性能开销大，不推荐）
      
      # ==================== 连接回收配置 ====================
      time-between-eviction-runs-millis: 60000  # 连接回收间隔（60秒）
      min-evictable-idle-time-millis: 300000    # 连接最小空闲时间（5分钟）
      
      # ==================== 性能优化配置 ====================
      pool-prepared-statements: true             # 开启预编译语句池
      max-pool-prepared-statement-per-connection-size: 20  # 每个连接最大预编译语句数
      
      # ==================== 监控配置 ====================
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin
        reset-enable: false  # 禁用重置功能（生产环境）
      
      # ==================== 慢查询监控 ====================
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000      # 慢查询阈值（1秒）
          log-slow-sql: true          # 记录慢SQL
          merge-sql: true             # 合并相同SQL统计
        wall:
          enabled: true               # SQL防火墙
          config:
            multi-statement-allow: false  # 禁止多语句执行
```

---

## 📝 配置示例

### 示例1: ioedream-common-service-dev.yaml

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin
        reset-enable: false
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000
          log-slow-sql: true
          merge-sql: true
        wall:
          enabled: true
          config:
            multi-statement-allow: false
```

---

## 🔍 验证步骤

### 1. 重启服务

配置完成后，重启对应的微服务，使配置生效。

### 2. 访问Druid监控页面

访问各服务的Druid监控页面：
- Common Service: `http://localhost:8088/druid/index.html`
- Consume Service: `http://localhost:8094/druid/index.html`
- Access Service: `http://localhost:8090/druid/index.html`
- Attendance Service: `http://localhost:8091/druid/index.html`
- Visitor Service: `http://localhost:8095/druid/index.html`
- Video Service: `http://localhost:8092/druid/index.html`
- Device Comm Service: `http://localhost:8087/druid/index.html`
- OA Service: `http://localhost:8089/druid/index.html`

**登录信息**:
- 用户名: `admin`
- 密码: `admin`

### 3. 检查连接池状态

在Druid监控页面中，检查以下指标：
- **活跃连接数**: 应该小于 `max-active` 配置值
- **等待连接数**: 应该为0或接近0
- **连接池利用率**: 应该合理（建议在60%-80%之间）
- **慢查询数量**: 应该为0或接近0

---

## ⚠️ 注意事项

### 1. 配置生效时间

- 配置更新后，需要重启服务才能生效
- 建议在非高峰期进行配置更新

### 2. 连接池大小调整

根据实际业务负载调整连接池大小：
- **低并发场景**: `max-active: 20-30`
- **中并发场景**: `max-active: 50-80`
- **高并发场景**: `max-active: 100-200`

### 3. 监控页面安全

生产环境建议：
- 修改默认用户名和密码
- 限制监控页面访问IP
- 考虑禁用监控页面（`enabled: false`）

### 4. 慢查询阈值

根据业务需求调整慢查询阈值：
- **开发环境**: `slow-sql-millis: 1000` (1秒)
- **测试环境**: `slow-sql-millis: 500` (0.5秒)
- **生产环境**: `slow-sql-millis: 200` (0.2秒)

---

## 📊 性能优化建议

### 1. 连接池参数调优

```yaml
# 高并发场景配置
druid:
  initial-size: 20
  min-idle: 20
  max-active: 100
  max-wait: 30000  # 减少等待时间
```

### 2. 预编译语句池优化

```yaml
druid:
  pool-prepared-statements: true
  max-pool-prepared-statement-per-connection-size: 50  # 增加预编译语句数
```

### 3. 连接回收优化

```yaml
druid:
  time-between-eviction-runs-millis: 30000  # 缩短回收间隔
  min-evictable-idle-time-millis: 180000    # 缩短空闲时间
```

---

## ✅ 验证清单

- [ ] Nacos控制台可访问
- [ ] 所有服务的Druid配置已添加
- [ ] 服务已重启
- [ ] Druid监控页面可访问
- [ ] 连接池状态正常
- [ ] 慢查询监控正常
- [ ] 连接池利用率合理

---

**配置完成后，请更新配置状态**

