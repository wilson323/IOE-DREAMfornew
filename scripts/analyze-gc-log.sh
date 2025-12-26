#!/bin/bash
# ============================================================
# GC日志分析工具
#
# 功能：分析GC日志，生成性能报告
# 使用方法：./scripts/analyze-gc-log.sh <gc-log-file>
# ============================================================

GC_LOG_FILE=$1

if [ -z "$GC_LOG_FILE" ]; then
    echo "❌ 错误：请指定GC日志文件路径"
    echo "使用方法: $0 <gc-log-file>"
    echo "示例: $0 ./logs/gc-12345.log"
    exit 1
fi

if [ ! -f "$GC_LOG_FILE" ]; then
    echo "❌ 错误：GC日志文件不存在: $GC_LOG_FILE"
    exit 1
fi

echo "============================================================"
echo "📊 GC日志分析报告"
echo "============================================================"
echo "日志文件: $GC_LOG_FILE"
echo "分析时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo ""

# ============================================================
# 1. GC统计信息
# ============================================================

echo "📈 1. GC统计概览"
echo "------------------------------------------------------------"

# GC次数统计
YOUNG_GC_COUNT=$(grep "GC (Allocation Failure)" "$GC_LOG_FILE" 2>/dev/null | wc -l)
MIXED_GC_COUNT=$(grep "G1 Evacuation Pause" "$GC_LOG_FILE" 2>/dev/null | wc -l)
FULL_GC_COUNT=$(grep "Full GC" "$GC_LOG_FILE" 2>/dev/null | wc -l)

echo "Young GC次数: $YOUNG_GC_COUNT"
echo "Mixed GC次数: $MIXED_GC_COUNT"
echo "Full GC次数: $FULL_GC_COUNT"
echo ""

# ============================================================
# 2. GC暂停时间分析
# ============================================================

echo "⏱️  2. GC暂停时间分析"
echo "------------------------------------------------------------"

# 提取GC暂停时间
echo "分析GC暂停时间分布..."

# 使用awk提取暂停时间并计算统计值
PAUSE_TIMES=$(grep -o "Real: [0-9.]* ms" "$GC_LOG_FILE" 2>/dev/null | sed 's/Real: //; s/ ms//' | sort -n)

if [ -n "$PAUSE_TIMES" ]; then
    # 计算统计值
    PAUSE_COUNT=$(echo "$PAUSE_TIMES" | wc -l)
    PAUSE_AVG=$(echo "$PAUSE_TIMES" | awk '{sum+=$1; count++} END {if(count>0) print sum/count}')
    PAUSE_MAX=$(echo "$PAUSE_TIMES" | awk 'max=="" || $1>max {max=$1} END {print max}')
    PAUSE_MIN=$(echo "$PAUSE_TIMES" | awk 'min=="" || $1<min {min=$1} END {print min}')

    echo "GC暂停次数: $PAUSE_COUNT"
    echo "平均暂停时间: ${PAUSE_AVG}ms"
    echo "最大暂停时间: ${PAUSE_MAX}ms"
    echo "最小暂停时间: ${PAUSE_MIN}ms"

    # 暂停时间分布
    echo ""
    echo "暂停时间分布:"
    echo "  <100ms:  $(echo "$PAUSE_TIMES" | awk '$1<100 {count++} END {print count}')"
    echo "  100-200ms: $(echo "$PAUSE_TIMES" | awk '$1>=100 && $1<200 {count++} END {print count}')"
    echo "  200-500ms: $(echo "$PAUSE_TIMES" | awk '$1>=200 && $1<500 {count++} END {print count}')"
    echo "  >500ms:  $(echo "$PAUSE_TIMES" | awk '$1>=500 {count++} END {print count}')"
else
    echo "⚠️  未找到GC暂停时间数据"
fi
echo ""

# ============================================================
# 3. GC原因分析
# ============================================================

echo "🔍 3. GC原因分析"
echo "------------------------------------------------------------"

echo "分析GC触发原因..."

# 统计GC原因
ALLOCATION_FAILURE=$(grep -c "Allocation Failure" "$GC_LOG_FILE" 2>/dev/null)
EVACUATION=$(grep -c "Evacuation Pause" "$GC_LOG_FILE" 2>/dev/null)
SYSTEM_GC=$(grep -c "System.gc()" "$GC_LOG_FILE" 2>/dev/null)
HEAP_DUMP=$(grep -c "heap dump" "$GC_LOG_FILE" 2>/dev/null)

echo "Allocation Failure（分配失败）: $ALLOCATION_FAILURE"
echo "Evacuation Pause（区域清理）: $EVACUATION"
echo "System.gc()（系统调用）: $SYSTEM_GC"
echo "Heap Dump（堆转储）: $HEAP_DUMP"
echo ""

# ============================================================
# 4. 堆内存使用分析
# ============================================================

echo "💾 4. 堆内存使用分析"
echo "------------------------------------------------------------"

# 提取堆内存信息
echo "堆内存使用趋势..."

# 提取最后的堆内存使用情况
HEAP_INFO=$(grep -A 2 "Heap" "$GC_LOG_FILE" 2>/dev/null | tail -10)
if [ -n "$HEAP_INFO" ]; then
    echo "$HEAP_INFO"
else
    echo "⚠️  未找到堆内存信息"
fi
echo ""

# ============================================================
# 5. 性能评估
# ============================================================

echo "✅ 5. 性能评估"
echo "------------------------------------------------------------"

# 评估GC性能
PAUSE_200MS_COUNT=0
PAUSE_500MS_COUNT=0

if [ -n "$PAUSE_TIMES" ]; then
    PAUSE_200MS_COUNT=$(echo "$PAUSE_TIMES" | awk '$1<=200 {count++} END {print count}')
    PAUSE_500MS_COUNT=$(echo "$PAUSE_TIMES" | awk '$1<=500 {count++} END {print count}')

    TOTAL_PAUSES=$PAUSE_COUNT

    if [ $TOTAL_PAUSES -gt 0 ]; then
        RATE_200MS=$((PAUSE_200MS_COUNT * 100 / TOTAL_PAUSES))
        RATE_500MS=$((PAUSE_500MS_COUNT * 100 / TOTAL_PAUSES))

        echo "GC暂停时间性能评估:"

        if [ $RATE_200MS -ge 95 ]; then
            echo "  ✅ 优秀: ${RATE_200MS}%的GC暂停时间≤200ms"
        elif [ $RATE_200MS -ge 80 ]; then
            echo "  ✔️  良好: ${RATE_200MS}%的GC暂停时间≤200ms"
        else
            echo "  ⚠️  需要优化: 仅${RATE_200MS}%的GC暂停时间≤200ms"
        fi

        if [ $RATE_500MS -ge 99 ]; then
            echo "  ✅ 优秀: ${RATE_500MS}%的GC暂停时间≤500ms"
        elif [ $RATE_500MS -ge 95 ]; then
            echo "  ✔️  良好: ${RATE_500MS}%的GC暂停时间≤500ms"
        else
            echo "  ⚠️  需要优化: 仅${RATE_500MS}%的GC暂停时间≤500ms"
        fi
    fi
else
    echo "⚠️  无法评估GC暂停时间性能"
fi

echo ""

# Full GC评估
if [ $FULL_GC_COUNT -eq 0 ]; then
    echo "Full GC频率: ✅ 优秀（未检测到Full GC）"
elif [ $FULL_GC_COUNT -lt 10 ]; then
    echo "Full GC频率: ✔️  良好（$FULL_GC_COUNT次）"
else
    echo "Full GC频率: ⚠️  需要优化（$FULL_GC_COUNT次）"
fi

echo ""

# ============================================================
# 6. 优化建议
# ============================================================

echo "💡 6. 优化建议"
echo "------------------------------------------------------------"

if [ $FULL_GC_COUNT -gt 10 ]; then
    echo "⚠️  Full GC频繁（${FULL_GC_COUNT}次），建议："
    echo "   1. 增大堆内存大小（-Xmx参数）"
    echo "   2. 调整InitiatingHeapOccupancyPercent（默认45%）"
    echo "   3. 检查是否存在内存泄漏"
    echo ""
fi

if [ -n "$PAUSE_AVG" ] && [ $(echo "$PAUSE_AVG < 200" | bc -l) -eq 0 ]; then
    echo "⚠️  平均GC暂停时间过长（${PAUSE_AVG}ms），建议："
    echo "   1. 调整MaxGCPauseMillis（当前200ms）"
    echo "   2. 增大G1HeapRegionSize（当前16m）"
    echo "   3. 检查CPU负载是否过高"
    echo ""
fi

if [ $ALLOCATION_FAILURE -gt 100 ]; then
    echo "⚠️  Allocation Failure频繁（${ALLOCATION_FAILURE}次），建议："
    echo "   1. 增大堆内存大小"
    echo "   2. 减少对象创建速率"
    echo "   3. 优化内存使用"
    echo ""
fi

echo "============================================================"
echo "✅ GC日志分析完成"
echo "============================================================"
echo ""
echo "📋 后续建议："
echo "1. 定期运行此脚本分析GC日志"
echo "2. 使用GCViewer或JClarity进行可视化分析"
echo "3. 根据分析结果调整JVM参数"
echo "4. 配置GC告警规则"
echo ""
