package net.lab1024.sa.devicecomm.protocol.message;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
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

    /**
     * 序列号（协议消息序列号）
     */
    private Integer sequenceNumber;

    /**
     * 获取用户ID
     * <p>
     * 从data字段中获取用户ID
     * </p>
     *
     * @return 用户ID，如果不存在返回null
     */
    public Long getUserId() {
        if (data != null && data.containsKey("userId")) {
            Object userId = data.get("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            } else if (userId instanceof Integer) {
                return ((Integer) userId).longValue();
            } else if (userId instanceof String) {
                try {
                    return Long.parseLong((String) userId);
                } catch (NumberFormatException e) {
                    log.debug("[协议消息] 用户ID解析失败，返回null: userId={}", userId);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 获取序列号
     *
     * @return 序列号
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * 设置序列号
     *
     * @param sequenceNumber 序列号
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * 获取属性值
     *
     * @param key 属性键
     * @return 属性值，如果不存在返回null
     */
    public Object getAttribute(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }

    /**
     * 添加属性
     *
     * @param key 属性键
     * @param value 属性值
     */
    public void addAttribute(String key, Object value) {
        if (data == null) {
            data = new java.util.HashMap<>();
        }
        data.put(key, value);
    }

    /**
     * 获取消息长度
     *
     * @return 消息长度
     */
    public int getMessageLength() {
        return rawDataBytes != null ? rawDataBytes.length : 0;
    }

    /**
     * 设置用户ID（通过data字段）
     *
     * @param userId 用户ID
     */
    public void setUserId(Long userId) {
        addAttribute("userId", userId);
    }

    /**
     * 设置用户ID（通过data字段）
     *
     * @param userId 用户ID
     */
    public void setUserId(long userId) {
        addAttribute("userId", userId);
    }

    /**
     * 设置用户ID（通过data字段）
     *
     * @param userId 用户ID（字符串）
     */
    public void setUserId(String userId) {
        addAttribute("userId", userId);
    }

    /**
     * 设置特征数据（通过data字段）
     *
     * @param featureData 特征数据
     */
    public void setFeatureData(byte[] featureData) {
        addAttribute("featureData", featureData);
    }

    /**
     * 设置匹配分数（通过data字段）
     *
     * @param score 匹配分数
     */
    public void setScore(double score) {
        addAttribute("score", score);
    }

    /**
     * 设置验证类型（通过data字段）
     *
     * @param verifyType 验证类型
     */
    public void setVerifyType(Object verifyType) {
        addAttribute("verifyType", verifyType);
    }
}

