package net.lab1024.sa.common.audit.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志查询DTO
 * 整合自ioedream-audit-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自audit-service）
 */
@Data
public class AuditLogQueryDTO {

    private Long userId;
    private String moduleName;
    private Integer operationType;
    private Integer resultStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String clientIp;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}

