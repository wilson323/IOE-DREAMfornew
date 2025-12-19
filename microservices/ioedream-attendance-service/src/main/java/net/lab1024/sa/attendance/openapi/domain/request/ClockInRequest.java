package net.lab1024.sa.attendance.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 考勤打卡请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "考勤打卡请求")
public class ClockInRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    @NotNull(message = "打卡类型不能为空")
    @Pattern(regexp = "^(on|off|break_start|break_end)$", message = "打卡类型必须是on、off、break_start、break_end之一")
    @Schema(description = "打卡类型", example = "on", required = true,
            allowableValues = {"on", "off", "break_start", "break_end"})
    private String clockType;

    @Schema(description = "打卡时间", example = "2025-12-16T09:00:00")
    private LocalDateTime clockTime;

    @Schema(description = "设备ID", example = "ATTEND_001")
    private String deviceId;

    @Schema(description = "设备名称", example = "前台考勤机")
    private String deviceName;

    @Schema(description = "位置信息", example = "公司前台")
    private String location;

    @Schema(description = "经度", example = "116.397128")
    private Double longitude;

    @Schema(description = "纬度", example = "39.916527")
    private Double latitude;

    @Schema(description = "打卡方式", example = "face", allowableValues = {"face", "fingerprint", "card", "password", "location", "manual"})
    private String clockMethod = "face";

    @Schema(description = "人脸特征数据", example = "base64_encoded_face_data")
    private String faceData;

    @Schema(description = "指纹特征数据", example = "base64_encoded_fingerprint_data")
    private String fingerprintData;

    @Schema(description = "卡号", example = "1234567890")
    private String cardNumber;

    @Schema(description = "照片URL", example = "https://example.com/clock_photo.jpg")
    private String photoUrl;

    @Schema(description = "体温数据", example = "36.5")
    private Double temperature;

    @Schema(description = "是否佩戴口罩", example = "true")
    private Boolean wearingMask;

    @Schema(description = "备注", example = "正常打卡")
    private String remark;

    @Schema(description = "是否需要位置验证", example = "true")
    private Boolean requireLocationVerify = true;

    @Schema(description = "允许的打卡距离（米）", example = "100")
    private Integer allowedDistance = 100;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}