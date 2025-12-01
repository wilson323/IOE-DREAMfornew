package net.lab1024.sa.admin.module.smart.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁通行事件实体
 * <p>
 * 记录完整的通行事件信息，包括用户、设备、时间、结果、照片等
 * 支持高效查询和统计分析
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@EqualsAndHashCode(callSuper = false) // 通行事件实体独立使用，不继承审计字段
@TableName("t_smart_access_event")
public class AccessEventEntity {

    /**
     * 事件ID（主键）
     */
    @TableId(value = "event_id", type = IdType.AUTO)
    private Long eventId;

    /**
     * 事件编号（系统生成唯一编号）
     */
    @NotNull(message = "事件编号不能为空")
    @Size(max = 64, message = "事件编号长度不能超过64个字符")
    private String eventNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户工号
     */
    @Size(max = 50, message = "用户工号长度不能超过50个字符")
    private String userCode;

    /**
     * 用户姓名
     */
    @Size(max = 100, message = "用户姓名长度不能超过100个字符")
    private String userName;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String departmentName;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    /**
     * 设备编码
     */
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String deviceName;

    /**
     * 区域ID
     */
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    /**
     * 区域名称
     */
    @Size(max = 100, message = "区域名称长度不能超过100个字符")
    private String areaName;

    /**
     * 验证方式（1:刷卡 2:密码 3:指纹 4:人脸 5:二维码 6:IC卡+密码 7:其他）
     */
    @NotNull(message = "验证方式不能为空")
    private Integer verifyMethod;

    /**
     * 验证结果（0:成功 1:失败 2:超时 3:取消 4:其他）
     */
    @NotNull(message = "验证结果不能为空")
    private Integer verifyResult;

    /**
     * 失败原因
     */
    @Size(max = 200, message = "失败原因长度不能超过200个字符")
    private String failReason;

    /**
     * 卡号
     */
    @Size(max = 50, message = "卡号长度不能超过50个字符")
    private String cardNo;

    /**
     * 生物特征ID
     */
    private Long biometricId;

    /**
     * 指纹模板ID
     */
    @Size(max = 50, message = "指纹模板ID长度不能超过50个字符")
    private String fingerprintId;

    /**
     * 人脸特征ID
     */
    @Size(max = 50, message = "人脸特征ID长度不能超过50个字符")
    private String faceId;

    /**
     * 通行方向（0:进入 1:外出 2:未知）
     */
    private Integer direction;

    /**
     * 通行时间
     */
    @NotNull(message = "通行时间不能为空")
    private LocalDateTime eventTime;

    /**
     * 门锁动作时间（门锁实际打开时间）
     */
    private LocalDateTime doorOpenTime;

    /**
     * 门锁关闭时间
     */
    private LocalDateTime doorCloseTime;

    /**
     * 通行时长（秒）
     */
    private Integer duration;

    /**
     * 是否开门成功（0:失败 1:成功）
     */
    private Integer doorOpened;

    /**
     * 拍照路径（通行时拍摄的照片）
     */
    @Size(max = 500, message = "拍照路径长度不能超过500个字符")
    private String photoPath;

    /**
     * 拍照缩略图路径
     */
    @Size(max = 500, message = "拍照缩略图路径长度不能超过500个字符")
    private String thumbnailPath;

    /**
     * 温度检测值（摄氏度）
     */
    private Double temperature;

    /**
     * 是否体温异常（0:正常 1:异常）
     */
    private Integer temperatureAbnormal;

    /**
     * 口罩佩戴状态（0:未佩戴 1:佩戴 2:未检测）
     */
    private Integer maskStatus;

    /**
     * 访客ID（临时访客记录）
     */
    private Long visitorId;

    /**
     * 访客姓名
     */
    @Size(max = 100, message = "访客姓名长度不能超过100个字符")
    private String visitorName;

    /**
     * 访客证件号
     */
    @Size(max = 50, message = "访客证件号长度不能超过50个字符")
    private String visitorIdCard;

    /**
     * 被访人ID
     */
    private Long visiteeId;

    /**
     * 被访人姓名
     */
    @Size(max = 100, message = "被访人姓名长度不能超过100个字符")
    private String visiteeName;

    /**
     * 有效期开始时间
     */
    private LocalDateTime validStartTime;

    /**
     * 有效期结束时间
     */
    private LocalDateTime validEndTime;

    /**
     * 事件级别（0:普通 1:重要 2:紧急 3:异常）
     */
    private Integer eventLevel;

    /**
     * 是否黑名单事件（0:否 1:是）
     */
    private Integer blacklistEvent;

    /**
     * 报警类型（0:无报警 1:非法闯入 2:尾随 3:胁迫 4:其他）
     */
    private Integer alarmType;

    /**
     * 报警描述
     */
    @Size(max = 200, message = "报警描述长度不能超过200个字符")
    private String alarmDesc;

    /**
     * 处理状态（0:未处理 1:已处理 2:忽略）
     */
    private Integer handleStatus;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理人姓名
     */
    @Size(max = 100, message = "处理人姓名长度不能超过100个字符")
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    private String handleRemark;

    /**
     * 数据来源（0:设备上报 1:手动录入 2:第三方系统 3:其他）
     */
    private Integer dataSource;

    /**
     * 第三方系统ID
     */
    @Size(max = 100, message = "第三方系统ID长度不能超过100个字符")
    private String thirdPartyId;

    /**
     * 原始报文（设备发送的原始数据）
     */
    @Size(max = 2000, message = "原始报文长度不能超过2000个字符")
    private String rawMessage;

    /**
     * 备注信息
     */
    @Size(max = 500, message = "备注信息长度不能超过500个字符")
    private String remark;

    // =================== 非数据库字段，用于展示 ===================

    /**
     * 验证方式名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String verifyMethodName;

    /**
     * 验证结果名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String verifyResultName;

    /**
     * 事件级别名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String eventLevelName;

    /**
     * 是否需要处理（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private Boolean needHandle;
}