package net.lab1024.sa.admin.module.attendance.domain.vo;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 考勤记录查询条件 VO
 *
 * 严格遵循repowiki规范:
 * - 用于查询条件的数据传输对象
 * - 包含所有可能的查询筛选条件
 * - 支持范围查询和模糊查询
 * - 完整的Swagger注解
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "考勤记录查询条件VO")
public class AttendanceRecordQueryVO {

    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "员工ID")
    private Long employeeId;

    @Schema(description = "员工姓名（模糊查询）")
    private String employeeName;

    @Schema(description = "员工工号")
    private String employeeNo;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "部门名称（模糊查询）")
    private String departmentName;

    @Schema(description = "考勤日期开始")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDateStart;

    @Schema(description = "考勤日期结束")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDateEnd;

    @Schema(description = "考勤状态：NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-旷工, ABNORMAL-异常")
    private String attendanceStatus;

    @Schema(description = "异常类型：LATE-迟到, EARLY_LEAVE-早退, ABSENTEEISM-旷工, FORGET_PUNCH-忘记打卡, INCOMPLETE_RECORD-记录不完整")
    private String exceptionType;

    @Schema(description = "是否需要审批：0-否，1-是")
    private Integer needApproval;

    @Schema(description = "审批状态：PENDING-待审批, APPROVED-已通过, REJECTED-已拒绝")
    private String approvalStatus;

    @Schema(description = "打卡设备")
    private String device;

    @Schema(description = "打卡位置（模糊查询）")
    private String location;

    @Schema(description = "最小工作时长（小时）")
    private java.math.BigDecimal minWorkHours;

    @Schema(description = "最大工作时长（小时）")
    private java.math.BigDecimal maxWorkHours;

    @Schema(description = "最小加班时长（小时）")
    private java.math.BigDecimal minOvertimeHours;

    @Schema(description = "最大加班时长（小时）")
    private java.math.BigDecimal maxOvertimeHours;

    @Schema(description = "是否有迟到记录：0-否，1-是")
    private Integer hasLateRecord;

    @Schema(description = "是否有早退记录：0-否，1-是")
    private Integer hasEarlyLeaveRecord;

    @Schema(description = "是否有旷工记录：0-否，1-是")
    private Integer hasAbsentRecord;

    @Schema(description = "创建时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createTimeStart;

    @Schema(description = "创建时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private java.time.LocalDateTime createTimeEnd;

    @Schema(description = "备注信息（模糊查询）")
    private String remark;

    @Schema(description = "排序字段：attendanceDate, workHours, overtimeHours, createTime")
    private String sortBy;

    @Schema(description = "排序方向：asc-升序，desc-降序")
    private String sortDirection;

    @Schema(description = "页码", example = "1")
    private Long pageNum;

    @Schema(description = "每页数量", example = "10")
    private Long pageSize;

    /**
     * 获取页码
     *
     * @return 页码，默认为1
     */
    public Long getPageNum() {
        return pageNum != null ? pageNum : 1L;
    }

    /**
     * 设置页码
     *
     * @param pageNum 页码
     */
    public void setPageNum(Long pageNum) {
        this.pageNum = pageNum;
    }

    /**
     * 获取每页数量
     *
     * @return 每页数量，默认为10
     */
    public Long getPageSize() {
        return pageSize != null ? pageSize : 10L;
    }

    /**
     * 设置每页数量
     *
     * @param pageSize 每页数量
     */
    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取开始日期（兼容方法）
     *
     * @return 开始日期
     */
    public LocalDate getStartDate() {
        return attendanceDateStart;
    }

    /**
     * 获取结束日期（兼容方法）
     *
     * @return 结束日期
     */
    public LocalDate getEndDate() {
        return attendanceDateEnd;
    }
}
