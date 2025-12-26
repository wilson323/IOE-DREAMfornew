package net.lab1024.sa.access.domain.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁报警视图对象
 * <p>
 * 用于实时监控模块显示报警信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "门禁报警视图对象")
public class AccessAlarmVO {

    /**
     * 报警ID
     */
    @Schema(description = "报警ID")
    private Long alarmId;

    /**
     * 报警类型
     */
    @Schema(description = "报警类型: DEVICE_OFFLINE-设备离线, ILLEGAL_ENTRY-非法闯入, DOOR_TIMEOUT-门超时未关, FORCED_ENTRY-强力破门, DURESS_ALARM-胁迫报警, CARD_ABNORMAL-卡片异常")
    private String alarmType;

    /**
     * 报警类型名称
     */
    @Schema(description = "报警类型名称")
    private String alarmTypeName;

    /**
     * 报警级别
     */
    @Schema(description = "报警级别: 1-低级, 2-中级, 3-高级, 4-紧急, 5-严重")
    private Integer alarmLevel;

    /**
     * 报警级别名称
     */
    @Schema(description = "报警级别名称")
    private String alarmLevelName;

    // ==================== 设备信息 ====================

    /**
     * 设备ID
     */
    @Schema(description = "设备ID")
    private String deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

    // ==================== 区域信息 ====================

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;

    // ==================== 用户信息 ====================

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String userName;

    /**
     * 通行记录ID
     */
    @Schema(description = "通行记录ID")
    private Long accessRecordId;

    // ==================== 报警信息 ====================

    /**
     * 报警时间
     */
    @Schema(description = "报警时间")
    private LocalDateTime alarmTime;

    /**
     * 报警日期
     */
    @Schema(description = "报警日期")
    private LocalDate alarmDate;

    /**
     * 报警描述
     */
    @Schema(description = "报警描述")
    private String alarmDescription;

    /**
     * 报警详情
     */
    @Schema(description = "报警详情（JSON格式）")
    private String alarmDetail;

    /**
     * 报警图片URL
     */
    @Schema(description = "报警图片URL")
    private String alarmImageUrl;

    /**
     * 报警视频URL
     */
    @Schema(description = "报警视频URL")
    private String alarmVideoUrl;

    // ==================== 处理信息 ====================

    /**
     * 处理状态
     */
    @Schema(description = "处理状态: 0-未处理, 1-处理中, 2-已处理, 3-已忽略")
    private Integer processStatus;

    /**
     * 处理状态名称
     */
    @Schema(description = "处理状态名称")
    private String processStatusName;

    /**
     * 是否已处理
     */
    @Schema(description = "是否已处理")
    private Boolean processed;

    /**
     * 是否已处理名称
     */
    @Schema(description = "是否已处理名称")
    private String processedName;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID")
    private Long handlerId;

    /**
     * 处理人名称
     */
    @Schema(description = "处理人名称")
    private String handlerName;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注")
    private String handleRemark;

    /**
     * 处理结果
     */
    @Schema(description = "处理结果")
    private String handleResult;

    // ==================== 确认信息 ====================

    /**
     * 是否已确认
     */
    @Schema(description = "是否已确认")
    private Boolean confirmed;

    /**
     * 确认人ID
     */
    @Schema(description = "确认人ID")
    private Long confirmerId;

    /**
     * 确认时间
     */
    @Schema(description = "确认时间")
    private LocalDateTime confirmTime;

    // ==================== 通知信息 ====================

    /**
     * 是否已发送通知
     */
    @Schema(description = "是否已发送通知")
    private Boolean notificationSent;

    /**
     * 通知发送时间
     */
    @Schema(description = "通知发送时间")
    private LocalDateTime notificationTime;

    /**
     * 通知接收人列表
     */
    @Schema(description = "通知接收人列表（JSON格式）")
    private String notificationRecipients;

    // ==================== 报警源和状态 ====================

    /**
     * 报警源
     */
    @Schema(description = "报警源: SYSTEM-系统自动检测, MANUAL-人工上报, DEVICE-设备上报")
    private String alarmSource;

    /**
     * 报警状态
     */
    @Schema(description = "报警状态: ACTIVE-活跃, ACKNOWLEDGED-已确认, PROCESSING-处理中, RESOLVED-已解决, CLOSED-已关闭, IGNORED-已忽略")
    private String alarmStatus;

    /**
     * 报警状态名称
     */
    @Schema(description = "报警状态名称")
    private String alarmStatusName;

    /**
     * 优先级
     */
    @Schema(description = "优先级（用于排序）")
    private Integer priority;

    // ==================== 重复报警信息 ====================

    /**
     * 重复次数
     */
    @Schema(description = "重复次数")
    private Integer repeatCount;

    /**
     * 首次报警时间
     */
    @Schema(description = "首次报警时间")
    private LocalDateTime firstAlarmTime;

    /**
     * 最后报警时间
     */
    @Schema(description = "最后报警时间")
    private LocalDateTime lastAlarmTime;

    // ==================== 扩展属性 ====================

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性（JSON格式）")
    private String extendedAttributes;

    // ==================== 审计信息 ====================

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updateUserId;
}
