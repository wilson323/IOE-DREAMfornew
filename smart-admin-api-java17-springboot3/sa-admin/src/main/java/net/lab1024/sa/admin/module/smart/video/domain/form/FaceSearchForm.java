package net.lab1024.sa.admin.module.smart.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 人脸搜索表单
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "人脸搜索请求")
public class FaceSearchForm {

    @Schema(description = "人脸特征数据")
    @NotNull(message = "人脸特征数据不能为空")
    private String faceFeature;

    @Schema(description = "相似度阈值")
    private Double similarityThreshold;

    @Schema(description = "搜索结果数量限制")
    private Integer limit;

    @Schema(description = "设备ID列表")
    private String deviceIds;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}