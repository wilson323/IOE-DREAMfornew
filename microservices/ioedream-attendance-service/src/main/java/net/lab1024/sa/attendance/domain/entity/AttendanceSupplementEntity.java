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
 * 考勤补签实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_supplement")
public class AttendanceSupplementEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String supplementNo;
    private Long employeeId;
    private String employeeName;
    private LocalDate supplementDate;
    private LocalTime punchTime;
    private String punchType;
    private String reason;
    private String status;
    private String approvalComment;
    private LocalDateTime approvalTime;
    private String remark;

    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}



