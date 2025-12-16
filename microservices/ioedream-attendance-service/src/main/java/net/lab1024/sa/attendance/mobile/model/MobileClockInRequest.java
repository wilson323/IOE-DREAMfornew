package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * 移动端上班打卡请求
 * <p>
 * 封装移动端上班打卡的请求参数
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "移动端上班打卡请求")
public class MobileClockInRequest {

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV001")
    private String deviceCode;

    /**
     * 位置信息
     */
    @Schema(description = "位置信息")
    private Location location;

    /**
     * 生物识别数据
     */
    @Schema(description = "生物识别数据")
    private BiometricData biometricData;

    /**
     * 打卡备注
     */
    @Schema(description = "打卡备注", example = "正常上班打卡")
    private String remark;

    /**
     * 打卡照片
     */
    @Schema(description = "打卡照片Base64")
    private String photo;

    /**
     * 扩展参数
     */
    @Schema(description = "扩展参数")
    private Map<String, Object> extendParams;

    /**
     * 位置信息
     */
    @Data
    @Schema(description = "位置信息")
    public static class Location {

        /**
         * 纬度
         */
        @NotNull(message = "纬度不能为空")
        @Schema(description = "纬度", example = "39.9042")
        private Double latitude;

        /**
         * 经度
         */
        @NotNull(message = "经度不能为空")
        @Schema(description = "经度", example = "116.4074")
        private Double longitude;

        /**
         * 地址
         */
        @Schema(description = "地址", example = "北京市朝阳区建国门外大街1号")
        private String address;

        /**
         * 精度（米）
         */
        @Schema(description = "精度（米）", example = "10.5")
        private Double accuracy;

        /**
         * 时间戳
         */
        @Schema(description = "时间戳", example = "1703020800000")
        private Long timestamp;

        /**
         * 速度（米/秒）
         */
        @Schema(description = "速度（米/秒）", example = "0.0")
        private Double speed;

        /**
         * 海拔（米）
         */
        @Schema(description = "海拔（米）", example = "50.0")
        private Double altitude;

        /**
         * 方向角度
         */
        @Schema(description = "方向角度", example = "45.0")
        private Double bearing;
    }

    /**
     * 生物识别数据
     */
    @Data
    @Schema(description = "生物识别数据")
    public static class BiometricData {

        /**
         * 生物识别类型
         */
        @Schema(description = "生物识别类型", example = "FACE", allowableValues = {"FACE", "FINGERPRINT", "VOICE", "IRIS"})
        private String type;

        /**
         * 生物识别数据（Base64编码）
         */
        @Schema(description = "生物识别数据（Base64编码）", example = "base64_encoded_biometric_data")
        private String data;

        /**
         * 生物识别模板ID
         */
        @Schema(description = "生物识别模板ID", example = "BIOTEMPLATE_001")
        private String templateId;

        /**
         * 匹配度
         */
        @Schema(description = "匹配度", example = "95.5")
        private Double confidence;

        /**
         * 活体检测结果
         */
        @Schema(description = "活体检测结果", example = "true")
        private Boolean livenessCheck;

        /**
         * 采集时间
         */
        @Schema(description = "采集时间", example = "1703020800000")
        private Long captureTime;
    }
}