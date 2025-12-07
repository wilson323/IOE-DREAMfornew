package net.lab1024.sa.common.workflow.constant;

/**
 * 业务类型枚举
 * <p>
 * 统一管理所有业务模块的业务类型标识
 * 严格遵循CLAUDE.md规范：枚举类使用enum定义
 * </p>
 * <p>
 * 说明：
 * - 业务类型用于标识不同的业务场景
 * - 用于工作流实例的分类和统计
 * - 新增业务类型时，需同步更新本枚举
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public enum BusinessTypeEnum {

    // ==================== 消费模块业务类型 ====================

    /**
     * 充值退款
     * <p>
     * 用于消费模块的充值退款业务
     * </p>
     */
    CONSUME_REFUND("充值退款", "消费模块"),

    /**
     * 报销
     * <p>
     * 用于消费模块的报销业务
     * </p>
     */
    CONSUME_REIMBURSEMENT("报销", "消费模块"),

    // ==================== 访客模块业务类型 ====================

    /**
     * 访客预约
     * <p>
     * 用于访客模块的访客预约业务
     * </p>
     */
    VISITOR_APPOINTMENT("访客预约", "访客模块"),

    // ==================== 门禁模块业务类型 ====================

    /**
     * 门禁权限申请
     * <p>
     * 用于门禁模块的权限申请业务
     * </p>
     */
    ACCESS_PERMISSION_APPLY("门禁权限申请", "门禁模块"),

    /**
     * 紧急权限申请
     * <p>
     * 用于门禁模块的紧急权限申请业务
     * </p>
     */
    ACCESS_EMERGENCY_PERMISSION("紧急权限申请", "门禁模块"),

    // ==================== 考勤模块业务类型 ====================

    /**
     * 请假
     * <p>
     * 用于考勤模块的请假业务
     * </p>
     */
    ATTENDANCE_LEAVE("请假", "考勤模块"),

    /**
     * 出差
     * <p>
     * 用于考勤模块的出差业务
     * </p>
     */
    ATTENDANCE_TRAVEL("出差", "考勤模块"),

    /**
     * 加班
     * <p>
     * 用于考勤模块的加班业务
     * </p>
     */
    ATTENDANCE_OVERTIME("加班", "考勤模块"),

    /**
     * 补签
     * <p>
     * 用于考勤模块的补签业务
     * </p>
     */
    ATTENDANCE_SUPPLEMENT("补签", "考勤模块"),

    /**
     * 调班
     * <p>
     * 用于考勤模块的调班业务
     * </p>
     */
    ATTENDANCE_SHIFT("调班", "考勤模块");

    /**
     * 业务类型名称
     */
    private final String name;

    /**
     * 所属模块
     */
    private final String module;

    /**
     * 构造函数
     *
     * @param name   业务类型名称
     * @param module 所属模块
     */
    BusinessTypeEnum(String name, String module) {
        this.name = name;
        this.module = module;
    }

    /**
     * 获取业务类型名称
     *
     * @return 业务类型名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取所属模块
     *
     * @return 所属模块
     */
    public String getModule() {
        return module;
    }

    /**
     * 根据名称获取枚举
     *
     * @param name 业务类型名称
     * @return 业务类型枚举，未找到返回null
     */
    public static BusinessTypeEnum fromName(String name) {
        for (BusinessTypeEnum type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据模块获取该模块下的所有业务类型
     *
     * @param module 模块名称
     * @return 业务类型枚举数组
     */
    public static BusinessTypeEnum[] getByModule(String module) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.module.equals(module))
                .toArray(BusinessTypeEnum[]::new);
    }
}

