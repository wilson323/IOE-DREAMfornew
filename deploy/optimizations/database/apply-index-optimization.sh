#!/bin/bash
echo "IOE-DREAM 数据库索引优化执行脚本"
echo "===================================="

DB_HOST=${DB_HOST:-127.0.0.1}
DB_PORT=${DB_PORT:-3306}
DB_NAME=${DB_NAME:-ioedream}
DB_USER=${DB_USER:-root}

echo "数据库连接信息:"
echo "主机: $DB_HOST:$DB_PORT"
echo "数据库: $DB_NAME"
echo "用户: $DB_USER"
echo

read -s -p "请输入数据库密码: " DB_PASSWORD
echo

echo "正在执行索引优化..."
mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME < p0-index-optimization.sql

if [ $? -eq 0 ]; then
    echo "索引优化执行成功！"
    echo "正在验证优化效果..."
    mysql -h$DB_HOST -P$DB_PORT -u$DB_USER -p$DB_PASSWORD $DB_NAME < verify-index-performance.sql
else
    echo "索引优化执行失败！"
    exit 1
fi

echo "数据库优化完成！"
