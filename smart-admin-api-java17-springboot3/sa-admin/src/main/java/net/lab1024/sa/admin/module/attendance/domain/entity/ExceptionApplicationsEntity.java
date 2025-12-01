package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 异常申请表实体
 *
 * 异常申请管理的核心实体，支持请假、加班、调班、补签、销假等申请类型
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@TableName("t_exception_applications")
public class ExceptionApplicationsEntity extends BaseEntity {

    /**
     * 申请ID
     */
    private Long applicationId;

    /**
     * 申请编码
     */
    @TableField("`application_code`")
    private String applicationCode;

    /**
     * 员工ID
     */
    @TableField("`employee_id`")
    private Long employeeId;

    /**
     * 员工姓名
     */
    @TableField("`employee_name`")
    private String employeeName;

    /**
     * 员工编号
     */
    @TableField("`employee_code`")
    private String employeeCode;

    /**
     * 异常类型: LEAVE-请假 OVERTIME-加班 SHIFT_CHANGE-调班 MAKEUP-补签 RESIGNATION-销假
     */
    @TableField("`exception_type`")
    private String exceptionType;

    /**
     * 假种ID（请假时必填）
     */
    @TableField("`leave_type_id`")
    private Long leaveTypeId;

    /**
     * 开始日期
     */
    @TableField("`start_date`")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("`end_date`")
    private LocalDate endDate;

    /**
     * 开始时间
     */
    @TableField("`start_time`")
    private LocalTime startTime;

    /**
     * 结束时间
     */
    @TableField("`end_time`")
    private LocalTime endTime;

    /**
     * 总天数
     */
    @TableField("`total_days`")
    private BigDecimal totalDays;

    /**
     * 总小时数
     */
    @TableField("`total_hours`")
    private BigDecimal totalHours;

    /**
     * 申请原因
     */
    @TableField("`reason`")
    private String reason;

    /**
     * 附件文件列表 JSON格式
     * [{"fileName":"病假证明.pdf","fileUrl":"/uploads/leave_proof.pdf","fileSize":1024}]
     */
    @TableField("`attachment_files`")
    private String attachmentFiles;

    /**
     * 联系方式 JSON格式
     * {"phone":"13800138000","email":"test@example.com","emergencyContact":"张三"}
     */
    @TableField("`contact_info`")
    private String contactInfo;

    /**
     * 申请状态: PENDING-待审批 APPROVED-已通过 REJECTED-已拒绝 WITHDRAWN-已撤回
     */
    @TableField("`application_status`")
    private String applicationStatus;

    /**
     * 紧急程度: LOW-低 NORMAL-中 HIGH-高 URGENT-紧急
     */
    @TableField("`urgency_level`")
    private String urgencyLevel;

    /**
     * 是否自动审批
     */
    @TableField("`auto_approve`")
    private Boolean autoApprove;

    /**
     * 代理人员工ID
     */
    @TableField("`delegate_employee_id`")
    private Long delegateEmployeeId;

    /**
     * 备注
     */
    @TableField("`remarks`")
    private String remarks;

    /**
     * 异常类型描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String exceptionTypeDesc;

    /**
     * 假种名称（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String leaveTypeName;

    /**
     * 部门名称（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 职位名称（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String positionName;

    /**
     * 申请状态描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String applicationStatusDesc;

    /**
     * 紧急程度描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String urgencyLevelDesc;

    /**
     * 代理人姓名（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String delegateEmployeeName;

    /**
     * 格式化申请时间范围（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String formattedTimeRange;

    /**
     * 当前审批层级（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Integer currentApprovalLevel;

    /**
     * 总审批层级（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Integer totalApprovalLevels;
}