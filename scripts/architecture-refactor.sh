#!/bin/bash

# IOE-DREAM 架构彻底重构分析脚本（只分析不修改）
# 用途：分析需要修改的文件和依赖关系
# 作者：IOE-DREAM 架构委员会
# 日期：2025-12-22

set -e

echo "🔍 IOE-DREAM 架构重构分析脚本"
echo "======================================"
echo "⚠️  注意：此脚本只进行分析，不会修改任何代码文件"
echo ""

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

# 检查当前目录
if [ ! -d "microservices" ]; then
    log_error "请在项目根目录执行此脚本"
    exit 1
fi

# Phase 1: 分析需要修改的包路径
analyze_package_paths() {
    log_info "Phase 1: 分析需要修改的包路径..."

    # 1. 分析ResponseDTO导入问题
    local response_dto_files=$(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\..*ResponseDTO" {} \;)
    if [ ! -z "$response_dto_files" ]; then
        log_warning "  发现 $GREEN$(echo "$response_dto_files" | wc -l)$NC 个文件需要更新ResponseDTO导入路径："
        echo "$response_dto_files" | head -5
        log_info "    需要改为: net.lab1024.sa.platform.core.dto.ResponseDTO"
    fi

    # 2. 分析异常类导入问题
    local exception_files=$(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\..*Exception" {} \;)
    if [ ! -z "$exception_files" ]; then
        log_warning "  发现 $GREEN$(echo "$exception_files" | wc -l)$NC 个文件需要更新异常类导入路径："
        echo "$exception_files" | head -5
        log_info "    需要改为: net.lab1024.sa.platform.core.exception.*"
    fi

    # 3. 分析工具类导入问题
    local util_files=$(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.util\." {} \;)
    if [ ! -z "$util_files" ]; then
        log_warning "  发现 $GREEN$(echo "$util_files" | wc -l)$NC 个文件需要更新工具类导入路径："
        echo "$util_files" | head -5
        log_info "    需要改为: net.lab1024.sa.platform.core.util.*"
    fi

    # 4. 分析分页结果导入问题
    local page_result_files=$(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.domain\.PageResult" {} \;)
    if [ ! -z "$page_result_files" ]; then
        log_warning "  发现 $GREEN$(echo "$page_result_files" | wc -l)$NC 个文件需要更新PageResult导入路径："
        echo "$page_result_files" | head -5
        log_info "    需要改为: net.lab1024.sa.platform.core.dto.PageResult"
    fi
}

# Phase 2: 分析Maven依赖问题
analyze_maven_dependencies() {
    log_info "Phase 2: 分析Maven依赖问题..."

    local services=(
        "ioedream-access-service"
        "ioedream-attendance-service"
        "ioedream-consume-service"
        "ioedream-video-service"
        "ioedream-visitor-service"
        "ioedream-device-comm-service"
    )

    for service in "${services[@]}"; do
        if [ -d "microservices/$service" ]; then
            local pom_file="microservices/$service/pom.xml"
            if [ -f "$pom_file" ]; then
                # 检查是否有microservices-common依赖
                if grep -q "microservices-common" "$pom_file"; then
                    log_warning "  $service 仍在使用microservices-common依赖，需要更新"
                    log_info "    建议: 移除microservices-common依赖，使用platform-core依赖"
                fi

                # 检查是否有细粒度模块依赖
                local fine_grained_deps=$(grep -c "microservices-common-" "$pom_file" 2>/dev/null || echo "0")
                if [ "$fine_grained_deps" -gt "0" ]; then
                    log_warning "  $service 仍在使用 $fine_grained_deps 个细粒度模块依赖，需要更新"
                    log_info "    建议: 移除细粒度模块依赖，使用platform-business依赖"
                fi
            else
                log_warning "  $service 的pom.xml文件不存在"
            fi
        else
            log_warning "  $service 目录不存在，跳过"
        fi
    done
}

# Phase 3: 分析父POM问题
analyze_parent_pom() {
    log_info "Phase 3: 分析父POM问题..."

    local parent_pom="microservices/pom.xml"
    if [ -f "$parent_pom" ]; then
        # 检查是否有microservices-common模块
        if grep -q "microservices-common" "$parent_pom"; then
            log_warning "  父POM中仍引用microservices-common相关模块"
            log_info "    建议: 移除废弃的common模块引用"
        fi
    else
        log_warning "父POM文件不存在"
    fi
}

# Phase 4: 生成分析报告
generate_analysis_report() {
    log_info "Phase 4: 生成分析报告..."

    local report_file="ARCHITECTURE_REFACTOR_ANALYSIS_$(date +%Y%m%d_%H%M%S).md"

    cat > "$report_file" << EOF
# IOE-DREAM 架构重构分析报告

## 分析时间
- 开始时间: $(date)
- 分析脚本: scripts/architecture-refactor.sh

## 分析结果摘要

### 📊 需要修改的文件统计

#### 包路径问题
- **ResponseDTO导入**: $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\..*ResponseDTO" {} \; | wc -l) 个文件
- **异常类导入**: $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\..*Exception" {} \; | wc -l) 个文件
- **工具类导入**: $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.util\." {} \; | wc -l) 个文件
- **PageResult导入**: $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.domain\.PageResult" {} \; | wc -l) 个文件

#### Maven依赖问题
- **使用microservices-common的服务**: $(find microservices -name "pom.xml" -exec grep -l "microservices-common" {} \; | wc -l) 个
- **使用细粒度模块的服务**: $(find microservices -name "pom.xml" -exec grep -c "microservices-common-" {} \; | awk '{sum+=$0} END') 个

## 详细修改指南

### 1. 包路径映射表

| 原包路径 | 新包路径 | 影响文件数 |
|---------|---------|-----------|
| net.lab1024.sa.common.domain.ResponseDTO | net.lab1024.sa.platform.core.dto.ResponseDTO | $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.domain\.ResponseDTO" {} \; | wc -l) |
| net.lab1024.sa.common.dto.ResponseDTO | net.lab1024.sa.platform.core.dto.ResponseDTO | $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.dto\.ResponseDTO" {} \; | wc -l) |
| net.lab1024.sa.common.exception.BusinessException | net.lab1024.sa.platform.core.exception.BusinessException | $(find microservices -name "*.java" -type f -exec grep -l "net\.lab1024\.sa\.common\.exception\.BusinessException" {} \; | wc -l) |
| net.lab1024.sa.common.exception.SystemException | net.lab1024.sa.platform.core.exception.SystemException | $(find microservices -name "*.java" -type f -exec grep -l "net.lab1024\.sa\.common\.exception\.SystemException" {} \; | wc -l) |

### 2. 依赖更新模板

#### 业务服务POM.xml标准模板
\`\`\xml
<dependencies>
    <!-- 只依赖平台核心 -->
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>platform-core</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- 通过网关调用其他服务 -->
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>platform-gateway</artifactId>
        <version>1.0.0</version>
    </dependency>

    <!-- 业务逻辑依赖平台业务层 -->
    <dependency>
        <groupId>net.lab1024.sa</groupId>
        <artifactId>platform-business</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
\`\`

### 3. 手动执行步骤

#### Step 1: 包路径统一化（预计2-3小时）
1. 更新所有ResponseDTO导入语句
2. 更新所有异常类导入语句
3. 更新所有工具类导入语句
4. 更新所有PageResult导入语句

#### Step 2: Maven依赖更新（预计1-2小时）
1. 更新各业务服务的pom.xml文件
2. 移除对microservices-common的依赖
3. 移除对细粒度模块的直接依赖
4. 添加对platform模块的依赖

#### Step 3: 编译验证（预计1小时）
1. 编译新平台模块
2. 编译业务服务
3. 修复编译错误
4. 运行测试验证

## 风险控制

### 回滚策略
1. **Git分支保护**: 在修改前创建备份分支
2. **分阶段执行**: 按模块逐步修改
3. **编译验证**: 每阶段完成后立即验证

### 质量标准
- ✅ 所有模块编译成功
- ✅ 零循环依赖
- ✅ 服务启动正常

---
分析完成时间: $(date)
EOF

    log_success "分析报告已生成: $report_file"
}

# 主执行流程
main() {
    log_info "开始IOE-DREAM架构重构分析"
    log_info "此脚本只进行分析，不会修改任何代码文件"
    echo ""

    # 执行各个分析阶段
    analyze_package_paths
    echo ""

    analyze_maven_dependencies
    echo ""

    analyze_parent_pom
    echo ""

    generate_analysis_report
    echo ""

    log_success "🔍 架构重构分析完成！"
    echo ""
    log_info "请查看分析报告了解具体修改内容"
    log_info "建议按照报告中的步骤手动执行重构"
    echo ""
    log_warning "⚠️  请务必备份后再执行代码修改"
}

# 脚本入口
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi