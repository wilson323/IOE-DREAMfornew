package net.lab1024.sa.admin.module.device.domain.enums;

import lombok.Getter;

/**
 * 维护计划状态枚举
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Getter
public enum MaintenancePlanStatusEnum {

    PENDING("pending", "待处理", "计划已创建，等待开始执行"),
    IN_PROGRESS("in-progress", "进行中", "维护工作正在进行"),
    DONE("done", "已完成", "维护工作已完成"),
    CANCELLED("cancelled", "已取消", "维护计划已取消"),
    ON_HOLD("on-hold", "暂停", "维护工作暂停，等待进一步指示");

    private final String code;
    private final String description;
    private final String remark;

    MaintenancePlanStatusEnum(String code, String description, String remark) {
        this.code = code;
        this.description = description;
        this.remark = remark;
    }

    /**
     * 根据代码获取状态枚举
     */
    public static MaintenancePlanStatusEnum getByCode(String code) {
        if (code == null) {
            return null;
        }

        for (MaintenancePlanStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 检查是否可以转换为指定状态
     */
    public boolean canTransitionTo(MaintenancePlanStatusEnum targetStatus) {
        if (targetStatus == null) {
            return false;
        }

        switch (this) {
            case PENDING:
                return targetStatus == IN_PROGRESS || targetStatus == CANCELLED || targetStatus == ON_HOLD;
            case IN_PROGRESS:
                return targetStatus == DONE || targetStatus == ON_HOLD || targetStatus == CANCELLED;
            case ON_HOLD:
                return targetStatus == IN_PROGRESS || targetStatus == CANCELLED;
            case DONE:
            case CANCELLED:
                return false; // 已完成或已取消的状态不能再变更
            default:
                return false;
        }
    }
}