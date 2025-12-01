package net.lab1024.sa.audit.domain.form;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.domain.PageParam;

/**
 * 审计日志查询表单
 * <p>
 * 用于审计日志的查询条件
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的查询条件字段
 * - 支持多种查询方式组合
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@Accessors(chain = true)
@lombok.EqualsAndHashCode(callSuper = false)
public class AuditLogQueryForm extends PageParam {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作类型：1-登录 2-登出 3-增 4-删 5-改 6-查 7-导出 8-导入 9-配置 10-其他
     */
    private Integer operationType;

    /**
     * 结果状态：1-成功 2-失败 3-异常
     */
    private Integer resultStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 关键词搜索（用户名、模块名、功能名、描述）
     */
    private String keyword;

    /**
     * 风险等级：1-低 2-中 3-高
     */
    private Integer riskLevel;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 审计标签
     */
    private String auditTags;

    /**
     * 最小执行时长（毫秒）
     */
    private Long minExecutionTime;

    /**
     * 最大执行时长（毫秒）
     */
    private Long maxExecutionTime;

    /**
     * 是否包含数据变更
     */
    private Boolean hasDataChange;

    /**
     * 排序字段：operation_time, create_time, execution_time
     */
    private String sortField = "operation_time";

    /**
     * 排序方向：asc, desc
     */
    private String sortDirection = "desc";

    /**
     * 设置默认查询时间范围（最近7天）
     */
    public void setDefaultTimeRange() {
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now();
            startTime = endTime.minusDays(7);
        }
    }

    /**
     * 设置今日查询时间范围
     */
    public void setTodayRange() {
        LocalDateTime now = LocalDateTime.now();
        startTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 设置本周查询时间范围
     */
    public void setThisWeekRange() {
        LocalDateTime now = LocalDateTime.now();
        // 获取本周第一天（周一）
        LocalDateTime monday = now.minusDays(now.getDayOfWeek().getValue() - 1);
        startTime = monday.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 设置本月查询时间范围
     */
    public void setThisMonthRange() {
        LocalDateTime now = LocalDateTime.now();
        startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        endTime = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 验证查询参数
     */
    public boolean isValid() {
        // 验证时间范围
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return false;
        }

        // 验证执行时长范围
        if (minExecutionTime != null && maxExecutionTime != null
                && minExecutionTime > maxExecutionTime) {
            return false;
        }

        // 验证操作类型
        if (operationType != null && (operationType < 1 || operationType > 10)) {
            return false;
        }

        // 验证结果状态
        if (resultStatus != null && (resultStatus < 1 || resultStatus > 3)) {
            return false;
        }

        // 验证风险等级
        if (riskLevel != null && (riskLevel < 1 || riskLevel > 3)) {
            return false;
        }

        return true;
    }

    /**
     * 获取查询条件摘要
     */
    public String getQuerySummary() {
        StringBuilder summary = new StringBuilder();

        if (userId != null) {
            summary.append("用户ID: ").append(userId).append(", ");
        }

        if (moduleName != null && !moduleName.trim().isEmpty()) {
            summary.append("模块: ").append(moduleName).append(", ");
        }

        if (operationType != null) {
            summary.append("操作类型: ").append(operationType).append(", ");
        }

        if (resultStatus != null) {
            summary.append("结果状态: ").append(resultStatus).append(", ");
        }

        if (startTime != null || endTime != null) {
            summary.append("时间范围: ").append(startTime).append(" ~ ").append(endTime).append(", ");
        }

        if (clientIp != null && !clientIp.trim().isEmpty()) {
            summary.append("客户端IP: ").append(clientIp).append(", ");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            summary.append("关键词: ").append(keyword).append(", ");
        }

        if (riskLevel != null) {
            summary.append("风险等级: ").append(riskLevel).append(", ");
        }

        // 移除最后的逗号和空格
        if (summary.length() > 0) {
            summary.setLength(summary.length() - 2);
        }

        return summary.toString();
    }
}
