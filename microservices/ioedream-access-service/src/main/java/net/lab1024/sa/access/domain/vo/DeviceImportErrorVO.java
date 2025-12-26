package net.lab1024.sa.access.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备导入错误明细VO
 * <p>
 * 用于返回导入错误的详细信息：
 * - 错误行号
 * - 错误码和错误消息
 * - 原始数据
 * - 验证错误详情
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备导入错误明细VO")
public class DeviceImportErrorVO {

    @Schema(description = "错误ID", example = "1")
    private Long errorId;

    @Schema(description = "批次ID", example = "1")
    private Long batchId;

    @Schema(description = "行号（Excel行号）", example = "5")
    private Integer rowNumber;

    // ==================== 原始数据 ====================

    @Schema(description = "原始数据（JSON格式）", example = "{\"deviceCode\":\"\",\"deviceName\":\"测试设备\"}")
    private String rawData;

    // ==================== 错误信息 ====================

    @Schema(description = "错误码", example = "DEVICE_CODE_REQUIRED")
    private String errorCode;

    @Schema(description = "错误消息", example = "设备编码不能为空")
    private String errorMessage;

    @Schema(description = "错误字段", example = "deviceCode")
    private String errorField;

    // ==================== 数据验证结果 ====================

    @Schema(description = "验证错误详情（JSON格式）")
    private String validationErrors;

    @Schema(description = "创建时间", example = "2025-01-30T10:00:05")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
