package net.lab1024.sa.device.comm.protocol.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 协议消息对象
 * <p>
 * 表示设备通讯协议中的标准消息格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolMessage {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 命令代码
     */
    private String commandCode;

    /**
     * 业务数据
     */
    private Map<String, Object> businessData;

    /**
     * 原始数据(字节数组转十六进制)
     */
    private String rawDataHex;

    /**
     * 序列号
     */
    private String sequenceNumber;

    /**
     * 校验码
     */
    private String checksum;

    /**
     * 消息时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 消息方向 (INBOUND:设备上报, OUTBOUND:平台下发)
     */
    private String direction;

    /**
     * 消息状态 (PENDING, PROCESSING, PROCESSED, FAILED)
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 附加属性
     */
    private Map<String, Object> attributes;
}
