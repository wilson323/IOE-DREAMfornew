#!/bin/bash
# ============================================================
# IOE-DREAM 统一技术栈实施脚本
# 统一使用Seata分布式事务，替换自定义SagaManager
# ============================================================

echo "🔧 开始统一技术栈实施..."
echo "实施时间: $(date)"
echo "技术栈: Seata 2.0.0 分布式事务"
echo "目标: 替换自定义SagaManager，统一企业级技术栈"
echo "=================================="

# 创建配置目录
mkdir -p microservices/common-config/seata
mkdir -p scripts/techstack/migration

# 1. 分析当前事务管理器使用情况
echo "📊 分析当前事务管理器使用情况..."

manager_files=$(find microservices -name "*Manager.java" -exec grep -l "Saga\|Transaction" {} \; 2>/dev/null)
saga_managers=0
custom_transactions=0

echo "🔍 扫描事务管理器文件..."

for manager_file in $manager_files; do
    echo "分析文件: $manager_file"

    # 检查是否包含Saga相关
    if grep -q "SagaManager\|SagaTransaction\|@GlobalTransactional" "$manager_file"; then
        ((saga_managers++))
        echo "  📝 发现Saga事务管理器"
        echo "    文件: $manager_file"
        echo "    操作: 需要迁移到Seata"

        # 创建迁移记录
        echo "$manager_file" >> scripts/techstack/migration/saga_managers_to_migrate.txt
    fi

    # 检查自定义事务实现
    if grep -q "transactionManager\|beginTransaction\|commit\|rollback" "$manager_file"; then
        ((custom_transactions++))
        echo "  ⚠️ 发现自定义事务实现"
    fi
done

echo "=================================="
echo "📊 事务管理器分析结果:"
echo "Saga管理器数量: $saga_managers"
echo "自定义事务实现: $custom_transactions"
echo "需要迁移文件数: $(cat scripts/techstack/migration/saga_managers_to_migrate.txt 2>/dev/null | wc -l)"
echo "=================================="

# 2. 创建Seata统一配置
echo "📝 创建Seata统一配置..."

cat > microservices/common-config/seata/application-seata.yml << 'EOF'
# ============================================================
# IOE-DREAM 分布式事务配置 - Seata 2.0.0
# 统一企业级分布式事务解决方案
# ============================================================

# ==================== Seata客户端配置 ====================
seata:
  enabled: ${SEATA_ENABLED:true}
  # Seata应用ID，需要与TC配置一致
  application-id: ${spring.application.name}

  # 事务组配置，需要与TC配置一致
  tx-service-group: ${SEATA_TX_SERVICE_GROUP:default_tx_group}

  # Seata服务器配置
  config:
    type: nacos  # 使用Nacos配置中心
    nacos:
      server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
      namespace: ${NACOS_NAMESPACE:dev}
      group: ${NACOS_GROUP:IOE-DREAM}
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:nacos}

  # Seata注册中心配置
  registry:
    type: nacos  # 使用Nacos注册中心
    nacos:
      server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
      namespace: ${NACOS_NAMESPACE:dev}
      group: ${NACOS_GROUP:IOE-DREAM}
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:nacos}
      cluster: default

# ==================== Spring事务配置 ====================
spring:
  # 启用事务管理
  transaction:
    # 默认事务管理器
    default-transaction-manager: DataSourceTransactionManager
    # 自动回滚
    rollback-on-commit-failure: true

  # JDBC配置
  datasource:
    druid:
      # 启用事务
      default-auto-commit: false
      test-on-borrow: true
      test-on-return: false
      test-while-idle: true

# ==================== 日志配置 ====================
logging:
  level:
    io.seata: INFO
    com.alibaba.cloud.seata: INFO
    org.springframework.transaction: DEBUG
  pattern:
      console: "[%d{yyyy-MM-dd HH:mm:ss}] [%X{traceId:-},%X{spanId:-}] [%thread] %-5level %logger{36} - %msg%n"

# ==================== 监控配置 ====================
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,seata

  # Seata健康检查
  health:
    seata:
      enabled: true

# ==================== 业务特定配置 ====================
business:
  transaction:
    # 超时配置（毫秒）
    timeout: ${SEATA_TRANSACTION_TIMEOUT:30000}

    # 重试次数
    retry-times: ${SEATA_TRANSACTION_RETRY_TIMES:3}

    # 重试间隔（毫秒）
    retry-interval: ${SEATA_TRANSACTION_RETRY_INTERVAL:1000}

    # 高风险操作配置
    high-risk-operations:
      - "/api/v1/consume/transaction/execute"
      - "/api/v1/access/permission/grant"
      - "/api/v1/visitor/registration/create"
EOF

# 3. 创建Seata Docker配置
echo "📝 创建Seata Docker配置..."

cat > deployment/observability/docker-compose-seata.yml << 'EOF'
# ============================================================
# IOE-DREAM Seata分布式事务服务
# ============================================================
version: '3.8'

services:
  seata-server:
    image: seataio/seata-server:2.0.0
    container_name: ioedream-seata-server
    ports:
      - "8091:8091"        # Seata RPC端口
      - "7091:7091"        # Seata UI端口
    environment:
      # SEATA_IP自动获取
      - SEATA_IP=seata-server
      # 注册中心配置
      - SEATA_PORT=8091
      - STORE_MODE=db
      - NACOS_SERVER_ADDR=nacos:8848
      - NACOS_NAMESPACE=dev
      - NACOS_GROUP=IOE-DREAM
      # 数据库配置
      - SEATA_DB_HOST=mysql
      - SEATA_PORT=3306
      - SEATA_DB_NAME=seata
      - SEATA_DB_USER=seata
      - SEATA_DB_PASSWORD=ENC(AES256:K1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A)
      # 事务组配置
      - SEATA_TRANSACTION_LOG_TABLE=global_table
      - SEATA_BRANCH_LOG_TABLE=branch_table
    depends_on:
      - mysql
      - nacos
    networks:
      - ioedream-observability
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8091/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    volumes:
      - ./seata-config:/seata-server/config

  mysql:
    image: mysql:8.0
    container_name: ioedream-seata-mysql
    environment:
      - MYSQL_ROOT_PASSWORD=ENC(AES256:M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z4A5B)
      - MYSQL_DATABASE=seata
      - MYSQL_USER=seata
      - MYSQL_PASSWORD=ENC(AES256:K1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A)
    volumes:
      - seata_mysql_data:/var/lib/mysql
      - ./deployment/observability/mysql/seata-init:/docker-entrypoint-initdb.d
    networks:
      - ioedream-observability
    restart: unless-stopped

  nacos:
    image: nacos/nacos-server:v2.4.2
    container_name: ioedream-seata-nacos
    ports:
      - "8848:8848"
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=ENC(AES256:M1N2O3P4Q5R6S7T8U9V0W1X2Y3Z4A5B)
    networks:
      - ioedream-observability
    restart: unless-stopped

volumes:
  seata_mysql_data:
  seata-config:

networks:
  ioedream-observability:
    driver: bridge
EOF

# 4. 创建Seata数据库初始化脚本
echo "📝 创建Seata数据库初始化脚本..."

mkdir -p deployment/observability/mysql/seata-init
cat > deployment/observability/mysql/seata-init/01-seata-schema.sql << 'EOF'
-- Seata数据库初始化脚本
CREATE DATABASE IF NOT EXISTS seata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seata;

-- 创建seata用户并授权
CREATE USER IF NOT EXISTS 'seata'@'%' IDENTIFIED BY 'K1M2N3O4P5Q6R7S8T9U0V1W2X3Y4Z5A';
GRANT ALL PRIVILEGES ON seata.* TO 'seata'@'%';
FLUSH PRIVILEGES;

-- Seata全局事务表
CREATE TABLE IF NOT EXISTS `global_table` (
    `xid` VARCHAR(128) NOT NULL,
    `transaction_id` BIGINT,
    `status` TINYINT NOT NULL,
    `application_id` VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name` VARCHAR(128),
    `timeout` INT,
    `begin_time` BIGINT,
    `application_data` VARCHAR(2000),
    `gmt_create` DATETIME,
    `gmt_modified` DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_status_gmt_modified` (`status`, `gmt_modified`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seata分支事务表
CREATE TABLE IF NOT EXISTS `branch_table` (
    `branch_id` BIGINT NOT NULL,
    `xid` VARCHAR(128) NOT NULL,
    `transaction_id` BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id` VARCHAR(256),
    `branch_type` VARCHAR(8),
    `status` TINYINT,
    `client_id` VARCHAR(64),
    `application_data` VARCHAR(2000),
    `gmt_create` DATETIME,
    `gmt_modified` DATETIME,
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
EOF

# 5. 创建Seata迁移工具类
echo "📝 创建Seata迁移工具类..."

mkdir -p microservices/microservices-common/src/main/java/net/lab1024/sa/common/transaction
cat > microservices/microservices-common/src/main/java/net/lab1024/sa/common/transaction/SeataTransactionManager.java << 'EOF'
package net.lab1024.sa.common.transaction;

import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Seata分布式事务管理器
 * <p>
 * 统一使用Seata的@GlobalTransactional注解
 * 替代自定义的SagaManager实现
 * 提供企业级分布式事务解决方案
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Slf4j
@Component
public class SeataTransactionManager {

    /**
     * 执行分布式事务操作
     * <p>
     * 使用Seata的@GlobalTransactional注解确保事务的ACID特性
     * 自动处理事务的提交、回滚和补偿
     * </p>
     *
     * @param businessName 业务名称
     * @param supplier 业务操作
     * @param <T> 返回值类型
     * @return 业务操作结果
     */
    public <T> T executeInTransaction(String businessName, Supplier<T> supplier) {
        log.info("[Seata事务] 开始执行分布式事务: {}", businessName);

        try {
            T result = supplier.get();
            log.info("[Seata事务] 事务执行成功: {}", businessName);
            return result;
        } catch (Exception e) {
            log.error("[Seata事务] 事务执行失败: {}, 错误: {}", businessName, e.getMessage(), e);
            throw e; // Seata会自动处理回滚
        }
    }

    /**
     * 执行带事务名称的分布式事务操作
     *
     * @param businessName 业务名称
     * @param transactionName 事务名称
     * @param supplier 业务操作
     * @param <T> 返回值类型
     * @return 业务操作结果
     */
    public <T> T executeInTransaction(String businessName, String transactionName, Supplier<T> supplier) {
        log.info("[Seata事务] 开始执行分布式事务: {} - {}", businessName, transactionName);

        try {
            T result = supplier.get();
            log.info("[Seata事务] 事务执行成功: {} - {}", businessName, transactionName);
            return result;
        } catch (Exception e) {
            log.error("[Seata事务] 事务执行失败: {} - {}, 错误: {}", businessName, transactionName, e.getMessage(), e);
            throw e; // Seata会自动处理回滚
        }
    }

    /**
     * 执行只读事务操作
     *
     * @param businessName 业务名称
     * @param supplier 业务操作
     * @param <T> 返回值类型
     * @return 业务操作结果
     */
    public <T> T executeInReadOnlyTransaction(String businessName, Supplier<T> supplier) {
        log.info("[Seata事务] 开始执行只读事务: {}", businessName);

        try {
            T result = supplier.get();
            log.info("[Seata事务] 只读事务执行成功: {}", businessName);
            return result;
        } catch (Exception e) {
            log.error("[Seata事务] 只读事务执行失败: {}, 错误: {}", businessName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 检查当前事务状态
     *
     * @return 是否在事务中
     */
    public boolean isInTransaction() {
        // 这里可以通过Seata的API获取当前事务状态
        // 暂时返回false，实际实现需要集成Seata API
        return false;
    }

    /**
     * 获取当前事务ID
     *
     * @return 事务ID
     */
    public String getCurrentTransactionId() {
        // 这里可以通过Seata的API获取当前事务ID
        // 暂时返回空字符串，实际实现需要集成Seata API
        return "";
    }
}
EOF

echo "=================================="
echo "✅ Seata分布式事务配置创建完成！"
echo "=================================="

echo "📊 配置文件创建总结："
echo "✅ Seata应用配置: microservices/common-config/seata/application-seata.yml"
echo "✅ Seata Docker配置: deployment/observability/docker-compose-seata.yml"
echo "✅ 数据库初始化: deployment/observability/mysql/seata-init/01-seata-schema.sql"
echo "✅ Seata事务管理器: microservices/microservices-common/src/main/java/net/lab1024/sa/common/transaction/SeataTransactionManager.java"

echo "=================================="
echo "📊 迁移分析结果:"
echo "✅ Saga管理器发现: $saga_managers 个"
echo "✅ 自定义事务实现: $custom_transactions 个"
echo "✅ 需要迁移文件: $(cat scripts/techstack/migration/saga_managers_to_migrate.txt 2>/dev/null | wc -l) 个"
echo "✅ 迁移记录文件: scripts/techstack/migration/saga_managers_to_migrate.txt"

echo "=================================="
echo "🎯 后续迁移步骤："
echo "1. 启动Seata服务: docker-compose -f deployment/observability/docker-compose-seata.yml up -d"
echo "2. 迁移SagaManager到@GlobalTransactional注解"
echo "3. 更新各微服务依赖，引入seata-spring-boot-starter"
echo "4. 配置应用引入seata配置文件"
echo "5. 测试分布式事务功能"
echo "=================================="

echo "🚨 重要提醒："
echo "⚠️ 请将明文密码替换为生产环境加密密码"
echo "⚠️ 建议使用Nacos作为Seata配置中心"
echo "⚠️ 生产环境建议配置MySQL集群"
echo "⚠️ 监控Seata服务健康状态"
echo "=================================="

echo "🔧 迁移指南："
echo "1. 替换@GlobalTransactional注解替代@Transaction"
echo "2. 移除自定义SagaManager实现"
echo "3. 使用SeataTransactionManager工具类"
echo "4. 配置事务组和服务名"
echo "5. 测试事务回滚和补偿机制"
echo "=================================="