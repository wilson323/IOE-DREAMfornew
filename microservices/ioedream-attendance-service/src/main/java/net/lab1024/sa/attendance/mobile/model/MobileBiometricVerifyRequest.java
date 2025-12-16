package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 移动端生物识别验证请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端生物识别验证请求")
public class MobileBiometricVerifyRequest {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "生物识别类型", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "FACE", allowableValues = {"FACE", "FINGERPRINT", "IRIS", "VOICE"})
    @NotBlank(message = "生物识别类型不能为空")
    private String biometricType;

    @Schema(description = "生物识别数据", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生物识别数据不能为空")
    private BiometricData biometricData;

    @Schema(description = "验证场景", example = "CLOCK_IN", allowableValues = {"CLOCK_IN", "CLOCK_OUT", "LOGIN", "ACCESS"})
    private String verificationScenario;

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "验证阈值", example = "0.85")
    private Double verificationThreshold;

    @Schema(description = "是否启用活体检测", example = "true")
    private Boolean enableLivenessCheck;

    @Schema(description = "是否启用防欺骗检测", example = "true")
    private Boolean enableAntiSpoofing;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedAttributes;

    /**
     * 生物识别数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "生物识别数据")
    public static class BiometricData {

        @Schema(description = "人脸数据")
        private FaceData faceData;

        @Schema(description = "指纹数据")
        private List<FingerprintData> fingerprintData;

        @Schema(description = "虹膜数据")
        private IrisData irisData;

        @Schema(description = "声纹数据")
        private VoiceData voiceData;

        @Schema(description = "特征向量", example = "base64_encoded_feature_vector")
        private String featureVector;

        @Schema(description = "原始数据Base64", example = "base64_encoded_raw_data")
        private String rawData;

        @Schema(description = "数据质量分数", example = "0.95")
        private Double qualityScore;

        @Schema(description = "采集时间戳", example = "1703020800000")
        private Long captureTimestamp;
    }

    /**
     * 人脸数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "人脸数据")
    public static class FaceData {

        @Schema(description = "人脸图像Base64", example = "base64_encoded_face_image")
        private String faceImage;

        @Schema(description = "人脸特征向量", example = "base64_encoded_face_features")
        private String faceFeatures;

        @Schema(description = "人脸检测框", example = "x,y,width,height")
        private String faceBoundingBox;

        @Schema(description = "人脸关键点", example = "keypoints_json")
        private String faceLandmarks;

        @Schema(description = "人脸角度", example = "0.0")
        private Double faceAngle;

        @Schema(description = "人脸大小", example = "200x200")
        private String faceSize;

        @Schema(description = "光照条件", example = "NORMAL", allowableValues = {"GOOD", "NORMAL", "POOR"})
        private String lightingCondition;

        @Schema(description = "是否正脸", example = "true")
        private Boolean isFrontal;

        @Schema(description = "眼睛状态", example = "OPEN", allowableValues = {"OPEN", "CLOSED", "PARTIAL"})
        private String eyeState;

        @Schema(description = "嘴巴状态", example = "CLOSED", allowableValues = {"OPEN", "CLOSED", "SMILE"})
        private String mouthState;
    }

    /**
     * 指纹数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "指纹数据")
    public static class FingerprintData {

        @Schema(description = "指纹ID", example = "FINGER_001")
        private String fingerprintId;

        @Schema(description = "手指类型", example = "THUMB", allowableValues = {"THUMB", "INDEX", "MIDDLE", "RING", "PINKY"})
        private String fingerType;

        @Schema(description = "左右手", example = "RIGHT", allowableValues = {"LEFT", "RIGHT"})
        private String handType;

        @Schema(description = "指纹图像Base64", example = "base64_encoded_fingerprint_image")
        private String fingerprintImage;

        @Schema(description = "指纹特征向量", example = "base64_encoded_fingerprint_features")
        private String fingerprintFeatures;

        @Schema(description = "指纹质量分数", example = "0.92")
        private Double qualityScore;

        @Schema(description = "指纹压力等级", example = "MEDIUM", allowableValues = {"LIGHT", "MEDIUM", "HEAVY"})
        private String pressureLevel;

        @Schema(description = "接触面积", example = "0.85")
        private Double contactArea;

        @Schema(description = "皮肤状态", example = "NORMAL", allowableValues = {"NORMAL", "DRY", "WET", "DIRTY"})
        private String skinCondition;
    }

    /**
     * 虹膜数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "虹膜数据")
    public static class IrisData {

        @Schema(description = "虹膜图像Base64", example = "base64_encoded_iris_image")
        private String irisImage;

        @Schema(description = "虹膜特征向量", example = "base64_encoded_iris_features")
        private String irisFeatures;

        @Schema(description = "眼睛类型", example = "LEFT", allowableValues = {"LEFT", "RIGHT"})
        private String eyeType;

        @Schema(description = "瞳孔直径", example = "4.5")
        private Double pupilDiameter;

        @Schema(description = "虹膜半径", example = "6.2")
        private Double irisRadius;

        @Schema(description = "光照强度", example = "NORMAL", allowableValues = {"LOW", "NORMAL", "HIGH"})
        private String illuminationLevel;

        @Schema(description = "图像清晰度", example = "0.94")
        private Double imageSharpness;

        @Schema(description = "是否有眼镜", example = "false")
        private Boolean hasGlasses;

        @Schema(description = "眼镜类型", example = "NONE", allowableValues = {"NONE", "NORMAL", "SUNGLASSES"})
        private String glassesType;
    }

    /**
     * 声纹数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "声纹数据")
    public static class VoiceData {

        @Schema(description = "语音数据Base64", example = "base64_encoded_voice_data")
        private String voiceData;

        @Schema(description = "声纹特征向量", example = "base64_encoded_voice_features")
        private String voiceFeatures;

        @Schema(description = "语音时长（秒）", example = "3.5")
        private Double duration;

        @Schema(description = "采样率", example = "16000")
        private Integer sampleRate;

        @Schema(description = "音频质量", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
        private String audioQuality;

        @Schema(description = "信噪比", example = "25.5")
        private Double signalToNoiseRatio;

        @Schema(description = "语音类型", example = "PHRASE", allowableValues = {"PHRASE", "NUMBER", "WORD", "SENTENCE"})
        private String voiceType;

        @Schema(description = "语音内容", example = "请说出你的姓名")
        private String voiceContent;

        @Schema(description = "音量等级", example = "MEDIUM", allowableValues = {"LOW", "MEDIUM", "HIGH"})
        private String volumeLevel;

        @Schema(description = "语速", example = "NORMAL", allowableValues = {"SLOW", "NORMAL", "FAST"})
        private String speechRate;
    }
}