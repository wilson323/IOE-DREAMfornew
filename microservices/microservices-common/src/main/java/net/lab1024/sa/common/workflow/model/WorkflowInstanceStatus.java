package net.lab1024.sa.common.workflow.model;

/**
 * 工作流实例状态枚举
 * <p>
 * 定义工作流实例的各种状态
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
public enum WorkflowInstanceStatus {

    /**
     * 未找到
     */
    NOT_FOUND("未找到", 0),

    /**
     * 运行中
     */
    RUNNING("运行中", 1),

    /**
     * 已完成
     */
    COMPLETED("已完成", 2),

    /**
     * 已终止
     */
    TERMINATED("已终止", 3),

    /**
     * 已暂停
     */
    SUSPENDED("已暂停", 4),

    /**
     * 已取消
     */
    CANCELLED("已取消", 5),

    /**
     * 等待中
     */
    WAITING("等待中", 6);

    private final String name;
    private final int code;

    WorkflowInstanceStatus(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    /**
     * 根据代码获取状态
     */
    public static WorkflowInstanceStatus fromCode(int code) {
        for (WorkflowInstanceStatus status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return NOT_FOUND;
    }
}