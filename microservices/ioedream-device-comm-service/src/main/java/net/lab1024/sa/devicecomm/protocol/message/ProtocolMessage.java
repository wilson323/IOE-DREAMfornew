package net.lab1024.sa.devicecomm.protocol.message;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

/**
 * 协议消息基类
 * <p>
 * 所有设备协议消息的基类，包含通用字段
 * 严格遵循CLAUDE.md规范：
 * - 使用Lombok简化代码
 * - 字段清晰，职责明确
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class ProtocolMessage {

    /**
     * 消息类型
     * <p>
     * 例如：ATTENDANCE_RECORD（考勤记录）、ACCESS_EVENT（门禁事件）、CONSUME_RECORD（消费记录）
     * </p>
     */
    private String messageType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编号
     */
    private String deviceCode;

    /**
     * 消息时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 协议类型
     * <p>
     * 例如："ATTENDANCE_ENTROPY_V4.0"、"ACCESS_ENTROPY_V4.8"、"CONSUME_ZKTECO_V1.0"
     * </p>
     */
    private String protocolType;

    /**
     * 原始数据（十六进制字符串）
     */
    private String rawDataHex;

    /**
     * 原始数据（字节数组）
     */
    private byte[] rawDataBytes;

    /**
     * 消息数据（解析后的业务数据）
     * <p>
     * 存储解析后的业务数据，格式为Map，便于扩展
     * </p>
     */
    private Map<String, Object> data;

    /**
     * 消息状态
     * <p>
     * - PARSED - 已解析
     * - VALIDATED - 已验证
     * - PROCESSED - 已处理
     * - FAILED - 处理失败
     * </p>
     */
    private String status;

    /**
     * 错误信息（处理失败时）
     */
    private String errorMessage;

    /**
     * 错误码（处理失败时）
     */
    private String errorCode;
}

