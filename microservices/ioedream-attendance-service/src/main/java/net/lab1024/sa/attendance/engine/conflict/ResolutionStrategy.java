package net.lab1024.sa.attendance.engine.conflict;

/**
 * 冲突解决策略
 * <p>
 * 定义各种冲突解决策略
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public enum ResolutionStrategy {

    /**
     * 时间调整策略 - 调整冲突排班的时间
     */
    TIME_ADJUSTMENT("时间调整", "调整冲突排班的时间以避免重叠"),

    /**
     * 人员替换策略 - 替换不合适的员工
     */
    EMPLOYEE_REPLACEMENT("人员替换", "替换不合适的员工以解决技能或时间冲突"),

    /**
     * 班次调整策略 - 调整班次安排
     */
    SHIFT_ADJUSTMENT("班次调整", "调整班次安排以解决容量或时间冲突"),

    /**
     * 记录删除策略 - 删除冲突的排班记录
     */
    RECORD_DELETION("记录删除", "删除冲突的排班记录"),

    /**
     * 分段处理策略 - 将长时段拆分为短时段
     */
    SEGMENTATION("分段处理", "将长时段拆分为短时段以解决时长冲突"),

    /**
     * 优先级策略 - 基于优先级解决冲突
     */
    PRIORITY_BASED("优先级策略", "基于优先级保留重要排班，调整次要排班"),

    /**
     * 最小影响策略 - 选择影响最小的解决方案
     */
    MINIMAL_IMPACT("最小影响", "选择对整体排班影响最小的解决方案"),

    /**
     * 自动重排策略 - 自动重新排班
     */
    AUTO_RESCHEDULING("自动重排", "自动重新排班以解决冲突"),

    /**
     * 人工确认策略 - 需要人工确认的解决方案
     */
    MANUAL_CONFIRMATION("人工确认", "需要人工确认的解决方案"),

    /**
     * 混合策略 - 结合多种策略
     */
    HYBRID_STRATEGY("混合策略", "结合多种策略的解决方案");

    private final String displayName;
    private final String description;

    ResolutionStrategy(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}