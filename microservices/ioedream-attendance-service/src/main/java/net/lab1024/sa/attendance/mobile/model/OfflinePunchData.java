package net.lab1024.sa.attendance.mobile.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 离线打卡数据
 *
 * @author IOE-DREAM Team
 * @since 2025-12-04
 */
@Data
@Schema(description = "离线打卡数据")
public class OfflinePunchData {

    @Schema(description = "打卡类型")
    private String punchType;

    @Schema(description = "打卡时间")
    private LocalDateTime punchTime;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "照片URL")
    private String photoUrl;
}
