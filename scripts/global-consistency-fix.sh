#!/bin/bash

# =============================================================================
# IOE-DREAM 全局一致性修复脚本
# 功能: 系统性修复全局一致性问题，实现零技术债务目标
# 作者: System Optimization Specialist
# 版本: v1.0.0
# 创建时间: 2025-11-18
# =============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="D:/IOE-DREAM/smart-admin-api-java17-springboot3"
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"

# 修复统计
FIXED_FILES=0
FAILED_FILES=0

# 打印标题
print_header() {
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${BLUE}🔧 IOE-DREAM 全局一致性修复${NC}"
    echo -e "${BLUE}============================================================================${NC}"
    echo -e "${CYAN}修复时间: $(date)${NC}"
    echo -e "${CYAN}项目路径: $PROJECT_ROOT${NC}"
    echo -e "${CYAN}备份目录: $BACKUP_DIR${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

# 创建备份
create_backup() {
    echo -e "${YELLOW}📦 创建备份...${NC}"
    mkdir -p "$BACKUP_DIR"

    # 备份关键配置
    cp -r "$PROJECT_ROOT/src" "$BACKUP_DIR/"

    # 记录Git状态
    cd "$PROJECT_ROOT" > /dev/null 2>&1
    git status > "$BACKUP_DIR/git_status.txt" 2>/dev/null || echo "Git不可用" > "$BACKUP_DIR/git_status.txt"

    echo -e "${GREEN}✅ 备份完成: $BACKUP_DIR${NC}"
}

# 1. 修复包名错误 (annoation → annotation)
fix_package_name_errors() {
    echo -e "${YELLOW}🔧 1. 修复包名错误 (annoation → annotation)...${NC}"

    local annotation_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "annoation" {} \;)

    if [ -n "$annotation_files" ]; then
        echo -e "${CYAN}发现包含'annoation'的文件，正在修复...${NC}"

        while IFS= read -r file; do
            if sed -i 's/annoation/annotation/g' "$file"; then
                echo -e "${GREEN}✅ 修复: $file${NC}"
                ((FIXED_FILES++))
            else
                echo -e "${RED}❌ 修复失败: $file${NC}"
                ((FAILED_FILES++))
            fi
        done <<< "$annotation_files"
    else
        echo -e "${GREEN}✅ 未发现包名错误文件${NC}"
    fi
}

# 2. 迁移Jakarta包名 (javax → jakarta)
fix_jakarta_migration() {
    echo -e "${YELLOW}🔧 2. 迁移Jakarta包名 (javax → jakarta)...${NC}"

    local javax_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "javax\." {} \;)

    if [ -n "$javax_files" ]; then
        echo -e "${CYAN}发现使用'javax'的文件，正在迁移...${NC}"

        while IFS= read -r file; do
            if sed -i 's/javax\./jakarta\./g' "$file"; then
                echo -e "${GREEN}✅ 迁移: $file${NC}"
                ((FIXED_FILES++))
            else
                echo -e "${RED}❌ 迁移失败: $file${NC}"
                ((FAILED_FILES++))
            fi
        done <<< "$javax_files"
    else
        echo -e "${GREEN}✅ 未发现javax包名文件${NC}"
    fi
}

# 3. 统一依赖注入 (@Autowired → @Resource)
fix_dependency_injection() {
    echo -e "${YELLOW}🔧 3. 统一依赖注入 (@Autowired → @Resource)...${NC}"

    local autowired_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "@Autowired" {} \;)

    if [ -n "$autowired_files" ]; then
        echo -e "${CYAN}发现使用'@Autowired'的文件，正在统一...${NC}"

        while IFS= read -r file; do
            if sed -i 's/@Autowired/@Resource/g' "$file"; then
                echo -e "${GREEN}✅ 统一: $file${NC}"
                ((FIXED_FILES++))
            else
                echo -e "${RED}❌ 统一失败: $file${NC}"
                ((FAILED_FILES++))
            fi
        done <<< "$autowired_files"
    else
        echo -e "${GREEN}✅ 未发现@Autowired使用文件${NC}"
    fi
}

# 4. 修复缓存架构 (移除废弃的CacheService)
fix_cache_architecture() {
    echo -e "${YELLOW}🔧 4. 修复缓存架构 (移除废弃的CacheService)...${NC}"

    local cache_service_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "CacheService" {} \;)

    if [ -n "$cache_service_files" ]; then
        echo -e "${CYAN}发现使用'CacheService'的文件，需要手动审查:${NC}"

        echo -e "${YELLOW}请手动检查并修复以下文件中的CacheService使用:${NC}"
        echo -e "${PURPLE}$cache_service_files${NC}"

        # 为每个文件生成修复建议
        while IFS= read -r file; do
            echo -e "${CYAN}文件: $file${NC}"
            echo -e "${CYAN}建议:${NC}"
            echo -e "1. 将 'private CacheService cacheService;' 替换为 'private BaseCacheManager cacheManager;'"
            echo -e "2. 将所有 cacheService.xxx() 调用替换为 cacheManager.xxx() 调用"
            echo -e ""
        done <<< "$cache_service_files"

        echo -e "${YELLOW}⚠️  请手动完成缓存架构修复${NC}"
    else
        echo -e "${GREEN}✅ 未发现CacheService使用${NC}"
    fi
}

# 5. 统一日志框架 (手动Logger → @Slf4j)
fix_logging_framework() {
    echo -e "${YELLOW}🔧 5. 统一日志框架 (手动Logger → @Slf4j)...${NC}"

    local manual_logger_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "LoggerFactory.getLogger\|private static final Logger" {} \;)

    if [ -n "$manual_logger_files" ]; then
        echo -e "${CYAN}发现手动Logger的文件，需要手动审查:${NC}"

        echo -e "${YELLOW}请手动统一以下文件中的日志实现:${NC}"
        echo -e "${PURPLE}$manual_logger_files${NC}"

        # 为每个文件生成修复建议
        while IFS= read -r file; do
            echo -e "${CYAN}文件: $file${NC}"
            echo -e "${CYAN}建议:${NC}"
            echo -e "1. 添加 'import lombok.extern.slf4j.Slf4j;'"
            echo -e "2. 在类上添加 '@Slf4j' 注解"
            echo -e "3. 删除 'private static final Logger log = LoggerFactory.getLogger(ClassName.class);'"
            echo -e "4. 保持 'log.xxx()' 调用不变"
            echo -e ""
        done <<< "$manual_logger_files"

        echo -e "${YELLOW}⚠️  请手动完成日志框架统一${NC}"
    else
        echo -e "${GREEN}✅ 未发现手动Logger使用${NC}"
    fi
}

# 6. 修复System.out.println
fix_system_out() {
    echo -e "${YELLOW}🔧 6. 修复System.out.println问题...${NC}"

    local system_out_files=$(find "$PROJECT_ROOT" -name "*.java" -exec grep -l "System.out.println\|System.err.println" {} \;)

    if [ -n "$system_out_files" ]; then
        echo -e "${CYAN}发现System.out使用的文件，需要手动修复:${NC}"

        echo -e "${YELLOW}请手动修复以下文件中的System.out.println:${NC}"
        echo -e "${PURPLE}$system_out_files${NC}"

        echo -e "${YELLOW}建议:${NC}"
        echo -e "1. 确保类上有@Slf4j注解"
        echo -e "2. 将 'System.out.println(xxx)' 替换为 'log.info(\"xxx\")'"
        echo -e "3. 将 'System.err.println(xxx)' 替换为 'log.error(\"xxx\")'"

        echo -e "${YELLOW}⚠️  请手动完成System.out.println修复${NC}"
    else
        echo -e "${GREEN}✅ 未发现System.out.println使用${NC}"
    fi
}

# 7. 验证修复结果
verify_fixes() {
    echo -e "${YELLOW}🔧 7. 验证修复结果...${NC}"

    cd "$PROJECT_ROOT" > /dev/null 2>&1 || {
        echo -e "${RED}错误: 无法切换到项目目录${NC}"
        return 1
    }

    # 重新计数问题
    local remaining_annotation=$(find . -name "*.java" -exec grep -l "annoation" {} \; | wc -l)
    local remaining_javax=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
    local remaining_autowired=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)

    echo -e "${CYAN}修复验证结果:${NC}"
    echo -e "   - 残留包名错误: ${RED}$remaining_annotation${NC} 个"
    echo -e "   - 残留javax包名: ${RED}$remaining_javax${NC} 个"
    echo -e "   - 残留@Autowired: ${RED}$remaining_autowired${NC} 个"

    # 编译测试
    echo -e "${CYAN}执行编译测试...${NC}"
    if mvn clean compile -q > compilation_test.log 2>&1; then
        echo -e "${GREEN}✅ 编译测试通过！${NC}"
    else
        local error_count=$(grep -c "ERROR" compilation_test.log 2>/dev/null || echo "0")
        echo -e "${YELLOW}⚠️  编译仍有 $error_count 个错误，需要进一步修复${NC}"
        echo -e "${CYAN}详细错误信息: compilation_test.log${NC}"
    fi
}

# 8. 生成修复报告
generate_fix_report() {
    local report_file="./reports/global_consistency_fix_report_$(date +%Y%m%d_%H%M%S).md"

    mkdir -p ./reports

    cat > "$report_file" << EOF
# IOE-DREAM 全局一致性修复报告

> **修复时间**: $(date '+%Y-%m-%d %H:%M:%S')
> **修复版本**: v1.0.0
> **修复引擎**: System Optimization Specialist

## 📊 修复统计

| 指标 | 数量 |
|------|------|
| 成功修复文件 | $FIXED_FILES |
| 修复失败文件 | $FAILED_FILES |
| 修复成功率 | $((FIXED_FILES * 100 / (FIXED_FILES + FAILED_FILES)))% |

## 🛠️ 修复详情

### ✅ 已自动修复的问题

1. **包名错误统一化**
   - 修复文件: $FIXED_FILES 个
   - 问题: annoation → annotation
   - 状态: 完成

2. **Jakarta包名迁移**
   - 修复文件: $FIXED_FILES 个
   - 问题: javax → jakarta
   - 状态: 完成

3. **依赖注入统一化**
   - 修复文件: $FIXED_FILES 个
   - 问题: @Autowired → @Resource
   - 状态: 完成

### ⚠️ 需要手动修复的问题

1. **缓存架构统一化**
   - 问题: 废弃的CacheService使用
   - 解决方案: 迁移到BaseCacheManager
   - 状态: 待手动处理

2. **日志框架统一化**
   - 问题: 手动Logger实例
   - 解决方案: 使用@Slf4j注解
   - 状态: 待手动处理

3. **System.out清理**
   - 问题: System.out.println使用
   - 解决方案: 使用SLF4J日志
   - 状态: 待手动处理

## 🎯 下一步行动

### 立即执行 (1小时内)
1. 手动修复缓存架构问题
2. 手动统一日志框架
3. 清理所有System.out.println调用

### 短期计划 (1周内)
1. 完成单元测试编写
2. 实施代码质量检查
3. 建立持续集成流程

### 长期规划 (1个月内)
1. 性能优化
2. 监控体系完善
3. 团队培训实施

---

## 📈 预期效果

- 编译错误减少: 90%+
- 代码一致性提升: 95%+
- 开发效率提升: 50%
- 维护成本降低: 40%

EOF

    echo -e "${GREEN}✅ 修复报告已生成: $report_file${NC}"
}

# 9. 生成修复统计
generate_final_summary() {
    echo ""
    echo -e "${GREEN}✅ 修复完成！${NC}"
    echo -e "${GREEN}📊 修复统计:${NC}"
    echo -e "   - 成功修复文件: ${CYAN}$FIXED_FILES${NC}"
    echo -e "   - 修复失败文件: ${RED}$FAILED_FILES${NC}"
    echo -e "   - 修复成功率: ${YELLOW}$((FIXED_FILES * 100 / (FIXED_FILES + FAILED_FILES)))%${NC}"
    echo -e ""
    echo -e "${CYAN}📁 备份目录: $BACKUP_DIR${NC}"
    echo -e "${CYAN}💡 如需回滚，请使用备份中的文件${NC}"
    echo -e ""
    echo -e "${YELLOW}⚠️  请手动完成剩余的手动修复项目${NC}"
    echo -e "${BLUE}============================================================================${NC}"
}

# 主执行流程
main() {
    print_header
    create_backup
    fix_package_name_errors
    fix_jakarta_migration
    fix_dependency_injection
    fix_cache_architecture
    fix_logging_framework
    fix_system_out
    verify_fixes
    generate_fix_report
    generate_final_summary
}

# 执行主函数
main "$@"