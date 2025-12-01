package net.lab1024.sa.access.domain.form;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门禁设备表单
 * <p>
 * 用于门禁设备信息的创建和更新
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "门禁设备表单")
public class AccessDeviceForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门禁设备ID（更新时必填）
     */
    @Schema(description = "门禁设备ID", example = "1")
    private Long accessDeviceId;

    /**
     * 设备ID（继承自SmartDeviceEntity）
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    @Schema(description = "设备编码", example = "DEV_001", required = true)
    private String deviceCode;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    @Schema(description = "设备名称", example = "主门禁机", required = true)
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "1")
    private Integer deviceType;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "1")
    private Integer deviceStatus;

    /**
     * 所属区域ID
     */
    @NotNull(message = "所属区域不能为空")
    @Schema(description = "所属区域ID", example = "1", required = true)
    private Long areaId;

    /**
     * 门禁设备类型
     */
    @NotNull(message = "门禁设备类型不能为空")
    @Schema(description = "门禁设备类型 1:门禁机 2:读卡器 3:指纹机 4:人脸识别机 5:密码键盘 6:三辊闸 7:翼闸 8:摆闸 9:其他", example = "1", required = true)
    private Integer accessDeviceType;

    /**
     * 设备厂商
     */
    @Size(max = 50, message = "设备厂商长度不能超过50个字符")
    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    /**
     * 设备型号
     */
    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    @Schema(description = "设备型号", example = "DS-K2801")
    private String deviceModel;

    /**
     * 设备序列号
     */
    @Size(max = 100, message = "设备序列号长度不能超过100个字符")
    @Schema(description = "设备序列号", example = "DSK280120241116001")
    private String serialNumber;

    /**
     * 通信协议
     */
    @Size(max = 20, message = "通信协议长度不能超过20个字符")
    @Schema(description = "通信协议 TCP/UDP/HTTP/HTTPS/MQTT", example = "TCP")
    private String protocol;

    /**
     * IP地址
     */
    @Size(max = 45, message = "IP地址格式不正确")
    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 端口号
     */
    @Schema(description = "端口号", example = "8000")
    private Integer port;

    /**
     * 通信密钥
     */
    @Size(max = 100, message = "通信密钥长度不能超过100个字符")
    @Schema(description = "通信密钥", example = "ABC123DEF456")
    private String commKey;

    /**
     * 设备方向
     */
    @Schema(description = "设备方向 0:单向进入 1:单向外出 2:双向", example = "2")
    private Integer direction;

    /**
     * 开门方式（支持多选）
     */
    @Schema(description = "开门方式列表 1:刷卡 2:密码 3:指纹 4:人脸 5:二维码", example = "[1, 3, 4]")
    private List<Integer> openMethods;

    /**
     * 开门延时时间
     */
    @Schema(description = "开门延时时间（秒）", example = "3")
    private Integer openDelay;

    /**
     * 有效开门时间
     */
    @Schema(description = "有效开门时间（秒）", example = "5")
    private Integer validTime;

    /**
     * 是否支持远程开门
     */
    @Schema(description = "是否支持远程开门 0:不支持 1:支持", example = "1")
    private Integer remoteOpenEnabled;

    /**
     * 是否支持反潜回
     */
    @Schema(description = "是否支持反潜回 0:不支持 1:支持", example = "1")
    private Integer antiPassbackEnabled;

    /**
     * 是否支持多人同时进入
     */
    @Schema(description = "是否支持多人同时进入 0:不支持 1:支持", example = "0")
    private Integer multiPersonEnabled;

    /**
     * 是否支持门磁检测
     */
    @Schema(description = "是否支持门磁检测 0:不支持 1:支持", example = "1")
    private Integer doorSensorEnabled;

    /**
     * 设备工作模式
     */
    @Schema(description = "设备工作模式 1:正常模式 2:维护模式 3:紧急模式 4:锁闭模式", example = "1")
    private Integer workMode;

    /**
     * 设备版本信息
     */
    @Size(max = 50, message = "设备版本信息长度不能超过50个字符")
    @Schema(description = "设备版本信息", example = "V2.1.0")
    private String firmwareVersion;

    /**
     * 硬件版本信息
     */
    @Size(max = 50, message = "硬件版本信息长度不能超过50个字符")
    @Schema(description = "硬件版本信息", example = "V1.0.0")
    private String hardwareVersion;

    /**
     * 心跳间隔
     */
    @Schema(description = "心跳间隔（秒）", example = "60")
    private Integer heartbeatInterval;

    /**
     * 是否启用
     */
    @Schema(description = "是否启用 0:禁用 1:启用", example = "1")
    private Integer enabled;

    /**
     * 设备位置
     */
    @Size(max = 200, message = "设备位置长度不能超过200个字符")
    @Schema(description = "设备位置", example = "主楼入口")
    private String deviceLocation;

    /**
     * 安装位置描述
     */
    @Size(max = 200, message = "安装位置描述长度不能超过200个字符")
    @Schema(description = "安装位置描述", example = "主楼入口右侧")
    private String installLocation;

    /**
     * 经度坐标
     */
    @Schema(description = "经度坐标", example = "116.397128")
    private Double longitude;

    /**
     * 纬度坐标
     */
    @Schema(description = "纬度坐标", example = "39.916527")
    private Double latitude;

    /**
     * 设备描述
     */
    @Size(max = 500, message = "设备描述长度不能超过500个字符")
    @Schema(description = "设备描述", example = "主要入口门禁设备")
    private String deviceDesc;

    /**
     * 设备配置
     */
    @Schema(description = "设备配置（JSON格式）", example = "{\"timeout\":30,\"retry\":3}")
    private String deviceConfig;

    /**
     * 设备照片
     */
    @Size(max = 500, message = "设备照片路径长度不能超过500个字符")
    @Schema(description = "设备照片路径", example = "/upload/device/photo_001.jpg")
    private String devicePhoto;

    /**
     * 安装时间
     */
    @Schema(description = "安装时间", example = "2025-01-16T10:00:00")
    private LocalDateTime installTime;

    /**
     * 维护人员
     */
    @Size(max = 50, message = "维护人员长度不能超过50个字符")
    @Schema(description = "维护人员", example = "张三")
    private String maintenancePerson;

    /**
     * 维护联系电话
     */
    @Size(max = 20, message = "维护联系电话长度不能超过20个字符")
    @Schema(description = "维护联系电话", example = "13800138000")
    private String maintenancePhone;

    /**
     * 上次维护时间
     */
    @Schema(description = "上次维护时间", example = "2025-10-16T10:00:00")
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间
     */
    @Schema(description = "下次维护时间", example = "2026-01-16T10:00:00")
    private LocalDateTime nextMaintenanceTime;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "主要入口设备，需要重点关注")
    private String remark;

    /**
     * 授权用户列表（可选）
     */
    @Schema(description = "授权用户ID列表")
    private List<Long> authorizedUserIds;

    /**
     * 权限时间段配置（可选）
     */
    @Schema(description = "权限时间段配置")
    private List<TimeSlotForm> timeSlots;

    /**
     * 时间段配置内部类
     */

    @Schema(description = "时间段配置")
    public static class TimeSlotForm implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "开始时间", example = "08:00")
        private String startTime;

        @Schema(description = "结束时间", example = "18:00")
        private String endTime;

        @Schema(description = "有效星期 逗号分隔 1-7", example = "1,2,3,4,5")
        private String weekdays;

        public TimeSlotForm() {}

        public TimeSlotForm(String startTime, String endTime, String weekdays) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.weekdays = weekdays;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getWeekdays() {
            return weekdays;
        }

        public void setWeekdays(String weekdays) {
            this.weekdays = weekdays;
        }

        /**
         * 验证时间段配置是否有效
         *
         * @return 是否有效
         */
        public boolean isValid() {
            return startTime != null && endTime != null &&
                   isValidTimeFormat(startTime) && isValidTimeFormat(endTime);
        }

        /**
         * 验证时间格式 (HH:mm)
         *
         * @param timeString 时间字符串
         * @return 是否格式正确
         */
        private boolean isValidTimeFormat(String timeString) {
            if (timeString == null) return false;
            try {
                java.time.LocalTime.parse(timeString);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * 获取星期数组
         *
         * @return 星期数组
         */
        public String[] getWeekdayArray() {
            if (weekdays == null || weekdays.trim().isEmpty()) {
                return new String[0];
            }
            return weekdays.split(",");
        }

        /**
         * 获取时间段描述
         *
         * @return 描述信息
         */
        public String getDescription() {
            return String.format("时间段: %s-%s, 有效星期: %s",
                               startTime != null ? startTime : "--:--",
                               endTime != null ? endTime : "--:--",
                               weekdays != null ? weekdays : "未设置");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimeSlotForm that = (TimeSlotForm) o;
            return java.util.Objects.equals(startTime, that.startTime) &&
                   java.util.Objects.equals(endTime, that.endTime) &&
                   java.util.Objects.equals(weekdays, that.weekdays);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(startTime, endTime, weekdays);
        }

        @Override
        public String toString() {
            return "TimeSlotForm{" +
                    "startTime='" + startTime + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", weekdays='" + weekdays + '\'' +
                    '}';
        }
    }
}