package net.lab1024.sa.common.transaction.saga;

/**
 * SAGA事务状态枚举
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public enum SagaTransactionStatus {

    /**
     * 已创建
     */
    CREATED("已创建"),

    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 成功
     */
    SUCCESS("成功"),

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
     * 已取消
     */
    CANCELLED("已取消");

    private final String description;

    SagaTransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}