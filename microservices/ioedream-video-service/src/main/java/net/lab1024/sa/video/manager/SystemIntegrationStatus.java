package net.lab1024.sa.video.manager;

import java.time.LocalDateTime;

/**
 * 系统集成状态
 * <p>
 * 用于表示视频系统与其他系统的集成状态
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
public class SystemIntegrationStatus {

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * 系统类型
     */
    private String systemType;

    /**
     * 连接状态
     */
    private Boolean connected;

    /**
     * 最后检查时间
     */
    private LocalDateTime lastCheckTime;

    /**
     * 响应时间(毫秒)
     */
    private Long responseTime;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 系统版本
     */
    private String version;

    /**
     * 状态描述
     */
    private String statusDescription;

    public SystemIntegrationStatus() {
    }

    public SystemIntegrationStatus(String systemName, String systemType, Boolean connected) {
        this.systemName = systemName;
        this.systemType = systemType;
        this.connected = connected;
        this.lastCheckTime = LocalDateTime.now();
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public LocalDateTime getLastCheckTime() {
        return lastCheckTime;
    }

    public void setLastCheckTime(LocalDateTime lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        return "SystemIntegrationStatus{" +
                "systemName='" + systemName + '\'' +
                ", systemType='" + systemType + '\'' +
                ", connected=" + connected +
                ", lastCheckTime=" + lastCheckTime +
                ", responseTime=" + responseTime +
                ", errorMessage='" + errorMessage + '\'' +
                ", version='" + version + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                '}';
    }
}