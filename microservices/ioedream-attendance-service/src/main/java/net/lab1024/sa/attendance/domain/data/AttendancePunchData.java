package net.lab1024.sa.attendance.domain.data;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 考勤打卡数据
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-25
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendancePunchData {

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工编号
     */
    private String employeeCode;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 打卡时间
     */
    private LocalDateTime punchTime;

    /**
     * 打卡类型：IN-上班，OUT-下班，BREAK-休息
     */
    private String punchType;

    /**
     * 验证方式：FACE-人脸，FINGERPRINT-指纹，CARD-刷卡，PASSWORD-密码
     */
    private String verifyMethod;

    /**
     * 生物特征数据
     */
    private String biometricData;

    /**
     * 位置信息
     */
    private String location;

    /**
     * 照片路径
     */
    private String photoPath;

    /**
     * 照片URL
     */
    private String photoUrl;

    /**
     * 验证方式
     */
    private String verificationMethod;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 处理状态：PENDING-待处理，PROCESSED-已处理，FAILED-处理失败
     */
    private String processStatus;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedProperties;

    /**
     * 验证打卡数据是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        return employeeId != null && deviceId != null && punchTime != null && punchType != null;
    }

    /**
     * 是否为上班打卡
     *
     * @return 是否为上班打卡
     */
    public boolean isPunchIn() {
        return "IN".equals(this.punchType);
    }

    /**
     * 是否为下班打卡
     *
     * @return 是否为下班打卡
     */
    public boolean isPunchOut() {
        return "OUT".equals(this.punchType);
    }

    /**
     * 是否为生物识别打卡
     *
     * @return 是否为生物识别打卡
     */
    public boolean isBiometricPunch() {
        return "FACE".equals(this.verifyMethod) || "FINGERPRINT".equals(this.verifyMethod);
    }
}