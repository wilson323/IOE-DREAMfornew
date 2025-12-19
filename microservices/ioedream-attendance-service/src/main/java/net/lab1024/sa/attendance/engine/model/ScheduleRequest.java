package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 排班请求模型
 * <p>
 * 智能排班引擎的输入参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {

    /**
     * 排班计划ID
     */
    private Long planId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 排班类型：1-固定排班 2-循环排班 3-弹性排班 4-智能排班
     */
    private Integer planType;

    /**
     * 排班算法：GENETIC-遗传算法 GREEDY-贪心算法 BACKTRACK-回溯算法
     */
    private String scheduleAlgorithm;

    /**
     * 排班开始日期
     */
    private LocalDate startDate;

    /**
     * 排班结束日期
     */
    private LocalDate endDate;

    /**
     * 目标用户列表
     */
    private List<Long> targetUsers;

    /**
     * 目标部门列表
     */
    private List<Long> targetDepartments;

    /**
     * 可用班次列表
     */
    private List<Long> availableShifts;

    /**
     * 排班约束条件
     */
    private Map<String, Object> constraints;

    /**
     * 优化目标：BALANCE-均衡 COST-成本 PREFERENCE-偏好
     */
    private String optimizationTarget;

    /**
     * 冲突解决方式：MANUAL-手动 AUTO-自动 PRIORITY-优先级
     */
    private String conflictResolution;

    /**
     * 是否自动审批
     */
    private Boolean autoApproval;

    /**
     * 排班配置
     */
    private Map<String, Object> scheduleConfig;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;
}