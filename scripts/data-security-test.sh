#!/bin/bash

# =============================================================================
# IOE-DREAM 数据传输和存储安全性验证脚本
#
# 功能描述：
# 专门测试数据传输和存储的安全性，包括加密、脱敏、备份安全等
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
readonly REPORT_FILE="${REPORTS_DIR}/data_security_test_${TIMESTAMP}.md"

# 敏感数据类型
readonly SENSITIVE_DATA_TYPES=("password" "token" "secret" "key" "creditcard" "ssn" "phone" "email" "idcard" "bankaccount")

# 创建报告目录
mkdir -p "${REPORTS_DIR}"

# 测试结果统计
declare -A TEST_RESULTS=(
    ["total_tests"]=0
    ["passed_tests"]=0
    ["failed_tests"]=0
    ["warning_tests"]=0
    ["critical_issues"]=0
    ["encryption_gaps"]=0
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
    ((TEST_RESULTS[encryption_gaps]++))
}

# 初始化测试报告
init_test_report() {
    cat > "${REPORT_FILE}" << EOF
# IOE-DREAM 数据传输和存储安全性验证报告

**测试时间**: $(date '+%Y年%m月%d日 %H:%M:%S')
**测试范围**: 数据传输和存储安全性验证
**测试版本**: v1.0.0
**测试团队**: IOE-DREAM 安全团队

---

## 📋 测试概述

本报告详细记录了IOE-DREAM微服务架构中数据传输和存储安全性的验证结果，包括：
- 数据传输加密验证
- 数据存储加密验证
- 敏感数据脱敏验证
- 数据备份安全验证
- 数据访问控制验证
- 数据生命周期管理验证

---

## 💾 测试结果详情

EOF
    log_info "数据安全验证报告初始化完成"
}

# 1. 数据传输加密验证
test_data_transmission_encryption() {
    log_info "开始数据传输加密验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 1. 数据传输加密验证" >> "${REPORT_FILE}"

    # 1.1 检查HTTPS/SSL配置
    log_info "测试HTTPS/SSL传输加密..."

    local ssl_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "ssl:\|https:\|useSSL=true" {} \; | wc -l)
    if [[ $ssl_configs -gt 0 ]]; then
        log_success "发现${ssl_configs}个服务配置了SSL/TLS"
        echo "✅ **HTTPS/SSL**: ${ssl_configs}个服务配置了SSL/TLS传输加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现SSL/TLS传输加密配置"
        echo "⚠️ **HTTPS/SSL**: 未发现SSL/TLS传输加密配置" >> "${REPORT_FILE}"
    fi

    # 1.2 检查数据库连接加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据库连接加密..."

    local db_ssl_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "useSSL=true\|ssl.*true" {} \; | wc -l)
    if [[ $db_ssl_configs -gt 0 ]]; then
        log_success "发现${db_ssl_configs}个服务配置了数据库SSL连接"
        echo "✅ **数据库SSL**: ${db_ssl_configs}个服务配置了数据库SSL连接" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据库SSL连接配置"
        echo "⚠️ **数据库SSL**: 未发现数据库SSL连接配置" >> "${REPORT_FILE}"
    fi

    # 1.3 检查Redis连接加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试Redis连接加密..."

    local redis_ssl_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "redis.*ssl\|ssl.*redis" {} \; | wc -l)
    if [[ $redis_ssl_configs -gt 0 ]]; then
        log_success "发现Redis SSL连接配置"
        echo "✅ **Redis SSL**: 已配置Redis SSL连接" >> "${REPORT_FILE}"
    else
        log_warning "未发现Redis SSL连接配置"
        echo "⚠️ **Redis SSL**: 未发现Redis SSL连接配置" >> "${REPORT_FILE}"
    fi

    # 1.4 检查消息队列加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试消息队列传输加密..."

    local mq_encryption=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "ssl.*rabbit\|ssl.*kafka\|tls.*mqtt" {} \; | wc -l)
    if [[ $mq_encryption -gt 0 ]]; then
        log_success "发现消息队列加密配置"
        echo "✅ **消息队列加密**: 已配置消息队列传输加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现消息队列加密配置"
        echo "⚠️ **消息队列加密**: 未发现消息队列加密配置" >> "${REPORT_FILE}"
    fi

    # 1.5 检查API接口加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试API接口传输加密..."

    local api_encryption=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "encryption.*api\|api.*encryption\|request.*encrypt" {} \; | wc -l)
    if [[ $api_encryption -gt 0 ]]; then
        log_success "发现API接口加密实现"
        echo "✅ **API加密**: 已实现API接口传输加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现API接口加密实现"
        echo "⚠️ **API加密**: 未发现API接口加密实现" >> "${REPORT_FILE}"
    fi

    log_info "数据传输加密验证完成"
}

# 2. 数据存储加密验证
test_data_storage_encryption() {
    log_info "开始数据存储加密验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 2. 数据存储加密验证" >> "${REPORT_FILE}"

    # 2.1 检查密码存储加密
    log_info "测试密码存储加密..."

    local password_encryption=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "BCrypt\|password.*encode\|密码加密\|PasswordEncoder" {} \; | wc -l)
    if [[ $password_encryption -gt 0 ]]; then
        log_success "发现密码存储加密实现"
        echo "✅ **密码加密**: 已实现密码存储加密" >> "${REPORT_FILE}"

        # 检查加密算法强度
        local bcrypt_usage=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "BCryptPasswordEncoder" {} \; | wc -l)
        if [[ $bcrypt_usage -gt 0 ]]; then
            log_success "使用BCrypt强加密算法"
            echo "✅ **加密算法**: 使用BCrypt强加密算法" >> "${REPORT_FILE}"
        else
            log_warning "未使用BCrypt加密算法"
            echo "⚠️ **加密算法**: 建议使用BCrypt等强加密算法" >> "${REPORT_FILE}"
        fi
    else
        log_critical "未发现密码存储加密实现"
        echo "❌ **密码加密**: 未发现密码存储加密，存在严重安全风险" >> "${REPORT_FILE}"
    fi

    # 2.2 检查敏感字段加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试敏感字段加密..."

    local sensitive_field_encryption=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "@ColumnEncrypt\|field.*encrypt\|敏感字段加密" {} \; | wc -l)
    if [[ $sensitive_field_encryption -gt 0 ]]; then
        log_success "发现敏感字段加密实现"
        echo "✅ **字段加密**: 已实现敏感字段加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现敏感字段加密实现"
        echo "⚠️ **字段加密**: 未发现敏感字段加密实现" >> "${REPORT_FILE}"
    fi

    # 2.3 检查数据库透明加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据库透明加密..."

    local tde_configs=$(find "${MICROSERVICES_DIR}" -name "*.sql" -o -name "*.yml" -exec grep -l "transparent.*data.*encryption\|TDE\|透明加密" {} \; | wc -l)
    if [[ $tde_configs -gt 0 ]]; then
        log_success "发现数据库透明加密配置"
        echo "✅ **透明加密**: 已配置数据库透明加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据库透明加密配置"
        echo "⚠️ **透明加密**: 未发现数据库透明加密配置" >> "${REPORT_FILE}"
    fi

    # 2.4 检查文件存储加密
    ((TEST_RESULTS[total_tests]++))
    log_info "测试文件存储加密..."

    local file_encryption=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "file.*encrypt\|encrypt.*file\|文件加密" {} \; | wc -l)
    if [[ $file_encryption -gt 0 ]]; then
        log_success "发现文件存储加密实现"
        echo "✅ **文件加密**: 已实现文件存储加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现文件存储加密实现"
        echo "⚠️ **文件加密**: 未发现文件存储加密实现" >> "${REPORT_FILE}"
    fi

    log_info "数据存储加密验证完成"
}

# 3. 敏感数据脱敏验证
test_data_masking() {
    log_info "开始敏感数据脱敏验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 3. 敏感数据脱敏验证" >> "${REPORT_FILE}"

    # 3.1 检查数据脱敏注解
    log_info "测试数据脱敏注解实现..."

    local masking_annotations=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "@DataMask\|@Sensitive\|@Masked\|数据脱敏" {} \; | wc -l)
    if [[ $masking_annotations -gt 0 ]]; then
        log_success "发现数据脱敏注解实现"
        echo "✅ **脱敏注解**: 已实现数据脱敏注解" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据脱敏注解实现"
        echo "⚠️ **脱敏注解**: 未发现数据脱敏注解实现" >> "${REPORT_FILE}"
    fi

    # 3.2 检查日志脱敏
    ((TEST_RESULTS[total_tests]++))
    log_info "测试日志脱敏实现..."

    local log_masking=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "log.*mask\|mask.*log\|日志脱敏" {} \; | wc -l)
    if [[ $log_masking -gt 0 ]]; then
        log_success "发现日志脱敏实现"
        echo "✅ **日志脱敏**: 已实现日志脱敏" >> "${REPORT_FILE}"
    else
        log_warning "未发现日志脱敏实现"
        echo "⚠️ **日志脱敏**: 未发现日志脱敏实现" >> "${REPORT_FILE}"
    fi

    # 3.3 检查返回结果脱敏
    ((TEST_RESULTS[total_tests]++))
    log_info "测试返回结果脱敏..."

    local response_masking=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "response.*mask\|result.*mask\|返回脱敏" {} \; | wc -l)
    if [[ $response_masking -gt 0 ]]; then
        log_success "发现返回结果脱敏实现"
        echo "✅ **返回脱敏**: 已实现返回结果脱敏" >> "${REPORT_FILE}"
    else
        log_warning "未发现返回结果脱敏实现"
        echo "⚠️ **返回脱敏**: 未发现返回结果脱敏实现" >> "${REPORT_FILE}"
    fi

    # 3.4 检查脱敏规则配置
    ((TEST_RESULTS[total_tests]++))
    log_info "测试脱敏规则配置..."

    local masking_rules=$(find "${MICROSERVICES_DIR}" -name "*.yml" -o -name "*.properties" -exec grep -l "masking.*rule\|脱敏规则" {} \; | wc -l)
    if [[ $masking_rules -gt 0 ]]; then
        log_success "发现脱敏规则配置"
        echo "✅ **脱敏规则**: 已配置数据脱敏规则" >> "${REPORT_FILE}"
    else
        log_warning "未发现脱敏规则配置"
        echo "⚠️ **脱敏规则**: 未发现脱敏规则配置" >> "${REPORT_FILE}"
    fi

    log_info "敏感数据脱敏验证完成"
}

# 4. 数据访问控制验证
test_data_access_control() {
    log_info "开始数据访问控制验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 4. 数据访问控制验证" >> "${REPORT_FILE}"

    # 4.1 检查数据库访问权限
    log_info "测试数据库访问权限控制..."

    local db_access_control=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "username.*root\|password.*root\|admin.*password" {} \; | wc -l)
    if [[ $db_access_control -gt 0 ]]; then
        log_warning "发现${db_access_control}个配置可能使用管理员账户"
        echo "⚠️ **数据库权限**: 发现可能使用管理员账户的配置" >> "${REPORT_FILE}"
    else
        log_success "未发现明显的数据库权限配置问题"
        echo "✅ **数据库权限**: 数据库权限配置相对安全" >> "${REPORT_FILE}"
    fi

    # 4.2 检查行级数据权限
    ((TEST_RESULTS[total_tests]++))
    log_info "测试行级数据权限控制..."

    local row_level_security=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "row.*level.*security\|行级权限\|data.*scope" {} \; | wc -l)
    if [[ $row_level_security -gt 0 ]]; then
        log_success "发现行级数据权限控制实现"
        echo "✅ **行级权限**: 已实现行级数据权限控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现行级数据权限控制实现"
        echo "⚠️ **行级权限**: 未发现行级数据权限控制实现" >> "${REPORT_FILE}"
    fi

    # 4.3 检查列级数据权限
    ((TEST_RESULTS[total_tests]++))
    log_info "测试列级数据权限控制..."

    local column_level_security=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "column.*level.*security\|列级权限\|field.*permission" {} \; | wc -l)
    if [[ $column_level_security -gt 0 ]]; then
        log_success "发现列级数据权限控制实现"
        echo "✅ **列级权限**: 已实现列级数据权限控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现列级数据权限控制实现"
        echo "⚠️ **列级权限**: 未发现列级数据权限控制实现" >> "${REPORT_FILE}"
    fi

    # 4.4 检查数据访问审计
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据访问审计..."

    local data_access_audit=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*access.*audit\|数据访问审计\|access.*log" {} \; | wc -l)
    if [[ $data_access_audit -gt 0 ]]; then
        log_success "发现数据访问审计实现"
        echo "✅ **访问审计**: 已实现数据访问审计" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据访问审计实现"
        echo "⚠️ **访问审计**: 未发现数据访问审计实现" >> "${REPORT_FILE}"
    fi

    log_info "数据访问控制验证完成"
}

# 5. 数据备份安全验证
test_data_backup_security() {
    log_info "开始数据备份安全验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 5. 数据备份安全验证" >> "${REPORT_FILE}"

    # 5.1 检查备份加密
    log_info "测试数据备份加密..."

    local backup_encryption=$(find "${MICROSERVICES_DIR}" -name "*.yml" -o -name "*.sh" -exec grep -l "backup.*encrypt\|加密备份\|backup.*ssl" {} \; | wc -l)
    if [[ $backup_encryption -gt 0 ]]; then
        log_success "发现备份加密实现"
        echo "✅ **备份加密**: 已实现数据备份加密" >> "${REPORT_FILE}"
    else
        log_warning "未发现备份加密实现"
        echo "⚠️ **备份加密**: 未发现数据备份加密实现" >> "${REPORT_FILE}"
    fi

    # 5.2 检查备份存储安全
    ((TEST_RESULTS[total_tests]++))
    log_info "测试备份存储安全..."

    local backup_storage_security=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "backup.*secure\|安全存储\|backup.*access" {} \; | wc -l)
    if [[ $backup_storage_security -gt 0 ]]; then
        log_success "发现备份存储安全配置"
        echo "✅ **备份存储**: 已配置安全的备份存储" >> "${REPORT_FILE}"
    else
        log_warning "未发现备份存储安全配置"
        echo "⚠️ **备份存储**: 未发现备份存储安全配置" >> "${REPORT_FILE}"
    fi

    # 5.3 检查备份访问控制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试备份访问控制..."

    local backup_access_control=$(find "${MICROSERVICES_DIR}" -name "*.yml" -o -name "*.sh" -exec grep -l "backup.*permission\|backup.*auth\|备份权限" {} \; | wc -l)
    if [[ $backup_access_control -gt 0 ]]; then
        log_success "发现备份访问控制实现"
        echo "✅ **备份权限**: 已实现备份访问控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现备份访问控制实现"
        echo "⚠️ **备份权限**: 未发现备份访问控制实现" >> "${REPORT_FILE}"
    fi

    # 5.4 检查备份恢复安全
    ((TEST_RESULTS[total_tests]++))
    log_info "测试备份恢复安全..."

    local backup_recovery_security=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "recovery.*security\|恢复安全\|restore.*auth" {} \; | wc -l)
    if [[ $backup_recovery_security -gt 0 ]]; then
        log_success "发现备份恢复安全实现"
        echo "✅ **恢复安全**: 已实现备份恢复安全" >> "${REPORT_FILE}"
    else
        log_warning "未发现备份恢复安全实现"
        echo "⚠️ **恢复安全**: 未发现备份恢复安全实现" >> "${REPORT_FILE}"
    fi

    log_info "数据备份安全验证完成"
}

# 6. 数据生命周期管理验证
test_data_lifecycle_management() {
    log_info "开始数据生命周期管理验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 6. 数据生命周期管理验证" >> "${REPORT_FILE}"

    # 6.1 检查数据保留策略
    log_info "测试数据保留策略..."

    local data_retention=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*retention\|保留策略\|retention.*policy" {} \; | wc -l)
    if [[ $data_retention -gt 0 ]]; then
        log_success "发现数据保留策略实现"
        echo "✅ **保留策略**: 已实现数据保留策略" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据保留策略实现"
        echo "⚠️ **保留策略**: 未发现数据保留策略实现" >> "${REPORT_FILE}"
    fi

    # 6.2 检查数据清理机制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据清理机制..."

    local data_cleanup=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*cleanup\|数据清理\|cleanup.*job" {} \; | wc -l)
    if [[ $data_cleanup -gt 0 ]]; then
        log_success "发现数据清理机制实现"
        echo "✅ **数据清理**: 已实现数据清理机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据清理机制实现"
        echo "⚠️ **数据清理**: 未发现数据清理机制实现" >> "${REPORT_FILE}"
    fi

    # 6.3 检查数据归档策略
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据归档策略..."

    local data_archiving=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*archive\|数据归档\|archive.*policy" {} \; | wc -l)
    if [[ $data_archiving -gt 0 ]]; then
        log_success "发现数据归档策略实现"
        echo "✅ **数据归档**: 已实现数据归档策略" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据归档策略实现"
        echo "⚠️ **数据归档**: 未发现数据归档策略实现" >> "${REPORT_FILE}"
    fi

    # 6.4 检查数据销毁机制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据销毁机制..."

    local data_destruction=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*destroy\|数据销毁\|secure.*delete" {} \; | wc -l)
    if [[ $data_destruction -gt 0 ]]; then
        log_success "发现数据销毁机制实现"
        echo "✅ **数据销毁**: 已实现安全数据销毁机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据销毁机制实现"
        echo "⚠️ **数据销毁**: 未发现安全数据销毁机制实现" >> "${REPORT_FILE}"
    fi

    log_info "数据生命周期管理验证完成"
}

# 7. 敏感数据识别和分类
test_sensitive_data_identification() {
    log_info "开始敏感数据识别和分类验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 7. 敏感数据识别和分类验证" >> "${REPORT_FILE}"

    # 7.1 检查敏感数据标记
    log_info "测试敏感数据标记实现..."

    local sensitive_data_annotation=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "@SensitiveData\|@Classified\|敏感数据" {} \; | wc -l)
    if [[ $sensitive_data_annotation -gt 0 ]]; then
        log_success "发现敏感数据标记实现"
        echo "✅ **敏感数据标记**: 已实现敏感数据标记" >> "${REPORT_FILE}"
    else
        log_warning "未发现敏感数据标记实现"
        echo "⚠️ **敏感数据标记**: 未发现敏感数据标记实现" >> "${REPORT_FILE}"
    fi

    # 7.2 检查数据分类管理
    ((TEST_RESULTS[total_tests]++))
    log_info "测试数据分类管理..."

    local data_classification=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*classification\|数据分类\|classification.*level" {} \; | wc -l)
    if [[ $data_classification -gt 0 ]]; then
        log_success "发现数据分类管理实现"
        echo "✅ **数据分类**: 已实现数据分类管理" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据分类管理实现"
        echo "⚠️ **数据分类**: 未发现数据分类管理实现" >> "${REPORT_FILE}"
    fi

    # 7.3 检查PII数据处理
    ((TEST_RESULTS[total_tests]++))
    log_info "测试PII(个人身份信息)数据处理..."

    local pii_processing=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "PII\|personal.*information\|个人身份信息" {} \; | wc -l)
    if [[ $pii_processing -gt 0 ]]; then
        log_success "发现PII数据处理实现"
        echo "✅ **PII处理**: 已实现PII数据处理" >> "${REPORT_FILE}"
    else
        log_warning "未发现PII数据处理实现"
        echo "⚠️ **PII处理**: 未发现PII数据处理实现" >> "${REPORT_FILE}"
    fi

    log_info "敏感数据识别和分类验证完成"
}

# 8. 加密密钥管理验证
test_encryption_key_management() {
    log_info "开始加密密钥管理验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 8. 加密密钥管理验证" >> "${REPORT_FILE}"

    # 8.1 检查密钥存储安全
    log_info "测试密钥存储安全..."

    local key_storage_security=$(find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "jks\|keystore\|key.*store" {} \; | wc -l)
    if [[ $key_storage_security -gt 0 ]]; then
        log_success "发现密钥存储安全实现"
        echo "✅ **密钥存储**: 已实现安全的密钥存储" >> "${REPORT_FILE}"
    else
        log_warning "未发现密钥存储安全实现"
        echo "⚠️ **密钥存储**: 未发现密钥存储安全实现" >> "${REPORT_FILE}"
    fi

    # 8.2 检查密钥轮换机制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试密钥轮换机制..."

    local key_rotation=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "key.*rotation\|密钥轮换\|rotate.*key" {} \; | wc -l)
    if [[ $key_rotation -gt 0 ]]; then
        log_success "发现密钥轮换机制实现"
        echo "✅ **密钥轮换**: 已实现密钥轮换机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现密钥轮换机制实现"
        echo "⚠️ **密钥轮换**: 未发现密钥轮换机制实现" >> "${REPORT_FILE}"
    fi

    # 8.3 检查密钥访问控制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试密钥访问控制..."

    local key_access_control=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "key.*access\|密钥权限\|key.*permission" {} \; | wc -l)
    if [[ $key_access_control -gt 0 ]]; then
        log_success "发现密钥访问控制实现"
        echo "✅ **密钥权限**: 已实现密钥访问控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现密钥访问控制实现"
        echo "⚠️ **密钥权限**: 未发现密钥访问控制实现" >> "${REPORT_FILE}"
    fi

    # 8.4 检查密钥生命周期管理
    ((TEST_RESULTS[total_tests]++))
    log_info "测试密钥生命周期管理..."

    local key_lifecycle=$(find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "key.*lifecycle\|密钥生命周期\|key.*management" {} \; | wc -l)
    if [[ $key_lifecycle -gt 0 ]]; then
        log_success "发现密钥生命周期管理实现"
        echo "✅ **密钥生命周期**: 已实现密钥生命周期管理" >> "${REPORT_FILE}"
    else
        log_warning "未发现密钥生命周期管理实现"
        echo "⚠️ **密钥生命周期**: 未发现密钥生命周期管理实现" >> "${REPORT_FILE}"
    fi

    log_info "加密密钥管理验证完成"
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
    local encryption_gaps=${TEST_RESULTS[encryption_gaps]}

    # 计算通过率
    local pass_rate=0
    if [[ $total_tests -gt 0 ]]; then
        pass_rate=$((passed_tests * 100 / total_tests))
    fi

    cat >> "${REPORT_FILE}" << EOF

### 📈 数据安全测试统计

| 测试指标 | 数值 | 说明 |
|----------|------|------|
| **总测试项** | ${total_tests} | 数据安全测试总项目数 |
| **通过测试** | ${passed_tests} | 符合安全要求的测试项目 |
| **失败测试** | ${failed_tests} | 不符合安全要求的测试项目 |
| **警告测试** | ${warning_tests} | 需要改进的测试项目 |
| **严重问题** | ${critical_issues} | 严重安全问题数量 |
| **加密缺口** | ${encryption_gaps} | 加密保护不足的数量 |

### 🎯 数据安全评分

**数据传输和存储安全评分: ${pass_rate}/100**

### 🛡️ 数据安全等级评估

EOF

    # 根据评分给出安全等级
    local security_level="优秀"
    if [[ $pass_rate -lt 60 ]]; then
        security_level="需要重大改进"
        echo "🔴 **数据安全等级**: ${security_level} - 数据安全存在严重问题" >> "${REPORT_FILE}"
    elif [[ $pass_rate -lt 80 ]]; then
        security_level="良好"
        echo "🟡 **数据安全等级**: ${security_level} - 数据安全需要完善" >> "${REPORT_FILE}"
    else
        echo "🟢 **数据安全等级**: ${security_level} - 数据安全防护良好" >> "${REPORT_FILE}"
    fi

    cat >> "${REPORT_FILE}" << EOF

### 🚨 关键数据安全风险

#### 高风险问题
1. **密码存储未加密**: 严重安全隐患，可能导致账户泄露
2. **数据传输未加密**: 可能被中间人攻击窃取数据
3. **敏感字段未脱敏**: 可能导致敏感信息泄露
4. **备份未加密**: 备份数据可能被恶意访问

#### 中风险问题
1. **密钥管理不完善**: 可能导致加密密钥泄露
2. **数据访问控制不足**: 可能导致越权访问
3. **日志脱敏缺失**: 日志中可能包含敏感信息
4. **生命周期管理缺失**: 数据可能长期滞留系统

### 🔧 关键修复建议

#### 立即修复 (高优先级)
1. **实现密码加密存储**: 使用BCrypt等强加密算法
2. **启用HTTPS传输**: 所有数据传输使用SSL/TLS加密
3. **实现敏感字段加密**: 对身份证、手机号等敏感字段加密
4. **实现数据脱敏**: 日志和返回结果中的敏感数据脱敏

#### 短期修复 (中优先级)
1. **完善备份加密**: 实现备份数据的加密存储
2. **实现数据分类**: 建立敏感数据分类和标记机制
3. **加强密钥管理**: 实现密钥的安全存储和轮换
4. **完善访问控制**: 实现行级和列级数据权限控制

#### 长期改进 (低优先级)
1. **数据生命周期管理**: 建立完整的数据生命周期管理
2. **数据丢失防护**: 实现DLP系统防止数据泄露
3. **隐私合规**: 确保符合GDPR等隐私保护法规
4. **数据安全审计**: 建立定期数据安全审计机制

### ✅ 数据安全检查清单

- [ ] 所有数据传输都使用HTTPS/SSL加密
- [ ] 数据库连接启用SSL/TLS
- [ ] 密码使用BCrypt或更强的加密算法
- [ ] 敏感字段已加密存储
- [ ] 实现了数据脱敏机制
- [ ] 日志中敏感信息已脱敏
- [ ] 备份数据已加密
- [ ] 实现了数据访问权限控制
- [ ] 建立了敏感数据分类机制
- [ ] 实现了密钥安全管理
- [ ] 建立了数据生命周期管理
- [ ] 实现了数据访问审计

### 🔒 数据安全最佳实践

#### 加密策略
1. **传输加密**: 所有外部通信使用TLS 1.2+
2. **存储加密**: 敏感数据使用AES-256加密
3. **密钥管理**: 使用HSM或KMS管理加密密钥
4. **密钥轮换**: 定期轮换加密密钥

#### 访问控制
1. **最小权限原则**: 用户只能访问必要的最小数据
2. **数据分级**: 根据敏感性对数据进行分级保护
3. **审计日志**: 记录所有数据访问和修改操作
4. **定期审查**: 定期审查数据访问权限

#### 合规要求
1. **隐私保护**: 遵循GDPR、CCPA等隐私法规
2. **数据保留**: 建立合规的数据保留策略
3. **跨境传输**: 确保跨境数据传输符合法规要求
4. **用户权利**: 支持用户的数据访问、修改、删除权利

EOF

    # 根据测试结果给出总体评价
    if [[ $encryption_gaps -gt 0 ]]; then
        echo -e "⚠️ **总体评价**: 发现${encryption_gaps}个加密保护缺口，数据安全需要加强" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 80 ]]; then
        echo -e "✅ **总体评价**: 数据传输和存储安全防护完善，安全性良好" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 60 ]]; then
        echo -e "⚠️ **总体评价**: 数据安全防护基本到位，需要进一步完善" >> "${REPORT_FILE}"
    else
        echo -e "❌ **总体评价**: 数据安全防护存在较多问题，需要全面改进" >> "${REPORT_FILE}"
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

              数据传输和存储安全性验证工具
EOF
    echo -e "${NC}"

    log_info "开始IOE-DREAM数据传输和存储安全性验证..."

    # 初始化测试报告
    init_test_report

    # 执行各项测试
    test_data_transmission_encryption
    test_data_storage_encryption
    test_data_masking
    test_data_access_control
    test_data_backup_security
    test_data_lifecycle_management
    test_sensitive_data_identification
    test_encryption_key_management

    # 生成测试总结
    generate_test_summary

    # 输出测试结果
    echo -e "\n${GREEN}=== 数据安全验证完成 ===${NC}"
    echo -e "总测试项: ${TEST_RESULTS[total_tests]}"
    echo -e "通过测试: ${GREEN}${TEST_RESULTS[passed_tests]}${NC}"
    echo -e "失败测试: ${RED}${TEST_RESULTS[failed_tests]}${NC}"
    echo -e "警告测试: ${YELLOW}${TEST_RESULTS[warning_tests]}${NC}"
    echo -e "严重问题: ${RED}${TEST_RESULTS[critical_issues]}${NC}"
    echo -e "加密缺口: ${PURPLE}${TEST_RESULTS[encryption_gaps]}${NC}"
    echo -e "\n详细报告: ${BLUE}${REPORT_FILE}${NC}"

    # 根据结果返回退出码
    if [[ ${TEST_RESULTS[encryption_gaps]} -gt 0 ]]; then
        exit 1
    elif [[ ${TEST_RESULTS[failed_tests]} -gt 0 ]]; then
        exit 2
    else
        exit 0
    fi
}

# 执行主函数
main "$@"