package net.lab1024.sa.access.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁记录批量上传请求DTO
 * <p>
 * 用于接收设备批量上传的通行记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "门禁记录批量上传请求")
public class AccessRecordBatchUploadRequest {

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "设备ID", example = "DEV001", required = true)
    private String deviceId;

    /**
     * 批次ID（用于幂等性检查）
     */
    @Schema(description = "批次ID（用于幂等性检查）", example = "BATCH_20250130_001")
    private String batchId;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间", example = "2025-01-30 10:00:00")
    private LocalDateTime uploadTime;

    /**
     * 通行记录列表
     */
    @NotEmpty(message = "通行记录列表不能为空")
    @Valid
    @Schema(description = "通行记录列表", required = true)
    private List<AccessRecordDTO> records;

    /**
     * 通行记录DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "通行记录")
    public static class AccessRecordDTO {
        /**
         * 用户ID
         */
        @NotNull(message = "用户ID不能为空")
        @Schema(description = "用户ID", example = "1001", required = true)
        private Long userId;

        /**
         * 区域ID
         */
        @Schema(description = "区域ID", example = "2001")
        private Long areaId;

        /**
         * 通行结果（1-成功 2-失败）
         */
        @NotNull(message = "通行结果不能为空")
        @Schema(description = "通行结果", example = "1", allowableValues = {"1", "2"}, required = true)
        private Integer accessResult;

        /**
         * 通行时间
         */
        @NotNull(message = "通行时间不能为空")
        @Schema(description = "通行时间", example = "2025-01-30 10:00:00", required = true)
        private LocalDateTime accessTime;

        /**
         * 通行类型（IN-进入 OUT-离开）
         */
        @Schema(description = "通行类型", example = "IN", allowableValues = {"IN", "OUT"})
        private String accessType;

        /**
         * 验证方式（FACE-人脸 CARD-刷卡 FINGERPRINT-指纹）
         */
        @Schema(description = "验证方式", example = "FACE", allowableValues = {"FACE", "CARD", "FINGERPRINT"})
        private String verifyMethod;

        /**
         * 照片路径
         */
        @Schema(description = "照片路径", example = "/photos/2025/01/30/xxx.jpg")
        private String photoPath;

        /**
         * 记录唯一标识（用于幂等性检查）
         */
        @Schema(description = "记录唯一标识（用于幂等性检查）", example = "REC_20250130_1001_001")
        private String recordUniqueId;
    }

    /**
     * 批量上传结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批量上传结果")
    public static class BatchUploadResult {
        /**
         * 批次ID
         */
        @Schema(description = "批次ID", example = "BATCH_20250130_001")
        private String batchId;

        /**
         * 总记录数
         */
        @Schema(description = "总记录数", example = "100")
        private Integer totalCount;

        /**
         * 成功记录数
         */
        @Schema(description = "成功记录数", example = "95")
        private Integer successCount;

        /**
         * 失败记录数
         */
        @Schema(description = "失败记录数", example = "5")
        private Integer failCount;

        /**
         * 重复记录数（幂等性检查）
         */
        @Schema(description = "重复记录数", example = "3")
        private Integer duplicateCount;

        /**
         * 处理状态（PROCESSING-处理中 SUCCESS-成功 FAILED-失败）
         */
        @Schema(description = "处理状态", example = "SUCCESS", allowableValues = {"PROCESSING", "SUCCESS", "FAILED"})
        private String status;

        /**
         * 处理时间（毫秒）
         */
        @Schema(description = "处理时间（毫秒）", example = "1250")
        private Long processTime;

        /**
         * 错误信息列表
         */
        @Schema(description = "错误信息列表")
        private List<String> errorMessages;
    }
}
