package net.lab1024.sa.admin.module.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检测结果VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */

@Schema(description = "检测结果信息")
public class DetectionResultVO {

    @Schema(description = "检测ID")
    private Long detectionId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "检测类型")
    private String detectionType;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "检测对象数量")
    private Integer objectCount;

    @Schema(description = "检测框列表")
    private List<DetectionBox> detectionBoxes;

    @Schema(description = "截图路径")
    private String snapshotPath;

    @Schema(description = "检测时间")
    private LocalDateTime detectionTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // 添加Controller需要的字段
    @Schema(description = "检测数量")
    private Integer detectionCount;

    @Schema(description = "处理时间(毫秒)")
    private Long processingTime;

    /**
     * 检测框
     */
    
    @Schema(description = "检测框信息")
    public static class DetectionBox {
        @Schema(description = "X坐标")
        private Integer x;

        @Schema(description = "Y坐标")
        private Integer y;

        @Schema(description = "宽度")
        private Integer width;

        @Schema(description = "高度")
        private Integer height;

        @Schema(description = "对象类型")
        private String objectType;

        @Schema(description = "置信度")
        private Double confidence;
    }
}