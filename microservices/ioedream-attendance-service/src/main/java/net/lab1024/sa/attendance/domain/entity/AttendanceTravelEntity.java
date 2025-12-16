package net.lab1024.sa.attendance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 考勤出差实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_travel")
public class AttendanceTravelEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String travelNo;
    private Long employeeId;
    private String employeeName;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer travelDays;
    private BigDecimal estimatedCost;
    private String reason;
    private String status;
    private String approvalComment;
    private LocalDateTime approvalTime;
    private String remark;

    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}



