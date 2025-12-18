package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 鐢熺墿璇嗗埆妯℃澘瀹炰綋
 * 瀛樺偍鐢ㄦ埛鐢熺墿璇嗗埆鐗瑰緛妯℃澘鏁版嵁
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_biometric_template")
@Schema(description = "鐢熺墿璇嗗埆妯℃澘瀹炰綋")
public class BiometricTemplateEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "妯℃澘ID", example = "1001")
    private Long templateId;

    @NotNull
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @NotNull
    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "1", allowableValues = {"1", "2", "3"})
    private Integer biometricType;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "妯℃澘鍚嶇О", example = "鐢ㄦ埛浜鸿劯鐗瑰緛妯℃澘")
    private String templateName;

    @NotNull
    @Schema(description = "妯℃澘鐘舵€?, example = "1", allowableValues = {"1", "2", "3"})
    private Integer templateStatus;

    @Schema(description = "鐗瑰緛鏁版嵁", example = "鐢熺墿璇嗗埆鐗瑰緛鏁版嵁(Base64缂栫爜)")
    private String featureData;

    @Schema(description = "鐗瑰緛鍚戦噺", example = "AI绠楁硶鐢熸垚鐨勭壒寰佸悜閲?)
    private String featureVector;

    @Schema(description = "鍖归厤闃堝€?, example = "0.85")
    private Double matchThreshold;

    @Schema(description = "娲讳綋妫€娴嬮厤缃?, example = "{\"enabled\":true,\"liveness\":0.9}")
    private String livenessConfig;

    @Schema(description = "绠楁硶鐗堟湰", example = "v2.1.0")
    private String algorithmVersion;

    @Schema(description = "璁惧ID", example = "DEVICE_001")
    private String deviceId;

    @Schema(description = "閲囬泦鏃堕棿", example = "2025-01-30T14:30:00")
    private LocalDateTime captureTime;

    @Schema(description = "杩囨湡鏃堕棿", example = "2026-01-30T14:30:00")
    private LocalDateTime expireTime;

    @Schema(description = "浣跨敤娆℃暟", example = "156")
    private Integer useCount;

    @Schema(description = "楠岃瘉鎴愬姛娆℃暟", example = "152")
    private Integer successCount;

    @Schema(description = "楠岃瘉澶辫触娆℃暟", example = "4")
    private Integer failCount;

    @Schema(description = "涓婃浣跨敤鏃堕棿", example = "2025-01-30T09:15:00")
    private LocalDateTime lastUseTime;

    @Schema(description = "鍥剧墖璺緞", example = "/biometric/face/1001_20250130.jpg")
    private String imagePath;

    @Schema(description = "鎵╁睍灞炴€?, example = "{\"quality\":0.95,\"lighting\":\"good\"}")
    private String extendedAttributes;

    // 鏋氫妇瀹氫箟
    public enum BiometricType {
        FACE(1, "浜鸿劯璇嗗埆"),
        FINGERPRINT(2, "鎸囩汗璇嗗埆"),
        IRIS(3, "铏硅啘璇嗗埆"),
        VOICE(4, "澹扮汗璇嗗埆"),
        PALM(5, "鎺岀汗璇嗗埆");

        private final int code;
        private final String description;

        BiometricType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static BiometricType fromCode(int code) {
            for (BiometricType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid biometric type code: " + code);
        }
    }

    public enum TemplateStatus {
        ACTIVE(1, "婵€娲?),
        INACTIVE(2, "鏈縺娲?),
        EXPIRED(3, "宸茶繃鏈?),
        LOCKED(4, "宸查攣瀹?);

        private final int code;
        private final String description;

        TemplateStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static TemplateStatus fromCode(int code) {
            for (TemplateStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid template status code: " + code);
        }
    }
}