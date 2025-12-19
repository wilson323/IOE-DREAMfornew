package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 离线同步结果
 * <p>
 * 封装离线数据同步的结果信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfflineSyncResult {

    /**
     * 离线记录ID
     */
    private String offlineRecordId;

    /**
     * 同步状态
     */
    private String syncStatus;

    /**
     * 同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 错误信息
     */
    private String errorMessage;
}
