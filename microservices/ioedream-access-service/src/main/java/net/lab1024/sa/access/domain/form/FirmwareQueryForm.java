package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 固件查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件查询表单")
public class FirmwareQueryForm {

    @Schema(description = "固件名称（模糊查询）", example = "AC-2000")
    private String firmwareName;

    @Schema(description = "固件版本号", example = "v1.0.0")
    private String firmwareVersion;

    @Schema(description = "设备类型：1-门禁 2-考勤 3-消费 4-视频 5-访客", example = "1")
    private Integer deviceType;

    @Schema(description = "设备型号", example = "AC-2000")
    private String deviceModel;

    @Schema(description = "设备品牌", example = "Hikvision")
    private String brand;

    @Schema(description = "固件状态：1-测试中 2-正式发布 3-已废弃", example = "2")
    private Integer firmwareStatus;

    @Schema(description = "是否启用：0-禁用 1-启用", example = "1")
    private Integer isEnabled;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
}
