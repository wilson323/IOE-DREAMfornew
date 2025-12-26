package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁权限导入错误VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "门禁权限导入错误记录")
public class AccessPermissionImportErrorVO {

    /**
     * 错误ID
     */
    @Schema(description = "错误ID", example = "1")
    private Long errorId;

    /**
     * 批次ID
     */
    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    /**
     * 行号
     */
    @Schema(description = "行号（Excel行号，从2开始，第1行是表头）", example = "2")
    private Integer rowNumber;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "100")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "办公区域A")
    private String areaName;

    /**
     * 开始时间
     */
    @Schema(description = "生效开始时间", example = "2025-01-01 00:00:00")
    private String effectiveStartTime;

    /**
     * 结束时间
     */
    @Schema(description = "生效结束时间", example = "2025-12-31 23:59:59")
    private String effectiveEndTime;

    /**
     * 错误字段
     */
    @Schema(description = "错误字段（userId/areaId/effectiveStartTime等）",
             example = "userId")
    private String errorField;

    /**
     * 错误消息
     */
    @Schema(description = "错误消息", example = "用户ID不存在")
    private String errorMessage;

    /**
     * 错误级别
     */
    @Schema(description = "错误级别（ERROR-错误 WARN-警告 INFO-信息）",
             example = "ERROR")
    private String errorLevel;

    /**
     * 原始数据（JSON格式）
     */
    @Schema(description = "原始数据（JSON格式）")
    private String rawData;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-12-26T10:30:00")
    private String createTime;
}
