package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 鐢熺墿璇嗗埆璁よ瘉缁撴灉瑙嗗浘瀵硅薄
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "鐢熺墿璇嗗埆璁よ瘉缁撴灉瑙嗗浘瀵硅薄")
public class BiometricAuthResultVO {

    @Schema(description = "楠岃瘉鏄惁鎴愬姛", example = "true")
    private Boolean success;

    @Schema(description = "妯℃澘ID", example = "1001")
    private Long templateId;

    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @Schema(description = "妯℃澘鍚嶇О", example = "鐢ㄦ埛浜鸿劯鐗瑰緛妯℃澘")
    private String templateName;

    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷鎻忚堪", example = "浜鸿劯璇嗗埆")
    private String biometricTypeDesc;

    @Schema(description = "鍖归厤寰楀垎", example = "0.95")
    private Double matchScore;

    @Schema(description = "娲讳綋妫€娴嬫槸鍚﹂€氳繃", example = "true")
    private Boolean livenessPassed;

    @Schema(description = "娲讳綋妫€娴嬪緱鍒?, example = "0.92")
    private Double livenessScore;

    @Schema(description = "楠岃瘉鑰楁椂(姣)", example = "1250")
    private Long duration;

    @Schema(description = "楠岃瘉缁撴灉娑堟伅", example = "楠岃瘉鎴愬姛")
    private String message;

    @Schema(description = "楠岃瘉鏃堕棿", example = "2025-01-30T14:30:00")
    private String authTime;

    @Schema(description = "璁惧浣嶇疆", example = "A鏍?妤煎ぇ鍘?)
    private String deviceLocation;

    @Schema(description = "鍙枒鎿嶄綔鏍囪瘑", example = "false")
    private Boolean suspiciousOperation;

    @Schema(description = "鍙枒鍘熷洜", example = "")
    private String suspiciousReason;

    @Schema(description = "瀹夊叏绾у埆", example = "2")
    private Integer securityLevel;
}