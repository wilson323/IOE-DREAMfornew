package net.lab1024.sa.attendance.domain.vo.smartSchedule;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 智能排班计划VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class SmartSchedulePlanVO {

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
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 计划状态 (0-草稿 1-待优化 2-优化中 3-已完成 4-已确认 5-已取消 6-优化失败)
     */
    private Integer status;

    /**
     * 优化目标
     */
    private Integer optimizationGoal;

    /**
     * 算法类型
     */
    private Integer algorithmType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
