package net.lab1024.sa.consume.client.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 余额检查请求DTO
 * <p>
 * 用于检查账户余额是否充足，支持单用户和批量检查
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Schema(description = "余额检查请求")
public class BalanceCheckRequest {

    @Schema(description = "用户ID（单用户检查）", example = "10001")
    private Long userId;

    @Schema(description = "用户ID列表（批量检查）", example = "[10001, 10002, 10003]")
    private List<Long> userIds;

    @NotNull(message = "检查金额不能为空")
    @Schema(description = "需要检查的金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "业务类型", example = "CONSUME")
    private String businessType;

    @Schema(description = "是否检查冻结余额", example = "false")
    private Boolean checkFrozen = false;

    /**
     * 构建单用户检查请求
     */
    public static BalanceCheckRequest forSingleUser(Long userId, BigDecimal amount) {
        BalanceCheckRequest request = new BalanceCheckRequest();
        request.setUserId(userId);
        request.setAmount(amount);
        return request;
    }

    /**
     * 构建批量用户检查请求
     */
    public static BalanceCheckRequest forBatchUsers(List<Long> userIds, BigDecimal amount) {
        BalanceCheckRequest request = new BalanceCheckRequest();
        request.setUserIds(userIds);
        request.setAmount(amount);
        return request;
    }
}
