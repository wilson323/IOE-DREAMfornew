package net.lab1024.sa.attendance.realtime.event;

/**
 * 事件处理器状态枚举
 * <p>
 * 定义事件处理器的运行状态
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
public enum ProcessorStatus {
    /**
     * 已停止
     */
    STOPPED,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 停止中
     */
    STOPPING,

    /**
     * 错误状态
     */
    ERROR
}
