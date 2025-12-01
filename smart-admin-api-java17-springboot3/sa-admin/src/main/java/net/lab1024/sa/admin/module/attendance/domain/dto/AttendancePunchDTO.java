package net.lab1024.sa.admin.module.attendance.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

/**
 * 考勤打卡 DTO (Data Transfer Object)
 *
 * 严格遵循repowiki规范:
 * - 用于打卡请求的数据传输对象
 * - 完整的参数验证注解
 * - 支持位置信息和设备验证
 * - 包含照片上传功能
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "考勤打卡DTO")
public class AttendancePunchDTO {

    @NotNull(message = "员工ID不能为空")
    @Schema(description = "员工ID", required = true)
    private Long employeeId;

    @NotNull(message = "打卡时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "打卡时间", required = true)
    private LocalDateTime punchTime;

    @NotBlank(message = "打卡类型不能为空")
    @Pattern(regexp = "^(上班|下班)$", message = "打卡类型只能是上班或下班")
    @Schema(description = "打卡类型：上班、下班", required = true)
    private String punchType;

    @Schema(description = "纬度")
    private Double latitude;

    @Schema(description = "经度")
    private Double longitude;

    @Schema(description = "打卡位置描述")
    private String location;

    @Schema(description = "打卡设备ID")
    private String deviceId;

    @Schema(description = "打卡设备名称")
    private String deviceName;

    @Schema(description = "打卡设备类型：MOBILE-手机，CARD-刷卡机，FACE-人脸识别，FINGER-指纹")
    private String deviceType;

    @Schema(description = "打卡照片URL")
    private String photoUrl;

    @Schema(description = "打卡照片Base64数据")
    private String photoBase64;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "MAC地址")
    private String macAddress;

    @Schema(description = "WiFi名称")
    private String wifiName;

    @Schema(description = "是否人脸识别验证：0-否，1-是")
    private Integer faceVerified;

    @Schema(description = "人脸识别相似度")
    private Double faceSimilarity;

    @Schema(description = "是否需要位置验证：0-否，1-是")
    private Integer needLocationValidation;

    @Schema(description = "位置验证结果：0-失败，1-成功")
    private Integer locationValidationResult;

    @Schema(description = "位置偏差距离（米）")
    private Double locationDistance;

    @Schema(description = "打卡备注")
    private String remark;

    @Schema(description = "打卡场景：NORMAL-正常打卡，MAKE_UP-补卡，OUTDOOR-外勤打卡")
    private String punchScene;

    @Schema(description = "外勤事由（外勤打卡时必填）")
    private String outdoorReason;

    @Schema(description = "客户端版本")
    private String clientVersion;

    @Schema(description = "操作系统：iOS、Android、Windows")
    private String operatingSystem;

    @Schema(description = "应用标识")
    private String appId;
}