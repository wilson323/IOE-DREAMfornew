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
 * 考勤统计Excel数据模型
 * <p>
 * 用于EasyExcel导出的统计数据模型
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
public class AttendanceStatisticsExcelVO {

    @ExcelProperty("统计类型")
    @ColumnWidth(15)
    private String statisticsType;

    @ExcelProperty("统计对象名称")
    @ColumnWidth(20)
    private String targetName;

    @ExcelProperty("总人数")
    @ColumnWidth(12)
    private Integer totalCount;

    @ExcelProperty("出勤人数")
    @ColumnWidth(12)
    private Integer attendanceCount;

    @ExcelProperty("缺勤人数")
    @ColumnWidth(12)
    private Integer absenceCount;

    @ExcelProperty("出勤率(%)")
    @ColumnWidth(15)
    private BigDecimal attendanceRate;

    @ExcelProperty("平均工作时长(小时)")
    @ColumnWidth(20)
    private BigDecimal avgWorkHours;

    @ExcelProperty("迟到总次数")
    @ColumnWidth(15)
    private Integer lateCount;

    @ExcelProperty("早退总次数")
    @ColumnWidth(15)
    private Integer earlyLeaveCount;

    @ExcelProperty("加班总时长(小时)")
    @ColumnWidth(20)
    private BigDecimal totalOvertimeHours;

    @ExcelProperty("人均加班时长(小时)")
    @ColumnWidth(20)
    private BigDecimal avgOvertimeHours;
}
