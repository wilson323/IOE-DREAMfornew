package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * 门禁告警事件VO
 * 基于现有监控系统增强的门禁专用告警事件类
 *
 * @author SmartAdmin Team
 * @since 2025-11-25
 */
@Schema(description = "门禁告警事件VO")
public class AlertEventVO {

    /**
     * 告警ID
     */
    @Schema(description = "告警ID", example = "1001")
    private Long alertId;

    /**
     * 告警编号
     */
    @Schema(description = "告警编号", example = "AL20251125001")
    private String alertCode;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主门禁设备")
    private String deviceName;

    /**
     * 设备位置
     */
    @Schema(description = "设备位置", example = "主入口")
    private String deviceLocation;

    /**
     * 告警级别
     * INFO-信息
     * WARNING-警告
     * ERROR-错误
     * CRITICAL-严重
     * EMERGENCY-紧急
     */
    @Schema(description = "告警级别", example = "WARNING")
    private String alertLevel;

    /**
     * 告警类型
     * DEVICE_OFFLINE-设备离线
     * DEVICE_FAULT-设备故障
     * ACCESS_DENIED-拒绝访问
     * FORCED_ENTRY-强制进入
     * DOOR_FORCED-门被强制
     * DOOR_OPEN_TOO_LONG-门开启过长
     * TAILGATING-尾随进入
     * DUPLICATE_ENTRY-重复进入
     * INVALID_CARD-无效卡
     * EXPIRED_CARD-过期卡
     * AREA_OVERFLOW-区域超容
     * POWER_FAILURE-断电
     * NETWORK_ERROR-网络故障
     * TAMPER_DETECTED-防拆触发
     */
    @Schema(description = "告警类型", example = "ACCESS_DENIED")
    private String alertType;

    /**
     * 告警标题
     */
    @Schema(description = "告警标题", example = "无效卡尝试访问")
    private String alertTitle;

    /**
     * 告警消息
     */
    @Schema(description = "告警消息", example = "用户张三使用无效卡尝试进入办公区域")
    private String alertMessage;

    /**
     * 告警详情
     */
    @Schema(description = "告警详情", example = "卡号：1234567890，读取失败原因：卡片未注册")
    private String alertDetail;

    /**
     * 告警来源
     * DEVICE-设备
     * SYSTEM-系统
     * MANUAL-手动
     * INTEGRATION-集成系统
     */
    @Schema(description = "告警来源", example = "DEVICE")
    private String alertSource;

    /**
     * 告警状态
     * ACTIVE-活跃
     * HANDLED-已处理
     * IGNORED-已忽略
     * RESOLVED-已解决
     */
    @Schema(description = "告警状态", example = "ACTIVE")
    private String alertStatus;

    /**
     * 处理状态
     * UNHANDLED-未处理
     * HANDLING-处理中
     * HANDLED-已处理
     * ESCALATED-已升级
     */
    @Schema(description = "处理状态", example = "UNHANDLED")
    private String handleStatus;

    /**
     * 紧急程度
     * LOW-低
     * MEDIUM-中
     * HIGH-高
     * URGENT-紧急
     */
    @Schema(description = "紧急程度", example = "MEDIUM")
    private String urgencyLevel;

    /**
     * 影响范围
     * LOCAL-局部
     * AREA-区域
     * GLOBAL-全局
     */
    @Schema(description = "影响范围", example = "AREA")
    private String impactScope;

    /**
     * 相关人员ID
     */
    @Schema(description = "相关人员ID", example = "12345")
    private Long relatedPersonId;

    /**
     * 相关人员姓名
     */
    @Schema(description = "相关人员姓名", example = "张三")
    private String relatedPersonName;

    /**
     * 相关人员类型
     */
    @Schema(description = "相关人员类型", example = "EMPLOYEE")
    private String relatedPersonType;

    /**
     * 卡号/凭证号
     */
    @Schema(description = "卡号/凭证号", example = "1234567890")
    private String credentialNumber;

    /**
     * 访问位置
     */
    @Schema(description = "访问位置", example = "办公区域A")
    private String accessLocation;

    /**
     * 告警发生时间
     */
    @Schema(description = "告警发生时间", example = "2025-11-25T10:30:00")
    private LocalDateTime alertTime;

    /**
     * 告警检测时间
     */
    @Schema(description = "告警检测时间", example = "2025-11-25T10:30:05")
    private LocalDateTime detectedTime;

    /**
     * 首次处理时间
     */
    @Schema(description = "首次处理时间", example = "2025-11-25T10:35:00")
    private LocalDateTime firstHandleTime;

    /**
     * 解决时间
     */
    @Schema(description = "解决时间", example = "2025-11-25T10:45:00")
    private LocalDateTime resolvedTime;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "10001")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名", example = "安保员李四")
    private String handlerName;

    /**
     * 处理结果
     */
    @Schema(description = "处理结果", example = "确认无误，已记录在案")
    private String handleResult;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注", example = "该人员为新员工，卡片尚未录入系统")
    private String handleRemarks;

    /**
     * 是否需要人工确认
     */
    @Schema(description = "是否需要人工确认", example = "true")
    private Boolean needManualConfirm;

    /**
     * 是否自动升级
     */
    @Schema(description = "是否自动升级", example = "true")
    private Boolean autoEscalate;

    /**
     * 升级条件（分钟）
     */
    @Schema(description = "升级条件（分钟）", example = "30")
    private Integer escalateMinutes;

    /**
     * 升级级别
     */
    @Schema(description = "升级级别", example = "2")
    private Integer escalateLevel;

    /**
     * 是否已推送通知
     */
    @Schema(description = "是否已推送通知", example = "true")
    private Boolean notificationSent;

    /**
     * 通知方式
     */
    @Schema(description = "通知方式", example = "SMS,EMAIL,WECHAT")
    private String notificationMethods;

    /**
     * 通知人员列表
     */
    @Schema(description = "通知人员列表")
    private List<Long> notifiedPersonIds;

    /**
     * 关联图片/视频URL
     */
    @Schema(description = "关联图片/视频URL", example = "http://example.com/aler1.jpg")
    private String mediaUrl;

    /**
     * 关联记录ID
     */
    @Schema(description = "关联记录ID", example = "5001")
    private Long relatedRecordId;

    /**
     * 关联记录类型
     */
    @Schema(description = "关联记录类型", example = "ACCESS_RECORD")
    private String relatedRecordType;

    /**
     * 附加数据
     */
    @Schema(description = "附加数据")
    private Map<String, Object> additionalData;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表", example = "[\"可疑行为\",\"需要关注\"]")
    private List<String> tags;

    /**
     * 处理历史
     */
    @Schema(description = "处理历史")
    private List<AlertHandleHistoryVO> handleHistory;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-11-25T10:30:05")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2025-11-25T10:45:00")
    private LocalDateTime updateTime;

    /**
     * 版本号
     */
    @Schema(description = "版本号", example = "1")
    private Integer version;

    // 内部类：处理历史
    @Schema(description = "告警处理历史")
    public static class AlertHandleHistoryVO {
        @Schema(description = "处理步骤ID", example = "1")
        private Integer stepId;

        @Schema(description = "处理时间", example = "2025-11-25T10:35:00")
        private LocalDateTime handleTime;

        @Schema(description = "处理人ID", example = "10001")
        private Long handlerId;

        @Schema(description = "处理人姓名", example = "安保员李四")
        private String handlerName;

        @Schema(description = "处理动作", example = "CONFIRM")
        private String handleAction;

        @Schema(description = "处理结果", example = "确认告警有效")
        private String handleResult;

        @Schema(description = "处理备注", example = "已联系相关部门处理")
        private String remarks;

        @Schema(description = "处理前状态", example = "ACTIVE")
        private String beforeStatus;

        @Schema(description = "处理后状态", example = "HANDLED")
        private String afterStatus;

        // Getters and Setters
        public Integer getStepId() { return stepId; }
        public void setStepId(Integer stepId) { this.stepId = stepId; }

        public LocalDateTime getHandleTime() { return handleTime; }
        public void setHandleTime(LocalDateTime handleTime) { this.handleTime = handleTime; }

        public Long getHandlerId() { return handlerId; }
        public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }

        public String getHandlerName() { return handlerName; }
        public void setHandlerName(String handlerName) { this.handlerName = handlerName; }

        public String getHandleAction() { return handleAction; }
        public void setHandleAction(String handleAction) { this.handleAction = handleAction; }

        public String getHandleResult() { return handleResult; }
        public void setHandleResult(String handleResult) { this.handleResult = handleResult; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        public String getBeforeStatus() { return beforeStatus; }
        public void setBeforeStatus(String beforeStatus) { this.beforeStatus = beforeStatus; }

        public String getAfterStatus() { return afterStatus; }
        public void setAfterStatus(String afterStatus) { this.afterStatus = afterStatus; }
    }

    // 主要类的Getters and Setters
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public String getAlertCode() { return alertCode; }
    public void setAlertCode(String alertCode) { this.alertCode = alertCode; }

    public Long getDeviceId() { return deviceId; }
    public void setDeviceId(Long deviceId) { this.deviceId = deviceId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceLocation() { return deviceLocation; }
    public void setDeviceLocation(String deviceLocation) { this.deviceLocation = deviceLocation; }

    public String getAlertLevel() { return alertLevel; }
    public void setAlertLevel(String alertLevel) { this.alertLevel = alertLevel; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getAlertTitle() { return alertTitle; }
    public void setAlertTitle(String alertTitle) { this.alertTitle = alertTitle; }

    public String getAlertMessage() { return alertMessage; }
    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }

    public String getAlertDetail() { return alertDetail; }
    public void setAlertDetail(String alertDetail) { this.alertDetail = alertDetail; }

    public String getAlertSource() { return alertSource; }
    public void setAlertSource(String alertSource) { this.alertSource = alertSource; }

    public String getAlertStatus() { return alertStatus; }
    public void setAlertStatus(String alertStatus) { this.alertStatus = alertStatus; }

    public String getHandleStatus() { return handleStatus; }
    public void setHandleStatus(String handleStatus) { this.handleStatus = handleStatus; }

    public String getUrgencyLevel() { return urgencyLevel; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }

    public String getImpactScope() { return impactScope; }
    public void setImpactScope(String impactScope) { this.impactScope = impactScope; }

    public Long getRelatedPersonId() { return relatedPersonId; }
    public void setRelatedPersonId(Long relatedPersonId) { this.relatedPersonId = relatedPersonId; }

    public String getRelatedPersonName() { return relatedPersonName; }
    public void setRelatedPersonName(String relatedPersonName) { this.relatedPersonName = relatedPersonName; }

    public String getRelatedPersonType() { return relatedPersonType; }
    public void setRelatedPersonType(String relatedPersonType) { this.relatedPersonType = relatedPersonType; }

    public String getCredentialNumber() { return credentialNumber; }
    public void setCredentialNumber(String credentialNumber) { this.credentialNumber = credentialNumber; }

    public String getAccessLocation() { return accessLocation; }
    public void setAccessLocation(String accessLocation) { this.accessLocation = accessLocation; }

    public LocalDateTime getAlertTime() { return alertTime; }
    public void setAlertTime(LocalDateTime alertTime) { this.alertTime = alertTime; }

    public LocalDateTime getDetectedTime() { return detectedTime; }
    public void setDetectedTime(LocalDateTime detectedTime) { this.detectedTime = detectedTime; }

    public LocalDateTime getFirstHandleTime() { return firstHandleTime; }
    public void setFirstHandleTime(LocalDateTime firstHandleTime) { this.firstHandleTime = firstHandleTime; }

    public LocalDateTime getResolvedTime() { return resolvedTime; }
    public void setResolvedTime(LocalDateTime resolvedTime) { this.resolvedTime = resolvedTime; }

    public Long getHandlerId() { return handlerId; }
    public void setHandlerId(Long handlerId) { this.handlerId = handlerId; }

    public String getHandlerName() { return handlerName; }
    public void setHandlerName(String handlerName) { this.handlerName = handlerName; }

    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }

    public String getHandleRemarks() { return handleRemarks; }
    public void setHandleRemarks(String handleRemarks) { this.handleRemarks = handleRemarks; }

    public Boolean getNeedManualConfirm() { return needManualConfirm; }
    public void setNeedManualConfirm(Boolean needManualConfirm) { this.needManualConfirm = needManualConfirm; }

    public Boolean getAutoEscalate() { return autoEscalate; }
    public void setAutoEscalate(Boolean autoEscalate) { this.autoEscalate = autoEscalate; }

    public Integer getEscalateMinutes() { return escalateMinutes; }
    public void setEscalateMinutes(Integer escalateMinutes) { this.escalateMinutes = escalateMinutes; }

    public Integer getEscalateLevel() { return escalateLevel; }
    public void setEscalateLevel(Integer escalateLevel) { this.escalateLevel = escalateLevel; }

    public Boolean getNotificationSent() { return notificationSent; }
    public void setNotificationSent(Boolean notificationSent) { this.notificationSent = notificationSent; }

    public String getNotificationMethods() { return notificationMethods; }
    public void setNotificationMethods(String notificationMethods) { this.notificationMethods = notificationMethods; }

    public List<Long> getNotifiedPersonIds() { return notifiedPersonIds; }
    public void setNotifiedPersonIds(List<Long> notifiedPersonIds) { this.notifiedPersonIds = notifiedPersonIds; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public Long getRelatedRecordId() { return relatedRecordId; }
    public void setRelatedRecordId(Long relatedRecordId) { this.relatedRecordId = relatedRecordId; }

    public String getRelatedRecordType() { return relatedRecordType; }
    public void setRelatedRecordType(String relatedRecordType) { this.relatedRecordType = relatedRecordType; }

    public Map<String, Object> getAdditionalData() { return additionalData; }
    public void setAdditionalData(Map<String, Object> additionalData) { this.additionalData = additionalData; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<AlertHandleHistoryVO> getHandleHistory() { return handleHistory; }
    public void setHandleHistory(List<AlertHandleHistoryVO> handleHistory) { this.handleHistory = handleHistory; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}