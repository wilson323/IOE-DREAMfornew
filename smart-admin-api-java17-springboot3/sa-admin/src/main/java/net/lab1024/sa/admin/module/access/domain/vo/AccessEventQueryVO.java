package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 门禁通行事件查询VO
 * <p>
 * 用于前端查询和展示通行事件记录
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */

@Schema(description = "门禁通行事件查询VO")
public class AccessEventQueryVO {

    /**
     * 事件ID
     */
    @Schema(description = "事件ID", example = "1")
    private Long eventId;

    /**
     * 事件编号
     */
    @Schema(description = "事件编号", example = "AE202511160001")
    private String eventNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户工号
     */
    @Schema(description = "用户工号", example = "EMP001")
    private String userCode;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    /**
     * 部门ID
     */
    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Schema(description = "设备编码", example = "DEV_001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "主门禁机")
    private String deviceName;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "主园区")
    private String areaName;

    /**
     * 验证方式
     */
    @Schema(description = "验证方式", example = "4")
    private Integer verifyMethod;

    /**
     * 验证方式名称
     */
    @Schema(description = "验证方式名称", example = "人脸识别")
    private String verifyMethodName;

    /**
     * 验证结果
     */
    @Schema(description = "验证结果", example = "0")
    private Integer verifyResult;

    /**
     * 验证结果名称
     */
    @Schema(description = "验证结果名称", example = "成功")
    private String verifyResultName;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因", example = "")
    private String failReason;

    /**
     * 卡号
     */
    @Schema(description = "卡号", example = "1234567890")
    private String cardNo;

    /**
     * 生物特征ID
     */
    @Schema(description = "生物特征ID", example = "1001")
    private Long biometricId;

    /**
     * 指纹模板ID
     */
    @Schema(description = "指纹模板ID", example = "FP001")
    private String fingerprintId;

    /**
     * 人脸特征ID
     */
    @Schema(description = "人脸特征ID", example = "FACE001")
    private String faceId;

    /**
     * 通行方向
     */
    @Schema(description = "通行方向", example = "0")
    private Integer direction;

    /**
     * 通行方向名称
     */
    @Schema(description = "通行方向名称", example = "进入")
    private String directionName;

    /**
     * 通行时间
     */
    @Schema(description = "通行时间", example = "2025-11-16T09:30:00")
    private LocalDateTime eventTime;

    /**
     * 门锁动作时间
     */
    @Schema(description = "门锁动作时间", example = "2025-11-16T09:30:01")
    private LocalDateTime doorOpenTime;

    /**
     * 门锁关闭时间
     */
    @Schema(description = "门锁关闭时间", example = "2025-11-16T09:30:05")
    private LocalDateTime doorCloseTime;

    /**
     * 通行时长
     */
    @Schema(description = "通行时长（秒）", example = "4")
    private Integer duration;

    /**
     * 是否开门成功
     */
    @Schema(description = "是否开门成功", example = "1")
    private Integer doorOpened;

    /**
     * 拍照路径
     */
    @Schema(description = "拍照路径", example = "/upload/access/20251116/1001.jpg")
    private String photoPath;

    /**
     * 拍照缩略图路径
     */
    @Schema(description = "拍照缩略图路径", example = "/upload/access/20251116/thumb_1001.jpg")
    private String thumbnailPath;

    /**
     * 温度检测值
     */
    @Schema(description = "温度检测值（摄氏度）", example = "36.5")
    private Double temperature;

    /**
     * 是否体温异常
     */
    @Schema(description = "是否体温异常", example = "0")
    private Integer temperatureAbnormal;

    /**
     * 口罩佩戴状态
     */
    @Schema(description = "口罩佩戴状态", example = "1")
    private Integer maskStatus;

    /**
     * 口罩佩戴状态名称
     */
    @Schema(description = "口罩佩戴状态名称", example = "已佩戴")
    private String maskStatusName;

    /**
     * 访客ID
     */
    @Schema(description = "访客ID", example = "0")
    private Long visitorId;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名", example = "")
    private String visitorName;

    /**
     * 访客证件号
     */
    @Schema(description = "访客证件号", example = "")
    private String visitorIdCard;

    /**
     * 被访人ID
     */
    @Schema(description = "被访人ID", example = "0")
    private Long visiteeId;

    /**
     * 被访人姓名
     */
    @Schema(description = "被访人姓名", example = "")
    private String visiteeName;

    /**
     * 事件级别
     */
    @Schema(description = "事件级别", example = "0")
    private Integer eventLevel;

    /**
     * 事件级别名称
     */
    @Schema(description = "事件级别名称", example = "普通")
    private String eventLevelName;

    /**
     * 是否黑名单事件
     */
    @Schema(description = "是否黑名单事件", example = "0")
    private Integer blacklistEvent;

    /**
     * 报警类型
     */
    @Schema(description = "报警类型", example = "0")
    private Integer alarmType;

    /**
     * 报警类型名称
     */
    @Schema(description = "报警类型名称", example = "无报警")
    private String alarmTypeName;

    /**
     * 报警描述
     */
    @Schema(description = "报警描述", example = "")
    private String alarmDesc;

    /**
     * 处理状态
     */
    @Schema(description = "处理状态", example = "1")
    private Integer handleStatus;

    /**
     * 处理状态名称
     */
    @Schema(description = "处理状态名称", example = "已处理")
    private String handleStatusName;

    /**
     * 处理人ID
     */
    @Schema(description = "处理人ID", example = "1")
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @Schema(description = "处理人姓名", example = "管理员")
    private String handlerName;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间", example = "2025-11-16T09:35:00")
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @Schema(description = "处理备注", example = "已确认处理")
    private String handleRemark;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源", example = "0")
    private Integer dataSource;

    /**
     * 数据来源名称
     */
    @Schema(description = "数据来源名称", example = "设备上报")
    private String dataSourceName;

    /**
     * 第三方系统ID
     */
    @Schema(description = "第三方系统ID", example = "")
    private String thirdPartyId;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息", example = "")
    private String remark;

    /**
     * 是否需要处理
     */
    @Schema(description = "是否需要处理", example = "false")
    private Boolean needHandle;

    /**
     * 事件图标（前端展示用）
     */
    @Schema(description = "事件图标", example = "success")
    private String eventIcon;

    /**
     * 事件颜色（前端展示用）
     */
    @Schema(description = "事件颜色", example = "#52c41a")
    private String eventColor;
}