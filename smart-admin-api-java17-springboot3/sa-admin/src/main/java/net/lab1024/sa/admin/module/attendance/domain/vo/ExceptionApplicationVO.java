package net.lab1024.sa.admin.module.attendance.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 异常申请视图对象
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-24
 */
@Data
@Accessors(chain = true)
public class ExceptionApplicationVO {

    /**
     * 申请ID
     */
    private Long applicationId;

    /**
     * 员工ID
     */
    private Long employeeId;

    /**
     * 员工姓名
     */
    private String employeeName;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 异常类型
     */
    private String exceptionType;

    /**
     * 异常类型描述
     */
    private String exceptionTypeDesc;

    /**
     * 异常日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exceptionDate;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "HH:mm:ss")
    private java.time.LocalTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "HH:mm:ss")
    private java.time.LocalTime endTime;

    /**
     * 申请原因
     */
    private String reason;

    /**
     * 附件
     */
    private String attachment;

    /**
     * 申请状态
     */
    private String applicationStatus;

    /**
     * 申请状态描述
     */
    private String applicationStatusDesc;

    /**
     * 工作流实例ID
     */
    private String workflowInstanceId;

    /**
     * 审批人ID
     */
    private Long approverId;

    /**
     * 审批人姓名
     */
    private String approverName;

    /**
     * 审批意见
     */
    private String approvalComments;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 当前审批任务ID
     */
    private String currentTaskId;

    /**
     * 是否可以撤销
     */
    private Boolean canRevoke = false;

    /**
     * 是否可以编辑
     */
    private Boolean canEdit = false;

    /**
     * 获取申请状态描述
     */
    public String getApplicationStatusDesc() {
        if (applicationStatus == null) {
            return "未知";
        }
        switch (applicationStatus) {
            case "PENDING":
                return "待审批";
            case "APPROVED":
                return "已批准";
            case "REJECTED":
                return "已驳回";
            case "CANCELLED":
                return "已取消";
            default:
                return applicationStatus;
        }
    }

    /**
     * 获取异常类型描述
     */
    public String getExceptionTypeDesc() {
        if (exceptionType == null) {
            return "未知";
        }
        switch (exceptionType) {
            case "LATE":
                return "迟到";
            case "EARLY_LEAVE":
                return "早退";
            case "ABSENTEEISM":
                return "旷工";
            case "OVERTIME":
                return "加班申请";
            case "LEAVE":
                return "请假申请";
            case "FORGOT_PUNCH":
                return "忘打卡";
            default:
                return exceptionType;
        }
    }
}