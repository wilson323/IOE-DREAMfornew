package net.lab1024.sa.device.protocol;

import net.lab1024.sa.device.domain.entity.AccessDeviceEntity;

/**
 * 门禁设备协议适配器接口 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 统一接口设计，支持多协议切换
 * - 超时控制和重试机制
 * - 统一异常处理和日志记录
 * - 支持所有门禁设备协议操作
 * <p>
 * 支持的协议类型：
 * - TCP: TCP/IP协议
 * - HTTP: HTTP/HTTPS协议
 * - MQTT: MQTT物联网协议
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
public interface DeviceProtocolAdapter {

    /**
     * 协议类型枚举
     */
    enum ProtocolType {
        /**
         * TCP/IP协议
         */
        TCP("TCP协议"),

        /**
         * HTTP/HTTPS协议
         */
        HTTP("HTTP协议"),

        /**
         * MQTT物联网协议
         */
        MQTT("MQTT协议");

        private final String description;

        ProtocolType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据字符串获取协议类型
         *
         * @param protocol 协议字符串
         * @return 协议类型，如果未找到返回TCP
         */
        public static ProtocolType fromString(String protocol) {
            if (protocol == null || protocol.isEmpty()) {
                return TCP;
            }
            String upperProtocol = protocol.toUpperCase();
            for (ProtocolType type : values()) {
                if (type.name().equals(upperProtocol)) {
                    return type;
                }
            }
            return TCP; // 默认返回TCP
        }
    }

    /**
     * 获取协议类型
     *
     * @return 协议类型
     */
    ProtocolType getProtocolType();

    /**
     * 获取支持的厂商列表
     *
     * @return 支持的厂商列表
     */
    java.util.List<String> getSupportedManufacturers();

    /**
     * 远程开门
     * <p>
     * 向设备发送远程开门指令
     *
     * @param device 门禁设备实体
     * @return 是否成功
     * @throws DeviceProtocolException 协议调用异常
     */
    boolean remoteOpen(AccessDeviceEntity device) throws DeviceProtocolException;

    /**
     * 重启设备
     * <p>
     * 向设备发送重启指令
     *
     * @param device 门禁设备实体
     * @return 是否成功
     * @throws DeviceProtocolException 协议调用异常
     */
    boolean restartDevice(AccessDeviceEntity device) throws DeviceProtocolException;

    /**
     * 同步设备时间
     * <p>
     * 将服务器时间同步到设备
     *
     * @param device 门禁设备实体
     * @return 是否成功
     * @throws DeviceProtocolException 协议调用异常
     */
    boolean syncDeviceTime(AccessDeviceEntity device) throws DeviceProtocolException;

    /**
     * 检查设备连接状态
     * <p>
     * 检查设备是否在线
     *
     * @param device 门禁设备实体
     * @return 是否在线
     * @throws DeviceProtocolException 协议调用异常
     */
    boolean checkConnection(AccessDeviceEntity device) throws DeviceProtocolException;

    /**
     * 获取设备状态信息
     * <p>
     * 获取设备的当前状态信息
     *
     * @param device 门禁设备实体
     * @return 设备状态信息
     * @throws DeviceProtocolException 协议调用异常
     */
    java.util.Map<String, Object> getDeviceStatus(AccessDeviceEntity device) throws DeviceProtocolException;

    /**
     * 发送自定义命令
     * <p>
     * 向设备发送自定义控制命令
     *
     * @param device   门禁设备实体
     * @param command  命令内容
     * @param params   命令参数
     * @return 命令执行结果
     * @throws DeviceProtocolException 协议调用异常
     */
    java.util.Map<String, Object> sendCustomCommand(AccessDeviceEntity device, String command, java.util.Map<String, Object> params) throws DeviceProtocolException;
}