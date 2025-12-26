package net.lab1024.sa.common.monitor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 告警实体（安防告警）
 * <p>
 * 对应数据库表: t_alert
 * </p>
 * <p>
 * 告警类型（alert_type）:
 * - 1: 设备告警（门禁、视频等设备故障或异常）
 * - 2: AI告警（人脸识别、行为分析等AI检测告警）
 * - 3: 系统告警（系统监控、性能告警等）
 * </p>
 * <p>
 * 告警级别（alert_level）:
 * - 1: P1紧急（需要立即处理，全通道推送）
 * - 2: P2重要（需要及时处理，APP+Web推送）
 * - 3: P3普通（常规处理，仅Web推送）
 * - 4: P4提示（信息提示，不推送）
 * </p>
 * <p>
 * 告警状态（status）:
 * - 1: 待处理
 * - 2: 处理中
 * - 3: 已处理
 * - 4: 已忽略
 * </p>
 * <p>
 * 处理结果（handle_result）:
 * - 1: 确认（告警属实）
 * - 2: 误报（告警不属实）
 * - 3: 无法处理（需要其他部门处理）
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_alert")
public class AlertEntity extends BaseEntity {

    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long alertId;

    /**
     * 告警编码（唯一标识）
     */
    @TableField("alert_code")
    private String alertCode;

    /**
     * 告警类型（1-设备告警, 2-AI告警, 3-系统告警）
     */
    @TableField("alert_type")
    private Integer alertType;

    /**
     * 告警子类型（如：人脸识别、行为分析、设备故障等）
     */
    @TableField("alert_subtype")
    private String alertSubtype;

    /**
     * 告警级别（1-P1紧急, 2-P2重要, 3-P3普通, 4-P4提示）
     */
    @TableField("alert_level")
    private Integer alertLevel;

    /**
     * 告警标题
     */
    @TableField("alert_title")
    private String alertTitle;

    /**
     * 告警内容
     */
    @TableField("alert_content")
    private String alertContent;

    /**
     * 设备ID（关联设备）
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 通道号（视频设备通道）
     */
    @TableField("channel_no")
    private Integer channelNo;

    /**
     * 区域ID（关联区域）
     */
    @TableField("region_id")
    private Long regionId;

    /**
     * 区域名称
     */
    @TableField("region_name")
    private String regionName;

    /**
     * 告警时间
     */
    @TableField("alert_time")
    private LocalDateTime alertTime;

    /**
     * 截图路径（AI告警的截图）
     */
    @TableField("snapshot_path")
    private String snapshotPath;

    /**
     * 录像路径（告警相关的录像）
     */
    @TableField("video_path")
    private String videoPath;

    /**
     * 来源ID（AI事件ID等）
     */
    @TableField("source_id")
    private Long sourceId;

    /**
     * 来源类型（AI_EVENT, DEVICE_EVENT等）
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 告警来源（兼容字段，用于系统告警）
     */
    @TableField("source")
    private String source;

    /**
     * 告警状态（1-待处理, 2-处理中, 3-已处理, 4-已忽略）
     */
    @TableField("status")
    private Integer status;

    /**
     * 处理人ID
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @TableField("handler_name")
    private String handlerName;

    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;

    /**
     * 处理结果（1-确认, 2-误报, 3-无法处理）
     */
    @TableField("handle_result")
    private Integer handleResult;

    /**
     * 处理备注
     */
    @TableField("handle_remark")
    private String handleRemark;

    /**
     * 聚合数量（相同告警的聚合数量）
     */
    @TableField("aggregate_count")
    private Integer aggregateCount;

    /**
     * 获取ID（兼容BaseEntity的getId方法）
     * <p>
     * 由于使用alertId作为主键，需要提供getId方法以兼容代码中的entity.getId()调用
     * </p>
     *
     * @return 告警ID
     */
    public Long getId() {
        return alertId;
    }

    /**
     * 设置ID（兼容BaseEntity的setId方法）
     *
     * @param id 告警ID
     */
    public void setId(Long id) {
        this.alertId = id;
    }
}

