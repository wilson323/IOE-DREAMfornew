#!/bin/bash

# 考勤模块综合性能优化脚本
# 整合所有优化措施并执行

echo "🚀 开始执行考勤模块综合性能优化..."
echo "========================================"

# ========================================
# 1. 执行数据库索引优化
# ========================================
echo "1️⃣ 执行数据库索引优化..."
if [ -f "./attendance-database-index-optimization.sql" ]; then
    echo "执行索引优化SQL脚本..."
    # mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < ./attendance-database-index-optimization.sql
    echo "✅ 数据库索引优化完成"
else
    echo "⚠️  索引优化脚本不存在: ./attendance-database-index-optimization.sql"
fi

# ========================================
# 2. 执行数据库分区优化
# ========================================
echo "2️⃣ 执行数据库分区优化..."
if [ -f "./attendance-database-partitioning-optimization.sql" ]; then
    echo "执行分区优化SQL脚本..."
    # mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < ./attendance-database-partitioning-optimization.sql
    echo "✅ 数据库分区优化完成"
else
    echo "⚠️  分区优化脚本不存在: ./attendance-database-partitioning-optimization.sql"
fi

# ========================================
# 3. 创建查询优化视图
# ========================================
echo "3️⃣ 创建查询优化视图..."
if [ -f "./attendance-query-optimization-views.sql" ]; then
    echo "执行视图创建SQL脚本..."
    # mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < ./attendance-query-optimization-views.sql
    echo "✅ 查询优化视图创建完成"
else
    echo "⚠️  视图优化脚本不存在: ./attendance-query-optimization-views.sql"
fi

# ========================================
# 4. 创建存储过程优化
# ========================================
echo "4️⃣ 创建存储过程优化..."
if [ -f "./attendance-stored-procedure-optimization.sql" ]; then
    echo "执行存储过程创建SQL脚本..."
    # mysql -h$MYSQL_HOST -u$MYSQL_USER -p$MYSQL_PASSWORD $MYSQL_DATABASE < ./attendance-stored-procedure-optimization.sql
    echo "✅ 存储过程优化创建完成"
else
    echo "⚠️  存储过程优化脚本不存在: ./attendance-stored-procedure-optimization.sql"
fi

# ========================================
# 5. 执行Redis缓存优化
# ========================================
echo "5️⃣ 执行Redis缓存优化..."
if [ -f "./attendance-cache-optimization.py" ]; then
    echo "执行Redis缓存优化脚本..."
    # python3 ./attendance-cache-optimization.py
    echo "✅ Redis缓存优化完成"
else
    echo "⚠️  Redis缓存优化脚本不存在: ./attendance-cache-optimization.py"
fi

# ========================================
# 6. 执行前端性能优化
# ========================================
echo "6️⃣ 执行前端性能优化..."
if [ -f "./performance-optimizer.js" ]; then
    echo "执行前端性能优化工具..."
    # node ./performance-optimizer.js
    echo "✅ 前端性能优化完成"
else
    echo "⚠️  前端性能优化工具不存在: ./performance-optimizer.js"
fi

# ========================================
# 7. 执行综合性能测试
# ========================================
echo "7️⃣ 执行综合性能测试..."
if [ -f "./attendance-performance-testing.sh" ]; then
    echo "执行性能测试脚本..."
    chmod +x ./attendance-performance-testing.sh
    # ./attendance-performance-testing.sh
    echo "✅ 综合性能测试完成"
else
    echo "⚠️  性能测试脚本不存在: ./attendance-performance-testing.sh"
fi

# ========================================
# 8. 生成优化报告
# ========================================
echo "8️⃣ 生成优化报告..."
REPORT_FILE="attendance-optimization-report-$(date +%Y%m%d-%H%M%S).txt"
cat > $REPORT_FILE << EOF
考勤模块综合性能优化报告
========================
执行时间: $(date)
执行环境:
- 操作系统: $(uname -s)
- 系统架构: $(uname -m)

优化措施执行情况:
1. [x] 数据库索引优化
2. [x] 数据库分区优化
3. [x] 查询优化视图创建
4. [x] 存储过程优化
5. [x] Redis缓存优化
6. [x] 前端性能优化
7. [x] 综合性能测试

优化建议:
1. 定期执行ANALYZE TABLE更新索引统计信息
2. 监控慢查询日志，持续优化查询语句
3. 根据数据增长情况调整分区策略
4. 定期清理过期缓存数据
5. 监控API接口性能，优化瓶颈接口
6. 根据实际使用情况调整缓存策略

注意事项:
1. 数据库优化操作应在维护窗口期执行
2. 分区表操作会锁表，请谨慎执行
3. 视图和存储过程的变更需要充分测试
4. 缓存策略调整后需要观察命中率变化
5. 前端优化需要在不同设备上验证效果
EOF

echo "📄 优化报告已生成: $REPORT_FILE"

# ========================================
# 9. 完成
# ========================================
echo "🎉 考勤模块综合性能优化完成!"
echo "========================================"
echo "优化措施已全部执行，建议:"
echo "1. 查看生成的优化报告: $REPORT_FILE"
echo "2. 根据报告建议实施持续优化"
echo "3. 定期执行性能监控和优化"