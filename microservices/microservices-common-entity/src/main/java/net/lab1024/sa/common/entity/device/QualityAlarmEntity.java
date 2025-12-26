package net.lab1024.sa.common.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 质量告警实体
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_quality_alarm")
@Schema(description = "质量告警实体")
public class QualityAlarmEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "告警ID")
    private Long alarmId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "触发的规则ID")
    private Long ruleId;

    @Schema(description = "告警级别(1-低 2-中 3-高 4-紧急)")
    private Integer alarmLevel;

    @Schema(description = "告警标题")
    private String alarmTitle;

    @Schema(description = "告警内容")
    private String alarmContent;

    @Schema(description = "告警状态(1-待处理 2-处理中 3-已处理)")
    private Integer alarmStatus;

    @Schema(description = "处理结果")
    private String handleResult;

    @Schema(description = "处理人ID")
    private Long handleUserId;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
}
