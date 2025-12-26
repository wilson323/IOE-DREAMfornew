package net.lab1024.sa.access.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备导入批次VO
 * <p>
 * 用于返回导入批次记录信息：
 * - 批次基础信息
 * - 导入统计数据
 * - 状态信息
 * - 时间信息
 * - 操作人信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备导入批次VO")
public class DeviceImportBatchVO {

    @Schema(description = "导入批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次名称", example = "2025年1月设备导入")
    private String batchName;

    @Schema(description = "导入文件名", example = "devices_20250130.xlsx")
    private String fileName;

    @Schema(description = "文件大小（字节）", example = "1048576")
    private Long fileSize;

    @Schema(description = "文件大小（可读格式）", example = "1.0 MB")
    private String fileSizeReadable;

    @Schema(description = "文件MD5值", example = "d41d8cd98f00b204e9800998ecf8427e")
    private String fileMd5;

    // ==================== 导入统计 ====================

    @Schema(description = "总记录数", example = "100")
    private Integer totalCount;

    @Schema(description = "成功数量", example = "95")
    private Integer successCount;

    @Schema(description = "失败数量", example = "3")
    private Integer failedCount;

    @Schema(description = "跳过数量（已存在）", example = "2")
    private Integer skippedCount;

    @Schema(description = "成功率（%）", example = "95.0")
    private Double successRate;

    // ==================== 导入状态 ====================

    @Schema(description = "导入状态: 0-处理中 1-成功 2-部分失败 3-全部失败", example = "1")
    private Integer importStatus;

    @Schema(description = "导入状态名称", example = "成功")
    private String importStatusName;

    @Schema(description = "状态消息", example = "导入完成")
    private String statusMessage;

    @Schema(description = "错误摘要（JSON格式）")
    private String errorSummary;

    // ==================== 时间信息 ====================

    @Schema(description = "开始时间", example = "2025-01-30T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-01-30T10:01:30")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "耗时（毫秒）", example = "5230")
    private Long durationMs;

    @Schema(description = "耗时（可读格式）", example = "5.23 秒")
    private String durationReadable;

    // ==================== 操作信息 ====================

    @Schema(description = "操作人类型: 1-管理员 2-普通用户", example = "1")
    private Integer operatorType;

    @Schema(description = "操作人类型名称", example = "管理员")
    private String operatorTypeName;

    @Schema(description = "操作人ID", example = "1")
    private Long operatorId;

    @Schema(description = "操作人姓名", example = "张三")
    private String operatorName;

    @Schema(description = "创建时间", example = "2025-01-30T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
