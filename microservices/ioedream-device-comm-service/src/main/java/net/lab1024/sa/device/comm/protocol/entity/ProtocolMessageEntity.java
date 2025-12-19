package net.lab1024.sa.device.comm.protocol.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协议消息基础实体
 * <p>
 * 存储所有设备通讯协议的原始消息和解析结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@TableName("t_protocol_message")
public class ProtocolMessageEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long messageId;

    /**
     * 设备ID
     */
    @TableField("device_id")
    private Long deviceId;

    /**
     * 设备编码
     */
    @TableField("device_code")
    private String deviceCode;

    /**
     * 协议类型
     * ACCESS_ENTROPY_V4_8, ATTENDANCE_ENTROPY_V4_0, CONSUME_ZKTECO_V1_0
     */
    @TableField("protocol_type")
    private String protocolType;

    /**
     * 消息方向
     * UP-设备上传, DOWN-平台下发
     */
    @TableField("message_direction")
    private String messageDirection;

    /**
     * 消息类型
     * 协议中定义的消息类型代码
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 命令代码
     * 具体的业务命令代码
     */
    @TableField("command_code")
    private String commandCode;

    /**
     * 原始消息数据（十六进制字符串）
     */
    @TableField("raw_hex_data")
    private String rawHexData;

    /**
     * 原始消息数据（字节数组Base64编码）
     */
    @TableField("raw_byte_data")
    private String rawByteData;

    /**
     * 解析后的业务数据（JSON格式）
     */
    @TableField("business_data")
    private String businessData;

    /**
     * 消息状态
     * PENDING-待处理, PROCESSED-已处理, FAILED-处理失败, IGNORED-已忽略
     */
    @TableField("message_status")
    private String messageStatus;

    /**
     * 处理结果
     * SUCCESS-成功, FAILED-失败, PARTIAL-部分成功
     */
    @TableField("process_result")
    private String processResult;

    /**
     * 错误代码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 处理耗时（毫秒）
     */
    @TableField("process_duration")
    private Long processDuration;

    /**
     * 消息序号（用于消息去重和排序）
     */
    @TableField("sequence_number")
    private Long sequenceNumber;

    /**
     * 会话ID（用于关联请求和响应）
     */
    @TableField("session_id")
    private String sessionId;

    /**
     * 验证签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 校验码
     */
    @TableField("checksum")
    private String checksum;

    /**
     * 消息优先级
     * HIGH-高, MEDIUM-中, LOW-低
     */
    @TableField("priority")
    private String priority;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    private LocalDateTime nextRetryTime;

    /**
     * 消息接收时间
     */
    @TableField("receive_time")
    private LocalDateTime receiveTime;

    /**
     * 消息处理时间
     */
    @TableField("process_time")
    private LocalDateTime processTime;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记
     */
    @TableField("deleted_flag")
    private Boolean deletedFlag;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
