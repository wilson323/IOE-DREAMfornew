package net.lab1024.sa.consume.domain.vo;

import java.util.List;

import lombok.Data;
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;

/**
 * 离线同步结果
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@Data
public class ConsumeSyncResultVO {

    private Boolean success;

    private Integer syncedCount;

    private Integer failedCount;

    private List<ConsumeOfflineSyncForm.OfflineTransaction> failedTransactions;

    private String message;
}
