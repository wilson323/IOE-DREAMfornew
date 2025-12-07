package net.lab1024.sa.common.workflow.constant;

/**
 * 工作流流程定义常量
 * <p>
 * 统一管理所有业务模块的流程定义ID
 * 严格遵循CLAUDE.md规范：常量类使用final class + private构造函数
 * </p>
 * <p>
 * 说明：
 * - 流程定义ID需要在OA服务中预先部署流程后获取
 * - 本常量类用于统一管理和引用流程定义ID
 * - 新增流程定义时，需同步更新本常量类
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public final class WorkflowDefinitionConstants {

    /**
     * 禁止实例化
     */
    private WorkflowDefinitionConstants() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    // ==================== 消费模块流程定义 ====================

    /**
     * 充值退款审批流程
     * <p>
     * 用于消费模块的充值退款审批
     * 流程：申请人 → 部门经理 → 财务审批
     * </p>
     */
    public static final Long CONSUME_REFUND = 1L;

    /**
     * 报销审批流程
     * <p>
     * 用于消费模块的报销审批
     * 流程：申请人 → 部门经理 → 财务审批
     * </p>
     */
    public static final Long CONSUME_REIMBURSEMENT = 2L;

    // ==================== 访客模块流程定义 ====================

    /**
     * 访客预约审批流程
     * <p>
     * 用于访客模块的访客预约审批
     * 流程：申请人 → 被访人审批 → 安保审批
     * </p>
     */
    public static final Long VISITOR_APPOINTMENT = 10L;

    // ==================== 门禁模块流程定义 ====================

    /**
     * 门禁权限申请审批流程
     * <p>
     * 用于门禁模块的权限申请审批
     * 流程：申请人 → 部门经理 → 安保部门审批
     * </p>
     */
    public static final Long ACCESS_PERMISSION_APPLY = 20L;

    /**
     * 紧急权限申请审批流程
     * <p>
     * 用于门禁模块的紧急权限申请
     * 流程：申请人 → 部门经理快速审批
     * </p>
     */
    public static final Long ACCESS_EMERGENCY_PERMISSION = 21L;

    // ==================== 考勤模块流程定义 ====================

    /**
     * 请假审批流程
     * <p>
     * 用于考勤模块的请假审批
     * 流程：申请人 → 部门经理 → HR审批（根据请假天数决定审批层级）
     * </p>
     */
    public static final Long ATTENDANCE_LEAVE = 30L;

    /**
     * 出差审批流程
     * <p>
     * 用于考勤模块的出差审批
     * 流程：申请人 → 部门经理 → 财务审批
     * </p>
     */
    public static final Long ATTENDANCE_TRAVEL = 31L;

    /**
     * 加班审批流程
     * <p>
     * 用于考勤模块的加班审批
     * 流程：申请人 → 部门经理审批
     * </p>
     */
    public static final Long ATTENDANCE_OVERTIME = 32L;

    /**
     * 补签审批流程
     * <p>
     * 用于考勤模块的补签审批
     * 流程：申请人 → 部门经理审批
     * </p>
     */
    public static final Long ATTENDANCE_SUPPLEMENT = 33L;

    /**
     * 调班审批流程
     * <p>
     * 用于考勤模块的调班审批
     * 流程：申请人 → 部门经理审批
     * </p>
     */
    public static final Long ATTENDANCE_SHIFT = 34L;
}

