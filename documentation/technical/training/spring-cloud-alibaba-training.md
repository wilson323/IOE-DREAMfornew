# Spring Cloud Alibaba 深度培训教材

## 📚 培训大纲

**培训时间**: Phase 1 Week 7-8 Day 1-2
**培训对象**: 开发团队全体成员
**培训目标**: 深入掌握Spring Cloud Alibaba核心技术，为微服务开发奠定基础

---

## Day 1: Spring Cloud基础与Nacos深度实战

### 🎯 学习目标
- 理解微服务架构设计原理
- 掌握Spring Boot 3.x 深度特性
- 熟练使用Nacos服务注册发现
- 实践配置中心动态配置

### 📖 理论知识

#### 1.1 微服务架构概述

##### 微服务定义
微服务是一种架构风格，将单一应用程序开发为一组小型服务，每个服务都运行在自己的进程中，服务之间通过轻量级的通信机制进行通信。

##### 微服务优势
```
✅ 技术异构性 - 每个服务可以选择最适合的技术栈
✅ 独立部署 - 服务可以独立部署和扩展
✅ 故障隔离 - 单个服务故障不影响其他服务
✅ 团队自治 - 小团队可以独立开发和维护
✅ 快速迭代 - 可以频繁部署和更新
```

##### 微服务挑战
```
❌ 分布式复杂性 - 需要处理网络延迟、部分失败
❌ 服务发现 - 需要动态发现和管理服务实例
❌ 配置管理 - 需要统一管理分散的配置
❌ 数据一致性 - 分布式事务处理复杂
❌ 监控运维 - 需要统一的监控和日志
```

#### 1.2 IOE-DREAM微服务架构

##### 整体架构图
```
┌─────────────────────────────────────────────────────────────────┐
│                    IOE-DREAM 微服务架构                      │
├─────────────────────────────────────────────────────────────────┤
│  前端应用层                                                │
│  ├─ Web应用 (Vue3)                                         │
│  ├─ 移动应用 (uni-app)                                   │
│  └─ 桌面应用 (Electron)                                   │
├─────────────────────────────────────────────────────────────────┤
│  API网关层                                                │
│  └─ Spring Cloud Gateway (认证、路由、限流、监控)                │
├─────────────────────────────────────────────────────────────────┤
│  微服务层                                                │
│  ├─ 身份权限服务 (identity-service)                            │
│  ├─ 设备管理服务 (device-service)                              │
│  ├─ 访问控制服务 (access-service)                              │
│  ├─ 消费管理服务 (consume-service)                             │
│  ├─ 考勤管理服务 (attendance-service)                          │
│  ├─ 视频监控服务 (video-service)                               │
│  └─ 通知服务 (notification-service)                         │
├─────────────────────────────────────────────────────────────────┤
│  基础设施层                                              │
│  ├─ Nacos (服务注册发现、配置中心)                          │
│  ├─ Redis (缓存、会话存储)                                  │
│  ├─ MySQL (主从复制、分库分表)                            │
│  └─ Kubernetes (容器编排)                                │
└─────────────────────────────────────────────────────────────────┘�
```

##### 服务职责划分
```
身份权限服务 (identity-service):
├── 用户管理 - 用户注册、认证、授权
├── 角色管理 - 角色定义、权限分配
├── 区域权限 - 基于区域的细粒度权限
└── 组织管理 - 部门、岗位管理

设备管理服务 (device-service):
├── 设备注册 - 设备信息录入、认证
├── 设备监控 - 设备状态、连接状态
├── 设备配置 - 设备参数配置、固件升级
└── 设备管理 - 设备分组、批量操作

访问控制服务 (access-service):
├── 访问记录 - 出入记录、访问历史
├── 区域管理 - 区域定义、区域权限
├── 门禁控制 - 门禁规则、实时控制
└── 访客管理 - 访客预约、临时授权

消费管理服务 (consume-service):
├── 消费记录 - 消费明细、账单管理
├── 充值管理 - 在线充值、退款处理
├── 限额控制 - 消费限额、风控规则
└── 统计报表 - 消费统计、数据分析

考勤管理服务 (attendance-service):
├── 考勤记录 - 打卡记录、考勤统计
├── 排班管理 - 排班规则、班次管理
├── 请假管理 - 请假流程、审批流程
└── 考勤报表 - 考勤统计、异常分析

视频监控服务 (video-service):
├── 视频流 - 实时视频流、录像存储
├── 设备监控 - 摄像头状态、网络状态
├── 智能分析 - 行为识别、异常检测
└── 回放管理 - 视频回放、事件检索

通知服务 (notification-service):
├─ 邮件通知 - 邮件发送、模板管理
├─ 短信通知 - 短信发送、验证码
├─ 推送通知 - App推送、离线消息
└─ 系统通知 - 系统公告、状态变更
```

### 💻 实战演练

#### 1.3 Spring Boot 3.x 深度理解

##### 自动配置原理
Spring Boot通过`@EnableAutoConfiguration`注解启用自动配置，通过`spring.factories`文件配置的`AutoConfiguration`类，实现组件的自动装配。

##### 自动配置流程
```
1. @EnableAutoConfiguration 启用自动配置
2. 扫 AutoConfigurationImportSelector 加载配置类
3. 条件注解评估 (@Conditional) 过滤配置类
4. 配置类按优先级排序
5. 通过@Bean方法创建Bean对象
6. 注入到Spring容器
```

##### 关键配置类
```java
// DataSourceAutoConfiguration - 数据源自动配置
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        // 创建数据源配置
    }
}

// WebMvcAutoConfiguration - Web MVC自动配置
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class })
public class WebMvcAutoConfiguration {

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        // 创建请求映射处理器
    }
}
```

##### 启动流程分析
```java
// SpringApplication启动流程
public class SpringApplication {
    public ConfigurableApplicationContext run(String... args) {
        // 1. 创建应用上下文
        ConfigurableApplicationContext context = createApplicationContext();

        // 2. 准备环境
        prepareEnvironment(context, args);

        // 3. 刷新上下文
        refreshContext(context);

        // 4. 启动应用
        afterRefresh(context, args);

        return context;
    }

    protected void refresh(ConfigurableApplicationContext context) {
        // 刷新应用上下文，触发自动配置
        super.refresh(context);
    }
}
```

### 🔧 实战演练: 搭建Nacos高可用集群

#### 1.4 Nacos集群部署配置

##### 1.4.1 Nacos架构设计

##### 集群架构图
```
                    ┌─────────────────────────┐
                    │     Client (浏览器/应用)     │
                    └─────────────────────────┘�
                                      │
                    ┌─────────────────────────┐
                    │     Load Balancer          │
                    └─────────────────────────┘�
                                      │
        ┌──────────────┬──────────────┬──────────────┐
        │   Nacos-1   │   Nacos-2   │   Nacos-3   │
        │  (Master)   │  (Master)   │  (Follower) │
        └──────────────┴──────────────┴──────────────┘�
                    ┌─────────────────────────┐
                    │    MySQL Cluster         │
                    │  (主从复制)              │
                    └─────────────────────────┘�
```

##### 1.4.2 Docker Compose 配置

##### docker-compose.yml
```yaml
version: '3.8'

services:
  # Nacos节点1
  nacos-1:
    image: nacos/nacos-server:v2.3.0
    container_name: nacos-1
    environment:
      # 集群模式
      - MODE=cluster
      # 集群节点列表
      - NACOS_SERVERS="nacos-1:8848 nacos-2:8848 nacos-3:8848"
      # 数据库配置
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql-cluster
      - MYSQL_SERVICE_DB_NAME=nacos_config
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=nacos
      - MYSQL_SERVICE_PASSWORD=nacos123
      # 认证配置
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
      - NACOS_AUTH_ENABLE=false
      # JVM参数
      - JVM_XMS=512m
      - JVM_XMX=1024m
      - JVM_XMN=256m
    ports:
      - "8848:8848"   # HTTP端口
      - "9848:9848"   # RPC端口
    volumes:
      - ./nacos/logs/nacos-1:/home/nacos/logs
      - ./nacos/data/nacos-1:/home/nacos/data
    networks:
      - nacos-network
    depends_on:
      - mysql-cluster
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/v1/ns/operator/servers"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Nacos节点2
  nacos-2:
    image: nacos/nacos-server:v2.3.0
    container_name: nacos-2
    environment:
      - MODE=cluster
      - NACOS_SERVERS="nacos-1:8848 nacos-2:8848 nacos-3:8848"
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql-cluster
      - MYSQL_SERVICE_DB_NAME=nacos_config
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=nacos
      - MYSQL_SERVICE_PASSWORD=nacos123
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
      - NACOS_AUTH_ENABLE=false
      - JVM_XMS=512m
      - JVM_XMX=1024m
      - JVM_XMN=256m
    ports:
      - "8849:8848"
      - "9849:9848"
    volumes:
      - ./nacos/logs/nacos-2:/home/nacos/logs
      - ./nacos/data/nacos-2:/home/nacos/data
    networks:
      - nacos-network
    depends_on:
      - mysql-cluster
    restart: unless-stopped

  # Nacos节点3
  nacos-3:
    image: nacos/nacos-server:v2.2.3
    container_name: nacos-3
    environment:
      - MODE=cluster
      - NACOS_SERVERS="nacos-1:8848 nacos-2:8848 nacos-3:8848"
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql-cluster
      - MYSQL_SERVICE_DB_NAME=nacos_config
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=nacos
      - MYSQL_SERVICE_PASSWORD=nacos123
      - NACOS_AUTH_TOKEN=SecretKey012345678901234567890123456789012345678901234567890123456789
      - NACOS_AUTH_ENABLE=false
      - JVM_XMS=512m
      - JVM_XMX=1024m
      - JVM_XMN=256m
    ports:
      - "8850:8848"
      - "9850:9848"
    volumes:
      - ./nacos/logs/nacos-3:/home/nacos/logs
      - ./nacos/data/nacos-3:/home/nacos/data
    networks:
      - nacos-network
    depends_on:
      - mysql-cluster
    restart: unless-stopped

  # MySQL集群
  mysql-cluster:
    image: mysql:8.0.35
    container_name: mysql-cluster
    command:
      --server-id=1
      --log-bin=mysql-bin
      --binlog-format=ROW
      --expire-logs-days=7
      --relay-log=relay-bin
      --relay-log-space-recovery=1
      --sync-binlog=0
    environment:
      - MYSQL_ROOT_PASSWORD=root123
      - MYSQL_DATABASE=nacos_config
      - MYSQL_USER=nacos
      - MYSQL_PASSWORD=nacos123
      - MYSQL_CHARACTER_SET_SERVER=utf8mb4
    ports:
      - "3306:3306"
    volumes:
      - ./mysql/conf:/etc/mysql/conf.d
      - ./mysql/data:/var/lib/mysql
      - ./mysql/init:/docker-entrypoint-initdb.d
    networks:
      -nacos-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s

networks:
  nacos-network:
    driver: bridge
```

##### 1.4.3 MySQL初始化脚本

##### init.sql
```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS nacos_config CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户和授权
CREATE USER IF NOT EXISTS 'nacos'@' IDENTIFIED BY 'nacos123';
GRANT ALL PRIVILEGES ON nacos_config.* TO 'nacos'@'%';
FLUSH PRIVILEGES;

-- 切换到nacos_config数据库
USE nacos_config;

-- Nacos需要的表结构
CREATE TABLE IF NOT EXISTS config_info (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  data_id varchar(255) NOT NULL COMMENT 'data_id',
  app_name varchar(128) DEFAULT NULL COMMENT 'app_name',
  content longtext NOT NULL COMMENT 'content',
  md5 varchar(32) DEFAULT NULL COMMENT 'md5',
  gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'gmt_create',
  gmt_modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'gmt_modified',
  src_user text COMMENT 'src_user',
  src_ip varchar(50) COMMENT 'src_ip'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS config_info_tag (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  data_id bigint NOT NULL COMMENT 'data_id',
  tag_name varchar(128) NOT NULL COMMENT 'tag_name',
  tag_type varchar(64) DEFAULT NULL COMMENT 'tag_type'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS config_tags_relation (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  data_id bigint NOT NULL COMMENT 'data_id',
  tag_name varchar(128) NOT NULL COMMENT 'tag_name',
  tag_type varchar(64) DEFAULT NULL COMMENT 'tag_type'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS history_config_info (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  nid bigint NOT NULL COMMENT 'nid, 租位更新时间戳id',
  data_id varchar(255) NOT NULL COMMENT 'data_id',
  app_name varchar(128) DEFAULT NULL COMMENT 'app_name',
  content longtext NOT NULL COMMENT 'content',
  md5 varchar(32) DEFAULT NULL COMMENT 'md5',
  gmt_create datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'gmt_create',
  gmt_modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'gmt_modified',
  src_user text COMMENT 'src_user',
  src_ip varchar(50) COMMENT 'src_ip',
  op_type char(10) DEFAULT NULL COMMENT 'op_type',
  brief text COMMENT 'brief'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenant_info (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  kp varchar(128) NOT NULL COMMENT 'tenant_id',
  tenant_name varchar(128) DEFAULT NULL COMMENT 'tenant_name',
  tenant_desc varchar(256) DEFAULT NULL COMMENT 'tenant_desc',
  create_source varchar(32) DEFAULT NULL COMMENT 'create_source',
  gmt_create bigint NOT NULL DEFAULT 0 COMMENT 'gmt_create',
  gmt_modified bigint NOT NULL DEFAULT 0 COMMENT 'gmt_modified',
  encrypted_data_key text NOT NULL COMMENT 'encrypted_data_key'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenant_capacity (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  tenant_id varchar(128) NOT NULL COMMENT 'tenant_id',
  resource varchar(128) NOT NULL COMMENT 'resource',
  usage int DEFAULT 0 COMMENT 'usage',
  max_size int DEFAULT 0 COMMENT 'max_size',
  max_aggr_size int DEFAULT 0 COMMENT 'max_aggr_size',
  gmt_create bigint NOT NULL DEFAULT 0 COMMENT 'gmt_create',
  gmt_modified bigint NOT NULL DEFAULT 0 COMMENT 'gmt_modified'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenant_usage (
  id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
  tenant_id varchar(128) NOT NULL COMMENT 'tenant_id',
  resource varchar(128) NOT NULL COMMENT 'resource',
  usage int DEFAULT 0 COMMENT 'usage'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user
(
    username varchar(50) NOT NULL PRIMARY KEY,
    password varchar(500) NOT NULL,
    enabled tinyint(4) NOT NULL,
    groups varchar(100) DEFAULT ''
);
```

#### 1.4.4 启动Nacos集群

##### 启动脚本 (start-nacos.sh)
```bash
#!/bin/bash

# IOE-DREAM Nacos集群启动脚本

echo "🚀 启动IOE-DREAM Nacos集群..."

# 检查Docker环境
if ! command -v docker &> /dev/null
then
    echo "❌ Docker未安装，请先安装Docker"
    exit 1
fi

# 检查Docker Compose
if ! command -v docker-compose &> /dev/null
then
    echo "❌ Docker Compose未安装，请先安装Docker Compose"
    exit 1
fi

# 进入项目目录
cd "$(dirname "$0")"

# 创建必要的目录
mkdir -p nacos/logs/{nacos-1,nacos-2,nacos-3}
mkdir -p nacos/data/{nacos-1,nacos-2,nacos-3}
mkdir -p mysql/conf mysql/data mysql/init

# 复制配置文件
if [ ! -f mysql/conf/my.cnf ]; then
    echo "📋 创建MySQL配置文件..."
    cat > mysql/conf/my.cnf << 'EOF'
[mysqld]
server-id = 1
log-bin = mysql-bin
binlog-format = ROW
expire_logs_days = 7
max_connections = 1000
max_allowed_packet = 64M
innodb_buffer_pool_size = 1G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2
sync_binlog = 1

[client]
default-character-set = utf8mb4
EOF
fi

# 复制MySQL初始化脚本
if [ ! -f mysql/init/init.sql ]; then
    echo "📋 复制MySQL初始化脚本..."
    cp mysql/init.sql mysql/init/
fi

# 启动MySQL集群
echo "📦 启动MySQL集群..."
docker-compose up -d mysql-cluster

# 等待MySQL就绪
echo "⏳ 等待MySQL启动完成..."
sleep 30

# 检查MySQL连接
for i in {1..10}; do
    if docker exec mysql-cluster mysqladmin ping -h localhost -u root -proot123 &> /dev/null; then
        echo "✅ MySQL启动成功"
        break
    fi
    echo "等待MySQL启动... ($i/10)"
    sleep 10
done

# 启动Nacos集群
echo "📦 启动Nacos集群..."
docker-compose up -d nacos-1 nacos-2 nacos-3

# 等待Nacos就绪
echo "⏳ 等待Nacos启动完成..."
sleep 60

# 检查Nacos节点状态
echo "📊 检查Nacos集群状态..."
for i in {1..3}; do
    node="nacos-$i"
    status=$(docker inspect $node --format='{{.State.Status}}')
    if [ "$status" = "running" ]; then
        health=$(docker exec $node curl -s http://localhost:8848/nacos/v1/ns/operator/servers 2>/dev/null | jq -r '.servers[] | select(.healthy)')
        healthy_count=$(echo "$health" | jq '[.healthy] | select(== true)' | length)
        total_count=$(echo "$health" | jq '.servers | length')
        echo "✅ $node: $healthy_count/$total_count 节点健康"
    else
        echo "❌ $node: 未运行"
    fi
done

# 显示访问信息
echo "🌐 Nacos集群启动完成！"
echo "📱 控制台访问地址: http://localhost:8848/nacos"
echo "📱 集群管理: http://localhost:8848/nacos/#/cluster-management"
echo "🔑 用户名: nacos"
echo "🔑 密码: nacos"
echo "📊 集群节点数: 3"
echo "📊 数据库端口: 3306"

# 显示健康检查命令
echo ""
echo "📋 健康检查命令:"
echo "docker-compose ps"
echo "docker logs nacos-1"
echo "docker logs nacos-2"
echo "docker logs nacos-3"
echo "docker logs mysql-cluster"
```

##### 停止脚本 (stop-nacos.sh)
```bash
#!/bin/bash

echo "🛑 停止IOE-DREAM Nacos集群..."

# 停止Nacos服务
docker-compose stop nacos-1 nacos-2 nacos-3

# 停止MySQL服务
docker-compose stop mysql-cluster

echo "✅ Nacos集群已停止"
```

#### 1.4.5 验证集群状态

##### 验证脚本 (verify-nacos-cluster.sh)
```bash
#!/bin/bash

echo "🔍 验证Nacos集群状态..."

# 检查所有节点状态
echo "📊 检查Nacos节点状态:"
docker-compose ps

echo ""
echo "📋 检查节点健康状态:"
for i in {1..3}; do
    node="nacos-$i"
    echo "检查节点 $node..."

    # 检查容器状态
    status=$(docker inspect $node --format='{{.State.Status}}')
    if [ "$status" != "running" ]; then
        echo "❌ $node: 未运行"
        continue
    fi

    # 检查健康状态
    echo "  容器状态: $status"

    # 检查HTTP访问
    http_status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:884$((i+7))/nacos/actuator/health)
    if [ "$http_status" = "200" ]; then
        echo "  HTTP访问: ✅ 正常"
    else
        echo "  HTTP访问: ❌ 异常 ($http_status)"
    fi

    # 获取集群信息
    cluster_info=$(curl -s http://localhost:884$((i+7))/nacos/v1/ns/operator/servers 2>/dev/null)
    if [ -n "$cluster_info" ]; then
        leader=$(echo "$cluster_info" | jq -r '.servers[] | select(.leader) | select(.address) | first')
        follower_count=$(echo "$leader" | jq -r '[.servers[] | select(.leader) | length] | length')
        echo "  集群信息: 领导节点 $leader，$follower_count 个从节点"
    fi
done

echo ""
echo "✅ 集群状态验证完成"
```

### 🎯 实战项目: 服务注册实战

#### 1.5 创建微服务父项目

##### parent/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>net.lab1024.ioe-dream</groupId>
    <artifactId>ioe-dream-parent</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <name>IOE-DREAM Parent POM</name>
    <description>IOE-DREAM微服务父项目</description>

    <properties>
        <java.version>17</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

        <!-- Spring Boot版本 -->
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <spring-cloud-alibaba.version>2022.0.0.0.5</spring-cloud-alibaba.version>
        <spring-boot-admin.version>3.2.0</spring-boot-admin.version>

        <!-- 工具版本 -->
        <maven.compiler.version>3.11.0</maven.compiler.version>
        <maven.surefire.version>3.1.2</maven.surefire.version>
        <lombok.version>1.18.30</lombok.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
        <mybatis-plus.version>3.5.5</mybatis-plus.version>
        <druid.version>1.2.20</druid.version>

        <!-- 监控版本 -->
        <micrometer.version>1.12.0</micrometer.version>
        <actuator.version>3.1.8</actuator.version>

        <!-- 默认UTF-8编码 -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud BOM -->
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- Spring Cloud Alibaba BOM -->
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- 工具库版本管理 -->
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </dependency>

            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>

            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-boot-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>druid-spring-boot-3-starter</artifactId>
                <version>${druid.version}</version>
            </dependency>

            <!-- 监控相关 -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-actuator</artifactId>
            </dependency>

            <dependency>
                <groupId>io.micrometer</groupId>
                <artifactId>micrometer-registry-prometheus</artifactId>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
        </dependency>

        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Spring Cloud Starter -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>

        <!-- Spring Cloud Alibaba -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- 工具库 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- 监控 -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-starter-webflux</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
                <configuration>
                    <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
                <configuration>
                    <annotationProcessorPaths>
                        <path>lombok/*</path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven.compiler.version}</version>
                <configuration>
                    <source>1.8</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${maven.surefire.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

#### 1.6 创建第一个微服务

##### identity-service/pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>net.lab1024.ioe-dream</groupId>
        <artifactId>ioe-dream-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>identity-service</artifactId>
    <packaging>jar</packaging>

    <name>身份权限服务</name>
    <description>IOE-DREAM身份权限微服务</description>

    <dependencies>
        <!-- Web Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Nacos Discovery -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <!-- Nacos Config -->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>

        <!-- Load Balancer -->
        <dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        </dependency>

        <!-- OpenFeign -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- Micrometer -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

##### application.yml
```yaml
server:
  port: 8081

spring:
  application:
    name: identity-service

  cloud:
    nacos:
      discovery:
        server-addr: 192.168.10.100:8848
        namespace: ioe-dream
        group: SERVICE_GROUP
        username: nacos
        password: nacos

        # 服务注册配置
        enabled: true
        register-enabled: true

        # 服务实例配置
        instance:
          # 实例名称
          instance-name: ${spring.application.name}:${spring.cloud.client.ip-address}:${server.port}
          # IP地址
          ip-address: ${spring.cloud.client.ip-address}
          # 端口
          port: ${server.port}
          # 是否启用
          enabled: true
          # 权重
          weight: 1
          # 元数据
          metadata:
            version: ${project.version:1.0.0}
            environment: ${spring.profiles.active:dev}
            region: ${REGION:default}
            zone: ${ZONE:default}
            cluster: ${CLUSTER:default}

      # 配置中心配置
      config:
        server-addr: 192.168.10.100:8848
        namespace: ioe-dream
        group: CONFIG_GROUP
        username: nacos
        password: nacos
        # 配置文件类型
        file-extension: yaml
        # 是否启用配置刷新
        refresh-enabled: true
        # 配置刷新间隔
        refresh-interval: 30s
        # 是否启用远程配置
        enabled: true

        # 共享配置
        shared-configs:
          - data-id: common-db.yml
            group: CONFIG_GROUP
            refresh: true
          - data-id: common-redis.yml
            group: CONFIG_GROUP
            refresh: true
          - data-id: common-logback.yml
            group: CONFIG_GROUP
            refresh: true

        # 扩展配置
        extension-configs:
          - data-id: business-config.yml
            group: BUSINESS_GROUP
            refresh: true

# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    info:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true

# 日志配置
logging:
  level:
    org.springframework.cloud: INFO
    com.alibaba.nacos: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"
```

##### 主程序类
```java
package net.lab1024.sa.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 身份权限服务主程序
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
```

##### 服务注册配置
```java
package net.lab1024.sa.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 服务注册发现配置
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Component
@EnableDiscoveryClient
public class ServiceRegistryConfig {

    @Value("${spring.cloud.client.ip-address}")
    private String ipAddress;

    @Bean
    public void configureClientIp() {
        // 设置客户端IP地址
        System.setProperty("spring.cloud.client.ip-address", ipAddress);
    }
}
```

##### 服务发现客户端
```java
package net.lab1024.sa.identity.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 服务发现客户端
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-2025-11-27
 */
@Slf4j
@Component
public class ServiceDiscoveryClient {

    private final DiscoveryClient discoveryClient;

    public ServiceDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * 获取指定服务的所有实例
     */
    public List<ServiceInstance> getInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }

    /**
     * 选择一个服务实例
     */
    public ServiceInstance chooseInstance(String serviceName) {
        List<ServiceInstance> instances = getInstances(serviceName);
        if (instances.isEmpty()) {
            throw new RuntimeException("No instances available for service: " + serviceName);
        }
        return instances.get(0);
    }

    /**
     * 获取健康的服务实例
     */
    public List<ServiceInstance> getHealthyInstances(String serviceName) {
        return discoveryClient.getInstances(serviceName).stream()
            .filter(instance -> "UP".equals(instance.getMetadata().get("health")))
            .toList();
    }

    /**
     * 服务实例状态检查
     */
    public boolean isServiceHealthy(String serviceName) {
        List<ServiceInstance> instances = getInstances(serviceName);
        return !instances.isEmpty() &&
               instances.stream().any(instance -> "UP".equals(instance.getMetadata().get("health")));
    }
}
```

##### REST控制器
```java
package net.lab1024.sa.identity.controller;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;
import net.lab1024.sa.identity.service.UserService;
import net.lab1024.sa.identity.domain.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseDTO<String> register(@RequestBody UserVO userVO) {
        try {
            String result = userService.register(userVO);
            return SmartResponseUtil.success(result);
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return SmartResponseUtil.error("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseDTO<String> login(@RequestParam String username, @RequestParam String password) {
        try {
            String token = userService.login(username, password);
            return SmartResponseUtil.success(token);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return SmartResponseUtil.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseDTO<UserVO> getUser(@PathVariable Long id) {
        try {
            UserVO user = userService.getUserById(id);
            return SmartResponseUtil.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return SmartResponseUtil.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseDTO<String> health() {
        return SmartResponseUtil.success("身份权限服务运行正常");
    }
}
```

## 📚 知识点总结

### Day 1 核心知识点
1. 微服务架构优势和挑战
2. Spring Boot 3.x自动配置原理
3. Nacos集群部署和配置
4. 服务注册发现机制
5. 配置中心动态配置

### 关键技能点
1. Docker容器化部署
2. 集群架构设计
3. 微服务基础架构搭建
4. 服务注册和发现实战
5. 配置管理和热更新

### 常见问题和解决方案
1. **Nacos集群问题**: 节点通信、数据一致性、选举机制
2. **服务注册问题**: 网络隔离、健康检查、元数据配置
3. **配置更新问题**: 版本管理、缓存机制、事件通知
4. **性能优化**: 连接池配置、缓存策略、负载均衡

---

**教材版本**: v1.0
**创建时间**: 2025-11-27T01:10:00+08:00
**适用阶段**: Phase 1 Week 7-8 Day 1-2
**下一步**: Day 3-4 - Spring Cloud Gateway和Sentinel实战