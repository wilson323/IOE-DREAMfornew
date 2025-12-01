package net.lab1024.sa.admin.module.smart.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 人脸搜索结果VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "人脸搜索结果信息")
public class FaceSearchResultVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "人员ID")
    private Long personId;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "相似度")
    private Double similarity;

    @Schema(description = "截图路径")
    private String snapshotPath;

    @Schema(description = "视频路径")
    private String videoPath;

    @Schema(description = "检测时间")
    private LocalDateTime detectionTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // 添加Controller需要的字段
    @Schema(description = "匹配数量")
    private Integer matchCount;

    @Schema(description = "处理时间(毫秒)")
    private Long processingTime;
}