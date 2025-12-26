package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 固件详情VO
 * <p>
 * 继承FirmwareVO，添加更多详情信息：
 * - 固件文件存储路径
 * - 统计信息
 * - 版本兼容性信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "固件详情VO")
public class FirmwareDetailVO extends FirmwareVO {

    @Schema(description = "固件文件存储路径", example = "/firmware/ac2000_v1.0.0.bin")
    private String firmwareFilePath;

    // ==================== 统计信息 ====================

    @Schema(description = "总固件数量", example = "100")
    private Integer totalFirmwareCount;

    @Schema(description = "测试中固件数量", example = "10")
    private Integer testingFirmwareCount;

    @Schema(description = "正式发布固件数量", example = "85")
    private Integer releasedFirmwareCount;

    @Schema(description = "已废弃固件数量", example = "5")
    private Integer deprecatedFirmwareCount;

    // ==================== 版本兼容性信息 ====================

    @Schema(description = "是否兼容当前版本", example = "true")
    private Boolean isCompatible;

    @Schema(description = "兼容性说明", example = "当前版本在可升级范围内")
    private String compatibilityNote;
}
