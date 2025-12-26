package net.lab1024.sa.common.entity.attendance;

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
 * 考勤调班实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_shift")
public class AttendanceShiftEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String shiftNo;
    private Long employeeId;
    private String employeeName;
    private LocalDate shiftDate;
    private Long originalShiftId;
    private Long targetShiftId;
    private String reason;
    private String status;
    private String approvalComment;
    private LocalDateTime approvalTime;
    private String remark;

    @TableField("workflow_instance_id")
    private Long workflowInstanceId;
}



