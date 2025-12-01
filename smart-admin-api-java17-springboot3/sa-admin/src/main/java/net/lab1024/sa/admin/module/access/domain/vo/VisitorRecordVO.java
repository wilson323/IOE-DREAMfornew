package net.lab1024.sa.admin.module.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客记录视图对象
 * <p>
 * 严格遵循repowiki规范：
 * - 完整的字段定义
 * - 清晰的注释说明
 * - 标准的命名规范
 * - Swagger文档注解
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
@Schema(description = "访客记录视图对象")
public class VisitorRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID")
    private Long recordId;

    /**
     * 预约ID
     */
    @Schema(description = "预约ID")
    private Long reservationId;

    /**
     * 预约编号
     */
    @Schema(description = "预约编号")
    private String reservationCode;

    /**
     * 访客姓名
     */
    @Schema(description = "访客姓名")
    private String visitorName;

    /**
     * 访客手机号
     */
    @Schema(description = "访客手机号")
    private String visitorPhone;

    /**
     * 访客身份证号
     */
    @Schema(description = "访客身份证号")
    private String visitorIdCard;

    /**
     * 访客照片
     */
    @Schema(description = "访客照片")
    private String visitorPhoto;

    /**
     * 访问区域ID
     */
    @Schema(description = "访问区域ID")
    private Long visitAreaId;

    /**
     * 访问区域名称
     */
    @Schema(description = "访问区域名称")
    private String visitAreaName;

    /**
     * 访问设备ID
     */
    @Schema(description = "访问设备ID")
    private Long deviceId;

    /**
     * 访问设备名称
     */
    @Schema(description = "访问设备名称")
    private String deviceName;

    /**
     * 访问时间
     */
    @Schema(description = "访问时间")
    private LocalDateTime accessTime;

    /**
     * 通行方式
     */
    @Schema(description = "通行方式")
    private Integer accessMethod;

    /**
     * 通行方式名称
     */
    @Schema(description = "通行方式名称")
    private String accessMethodName;

    /**
     * 通行结果
     */
    @Schema(description = "通行结果")
    private Integer accessResult;

    /**
     * 通行结果名称
     */
    @Schema(description = "通行结果名称")
    private String accessResultName;

    /**
     * 通行结果描述
     */
    @Schema(description = "通行结果描述")
    private String accessResultDesc;

    /**
     * 接待人用户ID
     */
    @Schema(description = "接待人用户ID")
    private Long hostUserId;

    /**
     * 接待人姓名
     */
    @Schema(description = "接待人姓名")
    private String hostUserName;

    /**
     * 接待部门
     */
    @Schema(description = "接待部门")
    private String hostDepartment;

    /**
     * 进入时间
     */
    @Schema(description = "进入时间")
    private LocalDateTime entryTime;

    /**
     * 离开时间
     */
    @Schema(description = "离开时间")
    private LocalDateTime exitTime;

    /**
     * 停留时长（分钟）
     */
    @Schema(description = "停留时长（分钟）")
    private Integer stayDuration;

    /**
     * 访问目的
     */
    @Schema(description = "访问目的")
    private String visitPurpose;

    /**
     * 访问备注
     */
    @Schema(description = "访问备注")
    private String visitRemarks;

    /**
     * 体温检测结果
     */
    @Schema(description = "体温检测结果")
    private Double bodyTemperature;

    /**
     * 健康码状态
     */
    @Schema(description = "健康码状态")
    private Integer healthCodeStatus;

    /**
     * 健康码状态名称
     */
    @Schema(description = "健康码状态名称")
    private String healthCodeStatusName;

    /**
     * 安检状态
     */
    @Schema(description = "安检状态")
    private Integer securityCheckStatus;

    /**
     * 安检状态名称
     */
    @Schema(description = "安检状态名称")
    private String securityCheckStatusName;

    /**
     * 安检备注
     */
    @Schema(description = "安检备注")
    private String securityCheckRemarks;

    /**
     * 随行人员数量
     */
    @Schema(description = "随行人员数量")
    private Integer companionCount;

    /**
     * 随行人员信息
     */
    @Schema(description = "随行人员信息")
    private String companionInfo;

    /**
     * 物品登记信息
     */
    @Schema(description = "物品登记信息")
    private String baggageInfo;

    /**
     * 车辆信息
     */
    @Schema(description = "车辆信息")
    private String vehicleInfo;

    /**
     * 访客类型
     */
    @Schema(description = "访客类型")
    private Integer visitorType;

    /**
     * 访客类型名称
     */
    @Schema(description = "访客类型名称")
    private String visitorTypeName;

    /**
     * 紧急程度
     */
    @Schema(description = "紧急程度")
    private Integer urgencyLevel;

    /**
     * 紧急程度名称
     */
    @Schema(description = "紧急程度名称")
    private String urgencyLevelName;

    /**
     * 访问类型
     */
    @Schema(description = "访问类型")
    private Integer visitType;

    /**
     * 访问类型名称
     */
    @Schema(description = "访问类型名称")
    private String visitTypeName;

    /**
     * 通行权限级别
     */
    @Schema(description = "通行权限级别")
    private Integer accessLevel;

    /**
     * 通行权限级别名称
     */
    @Schema(description = "通行权限级别名称")
    private String accessLevelName;

    /**
     * 验证方式
     */
    @Schema(description = "验证方式")
    private Integer verificationMethod;

    /**
     * 验证方式名称
     */
    @Schema(description = "验证方式名称")
    private String verificationMethodName;

    /**
     * 验证人员ID
     */
    @Schema(description = "验证人员ID")
    private Long verifierId;

    /**
     * 验证人员姓名
     */
    @Schema(description = "验证人员姓名")
    private String verifierName;

    /**
     * 验证时间
     */
    @Schema(description = "验证时间")
    private LocalDateTime verifyTime;

    /**
     * 验证结果
     */
    @Schema(description = "验证结果")
    private String verifyResult;

    /**
     * 异常类型
     */
    @Schema(description = "异常类型")
    private Integer exceptionType;

    /**
     * 异常类型名称
     */
    @Schema(description = "异常类型名称")
    private String exceptionTypeName;

    /**
     * 异常描述
     */
    @Schema(description = "异常描述")
    private String exceptionDesc;

    /**
     * 处理状态
     */
    @Schema(description = "处理状态")
    private Integer handleStatus;

    /**
     * 处理状态名称
     */
    @Schema(description = "处理状态名称")
    private String handleStatusName;

    /**
     * 处理人员ID
     */
    @Schema(description = "处理人员ID")
    private Long handlerId;

    /**
     * 处理人员姓名
     */
    @Schema(description = "处理人员姓名")
    private String handlerName;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    /**
     * 处理结果
     */
    @Schema(description = "处理结果")
    private String handleResult;

    /**
     * 图片证据
     */
    @Schema(description = "图片证据")
    private String imageEvidence;

    /**
     * 视频证据路径
     */
    @Schema(description = "视频证据路径")
    private String videoEvidence;

    /**
     * 访客评价
     */
    @Schema(description = "访客评价")
    private Integer visitorRating;

    /**
     * 访客评价名称
     */
    @Schema(description = "访客评价名称")
    private String visitorRatingName;

    /**
     * 访客反馈
     */
    @Schema(description = "访客反馈")
    private String visitorFeedback;

    /**
     * 反馈时间
     */
    @Schema(description = "反馈时间")
    private LocalDateTime feedbackTime;

    /**
     * 数据来源
     */
    @Schema(description = "数据来源")
    private Integer dataSource;

    /**
     * 数据来源名称
     */
    @Schema(description = "数据来源名称")
    private String dataSourceName;

    /**
     * 同步状态
     */
    @Schema(description = "同步状态")
    private Integer syncStatus;

    /**
     * 同步状态名称
     */
    @Schema(description = "同步状态名称")
    private String syncStatusName;

    /**
     * 同步时间
     */
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    /**
     * 地理位置
     */
    @Schema(description = "地理位置")
    private String location;

    /**
     * IP地址
     */
    @Schema(description = "IP地址")
    private String ipAddress;

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号")
    private String deviceSerial;

    /**
     * 访客签名图片
     */
    @Schema(description = "访客签名图片")
    private String visitorSignature;

    /**
     * 接待人签名图片
     */
    @Schema(description = "接待人签名图片")
    private String hostSignature;

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
     * 创建人姓名
     */
    @Schema(description = "创建人姓名")
    private String createUserName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID")
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名")
    private String updateUserName;
}