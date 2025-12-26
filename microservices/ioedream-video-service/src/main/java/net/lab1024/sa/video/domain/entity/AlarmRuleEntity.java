package net.lab1024.sa.video.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 告警规则实体
 * <p>
 * 边缘计算架构：设备 AI 事件触发告警规则匹配
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@TableName("t_video_alarm_rule")
@Schema(description = "告警规则实体")
public class AlarmRuleEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则类型: FALL_DETECTION-跌倒检测, LOITERING_DETECTION-徘徊检测, GATHERING_DETECTION-聚集检测, FIGHTING_DETECTION-打架检测, INTRUSION_DETECTION-入侵检测")
    private String ruleType;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "置信度阈值（0.0000-1.0000）")
    private Double confidenceThreshold;

    @Schema(description = "区域ID（可选）")
    private Long areaId;

    @Schema(description = "设备ID（可选，空表示所有设备）")
    private String deviceId;

    @Schema(description = "规则状态: 1-启用, 0-禁用")
    private Integer ruleStatus;

    @Schema(description = "生效时间开始")
    private LocalTime effectiveStartTime;

    @Schema(description = "生效时间结束")
    private LocalTime effectiveEndTime;

    @Schema(description = "告警级别: 1-低, 2-中, 3-高, 4-紧急")
    private Integer alarmLevel;

    @Schema(description = "是否推送通知: 1-是, 0-否")
    private Integer pushNotification;

    @Schema(description = "通知方式（JSON）: {\"email\":true,\"sms\":false,\"websocket\":true}")
    private String notificationMethods;

    @Schema(description = "告警消息模板")
    private String alarmMessageTemplate;

    @Schema(description = "规则优先级（数字越大优先级越高）")
    private Integer priority;

    @Schema(description = "扩展配置（JSON格式）")
    private String extendedConfig;

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
