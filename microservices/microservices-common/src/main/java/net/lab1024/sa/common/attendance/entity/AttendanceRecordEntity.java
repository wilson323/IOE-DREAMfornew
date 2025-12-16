package net.lab1024.sa.common.attendance.entity;

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
 * 考勤记录实体类
 * <p>
 * 用于记录员工考勤打卡信息
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * - 字段数控制在30个以内
 * - 行数控制在200行以内
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_record")
public class AttendanceRecordEntity extends BaseEntity {

    /**
     * 考勤记录ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long recordId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    private String userName;

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
     * 班次ID
     */
    @TableField("shift_id")
    private Long shiftId;

    /**
     * 班次名称
     */
    @TableField("shift_name")
    private String shiftName;

    /**
     * 考勤日期
     */
    @TableField("attendance_date")
    private LocalDate attendanceDate;

    /**
     * 打卡时间
     */
    @TableField("punch_time")
    private LocalDateTime punchTime;

    /**
     * 考勤状态
     * <p>
     * NORMAL-正常
     * LATE-迟到
     * EARLY-早退
     * ABSENT-缺勤
     * OVERTIME-加班
     * </p>
     */
    @TableField("attendance_status")
    private String attendanceStatus;

    /**
     * 考勤类型
     * <p>
     * CHECK_IN-上班打卡
     * CHECK_OUT-下班打卡
     * </p>
     */
    @TableField("attendance_type")
    private String attendanceType;

    /**
     * 打卡位置（经度）
     */
    @TableField("longitude")
    private java.math.BigDecimal longitude;

    /**
     * 打卡位置（纬度）
     */
    @TableField("latitude")
    private java.math.BigDecimal latitude;

    /**
     * 打卡地址
     */
    @TableField("punch_address")
    private String punchAddress;

    /**
     * 打卡设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 打卡设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}



