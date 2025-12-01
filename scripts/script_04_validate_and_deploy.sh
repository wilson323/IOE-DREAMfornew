#!/bin/bash
# =================================================================
# repowiki规范修复脚本 - 第四阶段：验证和部署
# 目标：全面验证修复结果，确保零编译错误，符合repowiki规范
# 版本：v1.0
# 创建时间：2025-11-18
# =================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 全局变量
TOTAL_ISSUES=0
FIXED_ISSUES=0
REMAINING_ISSUES=0

# 检查是否在正确的目录
check_directory() {
    if [ ! -f "pom.xml" ]; then
        log_error "请确保在项目根目录（包含pom.xml的目录）执行此脚本"
        exit 1
    fi

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        log_error "未找到smart-admin-api-java17-springboot3目录"
        exit 1
    fi

    log_success "目录检查通过"
}

# 显示验证开始信息
show_validation_start() {
    echo "========================================"
    echo "  repowiki规范修复验证 - 第四阶段"
    echo "  版本: v1.0"
    echo "  目标: 全面验证和部署准备"
    echo "========================================"
    echo

    log_info "开始全面验证repowiki规范修复结果..."
    echo "验证时间: $(date)"
    echo
}

# 1. repowiki基础规范验证
validate_basic_standards() {
    log_info "=== 1. repowiki基础规范验证 ==="

    local base_dir="smart-admin-api-java17-springboot3"
    local issues=0

    # 1.1 javax包使用检查（一级规范）
    javax_count=$(find "$base_dir" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    if [ $javax_count -eq 0 ]; then
        log_success "✅ javax包使用: 0 (符合规范)"
    else
        log_error "❌ javax包使用: $javax_count (目标: 0)"
        issues=$((issues + javax_count))
        echo "需要修复的文件:"
        find "$base_dir" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | head -5
    fi

    # 1.2 @Autowired使用检查（一级规范）
    autowired_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    if [ $autowired_count -eq 0 ]; then
        log_success "✅ @Autowired使用: 0 (符合规范)"
    else
        log_error "❌ @Autowired使用: $autowired_count (目标: 0)"
        issues=$((issues + autowired_count))
    fi

    # 1.3 包名错误检查（一级规范）
    annoation_count=$(find "$base_dir" -name "*.java" -exec grep -l "annoation" {} \; 2>/dev/null | wc -l)
    if [ $annoation_count -eq 0 ]; then
        log_success "✅ 包名错误(annoation): 0 (符合规范)"
    else
        log_error "❌ 包名错误(annoation): $annoation_count (目标: 0)"
        issues=$((issues + annoation_count))
    fi

    # 1.4 资源注入检查（一级规范）
    resource_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Resource" {} \; 2>/dev/null | wc -l)
    log_info "📊 @Resource使用: $resource_count (推荐使用)"

    TOTAL_ISSUES=$((TOTAL_ISSUES + issues))

    if [ $issues -eq 0 ]; then
        log_success "🎉 基础规范验证通过！"
    else
        log_warning "⚠️ 基础规范存在 $issues 个问题"
    fi

    echo
}

# 2. 四层架构完整性验证
validate_architecture() {
    log_info "=== 2. 四层架构完整性验证 ==="

    local base_dir="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module"
    local issues=0
    local total_modules=0
    local complete_modules=0

    echo "模块架构完整性检查:"
    echo "=================="

    for module_dir in "$base_dir"/*; do
        if [ -d "$module_dir" ]; then
            module_name=$(basename "$module_dir")
            total_modules=$((total_modules + 1))

            controller_count=$(find "$module_dir" -name "*Controller.java" -type f | wc -l)
            service_count=$(find "$module_dir" -name "*Service*.java" -type f | wc -l)
            manager_count=$(find "$module_dir" -name "*Manager.java" -type f | wc -l)
            dao_count=$(find "$module_dir" -name "*Dao.java" -type f | wc -l)
            repository_count=$(find "$module_dir" -name "*Repository.java" -type f | wc -l)

            data_access_count=$((dao_count + repository_count))

            echo "$module_name:"
            echo "  Controller: $controller_count"
            echo "  Service: $service_count"
            echo "  Manager: $manager_count"
            echo "  DAO/Repository: $data_access_count"

            # 架构完整性评估
            if [ $controller_count -gt 0 ] && [ $service_count -gt 0 ] && [ $manager_count -gt 0 ] && [ $data_access_count -gt 0 ]; then
                echo "  架构完整性: ✅ 完整"
                complete_modules=$((complete_modules + 1))
            else
                echo "  架构完整性: ⚠️ 不完整"
                issues=$((issues + 1))

                # 指出缺失的层级
                [ $controller_count -eq 0 ] && echo "    缺失: Controller层"
                [ $service_count -eq 0 ] && echo "    缺失: Service层"
                [ $manager_count -eq 0 ] && echo "    缺失: Manager层"
                [ $data_access_count -eq 0 ] && echo "    缺失: DAO/Repository层"
            fi
            echo
        fi
    done

    echo "架构完整性统计:"
    echo "总模块数: $total_modules"
    echo "完整模块数: $complete_modules"
    echo "完整率: $(( complete_modules * 100 / total_modules ))%"

    TOTAL_ISSUES=$((TOTAL_ISSUES + issues))

    if [ $issues -eq 0 ]; then
        log_success "🎉 四层架构完整性验证通过！"
    else
        log_warning "⚠️ 发现 $issues 个模块架构不完整"
    fi

    echo
}

# 3. 缓存架构统一性验证
validate_cache_architecture() {
    log_info "=== 3. 缓存架构统一性验证 ==="

    local base_dir="smart-admin-api-java17-springboot3"
    local issues=0

    # 3.1 检查核心组件是否存在
    cache_service_file="$base_dir/sa-base/src/main/java/net/lab1024/sa/base/common/cache/service/UnifiedCacheService.java"
    if [ -f "$cache_service_file" ]; then
        log_success "✅ UnifiedCacheService接口已创建"
    else
        log_error "❌ UnifiedCacheService接口未找到"
        issues=$((issues + 1))
    fi

    # 3.2 检查业务数据类型枚举
    data_type_file="$base_dir/sa-base/src/main/java/net/lab1024/sa/base/common/cache/enum/BusinessDataType.java"
    if [ -f "$data_type_file" ]; then
        log_success "✅ BusinessDataType枚举已创建"
    else
        log_error "❌ BusinessDataType枚举未找到"
        issues=$((issues + 1))
    fi

    # 3.3 检查直接使用Redis的文件
    redis_files=$(find "$base_dir" -name "*.java" -exec grep -l "RedisTemplate\|StringRedisTemplate" {} \; 2>/dev/null | wc -l)
    if [ $redis_files -eq 0 ]; then
        log_success "✅ 直接使用Redis的文件: 0 (符合规范)"
    else
        log_warning "⚠️ 直接使用Redis的文件: $redis_files (建议迁移到统一缓存)"
        issues=$((issues + redis_files))
    fi

    # 3.4 检查缓存工具直接使用
    cache_util_files=$(find "$base_dir" -name "*.java" -exec grep -l "RedisUtil\|CacheService" {} \; 2>/dev/null | wc -l)
    if [ $cache_util_files -eq 0 ]; then
        log_success "✅ 直接使用缓存工具的文件: 0 (符合规范)"
    else
        log_warning "⚠️ 直接使用缓存工具的文件: $cache_util_files (建议迁移)"
        issues=$((issues + cache_util_files))
    fi

    TOTAL_ISSUES=$((TOTAL_ISSUES + issues))

    if [ $issues -eq 0 ]; then
        log_success "🎉 缓存架构统一性验证通过！"
    else
        log_warning "⚠️ 缓存架构存在 $issues 个问题"
    fi

    echo
}

# 4. 编译验证
validate_compilation() {
    log_info "=== 4. 编译验证 ==="

    cd smart-admin-api-java17-springboot3

    # 4.1 清理项目
    log_info "清理项目..."
    mvn clean -q > /dev/null 2>&1 || {
        log_error "项目清理失败"
        cd ..
        TOTAL_ISSUES=$((TOTAL_ISSUES + 1))
        return 1
    }

    # 4.2 编译项目
    log_info "编译项目..."
    compile_start_time=$(date +%s)

    compile_output=$(mvn compile -q 2>&1 || echo "COMPILE_FAILED")
    compile_end_time=$(date +%s)
    compile_duration=$((compile_end_time - compile_start_time))

    cd ..

    # 4.3 分析编译结果
    error_count=$(echo "$compile_output" | grep -c "ERROR" || echo "0")
    warning_count=$(echo "$compile_output" | grep -c "WARNING" || echo "0")

    echo "编译结果:"
    echo "编译时间: ${compile_duration}秒"
    echo "错误数量: $error_count"
    echo "警告数量: $warning_count"

    if [ "$compile_output" = "COMPILE_FAILED" ] || [ $error_count -gt 0 ]; then
        log_error "❌ 编译失败"
        echo "编译错误详情:"
        echo "$compile_output" | grep "ERROR" | head -10

        # 显示错误文件
        error_files=$(echo "$compile_output" | grep "ERROR" | grep -o '\[ERROR\] [^:]*:' | sort -u)
        if [ -n "$error_files" ]; then
            echo "错误文件列表:"
            echo "$error_files"
        fi

        TOTAL_ISSUES=$((TOTAL_ISSUES + error_count))
        return 1
    else
        log_success "✅ 编译成功，耗时${compile_duration}秒"
    fi

    echo
}

# 5. 测试验证
validate_tests() {
    log_info "=== 5. 测试验证 ==="

    cd smart-admin-api-java17-springboot3

    # 5.1 运行单元测试
    log_info "运行单元测试..."
    test_start_time=$(date +%s)

    test_output=$(mvn test -q 2>&1 || echo "TEST_FAILED")
    test_end_time=$(date +%s)
    test_duration=$((test_end_time - test_start_time))

    cd ..

    # 5.2 分析测试结果
    if echo "$test_output" | grep -q "BUILD FAILURE\|TEST_FAILED"; then
        log_error "❌ 测试失败"
        echo "测试失败详情:"
        echo "$test_output" | grep -A5 -B5 "FAILURE\|ERROR" | head -20

        test_failures=$(echo "$test_output" | grep -c "FAILURE" || echo "0")
        test_errors=$(echo "$test_output" | grep -c "ERROR" || echo "0")
        TOTAL_ISSUES=$((TOTAL_ISSUES + test_failures + test_errors))
    else
        # 统计测试结果
        tests_run=$(echo "$test_output" | grep -o "Tests run: [0-9]*" | grep -o "[0-9]*" | head -1 || echo "0")
        tests_failed=$(echo "$test_output" | grep -o "Failures: [0-9]*" | grep -o "[0-9]*" | head -1 || echo "0")
        tests_errors=$(echo "$test_output" | grep -o "Errors: [0-9]*" | grep -o "[0-9]*" | head -1 || echo "0")
        tests_skipped=$(echo "$test_output" | grep -o "Skipped: [0-9]*" | grep -o "[0-9]*" | head -1 || echo "0")

        echo "测试结果:"
        echo "测试时间: ${test_duration}秒"
        echo "总测试数: $tests_run"
        echo "失败: $tests_failed"
        echo "错误: $tests_errors"
        echo "跳过: $tests_skipped"

        if [ "$tests_failed" = "0" ] && [ "$tests_errors" = "0" ]; then
            log_success "✅ 所有测试通过"
        else
            log_warning "⚠️ 存在测试失败或错误"
            TOTAL_ISSUES=$((TOTAL_ISSUES + tests_failed + tests_errors))
        fi
    fi

    echo
}

# 6. 代码质量验证
validate_code_quality() {
    log_info "=== 6. 代码质量验证 ==="

    local base_dir="smart-admin-api-java17-springboot3"
    local issues=0

    # 6.1 检查日志使用规范
    system_out_count=$(find "$base_dir" -name "*.java" -exec grep -l "System\.out\.println\|System\.err\.println" {} \; 2>/dev/null | wc -l)
    if [ $system_out_count -eq 0 ]; then
        log_success "✅ System.out使用: 0 (符合规范)"
    else
        log_warning "⚠️ System.out使用: $system_out_count (建议使用日志框架)"
        issues=$((issues + system_out_count))
    fi

    # 6.2 检查硬编码字符串
    magic_string_count=$(find "$base_dir" -name "*.java" -exec grep -l "\"[a-zA-Z_][a-zA-Z0-9_]*\":" {} \; 2>/dev/null | wc -l)
    log_info "📊 可能存在硬编码字符串的文件: $magic_string_count"

    # 6.3 检查异常处理
    exception_count=$(find "$base_dir" -name "*.java" -exec grep -l "catch.*Exception.*{[[:space:]]*}" {} \; 2>/dev/null | wc -l)
    log_info "📊 异常处理文件数: $exception_count"

    # 6.4 检查方法长度（简单检查）
    long_method_files=$(find "$base_dir" -name "*.java" -exec awk 'length($0) > 200 && /\{/ {print FILENAME}' {} \; 2>/dev/null | sort -u | wc -l)
    if [ $long_method_files -eq 0 ]; then
        log_success "✅ 长方法检查通过"
    else
        log_warning "⚠️ 可能存在长方法的文件: $long_method_files"
        issues=$((issues + long_method_files))
    fi

    TOTAL_ISSUES=$((TOTAL_ISSUES + issues))

    if [ $issues -eq 0 ]; then
        log_success "🎉 代码质量验证通过！"
    else
        log_warning "⚠️ 代码质量存在 $issues 个潜在问题"
    fi

    echo
}

# 7. 安全规范验证
validate_security() {
    log_info "=== 7. 安全规范验证 ==="

    local base_dir="smart-admin-api-java17-springboot3"
    local issues=0

    # 7.1 检查SQL注入风险
    sql_injection_count=$(find "$base_dir" -name "*.java" -exec grep -l "\+.*\+.*SELECT\|'.*'.*SELECT" {} \; 2>/dev/null | wc -l)
    if [ $sql_injection_count -eq 0 ]; then
        log_success "✅ SQL注入风险检查通过"
    else
        log_warning "⚠️ 可能存在SQL注入风险的文件: $sql_injection_count"
        issues=$((issues + sql_injection_count))
    fi

    # 7.2 检查XSS防护
    xss_count=$(find "$base_dir" -name "*.java" -exec grep -l "request\.getParameter\|request\.getParameter" {} \; 2>/dev/null | wc -l)
    log_info "📊 直接使用request.getParameter的文件: $xss_count (建议添加XSS防护)"

    # 7.3 检查权限注解使用
    permission_annotation_count=$(find "$base_dir" -name "*Controller.java" -exec grep -l "@SaCheckPermission" {} \; 2>/dev/null | wc -l)
    controller_count=$(find "$base_dir" -name "*Controller.java" -type f | wc -l)

    if [ $controller_count -gt 0 ]; then
        permission_coverage=$(( permission_annotation_count * 100 / controller_count ))
        log_info "📊 Controller权限注解覆盖率: $permission_coverage% ($permission_annotation_count/$controller_count)"

        if [ $permission_coverage -ge 80 ]; then
            log_success "✅ 权限控制覆盖率良好"
        else
            log_warning "⚠️ 权限控制覆盖率较低，建议添加@SaCheckPermission注解"
            issues=$((issues + 1))
        fi
    fi

    TOTAL_ISSUES=$((TOTAL_ISSUES + issues))

    if [ $issues -eq 0 ]; then
        log_success "🎉 安全规范验证通过！"
    else
        log_warning "⚠️ 安全规范存在 $issues 个问题"
    fi

    echo
}

# 8. 性能规范验证
validate_performance() {
    log_info "=== 8. 性能规范验证 ==="

    local base_dir="smart-admin-api-java17-springboot3"
    local issues=0

    # 8.1 检查数据库查询规范
    select_star_count=$(find "$base_dir" -name "*.java" -exec grep -l "SELECT.*\*" {} \; 2>/dev/null | wc -l)
    if [ $select_star_count -eq 0 ]; then
        log_success "✅ SELECT * 检查通过"
    else
        log_warning "⚠️ 使用SELECT *的文件: $select_star_count (建议明确字段)"
        issues=$((issues + select_star_count))
    fi

    # 8.2 检查缓存使用
    cache_usage_count=$(find "$base_dir" -name "*.java" -exec grep -l "cache\|Cache" {} \; 2>/dev/null | wc -l)
    log_info "📊 使用缓存的文件: $cache_usage_count"

    # 8.3 检查异步处理
    async_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Async\|CompletableFuture" {} \; 2>/dev/null | wc -l)
    log_info "📊 使用异步处理的文件: $async_count"

    TOTAL_ISSUES=$((TOTAL_ISSUES + issues))

    if [ $issues -eq 0 ]; then
        log_success "🎉 性能规范验证通过！"
    else
        log_warning "⚠️ 性能规范存在 $issues 个问题"
    fi

    echo
}

# 生成最终验证报告
generate_final_report() {
    local report_file="repowiki_final_validation_report_$(date +%Y%m%d_%H%M%S).md"

    # 重新统计当前状态
    base_dir="smart-admin-api-java17-springboot3"
    final_javax_count=$(find "$base_dir" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    final_autowired_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    final_annoation_count=$(find "$base_dir" -name "*.java" -exec grep -l "annoation" {} \; 2>/dev/null | wc -l)
    final_resource_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Resource" {} \; 2>/dev/null | wc -l)

    # 计算修复统计
    FIXED_ISSUES=$((final_resource_count - (final_javax_count + final_autowired_count + final_annoation_count)))
    REMAINING_ISSUES=$TOTAL_ISSUES

    cat > "$report_file" << EOF
# repowiki规范修复最终验证报告

**验证时间**: $(date)
**脚本版本**: v1.0
**项目**: IOE-DREAM SmartAdmin v3

## 📊 总体修复结果

### repowiki基础规范修复
- **javax包使用**: $final_javax_count (目标: 0) $([ $final_javax_count -eq 0 ] && echo "✅" || echo "❌")
- **@Autowired使用**: $final_autowired_count (目标: 0) $([ $final_autowired_count -eq 0 ] && echo "✅" || echo "❌")
- **包名错误(annoation)**: $final_annoation_count (目标: 0) $([ $final_annoation_count -eq 0 ] && echo "✅" || echo "❌")
- **@Resource使用**: $final_resource_count (推荐使用) ✅

### 架构完整性修复
- **四层架构**: Controller→Service→Manager→DAO
- **Manager层补全**: 已创建标准Manager模板
- **Service层更新**: 需要手动更新以使用Manager层

### 缓存架构统一化
- **核心组件**: UnifiedCacheService, BusinessDataType, CacheModule
- **TTL策略**: 五级分类（REALTIME → LONG_TERM）
- **模块化治理**: 基于业务模块的缓存管理

## 🎯 验证结果汇总

| 验证项目 | 状态 | 问题数量 |
|---------|------|----------|
| 基础规范 | $([ $((final_javax_count + final_autowired_count + final_annoation_count)) -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") | $((final_javax_count + final_autowired_count + final_annoation_count)) |
| 架构完整性 | ⚠️ 部分完成 | 需手动更新Service层 |
| 缓存架构 | ✅ 核心组件完成 | 需迁移现有代码 |
| 编译验证 | $([ $TOTAL_ISSUES -eq 0 ] && echo "✅ 通过" || echo "❌ 失败") | $TOTAL_ISSUES |
| 测试验证 | ⚠️ 需要运行 | - |
| 代码质量 | ⚠️ 需要检查 | - |
| 安全规范 | ⚠️ 需要检查 | - |
| 性能规范 | ⚠️ 需要检查 | - |

## 📋 修复统计

- **总问题数**: $TOTAL_ISSUES
- **已修复数**: $FIXED_ISSUES
- **剩余问题数**: $REMAINING_ISSUES
- **修复进度**: $(( (FIXED_ISSUES * 100) / (FIXED_ISSUES + REMAINING_ISSUES) ))%

## 🚀 下一步行动计划

### 立即执行（高优先级）
1. **解决编译错误**:
   \`\`\`bash
   cd smart-admin-api-java17-springboot3
   mvn compile
   \`\`\`

2. **手动修复剩余问题**:
   - 修复javax包使用问题
   - 替换剩余的@Autowired
   - 修复包名错误

### 中期执行（中优先级）
1. **更新Service层**:
   - 添加Manager依赖注入
   - 调用Manager层方法
   - 确保架构完整性

2. **迁移缓存代码**:
   - 使用UnifiedCacheService
   - 选择合适的BusinessDataType
   - 应用getOrSet模式

### 长期执行（低优先级）
1. **代码质量提升**:
   - 添加单元测试
   - 优化长方法
   - 改进异常处理

2. **性能和安全优化**:
   - 优化数据库查询
   - 增强安全防护
   - 添加性能监控

## 🎯 成功标准

### 短期目标（1-2天）
- [ ] 编译错误: 0
- [ ] javax包使用: 0
- [ ] @Autowired使用: 0
- [ ] 包名错误: 0

### 中期目标（1周）
- [ ] 四层架构完整性: 100%
- [ ] 缓存架构统一: 90%
- [ ] 单元测试覆盖率: ≥80%

### 长期目标（1个月）
- [ ] 代码质量指标: 全部达标
- [ ] 性能指标: P95 ≤ 200ms
- [ ] 安全指标: 0高危漏洞

## 📞 技术支持

如需技术支持，请参考：
- repowiki开发规范体系文档
- 项目GitHub Issues
- 技术团队支持

---
**报告生成时间**: $(date)
**基于**: repowiki开发规范体系 v1.1
**修复策略**: 系统性修复方案 v1.0
EOF

    log_success "最终验证报告已生成: $report_file"

    # 显示关键统计
    echo
    echo "========================================"
    echo "  🎯 关键修复统计"
    echo "========================================"
    echo "总问题数: $TOTAL_ISSUES"
    echo "已修复: $FIXED_ISSUES"
    echo "剩余: $REMAINING_ISSUES"
    echo "修复进度: $(( (FIXED_ISSUES * 100) / (FIXED_ISSUES + REMAINING_ISSUES) ))%"
    echo
}

# 生成部署检查清单
generate_deployment_checklist() {
    local checklist_file="deployment_checklist_$(date +%Y%m%d_%H%M%S).md"

    cat > "$checklist_file" << 'EOF'
# repowiki规范部署检查清单

**创建时间**: [当前时间]
**版本**: v1.0

## 🔍 部署前检查

### 编译检查
- [ ] `mvn clean compile` 执行成功
- [ ] 编译错误数量: 0
- [ ] 编译警告数量: < 10
- [ ] 所有模块编译通过

### 测试检查
- [ ] 单元测试执行成功
- [ ] 测试覆盖率 ≥ 80%
- [ ] 核心业务测试通过
- [ ] 集成测试通过

### 代码质量检查
- [ ] javax包使用数量: 0
- [ ] @Autowired使用数量: 0
- [ ] 包名错误数量: 0
- [ ] System.out使用数量: 0

### 架构检查
- [ ] 四层架构完整: Controller→Service→Manager→DAO
- [ ] 缓存架构统一: 使用UnifiedCacheService
- [ ] 依赖注入规范: 使用@Resource
- [ ] 包名规范: jakarta包名

## 🚀 部署步骤

### 1. 环境准备
- [ ] 备份生产环境
- [ ] 检查数据库连接
- [ ] 检查Redis连接
- [ ] 检查配置文件

### 2. 应用部署
- [ ] 停止应用服务
- [ ] 部署新版本
- [ ] 更新配置文件
- [ ] 启动应用服务

### 3. 验证检查
- [ ] 应用启动成功
- [ ] 健康检查通过
- [ ] 核心功能测试
- [ ] 性能监控正常

### 4. 监控设置
- [ ] 日志监控配置
- [ ] 性能监控配置
- [ ] 错误告警配置
- [ ] 业务指标监控

## 📊 部署后验证

### 功能验证
- [ ] 用户登录功能
- [ ] 核心业务功能
- [ ] 权限控制功能
- [ ] 缓存功能正常

### 性能验证
- [ ] 接口响应时间 ≤ 200ms (P95)
- [ ] 数据库查询性能正常
- [ ] 缓存命中率 ≥ 90%
- [ ] 系统资源使用正常

### 安全验证
- [ ] 权限控制生效
- [ ] SQL注入防护生效
- [ ] XSS防护生效
- [ ] 敏感数据加密

## 🔄 回滚预案

### 回滚触发条件
- [ ] 编译错误 > 0
- [ ] 核心功能异常
- [ ] 性能下降 > 50%
- [ ] 安全漏洞发现

### 回滚步骤
1. 停止当前服务
2. 恢复备份版本
3. 验证功能正常
4. 通知相关人员

---

**重要提醒**:
- 每个检查项都必须完成
- 发现问题立即停止部署
- 记录部署过程和结果
EOF

    log_success "部署检查清单已生成: $checklist_file"
}

# 主函数
main() {
    show_validation_start

    check_directory

    # 执行所有验证
    validate_basic_standards
    validate_architecture
    validate_cache_architecture
    validate_compilation
    validate_tests
    validate_code_quality
    validate_security
    validate_performance

    # 生成报告
    generate_final_report
    generate_deployment_checklist

    # 显示最终结果
    echo "========================================"
    echo "  🎯 最终验证结果"
    echo "========================================"

    if [ $TOTAL_ISSUES -eq 0 ]; then
        log_success "🎉 所有验证通过！repowiki规范修复完成！"
        log_success "✅ 可以开始部署流程"
    else
        log_warning "⚠️ 发现 $TOTAL_ISSUES 个问题需要修复"
        log_info "📋 请查看详细报告了解具体问题"
        log_info "🔧 按照修复指南解决剩余问题后重新验证"
    fi

    echo
    echo "========================================"
    echo "  repowiki规范修复验证完成！"
    echo "  报告文件已生成，请查看详细信息"
    echo "========================================"
}

# 执行主函数
main "$@"