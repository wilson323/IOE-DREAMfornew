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
import java.math.BigDecimal;

/**
 * 鐢熺墿璇嗗埆楠岃瘉璁板綍瀹炰綋
 * 璁板綍姣忔鐢熺墿璇嗗埆楠岃瘉鐨勮缁嗕俊鎭?
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_access_biometric_auth_record")
@Schema(description = "鐢熺墿璇嗗埆楠岃瘉璁板綍瀹炰綋")
public class BiometricAuthRecordEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "楠岃瘉ID", example = "BIO_AUTH_20250130_001")
    private String authId;

    @NotNull
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @NotNull
    @Schema(description = "璁惧ID", example = "DEVICE_001")
    private String deviceId;

    @NotNull
    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "1", allowableValues = {"1", "2", "3"})
    private Integer biometricType;

    @NotNull
    @Schema(description = "楠岃瘉绫诲瀷", example = "1", allowableValues = {"1", "2", "3"})
    private Integer authType;

    @NotNull
    @Schema(description = "楠岃瘉缁撴灉", example = "1", allowableValues = {"1", "2", "3"})
    private Integer authResult;

    @Schema(description = "鍖归厤寰楀垎", example = "0.95")
    private BigDecimal matchScore;

    @Schema(description = "鍖归厤闃堝€?, example = "0.85")
    private BigDecimal matchThreshold;

    @Schema(description = "娲讳綋妫€娴嬪緱鍒?, example = "0.92")
    private BigDecimal livenessScore;

    @Schema(description = "娲讳綋妫€娴嬬粨鏋?, example = "true")
    private Boolean livenessPassed;

    @Schema(description = "楠岃瘉鏃堕棿(姣)", example = "1250")
    private Long authDuration;

    @Schema(description = "楠岃瘉鏃堕棿", example = "2025-01-30T14:30:00")
    private LocalDateTime authTime;

    @Schema(description = "妯℃澘ID", example = "1001")
    private Long templateId;

    @Schema(description = "楠岃瘉鍥剧墖璺緞", example = "/biometric/auth/face_20250130_143000.jpg")
    private String authImagePath;

    @Schema(description = "璇嗗埆閿欒鐮?, example = "")
    private String errorCode;

    @Schema(description = "璇嗗埆閿欒淇℃伅", example = "")
    private String errorMessage;

    @Schema(description = "瀹㈡埛绔疘P鍦板潃", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "瀹㈡埛绔俊鎭?, example = "iOS App v1.2.0")
    private String clientInfo;

    @Schema(description = "璁惧浣嶇疆", example = "A鏍?妤煎ぇ鍘?)
    private String deviceLocation;

    @Schema(description = "鐜淇℃伅", example = "{\"temperature\":25.5,\"humidity\":60,\"lighting\":0.8}")
    private String environmentInfo;

    @Schema(description = "绠楁硶鐗堟湰", example = "v2.1.0")
    private String algorithmVersion;

    @Schema(description = "瀹夊叏绾у埆", example = "2")
    private Integer securityLevel;

    @Schema(description = "鏄惁鍙枒鎿嶄綔", example = "false")
    private Boolean suspiciousOperation;

    @Schema(description = "鍙枒鍘熷洜", example = "")
    private String suspiciousReason;

    @Schema(description = "浜哄伐澶嶆牳鐘舵€?, example = "0")
    private Integer manualReviewStatus;

    @Schema(description = "澶嶆牳浜哄憳ID", example = "2001")
    private Long reviewerId;

    @Schema(description = "澶嶆牳鏃堕棿", example = "2025-01-30T14:35:00")
    private LocalDateTime reviewTime;

    @Schema(description = "澶嶆牳鎰忚", example = "楠岃瘉姝ｅ父锛岄€氳繃")
    private String reviewComment;

    // 鏋氫妇瀹氫箟
    public enum AuthType {
        LOGIN(1, "鐧诲綍楠岃瘉"),
        ACCESS(2, "闂ㄧ楠岃瘉"),
        ATTENDANCE(3, "鑰冨嫟楠岃瘉"),
        PAYMENT(4, "鏀粯楠岃瘉"),
        REGISTER(5, "娉ㄥ唽楠岃瘉");

        private final int code;
        private final String description;

        AuthType(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AuthType fromCode(int code) {
            for (AuthType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid auth type code: " + code);
        }
    }

    public enum AuthResult {
        SUCCESS(1, "楠岃瘉鎴愬姛"),
        FAILED(2, "楠岃瘉澶辫触"),
        TIMEOUT(3, "楠岃瘉瓒呮椂"),
        ERROR(4, "绯荤粺閿欒");

        private final int code;
        private final String description;

        AuthResult(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static AuthResult fromCode(int code) {
            for (AuthResult result : values()) {
                if (result.code == code) {
                    return result;
                }
            }
            throw new IllegalArgumentException("Invalid auth result code: " + code);
        }
    }

    public enum ManualReviewStatus {
        NOT_REVIEWED(0, "鏈鏍?),
        APPROVED(1, "閫氳繃"),
        REJECTED(2, "鎷掔粷"),
        PENDING(3, "寰呭鏍?);

        private final int code;
        private final String description;

        ManualReviewStatus(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ManualReviewStatus fromCode(int code) {
            for (ManualReviewStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid manual review status code: " + code);
        }
    }
}