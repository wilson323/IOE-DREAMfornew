package net.lab1024.sa.common.transaction.saga;

/**
 * SAGA步骤状态枚举
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public enum SagaStepStatus {

    /**
     * 待执行
     */
    PENDING("待执行"),

    /**
     * 执行中
     */
    EXECUTING("执行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 失败
     */
    FAILED("失败"),

    /**
     * 补偿中
     */
    COMPENSATING("补偿中"),

    /**
     * 已补偿
     */
    COMPENSATED("已补偿"),

    /**
     * 补偿失败
     */
    COMPENSATION_FAILED("补偿失败");

    private final String description;

    SagaStepStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}