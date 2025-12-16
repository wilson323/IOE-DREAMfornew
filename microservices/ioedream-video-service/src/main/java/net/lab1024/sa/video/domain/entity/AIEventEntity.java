package net.lab1024.sa.video.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI事件实体类
 * <p>
 * 对应数据库表 t_video_ai_event，存储AI智能分析事件信息：
 * 1. 基础事件信息
 * 2. 设备和位置信息
 * 3. AI分析结果
 * 4. 处理状态和结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video_ai_event")
@Schema(description = "AI事件实体")
public class AIEventEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("event_id") @Schema(description = "事件ID")
    private String eventId;

    @TableField("device_id") @Schema(description = "设备ID")
    private Long deviceId;

    @TableField("event_type") @Schema(description = "事件类型")
    private String eventType;

    @TableField("event_sub_type") @Schema(description = "事件子类型")
    private String eventSubType;

    @TableField("event_title") @Schema(description = "事件标题")
    private String eventTitle;

    @TableField("event_description") @Schema(description = "事件描述")
    private String eventDescription;

    @TableField("priority") @Schema(description = "优先级(1-10)")
    private Integer priority;

    @TableField("severity") @Schema(description = "严重程度(1-低, 2-中, 3-高, 4-紧急)")
    private Integer severity;

    @TableField("confidence") @Schema(description = "置信度")
    private BigDecimal confidence;

    @TableField("event_data") @Schema(description = "事件数据")
    private String eventData;

    @TableField("video_url") @Schema(description = "视频文件路径")
    private String videoUrl;

    @TableField("image_url") @Schema(description = "图片文件路径")
    private String imageUrl;

    @TableField("location") @Schema(description = "事件发生位置")
    private String location;

    @TableField("longitude") @Schema(description = "经度")
    private BigDecimal longitude;

    @TableField("latitude") @Schema(description = "纬度")
    private BigDecimal latitude;

    @TableField("event_status") @Schema(description = "事件状态(1-待处理, 2-已处理, 3-已忽略)")
    private Integer eventStatus;

    @TableField("process_result") @Schema(description = "处理结果")
    private String processResult;

    @TableField("process_time") @Schema(description = "处理时间")
    private LocalDateTime processTime;

    @TableField("process_user_id") @Schema(description = "处理人ID")
    private Long processUserId;

    @TableField("process_user_name") @Schema(description = "处理人姓名")
    private String processUserName;

    @TableField("process_times") @Schema(description = "处理次数")
    private Integer processTimes;

    @TableField("ai_model_id") @Schema(description = "AI模型ID")
    private String aiModelId;

    @TableField("ai_model_name") @Schema(description = "AI模型名称")
    private String aiModelName;

    @TableField("ai_model_version") @Schema(description = "AI模型版本")
    private String aiModelVersion;

    @TableField("analysis_duration_ms") @Schema(description = "分析耗时(毫秒)")
    private Long analysisDurationMs;

    @TableField("need_manual_review") @Schema(description = "是否需要人工审核")
    private Boolean needManualReview;

    @TableField("manual_review_result") @Schema(description = "人工审核结果")
    private String manualReviewResult;

    @TableField("manual_review_time") @Schema(description = "人工审核时间")
    private LocalDateTime manualReviewTime;

    @TableField("manual_review_user_id") @Schema(description = "人工审核人ID")
    private Long manualReviewUserId;

    @TableField("manual_review_user_name") @Schema(description = "人工审核人姓名")
    private String manualReviewUserName;

    @TableField("tags") @Schema(description = "标签")
    private String tags;

    @TableField("related_event_ids") @Schema(description = "关联事件ID列表")
    private String relatedEventIds;

    @TableField("alert_level") @Schema(description = "预警级别")
    private String alertLevel;

    @TableField("alert_description") @Schema(description = "预警描述")
    private String alertDescription;

    @TableField("notification_sent") @Schema(description = "是否已通知")
    private Boolean notificationSent;

    @TableField("notification_time") @Schema(description = "通知时间")
    private LocalDateTime notificationTime;

    @TableField("notification_methods") @Schema(description = "通知方式")
    private String notificationMethods;

    @TableField("forwarded") @Schema(description = "是否已转发")
    private Boolean forwarded;

    @TableField("forward_target") @Schema(description = "转发目标")
    private String forwardTarget;

    @TableField("forward_time") @Schema(description = "转发时间")
    private LocalDateTime forwardTime;

    @TableField("file_size") @Schema(description = "存储文件大小(字节)")
    private Long fileSize;

    @TableField("storage_path") @Schema(description = "存储路径")
    private String storagePath;

    @TableField("remark") @Schema(description = "备注")
    private String remark;

    @TableField("extended_attributes") @Schema(description = "扩展属性")
    private String extendedAttributes;

    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer deletedFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Version
    @TableField("version")
    private Integer version;
}