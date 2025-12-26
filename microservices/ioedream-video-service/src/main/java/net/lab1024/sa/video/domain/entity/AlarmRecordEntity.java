package net.lab1024.sa.video.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 告警记录实体
 * <p>
 * 边缘计算架构：设备 AI 事件触发告警后的告警记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@TableName("t_video_alarm_record")
@Schema(description = "告警记录实体")
public class AlarmRecordEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "告警ID")
    private String alarmId;

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "事件ID")
    private String eventId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "告警级别: 1-低, 2-中, 3-高, 4-紧急")
    private Integer alarmLevel;

    @Schema(description = "告警状态: 0-待处理, 1-处理中, 2-已处理, 3-已忽略")
    private Integer alarmStatus;

    @Schema(description = "置信度")
    private Double confidence;

    @Schema(description = "边界框（JSON格式）")
    private String bbox;

    @Schema(description = "抓拍图片URL")
    private String snapshotUrl;

    @Schema(description = "告警消息")
    private String alarmMessage;

    @Schema(description = "告警时间")
    private LocalDateTime alarmTime;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人姓名")
    private String handlerName;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "是否已推送通知: 1-是, 0-否")
    private Integer notificationSent;

    @Schema(description = "通知推送时间")
    private LocalDateTime notificationTime;

    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField("deleted_flag")
    @Schema(description = "删除标记: 0-未删除, 1-已删除")
    private Integer deletedFlag;
}
