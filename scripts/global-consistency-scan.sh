#!/bin/bash

# =============================================================================
# IOE-DREAM 全局一致性扫描脚本
# 功能: 全面扫描项目全局一致性问题，生成详细的诊断报告
# 作者: System Optimization Specialist
# 版本: v1.0.0
# 创建时间: 2025-11-18
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"
REPORT_DIR="./reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="${REPORT_DIR}/global_consistency_report_${TIMESTAMP}.md"

# 创建报告目录
mkdir -p "$REPORT_DIR"

# 打印标题
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}🔍 IOE-DREAM 全局一致性扫描报告${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${CYAN}扫描时间: $(date)${NC}"
    echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
    echo -e "${CYAN}报告文件: $REPORT_FILE${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

# 初始化报告
init_report() {
    cat > "$REPORT_FILE" << EOF
# IOE-DREAM 全局一致性扫描报告

> **扫描时间**: $(date '+%Y-%m-%d %H:%M:%S')
> **扫描版本**: v1.0.0
> **扫描引擎**: System Optimization Specialist

## 📊 扫执行摘要

EOF
}

# 统计函数
count_files() {
    local pattern=$1
    find "$PROJECT_ROOT" -name "*.java" -exec grep -l "$pattern" {} \; | wc -l
}

# 1. 包结构一致性检查
check_package_consistency() {
    echo -e "${YELLOW}📦 1. 包结构一致性检查${NC}"

    local annotation_errors=$(count_files "annoation")
    local javax_errors=$(count_files "javax\.")
    local autowired_errors=$(count_files "@Autowired")

    echo -e "   - 包名错误 (annoation→annotation): ${RED}$annotation_errors${NC} 个文件"
    echo -e "   - Jakarta未迁移 (javax→jakarta): ${RED}$javax_errors${NC} 个文件"
    echo -e "   - 依赖注入不统一 (@Autowired→@Resource): ${RED}$autowired_errors${NC} 个文件"

    # 写入报告
    cat >> "$REPORT_FILE" << EOF

## 📦 包结构一致性

| 检查项 | 问题数量 | 状态 |
|--------|----------|------|
| 包名错误 (annoation→annotation) | $annotation_errors | $([ $annotation_errors -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") |
| Jakarta未迁移 (javax→jakarta) | $javax_errors | $([ $javax_errors -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") |
| 依赖注入不统一 (@Autowired) | $autowired_errors | $([ $autowired_errors -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") |

### 🔍 详细问题清单

EOF

    if [ $annotation_errors -gt 0 ]; then
        echo -e "${RED}包名错误文件:${NC}" >> "$REPORT_FILE"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "annoation" {} \; >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi

    if [ $javax_errors -gt 0 ]; then
        echo -e "${RED}Jakarta未迁移文件:${NC}" >> "$REPORT_FILE"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "javax\." {} \; >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi

    if [ $autowired_errors -gt 0 ]; then
        echo -e "${RED}依赖注入不统一文件:${NC}" >> "$REPORT_FILE"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \; >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi
}

# 2. 架构一致性检查
check_architecture_consistency() {
    echo -e "${YELLOW}🏗️ 2. 架构一致性检查${NC}"

    local java_files=$(find "$PROJECT_ROOT" -name "*.java" | wc -l)
    local slf4j_files=$(count_files "@Slf4j")
    local manual_logger_files=$(count_files "LoggerFactory.getLogger\|private static final Logger")

    echo -e "   - Java文件总数: $java_files"
    echo -e "   - 使用@Slf4j: ${GREEN}$slf4j_files${NC} 个文件"
    echo -e "   - 手动Logger: ${YELLOW}$manual_logger_files${NC} 个文件"

    # 计算日志统一性
    local total_log_files=$((slf4j_files + manual_logger_files))
    local log_consistency_rate=0
    if [ $total_log_files -gt 0 ]; then
        log_consistency_rate=$((slf4j_files * 100 / total_log_files))
    fi

    echo -e "   - 日志统一性: ${CYAN}${log_consistency_rate}%${NC}"

    # 写入报告
    cat >> "$REPORT_FILE" << EOF

## 🏗️ 架构一致性

### 日志框架统一性

| 指标 | 数量 | 百分比 |
|------|------|--------|
| Java文件总数 | $java_files | 100% |
| 使用@Slf4j | $slf4j_files | ${log_consistency_rate}% |
| 手动Logger | $manual_logger_files | $((100 - log_consistency_rate))% |

### 🎯 日志统一性评估
EOF

    if [ $log_consistency_rate -ge 90 ]; then
        echo -e "✅ ${GREEN}优秀${NC} - 日志框架高度统一" >> "$REPORT_FILE"
    elif [ $log_consistency_rate -ge 70 ]; then
        echo -e "⚠️  ${YELLOW}良好${NC} - 日志框架基本统一，建议优化" >> "$REPORT_FILE"
    else
        echo -e "❌ ${RED}需改进${NC} - 日志框架不统一，存在维护风险" >> "$REPORT_FILE"
    fi

    echo "" >> "$REPORT_FILE"
}

# 3. 缓存架构一致性检查
check_cache_consistency() {
    echo -e "${YELLOW}💾 3. 缓存架构一致性检查${NC}"

    local cache_service_files=$(count_files "CacheService")
    local cache_manager_files=$(count_files "BaseCacheManager\|UnifiedCacheManager")
    local cache_files_total=$((cache_service_files + cache_manager_files))

    echo -e "   - 废弃CacheService: ${RED}$cache_service_files${NC} 个文件"
    echo -e "   - 标准CacheManager: ${GREEN}$cache_manager_files${NC} 个文件"

    # 缓存架构一致性
    local cache_consistency_rate=0
    if [ $cache_files_total -gt 0 ]; then
        cache_consistency_rate=$((cache_manager_files * 100 / cache_files_total))
    fi

    echo -e "   - 缓存架构统一性: ${CYAN}${cache_consistency_rate}%${NC}"

    # 写入报告
    cat >> "$REPORT_FILE" << EOF

## 💾 缓存架构一致性

### 缓存实现使用情况

| 缓存实现 | 使用数量 | 状态 |
|----------|----------|------|
| 标准CacheManager | $cache_manager_files | $([ $cache_manager_files -gt 0 ] && echo "✅ 推荐" || echo "⚠️ 待检查") |
| 废弃CacheService | $cache_service_files | $([ $cache_service_files -eq 0 ] && echo "✅ 无使用" || echo "❌ 需迁移") |

### 🎯 缓存架构评估
EOF

    if [ $cache_consistency_rate -ge 90 ]; then
        echo -e "✅ ${GREEN}优秀${NC} - 缓存架构高度统一" >> "$REPORT_FILE"
    elif [ $cache_consistency_rate -ge 70 ]; then
        echo -e "⚠️  ${YELLOW}良好${NC} - 缓存架构基本统一" >> "$REPORT_FILE"
    else
        echo -e "❌ ${RED}需改进${NC} - 缓存架构不统一" >> "$REPORT_FILE"
    fi

    if [ $cache_service_files -gt 0 ]; then
        echo -e "${RED}需要迁移的CacheService文件:${NC}" >> "$REPORT_FILE"
        find "$PROJECT_ROOT" -name "*.java" -exec grep -l "CacheService" {} \; >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi
}

# 4. 编译错误统计
check_compilation_errors() {
    echo -e "${YELLOW}🔧 4. 编译错误统计${NC}"

    cd "$PROJECT_ROOT" > /dev/null 2>&1 || {
        echo -e "${RED}错误: 无法切换到项目目录 $PROJECT_ROOT${NC}"
        return 1
    }

    echo -e "   - 正在编译项目..."
    local compilation_result=0
    local error_count=0
    local warning_count=0

    if mvn clean compile -q > compilation_output.log 2>&1; then
        echo -e "   - ${GREEN}编译成功${NC}"
        compilation_result=1
    else
        echo -e "   - ${RED}编译失败${NC}"
        error_count=$(grep -c "ERROR" compilation_output.log 2>/dev/null || echo "0")
        warning_count=$(grep -c "WARNING" compilation_output.log 2>/dev/null || echo "0")
        echo -e "   - 错误数量: ${RED}$error_count${NC}"
        echo -e "   - 警告数量: ${YELLOW}$warning_count${NC}"
    fi

    # 写入报告
    cat >> "$REPORT_FILE" << EOF

## 🔧 编译状态

### Maven编译结果

| 项目 | 状态 | 错误数 | 警告数 |
|------|------|--------|--------|
| smart-admin-api-java17-springboot3 | $([ $compilation_result -eq 1 ] && echo "✅ 成功" || echo "❌ 失败") | $error_count | $warning_count |

### 📈 编译健康度评估
EOF

    if [ $compilation_result -eq 1 ] && [ $error_count -eq 0 ]; then
        echo -e "🎉 ${GREEN}编译完美通过，无任何错误或警告！${NC}" >> "$REPORT_FILE"
    elif [ $error_count -lt 50 ]; then
        echo -e "⚠️  ${YELLOW}编译存在少量错误，需要修复${NC}" >> "$REPORT_FILE"
    else
        echo -e "❌ ${RED}编译存在大量错误，需要立即修复${NC}" >> "$REPORT_FILE"
    fi

    echo "" >> "$REPORT_FILE"
}

# 5. 技术债务评估
assess_technical_debt() {
    echo -e "${PURPLE}📊 5. 技术债务评估${NC}"

    # 计算总体一致性分数
    local package_score=$((100 - (annotation_errors + javax_errors + autowired_errors) * 2))
    local arch_score=$log_consistency_rate
    local cache_score=$cache_consistency_rate
    local compile_score=$((100 - error_count / 10))

    # 加权平均
    local overall_score=$((package_score * 30 + arch_score * 30 + cache_score * 20 + compile_score * 20))
    overall_score=$((overall_score / 100))

    echo -e "   - 包结构一致性: ${CYAN}${package_score}%${NC}"
    echo -e "   - 架构一致性: ${CYAN}${arch_score}%${NC}"
    echo -e "   - 缓存一致性: ${CYAN}${cache_score}%${NC}"
    echo -e "   - 编译状态: ${CYAN}${compile_score}%${NC}"
    echo -e "   - ${PURPLE}总体健康度: ${CYAN}${overall_score}%${NC}"

    # 写入报告
    cat >> "$REPORT_FILE" << EOF

## 📊 技术债务评估

### 整体健康度评分

| 维度 | 评分 | 权重 | 加权分数 |
|------|------|------|----------|
| 包结构一致性 | ${package_score}% | 30% | $((package_score * 30 / 100))% |
| 架构一致性 | ${arch_score}% | 30% | $((arch_score * 30 / 100))% |
| 缓存一致性 | ${cache_score}% | 20% | $((cache_score * 20 / 100))% |
| 编译状态 | ${compile_score}% | 20% | $((compile_score * 20 / 100))% |
| **总体健康度** | **${overall_score}%** | **100%** | **${overall_score}%** |

### 🎯 评级标准
EOF

    if [ $overall_score -ge 90 ]; then
        echo -e "🏆 ${GREEN}A级 (优秀)${NC}" >> "$REPORT_FILE"
        echo -e "项目质量极高，技术债务极少" >> "$REPORT_FILE"
    elif [ $overall_score -ge 80 ]; then
        echo -e "🥇 ${GREEN}B级 (良好)${NC}" >> "$REPORT_FILE"
        echo -e "项目质量良好，存在少量技术债务" >> "$REPORT_FILE"
    elif [ $overall_score -ge 70 ]; then
        echo -e "🥈 ${YELLOW}C级 (一般)${NC}" >> "$REPORT_FILE"
        echo -e "项目质量一般，需要关注技术债务" >> "$REPORT_FILE"
    elif [ $overall_score -ge 60 ]; then
        echo -e "🥉 ${ORANGE}D级 (需改进)${NC}" >> "$REPORT_FILE"
        echo -e "项目质量需要改进，技术债务较重" >> "$REPORT_FILE"
    else
        echo -e "💣 ${RED}E级 (严重)${NC}" >> "$REPORT_FILE"
        echo -e "项目质量严重，技术债务极重，需要立即处理" >> "$REPORT_FILE"
    fi
}

# 6. 修复建议生成
generate_fix_suggestions() {
    echo -e "${CYAN}🛠️ 6. 修复建议生成${NC}"

    cat >> "$REPORT_FILE" << EOF

## 🛠️ 修复建议

### 🚨 高优先级修复 (立即执行)

#### 1. 包结构统一化
\`\`\`bash
# 修复包名错误
find . -name "*.java" -exec sed -i 's/annoation/annotation/g' {} \;

# 迁移Jakarta包名
find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;

# 统一依赖注入
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
\`\`\`

#### 2. 缓存架构迁移
\`\`\`java
// 替换废弃的CacheService
// @Resource
// private CacheService cacheService;  // ❌ 删除

@Resource
private BaseCacheManager cacheManager;  // ✅ 添加
\`\`\`

#### 3. 日志框架统一化
\`\`\`java
// 统一使用Lombok @Slf4j
@Slf4j  // ✅ 在类上添加
@Service
public class SampleService {
    public void doSomething() {
        log.info("执行操作");  // ✅ 使用log实例
    }
}
\`\`\`

### 📋 中优先级优化 (1周内执行)

#### 1. 代码质量提升
- 统一代码注释格式
- 完善异常处理
- 优化方法复杂度

#### 2. 架构完善
- 四层架构完整性检查
- 跨层访问违规修复
- 循环依赖检测

#### 3. 测试覆盖率
- 单元测试编写
- 集成测试完善
- 代码覆盖率提升

### 🎯 低优先级规划 (1个月内)

#### 1. 性能优化
- 数据库查询优化
- 缓存策略优化
- JVM参数调优

#### 2. 监控完善
- 应用性能监控
- 业务指标监控
- 异常告警配置

### 🔧 自动化工具推荐

#### 1. 代码质量检查
\`\`\`bash
# SonarQube集成
mvn sonar:sonar

# Checkstyle规则检查
mvn checkstyle:check
\`\`\`

#### 2. 持续集成配置
\`\`\`yaml
# .github/workflows/quality-gate.yml
name: Quality Gate
on: [push, pull_request]
jobs:
  quality-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run Quality Check
        run: |
          mvn clean compile
          mvn checkstyle:check
          mvn sonar:sonar
\`\`\`

---

EOF
}

# 7. 生成最终统计
generate_final_summary() {
    echo -e "${GREEN}✅ 扫描完成！${NC}"
    echo -e "${GREEN}📄 详细报告: $REPORT_FILE${NC}"
    echo -e "${GREEN}🌐 可在浏览器中查看完整报告${NC}"

    # 生成控制台摘要
    echo ""
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}📊 全局一致性扫描摘要${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${CYAN}包结构问题: $((annotation_errors + javax_errors + autowired_errors)) 个${NC}"
    echo -e "${CYAN}编译错误: $error_count 个${NC}"
    echo -e "${CYAN}日志统一性: ${log_consistency_rate}%${NC}"
    echo -e "${CYAN}缓存统一性: ${cache_consistency_rate}%${NC}"
    echo -e "${CYAN}总体健康度: ${overall_score}%${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

# 主执行流程
main() {
    print_header
    init_report

    check_package_consistency
    check_architecture_consistency
    check_cache_consistency
    check_compilation_errors
    assess_technical_debt
    generate_fix_suggestions
    generate_final_summary
}

# 执行主函数
main "$@"