#!/bin/bash
# =================================================================
# repowiki规范修复 - 快速启动脚本
# 目标：一键启动系统性修复流程
# 版本：v1.0
# 创建时间：2025-11-18
# =================================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# 显示横幅
show_banner() {
    echo -e "${CYAN}${BOLD}"
    echo "========================================"
    echo "     🚀 IOE-DREAM repowiki规范修复"
    echo "     系统性修复方案 - 快速启动"
    echo "     版本: v1.0"
    echo "     目标: 361个编译错误 → 0"
    echo "========================================"
    echo -e "${NC}"
}

# 显示菜单
show_menu() {
    echo -e "${BLUE}${BOLD}请选择修复阶段：${NC}"
    echo
    echo -e "${YELLOW}🔥 第一阶段：基础规范修复 (推荐优先执行)${NC}"
    echo "  1) 修复包名错误 (annoation→annotation)"
    echo "  2) 修复javax包名 (javax→jakarta)"
    echo "  3) 修复依赖注入 (@Autowired→@Resource)"
    echo "  4) 执行完整第一阶段修复"
    echo
    echo -e "${YELLOW}🏗️ 第二阶段：架构完整性修复${NC}"
    echo "  5) 补全Manager层"
    echo "  6) 更新Service层"
    echo
    echo -e "${YELLOW}🚀 第三阶段：缓存架构统一${NC}"
    echo "  7) 创建缓存核心组件"
    echo "  8) 迁移现有缓存代码"
    echo
    echo -e "${YELLOW}✅ 第四阶段：验证和部署${NC}"
    echo "  9) 全面验证修复结果"
    echo " 10) 生成部署清单"
    echo
    echo -e "${GREEN}🎯 快速选项${NC}"
    echo " 11) 执行完整修复流程 (推荐)"
    echo " 12) 检查当前状态"
    echo " 13) 生成修复报告"
    echo
    echo " 0) 退出"
    echo
}

# 检查环境
check_environment() {
    echo -e "${BLUE}[INFO]${NC} 检查环境..."

    if [ ! -f "pom.xml" ]; then
        echo -e "${RED}[ERROR]${NC} 请在项目根目录执行此脚本"
        exit 1
    fi

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        echo -e "${RED}[ERROR]${NC} 未找到smart-admin-api-java17-springboot3目录"
        exit 1
    fi

    if [ ! -f "scripts/script_01_fix_package_names.sh" ]; then
        echo -e "${RED}[ERROR]${NC} 未找到修复脚本文件"
        echo "请确保scripts目录下有完整的修复脚本"
        exit 1
    fi

    echo -e "${GREEN}[SUCCESS]${NC} 环境检查通过"
}

# 显示当前状态
show_current_status() {
    echo -e "${BLUE}[INFO]${NC} 检查当前状态..."
    echo
    echo -e "${YELLOW}=== 当前项目状态 ===${NC}"

    base_dir="smart-admin-api-java17-springboot3"

    # 基础规范检查
    javax_count=$(find "$base_dir" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    autowired_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    annoation_count=$(find "$base_dir" -name "*.java" -exec grep -l "annoation" {} \; 2>/dev/null | wc -l)
    resource_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Resource" {} \; 2>/dev/null | wc -l)

    echo -e "${CYAN}基础规范:${NC}"
    echo "  javax包使用: $javax_count $( [ $javax_count -eq 0 ] && echo "✅" || echo "❌" )"
    echo "  @Autowired使用: $autowired_count $( [ $autowired_count -eq 0 ] && echo "✅" || echo "❌" )"
    echo "  包名错误(annoation): $annoation_count $( [ $annoation_count -eq 0 ] && echo "✅" || echo "❌" )"
    echo "  @Resource使用: $resource_count ✅"

    # 编译状态检查
    echo
    echo -e "${CYAN}编译状态:${NC}"
    cd "$base_dir" 2>/dev/null && {
        error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR" || echo "0")
        cd .. 2>/dev/null
        echo "  编译错误: $error_count $( [ $error_count -eq 0 ] && echo "✅" || echo "❌" )"
    } || {
        echo "  编译状态: ❌ 无法检查"
    }

    # 架构完整性检查
    echo
    echo -e "${CYAN}架构完整性:${NC}"
    controller_count=$(find "$base_dir" -name "*Controller.java" -type f | wc -l)
    service_count=$(find "$base_dir" -name "*Service*.java" -type f | wc -l)
    manager_count=$(find "$base_dir" -name "*Manager.java" -type f | wc -l)
    dao_count=$(find "$base_dir" -name "*Dao.java" -type f | wc -l)

    echo "  Controller层: $controller_count"
    echo "  Service层: $service_count"
    echo "  Manager层: $manager_count"
    echo "  DAO层: $dao_count"

    if [ $controller_count -gt 0 ] && [ $service_count -gt 0 ] && [ $manager_count -gt 0 ] && [ $dao_count -gt 0 ]; then
        echo "  架构状态: ✅ 基本完整"
    else
        echo "  架构状态: ⚠️ 不完整 (需要补全Manager层)"
    fi

    # 修复进度
    total_issues=$((javax_count + autowired_count + annoation_count))
    if [ $total_issues -eq 0 ]; then
        progress=100
    else
        progress=$(( (resource_count - total_issues) * 100 / resource_count ))
    fi

    echo
    echo -e "${GREEN}修复进度: $progress%${NC}"
    echo -e "${GREEN}剩余问题: $total_issues${NC}"
    echo
}

# 执行第一阶段修复
execute_stage1() {
    local choice=$1

    echo -e "${BLUE}[INFO]${NC} 执行第一阶段修复..."
    echo

    case $choice in
        1|2|3|4)
            echo -e "${YELLOW}即将执行: script_01_fix_package_names.sh${NC}"
            echo -e "${CYAN}此脚本将修复：${NC}"
            echo "  - annoation → annotation 包名错误"
            echo "  - javax → jakarta 包名"
            echo "  - @Autowired → @Resource 依赖注入"
            echo
            read -p "确认执行？(y/N): " confirm
            if [[ $confirm =~ ^[Yy]$ ]]; then
                chmod +x scripts/script_01_fix_package_names.sh
                bash scripts/script_01_fix_package_names.sh
            else
                echo "已取消执行"
            fi
            ;;
        *)
            echo "无效选择"
            ;;
    esac
}

# 执行第二阶段修复
execute_stage2() {
    local choice=$1

    echo -e "${BLUE}[INFO]${NC} 执行第二阶段修复..."
    echo

    case $choice in
        5)
            echo -e "${YELLOW}即将执行: script_02_create_missing_managers.sh${NC}"
            echo -e "${CYAN}此脚本将：${NC}"
            echo "  - 创建缺失的Manager层"
            echo "  - 提供Manager层标准模板"
            echo "  - 确保四层架构完整性"
            echo
            read -p "确认执行？(y/N): " confirm
            if [[ $confirm =~ ^[Yy]$ ]]; then
                chmod +x scripts/script_02_create_missing_managers.sh
                bash scripts/script_02_create_missing_managers.sh
            else
                echo "已取消执行"
            fi
            ;;
        6)
            echo -e "${YELLOW}手动更新Service层${NC}"
            echo "请按照以下步骤手动更新Service层："
            echo "1. 在Service类中添加Manager依赖注入"
            echo "2. 修改Service方法，调用Manager层方法"
            echo "3. 确保Service层不直接访问DAO层"
            echo
            echo "示例代码："
            echo "@Resource"
            echo "private AccountManager accountManager;"
            echo
            echo "public AccountVO getAccount(Long userId) {"
            echo "    return accountManager.getAccount(userId);"
            echo "}"
            ;;
        *)
            echo "无效选择"
            ;;
    esac
}

# 执行第三阶段修复
execute_stage3() {
    local choice=$1

    echo -e "${BLUE}[INFO]${NC} 执行第三阶段修复..."
    echo

    case $choice in
        7)
            echo -e "${YELLOW}即将执行: script_03_unify_cache_architecture.sh${NC}"
            echo -e "${CYAN}此脚本将：${NC}"
            echo "  - 创建缓存架构核心组件"
            echo "  - 定义BusinessDataType枚举"
            echo "  - 提供UnifiedCacheService接口"
            echo
            read -p "确认执行？(y/N): " confirm
            if [[ $confirm =~ ^[Yy]$ ]]; then
                chmod +x scripts/script_03_unify_cache_architecture.sh
                bash scripts/script_03_unify_cache_architecture.sh
            else
                echo "已取消执行"
            fi
            ;;
        8)
            echo -e "${YELLOW}手动迁移缓存代码${NC}"
            echo "请按照迁移指南手动迁移现有缓存代码："
            echo "1. 使用UnifiedCacheService替代RedisTemplate"
            echo "2. 选择合适的BusinessDataType"
            echo "3. 应用getOrSet模式防止缓存穿透"
            echo "4. 使用模块化缓存管理"
            echo
            echo "详细迁移指南请查看生成的文档文件"
            ;;
        *)
            echo "无效选择"
            ;;
    esac
}

# 执行第四阶段修复
execute_stage4() {
    local choice=$1

    echo -e "${BLUE}[INFO]${NC} 执行第四阶段修复..."
    echo

    case $choice in
        9)
            echo -e "${YELLOW}即将执行: script_04_validate_and_deploy.sh${NC}"
            echo -e "${CYAN}此脚本将：${NC}"
            echo "  - 全面验证修复结果"
            echo "  - 检查repowiki规范符合性"
            echo "  - 生成验证报告"
            echo
            read -p "确认执行？(y/N): " confirm
            if [[ $confirm =~ ^[Yy]$ ]]; then
                chmod +x scripts/script_04_validate_and_deploy.sh
                bash scripts/script_04_validate_and_deploy.sh
            else
                echo "已取消执行"
            fi
            ;;
        10)
            echo -e "${YELLOW}生成部署检查清单${NC}"
            echo "将自动生成部署前的检查清单..."

            # 生成简化的部署清单
            local checklist_file="quick_deployment_checklist_$(date +%Y%m%d_%H%M%S).txt"

            cat > "$checklist_file" << EOF
repowiki规范部署检查清单
生成时间: $(date)

=== 部署前必须检查 ===
1. 编译检查
   [ ] mvn clean compile 成功
   [ ] 编译错误数量: 0

2. 规范检查
   [ ] javax包使用: 0
   [ ] @Autowired使用: 0
   [ ] 包名错误: 0

3. 功能测试
   [ ] 核心功能测试通过
   [ ] 用户登录正常
   [ ] 业务流程正常

=== 部署步骤 ===
1. 备份当前版本
2. 停止应用服务
3. 部署新版本
4. 验证功能正常
5. 启动监控告警

=== 部署后验证 ===
1. 应用启动成功
2. 健康检查通过
3. 核心功能正常
4. 性能指标正常

=== 应急回滚 ===
发现问题立即执行回滚
1. 停止新版本
2. 恢复备份版本
3. 验证功能正常
EOF

            echo -e "${GREEN}部署检查清单已生成: $checklist_file${NC}"
            ;;
        *)
            echo "无效选择"
            ;;
    esac
}

# 执行完整修复流程
execute_complete_repair() {
    echo -e "${BLUE}[INFO]${NC} 执行完整修复流程..."
    echo
    echo -e "${YELLOW}${BOLD}⚠️ 警告：这将执行完整的修复流程，耗时可能较长${NC}"
    echo
    echo "修复流程包括："
    echo "1. 基础规范修复（包名、依赖注入）"
    echo "2. 架构完整性修复（Manager层）"
    echo "3. 缓存架构统一化"
    echo "4. 全面验证和报告生成"
    echo
    read -p "确认执行完整修复流程？(y/N): " confirm

    if [[ $confirm =~ ^[Yy]$ ]]; then
        echo -e "${GREEN}[START]${NC} 开始执行完整修复流程..."
        echo

        # 第一阶段
        echo -e "${BLUE}[1/4]${NC} 执行第一阶段：基础规范修复..."
        chmod +x scripts/script_01_fix_package_names.sh
        bash scripts/script_01_fix_package_names.sh

        echo
        # 第二阶段
        echo -e "${BLUE}[2/4]${NC} 执行第二阶段：架构完整性修复..."
        chmod +x scripts/script_02_create_missing_managers.sh
        bash scripts/script_02_create_missing_managers.sh

        echo
        # 第三阶段
        echo -e "${BLUE}[3/4]${NC} 执行第三阶段：缓存架构统一化..."
        chmod +x scripts/script_03_unify_cache_architecture.sh
        bash scripts/script_03_unify_cache_architecture.sh

        echo
        # 第四阶段
        echo -e "${BLUE}[4/4]${NC} 执行第四阶段：验证和部署..."
        chmod +x scripts/script_04_validate_and_deploy.sh
        bash scripts/script_04_validate_and_deploy.sh

        echo
        echo -e "${GREEN}[COMPLETE]${NC} 完整修复流程执行完成！"
    else
        echo "已取消执行"
    fi
}

# 生成修复报告
generate_quick_report() {
    echo -e "${BLUE}[INFO]${NC} 生成修复报告..."

    local report_file="quick_repair_report_$(date +%Y%m%d_%H%M%S).md"

    base_dir="smart-admin-api-java17-springboot3"
    javax_count=$(find "$base_dir" -name "*.java" -exec grep -l "javax\." {} \; 2>/dev/null | wc -l)
    autowired_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Autowired" {} \; 2>/dev/null | wc -l)
    annoation_count=$(find "$base_dir" -name "*.java" -exec grep -l "annoation" {} \; 2>/dev/null | wc -l)
    resource_count=$(find "$base_dir" -name "*.java" -exec grep -l "@Resource" {} \; 2>/dev/null | wc -l)

    cd "$base_dir" 2>/dev/null && {
        error_count=$(mvn clean compile -q 2>&1 | grep -c "ERROR" || echo "0")
        cd .. 2>/dev/null
    } || {
        error_count=-1
    }

    cat > "$report_file" << EOF
# IOE-DREAM repowiki规范修复快速报告

**生成时间**: $(date)
**项目状态**: 修复中

## 📊 当前状态

### 基础规范
- javax包使用: $javax_count
- @Autowired使用: $autowired_count
- 包名错误(annoation): $annoation_count
- @Resource使用: $resource_count

### 编译状态
- 编译错误: $error_count

### 修复进度
- 剩余问题: $((javax_count + autowired_count + annoation_count))
- 总体进度: $(( (resource_count - (javax_count + autowired_count + annoation_count)) * 100 / resource_count ))%

## 🎯 推荐行动

### 立即执行
1. 运行 \`./scripts/script_01_fix_package_names.sh\`
2. 检查编译结果
3. 解决剩余编译错误

### 中期执行
1. 运行 \`./scripts/script_02_create_missing_managers.sh\`
2. 更新Service层代码
3. 运行 \`./scripts/script_03_unify_cache_architecture.sh\`

### 最终验证
1. 运行 \`./scripts/script_04_validate_and_deploy.sh\`
2. 生成部署清单
3. 执行部署

---
**基于**: repowiki开发规范体系 v1.1
EOF

    echo -e "${GREEN}快速修复报告已生成: $report_file${NC}"
}

# 主循环
main() {
    show_banner

    while true; do
        show_menu
        read -p "请选择 (0-13): " choice

        case $choice in
            1|2|3|4)
                execute_stage1 $choice
                ;;
            5|6)
                execute_stage2 $choice
                ;;
            7|8)
                execute_stage3 $choice
                ;;
            9|10)
                execute_stage4 $choice
                ;;
            11)
                execute_complete_repair
                ;;
            12)
                show_current_status
                ;;
            13)
                generate_quick_report
                ;;
            0)
                echo -e "${GREEN}[INFO]${NC} 退出修复程序"
                exit 0
                ;;
            *)
                echo -e "${RED}[ERROR]${NC} 无效选择，请重新输入"
                ;;
        esac

        echo
        read -p "按回车键继续..."
        echo
    done
}

# 检查环境并启动
check_environment
main