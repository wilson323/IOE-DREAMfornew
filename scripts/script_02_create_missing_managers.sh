#!/bin/bash
# =================================================================
# repowiki规范修复脚本 - 第二阶段：四层架构完整性修复
# 目标：补全缺失的Manager层，确保Controller→Service→Manager→DAO完整链路
# 版本：v1.0
# 创建时间：2025-11-18
# =================================================================

set -e  # 遇到错误立即退出

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

# 检查是否在正确的目录
check_directory() {
    if [ ! -f "pom.xml" ]; then
        log_error "请确保在项目根目录（包含pom.xml的目录）执行此脚本"
        exit 1
    fi

    if [ ! -d "smart-admin-api-java17-springboot3" ]; then
        log_error "未找到smart-admin-api-java17-springboot3目录"
        exit 1
    fi

    log_success "目录检查通过"
}

# 创建Manager层目录结构
create_manager_directories() {
    log_info "=== 创建Manager层目录结构 ==="

    BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module"

    # 需要创建Manager目录的模块
    declare -A manager_modules=(
        ["consume"]=1
        ["attendance"]=1
        ["smart/access"]=1
        ["smart/video"]=1
        ["smart/monitor"]=1
        ["oa/document"]=1
        ["oa/workflow"]=1
        ["hr"]=1
        ["system/device"]=1
    )

    for module in "${!manager_modules[@]}"; do
        manager_dir="$BASE_DIR/$module/manager"
        if [ ! -d "$manager_dir" ]; then
            mkdir -p "$manager_dir"
            log_info "创建目录: $manager_dir"
        fi
    done

    log_success "Manager层目录结构创建完成"
}

# Manager层模板
create_manager_template() {
    local module_name="$1"
    local entity_name="$2"
    local manager_name="$3"
    local file_path="$4"

    cat > "$file_path" << EOF
package net.lab1024.sa.admin.module.${module_name}.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.*;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * ${manager_name} - 业务管理器
 * 负责复杂业务逻辑处理、缓存管理、第三方服务集成
 *
 * 基于repowiki架构设计规范 - Manager层职责：
 * 1. 复杂业务逻辑处理
 * 2. 缓存管理和一致性保证
 * 3. 第三方服务集成
 * 4. 跨Repository数据组装
 * 5. 复杂计算和业务规则
 *
 * @author SmartAdmin
 * @date 2025-11-18
 * @version 1.0
 */
@Component
@Slf4j
public class ${manager_name} {

    @Resource
    private UnifiedCacheService unifiedCacheService;

    @Resource
    private EnhancedCacheMetricsCollector metricsCollector;

    // TODO: 添加对应的DAO依赖
    // @Resource
    // private ${entity_name}Dao ${entity_name?lower_case}Dao;

    /**
     * 获取${entity_name}信息（带缓存）
     *
     * 使用getOrSet模式防止缓存穿透，基于repowiki缓存架构规范
     *
     * @param id 实体ID
     * @return ${entity_name}VO
     */
    public ${entity_name}VO get${entity_name}(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = id.toString();

        // 防缓存穿透模式
        return unifiedCacheService.getOrSet(
            CacheModule.${module_name?upper_case},
            "${entity_name?lower_case}",
            cacheKey,
            () -> this.load${entity_name}FromDatabase(id),
            ${entity_name}VO.class,
            BusinessDataType.NORMAL
        );
    }

    /**
     * 设置${entity_name}缓存
     *
     * @param id 实体ID
     * @param ${entity_name?lower_case}VO 实体VO
     */
    public void set${entity_name}Cache(Long id, ${entity_name}VO ${entity_name?lower_case}VO) {
        String cacheKey = id.toString();

        unifiedCacheService.set(
            CacheModule.${module_name?upper_case},
            "${entity_name?lower_case}",
            cacheKey,
            ${entity_name?lower_case}VO,
            BusinessDataType.NORMAL
        );

        // 记录缓存设置指标
        metricsCollector.recordModuleSet(
            CacheModule.${module_name?upper_case},
            "${entity_name?lower_case}",
            1, // 缓存项数量
            BusinessDataType.NORMAL
        );

        log.debug("${entity_name}缓存设置成功, id: {}", id);
    }

    /**
     * 清除相关缓存（双删策略）
     *
     * 基于repowiki缓存架构规范的双删策略，防止双写问题
     *
     * @param id 实体ID
     * @return CompletableFuture<Void>
     */
    @Async("cacheExecutor")
    public CompletableFuture<Void> remove${entity_name}Cache(Long id) {
        try {
            String cacheKey = id.toString();

            // 第一次删除缓存
            unifiedCacheService.delete(CacheModule.${module_name?upper_case}, "${entity_name?lower_case}", cacheKey);

            // 记录缓存删除指标
            metricsCollector.recordModuleDelete(
                CacheModule.${module_name?upper_case},
                "${entity_name?lower_case}"
            );

            // 延迟500ms后再次删除（防止双写问题）
            Thread.sleep(500);
            unifiedCacheService.delete(CacheModule.${module_name?upper_case}, "${entity_name?lower_case}", cacheKey);

            log.info("${entity_name}缓存清除完成(双删策略), id: {}", id);

        } catch (Exception e) {
            log.error("清除${entity_name}缓存失败, id: {}", id, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * 从数据库加载${entity_name}
     *
     * @param id 实体ID
     * @return ${entity_name}VO
     */
    private ${entity_name}VO load${entity_name}FromDatabase(Long id) {
        // TODO: 实现数据库加载逻辑
        // ${entity_name}Entity entity = ${entity_name?lower_case}Dao.selectById(id);
        // if (entity == null) {
        //     return null;
        // }
        // return SmartBeanUtil.copy(entity, ${entity_name}VO.class);

        log.warn("TODO: 实现load${entity_name}FromDatabase方法, id: {}", id);
        return null;
    }

    /**
     * 清除模块级缓存
     *
     * 用于批量操作或系统维护时清除整个模块的缓存
     */
    @Async("cacheExecutor")
    public CompletableFuture<Void> clearModuleCache() {
        try {
            unifiedCacheService.clearModule(CacheModule.${module_name?upper_case});
            log.info("${module_name}模块缓存清除完成");
        } catch (Exception e) {
            log.error("清除${module_name}模块缓存失败", e);
        }

        return CompletableFuture.completedFuture(null);
    }
}
EOF

    log_info "创建Manager: $file_path"
}

# 创建消费模块Manager
create_consume_managers() {
    log_info "=== 创建消费模块Manager ==="

    BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/consume/manager"

    # AccountManager
    create_manager_template "consume" "Account" "AccountManager" "$BASE_DIR/AccountManager.java"

    # ConsumeManager
    create_manager_template "consume" "Consume" "ConsumeManager" "$BASE_DIR/ConsumeManager.java"

    # AdvancedReportManager
    create_manager_template "consume" "AdvancedReport" "AdvancedReportManager" "$BASE_DIR/AdvancedReportManager.java"

    # RechargeManager
    create_manager_template "consume" "Recharge" "RechargeManager" "$BASE_DIR/RechargeManager.java"

    # RefundManager
    create_manager_template "consume" "Refund" "RefundManager" "$BASE_DIR/RefundManager.java"

    log_success "消费模块Manager创建完成"
}

# 创建考勤模块Manager
create_attendance_managers() {
    log_info "=== 创建考勤模块Manager ==="

    BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/attendance/manager"

    # AttendanceManager
    create_manager_template "attendance" "Attendance" "AttendanceManager" "$BASE_DIR/AttendanceManager.java"

    # AttendanceRuleManager
    create_manager_template "attendance" "AttendanceRule" "AttendanceRuleManager" "$BASE_DIR/AttendanceRuleManager.java"

    # AttendanceScheduleManager
    create_manager_template "attendance" "AttendanceSchedule" "AttendanceScheduleManager" "$BASE_DIR/AttendanceScheduleManager.java"

    # AttendanceStatisticsManager
    create_manager_template "attendance" "AttendanceStatistics" "AttendanceStatisticsManager" "$BASE_DIR/AttendanceStatisticsManager.java"

    log_success "考勤模块Manager创建完成"
}

# 创建智能系统模块Manager
create_smart_managers() {
    log_info "=== 创建智能系统模块Manager ==="

    # 门禁模块Manager
    ACCESS_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/access/manager"

    create_manager_template "smart/access" "AccessArea" "AccessAreaManager" "$ACCESS_BASE_DIR/AccessAreaManager.java"
    create_manager_template "smart/access" "AccessRecord" "AccessRecordManager" "$ACCESS_BASE_DIR/AccessRecordManager.java"
    create_manager_template "smart/access" "AccessControl" "AccessControlManager" "$ACCESS_BASE_DIR/AccessControlManager.java"

    # 视频模块Manager
    VIDEO_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/video/manager"

    create_manager_template "smart/video" "VideoDevice" "VideoDeviceManager" "$VIDEO_BASE_DIR/VideoDeviceManager.java"
    create_manager_template "smart/video" "VideoSurveillance" "VideoSurveillanceManager" "$VIDEO_BASE_DIR/VideoSurveillanceManager.java"
    create_manager_template "smart/video" "VideoPreview" "VideoPreviewManager" "$VIDEO_BASE_DIR/VideoPreviewManager.java"
    create_manager_template "smart/video" "VideoPlayback" "VideoPlaybackManager" "$VIDEO_BASE_DIR/VideoPlaybackManager.java"

    # 监控模块Manager
    MONITOR_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/monitor/manager"

    create_manager_template "smart/monitor" "AccessMonitor" "AccessMonitorManager" "$MONITOR_BASE_DIR/AccessMonitorManager.java"
    create_manager_template "smart/monitor" "VideoMonitor" "VideoMonitorManager" "$MONITOR_BASE_DIR/VideoMonitorManager.java"

    log_success "智能系统模块Manager创建完成"
}

# 创建OA模块Manager
create_oa_managers() {
    log_info "=== 创建OA模块Manager ==="

    # 文档模块Manager
    DOCUMENT_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/document/manager"
    create_manager_template "oa/document" "Document" "DocumentManager" "$DOCUMENT_BASE_DIR/DocumentManager.java"

    # 工作流模块Manager
    WORKFLOW_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/oa/workflow/manager"
    create_manager_template "oa/workflow" "WorkflowEngine" "WorkflowEngineManager" "$WORKFLOW_BASE_DIR/WorkflowEngineManager.java"

    log_success "OA模块Manager创建完成"
}

# 创建HR和系统模块Manager
create_hr_and_system_managers() {
    log_info "=== 创建HR和系统模块Manager ==="

    # HR模块Manager
    HR_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/hr/manager"
    create_manager_template "hr" "Employee" "EmployeeManager" "$HR_BASE_DIR/EmployeeManager.java"

    # 系统模块Manager
    SYSTEM_BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/system/device/manager"
    create_manager_template "system/device" "UnifiedDevice" "UnifiedDeviceManager" "$SYSTEM_BASE_DIR/UnifiedDeviceManager.java"

    log_success "HR和系统模块Manager创建完成"
}

# 分析现有架构完整性
analyze_architecture() {
    log_info "=== 分析四层架构完整性 ==="

    BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module"

    echo "模块架构分析报告:"
    echo "=================="

    for module_dir in "$BASE_DIR"/*; do
        if [ -d "$module_dir" ]; then
            module_name=$(basename "$module_dir")

            controller_count=$(find "$module_dir" -name "*Controller.java" -type f | wc -l)
            service_count=$(find "$module_dir" -name "*Service*.java" -type f | wc -l)
            manager_count=$(find "$module_dir" -name "*Manager.java" -type f | wc -l)
            dao_count=$(find "$module_dir" -name "*Dao.java" -type f | wc -l)
            repository_count=$(find "$module_dir" -name "*Repository.java" -type f | wc -l)

            data_access_count=$((dao_count + repository_count))

            echo "$module_name:"
            echo "  Controller: $controller_count"
            echo "  Service: $service_count"
            echo "  Manager: $manager_count"
            echo "  DAO/Repository: $data_access_count"

            # 架构完整性评估
            if [ $controller_count -gt 0 ] && [ $service_count -gt 0 ] && [ $manager_count -gt 0 ] && [ $data_access_count -gt 0 ]; then
                echo "  架构完整性: ✅ 完整"
            else
                echo "  架构完整性: ⚠️ 不完整"
            fi
            echo
        fi
    done

    log_success "架构完整性分析完成"
}

# 更新Service层以使用Manager
update_service_layer() {
    log_info "=== 分析Service层Manager使用情况 ==="

    BASE_DIR="smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module"

    service_files=$(find "$BASE_DIR" -name "*Service*.java" -type f)

    total_services=0
    services_with_manager=0
    services_need_update=0

    for service_file in $service_files; do
        total_services=$((total_services + 1))

        if grep -q "Manager" "$service_file"; then
            services_with_manager=$((services_with_manager + 1))
        else
            services_need_update=$((services_need_update + 1))
            echo "需要更新: $(basename "$service_file")"
        fi
    done

    echo "Service层分析报告:"
    echo "=================="
    echo "总Service数量: $total_services"
    echo "已使用Manager的Service: $services_with_manager"
    echo "需要更新的Service: $services_need_update"

    if [ $services_need_update -gt 0 ]; then
        log_warning "发现 $services_need_update 个Service需要更新以使用Manager层"
        log_info "请手动更新这些Service，添加Manager依赖并调用Manager层方法"
    fi
}

# 生成架构修复报告
generate_architecture_report() {
    local report_file="repowiki_architecture_fix_report_$(date +%Y%m%d_%H%M%S).md"

    cat > "$report_file" << EOF
# repowiki架构完整性修复报告

**修复时间**: $(date)
**脚本版本**: v1.0
**目标**: 四层架构完整性修复 (Controller→Service→Manager→DAO)

## 修复内容

### 1. Manager层补全
- **消费模块**: AccountManager, ConsumeManager, AdvancedReportManager, RechargeManager, RefundManager
- **考勤模块**: AttendanceManager, AttendanceRuleManager, AttendanceScheduleManager, AttendanceStatisticsManager
- **智能系统模块**:
  - 门禁: AccessAreaManager, AccessRecordManager, AccessControlManager
  - 视频: VideoDeviceManager, VideoSurveillanceManager, VideoPreviewManager, VideoPlaybackManager
  - 监控: AccessMonitorManager, VideoMonitorManager
- **OA模块**: DocumentManager, WorkflowEngineManager
- **HR模块**: EmployeeManager
- **系统模块**: UnifiedDeviceManager

### 2. 架构完整性分析
- 已分析所有模块的四层架构完整性
- 识别需要更新的Service文件
- 提供了Manager层标准模板

## 下一步行动

### 立即执行
1. 检查生成的Manager文件，根据实际需求调整TODO项
2. 更新对应的Service文件，添加Manager依赖注入
3. 修改Service方法，调用Manager层方法
4. 确保每个Service都正确使用Manager层

### 手动更新示例
\`\`\`java
@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private ConsumeDao consumeDao;

    // 添加Manager依赖
    @Resource
    private AccountManager accountManager;

    @Resource
    private ConsumeManager consumeManager;

    public AccountVO getAccount(Long userId) {
        // 使用Manager层方法
        return accountManager.getAccount(userId);
    }

    public void recordConsume(ConsumeRecord record) {
        // 业务逻辑处理
        consumeDao.insert(record);

        // 清除相关缓存
        accountManager.removeAccountCache(record.getUserId());
    }
}
\`\`\`

## repowiki规范遵循

✅ 四层架构设计: Controller→Service→Manager→DAO
✅ Manager层职责: 复杂业务逻辑、缓存管理、第三方集成
✅ 缓存架构: 统一使用UnifiedCacheService
✅ 异步处理: 使用@Async进行缓存清理
✅ 双删策略: 防止缓存双写问题

---
**报告生成时间**: $(date)
**基于**: repowiki架构设计规范 v1.0
EOF

    log_success "架构修复报告已生成: $report_file"
}

# 主函数
main() {
    echo "========================================"
    echo "  repowiki架构修复脚本 - 第二阶段"
    echo "  版本: v1.0"
    echo "  目标: 四层架构完整性修复"
    echo "========================================"
    echo

    # 执行修复步骤
    check_directory
    create_manager_directories
    create_consume_managers
    create_attendance_managers
    create_smart_managers
    create_oa_managers
    create_hr_and_system_managers

    echo
    analyze_architecture
    update_service_layer

    generate_architecture_report

    echo
    echo "========================================"
    echo "  架构完整性修复完成！"
    echo "  下一步: 运行 script_03_unify_cache_architecture.sh"
    echo "========================================"
}

# 执行主函数
main "$@"