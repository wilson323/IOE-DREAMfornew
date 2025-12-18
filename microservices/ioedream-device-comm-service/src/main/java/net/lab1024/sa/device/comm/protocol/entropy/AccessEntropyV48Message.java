package net.lab1024.sa.device.comm.protocol.entropy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolMessage;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 熵基科技门禁协议V4.8消息实体
 * <p>
 * 严格按照熵基科技门禁PUSH通讯协议V4.8规范实现
 * 支持实时事件上传、设备状态监控、用户权限验证等功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AccessEntropyV48Message extends ProtocolMessage {

    // ==================== 协议头信息 ====================

    /**
     * 协议版本号
     * 固定值：V4.8
     */
    private String protocolVersion = "V4.8";

    /**
     * 设备唯一标识
     * 16位设备SN码
     */
    private String deviceSn;

    /**
     * 消息类型代码
     * 0x01 - 实时事件上传
     * 0x02 - 设备状态上报
     * 0x03 - 心跳包
     * 0x04 - 权限请求
     * 0x05 - 验证结果
     * 0x06 - 错误报告
     */
    private Integer messageTypeCode;

    /**
     * 消息类型名称
     */
    private String messageTypeName;

    /**
     * 命令代码（Integer格式，用于业务逻辑）
     * 具体业务操作的命令代码
     * 注：继承自ProtocolMessage的commandCode是String类型
     */
    private Integer commandCodeInt;

    /**
     * 消息序号（Long格式）
     * 用于消息去重和排序
     * 注：继承自ProtocolMessage的sequenceNumber是String类型
     */
    private Long sequenceNumberLong;

    /**
     * 时间戳（Long格式）
     * 设备端时间戳（Unix时间戳）
     * 注：继承自ProtocolMessage的timestamp是LocalDateTime类型
     */
    private Long timestampLong;

    /**
     * 会话ID
     * 用于关联请求和响应
     */
    private String sessionId;

    // ==================== 事件相关字段 ====================

    /**
     * 事件类型代码
     * 0x01 - 刷卡事件
     * 0x02 - 人脸识别事件
     * 0x03 - 指纹识别事件
     * 0x04 - 密码验证事件
     * 0x05 - 二维码事件
     * 0x06 - 胁迫事件
     * 0x07 - 尾随事件
     * 0x08 - 反潜回事件
     * 0x09 - 门磁状态事件
     * 0x0A - 报警事件
     */
    private Integer eventTypeCode;

    /**
     * 事件类型名称
     */
    private String eventTypeName;

    /**
     * 事件编号
     * 设备端事件唯一编号
     */
    private String eventNumber;

    /**
     * 用户ID
     * 触发事件用户的ID
     */
    private Long userId;

    /**
     * 用户卡号
     * 刷卡事件的卡号
     */
    private String cardNumber;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 验证方式
     * CARD-刷卡, FACE-人脸, FINGER-指纹, PASSWORD-密码, QR-二维码
     */
    private String verifyMethod;

    /**
     * 验证结果
     * SUCCESS-成功, FAILED-失败, TIMEOUT-超时, INVALID-无效
     */
    private String verifyResult;

    // ==================== 设备相关字段 ====================

    /**
     * 设备类型
     * ACCESS_CONTROLLER-门禁控制器, CARD_READER-读卡器,
     * FACE_RECOGNIZER-人脸识别终端, FINGERPRINT_READER-指纹识别终端
     */
    private String deviceType;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 设备固件版本
     */
    private String firmwareVersion;

    /**
     * 设备IP地址
     */
    private String deviceIpAddress;

    /**
     * 设备MAC地址
     */
    private String deviceMacAddress;

    /**
     * 门磁状态
     * OPEN-开门, CLOSE-关门, UNKNOWN-未知
     */
    private String doorStatus;

    /**
     * 锁状态
     * LOCKED-锁定, UNLOCKED-解锁, FAULT-故障
     */
    private String lockStatus;

    // ==================== 生物识别相关字段 ====================

    /**
     * 人脸特征数据
     * Base64编码的人脸特征值
     */
    private String faceFeatureData;

    /**
     * 人脸置信度
     * 0-100，表示人脸识别的置信度
     */
    private Float faceConfidence;

    /**
     * 活体检测结果
     * REAL-真人, PHOTO-照片, VIDEO-视频, MASK-面具
     */
    private String livenessResult;

    /**
     * 活体置信度
     * 0-100，表示活体检测的置信度
     */
    private Float livenessConfidence;

    /**
     * 指纹特征数据
     * Base64编码的指纹特征值
     */
    private String fingerprintFeatureData;

    /**
     * 指纹匹配分数
     * 0-100，表示指纹匹配的分数
     */
    private Integer fingerprintMatchScore;

    // ==================== 位置和时间信息 ====================

    /**
     * 门禁点ID
     */
    private Long accessPointId;

    /**
     * 门禁点名称
     */
    private String accessPointName;

    /**
     * 门禁点类型
     * ENTRANCE-入口, EXIT-出口, MAIN-主门, SIDE-侧门, BACK-后门
     */
    private String accessPointType;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 通行时间
     * 事件发生的具体时间
     */
    private LocalDateTime accessTime;

    /**
     * 通行方向
     * IN-进入, OUT-离开, UNKNOWN-未知
     */
    private String accessDirection;

    // ==================== 权限和安全相关字段 ====================

    /**
     * 权限组ID
     */
    private Long permissionGroupId;

    /**
     * 权限组名称
     */
    private String permissionGroupName;

    /**
     * 有效开始时间
     */
    private LocalDateTime validStartTime;

    /**
     * 有效结束时间
     */
    private LocalDateTime validEndTime;

    /**
     * 访问级别
     * NORMAL-普通, VIP-贵宾, SECURITY-安保, ADMIN-管理员
     */
    private String accessLevel;

    /**
     * 是否反潜回
     */
    private Boolean antiPassbackEnabled;

    /**
     * 是否需要多因素认证
     */
    private Boolean multiFactorRequired;

    // ==================== 异常和报警相关字段 ====================

    /**
     * 异常代码
     * 权限异常、设备异常、网络异常等
     */
    private Integer exceptionCode;

    /**
     * 异常描述
     */
    private String exceptionDescription;

    /**
     * 报警级别
     * LOW-低, MEDIUM-中, HIGH-高, CRITICAL-紧急
     */
    private String alarmLevel;

    /**
     * 报警类型
     * UNAUTHORIZED-未授权, FORCED-强行闯入, TAMPER-防拆,
     * DOOR_OPEN_TOO_LONG-门开超时, DUPLICATE_CARD-重复刷卡
     */
    private String alarmType;

    /**
     *胁迫码
     * 胁迫事件中使用的特殊代码
     */
    private String duressCode;

    /**
     *胁迫处理方式
     * SILENT_ALARM-静默报警, FAKE_ACCEPT-假意接受, IMMEDIATE_ALERT-立即报警
     */
    private String duressHandling;

    // ==================== 扩展字段 ====================

    /**
     * 自定义数据字段（JSON格式）
     * 用于存储协议扩展和厂商自定义字段
     */
    private String customData;

    /**
     * 签名数据
     * 用于消息完整性验证
     */
    private String signature;

    /**
     * 校验码
     * 用于数据完整性校验
     */
    private String checksum;

    /**
     * 协议扩展标志
     * 标识是否包含扩展字段
     */
    private Boolean hasExtendedFields;

    /**
     * 扩展字段数量
     */
    private Integer extendedFieldCount;

    /**
     * 扩展字段数据（Map格式）
     */
    private Map<String, Object> extendedFields;

    // ==================== 处理相关字段 ====================

    /**
     * 消息接收时间
     */
    private LocalDateTime receiveTime;

    /**
     * 消息处理时间
     */
    private LocalDateTime processTime;

    /**
     * 处理状态
     * PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败
     */
    private String processStatus;

    /**
     * 处理结果描述
     */
    private String processResult;

    /**
     * 处理耗时（毫秒）
     */
    private Long processDuration;

    /**
     * 响应消息ID
     * 用于关联请求和响应
     */
    private String responseMessageId;

    /**
     * 是否需要重试
     */
    private Boolean needRetry;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;
}
