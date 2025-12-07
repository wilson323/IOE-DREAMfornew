# Druid连接池配置指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待配置

---

## 📋 配置说明

### 配置位置

- **Nacos配置中心**: 各服务的 `application.yml` 或 `sa-base.yaml`
- **配置模板**: `microservices/microservices-common/src/main/resources/application-druid-template.yml`

---

## 🔧 配置步骤

### 1. 在Nacos配置中心添加Druid配置

#### 1.1 登录Nacos控制台

- 地址: `http://localhost:8848/nacos`
- 用户名: `nacos`
- 密码: `nacos`

#### 1.2 创建或更新配置

- **Data ID**: `ioedream-{service-name}-dev.yaml` (如: `ioedream-consume-service-dev.yaml`)
- **Group**: `IOE-DREAM`
- **配置格式**: YAML

#### 1.3 添加Druid配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ioedream}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
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

## 📊 配置参数说明

### 核心连接池配置

| 参数 | 说明 | 推荐值 | 说明 |
|------|------|--------|------|
| `initial-size` | 初始连接数 | 10 | 根据并发量调整 |
| `min-idle` | 最小空闲连接数 | 10 | 保持的最小连接数 |
| `max-active` | 最大活跃连接数 | 50 | 根据数据库性能调整 |
| `max-wait` | 获取连接最大等待时间 | 60000 | 单位：毫秒 |

### 连接有效性检测

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| `validation-query` | 验证查询SQL | SELECT 1 |
| `test-while-idle` | 空闲时检测 | true |
| `test-on-borrow` | 借用时检测 | false（性能开销大） |
| `test-on-return` | 归还时检测 | false（性能开销大） |

### 慢查询监控

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| `slow-sql-millis` | 慢查询阈值 | 1000（1秒） |
| `log-slow-sql` | 记录慢SQL | true |
| `merge-sql` | 合并相同SQL统计 | true |

---

## 🔍 配置验证

### 1. 检查配置是否生效

启动服务后，访问Druid监控页面：

- **URL**: `http://localhost:{port}/druid/index.html`
- **用户名**: `admin`
- **密码**: `admin`

### 2. 检查连接池状态

在Druid监控页面中查看：

- **活跃连接数**: 应小于 `max-active`
- **等待连接数**: 应接近0
- **连接池利用率**: 应小于90%

### 3. 检查慢查询

在Druid监控页面中查看：

- **慢SQL统计**: 查看慢SQL列表
- **SQL执行时间**: 分析慢SQL原因

---

## ⚠️ 注意事项

1. **生产环境**: 必须修改 `login-username` 和 `login-password`
2. **连接数调整**: 根据实际负载调整 `max-active`
3. **慢查询阈值**: 根据业务需求调整 `slow-sql-millis`
4. **监控页面**: 生产环境建议限制访问IP

---

## 📈 性能调优建议

### 高并发场景

```yaml
initial-size: 20
min-idle: 20
max-active: 100
```

### 低并发场景

```yaml
initial-size: 5
min-idle: 5
max-active: 20
```

### 数据库性能较差

```yaml
max-active: 30
max-wait: 120000  # 增加等待时间
```

---

## ✅ 配置验证

### 配置完成后验证

配置完成后，请验证连接池是否正常工作。
