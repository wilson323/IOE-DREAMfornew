package net.lab1024.sa.devicecomm.protocol.handler.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.devicecomm.protocol.enums.VerifyTypeEnum;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolHandler;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolParseException;
import net.lab1024.sa.devicecomm.protocol.handler.ProtocolProcessException;
import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;
import net.lab1024.sa.devicecomm.service.BiometricService;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 生物识别协议处理器
 * <p>
 * 支持多种生物识别设备的协议解析和处理：
 * - 人脸识别设备（熵基科技 V4.8）
 * - 指纹识别设备（中控智慧 V1.0）
 * - 虹膜识别设备（熵基科技 V4.8）
 * - 掌纹识别设备（熵基科技 V4.8）
 * - 指静脉识别设备（熵基科技 V4.8）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class BiometricProtocolHandler implements ProtocolHandler {

    @Resource
    private BiometricService biometricService;

    // 协议头标识（字节数组形式，用于实际协议解析）
    private static final byte[] PROTOCOL_HEADER_ENTROPY_BYTES = {0x50, 0x55, 0x53, 0x48}; // "PUSH"
    private static final byte[] PROTOCOL_HEADER_ZKTECO_BYTES = {0x5A, 0x4B, 0x5F, 0x50, 0x55, 0x53, 0x48}; // "ZK_PUSH"

    // 协议版本
    private static final String ENTROPY_VERSION = "V4.8";

    // 消息类型定义
    private static final String MSG_TYPE_BIOMETRIC_VERIFY = "BIOMETRIC_VERIFY";  // 生物识别验证
    private static final String MSG_TYPE_BIOMETRIC_REGISTER = "BIOMETRIC_REGISTER"; // 生物识别注册
    private static final String MSG_TYPE_BIOMETRIC_DELETE = "BIOMETRIC_DELETE";   // 生物识别删除
    private static final String MSG_TYPE_BIOMETRIC_UPDATE = "BIOMETRIC_UPDATE";   // 生物识别更新

    @Override
    public String getProtocolType() {
        return "BIOMETRIC_ENTROPY_V4.8";
    }

    @Override
    public String getManufacturer() {
        return "熵基科技";
    }

    @Override
    public String getVersion() {
        return ENTROPY_VERSION;
    }

    @Override
    public ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException {
        if (rawData == null || rawData.length < 20) {
            throw new ProtocolParseException("数据长度不足，无法解析协议消息");
        }

        try {
            // 检查协议头
            if (!checkProtocolHeader(rawData)) {
                throw new ProtocolParseException("协议头格式不正确");
            }

            ProtocolMessage message = new ProtocolMessage();

            // 解析协议头
            int offset = parseHeader(rawData, message);

            // 解析消息体
            offset = parseBody(rawData, offset, message);

            // 解析协议尾
            parseFooter(rawData, offset, message);

            log.debug("[生物识别协议] 解析成功: 设备={}, 类型={}, 用户={}",
                    message.getDeviceId(), message.getMessageType(), message.getUserId());

            return message;

        } catch (Exception e) {
            log.error("[生物识别协议] 解析失败: {}", e.getMessage(), e);
            throw new ProtocolParseException("协议解析失败: " + e.getMessage());
        }
    }

    @Override
    public ProtocolMessage parseMessage(String rawData) throws ProtocolParseException {
        if (rawData == null || rawData.trim().isEmpty()) {
            throw new ProtocolParseException("原始数据不能为空");
        }

        try {
            // 尝试JSON格式解析
            if (rawData.trim().startsWith("{")) {
                return parseJsonMessage(rawData);
            }

            // 尝试逗号分隔格式解析
            if (rawData.contains(",")) {
                return parseCommaMessage(rawData);
            }

            // 尝试二进制字符串解析
            return parseMessage(rawData.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            log.error("[生物识别协议] 字符串解析失败: {}", e.getMessage(), e);
            throw new ProtocolParseException("字符串协议解析失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateMessage(ProtocolMessage message) {
        if (message == null) {
            return false;
        }

        try {
            // 验证必要字段
            if (message.getDeviceId() == null || message.getDeviceId() <= 0) {
                log.warn("[生物识别协议] 设备ID无效: {}", message.getDeviceId());
                return false;
            }

            if (message.getMessageType() == null || message.getMessageType().trim().isEmpty()) {
                log.warn("[生物识别协议] 消息类型无效: {}", message.getMessageType());
                return false;
            }

            // 验证生物识别相关字段
            Integer verifyType = (Integer) message.getAttribute("verifyType");
            if (verifyType != null && !isValidVerifyType(verifyType)) {
                log.warn("[生物识别协议] 验证类型无效: {}", verifyType);
                return false;
            }

            // 验证时间戳
            Long timestamp = (Long) message.getAttribute("timestamp");
            if (timestamp != null && timestamp > System.currentTimeMillis() + 300000) { // 5分钟误差
                log.warn("[生物识别协议] 时间戳异常: {}", timestamp);
                return false;
            }

            // 验证校验和
            if (message.getAttribute("checksum") != null) {
                String calculatedChecksum = calculateChecksum(message);
                String receivedChecksum = (String) message.getAttribute("checksum");
                if (!calculatedChecksum.equals(receivedChecksum)) {
                    log.warn("[生物识别协议] 校验和不匹配: 计算={}, 接收={}", calculatedChecksum, receivedChecksum);
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("[生物识别协议] 消息验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void processMessage(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        try {
            log.info("[生物识别协议] 开始处理消息: 设备={}, 类型={}", deviceId, message.getMessageType());

            switch (message.getMessageType()) {
                case MSG_TYPE_BIOMETRIC_VERIFY:
                    processBiometricVerify(message, deviceId);
                    break;
                case MSG_TYPE_BIOMETRIC_REGISTER:
                    processBiometricRegister(message, deviceId);
                    break;
                case MSG_TYPE_BIOMETRIC_DELETE:
                    processBiometricDelete(message, deviceId);
                    break;
                case MSG_TYPE_BIOMETRIC_UPDATE:
                    processBiometricUpdate(message, deviceId);
                    break;
                default:
                    log.warn("[生物识别协议] 未知消息类型: {}", message.getMessageType());
                    throw new ProtocolProcessException("未知消息类型: " + message.getMessageType());
            }

            log.info("[生物识别协议] 消息处理完成: 设备={}, 类型={}", deviceId, message.getMessageType());

        } catch (Exception e) {
            log.error("[生物识别协议] 消息处理失败: 设备={}, 错误={}", deviceId, e.getMessage(), e);
            throw new ProtocolProcessException("消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] buildResponse(ProtocolMessage requestMessage, boolean success, String errorCode, String errorMessage) {
        try {
            ProtocolMessage response = new ProtocolMessage();

            // 复制请求消息的基本信息
            response.setDeviceId(requestMessage.getDeviceId());
            response.setSequenceNumber(requestMessage.getSequenceNumber());

            // 设置响应信息
            response.setMessageType(success ? "0x80" : "0x81"); // 成功/失败响应
            response.addAttribute("success", success);
            response.addAttribute("errorCode", errorCode);
            response.addAttribute("errorMessage", errorMessage);
            response.addAttribute("timestamp", System.currentTimeMillis());

            // 计算校验和
            String checksum = calculateChecksum(response);
            response.addAttribute("checksum", checksum);

            // 构建响应数据
            String responseData = buildResponseString(response);
            return responseData.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("[生物识别协议] 构建响应失败: {}", e.getMessage(), e);
            return "ERROR:BUILD_RESPONSE_FAILED".getBytes(StandardCharsets.UTF_8);
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 检查协议头
     */
    private boolean checkProtocolHeader(byte[] data) {
        if (data.length >= PROTOCOL_HEADER_ENTROPY_BYTES.length) {
            return Arrays.equals(Arrays.copyOfRange(data, 0, PROTOCOL_HEADER_ENTROPY_BYTES.length),
                               PROTOCOL_HEADER_ENTROPY_BYTES) ||
                   Arrays.equals(Arrays.copyOfRange(data, 0, PROTOCOL_HEADER_ZKTECO_BYTES.length),
                               PROTOCOL_HEADER_ZKTECO_BYTES);
        }
        return false;
    }

    /**
     * 解析协议头
     */
    private int parseHeader(byte[] data, ProtocolMessage message) {
        int offset = 0;

        // 协议头标识
        String header = new String(data, offset, 4, StandardCharsets.UTF_8);
        message.addAttribute("protocolHeader", header);
        offset += 4;

        // 设备ID (4字节)
        long deviceId = bytesToLong(data, offset, 4);
        message.setDeviceId(deviceId);
        offset += 4;

        // 消息类型 (1字节)
        int messageType = data[offset] & 0xFF;
        message.setMessageType(String.valueOf(messageType));
        offset += 1;

        // 序列号 (2字节)
        int sequenceNumber = bytesToInt(data, offset, 2);
        message.setSequenceNumber(sequenceNumber);
        offset += 2;

        return offset;
    }

    /**
     * 解析消息体
     */
    private int parseBody(byte[] data, int offset, ProtocolMessage message) {
        // 数据长度 (2字节)
        int dataLength = bytesToInt(data, offset, 2);
        message.addAttribute("dataLength", dataLength);
        offset += 2;

        // 用户ID (4字节)
        long userId = bytesToLong(data, offset, 4);
        message.setUserId(userId);
        offset += 4;

        // 验证类型 (1字节)
        int verifyType = data[offset] & 0xFF;
        message.addAttribute("verifyType", verifyType);
        offset += 1;

        // 验证结果 (1字节)
        int verifyResult = data[offset] & 0xFF;
        message.addAttribute("verifyResult", verifyResult);
        offset += 1;

        // 时间戳 (4字节)
        long timestamp = bytesToLong(data, offset, 4);
        message.addAttribute("timestamp", timestamp);
        offset += 4;

        // 生物特征数据长度 (2字节)
        int bioDataLength = bytesToInt(data, offset, 2);
        message.addAttribute("bioDataLength", bioDataLength);
        offset += 2;

        // 生物特征数据 (变长)
        if (bioDataLength > 0 && offset + bioDataLength <= data.length) {
            byte[] bioData = Arrays.copyOfRange(data, offset, offset + bioDataLength);
            message.addAttribute("bioData", bioData);
            offset += bioDataLength;
        }

        return offset;
    }

    /**
     * 解析协议尾
     */
    private void parseFooter(byte[] data, int offset, ProtocolMessage message) {
        // 校验和 (2字节)
        if (offset + 2 <= data.length) {
            int checksum = bytesToInt(data, offset, 2);
            message.addAttribute("checksum", String.format("%04X", checksum));
        }

        // 结束标识 (2字节)
        if (offset + 4 <= data.length) {
            String endMarker = new String(data, offset + 2, 2, StandardCharsets.UTF_8);
            message.addAttribute("endMarker", endMarker);
        }
    }

    /**
     * 解析JSON格式消息
     */
    private ProtocolMessage parseJsonMessage(String jsonData) throws ProtocolParseException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            HashMap<String, Object> jsonMap = objectMapper.readValue(jsonData, HashMap.class);

            ProtocolMessage message = new ProtocolMessage();
            message.setMessageType(MSG_TYPE_BIOMETRIC_VERIFY);

            // 从JSON中提取关键字段
            if (jsonMap.containsKey("verifyType")) {
                message.setVerifyType(VerifyTypeEnum.fromCode(jsonMap.get("verifyType").toString()));
            }
            if (jsonMap.containsKey("userId")) {
                message.setUserId(jsonMap.get("userId").toString());
            }
            if (jsonMap.containsKey("featureData")) {
                message.setFeatureData(jsonMap.get("featureData").toString().getBytes(StandardCharsets.UTF_8));
            }
            if (jsonMap.containsKey("score")) {
                message.setScore(Double.parseDouble(jsonMap.get("score").toString()));
            }

            return message;

        } catch (JsonProcessingException e) {
            log.error("[生物识别协议] JSON解析失败: {}", e.getMessage(), e);
            throw new ProtocolParseException("JSON格式错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("[生物识别协议] JSON处理异常: {}", e.getMessage(), e);
            throw new ProtocolParseException("JSON处理异常: " + e.getMessage());
        }
    }

    /**
     * 解析逗号分隔格式消息
     */
    private ProtocolMessage parseCommaMessage(String commaData) throws ProtocolParseException {
        String[] fields = commaData.split(",");
        if (fields.length < 6) {
            throw new ProtocolParseException("逗号分隔格式字段不足");
        }

        ProtocolMessage message = new ProtocolMessage();

        try {
            message.setDeviceId(Long.parseLong(fields[0]));
            message.setMessageType(String.valueOf(Integer.parseInt(fields[1])));
            message.setSequenceNumber(Integer.parseInt(fields[2]));
            message.setUserId(Long.parseLong(fields[3]));
            message.addAttribute("verifyType", Integer.parseInt(fields[4]));
            message.addAttribute("verifyResult", Integer.parseInt(fields[5]));
            message.addAttribute("timestamp", System.currentTimeMillis());

        } catch (NumberFormatException e) {
            log.debug("[生物识别协议] 字段格式错误: error={}", e.getMessage());
            throw new ProtocolParseException("字段格式错误: " + e.getMessage());
        }

        return message;
    }

    /**
     * 验证验证类型是否有效
     */
    private boolean isValidVerifyType(int verifyType) {
        return VerifyTypeEnum.fromCode(verifyType) != VerifyTypeEnum.UNKNOWN;
    }

    /**
     * 计算校验和
     */
    private String calculateChecksum(ProtocolMessage message) {
        // 简单的校验和计算
        int sum = 0;
        sum += message.getDeviceId().intValue();
        try {
            sum += Integer.parseInt(message.getMessageType());
        } catch (NumberFormatException e) {
            log.debug("[生物识别协议] 消息类型不是数字格式，校验和计算时使用0: messageType={}", message.getMessageType());
            sum += 0;
        }
        sum += message.getSequenceNumber();
        if (message.getUserId() != null) {
            sum += message.getUserId().intValue();
        }
        return String.format("%04X", sum & 0xFFFF);
    }

    /**
     * 构建响应字符串
     */
    private String buildResponseString(ProtocolMessage message) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESP,");
        sb.append(message.getDeviceId()).append(",");
        sb.append(message.getMessageType()).append(",");
        sb.append(message.getSequenceNumber()).append(",");
        sb.append(message.getAttribute("success")).append(",");
        sb.append(message.getAttribute("errorCode")).append(",");
        sb.append(message.getAttribute("errorMessage")).append(",");
        sb.append(message.getAttribute("timestamp")).append(",");
        sb.append(message.getAttribute("checksum"));
        return sb.toString();
    }

    /**
     * 处理生物识别验证
     */
    private void processBiometricVerify(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        Integer verifyType = (Integer) message.getAttribute("verifyType");
        Integer verifyResult = (Integer) message.getAttribute("verifyResult");
        Long userId = message.getUserId();

        VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.fromCode(verifyType);
        Double score = (Double) message.getAttribute("score");
        byte[] featureData = (byte[]) message.getAttribute("featureData");

        log.info("[生物识别验证] 设备={}, 用户={}, 验证方式={}, 结果={}",
                deviceId, userId, verifyTypeEnum != null ? verifyTypeEnum.getName() : "未知", verifyResult);

        // 调用业务服务处理验证结果
        try {
            biometricService.processBiometricVerify(deviceId, userId, verifyTypeEnum,
                    score != null ? score : 0.0, featureData, verifyResult.toString());
        } catch (Exception e) {
            log.error("[生物识别验证] 处理验证结果失败: 设备={}, 用户={}, 错误={}",
                    deviceId, userId, e.getMessage(), e);
            throw new ProtocolProcessException("验证结果处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理生物识别注册
     */
    private void processBiometricRegister(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        Long userId = message.getUserId();
        Integer verifyType = (Integer) message.getAttribute("verifyType");
        byte[] bioData = (byte[]) message.getAttribute("bioData");

        VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.fromCode(verifyType);

        log.info("[生物识别注册] 设备={}, 用户={}, 验证方式={}, 数据长度={}",
                deviceId, userId, verifyTypeEnum.getName(), bioData != null ? bioData.length : 0);

        // 调用业务服务处理注册
        try {
            biometricService.processBiometricRegister(deviceId, userId, verifyTypeEnum, bioData);
        } catch (Exception e) {
            log.error("[生物识别注册] 处理注册失败: 设备={}, 用户={}, 错误={}",
                    deviceId, userId, e.getMessage(), e);
            throw new ProtocolProcessException("注册处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理生物识别删除
     */
    private void processBiometricDelete(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        Long userId = message.getUserId();
        Integer verifyType = (Integer) message.getAttribute("verifyType");

        VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.fromCode(verifyType);

        log.info("[生物识别删除] 设备={}, 用户={}, 验证方式={}",
                deviceId, userId, verifyTypeEnum.getName());

        // 调用业务服务处理删除
        try {
            biometricService.processBiometricDelete(deviceId, userId, verifyTypeEnum);
        } catch (Exception e) {
            log.error("[生物识别删除] 处理删除失败: 设备={}, 用户={}, 错误={}",
                    deviceId, userId, e.getMessage(), e);
            throw new ProtocolProcessException("删除处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理生物识别更新
     */
    private void processBiometricUpdate(ProtocolMessage message, Long deviceId) throws ProtocolProcessException {
        Long userId = message.getUserId();
        Integer verifyType = (Integer) message.getAttribute("verifyType");
        byte[] bioData = (byte[]) message.getAttribute("bioData");

        VerifyTypeEnum verifyTypeEnum = VerifyTypeEnum.fromCode(verifyType);

        log.info("[生物识别更新] 设备={}, 用户={}, 验证方式={}, 数据长度={}",
                deviceId, userId, verifyTypeEnum.getName(), bioData != null ? bioData.length : 0);

        // 调用业务服务处理更新
        try {
            biometricService.processBiometricUpdate(deviceId, userId, verifyTypeEnum, bioData);
        } catch (Exception e) {
            log.error("[生物识别更新] 处理更新失败: 设备={}, 用户={}, 错误={}",
                    deviceId, userId, e.getMessage(), e);
            throw new ProtocolProcessException("更新处理失败: " + e.getMessage());
        }
    }

    /**
     * 字节数组转长整型
     */
    private long bytesToLong(byte[] bytes, int offset, int length) {
        long result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) | (bytes[offset + i] & 0xFF);
        }
        return result;
    }

    /**
     * 字节数组转整型
     */
    private int bytesToInt(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = (result << 8) | (bytes[offset + i] & 0xFF);
        }
        return result;
    }
}
