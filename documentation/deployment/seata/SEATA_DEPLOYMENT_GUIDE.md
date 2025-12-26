# Seata分布式事务部署指南

## 📋 概述

本文档介绍如何在IOE-DREAM项目中部署和配置Seata分布式事务服务器。

## 🎯 部署目标

- 部署Seata Server（TC - Transaction Coordinator）
- 配置Seata与Nacos集成
- 配置Seata与MySQL存储模式
- 验证Seata服务正常运行

## 📦 前置要求

- Java 17+
- MySQL 8.0+
- Nacos 2.0+
- Docker（可选，用于容器化部署）

## 🚀 部署方式

### 方式1: Docker部署（推荐）

#### 1.1 创建Docker Compose配置

创建 `docker-compose-seata.yml`:

```yaml
version: '3.8'

services:
  seata-server:
    image: seataio/seata-server:2.0.0
    container_name: seata-server
    ports:
      - "8091:8091"
      - "7091:7091"
    environment:
      - SEATA_PORT=8091
      - STORE_MODE=db
      - SEATA_IP=${SEATA_IP:-127.0.0.1}
    volumes:
      - ./seata/conf:/root/seata-server/resources
      - ./seata/logs:/root/logs
    networks:
      - ioedream-network
    depends_on:
      - mysql
      - nacos
    restart: unless-stopped

networks:
  ioedream-network:
    external: true
```

#### 1.2 启动Seata Server

```bash
docker-compose -f docker-compose-seata.yml up -d
```

### 方式2: 本地部署

#### 2.1 下载Seata Server

```bash
# 下载Seata 2.0.0
wget https://github.com/seata/seata/releases/download/v2.0.0/seata-server-2.0.0.zip
unzip seata-server-2.0.0.zip
cd seata
```

#### 2.2 配置Seata Server

编辑 `conf/application.yml`:

```yaml
server:
  port: 7091

spring:
  application:
    name: seata-server

seata:
  config:
    type: nacos
    nacos:
      server-addr: 127.0.0.1:8848
      namespace: dev
      group: SEATA_GROUP
      username: nacos
      password: nacos
      data-id: seataServer.properties
  registry:
    type: nacos
    nacos:
      application: seata-server
      server-addr: 127.0.0.1:8848
      namespace: dev
      group: SEATA_GROUP
      username: nacos
      password: nacos
      cluster: default
  store:
    mode: db
    db:
      datasource: druid
      db-type: mysql
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=UTC
      user: root
      password: ${MYSQL_PASSWORD}
      min-conn: 5
      max-conn: 100
      global-table: global_table
      branch-table: branch_table
      lock-table: lock_table
      distributed-lock-table: distributed_lock
      query-limit: 1000
      max-wait: 5000
```

#### 2.3 初始化数据库

执行 `script/server/db/mysql.sql` 创建Seata所需的表：

```sql
-- 全局事务表
CREATE TABLE IF NOT EXISTS `global_table`
(
    `xid`                       VARCHAR(128) NOT NULL,
    `transaction_id`            BIGINT,
    `status`                   TINYINT      NOT NULL,
    `application_id`            VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name`          VARCHAR(128),
    `timeout`                   INT,
    `begin_time`                BIGINT,
    `application_data`          VARCHAR(2000),
    `gmt_create`               DATETIME,
    `gmt_modified`              DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_status_gmt_modified` (`status`, `gmt_modified`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 分支事务表
CREATE TABLE IF NOT EXISTS `branch_table`
(
    `branch_id`         BIGINT       NOT NULL,
    `xid`               VARCHAR(128) NOT NULL,
    `transaction_id`     BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id`       VARCHAR(256),
    `branch_type`       VARCHAR(8),
    `status`            TINYINT,
    `client_id`         VARCHAR(64),
    `application_data`  VARCHAR(2000),
    `gmt_create`        DATETIME(6),
    `gmt_modified`       DATETIME(6),
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 锁表
CREATE TABLE IF NOT EXISTS `lock_table`
(
    `row_key`        VARCHAR(128) NOT NULL,
    `xid`            VARCHAR(128),
    `transaction_id` BIGINT,
    `branch_id`      BIGINT       NOT NULL,
    `resource_id`    VARCHAR(256),
    `table_name`     VARCHAR(32),
    `pk`             VARCHAR(36),
    `status`         TINYINT      NOT NULL DEFAULT '0',
    `gmt_create`     DATETIME,
    `gmt_modified`   DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_status` (`status`),
    KEY `idx_branch_id` (`branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

#### 2.4 启动Seata Server

```bash
# Linux/Mac
sh bin/seata-server.sh

# Windows
bin\seata-server.bat
```

## 🔧 Nacos配置

### 在Nacos中创建Seata配置

Data ID: `seataServer.properties`
Group: `SEATA_GROUP`

配置内容：

```properties
store.mode=db
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true&characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=UTC
store.db.user=root
store.db.password=${MYSQL_PASSWORD}
store.db.minConn=5
store.db.maxConn=100
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.lockTable=lock_table
store.db.distributedLockTable=distributed_lock
store.db.queryLimit=1000
store.db.maxWait=5000
```

## ✅ 验证部署

### 1. 检查Seata Server状态

访问Seata控制台: http://127.0.0.1:7091

### 2. 检查Nacos服务注册

在Nacos控制台查看服务列表，确认 `seata-server` 已注册。

### 3. 检查数据库表

确认Seata数据库表已创建：
- `global_table`
- `branch_table`
- `lock_table`

## 📝 微服务配置

各微服务已在 `bootstrap.yml` 中配置Seata客户端，确保：

1. Seata依赖已添加（已在pom.xml中配置）
2. 数据源代理已启用（`enable-auto-data-source-proxy: true`）
3. 事务组配置正确（`tx-service-group: ioedream-tx-group`）

## 🔍 故障排查

### 问题1: Seata Server无法启动

- 检查Java版本（需要Java 17+）
- 检查端口占用（7091, 8091）
- 检查Nacos连接配置

### 问题2: 微服务无法连接Seata

- 检查Nacos服务发现配置
- 检查事务组名称是否匹配
- 检查网络连通性

### 问题3: 事务不生效

- 检查数据源代理是否启用
- 检查`@GlobalTransactional`注解是否正确
- 检查数据库undo_log表是否创建

## 📚 参考文档

- [Seata官方文档](https://seata.io/zh-cn/docs/overview/what-is-seata.html)
- [Seata部署指南](https://seata.io/zh-cn/docs/ops/deploy-guide-beginner.html)
- [Seata与Spring Cloud集成](https://seata.io/zh-cn/docs/user/integration/springcloud.html)

