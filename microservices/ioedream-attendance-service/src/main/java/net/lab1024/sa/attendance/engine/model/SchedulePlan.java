package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排班计划模型
 * <p>
 * 排班计划的完整数据结构
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
public class SchedulePlan {

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 计划描述
     */
    private String description;

    /**
     * 计划状态
     */
    private String status;

    /**
     * 排班开始日期
     */
    private String startDate;

    /**
     * 排班结束日期
     */
    private String endDate;

    /**
     * 目标员工数量
     */
    private Integer targetEmployeeCount;

    /**
     * 实际员工数量
     */
    private Integer actualEmployeeCount;

    /**
     * 总班次数
     */
    private Integer totalShiftCount;

    /**
     * 已分配班次数
     */
    private Integer assignedShiftCount;

    /**
     * 完成率
     */
    private Double completionRate;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 审批状态
     */
    private String approvalStatus;

    /**
     * 审批人
     */
    private Long approvedBy;

    /**
     * 审批时间
     */
    private LocalDateTime approvedTime;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 排班记录列表
     */
    private List<ScheduleRecord> scheduleRecords;

    /**
     * 统计信息
     */
    private Map<String, Object> statistics;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;
}