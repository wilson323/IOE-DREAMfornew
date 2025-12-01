package net.lab1024.sa.admin.module.consume.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账户状态枚举
 * 严格遵循repowiki规范：定义所有可能的账户状态，确保状态管理的一致性
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 */
@Getter
@AllArgsConstructor
public enum AccountStatusEnum {

    /**
     * 正常状态
     * 可以进行所有消费操作
     */
    NORMAL("NORMAL", "正常", "可以进行所有消费操作"),

    /**
     * 活跃状态
     * 可以进行所有消费操作
     */
    ACTIVE("ACTIVE", "活跃", "可以进行所有消费操作"),

    /**
     * 冻结状态
     * 不能进行消费操作，余额仍保留
     */
    FROZEN("FROZEN", "冻结", "不能进行消费操作，余额仍保留"),

    /**
     * 挂失状态
     * 不能进行消费操作，余额仍保留
     */
    SUSPENDED("SUSPENDED", "挂失", "不能进行消费操作，余额仍保留"),

    /**
     * 注销状态
     * 账户已注销，不能进行任何操作
     */
    CLOSED("CLOSED", "注销", "账户已注销，不能进行任何操作"),

    /**
     * 暂停状态
     * 临时暂停，不能进行消费操作
     */
    PAUSED("PAUSED", "暂停", "临时暂停，不能进行消费操作"),

    /**
     * 待激活状态
     * 账户已创建但未激活，不能进行消费操作
     */
    PENDING("PENDING", "待激活", "账户已创建但未激活，不能进行消费操作");

    private final String code;
    private final String name;
    private final String description;

    /**
     * 检查状态是否允许消费
     */
    public boolean isConsumeAllowed() {
        return this == NORMAL || this == ACTIVE;
    }

    /**
     * 检查账户是否有效（未注销）
     */
    public boolean isValid() {
        return this != CLOSED;
    }

    /**
     * 检查是否为临时状态
     */
    public boolean isTemporary() {
        return this == FROZEN || this == SUSPENDED || this == PAUSED;
    }

    /**
     * 根据代码获取枚举
     */
    public static AccountStatusEnum fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (AccountStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }

        return null;
    }

    /**
     * 检查代码是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * 获取所有允许消费的状态
     */
    public static AccountStatusEnum[] getConsumeAllowedStatuses() {
        return new AccountStatusEnum[]{NORMAL, ACTIVE};
    }

    /**
     * 获取所有有效状态（未注销）
     */
    public static AccountStatusEnum[] getValidStatuses() {
        AccountStatusEnum[] allStatuses = values();
        AccountStatusEnum[] validStatuses = new AccountStatusEnum[allStatuses.length - 1];

        int index = 0;
        for (AccountStatusEnum status : allStatuses) {
            if (status != CLOSED) {
                validStatuses[index++] = status;
            }
        }

        return validStatuses;
    }
}