package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 鐢熺墿璇嗗埆鏁版嵁琛ㄥ崟
 * <p>
 * 鐢ㄤ簬鐢熺墿璇嗗埆瀹夊叏楠岃瘉鐨勮姹傛暟鎹?
 * 涓ユ牸閬靛惊CLAUDE.md瑙勮寖锛?
 * - 浣跨敤@Data娉ㄨВ
 * - 瀹屾暣鐨勫瓧娈甸獙璇佹敞瑙?
 * - Swagger鏂囨。娉ㄨВ
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "鐢熺墿璇嗗埆鏁版嵁")
public class BiometricDataForm {

    /**
     * 鐢ㄦ埛ID
     */
    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    /**
     * 鐢熺墿璇嗗埆绫诲瀷
     * FACE - 浜鸿劯璇嗗埆
     * FINGERPRINT - 鎸囩汗璇嗗埆
     * IRIS - 铏硅啘璇嗗埆
     * PALM - 鎺岀汗璇嗗埆
     * VOICE - 澹扮汗璇嗗埆
     */
    @NotNull(message = "鐢熺墿璇嗗埆绫诲瀷涓嶈兘涓虹┖")
    @Pattern(regexp = "^(FACE|FINGERPRINT|IRIS|PALM|VOICE)$", message = "鐢熺墿璇嗗埆绫诲瀷鏃犳晥")
    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "FACE", allowableValues = {"FACE", "FINGERPRINT", "IRIS", "PALM", "VOICE"})
    private String biometricType;

    /**
     * 鐢熺墿璇嗗埆鏁版嵁锛圔ase64缂栫爜锛?
     */
    @NotNull(message = "鐢熺墿璇嗗埆鏁版嵁涓嶈兘涓虹┖")
    @Schema(description = "鐢熺墿璇嗗埆鏁版嵁锛圔ase64缂栫爜锛?, example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...")
    private String biometricData;

    /**
     * 鏁版嵁璐ㄩ噺璇勫垎锛?-100锛?
     */
    @Schema(description = "鏁版嵁璐ㄩ噺璇勫垎锛?-100锛?, example = "92.5")
    private Integer qualityScore;

    /**
     * 璁惧淇℃伅
     */
    @Schema(description = "閲囬泦璁惧淇℃伅", example = "iPhone 13 Pro")
    private String deviceInfo;

    /**
     * 鐜淇℃伅
     */
    @Schema(description = "鐜淇℃伅", example = "瀹ゅ唴姝ｅ父鍏夌収")
    private String environmentInfo;

    /**
     * 鍏冩暟鎹?
     */
    @Schema(description = "棰濆鍏冩暟鎹?)
    private String metadata;
}