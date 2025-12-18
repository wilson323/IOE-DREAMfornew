package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 鐢熺墿璇嗗埆娉ㄥ唽琛ㄥ崟
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "鐢熺墿璇嗗埆娉ㄥ唽琛ㄥ崟")
public class BiometricRegisterForm {

    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @NotNull(message = "鐢熺墿璇嗗埆绫诲瀷涓嶈兘涓虹┖")
    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer biometricType;

    @NotBlank(message = "鐗瑰緛鏁版嵁涓嶈兘涓虹┖")
    @Size(max = 5000000, message = "鐗瑰緛鏁版嵁涓嶈兘瓒呰繃5MB")
    @Schema(description = "鐗瑰緛鏁版嵁(Base64缂栫爜)", example = "base64_encoded_feature_data")
    private String featureData;

    @NotBlank(message = "璁惧ID涓嶈兘涓虹┖")
    @Size(max = 100, message = "璁惧ID闀垮害涓嶈兘瓒呰繃100涓瓧绗?)
    @Schema(description = "璁惧ID", example = "DEVICE_001")
    private String deviceId;

    @Size(max = 200, message = "澶囨敞闀垮害涓嶈兘瓒呰繃200涓瓧绗?)
    @Schema(description = "澶囨敞", example = "浜鸿劯璇嗗埆妯℃澘")
    private String remarks;
}