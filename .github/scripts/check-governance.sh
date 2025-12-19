#!/bin/bash
# P4：工程治理检查脚本
# 验证企业级编码规范和最佳实践

set -euo pipefail

echo "🔍 开始P4：工程治理检查..."

# 日志文件
LOG_FILE="governance-violations.log"
echo "IOE-DREAM 工程治理检查报告" > "$LOG_FILE"
echo "检查时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "======================================" >> "$LOG_FILE"

VIOLATION_COUNT=0
WARNING_COUNT=0
MAX_VIOLATIONS=0

# 检查代码质量
check_code_quality() {
    echo "🔍 检查代码质量..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件和生成文件
        if [[ "$java_file" == *"test"* ]] || [[ "$java_file" == *"target"* ]]; then
            continue
        fi

        # 检查文件大小（过大文件可能是设计问题）
        file_size=$(wc -l < "$java_file")
        if [ $file_size -gt 500 ]; then
            echo "⚠️  文件过大 ($file_size 行)：$java_file" >> "$LOG_FILE"
            echo "   建议考虑拆分重构" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi

        # 检查方法数量（过多的方法可能违反单一职责）
        method_count=$(grep -c "^\s*public\|^\s*private\|^\s*protected" "$java_file" || echo 0)
        if [ $method_count -gt 30 ]; then
            echo "⚠️  方法过多 ($method_count 个)：$java_file" >> "$LOG_FILE"
            echo "   建议考虑拆分类" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi

        # 检查长方法
        awk '/^\s*(public|private|protected).*{/{method=$0; line=NR; brace=1; next}
             /{/{brace++}
             /}/{brace--; if(brace==1 && method && NR-line>50){print "⚠️  长方法 (50+行):" method " 在行: " line >> "'$LOG_FILE'"; print "   文件: '$java_file'" >> "'$LOG_FILE'"; print "" >> "'$LOG_FILE'"; WARNING_COUNT++};
             brace==0{method=""}' "$java_file"

        # 检查硬编码
        if grep -q -E "\"[0-9]{10,}\"|'[0-9]{10,}'" "$java_file"; then
            echo "⚠️  发现可能的硬编码时间戳：$java_file" >> "$LOG_FILE"
            grep -n -E "\"[0-9]{10,}\"|'[0-9]{10,}'" "$java_file" >> "$LOG_FILE"
            echo "   建议使用常量或配置" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi
    done
}

# 检查安全规范
check_security_compliance() {
    echo "🔍 检查安全规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查SQL注入风险
        if grep -q -E "(Statement|executeQuery|executeUpdate).*\+" "$java_file"; then
            if ! grep -q -E "// (已验证|安全|TODO)" "$java_file"; then
                echo "❌ 潜在SQL注入风险：$java_file" >> "$LOG_FILE"
                grep -n -E "(Statement|executeQuery|executeUpdate).*\+" "$java_file" >> "$LOG_FILE"
                echo "   建议使用PreparedStatement" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"
                ((VIOLATION_COUNT++))
            fi
        fi

        # 检查明文密码
        if grep -q -E "(password|pwd|secret).*=.*\"[^\"]+[^\"]*\"" "$java_file"; then
            echo "❌ 发现明文密码：$java_file" >> "$LOG_FILE"
            grep -n -E "(password|pwd|secret).*=.*\"[^\"]+[^\"]*\"" "$java_file" >> "$LOG_FILE"
            echo "   请使用加密存储" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi

        # 检查敏感信息输出
        if grep -q -E "(log|print|System\.out).*(password|token|secret|key)" "$java_file"; then
            echo "❌ 敏感信息输出：$java_file" >> "$LOG_FILE"
            grep -n -E "(log|print|System\.out).*(password|token|secret|key)" "$java_file" >> "$LOG_FILE"
            echo "   请避免输出敏感信息" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi
    done
}

# 检查性能规范
check_performance_compliance() {
    echo "🔍 检查性能规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查深度分页
        if grep -q -E "LIMIT.*[0-9]{4,}" "$java_file"; then
            echo "⚠️  深度分页风险：$java_file" >> "$LOG_FILE"
            grep -n -E "LIMIT.*[0-9]{4,}" "$java_file" >> "$LOG_FILE"
            echo "   建议使用游标分页" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi

        # 检查全表扫描风险
        if grep -q -E "SELECT.*\*\s+FROM.*WHERE.*1\s*=\s*1" "$java_file"; then
            echo "⚠️  全表扫描风险：$java_file" >> "$LOG_FILE"
            grep -n -E "SELECT.*\*\s+FROM.*WHERE.*1\s*=\s*1" "$java_file" >> "$LOG_FILE"
            echo "   请添加合适的查询条件" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi

        # 检查N+1查询模式
        if grep -q -E "for.*\(.*\).*\{" "$java_file"; then
            # 简单检测，实际需要更复杂的分析
            echo "⚠️  可能的N+1查询：$java_file" >> "$LOG_FILE"
            grep -n -E "for.*\(.*\).*\{" "$java_file" | head -3 >> "$LOG_FILE"
            echo "   建议检查循环内的数据库查询" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi
    done
}

# 检查国际化规范
check_i18n_compliance() {
    echo "🔍 检查国际化规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查硬编码中文字符
        if grep -q -E "[\u4e00-\u9fff]" "$java_file"; then
            # 检查是否在注释中
            if grep -n -E "[\u4e00-\u9fff].*//" "$java_file" | grep -q -v -E "//.*[；；]"; then
                echo "⚠️  硬编码中文字符：$java_file" >> "$LOG_FILE"
                grep -n -E "[\u4e00-\u9fff].*//.*[^；；]" "$java_file" | head -5 >> "$LOG_FILE"
                echo "   建议使用国际化资源文件" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"
                ((WARNING_COUNT++))
            fi
        fi
    done
}

# 检查文档规范
check_documentation_compliance() {
    echo "🔍 检查文档规范..." | tee -a "$LOG_FILE"

    # 检查是否有README文件
    for service_dir in microservices/ioedream-*-service; do
        if [ -d "$service_dir" ]; then
            if [ ! -f "$service_dir/README.md" ]; then
                echo "⚠️  缺少README文件：$service_dir" >> "$LOG_FILE"
                ((WARNING_COUNT++))
            fi
        fi
    done

    # 检查API文档注释
    find microservices -name "*Controller.java" -type f | while read -r controller_file; do
        if ! grep -q -E "@Tag|@ApiOperation|@Operation" "$controller_file"; then
            echo "⚠️  缺少API文档注解：$controller_file" >> "$LOG_FILE"
            echo "   建议添加 @Tag 和 @Operation 注解" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi
    done
}

# 检查依赖版本一致性
check_dependency_consistency() {
    echo "🔍 检查依赖版本一致性..." | tee -a "$LOG_FILE"

    if [ -f "microservices/pom.xml" ]; then
        # 检查是否有版本冲突
        if grep -c "<version>" "microservices/pom.xml" | grep -q -E "^[0-9]+$"; then
            echo "✅ 依赖版本管理规范" >> "$LOG_FILE"
        else
            echo "⚠️  建议使用dependencyManagement统一管理版本" >> "$LOG_FILE"
            ((WARNING_COUNT++))
        fi
    fi
}

# 检查配置安全
check_configuration_security() {
    echo "🔍 检查配置安全..." | tee -a "$LOG_FILE"

    # 检查application.yml文件中的明文密码
    find microservices -name "application*.yml" -o -name "application*.properties" | while read -r config_file; do
        if grep -q -E "(password|secret|key).*:" "$config_file"; then
            if ! grep -q -E "ENC\(\{.*\})" "$config_file"; then
                echo "❌ 配置文件存在明文密码：$config_file" >> "$LOG_FILE"
                grep -n -E "(password|secret|key).*:" "$config_file" >> "$LOG_FILE"
                echo "   请使用加密配置" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"
                ((VIOLATION_COUNT++))
            fi
        fi
    done
}

# 执行检查
echo "开始工程治理检查..." | tee -a "$LOG_FILE"

# 1. 检查代码质量
check_code_quality

# 2. 检查安全规范
check_security_compliance

# 3. 检查性能规范
check_performance_compliance

# 4. 检查国际化规范
check_i18n_compliance

# 5. 检查文档规范
check_documentation_compliance

# 6. 检查依赖版本一致性
check_dependency_consistency

# 7. 检查配置安全
check_configuration_security

# 生成检查结果报告
echo "======================================" >> "$LOG_FILE"
echo "检查完成时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "严重问题数量: $VIOLATION_COUNT" >> "$LOG_FILE"
echo "警告数量: $WARNING_COUNT" >> "$LOG_FILE"

# 输出结果到控制台
echo ""
echo "📊 P4检查结果总结："
echo "🔍 检查项目: 代码质量、安全规范、性能规范、国际化、文档、配置安全"
echo "❌ 严重问题: $VIOLATION_COUNT 个"
echo "⚠️  警告问题: $WARNING_COUNT 个"

if [ $VIOLATION_COUNT -gt $MAX_VIOLATIONS ]; then
    echo "🚫 P4检查失败：发现 $VIOLATION_COUNT 个治理违规（最大允许: $MAX_VIOLATIONS）"
    echo "📋 详细报告见: $LOG_FILE"
    exit 1
else
    echo "✅ P4检查通过：工程治理符合要求"
    echo "📋 详细报告见: $LOG_FILE"

    if [ $WARNING_COUNT -gt 0 ]; then
        echo "💡 建议处理 $WARNING_COUNT 个警告问题以提升代码质量"
    fi

    exit 0
fi