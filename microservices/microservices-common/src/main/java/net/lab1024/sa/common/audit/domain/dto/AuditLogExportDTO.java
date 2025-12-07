package net.lab1024.sa.common.audit.domain.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 审计日志导出DTO
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Accessors(chain = true)
public class AuditLogExportDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 操作类型
     */
    private Integer operationType;

    /**
     * 结果状态
     */
    private Integer resultStatus;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 导出格式：EXCEL, CSV, PDF
     */
    private String exportFormat = "EXCEL";
}

