#!/bin/sh
# Nacos数据库自动初始化脚本
# 在Nacos启动前自动检查并初始化nacos数据库

set -e

echo "=== Nacos数据库自动初始化 ==="

# 等待MySQL就绪
echo "等待MySQL就绪..."
until mysql -h"$MYSQL_SERVICE_HOST" -P"$MYSQL_SERVICE_PORT" -u"$MYSQL_SERVICE_USER" -p"$MYSQL_SERVICE_PASSWORD" -e "SELECT 1" > /dev/null 2>&1; do
    echo "MySQL未就绪，等待中..."
    sleep 2
done

echo "MySQL已就绪"

# 检查nacos数据库是否存在
DB_EXISTS=$(mysql -h"$MYSQL_SERVICE_HOST" -P"$MYSQL_SERVICE_PORT" -u"$MYSQL_SERVICE_USER" -p"$MYSQL_SERVICE_PASSWORD" -e "SHOW DATABASES LIKE 'nacos';" 2>/dev/null | grep -c nacos || echo "0")

if [ "$DB_EXISTS" = "0" ]; then
    echo "nacos数据库不存在，正在创建..."
    mysql -h"$MYSQL_SERVICE_HOST" -P"$MYSQL_SERVICE_PORT" -u"$MYSQL_SERVICE_USER" -p"$MYSQL_SERVICE_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
    
    # 检查是否有初始化SQL文件
    if [ -f "/init-sql/nacos-schema.sql" ]; then
        echo "执行nacos数据库初始化脚本..."
        mysql -h"$MYSQL_SERVICE_HOST" -P"$MYSQL_SERVICE_PORT" -u"$MYSQL_SERVICE_USER" -p"$MYSQL_SERVICE_PASSWORD" nacos < /init-sql/nacos-schema.sql 2>/dev/null
        echo "nacos数据库初始化完成"
    else
        echo "警告: 未找到nacos-schema.sql文件，数据库已创建但未初始化表结构"
    fi
else
    echo "nacos数据库已存在"
    
    # 检查表数量
    TABLE_COUNT=$(mysql -h"$MYSQL_SERVICE_HOST" -P"$MYSQL_SERVICE_PORT" -u"$MYSQL_SERVICE_USER" -p"$MYSQL_SERVICE_PASSWORD" -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>/dev/null | tail -n 1)
    if [ "$TABLE_COUNT" = "0" ]; then
        echo "nacos数据库为空，正在初始化表结构..."
        if [ -f "/init-sql/nacos-schema.sql" ]; then
            mysql -h"$MYSQL_SERVICE_HOST" -P"$MYSQL_SERVICE_PORT" -u"$MYSQL_SERVICE_USER" -p"$MYSQL_SERVICE_PASSWORD" nacos < /init-sql/nacos-schema.sql 2>/dev/null
            echo "nacos数据库表结构初始化完成"
        fi
    else
        echo "nacos数据库已初始化 (表数量: $TABLE_COUNT)"
    fi
fi

echo "=== 初始化完成，启动Nacos ==="

# 执行原始Nacos启动命令
exec "$@"
