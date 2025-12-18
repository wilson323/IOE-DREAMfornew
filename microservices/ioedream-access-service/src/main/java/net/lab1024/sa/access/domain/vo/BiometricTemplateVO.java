package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 鐢熺墿璇嗗埆妯℃澘瑙嗗浘瀵硅薄
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "鐢熺墿璇嗗埆妯℃澘瑙嗗浘瀵硅薄")
public class BiometricTemplateVO {

    @Schema(description = "妯℃澘ID", example = "1001")
    private Long templateId;

    @Schema(description = "鐢ㄦ埛ID", example = "1001")
    private Long userId;

    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷", example = "1")
    private Integer biometricType;

    @Schema(description = "鐢熺墿璇嗗埆绫诲瀷鎻忚堪", example = "浜鸿劯璇嗗埆")
    private String biometricTypeDesc;

    @Schema(description = "妯℃澘鍚嶇О", example = "鐢ㄦ埛浜鸿劯鐗瑰緛妯℃澘")
    private String templateName;

    @Schema(description = "妯℃澘鐘舵€?, example = "1")
    private Integer templateStatus;

    @Schema(description = "妯℃澘鐘舵€佹弿杩?, example = "婵€娲?)
    private String templateStatusDesc;

    @Schema(description = "鍖归厤闃堝€?, example = "0.85")
    private Double matchThreshold;

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

    @Schema(description = "鎴愬姛鐜?, example = "97.43%")
    private String successRate;

    @Schema(description = "涓婃浣跨敤鏃堕棿", example = "2025-01-30T09:15:00")
    private LocalDateTime lastUseTime;

    @Schema(description = "鍥剧墖璺緞", example = "/biometric/face/1001_20250130.jpg")
    private String imagePath;

    @Schema(description = "鍒涘缓鏃堕棿", example = "2025-01-30T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "鏇存柊鏃堕棿", example = "2025-01-30T14:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "鐗瑰緛鏁版嵁", example = "鍔犲瘑鐨勭敓鐗╄瘑鍒壒寰佹暟鎹?)
    private String featureData;

    @Schema(description = "鐗瑰緛鍚戦噺", example = "AI绠楁硶鐢熸垚鐨勭壒寰佸悜閲?)
    private String featureVector;

    // ========== 缁熻鐩稿叧瀛楁 ==========

    @Schema(description = "鐢ㄦ埛鎬绘ā鏉挎暟", example = "3")
    private Integer totalTemplates;

    @Schema(description = "鐢ㄦ埛鎬讳娇鐢ㄦ鏁?, example = "1250")
    private Integer totalUseCount;

    @Schema(description = "鐢ㄦ埛鎬绘垚鍔熸鏁?, example = "1190")
    private Integer totalSuccessCount;

    @Schema(description = "鐢ㄦ埛鎬诲け璐ユ鏁?, example = "60")
    private Integer totalFailCount;

    @Schema(description = "澶囨敞", example = "浜鸿劯璇嗗埆妯℃澘")
    private String remarks;
}