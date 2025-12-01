package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 检测结果视图对象
 *
 * @author SmartAdmin Team
 * @version 1.0
 * @since 2025-11-16
 */
@Data
@Schema(description = "检测结果视图对象")
public class DetectionResultVO {

    /**
     * 检测结果ID
     */
    @Schema(description = "检测结果ID", example = "1")
    private Long detectionId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主入口摄像头")
    private String deviceName;

    /**
     * 检测类型
     */
    @Schema(description = "检测类型", example = "PERSON_DETECTION")
    private String detectionType;

    /**
     * 检测类型描述
     */
    @Schema(description = "检测类型描述", example = "人员检测")
    private String detectionTypeDesc;

    /**
     * 置信度
     */
    @Schema(description = "置信度", example = "0.95")
    private Double confidence;

    /**
     * 检测时间
     */
    @Schema(description = "检测时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime detectionTime;

    /**
     * 检测图像URL
     */
    @Schema(description = "检测图像URL", example = "http://server.com/images/detection_001.jpg")
    private String detectionImageUrl;

    /**
     * 检测对象数量
     */
    @Schema(description = "检测对象数量", example = "3")
    private Integer objectCount;

    /**
     * 检测对象列表
     */
    @Schema(description = "检测对象列表")
    private List<DetectionObject> objects;

    /**
     * 录像片段URL
     */
    @Schema(description = "录像片段URL", example = "http://server.com/records/detection_clip_001.mp4")
    private String videoClipUrl;

    /**
     * 录像开始时间
     */
    @Schema(description = "录像开始时间", example = "2025-01-15 10:29:50")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime videoStartTime;

    /**
     * 录像结束时间
     */
    @Schema(description = "录像结束时间", example = "2025-01-15 10:30:10")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime videoEndTime;

    /**
     * 告警级别
     */
    @Schema(description = "告警级别", example = "HIGH")
    private String alertLevel;

    /**
     * 告警级别描述
     */
    @Schema(description = "告警级别描述", example = "高级")
    private String alertLevelDesc;

    /**
     * 处理状态
     */
    @Schema(description = "处理状态", example = "PENDING")
    private String processStatus;

    /**
     * 处理状态描述
     */
    @Schema(description = "处理状态描述", example = "待处理")
    private String processStatusDesc;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "1001")
    private Long processUserId;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名", example = "保安员")
    private String processUserName;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间", example = "2025-01-15 10:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注", example = "已确认，正常情况")
    private String processRemark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-15 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 检测对象内部类
     */
    @Data
    @Schema(description = "检测对象")
    public static class DetectionObject {

        /**
         * 对象ID
         */
        @Schema(description = "对象ID", example = "obj_001")
        private String objectId;

        /**
         * 对象类型
         */
        @Schema(description = "对象类型", example = "PERSON")
        private String objectType;

        /**
         * 对象类型描述
         */
        @Schema(description = "对象类型描述", example = "人员")
        private String objectTypeDesc;

        /**
         * 置信度
         */
        @Schema(description = "置信度", example = "0.92")
        private Double confidence;

        /**
         * 位置坐标X
         */
        @Schema(description = "位置坐标X", example = "150")
        private Integer positionX;

        /**
         * 位置坐标Y
         */
        @Schema(description = "位置坐标Y", example = "200")
        private Integer positionY;

        /**
         * 宽度
         */
        @Schema(description = "宽度", example = "60")
        private Integer width;

        /**
         * 高度
         */
        @Schema(description = "高度", example = "120")
        private Integer height;

        /**
         * 属性信息
         */
        @Schema(description = "属性信息", example = "男性,成年,蓝色上衣")
        private String attributes;
    }
}