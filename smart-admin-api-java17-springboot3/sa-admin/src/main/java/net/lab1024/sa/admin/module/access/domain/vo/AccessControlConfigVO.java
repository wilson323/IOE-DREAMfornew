package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 门禁控制配置VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁控制配置VO")
public class AccessControlConfigVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 门禁模式
     */
    @Schema(description = "门禁模式", example = "CARD_PASSWORD")
    private String accessMode;

    /**
     * 开门方式
     */
    @Schema(description = "开门方式", example = "REMOTE")
    private String openMode;

    /**
     * 锁门方式
     */
    @Schema(description = "锁门方式", example = "AUTO")
    private String lockMode;

    /**
     * 验证模式
     */
    @Schema(description = "验证模式", example = "BIOMETRIC_CARD")
    private String verificationMode;

    /**
     * 是否启用反潜回
     */
    @Schema(description = "是否启用反潜回", example = "true")
    private Boolean antiPassbackEnabled;

    /**
     * 是否启用胁迫开门
     */
    @Schema(description = "是否启用胁迫开门", example = "true")
    private Boolean duressOpenEnabled;

    /**
     * 是否启用远程开门
     */
    @Schema(description = "是否启用远程开门", example = "true")
    private Boolean remoteOpenEnabled;

    /**
     * 是否启用时区控制
     */
    @Schema(description = "是否启用时区控制", example = "true")
    private Boolean timeZoneEnabled;

    /**
     * 门禁时区配置
     */
    @Schema(description = "门禁时区配置", example = "WEEKDAY_08:00-18:00")
    private String accessTimeZone;

    /**
     * 开门延时时间（秒）
     */
    @Schema(description = "开门延时时间（秒）", example = "3")
    private Integer openDelayTime;

    /**
     * 有效开门时间（秒）
     */
    @Schema(description = "有效开门时间（秒）", example = "5")
    private Integer validOpenTime;

    /**
     * 最大同时开门人数
     */
    @Schema(description = "最大同时开门人数", example = "10")
    private Integer maxSimultaneousUsers;

    /**
     * 是否启用多次刷卡
     */
    @Schema(description = "是否启用多次刷卡", example = "false")
    private Boolean multiCardEnabled;

    /**
     * 刷卡间隔时间（秒）
     */
    @Schema(description = "刷卡间隔时间（秒）", example = "2")
    private Integer cardIntervalTime;

    // Getters and Setters
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    public String getOpenMode() {
        return openMode;
    }

    public void setOpenMode(String openMode) {
        this.openMode = openMode;
    }

    public String getLockMode() {
        return lockMode;
    }

    public void setLockMode(String lockMode) {
        this.lockMode = lockMode;
    }

    public String getVerificationMode() {
        return verificationMode;
    }

    public void setVerificationMode(String verificationMode) {
        this.verificationMode = verificationMode;
    }

    public Boolean getAntiPassbackEnabled() {
        return antiPassbackEnabled;
    }

    public void setAntiPassbackEnabled(Boolean antiPassbackEnabled) {
        this.antiPassbackEnabled = antiPassbackEnabled;
    }

    public Boolean getDuressOpenEnabled() {
        return duressOpenEnabled;
    }

    public void setDuressOpenEnabled(Boolean duressOpenEnabled) {
        this.duressOpenEnabled = duressOpenEnabled;
    }

    public Boolean getRemoteOpenEnabled() {
        return remoteOpenEnabled;
    }

    public void setRemoteOpenEnabled(Boolean remoteOpenEnabled) {
        this.remoteOpenEnabled = remoteOpenEnabled;
    }

    public Boolean getTimeZoneEnabled() {
        return timeZoneEnabled;
    }

    public void setTimeZoneEnabled(Boolean timeZoneEnabled) {
        this.timeZoneEnabled = timeZoneEnabled;
    }

    public String getAccessTimeZone() {
        return accessTimeZone;
    }

    public void setAccessTimeZone(String accessTimeZone) {
        this.accessTimeZone = accessTimeZone;
    }

    public Integer getOpenDelayTime() {
        return openDelayTime;
    }

    public void setOpenDelayTime(Integer openDelayTime) {
        this.openDelayTime = openDelayTime;
    }

    public Integer getValidOpenTime() {
        return validOpenTime;
    }

    public void setValidOpenTime(Integer validOpenTime) {
        this.validOpenTime = validOpenTime;
    }

    public Integer getMaxSimultaneousUsers() {
        return maxSimultaneousUsers;
    }

    public void setMaxSimultaneousUsers(Integer maxSimultaneousUsers) {
        this.maxSimultaneousUsers = maxSimultaneousUsers;
    }

    public Boolean getMultiCardEnabled() {
        return multiCardEnabled;
    }

    public void setMultiCardEnabled(Boolean multiCardEnabled) {
        this.multiCardEnabled = multiCardEnabled;
    }

    public Integer getCardIntervalTime() {
        return cardIntervalTime;
    }

    public void setCardIntervalTime(Integer cardIntervalTime) {
        this.cardIntervalTime = cardIntervalTime;
    }
}