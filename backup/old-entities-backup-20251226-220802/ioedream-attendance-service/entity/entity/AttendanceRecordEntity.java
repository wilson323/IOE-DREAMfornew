package net.lab1024.sa.attendance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
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
/**     * 工作时长（小时）- 计算字段，不在数据库中存储     */    @TableField(exist = false)    private Double workDuration;

    // ========== 便利方法（兼容现有代码） ==========

    /**
     * 获取记录ID（兼容方法）
     * <p>
     * 实际字段为 recordId
     * </p>
     *
     * @return 记录ID
     */
    public Long getId() {
        return this.recordId;
    }

    /**
     * 设置记录ID（兼容方法）
     *
     * @param id 记录ID
     */
    public void setId(Long id) {
        this.recordId = id;
    }

    /**
     * 获取员工ID（兼容方法）
     * <p>
     * 实际字段为 userId
     * </p>
     *
     * @return 员工ID
     */
    public Long getEmployeeId() {
        return this.userId;
    }

    /**
     * 设置员工ID（兼容方法）
     *
     * @param employeeId 员工ID
     */
    public void setEmployeeId(Long employeeId) {
        this.userId = employeeId;
    }

    /**
     * 获取打卡类型（兼容方法）
     * <p>
     * 实际字段为 attendanceType (String)
     * 返回: 0-上班打卡, 1-下班打卡
     * </p>
     *
     * @return 打卡类型 (0-上班, 1-下班)
     */
    public Integer getPunchType() {
        if (this.attendanceType == null) {
            return null;
        }
        return "CHECK_IN".equals(this.attendanceType) ? 0 : 1;
    }

    /**
     * 设置打卡类型（兼容方法）
     *
     * @param punchType 打卡类型 (0-上班, 1-下班)
     */
    public void setPunchType(Integer punchType) {
        if (punchType == null) {
            this.attendanceType = null;
        } else {
            this.attendanceType = punchType == 0 ? "CHECK_IN" : "CHECK_OUT";
        }
    }

    /**
     * 获取上班打卡时间（兼容方法）
     * <p>
     * 实际字段为 punchTime
     * 当 attendanceType 为 CHECK_IN 时返回 punchTime
     * </p>
     *
     * @return 上班打卡时间
     */
    public LocalDateTime getClockInTime() {
        if ("CHECK_IN".equals(this.attendanceType)) {
            return this.punchTime;
        }
        return null;
    }

    /**
     * 获取下班打卡时间（兼容方法）
     * <p>
     * 实际字段为 punchTime
     * 当 attendanceType 为 CHECK_OUT 时返回 punchTime
     * </p>
     *
     * @return 下班打卡时间
     */
    public LocalDateTime getClockOutTime() {
        if ("CHECK_OUT".equals(this.attendanceType)) {
            return this.punchTime;
        }
        return null;
    }

    /**
     * 获取工作时长（兼容方法）
     * <p>
     * 注意：实体类中没有 workHours 字段
     * 此方法需要根据实际业务逻辑计算
     * </p>
     *
     * @return 工作时长（小时），如果无法计算返回null
     */
    public Double getWorkHours() {
        // 注意：此方法需要根据实际业务逻辑实现
        // 可能需要查询同一天的上班和下班记录来计算
        return null;
    }

    /**
     * 设置工作时长（兼容方法）
     * <p>
     * 注意：实体类中没有 workHours 字段
     * 此方法仅用于兼容，实际值需要存储在其他地方
     * </p>
     *
     * @param workHours 工作时长（小时）
     */
    public void setWorkHours(Double workHours) {
        // 注意：实体类中没有 workHours 字段
        // 此方法仅用于兼容，实际值需要存储在其他地方
    }

    /**
     * 获取打卡位置（兼容方法）
     * <p>
     * 返回格式化的位置字符串
     * </p>
     *
     * @return 打卡位置字符串
     */
    public String getLocation() {
        if (this.punchAddress != null) {
            return this.punchAddress;
        }
        if (this.longitude != null && this.latitude != null) {
            return this.longitude + "," + this.latitude;
        }
        return null;
    }

    /**
     * 设置打卡位置（兼容方法）
     * <p>
     * 如果传入的是坐标格式（经度,纬度），则解析并设置到对应字段
     * 否则设置为地址
     * </p>
     *
     * @param location 打卡位置（地址或坐标）
     */
    public void setLocation(String location) {
        if (location == null) {
            this.punchAddress = null;
            return;
        }
        // 判断是否为坐标格式
        if (location.contains(",") && location.split(",").length == 2) {
            try {
                String[] parts = location.split(",");
                this.longitude = new java.math.BigDecimal(parts[0].trim());
                this.latitude = new java.math.BigDecimal(parts[1].trim());
            } catch (Exception e) {
                // 解析失败，作为地址处理
                this.punchAddress = location;
            }
        } else {
            this.punchAddress = location;
        }
    }
}



