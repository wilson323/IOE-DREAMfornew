package net.lab1024.sa.attendance.report.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存清理结果（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCacheCleanupResult {
    private boolean success;
    private int cleanedItems;
    private String message;
    private String errorMessage;
    private String errorCode;
}

