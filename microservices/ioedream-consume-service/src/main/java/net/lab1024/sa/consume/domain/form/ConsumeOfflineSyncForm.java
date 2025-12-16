package net.lab1024.sa.consume.domain.form;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端离线同步表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeOfflineSyncForm {

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 离线交易数据列表
     */
    private List<OfflineTransactionData> transactionList;

    /**
     * 离线交易数据
     */
    @Data
    public static class OfflineTransactionData {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 消费金额
         */
        private java.math.BigDecimal amount;

        /**
         * 消费时间
         */
        private java.time.LocalDateTime consumeTime;

        /**
         * 交易类型
         */
        private String transactionType;
    }
}



