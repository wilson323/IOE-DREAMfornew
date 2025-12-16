package net.lab1024.sa.consume.domain.vo;

import java.util.List;
import lombok.Data;

/**
 * 消费同步数据VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ConsumeSyncDataVO {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 同步时间戳
     */
    private Long syncTimestamp;

    /**
     * 交易数据列表
     */
    private List<SyncTransactionData> transactionList;

    /**
     * 用户数据列表
     */
    private List<SyncUserData> userList;

    /**
     * 同步交易数据
     */
    @Data
    public static class SyncTransactionData {
        private Long transactionId;
        private Long userId;
        private java.math.BigDecimal amount;
        private java.time.LocalDateTime consumeTime;
    }

    /**
     * 同步用户数据
     */
    @Data
    public static class SyncUserData {
        private Long userId;
        private String userName;
        private java.math.BigDecimal balance;
    }
}



