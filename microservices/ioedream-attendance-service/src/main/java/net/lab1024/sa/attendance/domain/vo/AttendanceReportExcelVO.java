package net.lab1024.sa.attendance.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 考勤报表Excel数据模型
 * <p>
 * 用于EasyExcel导出的数据模型
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@HeadRowHeight(20)
@ContentRowHeight(18)
@ColumnWidth(20)
public class AttendanceReportExcelVO {

    @ExcelProperty("员工ID")
    @ColumnWidth(15)
    private Long employeeId;

    @ExcelProperty("员工姓名")
    @ColumnWidth(15)
    private String employeeName;

    @ExcelProperty("部门名称")
    @ColumnWidth(20)
    private String departmentName;

    @ExcelProperty("报表日期")
    @ColumnWidth(18)
    private String reportDate;

    @ExcelProperty("出勤天数")
    @ColumnWidth(12)
    private Integer attendanceDays;

    @ExcelProperty("缺勤天数")
    @ColumnWidth(12)
    private Integer absenceDays;

    @ExcelProperty("迟到次数")
    @ColumnWidth(12)
    private Integer lateCount;

    @ExcelProperty("早退次数")
    @ColumnWidth(12)
    private Integer earlyLeaveCount;

    @ExcelProperty("加班时长(小时)")
    @ColumnWidth(18)
    private BigDecimal overtimeHours;

    @ExcelProperty("工作总时长(小时)")
    @ColumnWidth(18)
    private BigDecimal totalWorkHours;
}
