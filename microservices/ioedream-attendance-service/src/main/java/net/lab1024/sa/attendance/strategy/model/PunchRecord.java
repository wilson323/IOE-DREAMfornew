package net.lab1024.sa.attendance.strategy.model;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打卡记录模型
 * <p>
 * 封装员工的打卡信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "打卡记录")
public class PunchRecord {

    @Schema(description = "打卡记录ID", example = "10001")
    private Long recordId;

    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    @Schema(description = "打卡时间", example = "2025-01-30 08:55:00")
    private LocalDateTime punchTime;

    @Schema(description = "打卡类型", example = "IN", allowableValues = {"IN", "OUT", "BREAK_IN", "BREAK_OUT"})
    private PunchType punchType;

    @Schema(description = "设备ID", example = "CAM001")
    private String deviceId;

    @Schema(description = "设备名称", example = "A栋1楼大厅考勤机")
    private String deviceName;

    @Schema(description = "打卡方式", example = "FACE", allowableValues = {"FACE", "FINGER", "CARD", "PASSWORD", "MANUAL"})
    private String punchMethod;

    @Schema(description = "位置信息(JSON格式)", example = "{\"longitude\":116.397128,\"latitude\":39.916527}")
    private String locationInfo;

    @Schema(description = "是否验证通过", example = "true")
    private Boolean verified;

    @Schema(description = "验证失败原因", example = "人脸识别失败")
    private String verificationFailureReason;

    @Schema(description = "来源系统", example = "MOBILE_APP", allowableValues = {"MOBILE_APP", "DEVICE", "WEB", "MANUAL"})
    private String sourceSystem;

    @Schema(description = "备注", example = "正常打卡")
    private String remarks;

    /**
     * 打卡类型枚举
     */
    public enum PunchType {
        IN("上班打卡", "IN"),
        OUT("下班打卡", "OUT"),
        BREAK_IN("休息开始", "BREAK_IN"),
        BREAK_OUT("休息结束", "BREAK_OUT");

        private final String description;
        private final String code;

        PunchType(String description, String code) {
            this.description = description;
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public String getCode() {
            return code;
        }

        public static PunchType fromCode(String code) {
            for (PunchType type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid punch type code: " + code);
        }
    }

    /**
     * 判断是否为上班打卡
     *
     * @return 是否上班打卡
     */
    public boolean isClockIn() {
        return punchType == PunchType.IN;
    }

    /**
     * 判断是否为下班打卡
     *
     * @return 是否下班打卡
     */
    public boolean isClockOut() {
        return punchType == PunchType.OUT;
    }

    /**
     * 判断是否为休息打卡
     *
     * @return 是否休息打卡
     */
    public boolean isBreakPunch() {
        return punchType == PunchType.BREAK_IN || punchType == PunchType.BREAK_OUT;
    }

    /**
     * 判断打卡是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(verified) && punchTime != null;
    }
}
