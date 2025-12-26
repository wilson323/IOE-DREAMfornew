package net.lab1024.sa.common.entity.attendance;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 部门统计表实体类
 * <p>
 * 持久化部门考勤统计数据
 * 从AttendanceSummary聚合生成
 * 严格遵循CLAUDE.md规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_department_statistics")
public class DepartmentStatisticsEntity extends BaseEntity {

    /**
     * 统计ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long statisticsId;

    /**
     * 部门ID
     */
    @TableField("department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @TableField("department_name")
    private String departmentName;

    /**
     * 统计月份（格式：2024-01）
     */
    @TableField("statistics_month")
    private String statisticsMonth;

    /**
     * 部门总人数
     */
    @TableField("total_employees")
    private Integer totalEmployees;

    /**
     * 出勤人数
     */
    @TableField("present_employees")
    private Integer presentEmployees;

    /**
     * 缺勤人数
     */
    @TableField("absent_employees")
    private Integer absentEmployees;

    /**
     * 迟到人数
     */
    @TableField("late_employees")
    private Integer lateEmployees;

    /**
     * 部门出勤率（0-1之间的小数）
     */
    @TableField("attendance_rate")
    private BigDecimal attendanceRate;

    /**
     * 平均工作时长（小时）
     */
    @TableField("avg_work_hours")
    private BigDecimal avgWorkHours;

    /**
     * 总加班时长（小时）
     */
    @TableField("total_overtime_hours")
    private BigDecimal totalOvertimeHours;

    /**
     * 异常次数
     */
    @TableField("exception_count")
    private Integer exceptionCount;

    /**
     * 统计详情JSON（包含各类详细统计）
     */
    @TableField("statistics_details")
    private String statisticsDetails;

    /**
     * 状态：0-删除，1-正常
     */
    @TableField("status")
    private Integer status;
}
