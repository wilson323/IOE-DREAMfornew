package net.lab1024.sa.devicecomm.protocol;

import net.lab1024.sa.common.organization.entity.DeviceEntity;

/**
 * 设备协议适配器接口
 * <p>
 * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
 * 使用策略模式实现不同的设备协议适配器：
 * - RS485协议适配器
 * - TCP协议适配器
 * - HTTP协议适配器
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface IDeviceProtocolAdapter {

    /**
     * 获取协议类型
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * </p>
     *
     * @return 协议类型（RS485、TCP、HTTP等）
     */
    String getProtocolType();

    /**
     * 发送设备指令
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 根据设备信息和指令内容发送设备指令
     * </p>
     *
     * @param device 设备实体
     * @param command 指令内容
     * @return 执行结果
     */
    DeviceCommandResult sendCommand(DeviceEntity device, String command);

    /**
     * 接收设备数据
     * <p>
     * 严格遵循ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md文档要求
     * 接收设备上传的数据
     * </p>
     *
     * @param device 设备实体
     * @param data 数据内容
     * @return 解析结果
     */
    DeviceDataResult receiveData(DeviceEntity device, String data);

    /**
     * 获取适配器优先级
     * <p>
     * 用于策略工厂排序，优先级高的适配器优先使用
     * </p>
     *
     * @return 优先级（数字越大优先级越高）
     */
    default int getPriority() {
        return 100;
    }

    /**
     * 设备指令执行结果
     */
    class DeviceCommandResult {
        private final boolean success;
        private final String message;
        private final String response;

        private DeviceCommandResult(boolean success, String message, String response) {
            this.success = success;
            this.message = message;
            this.response = response;
        }

        public static DeviceCommandResult success(String response) {
            return new DeviceCommandResult(true, "指令执行成功", response);
        }

        public static DeviceCommandResult failed(String message) {
            return new DeviceCommandResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getResponse() {
            return response;
        }
    }

    /**
     * 设备数据接收结果
     */
    class DeviceDataResult {
        private final boolean success;
        private final String message;
        private final Object parsedData;

        private DeviceDataResult(boolean success, String message, Object parsedData) {
            this.success = success;
            this.message = message;
            this.parsedData = parsedData;
        }

        public static DeviceDataResult success(Object parsedData) {
            return new DeviceDataResult(true, "数据解析成功", parsedData);
        }

        public static DeviceDataResult failed(String message) {
            return new DeviceDataResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Object getParsedData() {
            return parsedData;
        }
    }
}
