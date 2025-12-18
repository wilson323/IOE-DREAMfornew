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
 * 生物识别认证记录实体
 * 记录每次生物识别认证的详细信息
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
@Schema(description = "生物识别认证记录实体")
public class BiometricAuthRecordEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "认证ID", example = "BIO_AUTH_20250130_001")
    private String authId;

    @NotNull
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull
    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    @NotNull
    @Schema(description = "生物识别类型", example = "1", allowableValues = {"1", "2", "3"})
    private Integer biometricType;

    @NotNull
    @Schema(description = "认证类型", example = "1", allowableValues = {"1", "2", "3"})
    private Integer authType;

    @NotNull
    @Schema(description = "认证结果", example = "1", allowableValues = {"1", "2", "3"})
    private Integer authResult;

    @Schema(description = "匹配得分", example = "0.95")
    private BigDecimal matchScore;

    @Schema(description = "匹配阈值", example = "0.85")
    private BigDecimal matchThreshold;

    @Schema(description = "活体检测得分", example = "0.92")
    private BigDecimal livenessScore;

    @Schema(description = "活体检测结果", example = "true")
    private Boolean livenessPassed;

    @Schema(description = "认证耗时(毫秒)", example = "1250")
    private Long authDuration;

    @Schema(description = "认证时间", example = "2025-01-30T14:30:00")
    private LocalDateTime authTime;

    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @Schema(description = "认证图片路径", example = "/biometric/auth/face_20250130_143000.jpg")
    private String authImagePath;

    @Schema(description = "识别错误码", example = "")
    private String errorCode;

    @Schema(description = "识别错误信息", example = "")
    private String errorMessage;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "客户端信息", example = "iOS App v1.2.0")
    private String clientInfo;

    @Schema(description = "设备位置", example = "A栋1楼大厅")
    private String deviceLocation;

    @Schema(description = "环境信息", example = "{\"temperature\":25.5,\"humidity\":60,\"lighting\":0.8}")
    private String environmentInfo;

    @Schema(description = "算法版本", example = "v2.1.0")
    private String algorithmVersion;

    @Schema(description = "安全级别", example = "2")
    private Integer securityLevel;

    @Schema(description = "是否可疑操作", example = "false")
    private Boolean suspiciousOperation;

    @Schema(description = "可疑原因", example = "")
    private String suspiciousReason;

    @Schema(description = "人工复核状态", example = "0")
    private Integer manualReviewStatus;

    @Schema(description = "复核人员ID", example = "2001")
    private Long reviewerId;

    @Schema(description = "复核时间", example = "2025-01-30T14:35:00")
    private LocalDateTime reviewTime;

    @Schema(description = "复核意见", example = "认证正常，通过")
    private String reviewComment;

    // 枚举定义
    public enum AuthType {
        LOGIN(1, "登录认证"),
        ACCESS(2, "门禁认证"),
        ATTENDANCE(3, "考勤认证"),
        PAYMENT(4, "支付认证"),
        REGISTER(5, "注册认证");

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
        SUCCESS(1, "认证成功"),
        FAILED(2, "认证失败"),
        TIMEOUT(3, "认证超时"),
        ERROR(4, "系统错误");

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
        NOT_REVIEWED(0, "未复核"),
        APPROVED(1, "通过"),
        REJECTED(2, "拒绝"),
        PENDING(3, "待复核");

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