package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 鐢熺墿璇嗗埆閰嶇疆瀹炰綋
 * 瀛樺偍鐢熺墿璇嗗埆绯荤粺鐨勯厤缃弬鏁?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_biometric_config")
@Schema(description = "鐢熺墿璇嗗埆閰嶇疆瀹炰綋")
public class BiometricConfigEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "閰嶇疆ID", example = "1")
    private Long configId;

    @NotNull
    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "1", allowableValues = {"1", "2", "3"})
    private Integer biometricType;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "閰嶇疆鍚嶇О", example = "浜鸿劯璇嗗埆绯荤粺閰嶇疆")
    private String configName;

    @NotNull
    @Schema(description = "閰嶇疆鐘舵€?, example = "1", allowableValues = {"1", "2", "3"})
    private Integer configStatus;

    @NotNull
    @Schema(description = "绠楁硶鎻愪緵鍟?, example = "1", allowableValues = {"1", "2", "3"})
    private Integer algorithmProvider;

    @Schema(description = "绠楁硶妯″瀷鍚嶇О", example = "FaceNet-ResNet50")
    private String algorithmModel;

    @Schema(description = "绠楁硶妯″瀷璺緞", example = "/models/facenet_resnet50.onnx")
    private String modelPath;

    @Schema(description = "绠楁硶閰嶇疆鍙傛暟", example = "{\"input_size\":[224,224],\"batch_size\":1}")
    private String algorithmConfig;

    @NotNull
    @Schema(description = "鍖归厤闃堝€?, example = "0.85")
    private Double matchThreshold;

    @Schema(description = "娲讳綋妫€娴嬪惎鐢?, example = "true")
    private Boolean livenessEnabled;

    @Schema(description = "娲讳綋妫€娴嬬畻娉?, example = "1", allowableValues = {"1", "2", "3"})
    private Integer livenessAlgorithm;

    @Schema(description = "娲讳綋妫€娴嬮槇鍊?, example = "0.8")
    private Double livenessThreshold;

    @Schema(description = "娲讳綋妫€娴嬮厤缃?, example = "{\"challenge\":true,\"blink\":true,\"smile\":true}")
    private String livenessConfig;

    @Schema(description = "澶氬洜瀛愰獙璇佸惎鐢?, example = "true")
    private Boolean multiFactorEnabled;

    @Schema(description = "澶囩敤楠岃瘉鏂瑰紡", example = "2")
    private Integer backupAuthType;

    @Schema(description = "鏁版嵁鍔犲瘑鍚敤", example = "true")
    private Boolean dataEncryptionEnabled;

    @Schema(description = "鍔犲瘑瀵嗛挜鐗堟湰", example = "v1.0")
    private String encryptionKeyVersion;

    @Schema(description = "鍥剧墖璐ㄩ噺妫€鏌?, example = "true")
    private Boolean imageQualityCheck;

    @Schema(description = "鏈€灏忓浘鐗囧垎杈ㄧ巼", example = "640")
    private Integer minImageResolution;

    @Schema(description = "鍥剧墖璐ㄩ噺闃堝€?, example = "0.6")
    private Double imageQualityThreshold;

    @Schema(description = "鏈€澶ч獙璇佹椂闂?绉?", example = "5")
    private Integer maxAuthTime;

    @Schema(description = "閲嶈瘯娆℃暟闄愬埗", example = "3")
    private Integer retryLimit;

    @Schema(description = "閿佸畾鏃堕棿(鍒嗛挓)", example = "30")
    private Integer lockoutDuration;

    @Schema(description = "鏃ュ織璁板綍绾у埆", example = "2", allowableValues = {"1", "2", "3"})
    private Integer logLevel;

    @Schema(description = "瀹¤璁板綍鍚敤", example = "true")
    private Boolean auditEnabled;

    @Schema(description = "瀹炴椂鐩戞帶鍚敤", example = "true")
    private Boolean realTimeMonitoring;

    @Schema(description = "鎬ц兘鐩戞帶鍚敤", example = "true")
    private Boolean performanceMonitoring;

    @Schema(description = "鑷姩鏇存柊鍚敤", example = "true")
    private Boolean autoUpdateEnabled;

    @Schema(description = "鏇存柊妫€鏌ラ棿闅?灏忔椂)", example = "24")
    private Integer updateCheckInterval;

    @Schema(description = "閰嶇疆鐗堟湰", example = "1.0")
    private String configVersion;

    @Schema(description = "鐢熸晥鏃堕棿", example = "2025-01-30T00:00:00")
    private LocalDateTime effectiveTime;

    @Schema(description = "澶辨晥鏃堕棿", example = "2025-12-31T23:59:59")
    private LocalDateTime expireTime;

    @Schema(description = "澶囨敞", example = "浜鸿劯璇嗗埆绯荤粺涓婚厤缃?)
    private String remarks;

    // 鏋氫妇瀹氫箟
    public enum AlgorithmProvider {
        OPENCV(1, "OpenCV"),
        FACE_SDK(2, "Face SDK"),
        COMMERCIAL(3, "鍟嗕笟绠楁硶"),
        DEEP_LEARNING(4, "娣卞害瀛︿範"),
        CUSTOM(5, "鑷畾涔?);

        private final int code;
        private final String description;

        AlgorithmProvider(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AlgorithmProvider fromCode(int code) {
            for (AlgorithmProvider provider : values()) {
                if (provider.code == code) {
                    return provider;
                }
            }
            throw new IllegalArgumentException("Invalid algorithm provider code: " + code);
        }
    }

    public enum ConfigStatus {
        DRAFT(1, "鑽夌"),
        ACTIVE(2, "婵€娲?),
        INACTIVE(3, "鏈縺娲?),
        DEPRECATED(4, "宸插純鐢?);

        private final int code;
        private final String description;

        ConfigStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ConfigStatus fromCode(int code) {
            for (ConfigStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid config status code: " + code);
        }
    }
}