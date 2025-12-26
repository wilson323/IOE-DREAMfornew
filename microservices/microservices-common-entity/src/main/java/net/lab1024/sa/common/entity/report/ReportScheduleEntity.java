package net.lab1024.sa.common.entity.report;
import net.lab1024.sa.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 报表调度任务实体类
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_report_schedule")
@Schema(description = "报表调度任务实体")
public class ReportScheduleEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "调度ID")
    private Long scheduleId;

    @Schema(description = "报表ID", required = true)
    private Long reportId;

    @Schema(description = "调度名称", required = true, example = "每日考勤汇总自动生成")
    private String scheduleName;

    @Schema(description = "Cron表达式", required = true, example = "0 0 1 * * ?")
    private String cronExpression;

    @Schema(description = "调度参数（JSON格式）")
    private String parameters;

    @Schema(description = "通知配置（邮件、消息等）")
    private String notificationConfig;

    @Schema(description = "状态（1-启用 0-禁用）")
    private Integer status;

    @Schema(description = "最后执行时间")
    private LocalDateTime lastExecuteTime;

    @Schema(description = "下次执行时间")
    private LocalDateTime nextExecuteTime;
}
