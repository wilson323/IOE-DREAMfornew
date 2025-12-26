package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端离线消费同步表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeOfflineSyncForm {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "离线交易列表不能为空")
    @Valid
    private List<OfflineTransaction> transactions;

    @Data
    public static class OfflineTransaction {

        @NotBlank(message = "交易流水号不能为空")
        private String transactionNo;

        @NotNull(message = "用户ID不能为空")
        private Long userId;

        @NotNull(message = "消费金额不能为空")
        @DecimalMin(value = "0.01", message = "消费金额必须大于0")
        private BigDecimal amount;

        @NotNull(message = "消费时间不能为空")
        private LocalDateTime consumeTime;

        @NotBlank(message = "消费模式不能为空")
        private String consumeMode;
    }
}
