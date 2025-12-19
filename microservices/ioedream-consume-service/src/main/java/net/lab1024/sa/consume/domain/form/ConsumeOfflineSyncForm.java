package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 离线消费同步表单
 * <p>
 * 用于设备离线消费数据批量同步到服务器
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "离线消费同步表单")
public class ConsumeOfflineSyncForm {

    /**
     * 设备编号
     */
    @NotBlank(message = "设备编号不能为空")
    @Schema(description = "设备编号", required = true)
    private String deviceNo;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private Long deviceId;

    /**
     * 同步时间
     */
    @NotNull(message = "同步时间不能为空")
    @Schema(description = "同步时间", required = true)
    private LocalDateTime syncTime;

    /**
     * 离线消费记录列表
     */
    @NotNull(message = "离线消费记录列表不能为空")
    @Schema(description = "离线消费记录列表", required = true)
    private List<OfflineConsumeRecord> records;

    /**
     * 离线消费记录
     */
    @Data
    @Schema(description = "离线消费记录")
    public static class OfflineConsumeRecord {

        /**
         * 交易流水号
         */
        @NotBlank(message = "交易流水号不能为空")
        @Schema(description = "交易流水号", required = true)
        private String transactionNo;

        /**
         * 用户ID
         */
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID", required = true)
        private Long userId;

        /**
         * 账户编号
         */
        @NotBlank(message = "账户编号不能为空")
        @Schema(description = "账户编号", required = true)
        private String accountNo;

        /**
         * 消费金额
         */
        @NotNull(message = "消费金额不能为空")
        @Schema(description = "消费金额", required = true)
        private BigDecimal amount;

        /**
         * 消费时间
         */
        @NotNull(message = "消费时间不能为空")
        @Schema(description = "消费时间", required = true)
        private LocalDateTime consumeTime;

        /**
         * 备注
         */
        @Schema(description = "备注")
        private String remark;
    }
}
