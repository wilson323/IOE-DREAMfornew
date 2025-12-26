package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 设备导入结果VO
 * <p>
 * 用于返回导入操作的执行结果：
 * - 批次信息
 * - 导入统计
 * - 成功列表
 * - 失败列表
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备导入结果VO")
public class DeviceImportResultVO {

    @Schema(description = "导入批次ID", example = "1")
    private Long batchId;

    @Schema(description = "批次名称", example = "2025年1月设备导入")
    private String batchName;

    @Schema(description = "导入文件名", example = "devices_20250130.xlsx")
    private String fileName;

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

    // ==================== 详细信息 ====================

    @Schema(description = "成功导入的设备列表")
    private List<DeviceImportSuccessItemVO> successList;

    @Schema(description = "导入失败的错误列表")
    private List<DeviceImportErrorItemVO> errorList;

    @Schema(description = "耗时（秒）", example = "5.23")
    private Double durationSeconds;

    /**
     * 成功导入项VO
     */
    @Data
    @Schema(description = "成功导入项VO")
    public static class DeviceImportSuccessItemVO {

        @Schema(description = "行号", example = "3")
        private Integer rowNumber;

        @Schema(description = "设备ID", example = "1001")
        private Long deviceId;

        @Schema(description = "设备编码", example = "DEV001")
        private String deviceCode;

        @Schema(description = "设备名称", example = "1号门禁控制器")
        private String deviceName;
    }

    /**
     * 导入失败项VO
     */
    @Data
    @Schema(description = "导入失败项VO")
    public static class DeviceImportErrorItemVO {

        @Schema(description = "行号", example = "5")
        private Integer rowNumber;

        @Schema(description = "错误码", example = "DEVICE_CODE_REQUIRED")
        private String errorCode;

        @Schema(description = "错误消息", example = "设备编码不能为空")
        private String errorMessage;

        @Schema(description = "错误字段", example = "deviceCode")
        private String errorField;

        @Schema(description = "原始数据", example = "{\"deviceCode\":\"\",\"deviceName\":\"测试设备\"}")
        private String rawData;
    }
}
