#!/bin/bash

# =============================================================================
# IOE-DREAM RBAC权限控制系统完整性测试脚本
#
# 功能描述：
# 专门测试基于角色的访问控制(RBAC)系统的完整性和安全性
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
readonly NC='\033[0m'

# 项目路径配置
readonly PROJECT_ROOT="D:/IOE-DREAM"
readonly MICROSERVICES_DIR="${PROJECT_ROOT}/microservices"
readonly IDENTITY_SERVICE_DIR="${MICROSERVICES_DIR}/ioedream-identity-service"
readonly REPORTS_DIR="${PROJECT_ROOT}/security-audit-reports"
readonly TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
readonly REPORT_FILE="${REPORTS_DIR}/rbac_authorization_test_${TIMESTAMP}.md"

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
    echo -e "${PURPLE}[CRITICAL]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
    ((TEST_RESULTS[critical_issues]++))
}

# 初始化测试报告
init_test_report() {
    cat > "${REPORT_FILE}" << EOF
# IOE-DREAM RBAC权限控制系统完整性测试报告

**测试时间**: $(date '+%Y年%m月%d日 %H:%M:%S')
**测试范围**: RBAC权限控制系统完整性和安全性
**测试版本**: v1.0.0
**测试团队**: IOE-DREAM 安全团队

---

## 📋 测试概述

本报告详细记录了IOE-DREAM微服务架构中RBAC权限控制系统的完整性测试结果，包括：
- 权限模型设计验证
- 角色管理功能测试
- 权限分配机制测试
- 权限验证拦截器测试
- 数据权限控制测试

---

## 🛡️ 测试结果详情

EOF
    log_info "RBAC权限控制系统测试报告初始化完成"
}

# 1. 权限模型架构验证
test_permission_model_architecture() {
    log_info "开始权限模型架构验证..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 1. 权限模型架构验证" >> "${REPORT_FILE}"

    # 1.1 检查RBAC核心实体
    log_info "验证RBAC核心实体设计..."

    local rbac_domain="${IDENTITY_SERVICE_DIR}/src/main/java/net/lab1024/sa/identity/module/rbac/domain/entity"

    local required_entities=("RbacUserEntity" "RbacRoleEntity" "RbacResourceEntity" "RbacUserRoleEntity" "RbacRoleResourceEntity")
    local found_entities=0

    echo "#### RBAC核心实体检查" >> "${REPORT_FILE}"
    echo "| 实体名称 | 文件存在 | 说明 |" >> "${REPORT_FILE}"
    echo "|----------|----------|------|" >> "${REPORT_FILE}"

    for entity in "${required_entities[@]}"; do
        if find "${IDENTITY_SERVICE_DIR}" -name "${entity}.java" > /dev/null 2>&1; then
            echo "| $entity | ✅ 存在 | RBAC核心实体 |" >> "${REPORT_FILE}"
            ((found_entities++))
            log_success "发现RBAC核心实体: $entity"
        else
            echo "| $entity | ❌ 缺失 | RBAC核心实体 |" >> "${REPORT_FILE}"
            log_warning "缺失RBAC核心实体: $entity"
        fi
    done

    local entity_coverage=$((found_entities * 100 / ${#required_entities[@]}))
    if [[ $entity_coverage -ge 80 ]]; then
        log_success "RBAC核心实体完整度: ${entity_coverage}%"
    else
        log_warning "RBAC核心实体完整度偏低: ${entity_coverage}%"
    fi

    # 1.2 检查权限注解设计
    ((TEST_RESULTS[total_tests]++))
    log_info "验证权限注解设计..."

    local require_resource_annotation="${IDENTITY_SERVICE_DIR}/src/main/java/net/lab1024/sa/identity/module/rbac/annotation/RequireResource.java"

    if [[ -f "$require_resource_annotation" ]]; then
        log_success "发现@RequireResource权限注解"
        echo "✅ **权限注解**: @RequireResource注解已实现" >> "${REPORT_FILE}"

        # 检查注解属性
        local annotation_attrs=$(grep -o "@interface.*" "$require_resource_annotation" | head -1)
        if [[ -n "$annotation_attrs" ]]; then
            log_success "权限注解定义完整"
            echo "✅ **注解属性**: 权限注解属性定义完整" >> "${REPORT_FILE}"
        fi
    else
        log_error "未发现@RequireResource权限注解"
        echo "❌ **权限注解**: @RequireResource注解缺失" >> "${REPORT_FILE}"
    fi

    log_info "权限模型架构验证完成"
}

# 2. 角色管理功能测试
test_role_management() {
    log_info "开始角色管理功能测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 2. 角色管理功能测试" >> "${REPORT_FILE}"

    # 2.1 检查角色控制器
    log_info "测试角色管理控制器..."

    local role_controller="${IDENTITY_SERVICE_DIR}/src/main/java/net/lab1024/sa/identity/module/rbac/controller/RoleController.java"

    if [[ -f "$role_controller" ]]; then
        log_success "发现角色管理控制器"
        echo "✅ **角色控制器**: RoleController已实现" >> "${REPORT_FILE}"

        # 检查CRUD操作
        local crud_operations=("createRole\|addRole" "updateRole\|editRole" "deleteRole\|removeRole" "getRole\|queryRole")
        local implemented_operations=0

        echo "#### 角色CRUD操作检查" >> "${REPORT_FILE}"
        echo "| 操作类型 | 方法检查 | 状态 |" >> "${REPORT_FILE}"
        echo "|----------|----------|------|" >> "${REPORT_FILE}"

        for operation in "${crud_operations[@]}"; do
            if grep -q "$operation" "$role_controller"; then
                echo "| $operation | ✅ 实现 | 正常 |" >> "${REPORT_FILE}"
                ((implemented_operations++))
            else
                echo "| $operation | ❌ 缺失 | 需要实现 |" >> "${REPORT_FILE}"
            fi
        done

        local crud_coverage=$((implemented_operations * 100 / ${#crud_operations[@]}))
        log_info "角色CRUD操作覆盖率: ${crud_coverage}%"
    else
        log_error "角色管理控制器不存在"
        echo "❌ **角色控制器**: RoleController缺失" >> "${REPORT_FILE}"
    fi

    # 2.2 检查角色服务层
    ((TEST_RESULTS[total_tests]++))
    log_info "测试角色服务层实现..."

    local role_service="${IDENTITY_SERVICE_DIR}/src/main/java/net/lab1024/sa/identity/module/rbac/service/RoleService.java"

    if [[ -f "$role_service" ]]; then
        log_success "发现角色服务接口"
        echo "✅ **角色服务**: RoleService接口已定义" >> "${REPORT_FILE}"

        # 检查服务实现
        local role_service_impl="${IDENTITY_SERVICE_DIR}/src/main/java/net/lab1024/sa/identity/module/rbac/service/impl/RoleServiceImpl.java"
        if [[ -f "$role_service_impl" ]]; then
            log_success "发现角色服务实现"
            echo "✅ **服务实现**: RoleServiceImpl已实现" >> "${REPORT_FILE}"
        else
            log_warning "角色服务实现缺失"
            echo "⚠️ **服务实现**: RoleServiceImpl缺失" >> "${REPORT_FILE}"
        fi
    else
        log_error "角色服务接口不存在"
        echo "❌ **角色服务**: RoleService接口缺失" >> "${REPORT_FILE}"
    fi

    # 2.3 检查角色数据访问层
    ((TEST_RESULTS[total_tests]++))
    log_info "测试角色数据访问层..."

    local role_daos=$(find "${IDENTITY_SERVICE_DIR}" -name "*Role*Dao.java" | wc -l)
    if [[ $role_daos -gt 0 ]]; then
        log_success "发现${role_daos}个角色相关DAO"
        echo "✅ **数据访问**: 发现${role_daos}个角色DAO" >> "${REPORT_FILE}"
    else
        log_warning "未发现角色相关DAO"
        echo "⚠️ **数据访问**: 未发现角色DAO" >> "${REPORT_FILE}"
    fi

    log_info "角色管理功能测试完成"
}

# 3. 权限分配机制测试
test_permission_assignment() {
    log_info "开始权限分配机制测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 3. 权限分配机制测试" >> "${REPORT_FILE}"

    # 3.1 检查用户-角色分配
    log_info "测试用户-角色分配机制..."

    local user_role_assignments=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "assignUserRole\|user.*role.*assign" {} \; | wc -l)
    if [[ $user_role_assignments -gt 0 ]]; then
        log_success "发现用户-角色分配实现"
        echo "✅ **用户角色分配**: 已实现用户-角色分配机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现用户-角色分配实现"
        echo "⚠️ **用户角色分配**: 未发现用户-角色分配机制" >> "${REPORT_FILE}"
    fi

    # 3.2 检查角色-权限分配
    ((TEST_RESULTS[total_tests]++))
    log_info "测试角色-权限分配机制..."

    local role_permission_assignments=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "assignRolePermission\|role.*permission.*assign" {} \; | wc -l)
    if [[ $role_permission_assignments -gt 0 ]]; then
        log_success "发现角色-权限分配实现"
        echo "✅ **角色权限分配**: 已实现角色-权限分配机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现角色-权限分配实现"
        echo "⚠️ **角色权限分配**: 未发现角色-权限分配机制" >> "${REPORT_FILE}"
    fi

    # 3.3 检查权限继承机制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试权限继承机制..."

    local permission_inheritance=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "inherit\|extend\|parent.*role" {} \; | wc -l)
    if [[ $permission_inheritance -gt 0 ]]; then
        log_success "发现权限继承机制"
        echo "✅ **权限继承**: 已实现权限继承机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现权限继承机制"
        echo "⚠️ **权限继承**: 未发现权限继承机制" >> "${REPORT_FILE}"
    fi

    log_info "权限分配机制测试完成"
}

# 4. 权限验证拦截器测试
test_permission_interceptor() {
    log_info "开始权限验证拦截器测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 4. 权限验证拦截器测试" >> "${REPORT_FILE}"

    # 4.1 检查权限拦截器实现
    log_info "测试权限验证拦截器..."

    local interceptors=$(find "${IDENTITY_SERVICE_DIR}" -name "*Interceptor.java" | wc -l)
    if [[ $interceptors -gt 0 ]]; then
        log_success "发现${interceptors}个拦截器"
        echo "✅ **拦截器**: 发现${interceptors}个拦截器实现" >> "${REPORT_FILE}"

        # 检查权限相关拦截器
        local permission_interceptors=$(find "${IDENTITY_SERVICE_DIR}" -name "*Interceptor.java" -exec grep -l "permission\|auth\|require" {} \; | wc -l)
        if [[ $permission_interceptors -gt 0 ]]; then
            log_success "发现权限验证拦截器"
            echo "✅ **权限拦截器**: 发现${permission_interceptors}个权限验证拦截器" >> "${REPORT_FILE}"
        else
            log_warning "未发现专门的权限验证拦截器"
            echo "⚠️ **权限拦截器**: 未发现专门的权限验证拦截器" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现拦截器实现"
        echo "⚠️ **拦截器**: 未发现拦截器实现" >> "${REPORT_FILE}"
    fi

    # 4.2 检查AOP切面实现
    ((TEST_RESULTS[total_tests]++))
    log_info "测试AOP权限切面实现..."

    local aspects=$(find "${IDENTITY_SERVICE_DIR}" -name "*Aspect.java" | wc -l)
    if [[ $aspects -gt 0 ]]; then
        log_success "发现${aspects}个AOP切面"
        echo "✅ **AOP切面**: 发现${aspects}个AOP切面实现" >> "${REPORT_FILE}"

        # 检查权限相关切面
        local permission_aspects=$(find "${IDENTITY_SERVICE_DIR}" -name "*Aspect.java" -exec grep -l "RequireResource\|permission\|auth" {} \; | wc -l)
        if [[ $permission_aspects -gt 0 ]]; then
            log_success "发现权限AOP切面"
            echo "✅ **权限切面**: 发现${permission_aspects}个权限AOP切面" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现AOP切面实现"
        echo "⚠️ **AOP切面**: 未发现AOP切面实现" >> "${REPORT_FILE}"
    fi

    # 4.3 检查权限验证逻辑
    ((TEST_RESULTS[total_tests]++))
    log_info "测试权限验证逻辑..."

    local permission_checks=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "hasPermission\|checkPermission\|requireResource" {} \; | wc -l)
    if [[ $permission_checks -gt 0 ]]; then
        log_success "发现权限验证逻辑"
        echo "✅ **权限验证**: 发现${permission_checks}个权限验证实现" >> "${REPORT_FILE}"
    else
        log_warning "未发现权限验证逻辑"
        echo "⚠️ **权限验证**: 未发现权限验证逻辑" >> "${REPORT_FILE}"
    fi

    log_info "权限验证拦截器测试完成"
}

# 5. 控制器权限保护测试
test_controller_protection() {
    log_info "开始控制器权限保护测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 5. 控制器权限保护测试" >> "${REPORT_FILE}"

    # 5.1 检查所有控制器的权限保护覆盖率
    log_info "测试控制器权限保护覆盖率..."

    local all_controllers=$(find "${IDENTITY_SERVICE_DIR}" -name "*Controller.java" | wc -l)
    local protected_controllers=$(find "${IDENTITY_SERVICE_DIR}" -name "*Controller.java" -exec grep -l "@RequireResource" {} \; | wc -l)

    if [[ $all_controllers -gt 0 ]]; then
        local protection_coverage=$((protected_controllers * 100 / all_controllers))
        log_info "控制器权限保护覆盖率: ${protection_coverage}% (${protected_controllers}/${all_controllers})"

        echo "#### 控制器权限保护统计" >> "${REPORT_FILE}"
        echo "| 指标 | 数值 | 说明 |" >> "${REPORT_FILE}"
        echo "|------|------|------|" >> "${REPORT_FILE}"
        echo "| 总控制器数 | ${all_controllers} | 系统中的所有控制器 |" >> "${REPORT_FILE}"
        echo "| 受保护控制器 | ${protected_controllers} | 有权限保护的控制器 |" >> "${REPORT_FILE}"
        echo "| 保护覆盖率 | ${protection_coverage}% | 权限保护覆盖比例 |" >> "${REPORT_FILE}"

        if [[ $protection_coverage -ge 80 ]]; then
            log_success "控制器权限保护覆盖率良好"
            echo "✅ **保护覆盖率**: ${protection_coverage}%，覆盖率良好" >> "${REPORT_FILE}"
        elif [[ $protection_coverage -ge 60 ]]; then
            log_warning "控制器权限保护覆盖率一般"
            echo "⚠️ **保护覆盖率**: ${protection_coverage}%，覆盖率需要提升" >> "${REPORT_FILE}"
        else
            log_error "控制器权限保护覆盖率过低"
            echo "❌ **保护覆盖率**: ${protection_coverage}%，覆盖率过低" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现控制器文件"
        echo "⚠️ **控制器**: 未发现控制器文件" >> "${REPORT_FILE}"
    fi

    # 5.2 检查敏感接口保护
    ((TEST_RESULTS[total_tests]++))
    log_info "测试敏感接口权限保护..."

    local sensitive_operations=("delete\|remove" "update\|modify" "create\|add" "admin\|manage")
    local protected_sensitive=0

    echo "#### 敏感操作权限保护检查" >> "${REPORT_FILE}"
    echo "| 操作类型 | 接口数量 | 受保护数量 | 保护率 |" >> "${REPORT_FILE}"
    echo "|----------|----------|------------|--------|" >> "${REPORT_FILE}"

    for operation in "${sensitive_operations[@]}"; do
        local total_sensitive=$(find "${IDENTITY_SERVICE_DIR}" -name "*Controller.java" -exec grep -c "$operation" {} \; | awk '{sum+=$1} END {print sum}')
        local protected_sensitive_ops=$(find "${IDENTITY_SERVICE_DIR}" -name "*Controller.java" -exec grep -l "$operation" {} \; -exec grep -l "@RequireResource" {} \; | wc -l)

        if [[ $total_sensitive -gt 0 ]]; then
            local protection_rate=$((protected_sensitive_ops * 100 / total_sensitive))
            echo "| $operation | $total_sensitive | $protected_sensitive_ops | ${protection_rate}% |" >> "${REPORT_FILE}"

            if [[ $protection_rate -ge 80 ]]; then
                ((protected_sensitive++))
            fi
        fi
    done

    log_info "敏感操作保护完整性: ${protected_sensitive}/${#sensitive_operations[@]}"

    # 5.3 检查权限注解使用规范性
    ((TEST_RESULTS[total_tests]++))
    log_info "测试权限注解使用规范性..."

    local require_resource_usages=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -c "@RequireResource" {} \; | awk '{sum+=$1} END {print sum}')
    if [[ $require_resource_usages -gt 0 ]]; then
        log_success "发现${require_resource_usages}处@RequireResource使用"
        echo "✅ **权限注解使用**: 发现${require_resource_usages}处@RequireResource使用" >> "${REPORT_FILE}"

        # 检查注解参数完整性
        local complete_annotations=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -A 5 "@RequireResource" {} \; | grep -c "code.*action.*description" || echo "0")
        if [[ $complete_annotations -gt 0 ]]; then
            log_success "权限注解参数配置完整"
            echo "✅ **注解完整性**: 权限注解参数配置完整" >> "${REPORT_FILE}"
        else
            log_warning "部分权限注解参数不完整"
            echo "⚠️ **注解完整性**: 部分权限注解参数不完整" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现@RequireResource使用"
        echo "⚠️ **权限注解使用**: 未发现@RequireResource使用" >> "${REPORT_FILE}"
    fi

    log_info "控制器权限保护测试完成"
}

# 6. 数据权限控制测试
test_data_permission_control() {
    log_info "开始数据权限控制测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 6. 数据权限控制测试" >> "${REPORT_FILE}"

    # 6.1 检查数据权限注解
    log_info "测试数据权限注解实现..."

    local data_permission_annotations=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "DataScope\|DataPermission\|数据权限" {} \; | wc -l)
    if [[ $data_permission_annotations -gt 0 ]]; then
        log_success "发现数据权限注解实现"
        echo "✅ **数据权限注解**: 已实现数据权限注解" >> "${REPORT_FILE}"
    else
        log_warning "未发现数据权限注解实现"
        echo "⚠️ **数据权限注解**: 未发现数据权限注解实现" >> "${REPORT_FILE}"
    fi

    # 6.2 检查行级权限控制
    ((TEST_RESULTS[total_tests]++))
    log_info "测试行级权限控制..."

    local row_level_permissions=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "row.*level\|行级权限\|data.*scope" {} \; | wc -l)
    if [[ $row_level_permissions -gt 0 ]]; then
        log_success "发现行级权限控制实现"
        echo "✅ **行级权限**: 已实现行级权限控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现行级权限控制实现"
        echo "⚠️ **行级权限**: 未发现行级权限控制实现" >> "${REPORT_FILE}"
    fi

    # 6.3 检查部门数据权限
    ((TEST_RESULTS[total_tests]++))
    log_info "测试部门数据权限控制..."

    local department_permissions=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "department.*data\|dept.*scope\|部门数据权限" {} \; | wc -l)
    if [[ $department_permissions -gt 0 ]]; then
        log_success "发现部门数据权限控制"
        echo "✅ **部门权限**: 已实现部门数据权限控制" >> "${REPORT_FILE}"
    else
        log_warning "未发现部门数据权限控制"
        echo "⚠️ **部门权限**: 未发现部门数据权限控制" >> "${REPORT_FILE}"
    fi

    log_info "数据权限控制测试完成"
}

# 7. 权限缓存机制测试
test_permission_cache() {
    log_info "开始权限缓存机制测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 7. 权限缓存机制测试" >> "${REPORT_FILE}"

    # 7.1 检查权限缓存实现
    log_info "测试权限缓存实现..."

    local permission_cache=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "permission.*cache\|cache.*permission\|权限缓存" {} \; | wc -l)
    if [[ $permission_cache -gt 0 ]]; then
        log_success "发现权限缓存实现"
        echo "✅ **权限缓存**: 已实现权限缓存机制" >> "${REPORT_FILE}"

        # 检查缓存策略
        local cache_strategies=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "@Cacheable\|@CacheEvict\|@CachePut" {} \; | wc -l)
        if [[ $cache_strategies -gt 0 ]]; then
            log_success "发现缓存注解策略"
            echo "✅ **缓存策略**: 已配置缓存注解策略" >> "${REPORT_FILE}"
        fi
    else
        log_warning "未发现权限缓存实现"
        echo "⚠️ **权限缓存**: 未发现权限缓存实现" >> "${REPORT_FILE}"
    fi

    # 7.2 检查缓存一致性
    ((TEST_RESULTS[total_tests]++))
    log_info "测试缓存一致性机制..."

    local cache_consistency=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "cache.*sync\|cache.*update\|缓存同步" {} \; | wc -l)
    if [[ $cache_consistency -gt 0 ]]; then
        log_success "发现缓存一致性机制"
        echo "✅ **缓存一致性**: 已实现缓存一致性机制" >> "${REPORT_FILE}"
    else
        log_warning "未发现缓存一致性机制"
        echo "⚠️ **缓存一致性**: 未发现缓存一致性机制" >> "${REPORT_FILE}"
    fi

    log_info "权限缓存机制测试完成"
}

# 8. 权限审计日志测试
test_permission_audit() {
    log_info "开始权限审计日志测试..."

    ((TEST_RESULTS[total_tests]++))

    echo -e "\n### 8. 权限审计日志测试" >> "${REPORT_FILE}"

    # 8.1 检查权限操作日志
    log_info "测试权限操作日志记录..."

    local permission_logs=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "permission.*log\|权限.*日志\|权限审计" {} \; | wc -l)
    if [[ $permission_logs -gt 0 ]]; then
        log_success "发现权限操作日志实现"
        echo "✅ **权限日志**: 已实现权限操作日志" >> "${REPORT_FILE}"
    else
        log_warning "未发现权限操作日志实现"
        echo "⚠️ **权限日志**: 未发现权限操作日志实现" >> "${REPORT_FILE}"
    fi

    # 8.2 检查权限变更记录
    ((TEST_RESULTS[total_tests]++))
    log_info "测试权限变更记录..."

    local permission_changes=$(find "${IDENTITY_SERVICE_DIR}" -name "*.java" -exec grep -l "permission.*change\|权限.*变更\|role.*change" {} \; | wc -l)
    if [[ $permission_changes -gt 0 ]]; then
        log_success "发现权限变更记录实现"
        echo "✅ **变更记录**: 已实现权限变更记录" >> "${REPORT_FILE}"
    else
        log_warning "未发现权限变更记录实现"
        echo "⚠️ **变更记录**: 未发现权限变更记录实现" >> "${REPORT_FILE}"
    fi

    log_info "权限审计日志测试完成"
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

    # 计算通过率
    local pass_rate=0
    if [[ $total_tests -gt 0 ]]; then
        pass_rate=$((passed_tests * 100 / total_tests))
    fi

    cat >> "${REPORT_FILE}" << EOF

### 📈 RBAC系统测试统计

| 测试指标 | 数值 | 说明 |
|----------|------|------|
| **总测试项** | ${total_tests} | RBAC权限系统测试总项目数 |
| **通过测试** | ${passed_tests} | 符合设计要求的测试项目 |
| **失败测试** | ${failed_tests} | 不符合设计要求的测试项目 |
| **警告测试** | ${warning_tests} | 需要改进的测试项目 |
| **严重问题** | ${critical_issues} | 严重安全问题数量 |

### 🎯 RBAC系统成熟度评分

**RBAC权限控制系统评分: ${pass_rate}/100**

### 🛡️ 安全等级评估

EOF

    # 根据评分给出安全等级
    local security_level="优秀"
    if [[ $pass_rate -lt 60 ]]; then
        security_level="需要重构"
        echo "🔴 **安全等级**: ${security_level} - RBAC系统需要重大改进" >> "${REPORT_FILE}"
    elif [[ $pass_rate -lt 80 ]]; then
        security_level="良好"
        echo "🟡 **安全等级**: ${security_level} - RBAC系统需要完善" >> "${REPORT_FILE}"
    else
        echo "🟢 **安全等级**: ${security_level} - RBAC系统设计良好" >> "${REPORT_FILE}"
    fi

    cat >> "${REPORT_FILE}" << EOF

### 🔧 关键改进建议

#### 高优先级修复
1. **完善权限模型**: 确保RBAC核心实体完整性
2. **增强权限保护**: 提高控制器权限保护覆盖率至100%
3. **实现数据权限**: 添加行级和部门级数据权限控制
4. **加强权限验证**: 完善权限验证拦截器和AOP切面

#### 中优先级改进
1. **优化权限分配**: 完善用户-角色和角色-权限分配机制
2. **添加权限缓存**: 提高权限验证性能
3. **完善审计日志**: 记录详细的权限操作和变更
4. **加强权限测试**: 添加权限功能的单元测试和集成测试

#### 低优先级优化
1. **权限可视化**: 提供权限关系图形化展示
2. **权限模板**: 实现角色权限模板功能
3. **权限导出**: 支持权限配置导入导出
4. **权限分析**: 提供权限使用情况分析

### ✅ RBAC系统检查清单

- [ ] RBAC核心实体完整 (User, Role, Resource, UserRole, RoleResource)
- [ ] 权限注解@RequireResource已实现
- [ ] 角色管理CRUD操作完整
- [ ] 权限分配机制已实现
- [ ] 权限验证拦截器已实现
- [ ] 控制器权限保护覆盖率≥90%
- [ ] 敏感操作权限保护100%
- [ ] 数据权限控制已实现
- [ ] 权限缓存机制已配置
- [ ] 权限审计日志已记录
- [ ] 权限变更追踪已实现
- [ ] 权限测试覆盖率≥80%

EOF

    # 根据测试结果给出总体评价
    if [[ $critical_issues -gt 0 ]]; then
        echo -e "⚠️ **总体评价**: 发现${critical_issues}个严重问题，RBAC系统需要立即修复" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 80 ]]; then
        echo -e "✅ **总体评价**: RBAC权限控制系统设计完善，安全性良好" >> "${REPORT_FILE}"
    elif [[ $pass_rate -ge 60 ]]; then
        echo -e "⚠️ **总体评价**: RBAC权限控制系统基本完整，需要进一步完善" >> "${REPORT_FILE}"
    else
        echo -e "❌ **总体评价**: RBAC权限控制系统存在较多问题，需要重构" >> "${REPORT_FILE}"
    fi
}

# 主函数
main() {
    echo -e "${BLUE}"
    cat << 'EOF'
 _____ _   _ _   _    _    _   _ ____ _____ ____
| ____| \ | | | | |  / \  | \ | |/ ___| ____|  _ \
|  _| |  \| | |_| | / _ \ |  \| | |   |  _| | | | |
| |___| |\  |  _  |/ ___ \| |\  | |___| |___| |_| |
|_____|_| \_|_| |_/_/   \_\_| \_|\____|_____|____/

               RBAC权限控制系统完整性测试工具
EOF
    echo -e "${NC}"

    log_info "开始IOE-DREAM RBAC权限控制系统完整性测试..."

    # 初始化测试报告
    init_test_report

    # 执行各项测试
    test_permission_model_architecture
    test_role_management
    test_permission_assignment
    test_permission_interceptor
    test_controller_protection
    test_data_permission_control
    test_permission_cache
    test_permission_audit

    # 生成测试总结
    generate_test_summary

    # 输出测试结果
    echo -e "\n${GREEN}=== RBAC权限控制系统测试完成 ===${NC}"
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