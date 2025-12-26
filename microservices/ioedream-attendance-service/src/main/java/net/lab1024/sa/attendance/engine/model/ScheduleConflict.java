package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 排班冲突模型
 * <p>
 * 排班冲突的数据结构
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
public class ScheduleConflict {

    /**
     * 冲突ID
     */
    private String conflictId;

    /**
     * 冲突类型：TIME_CONFLICT-时间冲突 SKILL_CONFLICT-技能冲突
     */
    private String conflictType;

    /**
     * 冲突描述
     */
    private String description;

    /**
     * 冲突严重程度：LOW-低 MEDIUM-中 HIGH-高 CRITICAL-严重
     */
    private String severity;

    /**
     * 涉及的用户列表
     */
    private List<Long> affectedUsers;

    /**
     * 冲突日期
     */
    private String conflictDate;

    /**
     * 冲突时间段
     */
    private List<String> timeSlots;

    /**
     * 建议解决方案
     */
    private List<String> suggestedSolutions;

    /**
     * 扩展属性
     */
    private Object extendedAttributes;
}
