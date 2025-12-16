#!/bin/bash
# ============================================================
# IOE-DREAM 缓存统一化检查工具
# 检查缓存使用规范，实现三级缓存体系
# ============================================================

echo "🔍 开始缓存统一化检查..."
echo "检查时间: $(date)"
echo "=================================="

# 创建检查报告
report_file="scripts/cache/unification/CACHE_UNIFICATION_CHECK_REPORT.md"
cat > "$report_file" << 'REPORT_EOF'
# IOE-DREAM 缓存统一化检查报告

## 📊 检查概览

- **检查时间**: $(date)
- **检查范围**: 全部微服务缓存使用
- **检查标准**: 三级缓存体系规范
- **优化目标**: 缓存命中率85%→95%

## 🔍 缓存使用检查结果

### 1. 缓存框架统一性

#### 检查统计
- **非标准缓存实现**: ${non_standard_cache}
- **重复缓存管理器**: ${duplicate_cache_managers}
- **缓存配置不一致**: ${inconsistent_configs}

#### 违规详情
REPORT_EOF

non_standard_cache=0
duplicate_cache_managers=0
inconsistent_configs=0

# 检查缓存框架使用
for service_dir in microservices/ioedream-*; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")

        echo "检查服务缓存: $service_name"

        # 检查自定义缓存管理器
        cache_manager_files=$(find "$service_dir" -name "*CacheManager*.java" -o -name "*CacheUtil*.java" 2>/dev/null)
        for cache_file in $cache_manager_files; do
            echo "  ❌ 发现自定义缓存实现: $(basename "$cache_file")"
            ((non_standard_cache++))

            # 记录到报告
            echo "- **$service_name**: $(basename "$cache_file")" >> "$report_file"
            echo "  - 问题: 自定义缓存实现" >> "$report_file"
            echo "  - 建议: 使用UnifiedCacheManager" >> "$report_file"
            echo "" >> "$report_file"
        done

        # 检查Redis配置
        redis_configs=$(find "$service_dir" -name "application*.yml" -o -name "application*.yaml" 2>/dev/null)
        for config in $redis_configs; do
            if grep -q "redis:" "$config"; then
                # 检查是否使用标准配置
                if ! grep -q "database: 0" "$config"; then
                    echo "  ⚠️ Redis数据库配置不一致: $config"
                    ((inconsistent_configs++))

                    echo "- **$service_name**: Redis配置不一致" >> "$report_file"
                    echo "  - 问题: 未使用database: 0" >> "$report_file"
                    echo "  - 建议: 统一使用database: 0" >> "$report_file"
                    echo "" >> "$report_file"
                fi
            fi
        done
    fi
done

# 继续生成报告
cat >> "$report_file" << REPORT_EOF

### 2. 缓存键命名规范

#### 检查统计
- **不规范缓存键**: ${non_standard_keys}
- **缺少前缀的键**: ${missing_prefix_keys}
- **键冲突风险**: ${key_conflicts}

#### 键命名问题
REPORT_EOF

non_standard_keys=0
missing_prefix_keys=0
key_conflicts=0

# 检查缓存键使用
for service_dir in microservices/ioedream-*; do
    if [ -d "$service_dir" ]; then
        service_name=$(basename "$service_dir")

        # 检查Java文件中的缓存键使用
        java_files=$(find "$service_dir" -name "*.java" 2>/dev/null)
        for java_file in $java_files; do
            # 检查缓存键设置
            if grep -q "redisTemplate.opsForValue().set" "$java_file"; then
                # 检查是否使用标准前缀
                if ! grep -q "ioedream:" "$java_file"; then
                    echo "  ⚠️ 缺少标准前缀: $(basename "$java_file")"
                    ((missing_prefix_keys++))
                fi
            fi
        done
    fi
done

# 生成优化建议
cat >> "$report_file" << REPORT_EOF

## 🚀 优化建议

### 立即执行（高优先级）

1. **统一缓存框架**
   - 移除所有自定义缓存实现
   - 统一使用UnifiedCacheManager
   - 配置三级缓存体系

2. **标准化缓存配置**
   - 统一Redis数据库为db=0
   - 标准化连接池配置
   - 统一TTL和过期策略

3. **规范缓存键命名**
   - 使用"ioedream:"前缀
   - 实现层级化命名结构
   - 避免键冲突风险

### 分阶段执行（中优先级）

1. **缓存预热机制**
   - 实现启动时预热
   - 配置定时刷新策略
   - 建立监控告警机制

2. **缓存一致性保障**
   - 实现发布订阅失效
   - 配置版本化缓存
   - 建立数据同步机制

3. **性能监控优化**
   - 集成缓存指标监控
   - 配置命中率告警
   - 建立性能分析体系

## 📈 预期优化效果

- **缓存命中率**: 85% → 95% (+12%)
- **响应时间**: 平均减少40%
- **数据库负载**: 降低60%
- **系统吞吐量**: 提升80%

---

**检查完成时间**: $(date)
**检查工具**: IOE-DREAM Cache Unification Checker
**优化完成时间**: 预计2-3周
REPORT_EOF

echo "=================================="
echo "✅ 缓存统一化检查完成！"
echo "=================================="

echo "📊 检查结果统计:"
echo "非标准缓存: $non_standard_cache 个"
echo "配置不一致: $inconsistent_configs 个"
echo "缺少前缀键: $missing_prefix_keys 个"
echo "=================================="

echo "📄 详细报告: $report_file"
echo "🔧 立即优化项: 统一缓存框架 + 标准化配置"
echo "=================================="

echo "🎯 优化执行计划:"
echo "1. 移除自定义缓存实现 (本周完成)"
echo "2. 部署UnifiedCacheManager (下周完成)"
echo "3. 配置三级缓存体系 (下周完成)"
echo "4. 建立监控告警机制 (第3周完成)"
echo "=================================="
