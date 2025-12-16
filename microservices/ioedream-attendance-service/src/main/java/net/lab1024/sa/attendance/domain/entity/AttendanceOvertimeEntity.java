package net.lab1024.sa.attendance.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤加班实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_overtime")
public class AttendanceOvertimeEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String overtimeNo;
    private Long employeeId;
    private String employeeName;
    private LocalDate overtimeDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double overtimeHours;
    private String reason;
    private String status;
    private String approvalComment;
    private LocalDateTime approvalTime;
    private String remark;

    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}



