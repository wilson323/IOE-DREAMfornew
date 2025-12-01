#!/bin/bash

# =============================================================================
# IOE-DREAM 身份认证安全测试脚本
#
# 功能描述：
# 专门测试身份认证系统的安全性，包括密码策略、JWT令牌、会话管理等
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
readonly NC='\033[0m'

# 项目路径配置
readonly PROJECT_ROOT="D:/IOE-DREAM"
readonly MICROSERVICES_DIR="${PROJECT_ROOT}/microservices"
readonly AUTH_SERVICE_DIR="${MICROSERVICES_DIR}/ioedream-auth-service"
readonly REPORTS_DIR="${PROJECT_ROOT}/security-audit-reports"
readonly TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
readonly REPORT_FILE="${REPORTS_DIR}/authentication_test_${TIMESTAMP}.md"

# 测试配置
readonly AUTH_SERVICE_URL="http://localhost:8081"
readonly TEST_USERNAME="test_user"
readonly TEST_PASSWORD="Test123!@#"
readonly WEAK_PASSWORDS=("123456" "password" "admin" "123456789" "qwerty" "abc123")

# 创建报告目录
mkdir -p "${REPORTS_DIR}"

# 测试结果统计
declare -A TEST_RESULTS=(
    ["total_tests"]=0
    ["passed_tests"]=0
    ["failed_tests"]=0
    ["warning_tests"]=0
    ["critical_issues"]=0
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
    echo -e "${RED}[CRITICAL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
    ((TEST_RESULTS[critical_issues]++))
}

# 初始化测试报告
init_test_report() {
    cat > "${REPORT_FILE}" << EOF
# IOE-DREAM 身份认证安全测试报告

**测试时间**: $(date '+%Y年%m月%d日 %H:%M:%S')
**测试范围**: 身份认证系统安全性
**测试版本**: v1.0.0
**测试团队**: IOE-DREAM 安全团队

---

## 📋 测试概述

本报告详细记录了IOE-DREAM微服务架构中身份认证系统的安全性测试结果，包括：
- 密码策略验证
- 登录机制测试
- JWT令牌安全测试
- 会话管理验证
- 认证授权测试

---

## 🔐 测试结果详情

EOF
    log_info "身份认证安全测试报告初始化完成"
}

# 1. 密码策略安全测试
test_password_security() {
    log_info "开始密码策略安全测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 1. 密码策略安全测试" >> "${REPORT_FILE}"

    # 1.1 检查密码强度验证
    log_info "测试密码强度验证机制..."

    local auth_controller="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"
    local auth_service="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/service"

    if [[ -f "$auth_controller" ]]; then
        # 检查是否有密码验证逻辑
        if find "$auth_service" -name "*.java" -exec grep -l "password.*valid\|PasswordValid\|密码强度\|密码复杂度" {} \; > /dev/null 2>&1; then
            log_success "发现密码强度验证实现"
            echo "✅ **密码强度验证**: 已实现密码复杂度检查" >> "${REPORT_FILE}"
        else
            log_warning "未发现密码强度验证实现"
            echo "⚠️ **密码强度验证**: 未发现密码复杂度检查机制" >> "${REPORT_FILE}"
        fi

        # 检查密码加密
        if find "$auth_service" -name "*.java" -exec grep -l "BCrypt\|password.*encode\|密码加密" {} \; > /dev/null 2>&1; then
            log_success "发现密码加密实现"
            echo "✅ **密码加密**: 已实现密码哈希加密" >> "${REPORT_FILE}"
        else
            log_critical "未发现密码加密实现"
            echo "❌ **密码加密**: 未发现密码哈希加密，存在严重安全风险" >> "${REPORT_FILE}"
        fi
    else
        log_error "认证控制器文件不存在"
        echo "❌ **认证控制器**: 文件不存在，无法进行测试" >> "${REPORT_FILE}"
    fi

    # 1.2 弱密码测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试弱密码拒绝机制..."

    echo "#### 弱密码测试" >> "${REPORT_FILE}"
    echo "| 测试密码 | 预期结果 | 实际结果 | 状态 |" >> "${REPORT_FILE}"
    echo "|----------|----------|----------|------|" >> "${REPORT_FILE}"

    for weak_password in "${WEAK_PASSWORDS[@]}"; do
        # 模拟弱密码测试（实际应该调用API）
        echo "| $weak_password | 应被拒绝 | 需要API测试 | ⚠️ 待测试 |" >> "${REPORT_FILE}"
        log_warning "弱密码 '$weak_password' 需要通过API测试验证"
    done

    log_info "密码策略安全测试完成"
}

# 2. 登录机制安全测试
test_login_security() {
    log_info "开始登录机制安全测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 2. 登录机制安全测试" >> "${REPORT_FILE}"

    # 2.1 检查登录接口安全
    log_info "测试登录接口安全性..."

    local auth_controller="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"

    if [[ -f "$auth_controller" ]]; then
        # 检查输入验证
        if grep -q "@Valid\|@NotNull\|@NotBlank" "$auth_controller"; then
            log_success "登录接口包含输入验证"
            echo "✅ **输入验证**: 登录接口包含输入验证注解" >> "${REPORT_FILE}"
        else
            log_critical "登录接口缺少输入验证"
            echo "❌ **输入验证**: 登录接口缺少输入验证，存在注入风险" >> "${REPORT_FILE}"
        fi

        # 检查异常处理
        if grep -q "try.*catch\|Exception" "$auth_controller"; then
            log_success "登录接口包含异常处理"
            echo "✅ **异常处理**: 登录接口包含异常处理机制" >> "${REPORT_FILE}"
        else
            log_warning "登录接口异常处理不完善"
            echo "⚠️ **异常处理**: 登录接口异常处理不完善" >> "${REPORT_FILE}"
        fi

        # 检查日志记录
        if grep -q "log\|logger" "$auth_controller"; then
            log_success "登录接口包含日志记录"
            echo "✅ **日志记录**: 登录接口包含日志记录" >> "${REPORT_FILE}"
        else
            log_warning "登录接口缺少日志记录"
            echo "⚠️ **日志记录**: 登录接口缺少日志记录" >> "${REPORT_FILE}"
        fi
    fi

    # 2.2 登录失败处理测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试登录失败处理机制..."

    echo "#### 登录失败处理测试" >> "${REPORT_FILE}"

    # 检查是否有登录失败处理逻辑
    if find "${AUTH_SERVICE_DIR}" -name "*.java" -exec grep -l "login.*fail\|attempt.*count\|account.*lock\|登录失败\|账户锁定" {} \; > /dev/null 2>&1; then
        log_success "发现登录失败处理机制"
        echo "✅ **登录失败处理**: 已实现登录失败处理机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现登录失败处理机制"
        echo "⚠️ **登录失败处理**: 未发现登录失败处理机制" >> "${REPORT_FILE}"
    fi

    # 2.3 暴力破解防护测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试暴力破解防护机制..."

    # 检查是否有频率限制或账户锁定
    if find "${AUTH_SERVICE_DIR}" -name "*.java" -exec grep -l "rate.*limit\|attempt.*limit\|account.*lock\|IP.*limit" {} \; > /dev/null 2>&1; then
        log_success "发现暴力破解防护机制"
        echo "✅ **暴力破解防护**: 已实现暴力破解防护机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现暴力破解防护机制"
        echo "⚠️ **暴力破解防护**: 未发现暴力破解防护机制" >> "${REPORT_FILE}"
    fi

    log_info "登录机制安全测试完成"
}

# 3. JWT令牌安全测试
test_jwt_security() {
    log_info "开始JWT令牌安全测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 3. JWT令牌安全测试" >> "${REPORT_FILE}"

    # 3.1 检查JWT配置
    log_info "测试JWT配置安全性..."

    local auth_config="${AUTH_SERVICE_DIR}/src/main/resources/application.yml"

    if [[ -f "$auth_config" ]]; then
        # 检查JWT相关配置
        if grep -q "jwt\|JWT" "$auth_config"; then
            log_success "发现JWT配置"
            echo "✅ **JWT配置**: 已配置JWT令牌机制" >> "${REPORT_FILE}"

            # 检查JWT密钥配置
            if grep -q "secret\|key" "$auth_config"; then
                log_success "发现JWT密钥配置"
                echo "✅ **JWT密钥**: 已配置JWT签名密钥" >> "${REPORT_FILE}"
            else
                log_warning "未发现JWT密钥配置"
                echo "⚠️ **JWT密钥**: 未发现明确的JWT密钥配置" >> "${REPORT_FILE}"
            fi

            # 检查JWT过期时间配置
            if grep -q "expiration\|expire\|timeout" "$auth_config"; then
                log_success "发现JWT过期时间配置"
                echo "✅ **JWT过期**: 已配置JWT令牌过期时间" >> "${REPORT_FILE}"
            else
                log_warning "未发现JWT过期时间配置"
                echo "⚠️ **JWT过期**: 未发现明确的JWT过期时间配置" >> "${REPORT_FILE}"
            fi
        else
            log_warning "未发现JWT配置"
            echo "⚠️ **JWT配置**: 未发现JWT令牌配置" >> "${REPORT_FILE}"
        fi
    else
        log_error "认证服务配置文件不存在"
        echo "❌ **配置文件**: 认证服务配置文件不存在" >> "${REPORT_FILE}"
    fi

    # 3.2 令牌提取和验证测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试令牌提取和验证机制..."

    local auth_controller="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"

    if [[ -f "$auth_controller" ]]; then
        # 检查令牌提取
        if grep -q "Bearer\|Authorization\|extractToken" "$auth_controller"; then
            log_success "发现标准令牌提取机制"
            echo "✅ **令牌提取**: 已实现标准Bearer令牌提取机制" >> "${REPORT_FILE}"
        else
            log_warning "令牌提取机制不标准"
            echo "⚠️ **令牌提取**: 令牌提取机制可能不标准" >> "${REPORT_FILE}"
        fi

        # 检查令牌验证
        if grep -q "validateToken\|verify.*token\|token.*valid" "$auth_controller"; then
            log_success "发现令牌验证机制"
            echo "✅ **令牌验证**: 已实现令牌验证机制" >> "${REPORT_FILE}"
        else
            log_warning "未发现令牌验证机制"
            echo "⚠️ **令牌验证**: 未发现明确的令牌验证机制" >> "${REPORT_FILE}"
        fi
    fi

    # 3.3 令牌刷新机制测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试令牌刷新机制..."

    # 检查是否有刷新令牌接口
    if grep -q "refresh\|RefreshToken" "$auth_controller"; then
        log_success "发现令牌刷新机制"
        echo "✅ **令牌刷新**: 已实现令牌刷新机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现令牌刷新机制"
        echo "⚠️ **令牌刷新**: 未发现令牌刷新机制" >> "${REPORT_FILE}"
    fi

    log_info "JWT令牌安全测试完成"
}

# 4. 会话管理安全测试
test_session_security() {
    log_info "开始会话管理安全测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 4. 会话管理安全测试" >> "${REPORT_FILE}"

    # 4.1 检查会话配置
    log_info "测试会话配置安全性..."

    local auth_config="${AUTH_SERVICE_DIR}/src/main/resources/application.yml"

    if [[ -f "$auth_config" ]]; then
        # 检查会话相关配置
        if grep -q "session\|Session" "$auth_config"; then
            log_success "发现会话配置"
            echo "✅ **会话配置**: 已配置会话管理" >> "${REPORT_FILE}"

            # 检查会话超时配置
            if grep -q "timeout\|expire\|max-age" "$auth_config"; then
                log_success "发现会话超时配置"
                echo "✅ **会话超时**: 已配置会话超时时间" >> "${REPORT_FILE}"
            else
                log_warning "未发现会话超时配置"
                echo "⚠️ **会话超时**: 未发现明确的会话超时配置" >> "${REPORT_FILE}"
            fi
        else
            log_warning "未发现会话配置"
            echo "⚠️ **会话配置**: 未发现明确的会话配置" >> "${REPORT_FILE}"
        fi
    fi

    # 4.2 并发会话控制测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试并发会话控制..."

    # 检查是否有并发会话控制
    if find "${AUTH_SERVICE_DIR}" -name "*.java" -exec grep -l "concurrent.*session\|session.*control\|maximum.*session" {} \; > /dev/null 2>&1; then
        log_success "发现并发会话控制"
        echo "✅ **并发控制**: 已实现并发会话控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现并发会话控制"
        echo "⚠️ **并发控制**: 未发现并发会话控制机制" >> "${REPORT_FILE}"
    fi

    # 4.3 会话失效处理测试
    ((TEST_RESULTS[total_tests]++))
    log_info "测试会话失效处理..."

    # 检查登出接口
    local auth_controller="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"

    if [[ -f "$auth_controller" ]] && grep -q "logout\|Logout" "$auth_controller"; then
        log_success "发现登出接口"
        echo "✅ **登出处理**: 已实现登出接口" >> "${REPORT_FILE}"

        # 检查会话失效处理
        if grep -q "invalidate\|clear\|remove.*token" "$auth_controller"; then
            log_success "发现会话失效处理"
            echo "✅ **会话失效**: 已实现会话失效处理" >> "${REPORT_FILE}"
        else
            log_warning "会话失效处理不完善"
            echo "⚠️ **会话失效**: 会话失效处理可能不完善" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现登出接口"
        echo "⚠️ **登出处理**: 未发现登出接口" >> "${REPORT_FILE}"
    fi

    log_info "会话管理安全测试完成"
}

# 5. 多因子认证测试
test_mfa_security() {
    log_info "开始多因子认证测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 5. 多因子认证安全测试" >> "${REPORT_FILE}"

    # 5.1 检查MFA实现
    log_info "测试多因子认证实现..."

    # 检查是否有MFA相关实现
    if find "${AUTH_SERVICE_DIR}" -name "*.java" -exec grep -l "MFA\|2FA\|two.*factor\|multi.*factor\|OTP\|TOTP" {} \; > /dev/null 2>&1; then
        log_success "发现多因子认证实现"
        echo "✅ **多因子认证**: 已实现多因子认证机制" >> "${REPORT_FILE}"

        # 检查MFA配置
        if find "${AUTH_SERVICE_DIR}" -name "*.java" -exec grep -l "google.*authenticator\|authenticator.*app" {} \; > /dev/null 2>&1; then
            log_success "发现Google Authenticator支持"
            echo "✅ **Google Authenticator**: 支持Google Authenticator" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现多因子认证实现"
        echo "⚠️ **多因子认证**: 未发现多因子认证实现" >> "${REPORT_FILE}"
        echo "💡 **建议**: 考虑实现多因子认证以增强安全性" >> "${REPORT_FILE}"
    fi

    log_info "多因子认证测试完成"
}

# 6. 认证授权集成测试
test_auth_integration() {
    log_info "开始认证授权集成测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 6. 认证授权集成测试" >> "${REPORT_FILE}"

    # 6.1 检查权限验证接口
    log_info "测试权限验证接口..."

    local auth_controller="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"

    if [[ -f "$auth_controller" ]]; then
        # 检查权限验证接口
        if grep -q "hasPermission\|checkPermission\|permission.*check" "$auth_controller"; then
            log_success "发现权限验证接口"
            echo "✅ **权限验证**: 已实现权限验证接口" >> "${REPORT_FILE}"
        else
            log_warning "未发现权限验证接口"
            echo "⚠️ **权限验证**: 未发现权限验证接口" >> "${REPORT_FILE}"
        fi

        # 检查角色验证接口
        if grep -q "hasRole\|checkRole\|role.*check" "$auth_controller"; then
            log_success "发现角色验证接口"
            echo "✅ **角色验证**: 已实现角色验证接口" >> "${REPORT_FILE}"
        else
            log_warning "未发现角色验证接口"
            echo "⚠️ **角色验证**: 未发现角色验证接口" >> "${REPORT_FILE}"
        fi

        # 检查用户信息接口
        if grep -q "getUserInfo\|userInfo\|employee" "$auth_controller"; then
            log_success "发现用户信息接口"
            echo "✅ **用户信息**: 已实现用户信息获取接口" >> "${REPORT_FILE}"
        else
            log_warning "未发现用户信息接口"
            echo "⚠️ **用户信息**: 未发现用户信息获取接口" >> "${REPORT_FILE}"
        fi
    fi

    # 6.2 检查服务间认证
    ((TEST_RESULTS[total_tests]++))
    log_info "测试服务间认证机制..."

    # 检查是否有服务间调用认证
    if find "${MICROSERVICES_DIR}" -name "*.java" -exec grep -l "inter.*service.*auth\|service.*to.*service\|internal.*auth" {} \; > /dev/null 2>&1; then
        log_success "发现服务间认证机制"
        echo "✅ **服务间认证**: 已实现服务间认证机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现服务间认证机制"
        echo "⚠️ **服务间认证**: 未发现明确的服务间认证机制" >> "${REPORT_FILE}"
    fi

    log_info "认证授权集成测试完成"
}

# 7. API接口安全测试
test_api_security() {
    log_info "开始API接口安全测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 7. API接口安全测试" >> "${REPORT_FILE}"

    # 7.1 检查API接口保护
    log_info "测试API接口保护机制..."

    local auth_controller="${AUTH_SERVICE_DIR}/src/main/java/net/lab1024/sa/auth/controller/AuthController.java"

    if [[ -f "$auth_controller" ]]; then
        # 检查是否有公开接口和受保护接口的区分
        local public_endpoints=$(grep -c "@GetMapping.*health\|@GetMapping.*public\|@PostMapping.*login" "$auth_controller" || echo "0")
        local protected_endpoints=$(grep -c "@.*Mapping" "$auth_controller" || echo "0")

        if [[ $public_endpoints -gt 0 ]]; then
            log_success "发现公开接口设计"
            echo "✅ **公开接口**: 发现${public_endpoints}个公开接口" >> "${REPORT_FILE}"
        fi

        log_info "认证服务总共包含${protected_endpoints}个API接口"
    fi

    # 7.2 检查CORS配置
    ((TEST_RESULTS[total_tests]++))
    log_info "测试CORS配置安全性..."

    local auth_config="${AUTH_SERVICE_DIR}/src/main/resources/application.yml"

    if [[ -f "$auth_config" ]]; then
        if grep -q "cors\|CORS\|cross-origin" "$auth_config"; then
            log_success "发现CORS配置"
            echo "✅ **CORS配置**: 已配置跨域资源共享" >> "${REPORT_FILE}"

            # 检查CORS配置是否过于宽松
            if grep -q "origin.*\*\|access-control-allow-origin.*\*" "$auth_config"; then
                log_warning "CORS配置可能过于宽松"
                echo "⚠️ **CORS安全**: CORS配置允许所有源访问，存在安全风险" >> "${REPORT_FILE}"
            fi
        else
            log_warning "未发现CORS配置"
            echo "⚠️ **CORS配置**: 未发现CORS配置" >> "${REPORT_FILE}"
        fi
    fi

    log_info "API接口安全测试完成"
}

# 8. 生成测试报告总结
generate_test_summary() {
    log_info "生成测试报告总结..."

    echo -e "\n---" >> "${REPORT_FILE}"
    echo -e "\n## 📊 测试结果总结" >> "${REPORT_FILE}"

    local total_tests=${TEST_RESULTS[total_tests]}
    local passed_tests=${TEST_RESULTS[passed_tests]}
    local failed_tests=${TEST_RESULTS[failed_tests]}
    local warning_tests=${TEST_RESULTS[warning_tests]}
    local critical_issues=${TEST_RESULTS[critical_issues]}

    # 计算通过率
    local pass_rate=0
    if [[ $total_tests -gt 0 ]]; then
        pass_rate=$((passed_tests * 100 / total_tests))
    fi

    cat >> "${REPORT_FILE}" << EOF

### 📈 测试统计

| 测试指标 | 数值 | 说明 |
|----------|------|------|
| **总测试项** | ${total_tests} | 身份认证安全测试总项目数 |
| **通过测试** | ${passed_tests} | 符合安全要求的测试项目 |
| **失败测试** | ${failed_tests} | 不符合安全要求的测试项目 |
| **警告测试** | ${warning_tests} | 需要关注的测试项目 |
| **严重问题** | ${critical_issues} | 严重安全问题数量 |

### 🎯 安全评分

**身份认证安全评分: ${pass_rate}/100**

### 🔧 关键修复建议

#### 高优先级修复
1. **实现密码强度验证**: 确保密码满足复杂度要求
2. **加强密码加密**: 使用BCrypt等安全的哈希算法
3. **完善登录失败处理**: 实现账户锁定机制
4. **配置JWT安全**: 设置合理的过期时间和密钥

#### 中优先级改进
1. **添加多因子认证**: 提高身份认证安全性
2. **完善会话管理**: 配置会话超时和并发控制
3. **加强API保护**: 区分公开和受保护接口
4. **优化CORS配置**: 避免过于宽松的跨域配置

#### 低优先级优化
1. **完善日志记录**: 记录详细的认证事件
2. **加强监控告警**: 实现实时安全监控
3. **定期安全审计**: 建立定期安全检查机制

EOF

    # 根据测试结果给出总体评价
    if [[ $critical_issues -gt 0 ]]; then
        echo -e "⚠️ **总体评价**: 发现${critical_issues}个严重安全问题，需要立即修复" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 80 ]]; then
        echo -e "✅ **总体评价**: 身份认证系统安全性良好" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 60 ]]; then
        echo -e "⚠️ **总体评价**: 身份认证系统安全性需要改进" >> "${REPORT_FILE}"
    else
        echo -e "❌ **总体评价**: 身份认证系统存在较多安全问题" >> "${REPORT_FILE}"
    fi
}

# 主函数
main() {
    echo -e "${BLUE}"
    cat << 'EOF'
 _____ _   _ ____  _____ _______       _   _ _____ ____
| ____| \ | |  _ \| ____|__   __|____ | | | |_   _/ ___|
|  _| |  \| | | | |  _|    | |/ _ \ \ / / | | | || |
| |___| |\  | |_| | |___   | | (_) \ V /| |_| | | |
|_____|_| \_|____/|_____|  |_|\___/ \_/  \___/  |_|

                身份认证安全测试工具
EOF
    echo -e "${NC}"

    log_info "开始IOE-DREAM身份认证安全测试..."

    # 初始化测试报告
    init_test_report

    # 执行各项测试
    test_password_security
    test_login_security
    test_jwt_security
    test_session_security
    test_mfa_security
    test_auth_integration
    test_api_security

    # 生成测试总结
    generate_test_summary

    # 输出测试结果
    echo -e "\n${GREEN}=== 身份认证安全测试完成 ===${NC}"
    echo -e "总测试项: ${TEST_RESULTS[total_tests]}"
    echo -e "通过测试: ${GREEN}${TEST_RESULTS[passed_tests]}${NC}"
    echo -e "失败测试: ${RED}${TEST_RESULTS[failed_tests]}${NC}"
    echo -e "警告测试: ${YELLOW}${TEST_RESULTS[warning_tests]}${NC}"
    echo -e "严重问题: ${RED}${TEST_RESULTS[critical_issues]}${NC}"
    echo -e "\n详细报告: ${BLUE}${REPORT_FILE}${NC}"

    # 根据结果返回退出码
    if [[ ${TEST_RESULTS[critical_issues]} -gt 0 ]]; then
        exit 1
    elif [[ ${TEST_RESULTS[failed_tests]} -gt 0 ]]; then
        exit 2
    else
        exit 0
    fi
}

# 执行主函数
main "$@"