package net.lab1024.sa.attendance.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 打卡记录表实体
 *
 * 打卡记录管理的核心实体，记录所有员工的打卡数据
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_clock_records")
public class ClockRecordsEntity extends BaseEntity {

    /**
     * 打卡记录ID
     */
    private Long recordId;

    /**
     * 记录编码
     */
    @TableField("`record_code`")
    private String recordCode;

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
     * 打卡类型: IN-上班打卡 OUT-下班打卡 OVERTIME_IN-加班开始 OVERTIME_OUT-加班结束
     */
    @TableField("`clock_type`")
    private String clockType;

    /**
     * 设备ID
     */
    @TableField("`device_id`")
    private Long deviceId;

    /**
     * 设备名称
     */
    @TableField("`device_name`")
    private String deviceName;

    /**
     * 位置ID
     */
    @TableField("`location_id`")
    private Long locationId;

    /**
     * 位置名称
     */
    @TableField("`location_name`")
    private String locationName;

    /**
     * 打卡时间
     */
    @TableField("`clock_time`")
    private LocalDateTime clockTime;

    /**
     * 打卡日期
     */
    @TableField("`clock_date`")
    private LocalDate clockDate;

    /**
     * 生物特征类型: FACE-人脸 FINGERPRINT-指纹 PASSWORD-密码 QRCODE-二维码
     */
    @TableField("`biometric_type`")
    private String biometricType;

    /**
     * 验证结果: SUCCESS-成功 FAILED-失败 TIMEOUT-超时
     */
    @TableField("`verification_result`")
    private String verificationResult;

    /**
     * 匹配分数 0.0000-1.0000
     */
    @TableField("`match_score`")
    private BigDecimal matchScore;

    /**
     * 位置信息 JSON格式
     * {"lng":116.404,"lat":39.915,"accuracy":5.0,"address":"详细地址"}
     */
    @TableField("`location_info`")
    private String locationInfo;

    /**
     * 设备信息 JSON格式
     * {"deviceType":"attendance_machine","deviceId":"A001","firmware":"v1.0"}
     */
    @TableField("`device_info`")
    private String deviceInfo;

    /**
     * 照片路径
     */
    @TableField("`photo_path`")
    private String photoPath;

    /**
     * 异常信息 JSON格式
     * {"type":"LATE","minutes":15,"description":"迟到15分钟"}
     */
    @TableField("`exception_info`")
    private String exceptionInfo;

    /**
     * 考勤模式: NORMAL-正常 REMOTE-远程 OFFSITE-外勤
     */
    @TableField("`attendance_mode`")
    private String attendanceMode;

    /**
     * 同步状态: PENDING-待同步 SYNCED-已同步 FAILED-同步失败
     */
    @TableField("`sync_status`")
    private String syncStatus;

    /**
     * 原始数据 JSON格式
     */
    @TableField("`raw_data`")
    private String rawData;

    /**
     * 备注
     */
    @TableField("`remarks`")
    private String remarks;

    /**
     * 打卡类型描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String clockTypeDesc;

    /**
     * 生物特征类型描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String biometricTypeDesc;

    /**
     * 验证结果描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String verificationResultDesc;

    /**
     * 部门名称（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String departmentName;

    /**
     * 同步状态描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String syncStatusDesc;

    /**
     * 是否异常打卡（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean isAbnormal;

    /**
     * 异常类型（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String abnormalType;

    /**
     * 格式化打卡时间（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String formattedClockTime;

    /**
     * 照片缩略图URL（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String photoThumbnailUrl;
}
