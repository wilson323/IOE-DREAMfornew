package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 限制变更历史VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "限制变更历史")
public class LimitChangeHistory {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "人员ID")
    private Long personId;

    @Schema(description = "变更类型：1-日限额，2-单次限额，3-月限额")
    private Integer changeType;

    @Schema(description = "变更前值")
    private BigDecimal oldValue;

    @Schema(description = "变更后值")
    private BigDecimal newValue;

    @Schema(description = "操作人员ID")
    private Long operatorId;

    @Schema(description = "变更原因")
    private String changeReason;

    @Schema(description = "变更时间")
    private LocalDateTime changeTime;

    @Schema(description = "变更类型描述")
    private String changeTypeDesc;

    @Schema(description = "操作人员姓名")
    private String operatorName;

    /**
     * 获取变更类型描述
     */

    /**
     * 检查是否为增加限额
     */
    public boolean isLimitIncrease() {
        if (oldValue == null || newValue == null) {
            return false;
        }
        return newValue.compareTo(oldValue) > 0;
    }

    /**
     * 检查是否为减少限额
     */
    public boolean isLimitDecrease() {
        if (oldValue == null || newValue == null) {
            return false;
        }
        return newValue.compareTo(oldValue) < 0;
    }

    /**
     * 获取变更百分比
     */

    /**
     * 创建默认历史记录
     */
    public static LimitChangeHistory createDefault(@NotNull Long personId) {
        return LimitChangeHistory.builder()
                .personId(personId)
                .changeTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建限额变更记录
     */
    public static LimitChangeHistory createChangeRecord(
            Long userId,
            Long personId,
            Integer changeType,
            BigDecimal oldValue,
            BigDecimal newValue,
            Long operatorId,
            String changeReason) {

        return LimitChangeHistory.builder()
                .userId(userId)
                .personId(personId)
                .changeType(changeType)
                .oldValue(oldValue)
                .newValue(newValue)
                .operatorId(operatorId)
                .changeReason(changeReason)
                .changeTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)





















}
