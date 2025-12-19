package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班次容量冲突
 * <p>
 * 描述排班中的班次容量超限冲突情况
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
public class CapacityConflict {

    /**
     * 冲突ID
     */
    private String conflictId;

    /**
     * 冲突类型
     */
    private String conflictType;

    /**
     * 冲突描述
     */
    private String conflictDescription;

    /**
     * 班次ID
     */
    private Long shiftId;

    /**
     * 班次日期
     */
    private java.time.LocalDate shiftDate;

    /**
     * 实际排班人数
     */
    private Integer actualEmployeeCount;

    /**
     * 最大允许人数
     */
    private Integer maxCapacity;

    /**
     * 最少需要人数
     */
    private Integer minRequiredCount;

    /**
     * 超员数量
     */
    private Integer overCapacityCount;

    /**
     * 缺员数量
     */
    private Integer underCapacityCount;

    /**
     * 冲突严重程度（1-5，5最严重）
     */
    private Integer severity;

    /**
     * 冲突原因
     */
    private String conflictReason;

    /**
     * 是否可自动解决
     */
    private Boolean autoResolvable;

    /**
     * 建议解决方案
     */
    private String suggestedResolution;

    /**
     * 冲突检测时间
     */
    private LocalDateTime detectionTime;
}
