package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 鐢熺墿璇嗗埆璁よ瘉琛ㄥ崟
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "鐢熺墿璇嗗埆璁よ瘉琛ㄥ崟")
public class BiometricAuthForm {

    @Schema(description = "鐢ㄦ埛ID锛?:1楠岃瘉鏃跺繀濉級", example = "1001")
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

    @Min(value = 1, message = "杩斿洖缁撴灉鏁伴噺涓嶈兘灏忎簬1")
    @Max(value = 100, message = "杩斿洖缁撴灉鏁伴噺涓嶈兘瓒呰繃100")
    @Schema(description = "杩斿洖缁撴灉鏁伴噺闄愬埗锛?:N璇嗗埆鏃朵娇鐢級", example = "10")
    private Integer limit;

    @Schema(description = "瀹㈡埛绔疘P鍦板潃", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "瀹㈡埛绔俊鎭?, example = "iOS App v1.2.0")
    private String clientInfo;

    @Schema(description = "楠岃瘉绫诲瀷", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer authType;

    @Schema(description = "鏄惁寮哄埗娲讳綋妫€娴?, example = "true")
    private Boolean forceLiveness;
}