package net.lab1024.sa.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_smart_schedule_rule")
public class ScheduleRule {
    @TableId(type = IdType.AUTO)
    private Long ruleId;
    private String ruleName;
    private String expression;
    private String description;
    private Integer priority;
    private Integer ruleType;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deletedFlag;
}
