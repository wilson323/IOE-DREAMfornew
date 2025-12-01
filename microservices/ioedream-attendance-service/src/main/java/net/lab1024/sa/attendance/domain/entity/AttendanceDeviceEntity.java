package net.lab1024.sa.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
// TEMP: Device module not yet available
/**
 * 考勤设备实体类
 *
// TEMP: Device functionality disabled
 *
 * @author SmartAdmin Team
 * @date 2025-11-24
 */
import net.lab1024.sa.common.entity.BaseEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_attendance_device")
public class AttendanceDeviceEntity extends BaseEntity {

    /**
     * 考勤机类型 (FINGERPRINT-指纹机, FACE-人脸识别, CARD-刷卡机, PASSWORD-密码机, HYBRID-混合)
     */
    private String attendanceDeviceType;

    /**
     * 考勤方式 (BIOMETRIC-生物识别, CARD-刷卡, PASSWORD-密码, QR-二维码, NFC-NFC识别)
     */
    private String attendanceMethod;

    /**
     * 识别时间阈值(秒)
     */
    private Integer recognitionThreshold;

    /**
     * 是否支持活体检测 (0-不支持, 1-支持)
     */
    private Integer liveDetectionEnabled;

    /**
     * 是否支持WiFi连接 (0-不支持, 1-支持)
     */
    private Integer wifiEnabled;

    /**
     * WiFi SSID
     */
    private String wifiSSID;

    /**
     * 是否支持4G网络 (0-不支持, 1-支持)
     */
    private Integer fourGEnabled;

    /**
     * 4G卡号
     */
    private String simCardNumber;

    /**
     * 最大用户容量
     */
    private Integer maxUserCapacity;

    /**
     * 当前用户数量
     */
    private Integer currentUserCount;

    /**
     * 最大考勤记录数量
     */
    private Integer maxRecordCount;

    /**
     * 当前考勤记录数量
     */
    private Integer currentRecordCount;

    /**
     * 识别精度阈值 (%)
     */
    private Double accuracyThreshold;

    /**
     * 考勤规则模式 (FLEXIBLE-弹性模式, STRICT-严格模式, AUTO-自动模式)
     */
    private String attendanceMode;

    /**
     * 支持的考勤规则(JSON格式)
     */
    private String supportedRules;

    /**
     * 是否支持照片保存 (0-不支持, 1-支持)
     */
    private Integer photoSaveEnabled;

    /**
     * 照片质量 (HIGH-高质量, MEDIUM-中等质量, LOW-低质量)
     */
    private String photoQuality;

    /**
     * 是否支持温度检测 (0-不支持, 1-支持)
     */
    private Integer temperatureDetectionEnabled;

    /**
     * 温度异常阈值(℃)
     */
    private Double temperatureThreshold;

    /**
     * 是否支持口罩识别 (0-不支持, 1-支持)
     */
    private Integer maskRecognitionEnabled;

    /**
     * 口罩识别准确率阈值 (%)
     */
    private Double maskAccuracyThreshold;

    /**
     * 是否支持离线考勤 (0-不支持, 1-支持)
     */
    private Integer offlineAttendanceEnabled;

    /**
     * 离线存储天数
     */
    private Integer offlineStorageDays;

    /**
     * 数据同步方式 (REALTIME-实时同步, BATCH-批量同步, MANUAL-手动同步)
     */
    private String syncMode;

    /**
     * 自动同步间隔(分钟)
     */
    private Integer autoSyncInterval;

    /**
     * 是否支持GPS定位 (0-不支持, 1-支持)
     */
    private Integer gpsEnabled;

    /**
     * 定位精度范围(米)
     */
    private Double gpsAccuracyRange;

    /**
     * 考勤区域ID
     */
    private Long attendanceAreaId;

    /**
     * 设备安装位置详细描述
     */
    private String installLocationDetail;

    /**
     * 管理员ID
     */
    private Long adminUserId;
}
