package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 门禁设备状态VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁设备状态VO")
public class AccessDeviceStatusVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态", example = "true")
    private Boolean online;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "NORMAL")
    private String deviceStatus;

    /**
     * 最后心跳时间
     */
    @Schema(description = "最后心跳时间", example = "2025-11-25T10:30:00")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 最后通信时间
     */
    @Schema(description = "最后通信时间", example = "2025-11-25T10:30:00")
    private LocalDateTime lastCommTime;

    /**
     * 设备版本
     */
    @Schema(description = "设备版本", example = "V2.1.0")
    private String firmwareVersion;

    /**
     * 信号强度
     */
    @Schema(description = "信号强度", example = "85")
    private Integer signalStrength;

    /**
     * 电池电量（如果是无线设备）
     */
    @Schema(description = "电池电量（%）", example = "75")
    private Integer batteryLevel;

    /**
     * 门磁状态
     */
    @Schema(description = "门磁状态", example = "CLOSED")
    private String doorSensorStatus;

    /**
     * 锁状态
     */
    @Schema(description = "锁状态", example = "LOCKED")
    private String lockStatus;

    /**
     * 工作温度
     */
    @Schema(description = "工作温度（℃）", example = "25.5")
    private Double temperature;

    /**
     * 运行时长
     */
    @Schema(description = "运行时长（小时）", example = "168")
    private Long runningHours;

    /**
     * 错误代码
     */
    @Schema(description = "错误代码", example = "0")
    private String errorCode;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息", example = "无错误")
    private String errorMessage;

    // Getters and Setters
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public LocalDateTime getLastHeartbeatTime() {
        return lastHeartbeatTime;
    }

    public void setLastHeartbeatTime(LocalDateTime lastHeartbeatTime) {
        this.lastHeartbeatTime = lastHeartbeatTime;
    }

    public LocalDateTime getLastCommTime() {
        return lastCommTime;
    }

    public void setLastCommTime(LocalDateTime lastCommTime) {
        this.lastCommTime = lastCommTime;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public Integer getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getDoorSensorStatus() {
        return doorSensorStatus;
    }

    public void setDoorSensorStatus(String doorSensorStatus) {
        this.doorSensorStatus = doorSensorStatus;
    }

    public String getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(String lockStatus) {
        this.lockStatus = lockStatus;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Long getRunningHours() {
        return runningHours;
    }

    public void setRunningHours(Long runningHours) {
        this.runningHours = runningHours;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}