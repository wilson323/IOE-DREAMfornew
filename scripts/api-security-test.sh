#!/bin/bash

# =============================================================================
# IOE-DREAM API接口安全防护措施验证脚本
#
# 功能描述：
# 专门测试API接口的安全防护措施，包括输入验证、注入防护、CSRF防护等
#
# @author IOE-DREAM 安全团队
# @version 1.0.0
# @date 2025-11-29
# =============================================================================

set -euo pipefail

# 颜色定义
readonly RED='\033[0;31m'
readonly GREEN='\033[0;32m'
readonly YELLOW='\033[1;33m'
readonly BLUE='\033[0;34m'
readonly PURPLE='\033[0;35m'
readonly CYAN='\033[0;36m'
readonly NC='\033[0m'

# 项目路径配置
readonly PROJECT_ROOT="D:/IOE-DREAM"
readonly MICROSERVICES_DIR="${PROJECT_ROOT}/microservices"
readonly REPORTS_DIR="${PROJECT_ROOT}/security-audit-reports"
readonly TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
readonly REPORT_FILE="${REPORTS_DIR}/api_security_test_${TIMESTAMP}.md"

# 攻击载荷定义
readonly SQL_INJECTION_PAYLOADS=("' OR '1'='1" "'; DROP TABLE users; --" "' UNION SELECT * FROM users --" "1'; DELETE FROM users WHERE '1'='1")
readonly XSS_PAYLOADS=("<script>alert('XSS')</script>" "javascript:alert('XSS')" "<img src=x onerror=alert('XSS')>")
readonly CSRF_PAYLOADS=("<iframe src='http://evil.com/steal'></iframe>" "<form action='http://evil.com/attack' method='POST'>")
readonly PATH_TRAVERSAL_PAYLOADS=("../../../etc/passwd" "..\\..\\..\\windows\\system32\\drivers\\etc\\hosts")

# 创建报告目录
mkdir -p "${REPORTS_DIR}"

# 测试结果统计
declare -A TEST_RESULTS=(
    ["total_tests"]=0
    ["passed_tests"]=0
    ["failed_tests"]=0
    ["warning_tests"]=0
    ["critical_issues"]=0
    ["vulnerabilities_found"]=0
)

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
    ((TEST_RESULTS[passed_tests]++))
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
    ((TEST_RESULTS[warning_tests]++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
    ((TEST_RESULTS[failed_tests]++))
}

log_critical() {
    echo -e "${PURPLE}[CRITICAL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
    ((TEST_RESULTS[critical_issues]++))
    ((TEST_RESULTS[vulnerabilities_found]++))
}

log_vulnerability() {
    echo -e "${RED}[VULNERABILITY]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# 初始化测试报告
init_test_report() {
    cat > "${REPORT_FILE}" << EOF
# IOE-DREAM API接口安全防护措施验证报告

**测试时间**: $(date '+%Y年%m月%d日 %H:%M:%S')
**测试范围**: API接口安全防护措施验证
**测试版本**: v1.0.0
**测试团队**: IOE-DREAM 安全团队

---

## 📋 测试概述

本报告详细记录了IOE-DREAM微服务架构中API接口安全防护措施的验证结果，包括：
- 输入验证机制测试
- SQL注入防护测试
- XSS攻击防护测试
- CSRF攻击防护测试
- 文件上传安全测试
- API访问控制测试

---

## 🔌 测试结果详情

EOF
    log_info "API接口安全测试报告初始化完成"
}

# 1. 输入验证机制测试
test_input_validation() {
    log_info "开始输入验证机制测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 1. 输入验证机制测试" >> "${REPORT_FILE}"

    # 1.1 检查控制器输入验证注解
    log_info "测试控制器输入验证注解覆盖率..."

    local all_controllers=$(find "${MICROSERVICES_DIR}" -name "*Controller.java" | wc -l)
    local validated_controllers=$(find "${MICROSERVICES_DIR}" -name "*Controller.java" -exec grep -l "@Valid\|@NotNull\|@NotBlank\|@Size\|@Min\|@Max" {} \; | wc -l)

    if [[ $all_controllers -gt 0 ]]; then
        local validation_coverage=$((validated_controllers * 100 / all_controllers))
        log_info "控制器输入验证覆盖率: ${validation_coverage}% (${validated_controllers}/${all_controllers})"

        echo "#### 控制器输入验证统计" >> "${REPORT_FILE}"
        echo "| 指标 | 数值 | 说明 |" >> "${REPORT_FILE}"
        echo "|------|------|------|" >> "${REPORT_FILE}"
        echo "| 总控制器数 | ${all_controllers} | 系统中的所有控制器 |" >> "${REPORT_FILE}"
        echo "| 验证控制器数 | ${validated_controllers} | 有输入验证的控制器 |" >> "${REPORT_FILE}"
        echo "| 验证覆盖率 | ${validation_coverage}% | 输入验证覆盖比例 |" >> "${REPORT_FILE}"

        if [[ $validation_coverage -ge 90 ]]; then
            log_success "输入验证覆盖率优秀"
            echo "✅ **验证覆盖率**: ${validation_coverage}%，覆盖率优秀" >> "${REPORT_FILE}"
        elif [[ $validation_coverage -ge 70 ]]; then
            log_warning "输入验证覆盖率良好"
            echo "⚠️ **验证覆盖率**: ${validation_coverage}%，覆盖率需要提升" >> "${REPORT_FILE}"
        else
            log_error "输入验证覆盖率过低"
            echo "❌ **验证覆盖率**: ${validation_coverage}%，覆盖率过低，存在安全风险" >> "${REPORT_FILE}"
        fi
    fi

    # 1.2 检查具体验证注解使用
    ((TEST_RESULTS[total_tests]++))
    log_info "测试验证注解使用规范性..."

    local validation_annotations=(
        "@NotNull"
        "@NotBlank"
        "@Size"
        "@Min"
        "@Max"
        "@Email"
        "@Pattern"
        "@Valid"
    )

    echo "#### 验证注解使用统计" >> "${REPORT_FILE}"
    echo "| 注解类型 | 使用次数 | 说明 |" >> "${REPORT_FILE}"
    echo "|----------|----------|------|" >> "${REPORT_FILE}"

    for annotation in "${validation_annotations[@]}"; do
        local usage_count=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -c "$annotation" {} \; | awk '{sum+=$1} END {print sum}')
        echo "| $annotation | $usage_count | 验证注解使用 |" >> "${REPORT_FILE}"
        if [[ $usage_count -gt 0 ]]; then
            log_success "验证注解 $annotation 使用 $usage_count 次"
        fi
    done

    # 1.3 检查自定义验证
    ((TEST_RESULTS[total_tests]++))
    log_info "测试自定义验证实现..."

    local custom_validators=$(find "${MICROSERVICES_DIR}" -name "*Validator.java" -o -name "*Validation*.java" | wc -l)
    if [[ $custom_validators -gt 0 ]]; then
        log_success "发现${custom_validators}个自定义验证器"
        echo "✅ **自定义验证**: 发现${custom_validators}个自定义验证器" >> "${REPORT_FILE}"
    else
        log_warning "未发现自定义验证器"
        echo "⚠️ **自定义验证**: 未发现自定义验证器" >> "${REPORT_FILE}"
    fi

    log_info "输入验证机制测试完成"
}

# 2. SQL注入防护测试
test_sql_injection_protection() {
    log_info "开始SQL注入防护测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 2. SQL注入防护测试" >> "${REPORT_FILE}"

    # 2.1 检查参数化查询使用
    log_info "测试参数化查询实现..."

    local prepared_statements=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "PreparedStatement\|@Query\|参数化查询" {} \; | wc -l)
    local mybatis_plus_usage=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "@Select\|@Insert\|@Update\|@Delete\|QueryWrapper\|LambdaQueryWrapper" {} \; | wc -l)

    echo "#### SQL查询安全实现统计" >> "${REPORT_FILE}"
    echo "| 安全实现 | 使用数量 | 说明 |" >> "${REPORT_FILE}"
    echo "|----------|----------|------|" >> "${REPORT_FILE}"
    echo "| PreparedStatement | $prepared_statements | JDBC参数化查询 |" >> "${REPORT_FILE}"
    echo "| MyBatis-Plus注解 | $mybatis_plus_usage | MyBatis-Plus安全查询 |" >> "${REPORT_FILE}"

    if [[ $((prepared_statements + mybatis_plus_usage)) -gt 0 ]]; then
        log_success "发现安全的SQL查询实现"
        echo "✅ **安全查询**: 已实现安全的参数化查询" >> "${REPORT_FILE}"
    else
        log_warning "未发现明确的参数化查询实现"
        echo "⚠️ **安全查询**: 未发现明确的参数化查询实现" >> "${REPORT_FILE}"
    fi

    # 2.2 检查动态SQL构造
    ((TEST_RESULTS[total_tests]++))
    log_info "测试动态SQL构造安全性..."

    local unsafe_sql=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "String.*sql.*=\|\".*SELECT.*\"\+.*\|statement\.execute" {} \; | wc -l)
    if [[ $unsafe_sql -gt 0 ]]; then
        log_vulnerability "发现${unsafe_sql}个潜在的动态SQL构造，存在SQL注入风险"
        echo "❌ **动态SQL**: 发现${unsafe_sql}个潜在的不安全SQL构造" >> "${REPORT_FILE}"
    else
        log_success "未发现明显的动态SQL构造风险"
        echo "✅ **动态SQL**: 未发现明显的SQL注入风险" >> "${REPORT_FILE}"
    fi

    # 2.3 模拟SQL注入攻击测试
    ((TEST_RESULTS[total_tests]++))
    log_info "模拟SQL注入攻击测试..."

    echo "#### SQL注入攻击模拟测试" >> "${REPORT_FILE}"
    echo "| 攻击载荷 | 预期结果 | 测试状态 |" >> "${REPORT_FILE}"
    echo "|----------|----------|----------|" >> "${REPORT_FILE}"

    for payload in "${SQL_INJECTION_PAYLOADS[@]}"; do
        echo "| $payload | 应被拦截/转义 | ⚠️ 需要API测试 |" >> "${REPORT_FILE}"
        log_warning "SQL注入载荷 '$payload' 需要通过API测试验证防护效果"
    done

    log_info "SQL注入防护测试完成"
}

# 3. XSS攻击防护测试
test_xss_protection() {
    log_info "开始XSS攻击防护测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 3. XSS攻击防护测试" >> "${REPORT_FILE}"

    # 3.1 检查XSS过滤实现
    log_info "测试XSS过滤实现..."

    local xss_filters=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "XSS\|escapeHtml\|HtmlUtils\|sanitize\|Cleaner" {} \; | wc -l)
    if [[ $xss_filters -gt 0 ]]; then
        log_success "发现XSS过滤实现"
        echo "✅ **XSS过滤**: 已实现XSS攻击过滤" >> "${REPORT_FILE}"
    else
        log_warning "未发现XSS过滤实现"
        echo "⚠️ **XSS过滤**: 未发现XSS攻击过滤实现" >> "${REPORT_FILE}"
    fi

    # 3.2 检查输出编码
    ((TEST_RESULTS[total_tests]++))
    log_info "测试输出编码实现..."

    local output_encoding=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "escape\|encode\|URLEncoder\|HtmlUtils" {} \; | wc -l)
    if [[ $output_encoding -gt 0 ]]; then
        log_success "发现输出编码实现"
        echo "✅ **输出编码**: 已实现输出编码" >> "${REPORT_FILE}"
    else
        log_warning "未发现输出编码实现"
        echo "⚠️ **输出编码**: 未发现输出编码实现" >> "${REPORT_FILE}"
    fi

    # 3.3 检查前端XSS防护
    ((TEST_RESULTS[total_tests]++))
    log_info "测试前端XSS防护配置..."

    local frontend_dir="${PROJECT_ROOT}/smart-admin-web-javascript"
    if [[ -d "$frontend_dir" ]]; then
        local xss_protection_headers=$(find "$frontend_dir" -name "*.js" -o -name "*.ts" -o -name "*.vue" -exec grep -l "X-XSS-Protection\|Content-Security-Policy\|xss" {} \; | wc -l)
        if [[ $xss_protection_headers -gt 0 ]]; then
            log_success "发现前端XSS防护配置"
            echo "✅ **前端XSS防护**: 已配置前端XSS防护" >> "${REPORT_FILE}"
        else
            log_warning "未发现前端XSS防护配置"
            echo "⚠️ **前端XSS防护**: 未发现前端XSS防护配置" >> "${REPORT_FILE}"
        fi
    fi

    # 3.4 模拟XSS攻击测试
    ((TEST_RESULTS[total_tests]++))
    log_info "模拟XSS攻击测试..."

    echo "#### XSS攻击模拟测试" >> "${REPORT_FILE}"
    echo "| 攻击载荷 | 预期结果 | 测试状态 |" >> "${REPORT_FILE}"
    echo "|----------|----------|----------|" >> "${REPORT_FILE}"

    for payload in "${XSS_PAYLOADS[@]}"; do
        echo "| $payload | 应被过滤/转义 | ⚠️ 需要API测试 |" >> "${REPORT_FILE}"
        log_warning "XSS载荷 '$payload' 需要通过API测试验证防护效果"
    done

    log_info "XSS攻击防护测试完成"
}

# 4. CSRF攻击防护测试
test_csrf_protection() {
    log_info "开始CSRF攻击防护测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 4. CSRF攻击防护测试" >> "${REPORT_FILE}"

    # 4.1 检查CSRF Token实现
    log_info "测试CSRF Token实现..."

    local csrf_tokens=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "CsrfToken\|csrf\|_csrf" {} \; | wc -l)
    if [[ $csrf_tokens -gt 0 ]]; then
        log_success "发现CSRF Token实现"
        echo "✅ **CSRF Token**: 已实现CSRF Token防护" >> "${REPORT_FILE}"
    else
        log_warning "未发现CSRF Token实现"
        echo "⚠️ **CSRF Token**: 未发现CSRF Token实现" >> "${REPORT_FILE}"
    fi

    # 4.2 检查SameSite Cookie
    ((TEST_RESULTS[total_tests]++))
    log_info "测试SameSite Cookie配置..."

    local samesite_config=$(find "${MICROSERVICES_DIR}" -name "*.yml" -o -name "*.properties" -exec grep -l "SameSite\|same-site" {} \; | wc -l)
    if [[ $samesite_config -gt 0 ]]; then
        log_success "发现SameSite Cookie配置"
        echo "✅ **SameSite Cookie**: 已配置SameSite Cookie" >> "${REPORT_FILE}"
    else
        log_warning "未发现SameSite Cookie配置"
        echo "⚠️ **SameSite Cookie**: 未发现SameSite Cookie配置" >> "${REPORT_FILE}"
    fi

    # 4.3 检查Referer检查
    ((TEST_RESULTS[total_tests]++))
    log_info "测试Referer检查实现..."

    local referer_check=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "Referer\|referer.*check" {} \; | wc -l)
    if [[ $referer_check -gt 0 ]]; then
        log_success "发现Referer检查实现"
        echo "✅ **Referer检查**: 已实现Referer检查" >> "${REPORT_FILE}"
    else
        log_warning "未发现Referer检查实现"
        echo "⚠️ **Referer检查**: 未发现Referer检查实现" >> "${REPORT_FILE}"
    fi

    # 4.4 模拟CSRF攻击测试
    ((TEST_RESULTS[total_tests]++))
    log_info "模拟CSRF攻击测试..."

    echo "#### CSRF攻击模拟测试" >> "${REPORT_FILE}"
    echo "| 攻击场景 | 预期结果 | 测试状态 |" >> "${REPORT_FILE}"
    echo "|----------|----------|----------|" >> "${REPORT_FILE}"

    echo "| 跨站POST请求 | 应被CSRF防护拦截 | ⚠️ 需要API测试 |" >> "${REPORT_FILE}"
    echo "| 无CSRF Token请求 | 应被拒绝 | ⚠️ 需要API测试 |" >> "${REPORT_FILE}"

    for payload in "${CSRF_PAYLOADS[@]}"; do
        echo "| CSRF载荷注入 | 应被过滤 | ⚠️ 需要API测试 |" >> "${REPORT_FILE}"
    done

    log_info "CSRF攻击防护测试完成"
}

# 5. 文件上传安全测试
test_file_upload_security() {
    log_info "开始文件上传安全测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 5. 文件上传安全测试" >> "${REPORT_FILE}"

    # 5.1 检查文件上传控制器
    log_info "测试文件上传控制器..."

    local upload_controllers=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "MultipartFile\|upload.*file" {} \; | wc -l)
    if [[ $upload_controllers -gt 0 ]]; then
        log_success "发现${upload_controllers}个文件上传控制器"
        echo "✅ **文件上传**: 发现${upload_controllers}个文件上传实现" >> "${REPORT_FILE}"

        # 5.2 检查文件类型验证
        ((TEST_RESULTS[total_tests]++))
        log_info "测试文件类型验证..."

        local file_type_validation=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "contentType\|file.*type\|extension" {} \; | wc -l)
        if [[ $file_type_validation -gt 0 ]]; then
            log_success "发现文件类型验证实现"
            echo "✅ **类型验证**: 已实现文件类型验证" >> "${REPORT_FILE}"
        else
            log_vulnerability "文件上传缺少类型验证，存在安全风险"
            echo "❌ **类型验证**: 文件上传缺少类型验证" >> "${REPORT_FILE}"
        fi

        # 5.3 检查文件大小限制
        ((TEST_RESULTS[total_tests]++))
        log_info "测试文件大小限制..."

        local file_size_limit=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "max-file-size\|spring.servlet.multipart" {} \; | wc -l)
        if [[ $file_size_limit -gt 0 ]]; then
            log_success "发现文件大小限制配置"
            echo "✅ **大小限制**: 已配置文件大小限制" >> "${REPORT_FILE}"
        else
            log_warning "文件上传缺少大小限制"
            echo "⚠️ **大小限制**: 文件上传缺少大小限制" >> "${REPORT_FILE}"
        fi

        # 5.4 检查文件内容检查
        ((TEST_RESULTS[total_tests]++))
        log_info "测试文件内容安全检查..."

        local file_content_check=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "file.*content\|magic.*number\|file.*header" {} \; | wc -l)
        if [[ $file_content_check -gt 0 ]]; then
            log_success "发现文件内容安全检查"
            echo "✅ **内容检查**: 已实现文件内容安全检查" >> "${REPORT_FILE}"
        else
            log_warning "文件上传缺少内容安全检查"
            echo "⚠️ **内容检查**: 文件上传缺少内容安全检查" >> "${REPORT_FILE}"
        fi

        # 5.5 检查文件存储安全
        ((TEST_RESULTS[total_tests]++))
        log_info "测试文件存储安全..."

        local secure_storage=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "random.*name\|UUID\|secure.*path" {} \; | wc -l)
        if [[ $secure_storage -gt 0 ]]; then
            log_success "发现安全文件存储实现"
            echo "✅ **安全存储**: 已实现安全文件存储" >> "${REPORT_FILE}"
        else
            log_warning "文件存储可能不够安全"
            echo "⚠️ **安全存储**: 文件存储安全性需要加强" >> "${REPORT_FILE}"
        fi
    else
        log_info "未发现文件上传功能"
        echo "ℹ️ **文件上传**: 未发现文件上传功能" >> "${REPORT_FILE}"
    fi

    log_info "文件上传安全测试完成"
}

# 6. 路径遍历攻击防护测试
test_path_traversal_protection() {
    log_info "开始路径遍历攻击防护测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 6. 路径遍历攻击防护测试" >> "${REPORT_FILE}"

    # 6.1 检查路径规范化
    log_info "测试路径规范化实现..."

    local path_normalization=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "normalize\|getCanonicalPath\|realPath" {} \; | wc -l)
    if [[ $path_normalization -gt 0 ]]; then
        log_success "发现路径规范化实现"
        echo "✅ **路径规范化**: 已实现路径规范化" >> "${REPORT_FILE}"
    else
        log_warning "未发现路径规范化实现"
        echo "⚠️ **路径规范化**: 未发现路径规范化实现" >> "${REPORT_FILE}"
    fi

    # 6.2 检查路径白名单
    ((TEST_RESULTS[total_tests]++))
    log_info "测试路径白名单实现..."

    local path_whitelist=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "whitelist\|allowList\|safePath" {} \; | wc -l)
    if [[ $path_whitelist -gt 0 ]]; then
        log_success "发现路径白名单实现"
        echo "✅ **路径白名单**: 已实现路径白名单" >> "${REPORT_FILE}"
    else
        log_warning "未发现路径白名单实现"
        echo "⚠️ **路径白名单**: 未发现路径白名单实现" >> "${REPORT_FILE}"
    fi

    # 6.3 模拟路径遍历攻击测试
    ((TEST_RESULTS[total_tests]++))
    log_info "模拟路径遍历攻击测试..."

    echo "#### 路径遍历攻击模拟测试" >> "${REPORT_FILE}"
    echo "| 攻击载荷 | 预期结果 | 测试状态 |" >> "${REPORT_FILE}"
    echo "|----------|----------|----------|" >> "${REPORT_FILE}"

    for payload in "${PATH_TRAVERSAL_PAYLOADS[@]}"; do
        echo "| $payload | 应被拦截/规范化 | ⚠️ 需要API测试 |" >> "${REPORT_FILE}"
        log_warning "路径遍历载荷 '$payload' 需要通过API测试验证防护效果"
    done

    log_info "路径遍历攻击防护测试完成"
}

# 7. API访问控制测试
test_api_access_control() {
    log_info "开始API访问控制测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 7. API访问控制测试" >> "${REPORT_FILE}"

    # 7.1 检查频率限制
    log_info "测试API频率限制实现..."

    local rate_limiting=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "rate.*limit\|@RateLimit\|RateLimiter" {} \; | wc -l)
    if [[ $rate_limiting -gt 0 ]]; then
        log_success "发现API频率限制实现"
        echo "✅ **频率限制**: 已实现API频率限制" >> "${REPORT_FILE}"
    else
        log_warning "未发现API频率限制实现"
        echo "⚠️ **频率限制**: 未发现API频率限制实现" >> "${REPORT_FILE}"
    fi

    # 7.2 检查IP白名单
    ((TEST_RESULTS[total_tests]++))
    log_info "测试IP白名单/黑名单实现..."

    local ip_control=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "ip.*whitelist\|ip.*blacklist\|allowedIp" {} \; | wc -l)
    if [[ $ip_control -gt 0 ]]; then
        log_success "发现IP访问控制实现"
        echo "✅ **IP控制**: 已实现IP访问控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现IP访问控制实现"
        echo "⚠️ **IP控制**: 未发现IP访问控制实现" >> "${REPORT_FILE}"
    fi

    # 7.3 检查API密钥认证
    ((TEST_RESULTS[total_tests]++))
    log_info "测试API密钥认证实现..."

    local api_key_auth=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "apiKey\|api.*key\|X-API-Key" {} \; | wc -l)
    if [[ $api_key_auth -gt 0 ]]; then
        log_success "发现API密钥认证实现"
        echo "✅ **API密钥**: 已实现API密钥认证" >> "${REPORT_FILE}"
    else
        log_warning "未发现API密钥认证实现"
        echo "⚠️ **API密钥**: 未发现API密钥认证实现" >> "${REPORT_FILE}"
    fi

    # 7.4 检查HTTPS强制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试HTTPS强制配置..."

    local https_enforcement=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "ssl\|https\|redirect.*https" {} \; | wc -l)
    if [[ $https_enforcement -gt 0 ]]; then
        log_success "发现HTTPS配置"
        echo "✅ **HTTPS强制**: 已配置HTTPS" >> "${REPORT_FILE}"
    else
        log_warning "未发现HTTPS强制配置"
        echo "⚠️ **HTTPS强制**: 未发现HTTPS强制配置" >> "${REPORT_FILE}"
    fi

    log_info "API访问控制测试完成"
}

# 8. 敏感信息泄露测试
test_sensitive_info_leakage() {
    log_info "开始敏感信息泄露测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 8. 敏感信息泄露测试" >> "${REPORT_FILE}"

    # 8.1 检查异常信息泄露
    log_info "测试异常信息泄露防护..."

    local exception_handling=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "Exception.*handler\|@ControllerAdvice\|GlobalExceptionHandler" {} \; | wc -l)
    if [[ $exception_handling -gt 0 ]]; then
        log_success "发现全局异常处理"
        echo "✅ **异常处理**: 已实现全局异常处理" >> "${REPORT_FILE}"

        # 检查敏感信息过滤
        local sensitive_filter=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "sensitive.*filter\|信息脱敏\|数据脱敏" {} \; | wc -l)
        if [[ $sensitive_filter -gt 0 ]]; then
            log_success "发现敏感信息过滤"
            echo "✅ **敏感信息过滤**: 已实现敏感信息过滤" >> "${REPORT_FILE}"
        else
            log_warning "未发现敏感信息过滤"
            echo "⚠️ **敏感信息过滤**: 未发现敏感信息过滤" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现全局异常处理"
        echo "⚠️ **异常处理**: 未发现全局异常处理" >> "${REPORT_FILE}"
    fi

    # 8.2 检查调试信息泄露
    ((TEST_RESULTS[total_tests]++))
    log_info "测试调试信息泄露防护..."

    local debug_info=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "debug.*true\|stacktrace.*true" {} \; | wc -l)
    if [[ $debug_info -gt 0 ]]; then
        log_vulnerability "发现调试信息可能泄露"
        echo "❌ **调试信息**: 可能存在调试信息泄露" >> "${REPORT_FILE}"
    else
        log_success "未发现明显的调试信息泄露"
        echo "✅ **调试信息**: 未发现调试信息泄露" >> "${REPORT_FILE}"
    fi

    # 8.3 检查API文档安全
    ((TEST_RESULTS[total_tests]++))
    log_info "测试API文档安全性..."

    local swagger_configs=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "swagger\|openapi\|knife4j" {} \; | wc -l)
    if [[ $swagger_configs -gt 0 ]]; then
        log_warning "发现API文档配置，需确保生产环境已关闭"
        echo "⚠️ **API文档**: 发现API文档配置，生产环境建议关闭" >> "${REPORT_FILE}"

        # 检查生产环境配置
        local prod_swagger_disabled=$(find "${MICROSERVICES_DIR}" -name "*prod*.yml" -exec grep -l "swagger.*enabled.*false\|knife4j.*enable.*false" {} \; | wc -l)
        if [[ $prod_swagger_disabled -gt 0 ]]; then
            log_success "生产环境API文档已禁用"
            echo "✅ **生产文档**: 生产环境API文档已禁用" >> "${REPORT_FILE}"
        fi
    fi

    log_info "敏感信息泄露测试完成"
}

# 9. 生成测试报告总结
generate_test_summary() {
    log_info "生成测试报告总结..."

    echo -e "\n---" >> "${REPORT_FILE}"
    echo -e "\n## 📊 测试结果总结" >> "${REPORT_FILE}"

    local total_tests=${TEST_RESULTS[total_tests]}
    local passed_tests=${TEST_RESULTS[passed_tests]}
    local failed_tests=${TEST_RESULTS[failed_tests]}
    local warning_tests=${TEST_RESULTS[warning_tests]}
    local critical_issues=${TEST_RESULTS[critical_issues]}
    local vulnerabilities_found=${TEST_RESULTS[vulnerabilities_found]}

    # 计算通过率
    local pass_rate=0
    if [[ $total_tests -gt 0 ]]; then
        pass_rate=$((passed_tests * 100 / total_tests))
    fi

    cat >> "${REPORT_FILE}" << EOF

### 📈 API安全测试统计

| 测试指标 | 数值 | 说明 |
|----------|------|------|
| **总测试项** | ${total_tests} | API安全测试总项目数 |
| **通过测试** | ${passed_tests} | 符合安全要求的测试项目 |
| **失败测试** | ${failed_tests} | 不符合安全要求的测试项目 |
| **警告测试** | ${warning_tests} | 需要改进的测试项目 |
| **严重问题** | ${critical_issues} | 严重安全问题数量 |
| **发现漏洞** | ${vulnerabilities_found} | 发现的安全漏洞数量 |

### 🎯 API安全评分

**API接口安全评分: ${pass_rate}/100**

### 🛡️ 安全防护等级评估

EOF

    # 根据评分给出安全等级
    local security_level="优秀"
    if [[ $pass_rate -lt 60 ]]; then
        security_level="需要重大改进"
        echo "🔴 **安全防护等级**: ${security_level} - API安全防护存在严重问题" >> "${REPORT_FILE}"
    elif [[ $pass_rate -lt 80 ]]; then
        security_level="良好"
        echo "🟡 **安全防护等级**: ${security_level} - API安全防护需要完善" >> "${REPORT_FILE}"
    else
        echo "🟢 **安全防护等级**: ${security_level} - API安全防护良好" >> "${REPORT_FILE}"
    fi

    cat >> "${REPORT_FILE}" << EOF

### 🚨 关键安全风险

#### 高风险问题
1. **输入验证不足**: 可能导致注入攻击
2. **输出编码缺失**: 可能导致XSS攻击
3. **文件上传安全**: 可能导致任意文件上传
4. **路径遍历防护**: 可能导致目录遍历攻击

#### 中风险问题
1. **CSRF防护缺失**: 可能导致跨站请求伪造
2. **频率限制不足**: 可能被恶意请求攻击
3. **敏感信息泄露**: 可能暴露系统内部信息

### 🔧 关键修复建议

#### 立即修复 (高优先级)
1. **完善输入验证**: 所有API接口必须添加输入验证
2. **实现输出编码**: 防止XSS攻击
3. **加强文件上传安全**: 文件类型、大小、内容检查
4. **实现路径规范化**: 防止路径遍历攻击

#### 短期修复 (中优先级)
1. **添加CSRF防护**: 实现CSRF Token机制
2. **配置频率限制**: 防止API滥用
3. **完善异常处理**: 避免敏感信息泄露
4. **禁用生产API文档**: 防止接口信息泄露

#### 长期改进 (低优先级)
1. **实现WAF**: Web应用防火墙
2. **安全监控**: 实时安全监控和告警
3. **自动化测试**: 自动化安全测试集成
4. **安全培训**: 开发团队安全意识培训

### ✅ API安全检查清单

- [ ] 所有API接口都有输入验证 (覆盖率≥90%)
- [ ] 实现了参数化查询防护SQL注入
- [ ] 实现了XSS攻击过滤和输出编码
- [ ] 配置了CSRF防护机制
- [ ] 文件上传有类型、大小、内容检查
- [ ] 实现了路径遍历攻击防护
- [ ] 配置了API频率限制
- [ ] 实现了IP访问控制
- [ ] 生产环境启用了HTTPS
- [ ] 实现了全局异常处理
- [ ] 敏感信息已脱敏处理
- [ ] 生产环境已禁用API文档

### 🧪 进一步测试建议

#### 主动安全测试
1. **渗透测试**: 使用专业工具进行渗透测试
2. **模糊测试**: 对API接口进行模糊测试
3. **负载测试**: 测试API在负载下的安全性
4. **社交工程测试**: 测试人员安全意识

#### 持续安全监控
1. **WAF部署**: 部署Web应用防火墙
2. **入侵检测**: 实时入侵检测系统
3. **日志分析**: 安全日志分析和告警
4. **定期扫描**: 定期自动化安全扫描

EOF

    # 根据测试结果给出总体评价
    if [[ $vulnerabilities_found -gt 0 ]]; then
        echo -e "⚠️ **总体评价**: 发现${vulnerabilities_found}个安全漏洞，需要立即修复" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 80 ]]; then
        echo -e "✅ **总体评价**: API接口安全防护措施完善，安全性良好" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 60 ]]; then
        echo -e "⚠️ **总体评价**: API接口安全防护基本到位，需要进一步完善" >> "${REPORT_FILE}"
    else
        echo -e "❌ **总体评价**: API接口安全防护存在较多问题，需要全面改进" >> "${REPORT_FILE}"
    fi
}

# 主函数
main() {
    echo -e "${CYAN}"
    cat << 'EOF'
 _____ _   _ ____  _____ _______       _   _ _____ ____
| ____| \ | |  _ \| ____|__   __|____ | | | |_   _/ ___|
|  _| |  \| | | | |  _|    | |/ _ \ \ / / | | | || |
| |___| |\  | |_| | |___   | | (_) \ V /| |_| | | |
|_____|_| \_|____/|_____|  |_|\___/ \_/  \___/  |_|

                API接口安全防护措施验证工具
EOF
    echo -e "${NC}"

    log_info "开始IOE-DREAM API接口安全防护措施验证..."

    # 初始化测试报告
    init_test_report

    # 执行各项测试
    test_input_validation
    test_sql_injection_protection
    test_xss_protection
    test_csrf_protection
    test_file_upload_security
    test_path_traversal_protection
    test_api_access_control
    test_sensitive_info_leakage

    # 生成测试总结
    generate_test_summary

    # 输出测试结果
    echo -e "\n${GREEN}=== API接口安全测试完成 ===${NC}"
    echo -e "总测试项: ${TEST_RESULTS[total_tests]}"
    echo -e "通过测试: ${GREEN}${TEST_RESULTS[passed_tests]}${NC}"
    echo -e "失败测试: ${RED}${TEST_RESULTS[failed_tests]}${NC}"
    echo -e "警告测试: ${YELLOW}${TEST_RESULTS[warning_tests]}${NC}"
    echo -e "严重问题: ${RED}${TEST_RESULTS[critical_issues]}${NC}"
    echo -e "发现漏洞: ${PURPLE}${TEST_RESULTS[vulnerabilities_found]}${NC}"
    echo -e "\n详细报告: ${BLUE}${REPORT_FILE}${NC}"

    # 根据结果返回退出码
    if [[ ${TEST_RESULTS[vulnerabilities_found]} -gt 0 ]]; then
        exit 1
    elif [[ ${TEST_RESULTS[failed_tests]} -gt 0 ]]; then
        exit 2
    else
        exit 0
    fi
}

# 执行主函数
main "$@"