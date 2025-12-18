package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 闂ㄧ閫氳楠岃瘉璇锋眰
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "闂ㄧ閫氳楠岃瘉璇锋眰")
public class AccessVerifyRequest {

    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "1001", required = true)
    private Long userId;

    @NotBlank(message = "璁惧ID涓嶈兘涓虹┖")
    @Schema(description = "闂ㄧ璁惧ID", example = "ACCESS_001", required = true)
    private String deviceId;

    @Schema(description = "楠岃瘉绫诲瀷", example = "card", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
    private String verifyType = "card";

    @Schema(description = "楠岃瘉鏁版嵁", example = "1234567890")
    private String verifyData;

    @Schema(description = "鍗″彿", example = "1234567890")
    private String cardNumber;

    @Schema(description = "浜鸿劯鐗瑰緛鏁版嵁", example = "base64_encoded_face_data")
    private String faceData;

    @Schema(description = "鎸囩汗鐗瑰緛鏁版嵁", example = "base64_encoded_fingerprint_data")
    private String fingerprintData;

    @Schema(description = "瀵嗙爜", example = "123456")
    private String password;

    @Schema(description = "浜岀淮鐮佹暟鎹?, example = "qr_code_data_123")
    private String qrCodeData;

    @Schema(description = "娓╁害鏁版嵁", example = "36.5")
    private Double temperature;

    @Schema(description = "閫氳鏂瑰悜", example = "in", allowableValues = {"in", "out"})
    private String direction;

    @Schema(description = "鏄惁闇€瑕佹椿浣撴娴?, example = "true")
    private Boolean requireLivenessCheck = false;

    @Schema(description = "鏄惁闇€瑕佹埓鍙ｇ僵妫€娴?, example = "false")
    private Boolean requireMaskCheck = false;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "鎵嬫満鍙风爜鏍煎紡涓嶆纭?)
    @Schema(description = "鎵嬫満鍙风爜锛堢敤浜庣煭淇￠獙璇侊級", example = "13812345678")
    private String phoneNumber;

    @Schema(description = "鎵╁睍鍙傛暟锛圝SON鏍煎紡锛?, example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}