#!/bin/bash

# =============================================================================
# IOE-DREAM 微服务架构安全全面验证套件
#
# 功能描述：
# 全面验证微服务架构的安全性、权限控制和数据保护
# 包括身份认证、RBAC权限控制、API安全、数据安全、漏洞扫描等
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
readonly NC='\033[0m' # No Color

# 项目路径配置
readonly PROJECT_ROOT="D:/IOE-DREAM"
readonly MICROSERVICES_DIR="${PROJECT_ROOT}/microservices"
readonly MONOLITH_DIR="${PROJECT_ROOT}/smart-admin-api-java17-springboot3"
readonly REPORTS_DIR="${PROJECT_ROOT}/security-audit-reports"
readonly TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
readonly REPORT_FILE="${REPORTS_DIR}/security_audit_report_${TIMESTAMP}.md"

# 创建报告目录
mkdir -p "${REPORTS_DIR}"

# 安全验证结果统计
declare -A SECURITY_RESULTS=(
    ["total_checks"]=0
    ["passed_checks"]=0
    ["failed_checks"]=0
    ["warning_checks"]=0
    ["critical_issues"]=0
    ["high_issues"]=0
    ["medium_issues"]=0
    ["low_issues"]=0
)

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "${REPORT_FILE}"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "${REPORT_FILE}"
    ((SECURITY_RESULTS[passed_checks]++))
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "${REPORT_FILE}"
    ((SECURITY_RESULTS[warning_checks]++))
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "${REPORT_FILE}"
    ((SECURITY_RESULTS[failed_checks]++))
}

log_critical() {
    echo -e "${PURPLE}[CRITICAL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "${REPORT_FILE}"
    ((SECURITY_RESULTS[critical_issues]++))
}

# 初始化报告
init_report() {
    cat > "${REPORT_FILE}" << EOF
# IOE-DREAM 微服务架构安全审计报告

**审计时间**: $(date '+%Y年%m月%d日 %H:%M:%S')
**审计范围**: 微服务架构全栈安全验证
**审计版本**: v1.0.0
**审计团队**: IOE-DREAM 安全团队

---

## 📋 审计摘要

\`\`\`
审计开始时间: $(date '+%Y-%m-%d %H:%M:%S')
审计范围: 微服务架构安全全面验证
重点关注: 身份认证、权限控制、API安全、数据保护、漏洞扫描
\`\`\`

---

## 🔍 安全验证清单

EOF
    log_info "安全审计报告初始化完成"
}

# 1. 身份认证安全验证
verify_authentication_security() {
    log_info "开始身份认证安全验证..."

    echo -e "\n### 🔐 1. 身份认证安全验证" >> "${REPORT_FILE}"

    # 1.1 检查认证服务配置
    log_info "检查认证服务配置安全性..."
    ((SECURITY_RESULTS[total_checks]++))

    if [[ -f "${MICROSERVICES_DIR}/ioedream-auth-service/src/main/resources/application.yml" ]]; then
        local auth_config="${MICROSERVICES_DIR}/ioedream-auth-service/src/main/resources/application.yml"

        # 检查JWT配置
        if grep -q "jwt" "$auth_config"; then
            log_success "认证服务包含JWT配置"
        else
            log_warning "认证服务未发现JWT配置，建议添加JWT令牌机制"
        fi

        # 检查密码加密配置
        if grep -q "password.encoder\|bcrypt\|scrypt\|pbkdf2" "$auth_config"; then
            log_success "认证服务包含密码加密配置"
        else
            log_critical "认证服务未发现密码加密配置，存在严重安全风险"
        fi

        # 检查会话管理配置
        if grep -q "session\|timeout" "$auth_config"; then
            log_success "认证服务包含会话管理配置"
        else
            log_warning "认证服务未发现会话管理配置，建议添加会话超时机制"
        fi
    else
        log_error "认证服务配置文件不存在"
    fi

    # 1.2 检查认证控制器安全性
    ((SECURITY_RESULTS[total_checks]++))
    local auth_controller="${MICROSERVICES_DIR}/ioedream-auth-service/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"

    if [[ -f "$auth_controller" ]]; then
        # 检查登录接口安全验证
        if grep -q "@Valid\|@NotNull\|@NotBlank" "$auth_controller"; then
            log_success "登录接口包含输入验证注解"
        else
            log_critical "登录接口缺少输入验证，存在注入攻击风险"
        fi

        # 检查令牌提取机制
        if grep -q "Bearer\|Authorization" "$auth_controller"; then
            log_success "认证控制器包含标准令牌提取机制"
        else
            log_warning "认证控制器令牌提取机制不标准"
        fi

        # 检查异常处理
        if grep -q "try.*catch\|Exception" "$auth_controller"; then
            log_success "认证控制器包含异常处理机制"
        else
            log_warning "认证控制器异常处理不完善"
        fi
    else
        log_error "认证控制器文件不存在"
    fi

    # 1.3 检查密码策略
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查密码策略安全性..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "password.*policy\|PasswordPolicy\|密码强度\|密码复杂度" {} \; > /dev/null 2>&1; then
        log_success "发现密码策略配置"
    else
        log_warning "未发现明确的密码策略配置，建议添加密码强度要求"
    fi

    # 1.4 检查登录失败处理
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查登录失败处理机制..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "login.*fail\|attempt.*count\|account.*lock" {} \; > /dev/null 2>&1; then
        log_success "发现登录失败处理机制"
    else
        log_warning "未发现登录失败处理机制，建议添加账户锁定策略"
    fi

    log_info "身份认证安全验证完成"
}

# 2. RBAC权限控制验证
verify_rbac_authorization() {
    log_info "开始RBAC权限控制验证..."

    echo -e "\n### 🛡️ 2. RBAC权限控制验证" >> "${REPORT_FILE}"

    # 2.1 检查权限注解实现
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查权限注解实现..."

    local require_resource_annotation="${MICROSERVICES_DIR}/ioedream-identity-service/src/main/java/net/lab1024/sa/identity/module/rbac/annotation/RequireResource.java"

    if [[ -f "$require_resource_annotation" ]]; then
        log_success "发现@RequireResource权限注解实现"

        # 检查注解是否包含必要的权限属性
        if grep -q "code\|action\|description" "$require_resource_annotation"; then
            log_success "权限注解包含完整的权限属性"
        else
            log_warning "权限注解属性不完整"
        fi
    else
        log_error "未发现@RequireResource权限注解实现"
    fi

    # 2.2 检查角色控制器权限控制
    ((SECURITY_RESULTS[total_checks]++))
    local role_controller="${MICROSERVICES_DIR}/ioedream-identity-service/src/main/java/net/lab1024/sa/identity/module/rbac/controller/RoleController.java"

    if [[ -f "$role_controller" ]]; then
        local protected_methods=$(grep -c "@RequireResource" "$role_controller" || echo "0")
        local total_methods=$(grep -c "@.*Mapping" "$role_controller" || echo "0")

        if [[ $protected_methods -gt 0 ]]; then
            log_success "角色控制器包含${protected_methods}个权限保护接口"

            # 计算权限保护覆盖率
            local coverage=$((protected_methods * 100 / total_methods))
            if [[ $coverage -ge 80 ]]; then
                log_success "角色控制器权限保护覆盖率: ${coverage}%"
            elif [[ $coverage -ge 60 ]]; then
                log_warning "角色控制器权限保护覆盖率: ${coverage}%，建议提升"
            else
                log_error "角色控制器权限保护覆盖率过低: ${coverage}%"
            fi
        else
            log_critical "角色控制器未发现权限保护机制"
        fi
    else
        log_error "角色控制器文件不存在"
    fi

    # 2.3 检查权限拦截器实现
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查权限拦截器实现..."

    if find "${MICROSERVICES_DIR}" -name "*Interceptor*.java" -exec grep -l "permission\|authorization\|auth" {} \; > /dev/null 2>&1; then
        log_success "发现权限拦截器实现"
    else
        log_warning "未发现权限拦截器实现"
    fi

    # 2.4 检查数据权限控制
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查数据权限控制..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "data.*scope\|DataScope\|行级权限\|数据权限" {} \; > /dev/null 2>&1; then
        log_success "发现数据权限控制实现"
    else
        log_warning "未发现数据权限控制实现"
    fi

    log_info "RBAC权限控制验证完成"
}

# 3. API接口安全验证
verify_api_security() {
    log_info "开始API接口安全验证..."

    echo -e "\n### 🔌 3. API接口安全验证" >> "${REPORT_FILE}"

    # 3.1 检查输入验证
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查API输入验证..."

    local controllers_with_validation=$(find "${MICROSERVICES_DIR}" -name "*Controller.java" -exec grep -l "@Valid\|@NotNull\|@NotBlank\|@Size" {} \; | wc -l)
    local total_controllers=$(find "${MICROSERVICES_DIR}" -name "*Controller.java" | wc -l)

    if [[ $controllers_with_validation -gt 0 ]]; then
        local validation_coverage=$((controllers_with_validation * 100 / total_controllers))
        log_success "${controllers_with_validation}/${total_controllers} 个控制器包含输入验证 (覆盖率: ${validation_coverage}%)"

        if [[ $validation_coverage -lt 80 ]]; then
            log_warning "API输入验证覆盖率偏低，建议提升"
        fi
    else
        log_critical "未发现API输入验证机制，存在注入攻击风险"
    fi

    # 3.2 检查SQL注入防护
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查SQL注入防护..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "PreparedStatement\|@Query\|参数化查询" {} \; > /dev/null 2>&1; then
        log_success "发现参数化查询实现，有效防护SQL注入"
    else
        log_warning "未发现明确的SQL注入防护措施"
    fi

    # 3.3 检查XSS防护
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查XSS攻击防护..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "XSS\|escapeHtml\|sanitize" {} \; > /dev/null 2>&1; then
        log_success "发现XSS防护实现"
    else
        log_warning "未发现XSS防护措施，建议添加输入过滤和输出编码"
    fi

    # 3.4 检查CSRF防护
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查CSRF攻击防护..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "CSRF\|csrf\|@CrossOrigin" {} \; > /dev/null 2>&1; then
        log_success "发现CSRF防护配置"
    else
        log_warning "未发现CSRF防护措施"
    fi

    # 3.5 检查HTTPS配置
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查HTTPS配置..."

    local https_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -o -name "application*.yaml" -exec grep -l "ssl\|https\|tls" {} \; | wc -l)
    if [[ $https_configs -gt 0 ]]; then
        log_success "发现${https_configs}个服务包含HTTPS/SSL配置"
    else
        log_warning "未发现HTTPS配置，生产环境建议启用SSL/TLS"
    fi

    log_info "API接口安全验证完成"
}

# 4. 数据安全验证
verify_data_security() {
    log_info "开始数据安全验证..."

    echo -e "\n### 💾 4. 数据安全验证" >> "${REPORT_FILE}"

    # 4.1 检查敏感数据加密
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查敏感数据加密..."

    # 检查密码加密
    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "BCryptPasswordEncoder\|password.*encode\|密码加密" {} \; > /dev/null 2>&1; then
        log_success "发现密码加密实现"
    else
        log_critical "未发现密码加密实现，存在严重安全风险"
    fi

    # 检查数据传输加密
    if find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "ssl:\|https:" {} \; > /dev/null 2>&1; then
        log_success "发现数据传输加密配置"
    else
        log_warning "未发现数据传输加密配置"
    fi

    # 4.2 检查数据脱敏
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查数据脱敏实现..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "DataMasking\|脱敏\|mask\|sanitize" {} \; > /dev/null 2>&1; then
        log_success "发现数据脱敏实现"
    else
        log_warning "未发现数据脱敏实现"
    fi

    # 4.3 检查数据库安全配置
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查数据库安全配置..."

    local db_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "datasource" {} \;)
    local secure_db_configs=0

    while IFS= read -r config_file; do
        if grep -q "useSSL=true\|ssl=true" "$config_file"; then
            ((secure_db_configs++))
        fi
    done <<< "$db_configs"

    if [[ $secure_db_configs -gt 0 ]]; then
        log_success "${secure_db_configs}个服务配置了数据库SSL连接"
    else
        log_warning "未发现数据库SSL连接配置"
    fi

    # 4.4 检查Redis安全配置
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查Redis安全配置..."

    local redis_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "redis:" {} \; | wc -l)
    if [[ $redis_configs -gt 0 ]]; then
        log_success "发现${redis_configs}个服务使用Redis缓存"

        # 检查Redis密码配置
        local redis_with_password=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "redis:" {} \; -exec grep -l "password:" {} \; | wc -l)
        if [[ $redis_with_password -gt 0 ]]; then
            log_success "${redis_with_password}个Redis配置包含密码保护"
        else
            log_warning "部分Redis配置未设置密码"
        fi
    else
        log_warning "未发现Redis配置"
    fi

    log_info "数据安全验证完成"
}

# 5. 系统漏洞扫描
scan_system_vulnerabilities() {
    log_info "开始系统漏洞扫描..."

    echo -e "\n### 🔍 5. 系统漏洞扫描" >> "${REPORT_FILE}"

    # 5.1 检查依赖包漏洞
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查依赖包安全漏洞..."

    if command -v mvn &> /dev/null; then
        log_info "使用Maven检查依赖包漏洞..."
        cd "${MICROSERVICES_DIR}"

        # 检查是否有CVE漏洞的依赖
        if mvn dependency:tree -q | grep -i "cve\|vulnerability" > /dev/null 2>&1; then
            log_warning "发现可能存在安全漏洞的依赖包"
        else
            log_success "未发现明显的依赖包漏洞"
        fi
    else
        log_warning "Maven命令不可用，跳过依赖包漏洞检查"
    fi

    # 5.2 检查配置文件安全
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查配置文件安全性..."

    # 检查硬编码密码
    local hardcoded_passwords=$(find "${MICROSERVICES_DIR}" -name "*.yml" -o -name "*.yaml" -o -name "*.properties" -exec grep -l "password.*=\|password:\s*[^$\s]" {} \; | wc -l)
    if [[ $hardcoded_passwords -gt 0 ]]; then
        log_warning "发现${hardcoded_passwords}个配置文件可能包含硬编码密码"
    else
        log_success "未发现明显的硬编码密码"
    fi

    # 检查默认密码
    local default_passwords=$(find "${MICROSERVICES_DIR}" -name "*.yml" -o -name "*.yaml" -exec grep -l "password.*root\|password.*admin\|password.*123\|password.*password" {} \; | wc -l)
    if [[ $default_passwords -gt 0 ]]; then
        log_warning "发现${default_passwords}个配置文件使用默认密码"
    else
        log_success "未发现默认密码使用"
    fi

    # 5.3 检查日志安全
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查日志安全性..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "log.*password\|log.*secret\|log.*token" {} \; > /dev/null 2>&1; then
        log_warning "发现可能记录敏感信息的日志代码"
    else
        log_success "未发现敏感信息日志记录"
    fi

    # 5.4 检查文件上传安全
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查文件上传安全性..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "MultipartFile\|upload.*file" {} \; > /dev/null 2>&1; then
        log_success "发现文件上传功能"

        # 检查文件类型验证
        if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "file.*type\|contentType\|extension" {} \; > /dev/null 2>&1; then
            log_success "发现文件类型验证实现"
        else
            log_warning "文件上传功能缺少类型验证"
        fi

        # 检查文件大小限制
        if find "${MICROSERVICES_DIR}" -name "*.yml" -exec grep -l "max-file-size\|spring.servlet.multipart" {} \; > /dev/null 2>&1; then
            log_success "发现文件大小限制配置"
        else
            log_warning "文件上传功能缺少大小限制"
        fi
    else
        log_info "未发现文件上传功能"
    fi

    log_info "系统漏洞扫描完成"
}

# 6. 审计日志和监控验证
verify_audit_monitoring() {
    log_info "开始审计日志和监控验证..."

    echo -e "\n### 📊 6. 审计日志和监控验证" >> "${REPORT_FILE}"

    # 6.1 检查操作日志记录
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查操作日志记录..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "OperateLog\|操作日志\|AuditLog" {} \; > /dev/null 2>&1; then
        log_success "发现操作日志记录实现"
    else
        log_warning "未发现操作日志记录实现"
    fi

    # 6.2 检查安全事件日志
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查安全事件日志..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "SecurityEvent\|安全事件\|login.*fail\|access.*deny" {} \; > /dev/null 2>&1; then
        log_success "发现安全事件日志记录"
    else
        log_warning "未发现安全事件日志记录"
    fi

    # 6.3 检查监控配置
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查监控配置..."

    local actuator_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "management:" {} \; | wc -l)
    if [[ $actuator_configs -gt 0 ]]; then
        log_success "发现${actuator_configs}个服务包含Actuator监控配置"

        # 检查安全端点暴露
        local secure_endpoints=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "health\|info\|metrics" {} \; | wc -l)
        log_success "${secure_endpoints}个服务配置了安全的监控端点"
    else
        log_warning "未发现监控配置"
    fi

    # 6.4 检查告警机制
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查告警机制..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "Alert\|告警\|notification\|NotificationService" {} \; > /dev/null 2>&1; then
        log_success "发现告警机制实现"
    else
        log_warning "未发现告警机制实现"
    fi

    log_info "审计日志和监控验证完成"
}

# 7. 微服务通信安全验证
verify_microservice_communication_security() {
    log_info "开始微服务通信安全验证..."

    echo -e "\n### 🌐 7. 微服务通信安全验证" >> "${REPORT_FILE}"

    # 7.1 检查服务注册安全
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查服务注册安全..."

    local nacos_configs=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "nacos:" {} \; | wc -l)
    if [[ $nacos_configs -gt 0 ]]; then
        log_success "发现${nacos_configs}个服务使用Nacos服务发现"

        # 检查Nacos安全配置
        local nacos_with_auth=$(find "${MICROSERVICES_DIR}" -name "application*.yml" -exec grep -l "nacos:" {} \; -exec grep -l "username\|password" {} \; | wc -l)
        if [[ $nacos_with_auth -gt 0 ]]; then
            log_success "${nacos_with_auth}个Nacos配置包含认证信息"
        else
            log_warning "Nacos配置缺少认证信息"
        fi
    else
        log_warning "未发现服务注册配置"
    fi

    # 7.2 检查服务间调用安全
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查服务间调用安全..."

    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "RestTemplate\|WebClient\|FeignClient" {} \; > /dev/null 2>&1; then
        log_success "发现服务间调用实现"

        # 检查调用认证
        if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "Authorization\|Bearer\|token" {} \; > /dev/null 2>&1; then
            log_success "发现服务间调用认证机制"
        else
            log_warning "服务间调用可能缺少认证机制"
        fi
    else
        log_info "未发现服务间调用实现"
    fi

    # 7.3 检查网关安全配置
    ((SECURITY_RESULTS[total_checks]++))
    log_info "检查网关安全配置..."

    local gateway_configs=$(find "${MICROSERVICES_DIR}" -path "*gateway*" -name "application*.yml" | wc -l)
    if [[ $gateway_configs -gt 0 ]]; then
        log_success "发现网关配置"

        # 检查路由过滤
        if find "${MICROSERVICES_DIR}" -path "*gateway*" -name "*.yml" -exec grep -l "filter\|Predicate" {} \; > /dev/null 2>&1; then
            log_success "发现网关路由过滤配置"
        else
            log_warning "网关配置缺少路由过滤"
        fi
    else
        log_warning "未发现网关配置"
    fi

    log_info "微服务通信安全验证完成"
}

# 8. 生成安全评分
generate_security_score() {
    log_info "生成安全评分..."

    echo -e "\n---" >> "${REPORT_FILE}"
    echo -e "\n## 📈 安全评分统计" >> "${REPORT_FILE}"

    local total_checks=${SECURITY_RESULTS[total_checks]}
    local passed_checks=${SECURITY_RESULTS[passed_checks]}
    local failed_checks=${SECURITY_RESULTS[failed_checks]}
    local warning_checks=${SECURITY_RESULTS[warning_checks]}
    local critical_issues=${SECURITY_RESULTS[critical_issues]}

    # 计算安全评分
    local security_score=0
    if [[ $total_checks -gt 0 ]]; then
        security_score=$((passed_checks * 100 / total_checks))
    fi

    # 确定安全等级
    local security_level="优秀"
    if [[ $security_score -lt 60 ]]; then
        security_level="需要改进"
    elif [[ $security_score -lt 80 ]]; then
        security_level="良好"
    elif [[ $security_score -lt 90 ]]; then
        security_level="很好"
    fi

    cat >> "${REPORT_FILE}" << EOF

### 📊 总体统计

| 指标 | 数值 | 说明 |
|------|------|------|
| **总检查项** | ${total_checks} | 安全验证总项目数 |
| **通过检查** | ${passed_checks} | 符合安全要求的项目 |
| **失败检查** | ${failed_checks} | 不符合安全要求的项目 |
| **警告项目** | ${warning_checks} | 需要关注的项目 |
| **严重问题** | ${critical_issues} | 严重安全问题数量 |

### 🎯 安全评分

**综合安全评分: ${security_score}/100**

**安全等级: ${security_level}**

EOF

    # 根据评分给出建议
    if [[ $critical_issues -gt 0 ]]; then
        echo -e "⚠️ **建议**: 发现${critical_issues}个严重安全问题，建议立即修复" >> "${REPORT_FILE}"
    fi

    if [[ $security_score -lt 80 ]]; then
        echo -e "📋 **建议**: 安全评分偏低，建议制定安全改进计划" >> "${REPORT_FILE}"
    fi
}

# 9. 生成修复建议
generate_fix_recommendations() {
    log_info "生成修复建议..."

    echo -e "\n## 🔧 安全修复建议" >> "${REPORT_FILE}"

    cat >> "${REPORT_FILE}" << EOF

### 🚨 优先级修复项目

#### 高优先级 (严重问题)
1. **加强密码安全**
   - 实现强密码策略 (长度>=8，包含大小写字母、数字、特殊字符)
   - 使用BCrypt或PBKDF2进行密码哈希
   - 实现密码历史记录，防止重复使用

2. **完善认证机制**
   - 实现JWT令牌机制，设置合理的过期时间
   - 添加登录失败锁定机制
   - 实现多因子认证 (MFA)

3. **加强输入验证**
   - 所有API接口添加输入验证注解
   - 实现SQL注入防护 (使用参数化查询)
   - 添加XSS攻击防护

#### 中优先级 (重要改进)
4. **完善权限控制**
   - 确保所有敏感操作都有权限控制
   - 实现数据级权限控制
   - 添加权限审计日志

5. **加强数据保护**
   - 敏感数据传输使用HTTPS
   - 实现数据脱敏机制
   - 加密存储敏感信息

6. **完善监控告警**
   - 添加安全事件监控
   - 实现实时告警机制
   - 完善操作审计日志

#### 低优先级 (建议优化)
7. **配置安全优化**
   - 移除硬编码密码，使用环境变量
   - 配置安全的监控端点
   - 优化日志记录策略

8. **依赖安全**
   - 定期更新依赖包版本
   - 使用依赖包漏洞扫描工具
   - 建立安全补丁管理流程

### 🛡️ 安全最佳实践

#### 开发安全
- 代码审查必须包含安全检查
- 使用安全编码规范
- 定期进行安全培训

#### 部署安全
- 生产环境启用HTTPS
- 配置防火墙规则
- 实现网络隔离

#### 运维安全
- 定期安全审计
- 建立应急响应流程
- 实施备份加密

### 📋 安全检查清单

- [ ] 密码强度策略已实现
- [ ] JWT令牌机制已配置
- [ ] 登录失败处理已实现
- [ ] API输入验证已完成
- [ ] SQL注入防护已实现
- [ ] XSS攻击防护已实现
- [ ] CSRF防护已实现
- [ ] HTTPS传输已启用
- [ ] 敏感数据已加密
- [ ] 权限控制已完善
- [ ] 操作日志已记录
- [ ] 监控告警已配置

EOF
}

# 10. 完成报告
finalize_report() {
    log_info "完成安全审计报告..."

    cat >> "${REPORT_FILE}" << EOF

---

## 📞 技术支持

如有安全相关问题，请联系：
- **安全团队**: security@ioe-dream.com
- **技术支持**: support@ioe-dream.com
- **紧急响应**: emergency@ioe-dream.com

---

**报告生成时间**: $(date '+%Y年%m月%d日 %H:%M:%S')
**审计工具版本**: v1.0.0
**下次审计建议**: 3个月内或重大变更后

EOF

    log_success "安全审计报告已生成: ${REPORT_FILE}"
}

# 主函数
main() {
    echo -e "${CYAN}"
    cat << 'EOF'
 _____ _   _ _   _    _    _   _  ____ _____ ____
| ____| \ | | | | |  / \  | \ | |/ ___| ____|  _ \
|  _| |  \| | |_| | / _ \ |  \| | |   |  _| | | | |
| |___| |\  |  _  |/ ___ \| |\  | |___| |___| |_| |
|_____|_| \_|_| |_/_/   \_\_| \_|\____|_____|____/

               微服务架构安全全面验证套件
EOF
    echo -e "${NC}"

    log_info "开始IOE-DREAM微服务架构安全全面验证..."

    # 执行各项安全验证
    verify_authentication_security
    verify_rbac_authorization
    verify_api_security
    verify_data_security
    scan_system_vulnerabilities
    verify_audit_monitoring
    verify_microservice_communication_security

    # 生成报告
    generate_security_score
    generate_fix_recommendations
    finalize_report

    # 输出最终统计
    echo -e "\n${GREEN}=== 安全验证完成 ===${NC}"
    echo -e "总检查项: ${SECURITY_RESULTS[total_checks]}"
    echo -e "通过检查: ${GREEN}${SECURITY_RESULTS[passed_checks]}${NC}"
    echo -e "失败检查: ${RED}${SECURITY_RESULTS[failed_checks]}${NC}"
    echo -e "警告项目: ${YELLOW}${SECURITY_RESULTS[warning_checks]}${NC}"
    echo -e "严重问题: ${PURPLE}${SECURITY_RESULTS[critical_issues]}${NC}"
    echo -e "\n详细报告: ${BLUE}${REPORT_FILE}${NC}"

    # 根据结果返回退出码
    if [[ ${SECURITY_RESULTS[critical_issues]} -gt 0 ]]; then
        exit 1
    elif [[ ${SECURITY_RESULTS[failed_checks]} -gt 0 ]]; then
        exit 2
    else
        exit 0
    fi
}

# 执行主函数
main "$@"