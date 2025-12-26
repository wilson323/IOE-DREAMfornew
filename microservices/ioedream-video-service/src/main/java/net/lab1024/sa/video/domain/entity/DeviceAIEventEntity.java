package net.lab1024.sa.video.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备AI事件实体
 * <p>
 * 边缘计算架构：设备端完成AI分析，服务器接收结构化事件
 * </p>
 * <p>
 * 架构说明（2025-01-30）：
 * - 设备端AI芯片完成实时分析（跌倒检测、异常行为等）
 * - 设备上报结构化事件到服务器（eventType, confidence, bbox, snapshot）
 * - 服务器端只负责事件存储、规则匹配、告警推送
 * - 原始视频保留在设备端（7-30天），告警时按需回调
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@TableName("t_video_device_ai_event")
@Schema(description = "设备AI事件")
public class DeviceAIEventEntity {

    /**
     * 事件ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "事件ID")
    private String eventId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    @Schema(description = "设备编码")
    private String deviceCode;

    /**
     * 事件类型
     * <p>
     * 支持的事件类型：
     * - FALL_DETECTION: 跌倒检测
     * - LOITERING_DETECTION: 徘陪检测
     * - GATHERING_DETECTION: 聚集检测
     * - FIGHTING_DETECTION: 打架检测
     * - RUNNING_DETECTION: 奔跑检测
     * - CLIMBING_DETECTION: 攀爬检测
     * - FACE_DETECTION: 人脸检测
     * - INTRUSION_DETECTION: 入侵检测
     * </p>
     */
    @TableField("event_type")
    @Schema(description = "事件类型")
    private String eventType;

    /**
     * 置信度（0.0-1.0）
     */
    @TableField("confidence")
    @Schema(description = "置信度")
    private BigDecimal confidence;

    /**
     * 边界框（JSON格式）
     * <p>
     * 格式：{"x": 100, "y": 150, "width": 200, "height": 300}
     * </p>
     */
    @TableField("bbox")
    @Schema(description = "边界框")
    private String bbox;

    /**
     * 抓拍图片（二进制）
     * <p>
     * 事件发生时的抓拍图片（仅关键帧）
     * </p>
     */
    @TableField("snapshot")
    @Schema(description = "抓拍图片")
    private byte[] snapshot;

    /**
     * 事件时间
     */
    @TableField("event_time")
    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    /**
     * 扩展属性（JSON格式）
     * <p>
     * 用于存储特定事件类型的扩展信息，例如：
     * - 徘陪时长：{"duration": 120}
     * - 聚集人数：{"personCount": 5}
     * - 人员ID：{"personId": "P12345"}
     * </p>
     */
    @TableField("extended_attributes")
    @Schema(description = "扩展属性")
    private String extendedAttributes;

    /**
     * 事件状态
     * <p>
     * - 0: 待处理
     * - 1: 已处理
     * - 2: 已忽略
     * </p>
     */
    @TableField("event_status")
    @Schema(description = "事件状态")
    private Integer eventStatus;

    /**
     * 处理时间
     */
    @TableField("process_time")
    @Schema(description = "处理时间")
    private LocalDateTime processTime;

    /**
     * 告警ID（如果触发告警）
     */
    @TableField("alarm_id")
    @Schema(description = "告警ID")
    private String alarmId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记")
    private Integer deletedFlag;
}
