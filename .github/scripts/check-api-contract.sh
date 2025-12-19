#!/bin/bash
# P2：API契约一致性检查脚本
# 验证代码契约的一致性，防止跨层访问和依赖倒置

set -euo pipefail

echo "🔍 开始P2：API契约一致性检查..."

# 日志文件
LOG_FILE="api-violations.log"
echo "IOE-DREAM API契约一致性检查报告" > "$LOG_FILE"
echo "检查时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "======================================" >> "$LOG_FILE"

VIOLATION_COUNT=0
MAX_VIOLATIONS=0

# 检查跨层访问违规
check_layer_violations() {
    echo "🔍 检查跨层访问违规..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查Controller层是否直接调用DAO
        if [[ "$java_file" == *"/controller/"* ]]; then
            # 检查是否直接使用DAO
            if grep -q -E "(Dao|\.dao\.)" "$java_file"; then
                if ! grep -q -E "// (允许|临时|TODO)" "$java_file"; then
                    echo "❌ Controller直接访问DAO：$java_file" >> "$LOG_FILE"
                    grep -n -E "(Dao|\.dao\.)" "$java_file" >> "$LOG_FILE"
                    echo "" >> "$LOG_FILE"
                    ((VIOLATION_COUNT++))
                fi
            fi

            # 检查是否直接使用Manager
            if grep -q -E "(Manager|\.manager\.)" "$java_file"; then
                if ! grep -q -E "// (允许|临时|TODO)" "$java_file"; then
                    echo "❌ Controller直接访问Manager：$java_file" >> "$LOG_FILE"
                    grep -n -E "(Manager|\.manager\.)" "$java_file" >> "$LOG_FILE"
                    echo "" >> "$LOG_FILE"
                    ((VIOLATION_COUNT++))
                fi
            fi
        fi

        # 检查Service层是否直接访问数据库
        if [[ "$java_file" == *"/service/"* ]]; then
            # 检查是否直接使用JDBC或原生SQL
            if grep -q -E "(Connection|PreparedStatement|Statement|executeQuery)" "$java_file"; then
                if ! grep -q -E "// (允许|临时|TODO)" "$java_file"; then
                    echo "❌ Service直接访问数据库：$java_file" >> "$LOG_FILE"
                    grep -n -E "(Connection|PreparedStatement|Statement|executeQuery)" "$java_file" >> "$LOG_FILE"
                    echo "" >> "$LOG_FILE"
                    ((VIOLATION_COUNT++))
                fi
            fi
        fi
    done
}

# 检查依赖注入规范
check_dependency_injection() {
    echo "🔍 检查依赖注入规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查是否使用了@Autowired
        if grep -q -E "@Autowired" "$java_file"; then
            echo "❌ 使用@Autowired（应该使用@Resource）：$java_file" >> "$LOG_FILE"
            grep -n "@Autowired" "$java_file" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi

        # 检查数据访问层命名
        if [[ "$java_file" == *"/dao/"* ]]; then
            file_name=$(basename "$java_file" .java)
            if [[ "$file_name" != *"Dao"* ]]; then
                if grep -q -E "@Mapper" "$java_file"; then
                    echo "❌ DAO接口命名不规范（应该以Dao结尾）：$java_file" >> "$LOG_FILE"
                    echo "   当前文件名: $file_name" >> "$LOG_FILE"
                    echo "   建议文件名: ${file_name%Repository}Dao" >> "$LOG_FILE"
                    echo "" >> "$LOG_FILE"
                    ((VIOLATION_COUNT++))
                fi
            fi
        fi
    done
}

# 检查注解使用规范
check_annotation_compliance() {
    echo "🔍 检查注解使用规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查Repository注解使用
        if grep -q -E "@Repository" "$java_file"; then
            echo "❌ 使用@Repository（应该使用@Mapper）：$java_file" >> "$LOG_FILE"
            grep -n "@Repository" "$java_file" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi

        # 检查Jakarta EE包名使用
        if grep -q -E "javax\.(annotation|validation|persistence|servlet)" "$java_file"; then
            if ! grep -q -E "// (兼容|临时|TODO)" "$java_file"; then
                echo "❌ 使用javax包名（应该使用jakarta）：$java_file" >> "$LOG_FILE"
                grep -n -E "javax\.(annotation|validation|persistence|servlet)" "$java_file" >> "$LOG_FILE"
                echo "" >> "$LOG_FILE"
                ((VIOLATION_COUNT++))
            fi
        fi
    done
}

# 检查API设计规范
check_api_design() {
    echo "🔍 检查API设计规范..." | tee -a "$LOG_FILE"

    find microservices -name "*Controller.java" -type f | while read -r controller_file; do
        # 检查GET请求是否用于查询操作
        if grep -q -E "@PostMapping.*get|@PostMapping.*query|@PostMapping.*list" "$controller_file"; then
            echo "❌ POST用于查询操作（应该使用GET）：$controller_file" >> "$LOG_FILE"
            grep -n -E "@PostMapping.*get|@PostMapping.*query|@PostMapping.*list" "$controller_file" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi

        # 检查PUT/PATCH请求是否用于更新操作
        if grep -q -E "@PostMapping.*update|@PostMapping.*modify" "$controller_file"; then
            echo "❌ POST用于更新操作（应该使用PUT/PATCH）：$controller_file" >> "$LOG_FILE"
            grep -n -E "@PostMapping.*update|@PostMapping.*modify" "$controller_file" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi

        # 检查DELETE请求是否用于删除操作
        if grep -q -E "@PostMapping.*delete|@PostMapping.*remove" "$controller_file"; then
            echo "❌ POST用于删除操作（应该使用DELETE）：$controller_file" >> "$LOG_FILE"
            grep -n -E "@PostMapping.*delete|@PostMapping.*remove" "$controller_file" >> "$LOG_FILE"
            echo "" >> "$LOG_FILE"
            ((VIOLATION_COUNT++))
        fi
    done
}

# 检查事务管理规范
check_transaction_management() {
    echo "🔍 检查事务管理规范..." | tee -a "$LOG_FILE"

    find microservices -name "*.java" -type f | while read -r java_file; do
        # 跳过测试文件
        if [[ "$java_file" == *"test"* ]]; then
            continue
        fi

        # 检查Service层是否有事务注解
        if [[ "$java_file" == *"/service/"* ]] && [[ "$java_file" == *"Impl.java" ]]; then
            if ! grep -q -E "@Transactional" "$java_file"; then
                if grep -q -E "(update|delete|insert|save)" "$java_file"; then
                    echo "⚠️  Service实现类缺少事务注解：$java_file" >> "$LOG_FILE"
                    echo "   建议添加 @Transactional(rollbackFor = Exception.class)" >> "$LOG_FILE"
                    echo "" >> "$LOG_FILE"
                    # 这里不计数为违规，只是警告
                fi
            fi
        fi
    done
}

# 执行检查
echo "开始API契约一致性检查..." | tee -a "$LOG_FILE"

# 1. 检查跨层访问违规
check_layer_violations

# 2. 检查依赖注入规范
check_dependency_injection

# 3. 检查注解使用规范
check_annotation_compliance

# 4. 检查API设计规范
check_api_design

# 5. 检查事务管理规范
check_transaction_management

# 生成检查结果报告
echo "======================================" >> "$LOG_FILE"
echo "检查完成时间: $(date -u +"%Y-%m-%d %H:%M:%S UTC")" >> "$LOG_FILE"
echo "发现问题数量: $VIOLATION_COUNT" >> "$LOG_FILE"

# 输出结果到控制台
echo ""
echo "📊 P2检查结果总结："
echo "🔍 检查项目: 跨层访问、依赖注入、注解规范、API设计、事务管理"
echo "❌ 发现问题: $VIOLATION_COUNT 个"

if [ $VIOLATION_COUNT -gt $MAX_VIOLATIONS ]; then
    echo "🚫 P2检查失败：发现 $VIOLATION_COUNT 个API契约违规（最大允许: $MAX_VIOLATIONS）"
    echo "📋 详细报告见: $LOG_FILE"
    exit 1
else
    echo "✅ P2检查通过：API契约一致性符合要求"
    echo "📋 详细报告见: $LOG_FILE"
    exit 0
fi