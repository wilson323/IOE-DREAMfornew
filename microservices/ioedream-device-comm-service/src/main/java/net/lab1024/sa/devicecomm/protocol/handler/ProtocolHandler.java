package net.lab1024.sa.devicecomm.protocol.handler;

import net.lab1024.sa.devicecomm.protocol.message.ProtocolMessage;

/**
 * 设备协议处理器接口
 * <p>
 * 统一协议处理接口，所有设备协议处理器必须实现此接口
 * 严格遵循CLAUDE.md规范：
 * - 接口定义清晰，职责单一
 * - 支持多种设备厂商协议
 * - 异步处理，不阻塞主线程
 * </p>
 * <p>
 * 实现类：
 * - AttendanceProtocolHandler（考勤协议处理器）
 * - AccessProtocolHandler（门禁协议处理器）
 * - ConsumeProtocolHandler（消费协议处理器）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ProtocolHandler {

    /**
     * 获取协议类型
     * <p>
     * 返回协议的唯一标识，用于协议路由和识别
     * </p>
     *
     * @return 协议类型字符串，例如："ATTENDANCE_ENTROPY_V4.0"、"ACCESS_ENTROPY_V4.8"、"CONSUME_ZKTECO_V1.0"
     */
    String getProtocolType();

    /**
     * 获取支持的设备厂商
     * <p>
     * 返回协议支持的设备厂商名称
     * </p>
     *
     * @return 厂商名称，例如："熵基科技"、"中控智慧"
     */
    String getManufacturer();

    /**
     * 获取协议版本
     * <p>
     * 返回协议版本号
     * </p>
     *
     * @return 版本号，例如："V4.0"、"V4.8"、"V1.0"
     */
    String getVersion();

    /**
     * 解析协议消息
     * <p>
     * 将设备推送的原始数据解析为协议消息对象
     * 支持字节流和字符串两种格式
     * </p>
     *
     * @param rawData 原始数据（字节数组或字符串）
     * @return 解析后的协议消息对象
     * @throws ProtocolParseException 当数据格式不正确或解析失败时抛出
     */
    ProtocolMessage parseMessage(byte[] rawData) throws ProtocolParseException;

    /**
     * 解析协议消息（字符串格式）
     * <p>
     * 将字符串格式的协议数据解析为协议消息对象
     * </p>
     *
     * @param rawData 原始数据字符串
     * @return 解析后的协议消息对象
     * @throws ProtocolParseException 当数据格式不正确或解析失败时抛出
     */
    ProtocolMessage parseMessage(String rawData) throws ProtocolParseException;

    /**
     * 验证协议消息
     * <p>
     * 验证消息的完整性和合法性，包括：
     * - 消息头验证
     * - 校验和验证
     * - 数据长度验证
     * - 消息类型验证
     * </p>
     *
     * @param message 协议消息对象
     * @return true-验证通过，false-验证失败
     */
    boolean validateMessage(ProtocolMessage message);

    /**
     * 处理协议消息
     * <p>
     * 处理解析后的协议消息，执行相应的业务逻辑
     * 此方法应该是异步的，不阻塞调用线程
     * </p>
     *
     * @param message 协议消息对象
     * @param deviceId 设备ID
     * @throws ProtocolProcessException 当消息处理失败时抛出
     */
    void processMessage(ProtocolMessage message, Long deviceId) throws ProtocolProcessException;

    /**
     * 构建响应消息
     * <p>
     * 根据请求消息构建响应消息
     * </p>
     *
     * @param requestMessage 请求消息
     * @param success 是否成功
     * @param errorCode 错误码（失败时）
     * @param errorMessage 错误信息（失败时）
     * @return 响应消息字节数组
     */
    byte[] buildResponse(ProtocolMessage requestMessage, boolean success, String errorCode, String errorMessage);
}

